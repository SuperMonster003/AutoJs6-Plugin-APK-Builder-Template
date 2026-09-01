<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>Template plugin that powers the AutoJs6 "Package Application" feature</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
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

The AutoJs6 "Package Application" feature turns a script or project into an APK that installs and runs on its own, without AutoJs6 on the target device. Packaging needs a "template APK" as the skeleton: an app that already contains the complete script runtime. To keep the main app slim, recent versions of AutoJs6 no longer bundle this rather large template; it now lives in this plugin and is installed only by users who need packaging.

The plugin has no icon and no user interface. Everything happens in the background: when packaging, AutoJs6 discovers the plugin, checks version compatibility and file integrity, then reads the built-in template APK to finish the job.

One-line decision guide: if you use "Package Application", install the build selected for your AutoJs6 by Plugin Center from the compatibility matrix; if you never package standalone apps, you do not need it.

******

### How It Works

******

When packaging a standalone application, AutoJs6 and this plugin cooperate as follows:

1. Discovery: AutoJs6 locates the installed template plugin and reads its metadata
2. Compatibility check: versions, protocol versions, and the template package name are compared; mismatches produce a warning or block packaging
3. Integrity check: the SHA-256 digest of the template APK is verified to rule out corrupted or tampered files
4. Template transfer: the template APK is streamed through an inter-process pipe, without temporary copies
5. Packaging: AutoJs6 writes the script, configuration, and resources into the template and produces the final standalone APK

******

### Features

******

- Supplies the complete standalone application template (Runtime Kit) for the AutoJs6 "Package Application" feature; no configuration is needed after installation.
- Every plugin build keeps one built-for AutoJs6 host in its version name (such as 1.0.0+autojs6-6.8.0-alpha5) and may explicitly declare a verified closed patch interval; exact matches are silent, non-exact hosts inside that interval warn, and hosts outside it are blocked.
- Dual integrity protection: SHA-256 digests and required template entries are validated when the plugin is built, and the template digest is reported to AutoJs6 for re-verification when packaging.
- The template is streamed to AutoJs6 through an inter-process pipe without redundant temporary copies.
- Ships a default keystore, so an installable APK can be produced even before a custom signing key is configured.
- Supports an experimental "remote build" protocol in which the plugin process performs a lightweight build on its own (disabled by default; see Boundaries).
- Plugin metadata, usage instructions, README, and CHANGELOG cover 10 languages: Simplified Chinese, Traditional Chinese (Hong Kong/Taiwan), English, French, Spanish, Japanese, Korean, Russian, and Arabic.

******

### Quick Start

******

- **How to install**: Install from the AutoJs6 Plugin Center when possible: supported host builds read the compatibility matrix and automatically select both the paired plugin version and the exact device ABI artifact, with universal fallback. For a manual install, download the APK from [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) and use the AutoJs6-named release tag or the autojs6- suffix in the plugin version to identify the paired host (e.g. plugin v1.0.0+autojs6-6.8.0-alpha5 pairs with AutoJs6 v6.8.0 Alpha5). If Plugin Center selects a lower version than the one installed, follow its uninstall-and-reinstall guidance because Android cannot overwrite an app with a downgrade.
- **How to use**: No extra steps. Use the "Package Application" feature in AutoJs6 as usual; the packaging flow discovers the plugin and uses its built-in template automatically.
- **How to confirm it works**: Without the plugin (or with a mismatched version), the packaging entry in AutoJs6 prompts you to install or enable it; once a matching version is installed the prompt disappears, which means the plugin is recognized. The plugin has no icon or UI, so not finding it on the launcher is expected.
- **Where to look when something fails**: On a compatibility warning, use the build selected by Plugin Center from the compatibility matrix or verify that the current host is inside the plugin's declared interval; if an incompatibility blocks packaging, install the matrix-matched build; on a template corruption or verification error, reinstall the plugin from an official source; for anything else, file an [issue](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) with AutoJs6 logs and reproduction steps.

******

### Boundaries

******

To avoid misunderstandings, the following are explicitly outside the scope of this plugin:

- The plugin cannot be used on its own: it has no icon and no UI, and is only invoked by AutoJs6 during packaging.
- The plugin does not generate the template APK: the template and Runtime Kit are built and released by the AutoJs6 main repository; this plugin only verifies, packages, and distributes them.
- The plugin plays no part in writing or running scripts day to day: only the "Package Application" feature reads it.
- Remote build is experimental and disabled by default: officially released plugins do not enable it; it is only available in self-built plugins with the feature explicitly turned on.
- The plugin does not relax version requirements: packaging with a mismatched AutoJs6 version may be blocked, and even if it goes through, the output is not guaranteed to work.

