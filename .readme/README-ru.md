<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>Плагин шаблонного APK для упаковки автономных приложений AutoJs6</p>

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

Плагин AutoJs6 APK Builder Template предоставляет внешний шаблонный APK и Runtime Kit, которые AutoJs6 использует при упаковке автономных приложений. Хост читает шаблонный APK из сервиса плагина и проверяет совместимость по метаданным версии и протокола.

******

### Возможности

******

- Предоставляет сервис плагина `autojs6-apk-builder-template` с ID плагина `autojs6-apk-builder-template` и движком `apk-builder-template`.
- Открывает общие метаданные плагина через `org.autojs.plugin.INFO` и выдает шаблонный APK через `org.autojs.plugin.APK_BUILDER`.
- Проверяет SHA-256 дайджесты Runtime Kit и обязательные записи `template.apk` во время сборки.
- Упаковывает `template.apk`, хранилище ключей по умолчанию, метаданные среды выполнения и контрактные файлы в `assets/runtime-kit/`.
- Сообщает версию хоста, версию протокола, имя пакета шаблона, дайджест шаблона и метаданные возможности удаленной сборки.
- Метаданные плагина, инструкции, README и CHANGELOG охватывают испанский, французский, русский, арабский, японский, корейский, английский, упрощенный китайский, традиционный китайский Гонконга и традиционный китайский Тайваня.

******

### Runtime Kit

******

Runtime Kit поступает из основного репозитория AutoJs6 и является единственным источником истины для шаблона автономного приложения. Этот плагин только проверяет и упаковывает этот артефакт. Он не создает `template.apk`. Полный Runtime Kit обычно содержит эти файлы

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

### Локальная Сборка

******

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

******

### Процесс Выпуска

******

Ожидаемый производственный процесс выпуска:

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

### Подписание

******

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

### История Выпусков

******

# v6.8.0 Alpha5

###### 2026/07/16

* `Добавлено` Добавлен сервис плагина APK Builder Template с ID `autojs6-apk-builder-template`, движком `apk-builder-template` и вариантом `inrt-universal`
* `Добавлено` Метаданные плагина открыты через `org.autojs.plugin.INFO`, а шаблонный APK выдается через `org.autojs.plugin.APK_BUILDER`
* `Добавлено` Runtime Kit AutoJs6 упакован в `assets/runtime-kit/`, включая `template.apk`, хранилище ключей по умолчанию, метаданные и контрактные файлы
* `Добавлено` Добавлена проверка метаданных Runtime Kit, SHA-256 дайджестов и обязательных записей `template.apk` во время сборки
* `Добавлено` Добавлена передача версии хоста, версии протокола, имени пакета шаблона, SHA-256 шаблона, дайджестов Runtime API и возможности удаленной сборки
* `Добавлено` Добавлена необязательная поддержка экспериментального протокола удаленной сборки через `autojs.apkBuilder.templatePlugin.enableRemoteBuild`
* `Добавлено` Добавлена поддержка процесса выпуска для загрузки Runtime Kit, проверки assets, подписи доверенным ключом и загрузки universal APK
* `Добавлено` Добавлены локализованные ресурсы метаданных плагина, инструкций, README и CHANGELOG для испанского, французского, русского, арабского, японского, корейского, английского, упрощенного китайского, традиционного китайского Гонконга и традиционного китайского Тайваня

##### Дополнительная история выпусков

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-ru.md)

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

`strings.xml` содержит локализованные имена, описания и резервные инструкции плагина; `plugin_instruction.md` содержит инструкции, показываемые хостом. Файлы README и CHANGELOG создаются из JSON-источников скриптом `.python/generate_markdown.py`.

******

### Ссылки

******

- Основной проект AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Документация AutoJs6: https://docs.autojs6.com
