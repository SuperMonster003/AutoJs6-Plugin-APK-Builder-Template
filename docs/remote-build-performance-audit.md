# 远程 APK 构建 G4 性能与设备压力审计

## 1. 结论

2026/09/01 的仓库自有资格批次判定 G4 **通过**，但不改变项目的 R0 / No-Go 状态，也不启用官方插件的远程构建能力。

通过依据如下：

- API 36/x86_64、API 29/x86、API 24/x86_64 三档各完成同输入的本地 60 次与远程 60 次构建，远程 p95 分别为本地的 `1.128325x`、`0.478366x`、`0.887375x`，均低于 `1.5x` 门槛；
- G3 的基础、TypeScript 与 Image Quantization 同版本 APK 大小差异分别为 `1.106526%`、`-1.105671%`、`-1.085675%`，绝对值均低于 `2%`；
- API 24 / 1.5 GiB 资格档连续完成 60 次远程构建，唯一日志窗口内 LMK、`lmkd` kill、OOM、宿主/插件进程死亡和 fatal 均为 0；
- 插件在创建会话工作区或复制大输入前完成可计算空间预检，空间不足与非法零值声明两个设备故障注入均失败关闭；
- G2 的 PREPARE、BUILD、SIGN 各 10 次取消全部得到唯一 `STATUS_CANCELLED`，最慢终态为 153 ms，低于 5 秒门槛；
- 所有接受批次结束时远程开关为 `false`，宿主 staging 和插件 workspace 均为 0，临时 AVD 均已删除。

G5 的独立安全审查、G7 发布材料与双维护者确认、至少一个完整宿主预览周期，以及 R2/R3 所需的真正独立回退仍未完成。因此 G4 通过不等同于进入 R1、默认启用、发布或灰度授权。

## 2. 固定输入与方法

性能批次使用 AutoJs6 `6.8.0 / 5277`、协议 v3、同签名 remote-enabled 资格插件与 5277 universal Runtime Kit。资格插件只存在于工作区外证据目录；仓库属性及最终 Debug、Release、AndroidTest `BuildConfig.ENABLE_REMOTE_BUILD` 均恢复为 `false`。

每档测试均采用以下流程：

1. 在新建的隔离 AVD 上安装冻结的宿主、AndroidTest 与资格插件 APK；
2. 使用相同的小型目录源和单文件源，先预热，再交替执行各 30 次，共 60 次；
3. 本地基线走冻结旧宿主构建器，远程数据走真实 host → Binder → plugin 会话；
4. 从测试自身的单调时钟记录每次总耗时，并以 nearest-rank 计算 p50/p95；
5. 约每 2 秒读取宿主与插件完整 `dumpsys meminfo`，同时记录 PID、FD、staging/workspace 和完整日志；
6. API 24 因旧 `UiAutomationConnection` 不能从 system 侧执行 `run-as`，在预热、第 30 次、第 60 次和最终清理处暂停，由 exact-serial 外部探针检查后写入私有 ACK；
7. 仅当测试、日志、进程、FD、工作区和后置开关全部满足门槛时才接受该批次。

所有 ADB 命令都显式指定临时模拟器序列。四台在线实体机没有收到本阶段定向命令。

## 3. 三档性能结果

| 档位 | 本地成功 | 本地 p50 / p95 / max | 远程成功 | 远程 p50 / p95 / max | 远程 p95 / 本地 p95 | 判定 |
|---|---:|---:|---:|---:|---:|---|
| API 36 / x86_64 | 60/60 | 6,097 / 6,429 / 6,607 ms | 60/60 | 6,994 / 7,254 / 7,383 ms | `1.128325x` | 通过 |
| API 29 / x86 | 60/60 | 21,161 / 21,448 / 21,923 ms | 60/60 | 10,042 / 10,260 / 11,147 ms | `0.478366x` | 通过 |
| API 24 / x86_64，1 GiB 特征档 | 60/60 | 8,036 / 8,293 / 8,382 ms | 60/60 | 6,946 / 7,359 / 7,819 ms | `0.887375x` | 性能通过，压力资格失败 |

