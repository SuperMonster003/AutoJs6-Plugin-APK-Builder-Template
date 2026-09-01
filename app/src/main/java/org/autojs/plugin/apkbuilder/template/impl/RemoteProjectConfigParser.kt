package org.autojs.plugin.apkbuilder.template.impl

import org.autojs.plugin.apkbuilder.template.ApkBuildRequestExtraKeys
import org.json.JSONArray
import org.json.JSONException
import org.json.JSONObject
import java.io.IOException

internal data class RemoteProjectConfig(
    val json: JSONObject,
    val name: String,
    val packageName: String,
    val versionName: String,
    val versionCode: Int,
    val mainScript: String,
    val abis: List<String>?,
    val libs: List<String>,
    val permissions: List<String>?,
    val signatureScheme: String,
    val splashVisible: Boolean,
)

/** Parses project JSON without allowing coercion or unbounded values into binary editors. */
internal object RemoteProjectConfigParser {

    internal const val MAX_JSON_UTF8_BYTES = 512 * 1024
    internal const val MAX_JSON_NESTING_DEPTH = 64
    internal const val MAX_NAME_UTF8_BYTES = 256
    internal const val MAX_PACKAGE_UTF8_BYTES = 127
    internal const val MAX_VERSION_NAME_UTF8_BYTES = 256
    internal const val MAX_ARRAY_ENTRIES = 512
    internal const val MAX_ARRAY_VALUE_UTF8_BYTES = 255

    private const val DEFAULT_MAIN_SCRIPT = "main.js"
    private const val DEFAULT_SIGNATURE_SCHEME = "V1 + V2"
    private const val KEY_NAME = "name"
    private const val KEY_PACKAGE_NAME = "packageName"
    private const val KEY_VERSION_NAME = "versionName"
    private const val KEY_VERSION_CODE = "versionCode"
    private const val KEY_MAIN = "main"
    private const val KEY_PROJECT_TYPE = "projectType"
    private const val KEY_NODE_CONFIG = "nodeConfig"
    private const val KEY_ABIS = "abis"
    private const val KEY_LIBS = "libs"
    private const val KEY_PERMISSIONS = "permissions"
    private const val KEY_SIGNATURE_SCHEME = "signatureScheme"
    private const val KEY_LAUNCH_CONFIG = "launchConfig"
    private const val KEY_SPLASH_VISIBLE = "splashVisible"
    private const val KEY_BUILD = "build"
    private const val KEY_BUILD_NUMBER = "number"

    private val PACKAGE_NAME_PATTERN = Regex("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z][A-Za-z0-9_]*)+")
    private val SUPPORTED_ABIS = setOf("arm64-v8a", "armeabi-v7a", "x86_64", "x86")
    private val SUPPORTED_SIGNATURE_SCHEMES = setOf("V1", "V2", "V3", "V4")

    fun validateEnvelope(rawJson: String?) {
        val value = rawJson ?: throw IOException("Project config JSON is missing.")
        if (value.toByteArray(Charsets.UTF_8).size > MAX_JSON_UTF8_BYTES) {
            throw IOException("Project config JSON exceeds the length limit.")
        }
        var inString = false
        var escaping = false
        var depth = 0
        var rootClosed = false
        var firstNonWhitespace: Char? = null
        var lastNonWhitespace: Char? = null
        value.forEach { character ->
            if (!character.isWhitespace()) {
                if (rootClosed) {
                    throw IOException("Project config JSON must be one complete object.")
                }
                if (firstNonWhitespace == null) firstNonWhitespace = character
                lastNonWhitespace = character
            }
            if (inString) {
                when {
                    escaping -> escaping = false
                    character == '\\' -> escaping = true
                    character == '"' -> inString = false
                }
                return@forEach
            }
            when (character) {
                '"' -> inString = true
                '{', '[' -> {
                    depth += 1
                    if (depth > MAX_JSON_NESTING_DEPTH) {
                        throw IOException("Project config JSON exceeds the nesting limit.")
                    }
                }
                '}', ']' -> {
                    depth -= 1
                    if (depth < 0) {
                        throw IOException("Project config JSON has unbalanced delimiters.")
                    }
                    if (depth == 0) {
                        rootClosed = true
                    }
                }
            }
        }
        if (firstNonWhitespace != '{' || lastNonWhitespace != '}' || inString || depth != 0) {
            throw IOException("Project config JSON must be one complete object.")
        }
    }

