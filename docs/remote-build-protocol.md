# 远程构建协议

- 状态: 实验性, 官方插件构建默认关闭
- 当前编译期 API 版本: `ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION = 3`
- AIDL 服务: `org.autojs.plugin.APK_BUILDER`
- 权限: `org.autojs.permission.PLUGIN`

本文面向 AutoJs6 宿主与 APK Builder Template 插件的集成开发者. 协议真源是
`plugin-api/apk-builder-template` 中的 AIDL 与 Parcelable Kotlin 类; 本文解释这些字段的组合规则, 生命周期和错误语义.
若本文与源码冲突, 以源码为准并在同一次协议改动中修正文档.

Node.js 的运行时所有权与 APK 打包边界由 `docs/remote-build-node-packaging-decision.md` 定义。

## 1. 能力发现与版本协商

宿主绑定插件后先读取 `getTemplateInfo().capabilities`, 不应直接试调用远程构建.

| 能力键 | 类型 | 含义 |
|---|---:|---|
| `supportsRemoteBuild` | Boolean | 当前插件构建是否允许远程构建. 官方构建目前为 `false` |
| `apkBuilderRemoteBuildProtocolVersion` | Int | 与内置 Runtime Kit 配套并对宿主公布的协议版本; 来自 `runtime-kit.json#contract.remoteBuildProtocolVersion`, 缺失时回退到插件构建值 |
| `remoteBuildApiVersion` | Int | 插件代码实际编译支持的协议上限, 即 `REMOTE_BUILD_VERSION` |
| `remoteBuildStatus` | String | 当前为 `disabled` 或 `experimental` |

宿主的协商顺序:

1. `supportsRemoteBuild != true`: 不调用 `openBuildSession`. 当前 R1 语义是显示可行动的“远程构建不可用”并停止本次请求;
   关闭开关不构成构建回退。只有未来 R2/R3 已另行实现并验收独立构建路径后, 才能按
   `docs/remote-build-fallback-decision.md` 尝试一次受控回退。即使误调用, 插件会话也以 `STATUS_UNSUPPORTED` 结束.
2. 取 `min(apkBuilderRemoteBuildProtocolVersion, remoteBuildApiVersion)` 作为该配套构建可用的协议上限.
3. 宿主只使用不高于该上限的字段, 并把本次请求实际依赖的最低协议写入 `requiredProtocolVersion`.
4. 插件发现 `requiredProtocolVersion > REMOTE_BUILD_VERSION` 时拒绝请求: `STATUS_FAILED`, `LEVEL_BLOCK`,
   原因写入 `errors`.

协议版本只约束远程构建, 与模板读取协议 `TEMPLATE_VERSION` 相互独立. v3 在 v2 基础上增加 TypeScript 构建暂存的
认证加密元数据; 使用这些字段的请求必须声明 `requiredProtocolVersion >= 3`.

## 2. 会话时序与 AIDL

入口是 `IApkBuilderTemplatePlugin.openBuildSession(request, callback)`. 返回会话对象后, 宿主再显式调用 `start()`.

| 接口 | 方法 | 语义 |
|---|---|---|
| `IApkBuilderTemplatePlugin` | `openBuildSession(request, callback)` | 创建尚未启动的会话; `callback` 应非空 |
| `IApkBuildSession` | `start()` | 异步启动; 同一会话重复调用无效果 |
| `IApkBuildSession` | `cancel()` | 设置协作式取消信号; 可在 `start()` 前调用 |
| `IApkBuildSession` | `getProgress()` | 读取最近一次进度快照 |
| `IApkBuildSession` | `close()` | 取消未完成工作, 关闭输入描述符并删除会话工作区; 可重复调用 |
| `IApkBuildCallback` | `onStarted(progress)` | 会话开始, 初始步骤为 `STEP_PREPARE` |
| `IApkBuildCallback` | `onProgress(progress)` | 零到多次进度更新 |
| `IApkBuildCallback` | `onCompleted(result)` | 唯一成功终态 |
| `IApkBuildCallback` | `onFailed(result)` | 失败或不支持终态; 结合 `result.status` 区分 |
| `IApkBuildCallback` | `onCancelled(result)` | 取消终态 |

