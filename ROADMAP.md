# AutoJs6-Plugin-APK-Builder-Template 开发路线图

本路线图跟踪 APK Builder Template 插件的能力规划与完成情况。README 面向最终用户, 深度工程内容集中在本文件与 `docs/`。

开发哲学: **让 APK Builder 插件完整拥有构建核心, 让 AutoJs6 保持精简并把安全准入与输出复核做扎实。**

## 分轨原则

条目按 **"谁能关闭它"** 分轨, 而不是按主题分轨。把 "写代码就能完成" 与 "需要外部审查者 / 日历时间 / 他人签字" 的条目
平铺在同一张清单上, 会让后者无限期拖住整份路线图:

| 轨道 | 含义 | 归属 |
|---|---|---|
| **自主轨** | 本仓库或宿主仓库内的工程, 维护者自己即可关闭 | M1—M7 |
| **外部轨** | 依赖独立审查者, 预览周期, 双维护者签字等外部输入 | M8 |

**外部轨条目不参与 "功能完整且具备发布条件" 的判定。** 正式路径使用 `supportsApkBuild=true` 与
`apkBuilderBuildExecutionMode=on-device-plugin`; `supportsRemoteBuild=false` 只保留旧实验入口的关闭语义, 不再关闭普通打包。
架构决策见 `docs/plugin-managed-apk-build-architecture.md`。

## 状态与证据规则

- `[x]` 表示条目已完成, 并且本仓库或对应宿主仓库中存在可复核证据 (代码, 标签, 文档或工作流)。
- `[ ]` 表示尚未完成, 或虽有阶段性结果但尚未达到该条目的 "验收" 定义。
- 范围标记: `[P]` 仅本插件仓库, `[H]` 仅宿主 AutoJs6 仓库, `[H+P]` 两侧协同。

证据强度按条目影响面分级, 不再一刀切:

| 级别 | 适用范围 | 要求 |
|---|---|---|
| **T1** | 默认开启的用户可见行为, 安全边界, 签名与信任链 | 真实设备 / AVD 证据 + 自动化测试 |
| **T2** | 默认关闭的实验能力, 内部机制 | CI + 单元 / instrumented 测试 |
| **T3** | 文档, 多语言语料, 脚本 | 生成器与门禁脚本通过 |

路线图条目只保留 **一行结论 + 证据指针**, 审计正文归档到 `docs/evidence/` 与工作区外内容寻址证据包
(`docs/remote-build-release-evidence-index.md` 第 4 节)。

## 里程碑总览

| 里程碑 | 主题 | 轨道 | 状态 |
|---|---|---|---|
| M1 | 模板分发核心能力 | 自主 | 已完成 (v6.7.1 Alpha4 ~ v6.8.0 Alpha5) |
| M2 | 文档易读性与多语言流水线 | 自主 | 已完成 |
| M3 | 插件侧构建基础 (历史实验协议) | 自主 | 已完成; 2026/09/02 迁移为 M7 正式插件托管路径 |
| M4 | 体积与兼容性 | 自主 | 已完成 |
| M5 | 校验与安全强化 | 自主 | 已完成 |
| M6 | 插件独立版本机制与兼容矩阵 | 自主 | 进行中 (M6-1 至 M6-5 已完成; M6-6 待 M7 提供发布端点) |
| **M7** | **首个正式发布 v1.0.0 + 唯一插件托管构建路径** | 自主 | **进行中 (首轮候选已拒绝; 正在生成替代候选)** |
| M8 | GA 后独立保证与兼容层收敛 | 外部 | 未开始 (长期目标, 不阻塞首发) |

**当前 "具备发布条件" 的判定只取决于 M7。** M6-6 依赖 M7 产出的首个正式发布端点; M8 全部条目在发布之后继续推进。

## M1 —— 模板分发核心能力 (v6.7.1 Alpha4 ~ v6.8.0 Alpha5)

> 主题: 让 AutoJs6 的 "打包应用" 功能可以完全依赖外置插件提供的模板 APK。