    fun parse(rawJson: String?, sourceKind: String): RemoteProjectConfig {
        validateEnvelope(rawJson)
        try {
            val json = JSONObject(requireNotNull(rawJson))
            if (sourceKind == ApkBuildRequestExtraKeys.SOURCE_KIND_FILE) {
                json.put(KEY_MAIN, DEFAULT_MAIN_SCRIPT)
            }

            val name = requiredString(json, KEY_NAME, "name", MAX_NAME_UTF8_BYTES)
            val packageName = requiredString(
                json,
                KEY_PACKAGE_NAME,
                "packageName",
                MAX_PACKAGE_UTF8_BYTES,
            )
            if (!PACKAGE_NAME_PATTERN.matches(packageName)) {
                throw IOException("Project packageName is invalid.")
            }
            val versionName = requiredString(
                json,
                KEY_VERSION_NAME,
                "versionName",
                MAX_VERSION_NAME_UTF8_BYTES,
            )
            val versionCode = requiredPositiveInt(json, KEY_VERSION_CODE, "versionCode")
            val mainScript = optionalString(json, KEY_MAIN, "main", RemoteZipExtractor.MAX_PATH_UTF8_BYTES)
                ?: DEFAULT_MAIN_SCRIPT
            RemoteZipExtractor.normalizeRelativePath(mainScript, "project main script path")

            val abis = parseStringArray(json, KEY_ABIS)?.also { values ->
                if (values.size > SUPPORTED_ABIS.size || values.any { it !in SUPPORTED_ABIS }) {
                    throw IOException("Project abis contains an unsupported value.")
                }
            }
            val libs = parseStringArray(json, KEY_LIBS).orEmpty()
            RemoteEmbeddedNodePackagingPolicy.validateProjectConfig(
                selectedLibraries = libs,
                projectType = json.opt(KEY_PROJECT_TYPE) as? String,
                hasNodeConfig = json.has(KEY_NODE_CONFIG) && !json.isNull(KEY_NODE_CONFIG),
                mainScript = mainScript,
            )
            val permissions = parseStringArray(json, KEY_PERMISSIONS)
            val signatureScheme = parseSignatureScheme(json)
            val splashVisible = parseSplashVisibility(json)
            validateBuildObject(json)

            return RemoteProjectConfig(
                json = json,
                name = name,
                packageName = packageName,
                versionName = versionName,
                versionCode = versionCode,
                mainScript = mainScript,
                abis = abis,
                libs = libs,
                permissions = permissions,
                signatureScheme = signatureScheme,
                splashVisible = splashVisible,
            )
        } catch (error: IOException) {
            throw error
        } catch (error: JSONException) {
            throw IOException("Project config JSON is malformed.", error)
        } catch (error: RuntimeException) {
            throw IOException("Project config JSON contains an invalid value.", error)
        }
    }

    private fun requiredString(
        json: JSONObject,
        key: String,
        label: String,
        maximumUtf8Bytes: Int,
    ): String {
        return optionalString(json, key, label, maximumUtf8Bytes)
            ?: throw IOException("Project $label is missing.")
    }

    private fun optionalString(
        json: JSONObject,
        key: String,
        label: String,
        maximumUtf8Bytes: Int,
    ): String? {
        if (!json.has(key) || json.isNull(key)) return null
        val value = json.opt(key)
        if (value !is String || value.isBlank()) {
            throw IOException("Project $label must be a non-blank string.")
        }
        validateTextCharacters(value, label)
        if (value.toByteArray(Charsets.UTF_8).size > maximumUtf8Bytes) {
            throw IOException("Project $label exceeds the length limit.")
        }
        return value
    }

