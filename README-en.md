<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>Template APK plugin for AutoJs6 standalone application packaging</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/commit/dc4627c752f8c1f709b302651a7de57992eac2b5"><img alt="Created" src="https://img.shields.io/date/1784208939?color=2e7d32&label=Created"/></a>
    <br>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Languages

******

The current README.md supports the following languages:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- English [en] # current
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### Introduction

******

The AutoJs6 APK Builder Template Plugin supplies the external template APK and Runtime Kit used by AutoJs6 when packaging standalone applications. The host reads the template APK from the plugin service and checks version and protocol metadata for compatibility.

******

### Features

******

- Provides the `autojs6-apk-builder-template` plugin service with plugin ID `autojs6-apk-builder-template` and engine `apk-builder-template`.
- Exposes common plugin metadata through `org.autojs.plugin.INFO` and serves the template APK through `org.autojs.plugin.APK_BUILDER`.
- Validates Runtime Kit SHA-256 digests and required `template.apk` entries during the build.
- Packages `template.apk`, the default keystore, runtime metadata, and contract files under `assets/runtime-kit/`.
- Reports host version, protocol version, template package name, template digest, and remote build capability metadata.
- Plugin metadata, usage instructions, README, and CHANGELOG cover Spanish, French, Russian, Arabic, Japanese, Korean, English, Simplified Chinese, Hong Kong Traditional Chinese, and Taiwan Traditional Chinese.

******

### Runtime Kit

******

The Runtime Kit comes from the AutoJs6 main repository and is the only source of truth for the standalone application template. This plugin only verifies and packages that artifact. It does not generate `template.apk`. A complete Runtime Kit usually contains these files

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

******

### Local Build

******

Generate a Runtime Kit from the AutoJs6 main repository first:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

Then build this repository with the generated Runtime Kit directory:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

You can also unpack a released `autojs6-runtime-kit-*.zip` to `runtime-kit/` and build directly:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

******

### Release Flow

******

The expected production release flow is:

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

******

### Signing

******

Production plugin releases must be signed with the trusted AutoJs6 plugin signing key. GitHub Actions releases require these repository secrets:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

Local release builds still support the ignored root-level `sign.properties` file:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### Release History

******

# v6.8.0 Alpha5

###### 2026/07/16

* `Feature` Added the APK Builder Template plugin service with plugin ID `autojs6-apk-builder-template`, engine `apk-builder-template`, and variant `inrt-universal`
* `Feature` Exposed plugin metadata through `org.autojs.plugin.INFO` and served the template APK through `org.autojs.plugin.APK_BUILDER`
* `Feature` Packaged the AutoJs6 Runtime Kit under `assets/runtime-kit/`, including `template.apk`, the default keystore, metadata, and contract files
* `Feature` Added build-time validation for Runtime Kit metadata, SHA-256 digests, and required `template.apk` entries
* `Feature` Reported host version, protocol version, template package name, template SHA-256, Runtime API digests, and remote build capability
* `Feature` Added optional experimental remote build protocol support through `autojs.apkBuilder.templatePlugin.enableRemoteBuild`
* `Feature` Added release flow support for downloading the Runtime Kit, validating assets, signing with the trusted key, and uploading the universal APK
* `Feature` Added localized plugin metadata, usage instructions, README, and CHANGELOG resources for Spanish, French, Russian, Arabic, Japanese, Korean, English, Simplified Chinese, Hong Kong Traditional Chinese, and Taiwan Traditional Chinese

##### For more release history

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-en.md)

******

### Resource Layout

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` contains localized plugin names, descriptions, and fallback instructions; `plugin_instruction.md` contains usage instructions displayed by the host. README and CHANGELOG files are generated from JSON sources by `.python/generate_markdown.py`.

******

### Links

******

- AutoJs6 main project: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 documentation: https://docs.autojs6.com
