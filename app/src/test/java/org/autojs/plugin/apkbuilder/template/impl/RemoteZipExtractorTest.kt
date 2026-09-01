package org.autojs.plugin.apkbuilder.template.impl

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder

class RemoteZipExtractorTest {

    @get:Rule
    val temporaryFolder = TemporaryFolder()

    @Test
    fun validArchiveExtractsWithinTarget() {
        val archive = createZip(
            "valid.zip",
            linkedMapOf(
                "source.js" to "console.log('ok');".toByteArray(),
                "nested/data.bin" to byteArrayOf(1, 2, 3, 4),
            ),
        )
        val target = temporaryFolder.newFolder("valid-target")

        RemoteZipExtractor.extract(archive, target, testLimits())

        assertEquals("console.log('ok');", File(target, "source.js").readText())
        assertArrayEquals(byteArrayOf(1, 2, 3, 4), File(target, "nested/data.bin").readBytes())
    }

    @Test
    fun matchingDeclaredUncompressedSizeExtracts() {
        val content = "declared size".toByteArray()
        val archive = createZip("declared-size.zip", mapOf("source.js" to content))
        val target = File(temporaryFolder.root, "declared-size-target")

        RemoteZipExtractor.extract(
            archive,
            target,
            testLimits(),
            expectedUncompressedBytes = content.size.toLong(),
        )

        assertArrayEquals(content, File(target, "source.js").readBytes())
    }

    @Test
    fun mismatchedDeclaredUncompressedSizeFailsBeforeTargetCreation() {
        val archive = createZip("declared-size-mismatch.zip", mapOf("source.js" to byteArrayOf(1, 2)))
        val target = File(temporaryFolder.root, "declared-size-mismatch-target")

        assertFailsWithMessage("uncompressed size mismatch") {
            RemoteZipExtractor.extract(
                archive,
                target,
                testLimits(),
                expectedUncompressedBytes = 1L,
            )
        }

        assertFalse(target.exists())
    }

    @Test
    fun relativePathPolicyRejectsPosixWindowsUncTraversalAndAliases() {
        assertEquals(
            "project/main.js",
            RemoteZipExtractor.normalizeRelativePath("project\\main.js", "project source path"),
        )
        listOf(
            "/outside.js",
            "C:/outside.js",
            "C:outside.js",
            "\\\\server\\share\\outside.js",
            "../outside.js",
            "project/../outside.js",
            "project/./main.js",
            "project//main.js",
            "project/\u0000main.js",
        ).forEach { candidate ->
            assertFailsWithMessage("Unsafe project source path") {
                RemoteZipExtractor.normalizeRelativePath(candidate, "project source path")
            }
        }
    }

    @Test
    fun relativePathLimitCountsUtf8BytesRatherThanUtf16Characters() {
        val segment = "é".repeat(120)
        val accepted = List(17) { segment }.joinToString("/")
        assertEquals(RemoteZipExtractor.MAX_PATH_UTF8_BYTES, accepted.toByteArray(Charsets.UTF_8).size)
        assertEquals(accepted, RemoteZipExtractor.normalizeRelativePath(accepted, "project source path"))

        val rejected = "${accepted}é"
        assertFailsWithMessage("Unsafe project source path") {
            RemoteZipExtractor.normalizeRelativePath(rejected, "project source path")
        }
    }

    @Test
    fun relativePathPolicyBoundsSegmentsDepthAndUnicode() {
        val maximumSegment = "a".repeat(RemoteZipExtractor.MAX_SEGMENT_UTF8_BYTES)
        assertEquals(
            maximumSegment,
            RemoteZipExtractor.normalizeRelativePath(maximumSegment, "project source path"),
        )
        val maximumDepth = List(RemoteZipExtractor.MAX_PATH_SEGMENTS) { "a" }.joinToString("/")
        assertEquals(
            maximumDepth,
            RemoteZipExtractor.normalizeRelativePath(maximumDepth, "project source path"),
        )

        listOf(
            "a".repeat(RemoteZipExtractor.MAX_SEGMENT_UTF8_BYTES + 1),
            List(RemoteZipExtractor.MAX_PATH_SEGMENTS + 1) { "a" }.joinToString("/"),
            "project/\u0085main.js",
            "project/\uD800main.js",
        ).forEach { candidate ->
            assertFailsWithMessage("Unsafe project source path") {
                RemoteZipExtractor.normalizeRelativePath(candidate, "project source path")
            }
        }
    }