- [x] [P] **插件服务与模板读取**: 通过 `org.autojs.plugin.INFO` 与 `org.autojs.plugin.APK_BUILDER` 双服务对外提供插件信息与模板 APK, 模板经进程间管道流式传输。证据: `app/src/main/java/org/autojs/plugin/apkbuilder/template/impl/ApkBuilderTemplatePluginService.kt`, 标签 `v6.7.1-alpha4`。
- [x] [P] **Runtime Kit 构建期校验**: 构建插件时核对 Runtime Kit 全部文件的 SHA-256 摘要与 `template.apk` 必需条目, 校验失败即中断构建。证据: `app/build.gradle.kts` 的 `verifyRuntimeKit`, `scripts/verify_runtime_kit.py`。
- [x] [P] **版本兼容检查与能力上报**: 向宿主上报宿主版本, 协议版本, 模板包名, 模板摘要, Runtime API 摘要与远程构建能力; 不匹配时返回警告或阻止级别。证据: `ApkBuilderTemplateMetadata.kt`, `runtime-kit/runtime-kit.json`。
- [x] [H+P] **自动化发布流水线**: AutoJs6 主仓库发版后经 `repository_dispatch` 触发本仓库下载并校验 Runtime Kit, 构建, 签名 (含 `SIGNING_CERT_SHA256` 证书指纹校验) 并上传插件 APK; M4-1 后一次发布包含 universal + 四个 ABI 资产。证据: `.github/workflows/build-from-runtime-kit.yml`, `.github/workflows/validate-signing-config.yml`。
- [x] [P] **端到端发布演练清单**: 覆盖 CI 检查, 本地 APK 资产检查与 9 类设备场景。证据: `docs/e2e-release-drill.md`。
- [x] [P] **10 语言基础资源**: `strings.xml` 与 `plugin_instruction.md` 覆盖简体中文, 香港繁体, 台湾繁体, 英语, 法语, 西班牙语, 日语, 韩语, 俄语与阿拉伯语。证据: `app/src/main/res/values-*/`, `app/src/main/res/raw-*/`。

## M2 —— 文档易读性与多语言流水线

> 背景: 用户反馈 README / CHANGELOG 晦涩难懂。参照 DEX-Compiler, Kotlin-Runtime 等姊妹插件的 Python 多语言生成方案重建文档; README / CHANGELOG 面向最终用户, 工程细节收纳到 "技术参考" 与本路线图。

- [x] [P] **M2-1 README 语料重写**: 以 "简介 / 工作原理 / 功能 / 快速上手 (装—用—验证—排错) / 能力边界 / 常见问题 / 技术参考 / 路线图 / 发行历史 / 许可证" 为主线重写 `.readme/lang_*.json`, 覆盖全部 10 种语言。
- [x] [P] **M2-2 CHANGELOG 重写与补全**: 条目改为以用户可感知变化为粒度, `提示` 类目标注宿主版本配套要求; 补记缺失的 v6.7.1 Alpha4 发布记录。证据: `.changelog/lang_*.json`, 标签 `v6.7.1-alpha4`。
- [x] [P] **M2-3 生成器加固**: `.python/generate_markdown.py` 增加跨语言键序/版本序校验与占位符残留断言, 任一语言文件缺键, 乱序或漏翻时生成直接失败。
- [x] [P] **M2-4 建立路线图**: 新增本文件, README 各语言版本在 "开发路线图" 小节链接至此。
- [x] [P] **M2-5 CI 文档一致性门禁**
  - 内容: 在工作流中运行 `.python/generate_markdown.py` 后执行 `git diff --exit-code`, 防止 JSON 语料与生成产物脱节。
  - 验收: 手改生成产物或改 JSON 后漏跑脚本的提交, 会在 CI 中失败并指出差异文件。
  - 证据: `.github/workflows/docs-consistency.yml` 在 push / pull request / 手动触发时重生成文档, 以 `git diff --exit-code` 和未跟踪文件检查作为门禁; 两类沙箱故障注入均按预期失败并列出差异文件 (2026/08/30)。

## M3 —— 插件侧构建基础 (历史实验协议)

> 历史主题: 把插件侧轻量打包 (解包模板, 写入项目, 重写 Manifest 与 `resources.arsc`, ABI 裁剪, 重新签名) 做到功能正确,
> 协议清晰, 边界可控。早期因跨应用进程而称为“远程构建”，实际从未经过网络。
>
> 2026/09/02 迁移结论: 这些实现与证据成为唯一正式“设备内插件托管构建”的基础；正式能力使用
> `supportsApkBuild` / `APK_BUILD_VERSION` / `on-device-plugin`，不受旧实验开关控制。旧 remote 能力键和内部类名仅作兼容保留。

- [x] [P] **M3-0 TypeScript 构建暂存加密保护**: 远程构建过程中的 TypeScript 暂存内容加密处理, 避免以明文落盘。证据: 提交 `e7f4918`, `RemoteScriptEncryptor.kt`, `RemoteTypeScriptStagingDecryptor.kt`。
- [x] [P] **M3-1 插件侧构建端到端用例**
  - 内容: 覆盖构建会话的四类结局 (成功 / 取消 / UNSUPPORTED / 宿主不匹配含 `allowRiskyBuild` 分支), 以及单文件源与项目源两种输入形态。
  - 验收: 用例以 instrumented tests 或可复跑的演练清单形式合入, 全部通过; 失败路径均能拿到明确的 `warnings` / `errors`。
  - 证据: `RemoteApkBuildSessionInstrumentedTest.kt` 39 个测试方法, 五档 API/ABI 各 15 条功能矩阵 0 skipped/failed, API 36/x86_64 整类 39/39。复跑与剩余边界见 `docs/remote-build-e2e-drill.md`, 完整记录见 `docs/evidence/m3-4-gates.md` 第 3 节。
