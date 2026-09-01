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

AutoJs6 의 "앱 패키징" 기능은 스크립트나 프로젝트를 단독으로 설치, 실행되는 APK 로 만들어 줍니다. 대상 기기에 AutoJs6 가 없어도 됩니다. 패키징에는 완전한 스크립트 실행 환경을 내장한 "템플릿 APK" 가 뼈대로 필요합니다. 메인 앱을 가볍게 유지하기 위해 최근 버전의 AutoJs6 는 이 큰 템플릿을 더 이상 내장하지 않고 본 플러그인으로 분리했으며, 패키징 기능이 필요한 사용자만 설치하면 됩니다.

플러그인에는 아이콘도 화면도 없습니다. 모든 동작은 백그라운드에서 AutoJs6 가 자동으로 호출합니다: 패키징 시 AutoJs6 가 플러그인을 발견하고 버전 호환성과 파일 무결성을 검증한 뒤 내장 템플릿 APK 를 읽어 패키징을 마칩니다.

한 줄 판단 기준: AutoJs6 의 "앱 패키징" 기능을 쓴다면 플러그인 센터가 호환성 매트릭스에서 현재 AutoJs6 용으로 선택한 빌드를 설치하세요. 독립 실행형 앱을 패키징하지 않는다면 필요 없습니다.

******

### 동작 방식

******

독립 실행형 앱을 패키징할 때 AutoJs6 와 본 플러그인은 다음처럼 협력합니다:

1. 발견: AutoJs6 가 설치된 템플릿 플러그인을 찾아 메타데이터를 읽습니다
2. 호환성 검사: 버전, 프로토콜 버전, 템플릿 패키지 이름을 비교하고, 불일치하면 경고하거나 패키징을 차단합니다
3. 무결성 검사: 템플릿 APK 의 SHA-256 다이제스트를 대조해 손상되거나 변조된 파일을 걸러냅니다
4. 템플릿 전송: 프로세스 간 파이프로 템플릿 APK 를 스트리밍하며 임시 사본을 만들지 않습니다
5. 패키징: AutoJs6 가 스크립트, 설정, 리소스를 템플릿에 기록해 최종 독립 실행형 APK 를 생성합니다

******

### 기능

******

- AutoJs6 "앱 패키징" 기능에 완전한 독립 실행형 앱 템플릿 (Runtime Kit) 을 제공하며, 설치 후 별도 설정이 필요 없습니다.
- 각 플러그인 빌드는 실제로 빌드한 AutoJs6 호스트를 버전 이름에 유지하고 (예: 1.0.0+autojs6-6.8.0-alpha5), 검증된 패치 닫힌 구간을 명시적으로 선언할 수 있습니다. 정확히 일치하면 알리지 않고, 구간 안의 다른 호스트에는 경고하며, 구간 밖에서는 패키징을 차단합니다.
- 이중 무결성 보호: 플러그인 빌드 시 Runtime Kit 전체 파일의 SHA-256 다이제스트와 필수 항목을 검증하고, 패키징 시 템플릿 다이제스트를 AutoJs6 에 보고해 재검증합니다.
- 템플릿은 프로세스 간 파이프로 AutoJs6 에 스트리밍 전송되며 불필요한 임시 사본을 만들지 않습니다.
- 기본 키 저장소를 내장해 사용자 지정 서명 키를 설정하기 전에도 설치 가능한 APK 를 만들 수 있습니다.
- 실험적 "원격 빌드" 프로토콜을 지원해 플러그인 프로세스가 단독으로 경량 빌드를 수행할 수 있습니다 (기본 비활성; 기능 범위 참조).
- 플러그인 정보, 사용 설명, README, CHANGELOG 는 중국어 간체, 중국어 번체 (홍콩/대만), 영어, 프랑스어, 스페인어, 일본어, 한국어, 러시아어, 아랍어 등 10개 언어를 지원합니다.

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

