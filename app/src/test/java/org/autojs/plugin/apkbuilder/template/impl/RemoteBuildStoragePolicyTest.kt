package org.autojs.plugin.apkbuilder.template.impl

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException

class RemoteBuildStoragePolicyTest {

    @Test
    fun exactDeclarationsProduceDeterministicPeakEstimate() {
        val estimate = RemoteBuildStoragePolicy.estimate(
            inputs(
                projectArchiveBytes = 10L,
                nativeArchiveBytes = 20L,
                keyStoreBytes = 30L,
                templateArchiveBytes = 100L,
                projectUncompressedBytes = 200L,
                nativePresent = true,
                nativeUncompressedBytes = 300L,
            ),
        )

        assertEquals(400L, estimate.templateExpandedBytes)
        assertEquals(200L, estimate.projectUncompressedBytes)
        assertEquals(300L, estimate.nativeUncompressedBytes)
        assertEquals(
            RemoteBuildStoragePolicy.MINIMUM_FREE_RESERVE_BYTES + 60L + 200L + 3L * 900L,
            estimate.requiredUsableBytes,
        )
        assertFalse(estimate.usedConservativeProjectFallback)
        assertFalse(estimate.usedConservativeNativeFallback)
    }

    @Test
    fun missingProjectDeclarationUsesArchivePolicyMaximum() {
        val estimate = RemoteBuildStoragePolicy.estimate(
            inputs(projectUncompressedBytes = null),
        )

        assertEquals(
            RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.maxTotalUncompressedBytes,
            estimate.projectUncompressedBytes,
        )
        assertTrue(estimate.usedConservativeProjectFallback)
    }

    @Test
    fun missingNativeDeclarationUsesMaximumOnlyWhenArchiveExists() {
        val withNative = RemoteBuildStoragePolicy.estimate(
            inputs(nativePresent = true, nativeUncompressedBytes = null),
        )
        val withoutNative = RemoteBuildStoragePolicy.estimate(
            inputs(nativePresent = false, nativeUncompressedBytes = null),
        )

        assertEquals(
            RemoteZipExtractor.BUILD_INPUT_ARCHIVE_LIMITS.maxTotalUncompressedBytes,
            withNative.nativeUncompressedBytes,
        )
        assertTrue(withNative.usedConservativeNativeFallback)
        assertEquals(0L, withoutNative.nativeUncompressedBytes)
        assertFalse(withoutNative.usedConservativeNativeFallback)
    }

    @Test
    fun arithmeticOverflowSaturatesToLongMaximum() {
        val estimate = RemoteBuildStoragePolicy.estimate(
            inputs(
                projectArchiveBytes = Long.MAX_VALUE,
                templateArchiveBytes = Long.MAX_VALUE,
            ),
        )

        assertEquals(Long.MAX_VALUE, estimate.templateExpandedBytes)
        assertEquals(Long.MAX_VALUE, estimate.requiredUsableBytes)
    }

    @Test
    fun availabilityGateAcceptsBoundaryAndRejectsOneByteShort() {
        val estimate = RemoteBuildStoragePolicy.estimate(inputs())
        RemoteBuildStoragePolicy.requireAvailable(estimate.requiredUsableBytes, estimate)

        try {
            RemoteBuildStoragePolicy.requireAvailable(estimate.requiredUsableBytes - 1L, estimate)
            fail("Expected insufficient-storage rejection")
        } catch (error: IOException) {
            assertTrue(error.message.orEmpty().contains("Insufficient storage space"))
            assertTrue(error.message.orEmpty().contains("Free storage"))
        }
    }

    private fun inputs(
        projectArchiveBytes: Long = 10L,
        nativeArchiveBytes: Long = 0L,
        keyStoreBytes: Long = 0L,
        templateArchiveBytes: Long = 100L,
        projectUncompressedBytes: Long? = 200L,
        nativePresent: Boolean = false,
        nativeUncompressedBytes: Long? = 0L,
    ) = RemoteBuildStoragePolicy.Inputs(
        projectArchiveBytes = projectArchiveBytes,
        nativeArchiveBytes = nativeArchiveBytes,
        keyStoreBytes = keyStoreBytes,
        templateArchiveBytes = templateArchiveBytes,
        declaredProjectUncompressedBytes = projectUncompressedBytes,
        nativeArchivePresent = nativePresent,
        declaredNativeUncompressedBytes = nativeUncompressedBytes,
    )
}
