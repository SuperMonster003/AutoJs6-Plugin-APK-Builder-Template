<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>AutoJs6 のスタンドアロンアプリ包装向けテンプレート APK プラグイン</p>

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

### 言語

******

現在の README.md は次の言語に対応しています:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- 日本語 [ja] # 現在
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### 概要

******

AutoJs6 APK Builder Template プラグインは, AutoJs6 がスタンドアロンアプリを包装するときに使う外部テンプレート APK と Runtime Kit を提供します. ホストはプラグインサービスからテンプレート APK を読み取り, バージョンとプロトコルのメタデータで互換性を確認します.

******

### 機能

******

- `autojs6-apk-builder-template` プラグインサービスを提供し, プラグイン ID は `autojs6-apk-builder-template`, エンジンは `apk-builder-template` です.
- `org.autojs.plugin.INFO` で共通プラグイン情報を公開し, `org.autojs.plugin.APK_BUILDER` でテンプレート APK を提供します.
- ビルド時に Runtime Kit の SHA-256 ダイジェストと `template.apk` の必須エントリを検証します.
- `assets/runtime-kit/` に `template.apk`, 既定のキーストア, ランタイムメタデータ, 契約ファイルを包装します.
- ホストバージョン, プロトコルバージョン, テンプレートパッケージ名, テンプレートダイジェスト, リモートビルド機能を報告します.
- プラグイン情報, 使用説明, README, CHANGELOG はスペイン語/フランス語/ロシア語/アラビア語/日本語/韓国語/英語/簡体字中国語/香港繁体字/台湾繁体字をカバーします.

******

### Runtime Kit

******

Runtime Kit は AutoJs6 メインリポジトリから提供され, スタンドアロンアプリテンプレートの唯一の信頼できるソースです. このプラグインはその成果物を検証して包装するだけで, `template.apk` は生成しません. 完全な Runtime Kit には通常これらのファイルが含まれます

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

### ローカルビルド

******

まず AutoJs6 メインリポジトリで Runtime Kit を生成します:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

次に生成済み Runtime Kit ディレクトリを指定してこのリポジトリをビルドします:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

公開済みの `autojs6-runtime-kit-*.zip` を `runtime-kit/` に展開して直接ビルドすることもできます:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

******

### リリースフロー

******

想定される本番リリースフローは次のとおりです:

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

### 署名

******

本番プラグインリリースは信頼済みの AutoJs6 プラグイン署名鍵で署名する必要があります. GitHub Actions リリースには次のリポジトリシークレットが必要です:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

ローカルのリリースビルドでは, 無視対象のルート `sign.properties` ファイルも引き続き使えます:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### リリース履歴

******

# v6.8.0 Alpha5

###### 2026/07/16

* `追加` APK Builder Template プラグインサービスを追加しました. プラグイン ID は `autojs6-apk-builder-template`, エンジンは `apk-builder-template`, バリアントは `inrt-universal` です
* `追加` `org.autojs.plugin.INFO` でプラグイン情報を公開し, `org.autojs.plugin.APK_BUILDER` でテンプレート APK を提供するようにしました
* `追加` AutoJs6 Runtime Kit を `assets/runtime-kit/` に包装しました. `template.apk`, 既定のキーストア, メタデータ, 契約ファイルを含みます
* `追加` Runtime Kit メタデータ, SHA-256 ダイジェスト, `template.apk` の必須エントリをビルド時に検証するようにしました
* `追加` ホストバージョン, プロトコルバージョン, テンプレートパッケージ名, テンプレート SHA-256, Runtime API ダイジェスト, リモートビルド機能を報告するようにしました
* `追加` `autojs.apkBuilder.templatePlugin.enableRemoteBuild` による任意の実験的リモートビルドプロトコル対応を追加しました
* `追加` Runtime Kit のダウンロード, asset 検証, 信頼済み鍵による署名, universal APK のアップロードに対応するリリースフローを追加しました
* `追加` プラグイン情報, 使用説明, README, CHANGELOG にスペイン語/フランス語/ロシア語/アラビア語/日本語/韓国語/英語/簡体字中国語/香港繁体字/台湾繁体字の多言語リソースを追加しました

##### さらに詳しいリリース履歴

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-ja.md)

******

### リソース構成

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` はローカライズされたプラグイン名, 説明, フォールバック説明を提供します; `plugin_instruction.md` はホスト側に表示される使用説明を提供します. README と CHANGELOG は `.python/generate_markdown.py` により JSON ソースから生成されます.

******

### 関連リンク

******

- AutoJs6 メインプロジェクト: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 ドキュメント: https://docs.autojs6.com
