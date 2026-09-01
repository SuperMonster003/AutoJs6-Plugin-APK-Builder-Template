<!--suppress HtmlDeprecatedAttribute, HttpUrlsUsage -->

<div align="center">
  <p>
    <picture>
      <img src="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/res/mipmap/ic_launcher.png?raw=true" alt="autojs6-plugin-apk-builder-template-ic-launcher" border="0" width="128" />
    </picture>
  </p>

  <p>为 AutoJs6 "打包应用" 功能提供独立应用模板的插件</p>

  <p>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases"><img alt="GitHub release (latest by date)" src="https://img.shields.io/github/v/release/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?label=Release"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues"><img alt="GitHub closed issues" src="https://img.shields.io/github/issues/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=A24232&label=Issues"/></a>
    <a href="https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE"><img alt="GitHub License" src="https://img.shields.io/github/license/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template?color=534BAE&label=License"/></a>
  </p>
</div>

******

### 语言 (Languages)

******

当前 README.md 支持以下语言:

- 简体中文 [zh-Hans] # 当前
- [繁體中文 (香港) [zh-Hant-HK]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-HK.md)
- [繁體中文 (台灣) [zh-Hant-TW]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-zh-Hant-TW.md)
- [English [en]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-en.md)
- [Français [fr]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-fr.md)
- [Español [es]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-es.md)
- [日本語 [ja]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ja.md)
- [한국어 [ko]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ko.md)
- [Русский [ru]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ru.md)
- [العربية [ar]](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/.readme/README-ar.md)

******

### 简介

******

AutoJs6 的 "打包应用" 功能可以把脚本或项目打包成一个可独立安装, 独立运行的 APK, 无需在目标设备上安装 AutoJs6. 打包时需要一个内置完整脚本运行环境的 "模板 APK" 作为骨架. 为了给主程序 "瘦身", 较新版本的 AutoJs6 不再内置这个体积较大的模板, 而是将它拆分到本插件中, 由需要打包功能的用户按需安装.

本插件安装后没有图标, 也没有任何界面, 全部工作都在后台由 AutoJs6 自动调用: 打包时, AutoJs6 发现插件, 校验版本与文件完整性, 然后读取插件内置的模板 APK 完成打包.

一句话判断是否需要安装: 会用到 AutoJs6 的 "打包应用" 功能, 就安装插件中心按兼容矩阵为当前 AutoJs6 选出的构建; 从不打包独立应用则无需安装.

******

### 工作原理

******

打包一个独立应用时, AutoJs6 与本插件的协作过程如下:

1. 发现插件: AutoJs6 在系统中查找已安装的模板插件并读取其信息
2. 兼容检查: 比对插件与 AutoJs6 的版本, 协议版本与模板包名, 不匹配时给出警告或阻止打包
3. 完整性校验: 核对模板 APK 的 SHA-256 摘要, 确保文件未损坏, 未被篡改
4. 传输模板: 通过进程间管道流式读取模板 APK, 不产生临时副本
5. 完成打包: AutoJs6 将脚本, 配置与资源写入模板, 生成最终的独立 APK

******

### 功能

******

- 为 AutoJs6 的 "打包应用" 功能提供完整的独立应用模板 (Runtime Kit), 安装后无需任何配置即可使用.
- 每个插件构建都会在版本名中保留一个实际构建所用的 AutoJs6 宿主 (如 1.0.0+autojs6-6.8.0-alpha5), 并可显式声明经验证的补丁级闭区间; 精确匹配不提示, 区间内非精确宿主会警告, 区间外则阻止打包.
- 双重完整性保障: 构建插件时校验 Runtime Kit 全部文件的 SHA-256 摘要与模板必需条目, 打包时再向 AutoJs6 上报模板摘要供复核.
- 模板通过进程间管道流式传输给 AutoJs6, 不产生冗余的临时拷贝.
- 内置默认签名库, 未配置自定义签名时也能直接打出可安装的 APK.
- 支持实验性 "远程构建" 协议, 可由插件进程独立完成轻量打包 (默认关闭, 详见能力边界).
- 插件信息, 使用说明, README 与 CHANGELOG 覆盖简体中文, 香港繁体, 台湾繁体, 英语, 法语, 西班牙语, 日语, 韩语, 俄语与阿拉伯语共 10 种语言.

******

### 快速上手

******

