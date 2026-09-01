APK Builder Template は AutoJs6 がスタンドアロンアプリをパッケージ化するときに使う Runtime Kit を提供します.

可能なら新しい AutoJs6 プラグインセンターからインストールしてください. `compat-matrix.json` を読み取り, このホストに対応するビルドと端末の正確な ABI を優先して選び, 見つからなければ universal にフォールバックします. 手動インストールでは対応する Release タグまたはプラグインバージョンの autojs6- サフィックスを確認してください. 新しいプラグインビルドから戻す場合は, Android がダウングレード上書きを許可しないため先にアンインストールします. ホストは `org.autojs.plugin.APK_BUILDER` でプラグインを検出し, `assets/runtime-kit/template.apk` を読み取ります.

Runtime Kit は検証済みのパッチ区間を明示的に対象にできます. 実際の構築ホストでは警告せず包装し, 区間内の別ホストは警告後に続行しますが, 区間外のホストはテンプレート転送前にブロックされます.

パッケージ済み Runtime Kit には次のファイルが含まれます:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

包装は同じ Android 端末上のプラグインプロセス内で完結し, プロジェクトのソースは送信されません. AutoJs6 は信頼性と互換性を判定し, 返された APK を独立して検証します. 旧 `supportsRemoteBuild` スイッチは無効のままですが, この正式経路は無効になりません.
