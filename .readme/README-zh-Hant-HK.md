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

AutoJs6 的 "打包應用" 功能可以把腳本或項目打包成一個可獨立安裝, 獨立運行的 APK, 無需在目標設備上安裝 AutoJs6. 打包時需要一個內置完整腳本運行環境的 "模板 APK" 作為骨架. 為了給主程式 "瘦身", 較新版本的 AutoJs6 不再內置這個體積較大的模板, 而是將它拆分到本插件中, 由需要打包功能的用戶按需安裝.

本插件安裝後沒有圖示, 也沒有任何介面, 全部工作都在後台由 AutoJs6 自動調用: 打包時, AutoJs6 發現插件, 校驗版本與文件完整性, 然後讀取插件內置的模板 APK 完成打包.

一句話判斷是否需要安裝: 會用到 AutoJs6 的 "打包應用" 功能, 就安裝插件中心按兼容矩陣為目前 AutoJs6 選出的構建; 從不打包獨立應用則無需安裝.

******

### 工作原理

******

打包一個獨立應用時, AutoJs6 與本插件的協作過程如下:

1. 發現插件: AutoJs6 在系統中查找已安裝的模板插件並讀取其資訊
2. 兼容檢查: 比對插件與 AutoJs6 的版本, 協議版本與模板包名, 不匹配時給出警告或阻止打包
3. 完整性校驗: 核對模板 APK 的 SHA-256 摘要, 確保文件未損壞, 未被篡改
4. 傳輸模板: 透過進程間管道流式讀取模板 APK, 不產生臨時副本
5. 完成打包: AutoJs6 將腳本, 配置與資源寫入模板, 生成最終的獨立 APK

******

### 功能

******

- 為 AutoJs6 的 "打包應用" 功能提供完整的獨立應用模板 (Runtime Kit), 安裝後無需任何配置即可使用.
- 每個插件構建都會在版本名中保留一個實際構建所用的 AutoJs6 宿主 (如 1.0.0+autojs6-6.8.0-alpha5), 並可顯式聲明經驗證的補丁級閉區間; 精確匹配不提示, 區間內非精確宿主會警告, 區間外則阻止打包.
- 雙重完整性保障: 構建插件時校驗 Runtime Kit 全部文件的 SHA-256 摘要與模板必需項目, 打包時再向 AutoJs6 上報模板摘要供覆核.
- 模板透過進程間管道流式傳輸給 AutoJs6, 不產生冗餘的臨時拷貝.
- 內置預設簽名庫, 未配置自訂簽名時也能直接打出可安裝的 APK.
- 支援實驗性 "遠程構建" 協議, 可由插件進程獨立完成輕量打包 (預設關閉, 詳見能力邊界).
- 插件資訊, 使用說明, README 與 CHANGELOG 覆蓋簡體中文, 香港繁體, 台灣繁體, 英語, 法語, 西班牙語, 日語, 韓語, 俄語與阿拉伯語共 10 種語言.

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

- 本插件不能獨立使用: 它沒有圖示與介面, 僅供 AutoJs6 在打包時調用.
- 本插件不生成模板 APK: 模板與 Runtime Kit 由 AutoJs6 主倉庫構建發布, 本插件只負責校驗, 封裝與分發.
- 本插件不參與腳本的編寫與日常運行: 只有 "打包應用" 功能會讀取它.
- 遠程構建是實驗性能力且預設關閉: 官方發布的插件不啟用該能力, 僅在自行構建並顯式開啟時可用.
- 插件不放寬版本要求: 與 AutoJs6 版本不一致時打包可能被阻止; 即使勉強通過, 產物也不保證正常運行.

******

### 常見問題

******

**問: 插件中心如何選擇構建?**

答: 支援該機制的 AutoJs6 會用自身 versionCode 查詢 compat-matrix.json, 選擇兼容區間內 pluginVersionCode 最高的構建, 再優先選擇當前設備的精確 ABI 資產並在缺失時回退 universal. 只有顯式設定 allowPatchVersionMismatch=true 時, 矩陣條目才可覆蓋已驗證的補丁區間: 實際構建所用宿主可無提示打包, 區間內其他宿主重用同一構建時會收到警告, 區間外宿主不能使用該條目. 若矩陣沒有可用條目, 仍回退現有 Release/標籤通道. 若配套插件版本低於已安裝版本, 插件中心會提示先卸載再安裝配套構建; Android 無法執行覆蓋降級安裝.

**問: 為什麼插件要與 AutoJs6 版本配套?**

