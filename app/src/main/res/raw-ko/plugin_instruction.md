APK Builder Template는 AutoJs6가 독립 실행형 앱을 패키징할 때 사용하는 Runtime Kit를 제공합니다.

AutoJs6 버전과 일치하는 플러그인 릴리스를 설치하세요. 호스트는 `org.autojs.plugin.APK_BUILDER` 로 플러그인을 찾고 `assets/runtime-kit/template.apk` 를 읽습니다.

패키징된 Runtime Kit에는 다음 파일이 포함됩니다:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

원격 빌드 지원은 기본적으로 꺼져 있으며 `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true` 로 만든 빌드에서만 켤 수 있습니다.
