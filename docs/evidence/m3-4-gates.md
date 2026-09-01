# 远程构建资格门槛 G1—G7 审计记录

- 状态: 归档中, 随门槛推进追加
- 关联: `ROADMAP.md` M3-4 / M8, `docs/remote-build-rollout.md`, `docs/remote-build-release-evidence-index.md`
- 用途: 承载原先直接写在 `ROADMAP.md` M3-4 条目下的审计正文。路线图只保留一行结论与指针, 完整记录保存在此。

本文档**逐段原样保留**历史记录, 不做事后改写。段落之间存在相互覆盖关系 (后写的段落会声明它覆盖了前文的哪个旧快照),
这种覆盖关系本身也是审计信息, 因此一并保留。

## 1. 门槛状态索引

| 门槛 | 状态 | 一行结论 | 记录 |
|---|---|---|---|
| G1 协议与功能正确性 | 通过 | 五档 API/ABI 功能矩阵 + 两种源形态 + 真实 host→Binder 运行闭环 | [§2.1](#21-g1--g5--g6-综合进展2026-08-31) |
| G2 稳定性与清理 | 通过 | 180/180 正常构建 + 30/30 分阶段取消, 0 非预期失败 | [§2.6](#26-g2-稳定性资源清理2026-08-31) |
| G3 双构建器等价性 | 通过 | 静态/运行/双向升级/TypeScript/ImageQuant/Node 双边失败关闭 | [§2.7](#27-g3-双构建器等价性2026-09-01) |
| G4 性能与设备压力 | 通过 | 三档 p95 均低于 1.5x; 1.5 GiB 定为最低资格档 | [§2.8](#28-g4-性能设备压力2026-09-01) |
| G5 完整性与安全 (仓库内) | 通过 | 有界解压, 输出复核, 签名者绑定, 敏感数据审计, 确定性 fuzz 均已收口 | [§2.2](#22-g5-有界-zippath2026-08-31) — [§2.5](#25-g5-确定性畸形输入fuzz2026-08-31), [§2.10](#210-g5-宿主输出签名者身份绑定修订2026-09-01) |
| G5 独立安全审查 | **未通过** | 硬阻断, 需外部审查者; 已移交 `ROADMAP.md` M8-1 | [§2.9](#29-g5g7-交接准备2026-09-01) |
| G6 R1 控制面 | 通过 | 总开关, 能力门禁, 失败停止, 原子输出, 脱敏诊断, Binder death | [§2.1](#21-g1--g5--g6-综合进展2026-08-31) |
| G6 R2/R3 回退 | **未通过** | 真正独立的构建路径尚未实现; 已移交 `ROADMAP.md` M8-2 | — |
| G7 发布与支持 | **未通过** | 索引/门禁/Issue 表单已备; 签名候选, 预览周期, 双维护者确认缺失; 已移交 M8-3 | [§2.9](#29-g5g7-交接准备2026-09-01) |

当前阶段 **R0**, 官方 `supportsRemoteBuild=false`, **No-Go for default**。

工作区外内容寻址证据包统一位于 `D:\idea-projects\.a6-compat-audit-artifacts`, 逐包 `manifest.json` / `SHA256SUMS`
摘要见 `docs/remote-build-release-evidence-index.md` 第 4 节。

## 2. 原始记录

### 2.1 G1 / G5 / G6 综合进展 (2026/08/31)

2026/08/31 已完成五档 API/ABI 的 15 用例功能矩阵 (14 条完整矩阵 + 1 条较低协议聚焦), G1 的 API/ABI、高/当前/低协议、TypeScript v3、默认/自定义签名、V1/V2、图标、ABI 裁剪与当前 native 能力子项已有正反设备证据。当前 5277 宿主又通过真实 host → Binder → qualification plugin 路径分别生成项目目录源和单文件源两个 UI APK: producer 1/1、21.888 秒通过, 两次构建各 31 个进度回调; 两个输出均完成 size/SHA-256 发布复核、安装、冷启动、前台 `ScriptExecuteActivity` 与主脚本 UI/日志执行闭环, 截图、日志及 15/15 哈希清单归档于工作区外 `m3-4-g1-runtime-smoke-2026-08-31`。至此 G1 六项全部通过。API 36/x86_64 又将整类扩至 25/25, G5 的 project/native/keystore 六个 size/SHA-256 mismatch 与 ZIP 条目/sourcePath 越界、非 ZIP 输入、畸形 JSON 四项均失败关闭且无 FD/工作区残留; G5 仍缺有界解压、Parcelable/Manifest/ARSC fuzz、系统化敏感信息报告审计、宿主 APK 结构/签名复核与独立安全审查。宿主 G6 已具备能力/协议/兼容区间/ABI 门禁、输出大小/SHA-256 校验与原子替换、Binder death/超时/资源收口; 本轮又落地默认关闭的开发者总开关与最多 32 条的白名单脱敏诊断环, 准入策略 5/5、脱敏诊断 4/4、输出传输 6/6、等待策略 4/4、兼容适配器 API 4/4 单测通过。隔离的 API 36/x86_64 AVD 上先完成总开关 UI 与持久化 `false -> true -> false` 演练, R1 风险说明可见且最终恢复关闭, 截图/UI hierarchy/清单归档于工作区外 `m3-4-r1-host-gate-2026-08-31`; 随后用同签名、显式开启能力的临时资格插件完成真实 Binder 死亡 hostile test 1/1: 会话建立后精确 force-stop 插件, 宿主只产生一条 `FAILED / WAIT / BINDER_LOST` 白名单终态, 保留旧产物、清除暂存并在 Android 重启插件后即时重新发现, 完整日志中三组敏感哨兵 0 命中。演练同时修复 `AidlPluginHost` 旧连接多个终态回调的迟到解绑竞态, 最终用例 3.396 秒通过, APK/日志/哈希清单归档于工作区外 `m3-4-g6-binder-death-2026-08-31`。随后换装同签名、元数据明确含 `supportsRemoteBuild=false` 的资格插件完成独立能力门禁负向用例 1/1: 宿主在 PREPARE / 会话建立前返回唯一 `NOT_AVAILABLE / DISCOVERY / PROVIDER_NOT_AVAILABLE` 终态与可行动提示, progress 为 0, 未创建私有暂存并保留旧产物; 0.81 秒结果、完整日志与 7/7 哈希清单归档于工作区外 `m3-4-g6-capability-gate-2026-08-31`。`docs/remote-build-fallback-decision.md` 已采纳分阶段修订门槛: R1 明确试用时失败停止并保留旧产物, 不把关闭开关冒充回退; R2/R3 仍以真正独立的回退路径、一次远程/一次回退循环保护和等价性为硬前置。至此宿主 G6 的 R1 控制面七项均有可复核源码、单测或设备证据, 但 R2 独立回退仍未实现。G2 的 200 次资格批次、G3 双构建器等价性、G4 性能/压力、G5 剩余安全矩阵与 G7 放量材料也仍未完成, 因而当前继续停留 R0, 官方 `supportsRemoteBuild=false`, No-Go for default。

### 2.2 G5 有界 ZIP/path (2026/08/31)

覆盖并更新 §2.1 中 "仍缺有界解压" 的旧快照。

插件统一项目与 native/assets 两个宿主输入归档的全量预检和流式复核, 拒绝 POSIX/Windows/UNC 绝对路径、`.`/`..`/空段/重复路径、非法 native 顶层与 ABI, 并分别设置压缩输入、条目数、单条/总解压和单条/累计 250:1 压缩比上限; 恶意条目名不回显。JVM 边界矩阵 12/12、API 36/x86_64 聚焦故障注入 6/6、最终整类 31/31 均通过, 0 skipped/failed; 精确 Kit、APK、JUnit/HTML、保留的 wrong-kit 前置失败与逐文件 SHA-256 归档于工作区外 `m3-4-g5-bounded-zip-2026-08-31`。更广确定性 fuzz 与敏感数据审计已由下方追加证据关闭; G5 仍缺独立安全审查。

### 2.3 G5 宿主输出复核 (2026/08/31)

覆盖并更新 §2.1 中 "仍缺宿主 APK 结构/签名复核" 的旧快照。

AutoJs6 宿主把 callback 输出先写入目标同目录临时文件, 在 2 GiB 传输上限和 size/SHA-256 后执行最多 65,536 条目的安全 ZIP 结构/四个非空核心组件、仓库内 `apksig` 密码学签名、平台签名者及请求包名/versionName/versionCode 复核, 全部通过才原子替换。传输单测 7/7、结构策略单测 5/5、API 36/x86_64 真实 Binder 双输出 producer 1/1、签名篡改/结构缺项/身份错配拒绝设备用例 1/1 均通过; 失败时旧产物字节不变、临时残留 0、目标包安装 0。首次负样本运行因设备 JUnit 缺少 4.13 `ThrowingRunnable` 在测试方法前失败, 兼容断言最终复跑 32.93 秒通过, 两次尝试均归档。精确宿主/测试/资格插件 APK、正样本、独立签名验证、日志与 XML 位于工作区外 `m3-4-g5-output-validation-2026-08-31`。最低 SDK 补充资格又在专用 API 24/x86_64 AVD 上复用同字节的三个输入 APK, 实际覆盖旧平台 `GET_SIGNATURES` 分支; producer 1/1、21.847 秒与输出资格 1/1、12.719 秒均通过, 两个 V2-only 正样本另经独立签名、身份、条目与核心组件复核, 负样本结果仍为旧产物保留、临时残留/安装尝试/目标包均为 0。设备包与产物清理后已删除临时 AVD, 15 文件、14/14 哈希重放证据位于工作区外 `m3-4-g5-output-validation-api24-2026-08-31`。确定性 fuzz 已由下方追加证据关闭; 整个 G5 与 R0 状态不变, 仍受独立安全审查阻断。

### 2.4 G5 敏感数据 / 关闭后工作区 (2026/08/31)

覆盖并更新 §2.1 中 "仍缺系统化敏感信息审计" 的旧快照。

新增脱敏流式扫描器、4 类资格哨兵与 11 类高置信凭据模式, 报告不含命中字节或绝对扫描根。API 36/x86_64 聚焦用例在 success / TypeScript 认证失败 / pre-start cancel 三终态中扫描 callback/progress/result、成功 APK 原始字节与全部条目, 并核对 3 次密钥清零、6 个口令引用置空及关闭后工作区 0; 最终 1/1、265.37 秒通过。扫描器专项 8/8、仓库 Python 回归 18/18; 本次资格文本 15 files、主插件 APK 1,124 entries、历史证据文本 2,373 files、两仓库当前报告 19 files 四批均为 0 findings/errors; 插件本轮 JUnit/HTML/logcat 在 IDE 清理 `app/build` 前已复制进资格证据并由第一批覆盖。资格包与设备暂存已清理, 未操作实体机。规则、命令与局限见 `docs/sensitive-data-audit.md`, 完整证据位于工作区外 `m3-4-g5-sensitive-data-audit-2026-08-31`。更广确定性 fuzz 已由下方追加证据关闭; 整个 G5 与 R0 状态不变, 仍受独立安全审查阻断。

### 2.5 G5 确定性畸形输入 / fuzz (2026/08/31)

覆盖并更新前文 "仍缺更广 fuzz" 的旧快照。

AIDL/Bundle 新增白名单和严格类型/长度规则, 自定义 keystore 实际 FD 上限 64 MiB; `project.json` 限 512 KiB/64 层并拒绝相邻根和类型强转; 路径限 4,096 UTF-8 bytes、255-byte 单段和 128 段; 图标限 16 MiB、单边 4,096、4,194,304 pixels; ARSC 包名限 127 UTF-16 code unit, 输出 basename 限 238 bytes。JVM 24/24、API 36/x86_64 定点 2/2 与 72/72 unique cases、最终整类 39/39 均通过, 0 skipped/failed; 生成十语言 changelog 后又以最终生产 APK 字节完整复跑 39/39、141.647 秒。该次 1,515-line logcat 的 72 条脱敏终态齐全, 四类哨兵与 FATAL/crash 均为 0。最终日志、生产 APK 与 JVM XML 共 5 files、APK 1,124 entries 的扫描为 0 findings/errors。无效的首轮 2/2 假覆盖、输出后缀 `ENAMETOOLONG`、37/39 夹具失败、审查前 39/39、人工审查后 39/39 与最终资产复跑均按尝试历史保留。规则、结果和非穷尽性边界见 `docs/remote-build-fuzz-audit.md`, 证据位于工作区外 `m3-4-g5-deterministic-fuzz-2026-08-31`。至此 G5 的仓库内实现/自动化子项已收口, 但独立安全审查仍是硬阻断, 因而 M3-4 继续未完成、R0/No-Go 不变。

### 2.6 G2 稳定性 / 资源清理 (2026/08/31)

覆盖并更新前文 "G2 的 200 次资格批次仍未完成" 的历史快照。

同一最终功能三元组——AutoJs6 `6.8.0 / 5277` 宿主 `aa92d07a...99a31`、同签名 remote-enabled 资格插件 `bcafedf7e...43370`、5277 universal Runtime Kit / 协议 v3——在全新 API 36/x86_64、API 29/x86、API 24/x86_64 隔离 AVD 上分别完成目录源 30 次与单文件源 30 次成功构建; API 36 另在 PREPARE/BUILD/SIGN 各完成 10 次取消。合计 180/180 正常构建、30/30 预期取消, 即 210/210 接受结果、0 非预期失败。每次正常会话均核对请求身份/配置/唯一 marker 与唯一脱敏终态, 三档都保持单一插件 PID、FD 最终回到或低于基线、宿主暂存与插件工作区 0, 权威日志中的 fatal/ANR/`BINDER_LOST`/进程死亡/重启均为 0。API 24 通过 exact-serial 外部暂停/ACK 探针补足旧 `UiAutomationConnection` 无法 `run-as` 的平台限制。资格批次修复了早期取消误报 `FAILED`、session close 与 worker 的工作区复活竞态、Android 9–11 有效 V2/V3 APK 现代签名者视图为空三项生产缺陷; 宿主单测 36/36、插件 app 24/24、插件 API 7/7 与两仓库 Release Kotlin 编译均通过。旧 API 36 三元组与受外部旧会话干扰、错误夹具/探针的尝试全部以 0 计分保留; 最终 API 36 客户端 stdout 尾段缺失也如实记录并由完整设备 log 的 start/finish/零失败/精确汇总关联核验, 未补造输出。四个临时 AVD 均已删除, 官方源码及生成的 Debug/Release/AndroidTest `ENABLE_REMOTE_BUILD=false`。APK、Runtime Kit、日志、JUnit XML、尝试史、清理、脱敏扫描与可回放哈希位于工作区外 `m3-4-g2-stability-2026-08-31`。至此 G2 通过, 但 G3、G4、G5 独立安全审查、G7 和 R2/R3 独立回退仍是硬门槛, M3-4 继续未完成、R0/No-Go 不变。

### 2.7 G3 双构建器等价性 (2026/09/01)

覆盖并更新前文 "G3 仍是硬门槛" 的历史快照。

冻结旧宿主构建器提交 `fff913caafa3dc0d6172638c8532b027c0dfa8c0`, 在专用 API 36/x86_64 AVD 上与当前 5277 Runtime Kit / 远程构建器比较。基础 UI 的本地 3 包 + 远程 4 包静态 10/10, `local 100 -> remote 101 -> local 102` 与 `remote 200 -> local 201 -> remote 202` 两条三步序列的安装/升级、冷/热启动、前台 Activity、UI/日志全部通过。TypeScript 与 ImageQuant 本地/远程 4 包静态 21/21, 在对应 Runtime 插件均卸载时分别得到一致的 checksum 108 与 1,181 bytes/checksum 83,471,039, 大小差异 -1.105671% / -1.085675%; 过程发现并修复编译后 TypeScript launcher 丢失 `ui` 指令的问题。Node.js 运行时现由外部插件拥有, `docs/remote-build-node-packaging-decision.md` 已将旧 "内嵌 Node 等价" 修订为宿主/插件双边失败关闭: 宿主 JVM 3/3 + 设备 1/1 两请求在 progress/staging 前拒绝, 插件 JVM 4/4 + 直接会话设备 1/1 两请求均 `UNSUPPORTED`, BUILD/SIGN/output/工作区为 0, 旧 Manifest Node 注入实现已删除; 相同最终插件 APK 的完整设备类另以 41/41、139.568 秒通过, 41 个 start/finish、fatal=0、工作区=0。完整 APK、截图、hierarchy、日志、JSON/XML 与尝试史位于工作区外 `m3-4-g3-equivalence-2026-09-01`。至此 G3 通过; 当时 G4、G5 独立安全审查、G7 与 R2/R3 独立回退仍是硬门槛, 因而 M3-4 继续未完成、R0/No-Go 不变。

G4 首次本地基线随后发现冻结本地构建器仅对目录源递增 `build.number`, 单文件使用请求 versionCode 原值, 而远程实现错误地统一递增; 生产实现已按 source kind 修正, 插件目录/单文件设备断言及宿主基准夹具同步, 首次失败按尝试史保留, §2.8 的 G4 接受数据全部来自修正后的构建器。

### 2.8 G4 性能 / 设备压力 (2026/09/01)

覆盖并更新前文 "G4 性能/压力仍未完成" 的历史快照。

API 36/x86_64、API 29/x86、API 24/x86_64 以同一目录源/单文件源分别完成本地 60 次与真实 Binder 远程 60 次, 远程/本地 p95 为 `1.128325x`、`0.478366x`、`0.887375x`, 全部低于 `1.5x`; G3 基础/TypeScript/ImageQuant 同版本大小差异绝对值全部低于 2%, G2 分阶段取消最慢终态 153 ms。新增两项展开量协议字段、模板实际展开量 `<=4x` 构建约束及插件饱和空间估算, 在创建工作区/复制大输入前保留 256 MiB 并估算压缩输入、项目与 native 展开量及三份构建树; JVM 5/5、API 36 空间不足和 native 零声明各 1/1 失败关闭。API 24 / 1 GiB 虽完成 60/60, 但远程启动期出现 DeskClock 与一个未映射低优先级进程的系统 LMK, 因而如实判为 `PERFORMANCE_PASS_PRESSURE_FAIL`; 同一 AVD 冷启动为实际 1,533,776 KiB 后, 唯一标记窗口内再完成 60/60, p95 7,057 ms, 226 个 PSS 样本峰值为宿主 123,117 KiB、插件 57,356 KiB、同采样合计 176,349 KiB, LMK/`lmkd`/OOM/目标死亡/fatal 全为 0, PID/FD 稳定且工作区为 0, 从而把 1.5 GiB 定为当前最低资格档。最终插件 app/API 与宿主聚焦 JVM 为 35/35、7/7、60/60, 两仓库 Release 编译通过, 官方 Debug/Release/AndroidTest 能力均恢复 `false`; 四个临时 AVD 已删除、实体机定向命令 0。方法与限制见 `docs/remote-build-performance-audit.md`, 工作区外证据位于 `m3-4-g4-performance-pressure-2026-09-01`。至此 G4 通过; M3-4 仍因 G5 独立安全审查、G7、完整预览周期及 R2/R3 独立回退保持 R0/No-Go。

### 2.9 G5/G7 交接准备 (2026/09/01)

新增 `docs/remote-build-independent-security-review.md`, 固定双仓库未提交源码快照、攻击面、必审范围、Critical/High 零未解决准入规则与空白独立审查决定; `docs/remote-build-release-evidence-index.md` 汇总 G1—G6 manifest/SHA、阶段状态、签名资产缺口、预览周期和双维护者空白批准栏。协议文档门禁现从 AIDL/Kotlin 真源覆盖 73 个远程方法/字段/键/常量, 73/73、专项 2/2、Python 全回归 20/20, 并接入 docs consistency; 插件与宿主均新增只接受脱敏版本/status/step/error 分类的远程构建 Issue 表单。G4 证据最终封存为 manifest 102/102、SHA 103/103; 本地默认关闭 Release 变体虽可编译但未配置正式签名, 已明确标为不可发布。上述只完成仓库内材料准备, G5 独立审查、正式签名五 ABI 候选、完整宿主预览周期及双维护者确认仍为 `PENDING`, 所以 G7 未通过、R0/No-Go 不变。

### 2.10 G5 宿主输出签名者身份绑定修订 (2026/09/01)

覆盖并更新早期 "任意有效签名即可" 的输出复核语义。

组合审计发现包名/version 正确且 `apksig` 有效、但由另一张证书签署的远程 APK 仍可能通过。宿主现于打开 Binder 会话前从所选默认公开摘要或自定义 keystore 固定预期证书 SHA-256, 并要求输出恰好一个已验证签名者且摘要精确匹配; 插件上报值不能替代该比较。最终宿主 APK 打包公开摘要 1、默认私钥库 0; 宿主 APK Builder JVM 44/44、API 36 输出资格 3/3、最终真实 Binder 双输出 1/1、Release Kotlin 编译与独立 `apksigner` 均通过。错误签名者拒绝发生在原子替换前, 旧输出保留、临时残留/安装尝试为 0; 扩展名无关的自定义 BKS 在 Android 上解析通过。文本 39 files 与 4 APK/12,678 entries 扫描为 0 findings/errors, 资格包/测试包/三份夹具已清理, 总开关 `false`, AVD 已停止, 实体机定向操作 0。54/54 manifest 与 55/55 SHA 回放证据位于工作区外 `m3-4-g5-signer-binding-2026-09-01`, 摘要分别为 `e336b45b...b1c8c9` / `7e561fcd...ab27b2`。仓库内输出复核子项重新收口, 但独立安全审查仍未完成, 因此 G5、M3-4 与 R0/No-Go 状态不变。

## 3. M3-1 远程构建端到端用例原始记录

`RemoteApkBuildSessionInstrumentedTest.kt` 当前含 39 个测试方法: 既有功能/兼容/生命周期与 G5 负向输入、success/failure/cancel 敏感数据生命周期、72 条确定性 Parcelable/Bundle/JSON/Manifest/ARSC 语料, 以及 keystore/icon/二进制编辑边界。成功构建核对回调长度/SHA-256、核心 ZIP 条目、项目配置、PackageManager 包名/版本/应用名/签名证书和工作区清理; 失败路径核对 BLOCK、无输出、FD/口令关闭与工作区清理。2026/08/31 在 API 24/x86、API 28/armeabi-v7a、API 28/arm64-v8a、API 29/x86、API 36/x86_64 五档均完成 15 条功能矩阵, 0 skipped/failed; API 36/x86_64 的最终扩展整类为 39/39, 另有确定性语料聚焦 2/2、72/72 unique cases。v2 请求在 v3 插件上明确通过协商并进入后续原生输入校验。设备扩展测试推动修复 "TypeScript 密文漏列未提前拒绝" 与 "自定义 BKS 改名 `keystore.bin` 后误按 JKS 加载" 两个生产缺陷。复跑、恢复与剩余边界见 `docs/remote-build-e2e-drill.md`; 五档功能矩阵机器可读报告位于工作区外 `m3-4-qualification-2026-08-31`。