答: 模板 APK 內置的腳本運行環境與 AutoJs6 的運行時 API 嚴格對應, 因此兼容性依據插件聲明並通過校驗的 versionCode 契約判定, 而不是插件自身的語義版本號. 大多數構建只適配一個確切宿主; 構建亦可顯式聲明一個經過驗證的補丁版本閉區間. 此時配套宿主靜默通過, 區間內其他宿主收到警告, 區間外則阻止打包. 插件自身版本 (如 1.0.0) 獨立演進, 版本名中的 autojs6- 後綴與發布標籤標註配套宿主; 使用舊版 AutoJs6 時請下載對應舊標籤下的插件構建 (若需從新版插件回退, 請先卸載再安裝, Android 不支持降級覆蓋安裝).

**問: 安裝後在桌面上找不到插件, 是不是安裝失敗了?**

答: 不是. 本插件沒有圖示與介面, 只以後台服務形式供 AutoJs6 調用. 可在系統 "設定 > 應用程式" 列表中確認 APK Builder Template 已安裝.

**問: 打包時提示模板校驗失敗怎麼辦?**

答: 通常說明插件安裝包不完整或已損壞. 請從 AutoJs6 插件中心或本倉庫 Releases 重新下載安裝; 若問題依舊, 歡迎到 Issues 反饋.

**問: 插件體積為什麼這麼大?**

答: 插件內置了完整的獨立應用模板, 其中包含腳本引擎與各處理器架構的原生庫. 這正是模板從 AutoJs6 主程式中拆分出來的原因: 只有需要打包功能的用戶才需要承擔這部分體積.

**問: 什麼是 "遠程構建"?**

答: 一種實驗性協議, 允許插件在自身進程內完成輕量打包 (解包模板, 寫入腳本與配置, 重寫包名與資源, 重新簽名). 官方發布的插件預設關閉該能力, 目前僅供開發者自行構建體驗.

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

插件的能力規劃與完成情況以可核查的清單形式維護在 ROADMAP.md 中, 涵蓋遠程構建穩定化, 按架構拆分模板變體, 放寬補丁級版本兼容等方向. 歡迎透過 Issues 參與討論.

- [查看 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### 發行歷史

******

# v1.0.0

###### 2026/09/01

* `提示` 外掛程式獨立版本線的首個發佈版本; 外掛程式中心透過 compat-matrix.json 選擇配套 ABI 構建, 手動下載時應核對 autojs6- 宿主後綴, 遠端構建仍預設關閉
* `新增` 引入外掛程式 SemVer 1.0.0, 獨立構建號, 複合版本名稱與單調遞增的 Android versionCode, 支援同一宿主發佈多個外掛程式構建
* `新增` 新增 universal, arm64-v8a, armeabi-v7a, x86_64 與 x86 變體, 支援精確 ABI 選擇及 universal 後備
* `新增` 新增失敗關閉的宿主兼容區間契約與權威兼容矩陣, 讓經過明確驗證的相鄰修補區間可共用一個外掛程式構建
* `修復` 使實驗性遠程單檔案構建編號與舊構建器保持一致，並新增失敗關閉的工作區空間預檢：交叉核對輸入解壓大小、使用構建期驗證的範本展開上限，並保留 256 MiB 空間
* `修復` 在進入 BUILD/SIGN 前拒絕舊版內嵌 Node.js 封裝中繼資料及原始碼指令並提示遷移至外部 Runtime 外掛程式，同時移除已棄用的 Manifest 服務及前台權限注入
* `修復` 修正實驗性遠端工作階段中關閉與建置執行緒的競態: 取消或關閉後, 工作執行緒可能重新建立已刪除的工作階段工作區; 現時會等待工作執行緒結束再清理, 最終保持零殘留
* `修復` 加強實驗性遠端構建: 拒絕路徑清單未聲明的 TypeScript 暫存密文, 並在工作區規範化檔案名稱後正確識別自訂 BKS 簽名庫
* `修復` 收緊實驗性遠端構建輸入邊界: 嚴格驗證 Parcelable/Bundle 與 project.json 的類型, 大小及巢狀深度, 限制金鑰庫, 圖示及 ZIP 路徑深度/段長, 並修正 ARSC 套件名稱和衍生輸出檔名越界
* `修復` 部分系統安裝後無法透過插件中心激活的問題
* `優化` 統一 Gradle 與 Python 的 Runtime Kit 驗證規則, 涵蓋摘要, 大小, 必需檔案, APK 項目及五變體一致性
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