- [x] [P] **M3-2 构建协议文档化**
  - 内容: 以 `plugin-api/apk-builder-template` 的 AIDL (`IApkBuildSession`, `IApkBuildCallback`, `ApkBuildRequest`, `ApkBuildResult`) 为准编写 `docs/remote-build-protocol.md`, 说明正式 `APK_BUILD_VERSION`、旧 `REMOTE_BUILD_VERSION` 别名、密钥库接口、进度步骤与错误语义。
  - 验收: 文档字段与 AIDL 一一对应; 协议版本号变更时文档同步更新。
  - 证据: `docs/remote-build-protocol.md` 覆盖正式/兼容能力发现、会话生命周期、request / extras / project JSON / keystore 字段、ZIP 布局、FD 所有权、进度与 result / callback / status 映射、安全边界与升版清单; `scripts/verify_remote_build_protocol_docs.py` 从真源核对 101 个公开符号 (101/101), 已接入 `docs-consistency.yml`。
- [x] [H+P] **M3-3 历史实验放量条件**
  - 内容: 明确远程构建从 experimental 到默认开启的判定标准 (稳定性指标, 宿主侧开关与回退策略)。
  - 验收: 标准落入本路线图或 `docs/`; README 的 "能力边界" 与 FAQ 同步更新。
  - 证据: `docs/remote-build-rollout.md` 与 `docs/remote-build-fallback-decision.md` 保存 2026/09/01 前的实验路径决策与证据。其“恢复第二构建器后才能默认启用”结论已由 2026/09/02 架构 ADR 取代，不再作为正式路径发布门槛。
- [x] [P] **M3-4 资格门槛的仓库内工程部分**
  - 内容: 按 `docs/remote-build-rollout.md` 完成 G1—G7 中**不依赖外部输入**的全部工程与自动化: 功能矩阵, 稳定性批次, 双构建器等价性, 性能与压力, 安全实现与自动化审计, 宿主 R1 控制面。
  - 验收: 上述各项均有可复核的设备 / 单测 / 脚本证据; 依赖外部审查者, 预览周期或他人签字的部分转入 M8, 不计入本条。
  - 逐门槛状态 (详细记录见 `docs/evidence/m3-4-gates.md`):

    | 门槛 | 状态 | 一行结论 |
    |---|---|---|
    | G1 协议与功能正确性 | [x] | 五档 API/ABI 功能矩阵 + 两种源形态 + 真实 host→Binder 运行闭环 |
    | G2 稳定性与清理 | [x] | 180/180 正常构建 + 30/30 分阶段取消, 0 非预期失败 |
    | G3 双构建器等价性 | [x] | 静态/运行/双向升级/TypeScript/ImageQuant 一致, Node 双边失败关闭 |
    | G4 性能与设备压力 | [x] | 三档 p95 均低于 1.5x, 大小差异低于 2%; 1.5 GiB 为最低资格档 |
    | G5 完整性与安全 (仓库内) | [x] | 有界解压, 输出结构/签名者身份绑定, 敏感数据审计, 确定性 fuzz 均已收口 |
    | G6 R1 控制面 | [x] | 默认关闭总开关, 能力门禁, 失败停止, 原子输出, 脱敏诊断, Binder death |
    | G5 独立安全审查 | → M8-1 | 需外部审查者; GA 后增强项, 不阻塞 M7 |
    | G6 R2/R3 独立回退 | 历史条件 | 2026/09/02 ADR 已以唯一插件路径取代恢复第二构建器的方向 |
    | G7 发布与支持 | → M7 | 由替代正式候选、五 ABI 资产与装机闭环承接 |

  - 阶段结论: 仓库内工程证据已收口。历史实验入口继续保持 `supportsRemoteBuild=false`; 正式路径改由
    `supportsApkBuild=true` 独立准入，并把这些历史资格证据作为实现基线而非第二条构建路径的放量依据。

## M4 —— 体积与兼容性

> 主题: 降低用户获取成本, 放宽不必要的版本硬约束。

