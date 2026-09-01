# Remote Build E2E Drill

This drill executes the plugin-side remote build session against a real Android Runtime Kit. It does not require the AutoJs6 UI because the
instrumented test drives `RemoteApkBuildSession` directly through the same request, callback, file-descriptor, builder, and signing code used by
the exported service.

## Safety and prerequisites

1. Use a disposable emulator or test device. The Gradle connected-test task temporarily installs
   `org.autojs.plugin.apkbuilder.template` and `org.autojs.plugin.apkbuilder.template.test`.
2. Confirm the production plugin package is not installed on that target. Do not allow a debug test build to replace a plugin you intend to keep.
3. Provide a verified Runtime Kit in `runtime-kit/`, or pass
   `-Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<unpacked-kit>` to Gradle. The kit must contain `template.apk`, the default keystore, metadata,
   and their SHA-256 files.
4. Confirm the device meets the project minimum SDK and has enough free space for the plugin, five real template extraction/build operations,
   and their temporary output.
5. Select one device explicitly when several devices are connected. Never rely on Gradle choosing a target implicitly.

Read-only preflight example:

```powershell
$serial = "<test-device-serial>"
adb -s $serial shell getprop ro.build.version.sdk
adb -s $serial shell getprop ro.product.cpu.abi
adb -s $serial shell pm path org.autojs.plugin.apkbuilder.template
adb -s $serial shell pm path org.autojs.plugin.apkbuilder.template.test
```

Both `pm path` commands should produce no `package:` line on a disposable target.

## Compile and run

Compile the instrumentation source without changing a device:

```powershell
.\gradlew.bat :app:compileDebugAndroidTestKotlin --no-daemon
```

Run the current 39-method class (functional/compatibility/lifecycle coverage, bounded-input failures, one sensitive-data lifecycle method, two
72-case deterministic corpora, and keystore/icon/Manifest/ARSC boundary methods) on exactly one target:

```powershell
$env:ANDROID_SERIAL = "<test-device-serial>"
.\gradlew.bat :app:connectedDebugAndroidTest --no-daemon `
  "-Pandroid.testInstrumentationRunnerArguments.class=org.autojs.plugin.apkbuilder.template.impl.RemoteApkBuildSessionInstrumentedTest"
```

The tests inject both enabled and disabled feature-gate values at the session boundary, so the test APK itself does not need a production
`enableRemoteBuild=true` build. A manually built experimental plugin still uses:

```powershell
.\gradlew.bat :app:assembleDebug `
  -Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=runtime-kit
```

## Case matrix

Source: `app/src/androidTest/java/org/autojs/plugin/apkbuilder/template/impl/RemoteApkBuildSessionInstrumentedTest.kt`.

