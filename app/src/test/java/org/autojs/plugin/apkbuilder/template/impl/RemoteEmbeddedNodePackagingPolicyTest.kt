package org.autojs.plugin.apkbuilder.template.impl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.io.StringReader

class RemoteEmbeddedNodePackagingPolicyTest {

    @Test
    fun legacyLibraryNamesAndStructuredNodeSignalsAreRejected() {
        listOf(
            "Embedded Node.js",
            "node",
            "nodejs",
            "node.js",
            "embedded-node",
            "embedded-nodejs",
            "embedded_nodejs",
            "  EMBEDDED-NODEJS  ",
        ).forEach { library ->
            assertUnsupported {
                validateConfig(selectedLibraries = listOf(library))
            }
        }
        assertUnsupported { validateConfig(projectType = " NoDe ") }
        assertUnsupported { validateConfig(hasNodeConfig = true) }
    }

    @Test
    fun nodeEntryExtensionsAreRejectedWithoutMisclassifyingDeclarations() {
        listOf("main.mjs", "main.cjs", "main.mts", "main.cts", "main.node.js").forEach { name ->
            assertTrue(name, RemoteEmbeddedNodePackagingPolicy.isNodeEntryName(name))
            assertUnsupported { validateConfig(mainScript = name) }
        }
        listOf("main.js", "main.ts", "types.d.ts", "types.d.mts", "types.d.cts").forEach { name ->
            assertFalse(name, RemoteEmbeddedNodePackagingPolicy.isNodeEntryName(name))
        }
    }

    @Test
    fun executionModeDirectiveRecognizesAliasesSeparatorsCommentsAndEscapes() {
        listOf(
            "\"nodejs\";\nconsole.log('fixture');",
            "// leading comment\n'node';\n",
            "/* leading block */ \"ui | nodejs\";\n",
            "\ufeff\"\\u006eodejs\";\n",
        ).forEach { source ->
            assertTrue(
                source,
                RemoteEmbeddedNodePackagingPolicy.hasNodeExecutionDirective(StringReader(source)),
            )
        }
        listOf(
            "\"ui\";\nconsole.log('nodejs');",
            "\"nodejs\"",
            "const mode = 'nodejs';",
            "\"nodejs\" + value;",
        ).forEach { source ->
            assertFalse(
                source,
                RemoteEmbeddedNodePackagingPolicy.hasNodeExecutionDirective(StringReader(source)),
            )
        }
    }

    @Test
    fun overlongExecutionModePrologueFailsClosed() {
        val source = "/*" +
            "a".repeat(RemoteEmbeddedNodePackagingPolicy.MAX_EXECUTION_MODE_PROLOGUE_CHARACTERS) +
            "*/\n\"nodejs\";"
        val error = try {
            RemoteEmbeddedNodePackagingPolicy.hasNodeExecutionDirective(StringReader(source))
            null
        } catch (error: IOException) {
            error
        }
        assertTrue("Expected the prologue scan limit to reject input", error is IOException)
        assertTrue(error?.message.orEmpty().contains("scan limit"))
    }

    private fun validateConfig(
        selectedLibraries: List<String> = emptyList(),
        projectType: String? = "rhino",
        hasNodeConfig: Boolean = false,
        mainScript: String = "main.js",
    ) {
        RemoteEmbeddedNodePackagingPolicy.validateProjectConfig(
            selectedLibraries = selectedLibraries,
            projectType = projectType,
            hasNodeConfig = hasNodeConfig,
            mainScript = mainScript,
        )
    }

    private fun assertUnsupported(block: () -> Unit) {
        val error = try {
            block()
            null
        } catch (error: RemoteApkBuildUnsupportedException) {
            error
        }
        if (error == null) fail("Expected embedded Node packaging to be unsupported")
        assertTrue(error?.message.orEmpty().contains("external runtime plugin"))
    }
}
