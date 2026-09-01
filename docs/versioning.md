# 版本机制决策记录 (ADR): 插件独立版本与宿主配对

- 状态: 已采纳 (2026/08/30)
- 关联: `ROADMAP.md` 里程碑 M6
- 范围: 本仓库 (插件侧) 的版本字段, 构建接线, 发布流水线与兼容矩阵; 宿主侧解析 (M6-4) 已在 AutoJs6 主仓库插件中心实现

## 背景与问题

插件最初为了与宿主强绑定, 直接复用宿主版本号: CI 中 `scripts/sync_version_from_runtime_kit.py` 以 Runtime Kit 的
`host.versionName` / `host.versionCode` 覆写 `version.properties`, Android versionName / versionCode 与宿主完全一致。
由此产生三个问题:

1. **同一宿主版本内无法发布修复版**: versionCode 不变, Android 不识别为更新。
2. **版本号无法表达插件自身成熟度**: 插件没有自己的 v1.0.0 / 构建号语言。
3. **"下载最新版" 语义错误**: 宿主内部 API 逐版本变化, 旧版宿主需要的是 "配套旧插件", 甚至是降级, 而非最新版。

参考范式: JetBrains Marketplace (插件自身版本 + since/until 兼容区间 + 市场端解析) 与 KSP (`<宿主版>-<自身版>` 复合命名)。

核心转变: **版本号只承载识别, 兼容契约完全交给配对元数据与分发端解析。**

## 决策总览: 三层模型

| 层 | 承载物 | 职责 |
|---|---|---|
| ① 插件自身版本 | `PLUGIN_VERSION_NAME` (SemVer) + `PLUGIN_VERSION_BUILD` (自增构建号) | 表达插件自身演进与成熟度 |
| ② 配对元数据 | `BUILT_FOR_HOST_*`, 显式兼容区间, `REQUIRES_HOST_VERSION`, `runtimeApiLevel`, `hostVersionName/Code` 等能力与协议字段 | 唯一兼容契约; 默认精确匹配, 经显式授权才可放宽补丁区间 |
| ③ 分发端解析 | `compat-matrix.json` (仓库根, 随发布由 CI 追加) | 让任意宿主版本确定性解析到配套插件构建 |

## 决策细节

### D1 —— 插件自身版本线: 从 v1.0.0 起步

- `PLUGIN_VERSION_NAME` 采用 SemVer, 起始 `1.0.0`。理由: 核心能力 (模板分发 + 兼容检查 + 自动发布) 已随两个宿主版本
  (v6.7.1 Alpha4, v6.8.0 Alpha5) 稳定发布并被宿主正式集成, 不属于 0.x 探索期。
- `PLUGIN_VERSION_BUILD` 为全局自增构建号, 首个新机制发布为 `1`。由同步脚本在每次发布时自动 +1, 无需人工维护;
  `version.properties` 中保存的是 "最近一次已发布" 的值 (`0` 表示新机制下尚未发布)。
- `PLUGIN_VERSION_NAME` 由维护者在语义变化时手动调整 (修复 → patch, 新能力 → minor, 破坏性调整 → major),
  同步脚本与 CI 永不覆写该字段。

### D2 —— Android versionCode: `hostVersionCode × 100 + 序号` (方案 B)

| 方案 | 形态 | 优点 | 缺点 |
|---|---|---|---|
| A: 纯自增 + 大偏移 | `1000000 + PLUGIN_VERSION_BUILD` | 与宿主完全解耦 | 一次性跳变巨大; 从 code 无法读出配对宿主; 与既有装机 (5201) 的连续性依赖偏移选取 |
| **B: 宿主前缀 + 序号 (采纳)** | `hostVersionCode × 100 + 序号` | 已装机 5201 < 520100, 升级连续性天然成立; 从 code 即可读出配对宿主; 同宿主内修复版单调递增 | 序号上限 99; 跨宿主补发 (backport) 需要序号追踪 |

