# 远程构建默认启用与回退标准

- 决策状态: 标准已制定, 尚未达到默认启用条件
- 当前阶段: R0 (开发者实验)
- 当前发布行为: 官方插件 `supportsRemoteBuild=false`; 仅自行构建并传入
  `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true` 时启用; AutoJs6 宿主的远程构建开发者开关默认关闭
- 关联: ROADMAP M3-3 (标准制定), M3-4 (仓库内工程门槛), M8 (外部门槛与放量), `docs/evidence/m3-4-gates.md`,
  `docs/remote-build-fallback-decision.md`, `docs/remote-build-node-packaging-decision.md`

本文定义远程轻量构建从 experimental 进入 AutoJs6 默认路径之前必须满足的证据, 分阶段放量方式和回退责任. 它不是
“已经可以默认开启”的声明. 任一硬门槛缺少可复核证据时, 结论都是维持关闭.

## 1. 目标与非目标

默认启用的目标是让宿主在插件明确支持且协议匹配时优先使用插件侧轻量构建, 并在 R2 前具备一条真正独立于失败远程
会话的安全构建回退路径. 当前宿主没有可用的本地构建器, 因而 R1 只能是默认关闭的显式试用.
默认启用不意味着:

- 把关闭远程总开关称为已完成构建回退;
- 把 `allowRiskyBuild` 默认改为 `true`;
- 遇到完整性, 配置或签名错误时无条件静默回退;
- 放宽插件与 Runtime Kit 的配对要求;
- 将实验性协议承诺为永久不变的公开 API.

## 2. Go / No-Go 硬门槛

以下 G1—G7 必须同时通过. “局部通过”, “仅开发者设备可用”或“没有收到问题反馈”均不能替代量化证据.

### G1 —— 协议与功能正确性

1. `docs/remote-build-e2e-drill.md` 的 15 条当前基线用例在 API 24, API 28/29 和当期 target API 三类设备上通过;
   至少覆盖 arm64-v8a 与 armeabi-v7a, 有可用 x86_64 模拟器时纳入 CI.
2. 单文件源与项目目录源都验证输出 APK 的长度, SHA-256, 核心 ZIP 条目, Manifest 包名 / 版本 / 权限,
   `resources.arsc`, `assets/project/project.json` 和主脚本可读性.
3. 增补并通过 TypeScript v3 三类用例: 正常认证解密并重加密, 密文 / tag 被篡改, 路径清单缺项或多项.
4. 增补并通过默认签名库与自定义签名库, V1 + V2 至少两种签名方案, 图标替换, ABI 裁剪以及当前支持的原生能力标签.
5. 协议高低版本协商, `allowRiskyBuild=true/false`, `UNSUPPORTED`, `FAILED`, `CANCELLED` 的 callback / status /
   compatibilityLevel / warnings / errors 映射与 `docs/remote-build-protocol.md` 完全一致.
6. 插件构建关闭能力时, 直接调用会话只能得到 `STATUS_UNSUPPORTED`, 不得绕过 feature gate.

2026/08/31 的 G1 资格快照:

| 子项 | 当前证据 | 状态 |
|---|---|---|
| 1 API / ABI | API 24/x86、API 28/armeabi-v7a、API 28/arm64-v8a、API 29/x86 均为完整矩阵 14/14 + 较低协议聚焦 1/1; API 36/x86_64 另有整类 15/15 集成复跑 | 通过 |
| 2 输出结构与元数据 | 五个成功构建均核对长度、SHA-256、核心 ZIP 条目、项目配置、包名、版本、应用名与签名证书; 权限和 `resources.arsc` 图标路径另有聚焦断言; 当前 5277 宿主又分别用项目目录源与单文件源完成真实远程构建、安装、冷启动与主脚本 UI/日志执行闭环 | 通过 |
| 3 TypeScript v3 | 认证解密后最终重加密、篡改 tag、密文漏列和清单多项均有设备负向/正向用例 | 通过 |
| 4 签名 / 图标 / ABI / native | 默认库 V1、自定义 BKS FD + V2-only、精确图标像素、单 ABI 裁剪、OpenCV 与 Image Quantization 五个 native 库均通过 | 通过 |
| 5 协议与终态映射 | 当前协议、宿主要求更新协议、宿主要求较低协议、strict/risky、区间外阻止以及 `OK/UNSUPPORTED/FAILED/CANCELLED` 已覆盖 | 通过 |
| 6 关闭态门禁 | 会话入口强制返回 `STATUS_UNSUPPORTED`, 并清理 FD、TypeScript 密钥元数据与签名口令引用 | 通过 |

