APK Builder Template 為 AutoJs6 封裝獨立應用程式提供 Runtime Kit.

建議從新版 AutoJs6 外掛中心安裝: 它會讀取 `compat-matrix.json`, 自動選擇與目前宿主搭配的建置, 優先使用裝置精確 ABI, 缺少時回退 universal. 手動安裝時請按 Release 標籤或外掛版本名稱中的 autojs6- 後綴確認搭配的宿主; 若需從新版外掛回退, 請先解除安裝, 因為 Android 不支援降級覆蓋安裝. 宿主透過 `org.autojs.plugin.APK_BUILDER` 發現外掛, 並讀取 `assets/runtime-kit/template.apk`.

Runtime Kit 可明確涵蓋經驗證的修補區間. 實際建置所用宿主可不提示直接封裝; 區間內其他宿主會在警告後繼續, 區間外宿主則在範本傳輸前遭阻止.

內建 Runtime Kit 包含:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

封裝完全在同一台 Android 裝置的外掛處理程序內完成, 不上傳專案原始碼. AutoJs6 負責信任與相容准入, 並獨立複核外掛傳回的 APK. 舊 `supportsRemoteBuild` 開關繼續保持關閉, 但不關閉這條正式路徑.
