APK Builder Template 為 AutoJs6 打包獨立應用程式提供 Runtime Kit.

建議從新版 AutoJs6 插件中心安裝: 它會讀取 `compat-matrix.json`, 自動選擇與當前宿主配套的構建, 優先使用設備精確 ABI, 缺失時回退 universal. 手動安裝時請按 Release 標籤或插件版本名中的 autojs6- 後綴確認配套宿主; 若需從新版插件回退, 請先卸載, 因為 Android 不支援降級覆蓋安裝. 宿主透過 `org.autojs.plugin.APK_BUILDER` 發現插件, 並讀取 `assets/runtime-kit/template.apk`.

Runtime Kit 可顯式覆蓋經驗證的補丁區間. 實際構建所用宿主可無提示打包; 區間內其他宿主會在警告後繼續, 區間外宿主則在模板傳輸前被阻止.

內置 Runtime Kit 包含:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

打包完全在同一台 Android 裝置的插件進程內完成, 不上傳項目原始碼. AutoJs6 負責信任與兼容准入, 並獨立覆核插件返回的 APK. 舊 `supportsRemoteBuild` 開關繼續保持關閉, 但不關閉這條正式路徑.
