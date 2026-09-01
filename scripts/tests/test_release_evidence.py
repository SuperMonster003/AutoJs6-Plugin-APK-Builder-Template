import json
import sys
import tempfile
import unittest
import zlib
from pathlib import Path

SCRIPTS_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS_DIR))

from create_release_evidence import build_release_evidence
from update_compat_matrix import ABI_ORDER, VARIANT_ORDER


class ReleaseEvidenceTest(unittest.TestCase):

    def test_five_signed_assets_are_bound_to_runtime_kits(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            manifest, properties, apk_dir = create_fixture(root)

            evidence = build_release_evidence(
                runtime_kits_manifest=manifest,
                version_properties=properties,
                apk_dir=apk_dir,
                repository="example/plugin",
                release_tag="v6.8.0",
                source_repository="example/host",
                source_tag="v6.8.0",
                signer_certificate_sha256="AA:" + "bb:" * 30 + "cc",
                released_at="2026-09-01T00:00:00Z",
            )

            self.assertEqual("1.0.0+autojs6-6.8.0", evidence["plugin"]["compositeVersionName"])
            self.assertEqual(527701, evidence["plugin"]["versionCode"])
            self.assertTrue(evidence["plugin"]["apkBuildEnabledByDefault"])
            self.assertEqual("on-device-plugin", evidence["plugin"]["apkBuildExecutionMode"])
            self.assertFalse(evidence["plugin"]["remoteBuildEnabledByDefault"])
            self.assertEqual(5277, evidence["host"]["minCompatibleVersionCode"])
            self.assertEqual(3, evidence["protocol"]["apkBuildProtocolVersion"])
            self.assertEqual(3, evidence["protocol"]["remoteBuildProtocolVersion"])
            self.assertFalse(evidence["publication"]["candidateOnly"])
            self.assertEqual("github-release", evidence["publication"]["channel"])
            self.assertEqual("https://github.com/example/plugin/releases/tag/v6.8.0", evidence["releaseUrl"])
            self.assertEqual(list(VARIANT_ORDER), [item["variant"] for item in evidence["runtimeKits"]])
            self.assertEqual(5, len(evidence["artifacts"]))
            self.assertTrue(all(len(item["sha256"]) == 64 for item in evidence["artifacts"]))
            self.assertTrue(all(item["fileName"].endswith(f"-{item['crc32']}.apk") for item in evidence["artifacts"]))
            self.assertTrue(all(item["downloadUrl"] for item in evidence["artifacts"]))

    def test_candidate_assets_have_actions_provenance_without_release_urls(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            manifest, properties, apk_dir = create_fixture(root)

            evidence = build_release_evidence(
                runtime_kits_manifest=manifest,
                version_properties=properties,
                apk_dir=apk_dir,
                repository="example/plugin",
                release_tag="v6.8.0-rc1",
                source_repository="example/host",
                source_tag="v6.8.0-rc1",
                signer_certificate_sha256="ab" * 32,
                released_at="2026-09-01T00:00:00Z",
                candidate_only=True,
                workflow_run_url="https://github.com/example/plugin/actions/runs/1001",
                source_workflow_run_url="https://github.com/example/host/actions/runs/1000",
            )

            self.assertTrue(evidence["publication"]["candidateOnly"])
            self.assertEqual("actions-artifact", evidence["publication"]["channel"])
            self.assertEqual("https://github.com/example/plugin/actions/runs/1001", evidence["publication"]["workflowRunUrl"])
            self.assertIsNone(evidence["releaseUrl"])
            self.assertEqual("actions-artifact", evidence["source"]["channel"])
            self.assertEqual("https://github.com/example/host/actions/runs/1000", evidence["source"]["workflowRunUrl"])
            self.assertTrue(all(item["downloadUrl"] is None for item in evidence["artifacts"]))

    def test_candidate_evidence_requires_both_actions_run_urls(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            manifest, properties, apk_dir = create_fixture(root)

            with self.assertRaises(SystemExit):
                build_release_evidence(
                    runtime_kits_manifest=manifest,
                    version_properties=properties,
                    apk_dir=apk_dir,
                    repository="example/plugin",
                    release_tag="v6.8.0-rc1",
                    source_repository="example/host",
                    source_tag="v6.8.0-rc1",
                    signer_certificate_sha256="ab" * 32,
                    released_at="2026-09-01T00:00:00Z",
                    candidate_only=True,
                    workflow_run_url="https://github.com/example/plugin/actions/runs/1001",
                )

    def test_crc32_filename_mismatch_is_rejected(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            manifest, properties, apk_dir = create_fixture(root)
            universal = next(apk_dir.glob("*-universal-*.apk"))
            universal.rename(universal.with_name(universal.name.replace(universal.stem[-8:], "00000000")))

            with self.assertRaises(SystemExit):
                build_release_evidence(
                    runtime_kits_manifest=manifest,
                    version_properties=properties,
                    apk_dir=apk_dir,
                    repository="example/plugin",
                    release_tag="v6.8.0",
                    source_repository="example/host",
                    source_tag="v6.8.0",
                    signer_certificate_sha256="ab" * 32,
                    released_at="2026-09-01T00:00:00Z",
                )

    def test_version_properties_must_match_runtime_kits(self) -> None:
        with tempfile.TemporaryDirectory() as temp:
            root = Path(temp)
            manifest, properties, apk_dir = create_fixture(root)
            properties.write_text(properties.read_text("utf-8").replace("VERSION_BUILD=5277", "VERSION_BUILD=5278"), "utf-8")

            with self.assertRaises(SystemExit):
                build_release_evidence(
                    runtime_kits_manifest=manifest,
                    version_properties=properties,
                    apk_dir=apk_dir,
                    repository="example/plugin",
                    release_tag="v6.8.0",
                    source_repository="example/host",
                    source_tag="v6.8.0",
                    signer_certificate_sha256="ab" * 32,
                    released_at="2026-09-01T00:00:00Z",
                )


def create_fixture(root: Path) -> tuple[Path, Path, Path]:
    properties = root / "version.properties"
    properties.write_text(
        "\n".join(
            (
                "VERSION_NAME=6.8.0",
                "VERSION_BUILD=5277",
                "PLUGIN_VERSION_NAME=1.0.0",
                "PLUGIN_VERSION_BUILD=1",
                "PLUGIN_RELEASE_SEQ=1",
            )
        )
        + "\n",
        "utf-8",
    )
    apk_dir = root / "apks"
    apk_dir.mkdir()
    variants = []
    for variant in VARIANT_ORDER:
        slug = "universal" if variant == "inrt-universal" else variant.removeprefix("inrt-")
        supported_abis = list(ABI_ORDER) if variant == "inrt-universal" else [slug]
        kit_dir = root / variant
        kit_dir.mkdir()
        runtime_kit_id = f"autojs6-runtime-v6.8.0+5277-{'1' * 40}-{variant}"
        metadata = runtime_kit_metadata(variant, supported_abis, runtime_kit_id)
        (kit_dir / "runtime-kit.json").write_text(json.dumps(metadata), "utf-8")
        variants.append(
            {
                "variant": variant,
                "slug": slug,
                "supportedAbis": supported_abis,
                "runtimeKitDir": str(kit_dir),
                "runtimeKitId": runtime_kit_id,
            }
        )

        payload = f"signed-apk-{variant}".encode()
        crc32 = f"{zlib.crc32(payload) & 0xffffffff:08x}"
        (apk_dir / f"autojs6-apk-builder-template-v1.0.0-autojs6-v6.8.0-{slug}-{crc32}.apk").write_bytes(payload)

    manifest = root / "runtime-kits.json"
    manifest.write_text(json.dumps({"schemaVersion": 1, "variants": variants}), "utf-8")
    return manifest, properties, apk_dir


def runtime_kit_metadata(variant: str, supported_abis: list[str], runtime_kit_id: str) -> dict:
    return {
        "schemaVersion": 1,
        "runtimeKitId": runtime_kit_id,
        "host": {
            "versionName": "6.8.0",
            "versionCode": 5277,
            "gitSha": "1" * 40,
        },
        "template": {
            "variant": variant,
            "supportedAbis": supported_abis,
            "sha256": "2" * 64,
        },
        "compatibility": {
            "minHostVersionCode": 5277,
            "maxHostVersionCode": 5277,
            "allowPatchVersionMismatch": False,
        },
        "contract": {
            "apkBuilderProtocolVersion": 2,
            "apkBuildProtocolVersion": 3,
            "remoteBuildProtocolVersion": 3,
            "runtimeApiLevel": 5277,
            "runtimeApiHash": "3" * 64,
            "scriptEngineHash": "4" * 64,
            "resourcesContractHash": "5" * 64,
            "nativeLibManifestHash": "6" * 64,
        },
    }


if __name__ == "__main__":
    unittest.main()