G1 运行时闭环证据: 2026/08/31 在隔离 API 36/x86_64 AVD 上, 当前宿主 `6.8.0 / 5277` 经真实
host → Binder → qualification plugin 路径依次生成项目目录源和单文件源两个 APK. 两个输出均由宿主先核对 size/SHA-256
再原子发布, 随后 `adb install` 成功并从 `SplashActivity` 冷启动到前台 `ScriptExecuteActivity`; UI hierarchy 各命中两处唯一
`runtime smoke OK` 文本 (text + content-description), 各自脚本日志哨兵恰好 1 次, `FATAL EXCEPTION` 为 0. 两个 APK
均为 x86_64、V2 签名且共享 Runtime Kit 默认签名证书. producer instrumented test 1/1、21.888 秒通过, 两次构建各有
31 个进度回调, 私有源夹具已删除、请求暂存为空且宿主开关恢复关闭. 精确 APK、截图、UI XML、三份 logcat、元数据与
15/15 SHA-256 清单见工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g1-runtime-smoke-2026-08-31\manifest.json`. 因而 G1 六项在
当前资格范围内全部通过; 该结论不替代 G3 的多语言/能力双构建器等价性矩阵.

### G2 —— 稳定性与资源清理

在拟发布的宿主, 插件和 Runtime Kit 三元组上完成一次资格批次:

- 至少 200 次自动化构建, 每个 G1 核心设备档位与每种源形态不少于 30 次;
- 总成功率不低于 99%, 且不存在可归因于远程构建的崩溃, ANR, Binder 死亡或设备重启;
- 任何一次失败都必须进入且只进入一个终态回调, 并提供非空可行动的 warning 或 error;
- 在 PREPARE / BUILD / SIGN 各阶段至少执行 10 次取消注入, 全部进入 `STATUS_CANCELLED`;
- 每台设备连续 30 次成功 + 取消混合循环后, `cacheDir/remote-apk-build/session-*` 为零残留;
- 输入与输出 ParcelFileDescriptor 数量回到基线, 不出现 “too many open files” 或跨会话句柄增长;
- 同一进程连续构建不同包名 / 配置时无前一会话文件或配置串入.

99% 门槛只允许已分类且有修复 / 回退结论的非确定性设备问题; 任一数据损坏, 错签, 泄露明文或构建错误应用的案例均是
直接 No-Go, 不能由总体成功率抵消.

2026/09/01 的 G2 资格批次使用同一最终功能三元组: AutoJs6 `6.8.0 / 5277` 宿主 APK SHA-256
`aa92d07a80c0a6a6a761026d478435eaca752ac6429535d89fc43c9995999a31`, 资格插件 APK SHA-256
`bcafedf7ed0c9e539824ff7f530c4a4bf35d7d2b6679044452fa830a19343370`, 以及 5277 universal Runtime Kit / 远程协议 v3.
三个全新隔离 AVD 的接受结果为:

| 档位 | 成功构建 | 取消注入 | 成功耗时 p50 / p95 / max | 宿主 FD 基线 / max / final | 插件 FD 基线 / max / final |
|---|---:|---:|---:|---:|---:|
| API 36 / x86_64 | 目录 30 + 单文件 30 | PREPARE 10 + BUILD 10 + SIGN 10 | 7,236 / 7,822 / 14,575 ms | 121 / 121 / 119 | 89 / 89 / 89 |
| API 29 / x86 | 目录 30 + 单文件 30 | 0 | 10,161 / 10,867 / 12,164 ms | 65 / 67 / 64 | 42 / 42 / 42 |
| API 24 / x86_64 | 目录 30 + 单文件 30 | 0 | 7,186 / 7,503 / 7,778 ms | 55 / 55 / 54 | 36 / 36 / 35 |

合计 180/180 正常构建成功, 30/30 预期取消进入唯一 `STATUS_CANCELLED`, 即 210/210 接受结果、0 次非预期失败。每次正常
构建都验证唯一脱敏终态、请求包名/版本/项目配置与当前会话 marker, 因而 180 次均覆盖跨会话污染; 三档都保持单一插件 PID,
最终宿主暂存与插件工作区为 0。三份权威日志中 `FATAL EXCEPTION`、ANR、`BINDER_LOST`、宿主/插件进程死亡和设备重启均为 0。
API 24 的旧 `UiAutomationConnection` 无法从 system 侧执行 `run-as`, 因此该档位在预热、第 30 次、第 60 次及最终清理四个
显式暂停点由 exact-serial adb 探针核对插件 PID/FD/工作区后 ACK; API 29/36 继续使用每次会话后的进程内检查。

资格过程中发现并修复了三项真实缺陷: 启动早期取消可能误归为 `FAILED`, 插件关闭与构建线程竞态可能令已删除工作区复活,
以及 Android 9–11 对有效 V2/V3 APK 的现代归档签名者视图可能为空。最后一项只在 `apksig` 密码学验证成功且现代视图为空时
回退到已验证的旧签名者视图, 没有放宽签名门槛。宿主聚焦单测 36/36、插件 app 24/24、插件 API 7/7 通过, 两仓库 Release
Kotlin 编译退出码均为 0。旧 API 36 零失败批次因早于 Android 9–11 兼容修复而不计入最终三元组; 外部旧会话干扰、错误
Runtime Kit/测试夹具、一次性 FD 采样和 API 24 探针错误均以 0 资格计分保留。三台资格 AVD 与一次被替换的旧 API 36 AVD
均已删除, 官方源码和 Debug/Release/AndroidTest BuildConfig 都恢复 `ENABLE_REMOTE_BUILD=false`。完整 APK、Runtime Kit、原始/过滤
日志、尝试历史、设备清理、JUnit XML、脱敏扫描和逐文件 SHA-256 位于工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g2-stability-2026-08-31\manifest.json`。G2 至此通过; G3 已由下节
2026/09/01 的独立等价性批次关闭；在该快照时 G4、G5 独立安全审查、G7 与 R2/R3 独立回退门槛仍使项目保持 R0 / No-Go。

### G3 —— 与宿主原有构建器的产物等价性

对同一组固定项目分别使用宿主原有构建器与远程构建器, 至少验证:

- 两个 APK 都能安装, 首次启动, 执行主脚本并完成一次冷启动 / 热启动;
- 应用名, 包名, versionName / versionCode, 权限, splash 配置, 图标与签名方案符合相同项目配置;
- JavaScript、UI 脚本、编译后 TypeScript 与当前自包含原生能力的运行结果一致; Node.js 由外部 Runtime 插件拥有,
  相关旧配置和入口必须按 `docs/remote-build-node-packaging-decision.md` 在宿主与插件两侧失败关闭;
- ABI 选择后的 APK 不含未选 ABI, 且目标设备所需 `.so` 完整;
- 项目 build number / id / time 的推进语义与宿主保存项目配置的逻辑一致;
- 安装升级路径至少覆盖 “旧宿主构建器产物 → 远程构建产物” 和反向回退产物, 签名一致时均可升级.

二进制逐字节一致不是目标, 可观测功能, 元数据, 签名与升级行为一致才是验收项.

2026/09/01 的 G3 资格批次冻结旧宿主构建器提交 `fff913caafa3dc0d6172638c8532b027c0dfa8c0`, 并与当前
5277 Runtime Kit / 远程构建器在同一临时 API 36/x86_64 AVD 上比较。接受结果为:

