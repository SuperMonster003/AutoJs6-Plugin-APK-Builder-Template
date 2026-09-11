package org.autojs.plugin.apkbuilder.template.impl

import java.io.BufferedOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream

/** Compressed native entries inherit extractNativeLibs from the Runtime Kit manifest. */
internal object RemoteUnsignedApkWriter {
    private const val BUFFER_SIZE = 256 * 1024

    fun write(buildDir: File, unsignedApk: File, ensureActive: () -> Unit) {
        FileOutputStream(unsignedApk, false).use { output ->
            ZipOutputStream(BufferedOutputStream(output, BUFFER_SIZE)).use { zip ->
                addDirectory(buildDir, buildDir, zip, ensureActive)
                zip.finish()
                zip.flush()
            }
        }
    }

    private fun addDirectory(root: File, dir: File, zip: ZipOutputStream, ensureActive: () -> Unit) {
        dir.listFiles()?.sortedWith(compareBy<File> { !it.isDirectory }.thenBy { it.name })?.forEach { child ->
            ensureActive()
            val path = child.relativeTo(root).path.replace(File.separatorChar, '/')
            if (shouldSkip(path, child.isDirectory)) return@forEach
            if (child.isDirectory) {
                zip.putNextEntry(ZipEntry("$path/"))
                zip.closeEntry()
                addDirectory(root, child, zip, ensureActive)
            } else {
                zip.putNextEntry(ZipEntry(path))
                FileInputStream(child).use { input -> input.copyTo(zip, BUFFER_SIZE) }
                zip.closeEntry()
            }
        }
    }

    private fun shouldSkip(path: String, isDirectory: Boolean): Boolean {
        if (!path.startsWith("META-INF/", ignoreCase = true)) return false
        if (path.startsWith("META-INF/services", ignoreCase = true)) return false
        if (isDirectory) return path.equals("META-INF", true) || path.equals("META-INF/", true)
        return true
    }
}
