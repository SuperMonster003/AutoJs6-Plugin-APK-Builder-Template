package org.autojs.plugin.apkbuilder.template.impl

import org.junit.Assert.*
import org.junit.Test
import pxb.android.axml.AxmlReader
import pxb.android.axml.AxmlVisitor
import pxb.android.axml.AxmlWriter
import pxb.android.axml.NodeVisitor
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.zip.ZipEntry
import java.util.zip.ZipFile

class RemoteUnsignedApkWriterTest {
    private val androidNamespace = "http://schemas.android.com/apk/res/android"

    @Test fun nativeEntriesStayCompressedAndManifestExtractionPolicySurvivesRepackaging() {
        for (extract in listOf(true, false)) {
            val temporary = kotlin.io.path.createTempDirectory("apk-native-packaging").toFile()
            try {
                val root = File(temporary, "template").apply { mkdirs() }
                val writer = AxmlWriter()
                writer.ns("android", androidNamespace, 1)
                val manifest = writer.child(null, "manifest")
                manifest.attr(null, "package", -1, NodeVisitor.TYPE_STRING, "org.autojs.fixture")
                val application = manifest.child(null, "application")
                application.attr(androidNamespace, "extractNativeLibs", android.R.attr.extractNativeLibs,
                    NodeVisitor.TYPE_INT_BOOLEAN, extract)
                application.end(); manifest.end(); writer.end()
                val edited = ByteArrayOutputStream()
                RemoteApkManifestEditor(writer.toByteArray().inputStream())
                    .setPackageName("org.autojs.generated").setVersionCode(2).setVersionName("2.0")
                    .commit().writeTo(edited)
                File(root, "AndroidManifest.xml").writeBytes(edited.toByteArray())
                File(root, "lib/arm64-v8a/x.so").apply { requireNotNull(parentFile).mkdirs(); writeBytes(byteArrayOf(0x7f, 69, 76, 70)) }
                File(root, "META-INF/CERT.RSA").apply { requireNotNull(parentFile).mkdirs(); writeText("obsolete signature") }
                File(root, "META-INF/services/example.Service").apply { requireNotNull(parentFile).mkdirs(); writeText("example.Provider") }
                val output = File(temporary, "output.apk")
                RemoteUnsignedApkWriter.write(root, output) { }
                ZipFile(output).use { zip ->
                    val library = zip.getEntry("lib/arm64-v8a/x.so")
                    assertEquals(ZipEntry.DEFLATED, library.method)
                    assertFalse(zip.entries().asSequence().any { it.name.endsWith(".so") && it.method == ZipEntry.STORED })
                    assertNull(zip.getEntry("META-INF/CERT.RSA"))
                    assertNotNull(zip.getEntry("META-INF/services/example.Service"))
                    val bytes = zip.getInputStream(zip.getEntry("AndroidManifest.xml")).use { it.readBytes() }
                    assertArrayEquals(edited.toByteArray(), bytes)
                    var actual: Any? = null
                    val visitor = object : AxmlVisitor() {
                        override fun child(ns: String?, name: String?): NodeVisitor = object : NodeVisitor() {
                            override fun child(ns: String?, name: String?): NodeVisitor = this
                            override fun attr(ns: String?, name: String?, resourceId: Int, type: Int, obj: Any?) {
                                if (ns == androidNamespace && name == "extractNativeLibs") actual = obj
                            }
                        }
                    }
                    AxmlReader(bytes).accept(visitor)
                    assertEquals(extract, actual)
                }
            } finally { temporary.deleteRecursively() }
        }
    }

    /** Targeting API 30+ the package manager maps resources.arsc directly: stored, data on a 4-byte boundary. */
    @Test fun resourceTableIsStoredAndFourByteAligned() {
        // Filler names of different lengths move the local header of resources.arsc across every alignment phase.
        for (fillerLength in 1..4) {
            val temporary = kotlin.io.path.createTempDirectory("apk-arsc-alignment").toFile()
            try {
                val root = File(temporary, "template").apply { mkdirs() }
                File(root, "AndroidManifest.xml").writeBytes(ByteArray(77) { it.toByte() })
                File(root, "a".repeat(fillerLength) + ".bin").writeBytes(ByteArray(13) { 7 })
                val table = ByteArray(1001) { (it * 31).toByte() }
                File(root, "resources.arsc").writeBytes(table)
                val output = File(temporary, "output.apk")
                RemoteUnsignedApkWriter.write(root, output) { }
                ZipFile(output).use { zip ->
                    val entry = zip.getEntry("resources.arsc")
                    assertEquals(ZipEntry.STORED, entry.method)
                    assertEquals(table.size.toLong(), entry.size)
                    assertArrayEquals(table, zip.getInputStream(entry).use { it.readBytes() })
                    assertEquals(ZipEntry.DEFLATED, zip.getEntry("AndroidManifest.xml").method)
                }
                val dataOffset = localDataOffset(output.readBytes(), "resources.arsc")
                assertEquals("filler $fillerLength: data offset $dataOffset", 0L, dataOffset % 4)
            } finally { temporary.deleteRecursively() }
        }
    }

    /** Data offset of [name] from the zip's own headers: central directory -> local header -> name and extra lengths. */
    private fun localDataOffset(bytes: ByteArray, name: String): Long {
        val buffer = java.nio.ByteBuffer.wrap(bytes).order(java.nio.ByteOrder.LITTLE_ENDIAN)
        var eocd = bytes.size - 22
        while (eocd >= 0 && buffer.getInt(eocd) != 0x06054b50) eocd--
        assertTrue("end of central directory", eocd >= 0)
        val entries = buffer.getShort(eocd + 10).toInt() and 0xffff
        var central = buffer.getInt(eocd + 16)
        repeat(entries) {
            assertEquals(0x02014b50, buffer.getInt(central))
            val nameLength = buffer.getShort(central + 28).toInt() and 0xffff
            val extraLength = buffer.getShort(central + 30).toInt() and 0xffff
            val commentLength = buffer.getShort(central + 32).toInt() and 0xffff
            val entryName = String(bytes, central + 46, nameLength, Charsets.UTF_8)
            if (entryName == name) {
                val local = buffer.getInt(central + 42)
                assertEquals(0x04034b50, buffer.getInt(local))
                val localNameLength = buffer.getShort(local + 26).toInt() and 0xffff
                val localExtraLength = buffer.getShort(local + 28).toInt() and 0xffff
                assertTrue("alignment extra field present", localExtraLength >= 6)
                assertEquals(0xD935.toShort(), buffer.getShort(local + 30 + localNameLength))
                return (local + 30 + localNameLength + localExtraLength).toLong()
            }
            central += 46 + nameLength + extraLength + commentLength
        }
        throw AssertionError("Missing entry " + name)
    }
}