- **怎么装**: 建议从 AutoJs6 插件中心下载安装: 支持该机制的宿主会读取兼容矩阵, 自动选择配套插件版本与当前设备的精确 ABI 资产, 缺失时回退 universal. 手动安装时, 请前往本仓库 [Releases](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/releases) 页面, 根据与 AutoJs6 同名的发布标签或插件版本名中的 autojs6- 后缀确认配套宿主 (例如插件 v1.0.0+autojs6-6.8.0-alpha5 配套 AutoJs6 v6.8.0 Alpha5). 若插件中心选出的配套版本低于已安装版本, 请按提示先卸载再安装; Android 不支持降级覆盖安装.
- **怎么用**: 无需任何额外操作. 在 AutoJs6 中像往常一样使用 "打包应用" 功能, 打包过程会自动发现并使用本插件提供的模板.
- **怎么确认已生效**: 未安装 (或版本不匹配) 时, AutoJs6 的打包入口会提示先安装或启用本插件; 安装匹配版本后提示消失, 即表示插件已被正常识别. 插件本身没有图标与界面, 桌面上找不到它属于正常现象.
- **出错了看哪里**: 提示兼容性警告时, 请使用插件中心按兼容矩阵选出的构建, 或确认当前宿主位于插件声明区间内; 提示版本不兼容并阻止打包时, 请安装矩阵匹配的构建; 提示模板损坏或校验失败时, 从官方渠道重新下载安装插件; 其他问题可携带 AutoJs6 日志与复现步骤到 [Issues](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/issues) 反馈.

******

### 能力边界

******

为避免误解, 以下事项明确不属于本插件的功能范围:

- 本插件不能独立使用: 它没有图标与界面, 仅供 AutoJs6 在打包时调用.
- 本插件不生成模板 APK: 模板与 Runtime Kit 由 AutoJs6 主仓库构建发布, 本插件只负责校验, 封装与分发.
- 本插件不参与脚本的编写与日常运行: 只有 "打包应用" 功能会读取它.
- 远程构建是实验性能力且默认关闭: 官方发布的插件不启用该能力, 仅在自行构建并显式开启时可用.
- 插件不放宽版本要求: 与 AutoJs6 版本不一致时打包可能被阻止; 即使勉强通过, 产物也不保证正常运行.

******

### 常见问题

******

**问: 插件中心如何选择构建?**

答: 支持该机制的 AutoJs6 会用自身 versionCode 查询 compat-matrix.json, 选择兼容区间内 pluginVersionCode 最高的构建, 再优先选择当前设备的精确 ABI 资产并在缺失时回退 universal. 只有显式设置 allowPatchVersionMismatch=true 时, 矩阵条目才能覆盖已验证的补丁区间: 实际构建所用宿主可无提示打包, 区间内其他宿主复用同一构建时会收到警告, 区间外宿主不能使用该条目. 若矩阵无可用条目, 仍回退现有 Release/标签通道. 若配套插件版本低于已安装版本, 插件中心会提示先卸载再安装配套构建; Android 无法执行覆盖降级安装.

**问: 为什么插件要与 AutoJs6 版本配套?**

答: 模板 APK 内置的脚本运行环境与 AutoJs6 的运行时 API 严格对应, 因此兼容性依据插件声明并通过校验的 versionCode 契约判定, 而不是插件自身的语义版本号. 大多数构建只适配一个确切宿主; 构建也可以显式声明一个经过验证的补丁版本闭区间. 此时配套宿主静默通过, 区间内的其他宿主给出警告, 区间外则阻止打包. 插件自身版本 (如 1.0.0) 独立演进, 版本名中的 autojs6- 后缀与发布标签标注配套宿主; 使用旧版 AutoJs6 时请下载对应旧标签下的插件构建 (若需从新版插件回退, 请先卸载再安装, Android 不支持降级覆盖安装).

**问: 安装后在桌面上找不到插件, 是不是安装失败了?**

答: 不是. 本插件没有图标与界面, 只以后台服务形式供 AutoJs6 调用. 可在系统 "设置 > 应用" 列表中确认 APK Builder Template 已安装.

**问: 打包时提示模板校验失败怎么办?**

答: 通常说明插件安装包不完整或已损坏. 请从 AutoJs6 插件中心或本仓库 Releases 重新下载安装; 若问题依旧, 欢迎到 Issues 反馈.

**问: 插件体积为什么这么大?**

