package org.autojs.plugin.apkbuilder.template.impl

import java.io.IOException
import java.io.InputStream
import java.io.OutputStream

/** Copies an untrusted input stream without allowing it to exceed a caller-owned byte budget. */
internal object RemoteBoundedStreamCopier {

    fun copy(
        input: InputStream,
        output: OutputStream,
        maximumBytes: Long,
        limitError: String,
        checkActive: () -> Unit = {},
    ): Long {
        require(maximumBytes >= 0L) { "maximumBytes must not be negative" }
        require(limitError.isNotBlank()) { "limitError must not be blank" }

        val buffer = ByteArray(BUFFER_SIZE)
        var copiedBytes = 0L
        while (true) {
            checkActive()
            val read = input.read(buffer)
            if (read < 0) break
            if (read == 0) continue
            if (copiedBytes > maximumBytes - read.toLong()) {
                throw IOException(limitError)
            }
            output.write(buffer, 0, read)
            copiedBytes += read.toLong()
        }
        return copiedBytes
    }

    private const val BUFFER_SIZE = 256 * 1024
}
