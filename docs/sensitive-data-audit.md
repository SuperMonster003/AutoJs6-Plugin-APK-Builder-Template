# 远程构建敏感数据审计

## 1. 结论与边界

2026/08/31 的资格批次通过了 G5 的“敏感数据与关闭后工作区”子门槛。设备用例在成功、认证失败和启动前取消三种终态中
均验证了 callback / progress / `ApkBuildResult`、输出 APK、请求对象和关闭后工作区; 随后的宿主机扫描覆盖本次资格文本、
历史资格证据文本、两仓库现存构建/测试报告以及资格插件主 APK。插件本轮 JUnit/HTML/logcat 在 IDE 定向构建清理
`app/build` 前已复制进资格证据, 因而仍由本次资格文本批次覆盖。所有接受批次均为 `PASS`, 命中 0、扫描错误 0。

该结论只关闭这一子门槛, 不表示整个 G5 或 M3-4 已完成。Parcelable / JSON / ZIP / Manifest / ARSC 的确定性畸形语料已由
后续 `docs/remote-build-fuzz-audit.md` 收口; G2、G3、G4 已由后续资格批次关闭, 独立插件安全审查、G7、完整预览周期及 R2/R3 回退仍未完成。当前仍是 R0, 官方源码继续声明
`supportsRemoteBuild=false`, No-Go for default。

## 2. 受保护数据与观察面

资格规则在 `scripts/sensitive_data_rules.json` 中声明四类固定哨兵:

- TypeScript 构建暂存的脚本明文;
- 自定义签名库口令;
- key alias 口令;
- 32-byte TypeScript 一次性暂存密钥。

`scripts/audit_sensitive_data.py` 还包含高置信凭据模式: PEM 私钥头、GitHub classic / fine-grained token、AWS access key、
Google API key、Slack token、Stripe live key、JWT、URL basic auth、Bearer token 和常见 secret assignment。设备测试与离线扫描
共同覆盖以下观察面:

1. `warnings`, `errors`, output 元数据、更新后的 project JSON 与 `ApkBuildResult.extras`;
2. 每一条 progress callback 的 title / detail;
3. 成功输出 APK 的原始字节及全部解压条目;
4. 认证失败与取消结果, 包括无输出保证;
5. 请求 Bundle 的一次性密钥移除/清零和两个签名口令置空;
6. session 关闭后的插件私有工作区、宿主输入暂存与输出校验工作区;
7. Gradle / JUnit / HTML / logcat、历史资格证据文本和两仓库当前构建报告。

## 3. 扫描器安全契约

扫描器自身不得成为新的泄漏面:

- 报告不包含命中字节, 不包含扫描根绝对路径;
- 命中只记录规则 ID、相对位置、计数和 HMAC-SHA-256 截断指纹;
- HMAC key 每次运行随机生成且不写入报告, 进程退出后不可用于反查;
- 若文件名或 ZIP entry 名自身命中规则, 整个相对位置替换为 HMAC 路径指纹;
- 普通文件与 ZIP/APK 条目均流式扫描; archive 上限为 65,536 条目、单条 512 MiB、累计 2 GiB;
- 任一命中返回 exit 1, 任一扫描错误返回 exit 2; 只有命中 0 且错误 0 才返回 exit 0 / `PASS`。

归档默认只运行显式资格哨兵规则。若对编译产物启用通用启发式, 第三方代码中内嵌的示例 token、检测规则或测试夹具会产生
不可区分的静态误报; 需要这类审计时必须显式使用 `--scan-archive-heuristics` 并逐项复核。Android-test APK 和规则/测试源码
按设计内嵌资格哨兵, 因而不是“哨兵不得出现”的接受面; 本次对不含测试代码的主插件 APK 执行了全条目显式哨兵扫描。

## 4. 复跑方式

扫描资格文本与报告:

```powershell
python scripts/audit_sensitive_data.py `
  --rules scripts/sensitive_data_rules.json `
  --root "qualification=<evidence-directory>" `
  --include '*.txt' --include '*.xml' --include '*.html' --include '*.json' `
  --output '<evidence-directory>/focused-text-audit.json'
```

