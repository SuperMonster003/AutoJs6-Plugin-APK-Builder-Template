<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>為 AutoJs6 "封裝應用程式" 功能提供獨立應用程式範本的外掛</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 語言 (Languages)

******

目前 README.md 支援以下語言:

- [简体中文 [zh-Hans]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hans.md)
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- 繁體中文 (台灣) [zh-Hant-TW] # 目前
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### 簡介

******

AutoJs6 的 "封裝應用程式" 功能可把指令碼或專案產生為能獨立安裝和執行的 APK. 為保持主程式精簡, 體積較大的範本與全部封裝核心都由本外掛提供.

本外掛沒有圖示和介面. AutoJs6 負責發現與驗證外掛, 準備有界請求並顯示進度; 外掛負責展開自身範本, 寫入專案與資源, 修改 Manifest/resources, 選擇 ABI, 管理簽章並傳回候選 APK; AutoJs6 最後獨立複核輸出再發布.

整個過程都在同一台 Android 裝置上透過 Binder 與檔案描述元完成, 不會把專案原始碼上傳到網路或雲端建置服務.

******

### 運作原理

******

封裝獨立應用程式時, AutoJs6 與本外掛按以下步驟協作:

1. 准入: AutoJs6 驗證官方簽章, 啟用狀態, 宿主版本區間, ABI, 正式建置能力, 協定與裝置內執行模式
2. 準備請求: AutoJs6 產生有大小邊界的專案/原生程式庫/簽章庫輸入, 並固定預期套件身分與簽署者
3. 外掛建置: 外掛再次驗證請求, 展開自身 Runtime Kit 範本, 寫入專案, 修改 Manifest/resources, 裁剪 ABI 並簽章
4. 傳回結果: 外掛透過唯讀檔案描述元傳回候選 APK, 並清理私有工作區
5. 宿主發布: AutoJs6 重查大小, SHA-256, APK 結構, 簽章, 簽署者, 套件名稱與版本, 全部通過後才原子替換目標

******

### 功能

******

- 完整擁有 AutoJs6 的裝置內封裝核心, 包括範本處理, 專案/資源寫入, Manifest 與 resources.arsc 修改, ABI 選擇, 簽章庫操作和簽章.
- 保持 AutoJs6 精簡: 宿主只負責 UI, 信任/相容准入, 請求準備, 取消/進度和輸出獨立複核, 不保留第二套建置器.
- 全部在同一台 Android 裝置上透過 Binder/AIDL 與 ParcelFileDescriptor 執行, 不上傳專案原始碼到網路或雲端.
- 每個外掛建置都與經過驗證的 AutoJs6 Runtime Kit 配對, 並可宣告經明確驗證的修補版本閉區間.
- 提供 universal, arm64-v8a, armeabi-v7a, x86_64 與 x86 變體, 支援精確 ABI 選擇及 universal 回退.
- 內建預設簽章庫, 並由外掛建立/驗證 BKS/JKS, 同時繼續支援自訂簽章庫.
- 外掛資訊, 使用說明, README 與 CHANGELOG 覆蓋 10 種語言.

******

### 快速上手

******

- **怎麼裝**: 建議從 AutoJs6 外掛中心下載安裝: 支援此機制的宿主會讀取相容性矩陣, 自動選擇搭配的外掛版本與目前裝置的精確 ABI 資產, 缺少時回退 universal. 手動安裝時, 請前往本儲存庫 [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) 頁面, 根據與 AutoJs6 同名的發布標籤或外掛版本名稱中的 autojs6- 後綴確認搭配的宿主 (例如外掛 v1.0.0+autojs6-6.8.0-alpha5 搭配 AutoJs6 v6.8.0 Alpha5). 若外掛中心選出的搭配版本低於已安裝版本, 請依提示先解除安裝再重新安裝; Android 不支援降級覆蓋安裝.
- **怎麼用**: 無需任何額外操作. 在 AutoJs6 中像往常一樣使用 "封裝應用程式" 功能, 封裝過程會自動發現並使用本外掛提供的範本.
- **怎麼確認已生效**: 未安裝 (或版本不相符) 時, AutoJs6 的封裝入口會提示先安裝或啟用本外掛; 安裝相符版本後提示消失, 即表示外掛已被正常識別. 外掛本身沒有圖示與介面, 桌面上找不到它屬於正常現象.
- **出錯了看哪裡**: 提示相容性警告時, 請使用外掛中心按相容矩陣選出的建置, 或確認目前宿主位於外掛宣告區間內; 提示版本不相容並阻止封裝時, 請安裝矩陣匹配的建置; 提示範本損壞或驗證失敗時, 從官方管道重新下載安裝外掛; 其他問題可攜帶 AutoJs6 記錄與重現步驟到 [Issues](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) 回報.

******

### 能力邊界

******

為避免誤解, 以下事項明確不屬於本外掛的功能範圍:

- 本外掛不能獨立使用: 它沒有圖示和介面, 只能由相容的 AutoJs6 宿主呼叫.
- 裝置內建置不是雲端建置: 本協定不會上傳專案原始碼.
- AutoJs6 不再保留第二套處理程序內封裝核心. 外掛缺失, 被停用, 不受信任, 不相容或建置失敗時, 本次請求停止並保留舊產物.
- Runtime Kit 仍由 AutoJs6 儲存庫產生; 外掛負責驗證, 封裝, 發布和使用該套件, 不自行創造執行階段範本.
- 舊 "遠端建置" 能力繼續為舊宿主保持關閉. 該名稱只表示另一個裝置內應用程式處理程序, 不是網際網路服務, 且與正式外掛託管能力分離.

