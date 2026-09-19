<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>AutoJs6 "앱 패키징" 기능을 지원하는 템플릿 플러그인</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 언어

******

현재 README.md 는 다음 언어를 지원합니다:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- 한국어 [ko] # 현재
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### 소개

******

AutoJs6 의 "애플리케이션 패키징" 기능은 스크립트나 프로젝트를 독립 설치·실행 가능한 APK 로 만듭니다. AutoJs6 본체를 가볍게 유지하기 위해 큰 템플릿과 패키징 핵심 전체를 이 플러그인이 담당합니다.

플러그인에는 아이콘이나 화면이 없습니다. AutoJs6 는 플러그인 검색·검증, 제한된 요청 준비, 진행 표시를 담당합니다. 플러그인은 자체 템플릿을 풀고 프로젝트와 리소스를 기록하며 Manifest/resources, ABI, 키 저장소와 서명을 처리해 후보 APK 를 반환합니다. AutoJs6 는 배포 전에 결과를 독립적으로 다시 검증합니다.

모든 작업은 같은 Android 기기에서 Binder 와 파일 디스크립터로 수행됩니다. 프로젝트 소스를 네트워크나 클라우드 빌드 서비스로 업로드하지 않습니다.

******

### 동작 방식

******

패키징 흐름은 다음과 같습니다:

1. 허용 판정: AutoJs6 가 공식 서명, 활성 상태, 호스트 버전 범위, ABI, 정식 기능, 프로토콜, 기기 내 실행 모드를 확인
2. 요청 준비: 크기 제한이 있는 프로젝트/네이티브/키 저장소 입력을 만들고 예상 패키지와 서명자 신원을 고정
3. 플러그인 빌드: 요청을 다시 검증하고 Runtime Kit 템플릿을 풀어 프로젝트, Manifest/resources, ABI 를 처리하고 서명
4. 결과 반환: 읽기 전용 FD 로 후보 APK 를 반환하고 비공개 작업 공간을 정리
5. 호스트 배포: AutoJs6 가 크기, SHA-256, APK 구조, 서명, 서명자, 패키지명과 버전을 재검증하고 모두 통과할 때만 원자적으로 교체

******

### 기능

******

- 템플릿, 프로젝트/리소스, Manifest 와 resources.arsc, ABI, 키 저장소, 서명을 포함한 기기 내 패키징 핵심을 완전히 소유합니다.
- AutoJs6 를 가볍게 유지합니다: 호스트는 UI, 신뢰/호환성 판정, 준비, 취소/진행, 독립 출력 검증을 맡고 두 번째 빌더는 보유하지 않습니다.
- 같은 Android 기기에서 Binder/AIDL 과 ParcelFileDescriptor 로 완결되며 프로젝트를 인터넷이나 클라우드로 보내지 않습니다.
- 각 플러그인 빌드를 검증된 AutoJs6 Runtime Kit 와 연결하고 검증된 패치 폐구간을 선언할 수 있습니다.
- universal, arm64-v8a, armeabi-v7a, x86_64, x86 변형과 universal 대체를 제공합니다.
- 기본 키 저장소를 포함하고 플러그인이 BKS/JKS 를 생성·검증하며 사용자 키 저장소도 지원합니다.
- 메타데이터, 안내, README, CHANGELOG 는 10개 언어를 지원합니다.

******

### 빠른 시작

******