- [x] [H+P] **M4-1 按 ABI 拆分模板变体**
  - 内容: 在 `inrt-universal` 通用变体之外提供 `arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86` 分架构变体, 由插件中心按设备架构优先分发, universal 兜底。
  - 验收: 发布流水线同时提供五个插件 APK; 宿主可依据 Release 文件名选择安装资产, 并依据 `PluginInfo.variant` / `supportedAbis` 在模板与远程构建调用前复核架构。
  - 证据: `docs/abi-variants.md`; 宿主 `app/build.gradle.kts` 生成并经本地校验的五个 Runtime Kit; 插件 `verify_runtime_kit.py` / `verify_runtime_kit_set.py` 的内容与集合门禁; `build-from-runtime-kit.yml` 的五变体构建, 签名, 上传与矩阵合并; 宿主 `ApkBuilderTemplatePluginHost.kt` 的候选过滤与调用前保护。2026/08/30 本地生成五个 Kit 并实际 assemble 五个插件 APK, 逐包解开内嵌模板核对通过; ABI / 矩阵 Python 回归 6/6 与宿主策略单测 3/3 通过。**外部 Release 资产将由 M7 首次产生。**
- [x] [H+P] **M4-2 补丁级版本兼容**
  - 内容: 利用 `runtime-kit.json` 的 `compatibility` 区间 (`minHostVersionCode` / `maxHostVersionCode` / `allowPatchVersionMismatch`), 允许补丁级差异仅警告而不阻止打包。
  - 验收: 兼容区间放宽后, `docs/e2e-release-drill.md` 设备场景 7 的预期行为与 README FAQ 同步更新。
  - 证据: 宿主 Runtime Kit 生成任务与手动发布工作流支持显式闭区间, 默认仍为精确匹配; Python/Gradle 校验, 插件元数据, 宿主/插件共享策略, 矩阵写入/解析及本地化警告已打通。2026/08/31 以宿主 5277 合成 `5277..5278 + allow=true` 五 ABI Runtime Kit 完成真实组装与三重校验, `allow=false` 的同一区间按预期失败关闭。Python 回归 10/10, 两仓库共享策略单测各 7/7, 宿主矩阵解析单测 10/10 通过。同日 `5275..5276` 候选完成真实设备闭环: Sony G8441 / API 28 上 H0=5275 无警告打包安装启动成功, 原位升级到 H1=5276 后出现可继续的补丁兼容警告并再次成功, API 36 AVD 上 H2=5277 在 UI 中硬阻止且 `allowRiskyBuild=true` 也无法越过声明区间。完整契约与截图见 `docs/compatibility-candidate-audit-2026-08-31.md`。
- [x] [P] **M4-3 Runtime Kit 增量更新可行性**
  - 内容: 评估相邻版本模板差量分发 (如 bsdiff) 的收益与复杂度, 减少每次升级的完整下载。
  - 验收: 形成结论文档合入 `docs/`, 明确做或不做及其原因。
  - 证据: `docs/runtime-kit-incremental-update.md` 与 `scripts/analyze_runtime_kit_delta.py` (2026/08/30) —— 两组相邻模板的精确补丁分别占目标 44.235% / 86.372%, Alpha5 → 6.8.0 外层 ZIP 补丁占 113.737% 已大于全量。结合 Runtime Kit 内嵌于签名插件 APK, 插件侧补丁不能直接减少用户安装包下载, 当前结论 **No-Go**; 待至少 5 个正式发布边完成后重新评估。

## M5 —— 校验与安全强化

> 主题: 把 CI 侧已有的信任链延伸到设备端与工具链内部。

- [x] [H+P] **M5-1 设备端签名证书指纹校验**
  - 内容: 宿主在绑定插件前, 将插件签名证书与受信任指纹 (对齐 CI 的 `SIGNING_CERT_SHA256`) 比对, 非受信签名给出明确提示。
  - 验收: 安装非官方签名的插件时, 打包入口出现可识别的告警文案; 官方签名不受影响。
  - 证据: 宿主 `AidlPluginHost.preBindValidator` 在 Binder 连接前执行 `ApkBuilderPluginTrustPolicy`, 11 套字符串资源提供含包名与实测 SHA-256 的专用告警, 5 项定向单测与 release Kotlin 编译通过。API 36 AVD 负向演练中告警可见且 `onServiceConnected=0`; 替换为官方 `v6.8.0-alpha5` APK 后门禁通过且 `onServiceConnected=1`。详见 `docs/device-signature-validation.md` (2026/08/31)。
- [x] [P] **M5-2 统一 Runtime Kit 校验实现**
  - 内容: 合并 `app/build.gradle.kts#verifyRuntimeKit` 与 `scripts/verify_runtime_kit.py` 的规则来源, 避免两处校验规则漂移。
  - 验收: 两处共享同一规则清单 (必需文件, 必需 APK 条目, 摘要核对), 或存在防漂移的一致性测试。
  - 证据: `scripts/runtime_kit_validation_rules.json` 成为两种实现共同读取的唯一规则源; `:app:verifyApkBuilderRuntimeKit` 可独立运行并作为资产准备前置。真实套件经 Python / Gradle 双实现通过, 损坏 `template.apk.sha256` 的隔离夹具被两边以同一原因拒绝 (2026/08/30)。

