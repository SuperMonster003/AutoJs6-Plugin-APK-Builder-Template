#!/usr/bin/env python3

"""Measure exact delta-update reuse between two Runtime Kit artifacts.

The script is intentionally an evaluation tool, not a production patch format. It
combines ZIP-entry analysis with Git's binary-delta encoder, then applies the
forward-only patch in an isolated temporary directory and verifies the rebuilt
artifact byte-for-byte by SHA-256.
"""

import argparse
import hashlib
import json
import re
import shutil
import struct
import subprocess
import sys
import tempfile
import zipfile
from collections import defaultdict
from pathlib import Path


BUFFER_SIZE = 1024 * 1024
LOCAL_FILE_HEADER = struct.Struct("<IHHHHHIIIHH")
LOCAL_FILE_HEADER_SIGNATURE = 0x04034B50
BINARY_PATCH_MARKER = b"GIT binary patch\n"
BINARY_BLOCK_PATTERN = re.compile(rb"(delta|literal) ([0-9]+)\r?\n")


def require(condition: bool, message: str) -> None:
    if not condition:
        raise SystemExit(message)


def sha256(path: Path) -> str:
    digest = hashlib.sha256()
    with path.open("rb") as handle:
        for chunk in iter(lambda: handle.read(BUFFER_SIZE), b""):
            digest.update(chunk)
    return digest.hexdigest()


def digest_stream(handle) -> str:
    digest = hashlib.sha256()
    for chunk in iter(lambda: handle.read(BUFFER_SIZE), b""):
        digest.update(chunk)
    return digest.hexdigest()


def resolve_artifact(candidate: Path) -> Path:
    candidate = candidate.resolve()
    if candidate.is_file():
        return candidate
    require(candidate.is_dir(), f"Artifact does not exist: {candidate}")

    direct = candidate / "template.apk"
    if direct.is_file():
        return direct

    nested = sorted(candidate.glob("*/template.apk"))
    require(
        len(nested) == 1,
        f"Expected one template.apk below {candidate}, found {len(nested)}",
    )
    return nested[0]


def display_bytes(value: int) -> str:
    units = ("B", "KiB", "MiB", "GiB")
    amount = float(value)
    for unit in units:
        if abs(amount) < 1024.0 or unit == units[-1]:
            return f"{amount:.2f} {unit}"
        amount /= 1024.0
    raise AssertionError("unreachable")


def percent(part: int, whole: int) -> float:
    return round(part * 100.0 / whole, 3) if whole else 0.0


def zip_entry_category(name: str) -> str:
    if name.startswith("META-INF/"):
        return "META-INF"
    if re.fullmatch(r"classes[0-9]*\.dex", name):
        return "dex"
    if name.startswith("lib/"):
        return "native-libs"
    if name.startswith("assets/"):
        return "assets"
    if name.startswith("res/"):
        return "resources"
    if name == "resources.arsc":
        return "resources.arsc"
    if name == "AndroidManifest.xml":
        return "manifest"
    return "other"


def read_compressed_payload_digest(archive_path: Path, info: zipfile.ZipInfo) -> str:
    with archive_path.open("rb") as handle:
        handle.seek(info.header_offset)
        header = handle.read(LOCAL_FILE_HEADER.size)
        require(
            len(header) == LOCAL_FILE_HEADER.size,
            f"Truncated local ZIP header for {info.filename} in {archive_path}",
        )
        fields = LOCAL_FILE_HEADER.unpack(header)
        require(
            fields[0] == LOCAL_FILE_HEADER_SIGNATURE,
            f"Invalid local ZIP header for {info.filename} in {archive_path}",
        )
        file_name_length = fields[-2]
        extra_length = fields[-1]
        handle.seek(file_name_length + extra_length, 1)

        remaining = info.compress_size
        digest = hashlib.sha256()
        while remaining:
            chunk = handle.read(min(BUFFER_SIZE, remaining))
            require(
                bool(chunk),
                f"Truncated compressed payload for {info.filename} in {archive_path}",
            )
            digest.update(chunk)
            remaining -= len(chunk)
        return digest.hexdigest()