| Test | Source shape | Expected terminal result | Required assertions |
|---|---|---|---|
| `directorySourceWithRiskyHostNameMismatchCompletesWithWarning` | Project directory | `onCompleted`, `STATUS_OK`, `LEVEL_WARN` | Risk-accepted host versionName mismatch is retained in `warnings`; output APK contains both nested project files |
| `directorySourceOutsideDeclaredRangeFailsEvenWhenRiskyBuildIsAllowed` | Project directory | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | A host above the declared compatibility range is blocked even with `allowRiskyBuild=true`; no output is created |
| `fileSourceCompletes` | Single JavaScript file | `onCompleted`, `STATUS_OK` | Input becomes `assets/project/main.js`; updated project config reports `main.js` |
| `defaultKeyStoreProducesV1SignedApk` | Single JavaScript file + bundled keystore | `onCompleted`, `STATUS_OK` | The default keystore produces a V1-signed APK with a manifest and certificate block under `META-INF/` |
| `customKeyStoreV2IconNativeCapabilitiesAndAbiPruningComplete` | Single JavaScript file + custom BKS FD + icon + native archive | `onCompleted`, `STATUS_OK` | A custom BKS whose workspace filename has no `.bks` suffix is auto-detected; V2-only signing, permissions, icon replacement, native capability libraries, and exact single-ABI pruning are verified |
| `strictHostMismatchFailsBeforeWorkspaceCreation` | Single file fixture | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Mismatch is in `errors`, no output, input FDs are closed |
| `missingRequiredNativeInputReturnsUnsupported` | Single file + selected ABI | `onFailed`, `STATUS_UNSUPPORTED`, `LEVEL_WARN` | Missing native input is in `warnings`, `errors` is empty, no output |
| `cancellationBeforeStartReturnsCancelled` | Single file fixture | `onCancelled`, `STATUS_CANCELLED` | No output or error, input FDs are closed |
| `disabledPluginBuildReturnsUnsupported` | Single file fixture | `onFailed`, `STATUS_UNSUPPORTED`, `LEVEL_WARN` | Disabled reason is in `warnings`; the feature gate cannot be bypassed; early-failure TypeScript key bytes are zeroed, metadata is removed, and signing password references are released |
| `newerRequiredProtocolFails` | Single file fixture | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Newer-protocol reason is in `errors`, no output |
| `olderRequiredProtocolReachesPostNegotiationValidation` | Lower required protocol + deliberately missing native archive | `onFailed`, `STATUS_UNSUPPORTED`, `LEVEL_WARN` | Protocol v2 is accepted by the v3 plugin and the request reaches the later missing-native-input check; no newer-protocol warning or output is produced |
| `typeScriptStagingEnvelopeAuthenticatesAndIsReEncryptedForTheFinalApk` | Authenticated TypeScript staging envelope | `onCompleted`, `STATUS_OK` | AES-GCM envelope authenticates, its key is zeroed, metadata is removed, and the final APK contains neither the transport envelope nor staging cleartext |
| `typeScriptStagingTamperedAuthenticationTagFailsClosed` | TypeScript envelope with a modified GCM tag | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Authentication error is explicit, no output is created, and key/metadata cleanup still runs |
| `typeScriptStagingEnvelopeOmittedFromPathInventoryFailsClosed` | Encrypted entry omitted from the path inventory | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Envelope magic is detected before ordinary JS encryption and the missing inventory declaration fails closed |
| `typeScriptStagingPathInventoryWithMissingEntryFailsClosed` | Path inventory names a nonexistent encrypted entry | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Unconsumed inventory item is reported, no output is created, and key/metadata cleanup still runs |
| `projectArchiveSizeMismatchFailsClosed` | Project ZIP with a false declared length | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Exact project-archive size mismatch is reported before extraction; no output or workspace remains |
| `projectArchiveSha256MismatchFailsClosed` | Project ZIP with a false digest | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Exact project-archive SHA-256 mismatch is reported before extraction; no output or workspace remains |
| `projectArchiveDeclaredCompressedSizeLimitFailsClosed` | Small project ZIP declaring more than the 512 MiB compressed-input cap | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | The declared cap is enforced before FD copying; no output or workspace remains and all input descriptors close |
| `nativeArchiveSizeMismatchFailsClosed` | Native-input ZIP with a false declared length | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Native archive is rejected before the builder consumes it; no output or workspace remains |
| `nativeArchiveSha256MismatchFailsClosed` | Native-input ZIP with a false digest | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Native archive is rejected before the builder consumes it; no output or workspace remains |
| `keyStoreSizeMismatchFailsClosed` | Custom-keystore FD with a false declared length | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Keystore is rejected before signing; no output or workspace remains |
| `keyStoreSha256MismatchFailsClosed` | Custom-keystore FD with a false digest | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Keystore is rejected before signing; no output or workspace remains |
| `projectArchiveTraversalEntryFailsClosed` | ZIP containing `../outside.js` | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Android ZIP validation or the plugin's canonical-path check rejects the entry before an outside write |
| `projectArchivePosixAbsoluteEntryFailsClosed` | ZIP containing `/outside.js` | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | A POSIX absolute entry is rejected during full-archive preflight, before the extraction target is written |
| `projectArchiveWindowsAbsoluteEntryFailsClosed` | ZIP containing a Windows drive-absolute path | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | The normalized drive prefix is rejected and the host-controlled entry name is not echoed in callback errors |
| `projectArchiveEntryCountLimitFailsClosed` | ZIP containing 16,385 entries | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | The production 16,384-entry cap fails during preflight, before extraction begins |
| `projectArchiveCompressionRatioLimitFailsClosed` | ZIP containing a 2 MiB high-expansion entry | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | The production 250:1 expansion-ratio cap rejects the entry before extraction begins |
| `nativeArchiveUnexpectedTopLevelFailsClosed` | Native-input ZIP containing `dex/classes.dex` | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | The native/assets top-level allowlist rejects `dex/` before the archive can modify the template workspace |
| `projectSourceTraversalPathFailsClosed` | Valid ZIP with `SOURCE_PATH=../source.js` | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Source resolution rejects traversal even though the archive itself is valid |
| `malformedProjectArchiveFailsClosed` | Non-ZIP bytes advertised as a project ZIP | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | No source can be resolved, no builder output is created, and the workspace is removed |
| `malformedProjectConfigJsonFailsClosed` | Truncated project JSON | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | JSON parse failure is explicit; no output or workspace remains |
| `deterministicParcelableRequestCorpusFailsClosed` | 40 Parcel-round-tripped AIDL/Bundle mutations | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` for every case | Strict types, key allowlist, FD metadata, digest/length, output/path and TypeScript metadata bounds; 40 unique safe terminal markers and no sentinel reflection |
| `deterministicJsonAndBinaryEditorCorpusFailsClosed` | 32 project JSON / binary-editor mutations | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` for every case | Single-root/512 KiB/64-level JSON, strict field types and Manifest/ARSC/path bounds; 32 unique safe terminal markers |
| `keyStoreActualByteLimitFailsClosed` | Sparse custom-keystore FD containing 64 MiB + 1 byte while declaring 64 MiB | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | The actual stream budget rejects before writing the overflow byte; no build phase/output/workspace remains |
| `malformedIconFailsClosed` / `iconCompressedFileSizeLimitRejectsBeforeMetadataDecode` / `iconPixelLimitFailsClosedBeforeBitmapAllocation` | Invalid icon, 16 MiB + 1 file, and 2,049 × 2,049 declared PNG | `onFailed`, `STATUS_FAILED`, `LEVEL_BLOCK` | Invalid metadata, compressed-size, and pixel budgets fail before resource publication or an over-budget bitmap allocation |
| `manifestAndArscBoundaryValuesComplete` | Maximum accepted app/version/package/output names | `onCompleted`, `STATUS_OK` | 127-byte package slot and 238-byte output candidate complete Manifest/ARSC rewrite, packaging, signing, and package parsing; derived unsigned filename remains 255 bytes |

