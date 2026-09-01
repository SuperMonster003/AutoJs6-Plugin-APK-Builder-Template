# 远程 APK 构建的 Node.js 打包边界决策

- 状态: 已采纳
- 日期: 2026/09/01
- 关联: ROADMAP M3-4 / G3, `docs/remote-build-protocol.md`, `docs/remote-build-rollout.md`

## 1. 背景

旧宿主构建器曾把 Node.js 运行时视为可嵌入 APK 的能力, 并为生成应用注入
`NativeNodeEmbeddedScriptService`、独立进程和前台服务权限。当前 AutoJs6 架构已经把 Node.js 运行时所有权迁移到外部
Runtime 插件; 当前 Runtime Kit 与 APK Builder Template 不再携带一套可独立运行的 Node.js payload。宿主原有本地构建器也已从
当前代码中移除, 只保留远程协议适配入口。

因此, 把 G3 继续表述为“远程产物必须与旧构建器的内嵌 Node 产物等价”会产生两种错误结果: 要么以一个无法执行的 APK
冒充等价, 要么在没有重新评审运行时所有权、生命周期和安全边界的情况下复活已经退役的内嵌实现。两者都不可接受。

## 2. 决策

G3 的产物等价范围限定为当前架构实际支持的可打包能力:

- Rhino JavaScript 与 UI 脚本;
- 由宿主 TypeScript 插件编译为 Rhino JavaScript、再经远程协议暂存保护和最终脚本加密的 TypeScript / TSX;
- 由已验证 native/assets 输入自包含进生成 APK 的原生能力, 包括当前资格矩阵中的 Image Quantization;
- 项目配置、Manifest / ARSC、ABI、签名、build 元数据、冷/热启动与双向升级行为。

Node.js 脚本继续由 AutoJs6 宿主配合外部 Runtime 插件执行, **不属于 APK Builder 的可打包能力**。`Embedded Node.js`
只作为旧项目迁移识别符保留, 不再是可枚举或可构建的库。

### 2.1 失败关闭信号

下列任一信号都表示请求试图生成内嵌 Node.js APK:

| 信号 | 识别规则 |
|---|---|
| `libs` | `Embedded Node.js` 或旧别名 `node`, `nodejs`, `node.js`, `embedded-node`, `embedded-nodejs`, `embedded_nodejs`, 忽略大小写和首尾空白 |
| 项目类型 | `projectType=node`, 忽略大小写和首尾空白 |
| 项目配置 | 存在 `nodeConfig`, 不以其内容是否为空或可解析作为放行依据 |
| 入口文件 | `.mjs`, `.cjs`, `.mts`, `.cts` 或 `.node.js`; `.d.ts/.d.mts/.d.cts` 声明文件不误判 |
| 执行模式 | 主入口首条有效指令包含 `node` 或 `nodejs`; 支持 BOM、前导注释、分隔符和 JavaScript 转义 |

宿主在创建缓存目录、写项目归档、发现插件或打开 Binder 会话之前执行预检。命中时立即返回可行动错误, 提示移除旧的
Embedded Node.js 打包配置并改由宿主安装外部 Runtime 插件运行; 不产生进度回调或请求暂存。执行模式解析不再记录 token、
脚本正文或解析异常堆栈。

插件保留第二道独立门禁, 防止旧版或自定义 AIDL 客户端绕过宿主预检:

- 元数据类信号在创建会话工作区前返回 `STATUS_UNSUPPORTED / LEVEL_WARN`;
- 只有读取真实入口才能判断的脚本指令在有界解包后、进入 BUILD / SIGN 前返回同一终态, 随后关闭输入 FD 并清空工作区;
- 不生成 `outputApkFd`, 不写入 Manifest Node 服务或前台权限。旧注入调用与编辑器辅助实现均已从生产路径删除。

Node 门禁不能由 `allowRiskyBuild`、远程构建开发者开关或协议降级绕过。

## 3. 对 G3 的修订

G3 不再要求一个已退役、无当前运行时所有者的“内嵌 Node APK”与旧实现等价。验收改为:

1. 对当前支持的 JavaScript / UI / 编译后 TypeScript / 自包含 native 能力完成静态、运行时和升级等价性;
2. 对 Node.js 相关旧配置和源码入口完成宿主与插件双边失败关闭;
3. 错误必须可行动且脱敏, 不得把“不支持”伪装成成功、空壳 APK或独立构建回退。

这不是为现有失败实现豁免测试, 而是把门槛校正到当前运行时所有权。若未来要提供可安装的独立 Node.js APK, 必须另立
ADR, 明确运行时 payload 来源、版本/ABI 配对、服务生命周期、前台服务政策、签名与更新责任, 并提升或扩展协议后重新执行
G1—G7; 不得通过重新接回旧 Manifest 注入代码隐式恢复。

## 4. 资格证据

2026/09/01 在临时 API 36/x86_64 AVD `emulator-5560` / `EMULATOR37X1X11X0` 上完成以下接受结果:

- 基础 UI 项目共 7 个 APK, 静态等价 10/10; 同版本远程产物大小差异 1.106526%。
- `legacy-local-v100 -> current-remote-v101 -> legacy-local-v102` 与
  `current-remote-v200 -> legacy-local-v201 -> current-remote-v202` 两条三步序列均通过安装/升级、冷启动、热启动、前台
  `ScriptExecuteActivity`、UI 与脚本日志检查。
- TypeScript 与 Image Quantization 静态比较 21/21; 远程相对本地产物大小差异分别为 -1.105671% 和 -1.085675%。
  两个 Runtime 插件均卸载时, 本地/远程 TypeScript 产物都返回 checksum 108, 两个 Image Quantization 产物都返回
  1,181 bytes / checksum 83,471,039; 四个步骤的冷/热启动均通过。资格过程发现并修复了编译后 TypeScript launcher 丢失
  `ui` 执行指令的问题, 失败尝试保留在证据中。
- 宿主 Node 策略 JVM 3/3; 最终设备门禁 1/1、0.095 秒, 两个请求均在预处理前拒绝, progress=0, staging 未创建, 日志中的
  执行模式与源码哨兵明文为 0。移除 token 日志前的通过结果和一个无 Android locale mock 的混合 JVM 尝试均单列保留。
- 插件 Node 策略 JVM 4/4; 最终直接会话设备门禁 1/1、0.401 秒, 两个请求均为 `STATUS_UNSUPPORTED`, BUILD/SIGN
  progress=0, output=0, 工作区条目=0, 崩溃/脚本哨兵明文=0。最终 APK SHA-256 为
  `fec18e13acfd89e3acf8a15a330c9d2ed6a39073817772fa3cd185598a56ebc3`, 且旧 Manifest Node 注入符号计数为 0。相同最终
  APK 随后通过完整 `RemoteApkBuildSessionInstrumentedTest` 41/41、139.568 秒回归; logcat 中 41 个 start/finish 配对,
  fatal/instrumentation failure=0, 工作区=0。

完整 APK、截图、UI hierarchy、日志、静态/运行时 JSON、JUnit XML、尝试史与 Node 双边门禁位于工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g3-equivalence-2026-09-01`。

## 5. 发布影响

上述证据关闭 G3, 但不改变当前发布阶段。G4 已由后续性能/压力资格关闭；G5 独立安全审查、G7 发布材料与双维护者确认仍未完成;
R2/R3 还受独立构建回退硬门槛约束。因此项目继续处于 R0 / No-Go, 宿主开发者开关默认关闭, 官方插件继续
`supportsRemoteBuild=false`。
