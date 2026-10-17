package org.autojs.plugin.apkbuilder.template.impl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.fail
import org.junit.Test
import java.io.IOException
import java.io.StringReader

class RemoteEmbeddedNodePackagingPolicyTest {

    @Test fun bunDirectiveSelectionDoesNotConfuseNodeOrStrings() {
        for (name in listOf("main.bun.js", "main.BUN.ts", "main.bun.tsx")) assertTrue(RemoteEmbeddedNodePackagingPolicy.isBunEntryName(name))
        for (name in listOf("main.mjs", "main.js", "main.ts", "main.bun.d.ts")) assertFalse(RemoteEmbeddedNodePackagingPolicy.isBunEntryName(name))
        for (source in listOf("/* project */ 'bun';\n", "// comment\n\"ui | BUN\";", "'\\u0062un';"))
            assertTrue(source, RemoteEmbeddedNodePackagingPolicy.hasBunExecutionDirective(StringReader(source)))
        for (source in listOf("'node';", "const x='bun';", "'bun' + value;", "'bun'"))
            assertFalse(source, RemoteEmbeddedNodePackagingPolicy.hasBunExecutionDirective(StringReader(source)))
    }

    @Test
    fun onlyRetiredEmbeddedLibraryNamesAreRejected() {
        listOf(
            "Embedded Node.js",
            "embedded-node",
            "embedded-nodejs",
            "embedded_nodejs",
            "  EMBEDDED-NODEJS  ",
        ).forEach { library ->
            assertUnsupported {
                validateConfig(selectedLibraries = listOf(library))
            }
        }
        validateConfig(projectType = " NoDe ")
        validateConfig(hasNodeConfig = true)
        validateConfig(selectedLibraries = listOf("Node.js Runtime", "node", "nodejs", "node.js"))
    }

    @Test
    fun nodeEntriesAreAcceptedWithoutMisclassifyingDeclarations() {
        listOf("main.mjs", "main.cjs", "main.mts", "main.cts", "main.node.js").forEach { name ->
            assertTrue(name, RemoteEmbeddedNodePackagingPolicy.isNodeEntryName(name))
            validateConfig(mainScript = name)
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
        assertTrue(error?.message.orEmpty().contains("Node.js Runtime"))
    }
}
