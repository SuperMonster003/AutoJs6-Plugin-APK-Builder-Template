# 远程 APK 构建发布证据索引

- 关联: ROADMAP M3-4 (仓库内工程门槛), M8 (外部门槛), `docs/evidence/m3-4-gates.md` (逐门槛审计记录),
  `docs/remote-build-rollout.md`
- 范围提醒: 本索引针对**远程构建默认启用 (GA)** 的门槛。插件自身的首个正式发布 (ROADMAP M7) 不以本索引为前置 ——
  远程构建随包提供但默认关闭, 两条路径互不阻塞。

## 1. 发布结论

截至 2026/09/01，本索引的结论是 **R0 / No-Go for default or release**。

G1、G2、G3、G4、G6 的 R1 控制面以及 G5 的仓库内自动化子项已有证据；G5 独立安全审查、G7 双维护者确认、至少一个完整
宿主预览周期、可发布签名资产仍未完成。真正独立的构建回退、一次远程/一次回退循环保护与其等价性则是进入 R2/R3 的硬门槛。
本文只整理材料和空白批准栏，不启动 R1、不签名、不发布、不写兼容矩阵正式 Release 记录。

## 2. 源码与状态锚点

| 项目 | 当前值 |
|---|---|
| 插件基线 HEAD | `e7f4918e036171bfab4cac79b1b461e5cea5a1a7` |
| 宿主基线 HEAD | `b833227b01d05f69bc34d8a69c9f4d113a5a4ef6` |
| 冻结旧宿主构建器 | `fff913caafa3dc0d6172638c8532b027c0dfa8c0`，仅用于 G3/G4 对照 |
| 插件仓库本地提交邮箱 | `30370009+SuperMonster003@users.noreply.github.com` |
| 官方插件能力 | `supportsRemoteBuild=false` |
| 插件 Gradle 默认 | `autojs.apkBuilder.templatePlugin.enableRemoteBuild=false` |
| 宿主默认 | 开发者远程构建总开关关闭 |
| 当前阶段 | R0 |

两个主工作树都含未提交实现，单独的 HEAD 不能唯一固定当前源码。G5/G7 审查必须引用
`m3-4-g5-independent-review-handoff-2026-09-01-v2/manifest.json` 的最终 SHA-256；提交、PR 或发布前还需重新生成一次最终候选快照。

## 3. G1—G7 门槛状态

| 门槛 | 状态 | 结论 |
|---|---|---|
| G1 协议与功能正确性 | 通过 | 五档 API/ABI、两种源形态、TypeScript、签名、图标、native、运行主脚本闭环均有证据 |
| G2 稳定性与清理 | 通过 | 180/180 正常构建 + 30/30 分阶段取消，0 非预期失败 |
| G3 双构建器等价性 | 通过 | 静态/运行/双向升级、TypeScript、ImageQuant、Node 双边失败关闭 |
| G4 性能与设备压力 | 通过 | 三档 p95 比率低于 1.5；API 24/1.5 GiB 压力档 60/60、零 LMK/OOM |
| G5 完整性与安全 | **未通过** | 仓库内自动化已收口；独立安全审查尚未填写结论 |
| G6 R1 控制面 | 通过 | 默认关闭、能力门禁、单次远程、失败停止、原子输出、脱敏诊断、Binder death |
| G6 R2/R3 回退 | **未通过** | 真正独立构建路径与一次回退循环尚未实现/验收 |
| G7 发布与支持 | **未通过** | 索引/协议门禁/Issue 表单已准备；签名 RC、预览周期和双维护者确认缺失 |

## 4. 内容寻址证据

所有路径均位于：

```text
D:\idea-projects\.a6-compat-audit-artifacts
```

