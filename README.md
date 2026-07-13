# AutoJs6 Plugin APK Builder Template

Standalone APK Builder Template plugin for AutoJs6.

This repository contains the plugin shell, the APK Builder Template AIDL/API, Runtime Kit validation scripts, and release workflows. It does not contain AutoJs6 script engine source, INRT runtime source, or code that builds `template.apk`.

## Runtime Source

The AutoJs6 main repository is the only source of truth for the runtime. Each AutoJs6 release publishes an `autojs6-runtime-kit-*.zip` asset containing:

```text
template.apk
template.apk.sha256
default_key_store.bks
default_key_store.bks.sha256
runtime-kit.json
build-contract.json
public-api.txt
assets-manifest.json
native-libs.json
provenance.json
```

This plugin consumes that Runtime Kit and packages it under `assets/runtime-kit/`.

## Local Build

Generate a Runtime Kit from the main AutoJs6 repository first:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

Then build this repository with the generated kit:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=D:\idea-projects\AutoJs6\app\build\runtime-kit\autojs6-runtime-kit-v6.8.0+3924
```

Or unpack a released `autojs6-runtime-kit-*.zip` to `runtime-kit/` and run:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

## Release Flow

The expected production flow is:

```text
AutoJs6 tag
-> main repository generates autojs6-runtime-kit-*.zip
-> main repository uploads the Runtime Kit to its GitHub Release
-> main repository dispatches SuperMonster003/AutoJs6-Plugin-APK-Builder-Template
-> this repository downloads and verifies the Runtime Kit
-> this repository builds the plugin APK
-> this repository uploads the plugin APK to the same tag Release
-> AutoJs6 Plugin Center installs this plugin
```

Required main repository secret:

```text
AUTOJS6_PLUGIN_REPO_TOKEN
```

It must have `Contents: write` permission on `SuperMonster003/AutoJs6-Plugin-APK-Builder-Template` so the main repository can create `repository_dispatch` events.

Optional plugin repository secret:

```text
AUTOJS6_MAIN_REPO_TOKEN
```

Use it only when the AutoJs6 main repository or Release assets are private.

## Signing

Production plugin releases must be signed with the trusted AutoJs6 plugin signing key. Configure these repository-level Actions secrets in this repository:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
```

Create `SIGNING_KEY_BASE64` from the release keystore without line wrapping, for example:

```bash
openssl base64 -A -in sm003.jks -out keystore_base64.txt
```

The `Build APK Builder Plugin from Runtime Kit` workflow performs the following release-signing sequence:

1. verifies that all four signing secrets are present;
2. decodes the keystore into the runner's temporary directory with restricted permissions;
3. validates the keystore password and alias with `keytool`;
4. builds `assembleRelease` with signing values supplied through environment variables;
5. verifies the generated APK with Android `apksigner`;
6. confirms that the APK certificate SHA-256 digest matches the configured keystore certificate;
7. removes temporary keystore and certificate files before publishing the release asset.

The workflow fails instead of publishing an unsigned APK when signing configuration is absent, incomplete, invalid, or does not match the APK signer.

Local builds continue to support the ignored root-level `sign.properties` file:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

Unsigned or debug builds are suitable only for development and do not satisfy production signature-trust policies.
