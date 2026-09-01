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

La fonction "Empaqueter l'application" d'AutoJs6 transforme un script ou un projet en un APK qui s'installe et s'exécute de manière autonome, sans AutoJs6 sur l'appareil cible. L'empaquetage a besoin d'un "APK de modèle" comme squelette : une application qui contient déjà l'environnement d'exécution complet des scripts. Pour alléger l'application principale, les versions récentes d'AutoJs6 n'embarquent plus ce modèle volumineux ; il vit désormais dans ce plugin, installé uniquement par les utilisateurs qui empaquettent des applications.

Le plugin n'a ni icône ni interface. Tout se passe en arrière-plan : lors de l'empaquetage, AutoJs6 découvre le plugin, vérifie la compatibilité des versions et l'intégrité des fichiers, puis lit l'APK de modèle intégré pour terminer l'opération.

En une phrase : si vous utilisez "Empaqueter l'application", installez le build que le Centre de plugins a sélectionné pour votre AutoJs6 selon la matrice de compatibilité ; si vous n'empaquetez jamais d'applications autonomes, il ne vous est pas nécessaire.

******

### Fonctionnement

******

Lors de l'empaquetage d'une application autonome, AutoJs6 et ce plugin coopèrent comme suit :

1. Découverte : AutoJs6 localise le plugin de modèle installé et lit ses métadonnées
2. Contrôle de compatibilité : les versions, les versions de protocole et le nom du package de modèle sont comparés ; toute divergence produit un avertissement ou bloque l'empaquetage
3. Contrôle d'intégrité : l'empreinte SHA-256 de l'APK de modèle est vérifiée pour écarter les fichiers corrompus ou altérés
4. Transfert du modèle : l'APK de modèle est transmis en flux via un tube inter-processus, sans copie temporaire
5. Empaquetage : AutoJs6 écrit le script, la configuration et les ressources dans le modèle et produit l'APK autonome final

******

### Fonctions

******

- Fournit le modèle complet d'application autonome (Runtime Kit) pour la fonction "Empaqueter l'application" d'AutoJs6 ; aucune configuration n'est requise après l'installation.
- Chaque build du plugin conserve dans son nom l'hôte AutoJs6 utilisé pour le créer (par exemple 1.0.0+autojs6-6.8.0-alpha5) et peut déclarer explicitement un intervalle fermé de correctifs vérifié; la correspondance exacte reste silencieuse, un autre hôte dans l'intervalle déclenche un avertissement et tout hôte hors intervalle est bloqué.
- Double protection d'intégrité : les empreintes SHA-256 et les entrées requises du modèle sont validées à la construction du plugin, et l'empreinte du modèle est communiquée à AutoJs6 pour re-vérification lors de l'empaquetage.
- Le modèle est transmis à AutoJs6 en flux via un tube inter-processus, sans copies temporaires superflues.
- Embarque un magasin de clés par défaut : un APK installable peut être produit même sans clé de signature personnalisée.
- Prend en charge un protocole expérimental de "construction distante" où le processus du plugin réalise lui-même une construction légère (désactivé par défaut ; voir Limites).
- Les métadonnées du plugin, les instructions, le README et le CHANGELOG couvrent 10 langues : chinois simplifié, chinois traditionnel (Hong Kong/Taïwan), anglais, français, espagnol, japonais, coréen, russe et arabe.

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

- Le plugin ne s'utilise pas seul : il n'a ni icône ni interface et n'est invoqué par AutoJs6 que pendant l'empaquetage.
- Le plugin ne génère pas l'APK de modèle : le modèle et le Runtime Kit sont construits et publiés par le dépôt principal AutoJs6 ; ce plugin ne fait que les vérifier, les empaqueter et les distribuer.
- Le plugin n'intervient pas dans l'écriture ni l'exécution quotidienne des scripts : seule la fonction "Empaqueter l'application" le lit.
- La construction distante est expérimentale et désactivée par défaut : les plugins publiés officiellement ne l'activent pas ; elle n'est disponible que dans des plugins auto-construits avec l'option explicitement activée.
- Le plugin n'assouplit pas les exigences de version : empaqueter avec une version d'AutoJs6 divergente peut être bloqué, et même en cas de succès, le résultat n'est pas garanti.

******

### FAQ

******

**Q : Comment le Centre de plugins choisit-il un build ?**

R : Les versions compatibles d'AutoJs6 interrogent compat-matrix.json avec leur propre versionCode, sélectionnent le build ayant le pluginVersionCode le plus élevé dans l'intervalle compatible, puis privilégient l'ABI exact de l'appareil avec repli sur universal. Une entrée ne peut couvrir un intervalle de correctifs vérifié que si allowPatchVersionMismatch=true est explicite: l'hôte exact de construction empaquette sans avertissement, un autre hôte dans l'intervalle réutilise le même build avec un avertissement et un hôte hors intervalle ne peut pas l'utiliser. Si aucune entrée de la matrice n'est exploitable, le canal Release/tag existant reste utilisé. Si la version appariée du plugin est inférieure à celle installée, le Centre de plugins demande de désinstaller d'abord, puis d'installer le build apparié, car Android ne peut pas effectuer une rétrogradation sur place.

