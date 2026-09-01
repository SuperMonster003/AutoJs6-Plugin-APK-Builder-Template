# ABI 模板变体设计与发布契约

- 状态: 已实现 (2026/08/30)
- 关联: `ROADMAP.md` M4-1, `docs/versioning.md` D3 / D5
- 范围: AutoJs6 宿主生成 Runtime Kit, 本仓库构建与发布插件 APK, 宿主安装选择及调用前保护

## 目标与边界

APK Builder Template 插件把完整 `template.apk` 放在自身 assets 中。模板包含多套 JNI 库时, 所有设备都会下载并安装
无关架构的二进制。M4-1 将同一份宿主契约发布成一套可互换的 ABI 变体, 同时保留 universal 作为兼容兜底。

本轮只拆分模板中实际存在原生库的四种 ABI。`armeabi` 没有任何对应 `.so`, 因此不发布一个名义存在、内容却为空的
变体。Java / Kotlin 字节码、资源、脚本引擎与配对宿主契约在五个变体间保持一致。

## 固定变体集合

| Runtime Kit `template.variant` | `template.supportedAbis` | 资产名后缀 | 用途 |
|---|---|---|---|
| `inrt-universal` | `arm64-v8a`, `armeabi-v7a`, `x86_64`, `x86` | `universal` | 旧客户端、无法判断 ABI 或精确变体缺失时兜底 |
| `inrt-arm64-v8a` | `arm64-v8a` | `arm64-v8a` | 64 位 ARM 设备 |
| `inrt-armeabi-v7a` | `armeabi-v7a` | `armeabi-v7a` | 32 位 ARM 设备 |
| `inrt-x86_64` | `x86_64` | `x86_64` | 64 位 x86 设备 / 模拟器 |
| `inrt-x86` | `x86` | `x86` | 32 位 x86 设备 / 模拟器 |

五个插件 APK 使用完全相同的 packageName、签名、versionName 与 versionCode; 它们是同一次发布的替代资产, 只有
文件名、Runtime Kit 元数据和内嵌模板的原生库集合不同。文件名形态为:

```text
autojs6-apk-builder-template-v<plugin>-autojs6-v<host>-<abi-or-universal>-<crc32>.apk
```

## Runtime Kit 契约

`runtime-kit.json` schemaVersion 继续为 `1`, 在 `template` 下增加两个可选字段:

```json
{
  "template": {
    "variant": "inrt-arm64-v8a",
    "supportedAbis": ["arm64-v8a"]
  }
}
```

兼容规则如下:

1. 新 Runtime Kit 必须显式声明两个字段。单 ABI 变体的名称、列表和 `template.apk` 内实际 `lib/<abi>/*.so`
   集合必须精确一致。
2. 新 universal 必须显式列出上述四种 ABI, 且内嵌模板也必须同时包含四种 ABI。
3. 旧 Runtime Kit 同时缺少两个字段时按 `inrt-universal` 处理, 以保证历史 tag 仍能本地构建; 只缺一个字段或出现
   未知 ABI 时直接拒绝。
4. `scripts/verify_runtime_kit_set.py` 要求一次发布的五个变体齐全、无重复, 并核对它们除
   `nativeLibManifestHash` 外拥有相同宿主 / API / 资源契约。

宿主 `generateRuntimeKit` 每次先清理自己的输出目录, 再从五个 INRT APK 生成五个目录和五个 ZIP, 避免上次构建残留
被误当作本次发布资产。

## 发布与矩阵

`.github/workflows/build-from-runtime-kit.yml` 对一组 Runtime Kit 执行以下原子流程:

1. 下载所有 Runtime Kit ZIP, 分别执行内容校验, 再执行完整集合校验。
2. 只以 universal Kit 推进一次插件构建号与宿主内发布序号。
3. 依次用五个 Kit 构建插件; 五个 APK 共享同一 Android 版本, 分别签名、核对证书指纹并追加 CRC32 文件名。
4. 将五个 APK 一起上传到宿主 tag 同名 Release。
5. 对同一个 `pluginVersionCode` 重复执行矩阵 `add`; 脚本把五项合并到一个条目的 `artifacts` 数组中。

`compat-matrix.json` 保持 schemaVersion 1。每个 artifact 记录 `variant`, `supportedAbis`, `apkName`, `apkUrl`,
`apkSha256` 与 `apkSizeBytes`; 条目顶层的旧 `apk*` 字段继续投影 universal 资产, 因此旧矩阵消费者无需立即升级。
参考解析器支持 `--abi`: 优先选择精确单 ABI artifact, 缺失时回退 universal。

## 宿主选择与调用保护

分发前, AutoJs6 插件中心已有的资产选择器按设备 ABI 在 Release 文件名中寻找精确后缀, 未命中时选择 universal,
最后才使用旧资产兜底。安装后, 插件通过 `PluginInfo.variant` 和 `PluginInfo.supportedAbis` 上报真实能力。

宿主在三个位置复核 ABI:

- 模板服务候选筛选;
- 远程构建服务候选筛选;
- 真正调用服务前的 strict-target 二次检查。

因此即使用户绕过插件中心手动侧载了错误架构变体, 宿主也不会把它交给打包流程, 并会在不可用提示中展示插件 ABI
与设备 ABI。旧插件未上报 `supportedAbis` 时保持兼容, 视作 universal。

## 可复跑验证

本地实现验收使用 AutoJs6 `6.8.0 (5276)` 完成:

```powershell
# 宿主: 生成五个 Runtime Kit
.\gradlew.bat :app:generateRuntimeKit

# 插件: 单包和完整集合校验
python scripts\verify_runtime_kit.py <runtime-kit-dir>
python scripts\verify_runtime_kit_set.py <all-runtime-kits-dir>

# 插件: ABI 契约与矩阵回归
python -m unittest scripts.tests.test_abi_variants -v

# 宿主: 调用前 ABI 策略单元测试
.\gradlew.bat :app:testAppDebugUnitTest `
  --tests org.autojs.autojs.apkbuilder.template.ApkBuilderTemplateAbiPolicyTest
```

实测五个 Runtime Kit ZIP 均可独立校验, universal 模板包含四种 ABI, 每个单 ABI 模板只包含声明的那一种。当前
Runtime Kit ZIP 从 universal 的约 29.59 MiB 降到单 ABI 约 28.22–28.28 MiB; 五个插件 APK 也均已实际 assemble,
逐包核对声明 ABI 与内嵌模板实际 `.so` 目录完全一致。当前原生库占比不大, 节省幅度有限,
但分发契约已经固定, 后续加入更大的 JNI 依赖时不会再强迫所有设备下载全部架构。

首次真实发布仍需按 `docs/e2e-release-drill.md` 使用临时 tag 演练五资产上传、矩阵解析和至少一台 ARM 设备 / 一台
x86 模拟器安装。该步骤会产生外部 Release 状态, 不属于本地实现与验证范围。
