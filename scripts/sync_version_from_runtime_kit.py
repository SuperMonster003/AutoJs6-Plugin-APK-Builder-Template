#!/usr/bin/env python3
"""Sync host-pairing fields from a Runtime Kit and advance the plugin's own release counters.

Writes into version.properties (see docs/versioning.md):
  - VERSION_NAME / VERSION_BUILD: paired host version, taken from the Runtime Kit.
  - PLUGIN_VERSION_BUILD: global build counter, previous value + 1.
  - PLUGIN_RELEASE_SEQ: per-host release sequence (1..99), derived from compat-matrix.json
    entries for the same host (authoritative) and the previous snapshot as fallback.
  - PLUGIN_VERSION_NAME is never touched; it is maintained manually.
"""

import json
import re
import sys
from pathlib import Path

SEQ_MAX = 99


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


def get_property(text: str, key: str) -> str:
    match = re.search(rf"^{re.escape(key)}=(.*)$", text, re.MULTILINE)
    return match.group(1).strip() if match else ""


def get_int_property(text: str, key: str) -> int:
    value = get_property(text, key)
    return int(value) if value else 0


def highest_matrix_seq(matrix_path: Path, host_code: int) -> int:
    if not matrix_path.is_file():
        return 0
    entries = (json.loads(matrix_path.read_text("utf-8")) or {}).get("entries") or []
    highest = 0
    for entry in entries:
        if int(entry.get("hostVersionCode") or 0) != host_code:
            continue
        seq = int(entry.get("pluginVersionCode") or 0) - host_code * 100
        if 0 < seq <= SEQ_MAX:
            highest = max(highest, seq)
    return highest


def main() -> None:
    if len(sys.argv) != 2:
        raise SystemExit("Usage: sync_version_from_runtime_kit.py <runtime-kit-json-or-dir>")

    kit_json = resolve_runtime_kit_json(Path(sys.argv[1]).resolve())
    meta = json.loads(kit_json.read_text("utf-8"))
    host = meta.get("host") or {}
    host_version_name = str(host.get("versionName") or "").strip()
    host_version_code = str(host.get("versionCode") or "").strip()
    runtime_kit_id = str(meta.get("runtimeKitId") or "").strip()

    if not host_version_name or not host_version_code:
        raise SystemExit("Runtime Kit host.versionName/versionCode are required")
    host_code = int(host_version_code)

    version_properties = Path("version.properties")
    text = version_properties.read_text("utf-8")

    plugin_version_name = get_property(text, "PLUGIN_VERSION_NAME")
    if not plugin_version_name:
        raise SystemExit("version.properties must declare PLUGIN_VERSION_NAME (never synced from the Runtime Kit)")

    previous_host_code = get_int_property(text, "VERSION_BUILD")
    previous_seq = get_int_property(text, "PLUGIN_RELEASE_SEQ")
    previous_build = get_int_property(text, "PLUGIN_VERSION_BUILD")

    base_seq = highest_matrix_seq(Path("compat-matrix.json"), host_code)
    if previous_host_code == host_code:
        base_seq = max(base_seq, previous_seq)
    release_seq = base_seq + 1
    if release_seq > SEQ_MAX:
        raise SystemExit(f"PLUGIN_RELEASE_SEQ exhausted for host {host_code} (max {SEQ_MAX})")
    plugin_build = previous_build + 1

    text = put_property(text, "VERSION_NAME", host_version_name)
    text = put_property(text, "VERSION_BUILD", host_version_code)
    text = put_property(text, "PLUGIN_VERSION_BUILD", str(plugin_build))
    text = put_property(text, "PLUGIN_RELEASE_SEQ", str(release_seq))
    version_properties.write_text(text, "utf-8")

    gradle_properties = Path("gradle.properties")
    gradle_text = gradle_properties.read_text("utf-8") if gradle_properties.is_file() else ""
    gradle_text = put_property(gradle_text, "autojs.apkBuilder.runtimeKitId", runtime_kit_id)
    gradle_properties.write_text(gradle_text, "utf-8")

    host_slug = re.sub(r"\s+", "-", host_version_name).lower()
    print("Plugin version synced")
    print(f"  pluginVersionName: {plugin_version_name}")
    print(f"  pluginVersionBuild: {plugin_build}")
    print(f"  pluginReleaseSeq: {release_seq}")
    print(f"  pairedHostVersionName: {host_version_name}")
    print(f"  pairedHostVersionCode: {host_version_code}")
    print(f"  androidVersionCode: {host_code * 100 + release_seq}")
    print(f"  androidVersionName: {plugin_version_name}+autojs6-{host_slug}")
    print(f"  runtimeKitId: {runtime_kit_id}")


if __name__ == "__main__":
    main()