| 子项 | 当前证据 | 状态 |
|---|---|---|
| UI 静态等价 | 本地 3 包 + 远程 4 包, 应用名/包名/版本/权限/splash/图标/V2 单签名者、x86_64 与 build 元数据 10/10; 同版本远程大小差异 1.106526% | 通过 |
| UI 运行与升级 | `local 100 -> remote 101 -> local 102`、`remote 200 -> local 201 -> remote 202` 两条三步序列; 每步安装/升级、冷/热启动、前台 Activity、UI/日志均通过 | 通过 |
| TypeScript | 本地 300 与远程 301 静态/运行结果一致, 无 TypeScript Runtime 插件时 checksum 均为 108; 远程大小差异 -1.105671% | 通过 |
| Image Quantization | 本地 400 与远程 401 的 native/license 内容和运行结果一致, 无 ImageQuant Runtime 插件时均为 1,181 bytes / checksum 83,471,039; 远程大小差异 -1.085675% | 通过 |
| 高级静态/运行矩阵 | TypeScript + Image Quantization 静态 21/21; 两能力各完成本地→远程两步安装升级及四步冷/热启动 | 通过 |
| Node 架构边界 | 宿主 JVM 3/3 + 设备 1/1, 两请求在预处理前拒绝、progress=0、staging 未创建; 插件 JVM 4/4 + 直接会话设备 1/1, 两请求 `UNSUPPORTED`、BUILD/SIGN=0、output=0、工作区=0 | 通过 |

资格过程发现并修复了远程 TypeScript launcher 把 `"use strict"` 放在 `ui` 指令之前、导致 UI 模式丢失的问题; ImageQuant
夹具的全局名冲突、Node token 日志和测试/签名尝试也按尝试史保留, 未用最终结果覆盖。Node 不再作为内嵌 APK 能力,
其运行时所有权和未来重新引入条件由 `docs/remote-build-node-packaging-decision.md` 固化。最终生成资产对应的插件 APK 又通过
完整设备类 41/41、139.568 秒回归, 41 个 start/finish 配对、fatal=0、工作区=0。完整 7 个 UI 包、4 个高级能力包、
两条三步升级序列、四步高级运行证据及双边 Node 门禁位于工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g3-equivalence-2026-09-01`。至此 G3 通过; 在该快照时项目仍因 G4、G5 独立
安全审查、G7 与 R2/R3 独立回退保持 R0 / No-Go。G4 已由下述 2026/09/01 追加资格关闭。

### G4 —— 性能与设备压力

在 G1 三类 API 档位上记录同项目的宿主原有构建器基线和远程构建数据. 默认启用要求:

- 远程构建 p95 总耗时不高于同设备原有构建器的 1.5 倍;
- 输出 APK 大小差异不超过 2%, 超出时必须有明确的条目级解释;
- 峰值 PSS 不触发 low-memory kill, 在最低配置测试设备上连续 10 次构建无 OOM;
- 构建所需临时可用空间有可计算的预检值; 空间不足在写入大文件前失败并给出明确 error;
- 取消请求在各阶段发出后 5 秒内进入终态 (单次不可中断的系统签名调用可单独记录, 但必须有上界).

若远程路径显著更慢但解决了宿主稳定性问题, 只能通过一次记录理由, 数据与用户影响的显式 ADR 调整阈值, 不得临时忽略.

2026/09/01 的 G4 资格结论为 **通过**：

| 档位 | 本地 60 次 p95 | 远程 60 次 p95 | 比率 | 远程 PSS 宿主 / 插件 / 同采样合计峰值 | 压力判定 |
|---|---:|---:|---:|---:|---|
| API 36 / x86_64 | 6,429 ms | 7,254 ms | `1.128325x` | 225,989 / 101,527 / 316,334 KiB | 通过 |
| API 29 / x86 | 21,448 ms | 10,260 ms | `0.478366x` | 219,161 / 98,075 / 315,003 KiB | 通过 |
| API 24 / x86_64, 1 GiB 特征档 | 8,293 ms | 7,359 ms | `0.887375x` | 126,391 / 73,254 / 188,511 KiB | 性能通过; 远程启动期出现 2 个非目标 LMK, 压力资格失败 |
| API 24 / x86_64, 1.5 GiB 最低合格档 | 复用上行本地基线 | 7,057 ms | 参考 `0.850959x` | 123,117 / 57,356 / 176,349 KiB | 唯一窗口内 LMK/OOM/目标死亡/fatal 均为 0, 60/60 通过 |

三档严格同配置性能比率均小于 `1.5x`。G3 的基础、TypeScript、Image Quantization 同版本大小差异为
`1.106526%`、`-1.105671%`、`-1.085675%`, 绝对值均小于 2%。G2 的 PREPARE/BUILD/SIGN 各 10 次取消为
30/30, 最慢终态 153 ms。插件新增按模板/项目/native 展开量、压缩输入、三份构建树副本和 256 MiB reserve 计算的空间
预检, 在工作区/大输入写入前执行; API 36 的空间不足与 native 零展开量故障注入各 1/1 失败关闭。

1 GiB 档位的目标宿主/插件虽完成 60/60, 但不能以“目标没死”掩盖系统 LMK, 因而只保留为边界特征；1.5 GiB 档位才是
当前最低资格配置。四个本阶段临时 AVD 均已删除, 实体机定向命令为 0, 官方源码及最终 Debug/Release/AndroidTest
`ENABLE_REMOTE_BUILD=false`。方法、公式、过程失败、限制与工作区外证据见 `docs/remote-build-performance-audit.md` 和
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g4-performance-pressure-2026-09-01`。G4 通过不启动 R1；项目仍受 G5
独立安全审查、G7、完整预览周期及 R2/R3 独立回退阻断。

### G5 —— 完整性与安全

