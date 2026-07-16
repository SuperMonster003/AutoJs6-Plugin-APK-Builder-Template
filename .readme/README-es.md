<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>Plugin de APK de plantilla para empaquetar aplicaciones independientes de AutoJs6</p>

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

### Idiomas

******

El README.md actual admite los siguientes idiomas:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- Español [es] # actual
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### Introduccion

******

El plugin AutoJs6 APK Builder Template proporciona el APK de plantilla externo y el Runtime Kit que AutoJs6 usa al empaquetar aplicaciones independientes. El host lee el APK de plantilla desde el servicio del plugin y comprueba los metadatos de version y protocolo para confirmar la compatibilidad.

******

### Funciones

******

- Proporciona el servicio de plugin `autojs6-apk-builder-template` con ID de plugin `autojs6-apk-builder-template` y motor `apk-builder-template`.
- Expone metadatos comunes del plugin mediante `org.autojs.plugin.INFO` y entrega el APK de plantilla mediante `org.autojs.plugin.APK_BUILDER`.
- Valida los resumenes SHA-256 del Runtime Kit y las entradas requeridas de `template.apk` durante la compilacion.
- Empaqueta `template.apk`, el almacen de claves predeterminado, metadatos de runtime y archivos de contrato bajo `assets/runtime-kit/`.
- Informa la version del host, la version del protocolo, el nombre del paquete de plantilla, el resumen de plantilla y la capacidad de compilacion remota.
- Los metadatos del plugin, las instrucciones, README y CHANGELOG cubren espanol, frances, ruso, arabe, japones, coreano, ingles, chino simplificado, chino tradicional de Hong Kong y chino tradicional de Taiwan.

******

### Runtime Kit

******

El Runtime Kit proviene del repositorio principal de AutoJs6 y es la unica fuente fiable para la plantilla de aplicacion independiente. Este plugin solo verifica y empaqueta ese artefacto. No genera `template.apk`. Un Runtime Kit completo suele contener estos archivos

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

### Compilacion Local

******

Primero genera un Runtime Kit desde el repositorio principal de AutoJs6:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

Luego compila este repositorio con el directorio Runtime Kit generado:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

Tambien puedes extraer un `autojs6-runtime-kit-*.zip` publicado en `runtime-kit/` y compilar directamente:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

******

### Flujo De Publicacion

******

El flujo de publicacion de produccion esperado es:

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

### Firma

******

Las versiones de produccion del plugin deben firmarse con la clave de firma de plugin AutoJs6 confiable. Las publicaciones de GitHub Actions requieren estos secretos del repositorio:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

Las compilaciones locales de publicacion siguen admitiendo el archivo raiz ignorado `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### Historial De Versiones

******

# v6.8.0 Alpha5

###### 2026/07/16

* `Nuevo` Se agrego el servicio de plugin APK Builder Template con ID `autojs6-apk-builder-template`, motor `apk-builder-template` y variante `inrt-universal`
* `Nuevo` Se expusieron metadatos del plugin mediante `org.autojs.plugin.INFO` y se entrego el APK de plantilla mediante `org.autojs.plugin.APK_BUILDER`
* `Nuevo` Se empaqueto el Runtime Kit de AutoJs6 bajo `assets/runtime-kit/`, incluidos `template.apk`, el almacen de claves predeterminado, metadatos y archivos de contrato
* `Nuevo` Se agrego validacion en tiempo de compilacion para metadatos del Runtime Kit, resumenes SHA-256 y entradas requeridas de `template.apk`
* `Nuevo` Se informo la version del host, version del protocolo, nombre del paquete de plantilla, SHA-256 de la plantilla, resumenes Runtime API y capacidad de compilacion remota
* `Nuevo` Se agrego soporte opcional para el protocolo experimental de compilacion remota mediante `autojs.apkBuilder.templatePlugin.enableRemoteBuild`
* `Nuevo` Se agrego soporte de flujo de publicacion para descargar el Runtime Kit, validar assets, firmar con la clave confiable y subir el APK universal
* `Nuevo` Se agregaron recursos localizados de metadatos del plugin, instrucciones, README y CHANGELOG para espanol, frances, ruso, arabe, japones, coreano, ingles, chino simplificado, chino tradicional de Hong Kong y chino tradicional de Taiwan

##### Para mas historial

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-es.md)

******

### Estructura De Recursos

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` contiene nombres, descripciones e instrucciones de reserva localizados del plugin; `plugin_instruction.md` contiene instrucciones mostradas por el host. Los archivos README y CHANGELOG se generan desde fuentes JSON mediante `.python/generate_markdown.py`.

******

### Enlaces

******

- Proyecto principal AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Documentacion de AutoJs6: https://docs.autojs6.com
