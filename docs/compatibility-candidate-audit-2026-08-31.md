# 5275 → 5276 补丁区间候选审计

日期: 2026-08-31

状态: **内部真实设备验收通过; M4-2 已具备收口证据; 因端点尚未正式发布, 不具备正式矩阵发布资格。**

## 结论

仓库历史中的 AutoJs6 `6.8.0 / 5275` 与 `6.8.0 / 5276` 已完成 M4-2 的内部真实设备验收:

- 两端的 APK Builder 协议、远程构建协议、既有 Runtime API 摘要与 native 清单一致。
- `5276` 的脚本引擎与资源摘要发生变化, 但源码审计将变化限定为新增 Lua Runtime、Python 传输补充及其 manifest/文案; 既有 APK Builder API、Common Plugin API、JS declarations/modules/runtime API 没有改变。
- 以 `5275` Runtime Kit 为基线、显式声明 `5275..5276 + allowPatchVersionMismatch=true` 的同一个插件候选, 在 H0=5275 上不显示兼容警告并完成打包—安装—启动, 在宿主原位升级到 H1=5276 后显示可继续的补丁兼容警告并再次完成打包—升级安装—启动。
- H2=5277 在 UI 中被硬阻止且没有继续入口; 聚焦 instrumented test 进一步证明 `allowRiskyBuild=true` 也不能越过声明区间。
- 测试设备上的宿主、插件、烟测应用与脚本均已清理, 临时源码构建副本已移入 Windows 回收站; 工作区外证据保留完整。

这使 M4-2 的代码、文案与设备闭环均已完成, 但该候选仍**不能**写入 `compat-matrix.json`, M6-6 继续保持未勾选:

1. H0/H1 是没有对应 tag/Release 资产的历史提交端点; 2026-08-31 的 GitHub Release 只读复核也没有发现 `5275`/`5276` 端点。
2. 本轮插件是以 Android 调试证书签名、显式开启远程构建的内部单一 universal 候选, 不是受信任发布流水线生成的 universal + 四 ABI 正式资产。
3. M6-6 的验收明确要求至少一个 `min < max` 区间已经正式发布并进入权威矩阵; 设备预检不能替代这一步。

权威矩阵因此继续保持无虚构条目。未来正式 `v1.0.0` 发布仍须遵守 M3 的独立放量门禁: R0 下官方构建保持 `supportsRemoteBuild=false`; 只有另行满足 R1 进入条件并形成 Go 结论后, 才能在候选/正式发布中显式开启远程构建。本轮 `true` 只用于验证区间判定与远程会话路径。

## 候选选择

| 角色 | AutoJs6 版本 | versionCode | 提交 | 选择结论 |
|---|---:|---:|---|---|
| H0 | 6.8.0 | 5275 | `2caddcb763b39f0bf450909742fa6ec4caba27a8` | 内部候选基线, built-for 端 |
| H1 | 6.8.0 | 5276 | `b39872e2f1ccc940afcb74a6b95b5458e2fee594` | 内部候选上界, 补丁警告端 |
| H2 | 6.8.0 | 5277 | `fb32134a61a223a0bf7a968ff56c63fb34b361a1` | 范围外失败关闭端 |

另外两条历史边界被否决:

- `6.8.0 Alpha5 / 5201` → `6.8.0 / 5275`: 跨越 176 个提交、约 1941 个文件, 包含大范围 APK Builder 与运行时重构, 不属于可审计的补丁级放宽。
- `5276` → `5277`: 直接改动 APK Builder TypeScript 请求键、模板协议与 staging cipher, 不能沿用本候选的“既有构建协议未漂移”结论。

## Runtime Kit 契约比较

两端均从对应历史源码真实运行 `:app:generateRuntimeKit` 产生, 而非手工伪造模板。

| 契约项 | H0 / 5275 | H1 / 5276 | 判断 |
|---|---|---|---|
| APK Builder protocol | `2` | `2` | 一致 |
| Remote build protocol | `2` | `2` | 一致 |
| Runtime API level | `5275` | `5276` | 随宿主构建号变化 |
| Runtime API hash | `0bcee7bb…a3a99a` | `0bcee7bb…a3a99a` | 一致 |
| Script engine hash | `5f0f0222…e8ef7a` | `cda6acb0…f1e41` | 不一致, 已经源码与设备审计 |
| Resources contract hash | `cf0f4958…3cd4b` | `0c8997ec…65440` | 不一致, 已经源码与设备审计 |
| Native library manifest hash | `e3b0c442…b855` | `e3b0c442…b855` | 一致, 两者均为空清单 |
| Template size | 31,531,851 B | 31,640,087 B | H1 增加 108,236 B |
| Template SHA-256 | `955011a2…750a7` | `43393cc9…49200` | 不同构建, 符合预期 |

