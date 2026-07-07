#!/usr/bin/env python3

import json
import re
import sys
from pathlib import Path


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


def put_property(text: str, key: str, value: str) -> str:
    line = f"{key}={value}"
    pattern = re.compile(rf"^{re.escape(key)}=.*$", re.MULTILINE)
    if pattern.search(text):
        return pattern.sub(line, text)
    return text.rstrip() + "\n" + line + "\n"


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("Usage: sync_version_from_runtime_kit.py <runtime-kit-json-or-dir>")

    kit_json = resolve_runtime_kit_json(Path(sys.argv[1]).resolve())
    meta = json.loads(kit_json.read_text("utf-8"))
    host = meta.get("host") or {}
    version_name = str(host.get("versionName") or "").strip()
    version_code = str(host.get("versionCode") or "").strip()
    runtime_kit_id = str(meta.get("runtimeKitId") or "").strip()

    if not version_name or not version_code:
        raise SystemExit("Runtime Kit host.versionName/versionCode are required")

    version_properties = Path("version.properties")
    text = version_properties.read_text("utf-8")
    text = put_property(text, "VERSION_NAME", version_name)
    text = put_property(text, "VERSION_BUILD", version_code)
    version_properties.write_text(text, "utf-8")

    gradle_properties = Path("gradle.properties")
    gradle_text = gradle_properties.read_text("utf-8") if gradle_properties.is_file() else ""
    gradle_text = put_property(gradle_text, "autojs.apkBuilder.runtimeKitId", runtime_kit_id)
    gradle_properties.write_text(gradle_text, "utf-8")

    print("Plugin version synced")
    print(f"  versionName: {version_name}")
    print(f"  versionCode: {version_code}")
    print(f"  runtimeKitId: {runtime_kit_id}")


if __name__ == "__main__":
    main()
