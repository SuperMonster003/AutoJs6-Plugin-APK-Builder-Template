******

### Release History

******

# v6.8.0 Alpha5

###### 2026/07/16

* `Feature` Added the APK Builder Template plugin service with plugin ID `autojs6-apk-builder-template`, engine `apk-builder-template`, and variant `inrt-universal`
* `Feature` Exposed plugin metadata through `org.autojs.plugin.INFO` and served the template APK through `org.autojs.plugin.APK_BUILDER`
* `Feature` Packaged the AutoJs6 Runtime Kit under `assets/runtime-kit/`, including `template.apk`, the default keystore, metadata, and contract files
* `Feature` Added build-time validation for Runtime Kit metadata, SHA-256 digests, and required `template.apk` entries
* `Feature` Reported host version, protocol version, template package name, template SHA-256, Runtime API digests, and remote build capability
* `Feature` Added optional experimental remote build protocol support through `autojs.apkBuilder.templatePlugin.enableRemoteBuild`
* `Feature` Added release flow support for downloading the Runtime Kit, validating assets, signing with the trusted key, and uploading the universal APK
* `Feature` Added localized plugin metadata, usage instructions, README, and CHANGELOG resources for Spanish, French, Russian, Arabic, Japanese, Korean, English, Simplified Chinese, Hong Kong Traditional Chinese, and Taiwan Traditional Chinese