The five success tests use the real `template.apk` and perform template extraction, input copying, Manifest rewriting, ABI policy handling, project
script encryption, `resources.arsc` rewriting, APK repackaging, and signing. They then verify:

- reported length and SHA-256 equal the callback-copied APK;
- core APK entries and expected `assets/project/**` entries exist;
- Android `PackageManager` parses the expected package name, versionName, versionCode, application label, and at least one signing certificate;
- `warnings` contains the lightweight-builder notice and `errors` is empty;
- `STEP_BUILD` and `STEP_SIGN` were observed;
- the successful workspace remains readable until `session.close()` and is removed by `close()`.

The focused signing and customization builds additionally verify:

- bundled-keystore V1 signing contains its `META-INF` certificate block;
- custom-keystore-FD BKS input remains loadable after the workspace intentionally renames it to `keystore.bin`, while the V2-only output contains no V1 certificate block;
- requested `INTERNET` and `WAKE_LOCK` permissions are present;
- the launcher icon path is resolved through the rewritten `resources.arsc`, and the output bitmap retains the fixture's exact dimensions and center pixel;
- OpenCV and Image Quantization inputs contribute all five required native libraries, and the output contains only the selected ABI.

Every path waits for `STEP_FINISH`. Failure, cancellation, unsupported, and disabled paths also assert that all plugin-side input descriptor references
are cleared. The focused disabled-path security assertion was rerun after the key/password cleanup was added and passed 1/1 on the recorded device.
Every G5 negative-input method additionally asserts `LEVEL_BLOCK`, no output APK, cleared FD/password references, and removal of the per-session
workspace. The traversal test accepts either Android's earlier `Invalid zip entry path` rejection or the plugin's own safe path rejection so the
same property is portable across platform ZIP implementations. The pure-JVM boundary matrix is now 24/24: 3 bounded-stream copier tests, 6
JSON/ARSC envelope tests, and 15 ZIP/path tests covering valid extraction, POSIX/Windows/UNC/traversal/alias paths, 4,096-byte total paths,
255-byte segments, 128-segment depth, C0/C1/surrogate rejection, compressed input size, entry count, per-entry/aggregate extracted size and ratio,
the native/assets allowlist, and non-echoing of hostile names.

## Expected result and report

The Gradle task must end with:

```text
Finished 31 tests on <device>
BUILD SUCCESSFUL
```

HTML and XML reports are generated under `app/build/reports/androidTests/connected/` and
`app/build/outputs/androidTest-results/connected/`. These are build artifacts and are not committed.

Last recorded execution:

| Date | Device class | Android | ABI | Result |
|---|---|---:|---|---|
| 2026/08/31 | Minimum-SDK AVD | API 24 (Android 7) | x86 | Full matrix 14/14 + lower-protocol focus 1/1; composite 15/15, 0 skipped, 0 failed |
| 2026/08/31 | Sony G8441 physical test device | API 28 (Android 9) | armeabi-v7a (forced) | Full matrix 14/14 + lower-protocol focus 1/1; composite 15/15, 0 skipped, 0 failed |
| 2026/08/31 | Sony G8441 physical test device | API 28 (Android 9) | arm64-v8a (forced) | Full matrix 14/14 + lower-protocol focus 1/1; composite 15/15, 0 skipped, 0 failed |
| 2026/08/31 | AVD | API 29 (Android 10) | x86 | Full matrix 14/14 + lower-protocol focus 1/1; composite 15/15, 0 skipped, 0 failed |
| 2026/08/31 | Current-target AVD | API 36 (Android 16) | x86_64 | Integrated functional suite 15/15, then expanded full suite 25/25 (including 10 G5 negative inputs), 0 skipped, 0 failed; final Gradle exit 0 in 4m 16s |
| 2026/08/31 | Isolated current-target AVD | API 36 (Android 16) | x86_64 | Bounded-archive focus 6/6 and final full suite 31/31 (15 functional + 16 G5 negative inputs), 0 skipped, 0 failed; final Gradle exit 0 in 2m 43s |
| 2026/08/31 | Isolated current-target AVD | API 36 (Android 16) | x86_64 | Sensitive-data lifecycle focus 1/1 (success + authenticated failure + pre-start cancellation), 0 skipped, 0 failed; device test 265.37 s |
| 2026/08/31 | Isolated current-target AVD | API 36 (Android 16) | x86_64 | Deterministic malformed-input focus 2/2 with 72/72 unique cases; post-review full suite 39/39 in 146.516 s; generated-changelog final-byte rerun 39/39 in 141.647 s; 0 skipped/failed |

The machine-readable reports, direct-instrumentation summaries, test APKs, and SHA-256 manifests for this qualification run are retained outside the
Git worktree under `D:\idea-projects\.a6-compat-audit-artifacts\m3-4-qualification-2026-08-31`. `manifest.json` covers the full 14-case runs and
the two retained initial failures; `lower-protocol-manifest.json` covers the later focused fifteenth case on the same five API/ABI targets. The
`api36-full15-manifest.json` supplement covers the subsequent integrated 15/15 current-target rerun. The later
`api36-full25-g5-manifest.json` supplement covers the final 25/25 API 36 rerun, its HTML/JUnit reports, the exact app/test APKs, and the ten basic
project/native/keystore size/SHA-256 plus ZIP/path/JSON negative cases. The
expanded matrix exposed two production defects
before the final passes: an encrypted TypeScript envelope omitted from its declared path inventory was not rejected early, and a valid custom BKS
could not be loaded after the workspace normalized its name to `keystore.bin`. Both paths now fail safely or load by detected format, respectively,
and all five final device/ABI runs use the corrected implementation. An earlier positive TypeScript fixture also used the host source filename rather
than the compiled directory entry name; that fixture-only mismatch is retained separately from the final evidence instead of being hidden.

The bounded-archive follow-up is retained outside the worktree under
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g5-bounded-zip-2026-08-31`. It contains the 12/12 JVM boundary report, 6/6 focused device
report, final 31/31 JUnit/HTML report, exact app/test APKs, qualification Runtime Kit, device identity, sensitive-name scans, and a recursive
SHA-256 manifest. One intermediate 30-case run used the later capability-gate Runtime Kit, whose template intentionally omits the OpenCV and Image
Quantization test libraries; 29 tests passed and the native fixture precondition failed before the session under test. That wrong-kit run is retained
alongside the corrected 30/30 and final 31/31 runs instead of being discarded or reported as a production regression.

The host-output acceptance follow-up is retained under
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g5-output-validation-2026-08-31`. Host JVM tests passed 7/7 for staged transfer/publication
and 5/5 for non-extracting APK archive policy. On the isolated API 36/x86_64 AVD, the real host → Binder → qualification-plugin producer passed
1/1 and generated two 30,519,837-byte outputs through the new pre-publication structure, `apksig`, and package-identity checks. A separate opt-in
device test then passed 1/1: it accepted the real output, rejected a structurally complete but signing-block-removed/script-tampered APK,
rejected a missing-`classes.dex` APK, rejected mismatched requested identity, preserved an existing target byte-for-byte, left no publication
temporary files, and observed both target packages absent before and after. The first negative-test attempt is retained because Android's JUnit
runtime lacked the compile-time JUnit 4.13 `ThrowingRunnable`; the compatibility-assertion rerun passed in 32.93 seconds. This closes only the
host-output validation subgate. The later sensitive-data and deterministic-fuzz sections close those repository-owned subgates; independent
security review still remains.

