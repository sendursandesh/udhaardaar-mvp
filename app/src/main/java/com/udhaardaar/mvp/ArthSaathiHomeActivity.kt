package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Compatibility entry point: never render the retired V5-style home. */
class ArthSaathiHomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, V62HomeActivity::class.java).apply {
            addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)
        })
        finish()
    }
}