- 序号 `PLUGIN_RELEASE_SEQ` 取值 1..99, 表示 "同一配对宿主版本下的第几个插件构建"。
- 序号来源以 `compat-matrix.json` 为权威: 同步脚本取矩阵中同 `hostVersionCode` 条目的最大序号与
  `version.properties` 快照 (仅当快照宿主与本次相同时参与比较) 二者的较大者, 再 +1。
  这样即使发生跨宿主补发 (先发 6.8.0 → 再发 6.8.1 → 又给 6.8.0 补一个修复版), 序号也不会回退或撞车。
- 溢出余量: Android versionCode 上限 2147483647, 宿主 versionCode 需超过约 2147 万才会溢出 (迁移决策时为 5201, 余量充足)。
- 旧机制发布 (versionCode 3923, 5201) 不在矩阵中; 对应宿主的首个新机制构建从序号 1 (如 520101) 开始, 天然大于 5201。

### D3 —— versionName 复合命名与产物文件名

- Android versionName 采用 SemVer build metadata 形式: `<插件版本>+autojs6-<宿主版本 slug>`,
  如 `1.0.0+autojs6-6.8.0-alpha5` (slug 规则与产物文件名一致: 空白转 `-` 后转小写)。
- `PluginInfo.versionName` / `ApkBuilderTemplateInfo.versionName` 上报该复合形式, 用户在宿主界面一眼可读出配对宿主。
- APK 文件名同时携带两个版本和 ABI 变体 (`+` 不适合出现在发布资产名中, 以 `-` 连接):
  `autojs6-apk-builder-template-v<插件版本>-autojs6-v<宿主 slug>-<variant>-<crc32>.apk`,
  如 `autojs6-apk-builder-template-v1.0.0-autojs6-v6.8.0-alpha5-arm64-v8a-1a2b3c4d.apk`。
  `<variant>` 为 `universal`, `arm64-v8a`, `armeabi-v7a`, `x86_64` 或 `x86`; 五个替代资产共享同一
  versionName / versionCode。详细契约见 `docs/abi-variants.md`。

### D4 —— 保留配对身份, 显式声明兼容区间

- **配对身份不变**: `BUILT_FOR_HOST_VERSION_NAME/CODE`, `runtimeApiLevel`, 各 hash 能力字段, 以及
  `ApkBuilderTemplateInfo.hostVersionName/hostVersionCode/hostPackageName` 仍描述 Runtime Kit 实际由哪个宿主构建。
- **兼容契约扩展**: `ApkBuilderTemplateCapabilityKeys` 新增
  `COMPATIBILITY_MIN_HOST_VERSION_CODE`, `COMPATIBILITY_MAX_HOST_VERSION_CODE` 与
  `ALLOW_PATCH_VERSION_MISMATCH`; 通用 `REQUIRES_HOST_VERSION` 继续投影区间下界, 供旧宿主执行最低版本保护。
  缺少新字段的旧插件按 `BUILT_FOR_HOST_VERSION_CODE` 的单点区间处理, 不会被意外放宽。
- **切换**: `PluginInfo.versionName/versionCode` 与 `ApkBuilderTemplateInfo.versionName/versionCode` 从 "宿主版本"
  切换为 "插件自身 (复合) 版本", 即 APK 自身的 versionName / versionCode。
- **新增能力字段** (`ApkBuilderTemplateCapabilityKeys`): `pluginVersionName` (纯净 SemVer), `pluginVersionCode`,
  `pluginVersionBuild`, 供宿主无需解析复合字符串即可取用。
- ABI 变体继续使用既有 `PluginInfo.variant`, 并新增 `apkBuilderTemplateSupportedAbis` 能力字段; 前者标识
  `inrt-<variant>`, 后者与 `PluginInfo.supportedAbis` 一致, 不参与宿主版本兼容判断。

### D5 —— 分发解析: `compat-matrix.json` + 双通道

- 矩阵位于仓库根 `compat-matrix.json`, 由发布工作流在上传 APK 后追加条目并随版本持久化提交, 固定可取址:
  `https://raw.githubusercontent.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/master/compat-matrix.json`。
