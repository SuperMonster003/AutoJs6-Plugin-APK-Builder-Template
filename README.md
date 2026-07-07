# AutoJs6 Plugin APK Builder

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
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=D:\idea-projects\AutoJs6\app\build\runtime-kit\autojs6-runtime-kit-v6.7.1-alpha4+3923
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
-> main repository dispatches SuperMonster003/AutoJs6-Plugin-APK-Builder
-> this repository downloads and verifies the Runtime Kit
-> this repository builds the plugin APK
-> this repository uploads the plugin APK to the same tag Release
-> AutoJs6 Plugin Center installs this plugin
```

Required main repository secret:

```text
AUTOJS6_PLUGIN_REPO_TOKEN
```

It must have `Contents: write` permission on `SuperMonster003/AutoJs6-Plugin-APK-Builder` so the main repository can create `repository_dispatch` events.

Optional plugin repository secret:

```text
AUTOJS6_MAIN_REPO_TOKEN
```

Use it only when the AutoJs6 main repository or Release assets are private.

## Signing

For production, sign this plugin with the trusted AutoJs6 plugin signing key. Local unsigned or debug builds are only suitable for development and will not satisfy signature-trust policies on production hosts.