The declared-minimum-SDK supplement is retained under
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g5-output-validation-api24-2026-08-31`. It reused byte-identical host, Android-test, and
qualification-plugin APKs from the API 36 qualification on a dedicated API 24/x86_64 AVD. The real Binder producer passed 1/1 in 21.847 seconds;
the output validator passed 1/1 in 12.719 seconds and exercised the pre-API-28 `PackageManager.GET_SIGNATURES` branch with V2-only APKs. Both
outputs independently passed `apksigner`, identity, 3,667-entry, and required-component checks. All installed packages and device fixtures were
removed, then the temporary AVD was deleted. Its 15-file evidence set has a clean 14/14 SHA-256 replay.

These five device/ABI results close the functional M3-1 case matrix and satisfy the API/ABI, high/current/lower protocol negotiation, TypeScript,
signing, icon, and native-capability portions of the G1 qualification gate. They were not sufficient by themselves for default rollout. The
current-host installation/launch/runtime closure below completes G1. The G2 stability qualification and the G3 dual-builder qualification described
below are also complete; performance, independent security review, host rollback-control, and staged-release gates remain defined in
`docs/remote-build-rollout.md`.

## Current-host installation, launch, and main-script execution

On 2026/08/31, AutoJs6 `6.8.0 / 5277` and a same-signer, remote-enabled qualification plugin ran the opt-in host test
`RemoteApkBuildRuntimeSmokeProducerDeviceTest` on the isolated API 36/x86_64 AVD. One run produced two APKs through the full
host → Binder → plugin path:

- a directory-source UI project (`org.autojs.autojs6.m3g1directorysmoke`);
- a single-file UI script (`org.autojs.autojs6.m3g1singlefilesmoke`).

The producer passed 1/1 in 21.888 seconds. Both outputs had their size and SHA-256 verified before atomic publication, emitted 31 progress
callbacks, parsed with their requested package/version metadata, and left no request-staging entries. Each APK then installed, cold-launched from
`SplashActivity`, and reached a foreground `ScriptExecuteActivity`. Its unique UI marker appeared in both text and content-description, its script
log marker appeared exactly once, and no `FATAL EXCEPTION` appeared. Screenshots were visually inspected as well as checked through the UI XML.

Exact input/output APKs, screenshots, UI hierarchies, producer/runtime logs, a machine-readable manifest, and a verified 15-file checksum list are
outside the repositories at
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g1-runtime-smoke-2026-08-31\manifest.json`. The host switch was restored to false and the
official plugin source remains `supportsRemoteBuild=false`; this is qualification evidence, not a release or a default-enable decision.

## G2 stability and resource-cleanup qualification

On 2026/09/01, the same final functional tuple was exercised on three newly created, isolated AVDs: AutoJs6 `6.8.0 / 5277` host APK SHA-256
`aa92d07a80c0a6a6a761026d478435eaca752ac6429535d89fc43c9995999a31`, same-signer remote-enabled qualification plugin SHA-256
`bcafedf7ed0c9e539824ff7f530c4a4bf35d7d2b6679044452fa830a19343370`, the copied 5277 universal Runtime Kit, and remote protocol v3.

| Tier | Successful directory / single-file builds | Expected cancellation injection | Successful p50 / p95 / max | Host FD baseline / max / final | Plugin FD baseline / max / final |
|---|---:|---:|---:|---:|---:|
| API 36 / x86_64 | 30 / 30 | PREPARE 10, BUILD 10, SIGN 10 | 7,236 / 7,822 / 14,575 ms | 121 / 121 / 119 | 89 / 89 / 89 |
| API 29 / x86 | 30 / 30 | 0 | 10,161 / 10,867 / 12,164 ms | 65 / 67 / 64 | 42 / 42 / 42 |
| API 24 / x86_64 | 30 / 30 | 0 | 7,186 / 7,503 / 7,778 ms | 55 / 55 / 54 | 36 / 36 / 35 |

All 180 normal builds succeeded and all 30 injected cancellations produced exactly one `STATUS_CANCELLED`, for 210/210 accepted outcomes and no
unexpected failure. Every successful session checked the requested identity/configuration, a session-unique project marker, exactly one redacted
terminal diagnostic, and empty host/plugin workspaces. Each tier retained one plugin process, its final FD count returned to or below baseline, and
the authoritative logs contained no fatal exception, ANR, `BINDER_LOST`, host/plugin process death, or reboot. API 24's old
`UiAutomationConnection` cannot execute `run-as` from the system process, so exact-serial host-side probes checked PID, FD, and both workspaces at
warm-up, build 30, build 60, and final-cleanup pauses before acknowledging the test. API 29 and API 36 retained their in-process per-session probes.