- 项目, 原生输入和自定义签名库的 size / SHA-256 正反用例齐全;
- ZIP Slip, 绝对路径, `..`, 非法原生输入顶层目录和畸形 ZIP 均在解包目标外写入前失败;
- TypeScript 暂存密钥在读取后从 Bundle 移除并清零, 成功 / 失败 / 取消后工作区不留明文;
- 日志, callback, `ApkBuildResult.extras` 和测试报告不包含签名口令, TypeScript 一次性密钥或脚本明文;
- 宿主复制输出时复核 size / SHA-256、APK 结构、密码学签名、与会话前所选默认/自定义签名身份精确匹配的唯一签名者及包身份;
  校验失败不安装, 不覆盖已有产物;
- fuzz / 畸形输入测试对 AIDL Parcelable, JSON, ZIP 和 Manifest / ARSC 编辑入口无崩溃与目录逃逸;
- 完成一次插件侧安全审查, 阻断级与高危问题为零.

2026/08/31 的 G5 资格快照:

| 子项 | 当前证据 | 状态 |
|---|---|---|
| project/native/keystore 完整性 | 三类 FD 的正常 size/SHA-256 随成功构建通过; 六个独立 mismatch 用例均为 `STATUS_FAILED` / `LEVEL_BLOCK`, 无输出、FD 关闭、工作区清理 | 基础正反用例通过 |
| ZIP / path / JSON | API 36/x86_64 已覆盖 `../`、POSIX/Windows 绝对路径、`SOURCE_PATH` 越界、非法 `dex/` native 顶层、非 ZIP、截断/相邻根 JSON、声明压缩输入上限、16,384 条目上限与 250:1 压缩比; 统一解包器先全量预检, 再以实际流量复核单条/总解压与单条/累计压缩比, 路径另限 4,096 UTF-8 bytes / 255-byte 单段 / 128 段, 恶意文件名不回显 | 有界 ZIP/path 与确定性 JSON/path 畸形子项通过 |
| TypeScript 密钥、口令与明文 | 正常、认证失败、取消与关闭态均核对密钥清零/Bundle 移除、签名口令置空和工作区清理; 聚焦设备用例同时扫描 callback/progress/result 与成功 APK, 离线扫描覆盖本次/历史证据文本及两仓库报告 | 敏感数据子项通过; 设备 1/1、扫描器专项 8/8、四批扫描均为 0 findings/errors |
| 宿主输出复核 | 宿主在同目录临时文件上依次复核 2 GiB 传输上限、size/SHA-256、65,536 条目内的安全 ZIP 结构、四个非空核心组件、`apksig` 密码学签名、与会话前所选默认/自定义签名身份精确匹配的唯一签名者、平台签名可见性及请求包名/versionName/versionCode, 全部通过后才原子替换; 传输单测 7/7、结构策略单测 5/5、签名者解析/绑定单测 5/5, API 36 与最低 API 24 的真实 Binder 正样本、篡改/身份拒绝以及 API 36 错误签名者拒绝均通过 | 宿主输出复核子项通过; 校验失败保留旧产物、清理临时文件且不进入安装入口 |
| fuzz / 安全审查 | 40 条 Parcelable/Bundle + 32 条 JSON/Manifest/ARSC 确定性语料为 72/72 unique cases; JVM 24/24、API 36 定点 2/2、完整设备类 39/39; 最终日志/生产 APK/JVM XML 扫描 0 findings/errors | 确定性 fuzz 子项通过; 独立插件安全审查未通过 |

API 36 的最终整类复跑为 31/31 (原 15 条功能基线 + 16 条 G5 负向输入), 0 skipped, 0 failed, Gradle exit 0; 独立
`RemoteZipExtractorTest` 为 12/12, 聚焦设备注入为 6/6. JUnit、HTML、精确测试 APK、资格 Kit、过程失败与逐文件 SHA-256
见工作区外 `m3-4-g5-bounded-zip-2026-08-31`; 该结果关闭有界 ZIP/path 子项, 但不代表整个 G5 完成.

宿主输出复核追加证据: 当期 AutoJs6 `6.8.0 / 5277` 把插件 callback 输出先写入目标同目录临时文件, 完成
`RemoteBuildApkArchivePolicy`、`ApkVerifier` 和 `PackageManager` 身份三层复核后才发布. JVM 侧
`RemoteBuildOutputTransportTest` 7/7 与 `RemoteBuildApkArchivePolicyTest` 5/5 通过; 同一隔离 API 36/x86_64 AVD 上,
真实 host → Binder → qualification plugin producer 1/1 通过并生成两个 30,519,837-byte V2 APK, 随后输出资格用例 1/1
通过: 正样本接受, 重写脚本但保留核心结构的 APK 因签名失效被拒, 缺失 `classes.dex` 与包身份不匹配均被拒, 旧输出字节
不变, 临时残留和目标包安装数均为 0. 首次负样本运行因设备 JUnit 不含 4.13 `ThrowingRunnable` 在进入测试方法前失败,
改用兼容断言后最终 32.93 秒通过; 两次尝试均保留. 精确宿主/测试/资格插件 APK、两份正样本、独立 `apksigner` 输出、
日志和测试 XML 位于工作区外 `m3-4-g5-output-validation-2026-08-31`. 该结果只关闭宿主输出复核子项; 当时整个 G5 仍受
更广 fuzz、系统化敏感信息审计与独立安全审查阻断; 后续敏感信息子项的关闭证据见下文.

最低 SDK 补充资格复用上述 API 36 证据中的同字节宿主、Android-test 与资格插件 APK, 在专用 API 24/x86_64 AVD 上实际
走过旧平台 `PackageManager.GET_SIGNATURES` 分支. 真实 Binder producer 1/1、21.847 秒通过并生成两个 30,519,837-byte
V2-only APK; 输出资格用例 1/1、12.719 秒通过, 同样得到正样本接受、签名/结构/身份负样本拒绝、旧产物保留、临时残留 0、
安装尝试 0、目标包 0. 两个输出另经 `apksigner`、包身份、3,667 条目及四个核心组件独立复核. 测试包、资格插件、宿主与
设备产物均已清理, 临时 AVD 随后通过 AVD 管理器删除; 15 个证据文件与 14/14 哈希重放见工作区外
`m3-4-g5-output-validation-api24-2026-08-31`. 这项补充验证最低 SDK 分支, 不改变整个 G5 与 R0 的状态.

