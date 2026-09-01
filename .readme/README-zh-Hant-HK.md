<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>為 AutoJs6 "打包應用" 功能提供獨立應用模板的插件</p>

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
- 繁體中文 (香港) [zh-Hant-HK] # 目前
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
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

AutoJs6 的 "打包應用" 功能可把腳本或項目生成能獨立安裝和運行的 APK. 為保持主程式精簡, 體積較大的模板與全部打包核心都由本插件提供.

本插件沒有圖示和介面. AutoJs6 負責發現與校驗插件, 準備有界請求並顯示進度; 插件負責展開自身模板, 寫入項目與資源, 修改 Manifest/resources, 選擇 ABI, 管理簽名並返回候選 APK; AutoJs6 最後獨立覆核輸出再發布.

整個過程都在同一台 Android 裝置上透過 Binder 與檔案描述符完成, 不會把項目原始碼上傳到網絡或雲端構建服務.

******

### 工作原理

******

打包獨立應用時, AutoJs6 與本插件按以下步驟協作:

1. 准入: AutoJs6 校驗官方簽名, 啟用狀態, 宿主版本區間, ABI, 正式構建能力, 協議與裝置內執行模式
2. 準備請求: AutoJs6 生成有大小邊界的項目/原生庫/簽名庫輸入, 並固定預期包身份與簽名者
3. 插件構建: 插件再次校驗請求, 展開自身 Runtime Kit 模板, 寫入項目, 修改 Manifest/resources, 裁剪 ABI 並簽名
4. 返回結果: 插件透過唯讀檔案描述符返回候選 APK, 並清理私有工作區
5. 宿主發布: AutoJs6 重查大小, SHA-256, APK 結構, 簽名, 簽名者, 包名與版本, 全部通過後才原子替換目標

******

### 功能

******

- 完整擁有 AutoJs6 的裝置內打包核心, 包括模板處理, 項目/資源寫入, Manifest 與 resources.arsc 修改, ABI 選擇, 簽名庫操作和簽名.
- 保持 AutoJs6 精簡: 宿主只負責 UI, 信任/兼容准入, 請求準備, 取消/進度和輸出獨立覆核, 不保留第二套構建器.
- 全部在同一台 Android 裝置上透過 Binder/AIDL 與 ParcelFileDescriptor 執行, 不上傳項目原始碼到網絡或雲端.
- 每個插件構建都與經過校驗的 AutoJs6 Runtime Kit 配對, 並可聲明經顯式驗證的補丁版本閉區間.
- 提供 universal, arm64-v8a, armeabi-v7a, x86_64 與 x86 變體, 支援精確 ABI 選擇及 universal 回退.
- 內置預設簽名庫, 並由插件建立/驗證 BKS/JKS, 同時繼續支援自訂簽名庫.
- 插件資訊, 使用說明, README 與 CHANGELOG 覆蓋 10 種語言.

******

### 快速上手

******

- **怎麼裝**: 建議從 AutoJs6 插件中心下載安裝: 支援該機制的宿主會讀取兼容矩陣, 自動選擇配套插件版本與當前設備的精確 ABI 資產, 缺失時回退 universal. 手動安裝時, 請前往本倉庫 [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) 頁面, 根據與 AutoJs6 同名的發布標籤或插件版本名中的 autojs6- 後綴確認配套宿主 (例如插件 v1.0.0+autojs6-6.8.0-alpha5 配套 AutoJs6 v6.8.0 Alpha5). 若插件中心選出的配套版本低於已安裝版本, 請按提示先卸載再安裝; Android 不支援降級覆蓋安裝.
- **怎麼用**: 無需任何額外操作. 在 AutoJs6 中像往常一樣使用 "打包應用" 功能, 打包過程會自動發現並使用本插件提供的模板.
- **怎麼確認已生效**: 未安裝 (或版本不匹配) 時, AutoJs6 的打包入口會提示先安裝或啟用本插件; 安裝匹配版本後提示消失, 即表示插件已被正常識別. 插件本身沒有圖示與介面, 桌面上找不到它屬於正常現象.
- **出錯了看哪裏**: 提示兼容性警告時, 請使用插件中心按兼容矩陣選出的構建, 或確認目前宿主位於插件聲明區間內; 提示版本不兼容並阻止打包時, 請安裝矩陣匹配的構建; 提示模板損壞或校驗失敗時, 從官方渠道重新下載安裝插件; 其他問題可攜帶 AutoJs6 日誌與復現步驟到 [Issues](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) 反饋.

******

### 能力邊界

******

為避免誤解, 以下事項明確不屬於本插件的功能範圍:

- 本插件不能獨立使用: 它沒有圖示和介面, 只能由兼容的 AutoJs6 宿主調用.
- 裝置內構建不是雲端構建: 本協議不會上傳項目原始碼.
- AutoJs6 不再保留第二套進程內打包核心. 插件缺失, 被停用, 不受信任, 不兼容或構建失敗時, 本次請求停止並保留舊產物.
- Runtime Kit 仍由 AutoJs6 倉庫生成; 插件負責校驗, 封裝, 分發和使用該套件, 不自行創造運行時模板.
- 舊 "遠程構建" 能力繼續為舊宿主保持關閉. 該名稱只表示另一個裝置內應用進程, 不是互聯網服務, 且與正式插件託管能力分離.

******

### 常見問題

******

**問: 插件中心如何選擇構建?**

答: 支援該機制的 AutoJs6 會用自身 versionCode 查詢 compat-matrix.json, 選擇兼容區間內 pluginVersionCode 最高的構建, 再優先選擇當前設備的精確 ABI 資產並在缺失時回退 universal. 只有顯式設定 allowPatchVersionMismatch=true 時, 矩陣條目才可覆蓋已驗證的補丁區間: 實際構建所用宿主可無提示打包, 區間內其他宿主重用同一構建時會收到警告, 區間外宿主不能使用該條目. 若矩陣沒有可用條目, 仍回退現有 Release/標籤通道. 若配套插件版本低於已安裝版本, 插件中心會提示先卸載再安裝配套構建; Android 無法執行覆蓋降級安裝.

**問: 為什麼插件必須與 AutoJs6 版本配套?**

答: 模板內的運行時必須匹配宿主 API. 插件中心會從兼容矩陣選擇最高兼容插件版本與最合適的 ABI 資產; 區間外宿主會被阻止.

**問: 桌面上找不到插件, 是安裝失敗嗎?**

答: 不是. 插件特意不提供圖示和介面, 只作為 AutoJs6 的後台服務運行; 可在系統 設定 > 應用程式 中確認.

**問: 項目會發送到遠程伺服器嗎?**

答: 不會. 宿主與插件只在同一台 Android 裝置的兩個應用進程之間通信. 歷史源碼稱 "遠程構建" 是因為 Binder 調用跨進程; 正式模式明確為 `on-device-plugin`.

**問: 插件構建失敗會怎樣?**

答: AutoJs6 會停止請求, 顯示可行動錯誤並保留已有輸出 APK, 不會靜默切換到宿主內第二套構建器.

******

### 技術參考

******

以下內容面向插件開發者與整合方; 僅使用插件時通常無需閱讀.

#### Runtime Kit

Runtime Kit (運行時套件) 由 AutoJs6 主倉庫構建, 是獨立應用模板的唯一來源. 本插件只校驗並封裝該產物, 不生成 `template.apk`. 一個完整的 Runtime Kit 通常包含以下文件:

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

#### 插件發現標識

宿主透過以下標識發現並綁定本插件:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### 本地構建

先在 AutoJs6 主倉庫生成 Runtime Kit:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

再在本倉庫指定 Runtime Kit 目錄構建插件:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

也可以把發布的 `autojs6-runtime-kit-*.zip` 解壓到 `runtime-kit/`, 然後直接構建:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### 發布流程

生產發布流程如下:

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

#### 簽名

生產插件必須使用受信任的 AutoJs6 插件簽名密鑰. GitHub Actions 發布需要以下倉庫密鑰:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

