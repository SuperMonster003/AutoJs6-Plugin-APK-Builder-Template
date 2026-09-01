package org.autojs.plugin.apkbuilder.template

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Test

class ApkBuilderBuildProtocolTest {

    @Test
    fun legacyRemoteVersionAliasesTheFormalBuildProtocol() {
        assertEquals(
            ApkBuilderTemplateProtocol.APK_BUILD_VERSION,
            ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION,
        )
    }

    @Test
    fun formalAndLegacyCapabilityKeysRemainDistinct() {
        assertNotEquals(
            ApkBuilderTemplateCapabilityKeys.SUPPORTS_APK_BUILD,
            ApkBuilderTemplateCapabilityKeys.SUPPORTS_REMOTE_BUILD,
        )
        assertNotEquals(
            ApkBuilderTemplateCapabilityKeys.APK_BUILD_PROTOCOL_VERSION,
            ApkBuilderTemplateCapabilityKeys.REMOTE_BUILD_PROTOCOL_VERSION,
        )
    }

    @Test
    fun formalBuildExecutionIsPinnedToThePluginProcess() {
        assertEquals(
            "on-device-plugin",
            ApkBuilderTemplateProtocol.APK_BUILD_EXECUTION_MODE_ON_DEVICE_PLUGIN,
        )
    }
}