    @Test
    fun overlongSegmentErrorDoesNotEchoHostControlledName() {
        val sensitiveName = "SEGMENT_SECRET_SENTINEL_" +
            "a".repeat(RemoteZipExtractor.MAX_SEGMENT_UTF8_BYTES)
        val archive = createZip("overlong-segment.zip", mapOf(sensitiveName to byteArrayOf(1)))
        val target = File(temporaryFolder.root, "overlong-segment-target")

        val error = captureIOException {
            RemoteZipExtractor.extract(archive, target, testLimits())
        }

        assertTrue(error.message.orEmpty().contains("Unsafe test archive entry path"))
        assertFalse(error.message.orEmpty().contains("SEGMENT_SECRET_SENTINEL"))
        assertFalse(target.exists())
    }

    @Test
    fun absoluteArchiveEntryFailsBeforeTargetCreation() {
        val archive = createZip("absolute.zip", mapOf("C:\\outside.js" to byteArrayOf(1)))
        val target = File(temporaryFolder.root, "absolute-target")

        assertFailsWithMessage("Unsafe test archive entry path") {
            RemoteZipExtractor.extract(archive, target, testLimits())
        }

        assertFalse(target.exists())
    }

    @Test
    fun entryCountLimitFailsBeforeTargetCreation() {
        val archive = createZip(
            "entry-count.zip",
            linkedMapOf(
                "one" to byteArrayOf(1),
                "two" to byteArrayOf(2),
                "three" to byteArrayOf(3),
            ),
        )
        val target = File(temporaryFolder.root, "entry-count-target")

        assertFailsWithMessage("entry count exceeds limit") {
            RemoteZipExtractor.extract(archive, target, testLimits(maxEntries = 2))
        }

        assertFalse(target.exists())
    }

    @Test
    fun perEntrySizeLimitFailsBeforeTargetCreation() {
        val archive = createZip("entry-size.zip", mapOf("large.bin" to ByteArray(1_025)))
        val target = File(temporaryFolder.root, "entry-size-target")

        assertFailsWithMessage("entry size exceeds limit") {
            RemoteZipExtractor.extract(
                archive,
                target,
                testLimits(maxEntryBytes = 1_024, maxTotalBytes = 2_048),
            )
        }

        assertFalse(target.exists())
    }

    @Test
    fun totalSizeLimitFailsBeforeTargetCreation() {
        val archive = createZip(
            "total-size.zip",
            linkedMapOf(
                "one.bin" to ByteArray(700) { 1 },
                "two.bin" to ByteArray(700) { 2 },
            ),
        )
        val target = File(temporaryFolder.root, "total-size-target")

        assertFailsWithMessage("total extracted size exceeds limit") {
            RemoteZipExtractor.extract(
                archive,
                target,
                testLimits(maxEntryBytes = 1_000, maxTotalBytes = 1_000),
            )
        }

        assertFalse(target.exists())
    }

    @Test
    fun compressionRatioLimitFailsBeforeTargetCreation() {
        val archive = createZip("ratio.zip", mapOf("zeros.bin" to ByteArray(4_096)))
        val target = File(temporaryFolder.root, "ratio-target")

        assertFailsWithMessage("compression ratio exceeds limit") {
            RemoteZipExtractor.extract(
                archive,
                target,
                testLimits(
                    maxEntryBytes = 8_192,
                    maxTotalBytes = 8_192,
                    maxCompressionRatio = 2,
                    compressionRatioMinimumBytes = 100,
                ),
            )
        }

        assertFalse(target.exists())
    }

    @Test
    fun aggregateCompressionRatioCatchesManyIndividuallySmallEntries() {
        val entries = linkedMapOf<String, ByteArray>()
        repeat(20) { index ->
            entries["small-$index.bin"] = ByteArray(100)
        }
        val archive = createZip("aggregate-ratio.zip", entries)
        val target = File(temporaryFolder.root, "aggregate-ratio-target")

        assertFailsWithMessage("compression ratio exceeds limit") {
            RemoteZipExtractor.extract(
                archive,
                target,
                testLimits(
                    maxEntryBytes = 10_000,
                    maxTotalBytes = 20_000,
                    maxCompressionRatio = 2,
                    compressionRatioMinimumBytes = 1_000,
                ),
            )
        }

        assertFalse(target.exists())
    }

    @Test
    fun unsafeEntryErrorDoesNotEchoHostControlledName() {
        val sensitiveName = "C:\\SCRIPT_PLAINTEXT_SENTINEL.js"
        val archive = createZip("redacted-name.zip", mapOf(sensitiveName to byteArrayOf(1)))
        val target = File(temporaryFolder.root, "redacted-name-target")

        val error = captureIOException {
            RemoteZipExtractor.extract(archive, target, testLimits())
        }

        assertTrue(error.message.orEmpty().contains("Unsafe test archive entry path"))
        assertFalse(error.message.orEmpty().contains("SCRIPT_PLAINTEXT_SENTINEL"))
        assertFalse(target.exists())
    }