## M6 —— 插件独立版本机制与兼容矩阵

> 背景: 插件最初直接复用宿主版本号, 同一宿主版本内无法发布修复版 (versionCode 相同, Android 不识别为更新), 版本号也无法表达插件自身的成熟度。目标: 插件拥有自己的 SemVer 版本线, 同时任意版本 (含旧版) 的 AutoJs6 都能解析到与之配套的插件构建, 必要时引导 "降级"。参考范式: JetBrains Marketplace 与 KSP。核心转变: **版本号只承载识别, 兼容契约完全交给元数据与分发端解析。**

- [x] [P] **M6-1 版本方案决策记录 (ADR)**
  - 内容: 在 `docs/versioning.md` 固化三层模型 —— ① 插件自身版本 (`PLUGIN_VERSION_NAME` SemVer + 自增 `PLUGIN_VERSION_BUILD`); ② 配对元数据 (`BUILT_FOR_HOST_*` / `REQUIRES_HOST_VERSION` / `runtimeApiLevel`); ③ 分发端解析 (兼容矩阵)。
  - 验收: ADR 合入; 两种 versionCode 方案的取舍, 起始版本与迁移路径有明确结论。
  - 证据: `docs/versioning.md` (2026/08/30) —— 采纳方案 B (`hostVersionCode * 100 + 序号`), 起始版本 1.0.0, 迁移路径 5201 → 520101。
- [x] [P] **M6-2 引入独立版本字段与复合命名**
  - 内容: `version.properties` 新增 `PLUGIN_VERSION_NAME` / `PLUGIN_VERSION_BUILD`; `sync_version_from_runtime_kit.py` 改为只同步配对宿主字段; Android versionName 采用复合形式; `PluginInfo.versionName/versionCode` 切换为插件自身版本。
  - 验收: 同一宿主版本可发布多个插件构建且互为 Android 升级; 宿主侧 `openTemplate` 兼容检查行为不变。
  - 证据: `version.properties`, `scripts/sync_version_from_runtime_kit.py` (沙箱演练 392301 → 392302), `build-logic` `Versions.kt` 与 `app/build.gradle.kts`, `ApkBuilderTemplateMetadata.kt`。
- [x] [P] **M6-3 CI 生成并发布兼容矩阵 `compat-matrix.json`**
  - 内容: 发布工作流在上传 APK 后追加/更新矩阵条目 (插件 / 宿主版本, 兼容区间, Runtime API, tag, releasedAt 与 ABI `artifacts` 数组); 同时继续把 APK 上传到宿主同名 tag Release, 保留旧宿主按 tag 查找的通道。
  - 验收: 给定任意 hostVersionCode, 可确定性解析出 "满足兼容区间的最高插件版本"; 给定设备 ABI, 可进一步选择精确资产并在缺失时回退 universal。
  - 证据: `compat-matrix.json`, `scripts/update_compat_matrix.py` (`add` / `resolve --abi`), `build-from-runtime-kit.yml` 的 `concurrency` 串行化。ABI / 矩阵回归 6/6 通过。**首个正式矩阵条目由 M7 产生。**
- [x] [H] **M6-4 插件中心按矩阵解析匹配版本 (含降级引导)**
  - 内容: 宿主插件中心以自身 versionCode 查询矩阵, 提示并下载 "匹配版本" 而非 "最新版本"; 当目标插件 versionCode 低于已装插件时, 给出卸载重装引导。
  - 验收: 旧版 AutoJs6 上的安装引导指向配套旧插件; `docs/e2e-release-drill.md` 设备场景 1/7 的预期行为与文案同步更新。
  - 证据: 宿主 `ApkBuilderCompatibilityMatrixResolver.kt` / `PluginIndexRepository.kt` / `PluginReleaseTargetPolicy.kt` / `PluginInstallCompatibilityPrompt.kt` (2026/08/30); 解析/目标策略/列表语义单测 14/14, Plugin Center 包全量 47/47, `:app:compileAppReleaseKotlin` 通过。
- [x] [H+P] **M6-5 文档与提示文案同步**
  - 内容: README 快速上手/FAQ 从 "版本必须完全一致" 调整为 "插件中心自动匹配, 手动下载时按 Release 标签或矩阵对应"; `plugin_instruction.md` 与 CHANGELOG 提示同步。
  - 验收: 10 语言语料同步更新并重新生成, 生成器校验通过。
  - 证据: 10 语言 README 与 FAQ, 11 份 `plugin_instruction.md`, 10 语言 CHANGELOG 及宿主 11 套 `strings.xml` 均已同步, 文档生成器校验通过 (2026/08/30)。
