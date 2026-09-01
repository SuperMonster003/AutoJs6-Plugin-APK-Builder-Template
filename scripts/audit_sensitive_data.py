#!/usr/bin/env python3

"""Scan qualification artifacts for sensitive data without echoing matches.

The report deliberately omits matching bytes. Each match is represented by a
run-local HMAC fingerprint whose key is discarded when the process exits. This
lets reviewers distinguish repeated findings without turning the report into a
password or token oracle.
"""

from __future__ import annotations

import argparse
import base64
import fnmatch
import hashlib
import hmac
import json
import os
import re
import secrets
import sys
import zipfile
from collections import defaultdict
from dataclasses import dataclass
from datetime import datetime, timezone
from pathlib import Path
from typing import BinaryIO, Iterable, Optional


CHUNK_SIZE = 1024 * 1024
MATCH_OVERLAP = 4096
DEFAULT_MAX_ARCHIVE_ENTRIES = 65_536
DEFAULT_MAX_ARCHIVE_ENTRY_BYTES = 512 * 1024 * 1024
DEFAULT_MAX_ARCHIVE_TOTAL_BYTES = 2 * 1024 * 1024 * 1024
ARCHIVE_SUFFIXES = {".apk", ".aar", ".jar", ".zip"}
RULE_ID_PATTERN = re.compile(r"^[a-z0-9][a-z0-9._-]{0,79}$")


@dataclass(frozen=True)
class Rule:
    rule_id: str
    pattern: re.Pattern[bytes]
    kind: str


@dataclass(frozen=True)
class RootSpec:
    label: str
    path: Path


@dataclass(frozen=True)
class AuditOptions:
    include_globs: tuple[str, ...] = ("**",)
    exclude_globs: tuple[str, ...] = ()
    max_archive_entries: int = DEFAULT_MAX_ARCHIVE_ENTRIES
    max_archive_entry_bytes: int = DEFAULT_MAX_ARCHIVE_ENTRY_BYTES
    max_archive_total_bytes: int = DEFAULT_MAX_ARCHIVE_TOTAL_BYTES
    scan_archive_heuristics: bool = False


