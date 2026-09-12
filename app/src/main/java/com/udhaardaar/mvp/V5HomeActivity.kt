package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

/** Compatibility route: the V5 home now hands users to the branded ArthSaathi V6 command centre. */
class V5HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, ArthSaathiHomeActivity::class.java))
        finish()
    }
}