- [ ] [H+P] **M6-6 与 M4-2 打通: 区间兼容降低矩阵密度** —— *依赖 M7*
  - 内容: 矩阵区间与 `runtime-kit.json` 的 `compatibility` 联动, 补丁级宿主差异共用同一插件构建, 从源头减少需要降级的场景。
  - 验收: 至少一个补丁区间在矩阵中以 `min < max` 形式**正式发布**并通过端到端演练。
  - 现状: 实现侧已全部就绪 —— `allowPatchVersionMismatch` 贯穿 Runtime Kit, 能力键, 共享判定策略, `compat-matrix.json` 写入器与宿主解析器, 未显式授权的放宽条目在各层失败关闭, 区间外也不能由 `allowRiskyBuild` 绕过; 合成矩阵可让区间两端解析到同一插件。
  - 唯一剩余步骤: **等 M7 建立首个正式发布端点后**, 选择一对可发布的补丁区间端点, 由受信流水线发布首个 `min < max` 的插件版本并原子写入矩阵, 复核解析结果。在此之前不向权威矩阵写入虚构发布记录, 因此 `formalMatrixEligible=false`。

## M7 —— 首个正式发布 v1.0.0

> 主题: **把已完成的能力真正交付出去。** 这是当前唯一决定 "项目是否具备发布条件" 的里程碑。
>
> 范围声明: 本次发布**包含**模板分发 (M1), 多语言文档 (M2), 五 ABI 变体 (M4-1), 补丁级兼容 (M4-2),
> 设备端签名门禁 (M5), 独立版本与兼容矩阵 (M6-1—M6-5), 以及唯一正式的设备内插件托管构建路径。
> 旧实验入口继续 `supportsRemoteBuild=false`; M8 的任何条目都不是本里程碑的前置。
>
> 2026/09/01 候选审计结论: 上述范围在当前宿主架构中尚不可同时满足。AutoJs6 `b2fd2b6ae` 已移除完整的宿主
> 本地构建器, 而候选 `71e684b8d` 又按 R0 规则默认关闭远程总开关; 官方插件也上报 `supportsRemoteBuild=false`。
> 因此普通打包入口没有可执行路径。首轮签名候选已按失败关闭原则拒绝, 在重新建立非实验性打包路径并完成新候选装机
> 验收前, **禁止运行正式发布路径**。完整证据见 `docs/v1.0.0-rc1-candidate-audit-2026-09-01.md`。
>
> 2026/09/02 已采纳替代架构: 不恢复 AutoJs6 进程内构建器，而把插件侧构建正式化。宿主只负责发现/信任/兼容准入、请求准备、
> UI 与输出独立复核；插件独占模板处理、资源修改、ABI 裁剪、密钥库与签名。正式能力与旧实验键分离，详见
> `docs/plugin-managed-apk-build-architecture.md`。该决策需由新的精确宿主 SHA、五 ABI 插件候选和完整装机闭环重新验收。