class SensitiveDataAuditor:
    def __init__(
        self,
        rules: list[Rule],
        options: AuditOptions,
        fingerprint_key: Optional[bytes] = None,
    ) -> None:
        self.rules = rules
        self.literal_rules = [rule for rule in rules if rule.kind == "literal"]
        self.options = options
        self.fingerprint_key = fingerprint_key or secrets.token_bytes(32)
        self.findings: dict[tuple[str, str, str, str], dict] = {}
        self.errors: list[dict[str, str]] = []
        self.files_scanned = 0
        self.file_bytes_scanned = 0
        self.archive_entries_scanned = 0
        self.archive_uncompressed_bytes_scanned = 0

    def audit(self, roots: Iterable[RootSpec]) -> dict:
        root_summaries = []
        for root in roots:
            before_files = self.files_scanned
            before_bytes = self.file_bytes_scanned
            self._scan_root(root)
            root_summaries.append(
                {
                    "label": root.label,
                    "filesScanned": self.files_scanned - before_files,
                    "fileBytesScanned": self.file_bytes_scanned - before_bytes,
                }
            )

        status = "ERROR" if self.errors else "FAIL" if self.findings else "PASS"
        findings = sorted(
            self.findings.values(),
            key=lambda item: (
                item["root"],
                item["path"],
                item["scope"],
                item["ruleId"],
            ),
        )
        return {
            "schemaVersion": 1,
            "generatedAt": datetime.now(timezone.utc).isoformat(timespec="seconds"),
            "status": status,
            "privacy": {
                "matchingContentIncluded": False,
                "absoluteRootPathsIncluded": False,
                "fingerprint": "HMAC-SHA-256 with an unretained per-run key",
            },
            "rules": [
                {"id": rule.rule_id, "kind": rule.kind}
                for rule in sorted(self.rules, key=lambda item: item.rule_id)
            ],
            "roots": root_summaries,
            "summary": {
                "filesScanned": self.files_scanned,
                "fileBytesScanned": self.file_bytes_scanned,
                "archiveEntriesScanned": self.archive_entries_scanned,
                "archiveUncompressedBytesScanned": self.archive_uncompressed_bytes_scanned,
                "findings": sum(item["count"] for item in findings),
                "findingGroups": len(findings),
                "errors": len(self.errors),
            },
            "findings": findings,
            "errors": sorted(
                self.errors,
                key=lambda item: (item["root"], item["path"], item["category"]),
            ),
        }

    def _scan_root(self, root: RootSpec) -> None:
        resolved = root.path.resolve()
        if resolved.is_file():
            self._scan_file(root.label, resolved.name, resolved)
            return
        if not resolved.is_dir():
            self._record_error(root.label, ".", "missing-root")
            return

        for directory, child_directories, file_names in os.walk(resolved, followlinks=False):
            current = Path(directory)
            child_directories[:] = sorted(
                child
                for child in child_directories
                if not self._excluded(_relative_path(resolved, current / child) + "/")
            )
            for file_name in sorted(file_names):
                path = current / file_name
                relative = _relative_path(resolved, path)
                if self._included(relative) and not self._excluded(relative):
                    self._scan_file(root.label, relative, path)

    def _scan_file(self, root_label: str, relative: str, path: Path) -> None:
        if path.is_symlink():
            self._record_error(root_label, relative, "symbolic-link")
            return
        try:
            size = path.stat().st_size
            with path.open("rb") as handle:
                sample = handle.read(8192)
                handle.seek(0)
                text_like = _is_probably_text(sample)
                rules = self.rules if text_like else self.literal_rules
                self._scan_stream(root_label, relative, "file-content", handle, rules)
            self.files_scanned += 1
            self.file_bytes_scanned += size
            self._scan_path(root_label, relative, "file-path")
        except OSError:
            self._record_error(root_label, relative, "file-read")
            return

        if path.suffix.lower() not in ARCHIVE_SUFFIXES:
            return
        try:
            if zipfile.is_zipfile(path):
                self._scan_archive(root_label, relative, path)
        except (OSError, zipfile.BadZipFile, RuntimeError):
            self._record_error(root_label, relative, "archive-open")

    def _scan_archive(self, root_label: str, relative: str, path: Path) -> None:
        seen_names: set[str] = set()
        total_uncompressed = 0
        with zipfile.ZipFile(path) as archive:
            entries = archive.infolist()
            if len(entries) > self.options.max_archive_entries:
                self._record_error(root_label, relative, "archive-entry-limit")
                return
            for entry in entries:
                if entry.filename in seen_names:
                    self._record_error(root_label, relative, "archive-duplicate-entry")
                    return
                seen_names.add(entry.filename)
                self._scan_path(root_label, f"{relative}!/{entry.filename}", "archive-entry-path")
                if entry.is_dir():
                    continue
                if entry.file_size < 0 or entry.file_size > self.options.max_archive_entry_bytes:
                    self._record_error(root_label, relative, "archive-entry-size-limit")
                    return
                total_uncompressed += entry.file_size
                if total_uncompressed > self.options.max_archive_total_bytes:
                    self._record_error(root_label, relative, "archive-total-size-limit")
                    return
                rules = self.rules if self.options.scan_archive_heuristics else self.literal_rules
                try:
                    with archive.open(entry) as handle:
                        self._scan_stream(
                            root_label,
                            f"{relative}!/{entry.filename}",
                            "archive-entry-content",
                            handle,
                            rules,
                        )
                except (OSError, RuntimeError, zipfile.BadZipFile):
                    self._record_error(root_label, relative, "archive-entry-read")
                    return
                self.archive_entries_scanned += 1
                self.archive_uncompressed_bytes_scanned += entry.file_size

    def _scan_stream(
        self,
        root_label: str,
        relative: str,
        scope: str,
        handle: BinaryIO,
        rules: list[Rule],
    ) -> None:
        if not rules:
            return
        tail = b""
        consumed = 0
        seen: set[tuple[str, int]] = set()
        while True:
            chunk = handle.read(CHUNK_SIZE)
            if not chunk:
                break
            data = tail + chunk
            base_offset = consumed - len(tail)
            for rule in rules:
                for match in rule.pattern.finditer(data):
                    absolute_offset = base_offset + match.start()
                    match_key = (rule.rule_id, absolute_offset)
                    if match_key in seen:
                        continue
                    seen.add(match_key)
                    self._record_finding(
                        root_label,
                        relative,
                        scope,
                        rule,
                        match.group(0),
                    )
            consumed += len(chunk)
            tail = data[-MATCH_OVERLAP:]

    def _scan_path(self, root_label: str, relative: str, scope: str) -> None:
        encoded = relative.encode("utf-8", errors="replace")
        for rule in self.rules:
            for match in rule.pattern.finditer(encoded):
                self._record_finding(root_label, relative, scope, rule, match.group(0))

    def _record_finding(
        self,
        root_label: str,
        relative: str,
        scope: str,
        rule: Rule,
        matched: bytes,
    ) -> None:
        safe_path = self._safe_path(relative)
        key = (root_label, safe_path, scope, rule.rule_id)
        finding = self.findings.setdefault(
            key,
            {
                "root": root_label,
                "path": safe_path,
                "scope": scope,
                "ruleId": rule.rule_id,
                "count": 0,
                "fingerprints": [],
            },
        )
        finding["count"] += 1
        fingerprint = self._fingerprint(rule.rule_id.encode() + b"\0" + matched)
        if fingerprint not in finding["fingerprints"] and len(finding["fingerprints"]) < 5:
            finding["fingerprints"].append(fingerprint)

    def _record_error(self, root_label: str, relative: str, category: str) -> None:
        self.errors.append(
            {
                "root": root_label,
                "path": self._safe_path(relative),
                "category": category,
            }
        )

    def _safe_path(self, relative: str) -> str:
        encoded = relative.encode("utf-8", errors="replace")
        if any(rule.pattern.search(encoded) for rule in self.rules):
            return f"<redacted-path:{self._fingerprint(encoded)}>"
        return relative

    def _fingerprint(self, value: bytes) -> str:
        return hmac.new(self.fingerprint_key, value, hashlib.sha256).hexdigest()[:24]

    def _included(self, relative: str) -> bool:
        return any(_glob_match(relative, pattern) for pattern in self.options.include_globs)

    def _excluded(self, relative: str) -> bool:
        return any(_glob_match(relative, pattern) for pattern in self.options.exclude_globs)


