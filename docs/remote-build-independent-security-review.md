# 远程 APK 构建独立安全审查交接

## 1. 当前状态

- 门槛: G5 最后一项“独立插件安全审查”；
- 状态: `PENDING_INDEPENDENT_REVIEW`；
- 发布判定: R0 / No-Go，官方插件 `supportsRemoteBuild=false`；
- 本文用途: 给不参与本批实现的审查者提供固定范围、威胁模型、证据入口和签字格式；
- 非结论: 仓库作者的自审、自动化通过或本文本身都不能替代独立审查结论。

截至 2026/09/01，仓库内 G5 自动化子项已经收口：有界 ZIP/path、输入 size/SHA-256、TypeScript 密钥与签名口令生命周期、
宿主输出 APK 的结构/密码学签名/预选签名者/包身份复核、72 条确定性畸形语料和系统化敏感数据扫描均有设备或 JVM 证据。独立审查者仍需
从攻击者视角复核这些控制能否被组合绕过，并确认阻断级与高危未解决问题为零。

## 2. 审查独立性与快照规则

独立审查者不得是本批远程构建实现的唯一作者或唯一验收人。可以来自同一维护团队，但必须能够独立提出发现、要求修复并拒绝
Go。宿主维护者与插件维护者的 G7 发布确认是另一层批准，不能代替安全审查者。

当前两个工作树含尚未提交的 Roadmap 实现，因此仅记录 Git HEAD 不足以唯一标识待审源码：

| 仓库 | 基线 HEAD | 分支 | 说明 |
|---|---|---|---|
| APK Builder Template | `e7f4918e036171bfab4cac79b1b461e5cea5a1a7` | `master` | 待审最终文件由交接包逐文件 SHA-256 固定 |
| AutoJs6 宿主 | `b833227b01d05f69bc34d8a69c9f4d113a5a4ef6` | `master` | 待审最终文件由交接包逐文件 SHA-256 固定 |
| 冻结旧宿主构建器 | `fff913caafa3dc0d6172638c8532b027c0dfa8c0` | 独立 worktree | 只用于 G3/G4 对照，不是回退实现 |

权威交接目录为：

```text
D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g5-independent-review-handoff-2026-09-01-v2
```

审查决定必须引用该目录最终 `manifest.json` 的 SHA-256。交接包封存后，任一范围内源码发生变化都会使原决定只适用于旧快照；
修复阻断级/高危发现后必须生成新包或明确的增量包并复审，不得在旧签字下替换文件。

## 3. 威胁模型

审查至少假设以下输入或参与方可能恶意、畸形、过大、竞态或不一致：

1. 宿主传入的 AIDL Parcelable、Bundle extras、字符串、数值、FD 和声明的 size/SHA-256；
2. 项目 ZIP、native/assets ZIP、`project.json`、图标、自定义 keystore、Manifest/ARSC 替换值与输出文件名；
3. 插件通过 callback 返回的状态、进度、文件名、长度、摘要、输出 APK FD 与 result extras；
4. Binder 在 PREPARE/BUILD/SIGN/输出传输任一时点死亡、取消或重复回调；
5. 设备处于低内存、低磁盘、慢存储或并发会话环境；
6. 已安装的同包名插件具有错误签名、ABI、协议、宿主区间或伪造 capability；
7. 日志、错误文本、测试报告、工作区和最终 APK 被用于寻找项目正文、口令、密钥或路径；
8. Runtime Kit、模板 APK、默认签名库或发布工作流与声明的摘要、版本、ABI、签名不一致。

不在本次成功路径中的 Node.js 内嵌运行时必须继续双边失败关闭。当前 R1 没有独立构建回退；关闭开关、保留旧产物或冻结旧
宿主构建器都不得被审查者计作 R2/R3 回退证据。

## 4. 必审源码范围

