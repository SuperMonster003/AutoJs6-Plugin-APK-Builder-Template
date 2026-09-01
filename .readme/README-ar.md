<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>مكون القالب الذي تعتمد عليه ميزة "تغليف التطبيق" في AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
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

تحول ميزة "تغليف التطبيق" في AutoJs6 السكربت أو المشروع إلى APK مستقل. ولإبقاء التطبيق الرئيسي خفيفا, يوجد القالب الكبير وكل جوهر التغليف داخل هذا المكون.

ليس للمكون أيقونة أو واجهة. يكتشفه AutoJs6 ويتحقق منه, ويجهز طلبا محدودا ويعرض التقدم. يفك المكون قالبه الخاص, ويكتب المشروع والموارد, ويعدل Manifest/resources, ويختار ABI, ويدير مفاتيح التوقيع, ثم يعيد APK مرشحا. ويتحقق AutoJs6 من الناتج بشكل مستقل قبل نشره.

تجري العملية كلها على جهاز Android نفسه عبر Binder وواصفات الملفات. لا يرفع هذا البروتوكول مصدر المشروع إلى الشبكة أو خدمة بناء سحابية.

******

### كيف يعمل

******

تسير عملية التغليف كما يلي:

1. القبول: يتحقق AutoJs6 من التوقيع الرسمي, وحالة التفعيل, ونطاق المضيف, وABI, والقدرة الرسمية, والبروتوكول, ونمط التنفيذ على الجهاز
2. الإعداد: ينشئ مدخلات محدودة للمشروع/المكتبات/مخزن المفاتيح ويثبت هوية الحزمة والموقع المتوقعين
3. بناء المكون: يعيد فحص الطلب, ويفك قالب Runtime Kit, ويكتب المشروع, ويعدل Manifest/resources, ويقتطع ABI, ويوقع
4. النتيجة: يعيد APK المرشح عبر واصف للقراءة فقط وينظف مساحة العمل الخاصة
5. النشر: يعيد AutoJs6 التحقق من الحجم وSHA-256 والبنية والتوقيع والموقع والحزمة والإصدار, ثم يستبدل الهدف ذريًا فقط بعد نجاح الجميع

******

### الميزات

******

- يمتلك جوهر التغليف على الجهاز بالكامل: القالب, والمشروع/الموارد, وManifest وresources.arsc, وABI, ومخازن المفاتيح, والتوقيع.
- يبقي AutoJs6 خفيفا: يتولى المضيف الواجهة والثقة/التوافق والإعداد والإلغاء/التقدم والتحقق المستقل, لا بانيًا ثانيا.
- يعمل بالكامل على الجهاز نفسه عبر Binder/AIDL وParcelFileDescriptor; ولا يرسل المشروع إلى الإنترنت أو السحابة.
- يربط كل بنية للمكون بـ AutoJs6 Runtime Kit متحقق منه ويدعم نطاقات تصحيح مغلقة متحقق منها.
- يوفر universal وarm64-v8a وarmeabi-v7a وx86_64 وx86 مع اختيار ABI الدقيق والرجوع إلى universal.
- يتضمن مخزن مفاتيح افتراضيا وينشئ/يتحقق من BKS/JKS داخل المكون مع دعم المخازن المخصصة.
- تغطي البيانات والتعليمات وREADME وCHANGELOG عشر لغات.

******

### البدء السريع

******

- **كيف تثبته**: يفضل التثبيت من مركز مكونات AutoJs6: تقرأ بنى المضيف المدعومة مصفوفة التوافق وتختار تلقائيا إصدار المكون المقترن وملف ABI المطابق للجهاز, مع الرجوع إلى universal عند غيابه. للتثبيت اليدوي, نزل ملف APK من صفحة [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) وحدد المضيف المقترن من وسم الإصدار الذي يحمل اسم AutoJs6 أو من اللاحقة autojs6- في إصدار المكون (مثلا: المكون v1.0.0+autojs6-6.8.0-alpha5 يقترن بـ AutoJs6 v6.8.0 Alpha5). إذا اختار مركز المكونات إصدارا أقل من المثبت, فاتبع إرشادات إلغاء التثبيت ثم إعادة التثبيت لأن Android لا يسمح بالكتابة فوق التطبيق بإصدار أقدم.
- **كيف تستخدمه**: لا خطوات إضافية. استخدم ميزة "تغليف التطبيق" في AutoJs6 كالمعتاد; وسيكتشف مسار التغليف المكون ويستخدم قالبه المدمج تلقائيا.
- **كيف تتأكد أنه يعمل**: بدون المكون (أو مع إصدار غير مطابق) يطلب منك مدخل التغليف في AutoJs6 تثبيته أو تفعيله; وبعد تثبيت الإصدار المطابق يختفي التنبيه, مما يعني أن المكون معروف. ليس للمكون أيقونة ولا واجهة, لذا فعدم ظهوره على الشاشة الرئيسية أمر طبيعي.
- **أين تنظر عند الفشل**: عند ظهور تحذير توافق, استخدم البنية التي اختارها مركز المكونات وفق مصفوفة التوافق أو تحقق من أن المضيف الحالي داخل النطاق الذي يعلنه المكون; وإذا أدى عدم التوافق إلى منع التغليف فثبت البنية المطابقة للمصفوفة; وعند خطأ في التحقق أو تلف القالب, أعد تثبيت المكون من مصدر رسمي; ولأي شيء آخر افتح [بلاغا](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) مرفقا بسجلات AutoJs6 وخطوات إعادة الإنتاج.

