package com.udhaardaar.mvp

import android.app.Activity
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView

class ArthSaathiHomeActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!ArthSaathiSession.isLoggedIn(this)) {
            startActivity(android.content.Intent(this, ArthSaathiLoginActivity::class.java))
            finish()
            return
        }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,48,32,32) }
        root.addView(TextView(this).apply { text = "ArthSaathi"; textSize = 28f })
        root.addView(TextView(this).apply { text = "Home / Command Centre"; textSize = 18f; setPadding(0,16,0,24) })
        root.addView(TextView(this).apply {
            text = ArthSaathiArchitectureRegistry.modules.filter { it.canonical }
                .joinToString("\n") { module -> "• ${module.title}" }
            textSize = 15f
        })
        setContentView(root)
    }
}
