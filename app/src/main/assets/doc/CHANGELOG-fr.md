******

### Historique Des Versions

******

# v1.2.19

###### 2026/10/20

* `Fonctionnalité` API speech de reconnaissance vocale et de gestion des modèles pour Node.js, Bun et Workers avec WAV/PCM, dictée et activation vocale, installation/importation, synthèse et annulation

# v1.2.18

###### 2026/10/20

* `Fonctionnalité` API de synthèse vocale pour Node.js, Bun et Workers avec requêtes des moteurs et voix, lecture, silence, fichiers audio et événements des tâches

# v1.2.17

###### 2026/10/20

* `Fonctionnalité` Callbacks Java, sous-classes générées et chargement JAR/DEX pour Node.js, Bun et Workers, avec retours Promise, appels de la superclasse et libération en fin de session
* `Correctif` Arrêt prématuré de la page de lancement des applications Bun masquées, exposant le processus à la récupération pendant le démarrage à froid

# v1.2.16

###### 2026/10/20

* `Fonctionnalité` Interopérabilité Java/Android pour Node.js, Bun et Workers, avec constructeurs et membres publics, tableaux, valeurs long exactes, surcharges explicites et handles Context

# v1.2.15

###### 2026/10/19

* `Fonctionnalité` Objets Android pour Node.js, Bun et les Workers, avec Paint, ColorStateList, couleurs du thème et URI FileProvider
* `Fonctionnalité` API persistantes de calendrier et IntentTask pour Node.js et Bun, avec tâches quotidiennes, hebdomadaires, mensuelles, annuelles, ponctuelles, différées et de diffusion
* `Correctif` URI FileProvider des applications générées utilisant l'autorité du modèle au lieu de celle du manifeste réel

# v1.2.14

###### 2026/10/19

* `Fonctionnalité` Commandes de périphérique et dialogues pour Bun et les Workers Node.js, avec luminosité, volume et nettoyage indépendant des fenêtres

# v1.2.13

###### 2026/10/19

* `Fonctionnalité` Événements et notifications hôtes pour Bun et les Workers Node.js, avec nettoyage indépendant des abonnements et notifications

# v1.2.12

###### 2026/10/19

* `Fonctionnalité` Shell hôte avec flux, autorisation Shizuku et nettoyage des processus pour Bun, Node.js et les Workers

# v1.2.11

###### 2026/10/19

* `Fonctionnalité` Mesures et abonnements aux capteurs pour Bun et les Workers Node.js, avec annulation et libération automatique des écouteurs

# v1.2.10

###### 2026/10/19

* `Fonctionnalité` API hôtes database/sqlite pour Bun et les Workers Node.js, avec requêtes paramétrées, transactions et persistance

# v1.2.9

###### 2026/10/19

* `Fonctionnalité` API Bun de fichiers et de stockage persistant, avec autorisations distinctes et stockage partagé avec Node.js

# v1.2.8

###### 2026/10/19

* `Fonctionnalité` API Bun pour accessibilité, applications et presse-papiers alignées sur Node.js, avec autorisations distinctes et applications empaquetées

# v1.2.7

###### 2026/10/18

* `Fonctionnalité` Adaptateur de partage ecran WebView, capture audio par service de premier plan et enregistrement camera natif en arriere-plan pour Node.js/Bun et Node Worker

# v1.2.6

###### 2026/10/18

* `Fonctionnalité` Flux audio et video WebView getUserMedia/MediaRecorder, attente des Promise de page, demandes explicites de permissions multimedia Android et cycle de vie des Worker
* `Correctif` Les applications generees conservent MODIFY_AUDIO_SETTINGS avec RECORD_AUDIO pour la capture audio WebView

# v1.2.5

###### 2026/10/18

* `Fonctionnalité` Node.js/Bun: Capture vidéo WebView et enregistrement audio AAC/M4A au premier plan, avec options média, délais configurables et nettoyage des fichiers temporaires

# v1.2.4

###### 2026/10/18

* `Fonctionnalité` Node.js/Bun: Capture photo WebView en pleine résolution, sélection de fichiers et dossiers, lecture des octets et téléchargements blob/data vers les emplacements choisis

# v1.2.3

###### 2026/10/18

* `Fonctionnalité` Node.js/Bun: Téléchargements HTTP(S) WebView, sélection de fichiers système et rappels WebViewClient/WebChromeClient asynchrones avec règles natives de navigation et de ressources

# v1.2.2

###### 2026/10/18

* `Fonctionnalité` Node.js/Bun: Chargement WebView et résultats JavaScript, panneaux Drawer, navigation et menus Toolbar, pages ViewPager et événements natifs asynchrones

# v1.2.1

###### 2026/10/18