- 본 플러그인은 단독으로 사용할 수 없습니다: 아이콘과 화면이 없으며 패키징 시 AutoJs6 만 호출합니다.
- 본 플러그인은 템플릿 APK 를 생성하지 않습니다: 템플릿과 Runtime Kit 는 AutoJs6 메인 저장소가 빌드해 배포하며, 본 플러그인은 검증, 동봉, 배포만 담당합니다.
- 본 플러그인은 스크립트 작성이나 일상 실행에 관여하지 않습니다: "앱 패키징" 기능만 이를 읽습니다.
- 원격 빌드는 실험적 기능이며 기본적으로 꺼져 있습니다: 공식 배포 플러그인에서는 활성화되지 않으며, 직접 빌드하면서 명시적으로 켠 경우에만 사용할 수 있습니다.
- 본 플러그인은 버전 요건을 완화하지 않습니다: AutoJs6 와 버전이 다르면 패키징이 차단될 수 있고, 통과하더라도 결과물의 정상 동작은 보장되지 않습니다.

******

### 자주 묻는 질문

******

**Q: 플러그인 센터는 빌드를 어떻게 선택하나요?**

A: 지원되는 AutoJs6 는 자체 versionCode 로 compat-matrix.json 을 조회하고 호환 구간에서 pluginVersionCode 가 가장 높은 빌드를 선택합니다. 그런 다음 기기의 정확한 ABI 를 우선하고 없으면 universal 로 대체합니다. 매트릭스 항목은 allowPatchVersionMismatch=true 를 명시한 경우에만 검증된 패치 구간을 포함할 수 있습니다. 실제 빌드 호스트에서는 경고 없이 패키징하고, 구간 안의 다른 호스트는 같은 빌드를 경고와 함께 재사용하며, 구간 밖의 호스트는 사용할 수 없습니다. 사용할 수 있는 매트릭스 항목이 없으면 기존 Release/태그 경로를 사용합니다. 호환 플러그인 버전이 설치된 버전보다 낮으면 Android 가 제자리 다운그레이드를 할 수 없으므로 플러그인 센터가 먼저 제거한 뒤 호환 빌드를 설치하도록 안내합니다.

**Q: 왜 플러그인이 AutoJs6 버전과 짝을 이루어야 하나요?**

A: 템플릿 APK 에 내장된 스크립트 실행 환경은 AutoJs6 런타임 API 와 엄격하게 대응하므로, 호환성은 플러그인 자체의 시맨틱 버전이 아니라 플러그인이 선언하고 검증된 versionCode 계약으로 판정합니다. 대부분의 빌드는 하나의 정확한 호스트만 대상으로 하지만, 검증된 패치 버전 폐구간을 명시적으로 선언할 수도 있습니다. 이 경우 기준 호스트는 경고 없이 통과하고, 구간 안의 다른 호스트에는 경고가 표시되며, 구간 밖에서는 패키징이 차단됩니다. 플러그인 자체 버전 (예: 1.0.0) 은 독립적으로 발전하며, 버전 이름의 autojs6- 접미사와 릴리스 태그가 짝을 이루는 호스트를 나타냅니다. 구버전 AutoJs6 를 사용할 때는 해당 구버전 태그의 플러그인 빌드를 내려받으세요 (최신 플러그인에서 되돌리려면 먼저 제거해야 합니다. Android 는 다운그레이드 덮어쓰기 설치를 허용하지 않습니다).

**Q: 런처에서 플러그인을 찾을 수 없습니다. 설치에 실패한 건가요?**

A: 아닙니다. 플러그인에는 아이콘과 화면이 없으며 AutoJs6 를 위한 백그라운드 서비스로만 동작합니다. 시스템 "설정 > 애플리케이션" 목록에서 APK Builder Template 를 확인할 수 있습니다.

**Q: 패키징 시 템플릿 검증 실패가 표시됩니다. 어떻게 하나요?**

A: 보통 설치된 플러그인이 불완전하거나 손상된 경우입니다. AutoJs6 플러그인 센터나 이 저장소의 Releases 에서 다시 설치하세요. 문제가 계속되면 Issues 로 알려 주세요.