- 条目字段: `pluginVersionName`, `pluginVersionCode`, `pluginVersionBuild`, `hostVersionName`, `hostVersionCode`,
  `minHostVersionCode`, `maxHostVersionCode`, `allowPatchVersionMismatch`, `runtimeApiLevel`, `tag`, `artifacts`,
  `releasedAt`。`artifacts` 为五个 ABI 资产的数组; 顶层 `apkName` / `apkUrl` / `apkSha256` / `apkSizeBytes`
  保留为 universal 投影, 兼容旧消费者。
- 解析规则 (宿主插件中心, M6-4): 以宿主自身 versionCode `H` 过滤 `minHostVersionCode <= H <= maxHostVersionCode`
  的条目; 精确区间始终可用, `min < max` 的条目还必须显式携带 `allowPatchVersionMismatch: true`, 否则按无效条目拒绝。
  通过过滤后取 `pluginVersionCode` 最大者, 再按设备 ABI 优先取精确 artifact, 缺失时回退 universal。
  `scripts/update_compat_matrix.py resolve --host-version-code H --abi <abi>` 提供同规则的参考实现与离线自测。
- **双通道过渡**: APK 继续上传到与宿主 tag 同名的 Release (旧宿主按 tag 查找的通道不变); 矩阵是叠加的新通道,
  两者互不干扰。矩阵为空或未命中时, 分发端应回退到 tag 通道。
- 宿主实现 (2026/08/30): AutoJs6 插件中心只对官方 APK Builder 条目叠加矩阵结果, 将解析后的单个配套 Release
  写入既有索引缓存; URL 必须指向本仓库官方 GitHub Release, 且 APK 名称、SHA-256 与大小均需有效。矩阵请求、
  schema、区间或 ABI 资产任一环节不可用时, 不影响官方索引刷新, 直接保留既有 Release/tag 结果。

### D6 —— 补丁区间: 显式启用且失败关闭

- AutoJs6 的 `generateRuntimeKit` 默认生成 `min = max = host.versionCode` 且
  `allowPatchVersionMismatch = false`; 普通 tag 发布因此继续保持精确匹配。
- 只有完成相邻补丁验证后, 维护者才可通过 `release-runtime-kit.yml` 的手动输入或等价 Gradle 属性
  `autojs.runtimeKit.compatibility.minHostVersionCode`, `maxHostVersionCode` 与 `allowPatchVersionMismatch=true`
  生成闭区间。构建任务强制校验正数、`min <= builtFor <= max` 以及“放宽区间必须显式授权”。
- Runtime Kit 的 Python / Gradle 双校验器、矩阵写入器与宿主矩阵解析器执行同一组门禁。宿主和插件共享
  `ApkBuilderTemplateCompatibilityPolicy`: 精确 `builtFor` 为 `EXACT`; 显式区间内的非精确宿主为
  `PATCH_COMPATIBLE` 并显示本地化警告; 低于下界、高于上界、非法区间或未显式授权的区间均为 `BLOCKED`。
- `allowRiskyBuild` 不能绕过区间边界。补丁兼容也不由 versionName 或 SemVer 猜测, 只相信经过校验的 versionCode 契约。

### D7 —— 降级路径

Android 不允许降级覆盖安装 (`INSTALL_FAILED_VERSION_DOWNGRADE`)。当解析出的配套插件 versionCode 低于已装插件时
(用户主动回退宿主版本的场景), 宿主侧需给出 "卸载后重装" 引导 (M6-4)。方案 B 保证该场景只发生在 "宿主降级" 时,
同宿主内的修复版永远是正向升级。

AutoJs6 插件中心现将该目标显示为 "需要重新安装" 而非普通更新, 并在进入系统卸载前展示已安装/配套版本与
插件应用数据删除警告。卸载完成返回插件中心后, 未安装条目的安装按钮继续使用同一矩阵解析资产。

## `version.properties` 字段职责