API 29 的第一次 PSS 观察器使用了该旧平台不支持的 `dumpsys meminfo -s` 摘要格式，产生 54 个空样本。该次观察器结果不计入资格；完整测试没有失败，随后使用完整 `dumpsys meminfo` 重新采集 228 个有效批次样本。错误观察器输出按尝试史保留。

## 4. 内存、FD 与最低合格配置

| 档位 | MemTotal | PSS 样本 | 宿主峰值 | 插件峰值 | 同采样合计峰值 | 宿主 FD 基线 / max / 最终 | 插件 FD 基线 / max / 最终 | LMK/OOM 判定 |
|---|---:|---:|---:|---:|---:|---:|---:|---|
| API 36 / x86_64 | 资格 AVD 默认配置 | 207 | 225,989 KiB | 101,527 KiB | 316,334 KiB | 119 / 121 / 119 | 89 / 89 / 89 | 0，作为当前 target 档通过 |
| API 29 / x86 | 资格 AVD 默认配置 | 228 | 219,161 KiB | 98,075 KiB | 315,003 KiB | 73 / 73 / 72 | 41 / 42 / 40 | 0，通过 |
| API 24 / x86_64，1 GiB | 1,019,240 KiB | 230 | 126,391 KiB | 73,254 KiB | 188,511 KiB | 60 / 60 / 59 | 36 / 36 / 35 | 目标进程 0；系统级压力资格失败 |
| API 24 / x86_64，1.5 GiB | 1,533,776 KiB | 226 | 123,117 KiB | 57,356 KiB | 176,349 KiB | 55 / 56 / 54 | 36 / 36 / 35 | 唯一窗口内全部为 0，通过 |

1 GiB 特征批次的完整缓冲区含三条内核 LMK：一条是远程窗口前的 CryptKeeper；另外两条发生在宿主/插件启动后、第一条基线探针前，被回收者分别为 DeskClock 和无法从 ActivityManager 日志映射包名的 `pool-1-thread-1`。宿主 PID 3876、插件 PID 3927 均不是受害者并完成了 60/60，但系统已因该启动负载回收后台进程，所以该档明确记为 `PERFORMANCE_PASS_PRESSURE_FAIL`，不能作为最低支持配置。

同一 API 24 AVD 随后以 `-memory 1536` 冷启动，实际 `MemTotal=1,533,776 KiB`。资格窗口用唯一的 `M3_G4_1536_WINDOW_START` / `END` 标记包围：

- 目录源 30/30、单文件源 30/30，0 failure，p50/p95/max 为 6,804 / 7,057 / 7,143 ms；
- 窗口内 2,598 行日志的 kernel LMK、`lmkd` kill、OOM、目标进程死亡和 fatal 均为 0；
- 插件 PID 3217 在四个探针点不变，FD 为 36 / 35 / 35 / 35，工作区始终为 0；
- 连续 60 次已超过“最低配置连续 10 次无 OOM”的门槛。

因此当前资格档的最低配置定为 **API 24 AVD / 1.5 GiB 配置内存**。这是远程构建资格基线，不是对任意真机可用内存、系统常驻负载或厂商 LMK 参数的普遍保证。

## 5. 输出大小

G4 复用 G3 同一 Runtime Kit、同版本与同功能输入的双构建器静态对照：

| 场景 | 远程相对本地大小差异 | 2% 门槛 |
|---|---:|---|
| 基础 UI / 同版本 | `1.106526%` | 通过 |
| TypeScript | `-1.105671%` | 通过 |
| Image Quantization | `-1.085675%` | 通过 |

三项绝对值均小于 2%，无需以条目级例外豁免。G3 已另行核对包身份、版本、权限、签名、ABI、加密项目条目、运行结果和双向升级链；这里不重复把大小通过解释为功能等价。

## 6. 临时空间预检

