package org.autojs.plugin.apkbuilder.template.impl

import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

/**
 * Extracts host-provided archives only after a complete metadata and path preflight.
 *
 * The byte counters are repeated while streaming because ZIP central-directory sizes are
 * untrusted input. This keeps a forged header from bypassing the preflight limits.
 */
internal object RemoteZipExtractor {

    internal data class Limits(
        val label: String,
        val maxArchiveBytes: Long,
        val maxEntries: Int,
        val maxEntryUncompressedBytes: Long,
        val maxTotalUncompressedBytes: Long,
        val maxCompressionRatio: Long,
        val compressionRatioMinimumBytes: Long,
    ) {
        init {
            require(label.isNotBlank())
            require(maxArchiveBytes > 0L)
            require(maxEntries > 0)
            require(maxEntryUncompressedBytes > 0L)
            require(maxTotalUncompressedBytes >= maxEntryUncompressedBytes)
            require(maxCompressionRatio > 0L)
            require(compressionRatioMinimumBytes > 0L)
        }
    }

    internal data class Entry(
        val normalizedName: String,
        val segments: List<String>,
        val isDirectory: Boolean,
    )

    internal val PROJECT_ARCHIVE_LIMITS = Limits(
        label = "project archive",
        maxArchiveBytes = 512L * MEBIBYTE,
        maxEntries = 16_384,
        maxEntryUncompressedBytes = 256L * MEBIBYTE,
        maxTotalUncompressedBytes = 1L * GIBIBYTE,
        maxCompressionRatio = 250L,
        compressionRatioMinimumBytes = 1L * MEBIBYTE,
    )

    internal val BUILD_INPUT_ARCHIVE_LIMITS = Limits(
        label = "native build input archive",
        maxArchiveBytes = 1L * GIBIBYTE,
        maxEntries = 8_192,
        maxEntryUncompressedBytes = 512L * MEBIBYTE,
        maxTotalUncompressedBytes = 2L * GIBIBYTE,
        maxCompressionRatio = 250L,
        compressionRatioMinimumBytes = 1L * MEBIBYTE,
    )