| 字段 | 含义 | 写入方 |
|---|---|---|
| `VERSION_NAME` / `VERSION_BUILD` | 配对宿主的 versionName / versionCode (沿用旧字段名以保持 build-logic 兼容) | 同步脚本 (来自 Runtime Kit) |
| `PLUGIN_VERSION_NAME` | 插件自身 SemVer | 维护者手动 |
| `PLUGIN_VERSION_BUILD` | 最近一次已发布的全局构建号 (0 = 新机制下尚未发布) | 同步脚本自动 +1 |
| `PLUGIN_RELEASE_SEQ` | 最近一次已发布构建在其配对宿主内的序号 (0 = 该宿主尚无新机制发布) | 同步脚本自动推进 |

本地开发构建直接使用仓库快照值 (序号可能为 0 或上次发布值), 不影响正确性; CI 发布前总是先运行同步脚本推进字段。

## 发布时序 (`.github/workflows/build-from-runtime-kit.yml`)

1. 下载 universal + 四个单 ABI Runtime Kit; 分别校验内容, 再校验五变体集合齐全且宿主 / API 契约一致。
2. 只以 universal Kit 运行一次 `sync_version_from_runtime_kit.py`: 同步宿主配对字段 + 推进
   `PLUGIN_VERSION_BUILD` / `PLUGIN_RELEASE_SEQ` (不触碰 `PLUGIN_VERSION_NAME`)。
3. 依次构建五个相同 Android 版本的替代 APK, 分别签名、校验证书指纹并以 CRC32 命名。
4. 将五个 APK 一起上传到宿主 tag 同名 Release。
5. 持久化: 重置到远程分支 → 重跑一次同步脚本 (同一输入, 结果确定) → 对五个资产执行
   `update_compat_matrix.py add` 并合并为同一矩阵条目的 `artifacts` → 一并提交 `version.properties` 与
   `compat-matrix.json`。

工作流通过 `concurrency` 组串行执行, 避免并发发布竞争序号。

## 迁移与兼容性影响

- 已装旧机制插件 (versionCode 5201) 的用户: 同宿主的首个新机制构建为 520101 > 5201, 正常覆盖升级。
- 旧版宿主: 继续按 tag 通道查找, 完全无感; `BUILT_FOR_*` / `host*` 协议字段语义未变, `openTemplate` 检查行为不变。
- 新版宿主遇到显式补丁区间时: 精确 `builtFor` 无警告; 区间内其他宿主允许打包但明确警告并要求验证产物;
  区间外在模板传输前阻断。旧插件缺少区间字段时仍只允许精确 `builtFor`。
- 旧 Runtime Kit 同时缺少 `template.variant` / `template.supportedAbis` 时仍按 universal 接受; 新 Kit 必须显式声明,
  且声明必须与模板 APK 内实际原生库集合一致。
- 首个新机制发布时, CHANGELOG 开始以插件自身版本 (如 `v1.0.0`) 记录条目。

## 已知边界

- 对同一宿主 tag 重跑工作流会产生新的序号与矩阵条目 (视为一次新发布); 旧 CRC 命名的资产会残留在 Release 中,
  与既有行为一致, 矩阵解析始终指向最新条目。
- 代码路径、校验器、发布输入与离线区间测试已经打通, 但权威矩阵当前仍为空或为 `min = max` 的精确条目。
  只有相邻补丁宿主经过真实打包产物验证后, 才能发布首个 `min < max` 条目; 在此之前 M4-2/M6-6 的发布验收保持未完成。

## 否决的备选方案

1. **沿用宿主版本号 (现状)**: 无法在同宿主内发布修复版, 版本号无自身语义。否决。
2. **方案 A (纯自增 + 大偏移)**: versionCode 与配对宿主完全无关, 排障与矩阵核对需额外查表; 偏移选取一旦失误
   将破坏升级连续性且不可回退。否决。
3. **versionName 纯净 SemVer (不带配对信息)**: 用户在宿主界面与文件管理器中无法辨认配套宿主, 手动安装场景极易装错。
   否决, 配对信息进入 versionName build metadata 与文件名。
