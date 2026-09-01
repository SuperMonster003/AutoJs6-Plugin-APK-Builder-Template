<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>Plugin de modèle pour la fonction "Empaqueter l'application" d'AutoJs6</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
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

La fonction "Empaqueter l'application" d'AutoJs6 transforme un script ou un projet en APK autonome. Pour alléger l'application principale, le modèle volumineux et tout le cœur d'empaquetage résident dans ce plugin.

Le plugin n'a ni icône ni interface. AutoJs6 le découvre et le valide, prépare une requête bornée et affiche la progression. Le plugin décompresse son propre modèle, écrit le projet et les ressources, modifie Manifest/resources, choisit les ABI, gère la signature et renvoie un APK candidat. AutoJs6 vérifie indépendamment ce résultat avant de le publier.

Tout s'exécute sur le même appareil Android via Binder et des descripteurs de fichier. Le code source du projet n'est envoyé ni sur le réseau ni vers un service de build cloud.

******

### Fonctionnement

******

Lors de l'empaquetage, AutoJs6 et le plugin coopèrent ainsi :

1. Admission : AutoJs6 vérifie signature officielle, activation, intervalle d'hôte, ABI, capacité formelle, protocole et exécution sur l'appareil
2. Préparation : AutoJs6 crée des entrées bornées projet/bibliothèques/keystore et fixe l'identité attendue du package et du signataire
3. Build du plugin : il valide la requête, ouvre son modèle Runtime Kit, écrit le projet, modifie Manifest/resources, élague les ABI et signe
4. Résultat : il renvoie l'APK candidat par descripteur en lecture seule et nettoie son espace privé
5. Publication : AutoJs6 revérifie taille, SHA-256, structure, signature, signataire, package et version, puis remplace atomiquement la cible

******

### Fonctions

******

- Possède tout le cœur d'empaquetage sur l'appareil : modèle, projet/ressources, Manifest et resources.arsc, ABI, keystores et signature.
- Maintient AutoJs6 léger : l'hôte fournit UI, admission de confiance/compatibilité, préparation, annulation/progression et validation indépendante, pas un second builder.
- S'exécute entièrement sur le même appareil via Binder/AIDL et ParcelFileDescriptor ; aucun projet n'est envoyé sur Internet ou dans le cloud.
- Associe chaque build du plugin à un Runtime Kit AutoJs6 validé et prend en charge des intervalles fermés de correctifs vérifiés.
- Fournit les variantes universal, arm64-v8a, armeabi-v7a, x86_64 et x86 avec sélection ABI précise et repli universal.
- Inclut un keystore par défaut et la création/vérification BKS/JKS gérée par le plugin, tout en acceptant les keystores personnalisés.
- Métadonnées, instructions, README et CHANGELOG couvrent 10 langues.

******

### Démarrage Rapide

******

- **Comment installer**: Installez de préférence depuis le Centre de plugins AutoJs6 : les versions hôtes compatibles lisent la matrice de compatibilité et sélectionnent automatiquement la version appariée du plugin ainsi que l'artefact ABI exact de l'appareil, avec repli sur universal. Pour une installation manuelle, téléchargez l'APK depuis [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) et repérez l'hôte apparié grâce au tag portant le nom d'AutoJs6 ou au suffixe autojs6- de la version du plugin (par ex. le plugin v1.0.0+autojs6-6.8.0-alpha5 va avec AutoJs6 v6.8.0 Alpha5). Si le Centre de plugins sélectionne une version inférieure à celle installée, suivez son guide de désinstallation puis réinstallation, car Android ne permet pas une installation en rétrogradation par-dessus l'application.
- **Comment utiliser**: Aucune étape supplémentaire. Utilisez la fonction "Empaqueter l'application" dans AutoJs6 comme d'habitude ; le processus d'empaquetage découvre le plugin et utilise automatiquement son modèle intégré.
- **Comment vérifier que ça marche**: Sans le plugin (ou avec une version divergente), l'entrée d'empaquetage d'AutoJs6 vous invite à l'installer ou à l'activer ; une fois la version correspondante installée, l'invite disparaît, signe que le plugin est reconnu. Le plugin n'a ni icône ni interface : il est normal de ne pas le trouver sur l'écran d'accueil.
- **Où regarder en cas d'échec**: En cas d'avertissement de compatibilité, utilisez le build sélectionné par le Centre de plugins selon la matrice de compatibilité, ou vérifiez que l'hôte actuel appartient à l'intervalle déclaré du plugin ; si une incompatibilité bloque l'empaquetage, installez le build correspondant dans la matrice ; en cas d'erreur de vérification ou de modèle corrompu, réinstallez le plugin depuis une source officielle ; pour le reste, ouvrez un [ticket](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) avec les journaux AutoJs6 et les étapes de reproduction.

