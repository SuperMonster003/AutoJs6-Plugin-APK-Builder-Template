# 设备内插件托管 APK 构建架构决策

- 状态: 已采纳
- 日期: 2026/09/02
- 决策范围: AutoJs6 的“打包应用”运行时路径
- 替代候选: 宿主进程内本地构建器、实验性“远程构建”作为独立可选路径

## 1. 结论

AutoJs6 的“打包应用”正式确立为一条用户可见构建路径: **设备内插件托管构建**。

- AutoJs6 负责界面、插件发现与信任、版本/ABI/协议准入、请求准备、取消与进度展示，以及输出 APK 的独立复核和原子发布。
- APK Builder 插件负责读取自身 Runtime Kit、展开模板、写入脚本与项目资源、修改 Manifest 和 `resources.arsc`、裁剪 ABI、管理签名库、签名并返回候选 APK。
- AutoJs6 不再包含或恢复一份可独立完成打包的进程内核心实现。
- 插件缺失、被禁用、不受信任、不兼容或构建失败时，本次打包失败并保留已有输出；宿主不会静默切换到第二套构建器。

这里的“设备内”是关键限定。宿主与插件都运行在同一台 Android 设备上，通过 Binder/AIDL 和
`ParcelFileDescriptor` 通信；项目源码不会因为本协议被上传到网络、云端或外部构建服务器。

## 2. 为什么曾称为“远程构建”

早期实现从 AutoJs6 进程的视角，把另一个应用进程中的构建器称为 remote builder。因此源码、AIDL 文档和诊断中形成了
`RemoteApkBuild*`、`supportsRemoteBuild` 与 `REMOTE_BUILD_VERSION` 等名称。这里的 remote 指“跨进程 Binder 对端”，
不是“互联网远程服务”。

这一实现最初以实验能力、默认关闭开关存在，并与更早的宿主进程内构建器作对比。后来宿主内的完整构建器已经移除，首轮
v6.8.0 候选 `71e684b8d` 又同时保持实验开关关闭，导致普通打包入口没有任何可执行路径。该候选因此被拒绝。

本决策不重新引入宿主构建器，而是把已经完成安全与稳定性验证的插件侧实现提升为正式产品路径，并给予不含歧义的正式能力名。

## 3. 正式协议与兼容层

正式准入使用以下能力：

| 能力 | 当前值 | 作用 |
|---|---:|---|
| `supportsApkBuild` | `true` | 插件声明可承担正式 APK 构建 |
| `apkBuilderBuildProtocolVersion` | `3` | 插件侧正式构建协议上限 |
| `apkBuilderBuildExecutionMode` | `on-device-plugin` | 明确构建只在设备内插件进程执行 |
| `APK_BUILD_VERSION` | `3` | 宿主与插件共享的正式编译期协议版本 |

正式路径不读取实验性宿主总开关，也不依赖 `supportsRemoteBuild`。准入必须同时满足受信签名、启用状态、宿主兼容区间、设备
ABI、正式能力、协议版本和执行模式；任一条件缺失都失败关闭。

现有 AIDL 的方法顺序、Parcelable 字段顺序和 Binder 事务号保持不变。以下旧名称作为兼容层保留：

| 旧名称 | 迁移规则 |
|---|---|
| `REMOTE_BUILD_VERSION` | 保留为 `APK_BUILD_VERSION` 的数值别名 |
| `supportsRemoteBuild` | 继续表示旧实验入口是否开放，不解释为正式能力 |
| `apkBuilderRemoteBuildProtocolVersion` | 仅供旧实验宿主协商 |
| `remoteBuildStatus` / `remoteBuildApiVersion` | 仅供旧元数据消费者和审计工具 |
| `RemoteApkBuild*` 源码类名 | 暂作内部兼容实现名；不代表网络传输，也不形成第二条产品路径 |

官方插件可以同时发布 `supportsApkBuild=true` 与 `supportsRemoteBuild=false`。这不是矛盾：前者启用唯一正式构建路径，后者
继续关闭旧实验入口。不得通过把旧键直接翻转为 `true` 来完成迁移，因为旧宿主会把它解释为需要开发者开关的实验功能。

## 4. 责任边界

