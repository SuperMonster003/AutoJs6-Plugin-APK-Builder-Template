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