| 信任边界 | 主要范围 |
|---|---|
| 公共协议 | `plugin-api/apk-builder-template/src/main/aidl/**`、同模块 Parcelable/capability/compatibility Kotlin 类 |
| 插件请求入口 | `RemoteApkBuildSession`、`RemoteApkBuildRequestPolicy`、`RemoteProjectConfigParser`、`RemoteApkBuildWorkspace` |
| 解包与资源上限 | `RemoteZipExtractor`、`RemoteBoundedStreamCopier`、`RemoteApkIconPolicy`、`RemoteBuildStoragePolicy` |
| 构建与签名 | `RemoteApkLightweightBuilder`、`RemoteApkManifestEditor`、ARSC editor、APK signer、keystore helper、宿主 `RemoteBuildExpectedSignerResolver` |
| TypeScript 暂存 | `RemoteTypeScriptStagingDecryptor`、公共 `TypeScriptBuildStagingCipher`、宿主 `TypeScriptApkBuildCompiler` |
| 宿主准入与生命周期 | `ApkBuilderTemplatePluginHost`、`ApkBuilderPluginTrustPolicy`、`RemoteApkBuildClient`、`AidlPluginHost` |
| 宿主输入/输出边界 | `RemoteApkBuildRequestPreparer`、`RemoteBuildOutputTransport`、`RemoteBuildApkArchivePolicy` 及相关测试 |
| 发布与供应链 | Runtime Kit 校验脚本/规则、插件构建工作流、签名配置门禁、兼容矩阵生成与解析 |
| 诊断与支持 | 宿主白名单诊断环、`audit_sensitive_data.py`/规则、远程构建 Issue 表单 |

交接包的 `source-inventory.json` 是精确文件清单；本表只是评审导航。审查者如发现数据流进入未列文件，应扩大范围并在报告中
记录，而不是因为文件未列出就停止追踪。

## 5. 已有证据入口

| 子项 | 证据目录 | 当前自动化结论 |
|---|---|---|
| 五档功能矩阵 | `m3-4-qualification-2026-08-31` | 15/15 资格场景 |
| 有界 ZIP/path | `m3-4-g5-bounded-zip-2026-08-31` | JVM 12/12、设备聚焦 6/6、整类 31/31 |
| 宿主输出复核/API 36 | `m3-4-g5-output-validation-2026-08-31` | producer 1/1、正负输出复核 1/1 |
| 宿主输出复核/API 24 | `m3-4-g5-output-validation-api24-2026-08-31` | 旧签名 API 分支正负各 1/1 |
| 宿主输出签名者绑定 | `m3-4-g5-signer-binding-2026-09-01` | 宿主 JVM 44/44、API 36 3/3 + 真实 Binder 1/1、0 findings/errors |
| 敏感数据生命周期 | `m3-4-g5-sensitive-data-audit-2026-08-31` | 设备 1/1、扫描专项 8/8、四批 0 findings/errors |
| 确定性 fuzz | `m3-4-g5-deterministic-fuzz-2026-08-31` | 72/72 unique、JVM 24/24、最终设备 39/39 |
| Binder 死亡 | `m3-4-g6-binder-death-2026-08-31` | 唯一终态、旧输出/清理/恢复均通过 |
| 能力关闭负向门禁 | `m3-4-g6-capability-gate-2026-08-31` | PREPARE 前拒绝、零 progress/staging |

这些证据只能证明已执行的样本。审查者应优先寻找样本之间的组合漏洞、检查/使用时序差异、平台 API 分支差异和异常清理遗漏。

## 6. 最低审查清单

### 6.1 输入、解析与文件系统

- 所有 extras 只接受白名单键和精确类型，Parcelable 数值不会经字符串/浮点隐式转换；
- FD 声明长度、实际复制长度、SHA-256、ZIP 中央目录和实际解压字节之间不存在可绕过的不一致；
- POSIX/Windows/UNC 绝对路径、规范化重复、`.`/`..`、空段、控制字符、超长 UTF-8/段数和 symlink 风险均失败关闭；
- 条目数、压缩输入、单条/总展开量、单条/累计压缩比及实际流式字节均有上限且使用防溢出算术；
- 校验失败发生在目标目录或用户最终产物被替换之前，失败/取消/死亡后没有会话工作区复活。

### 6.2 配置、二进制编辑与资源消耗

- JSON 根、深度、大小、尾随 token、字段类型、数组数和字符串上限与实际消费者一致；
- 图标在完整 bitmap 分配前检查压缩大小、边长和像素总数，解码/OOM 只产生固定安全错误；
- Manifest/ARSC 固定槽、NUL 预算、包名/版本范围和输出 basename 后缀预算不会越界或截断成另一身份；
- 空间估算、Long 饱和、模板 4 倍展开约束和 256 MiB reserve 不会被缺字段或负值降为不安全的零；
- CPU、内存、磁盘和超时上限之间没有可造成长期拒绝服务且无法取消的明显路径。