def inspect_zip(path: Path) -> dict[str, dict]:
    require(zipfile.is_zipfile(path), f"Not a valid ZIP artifact: {path}")
    entries = {}
    with zipfile.ZipFile(path) as archive:
        for info in archive.infolist():
            if info.is_dir():
                continue
            require(info.filename not in entries, f"Duplicate ZIP entry: {info.filename} in {path}")
            with archive.open(info) as handle:
                content_digest = digest_stream(handle)
            entries[info.filename] = {
                "name": info.filename,
                "sizeBytes": info.file_size,
                "compressedSizeBytes": info.compress_size,
                "compressType": info.compress_type,
                "crc32": f"{info.CRC:08x}",
                "contentSha256": content_digest,
                "compressedPayloadSha256": read_compressed_payload_digest(path, info),
                "category": zip_entry_category(info.filename),
            }
    return entries


def compare_zip_entries(old_path: Path, new_path: Path) -> dict:
    old_entries = inspect_zip(old_path)
    new_entries = inspect_zip(new_path)
    old_names = set(old_entries)
    new_names = set(new_entries)

    unchanged_content = set()
    unchanged_compressed_payload = set()
    recompressed_only = set()
    changed = set()
    for name in old_names & new_names:
        old = old_entries[name]
        new = new_entries[name]
        same_content = (
            old["sizeBytes"] == new["sizeBytes"]
            and old["contentSha256"] == new["contentSha256"]
        )
        same_compressed_payload = (
            old["compressedSizeBytes"] == new["compressedSizeBytes"]
            and old["compressedPayloadSha256"] == new["compressedPayloadSha256"]
        )
        if same_content:
            unchanged_content.add(name)
            if same_compressed_payload:
                unchanged_compressed_payload.add(name)
            else:
                recompressed_only.add(name)
        else:
            changed.add(name)

    added = new_names - old_names
    deleted = old_names - new_names
    target_compressed_bytes = sum(entry["compressedSizeBytes"] for entry in new_entries.values())
    reusable_compressed_bytes = sum(
        new_entries[name]["compressedSizeBytes"] for name in unchanged_compressed_payload
    )
    reusable_content_bytes = sum(new_entries[name]["sizeBytes"] for name in unchanged_content)
    target_content_bytes = sum(entry["sizeBytes"] for entry in new_entries.values())

    category_totals = defaultdict(
        lambda: {
            "entryCount": 0,
            "targetContentBytes": 0,
            "targetCompressedBytes": 0,
            "reusableCompressedBytes": 0,
            "unchangedContentEntries": 0,
        }
    )
    for name, entry in new_entries.items():
        category = category_totals[entry["category"]]
        category["entryCount"] += 1
        category["targetContentBytes"] += entry["sizeBytes"]
        category["targetCompressedBytes"] += entry["compressedSizeBytes"]
        if name in unchanged_content:
            category["unchangedContentEntries"] += 1
        if name in unchanged_compressed_payload:
            category["reusableCompressedBytes"] += entry["compressedSizeBytes"]

    categories = {}
    for category_name in sorted(category_totals):
        values = dict(category_totals[category_name])
        values["compressedPayloadReusePercent"] = percent(
            values["reusableCompressedBytes"], values["targetCompressedBytes"]
        )
        categories[category_name] = values

    changed_or_added = changed | added | recompressed_only
    largest_changed_entries = []
    for name in sorted(
        changed_or_added,
        key=lambda item: (-new_entries[item]["compressedSizeBytes"], item),
    )[:20]:
        if name in added:
            status = "added"
        elif name in recompressed_only:
            status = "recompressed"
        else:
            status = "changed"
        entry = new_entries[name]
        largest_changed_entries.append(
            {
                "name": name,
                "status": status,
                "category": entry["category"],
                "sizeBytes": entry["sizeBytes"],
                "compressedSizeBytes": entry["compressedSizeBytes"],
            }
        )

    return {
        "oldEntryCount": len(old_entries),
        "newEntryCount": len(new_entries),
        "unchangedContentEntryCount": len(unchanged_content),
        "unchangedCompressedPayloadEntryCount": len(unchanged_compressed_payload),
        "recompressedOnlyEntryCount": len(recompressed_only),
        "changedEntryCount": len(changed),
        "addedEntryCount": len(added),
        "deletedEntryCount": len(deleted),
        "targetContentBytes": target_content_bytes,
        "reusableContentBytes": reusable_content_bytes,
        "contentReusePercent": percent(reusable_content_bytes, target_content_bytes),
        "targetCompressedPayloadBytes": target_compressed_bytes,
        "reusableCompressedPayloadBytes": reusable_compressed_bytes,
        "compressedPayloadReusePercent": percent(
            reusable_compressed_bytes, target_compressed_bytes
        ),
        "categories": categories,
        "largestChangedEntries": largest_changed_entries,
    }