def _relative_path(root: Path, path: Path) -> str:
    return path.relative_to(root).as_posix()


def _glob_match(relative: str, pattern: str) -> bool:
    normalized = relative.replace("\\", "/")
    normalized_pattern = pattern.replace("\\", "/")
    return fnmatch.fnmatchcase(normalized, normalized_pattern)


def _is_probably_text(sample: bytes) -> bool:
    if not sample:
        return True
    if sample.startswith((b"\xff\xfe", b"\xfe\xff", b"\xef\xbb\xbf")):
        return True
    if b"\0" in sample:
        return False
    controls = sum(byte < 9 or 13 < byte < 32 for byte in sample)
    return controls * 100 <= len(sample)


def load_rules(path: Path, include_heuristics: bool = True) -> list[Rule]:
    try:
        document = json.loads(path.read_text("utf-8"))
    except (OSError, UnicodeError, json.JSONDecodeError) as error:
        raise SystemExit("Unable to read sensitive-data rules.") from error
    if document.get("schemaVersion") != 1:
        raise SystemExit("Unsupported sensitive-data rule schema.")

    rules: list[Rule] = []
    seen_ids: set[str] = set()
    for item in document.get("literals", []):
        rule_id = item.get("id")
        if not isinstance(rule_id, str) or not RULE_ID_PATTERN.fullmatch(rule_id):
            raise SystemExit("Sensitive-data rule has an invalid id.")
        if rule_id in seen_ids:
            raise SystemExit("Sensitive-data rule ids must be unique.")
        seen_ids.add(rule_id)
        value = _decode_literal(item)
        if len(value) < 8:
            raise SystemExit(f"Sensitive-data literal is too short: {rule_id}")
        rules.append(Rule(rule_id, re.compile(re.escape(value)), "literal"))

    if include_heuristics:
        for rule_id, pattern in _heuristic_patterns():
            if rule_id in seen_ids:
                raise SystemExit("Sensitive-data rule ids must be unique.")
            seen_ids.add(rule_id)
            rules.append(Rule(rule_id, re.compile(pattern, re.IGNORECASE), "heuristic"))
    return rules


def _decode_literal(item: dict) -> bytes:
    encodings = [key for key in ("value", "valueHex", "valueBase64") if key in item]
    if len(encodings) != 1:
        raise SystemExit("Each sensitive-data literal requires exactly one encoded value.")
    try:
        if encodings[0] == "value":
            value = item["value"]
            if not isinstance(value, str):
                raise ValueError
            return value.encode("utf-8")
        if encodings[0] == "valueHex":
            return bytes.fromhex(item["valueHex"])
        return base64.b64decode(item["valueBase64"], validate=True)
    except (KeyError, TypeError, ValueError) as error:
        raise SystemExit("Sensitive-data literal encoding is invalid.") from error


