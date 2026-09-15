<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>Плагин с шаблоном для функции AutoJs6 "Упаковать приложение"</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=534BAE&label=License"/></a>
  </p>
</div>

******

### Языки

******

Текущий README.md поддерживает следующие языки:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- Русский [ru] # текущий
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### Введение

******

Функция AutoJs6 "Упаковать приложение" превращает скрипт или проект в автономный APK. Чтобы основное приложение оставалось компактным, крупный шаблон и все ядро упаковки находятся в этом плагине.

У плагина нет значка и интерфейса. AutoJs6 обнаруживает и проверяет его, готовит ограниченный запрос и показывает ход работы. Плагин распаковывает собственный шаблон, записывает проект и ресурсы, изменяет Manifest/resources, выбирает ABI, управляет ключами и подписью и возвращает APK-кандидат. Перед публикацией AutoJs6 независимо проверяет результат.

Все выполняется на одном Android-устройстве через Binder и файловые дескрипторы. Исходный код проекта не отправляется в сеть или облачную службу сборки.

******

### Как Это Работает

******

Процесс упаковки:

1. Допуск: AutoJs6 проверяет официальную подпись, включение, диапазон хоста, ABI, формальную возможность, протокол и режим выполнения на устройстве
2. Подготовка: создает ограниченные входы проекта/нативных библиотек/хранилища ключей и фиксирует ожидаемые пакет и подписанта
3. Сборка плагином: повторно проверяет запрос, распаковывает шаблон Runtime Kit, записывает проект, изменяет Manifest/resources, обрезает ABI и подписывает
4. Результат: возвращает APK-кандидат через дескриптор только для чтения и очищает приватную рабочую область
5. Публикация: AutoJs6 повторно проверяет размер, SHA-256, структуру, подпись, подписанта, пакет и версию и лишь затем атомарно заменяет цель

******

### Возможности

******

- Полностью владеет ядром упаковки на устройстве: шаблон, проект/ресурсы, Manifest и resources.arsc, ABI, хранилища ключей и подпись.
- Сохраняет AutoJs6 компактным: хост отвечает за UI, доверие/совместимость, подготовку, отмену/прогресс и независимую проверку, а не за второй сборщик.
- Полностью работает на том же устройстве через Binder/AIDL и ParcelFileDescriptor; проект не отправляется в Интернет или облако.
- Связывает каждую сборку плагина с проверенным Runtime Kit AutoJs6 и поддерживает проверенные замкнутые диапазоны патчей.
- Предоставляет universal, arm64-v8a, armeabi-v7a, x86_64 и x86 с точным выбором ABI и резервом universal.
- Содержит хранилище ключей по умолчанию и выполняет создание/проверку BKS/JKS в плагине, поддерживая пользовательские хранилища.
- Метаданные, инструкции, README и CHANGELOG доступны на 10 языках.

******

### Быстрый Старт

******

- **Как установить**: По возможности устанавливайте из Центра плагинов AutoJs6: поддерживаемые сборки хоста читают матрицу совместимости и автоматически выбирают сопряженную версию плагина и точный ABI-артефакт устройства, а при его отсутствии используют universal. Для ручной установки скачайте APK со страницы [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) и определите сопряженный хост по тегу с именем версии AutoJs6 или по суффиксу autojs6- в версии плагина (например, плагин v1.0.0+autojs6-6.8.0-alpha5 работает в паре с AutoJs6 v6.8.0 Alpha5). Если Центр плагинов выбрал версию ниже установленной, следуйте подсказке об удалении и повторной установке, поскольку Android не допускает установку с понижением поверх приложения.
- **Как пользоваться**: Никаких дополнительных действий. Используйте функцию "Упаковать приложение" в AutoJs6 как обычно; процесс упаковки сам обнаружит плагин и использует его встроенный шаблон.
- **Как убедиться, что все работает**: Без плагина (или при несовпадении версий) вход в упаковку в AutoJs6 предложит установить или включить его; после установки подходящей версии подсказка исчезает — значит, плагин распознан. У плагина нет значка и интерфейса, поэтому его отсутствие на рабочем столе — это норма.
- **Куда смотреть при сбое**: При предупреждении о совместимости используйте сборку, выбранную Центром плагинов по матрице совместимости, либо убедитесь, что текущий хост входит в заявленный плагином диапазон; если несовместимость блокирует упаковку, установите соответствующую матрице сборку; при ошибке проверки или повреждении шаблона переустановите плагин из официального источника; в остальных случаях создайте [issue](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues), приложив журналы AutoJs6 и шаги воспроизведения.

******

### Границы

******

Во избежание недоразумений следующее явно выходит за рамки этого плагина:

- Плагин нельзя использовать отдельно: у него нет значка и UI, его вызывает совместимый AutoJs6.
- Сборка на устройстве не является облачной: протокол не загружает исходный код проекта.
- AutoJs6 не хранит второе ядро упаковки в своем процессе. Если плагин отсутствует, выключен, не доверен, несовместим или дает сбой, запрос прекращается, а прежний результат сохраняется.
- Runtime Kit по-прежнему создает репозиторий AutoJs6; плагин проверяет, упаковывает, распространяет и использует его.
- Старая возможность "удаленной сборки" остается выключенной для прежних хостов. Название означало другой локальный процесс, а не Интернет-сервис, и отделено от формальной возможности.

******

### Частые Вопросы

******

**В: Как Центр плагинов выбирает сборку?**

О: Поддерживаемые версии AutoJs6 запрашивают compat-matrix.json со своим versionCode, выбирают сборку с наибольшим pluginVersionCode в совместимом диапазоне, затем предпочитают точный ABI устройства и при его отсутствии используют universal. Запись может охватывать проверенный диапазон исправлений только при явном allowPatchVersionMismatch=true: точный хост сборки упаковывает без предупреждения, другой хост внутри диапазона использует ту же сборку с предупреждением, а хост вне диапазона не может ее использовать. Если пригодной записи матрицы нет, сохраняется существующий канал Release/тег. Если сопряженная версия плагина ниже установленной, Центр плагинов предлагает сначала удалить плагин, а затем установить сопряженную сборку, поскольку Android не может выполнить понижение версии поверх приложения.

**В: Почему нужна версия плагина, соответствующая AutoJs6?**

О: Runtime шаблона должен совпадать с API хоста. Центр плагинов выбирает самую новую совместимую версию и лучший ABI; хост вне диапазона блокируется.

**В: Плагина нет на рабочем столе. Установка не удалась?**

О: Нет. У него намеренно нет значка и UI, он работает только как сервис AutoJs6. Проверьте Настройки > Приложения.

**В: Проект отправляется на удаленный сервер?**

О: Нет. Два процесса приложений общаются на одном Android-устройстве. Старое имя "удаленная сборка" означало переход Binder через границу процесса; формальный режим — `on-device-plugin`.

**В: Что будет при сбое плагина?**

О: AutoJs6 остановит запрос, покажет полезную ошибку и сохранит существующий APK; скрытого перехода на второй сборщик хоста нет.

******

### Техническая Справка

******

Разделы ниже адресованы разработчикам плагинов и интеграторам; для простого использования плагина они обычно не нужны.

#### Runtime Kit

Runtime Kit собирается основным репозиторием AutoJs6 и является единственным источником истины для шаблона автономного приложения. Этот плагин только проверяет и упаковывает этот артефакт; он не создает `template.apk`. Полный Runtime Kit обычно содержит эти файлы:

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

#### Идентификаторы Обнаружения

Хост обнаруживает и подключает этот плагин по следующим идентификаторам:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### Локальная Сборка

Сначала создайте Runtime Kit в основном репозитории AutoJs6:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

Затем соберите этот репозиторий с указанием созданного каталога Runtime Kit:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

Также можно распаковать опубликованный `autojs6-runtime-kit-*.zip` в `runtime-kit/` и собрать напрямую:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### Процесс Выпуска

Ожидаемый производственный процесс выпуска:

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

#### Подписание

Производственные выпуски плагина должны быть подписаны доверенным ключом подписи плагинов AutoJs6. Для выпусков GitHub Actions нужны эти секреты репозитория:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

Локальные сборки выпуска по-прежнему поддерживают игнорируемый корневой файл `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### Дорожная Карта

******

ROADMAP.md отслеживает проверяемым списком формальную сборку плагином, кандидаты, поставку по ABI, совместимость, доказательства безопасности и гарантии после GA.

- [Открыть ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### История Выпусков

******

# v1.0.3

###### 2026/09/15

* `Исправление` resources.arsc в создаваемых APK хранится без сжатия с выравниванием по 4 байта, чтобы приложения для Android 11 и новее устанавливались

# v1.0.2

###### 2026/09/13

* `Исправление` Дата версии плагина всегда на английском независимо от языка среды сборки
* `Улучшение` Согласованные переводы, явная активация плагина и проверка пакетов выпуска

# v1.0.1

###### 2026/09/13

* `Улучшение` Согласованные переводы, явная активация плагина и проверка пакетов выпуска

##### Дополнительная история выпусков

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-ru.md)

******

### Лицензия

******

Проект распространяется по лицензии Mozilla Public License 2.0, которая разрешает использование, изменение и распространение на ее условиях.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

******

### Структура Ресурсов

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` содержит локализованные имена, описания и резервные инструкции плагина; `plugin_instruction.md` содержит инструкции, показываемые хостом. Файлы README и CHANGELOG создаются из JSON-источников скриптом `.python/generate_markdown.py`; чтобы изменить документацию, правьте JSON и перезапускайте скрипт, а не редактируйте сгенерированные файлы.

******

### Ссылки

******

- Основной проект AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Документация AutoJs6: https://docs.autojs6.com


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/docs/16kb.md)