    private fun requiredPositiveInt(json: JSONObject, key: String, label: String): Int {
        val value = json.opt(key)
        val number = when (value) {
            is Byte -> value.toLong()
            is Short -> value.toLong()
            is Int -> value.toLong()
            is Long -> value
            else -> null
        }
        if (number == null || number !in 1L..Int.MAX_VALUE.toLong()) {
            throw IOException("Project $label must be a positive 32-bit integer.")
        }
        return number.toInt()
    }

    private fun parseStringArray(json: JSONObject, key: String): List<String>? {
        if (!json.has(key) || json.isNull(key)) return null
        val raw = json.opt(key)
        if (raw !is JSONArray || raw.length() > MAX_ARRAY_ENTRIES) {
            throw IOException("Project $key must be a bounded string array.")
        }
        val result = ArrayList<String>(raw.length())
        for (index in 0 until raw.length()) {
            val value = raw.opt(index)
            if (value !is String || value.isBlank()) {
                throw IOException("Project $key contains a non-string or blank value.")
            }
            validateTextCharacters(value, key)
            if (value.toByteArray(Charsets.UTF_8).size > MAX_ARRAY_VALUE_UTF8_BYTES) {
                throw IOException("Project $key contains an overlong value.")
            }
            result += value
        }
        if (result.distinct().size != result.size) {
            throw IOException("Project $key contains duplicate values.")
        }
        return result
    }

    private fun parseSignatureScheme(json: JSONObject): String {
        val raw = if (!json.has(KEY_SIGNATURE_SCHEME) || json.isNull(KEY_SIGNATURE_SCHEME)) {
            DEFAULT_SIGNATURE_SCHEME
        } else {
            optionalString(
                json,
                KEY_SIGNATURE_SCHEME,
                KEY_SIGNATURE_SCHEME,
                MAX_ARRAY_VALUE_UTF8_BYTES,
            ) ?: DEFAULT_SIGNATURE_SCHEME
        }
        val schemes = raw.split('+').map(String::trim)
        if (
            schemes.isEmpty() ||
            schemes.any { it !in SUPPORTED_SIGNATURE_SCHEMES } ||
            schemes.distinct().size != schemes.size
        ) {
            throw IOException("Project signatureScheme is invalid.")
        }
        return schemes.joinToString(" + ")
    }

    private fun parseSplashVisibility(json: JSONObject): Boolean {
        if (!json.has(KEY_LAUNCH_CONFIG) || json.isNull(KEY_LAUNCH_CONFIG)) return true
        val launchConfig = json.opt(KEY_LAUNCH_CONFIG)
        if (launchConfig !is JSONObject) {
            throw IOException("Project launchConfig must be an object.")
        }
        if (!launchConfig.has(KEY_SPLASH_VISIBLE) || launchConfig.isNull(KEY_SPLASH_VISIBLE)) return true
        val splashVisible = launchConfig.opt(KEY_SPLASH_VISIBLE)
        if (splashVisible !is Boolean) {
            throw IOException("Project splashVisible must be a boolean.")
        }
        return splashVisible
    }

    private fun validateBuildObject(json: JSONObject) {
        if (!json.has(KEY_BUILD) || json.isNull(KEY_BUILD)) return
        val build = json.opt(KEY_BUILD)
        if (build !is JSONObject) {
            throw IOException("Project build must be an object.")
        }
        if (!build.has(KEY_BUILD_NUMBER) || build.isNull(KEY_BUILD_NUMBER)) return
        val value = build.opt(KEY_BUILD_NUMBER)
        val number = when (value) {
            is Byte -> value.toLong()
            is Short -> value.toLong()
            is Int -> value.toLong()
            is Long -> value
            else -> null
        }
        if (number == null || number < 0L || number == Long.MAX_VALUE) {
            throw IOException("Project build number is invalid.")
        }
    }

    private fun validateTextCharacters(value: String, label: String) {
        if (value.any { it.code < 0x20 || it.code in 0x7f..0x9f } || value.hasUnpairedSurrogate()) {
            throw IOException("Project $label contains an invalid character.")
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
}