`runtimeApiHash` 覆盖 `app/src/main/assets/declarations`, `app/src/main/assets/modules`, `runtime/api`, `runtime/api/augment`, `plugin-api/apk-builder-template` 与 `plugin-api/common-plugin-api`; 两端摘要相同。源码 diff 另行确认这些路径没有变化。H1 新增的 `plugin-api/lua-runtime-api` 不在既有 APK Builder/JS 公共 API 合约内。

`scriptEngineHash` 会纳入所有 DEX, `resourcesContractHash` 会纳入 manifest、`resources.arsc` 与全部 `res/` 条目, 因而新增独立 Lua/Python 能力也会令其变化。源码审计确认共用 `ScriptEngineService` 只增加 Lua 异常文案分派, JS 引擎注册、JS 打包协议和 APK Builder Binder API 未改变。随后 H0/H1 的相同 JS 设备闭环为该判断补上了运行时证据。

精确模板差量报告结果:

- ZIP 条目: 3666 个内容不变, 173 个变化, 1 个新增, 0 个删除。
- 可回放 Git forward binary patch 为目标模板的 88.685%, 回放 SHA-256 通过。
- 完整 JSON 报告保存在工作区外审计目录的 `5275-to-5276-delta.json`。

## 可复现构建与校验

H0/H1 Runtime Kit 使用对应提交分别执行:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit `
  -x :app:lintVitalAnalyzeInrtRelease --no-daemon
```

随后由插件仓库执行:

```powershell
python scripts/verify_runtime_kit.py <runtime-kit-dir>
python scripts/analyze_runtime_kit_delta.py <h0-kit-dir> <h1-kit-dir> `
  --old-label "Host 6.8.0+5275" `
  --new-label "Host 6.8.0+5276" `
  --json-out <evidence-dir>/5275-to-5276-delta.json
```

候选 Runtime Kit 是 H0 真实产物的隔离副本, 只把 `runtime-kit.json#compatibility` 改为:

```json
{
  "minHostVersionCode": 5275,
  "maxHostVersionCode": 5276,
  "allowPatchVersionMismatch": true
}
```

插件在隔离源码副本中先运行 `sync_version_from_runtime_kit.py`, 再以
`-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true` 组装候选。最终用于设备验收的 APK 为非 debuggable release 变体, 经 zipalign 后使用标准 Android 调试证书本地签名。静态生成的 `BuildConfig.java` 与设备能力发现共同确认 `ENABLE_REMOTE_BUILD=true`。

候选元数据:

- plugin version: `1.0.0`, build `1`, release sequence `1`
- Android versionCode: `527501`
- Android versionName: `1.0.0+autojs6-6.8.0`
- built-for host: `5275`
- compatibility: `5275..5276`, `allowPatchVersionMismatch=true`
- embedded template SHA-256: `955011a2985d4b0f6ebef28ec346f01db2c01ff84eb56c26a6160dcc673750a7`
- candidate APK SHA-256: `9d9435bd8d0ba94e5d3d96ef030440eb0444eeed0c90f6f6f4b99b79c604ec92`

构建预检还捕获了两类不应混入正式证据的产物:

1. 最初的 `...-universal.apk` 沿用了仓库默认值, `ENABLE_REMOTE_BUILD=false`; 它只能作为“R0 默认值确实生效”的负向预检, 已标记 superseded。
2. 修正后的 remote-enabled debug APK 在 Sony G8441 / Android 9 上于 OEM 原生 `ADB-JDWP Connec` 线程崩溃, 发生在插件业务代码之前; 改用非 debuggable release 变体后不再触发。该 debug APK也只保留为诊断证据。

H0/H1 宿主均实际执行 `:app:assembleAppDebug`。两包 manifest 分别复核为 `versionCode=5275` 与 `5276`, 并与候选插件使用同一标准 Android 调试证书:

```text
C=US, O=Android, CN=Android Debug
SHA-256: 2e64822e13a6c80c12e1c4b47e8fb32d1e9334526289da75777b7a79145de4b8
```

H2 使用当前 5277 x86_64 debug APK 的本地重签副本, 只安装到临时 AVD; 已存在于另一 AVD 上、由正式证书签名的 5277 安装包只做了只读拉取与摘要保存, 未覆盖、卸载或修改。

所有本地证书与 APK 仅用于内部设备预检, 不能冒充正式签名产物。

## 真实设备结果

### H0: 精确 built-for 端

