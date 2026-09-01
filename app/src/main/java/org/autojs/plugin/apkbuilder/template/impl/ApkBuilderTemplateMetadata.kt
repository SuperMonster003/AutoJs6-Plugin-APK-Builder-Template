package org.autojs.plugin.apkbuilder.template.impl

import android.content.Context
import android.os.Bundle
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateCapabilityKeys
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateInfo
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplatePluginIds
import org.autojs.plugin.apkbuilder.template.ApkBuilderTemplateProtocol
import org.autojs.plugin.common.api.PluginCapabilityKeys
import org.autojs.plugin.common.api.PluginInfo
import org.json.JSONObject

object ApkBuilderTemplateMetadata {

    const val RUNTIME_KIT_JSON_ASSET = "runtime-kit/runtime-kit.json"
    const val TEMPLATE_APK_ASSET = "runtime-kit/template.apk"
    const val DEFAULT_KEY_STORE_ASSET = "runtime-kit/default_key_store.bks"
    private const val TEMPLATE_SHA256_ASSET = "runtime-kit/template.apk.sha256"

    @Volatile
    private var cachedTemplateSizeBytes = -1L

    fun pluginInfo(context: Context): PluginInfo {
        val templateInfo = templateInfo(context)
        return PluginInfo(
            name = context.getString(R.string.plugin_name),
            description = context.getString(R.string.plugin_description),
            instruction = context.readTextRawResourceOrNull(R.raw.plugin_instruction)
                ?: context.getString(R.string.plugin_instruction),
            author = "SuperMonster003",
            collaborators = null,
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE.toLong(),
            versionDate = null,
            id = ApkBuilderTemplatePluginIds.ID,
            engine = ApkBuilderTemplatePluginIds.ENGINE,
            variant = templateInfo.capabilities?.getString(ApkBuilderTemplateCapabilityKeys.TEMPLATE_KIND)
                ?: ApkBuilderTemplatePluginIds.VARIANT_INRT_UNIVERSAL,
            supportedAbis = templateInfo.capabilities?.getStringArray(
                ApkBuilderTemplateCapabilityKeys.TEMPLATE_SUPPORTED_ABIS
            ) ?: emptyArray(),
            capabilities = templateInfo.capabilities,
        )
    }

    fun templateInfo(context: Context): ApkBuilderTemplateInfo {
        val kit = loadRuntimeKitOrNull(context)
        val host = kit?.optJSONObject("host")
        val template = kit?.optJSONObject("template")
        val contract = kit?.optJSONObject("contract")
        val compatibility = kit?.optJSONObject("compatibility")

        val hostVersionName = host.optStringOrDefault("versionName", BuildConfig.HOST_VERSION_NAME)
        val hostVersionCode = host.optLongOrDefault("versionCode", BuildConfig.HOST_VERSION_CODE)
        val protocolVersion = contract.optIntOrDefault("apkBuilderProtocolVersion", BuildConfig.PROTOCOL_VERSION)
        val remoteBuildProtocolVersion = contract.optIntOrDefault(
            "remoteBuildProtocolVersion",
            BuildConfig.REMOTE_BUILD_PROTOCOL_VERSION,
        )
        val templateSha256 = template.optStringOrNull("sha256")
            ?: readTextAssetOrNull(context, TEMPLATE_SHA256_ASSET)?.trim()?.takeIf { it.isNotEmpty() }
        val runtimeApiLevel = contract.optIntOrDefault("runtimeApiLevel", hostVersionCode.toInt())
        val runtimeKitId = kit.optStringOrNull("runtimeKitId")
        val minHostVersionCode = compatibility.optLongOrDefault("minHostVersionCode", hostVersionCode)
        val maxHostVersionCode = compatibility.optLongOrDefault("maxHostVersionCode", hostVersionCode)
        val allowPatchVersionMismatch = compatibility.optBooleanOrDefault("allowPatchVersionMismatch", false)
        val templateVariant = template.optStringOrDefault(
            "variant",
            ApkBuilderTemplatePluginIds.VARIANT_INRT_UNIVERSAL,
        )
        val templateSupportedAbis = template.optStringArray("supportedAbis")

        return ApkBuilderTemplateInfo(
            id = ApkBuilderTemplatePluginIds.ID,
            name = context.getString(R.string.plugin_name),
            description = context.getString(R.string.plugin_description),
            author = "SuperMonster003",
            versionName = BuildConfig.VERSION_NAME,
            versionCode = BuildConfig.VERSION_CODE.toLong(),
            versionDate = null,
            protocolVersion = protocolVersion,
            hostPackageName = host.optStringOrDefault("packageName", BuildConfig.HOST_PACKAGE_NAME),
            hostVersionName = hostVersionName,
            hostVersionCode = hostVersionCode,
            templatePackageName = template.optStringOrDefault("packageName", BuildConfig.TEMPLATE_PACKAGE_NAME),
            templateSha256 = templateSha256,
            templateSizeBytes = templateSizeBytes(context),
            capabilities = Bundle().apply {
                putLong(PluginCapabilityKeys.REQUIRES_HOST_VERSION, minHostVersionCode)
                putString(ApkBuilderTemplateCapabilityKeys.PLUGIN_VERSION_NAME, BuildConfig.PLUGIN_VERSION_NAME)
                putLong(ApkBuilderTemplateCapabilityKeys.PLUGIN_VERSION_CODE, BuildConfig.VERSION_CODE.toLong())
                putInt(ApkBuilderTemplateCapabilityKeys.PLUGIN_VERSION_BUILD, BuildConfig.PLUGIN_VERSION_BUILD)
                putString(ApkBuilderTemplateCapabilityKeys.BUILT_FOR_HOST_VERSION_NAME, hostVersionName)
                putLong(ApkBuilderTemplateCapabilityKeys.BUILT_FOR_HOST_VERSION_CODE, hostVersionCode)
                putLong(ApkBuilderTemplateCapabilityKeys.COMPATIBILITY_MIN_HOST_VERSION_CODE, minHostVersionCode)
                putLong(ApkBuilderTemplateCapabilityKeys.COMPATIBILITY_MAX_HOST_VERSION_CODE, maxHostVersionCode)
                putBoolean(ApkBuilderTemplateCapabilityKeys.ALLOW_PATCH_VERSION_MISMATCH, allowPatchVersionMismatch)
                putInt(ApkBuilderTemplateCapabilityKeys.PROTOCOL_VERSION, protocolVersion)
                putString(ApkBuilderTemplateCapabilityKeys.TEMPLATE_KIND, templateVariant)
                putStringArray(ApkBuilderTemplateCapabilityKeys.TEMPLATE_SUPPORTED_ABIS, templateSupportedAbis)
                putString(ApkBuilderTemplateCapabilityKeys.TEMPLATE_PACKAGE_NAME, template.optStringOrDefault("packageName", BuildConfig.TEMPLATE_PACKAGE_NAME))
                putString(ApkBuilderTemplateCapabilityKeys.TEMPLATE_SHA256, templateSha256)
                putString(ApkBuilderTemplateCapabilityKeys.RUNTIME_KIT_ID, runtimeKitId)
                putInt(ApkBuilderTemplateCapabilityKeys.RUNTIME_API_LEVEL, runtimeApiLevel)
                putString(ApkBuilderTemplateCapabilityKeys.RUNTIME_API_HASH, contract.optStringOrNull("runtimeApiHash"))
                putString(ApkBuilderTemplateCapabilityKeys.SCRIPT_ENGINE_HASH, contract.optStringOrNull("scriptEngineHash"))
                putString(ApkBuilderTemplateCapabilityKeys.RESOURCES_CONTRACT_HASH, contract.optStringOrNull("resourcesContractHash"))
                putString(ApkBuilderTemplateCapabilityKeys.NATIVE_LIB_MANIFEST_HASH, contract.optStringOrNull("nativeLibManifestHash"))
                putBoolean(ApkBuilderTemplateCapabilityKeys.SUPPORTS_TEMPLATE_APK, true)
                putBoolean(ApkBuilderTemplateCapabilityKeys.SUPPORTS_REMOTE_BUILD, BuildConfig.ENABLE_REMOTE_BUILD)
                putInt(ApkBuilderTemplateCapabilityKeys.REMOTE_BUILD_PROTOCOL_VERSION, remoteBuildProtocolVersion)
                putString(
                    ApkBuilderTemplateCapabilityKeys.REMOTE_BUILD_STATUS,
                    if (BuildConfig.ENABLE_REMOTE_BUILD) "experimental" else "disabled",
                )
                putInt(
                    ApkBuilderTemplateCapabilityKeys.REMOTE_BUILD_API_VERSION,
                    ApkBuilderTemplateProtocol.REMOTE_BUILD_VERSION,
                )
            },
        )
    }