**Q: 플러그인이 왜 이렇게 큰가요?**

A: 스크립트 엔진과 모든 프로세서 아키텍처용 네이티브 라이브러리를 포함한 완전한 독립 실행형 앱 템플릿을 내장하기 때문입니다. 바로 그것이 템플릿을 AutoJs6 본체에서 분리한 이유입니다: 패키징 기능을 쓰는 사용자만 이 용량을 부담합니다.

**Q: "원격 빌드" 란 무엇인가요?**

A: 플러그인이 자신의 프로세스 안에서 경량 빌드 (템플릿 해제, 스크립트와 설정 기록, 패키지 이름과 리소스 재작성, 재서명) 를 수행하는 실험적 프로토콜입니다. 공식 배포 플러그인에서는 비활성 상태이며, 현재는 직접 빌드하는 개발자를 대상으로 합니다.

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

계획된 작업과 진행 상황은 ROADMAP.md 에 검증 가능한 체크리스트로 관리합니다: 원격 빌드 안정화, 아키텍처별 템플릿 변형, 패치 수준 버전 호환 등. Issues 에서의 논의를 환영합니다.

- [ROADMAP.md 보기](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### 릴리스 기록

******

# v1.0.0

###### 2026/09/01

* `안내` 플러그인 독립 버전 계열의 첫 정식 릴리스이며 AutoJs6 v6.8.0 (versionCode 5277) Runtime Kit와 정확히 대응합니다; 복합 플러그인 버전은 1.0.0+autojs6-6.8.0 (versionCode 527701)이고, 플러그인 센터는 compat-matrix.json에서 대응 ABI 빌드를 선택하며, 원격 빌드는 계속 기본적으로 비활성화됩니다
* `추가` 플러그인 SemVer 1.0.0, 독립 빌드 번호, 복합 버전 이름, 단조 증가하는 Android versionCode를 도입하여 동일한 호스트용 플러그인을 여러 번 릴리스할 수 있도록 변경
* `추가` universal, arm64-v8a, armeabi-v7a, x86_64, x86 변형을 추가하고 정확한 ABI 선택과 universal 대체 경로 지원
* `추가` 실패 시 닫히는 호스트 호환 범위 계약과 권위 있는 호환성 매트릭스를 추가하여 명시적으로 검증된 인접 패치 범위가 하나의 플러그인 빌드를 공유하도록 지원
* `수정` 실험적 원격 단일 파일 빌드 번호를 기존 빌더와 일치시키고, 교차 검증한 압축 해제 입력 크기와 빌드 시 검증한 템플릿 확장 한도 및 256 MiB 예약 공간을 사용하는 실패 차단형 작업 공간 저장소 사전 검사를 추가
* `수정` 기존 내장 Node.js 패키징 메타데이터와 소스 지시어를 BUILD/SIGN 전에 거부하고 외부 Runtime 플러그인으로의 마이그레이션을 안내하며, 더 이상 사용하지 않는 Manifest 서비스 및 포그라운드 권한 삽입을 제거했습니다
* `수정` 실험적 원격 세션에서 닫기와 빌드 스레드 간 경쟁으로 취소 또는 종료 후 삭제된 세션 작업 공간이 다시 생성될 수 있던 문제를 수정했습니다. 이제 작업자 종료를 기다린 뒤 정리하여 잔여 파일을 남기지 않습니다
* `수정` 실험적 원격 빌드를 강화하여 경로 목록에 없는 TypeScript 스테이징 암호문을 거부하고 작업 공간에서 파일 이름이 정규화된 후에도 사용자 지정 BKS 키 저장소를 올바르게 감지
* `수정` 실험적 원격 빌드 입력 경계를 강화하여 Parcelable/Bundle 및 project.json의 형식, 크기, 중첩을 엄격히 검증하고 키 저장소, 아이콘, ZIP 경로 깊이/세그먼트 길이를 제한했으며 ARSC 패키지 이름과 파생 출력 파일 이름의 경계 초과를 수정
* `수정` 일부 시스템에서 설치 후 플러그인 센터를 통해 플러그인을 활성화할 수 없는 문제
* `개선` 신뢰 릴리스 워크플로에 후보 격리 모드를 추가하여 고정된 호스트 Actions 아티팩트에서 프로덕션 서명된 5개 APK와 evidence를 생성하면서 Release 생성과 권위 있는 호환성 매트릭스 갱신은 수행하지 않도록 개선
* `개선` Gradle과 Python의 Runtime Kit 검증 규칙을 통합하여 해시, 크기, 필수 파일, APK 항목, 다섯 변형의 일관성 확인
* `개선` 5개 APK와 함께 기계 판독 가능한 JSON 증거 매니페스트를 게시하여 자산 다이제스트, 서명 인증서, 플러그인/호스트 버전, 호환 범위, Runtime Kit ID 및 프로토콜 버전을 결합
* `개선` 호환 버전, ABI 선택, 다운그레이드 복구, 독립 버전 체계를 설명하도록 설치 안내, FAQ, 릴리스 예행연습, 10개 언어 문서 업데이트
* `개선` README 레이아웃과 Gradle 플랫폼 버전 관리 방식을 통일

# v6.8.0 Alpha5

###### 2026/07/16

* `안내` AutoJs6 v6.8.0 Alpha5 와 짝을 이룹니다. 지원되는 플러그인 센터는 짝이 맞는 빌드를 자동으로 찾고, 수동 설치 시에는 일치하는 Release 태그 또는 autojs6- 접미사를 사용합니다. 플러그인에는 아이콘과 화면이 없으며 앱 패키징 시 자동으로 호출됩니다
* `추가` AutoJs6 가 플러그인을 자동 발견하고 내장 템플릿을 읽을 수 있게 하여, "앱 패키징" 기능이 본체에 내장된 템플릿 APK 에 의존하지 않게 했습니다
* `추가` 완전한 Runtime Kit 동봉: 템플릿 APK, 기본 키 저장소, 런타임 메타데이터, 계약 파일
* `추가` 패키징 전에 버전과 프로토콜 호환성 검사를 자동 수행하고, 불일치 시 명확히 경고하거나 차단하여 동작하지 않는 앱 생성을 방지합니다
* `추가` 플러그인 빌드 시 Runtime Kit 의 SHA-256 다이제스트와 필수 항목을 검증하고, 실행 시 템플릿 다이제스트를 AutoJs6 에 보고해 재검증합니다
* `추가` 플러그인 프로세스 안에서 경량 빌드를 수행하는 실험적 원격 빌드 프로토콜을 도입했습니다 (기본 비활성, 빌드 시 명시적으로 켜야 함)
* `추가` 자동 릴리스 흐름 구축: AutoJs6 메인 저장소가 릴리스를 발행하면 짝을 이루는 플러그인 APK 를 자동으로 빌드하고, 신뢰된 키로 서명하며 인증서 지문을 검증한 뒤 배포합니다
* `추가` 플러그인 정보, 사용 설명, README, CHANGELOG 를 10개 언어 (중국어 간체, 중국어 번체 (홍콩/대만), 영어, 프랑스어, 스페인어, 일본어, 한국어, 러시아어, 아랍어) 로 제공합니다

# v6.7.1 Alpha4

###### 2026/07/09

* `안내` 첫 공개 릴리스이며, 같은 버전의 AutoJs6 (v6.7.1 Alpha4) 와 함께 사용해야 합니다
* `추가` AutoJs6 메인 저장소에서 독립 플러그인 저장소로 분리하고 템플릿 APK 플러그인 서비스의 초기 구현을 제공했습니다
* `추가` AutoJs6 메인 저장소가 트리거하는 Runtime Kit 획득, 검증, 빌드, 배포 파이프라인을 구축했습니다

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