- 设备: Sony G8441, Android 9 / API 28, arm64-v8a。
- 初始状态: 本轮宿主、候选插件与烟测应用均未安装。
- 安装 H0=5275 和最终候选, 在 Plugin Center 中启用并授权插件。
- 构建入口没有出现版本兼容警告; 同一 JS 源成功生成 APK。
- 生成包: `org.autojs.autojs6.app_0_m42compatsmoke`, versionCode `1`, 设备 Package Manager 解析的 versionName `1.0.0`。
- 生成 APK: 29,581,958 B, SHA-256 `1b684d0d512841278e5e7b48983d0c060b7533c63d8ef7450acfb09c5e4fa66e`。
- 直接安装该原始输出后正常启动, UI 显示 `M4-2 compatibility smoke OK`。

宿主内置安装器第一次尝试发生在系统“允许未知来源”切换前建立的旧安装会话中, 返回 `Base APK has no verifiable signer`; 对完全相同的 APK 执行 `adb install` 成功, `apksigner` 也确认 V2 签名有效。因此该现象归为宿主安装会话复用边缘情况, 不是生成 APK 的签名无效。桌面 `aapt` 对资源引用显示了模板默认 versionName, 但设备安装后的 Package Manager 明确报告 `1.0.0`; 设备解析值作为最终依据。

### H1: 区间上界补丁端

- 在同一实体机上使用相同证书把宿主原位升级到 H1=5276, 不重装候选插件。
- 打开同一构建入口后显示补丁不匹配警告, 文案列出插件 `1.0.0+autojs6-6.8.0 / 527501` 与宿主 `6.8.0 / 5276`, 同时提供继续与取消。
- 不启用 `allowRiskyBuild`, 直接选择继续后构建成功。
- 生成包仍为 `org.autojs.autojs6.app_0_m42compatsmoke`, versionCode `2`, versionName `1.0.1`。
- 生成 APK: 29,586,054 B, SHA-256 `0a34e94b5982c382013a49ce588e1fffe89e4d49d0fcfb6a184518f869639d76`。
- 以升级方式安装并启动成功, UI 再次显示 `M4-2 compatibility smoke OK`。
- H0/H1 两个生成 APK 使用同一 V2 签名证书, SHA-256 `c4530b8ecba64b85e245670680e5e7e84dd14b6c0f63d12717fe01415593122e`。

### H2: 范围外失败关闭端

- 设备: API 36 / Android 16 / x86_64 AVD。
- 安装 H2=5277 本地重签 debug 包与同一候选插件, 启用并授权插件。
- 构建入口在模板传输/打包前显示硬阻止对话框: 插件只支持 `5275-5276`, 当前宿主为 `5277`; 按钮只有 `PLUGIN CENTER` 与 `OK`, 没有继续或 risky 入口。
- 设备上没有生成烟测 APK。
- 将 `RemoteApkBuildSessionInstrumentedTest.directorySourceOutsideDeclaredRangeFailsEvenWhenRiskyBuildIsAllowed` 修正为从 capability 读取声明上界并请求 `max + 1`, 显式设置 `allowRiskyBuild=true`; API 36 AVD 聚焦运行 1/1 passed, 0 failures, 0 errors。断言结果为 `STATUS_FAILED`、`LEVEL_BLOCK`, error 包含声明区间之外, 且没有输出 APK。

## 关键证据索引

