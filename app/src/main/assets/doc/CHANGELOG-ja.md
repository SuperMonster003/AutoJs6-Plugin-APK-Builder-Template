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