The batch exposed and fixed three production defects: an early cancellation could be classified as `FAILED`; closing a plugin session could race
its build worker and allow a deleted workspace to reappear; and Android 9–11 could expose an empty modern archive-signer view for a valid V2/V3 APK.
The signer compatibility fallback is used only when that modern view is empty, while repository-local `ApkVerifier` remains mandatory. Final focused
verification passed 36/36 host unit tests, 24/24 plugin-app tests, and 7/7 plugin-API tests, with both Release Kotlin compilations exiting zero.

Qualification accounting intentionally excludes an earlier zero-failure API 36 run because it predated the Android 9–11 signer fix. Failed fixture,
probe, and externally interfered attempts remain under `attempts/` with zero credit. During those rejected attempts, an unrelated stale Codex/Gradle
session selected every attached device and temporarily installed/uninstalled test packages on four physical devices; this workflow did not authorize
or repeat those operations and did not attempt to reconstruct prior physical-device state. The final accepted runs targeted only isolated AVDs.
The final API 36 `am instrument` terminal stream also lost its last client-side chunk after the process had completed; the evidence records that gap
instead of synthesizing stdout and correlates the one start/finish, zero failure markers, 60 successes, 30 cancellations, and exact terminal summary
from the complete device log. All four temporary AVD instances (including the replaced API 36 instance) were deleted afterward.

