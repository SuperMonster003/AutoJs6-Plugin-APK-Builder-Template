#!/usr/bin/env python3
"""Fail when remote-build AIDL/API symbols are missing from the protocol document.

This is intentionally a coverage check, not a Kotlin/AIDL compiler. The Android
build remains authoritative for syntax and types; this script makes additions to
the public remote-build surface visible to the documentation consistency job.
"""

import argparse
import json
import re
from pathlib import Path


REMOTE_PACKAGE = Path(
    "plugin-api/apk-builder-template/src/main/java/"
    "org/autojs/plugin/apkbuilder/template"
)
AIDL_PACKAGE = Path(
    "plugin-api/apk-builder-template/src/main/aidl/"
    "org/autojs/plugin/apkbuilder/template"
)
REMOTE_CAPABILITY_NAMES = (
    "SUPPORTS_REMOTE_BUILD",
    "REMOTE_BUILD_PROTOCOL_VERSION",
    "REMOTE_BUILD_STATUS",
    "REMOTE_BUILD_API_VERSION",
)
REQUEST_EXTRA_KEY_NAMES = (
    "ARCHIVE_FORMAT_VERSION",
    "SOURCE_KIND",
    "SOURCE_PATH",
    "SOURCE_ROOT_PATH",
    "PROJECT_ARCHIVE_UNCOMPRESSED_SIZE_BYTES",
    "NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES",
    "ICON_PATH",
    "HOST_OUTPUT_FILE_NAME",
    "TYPESCRIPT_STAGING_ENCRYPTION_VERSION",
    "TYPESCRIPT_STAGING_ENCRYPTION_KEY",
    "TYPESCRIPT_STAGING_ENCRYPTED_PATHS",
)


def _read(path: Path) -> str:
    return path.read_text("utf-8")


def _parcelable_fields(path: Path, class_name: str) -> list[str]:
    source = _read(path)
    match = re.search(
        rf"data\s+class\s+{re.escape(class_name)}\s*\((.*?)\)\s*:\s*Parcelable",
        source,
        re.DOTALL,
    )
    if match is None:
        raise ValueError(f"Cannot parse Parcelable constructor: {path}")
    fields = re.findall(r"\b(?:var|val)\s+([A-Za-z_][A-Za-z0-9_]*)\s*:", match.group(1))
    if not fields:
        raise ValueError(f"Parcelable constructor has no parsed fields: {path}")
    return fields


def _string_constants(path: Path, selected_names: tuple[str, ...] | None = None) -> list[str]:
    constants = dict(
        re.findall(
            r"\bconst\s+val\s+([A-Z][A-Z0-9_]*)\s*=\s*\"([^\"]+)\"",
            _read(path),
            re.DOTALL,
        )
    )
    if selected_names is None:
        if not constants:
            raise ValueError(f"No string constants parsed: {path}")
        return list(constants.values())
    missing = [name for name in selected_names if name not in constants]
    if missing:
        raise ValueError(f"Missing constants in {path}: {', '.join(missing)}")
    return [constants[name] for name in selected_names]


def _numeric_constant_names(path: Path, prefix: str) -> list[str]:
    names = re.findall(
        rf"\bconst\s+val\s+({re.escape(prefix)}[A-Z0-9_]*)\s*=\s*-?[0-9]+",
        _read(path),
    )
    if not names:
        raise ValueError(f"No {prefix} constants parsed: {path}")
    return names


def _aidl_methods(path: Path) -> list[str]:
    methods = re.findall(
        r"^\s*(?:[A-Za-z_][A-Za-z0-9_.<>]*)\s+([A-Za-z_][A-Za-z0-9_]*)\s*\(",
        _read(path),
        re.MULTILINE,
    )
    if not methods:
        raise ValueError(f"No AIDL methods parsed: {path}")
    return methods


def collect_required_tokens(repo_root: Path) -> dict[str, list[str]]:
    java_root = repo_root / REMOTE_PACKAGE
    aidl_root = repo_root / AIDL_PACKAGE
    plugin_methods = _aidl_methods(aidl_root / "IApkBuilderTemplatePlugin.aidl")
    if "openBuildSession" not in plugin_methods:
        raise ValueError("IApkBuilderTemplatePlugin.openBuildSession was not parsed")

    return {
        "aidlMethods": [
            "openBuildSession",
            *_aidl_methods(aidl_root / "IApkBuildSession.aidl"),
            *_aidl_methods(aidl_root / "IApkBuildCallback.aidl"),
        ],
        "requestFields": _parcelable_fields(
            java_root / "ApkBuildRequest.kt",
            "ApkBuildRequest",
        ),
        "resultFields": _parcelable_fields(
            java_root / "ApkBuildResult.kt",
            "ApkBuildResult",
        ),
        "progressFields": _parcelable_fields(
            java_root / "ApkBuildProgress.kt",
            "ApkBuildProgress",
        ),
        "requestExtraKeys": _string_constants(
            java_root / "ApkBuildRequestExtraKeys.kt",
            REQUEST_EXTRA_KEY_NAMES,
        ),
        "remoteCapabilityKeys": _string_constants(
            java_root / "ApkBuilderTemplateCapabilityKeys.kt",
            REMOTE_CAPABILITY_NAMES,
        ),
        "resultStatuses": _numeric_constant_names(
            java_root / "ApkBuildResult.kt",
            "STATUS_",
        ),
        "progressSteps": _numeric_constant_names(
            java_root / "ApkBuildProgress.kt",
            "STEP_",
        ),
        "protocolConstants": ["REMOTE_BUILD_VERSION"],
    }


def find_missing_tokens(document: str, inventory: dict[str, list[str]]) -> dict[str, list[str]]:
    missing: dict[str, list[str]] = {}
    for category, tokens in inventory.items():
        absent = [token for token in tokens if token not in document]
        if absent:
            missing[category] = absent
    return missing


def verify_repo(repo_root: Path) -> dict[str, object]:
    root = repo_root.resolve()
    document_path = root / "docs/remote-build-protocol.md"
    inventory = collect_required_tokens(root)
    missing = find_missing_tokens(_read(document_path), inventory)
    return {
        "status": "PASS" if not missing else "FAIL",
        "document": document_path.relative_to(root).as_posix(),
        "categories": {category: len(tokens) for category, tokens in inventory.items()},
        "totalRequiredTokens": sum(len(tokens) for tokens in inventory.values()),
        "missing": missing,
    }


def main() -> None:
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "--repo-root",
        type=Path,
        default=Path(__file__).resolve().parents[1],
    )
    args = parser.parse_args()
    result = verify_repo(args.repo_root)
    print(json.dumps(result, ensure_ascii=False, indent=2))
    if result["status"] != "PASS":
        raise SystemExit(1)


if __name__ == "__main__":
    main()