2026/09/01 对既有输出门禁做组合审计时发现一项真实缺口：有效 `apksig` 签名和正确包名/版本仍可能来自另一张证书。宿主现于
打开 Binder 会话前，从所选默认公开摘要或自定义 keystore 解析并固定预期证书 SHA-256；输出必须只有一个已验证签名者且摘要
精确匹配，否则在临时文件阶段返回 `OUTPUT_REJECTED`。最终宿主 APK 的公开摘要资产为 1、默认私钥库资产为 0。宿主 APK Builder
JVM 包 44/44、API 36 输出资格 3/3、最终宿主真实 Binder 双输出 1/1 与 Release Kotlin 编译均通过；错误签名者拒绝保持旧输出、
临时残留/安装尝试为 0，扩展名无关的自定义 BKS 解析在 Android 上通过。文本 39 files 与 4 APK / 12,678 entries 扫描均为
0 findings/errors；资格包、测试包和三份设备夹具已清理，总开关为 `false`，AVD 已停止，实体机定向操作为 0。证据位于工作区外
`m3-4-g5-signer-binding-2026-09-01`，manifest/SHA 清单摘要为
`e336b45b1350eb54faaf3ac22e783203bef6ee9cfa0005c65c75fec242b1c8c9` / `7e561fcd4b9fe3f3a291afdf6ef8d685595dd7beaa7b9eba4fd20ff697ab27b2`。
该修复重新收口仓库内输出复核子项，但不替代独立安全审查，G5 与 R0 状态不变。

敏感数据追加证据: `scripts/audit_sensitive_data.py` 以规则文件驱动固定资格哨兵和 11 类高置信凭据模式, 流式扫描且报告
从不回显命中值或绝对扫描根. 新增设备用例在成功、TypeScript 认证失败和启动前取消三种终态中检查 callback/progress/
result、成功 APK 原始字节与所有解压条目、密钥清零、口令置空及关闭后工作区, 最终 1/1、265.37 秒通过. 扫描器专项
8/8、仓库 Python 全回归 18/18 通过; 本次资格文本 15 files、主插件 APK 1,124 entries、历史资格证据文本 2,373 files、
两仓库当前报告 19 files 四批均为 0 findings/errors. 插件本轮报告在 IDE 清理 `app/build` 前已复制进资格证据并由第一批
覆盖. 资格包已卸载, 宿主暂存工作区不存在, 未操作实体机. 规则、命令、受控排除与局限见
`docs/sensitive-data-audit.md`, 完整证据见工作区外 `m3-4-g5-sensitive-data-audit-2026-08-31`. 该结果关闭
敏感数据/关闭后工作区子项; 后续确定性 fuzz 已由下段关闭, 整个 G5 仍受独立安全审查阻断.

确定性畸形输入追加证据: 新策略对 AIDL/Bundle 执行键白名单与严格类型/长度, 对自定义 keystore 同时限制声明和实际 FD
读取为 64 MiB, 对 `project.json` 限制 512 KiB/64 层并拒绝尾随或相邻根和类型强转, 对路径限制总 4,096 bytes、单段
255 bytes 与 128 段, 对图标限制 16 MiB/4,096 单边/4,194,304 pixels, 并在 ARSC 固定包名槽和派生输出名处保留终止/后缀
预算. JVM 24/24、API 36/x86_64 定点 2/2 与 72/72 unique cases、最终整类 39/39 均通过; 生成十语言 changelog 后以最终生产
APK 字节再次完整通过 39/39、141.647 秒. 该次 1,515-line logcat 中 72 条唯一安全终态齐全、四类哨兵与 FATAL/crash 均为 0,
最终日志、生产 APK 1,124 entries 与 JVM XML 的扫描为 0 findings/errors. 无效的首轮假覆盖、`ENAMETOOLONG`、37/39 夹具失败、
审查前 39/39 和人工审查后但文档生成前的 39/39 均在尝试历史中保留. 详见
`docs/remote-build-fuzz-audit.md` 与工作区外 `m3-4-g5-deterministic-fuzz-2026-08-31`. 该结果关闭确定性 fuzz 子项, 但不替代
独立安全审查, 所以整个 G5 仍未通过.

### G6 —— 宿主开关, 回退与可观测性

宿主侧合入并验证以下控制面后才可进入放量:

1. **能力门禁**: 只有 `supportsRemoteBuild=true` 且协商版本满足请求时才调用远程会话.
2. **总开关**: 提供无需替换插件即可关闭远程路径的宿主开关; 默认关闭, R1/R2 阶段必须能由开发者 / 测试渠道控制.
3. **分阶段失败与回退策略**:
   - R1: `NOT_AVAILABLE`, `UNSUPPORTED` 与 `FAILED` 均终止本次构建, 不自动回退; `CANCELLED` 保持用户取消语义;
   - R2/R3: `STATUS_UNSUPPORTED` 自动且只回退一次到独立构建路径; `STATUS_FAILED` 默认展示错误, 只有明确判定为可安全
     重试的类别才允许用户主动回退; `STATUS_CANCELLED` 不回退;
   - 所有阶段: 输出完整性或签名校验失败禁止自动回退掩盖问题, 并记录阻断事件.
4. **循环保护**: R1 每个用户请求最多一次远程尝试; R2/R3 最多一次远程尝试和一次独立回退尝试, 两条路径共享取消状态.
5. **结果落盘**: 先复制到宿主私有临时文件并校验, 成功后原子移动到用户目标; 旧产物不得被失败请求截断.
6. **诊断记录**: 仅记录协议版本, status, 阶段, 耗时, 大小和脱敏错误分类; 不记录项目正文, 密钥或口令.
7. **Binder 失联**: 插件进程死亡 / callback 失联被转换为明确失败并关闭 FDs 与会话; R1 停止本次构建, R2/R3 才允许用户
   选择经过验证的独立构建路径重试.

