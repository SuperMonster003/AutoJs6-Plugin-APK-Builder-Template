package org.autojs.plugin.apkbuilder.template.impl

import android.os.Bundle
import org.autojs.plugin.apkbuilder.template.ApkBuildRequest
import org.autojs.plugin.apkbuilder.template.ApkBuildRequestExtraKeys
import java.io.IOException

/** Bounded, type-strict validation for values that arrive through the AIDL request. */
internal object RemoteApkBuildRequestPolicy {

    internal const val MAX_KEYSTORE_BYTES = 64L * 1024L * 1024L
    internal const val MAX_OUTPUT_PATH_UTF8_BYTES = 4_096
    internal const val MAX_OUTPUT_FILE_NAME_UTF8_BYTES = 238
    internal const val MAX_PASSWORD_CHARACTERS = 4_096
    internal const val MAX_ALIAS_UTF8_BYTES = 255
    internal const val MAX_TYPESCRIPT_PATHS = 16_384
    internal const val MAX_EXTRAS_KEYS = 16

    private const val EXPECTED_ARCHIVE_FORMAT_VERSION = 1
    private const val TYPESCRIPT_PROTOCOL_VERSION = 3
    private const val TYPESCRIPT_KEY_BYTES = 32
    private const val MAX_HOST_PACKAGE_UTF8_BYTES = 255
    private const val MAX_HOST_VERSION_UTF8_BYTES = 255
    private val ALLOWED_EXTRA_KEYS = setOf(
        ApkBuildRequestExtraKeys.ARCHIVE_FORMAT_VERSION,
        ApkBuildRequestExtraKeys.SOURCE_KIND,
        ApkBuildRequestExtraKeys.SOURCE_PATH,
        ApkBuildRequestExtraKeys.SOURCE_ROOT_PATH,
        ApkBuildRequestExtraKeys.PROJECT_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
        ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
        ApkBuildRequestExtraKeys.ICON_PATH,
        ApkBuildRequestExtraKeys.HOST_OUTPUT_FILE_NAME,
        ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
        ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY,
        ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
    )
    private val SHA256_PATTERN = Regex("[0-9a-fA-F]{64}")

    fun validate(request: ApkBuildRequest) {
        validateBoundedText(
            value = request.hostPackageName,
            label = "host package name",
            maximumUtf8Bytes = MAX_HOST_PACKAGE_UTF8_BYTES,
            allowBlank = true,
        )
        validateBoundedText(
            value = request.hostVersionName,
            label = "host version name",
            maximumUtf8Bytes = MAX_HOST_VERSION_UTF8_BYTES,
            allowBlank = true,
        )
        if (request.hostVersionCode < 0L) {
            throw IOException("Remote build host versionCode must not be negative.")
        }
        if (request.requiredProtocolVersion <= 0) {
            throw IOException("Remote build required protocol version must be positive.")
        }

        validateSizeAndDigest(
            request.projectArchiveSizeBytes,
            request.projectArchiveSha256,
            RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.maxArchiveBytes,
            "project archive",
        )
        if (request.projectArchiveFd == null) {
            throw IOException("Remote build project archive fd is missing.")
        }
        validateSizeAndDigest(
            request.nativeLibrariesArchiveSizeBytes,
            request.nativeLibrariesArchiveSha256,
            RemoteZipExtractor.BUILD_INPUT_ARCHIVE_LIMITS.maxArchiveBytes,
            "native libraries archive",
        )
        if (
            request.nativeLibrariesArchiveFd == null &&
            (request.nativeLibrariesArchiveSizeBytes != 0L || !request.nativeLibrariesArchiveSha256.isNullOrBlank())
        ) {
            throw IOException("Remote build native archive metadata was supplied without an archive fd.")
        }

        validateSizeAndDigest(
            request.keyStoreSizeBytes,
            request.keyStoreSha256,
            MAX_KEYSTORE_BYTES,
            "keystore",
        )
        validateKeyStoreFields(request)

        RemoteProjectConfigParser.validateEnvelope(request.projectConfigJson)
        validateOutputFileName(request.outputFileName)
        validateExtras(request)
    }

