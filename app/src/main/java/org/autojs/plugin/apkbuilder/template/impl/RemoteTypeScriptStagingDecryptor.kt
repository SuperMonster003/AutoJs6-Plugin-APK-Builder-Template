package org.autojs.plugin.apkbuilder.template.impl

import org.autojs.plugin.apkbuilder.template.ApkBuildRequest
import org.autojs.plugin.apkbuilder.template.ApkBuildRequestExtraKeys
import org.autojs.plugin.apkbuilder.template.TypeScriptBuildStagingCipher
import java.io.Closeable
import java.io.File
import java.io.IOException

/** Decrypts build-time TypeScript output only while it is being re-encrypted for the final APK. */
internal class RemoteTypeScriptStagingDecryptor private constructor(
    private val key: ByteArray,
    encryptedPaths: Set<String>,
) : Closeable {

    private val remainingPaths = encryptedPaths.toMutableSet()

    fun decryptIfRequired(source: File, relativePath: String): ByteArray? {
        val path = canonicalPath(relativePath)
        if (path !in remainingPaths) return null
        if (!source.isFile || source.length() !in MIN_ENVELOPE_BYTES..MAX_ENVELOPE_BYTES) {
            throw IOException("Invalid encrypted TypeScript staging entry: $path")
        }
        val envelope = source.readBytes()
        try {
            if (!TypeScriptBuildStagingCipher.isEncrypted(envelope)) {
                throw IOException("TypeScript staging entry is not encrypted: $path")
            }
            val cleartext = try {
                TypeScriptBuildStagingCipher.decrypt(envelope, key)
            } catch (error: Exception) {
                throw IOException("Unable to authenticate TypeScript staging entry: $path", error)
            }
            remainingPaths.remove(path)
            return cleartext
        } finally {
            envelope.fill(0)
        }
    }

    fun requireAllConsumed() {
        if (remainingPaths.isNotEmpty()) {
            throw IOException(
                "Encrypted TypeScript staging entries are missing: " +
                    remainingPaths.sorted().joinToString(", "),
            )
        }
    }

    override fun close() {
        key.fill(0)
        remainingPaths.clear()
    }

    companion object {
        fun from(request: ApkBuildRequest): RemoteTypeScriptStagingDecryptor? {
            val extras = request.extras ?: return null
            val hasVersion = extras.containsKey(
                ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
            )
            val hasKey = extras.containsKey(
                ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY,
            )
            val hasPaths = extras.containsKey(
                ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
            )
            if (!hasVersion && !hasKey && !hasPaths) return null
            if (!hasVersion || !hasKey || !hasPaths) {
                clearRequestKey(extras)
                throw IOException("Incomplete TypeScript staging encryption metadata.")
            }

            val suppliedKey = extras.getByteArray(
                ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY,
            ) ?: throw IOException("TypeScript staging encryption key is missing.")
            val key = suppliedKey.copyOf()
            suppliedKey.fill(0)
            clearRequestKey(extras)
            try {
                val version = extras.getInt(
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION,
                    0,
                )
                if (version != TypeScriptBuildStagingCipher.VERSION) {
                    throw IOException(
                        "Unsupported TypeScript staging encryption version: $version",
                    )
                }
                val suppliedPaths = extras.getStringArrayList(
                    ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS,
                ).orEmpty()
                val paths = suppliedPaths.map(::canonicalPath).toSet()
                if (paths.isEmpty() || paths.size != suppliedPaths.size) {
                    throw IOException("Invalid TypeScript staging encryption path inventory.")
                }
                if (paths.any { path -> !isJavaScriptPath(path) }) {
                    throw IOException(
                        "TypeScript staging encryption may contain JavaScript entries only.",
                    )
                }
                extras.remove(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_VERSION)
                extras.remove(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTED_PATHS)
                return RemoteTypeScriptStagingDecryptor(key, paths)
            } catch (error: Throwable) {
                key.fill(0)
                throw error
            }
        }

        private fun clearRequestKey(extras: android.os.Bundle) {
            extras
                .getByteArray(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY)
                ?.fill(0)
            extras.remove(ApkBuildRequestExtraKeys.TYPESCRIPT_STAGING_ENCRYPTION_KEY)
        }

        private fun canonicalPath(value: String): String {
            val path = value.replace('\\', '/')
            if (
                path.isBlank() || path.startsWith('/') || path.endsWith('/') ||
                path.split('/').any { segment ->
                    segment.isBlank() || segment == "." || segment == ".." ||
                        segment.any { character ->
                            character.code in 0x00..0x1f || character.code in 0x7f..0x9f
                        }
                }
            ) {
                throw IOException("Invalid TypeScript staging path: $value")
            }
            return path
        }

        private fun isJavaScriptPath(path: String): Boolean {
            val lower = path.lowercase()
            return lower.endsWith(".js") ||
                lower.endsWith(".mjs") ||
                lower.endsWith(".cjs")
        }

        private const val MIN_ENVELOPE_BYTES = 36L
        private const val MAX_ENVELOPE_BYTES = 64L * 1024L * 1024L
    }
}
