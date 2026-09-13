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

AutoJs6 的 "打包应用" 功能可把脚本或项目生成能独立安装和运行的 APK. 为保持主程序精简, 体积较大的模板与全部打包核心都由本插件提供.

本插件没有图标和界面. AutoJs6 负责发现与校验插件, 准备有界请求并显示进度; 插件负责展开自身模板, 写入项目与资源, 修改 Manifest/resources, 选择 ABI, 管理签名并返回候选 APK; AutoJs6 最后独立复核输出再发布.

整个过程都在同一台 Android 设备上通过 Binder 与文件描述符完成, 不会把项目源码上传到网络或云端构建服务.

******

### 工作原理

******

打包独立应用时, AutoJs6 与本插件按以下步骤协作:

1. 准入: AutoJs6 校验官方签名, 启用状态, 宿主版本区间, ABI, 正式构建能力, 协议与设备内执行模式
2. 准备请求: AutoJs6 生成有大小边界的项目/原生库/签名库输入, 并固定预期包身份与签名者
3. 插件构建: 插件再次校验请求, 展开自身 Runtime Kit 模板, 写入项目, 修改 Manifest/resources, 裁剪 ABI 并签名
4. 返回结果: 插件通过只读文件描述符返回候选 APK, 并清理私有工作区
5. 宿主发布: AutoJs6 重查大小, SHA-256, APK 结构, 签名, 签名者, 包名与版本, 全部通过后才原子替换目标

******

### 功能

******

- 完整拥有 AutoJs6 的设备内打包核心, 包括模板处理, 项目/资源写入, Manifest 与 resources.arsc 修改, ABI 选择, 签名库操作和签名.
- 保持 AutoJs6 精简: 宿主只负责 UI, 信任/兼容准入, 请求准备, 取消/进度和输出独立复核, 不保留第二套构建器.
- 全部在同一台 Android 设备上通过 Binder/AIDL 与 ParcelFileDescriptor 执行, 不上传项目源码到网络或云端.
- 每个插件构建都与经过校验的 AutoJs6 Runtime Kit 配对, 并可声明经显式验证的补丁版本闭区间.
- 提供 universal, arm64-v8a, armeabi-v7a, x86_64 与 x86 变体, 支持精确 ABI 选择及 universal 回退.
- 内置默认签名库, 并由插件创建/验证 BKS/JKS, 同时继续支持自定义签名库.
- 插件信息, 使用说明, README 与 CHANGELOG 覆盖 10 种语言.

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

- 本插件不能独立使用: 它没有图标和界面, 只能由兼容的 AutoJs6 宿主调用.
- 设备内构建不是云端构建: 本协议不会上传项目源码.
- AutoJs6 不再保留第二套进程内打包核心. 插件缺失, 被禁用, 不受信任, 不兼容或构建失败时, 本次请求停止并保留旧产物.
- Runtime Kit 仍由 AutoJs6 仓库生成; 插件负责校验, 封装, 分发和使用该套件, 不自行创造运行时模板.
- 旧 "远程构建" 能力继续为旧宿主保持关闭. 该名称只表示另一个设备内应用进程, 不是互联网服务, 且与正式插件托管能力分离.

******

### 常见问题

******

**问: 插件中心如何选择构建?**

答: 支持该机制的 AutoJs6 会用自身 versionCode 查询 compat-matrix.json, 选择兼容区间内 pluginVersionCode 最高的构建, 再优先选择当前设备的精确 ABI 资产并在缺失时回退 universal. 只有显式设置 allowPatchVersionMismatch=true 时, 矩阵条目才能覆盖已验证的补丁区间: 实际构建所用宿主可无提示打包, 区间内其他宿主复用同一构建时会收到警告, 区间外宿主不能使用该条目. 若矩阵无可用条目, 仍回退现有 Release/标签通道. 若配套插件版本低于已安装版本, 插件中心会提示先卸载再安装配套构建; Android 无法执行覆盖降级安装.

**问: 为什么插件必须与 AutoJs6 版本配套?**

答: 模板内的运行时必须匹配宿主 API. 插件中心会从兼容矩阵选择最高兼容插件版本与最合适的 ABI 资产; 区间外宿主会被阻止.

**问: 桌面上找不到插件, 是安装失败吗?**

答: 不是. 插件特意不提供图标和界面, 只作为 AutoJs6 的后台服务运行; 可在系统 设置 > 应用 中确认.

**问: 项目会发送到远程服务器吗?**

答: 不会. 宿主与插件只在同一台 Android 设备的两个应用进程之间通信. 历史源码称 "远程构建" 是因为 Binder 调用跨进程; 正式模式明确为 `on-device-plugin`.

**问: 插件构建失败会怎样?**

答: AutoJs6 会停止请求, 显示可行动错误并保留已有输出 APK, 不会静默切换到宿主内第二套构建器.

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

ROADMAP.md 以可核查清单跟踪正式插件托管构建, 发布候选, 分 ABI 交付, 兼容性, 安全证据与 GA 后保证. 欢迎通过 Issues 参与讨论.

- [查看 ROADMAP.md](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/ROADMAP.md)

******

### 发行历史

******

# v1.0.2

###### 2026/09/13

* `修复` 插件版本日期固定使用英文, 不随构建机器的语言变化
* `优化` 统一多语言资源, 明确插件激活契约并校验发布产物

# v1.0.1

###### 2026/09/13

* `优化` 统一多语言资源, 明确插件激活契约并校验发布产物

# v6.8.0

###### 2026/09/11

* `优化` 构建阶段校验 64 位原生库的 16 KB 页大小对齐, 检查 manifest 契约并输出 JSON 报告

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


[16 KB page alignment and build verification](https://github.com/SuperMonster003/AutoJs6-Plugin-APK-Builder-Template/blob/master/docs/16kb.md)
