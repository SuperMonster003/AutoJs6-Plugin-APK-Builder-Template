package org.autojs.plugin.apkbuilder.template.impl

import java.io.File
import java.io.IOException
import java.io.Reader
import java.util.Locale

/**
 * Keeps the retired embedded Node runtime outside every plugin-side APK build path.
 *
 * The host performs the same preflight before opening a session. This second boundary is
 * intentional: older or custom AIDL clients must not be able to revive a partially removed
 * runtime by sending legacy project metadata directly to the plugin.
 */
internal object RemoteEmbeddedNodePackagingPolicy {

    internal const val MAX_EXECUTION_MODE_PROLOGUE_CHARACTERS = 1024 * 1024

    internal const val MIGRATION_MESSAGE =
        "Embedded Node.js APK packaging is unsupported because Node.js is owned by the external " +
            "runtime plugin. Remove the Embedded Node.js library from this packaged project and " +
            "run Node.js scripts from the AutoJs6 host with that runtime plugin installed."

    private val NODE_LIBRARY_NAMES = setOf(
        "embedded node.js",
        "node",
        "nodejs",
        "node.js",
        "embedded-node",
        "embedded-nodejs",
        "embedded_nodejs",
    )

    fun validateProjectConfig(
        selectedLibraries: List<String>,
        projectType: String?,
        hasNodeConfig: Boolean,
        mainScript: String,
    ) {
        if (
            selectedLibraries.any { it.trim().lowercase(Locale.ROOT) in NODE_LIBRARY_NAMES } ||
            projectType?.trim()?.equals("node", ignoreCase = true) == true ||
            hasNodeConfig ||
            isNodeEntryName(mainScript)
        ) {
            throw unsupported()
        }
    }

    fun validateProjectSource(projectConfig: RemoteProjectConfig, sourcePath: File) {
        if (isNodeEntryName(sourcePath.name)) {
            throw unsupported()
        }
        val entry = resolveEntry(projectConfig, sourcePath) ?: return
        if (isNodeEntryName(entry.name)) {
            throw unsupported()
        }
        entry.reader(Charsets.UTF_8).buffered().use { reader ->
            if (hasNodeExecutionDirective(reader)) {
                throw unsupported()
            }
        }
    }

    internal fun isNodeEntryName(fileName: String?): Boolean {
        val name = fileName?.trim()?.lowercase(Locale.ROOT) ?: return false
        if (name.endsWith(".d.ts") || name.endsWith(".d.mts") || name.endsWith(".d.cts")) {
            return false
        }
        return name.endsWith(".mjs") ||
            name.endsWith(".cjs") ||
            name.endsWith(".mts") ||
            name.endsWith(".cts") ||
            name.endsWith(".node.js")
    }

    internal fun hasNodeExecutionDirective(reader: Reader): Boolean {
        return ExecutionModeDirectiveParser(reader).parse()
    }

    private fun resolveEntry(projectConfig: RemoteProjectConfig, sourcePath: File): File? {
        if (sourcePath.isFile) return sourcePath
        if (!sourcePath.isDirectory) return null
        val root = sourcePath.canonicalFile
        val entry = File(root, projectConfig.mainScript).canonicalFile
        val rootPrefix = "${root.path}${File.separator}"
        return entry.takeIf { candidate ->
            (candidate.path == root.path || candidate.path.startsWith(rootPrefix)) && candidate.isFile
        }
    }

    private fun unsupported() = RemoteApkBuildUnsupportedException(MIGRATION_MESSAGE)

    private class ExecutionModeDirectiveParser(private val reader: Reader) {

        private var pushedCharacter = NO_CHARACTER
        private var consumedCharacters = 0

        fun parse(): Boolean {
            val quote = readFirstSignificantCharacter()
            if (quote != SINGLE_QUOTE && quote != DOUBLE_QUOTE) return false

            var nodeMode = false
            var modeToken = StringBuilder(MAX_NODE_MODE_LENGTH)
            var tokenTooLong = false

            fun finishModeToken() {
                if (!tokenTooLong) {
                    val token = modeToken.toString()
                    if (token.equals("node", ignoreCase = true) || token.equals("nodejs", ignoreCase = true)) {
                        nodeMode = true
                    }
                }
                modeToken = StringBuilder(MAX_NODE_MODE_LENGTH)
                tokenTooLong = false
            }

            while (true) {
                val character = readCharacter()
                when {
                    character < 0 -> return false
                    character == quote -> {
                        finishModeToken()
                        return nodeMode && hasDirectiveTerminator()
                    }
                    character == '\n'.code || character == '\r'.code -> return false
                    character == BACKSLASH -> {
                        val decoded = readEscapeSequence() ?: continue
                        decoded.forEach { value ->
                            if (isModeSeparator(value.code)) {
                                finishModeToken()
                            } else if (!tokenTooLong) {
                                if (modeToken.length >= MAX_NODE_MODE_LENGTH) {
                                    tokenTooLong = true
                                } else {
                                    modeToken.append(value.lowercaseChar())
                                }
                            }
                        }
                    }
                    isModeSeparator(character) -> finishModeToken()
                    !tokenTooLong -> {
                        if (modeToken.length >= MAX_NODE_MODE_LENGTH) {
                            tokenTooLong = true
                        } else {
                            modeToken.append(character.toChar().lowercaseChar())
                        }
                    }
                }
            }
        }

