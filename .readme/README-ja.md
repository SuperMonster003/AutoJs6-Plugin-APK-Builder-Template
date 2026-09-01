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

# v1.0.0

###### 2026/09/02

* `ヒント` 通常のアプリ包装は端末内 APK Builder プラグインを正式に必須とします; 旧 supportsRemoteBuild スイッチは無効のままですが通常包装を無効にしません
* `ヒント` プラグイン独立バージョン系列の初回正式リリースで, AutoJs6 v6.8.0 (versionCode 5277) Runtime Kit に厳密に対応します; 複合プラグインバージョンは 1.0.0+autojs6-6.8.0 (versionCode 527701) で, プラグインセンターは compat-matrix.json から対応 ABI ビルドを選択し, リモートビルドは引き続き既定で無効です
* `追加` プラグイン側エンジンを唯一の正式な端末内包装経路へ昇格; AutoJs6 は軽量なまま返却 APK を独立して検証
* `追加` バージョン付き失敗閉鎖型キーストア API により BKS/JKS の作成と検証をプラグインへ移管
* `追加` プラグイン SemVer 1.0.0, 独立ビルド番号, 複合バージョン名, 単調増加する Android versionCode を導入し, 同じホスト向けに複数のプラグインリリースを提供可能に変更
* `追加` universal, arm64-v8a, armeabi-v7a, x86_64, x86 の各バリアントを追加し, ABI の完全一致選択と universal フォールバックに対応
* `追加` 失敗時に閉じるホスト互換範囲契約と権威ある互換マトリックスを追加し, 明示的に検証された隣接パッチ範囲で 1 つのプラグインビルドを共有可能に変更
* `修正` 実験的なリモート単一ファイルビルドのビルド番号を従来のビルダーと一致させ、照合済みの展開後入力サイズ、ビルド時に検証されたテンプレート展開上限、256 MiB の予約領域を用いるフェイルクローズなワークスペース容量事前検査を追加
* `修正` 従来の組み込み Node.js パッケージ用メタデータとソース指令を BUILD/SIGN 前に拒否して外部 Runtime プラグインへの移行を案内し、不要になった Manifest サービスとフォアグラウンド権限の注入を削除しました
* `修正` 実験的なリモートセッションで, 終了処理とビルドスレッドの競合により, キャンセルまたは終了後に削除済みのセッション作業領域が再作成される問題を修正しました。現在はワーカーの終了を待ってからクリーンアップし, 残留ファイルを残しません
* `修正` 実験的なリモートビルドを強化し, パス一覧にない TypeScript 一時暗号文を拒否するとともに, ワークスペースでファイル名が正規化された後もカスタム BKS キーストアを正しく検出
* `修正` 実験的なリモートビルドの入力境界を強化し, Parcelable/Bundle と project.json の型, サイズ, ネストを厳密に検証するとともに, キーストア, アイコン, ZIP パスの深さ/セグメント長を制限し, ARSC パッケージ名と派生出力ファイル名の境界超過を修正
* `修正` 一部のシステムでインストール後にプラグインセンターからプラグインを有効化できない問題
* `改善` 受信リリースワークフローに候補隔離モードを追加し, 固定した宿主 Actions アーティファクトから本番署名済み 5 APK と evidence を生成しつつ, Release の作成と正式な互換性マトリクスの更新を行わないようにしました
* `改善` Gradle と Python の Runtime Kit 検証規則を統一し, ハッシュ, サイズ, 必須ファイル, APK エントリ, 5 バリアントの整合性を検証
* `改善` 5 個の APK とともに機械可読 JSON 証拠マニフェストを公開し, アセットダイジェスト, 署名証明書, プラグイン/ホストバージョン, 互換範囲, Runtime Kit ID およびプロトコルバージョンを関連付け
* `改善` 対応バージョン, ABI 選択, ダウングレード復旧, 独立バージョン方式について, インストール手順, FAQ, リリース演習, 10 言語ドキュメントを更新
* `改善` README のレイアウトと Gradle プラットフォームのバージョン管理方式を統一

# v6.8.0 Alpha5

###### 2026/07/16

* `ヒント` AutoJs6 v6.8.0 Alpha5 に対応します. 対応するプラグインセンターは対となるビルドを自動解決し, 手動インストールでは一致する Release タグまたは autojs6- サフィックスを使います. プラグインにはアイコンも画面もなく, アプリの包装時に自動的に呼び出されます
* `追加` AutoJs6 がプラグインを自動発見して内蔵テンプレートを読み取れるようにし, "アプリの包装" 機能が本体同梱のテンプレート APK に依存しないようにしました
* `追加` 完全な Runtime Kit を同梱: テンプレート APK, 既定のキーストア, ランタイムメタデータ, 契約ファイル
* `追加` 包装前にバージョンとプロトコルの互換性チェックを自動実行し, 不一致時は明確に警告またはブロックして, 動かないアプリの生成を防ぎます
* `追加` プラグインのビルド時に Runtime Kit の SHA-256 ダイジェストと必須エントリを検証し, 実行時にはテンプレートダイジェストを AutoJs6 へ報告して再検証させます
* `追加` 実験的なリモートビルドプロトコルに対応し, プラグインプロセス内で軽量ビルドを実行できるようにしました (既定で無効, ビルド時に明示的な有効化が必要)
* `追加` 自動リリースフローを整備: AutoJs6 メインリポジトリのリリース時に, 対応するプラグイン APK を自動でビルドし, 信頼済み鍵で署名して証明書フィンガープリントを検証したうえで公開します
* `追加` プラグイン情報, 使用説明, README, CHANGELOG を 10 言語 (簡体字中国語, 繁体字中国語 (香港/台湾), 英語, フランス語, スペイン語, 日本語, 韓国語, ロシア語, アラビア語) でカバーしました

# v6.7.1 Alpha4

###### 2026/07/09

* `ヒント` 初の公開リリース. 同一バージョンの AutoJs6 (v6.7.1 Alpha4) と組み合わせて使用します
* `追加` AutoJs6 メインリポジトリから独立したプラグインリポジトリとして分離し, テンプレート APK プラグインサービスの初期実装を提供しました
* `追加` AutoJs6 メインリポジトリから起動される, Runtime Kit の取得, 検証, ビルド, 公開までのパイプラインを確立しました

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