每个已启动会话只应出现一个终态回调. 宿主收到终态后应停止渲染后续进度, 复制并关闭输出描述符, 最后调用
`session.close()`. 回调是 `oneway`; 不应在回调线程执行 UI 阻塞操作.

推荐的宿主侧生命周期:

1. 从宿主所选默认或自定义 keystore 解析预期签名证书 SHA-256, 再创建所有输入文件描述符和 `ApkBuildRequest`.
2. 调用 `openBuildSession`; Binder 已复制描述符后, 宿主关闭自己持有的输入描述符.
3. 调用 `start()` 并消费回调.
4. `onCompleted` 中把 `outputApkFd` 复制到目标同目录的宿主临时文件, 校验长度、SHA-256、APK 结构、密码学签名、唯一
   签名者与会话前固定的签名身份, 以及请求包身份; 全部通过后才原子替换目标, 然后关闭该描述符.
5. 无论成功, 失败或取消, 最终都调用 `session.close()`; Binder 断开时也执行同等清理.

插件负责关闭它收到的 `projectArchiveFd`, `nativeLibrariesArchiveFd` 和 `keyStoreFd` 副本, 包括协议拒绝和功能关闭等
未进入工作区准备阶段的路径. 所有终态还会清零并移除尚未被解密器消费的 TypeScript 一次性密钥 / 路径元数据,
并释放请求中的签名库口令引用. 双方各自关闭自己的描述符副本, 不以另一方清理代替本方清理.

## 3. `ApkBuildRequest`

### 3.1 顶层字段

| 字段 | 必需性 | 规则 |
|---|---|---|
| `hostPackageName` | 建议 | 非空时必须等于插件配对宿主包名, 否则进入风险不匹配处理 |
| `hostVersionName` | 建议 | 非空时必须等于插件配对版本名 |
| `hostVersionCode` | 建议 | 大于 0 时必须等于插件配对 versionCode |
| `requiredProtocolVersion` | 必需 | 本次请求使用的最低远程构建协议; 不得高于插件 `REMOTE_BUILD_VERSION` |
| `projectArchiveFd` | 必需 | ZIP 格式项目输入; 插件消费并关闭收到的描述符副本 |
| `projectArchiveSizeBytes` | 可选校验 | 大于 0 时执行精确长度校验 |
| `projectArchiveSha256` | 可选校验 | 非空时执行不区分大小写的 SHA-256 校验 |
| `nativeLibrariesArchiveFd` | 条件必需 | 选择 ABI / 原生能力而模板不能单独满足时提供; 内容规则见 3.4 |
| `nativeLibrariesArchiveSizeBytes` | 可选校验 | 与项目归档相同 |
| `nativeLibrariesArchiveSha256` | 可选校验 | 与项目归档相同 |
| `projectConfigJson` | 必需 | 单一 JSON object, 最多 512 KiB UTF-8 / 64 层, 规则见 3.3 |
| `keyStoreFd` | 可选 | 自定义签名库; 缺失时使用 Runtime Kit 的默认签名库; 其余 keystore 元数据不得脱离 FD 单独出现 |
| `keyStoreSizeBytes` | 可选校验 | 自定义签名库声明长度; 声明与实际 FD 读取均最多 64 MiB |
| `keyStoreSha256` | 可选校验 | 自定义签名库摘要 |
| `keyStorePassword` | 条件必需 | 提供 `keyStoreFd` 时必需; 最多 4,096 个 UTF-16 code unit |
| `keyAlias` | 条件必需 | 提供 `keyStoreFd` 时必需; 最多 255 UTF-8 bytes |
| `keyAliasPassword` | 可选 | 缺失时回退到 `keyStorePassword`; 最多 4,096 个 UTF-16 code unit |
| `outputFileName` | 可选 | 输入路径最多 4,096 UTF-8 bytes; 仅采用最后一个路径片段且 basename 最多 238 bytes; 缺少 `.apk` 时自动追加; 空值回退 `remote-build.apk` |
| `allowRiskyBuild` | 必需 | `false`: 任一宿主身份不匹配立即失败; `true`: 记录 warning 后继续 |
| `extras` | 必需 | 项目归档元数据与协议扩展, 见 3.2 |

