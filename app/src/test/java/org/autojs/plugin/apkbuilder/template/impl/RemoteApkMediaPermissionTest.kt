package org.autojs.plugin.apkbuilder.template.impl

import org.junit.Assert.assertEquals
import org.junit.Test
import pxb.android.axml.AxmlReader
import pxb.android.axml.AxmlVisitor
import pxb.android.axml.AxmlWriter
import pxb.android.axml.NodeVisitor
import java.io.ByteArrayOutputStream

class RemoteApkMediaPermissionTest {
    @Test fun microphoneSelectionPreservesChromiumAudioPermissionWithoutGrantingCamera() {
        val microphone="android.permission.RECORD_AUDIO";val settings="android.permission.MODIFY_AUDIO_SETTINGS";val camera="android.permission.CAMERA"
        for ((selected,expected) in listOf(listOf(microphone) to setOf(microphone,settings), emptyList<String>() to emptySet(), listOf(settings) to setOf(settings))) {
            val writer=AxmlWriter();val manifest=writer.child(null,"manifest")
            manifest.attr(null,"package",-1,NodeVisitor.TYPE_STRING,"org.autojs.fixture")
            for(permission in listOf(microphone,settings,camera))manifest.child(null,"uses-permission").apply {
                attr("http://schemas.android.com/apk/res/android","name",android.R.attr.name,NodeVisitor.TYPE_STRING,permission);end()
            }
            manifest.end();writer.end()
            val output=ByteArrayOutputStream();RemoteApkManifestEditor(writer.toByteArray().inputStream()).setPermissions(selected).commit().writeTo(output)
            val actual=mutableSetOf<String>()
            AxmlReader(output.toByteArray()).accept(object:AxmlVisitor(){override fun child(ns:String?,name:String?)=object:NodeVisitor(){
                override fun child(ns:String?,name:String?)=object:NodeVisitor(){override fun attr(ns:String?,attribute:String?,resourceId:Int,type:Int,value:Any?){
                    if(name=="uses-permission"&&attribute=="name")actual.add(value as String)
                }}
            }})
            assertEquals(expected,actual)
        }
    }
}