- **설치 방법**: 가능하면 AutoJs6 플러그인 센터에서 설치하세요: 지원되는 호스트 빌드는 호환성 매트릭스를 읽어 짝이 맞는 플러그인 버전과 현재 기기의 정확한 ABI 자산을 자동 선택하고, 없으면 universal 로 대체합니다. 수동 설치 시에는 [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) 에서 APK 를 내려받고 AutoJs6 와 같은 이름의 릴리스 태그 또는 플러그인 버전의 autojs6- 접미사로 짝이 맞는 호스트를 확인하세요 (예: 플러그인 v1.0.0+autojs6-6.8.0-alpha5 는 AutoJs6 v6.8.0 Alpha5 와 짝을 이룹니다). 플러그인 센터가 설치된 버전보다 낮은 버전을 선택하면 Android 가 다운그레이드 덮어쓰기를 허용하지 않으므로 안내에 따라 먼저 제거한 뒤 다시 설치하세요.
- **사용 방법**: 추가 조작이 필요 없습니다. AutoJs6 에서 평소처럼 "앱 패키징" 기능을 사용하면 패키징 과정이 플러그인을 자동으로 발견해 내장 템플릿을 사용합니다.
- **적용 확인 방법**: 플러그인이 없거나 버전이 다르면 AutoJs6 의 패키징 진입점에서 설치 또는 활성화를 안내합니다. 일치하는 버전을 설치하면 안내가 사라지며, 이는 플러그인이 정상 인식되었다는 뜻입니다. 플러그인에는 아이콘과 화면이 없으므로 런처에서 보이지 않는 것이 정상입니다.
- **문제가 생기면**: 호환성 경고가 나오면 플러그인 센터가 호환성 매트릭스에서 선택한 빌드를 사용하거나 현재 호스트가 플러그인의 선언 범위 안에 있는지 확인하세요. 비호환으로 패키징이 차단되면 매트릭스에 맞는 빌드를 설치하세요. 템플릿 손상이나 검증 오류가 나오면 공식 경로에서 플러그인을 다시 설치하세요. 그 밖의 문제는 AutoJs6 로그와 재현 절차를 첨부해 [Issues](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) 에 알려 주세요.

******

### 기능 범위

******

오해를 피하기 위해 다음 사항은 본 플러그인의 기능 범위 밖임을 명시합니다:

- 단독으로 사용할 수 없습니다: 아이콘과 화면이 없고 호환 AutoJs6 가 호출합니다.
- 기기 내 빌드는 클라우드 빌드가 아니며 이 프로토콜은 프로젝트 소스를 업로드하지 않습니다.
- AutoJs6 는 프로세스 안에 두 번째 패키징 핵심을 보유하지 않습니다. 플러그인이 없거나 비활성, 비신뢰, 비호환 또는 실패하면 요청을 중단하고 기존 결과를 보존합니다.
- Runtime Kit 는 계속 AutoJs6 저장소가 생성하며 플러그인은 이를 검증, 패키징, 배포, 사용합니다.
- 이전 "원격 빌드" 기능은 구형 호스트용으로 비활성 상태를 유지합니다. 이름은 기기 안의 다른 프로세스를 뜻했으며 인터넷 서비스가 아니고 정식 기능과 분리됩니다.

******

### 자주 묻는 질문

******

**Q: 플러그인 센터는 빌드를 어떻게 선택하나요?**

A: 지원되는 AutoJs6 는 자체 versionCode 로 compat-matrix.json 을 조회하고 호환 구간에서 pluginVersionCode 가 가장 높은 빌드를 선택합니다. 그런 다음 기기의 정확한 ABI 를 우선하고 없으면 universal 로 대체합니다. 매트릭스 항목은 allowPatchVersionMismatch=true 를 명시한 경우에만 검증된 패치 구간을 포함할 수 있습니다. 실제 빌드 호스트에서는 경고 없이 패키징하고, 구간 안의 다른 호스트는 같은 빌드를 경고와 함께 재사용하며, 구간 밖의 호스트는 사용할 수 없습니다. 사용할 수 있는 매트릭스 항목이 없으면 기존 Release/태그 경로를 사용합니다. 호환 플러그인 버전이 설치된 버전보다 낮으면 Android 가 제자리 다운그레이드를 할 수 없으므로 플러그인 센터가 먼저 제거한 뒤 호환 빌드를 설치하도록 안내합니다.

**Q: 왜 AutoJs6 와 맞는 플러그인 버전이 필요한가요?**

A: 템플릿 런타임이 호스트 API 와 일치해야 합니다. 플러그인 센터가 가장 높은 호환 버전과 최적 ABI 를 선택하며 범위 밖 호스트는 차단됩니다.

**Q: 런처에 플러그인이 없습니다. 설치 실패인가요?**

A: 아닙니다. 의도적으로 아이콘과 화면이 없고 AutoJs6 백그라운드 서비스로만 동작합니다. 설정 > 애플리케이션에서 확인하세요.

**Q: 프로젝트가 원격 서버로 전송되나요?**

A: 아닙니다. 같은 Android 기기의 두 앱 프로세스가 통신합니다. 예전 코드의 "원격 빌드" 는 Binder 가 프로세스 경계를 넘는다는 뜻이며 정식 모드는 `on-device-plugin` 입니다.

