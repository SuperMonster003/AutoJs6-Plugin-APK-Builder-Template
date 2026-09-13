<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>Plugin de plantilla que impulsa la función "Empaquetar aplicación" de AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
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

### Introducción

******

La función "Empaquetar aplicación" de AutoJs6 convierte un script o proyecto en un APK instalable y autónomo. Para mantener ligera la aplicación principal, la plantilla voluminosa y todo el núcleo de empaquetado viven en este plugin.

El plugin no tiene icono ni interfaz. AutoJs6 lo descubre y valida, prepara una solicitud acotada y muestra el progreso. El plugin desempaqueta su propia plantilla, escribe el proyecto y los recursos, modifica Manifest/resources, selecciona ABI, gestiona la firma y devuelve un APK candidato. AutoJs6 verifica de forma independiente ese resultado antes de publicarlo.

Todo se ejecuta en el mismo dispositivo Android mediante Binder y descriptores de archivo. El código fuente del proyecto no se sube a una red ni a un servicio de compilación en la nube.

******

### Cómo Funciona

******

Al empaquetar una aplicación, AutoJs6 y el plugin cooperan así:

1. Admisión: AutoJs6 verifica firma oficial, estado habilitado, intervalo del host, ABI, capacidad formal, protocolo y modo de ejecución en el dispositivo
2. Preparación: AutoJs6 crea entradas acotadas de proyecto/bibliotecas/keystore y fija la identidad esperada del paquete y firmante
3. Compilación del plugin: valida la solicitud, abre su plantilla Runtime Kit, escribe el proyecto, modifica Manifest/resources, recorta ABI y firma
4. Resultado: devuelve el APK candidato mediante un descriptor de solo lectura y limpia su espacio privado
5. Publicación: AutoJs6 vuelve a comprobar tamaño, SHA-256, estructura, firma, firmante, paquete y versión; solo entonces reemplaza atómicamente el destino

******

### Funciones

******

- Posee todo el núcleo de empaquetado en el dispositivo: plantilla, proyecto/recursos, Manifest y resources.arsc, ABI, keystores y firma.
- Mantiene AutoJs6 ligero: el host aporta UI, admisión de confianza/compatibilidad, preparación, cancelación/progreso y validación independiente, no un segundo compilador.
- Se ejecuta por completo en el mismo dispositivo mediante Binder/AIDL y ParcelFileDescriptor; no sube el proyecto a Internet ni a la nube.
- Empareja cada compilación del plugin con un Runtime Kit AutoJs6 validado y admite intervalos cerrados de parches verificados.
- Ofrece variantes universal, arm64-v8a, armeabi-v7a, x86_64 y x86 con selección ABI exacta y reserva universal.
- Incluye keystore predeterminado y creación/verificación BKS/JKS propiedad del plugin, además de keystores personalizados.
- Metadatos, instrucciones, README y CHANGELOG cubren 10 idiomas.

******

### Inicio Rápido

******

- **Cómo instalar**: Instálalo preferiblemente desde el Centro de plugins de AutoJs6: las compilaciones de host compatibles leen la matriz de compatibilidad y seleccionan automáticamente tanto la versión emparejada del plugin como el artefacto ABI exacto del dispositivo, con respaldo universal. Para una instalación manual, descarga el APK desde [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) e identifica el host emparejado mediante la etiqueta con el nombre de AutoJs6 o el sufijo autojs6- de la versión del plugin (p. ej. el plugin v1.0.0+autojs6-6.8.0-alpha5 se empareja con AutoJs6 v6.8.0 Alpha5). Si el Centro de plugins selecciona una versión inferior a la instalada, sigue su guía para desinstalar y volver a instalar, ya que Android no permite sobrescribir una aplicación con una versión anterior.
- **Cómo usar**: Sin pasos adicionales. Usa la función "Empaquetar aplicación" en AutoJs6 como siempre; el flujo de empaquetado descubre el plugin y usa automáticamente su plantilla integrada.
- **Cómo confirmar que funciona**: Sin el plugin (o con una versión discrepante), la entrada de empaquetado de AutoJs6 te pide instalarlo o activarlo; una vez instalada la versión correcta, el aviso desaparece, señal de que el plugin fue reconocido. El plugin no tiene icono ni interfaz, así que es normal no encontrarlo en el lanzador.
- **Dónde mirar si algo falla**: Ante una advertencia de compatibilidad, usa la compilación seleccionada por el Centro de plugins según la matriz de compatibilidad o comprueba que el host actual está dentro del intervalo declarado por el plugin; si una incompatibilidad bloquea el empaquetado, instala la compilación correspondiente de la matriz; ante un error de verificación o plantilla dañada, reinstala el plugin desde una fuente oficial; para lo demás, abre un [issue](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) con los registros de AutoJs6 y los pasos de reproducción.

******

### Límites

******

Para evitar malentendidos, lo siguiente queda explícitamente fuera del alcance de este plugin:

- El plugin no funciona por sí solo: no tiene icono ni interfaz y lo invoca un AutoJs6 compatible.
- La compilación en el dispositivo no es una compilación en la nube: este protocolo no sube el código del proyecto.
- AutoJs6 no conserva un segundo núcleo de empaquetado en su proceso. Si falta el plugin, está deshabilitado, no es fiable, es incompatible o falla, la solicitud se detiene y se conserva el resultado anterior.
- El repositorio AutoJs6 sigue generando el Runtime Kit; el plugin lo verifica, empaqueta, distribuye y usa, pero no crea por sí solo una plantilla de runtime.
- La antigua capacidad de "compilación remota" permanece deshabilitada para hosts heredados. El nombre significaba otro proceso local, no un servicio de Internet, y está separado de la capacidad formal.

******

### Preguntas Frecuentes

******

**P: ¿Cómo elige una compilación el Centro de plugins?**

R: Las versiones compatibles de AutoJs6 consultan compat-matrix.json con su propio versionCode, seleccionan la compilación con el pluginVersionCode más alto dentro del intervalo compatible y después prefieren el ABI exacto del dispositivo, con respaldo universal. Una entrada solo puede cubrir un intervalo de parches verificado cuando declara explícitamente allowPatchVersionMismatch=true: el host exacto de compilación empaqueta sin aviso, otro host dentro del intervalo reutiliza la misma compilación con una advertencia y un host fuera del intervalo no puede usarla. Si no hay una entrada utilizable en la matriz, se conserva el canal Release/etiqueta existente. Si la versión emparejada del plugin es inferior a la instalada, el Centro de plugins pide desinstalar primero y después instalar la compilación emparejada, porque Android no puede realizar una actualización inversa sobre la aplicación.

**P: ¿Por qué el plugin debe emparejarse con AutoJs6?**

R: El runtime de la plantilla debe coincidir con la API del host. El Centro de plugins elige la versión compatible más alta y el mejor ABI; los hosts fuera del intervalo se bloquean.

**P: No veo el plugin en el lanzador. ¿Falló la instalación?**

R: No. No tiene icono ni interfaz y solo se ejecuta como servicio de AutoJs6. Compruébalo en Ajustes > Aplicaciones.

**P: ¿Mi proyecto se envía a un servidor remoto?**

R: No. Host y plugin se comunican entre dos procesos del mismo dispositivo Android. Los nombres históricos dicen "compilación remota" porque Binder cruza un límite de proceso; el modo formal es `on-device-plugin`.

**P: ¿Qué ocurre si falla el plugin?**

R: AutoJs6 detiene la solicitud, muestra un error útil y conserva cualquier APK anterior; no cambia silenciosamente a un segundo compilador del host.

******

### Referencia Técnica

******

Las secciones siguientes se dirigen a desarrolladores e integradores; normalmente no son necesarias para simplemente usar el plugin.

#### Runtime Kit

El Runtime Kit lo construye el repositorio principal de AutoJs6 y es la única fuente de verdad para la plantilla de aplicación independiente. Este plugin solo verifica y empaqueta ese artefacto; no genera `template.apk`. Un Runtime Kit completo suele contener estos archivos:

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

#### Identificadores De Descubrimiento

El host descubre y vincula este plugin mediante los siguientes identificadores:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### Compilación Local

Primero genera un Runtime Kit desde el repositorio principal de AutoJs6:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

Luego compila este repositorio con el directorio Runtime Kit generado:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

También puedes extraer un `autojs6-runtime-kit-*.zip` publicado en `runtime-kit/` y compilar directamente:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### Flujo De Publicación

El flujo de publicación de producción esperado es:

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

#### Firma

Las versiones de producción del plugin deben firmarse con la clave de firma de plugins AutoJs6 de confianza. Las publicaciones de GitHub Actions requieren estos secretos del repositorio:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

Las compilaciones locales de publicación siguen admitiendo el archivo raíz ignorado `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### Hoja De Ruta

******

ROADMAP.md sigue como lista verificable la compilación formal gestionada por el plugin, los candidatos, la entrega por ABI, la compatibilidad, las pruebas de seguridad y las garantías posteriores a GA.

- [Ver ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### Historial De Versiones

******

# v1.0.2

###### 2026/09/13

* `Corrección` Mantener la fecha de versión del complemento en inglés sin depender del idioma del equipo de compilación
* `Mejora` Recursos traducidos coherentes, activación explícita del complemento y validación de los paquetes de publicación

# v1.0.1

###### 2026/09/13

* `Mejora` Recursos traducidos coherentes, activación explícita del complemento y validación de los paquetes de publicación

# v6.8.0

###### 2026/09/11

* `Mejora` Verificación de compilación de la alineación de páginas de 16 KB en bibliotecas nativas de 64 bits, con controles del contrato manifest e informes JSON

##### Para más historial

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-es.md)

******

### Licencia

******

Este proyecto se publica bajo la Mozilla Public License 2.0, que permite su uso, modificación y distribución según sus términos.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

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

`strings.xml` contiene nombres, descripciones e instrucciones de reserva localizados del plugin; `plugin_instruction.md` contiene las instrucciones mostradas por el host. Los archivos README y CHANGELOG se generan desde fuentes JSON mediante `.python/generate_markdown.py`; para cambiar la documentación, edita los JSON y vuelve a ejecutar el script en lugar de editar los archivos generados.

******

### Enlaces

******

- Proyecto principal AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Documentación de AutoJs6: https://docs.autojs6.com


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/docs/16kb.md)
