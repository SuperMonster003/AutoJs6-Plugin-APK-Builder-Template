# Runtime Kit 增量更新可行性评估

- Roadmap: M4-3
- 评估日期: 2026-08-30
- 结论: **No-Go —— 当前架构下不实现生产级 Runtime Kit 增量分发**

## 结论摘要

本轮不接入 bsdiff, xdelta 或自定义设备端补丁器, 发布流水线继续分发完整 Runtime Kit 和完整插件 APK。M4-3 的验收目标是形成明确的做 / 不做结论, 因此本项评估完成, 但结论是暂不实施。

原因有三层:

1. 收益不稳定。两个相邻模板样本的精确单向二进制补丁分别占目标文件的 44.235% 和 86.372%, 净节省从 55.765% 降至 13.628%。
2. 当前 Runtime Kit ZIP 会再次压缩已经是 ZIP 的 `template.apk`。在实测的 Alpha5 → 6.8.0 样本中, 外层 ZIP 的补丁为 33.66 MiB, 反而比 29.59 MiB 的完整目标 ZIP 大 13.737%。
3. 用户实际安装的是已签名插件 APK, Runtime Kit 只是其中的不可变资产。只对内层 `template.apk` 做差分只能减少 CI 获取 Runtime Kit 的流量; 它不能让 Android 增量安装插件。若要减少用户下载, 必须由宿主插件中心对**完整、已签名的目标插件 APK**做补丁下载、重建、摘要和签名验证以及全量回退, 这已经是新的 H+P 分发协议, 不是插件仓库可单独完成的 M4-3 小改动。

M4-1 的 ABI 拆分现已落地: 它直接减少每个用户拿到的模板内容, 不需要维护版本边补丁、设备端重建器或额外回退状态。当前原生库占比较小, 单 ABI Runtime Kit 的节省有限, 但这条分发路径仍比引入设备端二进制补丁安全、直接。

## 当前分发链路

```text
AutoJs6 :app:generateRuntimeKit
  -> autojs6-runtime-kit-*.zip       (template.apk 再次 DEFLATE)
  -> 插件 CI 下载、校验并解压
  -> 已签名插件 APK                  (template.apk 作为 assets/runtime-kit/ 资产再次封装)
  -> Android Package Installer       (安装完整、签名一致的插件 APK)
```

这一区分决定了评估对象:

- 对 `template.apk` 的差分, 只能说明内层模板在算法层面有多少可复用内容;
- 对 `autojs6-runtime-kit-*.zip` 的差分, 反映主仓库到插件 CI 的实际 Runtime Kit 传输;
- 对最终用户是否有收益, 必须看完整插件 APK 的下载和安装链路。Android 不会把一个 Runtime Kit 补丁直接应用到已安装 APK 的资产中;
- 在设备上只替换 `template.apk` 后重新打包插件也不可行, 因为设备没有官方插件签名私钥。可安装的增量方案必须重建出与发布端已签名目标 APK **逐字节一致**的文件, 或把 Runtime Kit 迁出 APK 并重新设计信任边界。

## 样本与来源

评估没有从网络下载样本。两份新样本均由本机干净的 AutoJs6 主仓库使用正式任务生成:

```text
gradlew :app:generateRuntimeKit -x :app:lintVitalAnalyzeInrtRelease
```

| 样本 | 来源 | versionCode | `template.apk` 大小 | SHA-256 |
|---|---|---:|---:|---|
| 6.7.1 Alpha4 | 本仓库 `runtime-kit/`; 元数据 `gitSha=local` | 3923 | 79,623,039 B (75.93 MiB) | `99b7535d0817930a7a24469a93d871ce61ac73f7f5a72134e85bd4da6035a975` |
| 6.8.0 Alpha5 | AutoJs6 tag `v6.8.0-alpha5`, commit `13a357318a2544d43e1aa98fa2d803c665a5fe25` | 5201 | 79,454,581 B (75.77 MiB) | `fa3c331ac4a54c2c09ceea7946f80720f139c8ecd55fdef0fb809bbade794cd5` |
| 6.8.0 | AutoJs6 clean HEAD `d7bc884d6749369a9045fc545d3a3f3a7710a55f` | 5276 | 34,028,762 B (32.45 MiB) | `3059e6b67bfb695d640081e6e7b970db7c2c8a3b13457dd64c03266d6826abc2` |

