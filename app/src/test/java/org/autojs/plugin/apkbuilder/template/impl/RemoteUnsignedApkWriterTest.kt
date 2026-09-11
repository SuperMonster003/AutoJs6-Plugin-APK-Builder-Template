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
}