def run_git(arguments: list[str], cwd: Path, **kwargs) -> subprocess.CompletedProcess:
    command = ["git", *arguments]
    result = subprocess.run(command, cwd=cwd, check=False, **kwargs)
    if result.returncode != 0:
        stderr = result.stderr.decode("utf-8", "replace") if result.stderr else ""
        raise SystemExit(f"Command failed ({result.returncode}): {' '.join(command)}\n{stderr}")
    return result


def trim_to_forward_binary_patch(patch: bytes) -> tuple[bytes, str, int]:
    marker_offset = patch.find(BINARY_PATCH_MARKER)
    require(marker_offset >= 0, "Git did not emit a binary patch")
    block_offset = marker_offset + len(BINARY_PATCH_MARKER)
    block_match = BINARY_BLOCK_PATTERN.match(patch, block_offset)
    require(block_match is not None, "Git binary patch is missing its forward block")

    block_end = patch.find(b"\n\n", block_match.end())
    require(block_end >= 0, "Git binary patch forward block is not terminated")
    forward_patch = patch[: block_end + 2]
    kind = block_match.group(1).decode("ascii")
    inflated_size = int(block_match.group(2))
    return forward_patch, kind, inflated_size


def measure_git_binary_delta(old_path: Path, new_path: Path, expected_new_sha256: str) -> dict:
    require(shutil.which("git") is not None, "Git is required for binary-delta measurement")

    with tempfile.TemporaryDirectory(prefix="autojs6-runtime-kit-delta-") as temp_name:
        root = Path(temp_name)
        repository = root / "repository"
        verify_root = root / "verify"
        repository.mkdir()
        verify_root.mkdir()

        run_git(["init", "--quiet"], repository, stdout=subprocess.PIPE, stderr=subprocess.PIPE)
        run_git(["config", "user.name", "AutoJs6 Delta Evaluation"], repository)
        run_git(["config", "user.email", "delta-evaluation@invalid.local"], repository)
        run_git(["config", "commit.gpgsign", "false"], repository)
        run_git(["config", "core.autocrlf", "false"], repository)

        payload = repository / "payload.bin"
        shutil.copyfile(old_path, payload)
        run_git(["add", "--", payload.name], repository)
        run_git(
            ["commit", "--quiet", "-m", "base artifact"],
            repository,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        shutil.copyfile(new_path, payload)

        full_patch_path = root / "full.patch"
        with full_patch_path.open("wb") as output:
            run_git(
                ["diff", "--binary", "--full-index", "--no-renames", "HEAD", "--", payload.name],
                repository,
                stdout=output,
                stderr=subprocess.PIPE,
            )

        full_patch = full_patch_path.read_bytes()
        forward_patch, kind, inflated_size = trim_to_forward_binary_patch(full_patch)
        forward_patch_path = root / "forward.patch"
        forward_patch_path.write_bytes(forward_patch)

        verify_payload = verify_root / payload.name
        shutil.copyfile(old_path, verify_payload)
        run_git(
            ["apply", "--binary", "--", str(forward_patch_path)],
            verify_root,
            stdout=subprocess.PIPE,
            stderr=subprocess.PIPE,
        )
        rebuilt_sha256 = sha256(verify_payload)
        require(
            rebuilt_sha256 == expected_new_sha256,
            "Git binary patch verification failed: rebuilt artifact SHA-256 differs from target",
        )

        return {
            "encoder": "git diff --binary (forward block only)",
            "patchKind": kind,
            "inflatedForwardInstructionBytes": inflated_size,
            "forwardPatchBytes": len(forward_patch),
            "fullReversiblePatchBytes": len(full_patch),
            "targetBytes": new_path.stat().st_size,
            "forwardPatchToTargetPercent": percent(len(forward_patch), new_path.stat().st_size),
            "netTransferSavingsBytes": new_path.stat().st_size - len(forward_patch),
            "netTransferSavingsPercent": round(
                100.0 - percent(len(forward_patch), new_path.stat().st_size), 3
            ),
            "rebuiltSha256": rebuilt_sha256,
            "verifiedByteExact": True,
        }


def artifact_record(path: Path, label: str) -> dict:
    return {
        "label": label,
        "path": str(path),
        "sizeBytes": path.stat().st_size,
        "sha256": sha256(path),
    }


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description=(
            "Measure ZIP-entry reuse and an exact Git binary-delta proxy between two "
            "Runtime Kit artifacts. Directory arguments resolve to template.apk."
        )
    )
    parser.add_argument("old", type=Path, help="Old artifact file or Runtime Kit directory")
    parser.add_argument("new", type=Path, help="New artifact file or Runtime Kit directory")
    parser.add_argument("--old-label", default="old", help="Human-readable old artifact label")
    parser.add_argument("--new-label", default="new", help="Human-readable new artifact label")
    parser.add_argument("--json-out", type=Path, help="Optional path for the full JSON report")
    parser.add_argument(
        "--skip-git-delta",
        action="store_true",
        help="Skip exact Git binary-delta generation and replay",
    )
    return parser.parse_args()


