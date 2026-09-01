# E2E Release Drill

Use the isolated signed-candidate path before the first production tag. Candidate mode must not create a GitHub Release,
move a tag, update `compat-matrix.json`, or advance the tracked plugin release sequence.

## Isolated Signed Candidate

1. Push the reviewed AutoJs6 candidate to a non-default branch. Run `release-runtime-kit.yml` from that branch with an RC
   label, `candidate_only=true`, and `source_ref` set to the exact 40-character candidate commit.
2. Confirm the host run checks out that exact commit and retains `autojs6-runtime-kit` only as a 14-day Actions artifact.
   Confirm that it creates no Release and sends no downstream dispatch.
3. Run this repository's `build-from-runtime-kit.yml` from the reviewed release branch with the same RC label,
   `candidate_only=true`, the private host repository, host run ID, artifact name, and `expected_source_sha`.
4. Confirm Runtime Kit set validation rejects any source SHA other than `expected_source_sha`, then confirm all five APKs
   pass version, signature certificate, embedded asset, and CRC32 checks.
5. Download `apk-builder-template-signed-candidate`. Its evidence manifest must report `candidateOnly=true`, use
   `actions-artifact` for both publication channels, bind both Actions run URLs, expose no Release/APK download URL, and
   retain `remoteBuildEnabledByDefault=false`.
6. Confirm this repository still has no RC/GA Release for that label and that tracked `version.properties` and
   `compat-matrix.json` are unchanged. Only after device acceptance and an explicit GA decision may the formal path run.

## Production CI Checks

1. Run the AutoJs6 main repository `release-runtime-kit.yml` workflow with the production tag and `candidate_only=false`.
2. Confirm the main repository Release contains exactly one Runtime Kit ZIP for each of `universal`, `arm64-v8a`,
   `armeabi-v7a`, `x86_64`, and `x86`.
3. Confirm `repository_dispatch` triggers this repository's `build-from-runtime-kit.yml`.
4. Confirm this repository Release contains five APKs named
   `autojs6-apk-builder-template-v<plugin>-autojs6-v<host>-<variant>-<crc32>.apk`, one per variant above. Confirm all five
   report the same packageName, signing certificate, versionName, and versionCode.
5. Confirm the workflow committed `version.properties` (advanced `PLUGIN_VERSION_BUILD` / `PLUGIN_RELEASE_SEQ`) and one new
   `compat-matrix.json` entry with five `artifacts`. Run the resolver once per ABI and confirm it selects the exact artifact;
   remove one exact artifact in a temporary matrix copy and confirm universal fallback. For a deliberately widened patch
   release, also confirm `minHostVersionCode < maxHostVersionCode`, `allowPatchVersionMismatch` is exactly `true`, both
   endpoints resolve to the same plugin version, and a host just outside the interval does not resolve that entry:

   ```powershell
   python scripts\update_compat_matrix.py resolve --host-version-code <hostVersionCode> --abi arm64-v8a
   python scripts\update_compat_matrix.py resolve --host-version-code <hostVersionCode> --abi x86_64
   python scripts\update_compat_matrix.py resolve --host-version-code <minHostVersionCode>
   python scripts\update_compat_matrix.py resolve --host-version-code <maxHostVersionCode>
   python scripts\update_compat_matrix.py resolve --host-version-code <outsideHostVersionCode>
   ```

   See `docs/versioning.md` and `docs/abi-variants.md`.
6. Confirm AutoJs6 Plugin Center points to `SuperMonster003/AutoJs6-Plugin-APK-Builder-Template`.

## Local APK Checks

```powershell
python scripts\check_apk_assets.py `
  --main-apk D:\idea-projects\AutoJs6\app\build\outputs\apk\app\release\autojs6-v6.8.0-universal.apk `
  --plugin-apk app\build\outputs\apk\release\autojs6-apk-builder-template-v1.0.0-autojs6-v6.8.0-universal.apk

python scripts\verify_runtime_kit_set.py D:\temp\autojs6-runtime-kits
```

Expected:

```text
Main APK does not contain assets/template.apk or assets/runtime-kit/*
Plugin APK contains assets/runtime-kit/template.apk and runtime-kit.json
Runtime Kit set verified: 5 variants
```

For every plugin APK, inspect `assets/runtime-kit/runtime-kit.json` and its nested `template.apk`. The declared
`template.supportedAbis` must exactly equal the set of `lib/<abi>/*.so` directories in that template; universal must contain
all four supported ABIs and every specific variant must contain only one.

## Device Scenarios

1. Install only AutoJs6, then open Plugin Center on a host version covered by the test matrix.
   Expected: Plugin Center resolves the highest plugin build compatible with its own versionCode, selects the exact device
   ABI asset (or universal fallback), and the packaging entry prompts to install or enable that paired build.
2. Install AutoJs6 and matching APK Builder Template plugin.
   Expected: packaging succeeds without compatibility warning.
3. On an ARM64 device, let Plugin Center select the release asset.
   Expected: it prefers the `arm64-v8a` APK; packaging succeeds and the plugin reports `inrt-arm64-v8a`.
4. On an x86_64 emulator, let Plugin Center select the release asset.
   Expected: it prefers the `x86_64` APK; packaging succeeds and the plugin reports `inrt-x86_64`.
5. Temporarily hide the exact ABI asset from the test Release or matrix.
   Expected: Plugin Center selects universal and packaging still succeeds.
6. Manually sideload an incompatible ABI variant (for example x86_64 on ARM64).
   Expected: discovery / invocation protection rejects it before template transfer and the warning shows plugin and device ABIs.
7. Exercise both sides of the host compatibility boundary:
   - Publish a test entry built for host `H0` with `minHostVersionCode=H0`, `maxHostVersionCode=H1`, and
     `allowPatchVersionMismatch=true`, where `H1` is an already-verified adjacent patch host. On both hosts, let Plugin Center
     resolve and install the entry. Expected: both hosts select the same plugin version; `H0` packages without a version
     warning, while `H1` shows the localized patch-compatibility warning, continues packaging without `allowRiskyBuild`, and
     produces an APK that passes the normal installation/startup smoke test.
   - Install the same plugin on host `H2` outside the closed interval. Expected: Plugin Center does not select that matrix
     entry and direct sideloading is rejected before template transfer, even when risky build is enabled. If an older matrix
     entry matches `H2` and is lower than the installed plugin, Plugin Center shows "reinstall required", warns that plugin
     app data will be removed, and opens Android uninstallation first. If the matrix is unavailable or has no usable entry,
     the existing Release/tag channel remains the fallback.
8. Install a plugin with a damaged Runtime Kit or SHA mismatch.
   Expected: hard failure, packaging is blocked.
9. Package a project that selects external native/plugin modules such as Paddle OCR, MediaInfo, or Barcode.
   Expected: existing native libs/assets extraction still works.