    @Test
    fun compressedArchiveSizeLimitFailsBeforeTargetCreation() {
        val archive = createZip("archive-size.zip", mapOf("value.bin" to ByteArray(256) { it.toByte() }))
        val target = File(temporaryFolder.root, "archive-size-target")

        assertFailsWithMessage("compressed size exceeds limit") {
            RemoteZipExtractor.extract(
                archive,
                target,
                testLimits(maxArchiveBytes = archive.length() - 1L),
            )
        }

        assertFalse(target.exists())
    }

    @Test
    fun nativeBuildInputAllowsOnlySupportedLibAndAssetsTrees() {
        val validArchive = createZip(
            "native-valid.zip",
            linkedMapOf(
                "lib/x86_64/libfixture.so" to byteArrayOf(1, 2, 3),
                "assets/models/model.bin" to byteArrayOf(4, 5, 6),
            ),
        )
        val validTarget = temporaryFolder.newFolder("native-valid-target")
        RemoteZipExtractor.extract(
            validArchive,
            validTarget,
            testLimits(),
            validateEntry = RemoteZipExtractor::validateBuildInputEntry,
        )
        assertTrue(File(validTarget, "lib/x86_64/libfixture.so").isFile)
        assertTrue(File(validTarget, "assets/models/model.bin").isFile)

        listOf(
            "dex/classes.dex",
            "lib/mips/libfixture.so",
            "lib/x86_64/not-a-library.bin",
            "assets",
        ).forEachIndexed { index, invalidEntry ->
            val archive = createZip("native-invalid-$index.zip", mapOf(invalidEntry to byteArrayOf(1)))
            val target = File(temporaryFolder.root, "native-invalid-target-$index")
            assertFailsWithMessage("Unexpected") {
                RemoteZipExtractor.extract(
                    archive,
                    target,
                    testLimits(),
                    validateEntry = RemoteZipExtractor::validateBuildInputEntry,
                )
            }
            assertFalse(target.exists())
        }
    }

    @Test
    fun productionLimitsAreFiniteAndInternallyOrdered() {
        listOf(
            RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS,
            RemoteZipExtractor.BUILD_INPUT_ARCHIVE_LIMITS,
        ).forEach { limits ->
            assertTrue(limits.maxArchiveBytes > 0L)
            assertTrue(limits.maxEntries > 0)
            assertTrue(limits.maxEntryUncompressedBytes > 0L)
            assertTrue(limits.maxTotalUncompressedBytes >= limits.maxEntryUncompressedBytes)
            assertTrue(limits.maxCompressionRatio > 0L)
            assertTrue(limits.compressionRatioMinimumBytes <= limits.maxEntryUncompressedBytes)
        }
    }

    private fun testLimits(
        maxArchiveBytes: Long = 1L shl 20,
        maxEntries: Int = 100,
        maxEntryBytes: Long = 1L shl 20,
        maxTotalBytes: Long = 2L shl 20,
        maxCompressionRatio: Long = 10_000,
        compressionRatioMinimumBytes: Long = 1L shl 20,
    ) = RemoteZipExtractor.Limits(
        label = "test archive",
        maxArchiveBytes = maxArchiveBytes,
        maxEntries = maxEntries,
        maxEntryUncompressedBytes = maxEntryBytes,
        maxTotalUncompressedBytes = maxTotalBytes,
        maxCompressionRatio = maxCompressionRatio,
        compressionRatioMinimumBytes = compressionRatioMinimumBytes,
    )

    private fun createZip(name: String, entries: Map<String, ByteArray>): File {
        val archive = File(temporaryFolder.root, name)
        ZipOutputStream(FileOutputStream(archive).buffered()).use { zip ->
            entries.forEach { (entryName, content) ->
                zip.putNextEntry(ZipEntry(entryName))
                zip.write(content)
                zip.closeEntry()
            }
        }
        return archive
    }

    private fun assertFailsWithMessage(expectedFragment: String, block: () -> Unit) {
        val error = captureIOException(block)
        assertTrue(
            "Expected '${error.message}' to contain '$expectedFragment'",
            error.message.orEmpty().contains(expectedFragment, ignoreCase = true),
        )
    }

    private fun captureIOException(block: () -> Unit): IOException {
        try {
            block()
        } catch (error: IOException) {
            return error
        }
        throw AssertionError("Expected IOException")
    }
}
