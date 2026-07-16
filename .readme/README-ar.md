<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>مكون APK القالب لتغليف تطبيقات AutoJs6 المستقلة</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/commit/dc4627c752f8c1f709b302651a7de57992eac2b5"><img alt="Created" src="https://img.shields.io/date/1784208939?color=2e7d32&label=Created"/></a>
    <br>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=534BAE&label=License"/></a>
  </p>
</div>

******

### اللغات

******

يدعم README.md الحالي اللغات التالية:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- العربية [ar] # الحالية

******

### مقدمة

******

يوفر مكون AutoJs6 APK Builder Template ملف APK القالب الخارجي و Runtime Kit اللذين يستخدمهما AutoJs6 عند تغليف التطبيقات المستقلة. يقرأ المضيف APK القالب من خدمة المكون ويتحقق من بيانات الإصدار والبروتوكول للتوافق.

******

### الميزات

******

- يوفر خدمة المكون `autojs6-apk-builder-template` مع معرف المكون `autojs6-apk-builder-template` والمحرك `apk-builder-template`.
- يعرض بيانات المكون العامة عبر `org.autojs.plugin.INFO` ويقدم APK القالب عبر `org.autojs.plugin.APK_BUILDER`.
- يتحقق من ملخصات SHA-256 الخاصة ب Runtime Kit ومدخلات `template.apk` المطلوبة أثناء البناء.
- يغلف `template.apk`, ومخزن المفاتيح الافتراضي, وبيانات وقت التشغيل, وملفات العقد داخل `assets/runtime-kit/`.
- يرسل بيانات إصدار المضيف, وإصدار البروتوكول, واسم حزمة القالب, وملخص القالب, وقدرة البناء عن بعد.
- تغطي بيانات المكون, وتعليمات الاستخدام, و README, و CHANGELOG الإسبانية/الفرنسية/الروسية/العربية/اليابانية/الكورية/الإنجليزية/الصينية المبسطة/الصينية التقليدية لهونغ كونغ/الصينية التقليدية لتايوان.

******

### Runtime Kit

******

يأتي Runtime Kit من مستودع AutoJs6 الرئيسي وهو مصدر الحقيقة الوحيد لقالب التطبيق المستقل. يتحقق هذا المكون من ذلك الأثر ويغلفه فقط. لا ينشئ `template.apk`. يحتوي Runtime Kit الكامل عادة على هذه الملفات

```text
template.apk
template.apk.sha256
default_key_store.bks
default_key_store.bks.sha256
runtime-kit.json
build-contract.json
public-api.txt
assets-manifest.json
native-libs.json
provenance.json
```

******

### البناء المحلي

******

أنشئ Runtime Kit من مستودع AutoJs6 الرئيسي أولا:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

ثم ابن هذا المستودع مع تحديد دليل Runtime Kit الناتج:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

يمكنك أيضا فك ضغط `autojs6-runtime-kit-*.zip` منشور إلى `runtime-kit/` ثم البناء مباشرة:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

******

### مسار الإصدار

******

مسار إصدار الإنتاج المتوقع هو:

```text
AutoJs6 tag
-> main repository generates autojs6-runtime-kit-*.zip
-> main repository uploads the Runtime Kit to its GitHub Release
-> main repository dispatches SuperMonster003/AutoJs6-Plugin-APK-Builder-Template
-> this repository downloads and verifies the Runtime Kit
-> this repository builds the plugin APK
-> this repository uploads the plugin APK to the same tag Release
-> AutoJs6 Plugin Center installs this plugin
```

******

### التوقيع

******

يجب توقيع إصدارات الإنتاج للمكون بمفتاح توقيع مكونات AutoJs6 الموثوق. تتطلب إصدارات GitHub Actions أسرار المستودع التالية:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

ما زالت بنى الإصدار المحلية تدعم ملف الجذر المتجاهل `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### تاريخ الإصدارات

******

# v6.8.0 Alpha5

###### 2026/07/16

* `إضافة` تمت إضافة خدمة مكون APK Builder Template مع معرف المكون `autojs6-apk-builder-template`, والمحرك `apk-builder-template`, والمتغير `inrt-universal`
* `إضافة` تم عرض بيانات المكون عبر `org.autojs.plugin.INFO` وتقديم APK القالب عبر `org.autojs.plugin.APK_BUILDER`
* `إضافة` تم تغليف Runtime Kit الخاص ب AutoJs6 داخل `assets/runtime-kit/`, بما في ذلك `template.apk`, ومخزن المفاتيح الافتراضي, والبيانات, وملفات العقد
* `إضافة` تمت إضافة تحقق وقت البناء من بيانات Runtime Kit, وملخصات SHA-256, ومدخلات `template.apk` المطلوبة
* `إضافة` تم إرسال إصدار المضيف, وإصدار البروتوكول, واسم حزمة القالب, و SHA-256 للقالب, وملخصات Runtime API, وقدرة البناء عن بعد
* `إضافة` تمت إضافة دعم اختياري لبروتوكول البناء عن بعد التجريبي عبر `autojs.apkBuilder.templatePlugin.enableRemoteBuild`
* `إضافة` تمت إضافة دعم مسار الإصدار لتنزيل Runtime Kit, والتحقق من assets, والتوقيع بالمفتاح الموثوق, ورفع APK العام
* `إضافة` تمت إضافة موارد مترجمة لبيانات المكون, وتعليمات الاستخدام, و README, و CHANGELOG للإسبانية/الفرنسية/الروسية/العربية/اليابانية/الكورية/الإنجليزية/الصينية المبسطة/الصينية التقليدية لهونغ كونغ/الصينية التقليدية لتايوان

##### لمزيد من تاريخ الإصدارات

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-ar.md)

******

### بنية الموارد

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

يحتوي `strings.xml` على أسماء المكون وأوصافه وتعليمات الاحتياط المترجمة; ويحتوي `plugin_instruction.md` على تعليمات الاستخدام التي يعرضها المضيف. يتم توليد README و CHANGELOG من مصادر JSON بواسطة `.python/generate_markdown.py`.

******

### روابط

******

- مشروع AutoJs6 الرئيسي: https://github.com/SuperMonster003/AutoJs6
- وثائق AutoJs6: https://docs.autojs6.com
