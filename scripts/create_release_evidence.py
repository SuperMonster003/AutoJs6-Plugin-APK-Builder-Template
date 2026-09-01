#!/usr/bin/env python3
"""Create a machine-readable evidence manifest for one signed plugin release.

The release workflow has already verified each APK signature before invoking
this script. This step binds that verified signer digest to the exact APK
bytes, Runtime Kit identities, host compatibility contract, and plugin version
that are about to be published.
"""

import argparse
import json
import re
import zlib
from pathlib import Path
from urllib.parse import quote

from update_compat_matrix import (
    VARIANT_ORDER,
    read_properties,
    runtime_kit_variant,
    sha256_of,
)

SCHEMA_VERSION = 1
SHA256_PATTERN = re.compile(r"[0-9a-f]{64}")
CRC32_SUFFIX_PATTERN = re.compile(r"-([0-9a-f]{8})\.apk$")


def normalize_sha256(value: str) -> str:
    normalized = value.strip().lower().replace(":", "")
    if SHA256_PATTERN.fullmatch(normalized) is None:
        raise SystemExit("Signer certificate SHA-256 must contain exactly 64 hexadecimal characters")
    return normalized


def crc32_of(path: Path) -> str:
    digest = 0
    with path.open("rb") as stream:
        while chunk := stream.read(1 << 16):
            digest = zlib.crc32(chunk, digest)
    return f"{digest & 0xffffffff:08x}"


def host_version_slug(version_name: str) -> str:
    return re.sub(r"\s+", "-", version_name.strip()).lower()


def shared_runtime_kit_identity(metadata: dict) -> dict:
    host = metadata.get("host") or {}
    compatibility = metadata.get("compatibility") or {}
    contract = metadata.get("contract") or {}
    return {
        "hostVersionName": str(host.get("versionName") or "").strip(),
        "hostVersionCode": int(host.get("versionCode") or 0),
        "hostGitSha": str(host.get("gitSha") or "").strip(),
        "minHostVersionCode": int(compatibility.get("minHostVersionCode") or 0),
        "maxHostVersionCode": int(compatibility.get("maxHostVersionCode") or 0),
        "allowPatchVersionMismatch": compatibility.get("allowPatchVersionMismatch"),
        "apkBuilderProtocolVersion": int(contract.get("apkBuilderProtocolVersion") or 0),
        "remoteBuildProtocolVersion": int(contract.get("remoteBuildProtocolVersion") or 0),
        "runtimeApiLevel": int(contract.get("runtimeApiLevel") or 0),
        "runtimeApiHash": str(contract.get("runtimeApiHash") or "").strip(),
        "scriptEngineHash": str(contract.get("scriptEngineHash") or "").strip(),
        "resourcesContractHash": str(contract.get("resourcesContractHash") or "").strip(),
    }


def validate_shared_identity(identity: dict) -> None:
    host_code = identity["hostVersionCode"]
    minimum = identity["minHostVersionCode"]
    maximum = identity["maxHostVersionCode"]
    allow_patch = identity["allowPatchVersionMismatch"]
    if not identity["hostVersionName"] or host_code <= 0 or not identity["hostGitSha"]:
        raise SystemExit("Runtime Kit host identity is incomplete")
    if minimum <= 0 or maximum < minimum or not minimum <= host_code <= maximum:
        raise SystemExit(
            f"Runtime Kit host compatibility range is invalid: builtFor={host_code}, range={minimum}..{maximum}"
        )
    if not isinstance(allow_patch, bool):
        raise SystemExit("Runtime Kit compatibility.allowPatchVersionMismatch must be a boolean")
    if minimum != maximum and not allow_patch:
        raise SystemExit("A widened Runtime Kit host range requires allowPatchVersionMismatch=true")
    for field in ("apkBuilderProtocolVersion", "remoteBuildProtocolVersion", "runtimeApiLevel"):
        if int(identity[field]) <= 0:
            raise SystemExit(f"Runtime Kit {field} must be positive")
    for field in ("runtimeApiHash", "scriptEngineHash", "resourcesContractHash"):
        if SHA256_PATTERN.fullmatch(str(identity[field]).lower()) is None:
            raise SystemExit(f"Runtime Kit {field} must be a SHA-256 digest")