    private fun validateKeyStoreFields(request: ApkBuildRequest) {
        if (request.keyStoreFd == null) {
            if (
                request.keyStoreSizeBytes != 0L ||
                !request.keyStoreSha256.isNullOrBlank() ||
                request.keyStorePassword != null ||
                request.keyAlias != null ||
                request.keyAliasPassword != null
            ) {
                throw IOException("Remote build keystore metadata was supplied without a keystore fd.")
            }
            return
        }
        val password = request.keyStorePassword
            ?.takeIf { it.isNotEmpty() }
            ?: throw IOException("Remote build keystore password is missing.")
        if (password.length > MAX_PASSWORD_CHARACTERS) {
            throw IOException("Remote build keystore password exceeds the length limit.")
        }
        val alias = request.keyAlias
            ?.takeIf { it.isNotBlank() }
            ?: throw IOException("Remote build keystore alias is missing.")
        validateBoundedText(alias, "keystore alias", MAX_ALIAS_UTF8_BYTES, allowBlank = false)
        request.keyAliasPassword?.let { aliasPassword ->
            if (aliasPassword.isEmpty()) {
                throw IOException("Remote build key alias password is empty.")
            }
            if (aliasPassword.length > MAX_PASSWORD_CHARACTERS) {
                throw IOException("Remote build key alias password exceeds the length limit.")
            }
        }
    }

    private fun validateOutputFileName(value: String?) {
        value ?: return
        validateBoundedText(
            value = value,
            label = "output file path",
            maximumUtf8Bytes = MAX_OUTPUT_PATH_UTF8_BYTES,
            allowBlank = true,
        )
        val candidate = value.replace('\\', '/').substringAfterLast('/').trim()
        if (candidate.isNotEmpty() && candidate !in setOf(".", "..")) {
            validateBoundedText(
                value = candidate,
                label = "output file name",
                maximumUtf8Bytes = MAX_OUTPUT_FILE_NAME_UTF8_BYTES,
                allowBlank = false,
            )
        }
    }

    private fun validateExtras(request: ApkBuildRequest) {
        val extras = request.extras ?: throw IOException("Remote build request extras are missing.")
        try {
            val keys = extras.keySet()
            if (keys.size > MAX_EXTRAS_KEYS) {
                throw IOException("Remote build request extras exceed the key-count limit.")
            }
            if (keys.any { key -> key !in ALLOWED_EXTRA_KEYS }) {
                throw IOException("Remote build request extras contain an unsupported key.")
            }

            val archiveVersion = extras.value(ApkBuildRequestExtraKeys.ARCHIVE_FORMAT_VERSION)
            if (archiveVersion !is Int || archiveVersion != EXPECTED_ARCHIVE_FORMAT_VERSION) {
                throw IOException("Unsupported project archive format version.")
            }

            val sourceKind = extras.value(ApkBuildRequestExtraKeys.SOURCE_KIND)
            if (
                sourceKind !is String ||
                sourceKind !in setOf(
                    ApkBuildRequestExtraKeys.SOURCE_KIND_DIRECTORY,
                    ApkBuildRequestExtraKeys.SOURCE_KIND_FILE,
                )
            ) {
                throw IOException("Unsupported project source kind.")
            }
            val sourcePath = extras.value(ApkBuildRequestExtraKeys.SOURCE_PATH)
            if (sourcePath !is String) {
                throw IOException("Project source path is missing or has an invalid type.")
            }
            RemoteZipExtractor.normalizeRelativePath(sourcePath, "project source path")

            validateOptionalUncompressedSize(
                extras = extras,
                key = ApkBuildRequestExtraKeys.PROJECT_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
                label = "project archive",
                maximumBytes = RemoteZipExtractor.PROJECT_ARCHIVE_LIMITS.maxTotalUncompressedBytes,
            )
            val nativeUncompressedSize = validateOptionalUncompressedSize(
                extras = extras,
                key = ApkBuildRequestExtraKeys.NATIVE_LIBRARIES_ARCHIVE_UNCOMPRESSED_SIZE_BYTES,
                label = "native build input archive",
                maximumBytes = RemoteZipExtractor.BUILD_INPUT_ARCHIVE_LIMITS.maxTotalUncompressedBytes,
            )
            if (request.nativeLibrariesArchiveFd == null && nativeUncompressedSize != null && nativeUncompressedSize != 0L) {
                throw IOException("Remote build native archive uncompressed size was supplied without an archive fd.")
            }
            if (request.nativeLibrariesArchiveFd != null && nativeUncompressedSize != null && nativeUncompressedSize == 0L) {
                throw IOException("Remote build native archive uncompressed size must be positive when an archive fd is supplied.")
            }

            extras.optionalValue(ApkBuildRequestExtraKeys.ICON_PATH)?.let { iconPath ->
                if (iconPath !is String) {
                    throw IOException("Project icon path has an invalid type.")
                }
                RemoteZipExtractor.normalizeRelativePath(iconPath, "project icon path")
            }

            extras.optionalValue(ApkBuildRequestExtraKeys.SOURCE_ROOT_PATH)?.let { sourceRootPath ->
                if (sourceRootPath !is String) {
                    throw IOException("Project source root path has an invalid type.")
                }
                RemoteZipExtractor.normalizeRelativePath(sourceRootPath, "project source root path")
            }

            extras.optionalValue(ApkBuildRequestExtraKeys.HOST_OUTPUT_FILE_NAME)?.let { outputFileName ->
                if (outputFileName !is String) {
                    throw IOException("Host output file name has an invalid type.")
                }
                validateOutputFileName(outputFileName)
            }

            validateTypeScriptExtras(request, extras)
        } catch (error: IOException) {
            throw error
        } catch (error: RuntimeException) {
            throw IOException("Remote build request extras contain an invalid value type.", error)
        }
    }