def main() -> None:
    args = parse_args()
    old_path = resolve_artifact(args.old)
    new_path = resolve_artifact(args.new)
    require(old_path != new_path, "Old and new artifacts resolve to the same path")

    old_record = artifact_record(old_path, args.old_label)
    new_record = artifact_record(new_path, args.new_label)
    report = {
        "schemaVersion": 1,
        "old": old_record,
        "new": new_record,
    }

    if zipfile.is_zipfile(old_path) and zipfile.is_zipfile(new_path):
        report["zipEntries"] = compare_zip_entries(old_path, new_path)

    if not args.skip_git_delta:
        report["gitBinaryDelta"] = measure_git_binary_delta(
            old_path,
            new_path,
            new_record["sha256"],
        )

    if args.json_out:
        output_path = args.json_out.resolve()
        output_path.parent.mkdir(parents=True, exist_ok=True)
        output_path.write_text(json.dumps(report, indent=2, ensure_ascii=False) + "\n", "utf-8")

    print("Runtime Kit delta analysis")
    print(
        f"  old: {old_record['label']} | {display_bytes(old_record['sizeBytes'])} | "
        f"{old_record['sha256']}"
    )
    print(
        f"  new: {new_record['label']} | {display_bytes(new_record['sizeBytes'])} | "
        f"{new_record['sha256']}"
    )
    zip_report = report.get("zipEntries")
    if zip_report:
        print(
            "  ZIP entries: "
            f"unchanged-content={zip_report['unchangedContentEntryCount']}, "
            f"changed={zip_report['changedEntryCount']}, "
            f"added={zip_report['addedEntryCount']}, deleted={zip_report['deletedEntryCount']}"
        )
        print(
            "  exact compressed-payload reuse: "
            f"{display_bytes(zip_report['reusableCompressedPayloadBytes'])} / "
            f"{display_bytes(zip_report['targetCompressedPayloadBytes'])} "
            f"({zip_report['compressedPayloadReusePercent']:.3f}%)"
        )
    delta_report = report.get("gitBinaryDelta")
    if delta_report:
        print(
            "  exact forward binary patch: "
            f"{display_bytes(delta_report['forwardPatchBytes'])} "
            f"({delta_report['forwardPatchToTargetPercent']:.3f}% of target, "
            f"{delta_report['netTransferSavingsPercent']:.3f}% net savings, "
            f"kind={delta_report['patchKind']})"
        )
        print(f"  replay SHA-256 verified: {delta_report['verifiedByteExact']}")
    if args.json_out:
        print(f"  JSON report: {args.json_out.resolve()}")


if __name__ == "__main__":
    try:
        main()
    except KeyboardInterrupt:
        raise SystemExit(130)