The official plugin property and generated Debug, Release, and AndroidTest `BuildConfig` values were restored to
`ENABLE_REMOTE_BUILD=false`. Exact APKs, Runtime Kit, raw and filtered logs, JUnit XML, attempt history, cleanup records, redacting scans, manifest,
and replayable SHA-256 list are outside the repositories at
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g2-stability-2026-08-31\manifest.json`. G2 is complete. G3 and G4 were subsequently completed by
the independent qualifications below; the project remains R0 / No-Go because G5-review, G7, the preview cycle, and R2/R3 fallback gates are open.

## Host output validation before publication

The AutoJs6 host now treats a plugin's successful callback as untrusted until a same-directory staged file passes all of the following checks:

- the declared and actual size are within the 2 GiB transport cap and match, and the host-computed SHA-256 matches the callback;
- the APK has at most 65,536 unique, portable ZIP paths and contains non-empty `AndroidManifest.xml`, `resources.arsc`, `classes.dex`, and
  `assets/project/project.json`;
- repository-local `ApkVerifier` reports a valid cryptographic signature and at least one verified signer certificate;
- Android `PackageManager` sees a signer and the exact package name, version name, and version code requested by the local project.

Only then does the host atomically replace the destination. Any rejection returns a failed remote outcome, deletes the staged file, preserves an
existing destination, and therefore never reaches `BuildActivity`'s success-only installation entry. The API 36 qualification and its API 24
minimum-SDK supplement both exercised the real Binder success path and signature/structure/identity rejection paths. Each final device marker was:

```text
M3_G5_OUTPUT_VALIDATION_RESULT accepted=1 signature_rejected=1 structure_rejected=1 identity_rejected=1 old_output_preserved=1 temporary_artifacts=0 install_attempts=0 package_installed=0
```

## Sensitive-data and closed-workspace audit

The sensitive-data lifecycle method in the current 39-method class plants four qualification values: TypeScript cleartext, keystore password,
alias password, and a 32-byte staging key. In one
device method it exercises successful packaging, an authentication failure, and cancellation before start. It checks every callback/result/progress
surface, scans the successful APK as raw bytes and as all decompressed entries, verifies three key arrays were zeroed, verifies all six password
references were cleared, and requires the private workspace to be empty after close. The final marker was:

```text
M3_G5_SENSITIVE_DATA_RESULT success=1 failure=1 cancel=1 callback_findings=0 output_findings=0 workspace_entries=0 keys_zeroed=3 passwords_cleared=3
```

The device case passed 1/1 in 265.37 seconds. The redacting host scanner then passed four acceptance batches with zero findings and zero errors:
15 current qualification text files; 1,124 entries in the main qualification APK; 2,373 text artifacts across the complete retained evidence root;
and 19 currently present build/test report files across four plugin/host roots. The plugin JUnit/HTML/logcat files from this run were copied into
the qualification evidence before the IDE's targeted build cleaned `app/build`, so the first batch still covers them. Scanner-specific positive
controls passed 8/8 and the repository Python suite passed 18/18. The Android-test APK and rule/test sources intentionally embed the sentinels and
are therefore controlled inputs, not no-sentinel output
surfaces. Exact scope, replay commands, privacy guarantees, exclusions, limitations, and evidence paths are in `docs/sensitive-data-audit.md`.

## Deterministic malformed-input qualification

The final deterministic corpus contains 40 Parcel/Bundle cases and 32 JSON/Manifest/ARSC cases. Every request is round-tripped through a real
Android `Parcel`, then opened as a real plugin session. The accepted run produced 72 unique `RemoteFuzzAudit` terminal lines, all failed closed,
and reflected none of the protected fuzz value. Production limits now include a 64 MiB actual keystore stream budget, a 512 KiB / 64-level
single-root JSON envelope, 4,096-byte paths with 255-byte segments and 128-segment depth, 16 MiB / 4,194,304-pixel icons, a 127-code-unit ARSC
package slot, and a 238-byte output basename whose worst derived temporary name is exactly 255 bytes.

The focused run passed 2/2 with 72/72 unique cases in 6.430 seconds. The post-review full class passed 39/39 in 146.516 seconds. Because generating
the ten localized changelogs changes packaged assets, the exact final production APK was then rebuilt and the full class passed again, 39/39 in
141.647 seconds. Its 1,515-line logcat contained all 72 unique safe markers, no protected plaintext, and no fatal/crash marker. A redacting scan of the final log, production plugin APK
(1,124 archive entries), and three JVM XML files passed with zero findings and zero errors; the private session and fixture directories were empty.
The invalid initial 2/2 branch-coverage result, suffix-length `ENAMETOOLONG`, first 37/39 fixture run, pre-review 39/39, post-review pre-document
39/39, and final generated-asset 39/39
are retained rather than collapsed into the accepted result. Detailed limits, attempt history, replay criteria, and evidence location are in
`docs/remote-build-fuzz-audit.md`.

This is a deterministic, reproducible mutation corpus rather than a claim of exhaustive or coverage-guided fuzzing. It closes the repository-owned
G5 fuzz subgate; an independent plugin security review remains mandatory.

## G3 dual-builder equivalence and the Node packaging boundary

On 2026/09/01, the historical local builder was frozen at commit `fff913caafa3dc0d6172638c8532b027c0dfa8c0` and compared with the current
remote builder against the same 5277 universal Runtime Kit on the isolated API 36/x86_64 AVD. The accepted UI set contained three local APKs and
four remote APKs. Static comparison passed 10/10 for package identity, label, versions, permissions, splash behavior, launcher icon, V2-only
single-signer identity, x86_64 native contents, encrypted project entry, and build metadata. The same-version remote APK was 1.106526% smaller.

Both direction changes were then exercised as real Android updates:

```text
legacy-local-v100 -> current-remote-v101 -> legacy-local-v102
current-remote-v200 -> legacy-local-v201 -> current-remote-v202
```

Every one of the six steps passed fresh/update installation as appropriate, cold start, hot start, foreground `ScriptExecuteActivity`, UI marker,
and script-log checks. TypeScript and Image Quantization added four advanced APKs and passed a separate 21/21 static matrix. With both corresponding
Runtime plugins uninstalled, local and remote TypeScript returned checksum 108; local and remote Image Quantization returned 1,181 bytes and
checksum 83,471,039. All four advanced steps passed cold/hot runtime checks, while remote size deltas were -1.105671% and -1.085675% respectively.
The qualification exposed and fixed a real TypeScript packaging defect: placing `"use strict"` before the generated `ui` directive made the latter
ineffective. The rejected pre-fix output and a fixture-only ImageQuant global-name collision are retained as attempts.

Embedded Node.js is intentionally not a supported APK payload. Node runtime ownership now belongs to the external Runtime plugin, so the old G3
wording was revised by `docs/remote-build-node-packaging-decision.md` instead of reviving a partially removed embedded service. The host preflight
passed 3/3 JVM tests and a 1/1 device gate: two Node-shaped requests were rejected before progress or staging. The plugin's independent defense
passed 4/4 JVM tests and a final direct-session 1/1 device gate: a legacy library alias and an execution-mode directive both returned
`STATUS_UNSUPPORTED`, emitted no BUILD/SIGN progress or output, closed inputs, and left zero workspace entries. Neither accepted log exposed the
execution directive or fixture source; the obsolete Manifest service/permission injection call and helpers were removed. The exact final plugin APK
then passed the complete 41-method device class in 139.568 seconds with 41 matched logcat starts/finishes, no failure/fatal marker, and an empty
workspace.

The complete APKs, screenshots, UI hierarchies, logs, JSON results, JUnit XML, and attempt history are outside the repositories at
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g3-equivalence-2026-09-01`. G3 is complete within the accepted architecture, but this evidence
does not start R1 or enable the official plugin. At that checkpoint G4 was still open; the following qualification subsequently closed it. The
independent G5 security review, G7, a complete preview cycle, and the R2/R3 independent fallback remain open.