| 用途 | 目录 | `manifest.json` SHA-256 | `SHA256SUMS` SHA-256 |
|---|---|---|---|
| G1 五档功能矩阵 | `m3-4-qualification-2026-08-31` | `aecdeafa7203719eaaac39dc6095e0fdf4e43803b99e115fecdb0c4a51e44f81` | 早期包未生成独立清单文件 |
| G1 主脚本运行闭环 | `m3-4-g1-runtime-smoke-2026-08-31` | `fb8e2cf5b48b67f4ab6fbb5f8dddc6d65baf7aaaf677abf0e0f22863ba452f1e` | 早期包未生成独立清单文件 |
| G2 稳定性 | `m3-4-g2-stability-2026-08-31` | `dfb5150636b60067fdc4b3f5427882ff4380a730803dcd6f438ef52b09275433` | `f111c0f96c509d51d2d4f23284a34cc260b853170848e33e039cc1fab58d2783` |
| G3 等价性 | `m3-4-g3-equivalence-2026-09-01` | `dba0ae1694d77fbb56e733b4bd2ecfc4e2ad9e9cfea5e111707f38f5cff7b64e` | `d1bcb2406e92b8a37ab104260e9548edf36dbeefef2c1d7b79387596ad82eee2` |
| G4 性能/压力 | `m3-4-g4-performance-pressure-2026-09-01` | `eaeeba556bd2520024e7557a2fc63681d07343eac5a7a70448706e08bc6424f4` | `c8a22c2648d435785f3d23ba1da4ee15c39be460fe7414347cd6d59e6a1549f5` |
| G5 有界 ZIP/path | `m3-4-g5-bounded-zip-2026-08-31` | `429033f515c46495b854fa942c06dbd21393f44ba066110768aa64b3d4233760` | `abda33ab277caedd7e3d69465b9c0d07e9ca9b34710b1528b5a98362d56ab62c` |
| G5 输出复核/API 36 | `m3-4-g5-output-validation-2026-08-31` | `c78b1d9975026094e739dbfab16a658006ea39a80e1c83c7f040b44dfab7cc06` | `6c895629217f361a8904c7483522b86112eca4267e6e270351afc636f9f853cd` |
| G5 输出复核/API 24 | `m3-4-g5-output-validation-api24-2026-08-31` | `a8c094e8c4132a95446eeaa9e029dcc10db8c7bfe2d2ae10423fb802990dec60` | `bcf4076c0e79ded42f1ad30a37105e761daca6de8ff677c32a89642ac7d7f6a1` |
| G5 输出签名者绑定 | `m3-4-g5-signer-binding-2026-09-01` | `e336b45b1350eb54faaf3ac22e783203bef6ee9cfa0005c65c75fec242b1c8c9` | `7e561fcd4b9fe3f3a291afdf6ef8d685595dd7beaa7b9eba4fd20ff697ab27b2` |
| G5 敏感数据 | `m3-4-g5-sensitive-data-audit-2026-08-31` | `71850d293eb970e18388553590fdd7a4b8d5c9f5bf8ed77d56656c52239e1980` | `a62f7bbbcf6e669a7b166ca33986d57c653a587fbb89182613fd3c743d0098fa` |
| G5 确定性 fuzz | `m3-4-g5-deterministic-fuzz-2026-08-31` | `35b65a9d21e67268715745ae8bc93d3fbba250823a3e52c0f1e66a491f4fb129` | `3566248156c8be727ee11931277633b7ea35d37210764d7a8a797a2a8a6678ed` |
| G6 开关 UI | `m3-4-r1-host-gate-2026-08-31` | `b56074220f264fa9e81df60ab937f1ad64c5ce57379da4b68a9cc2e763da0c70` | 早期包未生成独立清单文件 |
| G6 Binder death | `m3-4-g6-binder-death-2026-08-31` | `fb1ce746fd41f3edcf87c98f2fa2930ef1057a24c5f034c80a853321a626a609` | 早期包未生成独立清单文件 |
| G6 capability=false | `m3-4-g6-capability-gate-2026-08-31` | `1ea6d9d945973128010c9f25905e2995b4530b05fa5e4ab3b81b5f8cb8405ca3` | 早期包未生成独立清单文件 |

早期 manifest 的 schema 与后期不同，因此表中只记录原文件摘要，不把缺少独立 `SHA256SUMS` 伪装成已存在。发布审阅者仍应按各
manifest 自身格式重算列出的文件摘要。

## 5. 协议与文档门禁

`scripts/verify_remote_build_protocol_docs.py` 从当前源码解析并要求协议文档出现以下 73 个公开符号：

- AIDL 远程方法 10 个；
- `ApkBuildRequest` 字段 20 个；
- `ApkBuildResult` 字段 10 个；
- `ApkBuildProgress` 字段 7 个；
- request extras 键 11 个；
- 远程 capability 键 4 个；
- status 4 个、step 6 个、协议常量 1 个。

当前检查为 73/73，0 missing；对应两条 Python 测试验证当前清单和缺字段故障注入。`.github/workflows/docs-consistency.yml`
会在多语言文档生成后运行该门禁。它是字段覆盖检查，不代替语义评审；协议版本、兼容行为和安全边界仍需人工复核。

## 6. 候选资产与签名状态

