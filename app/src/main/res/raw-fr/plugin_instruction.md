APK Builder Template fournit le Runtime Kit utilise par AutoJs6 pour empaqueter des applications autonomes.

Installez la version du plugin correspondant a votre version d'AutoJs6. L'hote le decouvre via `org.autojs.plugin.APK_BUILDER` et lit `assets/runtime-kit/template.apk`.

Le Runtime Kit empaquete contient:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

La construction distante est desactivee par defaut et ne peut etre activee que dans les builds crees avec `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true`.