******

### FAQ

******

**Q: How does Plugin Center choose a build?**

A: Supported AutoJs6 versions query compat-matrix.json with their own versionCode, select the highest compatible plugin build, then prefer the exact device ABI and fall back to universal. A matrix entry may cover a verified patch interval only when allowPatchVersionMismatch is explicitly true: its exact built-for host packages silently, another host inside the interval reuses the same build with a warning, and a host outside the interval cannot use it. If no matrix entry is usable, the existing Release/tag channel remains the fallback. If the matched plugin version is lower than the installed one, Plugin Center asks you to uninstall first and then install the matched build; Android cannot perform an in-place downgrade.

**Q: Why must the plugin pair with my AutoJs6 version?**

A: The script runtime inside the template APK corresponds strictly to the AutoJs6 runtime API, so compatibility is determined by the plugin's declared and validated versionCode contract, not by its own semantic version. Most builds target one exact host; a build may also explicitly declare a verified closed patch-version interval. The built-for host then passes silently, other hosts inside the interval receive a warning, and hosts outside it are blocked. The plugin's own version (such as 1.0.0) evolves independently, while the autojs6- suffix in the version name and the release tag mark the paired host; on an older AutoJs6, download the plugin build under the matching older tag (to move back from a newer plugin, uninstall it first — Android does not allow downgrade installs).

**Q: I cannot find the plugin on my launcher. Did the installation fail?**

A: No. The plugin has no icon or UI and only runs as a background service for AutoJs6. You can confirm it under the system "Settings > Apps" list as APK Builder Template.

**Q: Packaging reports a template verification failure. What now?**

A: This usually means the installed plugin is incomplete or corrupted. Reinstall it from the AutoJs6 Plugin Center or this repository's Releases; if the problem persists, please file an issue.

**Q: Why is the plugin so large?**

A: It bundles a complete standalone application template, including the script engine and native libraries for all processor architectures. That is exactly why the template was split out of the AutoJs6 main app: only users who package apps carry this weight.

**Q: What is "remote build"?**

A: An experimental protocol that lets the plugin perform a lightweight build in its own process (unpack the template, write the script and configuration, rewrite the package name and resources, re-sign). Officially released plugins keep it disabled; it currently targets developers building the plugin themselves.

******

### Technical Reference

******

The sections below target plugin developers and integrators; they are usually not needed for simply using the plugin.

#### Runtime Kit

The Runtime Kit is built by the AutoJs6 main repository and is the only source of truth for the standalone application template. This plugin only verifies and packages that artifact; it does not generate `template.apk`. A complete Runtime Kit usually contains these files:

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

#### Discovery Identifiers

The host discovers and binds this plugin through the following identifiers:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### Local Build

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

#### Release Flow

The expected production release flow is:

```text
AutoJs6 tag
-> main repository generates autojs6-runtime-kit-*.zip
-> main repository uploads the Runtime Kit to its GitHub Release
-> main repository dispatches SuperMonster003/AutoJs6-Plugin-APK-Builder-Template
-> this repository downloads and verifies the Runtime Kit
-> this repository builds the plugin APK
-> this repository uploads the plugin APK to the same tag Release
-> this repository records the pairing into compat-matrix.json
-> AutoJs6 Plugin Center installs this plugin
```

#### Signing

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

### Roadmap

******

Planned work and its progress are tracked as a verifiable checklist in ROADMAP.md, covering remote build stabilization, per-ABI template variants, patch-level version compatibility, and more. Discussion is welcome in Issues.

- [View ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

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

##### For more release history

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-en.md)

******

### License

******

This project is released under the Mozilla Public License 2.0, which permits use, modification, and distribution under its terms.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

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

`strings.xml` contains localized plugin names, descriptions, and fallback instructions; `plugin_instruction.md` contains usage instructions displayed by the host. README and CHANGELOG files are generated from JSON sources by `.python/generate_markdown.py`; to change the documentation, edit the JSON sources and re-run the script instead of editing generated files.

******

### Links

******

- AutoJs6 main project: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 documentation: https://docs.autojs6.com