**Q: 플러그인 빌드가 실패하면 어떻게 되나요?**

A: AutoJs6 가 요청을 중단하고 조치 가능한 오류를 표시하며 기존 APK 를 보존합니다. 호스트 내부의 두 번째 빌더로 몰래 전환하지 않습니다.

******

### 기술 참고

******

아래 내용은 플러그인 개발자와 통합 담당자를 위한 것으로, 플러그인을 사용하기만 한다면 보통 읽을 필요가 없습니다.

#### Runtime Kit

Runtime Kit 는 AutoJs6 메인 저장소가 빌드하며 독립 실행형 앱 템플릿의 유일한 기준입니다. 본 플러그인은 해당 산출물을 검증하고 동봉할 뿐 `template.apk` 를 생성하지 않습니다. 완전한 Runtime Kit 에는 일반적으로 다음 파일이 포함됩니다:

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

#### 발견 식별자

호스트는 다음 식별자로 본 플러그인을 발견하고 바인딩합니다:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### 로컬 빌드

먼저 AutoJs6 메인 저장소에서 Runtime Kit 를 생성합니다:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

그런 다음 생성된 Runtime Kit 디렉터리를 지정하여 이 저장소를 빌드합니다:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

릴리스된 `autojs6-runtime-kit-*.zip` 을 `runtime-kit/` 에 풀고 바로 빌드할 수도 있습니다:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### 릴리스 흐름

예상되는 프로덕션 릴리스 흐름은 다음과 같습니다:

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

#### 서명

프로덕션 플러그인 릴리스는 신뢰된 AutoJs6 플러그인 서명 키로 서명해야 합니다. GitHub Actions 릴리스에는 다음 저장소 시크릿이 필요합니다:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

로컬 릴리스 빌드는 무시되는 루트 `sign.properties` 파일도 계속 지원합니다:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### 로드맵

******

ROADMAP.md 에서 정식 플러그인 관리 빌드, 후보, ABI 별 배포, 호환성, 보안 증거와 GA 이후 보증을 검증 가능한 목록으로 추적합니다.

- [ROADMAP.md 보기](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### 릴리스 기록

******

# v1.0.3

###### 2026/09/19

* `수정` 생성된 APK의 resources.arsc를 비압축 및 4바이트 정렬로 저장하여 Android 11 이상을 대상으로 하는 앱을 설치할 수 있도록 함
* `수정` 공유 빌드 플러그인 1.8.3을 통해 AGP 9.1의 SDK XML v4 파싱 경고 및 JVM 단위 테스트 조립 작업에서 APK 네이티브 라이브러리 정렬 검사가 잘못 실행되는 문제 해결
* `개선` `enableRemoteBuild` 빌드 스위치를 제거하고 플러그인이 항상 APK 빌드 기능을 선언하도록 함
* `개선` compileSdk 와 targetSdk 를 37 (Android 17) 로 올리며, 플러그인 동작은 새 대상 버전의 영향을 받지 않음

# v1.0.2

###### 2026/09/13

* `수정` 빌드 환경의 언어와 관계없이 플러그인 버전 날짜를 영어로 유지
* `개선` 다국어 리소스 통일, 명시적인 플러그인 활성화 및 릴리스 산출물 검증

# v1.0.1

###### 2026/09/13

* `개선` 다국어 리소스 통일, 명시적인 플러그인 활성화 및 릴리스 산출물 검증

##### 더 많은 릴리스 기록

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-ko.md)

******

### 라이선스

******

본 프로젝트는 Mozilla Public License 2.0 에 따라 공개되며, 해당 조건에 따른 사용, 수정, 배포가 가능합니다.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

******

### 리소스 구조

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` 은 현지화된 플러그인 이름, 설명, 대체 안내를 제공합니다; `plugin_instruction.md` 는 호스트에 표시되는 사용 안내를 제공합니다. README 와 CHANGELOG 는 `.python/generate_markdown.py` 가 JSON 소스에서 생성합니다; 문서를 수정할 때는 생성물이 아니라 JSON 을 편집한 뒤 스크립트를 다시 실행하세요.

******

### 관련 링크

******

- AutoJs6 메인 프로젝트: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 문서: https://docs.autojs6.com


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/docs/16kb.md)
