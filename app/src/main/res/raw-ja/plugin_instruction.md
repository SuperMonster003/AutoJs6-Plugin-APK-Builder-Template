APK Builder Template は AutoJs6 がスタンドアロンアプリをパッケージ化するときに使う Runtime Kit を提供します.

AutoJs6 と一致するバージョンのプラグインをインストールしてください. ホストは `org.autojs.plugin.APK_BUILDER` でプラグインを検出し, `assets/runtime-kit/template.apk` を読み取ります.

パッケージ済み Runtime Kit には次のファイルが含まれます:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

リモートビルド対応は既定で無効です. `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true` で作成されたビルドでのみ有効化できます.
