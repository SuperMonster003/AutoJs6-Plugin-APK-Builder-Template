#!/usr/bin/env python3
"""Maintain compat-matrix.json, including per-ABI release artifacts.

Subcommands:
  add      Upsert one ABI artifact into the release entry described by
           version.properties + Runtime Kit. Repeated calls for the same
           pluginVersionCode merge variants into one release entry.
  resolve  Resolve the newest compatible plugin build and, when --abi is
           supplied, select its matching ABI artifact with universal fallback.
"""

import argparse
import hashlib
import json
import sys
from datetime import datetime, timezone
from pathlib import Path
from typing import Optional

SCHEMA_VERSION = 1
SEQ_MAX = 99
UNIVERSAL_VARIANT = "inrt-universal"
VARIANT_PREFIX = "inrt-"
ABI_ORDER = (
    "arm64-v8a",
    "armeabi-v7a",
    "x86_64",
    "x86",
)
VARIANT_ORDER = (UNIVERSAL_VARIANT, *(f"{VARIANT_PREFIX}{abi}" for abi in ABI_ORDER))

RELEASE_IDENTITY_KEYS = (
    "pluginVersionName",
    "pluginVersionCode",
    "pluginVersionBuild",
    "hostVersionName",
    "hostVersionCode",
    "minHostVersionCode",
    "maxHostVersionCode",
    "allowPatchVersionMismatch",
    "runtimeApiLevel",
    "tag",
)


def resolve_runtime_kit_json(candidate: Path) -> Path:
    if candidate.is_file():
        return candidate
    direct = candidate / "runtime-kit.json"
    if direct.is_file():
        return direct
    nested = sorted(
        path / "runtime-kit.json"
        for path in candidate.iterdir()
        if path.is_dir() and (path / "runtime-kit.json").is_file()
    )
    if nested:
        return nested[0]
    raise SystemExit(f"Cannot find runtime-kit.json under {candidate}")


def read_properties(path: Path) -> dict[str, str]:
    properties: dict[str, str] = {}
    for line in path.read_text("utf-8").splitlines():
        line = line.strip()
        if not line or line.startswith("#") or "=" not in line:
            continue
        key, _, value = line.partition("=")
        properties[key.strip()] = value.strip()
    return properties


def load_matrix(path: Path) -> dict:
    if path.is_file():
        matrix = json.loads(path.read_text("utf-8"))
        if not isinstance(matrix, dict):
            raise SystemExit(f"Malformed matrix file: {path}")
    else:
        matrix = {}
    matrix.setdefault("schemaVersion", SCHEMA_VERSION)
    if int(matrix.get("schemaVersion") or 0) != SCHEMA_VERSION:
        raise SystemExit(f"Unsupported matrix schemaVersion: {matrix.get('schemaVersion')}")
    matrix.setdefault("docs", "docs/versioning.md")
    matrix.setdefault("entries", [])
    if not isinstance(matrix["entries"], list):
        raise SystemExit(f"Malformed matrix entries: {path}")
    return matrix


def write_matrix(path: Path, matrix: dict) -> None:
    matrix["entries"] = sorted(
        matrix["entries"],
        key=lambda entry: int(entry.get("pluginVersionCode") or 0),
        reverse=True,
    )
    path.write_text(json.dumps(matrix, ensure_ascii=False, indent=2) + "\n", "utf-8")