扫描主插件 APK 的全部条目:

```powershell
python scripts/audit_sensitive_data.py `
  --rules scripts/sensitive_data_rules.json `
  --root "qualification-apk=<plugin-apk>" `
  --no-heuristics `
  --output '<evidence-directory>/qualification-plugin-apk-audit.json'
```

运行规则正控、脱敏输出和 archive 边界测试:

```powershell
python -m unittest scripts.tests.test_sensitive_data_audit -v
```

任何复跑只要不是 `PASS`, 或报告中的 `findings/errors` 任一大于 0, 即视为 G5 阻断; 不得把原始命中复制进 Issue、日志或
发布材料。需要调查时仅使用规则 ID、相对位置和本地受控环境中的原文件。

## 5. 2026/08/31 资格结果

| 批次 | 覆盖量 | 结果 |
|---|---:|---|
| 设备三终态聚焦用例 | 1 test; success/failure/cancel 各 1 | PASS; 265.37 s, 0 skipped/failed |
| 扫描器专项单元测试 | 8/8 | PASS; 含文字、文件名、archive、token、assignment 正控与脱敏断言 |
| 仓库 Python 全回归 | 18/18 | PASS |
| 本次资格文本 | 15 files / 3,598,640 bytes | PASS; 0 findings / 0 errors |
| 资格插件主 APK | 1 file / 1,124 entries / 104,995,142 uncompressed bytes | PASS; 0 findings / 0 errors |
| 全部历史资格证据文本 | 2,373 files / 26,982,232 bytes | PASS; 0 findings / 0 errors |
| 两仓库当前构建与测试报告 | 4 roots / 19 files / 5,869,418 bytes | PASS; 0 findings / 0 errors |
| 后续确定性 fuzz 最终日志、生产 APK 与 JVM XML | 5 files / APK 1,124 entries / 105,019,786 archive bytes | PASS; 6 类显式规则 + 11 类启发式, 0 findings / 0 errors |

设备最终哨兵为:

```text
M3_G5_SENSITIVE_DATA_RESULT success=1 failure=1 cancel=1 callback_findings=0 output_findings=0 workspace_entries=0 keys_zeroed=3 passwords_cleared=3
```

`connectedDebugAndroidTest` 在结束时卸载了资格插件与测试包。复核时插件包、测试包均不存在; 宿主保留, 宿主输入暂存和输出
校验工作区均不存在; 未操作实体机。完整 APK、JUnit/HTML、三次尝试日志、四份脱敏扫描报告、设备清理记录、manifest 与
逐文件 SHA-256 位于工作区外
`D:\idea-projects\.a6-compat-audit-artifacts\m3-4-g5-sensitive-data-audit-2026-08-31`。

前两次设备尝试均由维护者主动中止并保留: 首版测试 harness 在逐字节循环中执行 JUnit assertion; 第二版虽然改为 KMP,
仍为每个 ZIP entry 重建多模式状态。最终版复用单遍 Aho–Corasick matcher, 设备用例正常结束并通过。两次中止发生在测试
扫描吞吐阶段, 没有测试失败或生产异常, 不计入接受批次。

## 6. 已知局限

- 固定哨兵证明本次实际传输的已知敏感值没有逃逸; 通用规则只覆盖高置信格式, 不能数学证明任意未知字符串都不是秘密。
- 扫描的是持久化文件、callback/result/progress 与设备日志/报告, 不是进程内存取证; 密钥清零证据来自同一请求字节数组的
  设备断言。
- APK 通用凭据启发式默认关闭以避免第三方代码误报; 显式资格哨兵仍扫描主 APK 原始字节与每个解压条目。
- 本审计不能替代 G5 要求的独立插件安全审查。后续 72 条确定性 fuzz 也只覆盖已列举的可复跑语料, 不构成任意秘密或任意
  输入组合的数学证明。
