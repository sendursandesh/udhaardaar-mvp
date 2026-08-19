package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class V32SafeLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        startActivity(Intent(this, V32Activity::class.java))
        finish()
    }
}