**Q : Pourquoi le plugin doit-il être apparié à ma version d'AutoJs6 ?**

R : L'environnement d'exécution embarqué dans l'APK de modèle correspond strictement à l'API d'exécution d'AutoJs6 ; la compatibilité est donc déterminée par le contrat versionCode déclaré et validé du plugin, non par sa propre version sémantique. La plupart des builds ciblent un seul hôte exact ; un build peut aussi déclarer explicitement un intervalle fermé de versions correctives vérifié. L'hôte de référence passe alors sans avertissement, les autres hôtes dans l'intervalle en reçoivent un, et tout hôte hors intervalle est bloqué. La version propre du plugin (comme 1.0.0) évolue indépendamment, tandis que le suffixe autojs6- du nom de version et le tag de release indiquent l'hôte apparié ; sur un AutoJs6 plus ancien, téléchargez le build du plugin sous le tag ancien correspondant (pour revenir depuis un plugin plus récent, désinstallez-le d'abord — Android n'autorise pas l'installation en rétrogradation).

**Q : Je ne trouve pas le plugin sur mon écran d'accueil. L'installation a-t-elle échoué ?**

R : Non. Le plugin n'a ni icône ni interface et ne fonctionne que comme service d'arrière-plan pour AutoJs6. Vous pouvez le vérifier dans la liste "Paramètres > Applications" du système sous le nom APK Builder Template.

**Q : L'empaquetage signale un échec de vérification du modèle. Que faire ?**

R : Cela signifie généralement que le plugin installé est incomplet ou corrompu. Réinstallez-le depuis le Centre de plugins AutoJs6 ou les Releases de ce dépôt ; si le problème persiste, ouvrez un ticket.

**Q : Pourquoi le plugin est-il si volumineux ?**

R : Il embarque un modèle complet d'application autonome, avec le moteur de scripts et les bibliothèques natives de toutes les architectures. C'est précisément pourquoi le modèle a été extrait de l'application principale : seuls les utilisateurs qui empaquettent portent ce poids.

**Q : Qu'est-ce que la "construction distante" ?**

R : Un protocole expérimental qui permet au plugin de réaliser une construction légère dans son propre processus (décompresser le modèle, écrire le script et la configuration, réécrire le nom de package et les ressources, re-signer). Les plugins publiés officiellement la gardent désactivée ; elle s'adresse pour l'instant aux développeurs qui construisent le plugin eux-mêmes.

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

Les travaux prévus et leur avancement sont suivis sous forme de liste vérifiable dans ROADMAP.md : stabilisation de la construction distante, variantes de modèle par architecture, compatibilité au niveau des correctifs, etc. La discussion est ouverte dans les Issues.

- [Voir ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### Historique Des Versions

******

# v1.0.0

###### 2026/09/01

* `Info` Première version officielle de la ligne indépendante du plugin, associée exactement au Runtime Kit d'AutoJs6 v6.8.0 (versionCode 5277); la version composite du plugin est 1.0.0+autojs6-6.8.0 (versionCode 527701), le Centre de plugins sélectionne la build ABI associée via compat-matrix.json et les builds distantes restent désactivées par défaut
* `Ajout` Ajout du SemVer 1.0.0 du plugin, d'une numérotation de build indépendante, de noms de version composés et de valeurs Android versionCode monotones permettant plusieurs versions du plugin pour un même hôte
* `Ajout` Ajout des variantes universal, arm64-v8a, armeabi-v7a, x86_64 et x86 avec sélection exacte de l'ABI et repli universal
* `Ajout` Ajout d'un contrat de plage de compatibilité hôte à échec fermé et d'une matrice de compatibilité faisant autorité afin qu'une plage de correctifs adjacents explicitement validée puisse partager une build du plugin
* `Correction` Alignement de la numérotation des constructions distantes expérimentales à fichier unique sur le constructeur historique, et ajout d’un contrôle préalable de l’espace de travail à échec fermé fondé sur les tailles décompressées recoupées, une limite d’expansion du modèle vérifiée à la compilation et une réserve de 256 Mio
* `Correction` Refus des métadonnées et directives source héritées d'empaquetage Node.js intégré avant BUILD/SIGN, avec des indications de migration vers le plugin Runtime externe, et suppression de l'injection obsolète du service Manifest et des autorisations de premier plan
* `Correction` Correction d’une course entre la fermeture et le thread de construction des sessions distantes expérimentales, qui pouvait recréer un espace de travail supprimé après annulation ou fermeture ; le nettoyage attend désormais le worker et ne laisse aucun fichier résiduel
* `Correction` Renforcement des builds distantes expérimentales: rejet des données chiffrées de transit TypeScript absentes de l'inventaire des chemins et détection correcte des magasins de clés BKS personnalisés après normalisation du nom de fichier dans l'espace de travail
* `Correction` Durcissement des limites d'entrée du build distant expérimental: validation stricte des types, tailles et profondeurs de Parcelable/Bundle et project.json, limites pour les magasins de clés, icônes et chemins ZIP, et correction des dépassements de nom de package ARSC et de nom de sortie dérivé
* `Correction` Le plugin ne pouvait pas être activé depuis le centre de plugins après son installation sur certains systèmes
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
