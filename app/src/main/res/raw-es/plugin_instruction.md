APK Builder Template proporciona el Runtime Kit que AutoJs6 usa al empaquetar aplicaciones independientes.

Instálalo preferiblemente desde un Centro de plugins de AutoJs6 reciente: lee `compat-matrix.json`, elige la compilación emparejada con este host, prioriza el ABI exacto del dispositivo y recurre a universal. Para una instalación manual, usa la etiqueta Release correspondiente o el sufijo autojs6- de la versión del plugin; para volver desde una compilación más nueva, desinstálala primero, porque Android no puede sobrescribir la aplicación con una versión anterior. El host descubre el plugin mediante `org.autojs.plugin.APK_BUILDER` y lee `assets/runtime-kit/template.apk`.

Un Runtime Kit puede cubrir explícitamente un intervalo de parches verificado. Su host exacto de compilación empaqueta sin aviso; otro host dentro del intervalo continúa con una advertencia, mientras que uno fuera del intervalo queda bloqueado antes de transferir la plantilla.

El Runtime Kit empaquetado incluye:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

El empaquetado se ejecuta por completo dentro del proceso del plugin en el mismo dispositivo Android; el código del proyecto no se sube. AutoJs6 controla la confianza y la compatibilidad y valida de forma independiente el APK devuelto. El interruptor heredado `supportsRemoteBuild` sigue desactivado, pero no desactiva esta ruta formal.
