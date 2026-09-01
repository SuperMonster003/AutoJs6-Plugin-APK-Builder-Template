#!/usr/bin/env python3

import hashlib
import json
import re
import sys
import zipfile
from pathlib import Path


RULES_PATH = Path(__file__).with_name("runtime_kit_validation_rules.json")
SHA256_PATTERN = re.compile(r"[0-9a-f]{64}")


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(1024 * 1024), b""):
            digest.update(chunk)
    return digest.hexdigest()


def resolve_runtime_kit_root(candidate: Path) -> Path:
    if (candidate / "runtime-kit.json").is_file():
        return candidate
    nested = sorted(
        path
        for path in candidate.iterdir()
        if path.is_dir() and (path / "runtime-kit.json").is_file()
    )
    return nested[0] if nested else candidate


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


def read_metadata_path(metadata: dict, path: str):
    current = metadata
    for segment in path.split("."):
        if not isinstance(current, dict) or segment not in current:
            return None
        current = current[segment]
    return current


def load_rules() -> dict:
    rules = json.loads(RULES_PATH.read_text("utf-8"))
    require(rules.get("schemaVersion") == 1, "Unsupported Runtime Kit validation rules schemaVersion")
    return rules


def parse_template_variant(
    metadata: dict,
    rules: dict,
    *,
    require_explicit: bool = False,
) -> tuple[str, list[str]]:
    variant_rules = rules["templateVariant"]
    variant_value = read_metadata_path(metadata, variant_rules["variantMetadata"])
    supported_abis_value = read_metadata_path(metadata, variant_rules["supportedAbisMetadata"])
    universal_variant = variant_rules["universalVariant"]

    if variant_value is None and supported_abis_value is None:
        allow_legacy = bool(variant_rules.get("allowLegacyMissingMetadata"))
        require(
            allow_legacy and not require_explicit,
            "Runtime Kit must explicitly declare template.variant and template.supportedAbis",
        )
        return universal_variant, []

    require(
        isinstance(variant_value, str) and bool(variant_value.strip()),
        "Runtime Kit metadata does not declare a valid template.variant",
    )
    require(
        isinstance(supported_abis_value, list),
        "Runtime Kit metadata does not declare template.supportedAbis as an array",
    )

    variant = variant_value.strip()
    supported_abis = []
    for raw_abi in supported_abis_value:
        require(
            isinstance(raw_abi, str) and bool(raw_abi.strip()),
            "Runtime Kit template.supportedAbis contains an invalid ABI",
        )
        supported_abis.append(raw_abi.strip())
    require(
        len(supported_abis) == len(set(supported_abis)),
        "Runtime Kit template.supportedAbis contains duplicate ABIs",
    )

    allowed_abis = list(variant_rules["allowedAbis"])
    if variant == universal_variant:
        require(
            supported_abis == allowed_abis,
            f"Universal Runtime Kit {universal_variant} must declare "
            f"template.supportedAbis={allowed_abis!r}",
        )
        return variant, supported_abis

    prefix = variant_rules["variantPrefix"]
    require(
        variant.startswith(prefix),
        f"Unsupported Runtime Kit template.variant: {variant}",
    )
    abi = variant[len(prefix):]
    require(abi in allowed_abis, f"Unsupported Runtime Kit ABI variant: {abi}")
    require(
        supported_abis == [abi],
        f"Runtime Kit {variant} must declare template.supportedAbis=[{abi!r}]",
    )
    return variant, supported_abis


