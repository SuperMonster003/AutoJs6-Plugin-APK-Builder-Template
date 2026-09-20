******

### Release History

******

# v1.0.3

###### 2026/09/20

* `Fix` Store resources.arsc uncompressed and 4-byte aligned in generated APKs so that apps targeting Android 11 or newer can be installed
* `Fix` SDK XML v4 parsing warnings with AGP 9.1 and APK native alignment checks incorrectly triggered by JVM unit-test assembly tasks, using shared build plugins 1.8.3
* `Improvement` Removed the `enableRemoteBuild` build switch; the plugin always declares its APK build capability
* `Improvement` Raise compileSdk and targetSdk to 37 (Android 17); the plugin's behavior does not depend on the new target
* `Improvement` Rebuild the complete five-ABI Runtime Kit matrix for AutoJs6 6.8.0 build 5282 with an explicit paired-host compatibility range

# v1.0.2

###### 2026/09/13

* `Fix` Keep the plugin version date in English regardless of the build machine locale
* `Improvement` Consistent localized resources, explicit plugin activation and validated release preparation

# v1.0.1

###### 2026/09/13

* `Improvement` Consistent localized resources, explicit plugin activation and validated release preparation

# v6.8.0

###### 2026/09/11

* `Improvement` Build verification of 16 KB page alignment for 64-bit native libraries, including manifest contract checks and JSON reports

# v1.0.0

###### 2026/09/10

* `Hint` The ordinary Package Application path now requires the on-device APK Builder plugin; the legacy supportsRemoteBuild switch remains disabled but no longer disables normal packaging
* `Hint` First formal release on the independent plugin version line, paired exactly with the AutoJs6 v6.8.0 (versionCode 5277) Runtime Kit; the composite plugin version is 1.0.0+autojs6-6.8.0 (versionCode 527701), Plugin Center selects the paired ABI build through compat-matrix.json, and remote builds remain disabled by default
* `Feature` Promoted the plugin-side engine to the only formal on-device packaging path; AutoJs6 stays lean and independently validates every returned APK
* `Feature` Moved BKS/JKS creation and verification into the plugin through a versioned, fail-closed keystore API
* `Feature` Introduced plugin SemVer 1.0.0, independent build numbering, composite version names, and monotonic Android versionCode values that support multiple plugin releases for the same host
* `Feature` Added universal, arm64-v8a, armeabi-v7a, x86_64, and x86 variants with exact-ABI selection and universal fallback
* `Feature` Added a fail-closed host compatibility range contract and an authoritative compatibility matrix so one explicitly validated adjacent patch range can share a plugin build
* `Fix` Aligned experimental remote single-file build numbering with the legacy builder, and added fail-closed workspace storage preflight using cross-checked expanded input sizes, a build-verified template expansion bound, and a 256 MiB reserve
* `Fix` Rejected legacy Embedded Node.js packaging metadata and source directives before BUILD/SIGN with external Runtime-plugin migration guidance, and removed obsolete Manifest service and foreground-permission injection
* `Fix` Fixed a close/build-thread race in experimental remote sessions that could recreate a deleted session workspace after cancellation or closure; cleanup now waits for the worker and leaves zero residual files
* `Fix` Hardened experimental remote builds by rejecting unlisted TypeScript staging ciphertext and loading custom BKS keystores after workspace filename normalization
* `Fix` Tightened experimental remote-build input boundaries with strict Parcelable/Bundle and project.json type, size, and nesting checks; bounded keystores, icons, and ZIP path depth/segments; and fixed ARSC package-name and derived-output filename overflows
* `Fix` The plugin could not be activated from Plugin Center after installation on some systems
* `Improvement` Expand project and build-input archive preflight to 262144 entries, 4 GiB compressed archives, 2 GiB per entry, 8 GiB extracted totals, and a 2000:1 compression ratio
* `Improvement` Trusted release workflow now supports an isolated candidate mode that builds five production-signed APKs and evidence from a pinned host Actions artifact without creating a Release or updating the authoritative compatibility matrix
* `Improvement` Unified Runtime Kit validation rules across Gradle and Python, including hashes, sizes, required files, APK entries, and five-variant consistency
* `Improvement` Published a machine-readable JSON evidence manifest beside the five APKs, binding artifact digests, signer certificate, plugin/host versions, compatibility range, Runtime Kit IDs, and protocol versions
* `Improvement` Updated installation instructions, FAQ, release drill, and 10-language documentation for paired versions, ABI selection, downgrade recovery, and independent versioning
* `Improvement` Standardize the README layout and Gradle platform version management

# v6.8.0 Alpha5

###### 2026/07/16

* `Hint` Pairs with AutoJs6 v6.8.0 Alpha5; supported Plugin Center versions resolve paired builds automatically, while manual installs use the matching Release tag or autojs6- suffix; the plugin has no icon or UI and is invoked automatically when packaging applications
* `Feature` Let AutoJs6 discover the plugin and read its built-in template automatically, so "Package Application" no longer depends on a template APK bundled in the main app
* `Feature` Bundled the complete Runtime Kit: template APK, default keystore, runtime metadata, and contract files
* `Feature` Added automatic version and protocol compatibility checks before packaging, warning or blocking on mismatch to avoid producing broken apps
* `Feature` Validated Runtime Kit SHA-256 digests and required template entries at plugin build time, and reported the template digest to AutoJs6 for re-verification at runtime
* `Feature` Added an experimental remote build protocol that performs a lightweight build inside the plugin process (disabled by default, must be enabled explicitly at build time)
* `Feature` Wired up the automated release flow: when the AutoJs6 main repository publishes a release, a matching plugin APK is built, signed with the trusted key, certificate-fingerprint-verified, and published
* `Feature` Covered 10 languages in plugin metadata, usage instructions, README, and CHANGELOG: Simplified Chinese, Traditional Chinese (Hong Kong/Taiwan), English, French, Spanish, Japanese, Korean, Russian, and Arabic

# v6.7.1 Alpha4

###### 2026/07/09

* `Hint` First public release; pairs with AutoJs6 of the same version (v6.7.1 Alpha4)
* `Feature` Split off from the AutoJs6 main repository as a standalone plugin repository with the initial template APK plugin service
* `Feature` Established the Runtime-Kit-driven pipeline, triggered by the AutoJs6 main repository, that fetches, verifies, builds, and publishes the plugin
