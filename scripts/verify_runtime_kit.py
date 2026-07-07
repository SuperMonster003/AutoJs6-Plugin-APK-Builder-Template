#!/usr/bin/env python3

import hashlib
import json
import sys
import zipfile
from pathlib import Path


REQUIRED_FILES = [
    "runtime-kit.json",
    "template.apk",
    "template.apk.sha256",
    "default_key_store.bks",
    "default_key_store.bks.sha256",
]

REQUIRED_APK_ENTRIES = [
    "AndroidManifest.xml",
    "resources.arsc",
    "classes.dex",
    "assets/init.js",
]


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


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("Usage: verify_runtime_kit.py <runtime-kit-dir>")

    root = resolve_runtime_kit_root(Path(sys.argv[1]).resolve())
    for name in REQUIRED_FILES:
        require((root / name).is_file(), f"Missing Runtime Kit file: {root / name}")

    meta = json.loads((root / "runtime-kit.json").read_text("utf-8"))
    require(meta.get("schemaVersion") == 1, "Unsupported Runtime Kit schemaVersion")

    contract = meta.get("contract") or {}
    protocol = int(contract.get("apkBuilderProtocolVersion") or 0)
    require(protocol >= 2, f"Runtime Kit protocol too old: {protocol}")

    template_apk = root / "template.apk"
    expected_template_sha = str((meta.get("template") or {}).get("sha256") or "").lower()
    file_template_sha = (root / "template.apk.sha256").read_text("utf-8").strip().lower()
    actual_template_sha = sha256(template_apk).lower()
    require(expected_template_sha == actual_template_sha, "template.apk SHA-256 mismatch in runtime-kit.json")
    require(file_template_sha == actual_template_sha, "template.apk SHA-256 mismatch in template.apk.sha256")

    key_store = root / "default_key_store.bks"
    expected_key_store_sha = str((meta.get("defaultKeyStore") or {}).get("sha256") or "").lower()
    file_key_store_sha = (root / "default_key_store.bks.sha256").read_text("utf-8").strip().lower()
    actual_key_store_sha = sha256(key_store).lower()
    require(expected_key_store_sha == actual_key_store_sha, "default_key_store.bks SHA-256 mismatch in runtime-kit.json")
    require(file_key_store_sha == actual_key_store_sha, "default_key_store.bks SHA-256 mismatch in default_key_store.bks.sha256")

    with zipfile.ZipFile(template_apk) as archive:
        names = set(archive.namelist())
        missing = [name for name in REQUIRED_APK_ENTRIES if name not in names]
        require(not missing, f"template.apk missing required entries: {missing}")

    print("Runtime Kit verified")
    print(f"  root: {root}")
    print(f"  runtimeKitId: {meta.get('runtimeKitId')}")
    print(f"  versionName: {(meta.get('host') or {}).get('versionName')}")
    print(f"  versionCode: {(meta.get('host') or {}).get('versionCode')}")
    print(f"  protocol: {protocol}")
    print(f"  templateSha256: {actual_template_sha}")


if __name__ == "__main__":
    main()