def parse_host_compatibility(
    metadata: dict,
    rules: dict,
    *,
    require_explicit: bool = False,
) -> tuple[int, int, int, bool]:
    compatibility_rules = rules["compatibility"]
    built_for_value = read_metadata_path(
        metadata,
        compatibility_rules["builtForHostVersionCodeMetadata"],
    )
    require(
        isinstance(built_for_value, int) and not isinstance(built_for_value, bool) and built_for_value > 0,
        "Runtime Kit metadata does not declare a valid host.versionCode",
    )
    built_for = int(built_for_value)

    minimum_value = read_metadata_path(
        metadata,
        compatibility_rules["minHostVersionCodeMetadata"],
    )
    maximum_value = read_metadata_path(
        metadata,
        compatibility_rules["maxHostVersionCodeMetadata"],
    )
    allow_patch_value = read_metadata_path(
        metadata,
        compatibility_rules["allowPatchVersionMismatchMetadata"],
    )
    if minimum_value is None and maximum_value is None and allow_patch_value is None:
        require(
            bool(compatibility_rules.get("allowLegacyMissingMetadata")) and not require_explicit,
            "Runtime Kit must explicitly declare its host compatibility contract",
        )
        return built_for, built_for, built_for, False

    require(
        isinstance(minimum_value, int) and not isinstance(minimum_value, bool) and minimum_value > 0,
        "Runtime Kit compatibility.minHostVersionCode must be a positive integer",
    )
    require(
        isinstance(maximum_value, int) and not isinstance(maximum_value, bool) and maximum_value > 0,
        "Runtime Kit compatibility.maxHostVersionCode must be a positive integer",
    )
    require(
        isinstance(allow_patch_value, bool),
        "Runtime Kit compatibility.allowPatchVersionMismatch must be a boolean",
    )
    minimum = int(minimum_value)
    maximum = int(maximum_value)
    require(
        minimum <= maximum,
        f"Runtime Kit host compatibility range is invalid: {minimum}..{maximum}",
    )
    require(
        minimum <= built_for <= maximum,
        f"Runtime Kit built-for host {built_for} is outside its compatibility range {minimum}..{maximum}",
    )
    require(
        minimum == maximum or allow_patch_value,
        "A widened Runtime Kit host range requires allowPatchVersionMismatch=true",
    )
    return built_for, minimum, maximum, allow_patch_value


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("Usage: verify_runtime_kit.py <runtime-kit-dir>")

    rules = load_rules()
    kit_rules = rules["runtimeKit"]
    root = resolve_runtime_kit_root(Path(sys.argv[1]).resolve())
    for name in rules["requiredFiles"]:
        require((root / name).is_file(), f"Missing Runtime Kit file: {root / name}")

    metadata_file = root / kit_rules["metadataFile"]
    meta = json.loads(metadata_file.read_text("utf-8"))
    expected_schema = int(kit_rules["schemaVersion"])
    require(
        meta.get("schemaVersion") == expected_schema,
        f"Unsupported Runtime Kit schemaVersion: {meta.get('schemaVersion')}",
    )

    contract = meta.get("contract") or {}
    protocol = int(contract.get("apkBuilderProtocolVersion") or 0)
    minimum_protocol = int(kit_rules["minimumApkBuilderProtocolVersion"])
    require(protocol >= minimum_protocol, f"Runtime Kit protocol too old: {protocol} < {minimum_protocol}")
    variant, supported_abis = parse_template_variant(meta, rules)
    _, min_host_version_code, max_host_version_code, allow_patch_version_mismatch = (
        parse_host_compatibility(meta, rules)
    )

    verified_sha256 = {}
    for artifact_rules in rules["sha256Artifacts"]:
        artifact_name = artifact_rules["path"]
        artifact = root / artifact_name
        require(artifact.stat().st_size > 0, f"Runtime Kit artifact is empty: {artifact}")

        expected_size_value = read_metadata_path(meta, artifact_rules["metadataSizeBytes"])
        try:
            expected_size = int(expected_size_value)
        except (TypeError, ValueError):
            raise SystemExit(
                f"Runtime Kit metadata does not declare a valid {artifact_rules['metadataSizeBytes']}"
            )
        require(
            expected_size == artifact.stat().st_size,
            f"{artifact_name} size mismatch in {metadata_file.name}: "
            f"expected={expected_size} actual={artifact.stat().st_size}",
        )

        expected_sha = str(read_metadata_path(meta, artifact_rules["metadataSha256"]) or "").strip().lower()
        sidecar = root / artifact_rules["sidecar"]
        sidecar_sha = sidecar.read_text("utf-8").strip().lower()
        require(
            SHA256_PATTERN.fullmatch(expected_sha) is not None,
            f"Runtime Kit metadata does not declare a valid {artifact_rules['metadataSha256']}",
        )
        require(
            SHA256_PATTERN.fullmatch(sidecar_sha) is not None,
            f"Runtime Kit SHA-256 sidecar is invalid: {sidecar}",
        )

        actual_sha = sha256(artifact).lower()
        require(expected_sha == actual_sha, f"{artifact_name} SHA-256 mismatch in {metadata_file.name}")
        require(sidecar_sha == actual_sha, f"{artifact_name} SHA-256 mismatch in {sidecar.name}")
        verified_sha256[artifact_name] = actual_sha

    template_rules = rules["templateApk"]
    template_apk = root / template_rules["path"]
    require(zipfile.is_zipfile(template_apk), f"Runtime Kit template APK is not a valid ZIP: {template_apk}")
    with zipfile.ZipFile(template_apk) as archive:
        names = set(archive.namelist())
        missing = [name for name in template_rules["requiredEntries"] if name not in names]
        require(not missing, f"template.apk missing required entries: {missing}")
        native_abis = {
            parts[1]
            for name in names
            if name.endswith(".so")
            and len(parts := name.split("/")) >= 3
            and parts[0] == "lib"
        }
        if supported_abis:
            require(
                native_abis == set(supported_abis),
                f"template.apk native ABI mismatch for {variant}: "
                f"declared={supported_abis}, actual={sorted(native_abis)}",
            )

    print("Runtime Kit verified")
    print(f"  root: {root}")
    print(f"  runtimeKitId: {meta.get('runtimeKitId')}")
    print(f"  versionName: {(meta.get('host') or {}).get('versionName')}")
    print(f"  versionCode: {(meta.get('host') or {}).get('versionCode')}")
    print(f"  protocol: {protocol}")
    print(f"  variant: {variant}")
    print(f"  supportedAbis: {', '.join(supported_abis) if supported_abis else 'all'}")
    print(f"  hostCompatibility: {min_host_version_code}..{max_host_version_code}")
    print(f"  allowPatchVersionMismatch: {str(allow_patch_version_mismatch).lower()}")
    print(f"  templateSha256: {verified_sha256[template_rules['path']]}")
    print(f"  rules: {RULES_PATH}")


if __name__ == "__main__":
    main()
