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

La función "Empaquetar aplicación" de AutoJs6 convierte un script o proyecto en un APK que se instala y ejecuta por sí solo, sin AutoJs6 en el dispositivo de destino. El empaquetado necesita un "APK de plantilla" como esqueleto: una aplicación que ya contiene el entorno de ejecución de scripts completo. Para mantener ligera la aplicación principal, las versiones recientes de AutoJs6 ya no incluyen esta plantilla voluminosa; ahora vive en este plugin y solo la instalan los usuarios que necesitan empaquetar.

El plugin no tiene icono ni interfaz. Todo ocurre en segundo plano: al empaquetar, AutoJs6 descubre el plugin, comprueba la compatibilidad de versiones y la integridad de los archivos, y luego lee el APK de plantilla integrado para terminar el trabajo.

Guía en una línea: si usas "Empaquetar aplicación", instala la compilación que el Centro de plugins haya seleccionado para tu AutoJs6 según la matriz de compatibilidad; si nunca empaquetas aplicaciones independientes, no lo necesitas.

******

### Cómo Funciona

******

Al empaquetar una aplicación independiente, AutoJs6 y este plugin cooperan de la siguiente manera:

1. Descubrimiento: AutoJs6 localiza el plugin de plantilla instalado y lee sus metadatos
2. Comprobación de compatibilidad: se comparan las versiones, las versiones de protocolo y el nombre del paquete de plantilla; una discrepancia produce una advertencia o bloquea el empaquetado
3. Comprobación de integridad: se verifica el resumen SHA-256 del APK de plantilla para descartar archivos dañados o manipulados
4. Transferencia de la plantilla: el APK de plantilla se transmite por una tubería entre procesos, sin copias temporales
5. Empaquetado: AutoJs6 escribe el script, la configuración y los recursos en la plantilla y produce el APK independiente final

******

### Funciones

******

- Proporciona la plantilla completa de aplicación independiente (Runtime Kit) para la función "Empaquetar aplicación" de AutoJs6; no requiere configuración tras la instalación.
- Cada compilación del plugin conserva en su nombre el host de AutoJs6 con el que se creó (por ejemplo, 1.0.0+autojs6-6.8.0-alpha5) y puede declarar explícitamente un intervalo cerrado de parches verificado; la coincidencia exacta no avisa, otro host dentro del intervalo muestra una advertencia y cualquier host fuera de él queda bloqueado.
- Doble protección de integridad: los resúmenes SHA-256 y las entradas requeridas de la plantilla se validan al construir el plugin, y el resumen de la plantilla se informa a AutoJs6 para su re-verificación al empaquetar.
- La plantilla se transmite a AutoJs6 por una tubería entre procesos, sin copias temporales redundantes.
- Incluye un almacén de claves predeterminado, de modo que puede producirse un APK instalable incluso sin configurar una clave de firma personalizada.
- Admite un protocolo experimental de "compilación remota" en el que el proceso del plugin realiza por sí mismo una compilación ligera (desactivado por defecto; ver Límites).
- Los metadatos del plugin, las instrucciones, el README y el CHANGELOG cubren 10 idiomas: chino simplificado, chino tradicional (Hong Kong/Taiwán), inglés, francés, español, japonés, coreano, ruso y árabe.

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

- El plugin no puede usarse por sí solo: no tiene icono ni interfaz, y solo AutoJs6 lo invoca durante el empaquetado.
- El plugin no genera el APK de plantilla: la plantilla y el Runtime Kit los construye y publica el repositorio principal de AutoJs6; este plugin solo los verifica, empaqueta y distribuye.
- El plugin no participa en la escritura ni en la ejecución diaria de scripts: solo la función "Empaquetar aplicación" lo lee.
- La compilación remota es experimental y está desactivada por defecto: los plugins publicados oficialmente no la activan; solo está disponible en plugins autocompilados con la función activada explícitamente.
- El plugin no relaja los requisitos de versión: empaquetar con una versión discrepante de AutoJs6 puede bloquearse, y aunque funcione, el resultado no está garantizado.

******

### Preguntas Frecuentes

******

**P: ¿Cómo elige una compilación el Centro de plugins?**

