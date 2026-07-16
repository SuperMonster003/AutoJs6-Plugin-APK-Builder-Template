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
