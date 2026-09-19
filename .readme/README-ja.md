<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>AutoJs6 の "アプリの包装" 機能を支えるテンプレートプラグイン</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
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

AutoJs6 の "アプリの包装" は, スクリプトやプロジェクトを単体でインストール・実行できる APK にします. AutoJs6 本体を軽量に保つため, 大きなテンプレートと包装処理の中核はすべて本プラグインが持ちます.

プラグインにアイコンや画面はありません. AutoJs6 は発見・検証, 上限付きリクエストの準備, 進捗表示を担当します. プラグインは自身のテンプレートを展開し, プロジェクトとリソースを書き込み, Manifest/resources を変更し, ABI と署名を処理して候補 APK を返します. AutoJs6 は公開前に出力を独立して再検証します.

処理はすべて同じ Android 端末上で Binder とファイル記述子を使って行われます. プロジェクトのソースをネットワークやクラウドへ送信しません.

******

### 仕組み

******

包装時の流れは次のとおりです:

1. 許可判定: AutoJs6 が公式署名, 有効状態, ホスト範囲, ABI, 正式機能, プロトコル, 端末内実行モードを確認
2. リクエスト準備: サイズ制限付きのプロジェクト/ネイティブ/キーストア入力を作り, 期待するパッケージと署名者を固定
3. プラグイン構築: リクエストを再検証し, Runtime Kit テンプレートを展開, プロジェクトを書き込み, Manifest/resources と ABI を処理して署名
4. 結果返却: 読み取り専用 FD で候補 APK を返し, プライベート作業領域を消去
5. ホスト公開: AutoJs6 がサイズ, SHA-256, APK 構造, 署名, 署名者, パッケージ名, バージョンを再検証し, 全合格時だけ原子的に置換

******

### 機能

******

- テンプレート, プロジェクト/リソース, Manifest と resources.arsc, ABI, キーストア, 署名を含む端末内包装の中核を完全に所有します.
- AutoJs6 を軽量に保ちます: ホストは UI, 信頼/互換性判定, 準備, 取消/進捗, 独立出力検証を担当し, 第二のビルダーは持ちません.
- 同じ Android 端末上で Binder/AIDL と ParcelFileDescriptor により完結し, プロジェクトをネットやクラウドへ送りません.
- 各プラグインビルドを検証済み AutoJs6 Runtime Kit と対応付け, 検証済みパッチ閉区間を宣言できます.
- universal, arm64-v8a, armeabi-v7a, x86_64, x86 の各版と universal フォールバックを提供します.
- 既定キーストアを同梱し, プラグイン側で BKS/JKS の作成・検証を行い, カスタムキーストアにも対応します.
- メタデータ, 説明, README, CHANGELOG は 10 言語に対応します.

******

### クイックスタート

******

- **インストール方法**: 可能なら AutoJs6 プラグインセンターからインストールしてください: 対応ホストは互換性マトリクスを読み取り, 対応するプラグインバージョンと端末 ABI に一致するアセットを自動選択し, 見つからない場合は universal にフォールバックします. 手動インストールでは [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) から APK を取得し, AutoJs6 と同名のリリースタグまたはプラグインバージョンの autojs6- サフィックスで対応ホストを確認してください (例: プラグイン v1.0.0+autojs6-6.8.0-alpha5 は AutoJs6 v6.8.0 Alpha5 に対応). プラグインセンターがインストール済みより低い対応バージョンを選んだ場合は, Android がダウングレード上書きを許可しないため, 表示されるアンインストールと再インストールの案内に従ってください.
- **使い方**: 追加の操作は不要です. AutoJs6 でいつも通り "アプリの包装" 機能を使えば, 包装処理が自動的にプラグインを発見し, 内蔵テンプレートを使用します.
- **動作確認の方法**: プラグイン未インストール (またはバージョン不一致) の場合, AutoJs6 の包装入口にインストールや有効化を促すメッセージが表示されます. 一致するバージョンを入れるとメッセージが消え, プラグインが認識されたことが分かります. プラグインにはアイコンも画面もないため, ホーム画面に見当たらないのは正常です.
- **うまくいかないときは**: 互換性の警告が出たら, プラグインセンターが互換性マトリクスから選んだビルドを使うか, 現在のホストがプラグインの宣言範囲内か確認してください. 非互換として包装がブロックされた場合は, マトリクスに一致するビルドをインストールしてください. テンプレートの破損や検証エラーなら, 公式の入手先からプラグインを再インストールしてください. それ以外は AutoJs6 のログと再現手順を添えて [Issues](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) に報告してください.

******

### 機能範囲

******

誤解を避けるため, 次の事項は本プラグインの機能範囲外であることを明記します:

- 単独では使えません: アイコンと画面はなく, 互換 AutoJs6 から呼び出されます.
- 端末内ビルドはクラウドビルドではなく, 本プロトコルはプロジェクトのソースを送信しません.
- AutoJs6 はプロセス内に第二の包装中核を持ちません. プラグインが欠落, 無効, 非信頼, 非互換, または失敗した場合は処理を止め, 既存出力を保持します.
- Runtime Kit は引き続き AutoJs6 リポジトリが生成し, プラグインは検証・包装・配布・利用します.
- 旧 "リモートビルド" 機能は旧ホスト向けに無効のままです. 名称は端末内の別プロセスを指し, インターネットサービスではなく, 正式機能とは別です.

******

### よくある質問

******

**Q: プラグインセンターはどのようにビルドを選びますか?**

A: 対応する AutoJs6 は自身の versionCode で compat-matrix.json を照会し, 互換範囲内で pluginVersionCode が最も高いビルドを選択します. 次に端末の正確な ABI を優先し, なければ universal にフォールバックします. マトリクスエントリが検証済みパッチ区間を対象にできるのは allowPatchVersionMismatch=true を明示した場合だけです. 実際の構築ホストでは警告せず包装でき, 区間内の別ホストは同じビルドを警告付きで再利用し, 区間外のホストは利用できません. 使用可能なマトリクスエントリがなければ, 既存の Release/タグ経路へ戻ります. 対応プラグインのバージョンがインストール済みより低い場合, Android はそのままダウングレードできないため, プラグインセンターが先にアンインストールしてから対応ビルドをインストールするよう案内します.

**Q: なぜ AutoJs6 と対応する版が必要ですか?**

A: テンプレート内ランタイムがホスト API と一致する必要があります. プラグインセンターが互換性のある最新版と最適な ABI を選び, 範囲外ホストは拒否します.

**Q: ランチャーに見当たりません. 失敗ですか?**

A: いいえ. 意図的にアイコンと画面を持たず, AutoJs6 のバックグラウンドサービスとしてだけ動作します. 設定 > アプリで確認できます.

**Q: プロジェクトは外部サーバーへ送られますか?**

A: 送られません. 同じ Android 端末上の二つのアプリプロセス間で通信します. 旧コードの "リモートビルド" は Binder のプロセス境界を意味し, 正式モードは `on-device-plugin` です.

**Q: プラグインが失敗すると?**

A: AutoJs6 は処理を止め, 対処可能なエラーを示し, 既存 APK を保持します. ホスト内の第二ビルダーへ暗黙に切り替えません.

******

### 技術リファレンス

******

以下の内容はプラグイン開発者と統合担当者向けです. プラグインを使うだけなら通常は読む必要はありません.

#### Runtime Kit

Runtime Kit は AutoJs6 メインリポジトリがビルドし, スタンドアロンアプリテンプレートの唯一の信頼できるソースです. 本プラグインはその成果物を検証して同梱するだけで, `template.apk` は生成しません. 完全な Runtime Kit には通常これらのファイルが含まれます:

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

#### 発見用識別子

ホストは次の識別子で本プラグインを発見してバインドします:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### ローカルビルド

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

#### リリースフロー

想定される本番リリースフローは次のとおりです:

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

#### 署名

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

### ロードマップ

******

ROADMAP.md では正式なプラグイン管理ビルド, 候補, ABI 別配布, 互換性, セキュリティ証拠, GA 後保証を検証可能な一覧で追跡します.

- [ROADMAP.md を見る](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### リリース履歴

******

# v1.0.3

###### 2026/09/19

* `修正` 生成した APK の resources.arsc を非圧縮かつ 4 バイト境界に配置し, Android 11 以降を対象とするアプリをインストールできるようにした
* `修正` 共有ビルドプラグイン 1.8.3 により, AGP 9.1 での SDK XML v4 解析警告と, JVM 単体テストの組み立て時に APK ネイティブライブラリのアラインメント検証が誤って実行される問題
* `改善` `enableRemoteBuild` ビルドスイッチを削除し, プラグインが常に APK ビルド機能を宣言するようにした
* `改善` compileSdk と targetSdk を 37 (Android 17) に引き上げ, プラグインの動作は新しいターゲットの影響を受けない

# v1.0.2

###### 2026/09/13

* `修正` ビルド環境の言語にかかわらずプラグインのバージョン日付を英語で表示
* `改善` 多言語リソースの統一, プラグイン有効化の明確化, リリース成果物の検証

# v1.0.1

###### 2026/09/13

* `改善` 多言語リソースの統一, プラグイン有効化の明確化, リリース成果物の検証

##### さらに詳しいリリース履歴

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-ja.md)

******

### ライセンス

******

本プロジェクトは Mozilla Public License 2.0 の下で公開されており, その条件に従った使用, 改変, 配布が可能です.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

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

`strings.xml` はローカライズされたプラグイン名, 説明, フォールバック説明を提供します; `plugin_instruction.md` はホスト側に表示される使用説明を提供します. README と CHANGELOG は `.python/generate_markdown.py` により JSON ソースから生成されます; ドキュメントを変更するときは生成物ではなく JSON を編集してスクリプトを再実行してください.

******

### 関連リンク

******

- AutoJs6 メインプロジェクト: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 ドキュメント: https://docs.autojs6.com


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/docs/16kb.md)