* `Fonctionnalité` Node.js/Bun prennent en charge les formulaires et interfaces XML courants, les propriétés View asynchrones, les affectations ordonnées, la reprise après erreur et les Workers

# v1.2.0

###### 2026/10/18

* `Fonctionnalité` APK Builder prend en charge les projets Bun signés indépendamment, avec des capacités hôtes explicites et un Runtime Kit compatible avec Compose

# v1.1.0

###### 2026/10/11

* `Fonctionnalité` Les projets et scripts Node.js peuvent être empaquetés en applications signées indépendamment, exécutées par le plugin externe Node.js Runtime, avec une aide à l'installation si le runtime est indisponible

# v1.0.4

###### 2026/10/05

* `Amélioration` Les icônes du centre de plugins utilisent les tailles, positions, images claires et sombres et fonds circulaires réglés dans Icon Studio, avec les sources et paramètres permettant de les reproduire
* `Amélioration` Les icônes des informations d'application Android utilisent les illustrations et les fonds clairs et sombres d'Icon Studio, en conservant les images transparentes du centre de plugins et les choix du lanceur

# v1.0.3

###### 2026/09/20

* `Correctif` resources.arsc est stocké non compressé et aligné sur 4 octets dans les APK générés afin que les applications ciblant Android 11 ou plus récent puissent être installées
* `Correctif` Avertissements de lecture SDK XML v4 avec AGP 9.1 et contrôles d'alignement natif des APK déclenchés par erreur lors de l'assemblage des tests unitaires JVM, avec les plugins de compilation partagés 1.8.3
* `Amélioration` Suppression du commutateur de compilation `enableRemoteBuild` ; le plugin déclare toujours sa capacité de construction d'APK
* `Amélioration` compileSdk et targetSdk passent à 37 (Android 17) ; le comportement du plugin ne dépend pas de la nouvelle cible
* `Amélioration` Reconstruire les cinq variantes ABI du Runtime Kit pour AutoJs6 6.8.0 build 5282 avec une plage de compatibilité hôte explicite

# v1.0.2

###### 2026/09/13

* `Correctif` Conserver la date de version du plugin en anglais quelle que soit la langue de la machine de compilation
* `Amélioration` Ressources traduites cohérentes, activation explicite du plugin et validation des paquets de publication

# v1.0.1

###### 2026/09/13

* `Amélioration` Ressources traduites cohérentes, activation explicite du plugin et validation des paquets de publication

# v6.8.0

###### 2026/09/11

* `Amélioration` Vérification à la compilation de l'alignement des pages de 16 KB des bibliothèques natives 64 bits, avec contrôle du contrat manifest et rapports JSON

# v1.0.0

###### 2026/09/10

