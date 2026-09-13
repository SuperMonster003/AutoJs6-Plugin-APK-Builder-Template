import json
import os
from pathlib import Path
import sys
import tempfile
import unittest
from unittest.mock import patch

sys.path.insert(0, str(Path(__file__).resolve().parents[1]))
import sync_version_from_runtime_kit as sync


class SyncVersionIdentityTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.previous_cwd = Path.cwd()
        os.chdir(self.temp.name)
        self.addCleanup(os.chdir, self.previous_cwd)
        Path('kit.json').write_text(json.dumps({
            'host': {'versionName': '6.8.0', 'versionCode': 5279},
            'runtimeKitId': 'test-kit',
        }), encoding='utf-8')
        self.source = (
            'VERSION_NAME=1.0.2\nVERSION_BUILD=900\n'
            'HOST_VERSION_NAME=6.8.0\nHOST_VERSION_BUILD=5279\n'
            'PLUGIN_VERSION_NAME=1.0.2\nPLUGIN_VERSION_BUILD=900\nPLUGIN_RELEASE_SEQ=1\n'
        )
        Path('version.properties').write_text(self.source, encoding='utf-8')

    def run_sync(self):
        with patch.object(sys, 'argv', ['sync', 'kit.json']):
            sync.main()

    def test_git_count_does_not_replace_host_code_or_release_sequence(self):
        with patch.object(sync.subprocess, 'check_output', return_value='38\n') as git:
            self.run_sync()
        git.assert_called_once_with(['git', 'rev-list', '--count', 'HEAD'], text=True)
        result = Path('version.properties').read_text(encoding='utf-8')
        self.assertEqual('39', sync.get_property(result, 'VERSION_BUILD'))
        self.assertEqual('39', sync.get_property(result, 'PLUGIN_VERSION_BUILD'))
        self.assertEqual('5279', sync.get_property(result, 'HOST_VERSION_BUILD'))
        self.assertEqual('2', sync.get_property(result, 'PLUGIN_RELEASE_SEQ'))
        self.assertEqual('1.0.2', sync.get_property(result, 'VERSION_NAME'))

    def test_alias_mismatch_is_rejected_before_writing(self):
        original = self.source.replace('VERSION_NAME=1.0.2\n', 'VERSION_NAME=1.0.3\n', 1)
        Path('version.properties').write_text(original, encoding='utf-8')
        with self.assertRaisesRegex(SystemExit, 'must agree'):
            self.run_sync()
        self.assertEqual(original, Path('version.properties').read_text(encoding='utf-8'))
        self.assertFalse(Path('gradle.properties').exists())


if __name__ == '__main__':
    unittest.main()
