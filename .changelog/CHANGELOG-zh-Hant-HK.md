******

### 發行歷史

******

# v6.8.0 Alpha5

###### 2026/07/16

* `新增` APK Builder Template 插件服務, 插件 ID 為 `autojs6-apk-builder-template`, 引擎為 `apk-builder-template`, 變體為 `inrt-universal`
* `新增` 透過 `org.autojs.plugin.INFO` 暴露插件資訊, 透過 `org.autojs.plugin.APK_BUILDER` 提供模板 APK
* `新增` 將 AutoJs6 Runtime Kit 打包到 `assets/runtime-kit/`, 包含 `template.apk`, 預設簽名庫, 元數據和契約文件
* `新增` 構建時校驗 Runtime Kit 元數據, SHA-256 摘要和 `template.apk` 必需項目
* `新增` 上報宿主版本, 協議版本, 模板包名, 模板 SHA-256, Runtime API 摘要和遠程構建能力
* `新增` 支援透過 `autojs.apkBuilder.templatePlugin.enableRemoteBuild` 啟用實驗性遠程構建協議
* `新增` 發布流程支援下載 Runtime Kit, 驗證資產, 使用受信任密鑰簽名並上傳通用 APK
* `新增` 插件資訊, 使用說明, README 與 CHANGELOG 增加西班牙語/法語/俄語/阿拉伯語/日語/韓語/英語/簡體中文/香港繁體/台灣繁體多語言資源