答: 插件内置了完整的独立应用模板, 其中包含脚本引擎与各处理器架构的原生库. 这正是模板从 AutoJs6 主程序中拆分出来的原因: 只有需要打包功能的用户才需要承担这部分体积.

**问: 什么是 "远程构建"?**

答: 一种实验性协议, 允许插件在自身进程内完成轻量打包 (解包模板, 写入脚本与配置, 重写包名与资源, 重新签名). 官方发布的插件默认关闭该能力, 目前仅供开发者自行构建体验.

******

### 技术参考

******

以下内容面向插件开发者与集成方; 仅使用插件时通常无需阅读.

#### Runtime Kit

Runtime Kit (运行时套件) 由 AutoJs6 主仓库构建, 是独立应用模板的唯一来源. 本插件只校验并封装该产物, 不生成 `template.apk`. 一个完整的 Runtime Kit 通常包含以下文件:

```text
template.apk
template.apk.sha256
default_key_store.bks
default_key_store.bks.sha256
runtime-kit.json
build-contract.json
public-api.txt
assets-manifest.json
native-libs.json
provenance.json
```

#### 插件发现标识

宿主通过以下标识发现并绑定本插件:

```text
Plugin ID:  autojs6-apk-builder-template
Engine:     apk-builder-template
Variant:    inrt-universal
Actions:    org.autojs.plugin.INFO / org.autojs.plugin.APK_BUILDER
Template:   org.autojs.autojs6.inrt
```

#### 本地构建

先在 AutoJs6 主仓库生成 Runtime Kit:

```powershell
.\gradlew.bat --console=plain :app:generateRuntimeKit
```

再在本仓库指定 Runtime Kit 目录构建插件:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease `
  -Pautojs.apkBuilder.templatePlugin.runtimeKitDir=<runtime-kit-dir>
```

也可以把发布的 `autojs6-runtime-kit-*.zip` 解压到 `runtime-kit/`, 然后直接构建:

```powershell
.\gradlew.bat --console=plain :app:assembleRelease
```

#### 发布流程

生产发布流程如下:

```text
AutoJs6 tag
-> main repository generates autojs6-runtime-kit-*.zip
-> main repository uploads the Runtime Kit to its GitHub Release
-> main repository dispatches SuperMonster003/AutoJs6-Plugin-APK-Builder-Template
-> this repository downloads and verifies the Runtime Kit
-> this repository builds the plugin APK
-> this repository uploads the plugin APK to the same tag Release
-> this repository records the pairing into compat-matrix.json
-> AutoJs6 Plugin Center installs this plugin
```

#### 签名

生产插件必须使用受信任的 AutoJs6 插件签名密钥. GitHub Actions 发布需要以下仓库密钥:

```text
SIGNING_KEY_BASE64
SIGNING_KEY_STORE_PASSWORD
SIGNING_KEY_ALIAS
SIGNING_KEY_PASSWORD
SIGNING_CERT_SHA256
```

本地发布构建仍支持被忽略的根目录 `sign.properties`:

```properties
storeFile=/absolute/path/to/release.jks
storePassword=...
keyAlias=...
keyPassword=...
```

******

### 开发路线图

******

插件的能力规划与完成情况以可核查的清单形式维护在 ROADMAP.md 中, 涵盖远程构建稳定化, 按架构拆分模板变体, 放宽补丁级版本兼容等方向. 欢迎通过 Issues 参与讨论.