证据位于工作区外的
`D:\idea-projects\.a6-compat-audit-artifacts\candidate-5275-5276\`。机器可读索引为 `candidate-manifest.json`; 以下路径均相对于该目录。

| 证据 | 相对路径 | SHA-256 |
|---|---|---|
| 最终插件候选 | `plugin-rc/autojs6-apk-builder-template-v1.0.0-autojs6-v6.8.0-remote-enabled-release-local-debug-signed.apk` | `9d9435bd8d0ba94e5d3d96ef030440eb0444eeed0c90f6f6f4b99b79c604ec92` |
| H0 宿主 | `host-h0-5275/autojs6-v6.8.0-5275-universal-debug.apk` | `588ca0ced77e83a61743d21edd10ab84069b94d0f33ca048804b2a59fbe47fc0` |
| H1 宿主 | `host-h1-5276/autojs6-v6.8.0-5276-universal-debug.apk` | `938c73689e4e04d9fa41c0b3b961ecd203752a9f1107cec365fdea82a7939338` |
| H2 本地重签宿主 | `host-h2-5277/autojs6-v6.8.0-5277-x86_64-debug-local-resigned.apk` | `d92b36baf207503019142488ad51c1e6776100b6c516e6da901e1d212905134c` |
| H0 输出 APK | `device-run-2026-08-31/h0-generated-0_M42CompatSmoke_v1.0.0.apk` | `1b684d0d512841278e5e7b48983d0c060b7533c63d8ef7450acfb09c5e4fa66e` |
| H1 输出 APK | `device-run-2026-08-31/h1-generated-0_M42CompatSmoke_v1.0.1.apk` | `0a34e94b5982c382013a49ce588e1fffe89e4d49d0fcfb6a184518f869639d76` |
| H0 成功 UI | `device-run-2026-08-31/h0-generated-after-permission.png` | `b23c1a86b9471adc8f774dac98f09a3fdb0a10f19f1915179d8bb73dff110a2a` |
| H1 补丁警告 | `device-run-2026-08-31/h1-patch-warning.png` | `7bc763b8af3d82be9c6f8e5251615158f9858dd1176d6ad01ef4561350613851` |
| H1 成功 UI | `device-run-2026-08-31/h1-generated-launch.png` | `8af907016872b69be8d92765c0ffb6ce13d1e59999564e8a065925c98ddf8840` |
| H2 UI 硬阻止 | `device-run-2026-08-31/h2-hard-block.png` | `ffb91c8348a8c17e022436327928934c86fb38c0423e2c071749780475185051` |
| H2 对话框 XML | `device-run-2026-08-31/h2-hard-block.xml` | `6a7a7ba189639c7cb1694eeea52097119929d5300214cc6c8b59fb0649321324` |
| risky 聚焦测试日志 | `device-run-2026-08-31/h2-risky-instrumentation-gradle.log` | `f420639b93b8e66c43864fdc8f8350aecddde4e64b3e942ce4292ec3e0c7cf40` |
| risky JUnit 结果 | `device-run-2026-08-31/h2-risky-instrumentation-results/connected/debug/TEST-AVD_API_36.1(AVD) - 16-_app-.xml` | `0d87d32bdd8ca2c9dc627312e36bbc72ddb4589af5c7a9cf63f3ff08b40c1deb` |
| 候选 BuildConfig | `device-run-2026-08-31/candidate-plugin-release-BuildConfig.java` | `a799410f0fede56fad31722cd2b84f4a0faef4181b460ca43d40e0e195c5be24` |

H0/H1 Runtime Kit 归档仍分别为:

| 角色 | 文件 | 大小 | SHA-256 |
|---|---|---:|---|
| H0 Runtime Kit | `h0-5275/autojs6-runtime-kit-v6.8.0+5275.zip` | 28,591,987 B | `80ce422af6ed7e5deb9acd81d697635df9dbdb9cb92864fe40938a4a9ddafeb5` |
| H1 Runtime Kit | `h1-5276/autojs6-runtime-kit-v6.8.0+5276.zip` | 28,694,061 B | `a039e31d6baf575749f6f9b2fdd28cfbf09a30f0115dfe7c94372b68abdb2dcf` |

## 恢复与清理

Sony G8441 上已卸载本轮安装的 H1 宿主、候选插件与烟测应用, 并确认以下文件不存在:

- `/sdcard/Scripts/0_M42CompatSmoke.js`
- `/sdcard/Scripts/0_M42CompatSmoke_v1.0.0.apk`
- `/sdcard/Scripts/0_M42CompatSmoke_v1.0.1.apk`
- `/sdcard/0_M42CompatSmoke.js`

设备原有的其他 AutoJs6 插件和无关无障碍服务保持原状。API 36 AVD 上本轮宿主、候选/测试包与烟测脚本也已清除; 原有的其他插件保持原状。隔离源码临时构建目录经精确路径与父目录校验后移入 Windows 回收站, 外部证据目录未删除。

## 正式发布与回滚边界

内部设备演练通过仍不足以直接发布 `min < max` 矩阵条目。正式发布还需:

1. 选择具有正式 tag/Release 资产的相邻补丁 H0/H1 (或等待下一组可发布端点), 重新生成并审计五个 Runtime Kit。
2. 用受信任正式签名流水线生成 universal + 四个精确 ABI 插件资产; 不复用本轮调试证书产物。
3. 按当时的 M3 阶段执行对应路径: R0 官方插件继续 `supportsRemoteBuild=false`; 如要发布 remote-enabled 候选, 必须先另行满足 R1 的 G1/G5/G6 基本控制面并记录 Go 决策。
4. 对拟发布三元组复跑两端支持路径、H2 拒绝、ABI 选择和卸载/降级引导。
5. 发布插件 `v1.0.0` 后, 让流水线在 APK 上传成功后原子更新 `compat-matrix.json`; 不手工预写未来条目。
6. 验证矩阵按 H0/H1 均解析到同一最高 `pluginVersionCode`, ABI 精确资产优先且 universal 可回退。

若正式区间发布后发现回归, 不覆盖既有签名 APK: 立即通过单独矩阵修正提交撤回错误区间, 并发布更高插件构建号的精确匹配条目。宿主 tag 资产继续保留, 供旧通道与审计回溯。