* `Note` La voie normale Empaqueter l'application exige désormais le plugin APK Builder sur l'appareil ; l'ancien commutateur supportsRemoteBuild reste désactivé sans désactiver l'empaquetage normal
* `Note` Première version officielle de la ligne indépendante du plugin, associée exactement au Runtime Kit d'AutoJs6 v6.8.0 (versionCode 5277); la version composite du plugin est 1.0.0+autojs6-6.8.0 (versionCode 527701), le Centre de plugins sélectionne la build ABI associée via compat-matrix.json et les builds distantes restent désactivées par défaut
* `Fonctionnalité` Le moteur du plugin devient l'unique voie formelle d'empaquetage sur l'appareil ; AutoJs6 reste léger et valide indépendamment chaque APK renvoyé
* `Fonctionnalité` La création et la vérification BKS/JKS sont déplacées dans le plugin via une API de keystore versionnée et à échec fermé
* `Fonctionnalité` Ajout du SemVer 1.0.0 du plugin, d'une numérotation de build indépendante, de noms de version composés et de valeurs Android versionCode monotones permettant plusieurs versions du plugin pour un même hôte
* `Fonctionnalité` Ajout des variantes universal, arm64-v8a, armeabi-v7a, x86_64 et x86 avec sélection exacte de l'ABI et repli universal
* `Fonctionnalité` Ajout d'un contrat de plage de compatibilité hôte à échec fermé et d'une matrice de compatibilité faisant autorité afin qu'une plage de correctifs adjacents explicitement validée puisse partager une build du plugin
* `Correctif` Alignement de la numérotation des constructions distantes expérimentales à fichier unique sur le constructeur historique, et ajout d’un contrôle préalable de l’espace de travail à échec fermé fondé sur les tailles décompressées recoupées, une limite d’expansion du modèle vérifiée à la compilation et une réserve de 256 Mio
* `Correctif` Refus des métadonnées et directives source héritées d'empaquetage Node.js intégré avant BUILD/SIGN, avec des indications de migration vers le plugin Runtime externe, et suppression de l'injection obsolète du service Manifest et des autorisations de premier plan
* `Correctif` Correction d’une course entre la fermeture et le thread de construction des sessions distantes expérimentales, qui pouvait recréer un espace de travail supprimé après annulation ou fermeture ; le nettoyage attend désormais le worker et ne laisse aucun fichier résiduel
* `Correctif` Renforcement des builds distantes expérimentales: rejet des données chiffrées de transit TypeScript absentes de l'inventaire des chemins et détection correcte des magasins de clés BKS personnalisés après normalisation du nom de fichier dans l'espace de travail
* `Correctif` Durcissement des limites d'entrée du build distant expérimental: validation stricte des types, tailles et profondeurs de Parcelable/Bundle et project.json, limites pour les magasins de clés, icônes et chemins ZIP, et correction des dépassements de nom de package ARSC et de nom de sortie dérivé
* `Correctif` Le plugin ne pouvait pas être activé depuis le centre de plugins après son installation sur certains systèmes
* `Amélioration` Étendre la vérification des archives de projet et de compilation à 262144 entrées, 4 GiB compressés, 2 GiB par entrée, 8 GiB extraits au total et un rapport de compression de 2000:1
* `Amélioration` Le workflow de publication de confiance prend désormais en charge un mode candidat isolé qui produit cinq APK signés en production et leur evidence depuis un artefact Actions hôte épinglé, sans créer de Release ni mettre à jour la matrice de compatibilité de référence
* `Amélioration` Unification des règles de validation du Runtime Kit entre Gradle et Python, notamment les résumés, tailles, fichiers obligatoires, entrées APK et la cohérence des cinq variantes
* `Amélioration` Publication avec les cinq APK d'un manifeste de preuves JSON lisible par machine, liant les empreintes des artefacts, le certificat de signature, les versions plugin/hôte, la plage de compatibilité, les ID Runtime Kit et les versions de protocole
* `Amélioration` Mise à jour des instructions d'installation, de la FAQ, de la répétition de publication et de la documentation en 10 langues pour les versions associées, la sélection ABI, la récupération après rétrogradation et le versionnement indépendant
* `Amélioration` Uniformiser la mise en page du README et la gestion des versions de la plateforme Gradle

# v6.8.0 Alpha5

###### 2026/07/16

* `Note` S'associe à AutoJs6 v6.8.0 Alpha5 ; les versions compatibles du Centre de plugins résolvent automatiquement le build apparié, tandis qu'une installation manuelle utilise le tag Release ou le suffixe autojs6- correspondant ; le plugin n'a ni icône ni interface et est invoqué automatiquement lors de l'empaquetage
* `Fonctionnalité` Permis à AutoJs6 de découvrir le plugin et de lire automatiquement son modèle intégré, si bien que "Empaqueter l'application" ne dépend plus d'un APK de modèle embarqué dans l'application principale
* `Fonctionnalité` Intégré le Runtime Kit complet : APK de modèle, magasin de clés par défaut, métadonnées d'exécution et fichiers de contrat
* `Fonctionnalité` Introduit des contrôles automatiques de compatibilité de version et de protocole avant l'empaquetage, avec avertissement ou blocage en cas de divergence pour éviter de produire des applications défectueuses
* `Fonctionnalité` Validé les empreintes SHA-256 du Runtime Kit et les entrées requises du modèle à la construction du plugin, et communiqué l'empreinte du modèle à AutoJs6 pour re-vérification à l'exécution
* `Fonctionnalité` Introduit un protocole expérimental de construction distante réalisant une construction légère dans le processus du plugin (désactivé par défaut, à activer explicitement à la construction)
* `Fonctionnalité` Mis en place le flux de publication automatisé : quand le dépôt principal AutoJs6 publie une version, un APK de plugin assorti est construit, signé avec la clé approuvée, vérifié par empreinte de certificat puis publié
* `Fonctionnalité` Couvert 10 langues dans les métadonnées du plugin, les instructions, le README et le CHANGELOG : chinois simplifié, chinois traditionnel (Hong Kong/Taïwan), anglais, français, espagnol, japonais, coréen, russe et arabe

# v6.7.1 Alpha4

###### 2026/07/09

* `Note` Première version publique ; s'associe à AutoJs6 de la même version (v6.7.1 Alpha4)
* `Fonctionnalité` Détaché du dépôt principal AutoJs6 en dépôt de plugin autonome, avec l'implémentation initiale du service de plugin d'APK de modèle
* `Fonctionnalité` Établi le pipeline piloté par le Runtime Kit, déclenché par le dépôt principal AutoJs6, qui récupère, vérifie, construit et publie le plugin
