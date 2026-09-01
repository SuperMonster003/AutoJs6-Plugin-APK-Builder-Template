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

AutoJs6 の "アプリの包装" 機能は, スクリプトやプロジェクトを単体でインストール・実行できる APK に変換します. ターゲット端末に AutoJs6 は不要です. 包装には, 完全なスクリプト実行環境をあらかじめ内蔵した "テンプレート APK" が骨格として必要です. 本体アプリを軽量に保つため, 最近の AutoJs6 はこの大きなテンプレートを同梱せず, 本プラグインに分離しました. 包装機能を使うユーザーだけが必要に応じてインストールします.

本プラグインにはアイコンも画面もありません. すべてバックグラウンドで AutoJs6 から自動的に呼び出されます: 包装時に AutoJs6 がプラグインを発見し, バージョン互換性とファイル整合性を検証してから, 内蔵テンプレート APK を読み取って包装を完了します.

一言で判断するなら: AutoJs6 の "アプリの包装" 機能を使うなら, プラグインセンターが互換性マトリクスから現在の AutoJs6 向けに選んだビルドをインストールしてください. スタンドアロンアプリを包装しないなら不要です.

******

### 仕組み

******

スタンドアロンアプリを包装するとき, AutoJs6 と本プラグインは次のように連携します:

1. 発見: AutoJs6 がインストール済みのテンプレートプラグインを見つけてメタデータを読み取ります
2. 互換性チェック: バージョン, プロトコルバージョン, テンプレートパッケージ名を比較し, 不一致なら警告するか包装をブロックします
3. 整合性チェック: テンプレート APK の SHA-256 ダイジェストを照合し, 破損や改ざんを排除します
4. テンプレート転送: プロセス間パイプでテンプレート APK をストリーミングし, 一時コピーを作りません
5. 包装: AutoJs6 がスクリプト, 設定, リソースをテンプレートへ書き込み, 最終的なスタンドアロン APK を生成します

******

### 機能

******

- AutoJs6 の "アプリの包装" 機能に完全なスタンドアロンアプリテンプレート (Runtime Kit) を提供します. インストール後の設定は不要です.
- 各プラグインビルドは実際に構築した AutoJs6 ホストをバージョン名に保持し (例: 1.0.0+autojs6-6.8.0-alpha5), 検証済みのパッチ閉区間を明示的に宣言できます. 完全一致なら通知せず, 区間内の別ホストには警告し, 区間外では包装をブロックします.
- 二重の整合性保護: プラグインのビルド時に Runtime Kit 全ファイルの SHA-256 ダイジェストと必須エントリを検証し, 包装時にはテンプレートダイジェストを AutoJs6 へ報告して再検証させます.
- テンプレートはプロセス間パイプで AutoJs6 へストリーミング転送され, 余分な一時コピーを作りません.
- 既定のキーストアを内蔵しており, カスタム署名鍵を設定していなくてもインストール可能な APK を生成できます.
- 実験的な "リモートビルド" プロトコルに対応し, プラグインプロセス単体で軽量ビルドを実行できます (既定で無効; 機能範囲の節を参照).
- プラグイン情報, 使用説明, README, CHANGELOG は簡体字中国語, 繁体字中国語 (香港/台湾), 英語, フランス語, スペイン語, 日本語, 韓国語, ロシア語, アラビア語の 10 言語をカバーします.

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

- 本プラグインは単体では使えません: アイコンも画面もなく, 包装時に AutoJs6 から呼び出されるだけです.
- 本プラグインはテンプレート APK を生成しません: テンプレートと Runtime Kit は AutoJs6 メインリポジトリがビルドして公開し, 本プラグインは検証, 同梱, 配布のみを担当します.
- 本プラグインはスクリプトの作成や日常の実行には関与しません: "アプリの包装" 機能だけが読み取ります.
- リモートビルドは実験的機能で既定では無効です: 公式リリースのプラグインでは有効化されず, 自分でビルドして明示的に有効化した場合のみ使えます.
- 本プラグインはバージョン要件を緩和しません: AutoJs6 とバージョンが一致しない包装はブロックされることがあり, 通ったとしても成果物の動作は保証されません.

******

### よくある質問

******

**Q: プラグインセンターはどのようにビルドを選びますか?**

A: 対応する AutoJs6 は自身の versionCode で compat-matrix.json を照会し, 互換範囲内で pluginVersionCode が最も高いビルドを選択します. 次に端末の正確な ABI を優先し, なければ universal にフォールバックします. マトリクスエントリが検証済みパッチ区間を対象にできるのは allowPatchVersionMismatch=true を明示した場合だけです. 実際の構築ホストでは警告せず包装でき, 区間内の別ホストは同じビルドを警告付きで再利用し, 区間外のホストは利用できません. 使用可能なマトリクスエントリがなければ, 既存の Release/タグ経路へ戻ります. 対応プラグインのバージョンがインストール済みより低い場合, Android はそのままダウングレードできないため, プラグインセンターが先にアンインストールしてから対応ビルドをインストールするよう案内します.