    fun extract(
        zipFile: File,
        targetDir: File,
        limits: Limits,
        expectedUncompressedBytes: Long? = null,
        checkActive: () -> Unit = {},
        validateEntry: (Entry) -> Unit = {},
    ) {
        checkActive()
        if (!zipFile.isFile || zipFile.length() <= 0L) {
            throw IOException("Remote build ${limits.label} is empty.")
        }
        if (zipFile.length() > limits.maxArchiveBytes) {
            throw IOException(
                "Remote build ${limits.label} compressed size exceeds limit: " +
                    "actual=${zipFile.length()} max=${limits.maxArchiveBytes}",
            )
        }

        ZipFile(zipFile).use { zip ->
            val preflight = preflight(zip, limits, checkActive, validateEntry)
            if (
                expectedUncompressedBytes != null &&
                preflight.declaredUncompressedBytes != expectedUncompressedBytes
            ) {
                throw IOException(
                    "Remote build ${limits.label} uncompressed size mismatch: " +
                        "expected=$expectedUncompressedBytes actual=${preflight.declaredUncompressedBytes}",
                )
            }
            val plans = preflight.plans
            if (!targetDir.exists() && !targetDir.mkdirs()) {
                throw IOException("Failed to create ${limits.label} target dir: ${targetDir.path}")
            }
            if (!targetDir.isDirectory) {
                throw IOException("Remote build ${limits.label} target is not a directory: ${targetDir.path}")
            }

            val canonicalRoot = targetDir.canonicalFile
            var totalWritten = 0L
            plans.forEach { plan ->
                checkActive()
                val outputFile = try {
                    resolveOutput(canonicalRoot, plan.entry)
                } catch (error: IOException) {
                    throw IOException(
                        "Failed to resolve ${limits.label} output at entry #${plan.ordinal}",
                        error,
                    )
                }
                if (plan.entry.isDirectory) {
                    if (outputFile.exists() && !outputFile.isDirectory) {
                        throw IOException(
                            "Remote build ${limits.label} directory conflicts with a file at entry #${plan.ordinal}",
                        )
                    }
                    if (!outputFile.exists() && !outputFile.mkdirs()) {
                        throw IOException(
                            "Failed to create ${limits.label} directory at entry #${plan.ordinal}",
                        )
                    }
                    return@forEach
                }

                if (outputFile.isDirectory) {
                    throw IOException(
                        "Remote build ${limits.label} file conflicts with a directory at entry #${plan.ordinal}",
                    )
                }
                outputFile.parentFile?.let { parent ->
                    if (parent.exists() && !parent.isDirectory) {
                        throw IOException(
                            "Remote build ${limits.label} parent is not a directory at entry #${plan.ordinal}",
                        )
                    }
                    if (!parent.exists() && !parent.mkdirs()) {
                        throw IOException(
                            "Failed to create ${limits.label} parent directory at entry #${plan.ordinal}",
                        )
                    }
                }

                var entryWritten = 0L
                try {
                    zip.getInputStream(plan.zipEntry).use { input ->
                        FileOutputStream(outputFile, false).use { output ->
                            val buffer = ByteArray(BUFFER_SIZE)
                            while (true) {
                                checkActive()
                                val read = input.read(buffer)
                                if (read < 0) break
                                entryWritten = checkedIncrement(
                                    current = entryWritten,
                                    increment = read.toLong(),
                                    maximum = limits.maxEntryUncompressedBytes,
                                    message = "Remote build ${limits.label} entry size exceeds limit at entry #${plan.ordinal}",
                                )
                                totalWritten = checkedIncrement(
                                    current = totalWritten,
                                    increment = read.toLong(),
                                    maximum = limits.maxTotalUncompressedBytes,
                                    message = "Remote build ${limits.label} total extracted size exceeds limit",
                                )
                                checkCompressionRatio(
                                    uncompressedBytes = entryWritten,
                                    compressedBytes = plan.zipEntry.compressedSize,
                                    limits = limits,
                                    scope = "entry #${plan.ordinal}",
                                )
                                checkCompressionRatio(
                                    uncompressedBytes = totalWritten,
                                    compressedBytes = plan.compressedBytesThroughEntry,
                                    limits = limits,
                                    scope = "archive aggregate through entry #${plan.ordinal}",
                                )
                                output.write(buffer, 0, read)
                            }
                            output.flush()
                        }
                    }
                } catch (error: IOException) {
                    if (error.message.orEmpty().startsWith("Remote build ${limits.label} ")) {
                        throw error
                    }
                    throw IOException(
                        "Failed to extract ${limits.label} entry #${plan.ordinal}",
                        error,
                    )
                }
                if (plan.zipEntry.size >= 0L && entryWritten != plan.zipEntry.size) {
                    throw IOException(
                        "Remote build ${limits.label} entry size changed during extraction at entry #${plan.ordinal}",
                    )
                }
            }
            if (expectedUncompressedBytes != null && totalWritten != expectedUncompressedBytes) {
                throw IOException(
                    "Remote build ${limits.label} extracted size mismatch: " +
                        "expected=$expectedUncompressedBytes actual=$totalWritten",
                )
            }
        }
    }

    fun normalizeRelativePath(rawPath: String, label: String): String {
        return normalizePath(
            rawPath = rawPath,
            isDirectory = false,
            label = label,
            isArchiveEntry = false,
        ).normalizedName
    }

    fun validateBuildInputEntry(entry: Entry) {
        when (entry.segments.firstOrNull()) {
            "lib" -> validateNativeLibraryEntry(entry)
            "assets" -> {
                if (!entry.isDirectory && entry.segments.size < 2) {
                    throw IOException("Unexpected remote build input archive entry")
                }
            }
            else -> throw IOException("Unexpected remote build input archive entry")
        }
    }

