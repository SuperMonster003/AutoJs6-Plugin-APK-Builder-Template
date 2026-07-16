******

### 發行歷史

******

# v6.8.0 Alpha5

###### 2026/07/16

* `新增` APK Builder Template 外掛服務, 外掛 ID 為 `autojs6-apk-builder-template`, 引擎為 `apk-builder-template`, 變體為 `inrt-universal`
* `新增` 透過 `org.autojs.plugin.INFO` 暴露外掛資訊, 透過 `org.autojs.plugin.APK_BUILDER` 提供範本 APK
* `新增` 將 AutoJs6 Runtime Kit 封裝到 `assets/runtime-kit/`, 包含 `template.apk`, 預設簽章庫, 中繼資料和契約檔案
* `新增` 建置時驗證 Runtime Kit 中繼資料, SHA-256 摘要和 `template.apk` 必要項目
* `新增` 回報宿主版本, 協定版本, 範本套件名稱, 範本 SHA-256, Runtime API 摘要和遠端建置能力
* `新增` 支援透過 `autojs.apkBuilder.templatePlugin.enableRemoteBuild` 啟用實驗性遠端建置協定
* `新增` 發布流程支援下載 Runtime Kit, 驗證資產, 使用受信任金鑰簽章並上傳通用 APK
* `新增` 外掛資訊, 使用說明, README 與 CHANGELOG 增加西班牙文/法文/俄文/阿拉伯文/日文/韓文/英文/簡體中文/香港繁體/台灣繁體多語言資源
