يوفر APK Builder Template حزمة Runtime Kit التي يستخدمها AutoJs6 عند تغليف التطبيقات المستقلة.

ثبّت إصدار المكون المطابق لإصدار AutoJs6 لديك. يكتشفه المضيف عبر `org.autojs.plugin.APK_BUILDER` ويقرأ `assets/runtime-kit/template.apk`.

تتضمن Runtime Kit المضمنة:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

دعم البناء عن بعد معطل افتراضيا ولا يمكن تمكينه إلا في البنى التي تم إنشاؤها باستخدام `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true`.