        private fun readFirstSignificantCharacter(): Int {
            while (true) {
                val character = readCharacter()
                when {
                    character < 0 -> return character
                    character == BYTE_ORDER_MARK || Character.isWhitespace(character) -> continue
                    character != SLASH -> return character
                }

                val next = readCharacter()
                when (next) {
                    SLASH -> skipLineComment()
                    ASTERISK -> skipBlockComment()
                    else -> {
                        unreadCharacter(next)
                        return character
                    }
                }
            }
        }

        private fun skipLineComment() {
            while (true) {
                when (readCharacter()) {
                    -1, '\n'.code, '\r'.code -> return
                }
            }
        }

        private fun skipBlockComment() {
            var previous = NO_CHARACTER
            while (true) {
                val current = readCharacter()
                if (current < 0) return
                if (previous == ASTERISK && current == SLASH) return
                previous = current
            }
        }

        private fun hasDirectiveTerminator(): Boolean {
            while (true) {
                val character = readCharacter()
                when {
                    character < 0 -> return false
                    character == SEMICOLON || character == '\n'.code || character == '\r'.code -> return true
                    Character.isWhitespace(character) -> continue
                    else -> return false
                }
            }
        }

        private fun readEscapeSequence(): String? {
            return when (val escaped = readCharacter()) {
                -1 -> null
                '\n'.code -> null
                '\r'.code -> {
                    val next = readCharacter()
                    if (next != '\n'.code) unreadCharacter(next)
                    null
                }
                'b'.code -> "\b"
                'f'.code -> "\u000c"
                'n'.code -> "\n"
                'r'.code -> "\r"
                't'.code -> "\t"
                'v'.code -> "\u000b"
                '0'.code -> "\u0000"
                'x'.code -> readFixedHexEscape(2)
                'u'.code -> readUnicodeEscape()
                else -> escaped.toChar().toString()
            }
        }

        private fun readUnicodeEscape(): String? {
            val first = readCharacter()
            if (first == OPEN_BRACE) {
                var value = 0
                var digits = 0
                while (true) {
                    val character = readCharacter()
                    if (character == CLOSE_BRACE) break
                    val digit = Character.digit(character, 16)
                    if (digit < 0 || digits >= 6) return null
                    value = (value shl 4) or digit
                    digits += 1
                }
                if (digits == 0 || !Character.isValidCodePoint(value)) return null
                return String(Character.toChars(value))
            }
            unreadCharacter(first)
            return readFixedHexEscape(4)
        }

        private fun readFixedHexEscape(length: Int): String? {
            var value = 0
            repeat(length) {
                val digit = Character.digit(readCharacter(), 16)
                if (digit < 0) return null
                value = (value shl 4) or digit
            }
            return value.toChar().toString()
        }

        private fun isModeSeparator(character: Int): Boolean {
            return Character.isWhitespace(character) ||
                character == COMMA ||
                character == SEMICOLON ||
                character == PIPE
        }

        private fun readCharacter(): Int {
            if (pushedCharacter != NO_CHARACTER) {
                return pushedCharacter.also { pushedCharacter = NO_CHARACTER }
            }
            if (consumedCharacters >= MAX_EXECUTION_MODE_PROLOGUE_CHARACTERS) {
                throw IOException("Project entry execution-mode prologue exceeds the scan limit.")
            }
            return reader.read().also { character ->
                if (character >= 0) consumedCharacters += 1
            }
        }

        private fun unreadCharacter(character: Int) {
            if (character < 0) return
            check(pushedCharacter == NO_CHARACTER) { "Execution-mode parser pushback overflow." }
            pushedCharacter = character
        }

        private companion object {
            const val NO_CHARACTER = -2
            const val MAX_NODE_MODE_LENGTH = 6
            const val BYTE_ORDER_MARK = 0xfeff
            const val SINGLE_QUOTE = '\''.code
            const val DOUBLE_QUOTE = '"'.code
            const val BACKSLASH = '\\'.code
            const val SLASH = '/'.code
            const val ASTERISK = '*'.code
            const val SEMICOLON = ';'.code
            const val COMMA = ','.code
            const val PIPE = '|'.code
            const val OPEN_BRACE = '{'.code
            const val CLOSE_BRACE = '}'.code
        }
    }
}