    private fun preflight(
        zip: ZipFile,
        limits: Limits,
        checkActive: () -> Unit,
        validateEntry: (Entry) -> Unit,
    ): PreflightResult {
        val plans = ArrayList<PlannedEntry>()
        val normalizedNames = HashSet<String>()
        var declaredTotal = 0L
        var declaredCompressedTotal = 0L
        val entries = zip.entries()
        while (entries.hasMoreElements()) {
            checkActive()
            val zipEntry = entries.nextElement()
            if (plans.size >= limits.maxEntries) {
                throw IOException(
                    "Remote build ${limits.label} entry count exceeds limit: max=${limits.maxEntries}",
                )
            }
            val ordinal = plans.size + 1
            val entry = normalizePath(zipEntry.name, zipEntry.isDirectory, limits.label)
            validateEntry(entry)
            if (!normalizedNames.add(entry.normalizedName)) {
                throw IOException("Remote build ${limits.label} contains a duplicate entry at entry #$ordinal")
            }
            if (!entry.isDirectory) {
                val declaredSize = zipEntry.size
                if (declaredSize < 0L) {
                    throw IOException("Remote build ${limits.label} entry #$ordinal has unknown size")
                }
                val declaredCompressedSize = zipEntry.compressedSize
                if (declaredCompressedSize < 0L) {
                    throw IOException("Remote build ${limits.label} entry #$ordinal has unknown compressed size")
                }
                if (declaredSize > limits.maxEntryUncompressedBytes) {
                    throw IOException(
                        "Remote build ${limits.label} entry size exceeds limit: " +
                            "actual=$declaredSize max=${limits.maxEntryUncompressedBytes} entry=#$ordinal",
                    )
                }
                declaredTotal = checkedIncrement(
                    current = declaredTotal,
                    increment = declaredSize,
                    maximum = limits.maxTotalUncompressedBytes,
                    message = "Remote build ${limits.label} total extracted size exceeds limit",
                )
                declaredCompressedTotal = checkedIncrement(
                    current = declaredCompressedTotal,
                    increment = declaredCompressedSize,
                    maximum = Long.MAX_VALUE,
                    message = "Remote build ${limits.label} compressed-size accounting overflow",
                )
                checkCompressionRatio(declaredSize, declaredCompressedSize, limits, "entry #$ordinal")
                checkCompressionRatio(
                    declaredTotal,
                    declaredCompressedTotal,
                    limits,
                    "archive aggregate through entry #$ordinal",
                )
            }
            plans += PlannedEntry(zipEntry, entry, ordinal, declaredCompressedTotal)
        }
        return PreflightResult(plans, declaredTotal)
    }

    private fun normalizePath(
        rawPath: String,
        isDirectory: Boolean,
        label: String,
        isArchiveEntry: Boolean = true,
    ): Entry {
        val normalizedSeparators = rawPath.replace('\\', '/')
        val pathWithoutTrailingSlash = normalizedSeparators.removeSuffix("/")
        val hasUnsafeCharacter = rawPath.any { character ->
            character.code < 0x20 || character.code in 0x7f..0x9f
        }
        val hasWindowsDrivePrefix = WINDOWS_DRIVE_PREFIX.containsMatchIn(normalizedSeparators)
        if (
            normalizedSeparators.isBlank() ||
            normalizedSeparators.startsWith('/') ||
            hasWindowsDrivePrefix ||
            hasUnsafeCharacter ||
            rawPath.hasUnpairedSurrogate() ||
            pathWithoutTrailingSlash.isBlank() ||
            pathWithoutTrailingSlash.toByteArray(Charsets.UTF_8).size > MAX_PATH_UTF8_BYTES
        ) {
            throw unsafePathException(label, isArchiveEntry)
        }
        val segments = pathWithoutTrailingSlash.split('/')
        if (
            segments.size > MAX_PATH_SEGMENTS ||
            segments.any { segment ->
                segment.isEmpty() ||
                    segment == "." ||
                    segment == ".." ||
                    segment.toByteArray(Charsets.UTF_8).size > MAX_SEGMENT_UTF8_BYTES
            }
        ) {
            throw unsafePathException(label, isArchiveEntry)
        }
        return Entry(
            normalizedName = segments.joinToString("/"),
            segments = segments,
            isDirectory = isDirectory,
        )
    }

