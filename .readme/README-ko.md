<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
  </p>

  <p>AutoJs6 독립 실행형 앱 패키징용 템플릿 APK 플러그인</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/commit/dc4627c752f8c1f709b302651a7de57992eac2b5"><img alt="Created" src="https://img.shields.io/date/1784208939?color=2e7d32&label=Created"/></a>
    <br>
    <a href="https://developer.android.com/studio/archive"><img alt="Android Studio" src="https://img.shields.io/badge/Android%20Studio-2023.3+-B64FC8"/></a>
    <a href="https://www.jetbrains.com/idea/download/other.html"><img alt="IntelliJ IDEA" src="https://img.shields.io/badge/IntelliJ%20IDEA-2023.3+-EE4677"/></a>
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

AutoJs6 APK Builder Template 플러그인은 AutoJs6가 독립 실행형 앱을 패키징할 때 사용하는 외부 템플릿 APK와 Runtime Kit를 제공합니다. 호스트는 플러그인 서비스에서 템플릿 APK를 읽고 버전 및 프로토콜 메타데이터로 호환성을 확인합니다.

******

### 기능

******

- `autojs6-apk-builder-template` 플러그인 서비스를 제공하며 플러그인 ID는 `autojs6-apk-builder-template`, 엔진은 `apk-builder-template` 입니다.
- `org.autojs.plugin.INFO` 로 공통 플러그인 정보를 노출하고 `org.autojs.plugin.APK_BUILDER` 로 템플릿 APK를 제공합니다.
- 빌드 중 Runtime Kit SHA-256 다이제스트와 `template.apk` 필수 항목을 검증합니다.
- `assets/runtime-kit/` 아래에 `template.apk`, 기본 키 저장소, 런타임 메타데이터, 계약 파일을 패키징합니다.
- 호스트 버전, 프로토콜 버전, 템플릿 패키지 이름, 템플릿 다이제스트, 원격 빌드 기능 메타데이터를 보고합니다.
- 플러그인 메타데이터, 사용 설명, README, CHANGELOG 는 스페인어/프랑스어/러시아어/아랍어/일본어/한국어/영어/중국어 간체/홍콩 번체/대만 번체를 지원합니다.

******

### Runtime Kit

******

Runtime Kit는 AutoJs6 메인 저장소에서 제공되며 독립 실행형 앱 템플릿의 유일한 기준입니다. 이 플러그인은 해당 산출물을 검증하고 패키징할 뿐이며 `template.apk` 를 생성하지 않습니다. 완전한 Runtime Kit에는 일반적으로 다음 파일이 포함됩니다

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

******

### 로컬 빌드

******

먼저 AutoJs6 메인 저장소에서 Runtime Kit를 생성합니다:

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

******

### 릴리스 흐름

******

예상되는 프로덕션 릴리스 흐름은 다음과 같습니다:

```text
AutoJs6 tag
-> main repository generates autojs6-runtime-kit-*.zip
-> main repository uploads the Runtime Kit to its GitHub Release
-> main repository dispatches SuperMonster003/AutoJs6-Plugin-APK-Builder-Template
-> this repository downloads and verifies the Runtime Kit
-> this repository builds the plugin APK
-> this repository uploads the plugin APK to the same tag Release
-> AutoJs6 Plugin Center installs this plugin
```

******

### 서명

******

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

### 릴리스 기록

******

# v6.8.0 Alpha5

###### 2026/07/16

* `추가` 플러그인 ID `autojs6-apk-builder-template`, 엔진 `apk-builder-template`, 변형 `inrt-universal` 인 APK Builder Template 플러그인 서비스를 추가했습니다
* `추가` `org.autojs.plugin.INFO` 로 플러그인 메타데이터를 노출하고 `org.autojs.plugin.APK_BUILDER` 로 템플릿 APK를 제공했습니다
* `추가` `template.apk`, 기본 키 저장소, 메타데이터, 계약 파일을 포함한 AutoJs6 Runtime Kit를 `assets/runtime-kit/` 아래에 패키징했습니다
* `추가` Runtime Kit 메타데이터, SHA-256 다이제스트, `template.apk` 필수 항목에 대한 빌드 시 검증을 추가했습니다
* `추가` 호스트 버전, 프로토콜 버전, 템플릿 패키지 이름, 템플릿 SHA-256, Runtime API 다이제스트, 원격 빌드 기능을 보고했습니다
* `추가` `autojs.apkBuilder.templatePlugin.enableRemoteBuild` 로 선택적으로 켤 수 있는 실험적 원격 빌드 프로토콜 지원을 추가했습니다
* `추가` Runtime Kit 다운로드, asset 검증, 신뢰된 키 서명, universal APK 업로드를 위한 릴리스 흐름 지원을 추가했습니다
* `추가` 플러그인 메타데이터, 사용 설명, README, CHANGELOG 에 스페인어/프랑스어/러시아어/아랍어/일본어/한국어/영어/중국어 간체/홍콩 번체/대만 번체 다국어 리소스를 추가했습니다

##### 더 많은 릴리스 기록

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.changelog/CHANGELOG-ko.md)

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

`strings.xml` 은 현지화된 플러그인 이름, 설명, 대체 안내를 제공합니다; `plugin_instruction.md` 는 호스트에 표시되는 사용 안내를 제공합니다. README 와 CHANGELOG 는 `.python/generate_markdown.py` 가 JSON 소스에서 생성합니다.

******

### 관련 링크

******

- AutoJs6 메인 프로젝트: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 문서: https://docs.autojs6.com