宿主在请求 `extras` 中提供项目 ZIP 与 native/assets ZIP 的中央目录总解压字节数。插件先验证字段类型和范围，再按以下饱和算术估计插件私有缓存所需空间：

```text
templateExpanded = templateArchiveBytes * 4
compressedInputs = projectArchiveBytes + nativeArchiveBytes + keyStoreBytes
buildTree = templateExpanded + projectExpanded + nativeExpanded
requiredUsable = 256 MiB reserve + compressedInputs + projectExpanded + 3 * buildTree
```

若旧协议 v3 宿主没有提供新增字段，项目与 native 输入分别回退到 1 GiB、2 GiB 的总解压硬上限，而不是按 0 估算。存在 native FD 时声明解压量不得为 0。Runtime Kit 构建还要求内嵌模板实际总解压量不超过压缩 APK 的 4 倍，防止运行时估计系数与受信模板脱节。

空间检查在插件创建 `remote-apk-build/session-*`、复制输入 FD 或展开模板前执行；失败信息只包含 required/available 数值与“释放空间或缩小项目/native 输入”的动作建议。API 36 设备资格分别注入可用空间不足和“native FD + 零声明”，两项均 1/1 失败关闭，未创建工作区且输入 FD 完成收口。JVM `RemoteBuildStoragePolicyTest` 5/5 覆盖正常估算、旧宿主保守回退、native 缺失、空间不足和 Long 溢出饱和。

本门槛中的“大文件写入”特指插件侧模板展开、构建树、重打包与签名工作集；宿主已受限的请求 ZIP 在 Binder 会话前生成，并由既有失败清理契约负责。若未来将宿主请求归档也纳入容量承诺，应新增宿主文件系统预估与设备注入，不得把本次插件侧证据外推为已覆盖。

## 7. 取消上界

G2 的 API 36 批次在 PREPARE、BUILD、SIGN 各注入 10 次取消，30/30 均只产生一个 `STATUS_CANCELLED`，不覆盖旧产物并清空双方工作区；最慢终态 153 ms，显著低于 5 秒。G4 没有重复制造一套取消夹具，而是直接引用同一最终协议与会话实现的已封存 G2 证据。

## 8. 可重放证据与局限

权威证据位于工作区外：

```text
D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g4-performance-pressure-2026-09-01
```

目录保留三档本地/远程原始 instrumentation、完整 logcat、PSS CSV、API 24 外部探针、1 GiB 压力失败分类、1.5 GiB 唯一窗口、错误尝试、冻结输入 APK/Runtime Kit、默认关闭最终产物、JUnit XML、设备/AVD 清理记录、`manifest.json` 与 `SHA256SUMS`。过程失败不被最终通过报告覆盖。

最终封存回放为 manifest 102/102、SHA 清单 103/103，零失败；`manifest.json` SHA-256 为
`eaeeba556bd2520024e7557a2fc63681d07343eac5a7a70448706e08bc6424f4`，`SHA256SUMS` SHA-256 为
`c8a22c2648d435785f3d23ba1da4ee15c39be460fe7414347cd6d59e6a1549f5`。其中默认关闭的 Release 变体 APK
`88c97d357c7058ea08924a298153cbee3f17ec7e221fa3783387076e0af5e427` 因本机没有正式 release signing 配置而未签名，
只作为编译/能力关闭复核输入，明确不是可发布资产；设备资格使用的是证据包内单独固定、同测试签名的资格 APK。

本批次仍有以下边界：

- PSS 是约 2 秒间隔采样，不是内核级瞬时峰值追踪；
- 性能数字来自 AVD，不代表具体实体机的闪存、温控、厂商 LMK 或后台负载；
- 1.5 GiB 是当前测试配置下的最低合格档，而不是对更低配置的兼容承诺；
- G5 独立安全审查与 G7 发布确认未完成，官方能力继续为 `false`；
- R1 失败仍停止当前构建；真正独立回退仍是 R2/R3 的硬门槛。