| 资产 | SHA-256 / 签名 | 发布资格 |
|---|---|---|
| G4 最终默认关闭 `release` 变体 APK | `88c97d357c7058ea08924a298153cbee3f17ec7e221fa3783387076e0af5e427`；`apksigner` exit 1，未签名 | **不可发布**；只证明 Release 变体可编译且能力为 false |
| Debug/资格 APK | Android Debug/同测试证书，具体字节在各证据包中 | **不可发布**；只用于隔离 AVD 资格 |
| 正式 universal + 四 ABI Release | 尚未由受信工作流生成 | `PENDING` |

正式候选必须由受信签名工作流从确定提交生成五个 ABI 资产，逐个记录文件名、大小、SHA-256、签名证书摘要、插件版本、宿主范围、
Runtime Kit ID 与协议版本；随后复跑最小 G1/G5/G6 smoke。不得用 debug key 或本地临时密钥补齐 G7。

## 7. G7 可执行清单

| 项目 | 当前状态 | 完成条件 |
|---|---|---|
| G1—G6 证据索引 | 已准备 | 发布候选冻结后重新核对所有摘要与候选源码 |
| 协议字段覆盖 | 73/73 通过 | CI 对最终候选继续通过，人工语义复核完成 |
| 10 语言 README | R0 文案一致 | 只有实际进入 R1/R2/R3 时才按批准阶段切换，不提前宣传 |
| 11 份插件说明 | R0 文案一致 | 同上，并写明失败/回退真实语义 |
| 10 语言 CHANGELOG | 已含默认关闭、空间预检、单文件编号与输出签名者身份绑定修复 | 正式候选补精确 host/plugin/Runtime Kit 版本、默认态和关闭方法 |
| 支持入口 | 插件与宿主均新增 `remote_apk_build.yml` | 合并后确认表单可用，报告只含脱敏版本/status/step/error 类别 |
| G5 独立安全审查 | `PENDING` | 按 `docs/remote-build-independent-security-review.md` 完成，Critical/High 未解决为 0 |
| 正式签名候选 | `PENDING` | 受信工作流生成并验证五 ABI Release；本地 unsigned/debug 包不计 |
| 宿主完整预览周期 | `PENDING` | 固定候选、开始/结束时间、参与范围、问题分类和退出结论可复核 |
| 宿主维护者确认 | `PENDING` | 引用精确 manifest/候选摘要并签字 |
| 插件维护者确认 | `PENDING` | 引用精确 manifest/候选摘要并签字 |
| R2/R3 独立回退 | `PENDING` | 独立路径、一次循环保护、取消共享和等价性设备证据 |

## 8. 维护者批准模板

批准必须针对同一内容寻址候选，不能只写“测试通过”：

| 字段 | 宿主维护者 | 插件维护者 |
|---|---|---|
| 姓名/身份 | `PENDING` | `PENDING` |
| 日期与时区 | `PENDING` | `PENDING` |
| 候选提交/源码 manifest | `PENDING` | `PENDING` |
| universal + 四 ABI 资产 manifest | `PENDING` | `PENDING` |
| G5 独立审查报告/摘要 | `PENDING` | `PENDING` |
| 预览周期报告 | `PENDING` | `PENDING` |
| 批准阶段（仅 R1/R2/R3 之一） | `PENDING` | `PENDING` |
| Go / No-Go 结论与签字引用 | `PENDING` | `PENDING` |

任一批准栏为空、两人引用的候选摘要不同、独立审查失败、签名资产缺失或预览周期未结束时，结论保持 No-Go。

## 9. 下一步顺序

第 2 步属于 ROADMAP M7 (首个正式发布) 的自主轨, 与 GA 无关也必须先做; 其余各步属于 M8 外部轨。

1. 封存独立安全审查交接包，由独立审查者完成 G5 并处理发现； (ROADMAP M8-1)
2. 将当前未提交实现整理成可审阅提交/PR，由 CI 重跑构建、JVM、文档和协议门禁； (ROADMAP M7-1 / M7-2)
3. 由受信工作流生成正式签名候选及五 ABI 清单，不能复用本地 unsigned/debug 产物； (ROADMAP M8-3, 复用 M7-4 建立的发布流程)
4. 在默认关闭、显式 opt-in 的 R1 语义下完成一个宿主预览周期； (ROADMAP M8-3)
5. 宿主与插件维护者对同一候选分别给出可追溯签字； (ROADMAP M8-3)
6. 只有 R1 外部门槛都满足后才讨论启用候选能力；R2/R3 仍需先完成真正独立回退。 (ROADMAP M8-2 / M8-4)