    private fun validateTypeScriptExtras(request: ApkBuildRequest, extras: Bundle) {
        val keys = listOf(
            ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
            ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY,
            ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
        )
        val present = keys.map(extras::containsKey)
        if (present.none { it }) return
        if (!present.all { it }) {
            throw IOException("Incomplete TypeScript staging encryption metadata.")
        }
        if (request.requiredProtocolVersion < TYPESCRIPT_PROTOCOL_VERSION) {
            throw IOException("TypeScript staging metadata requires remote build protocol v3.")
        }
        val version = extras.value(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION)
        if (version !is Int || version != 1) {
            throw IOException("Unsupported TypeScript staging encryption version.")
        }
        val key = extras.value(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY)
        if (key !is ByteArray || key.size != TYPESCRIPT_KEY_BYTES) {
            throw IOException("TypeScript staging encryption key has an invalid length.")
        }
        val rawPaths = extras.value(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS)
        if (rawPaths !is ArrayList<*> || rawPaths.isEmpty() || rawPaths.size > MAX_TYPESCRIPT_PATHS) {
            throw IOException("TypeScript staging path inventory has an invalid size or type.")
        }
        val normalized = HashSet<String>(rawPaths.size)
        rawPaths.forEach { rawPath ->
            if (rawPath !is String || !isJavaScriptPath(rawPath)) {
                throw IOException("TypeScript staging path inventory contains an invalid entry.")
            }
            val path = RemoteZipExtractor.normalizeRelativePath(rawPath, "TypeScript staging path")
            if (!normalized.add(path)) {
                throw IOException("TypeScript staging path inventory contains duplicate entries.")
            }
        }
    }

    private fun validateOptionalUncompressedSize(
        extras: Bundle,
        key: String,
        label: String,
        maximumBytes: Long,
    ): Long? {
        val value = extras.optionalValue(key) ?: return null
        if (value !is Long) {
            throw IOException("Remote build $label uncompressed size has an invalid type.")
        }
        if (value < 0L) {
            throw IOException("Remote build $label uncompressed size must not be negative.")
        }
        if (value > maximumBytes) {
            throw IOException("Remote build $label uncompressed size exceeds limit: max=$maximumBytes")
        }
        return value
    }

    private fun validateSizeAndDigest(
        sizeBytes: Long,
        sha256: String?,
        maximumBytes: Long,
        label: String,
    ) {
        if (sizeBytes < 0L) {
            throw IOException("Remote build $label size must not be negative.")
        }
        if (sizeBytes > maximumBytes) {
            throw IOException("Remote build $label size exceeds limit: max=$maximumBytes")
        }
        if (!sha256.isNullOrBlank() && !SHA256_PATTERN.matches(sha256)) {
            throw IOException("Remote build $label SHA-256 has an invalid format.")
        }
    }

    private fun validateBoundedText(
        value: String,
        label: String,
        maximumUtf8Bytes: Int,
        allowBlank: Boolean,
    ) {
        if (!allowBlank && value.isBlank()) {
            throw IOException("Remote build $label is blank.")
        }
        if (value.any { it.code < 0x20 || it.code in 0x7f..0x9f } || value.hasUnpairedSurrogate()) {
            throw IOException("Remote build $label contains a control character.")
        }
        if (value.toByteArray(Charsets.UTF_8).size > maximumUtf8Bytes) {
            throw IOException("Remote build $label exceeds the length limit.")
        }
    }

    private fun isJavaScriptPath(value: String): Boolean {
        val lower = value.lowercase()
        return lower.endsWith(".js") || lower.endsWith(".mjs") || lower.endsWith(".cjs")
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

    @Suppress("DEPRECATION")
    private fun Bundle.value(key: String): Any? = get(key)

    private fun Bundle.optionalValue(key: String): Any? = if (containsKey(key)) value(key) else null
}