2026/08/31 的宿主 G6 源码与设备审计快照:

| 子项 | 当前实现 | 状态 |
|---|---|---|
| 1 能力门禁 | `ApkBuilderTemplatePluginHost` 只保留官方身份、宿主区间/ABI 兼容、`supportsRemoteBuild=true` 且远程协议版本不低于请求的候选; 已安装且明确声明 `supportsRemoteBuild=false` 的真实插件会在 PREPARE 前被拒绝并返回可行动提示 | R1 负向设备用例 1/1 通过 |
| 2 总开关 | AutoJs6 开发者选项新增默认关闭的远程 APK 构建开关; 客户端、候选解析与会话打开均失败关闭, 模板读取和密钥库操作不受影响 | R1 控制面与 API 36 设备 UI / 持久化演练通过 |
| 3 回退策略 | 已采纳分阶段修订门槛: R1 明确告知失败即停止且不自动回退; R2/R3 仍要求真正独立路径 | R1 语义已实现; R2/R3 未通过 |
| 4 循环保护 | R1 当前每次请求只执行一次远程尝试; 尚无可计数的一次独立回退尝试 | R1 通过; R2/R3 未通过 |
| 5 结果落盘 | `RemoteBuildOutputTransport` 在目标同目录写临时文件, 校验大小/SHA-256 并 flush/fsync, 再执行有界 APK 结构、`apksig` 签名和请求包身份复核后原子替换; 取消、超限、摘要或 APK 复核失败均保留旧产物且清除临时文件 | 通过; 传输 7/7 + 结构策略 5/5 单测, API 36 正/负设备用例各 1/1 |
| 6 诊断记录 | 宿主仅用枚举/数值记录协议、终态、阶段、耗时、大小和错误分类; 进程内环形缓冲区最多 32 条, 不接受自由文本且只输出白名单日志行 | 实现与聚焦单测 4/4 通过; API 36 真实 Binder 死亡日志审计通过, 唯一白名单终态且三组敏感哨兵 0 命中 |
| 7 Binder 失联 | 客户端注册 `linkToDeath`, 250 ms 轮询 Binder/取消, 设置 12 分钟无进度与 30 分钟总上限, 最终关闭会话、binding lease 与输出 FD | 等待策略单测 4/4、真实 Binder 死亡设备用例 1/1 通过; 旧产物、暂存清理、进程重启和即时重新发现均已验证 |

总开关设备证据: 2026/08/31 将当期 AutoJs6 x86_64 debug APK (`versionCode=5277`, SHA-256
`3d6611970d981089712047f424a1248ef233026bf9a14644d183e34bc9d1b274`) 全新安装到隔离的 API 36 / x86_64 AVD.
开发者选项中 APK Builder 分类、R1 “失败停止且没有自动本地回退”说明和关闭态均可见; UI 与持久化值完整经历
`false -> true -> false`, 最终恢复关闭. 截图、UI hierarchy 与机器可读清单位于工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-r1-host-gate-2026-08-31\manifest.json`. 本次没有操作实体机,
也没有把“开关可关闭”计作独立构建回退证据.

Binder 死亡与日志设备证据: 同一隔离 AVD 上安装与宿主同签名、显式开启远程能力的临时 universal 资格插件, 只在插件侧
首个 `Reading remote build request` 回调到达后精确执行一次 `am force-stop`。宿主在 1.419 秒内只记录一条
`FAILED / WAIT / BINDER_LOST` 白名单终态, 保留原目标 APK, 清除私有请求暂存, 随后重新发现由 Android 重启的插件进程。
首轮演练还暴露并修复了通用 `AidlPluginHost` 的连接池竞态: 旧绑定的多个终态回调可能让迟到的解绑误伤替代绑定; 现在按
对象身份先淘汰旧池项, 替代绑定改用新的 `ServiceConnection`。最终 hostile instrumented test 为 1/1, 3.396 秒通过;
完整 logcat 中三组路径/脚本/旧产物哨兵均为 0 命中。精确 APK、日志、SHA-256 与尝试历史见工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g6-binder-death-2026-08-31\manifest.json`。资格插件不改变官方
`supportsRemoteBuild=false` 状态, 本次也没有操作实体机或发布资产.

