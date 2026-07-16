APK Builder Template 为 AutoJs6 打包独立应用提供 Runtime Kit.

请安装与 AutoJs6 相同版本的插件. 宿主通过 `org.autojs.plugin.APK_BUILDER` 发现插件, 并读取 `assets/runtime-kit/template.apk`.

内置 Runtime Kit 包含:

- `template.apk`
- `template.apk.sha256`
- `default_key_store.bks`
- `runtime-kit.json`

远程构建默认关闭, 只能在使用 `-Pautojs.apkBuilder.templatePlugin.enableRemoteBuild=true` 构建的插件中启用.
