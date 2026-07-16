APK Builder Template proporciona el Runtime Kit que AutoJs6 usa al empaquetar aplicaciones independientes.

Instala la version del plugin que coincida con tu version de AutoJs6. El host lo descubre mediante `org.autojs.plugin.APK_BUILDER` y lee `assets/runtime-kit/template.apk`.

El Runtime Kit empaquetado incluye:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

El soporte de compilacion remota esta desactivado de forma predeterminada y solo puede activarse en compilaciones hechas con `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true`.