******

### الحدود

******

تجنبا لسوء الفهم, تقع الأمور التالية صراحة خارج نطاق هذا المكون:

- لا يعمل المكون وحده: لا أيقونة ولا واجهة له, ويستدعيه AutoJs6 متوافق.
- البناء على الجهاز ليس بناء سحابيا: لا يرفع البروتوكول مصدر المشروع.
- لا يحتفظ AutoJs6 بجوهر تغليف ثان داخل عمليته. إذا غاب المكون أو عطل أو لم يكن موثوقا أو متوافقا أو فشل, يتوقف الطلب ويحفظ الناتج السابق.
- يظل مستودع AutoJs6 هو من يولد Runtime Kit; ويتحقق المكون منه ويغلفه ويوزعه ويستخدمه.
- تبقى قدرة "البناء عن بعد" القديمة معطلة للمضيفين القدامى. كان الاسم يعني عملية أخرى على الجهاز, لا خدمة إنترنت, وهي منفصلة عن القدرة الرسمية.

******

### الأسئلة الشائعة

******

**س: كيف يختار مركز المكونات بنية المكون؟**

ج: تستعلم إصدارات AutoJs6 المدعومة عن compat-matrix.json باستخدام versionCode الخاص بها, وتختار البنية ذات أعلى pluginVersionCode ضمن نطاق التوافق, ثم تفضل ABI المطابق للجهاز وترجع إلى universal عند غيابه. لا يمكن لإدخال المصفوفة تغطية نطاق تصحيحات تم التحقق منه إلا عند التصريح صراحة بـ allowPatchVersionMismatch=true: يحزم مضيف البناء المطابق من دون تحذير, ويعيد مضيف آخر داخل النطاق استخدام البنية نفسها مع تحذير, ولا يمكن لمضيف خارج النطاق استخدامها. إذا لم يوجد إدخال صالح في المصفوفة يبقى مسار Release/الوسم الحالي هو البديل. إذا كان إصدار المكون المقترن أقل من الإصدار المثبت, يطلب مركز المكونات إلغاء التثبيت أولا ثم تثبيت البنية المقترنة لأن Android لا يمكنه خفض الإصدار فوق التطبيق الحالي.

**س: لماذا يلزم إصدار مكون مطابق لـ AutoJs6؟**

ج: يجب أن يطابق runtime القالب واجهة المضيف. يختار مركز المكونات أعلى إصدار متوافق وأفضل ABI, ويمنع المضيف خارج النطاق.

**س: لا أرى المكون في المشغل. هل فشل التثبيت؟**

ج: لا. لا يملك أيقونة أو واجهة عمدا ويعمل فقط كخدمة لـ AutoJs6. تحقق منه في الإعدادات > التطبيقات.

**س: هل يرسل مشروعي إلى خادم بعيد؟**

ج: لا. تتواصل عمليتا التطبيق على جهاز Android نفسه. اسم "البناء عن بعد" التاريخي يعني أن Binder يعبر حد العملية; أما النمط الرسمي فهو `on-device-plugin`.

**س: ماذا يحدث إذا فشل المكون؟**

ج: يوقف AutoJs6 الطلب, ويعرض خطأ قابلا للإجراء, ويحفظ أي APK سابق; ولا ينتقل خفية إلى باني ثان داخل المضيف.

******

### مرجع تقني

******

الأقسام التالية موجهة لمطوري المكونات والمدمجين; ولا يحتاج إليها عادة من يستخدم المكون فقط.

#### Runtime Kit

يبنى Runtime Kit في مستودع AutoJs6 الرئيسي وهو مصدر الحقيقة الوحيد لقالب التطبيق المستقل. يتحقق هذا المكون من ذلك الأثر ويغلفه فقط; ولا ينشئ `template.apk`. يحتوي Runtime Kit الكامل عادة على هذه الملفات:

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

#### معرفات الاكتشاف

يكتشف المضيف هذا المكون ويرتبط به عبر المعرفات التالية:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### البناء المحلي

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

