APK Builder Template는 AutoJs6가 독립 실행형 앱을 패키징할 때 사용하는 Runtime Kit를 제공합니다.

가능하면 최신 AutoJs6 플러그인 센터에서 설치하세요. 센터는 `compat-matrix.json` 을 읽어 이 호스트와 짝이 맞는 빌드를 선택하고 기기의 정확한 ABI 를 우선하며, 없으면 universal 로 대체합니다. 수동 설치 시에는 일치하는 Release 태그 또는 플러그인 버전의 autojs6- 접미사를 확인하세요. 더 새로운 플러그인 빌드에서 되돌릴 때는 Android 가 다운그레이드 덮어쓰기를 허용하지 않으므로 먼저 제거하세요. 호스트는 `org.autojs.plugin.APK_BUILDER` 로 플러그인을 찾고 `assets/runtime-kit/template.apk` 를 읽습니다.

Runtime Kit 는 검증된 패치 구간을 명시적으로 포함할 수 있습니다. 실제 빌드 호스트에서는 경고 없이 패키징하고, 구간 안의 다른 호스트는 경고 후 계속하지만, 구간 밖의 호스트는 템플릿 전송 전에 차단됩니다.

패키징된 Runtime Kit에는 다음 파일이 포함됩니다:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

패키징은 같은 Android 기기의 플러그인 프로세스 안에서 완전히 수행되며 프로젝트 소스를 업로드하지 않습니다. AutoJs6 는 신뢰와 호환성을 판정하고 반환된 APK 를 독립적으로 검증합니다. 이전 `supportsRemoteBuild` 스위치는 꺼진 상태를 유지하지만 이 정식 경로를 끄지는 않습니다.