| 阶段 | AutoJs6 宿主 | APK Builder 插件 |
|---|---|---|
| 发现与准入 | 发现服务、校验官方签名/启用状态、版本区间、ABI、正式能力、协议和执行模式 | 发布可验证的能力与 Runtime Kit 元数据 |
| 请求准备 | 规范化项目配置；以有界 ZIP/FD 提供项目、可选原生库和自定义签名库；固定预期包身份和签名证书摘要 | 再次验证请求类型、大小、摘要、路径、协议与宿主身份 |
| 构建 | 不修改模板，不写 Manifest/ARSC，不签名 | 展开自身模板、写入项目、修改资源、裁剪 ABI、重打包和签名 |
| 密钥库 | 提供 UI 和目标 FD，不持有插件默认私钥 | 创建/验证 BKS/JKS；保管并使用 Runtime Kit 默认签名库 |
| 输出接收 | 重算大小/SHA-256，检查有界 ZIP 结构、APK 签名、唯一签名者、包名/版本，再原子替换目标 | 通过只读 FD 返回候选 APK 和更新后的项目配置 |
| 失败与取消 | 显示可行动错误、保留旧产物、关闭绑定/FD | 协作取消、关闭 FD、清除口令/一次性密钥并删除私有工作区 |

“插件拥有构建核心”不等于宿主盲目信任插件输出。插件是生产者，宿主仍是发布边界上的独立验证者；两侧校验针对不同信任
边界，不属于重复实现构建核心。

## 5. 密钥库接口

`IApkBuilderTemplatePlugin.manageKeyStore(ApkKeyStoreRequest)` 追加在既有 AIDL 方法之后，使用独立的
`KEYSTORE_VERSION = 1`、`supportsKeyStoreOperations` 与 `apkBuilderKeyStoreApiVersion` 协商。插件支持：

- `OPERATION_CREATE`: 在插件私有临时目录生成 BKS/JKS，并写入宿主提供的输出 FD；
- `OPERATION_VERIFY`: 有界读取输入 FD，验证 store password、alias 和 alias password；
- 不支持的 operation 返回 `STATUS_UNSUPPORTED`，输入错误返回 `STATUS_FAILED`；
- 所有路径都关闭插件持有的 FD 副本并删除临时文件。

该接口使签名库生成与验证也落入插件所有权，宿主无需重新携带 `CertCreator`/`KeyStoreHelper` 形式的构建核心。

## 6. “唯一构建方式”的范围

唯一性针对 AutoJs6 在 Android 设备上响应用户“打包应用”请求的运行时实现，不包括：

- AutoJs6 仓库生成 Runtime Kit 的 Gradle/CI 流程；
- 本插件仓库把 Runtime Kit 封装、签名和发布为插件 APK 的 Gradle/CI 流程；
- 将来可能另立 ADR 设计的真正网络/离设备构建服务。

因此“本地构建插件”如果指开发者在电脑上运行 Gradle 生成插件 APK，仍会长期存在；它是插件的生产方式，不是设备内用户
APK 的第二套构建器。此前所谓“本地构建器”如果指 AutoJs6 进程内直接修改模板的实现，则不再恢复，也不与插件路径并存。

## 7. 安全与发布要求

- 正式路径默认可用不等于降低准入：官方签名、兼容区间、ABI、协议、执行模式和输出复核仍是硬门槛。
- `allowRiskyBuild` 只允许既有宿主身份警告语义，不得绕过声明区间、协议、摘要、路径、签名或输出身份校验。
- 不做自动回退或自动重试，避免两套实现漂移、循环和同因失败；用户修复插件后重新发起构建。
- `supportsRemoteBuild=false` 继续阻止旧实验入口，不再作为正式发布的 No-Go 条件。
- 首轮候选和此前 M3/G1—G7 证据作为实现来源与历史审计保留；凡依赖“双构建器”或“远程默认启用”的旧放量结论均由本 ADR
  取代。
- 架构迁移、自动化门禁与新候选真实设备“打包→安装→冷启动”通过前，不创建 v6.8.0/v1.0.0 公开 GA Release。

## 8. 后续演进规则

1. 新增正式构建字段时先判断是否提升 `APK_BUILD_VERSION`，并同步 Runtime Kit、能力元数据、协议文档和双侧测试。
2. 旧实验名称只有在已支持的宿主不再需要它们、且另有明确弃用窗口后才能删除；不得在兼容版本内重排 AIDL 方法或 Parcelable 字段。
3. 新的网络构建服务必须使用新的能力键、威胁模型和 ADR，不得复用 `on-device-plugin` 或把旧 remote 名称悄然改义。
4. Roadmap 以后以“正式插件托管构建的发布质量”为主线；独立安全复核、预览周期和双维护者确认仍可作为 GA 后强化项，但不再以
   恢复宿主第二构建器为目标。
