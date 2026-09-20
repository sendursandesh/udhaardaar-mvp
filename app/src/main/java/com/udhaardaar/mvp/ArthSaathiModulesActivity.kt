package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Compatibility entry point: route all legacy menu launches into the registered V6.2 Financial Centre. */
class ArthSaathiModulesActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val mode = intent.getStringExtra("mode")
        startActivity(Intent(this, V62ExtendedModulesActivity::class.java).apply {
            mode?.let { putExtra("mode", it) }
        })
        finish()
    }
}