`allowRiskyBuild=true` 只放宽宿主包名 / 版本名 / versionCode 不匹配; 它不绕过协议版本, 摘要, 路径, 配置,
模板处理或签名校验. 所有 digest 若提供必须是 64 位十六进制; 负长度和超过对应实现上限的声明在创建工作区前拒绝.

### 3.2 `extras`

| 键 | 协议 | 必需性 | 规则 |
|---|---:|---|---|
| `archiveFormatVersion` | v2+ | 必需 | 当前只能为 `1` |
| `sourceKind` | v2+ | 必需 | `file` 或 `directory` |
| `sourcePath` | v2+ | 必需 | 相对项目 ZIP 根的文件或目录路径, 类型必须与 `sourceKind` 一致; 使用 3.4 的统一路径上限 |
| `iconPath` | v2+ | 可选 | 相对项目 ZIP 根的图标路径; 文件存在时替换模板图标; 使用 3.4 的统一路径上限和 3.3 的图像上限 |
| `projectArchiveUncompressedSizeBytes` | v3 | 可选 | 宿主从项目 ZIP 中央目录计算的总解压字节数; 新宿主必须提供, 插件在解包前后复核并用于空间预检; 旧 v3 宿主缺失时按 1 GiB 上限保守估算 |
| `nativeLibrariesArchiveUncompressedSizeBytes` | v3 | 可选 | native/assets ZIP 的总解压字节数; 存在 native FD 的新宿主必须提供正值, 插件在解包前后复核; 旧 v3 宿主缺失时按 2 GiB 上限保守估算 |
| `hostOutputFileName` | 保留 | 可选 | 当前插件实现不读取, 不得依赖它改变输出名 |
| `sourceRootPath` | 返回字段 | 不应作为输入依赖 | 插件在成功结果 `extras` 中返回工作区项目根路径 |
| `typeScriptStagingEncryptionVersion` | v3 | 条件必需 | TypeScript 暂存保护版本, 当前为 `TypeScriptBuildStagingCipher.VERSION = 1` |
| `typeScriptStagingEncryptionKey` | v3 | 条件必需 | 32 字节的一次性 AES-GCM 密钥; 插件读取后立即从请求 Bundle 移除并覆写字节数组 |
| `typeScriptStagingEncryptedPaths` | v3 | 条件必需 | ZIP 内经过认证加密的 `.js` / `.mjs` / `.cjs` 相对路径清单 |

`extras` 只接受本表定义的 11 个键, 总键数最多 16, 每个值严格按声明类型读取而不做字符串/数字强制转换. 三个 TypeScript
暂存字段必须全部出现或全部缺失. 路径清单最多 16,384 项, 不得为空、重复、使用绝对路径、`.` / `..`、空段或控制字符;
每个声明文件都必须存在并成功认证解密, 未消费完的清单会使构建失败. 反向也执行失败关闭: 任一 JavaScript 文件带有
TypeScript 暂存密文头却未列入路径清单时, 必须在普通脚本加密前拒绝, 不得把传输密文再次当作脚本正文打包. 明文只在
内存中短暂存在, 随即按最终 APK 的运行时脚本密钥重新加密; 这属于传输期保护, 不是独立的长期密钥存储方案.

### 3.3 `projectConfigJson`

远程轻量构建器读取以下字段:

| JSON 路径 | 必需性 | 规则 / 默认值 |
|---|---|---|
| `name` | 必需 | 非空应用名, 最多 256 UTF-8 bytes |
| `packageName` | 必需 | 至少两个 ASCII Java 标识符段, 如 `org.example.app`; 最多 127 bytes, 为 ARSC 固定槽保留 NUL 空间 |
| `versionName` | 必需 | 非空, 最多 256 UTF-8 bytes |
| `versionCode` | 必需 | 正 32-bit 整数; 不接受字符串或浮点强制转换 |
| `main` | 可选 | 目录源默认 `main.js`; 文件源强制写为 `main.js` |
| `abis` | 可选 | 目标 ABI 清单; 提供后会裁剪其他 ABI; 只接受四种协议 ABI 且不得重复 |
| `libs` | 可选 | 当前可打包能力标签包括 `OpenCV`, `Image Quantization`; `Embedded Node.js` 及其旧别名只作为迁移拒绝信号保留, 从不嵌入运行时; 与其他数组一样最多 512 项、单项 255 UTF-8 bytes |
| `permissions` | 可选 | 覆写 Manifest 权限清单; 最多 512 项、单项 255 UTF-8 bytes |
| `signatureScheme` | 可选 | 默认 `V1 + V2`; 字符串中出现的 `V1` / `V2` / `V3` / `V4` 决定签名方案 |
| `launchConfig.splashVisible` | 可选 | 默认 `true` |
| `build.id/number/time` | 可选输入 | 插件生成新的 id / number / time, 经 `updatedProjectConfigJson` 返回 |