******

### 常見問題

******

**問: 外掛中心如何選擇建置?**

答: 支援此機制的 AutoJs6 會用自身 versionCode 查詢 compat-matrix.json, 選擇相容區間內 pluginVersionCode 最高的建置, 再優先選擇目前裝置的精確 ABI 資產並在缺少時回退 universal. 只有明確設定 allowPatchVersionMismatch=true 時, 矩陣項目才能涵蓋已驗證的修補區間: 實際建置所用宿主可不提示直接封裝, 區間內其他宿主重用同一建置時會收到警告, 區間外宿主不能使用該項目. 若矩陣沒有可用項目, 仍回退現有 Release/標籤通道. 若搭配的外掛版本低於已安裝版本, 外掛中心會提示先解除安裝再安裝搭配建置; Android 無法執行覆蓋降級安裝.

**問: 為什麼外掛必須與 AutoJs6 版本搭配?**

答: 範本內的執行階段必須符合宿主 API. 外掛中心會從相容矩陣選擇最高相容外掛版本與最合適的 ABI 資產; 區間外宿主會被阻止.

**問: 桌面上找不到外掛, 是安裝失敗嗎?**

答: 不是. 外掛特意不提供圖示和介面, 只作為 AutoJs6 的背景服務執行; 可在系統 設定 > 應用程式 中確認.

**問: 專案會傳送到遠端伺服器嗎?**

答: 不會. 宿主與外掛只在同一台 Android 裝置的兩個應用程式處理程序之間通訊. 歷史原始碼稱 "遠端建置" 是因為 Binder 呼叫跨處理程序; 正式模式明確為 `on-device-plugin`.

**問: 外掛建置失敗會怎樣?**

答: AutoJs6 會停止請求, 顯示可行動錯誤並保留已有輸出 APK, 不會靜默切換到宿主內第二套建置器.

******

### 技術參考

******

以下內容面向外掛開發者與整合方; 僅使用外掛時通常無需閱讀.

#### Runtime Kit

Runtime Kit (執行階段套件) 由 AutoJs6 主儲存庫建置, 是獨立應用程式範本的唯一來源. 本外掛只驗證並封裝該產物, 不產生 `template.apk`. 一個完整的 Runtime Kit 通常包含以下檔案:

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

#### 外掛發現識別碼

宿主透過以下識別碼發現並繫結本外掛:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### 本機建置

先在 AutoJs6 主儲存庫產生 Runtime Kit:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

再在本儲存庫指定 Runtime Kit 目錄建置外掛:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

也可以將發布的 `autojs6-runtime-kit-*.zip` 解壓縮到 `runtime-kit/`, 然後直接建置:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### 發布流程

正式發布流程如下:

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

#### 簽章

正式外掛必須使用受信任的 AutoJs6 外掛簽章金鑰. GitHub Actions 發布需要以下儲存庫密鑰:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

本機發布建置仍支援被忽略的根目錄 `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### 開發路線圖

******

ROADMAP.md 以可核查清單追蹤正式外掛託管建置, 發布候選, 分 ABI 交付, 相容性, 安全證據與 GA 後保證. 歡迎透過 Issues 參與討論.

- [查看 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### 發行歷史

******

# v1.0.3

###### 2026/09/20

* `修復` 產生的 APK 以未壓縮且 4 位元組對齊的方式存放 resources.arsc, 使目標為 Android 11 及以上的應用程式可以安裝
* `修復` AGP 9.1 建置時的 SDK XML v4 解析警告及 JVM 單元測試組裝工作誤觸發 APK 原生程式庫對齊檢查的問題 (共用建置外掛 1.8.3)
* `優化` 移除 `enableRemoteBuild` 建置開關, 外掛程式始終宣告 APK 建置能力
* `優化` 將 compileSdk 與 targetSdk 提升到 37 (Android 17), 外掛程式行為不受新目標版本影響
* `優化` 基於 AutoJs6 6.8.0 組建 5282 重新產生完整的五種 ABI Runtime Kit 產物, 明確配套主程式相容範圍

# v1.0.2

###### 2026/09/13

* `修復` 外掛版本日期固定使用英文, 不隨建置機器的語言變化
* `優化` 統一多語言資源, 明確外掛啟用契約並驗證發行產物

# v1.0.1

###### 2026/09/13

* `優化` 統一多語言資源, 明確外掛啟用契約並驗證發行產物

##### 更多發行歷史可參閱

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-TW.md)

******

### 授權條款

******

本專案基於 Mozilla Public License 2.0 開源, 允許在遵循該授權條款的前提下使用, 修改與散布.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

******

### 資源結構

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` 提供外掛名稱, 描述和備援說明的本地化; `plugin_instruction.md` 提供宿主側展示的外掛使用說明. README 與 CHANGELOG 由 `.python/generate_markdown.py` 根據 JSON 語料產生; 修改文件請編輯對應 JSON 後重新執行指令碼, 不要直接編輯產生的檔案.

******

### 相關連結

******

- AutoJs6 主專案: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 文件: https://docs.autojs6.com


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/docs/16kb.md)
