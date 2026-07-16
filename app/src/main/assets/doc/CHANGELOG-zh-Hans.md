# v6.8.0 Alpha5

###### 2026/07/16

* `新增` APK Builder Template 插件服务, 插件 ID 为 `autojs6-apk-builder-template`, 引擎为 `apk-builder-template`, 变体为 `inrt-universal`
* `新增` 通过 `org.autojs.plugin.INFO` 暴露插件信息, 通过 `org.autojs.plugin.APK_BUILDER` 提供模板 APK
* `新增` 将 AutoJs6 Runtime Kit 打包到 `assets/runtime-kit/`, 包含 `template.apk`, 默认签名库, 元数据和契约文件
* `新增` 构建时校验 Runtime Kit 元数据, SHA-256 摘要和 `template.apk` 必需条目
* `新增` 上报宿主版本, 协议版本, 模板包名, 模板 SHA-256, Runtime API 摘要和远程构建能力
* `新增` 支持通过 `autojs.apkBuilder.templatePlugin.enableRemoteBuild` 启用实验性远程构建协议
* `新增` 发布流程支持下载 Runtime Kit, 验证资产, 使用受信任密钥签名并上传通用 APK
* `新增` 插件信息, 使用说明, README 与 CHANGELOG 增加西班牙语/法语/俄语/阿拉伯语/日语/韩语/英语/简体中文/香港繁体/台湾繁体多语言资源