Node.js 不属于当前 APK 打包协议的成功路径。宿主在创建缓存/归档或打开 Binder 会话之前检查 `libs` 中的旧 Node 标签,
`projectType=node`, 任意 `nodeConfig`, Node 入口扩展名和主脚本首条执行模式指令。插件对旧版/自定义客户端重复执行独立门禁:
元数据命中在工作区前拒绝, 入口指令命中在有界解包后、BUILD/SIGN 前拒绝并清理工作区。两侧都返回可行动迁移信息,
不记录脚本 token/正文, 也不生成空壳 APK。完整信号表和未来重新引入条件见
`docs/remote-build-node-packaging-decision.md`。

整个字符串必须是一个完整根对象; 相邻对象、尾随 token、未平衡分隔符、超过 64 层、控制字符、未配对 surrogate 与未知类型均
失败关闭。项目图标另外限制为压缩文件最多 16 MiB、宽高各最多 4,096、总像素最多 4,194,304；插件先执行 bounds-only
解码，越界时不进入完整 bitmap 分配，metadata/完整解码异常或 OOM 只返回固定安全错误。

### 3.4 归档布局与路径安全

单文件源示例:

```text
project.zip
└── source.js          sourceKind=file, sourcePath=source.js
```

项目源示例:

```text
project.zip
└── project/           sourceKind=directory, sourcePath=project
    ├── main.js
    ├── modules/
    └── images/
```

项目 ZIP 与原生输入 ZIP 在写入解包目标前完成全量预检. 两者均拒绝 POSIX / UNC / Windows 盘符绝对路径, `.` / `..`、空段、
C0/C1 控制字符、未配对 surrogate、重复规范化路径以及逃逸目标目录的条目; 每条路径最多 4,096 UTF-8 bytes、单段最多
255 bytes、最多 128 段, `sourcePath` 与 TypeScript 清单使用相同规则. 原生输入 ZIP 只接受:

```text
lib/<abi>/<name>.so
assets/**
```

宿主提供的两个 ZIP 还受以下实现级硬上限约束. 声明压缩长度先于 FD 复制检查, 实际复制长度和流式解压字节再次计数; ZIP
中央目录中的 size 不能单独作为可信依据. 压缩比同时按单条与归档累计值检查, 防止用大量略小于单条阈值的文件拆分 ZIP bomb.

| 输入 | 压缩输入 | 条目数 | 单条解压 | 总解压 | 压缩比 |
|---|---:|---:|---:|---:|---:|
| 项目 ZIP | 512 MiB | 16,384 | 256 MiB | 1 GiB | 解压量达到 1 MiB 后最多 250:1 |
| 原生 / assets ZIP | 1 GiB | 8,192 | 512 MiB | 2 GiB | 解压量达到 1 MiB 后最多 250:1 |

这些上限只针对跨进程、由宿主请求提供的项目与构建输入归档. 内嵌 `template.apk` 来自已执行 size/SHA-256、结构、ABI 与兼容
元数据校验的 Runtime Kit, 属于不同信任边界. 任一上限或路径白名单失败均为 `STATUS_FAILED / LEVEL_BLOCK`, 不允许
`allowRiskyBuild` 绕过, 且错误只返回安全分类/条目序号, 不回显宿主控制的恶意文件名.

### 3.5 插件工作区空间预检

插件在创建会话工作区、复制输入 FD 或展开模板前读取私有缓存文件系统的 `usableSpace`. 所需空间以饱和算术计算:

```text
templateExpanded = templateArchiveBytes * 4
compressedInputs = projectArchiveBytes + nativeArchiveBytes + keyStoreBytes
buildTree = templateExpanded + projectExpanded + nativeExpanded
requiredUsable = 256 MiB reserve + compressedInputs + projectExpanded + 3 * buildTree
```

Runtime Kit 构建任务会实际统计 `template.apk` 的总解压量并要求它不超过压缩大小的 4 倍, 因此该系数不是无验证假设. 新宿主
提供 3.2 的两个展开量字段; 插件把它们与 ZIP 中央目录预检及流式实际解压量交叉核对. 旧 v3 宿主缺少字段时分别使用项目
1 GiB、native/assets 2 GiB 的总解压上限, 不按 0 估算. 存在 native FD 却声明 0 会在工作区前失败.

当 `available < requiredUsable` 时, 会话返回固定类别的阻断错误, 其中只包含 required/available 数值和释放空间或缩小输入的
行动建议; 不创建 `remote-apk-build/session-*`, 不复制大输入, 并关闭全部请求 FD. 该预检覆盖插件侧模板展开、构建树、重打包与
签名工作集. 宿主请求 ZIP 在 Binder 会话前生成, 由宿主自己的失败清理契约负责, 不应把本公式误称为宿主文件系统预检.

完整公式、API 36 故障注入和三档压力结果见 `docs/remote-build-performance-audit.md`.

Parcelable/Bundle、JSON、路径、keystore、图标、Manifest/ARSC 的 72 条确定性畸形语料、24/24 JVM 边界用例与 API 36
最终 39/39 结果见 `docs/remote-build-fuzz-audit.md`。该批次关闭确定性 fuzz 子门槛, 不替代独立安全审查。

当项目配置指定非空 `abis` 且需要宿主原生库时, 宿主必须提供原生输入 ZIP. 缺少归档或所选 ABI 下的必需 `.so` 时是
`STATUS_UNSUPPORTED`。在当前 R1 阶段宿主显示原因并停止; 只有 R2/R3 已验收独立构建路径后, 才允许按回退 ADR 执行一次
受控回退。

## 4. 进度模型

`ApkBuildProgress` 的 `step` 是阶段而非严格状态机; 同一阶段可出现多条消息, 将来可以增加消息. 宿主应识别已知阶段并对
未知值使用通用文案, 不应依赖当前英文 `title/detail` 做逻辑判断.

| 常量 | 值 | 当前用途 |
|---|---:|---|
| `STEP_IDLE` | 0 | `start()` 前初始状态 |
| `STEP_PREPARE` | 1 | 校验请求, 复制归档, 解包项目与模板 |
| `STEP_BUILD` | 2 | 写入输入, Manifest, ABI, 项目资源, 图标与 `resources.arsc` |
| `STEP_SIGN` | 3 | 重打包并签名 APK |
| `STEP_CLEAN` | 4 | 失败 / 取消清理或显式关闭 |
| `STEP_FINISH` | 5 | 会话逻辑结束后的最终快照 |

当前实现主要使用 `title/detail`; `current`, `total`, `percent` 未提供时均为 `-1`. 宿主必须支持不确定进度 UI.

## 5. `ApkBuildResult` 与终态

| status | 回调 | compatibilityLevel | `warnings` / `errors` 约定 |
|---|---|---|---|
| `STATUS_OK` (0) | `onCompleted` | 无 warning 为 `LEVEL_OK`, 否则 `LEVEL_WARN` | 可有 warning, `errors` 为空 |
| `STATUS_FAILED` (1) | `onFailed` | `LEVEL_BLOCK` | 失败原因在 `errors` |
| `STATUS_CANCELLED` (2) | `onCancelled` | `LEVEL_BLOCK` | 正常取消不写 `errors` |
| `STATUS_UNSUPPORTED` (3) | `onFailed` | `LEVEL_WARN` | 不支持/未来可回退的原因在 `warnings`, `errors` 为空 |

`STATUS_UNSUPPORTED` 不是损坏或安全失败, 但它也不证明当前存在另一条构建路径。R1 显示 warning 并停止; 只有 R2/R3
通过独立回退门槛后才可自动尝试一次。`STATUS_FAILED` 默认不应静默吞掉, 应显示 `errors`; 完整性/签名失败不得回退,
其他失败是否允许用户显式重试或回退由 `docs/remote-build-fallback-decision.md` 约束。

