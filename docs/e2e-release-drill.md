# E2E Release Drill

Use a temporary test tag before the first production tag.

## CI Checks

1. Run the AutoJs6 main repository `release-runtime-kit.yml` workflow with a test tag.
2. Confirm the main repository Release contains `autojs6-runtime-kit-*.zip`.
3. Confirm `repository_dispatch` triggers this repository's `build-from-runtime-kit.yml`.
4. Confirm this repository Release contains `autojs6-apk-builder-template-v*-universal.apk`.
5. Confirm AutoJs6 Plugin Center points to `SuperMonster003/AutoJs6-Plugin-APK-Builder`.

## Local APK Checks

```powershell
python scripts\check_apk_assets.py `
  --main-apk D:\idea-projects\AutoJs6\app\build\outputs\apk\app\release\autojs6-v6.7.1-alpha4-universal.apk `
  --plugin-apk app\build\outputs\apk\release\autojs6-apk-builder-template-v6.7.1-alpha4-universal.apk
```

Expected:

```text
Main APK does not contain assets/template.apk or assets/runtime-kit/*
Plugin APK contains assets/runtime-kit/template.apk and runtime-kit.json
```

## Device Scenarios

1. Install only AutoJs6.
   Expected: packaging entry prompts to install or enable APK Builder Template plugin.
2. Install AutoJs6 and matching APK Builder Template plugin.
   Expected: packaging succeeds without compatibility warning.
3. Install AutoJs6 and an older plugin.
   Expected: soft warning or hard failure based on protocol/hash compatibility.
4. Install a plugin with a damaged Runtime Kit or SHA mismatch.
   Expected: hard failure, packaging is blocked.
5. Package a project that selects external native/plugin modules such as Paddle OCR, MediaInfo, or Barcode.
   Expected: existing native libs/assets extraction still works.