### G3 single-file build-number correction

The first G4 local-baseline run exposed a previously missed G3 semantic difference rather than a performance failure. The frozen local builder
increments `build.number` only for a directory project whose `project.json` is committed after packaging; a standalone file uses the requested
version code exactly. The remote builder had incremented both forms. Production code now branches by source kind, direct plugin device tests assert
the exact file and directory behavior, and the host stability/local-baseline fixtures use the same rule. The rejected first G4 attempt is retained.
The corrected builder produced every accepted performance and pressure result below, and official remote capability remained disabled afterward.

## G4 performance and pressure qualification

On 2026/09/01, isolated API 36/x86_64, API 29/x86, and API 24/x86_64 AVDs each ran the same directory and standalone-file inputs 30 times per
source through both the frozen local builder and the real remote Binder path. Every 60-build local and remote batch completed. The timing gate was:

| Tier | Local p50 / p95 / max | Remote p50 / p95 / max | Remote/local p95 | Result |
|---|---:|---:|---:|---|
| API 36 / x86_64 | 6,097 / 6,429 / 6,607 ms | 6,994 / 7,254 / 7,383 ms | `1.128325x` | pass |
| API 29 / x86 | 21,161 / 21,448 / 21,923 ms | 10,042 / 10,260 / 11,147 ms | `0.478366x` | pass |
| API 24 / x86_64, 1 GiB | 8,036 / 8,293 / 8,382 ms | 6,946 / 7,359 / 7,819 ms | `0.887375x` | performance pass; pressure fail |

The API 24 1 GiB target processes survived all 60 builds, but the complete buffers showed two non-target kernel LMK victims during remote startup.
That run is deliberately classified `PERFORMANCE_PASS_PRESSURE_FAIL`, not rewritten as a pass. The same temporary AVD was cold-started with
1.5 GiB configured memory and ran a fresh remote 60-build pressure batch inside unique start/end log markers. It passed in 426.466 seconds with
p50/p95/max 6,804 / 7,057 / 7,143 ms; 226 PSS samples measured 123,117 KiB host, 57,356 KiB plugin, and 176,349 KiB combined sampled peaks.
The 2,598-line window contained zero kernel LMK, `lmkd` kill, OOM, host/plugin death, or fatal marker. Plugin PID stayed fixed and its four external
probe snapshots were 36 / 35 / 35 / 35 FDs with zero workspace entries. This establishes 1.5 GiB as the current minimum qualification profile.

The output-size gate reuses the G3 same-version comparisons: basic, TypeScript, and Image Quantization differed by `1.106526%`, `-1.105671%`, and
`-1.085675%`, all within 2%. The cancellation gate reuses G2's PREPARE/BUILD/SIGN 10 + 10 + 10 accepted cancellations, whose slowest terminal was
153 ms. Plugin storage admission now combines declared/cross-checked archive expansion, a build-time-verified 4x template expansion bound, three
build-tree copies, compressed inputs, and a 256 MiB reserve. API 36 insufficient-space and native-zero-declaration injections each failed before
workspace creation. The focused/final JVM results were plugin app 35/35, plugin API 7/7, and host APK Builder 60/60; Release compilation succeeded.

All four G4 temporary AVDs were deleted, no physical device received a targeted command, and the final plugin Debug/Release/AndroidTest constants
are `ENABLE_REMOTE_BUILD=false`. Raw instrumentation, all-buffer logs, PSS/probe CSV, the 1 GiB failure classification, 1.5 GiB marker window,
frozen inputs, rejected attempts, default-off final APK, JUnit XML, cleanup audit, and replayable hashes are stored outside the repositories at
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g4-performance-pressure-2026-09-01`. Detailed formulas and limitations are in
`docs/remote-build-performance-audit.md`. G4 is complete, but this remains qualification evidence rather than release authorization.

## Post-run restoration

`connectedDebugAndroidTest` normally removes both temporary packages. Verify rather than assume:

```powershell
adb -s $env:ANDROID_SERIAL shell pm path org.autojs.plugin.apkbuilder.template.test
adb -s $env:ANDROID_SERIAL shell pm path org.autojs.plugin.apkbuilder.template
```

If either package was absent before the drill but remains afterward, remove exactly that temporary package with `adb uninstall <package>`, then run
the read-only check again. Never uninstall a package that existed before the drill; restore that target from its recorded baseline instead.