本地發布構建仍支援被忽略的根目錄 `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### 開發路線圖

******

ROADMAP.md 以可核查清單跟蹤正式插件託管構建, 發布候選, 分 ABI 交付, 兼容性, 安全證據與 GA 後保證. 歡迎透過 Issues 參與討論.

- [查看 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### 發行歷史

******

# v1.0.0

###### 2026/09/02

* `提示` 普通打包應用現正式依賴裝置內 APK Builder 插件; 舊 supportsRemoteBuild 開關繼續關閉, 但不再關閉普通打包
* `提示` 外掛程式獨立版本線的首個正式發佈, 精確配套 AutoJs6 v6.8.0 (versionCode 5277) Runtime Kit; 外掛程式複合版本為 1.0.0+autojs6-6.8.0 (versionCode 527701), 外掛程式中心透過 compat-matrix.json 選擇配套 ABI 構建, 遠端構建仍預設關閉
* `新增` 將插件側構建器提升為唯一正式裝置內打包路徑; AutoJs6 保持精簡並獨立覆核每個返回 APK
* `新增` 透過帶版本且失敗關閉的簽名庫 API, 將 BKS/JKS 建立與驗證完整遷入插件
* `新增` 引入外掛程式 SemVer 1.0.0, 獨立構建號, 複合版本名稱與單調遞增的 Android versionCode, 支援同一宿主發佈多個外掛程式構建
* `新增` 新增 universal, arm64-v8a, armeabi-v7a, x86_64 與 x86 變體, 支援精確 ABI 選擇及 universal 後備
* `新增` 新增失敗關閉的宿主兼容區間契約與權威兼容矩陣, 讓經過明確驗證的相鄰修補區間可共用一個外掛程式構建
* `修復` 使實驗性遠程單檔案構建編號與舊構建器保持一致，並新增失敗關閉的工作區空間預檢：交叉核對輸入解壓大小、使用構建期驗證的範本展開上限，並保留 256 MiB 空間
* `修復` 在進入 BUILD/SIGN 前拒絕舊版內嵌 Node.js 封裝中繼資料及原始碼指令並提示遷移至外部 Runtime 外掛程式，同時移除已棄用的 Manifest 服務及前台權限注入
* `修復` 修正實驗性遠端工作階段中關閉與建置執行緒的競態: 取消或關閉後, 工作執行緒可能重新建立已刪除的工作階段工作區; 現時會等待工作執行緒結束再清理, 最終保持零殘留
* `修復` 加強實驗性遠端構建: 拒絕路徑清單未聲明的 TypeScript 暫存密文, 並在工作區規範化檔案名稱後正確識別自訂 BKS 簽名庫
* `修復` 收緊實驗性遠端構建輸入邊界: 嚴格驗證 Parcelable/Bundle 與 project.json 的類型, 大小及巢狀深度, 限制金鑰庫, 圖示及 ZIP 路徑深度/段長, 並修正 ARSC 套件名稱和衍生輸出檔名越界
* `修復` 部分系統安裝後無法透過插件中心激活的問題
* `優化` 受信發佈工作流程新增候選隔離模式, 從綁定的宿主 Actions 構件產生五個正式簽署 APK 與 evidence, 但不建立 Release 或寫入權威相容矩陣
* `優化` 統一 Gradle 與 Python 的 Runtime Kit 驗證規則, 涵蓋摘要, 大小, 必需檔案, APK 項目及五變體一致性
* `優化` 發佈流水線隨五個 APK 發佈機器可讀的 JSON 證據清單, 綁定資產摘要, 簽署憑證, 外掛程式/宿主版本, 相容區間, Runtime Kit ID 與協定版本
* `優化` 更新安裝說明, FAQ, 發佈演練與 10 種語言文件, 說明配套版本, ABI 選擇, 降級復原及獨立版本機制
* `優化` 統一 README 版式與 Gradle 平台版本管理方式

# v6.8.0 Alpha5

###### 2026/07/16

* `提示` 配套 AutoJs6 v6.8.0 Alpha5; 支援該機制的插件中心會自動解析配套構建, 手動安裝時按 Release 標籤或 autojs6- 後綴確認; 插件沒有圖示與介面, 由 AutoJs6 在打包應用時自動調用
* `新增` 支援被 AutoJs6 自動發現並讀取內置模板, "打包應用" 功能不再依賴主程式內置的模板 APK
* `新增` 內置完整 Runtime Kit: 模板 APK, 預設簽名庫, 運行時元數據與契約文件
* `新增` 打包前自動進行版本與協議兼容性檢查, 不匹配時明確警告或阻止, 避免產出無法運行的應用
* `新增` 構建插件時校驗 Runtime Kit 的 SHA-256 摘要與模板必需項目, 運行時向 AutoJs6 上報模板摘要供覆核
* `新增` 實驗性遠程構建協議: 可由插件進程獨立完成輕量打包 (預設關閉, 需構建時顯式開啟)
* `新增` 接入自動化發布流程: AutoJs6 主倉庫發版時自動構建插件, 使用受信任密鑰簽名並校驗證書指紋後發布配套 APK
* `新增` 插件資訊, 使用說明, README 與 CHANGELOG 覆蓋簡體中文, 香港繁體, 台灣繁體, 英語, 法語, 西班牙語, 日語, 韓語, 俄語與阿拉伯語共 10 種語言

# v6.7.1 Alpha4

###### 2026/07/09

* `提示` 首個公開發布版本, 需搭配同版本 AutoJs6 (v6.7.1 Alpha4) 使用
* `新增` 從 AutoJs6 主倉庫拆分為獨立插件倉庫, 提供模板 APK 插件服務的初始實現
* `新增` 建立由 AutoJs6 主倉庫觸發的 Runtime Kit 獲取, 校驗與插件構建發布流水線

##### 更多發行歷史可參閱

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hant-HK.md)

******

### 許可證

******

本項目基於 Mozilla Public License 2.0 開源, 允許在遵循該協議的前提下使用, 修改與分發.

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

`strings.xml` 提供插件名稱, 描述和兜底說明的本地化; `plugin_instruction.md` 提供宿主側展示的插件使用說明. README 與 CHANGELOG 由 `.python/generate_markdown.py` 根據 JSON 語料生成; 修改文檔請編輯對應 JSON 後重新運行腳本, 不要直接編輯生成產物.

******

### 相關連結

******

- AutoJs6 主項目: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 文件: https://docs.autojs6.com
