package org.autojs.plugin.apkbuilder.template

object ApkBuilderTemplateProtocol {
    const val TEMPLATE_VERSION = 2
    const val APK_BUILD_VERSION = 3
    const val APK_BUILD_EXECUTION_MODE_ON_DEVICE_PLUGIN = "on-device-plugin"

    /** Legacy name retained for hosts that still treat the on-device plugin process as remote. */
    const val REMOTE_BUILD_VERSION = APK_BUILD_VERSION
    const val KEYSTORE_VERSION = 1
    const val VERSION = TEMPLATE_VERSION
}
