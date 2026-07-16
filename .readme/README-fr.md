<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>Plugin APK de modele pour empaqueter des applications autonomes AutoJs6</p>

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

### Langues

******

Le README.md actuel prend en charge les langues suivantes:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- Français [fr] # actuel
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### Introduction

******

Le plugin AutoJs6 APK Builder Template fournit l'APK de modele externe et le Runtime Kit utilises par AutoJs6 pour empaqueter des applications autonomes. L'hote lit l'APK de modele depuis le service du plugin et verifie les metadonnees de version et de protocole pour la compatibilite.

******

### Fonctions

******

- Fournit le service de plugin `autojs6-apk-builder-template` avec l'ID de plugin `autojs6-apk-builder-template` et le moteur `apk-builder-template`.
- Expose les metadonnees de plugin communes via `org.autojs.plugin.INFO` et fournit l'APK de modele via `org.autojs.plugin.APK_BUILDER`.
- Verifie les sommes SHA-256 du Runtime Kit et les entrees requises de `template.apk` pendant la construction.
- Place `template.apk`, le magasin de cles par defaut, les metadonnees d'execution et les fichiers de contrat dans `assets/runtime-kit/`.
- Signale la version de l'hote, la version du protocole, le nom du package de modele, le resume du modele et la capacite de construction distante.
- Les metadonnees du plugin, les instructions, README et CHANGELOG couvrent l'espagnol, le francais, le russe, l'arabe, le japonais, le coreen, l'anglais, le chinois simplifie, le chinois traditionnel de Hong Kong et le chinois traditionnel de Taiwan.

******

### Runtime Kit

******

Le Runtime Kit provient du depot principal AutoJs6 et constitue la seule source fiable pour le modele d'application autonome. Ce plugin verifie et empaquette seulement cet artefact. Il ne genere pas `template.apk`. Un Runtime Kit complet contient generalement ces fichiers

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

### Construction Locale

******

Generez d'abord un Runtime Kit depuis le depot principal AutoJs6:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

Construisez ensuite ce depot avec le repertoire Runtime Kit genere:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

Vous pouvez aussi extraire un `autojs6-runtime-kit-*.zip` publie dans `runtime-kit/` et construire directement:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

******

### Flux De Publication

******

Le flux de publication de production attendu est:

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

### Signature

******

Les versions de production du plugin doivent etre signees avec la cle de signature de plugin AutoJs6 approuvee. Les publications GitHub Actions requierent ces secrets de depot:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

Les constructions locales de publication prennent toujours en charge le fichier racine ignore `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### Historique Des Versions

******

# v6.8.0 Alpha5

###### 2026/07/16

* `Ajout` Ajout du service de plugin APK Builder Template avec l'ID `autojs6-apk-builder-template`, le moteur `apk-builder-template` et la variante `inrt-universal`
* `Ajout` Exposition des metadonnees du plugin via `org.autojs.plugin.INFO` et fourniture de l'APK de modele via `org.autojs.plugin.APK_BUILDER`
* `Ajout` Empaquetage du Runtime Kit AutoJs6 dans `assets/runtime-kit/`, avec `template.apk`, le magasin de cles par defaut, les metadonnees et les fichiers de contrat
* `Ajout` Ajout de la validation de construction pour les metadonnees du Runtime Kit, les sommes SHA-256 et les entrees requises de `template.apk`
* `Ajout` Signalement de la version de l'hote, de la version du protocole, du nom du package de modele, du SHA-256 du modele, des resumes Runtime API et de la capacite de construction distante
* `Ajout` Ajout de la prise en charge optionnelle du protocole experimental de construction distante via `autojs.apkBuilder.templatePlugin.enableRemoteBuild`
* `Ajout` Ajout du flux de publication pour telecharger le Runtime Kit, valider les assets, signer avec la cle approuvee et televerser l'APK universel
* `Ajout` Ajout de ressources localisees pour les metadonnees du plugin, les instructions, README et CHANGELOG en espagnol, francais, russe, arabe, japonais, coreen, anglais, chinois simplifie, chinois traditionnel de Hong Kong et chinois traditionnel de Taiwan

##### Pour plus d'historique

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-fr.md)

******

### Structure Des Ressources

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` contient les noms, descriptions et instructions de secours localises du plugin; `plugin_instruction.md` contient les instructions affichees par l'hote. Les fichiers README et CHANGELOG sont generes depuis les sources JSON par `.python/generate_markdown.py`.

******

### Liens

******

- Projet principal AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Documentation AutoJs6: https://docs.autojs6.com