6.7.1 Alpha4 样本的 Runtime Kit 元数据未记录可追溯 commit, 因此它只作为本仓库既有发布输入参与测量, 不把 `gitSha=local` 解读为某个特定宿主提交。Alpha5 和 6.8.0 的工作树在构建前后均为 clean; Gradle 任务分别以 exit 0 完成。

外层 ZIP 样本如下:

| 样本 | 大小 | SHA-256 |
|---|---:|---|
| `autojs6-runtime-kit-v6.8.0-alpha5+5201.zip` | 74,716,467 B (71.26 MiB) | `71c10877aaed090d00ff16b1a99d84b5562577911887618681b893b9004be150` |
| `autojs6-runtime-kit-v6.8.0+5276.zip` | 31,030,106 B (29.59 MiB) | `c3b7eb07173ba4eb673c4947259bc79c3dcc04a6d36f43e41fb1b35bc680ee9e` |

## 测量方法

仓库新增 `scripts/analyze_runtime_kit_delta.py`, 使用 Python 标准库和本项目已依赖的 Git, 不依赖 bsdiff/xdelta 的本机安装。脚本做两类测量:

1. **ZIP 条目复用上限**
   - 对每个同名条目的解压内容计算 SHA-256;
   - 直接读取 ZIP local header 指向的压缩载荷并计算 SHA-256;
   - 只有目标条目的压缩载荷与旧文件完全相同, 才计入可直接复用字节;
   - 分类统计 dex, native libs, resources, assets, Manifest 和签名元数据。
2. **可回放的精确二进制 delta 代理**
   - 以 `git diff --binary --full-index` 生成 delta;
   - 去掉只用于反向恢复的第二块, 只计算前向传输块;
   - 在隔离临时目录把前向补丁应用到旧文件;
   - 要求重建文件大小和 SHA-256 与目标完全一致, 否则脚本失败。

Git binary delta 不是 bsdiff 的大小承诺。不同算法可能得到更小或更大的补丁; 本测量选择它是因为无需新增依赖, 能生成真实可应用的 delta, 并能验证“精确重建已签名 ZIP”这一必要条件。结论同时依赖条目复用率与分发架构, 不把某一个代理算法的数值当作永久上限。

## 实测结果

### 内层 `template.apk`

| 更新边 | 目标全量 | 目标压缩载荷可直接复用 | 精确前向补丁 | 补丁 / 目标 | 净节省 | SHA 回放 |
|---|---:|---:|---:|---:|---:|---|
| 6.7.1 Alpha4 → 6.8.0 Alpha5 | 75.77 MiB | 59.962% | 33.52 MiB | 44.235% | 55.765% | 通过 |
| 6.8.0 Alpha5 → 6.8.0 | 32.45 MiB | 24.120% | 28.03 MiB | 86.372% | 13.628% | 通过 |

条目变化:

| 更新边 | 旧条目 | 新条目 | 内容不变 | 内容变化 | 新增 | 删除 |
|---|---:|---:|---:|---:|---:|---:|
| 6.7.1 Alpha4 → 6.8.0 Alpha5 | 3,967 | 3,971 | 3,624 | 342 | 5 | 1 |
| 6.8.0 Alpha5 → 6.8.0 | 3,971 | 3,857 | 3,278 | 530 | 49 | 163 |

两组样本的共同瓶颈是 dex: 所有 `classes*.dex` 都发生变化, 精确压缩载荷复用率为 0%。第一组看起来较好, 主要因为 40.93 MiB 原生库压缩载荷保持不变, native libs 复用率为 95.92%; 第二组经过依赖模块化和体积下降后, 目标模板的 dex 压缩载荷约 20.99 MiB, 仍全部变化。由此可见, 补丁收益高度取决于某次发布是否保留大块原生库, 不能稳定外推到后续版本。

### 外层 Runtime Kit ZIP

| 更新边 | 目标全量 ZIP | 精确压缩载荷可直接复用 | 精确前向补丁 | 补丁 / 目标 | 净节省 | SHA 回放 |
|---|---:|---:|---:|---:|---:|---|
| 6.8.0 Alpha5 → 6.8.0 | 29.59 MiB | 0.007% | 33.66 MiB | 113.737% | **-13.737%** | 通过 |