- [x] [P] **M7-1 整理未提交实现为可审阅提交**
  - 内容: 将此前两个工作树中的实现 (远程构建加固, 输出复核, 脱敏审计, 协议门禁, 文档语料等) 固定为可定位, 可复核的提交。
  - 验收: `git status` 干净; 每个提交有独立可读的主题; 宿主侧对应改动同步落库。
  - 证据: 插件侧 `e7f4918` (TypeScript 暂存保护), `9587f28` (远程构建/发布流程收口) 与 PR [#5](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/pull/5), 宿主侧 `71e684b8d` (插件中心与能力集成); 2026/09/01 两个主工作树均为干净状态。
- [x] [P] **M7-2 全量门禁绿灯**
  - 内容: 在整理后的提交上跑通全部既有门禁。
  - 验收: `docs-consistency.yml` (含 `.python/generate_markdown.py` + `git diff --exit-code`), `scripts/verify_remote_build_protocol_docs.py` 101/101, Python 回归全绿, `:app:verifyApkBuilderRuntimeKit` 通过, 插件 app / API 单测与 Release Kotlin 编译通过。
  - 证据: 2026/09/01 在插件发布候选上重生成文档后差异为 0, 协议真源覆盖 73/73, Python 回归 25/25 (含 Release evidence 与候选隔离失败关闭用例); Gradle Runtime Kit 门禁, app/API 单测与 `:app:compileReleaseKotlin` 强制重跑 166/166 任务通过。PR #5 的 push/PR 文档门禁 ([33477042749](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/actions/runs/33477042749), [33477047543](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/actions/runs/33477047543)) 与签名配置门禁 ([33477047547](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/actions/runs/33477047547)) 全绿。
  - 正式路径复验: 2026/09/02 协议文档扩展为 101/101, Python 回归 25/25, 精确 Runtime Kit 的插件 API/app 单测、Release Kotlin、Runtime Kit 校验与 AndroidTest 编译通过；Sony G8441 上最终 arm64 Debug 设备类 45/45 通过。
- [ ] [P] **M7-3 版本定档**
  - 内容: 确定首发版本三元组 —— `PLUGIN_VERSION_NAME=1.0.0`, `PLUGIN_VERSION_BUILD`, `PLUGIN_RELEASE_SEQ`, 以及由 `hostVersionCode * 100 + 序号` 推出的 versionCode 与复合 versionName。确认配对宿主版本与兼容区间。
  - 验收: `version.properties` 与生成的 APK 元数据一致; versionCode 相对既有装机 (5201) 保持单调递增; 复合 versionName 与双版本文件名符合 `docs/versioning.md`。
  - 首轮候选: 已将 AutoJs6 源提交精确固定为 `71e684b8dc1a59783293e0ad282638e3a88e37b6`, 配对 `6.8.0 / 5277`, 精确兼容区间 `5277..5277`, 首发三元组 `1.0.0 / build 1 / seq 1`, 推导 Android versionCode `527701` 与复合 versionName `1.0.0+autojs6-6.8.0`; 私有 Actions artifact 端点完成来源绑定与签名构建。该源候选因 M7-6 装机阻断被拒绝, 所以本条保持未完成; 替代候选必须修复非实验性打包路径并重新固定新的精确源 SHA。
  - 替代候选: 保持 `6.8.0 / 5277` 与首发插件三元组不变；包含正式插件托管构建能力的新 AutoJs6 精确 SHA 已固定为 `18e6b28b469ec8192a129945cebf87b090590425`。五套本地 Runtime Kit 均绑定该 SHA，正式协议为 3；维护者确认该 SHA 前不得运行候选专用受信工作流，且本阶段不创建公开 Release。
- [ ] [P] **M7-4 受信流水线产出五 ABI 签名资产**
  - 内容: 用 `build-from-runtime-kit.yml` 的 `workflow_dispatch` (输入 AutoJs6 tag) 从确定提交产出 universal + 四个 ABI 的**正式签名** APK。本地 unsigned / debug 产物不计。
  - 验收: 五个资产齐全, `SIGNING_CERT_SHA256` 证书指纹校验通过, 逐个记录文件名, 大小, SHA-256, 签名证书摘要, 插件版本, 宿主范围, Runtime Kit ID 与协议版本。
  - 发布前接线: `scripts/create_release_evidence.py` 将上述字段绑定为机器可读 JSON, 同时保留为 Actions artifact 并随五个 APK 上传到同名 Release; 三类失败关闭回归覆盖五变体完整性, CRC32 文件名与版本/Runtime Kit 身份一致性。
  - 本地发布预演: 2026/09/01 从宿主 `71e684b8d` 生成 AutoJs6 `6.8.0 / 5277` 的五套 Runtime Kit, 在隔离工作树实际构建五个 `527701 / 1.0.0+autojs6-6.8.0` Release APK; Runtime Kit 集合校验, APK 资产校验, `aapt` 版本复核, CRC32 文件名和五资产 evidence JSON 生成均通过。该轮为未签名结构预演, 不代替本条所需的受信签名证据。
  - 隔离签名候选: 宿主候选运行 [33492382374](https://github.com/SuperMonster003/AutoJs6-Private/actions/runs/33492382374) 从精确源 `71e684b8d` 产出五套 Runtime Kit; 插件运行 [33493666040](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/actions/runs/33493666040) 以生产证书签名五个 APK, 并通过 `apksigner`, `aapt`, CRC32, 内嵌资产与 evidence 复核。两侧只保留 14 天 Actions artifact, 未创建 Release/tag, 未改 `version.properties` 或 `compat-matrix.json`。由于装机验收失败, 这些资产是**被拒绝的候选证据**, 不计作本条正式资产。
- [ ] [P] **M7-5 首条兼容矩阵记录**
  - 内容: 由流水线把首个条目写入 `compat-matrix.json` (当前 `entries` 为空), 含五个 ABI `artifacts` 与顶层 `apk*` 投影。
  - 验收: `scripts/update_compat_matrix.py resolve --abi <各架构>` 对配对 hostVersionCode 均能确定性解析到该版本, 缺失架构正确回退 universal; 旧 tag 通道并存不受影响。
  - 本地矩阵预演: 同一候选的五个 APK 已成功合并为单个 `v1.0.0 / 527701` 条目, `universal`, `arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86` 均解析到对应资产; 权威矩阵仍保持为空, 等待 M7-4 的正式 Release URL 与签名资产后由流水线原子写入。
- [ ] [H+P] **M7-6 装机验收**
  - 内容: 在真实设备 / AVD 上走完 `docs/e2e-release-drill.md` 的核心场景: 插件中心按矩阵解析 → 下载匹配版本 → 安装 → 打包应用 → 安装产物并冷启动。
  - 验收: 场景 1 (匹配版本安装) 与场景 7 (兼容区间行为) 通过; 签名门禁对官方资产放行；普通“打包应用”经
    `supportsApkBuild` 正式路径完成产物安装与冷启动；旧实验开关保持关闭且不影响普通打包。
  - 首轮候选结果: **失败 / No-Go**。Sony G8441 (Android 9 / API 28 / arm64-v8a) 全新安装精确源宿主与官方签名 arm64 候选后, 插件中心正确识别并启用 `1.0.0+autojs6-6.8.0 / 527701 / inrt-arm64-v8a`, 但打包入口在传输模板前报告插件不可用与远程总开关关闭。源码追踪确认 `TemplateApkResolver` 只解析远程构建候选, `BuildActivity` 只调用 `RemoteApkBuildClient`; 在 `supportsRemoteBuild=false` 与宿主开关默认关闭时无普通打包路径。未生成可安装输出, 因此安装/冷启动阶段不能继续。证据级别 T1, 详见 `docs/v1.0.0-rc1-candidate-audit-2026-09-01.md`。
  - 替代候选本地结果: **Debug 路径通过，正式签名候选仍待执行**。同一 G8441 上，宿主 `18e6b28b4` 与精确 arm64 Runtime Kit 插件在旧实验偏好键缺失（默认 `false`）时，从普通“Build APK”入口完成插件私有进程构建、签名、宿主发布复核、独立签名校验、安装与冷启动；最终 UI/logcat 命中 `PLUGIN BUILD SMOKE OK`，设备类 45/45。该证据不含生产签名、矩阵下载或受信候选工作流，故本条保持未完成。详见 `docs/v1.0.0-plugin-managed-local-smoke-2026-09-02.md`。
- [x] [P] **M7-7 发行文案定稿**
  - 内容: 10 语言 CHANGELOG 补记 v1.0.0, 写明精确宿主 / 插件 / Runtime Kit 版本与“构建核心完全由设备内插件承担”；README 与插件说明明确不上传项目、不再存在宿主内第二构建器，并把“远程构建”解释为旧兼容名称。
  - 验收: 生成器校验通过, `docs-consistency.yml` 绿灯, 10 语言键序与占位符断言无残留。
  - 证据: 2026/09/02 十语言 CHANGELOG、README 与 11 套插件说明均已迁移到唯一 `on-device-plugin` 正式路径；明确项目不上传、宿主不存在第二构建器、旧 remote 名称只作兼容保留。生成器、JSON 解析、占位符与协议文档 101/101 校验通过。证据级别 T3。

## M8 —— GA 后独立保证与兼容层收敛

> 主题: 在唯一正式插件托管路径稳定发布后继续提高独立保证、可观测性与协议整洁度。**长期目标, 不阻塞 M7 首发。**
> 旧 `docs/remote-build-rollout.md`、回退 ADR 和证据索引作为历史审计保留；其中要求恢复宿主第二构建器的 R2/R3 条件已被
> 2026/09/02 架构 ADR 取代，不再是产品方向。

- [ ] [H+P] **M8-1 独立安全审查**
  - 内容: 按既有独立审查范围增加正式能力键、密钥库接口、宿主准入与输出复核，固定双仓库精确源码快照后交由独立审查者。
  - 验收: Critical/High 未解决为 0，决定栏引用同一内容寻址 manifest；发现项进入公开可追踪修复流程。
- [ ] [H+P] **M8-2 完整预览周期与支持数据**
  - 内容: 对唯一插件路径观察至少一个完整宿主预览周期，按 ABI/API/项目类型收集脱敏终态、Binder 失联、取消、超时和输出拒绝分类。
  - 验收: 没有损坏、错签、数据泄露或不可恢复升级链问题；所有新增失败都有可行动分类和回归用例。
- [ ] [H+P] **M8-3 旧实验兼容层弃用计划**
  - 内容: 盘点仍使用 `supportsRemoteBuild`、`REMOTE_BUILD_VERSION` 与旧开发者开关的宿主版本，制定不少于两个稳定版本的弃用窗口。
  - 验收: 删除前有使用证据、迁移说明和兼容测试；不得重排既有 AIDL 事务或 Parcelable 字段。
- [ ] [H+P] **M8-4 将来真正的离设备构建另立 ADR**
  - 内容: 如未来需要云端/局域网构建，使用全新能力键、认证、隐私、传输和威胁模型，不复用 `on-device-plugin` 或旧 remote 语义。
  - 验收: 明确用户授权、源码数据边界、端到端加密、服务身份、失败回收与本地插件路径关系后，才允许进入实现。
