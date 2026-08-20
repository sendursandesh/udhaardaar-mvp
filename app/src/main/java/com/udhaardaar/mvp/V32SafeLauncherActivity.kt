package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V32SafeLauncherActivity : AppCompatActivity() {
    private lateinit var db: V32DatabaseHelper
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = V32DatabaseHelper(this)
        if (db.hasUser() && getSharedPreferences("udhaardaar_v32_session", MODE_PRIVATE).getBoolean("logged_in", false)) {
            startActivity(Intent(this, V32Activity::class.java)); finish()
        } else {
            startActivity(Intent(this, V32Activity::class.java)); finish()
        }
    }
}