def _heuristic_patterns() -> list[tuple[str, bytes]]:
    return [
        ("credential.pem-private-key", rb"-----BEGIN (?:RSA |EC |OPENSSH )?PRIVATE KEY-----"),
        ("credential.github-classic-token", rb"\bgh[pousr]_[A-Za-z0-9]{36,255}\b"),
        ("credential.github-fine-grained-token", rb"\bgithub_pat_[A-Za-z0-9_]{50,255}\b"),
        ("credential.aws-access-key", rb"\b(?:AKIA|ASIA)[A-Z0-9]{16}\b"),
        ("credential.google-api-key", rb"\bAIza[0-9A-Za-z_-]{35}\b"),
        ("credential.slack-token", rb"\bxox[baprs]-[0-9A-Za-z-]{20,255}\b"),
        ("credential.stripe-live-key", rb"\bsk_live_[0-9A-Za-z]{16,255}\b"),
        (
            "credential.jwt",
            rb"\beyJ[0-9A-Za-z_-]{8,}\.[0-9A-Za-z_-]{8,}\.[0-9A-Za-z_-]{8,}\b",
        ),
        (
            "credential.basic-auth-url",
            rb"https?://[^\s/:@]{2,128}:[^\s/@]{4,256}@[^\s/]+",
        ),
        (
            "credential.bearer-token",
            rb"\bBearer[ \t]+[0-9A-Za-z._~+/-]{20,255}={0,2}\b",
        ),
        (
            "credential.secret-assignment",
            rb"\b(?:password|passwd|api[_-]?key|access[_-]?token|client[_-]?secret)\b"
            rb"[ \t]*[:=][ \t]*(?:\"[^\"\r\n]{8,256}\"|'[^'\r\n]{8,256}'|[^\s,;]{8,256})",
        ),
    ]


def parse_root(value: str) -> RootSpec:
    label, separator, raw_path = value.partition("=")
    if not separator or not RULE_ID_PATTERN.fullmatch(label) or not raw_path:
        raise argparse.ArgumentTypeError("Root must use label=path with a safe label.")
    return RootSpec(label=label, path=Path(raw_path))


def parse_args(argv=None) -> argparse.Namespace:
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--rules", type=Path, required=True)
    parser.add_argument("--root", action="append", type=parse_root, required=True)
    parser.add_argument("--include", action="append", default=[])
    parser.add_argument("--exclude", action="append", default=[])
    parser.add_argument("--output", type=Path, required=True)
    parser.add_argument("--no-heuristics", action="store_true")
    parser.add_argument("--scan-archive-heuristics", action="store_true")
    parser.add_argument("--max-archive-entries", type=int, default=DEFAULT_MAX_ARCHIVE_ENTRIES)
    parser.add_argument(
        "--max-archive-entry-bytes",
        type=int,
        default=DEFAULT_MAX_ARCHIVE_ENTRY_BYTES,
    )
    parser.add_argument(
        "--max-archive-total-bytes",
        type=int,
        default=DEFAULT_MAX_ARCHIVE_TOTAL_BYTES,
    )
    return parser.parse_args(argv)


def main(argv=None) -> int:
    args = parse_args(argv)
    if min(
        args.max_archive_entries,
        args.max_archive_entry_bytes,
        args.max_archive_total_bytes,
    ) <= 0:
        raise SystemExit("Archive limits must be positive.")
    rules = load_rules(args.rules, include_heuristics=not args.no_heuristics)
    options = AuditOptions(
        include_globs=tuple(args.include or ["**"]),
        exclude_globs=tuple(args.exclude),
        max_archive_entries=args.max_archive_entries,
        max_archive_entry_bytes=args.max_archive_entry_bytes,
        max_archive_total_bytes=args.max_archive_total_bytes,
        scan_archive_heuristics=args.scan_archive_heuristics,
    )
    report = SensitiveDataAuditor(rules, options).audit(args.root)
    args.output.parent.mkdir(parents=True, exist_ok=True)
    temporary = args.output.with_name(args.output.name + ".tmp")
    temporary.write_text(json.dumps(report, indent=2, sort_keys=True) + "\n", "utf-8")
    temporary.replace(args.output)
    summary = report["summary"]
    print(
        "Sensitive-data audit: "
        f"{report['status']}; files={summary['filesScanned']}; "
        f"archive_entries={summary['archiveEntriesScanned']}; "
        f"findings={summary['findings']}; errors={summary['errors']}"
    )
    if report["status"] == "PASS":
        return 0
    return 1 if report["status"] == "FAIL" else 2


if __name__ == "__main__":
    sys.exit(main())
