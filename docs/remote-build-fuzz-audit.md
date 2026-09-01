# 远程构建确定性畸形输入审计

## 1. 结论与放量边界

2026/08/31，实验性远程构建完成了 G5 的确定性畸形输入 / fuzz 子门槛。40 条 AIDL Parcelable / Bundle 请求语料与
32 条 `project.json` / Manifest / ARSC 语料均通过真实 `Parcel` 往返并进入实际会话，共 72/72 个唯一 case；所有负向
case 都得到 `STATUS_FAILED / LEVEL_BLOCK`，没有输出、目录逃逸、未关闭 FD、工作区残留或宿主控制内容回显。API 36 / x86_64
上的完整设备类随后通过 39/39，0 skipped、0 failed。

这项结论关闭的是可确定复跑的畸形输入子门槛，不声称穷尽所有输入组合，也不等同于覆盖引导 fuzzer、内存取证或独立第三方
安全审查。G5 仍受独立插件安全审查阻断；G2、G3、G4 已由后续资格批次关闭，G7、完整预览周期与 R2 前的独立回退仍未完成。因此项目继续保持 R0，官方
`supportsRemoteBuild=false`，No-Go for default。

## 2. 新增生产边界

| 输入面 | 失败关闭规则 |
|---|---|
| AIDL request | `hostVersionCode >= 0`、协议版本为正；FD 元数据不得脱离对应 FD；摘要只能是 64 位十六进制；extras 只接受 9 个协议键且总键数最多 16 |
| 自定义签名库 | 声明长度和实际 FD 读取均最多 64 MiB；有 FD 时口令与 alias 必需；口令最多 4,096 个 UTF-16 code unit，alias 最多 255 UTF-8 bytes |
| 输出名 | 输入路径最多 4,096 UTF-8 bytes；实际 basename 最多 238 bytes，连同自动追加的 `.apk` 与 `.unsigned.apk` 后最坏值仍为 ext4 的 255-byte 单段上限 |
| TypeScript v3 | 三项元数据必须全有或全无；密钥必须为 32 bytes；路径清单最多 16,384 项，只允许 `.js/.mjs/.cjs`，且规范化后不得重复 |
| `project.json` | 完整单一根对象，最多 512 KiB UTF-8、64 层；拒绝尾随/相邻根、类型强制转换、控制字符与未配对 surrogate |
| 项目字段 | 应用名和 versionName 各最多 256 UTF-8 bytes；ASCII packageName 最多 127 bytes；数组最多 512 项，单项最多 255 bytes；versionCode 必须为正 32-bit integer |
| ZIP / 项目路径 | 总路径最多 4,096 UTF-8 bytes、单段最多 255 bytes、最多 128 段；拒绝绝对路径、空段、`.`/`..`、C0/C1 控制字符与未配对 surrogate；文件系统异常只回报安全条目序号 |
| 图标 | 压缩文件最多 16 MiB；先 bounds-only 解码；宽高各最多 4,096，总像素最多 4,194,304；metadata/完整解码的异常与 OOM 转为安全失败 |
| ARSC 固定包名槽 | 写入器独立要求最多 127 个 UTF-16 code unit，保留 NUL 终止空间，避免把后续 `resources.arsc` 字段整体错位 |
| 敏感清理 | 即使畸形 Bundle 在读取密钥时抛出运行时异常，两个签名口令字段仍在 `finally` 中置空；可读取的一次性密钥仍覆写并移除 |

项目与 native/assets ZIP 的压缩输入、条目数、单条/总解压及单条/累计压缩比上限继续以
`docs/remote-build-protocol.md` 的有界解包表为准。

## 3. 确定性语料

### 3.1 Parcelable / Bundle：40 条

覆盖负数/零协议字段、缺失 FD、负数或超限长度、摘要格式、无 FD 元数据、自定义签名库字段、口令/alias 上限、控制字符与
surrogate、输出路径上限、extras 缺失/未知键/键数、每个协议键的错误类型、source kind/path、255-byte 单段、128 段深度、
C1 字符，以及 TypeScript v3 的协议、版本、密钥长度、路径容器/数量/扩展名/重复项。

每条请求先经过真实 Android `Parcel.writeParcelable/readParcelable`，原始和往返后的 FD 分别管理；会话终态必须满足：

- `FAILED / STATUS_FAILED / LEVEL_BLOCK`；
- callback/progress 不含 fuzz 哨兵；
- 输出文件不存在；
- project/native/keystore FD 引用与两个口令字段已清空；
- `remote-apk-build` 工作区为空。

### 3.2 JSON / 二进制编辑入口：32 条

覆盖非对象、尾随文档、相邻根对象、65 层嵌套、512 KiB+、字段类型、空白/控制字符/surrogate、UTF-8 长度、package 语法与
ARSC 槽上限、versionCode 非整数/零、main 绝对路径/遍历、ABI 值/重复、数组类型/数量/单项长度、签名方案、launch/splash
类型以及 build number 上界。目录源专门用于验证 `main`，避免单文件源按协议强制改写为 `main.js` 而造成假覆盖。

### 3.3 独立边界与正向回归

