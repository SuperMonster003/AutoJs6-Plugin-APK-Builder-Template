package org.autojs.plugin.apkbuilder.template.impl

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import java.io.File
import java.io.IOException

/** Prevents compressed image metadata from driving an unbounded bitmap allocation. */
internal object RemoteApkIconPolicy {

    internal const val MAX_FILE_BYTES = 16L * 1024L * 1024L
    internal const val MAX_DIMENSION = 4_096
    internal const val MAX_PIXELS = 4_194_304L

    fun decode(file: File): Bitmap {
        if (!file.isFile || file.length() <= 0L) {
            throw IOException("Remote project icon is missing or empty.")
        }
        if (file.length() > MAX_FILE_BYTES) {
            throw IOException("Remote project icon exceeds the compressed size limit.")
        }
        val bounds = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            inScaled = false
        }
        try {
            BitmapFactory.decodeFile(file.path, bounds)
        } catch (error: RuntimeException) {
            throw IOException("Remote project icon metadata could not be decoded.", error)
        } catch (error: OutOfMemoryError) {
            throw IOException("Remote project icon metadata exceeded the memory limit.", error)
        }
        val width = bounds.outWidth
        val height = bounds.outHeight
        if (
            width <= 0 || height <= 0 ||
            width > MAX_DIMENSION || height > MAX_DIMENSION ||
            width.toLong() * height.toLong() > MAX_PIXELS
        ) {
            throw IOException("Remote project icon dimensions are invalid or exceed the limit.")
        }
        val bitmap = try {
            BitmapFactory.decodeFile(
                file.path,
                BitmapFactory.Options().apply {
                    inPreferredConfig = Bitmap.Config.ARGB_8888
                    inScaled = false
                },
            )
        } catch (error: RuntimeException) {
            throw IOException("Remote project icon could not be decoded.", error)
        } catch (error: OutOfMemoryError) {
            throw IOException("Remote project icon could not be decoded within the memory limit.", error)
        } ?: throw IOException("Remote project icon could not be decoded.")
        if (
            bitmap.width <= 0 || bitmap.height <= 0 ||
            bitmap.width > MAX_DIMENSION || bitmap.height > MAX_DIMENSION ||
            bitmap.width.toLong() * bitmap.height.toLong() > MAX_PIXELS
        ) {
            bitmap.recycle()
            throw IOException("Remote project icon decoded dimensions exceed the limit.")
        }
        return bitmap
    }
}
