APK Builder Template fournit le Runtime Kit utilisé par AutoJs6 pour empaqueter des applications autonomes.

Installez de préférence depuis un Centre de plugins AutoJs6 récent: il lit `compat-matrix.json`, choisit le build apparié à cet hôte, privilégie l'ABI exact de l'appareil et se replie sur universal. Pour une installation manuelle, utilisez le tag Release correspondant ou le suffixe autojs6- de la version du plugin; pour revenir depuis un build plus récent, désinstallez-le d'abord, car Android ne peut pas écraser l'application par une rétrogradation. L'hôte découvre le plugin via `org.autojs.plugin.APK_BUILDER` et lit `assets/runtime-kit/template.apk`.

Un Runtime Kit peut couvrir explicitement un intervalle de correctifs vérifié. Son hôte exact de construction empaquette sans avertissement; un autre hôte dans l'intervalle continue avec un avertissement, tandis qu'un hôte hors intervalle est bloqué avant le transfert du modèle.

Le Runtime Kit empaqueté contient :

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

L'empaquetage s'exécute entièrement dans le processus du plugin sur le même appareil Android ; le code du projet n'est pas envoyé. AutoJs6 contrôle la confiance et la compatibilité puis valide indépendamment l'APK renvoyé. L'ancien commutateur `supportsRemoteBuild` reste désactivé sans désactiver cette voie formelle.
