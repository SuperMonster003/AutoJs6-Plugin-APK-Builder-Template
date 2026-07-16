APK Builder Template 為 AutoJs6 封裝獨立應用程式提供 Runtime Kit.

請安裝與 AutoJs6 相同版本的外掛. 宿主透過 `org.autojs.plugin.APK_BUILDER` 發現外掛, 並讀取 `assets/runtime-kit/template.apk`.

內建 Runtime Kit 包含:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

遠端建置預設關閉, 只能在使用 `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true` 建置的外掛中啟用.
