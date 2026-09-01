package org.autojs.plugin.apkbuilder.template.impl

import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.concurrent.CancellationException
import org.junit.Assert.assertArrayEquals
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class RemoteBoundedStreamCopierTest {

    @Test
    fun exactByteBudgetCopiesCompletely() {
        val source = ByteArray(1_024) { index -> index.toByte() }
        val output = ByteArrayOutputStream()

        val copied = RemoteBoundedStreamCopier.copy(
            input = ByteArrayInputStream(source),
            output = output,
            maximumBytes = source.size.toLong(),
            limitError = "input exceeds limit",
        )

        assertEquals(source.size.toLong(), copied)
        assertArrayEquals(source, output.toByteArray())
    }

    @Test
    fun actualBytesOverBudgetFailBeforeOverflowByteIsWritten() {
        val source = ByteArray(11) { index -> index.toByte() }
        val input = object : ByteArrayInputStream(source) {
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                return super.read(buffer, offset, minOf(length, 5))
            }
        }
        val output = ByteArrayOutputStream()

        val error = captureIOException {
            RemoteBoundedStreamCopier.copy(
                input = input,
                output = output,
                maximumBytes = 10L,
                limitError = "input exceeds limit",
            )
        }

        assertEquals("input exceeds limit", error.message)
        assertEquals(10, output.size())
        assertArrayEquals(source.copyOf(10), output.toByteArray())
    }

    @Test
    fun cancellationCheckRunsBeforeReadingInput() {
        var reads = 0
        val input = object : ByteArrayInputStream(byteArrayOf(1)) {
            override fun read(buffer: ByteArray, offset: Int, length: Int): Int {
                reads += 1
                return super.read(buffer, offset, length)
            }
        }

        val error = runCatching {
            RemoteBoundedStreamCopier.copy(
                input = input,
                output = ByteArrayOutputStream(),
                maximumBytes = 1L,
                limitError = "input exceeds limit",
                checkActive = { throw CancellationException("cancelled") },
            )
        }.exceptionOrNull()

        assertTrue(error is CancellationException)
        assertEquals(0, reads)
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
