import json
from pathlib import Path
import tempfile
import unittest
import zipfile

from release_archive import ReleaseError, paired_host_filename


class PairedHostArchiveTest(unittest.TestCase):
    def setUp(self):
        self.temp = tempfile.TemporaryDirectory()
        self.addCleanup(self.temp.cleanup)
        self.root = Path(self.temp.name)

    def apk(self, abi='universal', filename_abi=None):
        path = self.root / f'autojs6-apk-builder-template-v1.0.2-autojs6-v6.8.0-{filename_abi or abi}.apk'
        with zipfile.ZipFile(path, 'w') as archive:
            archive.writestr('assets/runtime-kit/runtime-kit.json', json.dumps({
                'host': {'versionName': '6.8.0'},
                'template': {'variant': 'inrt-' + abi},
            }))
        return path

    def test_all_five_kit_names_retain_the_published_pairing_contract(self):
        for abi in ['universal', 'arm64-v8a', 'armeabi-v7a', 'x86_64', 'x86']:
            with self.subTest(abi=abi):
                name = paired_host_filename(self.apk(abi), '1.0.2+autojs6-6.8.0', 'AABBCCDD')
                self.assertEqual(f'autojs6-apk-builder-template-v1.0.2-autojs6-v6.8.0-{abi}-aabbccdd.apk', name)

    def test_wrong_kit_host_and_wrong_filename_are_rejected(self):
        with self.assertRaisesRegex(ReleaseError, 'host differs'):
            paired_host_filename(self.apk(), '1.0.2+autojs6-6.9.0', 'AABBCCDD')
        with self.assertRaisesRegex(ReleaseError, 'filename differs'):
            paired_host_filename(self.apk('arm64-v8a', 'universal'), '1.0.2+autojs6-6.8.0', 'AABBCCDD')


if __name__ == '__main__':
    unittest.main()
