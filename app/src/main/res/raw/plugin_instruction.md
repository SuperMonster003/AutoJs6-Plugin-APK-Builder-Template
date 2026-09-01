APK Builder Template supplies the Runtime Kit used by AutoJs6 when packaging standalone applications.

Install from a current AutoJs6 Plugin Center when possible: it reads `compat-matrix.json`, chooses the build paired with this host, prefers the exact device ABI, and falls back to universal. For a manual install, use the matching Release tag or the autojs6- suffix in the plugin version; if returning from a newer plugin build, uninstall it first because Android cannot overwrite an app with a downgrade. The host discovers the plugin through `org.autojs.plugin.APK_BUILDER` and reads `assets/runtime-kit/template.apk`.

A Runtime Kit may explicitly cover a verified patch interval. Its exact built-for host packages silently; another host inside the interval continues with a warning, while a host outside the interval is blocked before template transfer.

The packaged Runtime Kit includes:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

Remote build support is disabled by default and can be enabled only in builds made with `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true`.
