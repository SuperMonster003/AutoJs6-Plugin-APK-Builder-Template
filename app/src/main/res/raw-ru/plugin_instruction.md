APK Builder Template предоставляет Runtime Kit, который AutoJs6 использует при упаковке автономных приложений.

Установите выпуск плагина, совпадающий с вашей версией AutoJs6. Хост обнаруживает его через `org.autojs.plugin.APK_BUILDER` и читает `assets/runtime-kit/template.apk`.

Упакованный Runtime Kit содержит:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

Поддержка удаленной сборки по умолчанию отключена и может быть включена только в сборках, созданных с `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true`.