    private fun validateNativeLibraryEntry(entry: Entry) {
        if (entry.isDirectory) {
            val validDirectory = entry.segments.size == 1 ||
                (entry.segments.size == 2 && entry.segments[1] in SUPPORTED_ABIS)
            if (!validDirectory) {
                throw IOException("Unexpected native library archive entry")
            }
            return
        }
        val validLibrary = entry.segments.size == 3 &&
            entry.segments[1] in SUPPORTED_ABIS &&
            entry.segments[2].length > SHARED_LIBRARY_SUFFIX.length &&
            entry.segments[2].endsWith(SHARED_LIBRARY_SUFFIX)
        if (!validLibrary) {
            throw IOException("Unexpected native library archive entry")
        }
    }

    private fun resolveOutput(root: File, entry: Entry): File {
        val output = File(root, entry.normalizedName).canonicalFile
        if (output.path != root.path && !output.path.startsWith(root.path + File.separator)) {
            throw IOException("Remote build archive entry escapes target directory")
        }
        return output
    }

    private fun unsafePathException(label: String, isArchiveEntry: Boolean): IOException {
        val subject = if (isArchiveEntry) "$label entry path" else label
        return IOException("Unsafe $subject")
    }

    private fun checkedIncrement(
        current: Long,
        increment: Long,
        maximum: Long,
        message: String,
    ): Long {
        if (increment < 0L || current > maximum - increment) {
            throw IOException("$message: max=$maximum")
        }
        return current + increment
    }

    private fun checkCompressionRatio(
        uncompressedBytes: Long,
        compressedBytes: Long,
        limits: Limits,
        scope: String,
    ) {
        if (uncompressedBytes < limits.compressionRatioMinimumBytes) {
            return
        }
        val ratioExceeded = compressedBytes <= 0L ||
            (compressedBytes <= Long.MAX_VALUE / limits.maxCompressionRatio &&
                uncompressedBytes > compressedBytes * limits.maxCompressionRatio)
        if (ratioExceeded) {
            throw IOException(
                "Remote build ${limits.label} compression ratio exceeds limit: " +
                    "max=${limits.maxCompressionRatio}:1 scope=$scope",
            )
        }
    }

    private fun String.hasUnpairedSurrogate(): Boolean {
        var index = 0
        while (index < length) {
            val character = this[index]
            when {
                Character.isHighSurrogate(character) -> {
                    if (index + 1 >= length || !Character.isLowSurrogate(this[index + 1])) {
                        return true
                    }
                    index += 2
                }
                Character.isLowSurrogate(character) -> return true
                else -> index += 1
            }
        }
        return false
    }

    private data class PlannedEntry(
        val zipEntry: ZipEntry,
        val entry: Entry,
        val ordinal: Int,
        val compressedBytesThroughEntry: Long,
    )

    private data class PreflightResult(
        val plans: List<PlannedEntry>,
        val declaredUncompressedBytes: Long,
    )

    private const val BUFFER_SIZE = 256 * 1024
    internal const val MAX_PATH_UTF8_BYTES = 4_096
    internal const val MAX_PATH_SEGMENTS = 128
    internal const val MAX_SEGMENT_UTF8_BYTES = 255
    private const val MEBIBYTE = 1024L * 1024L
    private const val GIBIBYTE = 1024L * MEBIBYTE
    private const val SHARED_LIBRARY_SUFFIX = ".so"
    private val WINDOWS_DRIVE_PREFIX = Regex("^[A-Za-z]:")
    private val SUPPORTED_ABIS = setOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
}
