APK Builder Template supplies the Runtime Kit used by AutoJs6 when packaging standalone applications.

Install the plugin release that matches your AutoJs6 version. The host discovers it through `org.autojs.plugin.APK_BUILDER` and reads `assets/runtime-kit/template.apk`.

The packaged Runtime Kit includes:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

Remote build support is disabled by default and can be enabled only in builds made with `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true`.