R: Las versiones compatibles de AutoJs6 consultan compat-matrix.json con su propio versionCode, seleccionan la compilación con el pluginVersionCode más alto dentro del intervalo compatible y después prefieren el ABI exacto del dispositivo, con respaldo universal. Una entrada solo puede cubrir un intervalo de parches verificado cuando declara explícitamente allowPatchVersionMismatch=true: el host exacto de compilación empaqueta sin aviso, otro host dentro del intervalo reutiliza la misma compilación con una advertencia y un host fuera del intervalo no puede usarla. Si no hay una entrada utilizable en la matriz, se conserva el canal Release/etiqueta existente. Si la versión emparejada del plugin es inferior a la instalada, el Centro de plugins pide desinstalar primero y después instalar la compilación emparejada, porque Android no puede realizar una actualización inversa sobre la aplicación.

**P: ¿Por qué el plugin debe emparejarse con mi versión de AutoJs6?**

R: El entorno de ejecución dentro del APK de plantilla se corresponde estrictamente con la API de ejecución de AutoJs6, así que la compatibilidad se determina mediante el contrato versionCode declarado y validado por el plugin, no por su propia versión semántica. La mayoría de las compilaciones se dirigen a un único host exacto; una compilación también puede declarar explícitamente un intervalo cerrado y verificado de versiones de parche. El host de referencia pasa entonces sin aviso, los demás hosts dentro del intervalo reciben una advertencia y los que quedan fuera se bloquean. La versión propia del plugin (como 1.0.0) evoluciona de forma independiente, mientras que el sufijo autojs6- del nombre de versión y la etiqueta de la release indican el host emparejado; en un AutoJs6 antiguo, descarga la compilación del plugin bajo la etiqueta antigua correspondiente (para volver desde un plugin más nuevo, desinstálalo primero; Android no permite instalar una versión anterior sobre otra más reciente).

**P: No encuentro el plugin en mi lanzador. ¿Falló la instalación?**

R: No. El plugin no tiene icono ni interfaz y solo funciona como servicio en segundo plano para AutoJs6. Puedes confirmarlo en la lista "Ajustes > Aplicaciones" del sistema como APK Builder Template.

**P: El empaquetado informa de un fallo de verificación de la plantilla. ¿Qué hago?**

R: Normalmente significa que el plugin instalado está incompleto o dañado. Reinstálalo desde el Centro de plugins de AutoJs6 o desde las Releases de este repositorio; si el problema persiste, abre un issue.

**P: ¿Por qué el plugin es tan grande?**

R: Incluye una plantilla completa de aplicación independiente, con el motor de scripts y las bibliotecas nativas de todas las arquitecturas. Precisamente por eso la plantilla se separó de la aplicación principal: solo los usuarios que empaquetan cargan con este peso.

**P: ¿Qué es la "compilación remota"?**

R: Un protocolo experimental que permite al plugin realizar una compilación ligera en su propio proceso (desempaquetar la plantilla, escribir el script y la configuración, reescribir el nombre del paquete y los recursos, volver a firmar). Los plugins publicados oficialmente la mantienen desactivada; por ahora se dirige a desarrolladores que compilan el plugin por su cuenta.

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

El trabajo planificado y su progreso se registran como una lista verificable en ROADMAP.md: estabilización de la compilación remota, variantes de plantilla por arquitectura, compatibilidad a nivel de parche, etc. La discusión está abierta en Issues.