******

### Limites

******

Pour éviter tout malentendu, les points suivants sont explicitement hors du périmètre de ce plugin:

- Le plugin ne fonctionne pas seul : il n'a ni icône ni interface et doit être appelé par un AutoJs6 compatible.
- Le build sur l'appareil n'est pas un build cloud : ce protocole n'envoie pas le code du projet.
- AutoJs6 ne conserve pas un second cœur d'empaquetage dans son processus. Plugin absent, désactivé, non fiable, incompatible ou en échec : la requête s'arrête et l'ancien résultat est conservé.
- Le dépôt AutoJs6 génère toujours le Runtime Kit ; le plugin le vérifie, l'empaquette, le distribue et l'utilise sans inventer seul un modèle de runtime.
- L'ancienne capacité de "construction distante" reste désactivée pour les hôtes historiques. Ce nom désignait un autre processus local, pas un service Internet, et reste distinct de la capacité formelle.

******

### FAQ

******

**Q : Comment le Centre de plugins choisit-il un build ?**

R : Les versions compatibles d'AutoJs6 interrogent compat-matrix.json avec leur propre versionCode, sélectionnent le build ayant le pluginVersionCode le plus élevé dans l'intervalle compatible, puis privilégient l'ABI exact de l'appareil avec repli sur universal. Une entrée ne peut couvrir un intervalle de correctifs vérifié que si allowPatchVersionMismatch=true est explicite: l'hôte exact de construction empaquette sans avertissement, un autre hôte dans l'intervalle réutilise le même build avec un avertissement et un hôte hors intervalle ne peut pas l'utiliser. Si aucune entrée de la matrice n'est exploitable, le canal Release/tag existant reste utilisé. Si la version appariée du plugin est inférieure à celle installée, le Centre de plugins demande de désinstaller d'abord, puis d'installer le build apparié, car Android ne peut pas effectuer une rétrogradation sur place.

**Q : Pourquoi le plugin doit-il correspondre à AutoJs6 ?**

R : Le runtime du modèle doit correspondre à l'API hôte. Le Centre de plugins choisit le build compatible le plus récent et le meilleur ABI ; un hôte hors intervalle est bloqué.

**Q : Le plugin n'apparaît pas dans le lanceur. L'installation a-t-elle échoué ?**

R : Non. Il n'a volontairement ni icône ni interface et ne fonctionne que comme service AutoJs6. Vérifiez-le dans Paramètres > Applications.

**Q : Mon projet est-il envoyé à un serveur distant ?**

R : Non. Hôte et plugin communiquent entre deux processus du même appareil Android. Les anciens noms parlent de "construction distante" parce que Binder traverse une frontière de processus ; le mode formel est `on-device-plugin`.

**Q : Que se passe-t-il si le plugin échoue ?**

R : AutoJs6 arrête la requête, affiche une erreur exploitable et conserve tout APK existant ; il ne bascule pas silencieusement vers un second builder hôte.

******

### Référence Technique

******

Les sections ci-dessous s'adressent aux développeurs et intégrateurs ; elles ne sont généralement pas nécessaires pour simplement utiliser le plugin.

#### Runtime Kit

Le Runtime Kit est construit par le dépôt principal AutoJs6 et constitue la seule source de vérité pour le modèle d'application autonome. Ce plugin ne fait que vérifier et empaqueter cet artefact ; il ne génère pas `template.apk`. Un Runtime Kit complet contient généralement ces fichiers:

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