结果字段:

| 字段 | 成功 | 含义 |
|---|---:|---|
| `outputApkFd` | 必有 | 只读输出 APK 描述符; 宿主复制后关闭 |
| `outputFileName` | 必有 | 消毒后的 APK 文件名 |
| `outputSizeBytes` | 必有 | 输出长度 |
| `outputSha256` | 必有 | 小写十六进制 SHA-256 |
| `updatedProjectConfigJson` | 必有 | 写入新 build id / number / time 后的项目配置 |
| `compatibilityLevel` | 必有 | `LEVEL_OK/WARN/BLOCK` |
| `warnings` | 始终存在 | 可展示但不一定阻止构建 |
| `errors` | 始终存在 | 阻止原因 |
| `extras` | 可选 | 当前含 source kind/path, builder 类型和插件沙箱工作区路径; 路径仅供诊断, 宿主不得直接访问 |

宿主把成功 callback 中的输出仍视为不可信输入. 当前 AutoJs6 的接收顺序是:

1. 拒绝负数、超过 2 GiB 或与实际复制量不一致的长度, 并复算插件声明的 SHA-256;
2. 在不解压 APK 的前提下限制最多 65,536 个 ZIP 条目, 拒绝重复、绝对/反斜杠、`.`/`..`、空段和控制字符路径;
3. 要求 `AndroidManifest.xml`、`resources.arsc`、`classes.dex` 与 `assets/project/project.json` 各自存在且非空;
4. 使用仓库内 `com.android.apksig.ApkVerifier` 验证实际 APK 签名, 要求 `isVerified=true` 且至少一个已验证证书;
5. 要求实际已验证证书恰好一个, 且其 SHA-256 与宿主在打开 Binder 会话前从所选默认或自定义 keystore 固定的证书摘要
   完全一致; 插件声明的摘要不能替代该比较;
6. 再由平台 `PackageManager` 解析归档, 要求包名、versionName、versionCode 与本地请求完全一致且签名者对平台可见;
   API 28+ 使用 `GET_SIGNING_CERTIFICATES`, API 24—27 使用兼容的 `GET_SIGNATURES` 分支;
7. 临时文件已 flush/fsync 且上述复核全部通过后才原子替换旧目标. 任一失败返回宿主侧 `OUTPUT_REJECTED`, 保留旧产物,
   清除临时文件, 不进入构建成功后的安装入口.

结构/签名错误只返回固定安全分类, 不拼接插件控制的 ZIP 条目名或底层签名诊断. 这些规则属于宿主接收边界, 不改变
插件 `ApkBuildResult.status` 的协议含义, 也不能被 `allowRiskyBuild` 绕过.

2026/08/31 的设备资格同时覆盖 API 36 与声明的最低 API 24: 两档均通过真实 Binder 双输出以及签名篡改、核心结构缺项、
请求身份错配的拒绝用例. API 24 复用与 API 36 完全相同的宿主、测试和资格插件 APK 字节, 并实际执行上述
`GET_SIGNATURES` 分支; 资格后删除了专用临时 AVD. 证据分别位于工作区外
`m3-4-g5-output-validation-2026-08-31` 与 `m3-4-g5-output-validation-api24-2026-08-31`.

2026/09/01 的补充审计发现“签名在密码学上有效”仍不足以证明输出使用了宿主所选签名身份。宿主现把默认公开证书摘要或自定义
keystore 中的证书摘要写入本地准备态, 不通过 Binder 信任插件上报值；输出必须是与该摘要精确匹配的单一已验证签名者。API 36
最终设备用例 3/3 覆盖错误预期签名者拒绝、旧产物保护和扩展名无关的自定义 BKS 解析，最终宿主真实 Binder 双输出 1/1；宿主
APK 只含公开摘要资产, 不含默认私钥库。44/44 JVM 回归、Release Kotlin 编译、独立 `apksigner`、清理与 0 findings/errors 扫描
证据位于工作区外 `m3-4-g5-signer-binding-2026-09-01`，其 manifest/SHA 清单摘要分别为
`e336b45b1350eb54faaf3ac22e783203bef6ee9cfa0005c65c75fec242b1c8c9` / `7e561fcd4b9fe3f3a291afdf6ef8d685595dd7beaa7b9eba4fd20ff697ab27b2`。

