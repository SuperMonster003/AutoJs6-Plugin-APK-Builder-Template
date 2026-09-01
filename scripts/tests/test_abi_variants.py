import json
import sys
import tempfile
import unittest
from contextlib import redirect_stdout
from io import StringIO
from pathlib import Path
from types import SimpleNamespace

SCRIPTS_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS_DIR))

import update_compat_matrix as matrix
from verify_runtime_kit import load_rules, parse_host_compatibility, parse_template_variant
from verify_runtime_kit_set import build_manifest


class RuntimeKitVariantTest(unittest.TestCase):

    def setUp(self) -> None:
        self.rules = load_rules()
        self.abis = list(self.rules["templateVariant"]["allowedAbis"])

    def test_explicit_universal_and_specific_variants(self) -> None:
        universal = {"template": {"variant": "inrt-universal", "supportedAbis": self.abis}}
        arm64 = {"template": {"variant": "inrt-arm64-v8a", "supportedAbis": ["arm64-v8a"]}}
        self.assertEqual(("inrt-universal", self.abis), parse_template_variant(universal, self.rules))
        self.assertEqual(("inrt-arm64-v8a", ["arm64-v8a"]), parse_template_variant(arm64, self.rules))

    def test_variant_and_supported_abis_must_match(self) -> None:
        malformed = {"template": {"variant": "inrt-arm64-v8a", "supportedAbis": ["x86_64"]}}
        with self.assertRaises(SystemExit):
            parse_template_variant(malformed, self.rules)

    def test_legacy_missing_metadata_is_local_universal_only(self) -> None:
        legacy = {"template": {}}
        self.assertEqual(
            ("inrt-universal", []),
            parse_template_variant(legacy, self.rules),
        )
        with self.assertRaises(SystemExit):
            parse_template_variant(legacy, self.rules, require_explicit=True)

    def test_complete_set_manifest(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            variants = [("inrt-universal", self.abis)] + [
                (f"inrt-{abi}", [abi]) for abi in self.abis
            ]
            for variant, supported_abis in variants:
                kit_dir = root / variant
                kit_dir.mkdir()
                metadata = kit_metadata(variant, supported_abis)
                (kit_dir / "runtime-kit.json").write_text(json.dumps(metadata), "utf-8")

            manifest = build_manifest(root)
            self.assertEqual([variant for variant, _ in variants], [item["variant"] for item in manifest["variants"]])
            self.assertEqual("universal", manifest["variants"][0]["slug"])

    def test_incomplete_set_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            kit_dir = root / "inrt-universal"
            kit_dir.mkdir()
            metadata = kit_metadata("inrt-universal", self.abis)
            (kit_dir / "runtime-kit.json").write_text(json.dumps(metadata), "utf-8")
            with self.assertRaises(SystemExit):
                build_manifest(root)


class RuntimeKitCompatibilityTest(unittest.TestCase):

    def setUp(self) -> None:
        self.rules = load_rules()

    def test_explicit_patch_range_is_accepted(self) -> None:
        metadata = kit_metadata(
            "inrt-arm64-v8a",
            ["arm64-v8a"],
            min_host_version_code=5276,
            max_host_version_code=5278,
            allow_patch_version_mismatch=True,
        )

        self.assertEqual(
            (5276, 5276, 5278, True),
            parse_host_compatibility(metadata, self.rules, require_explicit=True),
        )

    def test_widened_range_without_permission_is_rejected(self) -> None:
        metadata = kit_metadata(
            "inrt-arm64-v8a",
            ["arm64-v8a"],
            min_host_version_code=5276,
            max_host_version_code=5278,
            allow_patch_version_mismatch=False,
        )

        with self.assertRaises(SystemExit):
            parse_host_compatibility(metadata, self.rules, require_explicit=True)

    def test_legacy_missing_compatibility_is_exact_for_local_use_only(self) -> None:
        metadata = kit_metadata("inrt-arm64-v8a", ["arm64-v8a"])
        metadata.pop("compatibility")

        self.assertEqual(
            (5276, 5276, 5276, False),
            parse_host_compatibility(metadata, self.rules),
        )
        with self.assertRaises(SystemExit):
            parse_host_compatibility(metadata, self.rules, require_explicit=True)


class CompatMatrixAbiArtifactTest(unittest.TestCase):

    def test_add_merges_variants_and_resolve_prefers_exact_abi(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            matrix_path = root / "compat-matrix.json"
            version_properties = root / "version.properties"
            version_properties.write_text(
                "\n".join(
                    (
                        "VERSION_NAME=6.8.0",
                        "VERSION_BUILD=5276",
                        "PLUGIN_VERSION_NAME=1.0.0",
                        "PLUGIN_VERSION_BUILD=1",
                        "PLUGIN_RELEASE_SEQ=1",
                    )
                )
                + "\n",
                "utf-8",
            )
            abis = list(load_rules()["templateVariant"]["allowedAbis"])
            variants = [("inrt-universal", abis)] + [(f"inrt-{abi}", [abi]) for abi in abis]
            for variant, supported_abis in variants:
                kit_dir = root / variant
                kit_dir.mkdir()
                (kit_dir / "runtime-kit.json").write_text(
                    json.dumps(kit_metadata(variant, supported_abis)),
                    "utf-8",
                )
                apk = root / f"plugin-{variant}.apk"
                apk.write_bytes(f"apk-{variant}".encode())
                with redirect_stdout(StringIO()):
                    matrix.cmd_add(
                        SimpleNamespace(
                            runtime_kit=kit_dir,
                            version_properties=version_properties,
                            apk=apk,
                            apk_name=None,
                            released_at="2026-08-30T00:00:00Z",
                            tag="v6.8.0",
                            repo="example/plugin",
                            matrix=matrix_path,
                        )
                    )

            entry = json.loads(matrix_path.read_text("utf-8"))["entries"][0]
            self.assertEqual(len(variants), len(entry["artifacts"]))
            self.assertEqual("plugin-inrt-universal.apk", entry["apkName"])
            self.assertFalse(entry["allowPatchVersionMismatch"])
            self.assertEqual(
                "inrt-arm64-v8a",
                matrix.select_artifact(entry, "arm64-v8a")["variant"],
            )
            without_arm64 = dict(entry)
            without_arm64["artifacts"] = [
                artifact for artifact in entry["artifacts"]
                if artifact["variant"] != "inrt-arm64-v8a"
            ]
            self.assertEqual(
                "inrt-universal",
                matrix.select_artifact(without_arm64, "arm64-v8a")["variant"],
            )

    def test_patch_range_is_written_and_resolves_multiple_hosts(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            matrix_path = root / "compat-matrix.json"
            version_properties = root / "version.properties"
            version_properties.write_text(
                "\n".join(
                    (
                        "VERSION_NAME=6.8.0",
                        "VERSION_BUILD=5276",
                        "PLUGIN_VERSION_NAME=1.0.0",
                        "PLUGIN_VERSION_BUILD=1",
                        "PLUGIN_RELEASE_SEQ=1",
                    )
                )
                + "\n",
                "utf-8",
            )
            kit_dir = root / "kit"
            kit_dir.mkdir()
            abis = list(load_rules()["templateVariant"]["allowedAbis"])
            (kit_dir / "runtime-kit.json").write_text(
                json.dumps(
                    kit_metadata(
                        "inrt-universal",
                        abis,
                        min_host_version_code=5276,
                        max_host_version_code=5278,
                        allow_patch_version_mismatch=True,
                    )
                ),
                "utf-8",
            )
            apk = root / "plugin.apk"
            apk.write_bytes(b"plugin")
            with redirect_stdout(StringIO()):
                matrix.cmd_add(
                    SimpleNamespace(
                        runtime_kit=kit_dir,
                        version_properties=version_properties,
                        apk=apk,
                        apk_name=None,
                        released_at="2026-08-30T00:00:00Z",
                        tag="v6.8.0",
                        repo="example/plugin",
                        matrix=matrix_path,
                    )
                )

            entry = json.loads(matrix_path.read_text("utf-8"))["entries"][0]
            self.assertTrue(entry["allowPatchVersionMismatch"])
            self.assertTrue(matrix.matrix_entry_allows_host(entry, 5276))
            self.assertTrue(matrix.matrix_entry_allows_host(entry, 5277))
            self.assertTrue(matrix.matrix_entry_allows_host(entry, 5278))
            self.assertFalse(matrix.matrix_entry_allows_host(entry, 5279))

            built_for_outside = dict(entry, hostVersionCode=5275)
            self.assertFalse(matrix.matrix_entry_allows_host(built_for_outside, 5277))
            invalid_permission = dict(entry, allowPatchVersionMismatch="true")
            self.assertFalse(matrix.matrix_entry_allows_host(invalid_permission, 5277))


def kit_metadata(
    variant: str,
    supported_abis: list[str],
    *,
    min_host_version_code: int = 5276,
    max_host_version_code: int = 5276,
    allow_patch_version_mismatch: bool = False,
) -> dict:
    return {
        "schemaVersion": 1,
        "runtimeKitId": f"kit-{variant}",
        "host": {
            "packageName": "org.autojs.autojs6",
            "versionName": "6.8.0",
            "versionCode": 5276,
            "gitSha": "test",
        },
        "template": {
            "packageName": "org.autojs.autojs6.inrt",
            "variant": variant,
            "supportedAbis": supported_abis,
        },
        "defaultKeyStore": {"sha256": "a" * 64},
        "contract": {
            "apkBuilderProtocolVersion": 2,
            "remoteBuildProtocolVersion": 3,
            "runtimeApiLevel": 5276,
            "runtimeApiHash": "b" * 64,
            "scriptEngineHash": "c" * 64,
            "resourcesContractHash": "d" * 64,
            "nativeLibManifestHash": variant,
        },
        "compatibility": {
            "minHostVersionCode": min_host_version_code,
            "maxHostVersionCode": max_host_version_code,
            "allowPatchVersionMismatch": allow_patch_version_mismatch,
        },
    }


if __name__ == "__main__":
    unittest.main()
