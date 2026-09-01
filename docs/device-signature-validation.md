# M5-1 设备端签名证书校验

## 目标与边界

M5-1 将发布流水线已有的官方签名信任边界延伸到 AutoJs6 设备端。宿主在首次读取 APK Builder 插件 Binder metadata 之前解析已安装软件包的签名证书 SHA-256, 仅允许以下签名继续连接:

- AutoJs6 内置的 APK Builder 官方发布证书指纹;
- 仅在宿主自身为 DEBUG 构建时, 与宿主使用相同证书的本地调试插件。

空证书集合、不同的调试证书以及任何其他证书均失败关闭。发布构建不接受同签名调试例外。

## 实现

宿主仓库的实现分成三层:

1. `PluginTrustManager` 解析软件包签名证书并判定官方指纹。
2. `ApkBuilderPluginTrustPolicy` 固化 APK Builder 的发布/调试接受规则, 并生成稳定、去重、排序后的实测指纹摘要。
3. `AidlPluginHost` 的 APK Builder 实例在共享连接和独占连接的绑定路径起点执行 `preBindValidator`; 校验失败时不会创建 Binder 连接。打包入口使用专门的多语言告警, 同时显示软件包名和实测 SHA-256, 便于用户定位并移除错误安装包。

兼容区间、ABI 和协议校验仍是签名门禁通过后的独立步骤, 不会代替或绕过证书校验。

## 自动化验证

宿主侧 `ApkBuilderPluginTrustPolicyTest` 覆盖:

- 发布构建接受官方签名;
- DEBUG 构建接受与宿主相同的签名;
- DEBUG 构建拒绝不同签名;
- 空签名集合失败关闭;
- 指纹摘要的标准化、去重、排序与空值回退。

定向单元测试共 5 项, 失败 0, 错误 0; `:app:compileAppReleaseKotlin` 同时通过, 确认发布变体可编译。

## API 36 设备演练

演练设备为 Android 16 / API 36 / x86_64 AVD (`emulator-5556`)。宿主使用隔离的 Android Debug 证书构建, 未使用生产签名材料。

### 负向场景

将待测插件重新签名为一次性非受信证书后安装。打包入口显示专用证书告警, 包含 `org.autojs.plugin.apkbuilder.template` 与实测指纹 `000fd7db83ccf1b055fb576e57b25b2c0a47b5081886fbf6f9209b4e099bda1c`。日志记录 2 次发现阶段拒绝、0 次 `onServiceConnected`, 证明两条调用路径都在 Binder 建连前失败关闭。

### 正向场景

仅将插件替换为 GitHub Release `v6.8.0-alpha5` 的官方已发布 APK。官方证书指纹 `31a681fcfffb3e428420cae280ded89292b12a3b0f59e19b7a73e32a8ae4c213` 通过门禁, 日志记录 1 次 `onServiceConnected`。随后出现的是该旧插件与当前 5277 宿主之间预期的版本兼容提示, 没有证书告警, 说明官方签名路径未受影响且后续门禁仍独立生效。

## 可复核证据

机器可读证据位于:

`D:\idea-projects\.a6-compat-audit-artifacts\m5-1-device-signature-2026-08-31\manifest.json`

清单记录宿主、正负向插件、截图、UI XML、logcat 和 JUnit XML 的字节数与 SHA-256, 并记录设备、证书、断言及清理状态。清单内所有摘要在收口时重新计算并校验。一次性负向证书的私钥库已删除; 宿主与插件测试包、设备脚本均已从 AVD 清理, 未改动其他已安装软件包。
