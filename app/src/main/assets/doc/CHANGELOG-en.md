******

### Release History

******

# v1.0.0

###### 2026/09/01

* `Hint` First formal release on the independent plugin version line, paired exactly with the AutoJs6 v6.8.0 (versionCode 5277) Runtime Kit; the composite plugin version is 1.0.0+autojs6-6.8.0 (versionCode 527701), Plugin Center selects the paired ABI build through compat-matrix.json, and remote builds remain disabled by default
* `Feature` Introduced plugin SemVer 1.0.0, independent build numbering, composite version names, and monotonic Android versionCode values that support multiple plugin releases for the same host
* `Feature` Added universal, arm64-v8a, armeabi-v7a, x86_64, and x86 variants with exact-ABI selection and universal fallback
* `Feature` Added a fail-closed host compatibility range contract and an authoritative compatibility matrix so one explicitly validated adjacent patch range can share a plugin build
* `Fix` Aligned experimental remote single-file build numbering with the legacy builder, and added fail-closed workspace storage preflight using cross-checked expanded input sizes, a build-verified template expansion bound, and a 256 MiB reserve
* `Fix` Rejected legacy Embedded Node.js packaging metadata and source directives before BUILD/SIGN with external Runtime-plugin migration guidance, and removed obsolete Manifest service and foreground-permission injection
* `Fix` Fixed a close/build-thread race in experimental remote sessions that could recreate a deleted session workspace after cancellation or closure; cleanup now waits for the worker and leaves zero residual files
* `Fix` Hardened experimental remote builds by rejecting unlisted TypeScript staging ciphertext and loading custom BKS keystores after workspace filename normalization
* `Fix` Tightened experimental remote-build input boundaries with strict Parcelable/Bundle and project.json type, size, and nesting checks; bounded keystores, icons, and ZIP path depth/segments; and fixed ARSC package-name and derived-output filename overflows
* `Fix` The plugin could not be activated from Plugin Center after installation on some systems
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
