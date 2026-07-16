<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>為 AutoJs6 提供獨立應用程式封裝範本 APK 的外掛</p>

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

### 語言 (Languages)

******

目前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 目前
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### 簡介

******

AutoJs6 APK Builder Template 外掛為 AutoJs6 的獨立應用程式封裝流程提供外部範本 APK 和 Runtime Kit. 宿主透過外掛服務讀取範本 APK, 並以版本和協定中繼資料確認相容性.

******

### 功能

******

- 提供 `autojs6-apk-builder-template` 外掛服務, 外掛 ID 為 `autojs6-apk-builder-template`, 引擎為 `apk-builder-template`.
- 透過 `org.autojs.plugin.INFO` 暴露通用外掛資訊, 透過 `org.autojs.plugin.APK_BUILDER` 提供範本 APK.
- 建置時驗證 Runtime Kit 的 SHA-256 摘要和 `template.apk` 必要項目.
- 在 `assets/runtime-kit/` 內封裝 `template.apk`, 預設簽章庫, 執行階段中繼資料和契約檔案.
- 回報宿主版本, 協定版本, 範本套件名稱, 範本摘要和遠端建置能力.
- 外掛資訊, 使用說明, README 與 CHANGELOG 覆蓋西班牙文/法文/俄文/阿拉伯文/日文/韓文/英文/簡體中文/香港繁體/台灣繁體.

******

### Runtime Kit

******

Runtime Kit 來自 AutoJs6 主儲存庫, 是獨立應用程式範本的唯一來源. 本外掛只驗證和封裝該產物, 不產生 `template.apk`. 完整 Runtime Kit 通常包含以下檔案

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

### 本機建置

******

先在 AutoJs6 主儲存庫產生 Runtime Kit:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

再在本儲存庫指定 Runtime Kit 目錄建置外掛:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

也可以將發布的 `autojs6-runtime-kit-*.zip` 解壓縮到 `runtime-kit/`, 然後直接建置:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

******

### 發布流程

******

正式發布流程如下:

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

### 簽章

******

正式外掛必須使用受信任的 AutoJs6 外掛簽章金鑰. GitHub Actions 發布需要以下儲存庫密鑰:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

本機發布建置仍支援被忽略的根目錄 `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### 發行歷史

******

# v6.8.0 Alpha5

###### 2026/07/16

* `新增` APK Builder Template 外掛服務, 外掛 ID 為 `autojs6-apk-builder-template`, 引擎為 `apk-builder-template`, 變體為 `inrt-universal`
* `新增` 透過 `org.autojs.plugin.INFO` 暴露外掛資訊, 透過 `org.autojs.plugin.APK_BUILDER` 提供範本 APK
* `新增` 將 AutoJs6 Runtime Kit 封裝到 `assets/runtime-kit/`, 包含 `template.apk`, 預設簽章庫, 中繼資料和契約檔案
* `新增` 建置時驗證 Runtime Kit 中繼資料, SHA-256 摘要和 `template.apk` 必要項目
* `新增` 回報宿主版本, 協定版本, 範本套件名稱, 範本 SHA-256, Runtime API 摘要和遠端建置能力
* `新增` 支援透過 `autojs.apkBuilder.templatePlugin.enableRemoteBuild` 啟用實驗性遠端建置協定
* `新增` 發布流程支援下載 Runtime Kit, 驗證資產, 使用受信任金鑰簽章並上傳通用 APK
* `新增` 外掛資訊, 使用說明, README 與 CHANGELOG 增加西班牙文/法文/俄文/阿拉伯文/日文/韓文/英文/簡體中文/香港繁體/台灣繁體多語言資源

##### 更多發行歷史可參閱

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-zh-Hant-TW.md)

******

### 資源結構

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` 提供外掛名稱, 描述和備援說明的本地化; `plugin_instruction.md` 提供宿主側展示的外掛使用說明. README 與 CHANGELOG 由 `.python/generate_markdown.py` 根據 JSON 原始檔產生.

******

### 相關連結

******

- AutoJs6 主專案: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 文件: https://docs.autojs6.com