#### مسار الإصدار

مسار إصدار الإنتاج المتوقع هو:

```text
AutoJs6 tag
-> main repository generates autojs6-runtime-kit-*.zip
-> main repository uploads the Runtime Kit to its GitHub Release
-> main repository dispatches SuperMonster003/AutoJs6-Plugin-APK-Builder-Template
-> this repository downloads and verifies the Runtime Kit
-> this repository builds the plugin APK
-> this repository uploads the plugin APK to the same tag Release
-> this repository records the pairing into compat-matrix.json
-> AutoJs6 Plugin Center installs this plugin
```

#### التوقيع

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

### خارطة الطريق

******

يتابع ROADMAP.md البناء الرسمي المدار بالمكون, والمرشحين, والتوزيع حسب ABI, والتوافق, وأدلة الأمان, وضمان ما بعد GA في قائمة قابلة للتحقق.

- [عرض ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### تاريخ الإصدارات

******

# v1.0.0

###### 2026/09/02

* `تلميح` يتطلب مسار تغليف التطبيق العادي الآن مكون APK Builder على الجهاز; يبقى مفتاح supportsRemoteBuild القديم معطلا لكنه لم يعد يعطل التغليف العادي
* `تلميح` أول إصدار رسمي في خط إصدارات الملحق المستقل, ومتوافق تحديدا مع Runtime Kit لـ AutoJs6 v6.8.0 (versionCode 5277); إصدار الملحق المركب هو 1.0.0+autojs6-6.8.0 (versionCode 527701), ويختار مركز الملحقات بناء ABI المتوافق عبر compat-matrix.json, وتظل عمليات البناء البعيدة معطلة افتراضيا
* `إضافة` رقي محرك المكون ليصبح مسار التغليف الرسمي الوحيد على الجهاز; يبقى AutoJs6 خفيفا ويتحقق بصورة مستقلة من كل APK معاد
* `إضافة` نقل إنشاء BKS/JKS والتحقق منهما إلى المكون عبر API مخزن مفاتيح ذي إصدار وفشل مغلق
* `إضافة` إضافة SemVer 1.0.0 للملحق وترقيم بناء مستقل وأسماء إصدارات مركبة وقيم Android versionCode متزايدة رتيبا تتيح نشر عدة إصدارات للملحق مع المضيف نفسه
* `إضافة` إضافة متغيرات universal وarm64-v8a وarmeabi-v7a وx86_64 وx86 مع اختيار ABI المطابق والرجوع إلى universal
* `إضافة` إضافة عقد نطاق توافق للمضيف مغلق عند الفشل ومصفوفة توافق موثوقة كي يشترك نطاق تصحيحات متجاورة تم التحقق منه صراحة في بناء واحد للملحق
* `إصلاح` تمت محاذاة ترقيم البناء التجريبي عن بُعد للملف الواحد مع أداة البناء القديمة، وأضيف فحص مسبق لمساحة العمل يفشل بشكل مغلق ويستخدم أحجام الإدخال بعد فك الضغط التي تم التحقق منها، وحدًا لتوسّع القالب تم التحقق منه أثناء البناء، واحتياطيًا قدره 256 MiB
* `إصلاح` رُفضت بيانات حزم Node.js المضمّن القديمة وتوجيهات المصدر قبل BUILD/SIGN مع إرشاد للانتقال إلى إضافة Runtime الخارجية، وأزيل حقن خدمة Manifest وأذونات المقدمة المتقادم
* `إصلاح` تم إصلاح حالة تسابق بين الإغلاق وخيط البناء في الجلسات البعيدة التجريبية كانت قد تعيد إنشاء مساحة عمل محذوفة بعد الإلغاء أو الإغلاق؛ تنتظر عملية التنظيف الآن انتهاء العامل ولا تترك ملفات متبقية
* `إصلاح` تعزيز أمان البناء البعيد التجريبي برفض نص TypeScript المرحلي المشفر غير المعلن في قائمة المسارات والتعرف الصحيح على مخازن مفاتيح BKS المخصصة بعد توحيد اسم الملف في مساحة العمل
* `إصلاح` تشديد حدود إدخال البناء البعيد التجريبي عبر التحقق الصارم من أنواع Parcelable/Bundle وproject.json وأحجامها وعمق تداخلها, وتقييد مخزن المفاتيح والأيقونة وعمق مسارات ZIP وأطوال مقاطعها, وإصلاح تجاوز حدود اسم حزمة ARSC واسم ملف الإخراج المشتق
* `إصلاح` تعذر تنشيط المكون الإضافي من مركز المكونات الإضافية بعد التثبيت على بعض الأنظمة
* `تحسين` يدعم مسار الإصدار الموثوق الآن وضع مرشح معزول ينشئ خمسة ملفات APK موقعة بمفتاح الإنتاج وملف evidence من عنصر Actions مثبت للمضيف, من دون إنشاء Release أو تحديث مصفوفة التوافق المعتمدة
* `تحسين` توحيد قواعد التحقق من Runtime Kit بين Gradle وPython, بما يشمل التجزئات والأحجام والملفات المطلوبة وإدخالات APK واتساق المتغيرات الخمسة
* `تحسين` نشر بيان أدلة JSON قابل للقراءة آليا بجانب ملفات APK الخمسة, لربط تجزئات الأصول وشهادة التوقيع وإصدارات الملحق/المضيف ونطاق التوافق ومعرفات Runtime Kit وإصدارات البروتوكول
* `تحسين` تحديث إرشادات التثبيت والأسئلة الشائعة وتمرين الإصدار والوثائق بعشر لغات لتوضيح الإصدارات المتوافقة واختيار ABI والاسترداد بعد الرجوع إلى إصدار أقدم ونظام الإصدار المستقل
* `تحسين` توحيد تخطيط README وطريقة إدارة إصدارات منصة Gradle

# v6.8.0 Alpha5

###### 2026/07/16

* `تلميح` يقترن بـ AutoJs6 v6.8.0 Alpha5; تحل إصدارات مركز المكونات المدعومة البنية المقترنة تلقائيا, بينما يستخدم التثبيت اليدوي وسم Release أو اللاحقة autojs6- المطابقة; ليس للمكون أيقونة ولا واجهة ويستدعى تلقائيا عند تغليف التطبيقات
* `إضافة` تمكين AutoJs6 من اكتشاف المكون وقراءة قالبه المدمج تلقائيا, بحيث لم تعد ميزة "تغليف التطبيق" تعتمد على APK قالب مضمن في التطبيق الرئيسي
* `إضافة` تضمين Runtime Kit الكامل: APK القالب, ومخزن المفاتيح الافتراضي, وبيانات وقت التشغيل الوصفية, وملفات العقد
* `إضافة` تنفيذ فحوص تلقائية لتوافق الإصدار والبروتوكول قبل التغليف, مع تحذير أو منع عند الاختلاف لتجنب إنتاج تطبيقات لا تعمل
* `إضافة` التحقق من ملخصات SHA-256 لـ Runtime Kit والمدخلات المطلوبة للقالب عند بناء المكون, وإبلاغ ملخص القالب إلى AutoJs6 لإعادة التحقق أثناء التشغيل
* `إضافة` تقديم بروتوكول تجريبي للبناء عن بعد ينفذ بناء خفيفا داخل عملية المكون (معطل افتراضيا, ويجب تفعيله صراحة عند البناء)
* `إضافة` ربط مسار الإصدار الآلي: عند نشر مستودع AutoJs6 الرئيسي إصدارا, يبنى APK مكون مقترن به آليا ويوقع بالمفتاح الموثوق ويتحقق من بصمة الشهادة ثم ينشر
* `إضافة` تغطية 10 لغات في بيانات المكون وتعليمات الاستخدام و README و CHANGELOG: الصينية المبسطة, والصينية التقليدية (هونغ كونغ/تايوان), والإنجليزية, والفرنسية, والإسبانية, واليابانية, والكورية, والروسية, والعربية

# v6.7.1 Alpha4

###### 2026/07/09

* `تلميح` أول إصدار عام; يقترن بإصدار AutoJs6 المطابق (v6.7.1 Alpha4)
* `إضافة` الانفصال عن مستودع AutoJs6 الرئيسي كمستودع مكون مستقل مع التنفيذ الأولي لخدمة مكون APK القالب
* `إضافة` إنشاء خط الإنتاج المعتمد على Runtime Kit والذي يطلقه مستودع AutoJs6 الرئيسي, فيجلب المكون ويتحقق منه ويبنيه وينشره

##### لمزيد من تاريخ الإصدارات

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-ar.md)

******

### الرخصة

******

ينشر هذا المشروع بموجب رخصة Mozilla Public License 2.0 التي تسمح بالاستخدام والتعديل والتوزيع وفق شروطها.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

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

يحتوي `strings.xml` على أسماء المكون وأوصافه وتعليمات الاحتياط المترجمة; ويحتوي `plugin_instruction.md` على تعليمات الاستخدام التي يعرضها المضيف. يولد README و CHANGELOG من مصادر JSON بواسطة `.python/generate_markdown.py`; ولتعديل الوثائق حرر ملفات JSON وأعد تشغيل السكربت بدلا من تحرير الملفات المولدة.

******

### روابط

******

- مشروع AutoJs6 الرئيسي: https://github.com/SuperMonster003/AutoJs6
- وثائق AutoJs6: https://docs.autojs6.com