- [查看 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### 发行历史

******

# v1.0.0

###### 2026/09/01

* `提示` 插件独立版本线的首个发布版本; 插件中心通过 compat-matrix.json 选择配套 ABI 构建, 手动下载时应核对 autojs6- 宿主后缀, 远程构建仍默认关闭
* `新增` 引入插件 SemVer 1.0.0, 独立构建号, 复合版本名与单调递增的 Android versionCode, 支持同一宿主发布多个插件构建
* `新增` 新增 universal, arm64-v8a, armeabi-v7a, x86_64 与 x86 变体, 支持精确 ABI 选择及 universal 回退
* `新增` 新增失败关闭的宿主兼容区间契约与权威兼容矩阵, 让经过显式验证的相邻补丁区间可共用一个插件构建
* `修复` 使实验性远程单文件构建编号与旧构建器保持一致，并新增失败关闭的工作区空间预检：交叉核对输入解压大小、使用构建期验证的模板展开上限，并保留 256 MiB 空间
* `修复` 在进入 BUILD/SIGN 前拒绝旧版内嵌 Node.js 打包元数据和源码指令并提示迁移到外部 Runtime 插件，同时移除已废弃的 Manifest 服务与前台权限注入
* `修复` 修复实验性远程会话中关闭与构建线程的竞态: 取消或关闭后, 工作线程可能重新创建已删除的会话工作区; 现在会等待工作线程结束再清理, 最终保持零残留
* `修复` 加强实验性远程构建: 拒绝路径清单未声明的 TypeScript 暂存密文, 并在工作区规范化文件名后正确识别自定义 BKS 签名库
* `修复` 收紧实验性远程构建输入边界: 严格校验 Parcelable/Bundle 与 project.json 的类型, 大小和嵌套深度, 限制签名库, 图标及 ZIP 路径深度/段长, 并修复 ARSC 包名和派生输出文件名越界
* `修复` 部分系统安装后无法通过插件中心激活的问题
* `优化` 统一 Gradle 与 Python 的 Runtime Kit 校验规则, 覆盖摘要, 大小, 必需文件, APK 条目及五变体一致性
* `优化` 更新安装说明, FAQ, 发布演练与 10 语言文档, 说明配套版本, ABI 选择, 降级恢复及独立版本机制
* `优化` 统一 README 版式与 Gradle 平台版本管理方式

# v6.8.0 Alpha5

###### 2026/07/16

* `提示` 配套 AutoJs6 v6.8.0 Alpha5; 支持该机制的插件中心会自动解析配套构建, 手动安装时按 Release 标签或 autojs6- 后缀确认; 插件没有图标与界面, 由 AutoJs6 在打包应用时自动调用
* `新增` 支持被 AutoJs6 自动发现并读取内置模板, "打包应用" 功能不再依赖主程序内置的模板 APK
* `新增` 内置完整 Runtime Kit: 模板 APK, 默认签名库, 运行时元数据与契约文件
* `新增` 打包前自动进行版本与协议兼容性检查, 不匹配时明确警告或阻止, 避免产出无法运行的应用
* `新增` 构建插件时校验 Runtime Kit 的 SHA-256 摘要与模板必需条目, 运行时向 AutoJs6 上报模板摘要供复核
* `新增` 实验性远程构建协议: 可由插件进程独立完成轻量打包 (默认关闭, 需构建时显式开启)
* `新增` 接入自动化发布流程: AutoJs6 主仓库发版时自动构建插件, 使用受信任密钥签名并校验证书指纹后发布配套 APK
* `新增` 插件信息, 使用说明, README 与 CHANGELOG 覆盖简体中文, 香港繁体, 台湾繁体, 英语, 法语, 西班牙语, 日语, 韩语, 俄语与阿拉伯语共 10 种语言

# v6.7.1 Alpha4

###### 2026/07/09

* `提示` 首个公开发布版本, 需搭配同版本 AutoJs6 (v6.7.1 Alpha4) 使用
* `新增` 从 AutoJs6 主仓库拆分为独立插件仓库, 提供模板 APK 插件服务的初始实现
* `新增` 建立由 AutoJs6 主仓库触发的 Runtime Kit 获取, 校验与插件构建发布流水线

##### 更多发行历史可参阅

* [CHANGELOG](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/app/src/main/assets/doc/CHANGELOG-zh-Hans.md)

******

### 许可证

******

本项目基于 Mozilla Public License 2.0 开源, 允许在遵循该协议的前提下使用, 修改与分发.

- [Mozilla Public License 2.0](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/LICENSE)

******

### 资源结构

******

```text
.readme/lang_*.json
.changelog/lang_*.json
.python/generate_markdown.py
app/src/main/res/values-*/strings.xml
app/src/main/res/raw-*/plugin_instruction.md
app/src/main/assets/doc/CHANGELOG-*.md
```

`strings.xml` 提供插件名称, 描述和兜底说明的本地化; `plugin_instruction.md` 提供宿主侧展示的插件使用说明. README 与 CHANGELOG 由 `.python/generate_markdown.py` 根据 JSON 语料生成; 修改文档请编辑对应 JSON 后重新运行脚本, 不要直接编辑生成产物.

******

### 相关链接

******

- AutoJs6 主项目: https://github.com/SuperMonster003/AutoJs6
- AutoJs6 文档: https://docs.autojs6.com