- [Ver ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### Historial De Versiones

******

# v1.0.0

###### 2026/09/01

* `Nota` Primera versión formal de la línea independiente del plugin, emparejada exactamente con el Runtime Kit de AutoJs6 v6.8.0 (versionCode 5277); la versión compuesta del plugin es 1.0.0+autojs6-6.8.0 (versionCode 527701), el Centro de plugins selecciona la compilación ABI correspondiente mediante compat-matrix.json y las compilaciones remotas siguen desactivadas de forma predeterminada
* `Nuevo` Introducidos SemVer 1.0.0 para el plugin, numeración de compilación independiente, nombres de versión compuestos y valores Android versionCode monotónicos que permiten varias versiones del plugin para el mismo host
* `Nuevo` Añadidas variantes universal, arm64-v8a, armeabi-v7a, x86_64 y x86 con selección ABI exacta y respaldo universal
* `Nuevo` Añadidos un contrato de intervalo de compatibilidad del host con cierre seguro y una matriz de compatibilidad autoritativa para que un intervalo de parches adyacentes validado explícitamente pueda compartir una compilación del plugin
* `Corrección` Se alineó la numeración de compilaciones remotas experimentales de un solo archivo con el compilador heredado y se añadió una comprobación previa y cerrada ante fallos del espacio de trabajo, basada en tamaños expandidos verificados, un límite de expansión de plantilla validado al compilar y una reserva de 256 MiB
* `Corrección` Se rechazan los metadatos y las directivas de código heredados para empaquetar Node.js integrado antes de BUILD/SIGN, con orientación para migrar al complemento Runtime externo, y se elimina la inyección obsoleta del servicio Manifest y los permisos de primer plano
* `Corrección` Se corrigió una carrera entre el cierre y el hilo de compilación en las sesiones remotas experimentales que podía volver a crear un espacio de trabajo eliminado tras cancelar o cerrar; la limpieza ahora espera al worker y no deja archivos residuales
* `Corrección` Reforzadas las compilaciones remotas experimentales: se rechaza el contenido cifrado temporal de TypeScript no declarado en el inventario de rutas y se detectan correctamente los almacenes de claves BKS personalizados tras normalizar el nombre de archivo en el espacio de trabajo
* `Corrección` Se restringieron los límites de entrada de la compilación remota experimental con validación estricta de tipos, tamaños y anidación de Parcelable/Bundle y project.json, límites para almacenes de claves, iconos y rutas ZIP, y correcciones de desbordamiento en el paquete ARSC y los nombres de salida derivados
* `Corrección` El complemento no se podía activar desde el centro de complementos después de instalarlo en algunos sistemas
* `Mejora` El flujo de publicación de confianza admite ahora un modo de candidato aislado que genera cinco APK con firma de producción y evidence desde un artefacto Actions del host fijado, sin crear una Release ni actualizar la matriz de compatibilidad autoritativa
* `Mejora` Unificadas las reglas de validación de Runtime Kit entre Gradle y Python, incluidos resúmenes, tamaños, archivos obligatorios, entradas APK y coherencia de las cinco variantes
* `Mejora` Publicación junto a los cinco APK de un manifiesto de evidencias JSON legible por máquina que vincula hashes de artefactos, certificado de firma, versiones del plugin/host, intervalo de compatibilidad, ID de Runtime Kit y versiones de protocolo
* `Mejora` Actualizadas las instrucciones de instalación, las preguntas frecuentes, el ensayo de publicación y la documentación en 10 idiomas para versiones compatibles, selección ABI, recuperación tras una degradación y versionado independiente
* `Mejora` Unificar el diseño del README y la gestión de versiones de la plataforma Gradle

# v6.8.0 Alpha5

###### 2026/07/16

* `Nota` Se empareja con AutoJs6 v6.8.0 Alpha5; las versiones compatibles del Centro de plugins resuelven automáticamente la compilación emparejada, mientras que una instalación manual usa la etiqueta Release o el sufijo autojs6- correspondiente; el plugin no tiene icono ni interfaz y se invoca automáticamente al empaquetar aplicaciones
* `Nuevo` Permitido que AutoJs6 descubra el plugin y lea automáticamente su plantilla integrada, de modo que "Empaquetar aplicación" ya no depende de un APK de plantilla incluido en la aplicación principal
* `Nuevo` Incluido el Runtime Kit completo: APK de plantilla, almacén de claves predeterminado, metadatos de ejecución y archivos de contrato
* `Nuevo` Añadidas comprobaciones automáticas de compatibilidad de versión y protocolo antes de empaquetar, con advertencia o bloqueo ante discrepancias para evitar aplicaciones defectuosas
* `Nuevo` Validados los resúmenes SHA-256 del Runtime Kit y las entradas requeridas de la plantilla al construir el plugin, e informado el resumen de la plantilla a AutoJs6 para re-verificación en ejecución
* `Nuevo` Añadido un protocolo experimental de compilación remota que realiza una compilación ligera dentro del proceso del plugin (desactivado por defecto, debe activarse explícitamente al compilar)
* `Nuevo` Conectado el flujo de publicación automatizado: cuando el repositorio principal de AutoJs6 publica una versión, se construye un APK de plugin emparejado, se firma con la clave de confianza, se verifica la huella del certificado y se publica
* `Nuevo` Cubiertos 10 idiomas en los metadatos del plugin, las instrucciones, el README y el CHANGELOG: chino simplificado, chino tradicional (Hong Kong/Taiwán), inglés, francés, español, japonés, coreano, ruso y árabe

# v6.7.1 Alpha4

###### 2026/07/09

* `Nota` Primera versión pública; se empareja con AutoJs6 de la misma versión (v6.7.1 Alpha4)
* `Nuevo` Separado del repositorio principal de AutoJs6 como repositorio de plugin independiente, con la implementación inicial del servicio de plugin de APK de plantilla
* `Nuevo` Establecida la canalización basada en el Runtime Kit, activada por el repositorio principal de AutoJs6, que obtiene, verifica, construye y publica el plugin

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