**Q: なぜプラグインは AutoJs6 のバージョンと対にする必要がありますか?**

A: テンプレート APK 内蔵のスクリプト実行環境は AutoJs6 のランタイム API と厳密に対応しているため, 互換性はプラグイン自身のセマンティックバージョンではなく, プラグインが宣言し検証された versionCode 契約で判定されます. ほとんどのビルドは 1 つの厳密なホストだけを対象としますが, 検証済みのパッチバージョン閉区間を明示的に宣言することもできます. その場合, 対応ホストは警告なしで通過し, 区間内の他のホストには警告が出て, 区間外は包装をブロックされます. プラグイン自身のバージョン (1.0.0 など) は独立して進化し, バージョン名の autojs6- サフィックスとリリースタグが対応ホストを示します. 古い AutoJs6 を使う場合は対応する古いタグのプラグインビルドをダウンロードしてください (新しいプラグインから戻すには先にアンインストールが必要です. Android はダウングレードの上書きインストールを許可していません).

**Q: ホーム画面にプラグインが見当たりません. インストールに失敗しましたか?**

A: いいえ. 本プラグインにはアイコンも画面もなく, AutoJs6 向けのバックグラウンドサービスとしてのみ動作します. システムの "設定 > アプリ" 一覧で APK Builder Template を確認できます.

**Q: 包装時にテンプレート検証失敗と表示されます. どうすればいいですか?**

A: 通常, インストールされたプラグインが不完全か破損しています. AutoJs6 プラグインセンターか本リポジトリの Releases から再インストールしてください. 解決しない場合は Issues へ報告してください.

**Q: プラグインはなぜこんなに大きいのですか?**

A: スクリプトエンジンと全アーキテクチャのネイティブライブラリを含む, 完全なスタンドアロンアプリテンプレートを内蔵しているためです. まさにそれが, テンプレートを AutoJs6 本体から分離した理由です: 包装機能を使うユーザーだけがこのサイズを負担します.

**Q: "リモートビルド" とは何ですか?**

A: プラグインが自身のプロセス内で軽量ビルド (テンプレート展開, スクリプトと設定の書き込み, パッケージ名とリソースの書き換え, 再署名) を行う実験的プロトコルです. 公式リリースのプラグインでは無効のままで, 現時点では自分でビルドする開発者向けです.

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

予定している作業と進捗は ROADMAP.md に検証可能なチェックリストとして管理しています: リモートビルドの安定化, アーキテクチャ別テンプレートバリアント, パッチレベルのバージョン互換など. 議論は Issues で歓迎します.

- [ROADMAP.md を見る](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### リリース履歴

******

# v1.0.0

###### 2026/09/01

* `ヒント` プラグイン独立バージョン系列の初回正式リリースで, AutoJs6 v6.8.0 (versionCode 5277) Runtime Kit に厳密に対応します; 複合プラグインバージョンは 1.0.0+autojs6-6.8.0 (versionCode 527701) で, プラグインセンターは compat-matrix.json から対応 ABI ビルドを選択し, リモートビルドは引き続き既定で無効です
* `追加` プラグイン SemVer 1.0.0, 独立ビルド番号, 複合バージョン名, 単調増加する Android versionCode を導入し, 同じホスト向けに複数のプラグインリリースを提供可能に変更
* `追加` universal, arm64-v8a, armeabi-v7a, x86_64, x86 の各バリアントを追加し, ABI の完全一致選択と universal フォールバックに対応
* `追加` 失敗時に閉じるホスト互換範囲契約と権威ある互換マトリックスを追加し, 明示的に検証された隣接パッチ範囲で 1 つのプラグインビルドを共有可能に変更
* `修正` 実験的なリモート単一ファイルビルドのビルド番号を従来のビルダーと一致させ、照合済みの展開後入力サイズ、ビルド時に検証されたテンプレート展開上限、256 MiB の予約領域を用いるフェイルクローズなワークスペース容量事前検査を追加
* `修正` 従来の組み込み Node.js パッケージ用メタデータとソース指令を BUILD/SIGN 前に拒否して外部 Runtime プラグインへの移行を案内し、不要になった Manifest サービスとフォアグラウンド権限の注入を削除しました
* `修正` 実験的なリモートセッションで, 終了処理とビルドスレッドの競合により, キャンセルまたは終了後に削除済みのセッション作業領域が再作成される問題を修正しました。現在はワーカーの終了を待ってからクリーンアップし, 残留ファイルを残しません
* `修正` 実験的なリモートビルドを強化し, パス一覧にない TypeScript 一時暗号文を拒否するとともに, ワークスペースでファイル名が正規化された後もカスタム BKS キーストアを正しく検出
* `修正` 実験的なリモートビルドの入力境界を強化し, Parcelable/Bundle と project.json の型, サイズ, ネストを厳密に検証するとともに, キーストア, アイコン, ZIP パスの深さ/セグメント長を制限し, ARSC パッケージ名と派生出力ファイル名の境界超過を修正
* `修正` 一部のシステムでインストール後にプラグインセンターからプラグインを有効化できない問題
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