能力门禁设备证据: 在同一隔离 AVD 上换装与宿主同签名、元数据明确含
`supportsRemoteBuild=false` 的 universal 资格插件, 临时打开宿主 R1 开关后执行独立 instrumented test. 宿主候选解析返回空,
客户端在 PREPARE / 会话建立前以唯一一条 `NOT_AVAILABLE / DISCOVERY / PROVIDER_NOT_AVAILABLE` 终态停止, 返回“安装声明该
能力的兼容插件或关闭开发者开关”的本地化提示; progress callback 为 0, 未创建私有请求暂存, 原目标 APK 保持原字节.
完整 logcat 中路径与源码哨兵均为 0 命中, 测试 1/1、0.81 秒通过, 最终开关恢复关闭. 精确 APK、日志和 7/7 SHA-256
清单位于工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g6-capability-gate-2026-08-31\manifest.json`。至此 G6 的 R1
控制面七项均有源码、单测或设备证据; 这不满足 R2/R3 所需的独立构建回退, 也不改变当前 R0 / 官方能力关闭状态.

AutoJs6 提交 `b2fd2b6ae` 已将原进程内 `ApkBuilder` 实现迁出宿主, 现有同名类只是远程协议兼容适配器. 历史提交
`46ef1ed45` 曾在远程失败后回退本地构建, 但依赖的是后来被移除的完整进程内实现. 2026/08/31 已在
`docs/remote-build-fallback-decision.md` 采纳“分阶段修订门槛”: R1 可在默认关闭和显式试用前提下失败即停止, 但关闭开关
仍不算回退; 真正独立的回退路径、一次性循环保护和产物等价性仍是进入 R2/R3 的硬门槛.

### G7 —— 发布, 文档与支持准备

- M3-1 用例扩展结果, G2 资格批次数据, G3 对比表和 G4 性能数据作为发布证据归档;
- `docs/remote-build-protocol.md` 与当期 AIDL / Parcelable / capability 常量逐项复核;
- 10 语言 README 的功能, 能力边界和 FAQ 从 “官方构建默认关闭” 同步到实际放量阶段;
- 11 份 `plugin_instruction.md` 同步当前状态和安全回退方式;
- CHANGELOG 明确宿主最低版本, 插件版本, Runtime Kit 配对版本, 是否默认启用以及关闭方法;
- Issues 模板要求附带脱敏后的 host/plugin/runtime-kit 版本, status, step 和 error 分类;
- 至少一名宿主维护者与一名插件维护者对 Go 证据签字确认.

2026/09/01 已完成 G7 的仓库内准备部分：`docs/remote-build-release-evidence-index.md` 汇总 G1—G6 的内容寻址证据与
空白批准栏；`scripts/verify_remote_build_protocol_docs.py` 从 AIDL/Kotlin 真源检查本文 73 个公开符号，当前 73/73 且两条
故障注入测试通过，并已接入文档一致性工作流；插件与宿主仓库均新增 `remote_apk_build.yml` Issue 表单，强制填写脱敏后的
版本、Runtime Kit、status、step、error 分类与旧产物状态。`docs/remote-build-independent-security-review.md` 则固定 G5 外部
审查范围和决定模板。

这些准备不等于 G7 通过。当前本地 Release 变体未配置正式签名，`apksigner` 明确拒绝，不能作为发布候选；G5 独立审查、
受信工作流生成的 universal + 四 ABI 正式资产、完整宿主预览周期以及宿主/插件双维护者针对同一内容哈希的确认仍为
`PENDING`。所有批准栏保持空白，阶段继续是 R0 / No-Go。

## 3. 分阶段启用

| 阶段 | 宿主默认 | 插件能力 | 进入条件 | 退出 / 晋级条件 |
|---|---|---|---|---|
| R0 开发者实验 (当前) | 远程总开关默认关闭 | 官方 `false`, 自编译可 `true` | 基础协议存在 | G1/G5 的 R1 必需证据与 G6 R1 控制面通过 |
| R1 显式试用 | 默认关闭, 开发者手动开启; 失败停止 | 候选发布 `true` | G1, G2, G5 与 G6 R1 控制面通过 | G3/G4 已完成; 至少一个完整宿主预览周期并确定独立回退 ADR |
| R2 受控灰度 | 远程仅对测试渠道 / 明确 cohort | `true` | G1—G7 初版证据 + 独立回退完整通过 | 连续 14 天无回滚触发项, 新增问题均有分类 |
| R3 默认启用 | 匹配插件优先远程 | `true` | G1—G7 全部通过且维护者签字 | 保留总开关与独立回退至少两个稳定宿主版本 |
| R4 稳定 | 远程默认 | `true` | 两个稳定版本无阻断回归 | 另立 ADR 决定是否缩减旧路径; 不自动删除回退 |

没有可靠远程 cohort / kill switch 基础设施时, R2 使用预览渠道 + 显式设置代替百分比灰度, 不伪造 “10%” 等无法执行的比例.

## 4. 回滚触发器与动作

任一情形立即停止晋级并关闭默认远程路径:

- 输出 APK 无法安装 / 启动, 包名 / 版本 / 权限错误, 签名不一致或升级链断裂;
- 项目脚本明文, 密钥, 口令或工作区文件泄露;
- 可利用的路径逃逸, 任意文件覆盖或畸形输入崩溃;
- 远程路径导致 crash / ANR / Binder 死亡率超过资格批次上界;
- `UNSUPPORTED` / `FAILED` 分类错误造成回退循环, 静默吞错或覆盖用户旧产物;
- 新宿主 / Runtime Kit 发布造成协议协商失败且矩阵未覆盖.

回滚顺序:

1. 宿主总开关停止新的远程会话. R1 明确停止打包; R2/R3 切入已验证的独立回退路径.
2. 已运行会话允许安全完成或统一取消; 不强杀正在写最终文件的进程.
3. 下一插件构建上报 `supportsRemoteBuild=false`; 必要时撤回有问题的 Release 资产并在兼容矩阵标记替代构建.
4. 发布已知问题与用户恢复步骤, 保留失败诊断但清除敏感内容.
5. 用固定输入复现, 增加回归测试, 重新从 R1 开始; 不从 R2/R3 原位恢复.

## 5. 当前差距清单

截至 2026/09/01:

- 已有: M3-1 十五条功能场景 instrumented tests; API 24/x86、API 28/armeabi-v7a、API 28/arm64-v8a、API 29/x86 与
  API 36/x86_64 五档均为完整矩阵 14/14 + 较低协议聚焦 1/1, 合计 15/15; API 36 另有整类 15/15 集成复跑.
  五个真实模板成功构建统一核对回调
  长度/SHA-256、核心 APK 结构、PackageManager
  包名/版本/应用名/签名证书与工作区生命周期; 聚焦用例另覆盖默认库 V1、自定义 BKS FD + V2-only、权限、精确图标替换、
  单 ABI 裁剪以及 OpenCV / Image Quantization native 库.
- 已有: TypeScript v3 正常认证解密并最终重加密、tag 篡改、加密条目从路径清单漏列、路径清单多项四种设备用例;
  输入 FD 在所有终态清理, 早失败路径清零一次性密钥并移除元数据. 扩展测试发现并修复了“漏列密文未提前拒绝”以及
  “自定义 BKS 被工作区改名为 `keystore.bin` 后按扩展名误判为 JKS”两个生产缺陷.
- 已有: 功能关闭在会话入口强制返回 `UNSUPPORTED`; 当前/过高/较低协议、strict/risky 宿主不匹配、区间外失败关闭及
  所有终态 callback/status/compatibilityLevel 基础映射已有设备证据. 较低协议用例明确证明 v2 请求被 v3 插件接受并进入
  后续原生输入校验, 而不是被协议门禁误拒绝.
- 已有: 当前 5277 宿主以真实 Binder 远程路径分别生成项目目录源与单文件源 APK, 两者均通过 size/SHA-256 发布复核、
  安装、冷启动、`ScriptExecuteActivity` 前台确认及主脚本 UI/日志哨兵; 至此 G1 六项全部通过.
- 已有: 协议字段, 生命周期, 进度与错误语义文档 (`docs/remote-build-protocol.md`).
- 已有宿主侧: 能力/协议/兼容区间/ABI 候选门禁, 默认关闭的开发者总开关, 32 条进程内白名单诊断环,
  输出大小与 SHA-256、有界 APK 结构、密码学签名与请求包身份复核、同目录临时文件、fsync 与原子发布, Binder death
  recipient、无进度/总超时以及会话/绑定/FD 收口. 2026/08/31 聚焦运行准入策略 5/5、脱敏诊断 4/4、
  `RemoteBuildOutputTransportTest` 7/7、`RemoteBuildApkArchivePolicyTest` 5/5、
  `RemoteBuildWaitPolicyTest` 4/4 与兼容适配器 API 测试 4/4, Gradle exit 0; API 36/x86_64 AVD 上总开关的 UI、
  R1 风险说明及 `false -> true -> false` 持久化/恢复演练通过. 同一 AVD 上真实 force-stop 插件进程的 Binder 死亡用例
  1/1 通过, 唯一脱敏终态、旧产物保全、暂存清理与即时重新发现均有日志和 APK 哈希证据; 随后以真实
  `supportsRemoteBuild=false` 插件完成能力门禁负向用例 1/1, 在 PREPARE 前返回唯一可行动提示且进度为 0. G6 的 R1
  控制面七项至此均有可复核证据.
- 已有 G5 完整性与有界 ZIP/path: API 36/x86_64 将整类扩至 31/31; project/native/keystore 的六个 size/SHA-256 mismatch、
  ZIP 条目越界、POSIX/Windows 绝对路径、sourcePath 越界、非法 native 顶层、非 ZIP、畸形 JSON、压缩输入/条目数/压缩比均
  明确失败关闭, 无输出并清理 FD/工作区. 统一解包器另以 12/12 JVM 测试覆盖单条/总大小和归档累计压缩比边界.
- 已有 G5 宿主输出复核: callback 输出只有在 size/SHA-256、安全 ZIP 结构、四个核心组件、`apksig` 签名、与会话前固定的
  默认/自定义签名身份精确匹配的唯一签名者、平台可见性与请求包身份全部通过后才原子发布。传输 7/7、结构策略 5/5、
  签名者解析/绑定 5/5 单测通过; API 36 与最低 API 24 的真实 Binder 正负路径通过, API 36 最终又以 3/3 + producer 1/1
  验证错误签名者拒绝、自定义 BKS 解析及最终二进制正向链路, 旧输出保留、临时残留/目标包安装为 0。API 24 已验证旧平台
  `GET_SIGNATURES` 分支, 专用临时 AVD 已删除; API 36 既有 AVD 已清理并停止.
- 已有 G5 敏感数据审计: success/failure/cancel 设备聚焦 1/1, 扫描器专项 8/8; callback/progress/result、成功 APK、关闭后
  工作区、本次与全部历史资格文本及两仓库当前构建报告均完成规则扫描, 四批均为 0 findings/errors, 资格包与设备暂存已清理.
- 已有 G5 确定性 fuzz: 40 条 Parcelable/Bundle 与 32 条 JSON/Manifest/ARSC 语料共 72/72, JVM 24/24、API 36 定点
  2/2 和整类 39/39 均通过; keystore/icon/路径/JSON/ARSC/输出名上限与失败关闭、FD/口令/工作区清理、哨兵零回显已有证据.
- 已有 G2 稳定性与资源清理: 同一最终功能三元组在 API 36/x86_64、API 29/x86、API 24/x86_64 完成 180 次成功构建与
  30 次分阶段取消, 210/210 接受结果、0 非预期失败; 三档的终态、会话隔离、PID/FD、工作区、崩溃/ANR/Binder/重启与清理
  均有可重放证据, 所有临时 AVD 已删除.
- 已有 G3 双构建器等价性: 7 个基础 UI APK 的静态 10/10 与两条双向三步升级序列通过; TypeScript / ImageQuant 静态
  21/21、无对应 Runtime 插件的四步运行语义一致, 大小差异均在 2% 内; Node 旧配置由宿主和插件双边门禁失败关闭。完整
  证据和架构范围分别见工作区外 `m3-4-g3-equivalence-2026-09-01` 与
  `docs/remote-build-node-packaging-decision.md`。
- 已有 G4: API 36/x86_64、API 29/x86、API 24/x86_64 的本地/远程各 60 次 p95 均低于 `1.5x`; 三类 G3 产物大小差异
  绝对值均低于 2%; 取消最慢终态 153 ms; 插件空间预检的 JVM 5/5 与 API 36 两项故障注入通过。API 24 / 1 GiB 因远程
  启动期两个非目标 LMK 明确判为压力失败, 1.5 GiB 独立窗口 60/60 且 LMK/OOM/目标死亡/fatal 全为 0, 因而成为最低合格档。
  完整限制与证据见 `docs/remote-build-performance-audit.md`; 该结论不外推为真机性能承诺。
- 待补 G5: 由独立审查者完成插件安全审查并确认阻断级与高危问题为零; 仓库内实现/自动化子项已收口, 不得由本批自审替代.
- 待宿主 G6 的 R2/R3 部分: R1 控制面已收口; 进入 R2 前还必须确定并实现真正独立的回退路径, 一次远程/一次回退
  计数保护及等价性验证. 分阶段门槛已经确定, 回退实现形态可在 R1 资格批次完成前另立 ADR, 但不得豁免 R2/R3 硬门槛.
- 待 G7: 发布证据归档、放量文案与宿主/插件双维护者确认.

因此当前结论明确为 **No-Go for default**, 继续保持 README 与 `plugin_instruction.md` 中的 “实验性, 官方构建默认关闭” 文案.