常见映射:

| 条件 | status | 信息位置 |
|---|---|---|
| 插件构建关闭远程能力 | `UNSUPPORTED` | warning: disabled |
| Node.js 旧库/项目类型/配置/入口/执行模式 | `UNSUPPORTED` | warning: use the external runtime plugin; 无输出 |
| 缺少必需原生输入或 `.so` | `UNSUPPORTED` | warning: missing build input |
| 宿主要求更高协议 | `FAILED` | error: newer protocol |
| 宿主身份不匹配且 `allowRiskyBuild=false` | `FAILED` | error: mismatch |
| 宿主身份不匹配且 `allowRiskyBuild=true` | 继续; 成功时 `OK` | warning: mismatch |
| 归档长度 / SHA / 路径 / 格式错误 | `FAILED` | error |
| 项目配置, Manifest / ARSC 或签名失败 | `FAILED` | error |
| `cancel()` / `close()` 或线程中断 | `CANCELLED` | 通常无 error |

## 6. 安全边界

- 所有跨进程大文件通过 `ParcelFileDescriptor` 传输, 不通过 Binder Bundle 携带正文.
- 宿主应填写长度与 SHA-256; 插件在解包前校验. 空文件始终拒绝.
- ZIP 解包先全量预检再写入, 执行路径规范化、目录逃逸、条目/大小/压缩比双重计数; 原生输入另有顶层目录与 ABI 白名单.
- 自定义签名口令仍是请求中的敏感数据; 宿主不得记录请求对象, 插件不得把口令写入结果或日志.
- TypeScript v3 一次性密钥只保护构建暂存传输, 使用后清零; 最终脚本使用项目运行时密钥重新加密.
- 输出 APK 必须由宿主再次校验 `outputSizeBytes/outputSha256`、有界 ZIP 结构、密码学签名、与会话前所选默认/自定义签名身份
  精确匹配的唯一签名者, 以及请求包身份, 再原子移动到最终位置; 校验失败不得覆盖旧产物或进入安装流程.
- `allowRiskyBuild` 不是安全校验总开关.

日志/callback/result、输出 APK、测试报告和关闭后工作区的敏感数据资格规则、脱敏扫描器及 2026/08/31 证据见
`docs/sensitive-data-audit.md`. 任一扫描 finding 或 scanner error 都是失败关闭条件.

## 7. 变更规则与验证

修改任一 AIDL 方法, Parcelable 字段语义, extra 键, status / step 常量或加密信封格式时:

1. 判断是否需要提升 `REMOTE_BUILD_VERSION`.
2. 同步插件 `BuildConfig.REMOTE_BUILD_PROTOCOL_VERSION` 与 AutoJs6 Runtime Kit 的
   `contract.remoteBuildProtocolVersion`.
3. 更新 `ApkBuilderTemplateCapabilityKeys`, 本文和宿主消费代码.
4. 为向后协商和拒绝路径补用例.
5. 运行 `RemoteApkBuildSessionInstrumentedTest` 与发布演练.

仓库还提供字段覆盖门禁：

```powershell
python scripts/verify_remote_build_protocol_docs.py
```

当前门禁从 AIDL/Kotlin 真源提取 10 个远程方法、20 个 request 字段、10 个 result 字段、7 个 progress 字段、11 个
extras 键、4 个远程 capability 键、4 个 status、6 个 step 与 1 个协议常量，共 73 个符号，并要求本文逐项出现。新增公开
字段却未更新本文时 CI 失败。该检查只保证覆盖，不判断文字语义是否正确；G7 仍要求人工逐项复核。

本仓库的会话端到端测试:

```powershell
$env:ANDROID_SERIAL = "<test-device-serial>"
.\gradlew.bat :app:connectedDebugAndroidTest --no-daemon
```

测试使用真实 Runtime Kit, 覆盖目录源 / 单文件源, 成功, 取消, `UNSUPPORTED`, 严格 / 宽松宿主不匹配, 功能关闭与
高版本协议拒绝. 详细环境与场景映射见 `docs/remote-build-e2e-drill.md`.