### 6.3 密钥、签名与输出完整性

- TypeScript 一次性密钥只在内存短暂存在，消费/失败/取消/关闭后从 Bundle 移除并覆盖；
- keystore/alias 口令不进入日志、callback/result、异常链、证据或最终 APK，引用在全部终态清除；
- 默认/自定义 keystore 格式判断不依赖被工作区改写的文件扩展名；
- 插件输出由宿主在同目录临时文件上重新计算 size/SHA-256，执行安全 ZIP、`apksig`、预选签名者和平台包身份复核后才原子发布；
- 宿主在打开 Binder 会话前从默认公开摘要或所选自定义 keystore 固定预期证书；输出必须是与其精确匹配的单一已验证签名者，
  不能信任插件上报摘要，也不能仅以“存在任意合法签名”通过；
- 宿主 APK 只打包默认证书公开摘要而不打包默认私钥库，自定义 keystore 格式回退不改变实际签名所用 provider 语义；
- 签名验证不存在“解析到证书即视为已验证”、旧 API 空签名者、多个签名者或篡改后继续安装的分支；
- 诊断只接受枚举/数值白名单，不把底层异常、恶意文件名、绝对路径或正文拼回日志。

### 6.4 身份、协议与生命周期

- 候选选择同时约束官方身份/签名、宿主闭区间、协议、ABI 和 `supportsRemoteBuild=true`，风险开关不能绕过安全门禁；
- 功能默认关闭在发现、会话创建和插件入口多层失败关闭；
- callback 恰好一个终态，迟到回调/解绑、Binder death、重复 close、取消和 worker 完成不会重复发布或误伤替代连接；
- R1 失败停止且保留旧产物；不存在被误称为“回退”的静默重试或循环；
- Node 旧标签、项目类型、配置、入口扩展名和执行模式在宿主/插件两侧一致拒绝。

## 7. 建议复跑

```powershell
python scripts/verify_remote_build_protocol_docs.py
python -m unittest discover -s scripts/tests -p "test_*.py" -v
.\gradlew.bat :plugin-api:apk-builder-template:testDebugUnitTest :app:testDebugUnitTest --no-daemon
```

设备故障注入需要显式指定隔离 AVD、Runtime Kit 和资格开关；不得把普通 `connectedDebugAndroidTest` 广播到在线实体机。命令、环境变量
和期望断言分别见 `docs/remote-build-e2e-drill.md`、`docs/remote-build-fuzz-audit.md` 与 `docs/sensitive-data-audit.md`。

## 8. 发现与准入规则

- `Critical/阻断级` 或 `High/高危` 未解决数量必须为 0；任何一个即 G5 失败；
- 修复后的 Critical/High 必须引用源码差异、回归用例和新证据摘要，并由独立审查者复核关闭；
- Medium/Low 必须逐项记录影响、可利用前提、接受理由、负责人和截止版本，不能只写“已知风险”；
- scanner error、证据哈希不匹配、快照不一致、无法确认签名/密钥边界均按审查失败处理；
- 审查通过只关闭 G5，不授权 R1、签名发布、预览放量、R2/R3 回退或默认启用。

## 9. 独立审查决定模板

以下字段必须由独立审查者填写；当前刻意留空：

| 字段 | 值 |
|---|---|
| 审查者姓名/团队 | `PENDING` |
| 与实现工作的独立性声明 | `PENDING` |
| 审查日期与时区 | `PENDING` |
| 交接包 `manifest.json` SHA-256 | `PENDING` |
| Critical：发现 / 未解决 | `PENDING` |
| High：发现 / 未解决 | `PENDING` |
| Medium/Low 及处置链接 | `PENDING` |
| 复跑命令与环境 | `PENDING` |
| 最终结论 `PASS` / `FAIL` | `PENDING` |
| 审查报告/签字引用 | `PENDING` |

在这些字段完成且交接包哈希一致之前，G5 必须继续显示为未通过。
