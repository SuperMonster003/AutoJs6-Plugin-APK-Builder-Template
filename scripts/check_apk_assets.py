#!/usr/bin/env python3

import argparse
import sys
import zipfile
from pathlib import Path


def entries(apk: Path) -> set[str]:
    with zipfile.ZipFile(apk) as archive:
        return set(archive.namelist())


def fail(message: str) -> None:
    raise SystemExit(message)


def main() -> None:
    parser = argparse.ArgumentParser(description="Check AutoJs6 main/plugin APK Runtime Kit asset layout.")
    parser.add_argument("--main-apk", type=Path, help="AutoJs6 main APK to verify as plugin-only")
    parser.add_argument("--plugin-apk", type=Path, help="APK Builder plugin APK to verify")
    args = parser.parse_args()

    if not args.main_apk and not args.plugin_apk:
        fail("Pass --main-apk and/or --plugin-apk")

    if args.main_apk:
        names = entries(args.main_apk)
        forbidden = [name for name in names if name == "assets/template.apk" or name.startswith("assets/runtime-kit/")]
        if forbidden:
            fail(f"Main APK contains forbidden template assets: {forbidden[:20]}")
        print(f"Main APK OK: {args.main_apk}")

    if args.plugin_apk:
        names = entries(args.plugin_apk)
        required = [
            "assets/runtime-kit/runtime-kit.json",
            "assets/runtime-kit/template.apk",
            "assets/runtime-kit/template.apk.sha256",
            "assets/runtime-kit/default_key_store.bks",
            "assets/runtime-kit/default_key_store.bks.sha256",
        ]
        missing = [name for name in required if name not in names]
        if missing:
            fail(f"Plugin APK missing Runtime Kit assets: {missing}")
        print(f"Plugin APK OK: {args.plugin_apk}")


if __name__ == "__main__":
    main()
