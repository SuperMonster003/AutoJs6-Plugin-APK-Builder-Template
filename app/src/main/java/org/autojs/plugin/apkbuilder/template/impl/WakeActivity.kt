package org.autojs.plugin.apkbuilder.template.impl

import android.app.Activity
import android.os.Bundle

class WakeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        finish()
    }
}