外层 ZIP 有 10 个文件, 其中只有默认密钥库及其 sidecar 两项内容不变; 可直接复用的压缩载荷只有 2.08 KiB。`template.apk` 自身已经压缩, 再经外层 DEFLATE 后, 内层相同字节不再对应稳定的外层压缩区间。即使 Git 仍识别出 delta, 其前向传输表示也比完整目标文件更大。

## 复杂度与风险

如果目标是减少最终用户下载, 一个可上线方案至少还需要:

| 领域 | 必需工作 |
|---|---|
| 补丁目标 | 从 Runtime Kit 改为完整、已签名插件 APK, 或重新设计外置 Runtime Kit 存储协议 |
| 版本解析 | 兼容矩阵不仅选择目标版本, 还要以旧 APK SHA-256 选择唯一补丁边; 不允许仅按 versionName 猜基线 |
| 发布资产 | 每个允许的基线 → 目标版本都要生成, 签名或摘要固定并发布; 还要清理失去价值的旧边 |
| 设备端存储 | 同时容纳旧 APK, 补丁和重建中的新 APK; 使用同目录临时文件、fsync 和原子重命名 |
| 信任链 | 应用补丁前验证基线 SHA-256; 重建后验证目标 SHA-256 和官方签名证书; 任一不符立即丢弃 |
| 安装与回退 | 宿主通过 Package Installer 安装完整重建 APK; 空间不足、补丁失败、未知基线或签名失败时回退全量下载 |
| 可观测性 | 区分基线不匹配、补丁损坏、重建失败、摘要失败、签名失败和安装失败, 才能安全灰度 |
| 测试矩阵 | Android API / ABI / 已装版本 / 降级引导 / 进程中断 / 磁盘耗尽 / 补丁资产 404 均需端到端覆盖 |

这会引入宿主插件中心改造, 新的发布资产协议和设备端状态机。M4-1 ABI 拆分与 M6-4 矩阵解析虽已完成, 但正式相邻发布样本仍未就绪; 以当前两组收益波动和外层负收益看, 尚不值得承担这套复杂度。

## 决策

**当前不做。** 具体含义:

- 不向发布工作流增加 bsdiff/xdelta 资产;
- 不改变 Runtime Kit ZIP 格式;
- 不在插件内引入补丁执行器;
- 不改变现有完整 Runtime Kit 校验和完整插件 APK 回退路径;
- 保留分析脚本, 供未来用更多正式相邻版本复测。

这不是永久否决。满足以下条件时, 应创建新的 H+P 分发任务重新评估:

1. M6-4 插件中心已能按兼容矩阵确定目标插件和下载资产, 并拥有全量回退入口 (**已满足**);
2. M4-1 ABI 拆分已经落地, 测量对象改为用户实际下载的各 ABI **完整签名插件 APK** (**已满足**);
3. 至少收集 5 个连续正式发布边, 每个候选补丁均完成逐字节重建、目标摘要和签名验证;
4. 只发布小于目标全量 70% 且至少节省 10 MiB 的单跳补丁, 5 个样本的中位数小于 50%; 未达门槛的版本直接使用全量;
5. 设备端具备原子落盘、空间预检、进程中断恢复、失败即全量回退和可观测错误分类;
6. 禁止多跳补丁链, 防止任一历史资产丢失或中间失败放大更新风险。

## 复跑

比较两个解压后的 Runtime Kit 目录时, 脚本会自动定位 `template.apk`:

```powershell
python scripts/analyze_runtime_kit_delta.py `
  <old-runtime-kit-dir> `
  <new-runtime-kit-dir> `
  --old-label <old-version> `
  --new-label <new-version> `
  --json-out <report.json>
```

比较实际发布 ZIP 时直接传文件:

```powershell
python scripts/analyze_runtime_kit_delta.py `
  <old-runtime-kit.zip> `
  <new-runtime-kit.zip> `
  --json-out <archive-report.json>
```

成功输出必须包含 `replay SHA-256 verified: True`。JSON 报告可留作 CI artifact; 本次未把报告或大体积样本提交到仓库, 文档中的大小、摘要和百分比足以复核本次决策。
