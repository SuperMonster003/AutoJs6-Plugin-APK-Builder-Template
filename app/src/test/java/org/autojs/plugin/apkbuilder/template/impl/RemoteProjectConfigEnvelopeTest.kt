package org.autojs.plugin.apkbuilder.template.impl

import java.io.ByteArrayOutputStream
import java.io.IOException
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import zhao.arsceditor.ResDecoder.IO.LEDataOutputStream

class RemoteProjectConfigEnvelopeTest {

    @Test
    fun completeObjectWithEscapedDelimitersIsAccepted() {
        RemoteProjectConfigParser.validateEnvelope(
            """{"value":"braces: { } [ ] and escaped quote: \\\""}""",
        )
    }

    @Test
    fun nonObjectTrailingAndUnbalancedDocumentsAreRejected() {
        listOf(
            "[]",
            "null",
            "{} true",
            "{}{}",
            "{} {\"second\":true}",
            "{",
            "}",
            "{\"value\":\"unterminated}",
        ).forEach { candidate ->
            assertRejects(candidate)
        }
    }

    @Test
    fun nestingDepthLimitAcceptsBoundaryAndRejectsNextLevel() {
        val acceptedArrayDepth = RemoteProjectConfigParser.MAX_JSON_NESTING_DEPTH - 1
        RemoteProjectConfigParser.validateEnvelope(
            "{" + "[".repeat(acceptedArrayDepth) + "]".repeat(acceptedArrayDepth) + "}",
        )

        val rejectedArrayDepth = RemoteProjectConfigParser.MAX_JSON_NESTING_DEPTH
        assertRejects(
            "{" + "[".repeat(rejectedArrayDepth) + "]".repeat(rejectedArrayDepth) + "}",
            "nesting limit",
        )
    }

    @Test
    fun utf8ByteLimitRejectsOversizedEnvelope() {
        val oversized = "{\"value\":\"" +
            "a".repeat(RemoteProjectConfigParser.MAX_JSON_UTF8_BYTES) +
            "\"}"

        assertRejects(oversized, "length limit")
    }

    @Test
    fun arscFixedPackageSlotRejectsNamesWithoutTerminatorSpace() {
        val output = LEDataOutputStream(ByteArrayOutputStream())
        val error = try {
            output.writeNulEndedString("a".repeat(128))
            null
        } catch (error: IOException) {
            error
        }

        assertTrue("Expected the fixed ARSC package slot to reject 128 characters", error is IOException)
        assertTrue(error?.message.orEmpty().contains("127"))
    }

    @Test
    fun outputNameBudgetIncludesBothDerivedApkSuffixes() {
        val maximumDerivedNameBytes =
            RemoteApkBuildRequestPolicy.MAX_OUTPUT_FILE_NAME_UTF8_BYTES +
                ".apk".toByteArray(Charsets.UTF_8).size +
                ".unsigned.apk".toByteArray(Charsets.UTF_8).size

        assertEquals(255, maximumDerivedNameBytes)
    }

    private fun assertRejects(candidate: String, expectedFragment: String? = null) {
        val error = try {
            RemoteProjectConfigParser.validateEnvelope(candidate)
            null
        } catch (error: IOException) {
            error
        }
        assertTrue("Expected JSON envelope rejection", error is IOException)
        expectedFragment?.let { fragment ->
            assertTrue(
                "Expected '${error?.message}' to contain '$fragment'",
                error?.message.orEmpty().contains(fragment, ignoreCase = true),
            )
        }
    }
}