- 实际 FD 为 64 MiB+1、声明为 64 MiB 的稀疏 keystore 在第一个越界字节写入前拒绝；
- 16 MiB+1 图标在 metadata 解码前拒绝，2,049 × 2,049 的 PNG header 在完整 bitmap 分配前拒绝；
- 畸形图标无法进入资源写入；
- 127-byte packageName、边界应用名/versionName 与 238-byte 输出候选实际完成 Manifest/ARSC 重写、打包与签名；
- `RemoteBoundedStreamCopier`、JSON/ARSC 外壳与 ZIP/path 的纯 JVM 矩阵共 24/24。

## 4. 最终接受结果

| 层级 | 结果 |
|---|---|
| JVM | 24/24：bounded copier 3、JSON/ARSC envelope 6、ZIP/path 15；0 failed/errors/skipped |
| API 36 定点语料 | 2/2 test methods，72/72 unique cases，6.430 秒；fuzz 哨兵 0 命中 |
| API 36 完整设备类 | 人工审查后 39/39、146.516 秒；生成十语言 changelog 后以最终生产 APK 字节再跑 39/39、141.647 秒；均为 0 skipped/failed |
| 最终 logcat | 1,515 行；72 条唯一 `RemoteFuzzAudit`；四类明文哨兵 0 命中；FATAL/crash 0 |
| 脱敏扫描 | 最终 logcat、生产插件 APK 与 JVM XML 共 5 files；APK 1,124 entries；0 findings/errors |
| 私有目录 | `cache/remote-apk-build` 与 `cache/remote-build-instrumented` 均为空 |

目标设备为专用 `AVD_API_36.1`，API 36，x86_64，硬件序列 `EMULATOR37X1X11X0`。本批未向实体机安装、启动、强停或写入
任何包；一次 Gradle 设备发现阶段曾对已连接实体机执行只读属性查询，后续安装和 instrumentation 全部使用显式
`adb -s emulator-5556`。

## 5. 过程缺陷与保留历史

本批没有把中间“绿色”结果直接升级为资格证据：

1. 首次 2/2 运行因 `present.none()` 错把“列表本身非空”当成“没有 TypeScript 字段”，所有普通请求在同一前置条件失败；报告
   虽为绿色，但分支覆盖无效，已单列为 invalid-coverage attempt。
2. 修复为 `present.none { it }` 后，语料生成器又暴露两处测试建模问题：`JSONObject` 会把 `1.0` 规范化为整数文本，单文件源
   也会按协议覆盖 `main`；最终分别改用原始 JSON value 和目录源。
3. 边界正样本发现输出候选名在自动追加两个后缀后达到 257 bytes，真实触发 `ENAMETOOLONG`；生产 basename 上限由草案值
   收紧为 238 bytes，并以 255-byte 派生结果单测固定。
4. 首次完整类为 37/39：一条旧错误短语断言过窄；敏感数据夹具在没有 keystore FD 时携带口令，已被新的严格协议正确拒绝。
   最终成功/认证失败改用有效自定义 keystore，取消路径保留唯一口令哨兵，未放宽生产协议。
5. 第一次 39/39 后的人工审查继续发现相邻 JSON 根、路径深度/单段/Unicode、bounds-only OOM 与畸形 Bundle 清理边角；修订后
   才执行本文列出的最终 24/24、72/72 和 39/39。
6. 十语言 changelog 生成会改变生产 APK 的 assets，因此另外保留人工审查后、文档生成前的 39/39 二进制与日志；随后用
   SHA-256 为 `de9f1ee100349e4af79bcc916c73803a37472ae9f121787208f68deb29a96d0b` 的最终生产 APK 完整复跑 39/39。

工作区外证据目录
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g5-deterministic-fuzz-2026-08-31`
保留 invalid attempt、审查前 39/39、人工审查后但文档生成前的 39/39 二进制/日志、最终 APK/AndroidTest/JVM XML/logcat、Runtime Kit 输入清单、脱敏扫描结果、
设备/尝试说明、机器可读 manifest 与逐文件 SHA-256。测试 Android APK 内含资格哨兵，按规则不作为“生产 APK 零哨兵”扫描对象；
生产插件 APK 和最终运行日志均纳入扫描。

## 6. 可复跑命令

```powershell
.\gradlew.bat :app:testDebugUnitTest :app:assembleDebug :app:assembleDebugAndroidTest --stacktrace

$adb = "<android-sdk>\platform-tools\adb.exe"
& $adb -s <isolated-serial> shell am instrument -w -r `
  -e class "org.autojs.plugin.apkbuilder.template.impl.RemoteApkBuildSessionInstrumentedTest#deterministicParcelableRequestCorpusFailsClosed,org.autojs.plugin.apkbuilder.template.impl.RemoteApkBuildSessionInstrumentedTest#deterministicJsonAndBinaryEditorCorpusFailsClosed" `
  org.autojs.plugin.apkbuilder.template.test/androidx.test.runner.AndroidJUnitRunner

& $adb -s <isolated-serial> shell am instrument -w -r `
  -e class org.autojs.plugin.apkbuilder.template.impl.RemoteApkBuildSessionInstrumentedTest `
  org.autojs.plugin.apkbuilder.template.test/androidx.test.runner.AndroidJUnitRunner
```

接受标准是测试退出成功、72 条 case 名唯一且每条只有一个失败关闭终态、敏感扫描 `PASS`、工作区为空。任何 crash、timeout、
输出、路径逃逸、未关闭 FD、敏感命中或 scanner error 都重新阻断 G5。
