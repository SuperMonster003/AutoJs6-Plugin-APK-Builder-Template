APK Builder Template 为 AutoJs6 打包独立应用提供 Runtime Kit.

建议从新版 AutoJs6 插件中心安装: 它会读取 `compat-matrix.json`, 自动选择与当前宿主配套的构建, 优先使用设备精确 ABI, 缺失时回退 universal. 手动安装时请按 Release 标签或插件版本名中的 autojs6- 后缀确认配套宿主; 若需从新版插件回退, 请先卸载, 因为 Android 不支持降级覆盖安装. 宿主通过 `org.autojs.plugin.APK_BUILDER` 发现插件, 并读取 `assets/runtime-kit/template.apk`.

Runtime Kit 可显式覆盖经验证的补丁区间. 实际构建所用宿主可无提示打包; 区间内其他宿主会在警告后继续, 区间外宿主则在模板传输前被阻止.

内置 Runtime Kit 包含:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

打包完全在同一台 Android 设备的插件进程内完成, 不上传项目源码. AutoJs6 负责信任与兼容准入, 并独立复核插件返回的 APK. 旧 `supportsRemoteBuild` 开关继续保持关闭, 但不关闭这条正式路径.
