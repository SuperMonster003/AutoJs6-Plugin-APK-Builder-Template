<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>为 AutoJs6 提供独立应用打包模板 APK 的插件</p>

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

### 语言 (Languages)

******

当前 README.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### 简介

******

AutoJs6 APK Builder Template 插件为 AutoJs6 的独立应用打包流程提供外部模板 APK 和 Runtime Kit. 宿主通过插件服务读取模板 APK, 并用版本和协议元数据确认兼容性.

******

### 功能

******

- 提供 `autojs6-apk-builder-template` 插件服务, 插件 ID 为 `autojs6-apk-builder-template`, 引擎为 `apk-builder-template`.
- 通过 `org.autojs.plugin.INFO` 暴露通用插件信息, 通过 `org.autojs.plugin.APK_BUILDER` 提供模板 APK.
- 构建时校验 Runtime Kit 的 SHA-256 摘要和 `template.apk` 必需条目.
- 在 `assets/runtime-kit/` 内打包 `template.apk`, 默认签名库, 运行时元数据和契约文件.
- 上报宿主版本, 协议版本, 模板包名, 模板摘要和远程构建能力.
- 插件信息, 使用说明, README 与 CHANGELOG 覆盖西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体.

******

### Runtime Kit

******

Runtime Kit 来自 AutoJs6 主仓库, 是独立应用模板的唯一来源. 本插件只验证和打包该产物, 不生成 `template.apk`. 一个完整 Runtime Kit 通常包含以下文件

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

### 本地构建

******

先在 AutoJs6 主仓库生成 Runtime Kit:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

再在本仓库指定 Runtime Kit 目录构建插件:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

也可以把发布的 `autojs6-runtime-kit-*.zip` 解压到 `runtime-kit/`, 然后直接构建:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

******

### 发布流程

******

生产发布流程如下:

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

### 签名

******

生产插件必须使用受信任的 AutoJs6 插件签名密钥. GitHub Actions 发布需要以下仓库密钥:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

本地发布构建仍支持被忽略的根目录 `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### 发行历史

******

# v6.8.0 Alpha5

###### 2026/07/16

* `新增` APK Builder Template 插件服务, 插件 ID 为 `autojs6-apk-builder-template`, 引擎为 `apk-builder-template`, 变体为 `inrt-universal`
* `新增` 通过 `org.autojs.plugin.INFO` 暴露插件信息, 通过 `org.autojs.plugin.APK_BUILDER` 提供模板 APK
* `新增` 将 AutoJs6 Runtime Kit 打包到 `assets/runtime-kit/`, 包含 `template.apk`, 默认签名库, 元数据和契约文件
* `新增` 构建时校验 Runtime Kit 元数据, SHA-256 摘要和 `template.apk` 必需条目
* `新增` 上报宿主版本, 协议版本, 模板包名, 模板 SHA-256, Runtime API 摘要和远程构建能力
* `新增` 支持通过 `autojs.apkBuilder.templatePlugin.enableRemoteBuild` 启用实验性远程构建协议
* `新增` 发布流程支持下载 Runtime Kit, 验证资产, 使用受信任密钥签名并上传通用 APK
* `新增` 插件信息, 使用说明, README 与 CHANGELOG 增加西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体多语言资源

##### 更多发行历史可参阅

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-zh-Hans.md)

******

### 资源结构

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` 提供插件名称, 描述和兜底说明的本地化; `plugin_instruction.md` 提供宿主侧展示的插件使用说明. README 与 CHANGELOG 由 `.python/generate_markdown.py` 根据 JSON 源文件生成.

******

### 相关链接

******

- AutoJs6 主项目: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 文档: https://docs.autojs6.com
