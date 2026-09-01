package org.autojs.plugin.apkbuilder.template.impl

import android.os.Bundle
import org.autojs.plugin.apkbuilder.template.ApkBuildRequest
import org.autojs.plugin.apkbuilder.template.ApkBuildRequestExtraKeys
import java.io.IOException

/**
 * Computes a conservative upper bound for the plugin-side temporary working set.
 *
 * The host reports the uncompressed input totals from ZIP central-directory metadata. The plugin
 * verifies those totals again before extraction. Older protocol-v3 hosts do not provide the two
 * optional values, so they receive the archive policy maximums instead of an unsafe zero estimate.
 */
internal object RemoteBuildStoragePolicy {

    private const val MEBIBYTE = 1024L * 1024L
    internal const val MINIMUM_FREE_RESERVE_BYTES = 256L * MEBIBYTE
    internal val TEMPLATE_EXPANSION_MULTIPLIER = BuildConfig.REMOTE_BUILD_TEMPLATE_EXPANSION_MULTIPLIER

    internal data class Estimate(
        val projectUncompressedBytes: Long,
        val nativeUncompressedBytes: Long,
        val templateExpandedBytes: Long,
        val requiredUsableBytes: Long,
        val usedConservativeProjectFallback: Boolean,
        val usedConservativeNativeFallback: Boolean,
    )

    internal data class Inputs(
        val projectArchiveBytes: Long,
        val nativeArchiveBytes: Long,
        val keyStoreBytes: Long,
        val templateArchiveBytes: Long,
        val declaredProjectUncompressedBytes: Long?,
        val nativeArchivePresent: Boolean,
        val declaredNativeUncompressedBytes: Long?,
    )

    fun estimate(request: ApkBuildRequest, templateArchiveBytes: Long): Estimate {
        return estimate(
            Inputs(
                projectArchiveBytes = request.projectArchiveSizeBytes,
                nativeArchiveBytes = request.nativeLibrariesArchiveSizeBytes,
                keyStoreBytes = request.keyStoreSizeBytes,
                templateArchiveBytes = templateArchiveBytes,
                declaredProjectUncompressedBytes = declaredUncompressedBytes(
                    request.extras,
                    ApkBuildRequestExtraKeys.PROJECT_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
                ),
                nativeArchivePresent = request.nativeLibrariesArchiveFd != null,
                declaredNativeUncompressedBytes = declaredUncompressedBytes(
                    request.extras,
                    ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
                ),
            ),
        )
    }

    fun estimate(inputs: Inputs): Estimate {
        if (inputs.templateArchiveBytes <= 0L) {
            throw IOException("Remote build template size is unavailable for storage preflight.")
        }
        val declaredProject = inputs.declaredProjectUncompressedBytes
        val declaredNative = inputs.declaredNativeUncompressedBytes
        val projectUncompressed = declaredProject
            ?: RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.maxTotalUncompressedBytes
        val nativeFallback = inputs.nativeArchivePresent && declaredNative == null
        val nativeUncompressed = when {
            !inputs.nativeArchivePresent -> 0L
            declaredNative != null -> declaredNative
            else -> RemoteZipExtractor.BUILD_INPUT_ARCHIVE_LIMITS.maxTotalUncompressedBytes
        }
        val templateExpanded = saturatedMultiply(
            inputs.templateArchiveBytes,
            TEMPLATE_EXPANSION_MULTIPLIER,
        )
        val compressedInputs = saturatedSum(
            inputs.projectArchiveBytes,
            inputs.nativeArchiveBytes,
            inputs.keyStoreBytes,
        )
        val buildTree = saturatedSum(templateExpanded, projectUncompressed, nativeUncompressed)
        val required = saturatedSum(
            MINIMUM_FREE_RESERVE_BYTES,
            compressedInputs,
            projectUncompressed,
            saturatedMultiply(buildTree, 3L),
        )
        return Estimate(
            projectUncompressedBytes = projectUncompressed,
            nativeUncompressedBytes = nativeUncompressed,
            templateExpandedBytes = templateExpanded,
            requiredUsableBytes = required,
            usedConservativeProjectFallback = declaredProject == null,
            usedConservativeNativeFallback = nativeFallback,
        )
    }

    fun requireAvailable(availableBytes: Long, estimate: Estimate) {
        if (availableBytes < estimate.requiredUsableBytes) {
            throw IOException(
                "Insufficient storage space for remote APK build: " +
                    "required=${estimate.requiredUsableBytes} available=${availableBytes}. " +
                    "Free storage or reduce the project/native build inputs.",
            )
        }
    }

    fun declaredProjectUncompressedBytes(extras: Bundle?): Long? = declaredUncompressedBytes(
        extras,
        ApkBuildRequestExtraKeys.PROJECT_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
    )

    fun declaredNativeUncompressedBytes(extras: Bundle?): Long? = declaredUncompressedBytes(
        extras,
        ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
    )

    @Suppress("DEPRECATION")
    private fun declaredUncompressedBytes(extras: Bundle?, key: String): Long? {
        if (extras == null || !extras.containsKey(key)) return null
        return extras.get(key) as? Long
    }

    private fun saturatedSum(vararg values: Long): Long {
        var total = 0L
        values.forEach { value ->
            if (value < 0L || total > Long.MAX_VALUE - value) return Long.MAX_VALUE
            total += value
        }
        return total
    }

    private fun saturatedMultiply(value: Long, multiplier: Long): Long {
        if (value < 0L || multiplier < 0L) return Long.MAX_VALUE
        if (value == 0L || multiplier == 0L) return 0L
        return if (value > Long.MAX_VALUE / multiplier) Long.MAX_VALUE else value * multiplier
    }
}
