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

# v1.0.3

###### 2026/09/15

* `修復` 產生的 APK 以未壓縮且 4 位元組對齊的方式存放 resources.arsc, 使目標為 Android 11 及以上的應用程式可以安裝
* `優化` 移除 `enableRemoteBuild` 建置開關, 插件始終宣告 APK 建置能力

# v1.0.2

###### 2026/09/13

* `修復` 外掛版本日期固定使用英文, 不隨建置機器的語言變化
* `優化` 統一多語言資源, 明確插件啟用契約並驗證發佈產物

# v1.0.1

###### 2026/09/13

* `優化` 統一多語言資源, 明確插件啟用契約並驗證發佈產物

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/docs/16kb.md)