def sha256_of(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as stream:
        while chunk := stream.read(1 << 16):
            digest.update(chunk)
    return digest.hexdigest()


def runtime_kit_variant(kit: dict) -> tuple[str, list[str]]:
    template = kit.get("template") or {}
    raw_variant = template.get("variant")
    raw_supported_abis = template.get("supportedAbis")
    if raw_variant is None and raw_supported_abis is None:
        return UNIVERSAL_VARIANT, []
    if not isinstance(raw_variant, str) or not raw_variant.strip():
        raise SystemExit("Runtime Kit must declare a valid template.variant")
    if not isinstance(raw_supported_abis, list) or not all(
        isinstance(abi, str) and abi.strip() for abi in raw_supported_abis
    ):
        raise SystemExit("Runtime Kit must declare template.supportedAbis as an array of ABIs")

    variant = raw_variant.strip()
    supported_abis = [abi.strip() for abi in raw_supported_abis]
    if variant == UNIVERSAL_VARIANT:
        if supported_abis != list(ABI_ORDER):
            raise SystemExit(
                "Universal Runtime Kit must declare all packaged ABIs: "
                f"expected={list(ABI_ORDER)}, actual={supported_abis}"
            )
        return variant, supported_abis
    if not variant.startswith(VARIANT_PREFIX):
        raise SystemExit(f"Unsupported Runtime Kit variant: {variant}")
    abi = variant.removeprefix(VARIANT_PREFIX)
    if abi not in ABI_ORDER or supported_abis != [abi]:
        raise SystemExit(f"Runtime Kit variant/ABI mismatch: variant={variant}, supportedAbis={supported_abis}")
    return variant, supported_abis


def artifact_from_legacy_entry(entry: dict) -> Optional[dict]:
    name = str(entry.get("apkName") or "").strip()
    url = str(entry.get("apkUrl") or "").strip()
    sha256 = str(entry.get("apkSha256") or "").strip()
    if not name or not url or not sha256:
        return None
    return {
        "variant": str(entry.get("variant") or UNIVERSAL_VARIANT),
        "supportedAbis": list(entry.get("supportedAbis") or []),
        "apkName": name,
        "apkUrl": url,
        "apkSha256": sha256,
        "apkSizeBytes": int(entry.get("apkSizeBytes") or 0),
    }


def artifacts_from_entry(entry: dict) -> list[dict]:
    artifacts = entry.get("artifacts")
    if isinstance(artifacts, list) and artifacts:
        return [dict(artifact) for artifact in artifacts if isinstance(artifact, dict)]
    legacy = artifact_from_legacy_entry(entry)
    return [legacy] if legacy else []


def sort_artifacts(artifacts: list[dict]) -> list[dict]:
    order = {variant: index for index, variant in enumerate(VARIANT_ORDER)}
    return sorted(artifacts, key=lambda artifact: order.get(str(artifact.get("variant")), len(order)))


def select_artifact(entry: dict, abi: Optional[str]) -> Optional[dict]:
    artifacts = artifacts_from_entry(entry)
    if not artifacts:
        return None
    if abi and abi != "universal":
        for artifact in artifacts:
            if artifact.get("variant") != UNIVERSAL_VARIANT and abi in list(artifact.get("supportedAbis") or []):
                return artifact
    for artifact in artifacts:
        if artifact.get("variant") == UNIVERSAL_VARIANT or not list(artifact.get("supportedAbis") or []):
            return artifact
    return artifacts[0] if abi is None else None


def cmd_add(args: argparse.Namespace) -> None:
    kit = json.loads(resolve_runtime_kit_json(args.runtime_kit.resolve()).read_text("utf-8"))
    compatibility = kit.get("compatibility") or {}
    contract = kit.get("contract") or {}
    variant, supported_abis = runtime_kit_variant(kit)

    properties = read_properties(args.version_properties)
    plugin_version_name = properties.get("PLUGIN_VERSION_NAME", "")
    host_version_name = properties.get("VERSION_NAME", "")
    host_code = int(properties.get("VERSION_BUILD") or 0)
    plugin_build = int(properties.get("PLUGIN_VERSION_BUILD") or 0)
    release_seq = int(properties.get("PLUGIN_RELEASE_SEQ") or 0)

    if not plugin_version_name or not host_version_name or host_code <= 0:
        raise SystemExit("version.properties must declare PLUGIN_VERSION_NAME, VERSION_NAME and VERSION_BUILD")
    kit_host = kit.get("host") or {}
    kit_host_name = str(kit_host.get("versionName") or "").strip()
    kit_host_code = int(kit_host.get("versionCode") or 0)
    if host_version_name != kit_host_name or host_code != kit_host_code:
        raise SystemExit(
            "version.properties and Runtime Kit host identity differ: "
            f"properties={host_version_name}/{host_code}, kit={kit_host_name}/{kit_host_code}"
        )
    if not 1 <= release_seq <= SEQ_MAX:
        raise SystemExit(f"PLUGIN_RELEASE_SEQ must be within 1..{SEQ_MAX} when adding an entry, got {release_seq}")

    min_host_version_code = int(compatibility.get("minHostVersionCode") or host_code)
    max_host_version_code = int(compatibility.get("maxHostVersionCode") or host_code)
    allow_patch_version_mismatch = compatibility.get("allowPatchVersionMismatch", False)
    if not isinstance(allow_patch_version_mismatch, bool):
        raise SystemExit("Runtime Kit compatibility.allowPatchVersionMismatch must be a boolean")
    if min_host_version_code <= 0 or max_host_version_code < min_host_version_code:
        raise SystemExit(
            "Runtime Kit host compatibility range is invalid: "
            f"{min_host_version_code}..{max_host_version_code}"
        )
    if not min_host_version_code <= host_code <= max_host_version_code:
        raise SystemExit(
            f"Runtime Kit built-for host {host_code} is outside its compatibility range "
            f"{min_host_version_code}..{max_host_version_code}"
        )
    if min_host_version_code != max_host_version_code and not allow_patch_version_mismatch:
        raise SystemExit("A widened Runtime Kit host range requires allowPatchVersionMismatch=true")

    apk = args.apk
    if not apk.is_file():
        raise SystemExit(f"APK not found: {apk}")
    apk_name = args.apk_name or apk.name
    released_at = args.released_at or datetime.now(timezone.utc).strftime("%Y-%m-%dT%H:%M:%SZ")
    apk_url = f"https://github.com/{args.repo}/releases/download/{args.tag}/{apk_name}"
    artifact = {
        "variant": variant,
        "supportedAbis": supported_abis,
        "apkName": apk_name,
        "apkUrl": apk_url,
        "apkSha256": sha256_of(apk),
        "apkSizeBytes": apk.stat().st_size,
    }
    release_identity = {
        "pluginVersionName": plugin_version_name,
        "pluginVersionCode": host_code * 100 + release_seq,
        "pluginVersionBuild": plugin_build,
        "hostVersionName": host_version_name,
        "hostVersionCode": host_code,
        "minHostVersionCode": min_host_version_code,
        "maxHostVersionCode": max_host_version_code,
        "allowPatchVersionMismatch": allow_patch_version_mismatch,
        "runtimeApiLevel": int(contract.get("runtimeApiLevel") or host_code),
        "tag": args.tag,
    }

    matrix = load_matrix(args.matrix)
    existing = next(
        (
            item for item in matrix["entries"]
            if int(item.get("pluginVersionCode") or 0) == release_identity["pluginVersionCode"]
        ),
        None,
    )
    if existing:
        for key in RELEASE_IDENTITY_KEYS:
            if existing.get(key) != release_identity[key]:
                raise SystemExit(
                    f"Cannot merge {variant}: release identity mismatch for {key}: "
                    f"existing={existing.get(key)!r}, incoming={release_identity[key]!r}"
                )

    artifacts = artifacts_from_entry(existing or {})
    artifacts = [item for item in artifacts if item.get("variant") != variant]
    artifacts.append(artifact)
    artifacts = sort_artifacts(artifacts)
    fallback = select_artifact({"artifacts": artifacts}, None) or artifact

    entry = {
        **release_identity,
        "artifacts": artifacts,
        # Legacy universal projection retained for existing matrix consumers.
        "apkName": fallback["apkName"],
        "apkUrl": fallback["apkUrl"],
        "apkSha256": fallback["apkSha256"],
        "apkSizeBytes": int(fallback.get("apkSizeBytes") or 0),
        "releasedAt": (existing or {}).get("releasedAt") or released_at,
    }
    matrix["entries"] = [
        item for item in matrix["entries"]
        if int(item.get("pluginVersionCode") or 0) != entry["pluginVersionCode"]
    ]
    matrix["entries"].append(entry)
    write_matrix(args.matrix, matrix)

    print(
        f"Compat matrix updated: {args.matrix} "
        f"({len(matrix['entries'])} releases, {len(artifacts)} artifacts in current release)"
    )
    print(json.dumps(artifact, ensure_ascii=False, indent=2))


def cmd_resolve(args: argparse.Namespace) -> None:
    matrix = load_matrix(args.matrix)
    host_code = args.host_version_code
    candidates = [
        entry for entry in matrix["entries"]
        if matrix_entry_allows_host(entry, host_code)
    ]
    if not candidates:
        print(f"No matrix entry matches hostVersionCode {host_code}; fall back to the tag channel.", file=sys.stderr)
        raise SystemExit(1)
    best = max(candidates, key=lambda entry: int(entry.get("pluginVersionCode") or 0))
    artifact = select_artifact(best, args.abi)
    if artifact is None:
        print(
            f"Matrix entry {best.get('pluginVersionCode')} has no artifact for ABI {args.abi} "
            "and no universal fallback.",
            file=sys.stderr,
        )
        raise SystemExit(1)

    resolved = dict(best)
    resolved["apkName"] = artifact["apkName"]
    resolved["apkUrl"] = artifact["apkUrl"]
    resolved["apkSha256"] = artifact["apkSha256"]
    resolved["apkSizeBytes"] = int(artifact.get("apkSizeBytes") or 0)
    resolved["selectedArtifact"] = artifact
    print(json.dumps(resolved, ensure_ascii=False, indent=2))


def matrix_entry_allows_host(entry: dict, host_code: int) -> bool:
    try:
        minimum = int(entry.get("minHostVersionCode") or 0)
        maximum = int(entry.get("maxHostVersionCode") or 0)
        raw_built_for = entry.get("hostVersionCode")
        built_for = int(raw_built_for if raw_built_for is not None else minimum)
    except (TypeError, ValueError):
        return False
    if minimum <= 0 or maximum < minimum or not minimum <= host_code <= maximum:
        return False
    if built_for <= 0 or not minimum <= built_for <= maximum:
        return False
    allow_patch_mismatch = entry.get("allowPatchVersionMismatch", False)
    if not isinstance(allow_patch_mismatch, bool):
        return False
    if minimum != maximum and not allow_patch_mismatch:
        return False
    return True


def main() -> None:
    parser = argparse.ArgumentParser(description=__doc__, formatter_class=argparse.RawDescriptionHelpFormatter)
    subparsers = parser.add_subparsers(dest="command", required=True)

    add = subparsers.add_parser("add", help="Upsert one ABI artifact into the current release")
    add.add_argument("--matrix", type=Path, default=Path("compat-matrix.json"))
    add.add_argument("--runtime-kit", type=Path, default=Path("runtime-kit"))
    add.add_argument("--version-properties", type=Path, default=Path("version.properties"))
    add.add_argument("--apk", type=Path, required=True, help="Path of the uploaded plugin APK")
    add.add_argument("--apk-name", help="Release asset name (defaults to the APK file name)")
    add.add_argument("--tag", required=True, help="Release tag the APK was uploaded to")
    add.add_argument("--repo", required=True, help="owner/repo hosting the release")
    add.add_argument("--released-at", help="ISO-8601 release instant (defaults to now, UTC)")
    add.set_defaults(func=cmd_add)

    resolve = subparsers.add_parser("resolve", help="Resolve the best plugin build and ABI artifact")
    resolve.add_argument("--matrix", type=Path, default=Path("compat-matrix.json"))
    resolve.add_argument("--host-version-code", type=int, required=True)
    resolve.add_argument("--abi", choices=("universal", *ABI_ORDER), help="Device ABI (universal fallback is automatic)")
    resolve.set_defaults(func=cmd_resolve)

    args = parser.parse_args()
    args.func(args)


if __name__ == "__main__":
    main()