def build_release_evidence(
    *,
    runtime_kits_manifest: Path,
    version_properties: Path,
    apk_dir: Path,
    repository: str,
    release_tag: str,
    source_repository: str,
    source_tag: str,
    signer_certificate_sha256: str,
    released_at: str,
) -> dict:
    manifest = json.loads(runtime_kits_manifest.read_text("utf-8"))
    if int(manifest.get("schemaVersion") or 0) != SCHEMA_VERSION:
        raise SystemExit(f"Unsupported Runtime Kit set manifest schemaVersion: {manifest.get('schemaVersion')}")
    records = manifest.get("variants") or []
    if not isinstance(records, list):
        raise SystemExit("Runtime Kit set manifest variants must be an array")
    record_variants = [str(record.get("variant") or "") for record in records if isinstance(record, dict)]
    if record_variants != list(VARIANT_ORDER):
        raise SystemExit(
            "Runtime Kit evidence requires the canonical five-variant set: "
            f"expected={list(VARIANT_ORDER)}, actual={record_variants}"
        )

    properties = read_properties(version_properties)
    plugin_version_name = properties.get("PLUGIN_VERSION_NAME", "").strip()
    plugin_version_build = int(properties.get("PLUGIN_VERSION_BUILD") or 0)
    plugin_release_seq = int(properties.get("PLUGIN_RELEASE_SEQ") or 0)
    paired_host_name = properties.get("VERSION_NAME", "").strip()
    paired_host_code = int(properties.get("VERSION_BUILD") or 0)
    if not plugin_version_name or plugin_version_build <= 0 or not 1 <= plugin_release_seq <= 99:
        raise SystemExit("Release version properties must contain a published plugin version/build/sequence")

    signer_digest = normalize_sha256(signer_certificate_sha256)
    if not repository.strip() or not release_tag.strip() or not source_repository.strip() or not source_tag.strip():
        raise SystemExit("Release and source repository/tag identities are required")
    if not released_at.strip():
        raise SystemExit("Release timestamp is required")

    baseline_identity = None
    runtime_kits = []
    artifacts = []
    for record in records:
        runtime_kit_dir = Path(str(record.get("runtimeKitDir") or ""))
        metadata_path = runtime_kit_dir / "runtime-kit.json"
        if not metadata_path.is_file():
            raise SystemExit(f"Runtime Kit metadata not found for {record.get('variant')}")
        metadata = json.loads(metadata_path.read_text("utf-8"))
        variant, supported_abis = runtime_kit_variant(metadata)
        if variant != record.get("variant"):
            raise SystemExit(
                f"Runtime Kit manifest variant mismatch: manifest={record.get('variant')}, metadata={variant}"
            )
        runtime_kit_id = str(metadata.get("runtimeKitId") or "").strip()
        if not runtime_kit_id or runtime_kit_id != str(record.get("runtimeKitId") or "").strip():
            raise SystemExit(f"Runtime Kit ID mismatch for {variant}")

        identity = shared_runtime_kit_identity(metadata)
        validate_shared_identity(identity)
        if baseline_identity is None:
            baseline_identity = identity
        elif identity != baseline_identity:
            raise SystemExit(f"Runtime Kit shared release identity mismatch for {variant}")

        slug = str(record.get("slug") or "").strip()
        expected_slug = "universal" if variant == "inrt-universal" else variant.removeprefix("inrt-")
        if slug != expected_slug or list(record.get("supportedAbis") or []) != supported_abis:
            raise SystemExit(f"Runtime Kit manifest slug/ABI mismatch for {variant}")
        matches = sorted(apk_dir.glob(f"*-{slug}-????????.apk"))
        if len(matches) != 1:
            raise SystemExit(f"Expected one signed APK for {variant}, found {len(matches)}")
        apk = matches[0]
        crc_match = CRC32_SUFFIX_PATTERN.search(apk.name.lower())
        actual_crc32 = crc32_of(apk)
        if crc_match is None or crc_match.group(1) != actual_crc32:
            raise SystemExit(f"APK CRC32 filename suffix does not match its bytes: {apk.name}")

        template = metadata.get("template") or {}
        template_sha256 = str(template.get("sha256") or "").strip().lower()
        if SHA256_PATTERN.fullmatch(template_sha256) is None:
            raise SystemExit(f"Runtime Kit template SHA-256 is invalid for {variant}")
        runtime_kits.append(
            {
                "variant": variant,
                "supportedAbis": supported_abis,
                "runtimeKitId": runtime_kit_id,
                "templateApkSha256": template_sha256,
            }
        )
        encoded_name = quote(apk.name)
        artifacts.append(
            {
                "variant": variant,
                "supportedAbis": supported_abis,
                "fileName": apk.name,
                "downloadUrl": f"https://github.com/{repository}/releases/download/{release_tag}/{encoded_name}",
                "sizeBytes": apk.stat().st_size,
                "sha256": sha256_of(apk),
                "crc32": actual_crc32,
                "signerCertificateSha256": signer_digest,
                "runtimeKitId": runtime_kit_id,
            }
        )

    assert baseline_identity is not None
    if paired_host_name != baseline_identity["hostVersionName"] or paired_host_code != baseline_identity["hostVersionCode"]:
        raise SystemExit(
            "version.properties and Runtime Kit host identity differ: "
            f"properties={paired_host_name}/{paired_host_code}, "
            f"kits={baseline_identity['hostVersionName']}/{baseline_identity['hostVersionCode']}"
        )
    plugin_version_code = paired_host_code * 100 + plugin_release_seq
    composite_version_name = f"{plugin_version_name}+autojs6-{host_version_slug(paired_host_name)}"

    return {
        "schemaVersion": SCHEMA_VERSION,
        "generatedAt": released_at,
        "repository": repository,
        "releaseTag": release_tag,
        "releaseUrl": f"https://github.com/{repository}/releases/tag/{release_tag}",
        "source": {
            "repository": source_repository,
            "tag": source_tag,
            "gitSha": baseline_identity["hostGitSha"],
        },
        "plugin": {
            "versionName": plugin_version_name,
            "compositeVersionName": composite_version_name,
            "versionCode": plugin_version_code,
            "versionBuild": plugin_version_build,
            "releaseSequence": plugin_release_seq,
            "remoteBuildEnabledByDefault": False,
        },
        "host": {
            "versionName": paired_host_name,
            "versionCode": paired_host_code,
            "minCompatibleVersionCode": baseline_identity["minHostVersionCode"],
            "maxCompatibleVersionCode": baseline_identity["maxHostVersionCode"],
            "allowPatchVersionMismatch": baseline_identity["allowPatchVersionMismatch"],
        },
        "protocol": {
            "apkBuilderProtocolVersion": baseline_identity["apkBuilderProtocolVersion"],
            "remoteBuildProtocolVersion": baseline_identity["remoteBuildProtocolVersion"],
            "runtimeApiLevel": baseline_identity["runtimeApiLevel"],
            "runtimeApiHash": baseline_identity["runtimeApiHash"],
            "scriptEngineHash": baseline_identity["scriptEngineHash"],
            "resourcesContractHash": baseline_identity["resourcesContractHash"],
        },
        "signerCertificateSha256": signer_digest,
        "runtimeKits": runtime_kits,
        "artifacts": artifacts,
    }


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--runtime-kits-manifest", type=Path, required=True)
    parser.add_argument("--version-properties", type=Path, default=Path("version.properties"))
    parser.add_argument("--apk-dir", type=Path, required=True)
    parser.add_argument("--repository", required=True)
    parser.add_argument("--release-tag", required=True)
    parser.add_argument("--source-repository", required=True)
    parser.add_argument("--source-tag", required=True)
    parser.add_argument("--signer-certificate-sha256", required=True)
    parser.add_argument("--released-at", required=True)
    parser.add_argument("--output", type=Path, required=True)
    args = parser.parse_args()

    evidence = build_release_evidence(
        runtime_kits_manifest=args.runtime_kits_manifest.resolve(),
        version_properties=args.version_properties.resolve(),
        apk_dir=args.apk_dir.resolve(),
        repository=args.repository,
        release_tag=args.release_tag,
        source_repository=args.source_repository,
        source_tag=args.source_tag,
        signer_certificate_sha256=args.signer_certificate_sha256,
        released_at=args.released_at,
    )
    args.output.parent.mkdir(parents=True, exist_ok=True)
    args.output.write_text(json.dumps(evidence, ensure_ascii=False, indent=2) + "\n", "utf-8")
    print(
        f"Release evidence created: {args.output} "
        f"({len(evidence['runtimeKits'])} Runtime Kits, {len(evidence['artifacts'])} APKs)"
    )


if __name__ == "__main__":
    main()
