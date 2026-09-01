import json
import sys
import tempfile
import unittest
import zipfile
from pathlib import Path
from typing import Optional

SCRIPTS_DIR = Path(__file__).resolve().parents[1]
sys.path.insert(0, str(SCRIPTS_DIR))

from audit_sensitive_data import AuditOptions, RootSpec, SensitiveDataAuditor, load_rules


class SensitiveDataAuditTest(unittest.TestCase):

    def setUp(self) -> None:
        self.temporary = tempfile.TemporaryDirectory()
        self.root = Path(self.temporary.name)
        self.scan_root = self.root / "scan"
        self.scan_root.mkdir()
        self.secret = "qualification-secret-value"
        self.rules_path = self.root / "rules.json"
        self.rules_path.write_text(
            json.dumps(
                {
                    "schemaVersion": 1,
                    "literals": [{"id": "test.literal", "value": self.secret}],
                }
            ),
            "utf-8",
        )

    def tearDown(self) -> None:
        self.temporary.cleanup()

    def audit(self, options: Optional[AuditOptions] = None) -> dict:
        rules = load_rules(self.rules_path)
        auditor = SensitiveDataAuditor(
            rules,
            options or AuditOptions(),
            fingerprint_key=b"test-fingerprint-key" * 2,
        )
        return auditor.audit([RootSpec("fixture", self.scan_root)])

    def test_clean_text_passes(self) -> None:
        (self.scan_root / "report.txt").write_text("qualification passed", "utf-8")

        report = self.audit()

        self.assertEqual("PASS", report["status"])
        self.assertEqual(0, report["summary"]["findings"])

    def test_literal_finding_never_echoes_matched_content(self) -> None:
        (self.scan_root / "report.txt").write_text(f"prefix {self.secret} suffix", "utf-8")

        report = self.audit()
        encoded_report = json.dumps(report)

        self.assertEqual("FAIL", report["status"])
        self.assertEqual(1, report["summary"]["findings"])
        self.assertNotIn(self.secret, encoded_report)
        self.assertEqual(24, len(report["findings"][0]["fingerprints"][0]))

    def test_sensitive_file_name_is_redacted(self) -> None:
        (self.scan_root / f"prefix-{self.secret}.txt").write_text("safe content", "utf-8")

        report = self.audit()
        encoded_report = json.dumps(report)

        self.assertEqual("FAIL", report["status"])
        self.assertNotIn(self.secret, encoded_report)
        self.assertIn("<redacted-path:", report["findings"][0]["path"])

    def test_archive_entry_content_is_scanned(self) -> None:
        archive_path = self.scan_root / "result.apk"
        with zipfile.ZipFile(archive_path, "w") as archive:
            archive.writestr("assets/project/main.js", self.secret)

        report = self.audit()

        self.assertEqual("FAIL", report["status"])
        self.assertEqual(1, report["summary"]["archiveEntriesScanned"])
        self.assertTrue(
            any(item["scope"] == "archive-entry-content" for item in report["findings"])
        )

    def test_common_token_heuristic_is_redacted(self) -> None:
        token = "ghp_" + "A" * 40
        (self.scan_root / "runner.log").write_text(token, "utf-8")

        report = self.audit()
        encoded_report = json.dumps(report)

        self.assertEqual("FAIL", report["status"])
        self.assertNotIn(token, encoded_report)
        self.assertTrue(
            any(item["ruleId"] == "credential.github-classic-token" for item in report["findings"])
        )

    def test_generic_secret_assignment_is_redacted(self) -> None:
        assignment = 'client_secret="not-a-real-client-secret"'
        (self.scan_root / "runner.log").write_text(assignment, "utf-8")

        report = self.audit()
        encoded_report = json.dumps(report)

        self.assertEqual("FAIL", report["status"])
        self.assertNotIn(assignment, encoded_report)
        self.assertTrue(
            any(item["ruleId"] == "credential.secret-assignment" for item in report["findings"])
        )

    def test_excluded_report_is_not_scanned(self) -> None:
        ignored = self.scan_root / "ignored"
        ignored.mkdir()
        (ignored / "report.txt").write_text(self.secret, "utf-8")
        (self.scan_root / "clean.txt").write_text("safe", "utf-8")

        report = self.audit(AuditOptions(exclude_globs=("ignored/**",)))

        self.assertEqual("PASS", report["status"])
        self.assertEqual(1, report["summary"]["filesScanned"])

    def test_archive_limit_error_contains_no_entry_content(self) -> None:
        archive_path = self.scan_root / "oversized.zip"
        with zipfile.ZipFile(archive_path, "w") as archive:
            archive.writestr("payload.bin", b"x" * 32)

        report = self.audit(AuditOptions(max_archive_entry_bytes=16))

        self.assertEqual("ERROR", report["status"])
        self.assertEqual("archive-entry-size-limit", report["errors"][0]["category"])


if __name__ == "__main__":
    unittest.main()