#### Identifiants De Découverte

L'hôte découvre et lie ce plugin via les identifiants suivants:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### Construction Locale

Générez d'abord un Runtime Kit depuis le dépôt principal AutoJs6:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

Construisez ensuite ce dépôt avec le répertoire Runtime Kit généré:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

Vous pouvez aussi extraire un `autojs6-runtime-kit-*.zip` publié dans `runtime-kit/` et construire directement:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### Flux De Publication

Le flux de publication de production attendu est:

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

#### Signature

Les versions de production du plugin doivent être signées avec la clé de signature de plugin AutoJs6 approuvée. Les publications GitHub Actions requièrent ces secrets de dépôt:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

Les constructions locales de publication prennent toujours en charge le fichier racine ignoré `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### Feuille De Route

******

ROADMAP.md suit sous forme de liste vérifiable le build formel géré par le plugin, les candidats, la livraison par ABI, la compatibilité, les preuves de sécurité et l'assurance après GA.

- [Voir ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### Historique Des Versions

******

# v1.0.0

###### 2026/09/02

* `Info` La voie normale Empaqueter l'application exige désormais le plugin APK Builder sur l'appareil ; l'ancien commutateur supportsRemoteBuild reste désactivé sans désactiver l'empaquetage normal
* `Info` Première version officielle de la ligne indépendante du plugin, associée exactement au Runtime Kit d'AutoJs6 v6.8.0 (versionCode 5277); la version composite du plugin est 1.0.0+autojs6-6.8.0 (versionCode 527701), le Centre de plugins sélectionne la build ABI associée via compat-matrix.json et les builds distantes restent désactivées par défaut
* `Ajout` Le moteur du plugin devient l'unique voie formelle d'empaquetage sur l'appareil ; AutoJs6 reste léger et valide indépendamment chaque APK renvoyé
* `Ajout` La création et la vérification BKS/JKS sont déplacées dans le plugin via une API de keystore versionnée et à échec fermé
* `Ajout` Ajout du SemVer 1.0.0 du plugin, d'une numérotation de build indépendante, de noms de version composés et de valeurs Android versionCode monotones permettant plusieurs versions du plugin pour un même hôte
* `Ajout` Ajout des variantes universal, arm64-v8a, armeabi-v7a, x86_64 et x86 avec sélection exacte de l'ABI et repli universal
* `Ajout` Ajout d'un contrat de plage de compatibilité hôte à échec fermé et d'une matrice de compatibilité faisant autorité afin qu'une plage de correctifs adjacents explicitement validée puisse partager une build du plugin
* `Correction` Alignement de la numérotation des constructions distantes expérimentales à fichier unique sur le constructeur historique, et ajout d’un contrôle préalable de l’espace de travail à échec fermé fondé sur les tailles décompressées recoupées, une limite d’expansion du modèle vérifiée à la compilation et une réserve de 256 Mio
* `Correction` Refus des métadonnées et directives source héritées d'empaquetage Node.js intégré avant BUILD/SIGN, avec des indications de migration vers le plugin Runtime externe, et suppression de l'injection obsolète du service Manifest et des autorisations de premier plan
* `Correction` Correction d’une course entre la fermeture et le thread de construction des sessions distantes expérimentales, qui pouvait recréer un espace de travail supprimé après annulation ou fermeture ; le nettoyage attend désormais le worker et ne laisse aucun fichier résiduel
* `Correction` Renforcement des builds distantes expérimentales: rejet des données chiffrées de transit TypeScript absentes de l'inventaire des chemins et détection correcte des magasins de clés BKS personnalisés après normalisation du nom de fichier dans l'espace de travail
* `Correction` Durcissement des limites d'entrée du build distant expérimental: validation stricte des types, tailles et profondeurs de Parcelable/Bundle et project.json, limites pour les magasins de clés, icônes et chemins ZIP, et correction des dépassements de nom de package ARSC et de nom de sortie dérivé
* `Correction` Le plugin ne pouvait pas être activé depuis le centre de plugins après son installation sur certains systèmes
* `Amélioration` Le workflow de publication de confiance prend désormais en charge un mode candidat isolé qui produit cinq APK signés en production et leur evidence depuis un artefact Actions hôte épinglé, sans créer de Release ni mettre à jour la matrice de compatibilité de référence
* `Amélioration` Unification des règles de validation du Runtime Kit entre Gradle et Python, notamment les résumés, tailles, fichiers obligatoires, entrées APK et la cohérence des cinq variantes
* `Amélioration` Publication avec les cinq APK d'un manifeste de preuves JSON lisible par machine, liant les empreintes des artefacts, le certificat de signature, les versions plugin/hôte, la plage de compatibilité, les ID Runtime Kit et les versions de protocole
* `Amélioration` Mise à jour des instructions d'installation, de la FAQ, de la répétition de publication et de la documentation en 10 langues pour les versions associées, la sélection ABI, la récupération après rétrogradation et le versionnement indépendant
* `Amélioration` Uniformiser la mise en page du README et la gestion des versions de la plateforme Gradle

# v6.8.0 Alpha5

###### 2026/07/16

* `Info` S'associe à AutoJs6 v6.8.0 Alpha5 ; les versions compatibles du Centre de plugins résolvent automatiquement le build apparié, tandis qu'une installation manuelle utilise le tag Release ou le suffixe autojs6- correspondant ; le plugin n'a ni icône ni interface et est invoqué automatiquement lors de l'empaquetage
* `Ajout` Permis à AutoJs6 de découvrir le plugin et de lire automatiquement son modèle intégré, si bien que "Empaqueter l'application" ne dépend plus d'un APK de modèle embarqué dans l'application principale
* `Ajout` Intégré le Runtime Kit complet : APK de modèle, magasin de clés par défaut, métadonnées d'exécution et fichiers de contrat
* `Ajout` Introduit des contrôles automatiques de compatibilité de version et de protocole avant l'empaquetage, avec avertissement ou blocage en cas de divergence pour éviter de produire des applications défectueuses
* `Ajout` Validé les empreintes SHA-256 du Runtime Kit et les entrées requises du modèle à la construction du plugin, et communiqué l'empreinte du modèle à AutoJs6 pour re-vérification à l'exécution
* `Ajout` Introduit un protocole expérimental de construction distante réalisant une construction légère dans le processus du plugin (désactivé par défaut, à activer explicitement à la construction)
* `Ajout` Mis en place le flux de publication automatisé : quand le dépôt principal AutoJs6 publie une version, un APK de plugin assorti est construit, signé avec la clé approuvée, vérifié par empreinte de certificat puis publié
* `Ajout` Couvert 10 langues dans les métadonnées du plugin, les instructions, le README et le CHANGELOG : chinois simplifié, chinois traditionnel (Hong Kong/Taïwan), anglais, français, espagnol, japonais, coréen, russe et arabe

# v6.7.1 Alpha4

###### 2026/07/09

* `Info` Première version publique ; s'associe à AutoJs6 de la même version (v6.7.1 Alpha4)
* `Ajout` Détaché du dépôt principal AutoJs6 en dépôt de plugin autonome, avec l'implémentation initiale du service de plugin d'APK de modèle
* `Ajout` Établi le pipeline piloté par le Runtime Kit, déclenché par le dépôt principal AutoJs6, qui récupère, vérifie, construit et publie le plugin

##### Pour plus d'historique

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-fr.md)

******

### Licence

******

Ce projet est publié sous la Mozilla Public License 2.0, qui permet l'utilisation, la modification et la distribution selon ses termes.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

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

`strings.xml` contient les noms, descriptions et instructions de secours localisés du plugin ; `plugin_instruction.md` contient les instructions affichées par l'hôte. Les fichiers README et CHANGELOG sont générés depuis les sources JSON par `.python/generate_markdown.py` ; pour modifier la documentation, éditez les JSON puis relancez le script au lieu de modifier les fichiers générés.

******

### Liens

******

- Projet principal AutoJs6: https://github.com/SuperMonster003/AutoJs6
- Documentation AutoJs6: https://docs.autojs6.com
