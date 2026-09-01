import sys
import unittest
from pathlib import Path


SCRIPTS_DIR = Path(__file__).resolve().parents[1]
REPO_ROOT = SCRIPTS_DIR.parent
sys.path.insert(0, str(SCRIPTS_DIR))

from verify_remote_build_protocol_docs import (  # noqa: E402
    collect_required_tokens,
    find_missing_tokens,
    verify_repo,
)


class RemoteBuildProtocolDocsTest(unittest.TestCase):

    def test_current_protocol_document_covers_authoritative_surface(self) -> None:
        result = verify_repo(REPO_ROOT)
        self.assertEqual("PASS", result["status"], result["missing"])
        self.assertEqual(101, result["totalRequiredTokens"])
        self.assertEqual(
            {
                "aidlMethods": 11,
                "requestFields": 20,
                "resultFields": 10,
                "progressFields": 7,
                "requestExtraKeys": 11,
                "keyStoreRequestFields": 17,
                "keyStoreResultFields": 2,
                "buildCapabilityKeys": 9,
                "resultStatuses": 4,
                "progressSteps": 6,
                "protocolConstants": 4,
            },
            result["categories"],
        )

    def test_missing_field_is_reported_without_source_content(self) -> None:
        inventory = collect_required_tokens(REPO_ROOT)
        document = (REPO_ROOT / "docs/remote-build-protocol.md").read_text("utf-8")
        document = document.replace("outputSha256", "redacted-field")
        missing = find_missing_tokens(document, inventory)
        self.assertEqual(["outputSha256"], missing["resultFields"])


if __name__ == "__main__":
    unittest.main()