    private fun loadRuntimeKitOrNull(context: Context): JSONObject? {
        return runCatching {
            JSONObject(readTextAssetOrNull(context, RUNTIME_KIT_JSON_ASSET).orEmpty())
        }.getOrNull()
    }

    private fun JSONObject?.optStringOrNull(key: String): String? {
        return this?.optString(key)?.takeIf { it.isNotBlank() }
    }

    private fun JSONObject?.optStringOrDefault(key: String, defaultValue: String): String {
        return optStringOrNull(key) ?: defaultValue
    }

    private fun JSONObject?.optLongOrDefault(key: String, defaultValue: Long): Long {
        return this?.optLong(key, defaultValue) ?: defaultValue
    }

    private fun JSONObject?.optIntOrDefault(key: String, defaultValue: Int): Int {
        return this?.optInt(key, defaultValue) ?: defaultValue
    }

    private fun JSONObject?.optBooleanOrDefault(key: String, defaultValue: Boolean): Boolean {
        return this?.optBoolean(key, defaultValue) ?: defaultValue
    }

    private fun JSONObject?.optStringArray(key: String): Array<String> {
        val values = this?.optJSONArray(key) ?: return emptyArray()
        return buildList {
            for (index in 0 until values.length()) {
                values.optString(index).trim().takeIf { it.isNotEmpty() }?.let(::add)
            }
        }.distinct().toTypedArray()
    }

    private fun readTextAssetOrNull(context: Context, assetPath: String): String? {
        return runCatching {
            context.assets.open(assetPath).use { input ->
                input.readBytes().toString(Charsets.UTF_8)
            }
        }.getOrNull()
    }

    private fun Context.readTextRawResourceOrNull(resourceId: Int): String? {
        return runCatching {
            resources.openRawResource(resourceId).use { input ->
                input.readBytes().toString(Charsets.UTF_8).trim()
            }.takeIf { it.isNotEmpty() }
        }.getOrNull()
    }

    private fun templateSizeBytes(context: Context): Long {
        cachedTemplateSizeBytes.takeIf { it >= 0L }?.let { return it }
        return synchronized(this) {
            cachedTemplateSizeBytes.takeIf { it >= 0L } ?: runCatching {
                context.assets.open(TEMPLATE_APK_ASSET).use { input ->
                    val buffer = ByteArray(DEFAULT_BUFFER_SIZE)
                    var total = 0L
                    while (true) {
                        val read = input.read(buffer)
                        if (read < 0) break
                        total += read
                    }
                    total
                }
            }.getOrDefault(0L).also { measured ->
                cachedTemplateSizeBytes = measured
            }
        }
    }
}
