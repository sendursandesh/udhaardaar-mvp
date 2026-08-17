package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity

class AdvancedLauncherActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_advanced_launcher)
        findViewById<Button>(R.id.btnAdvancedCredit).setOnClickListener {
            startActivity(Intent(this, RegisterCreditV3Activity::class.java))
        }
        findViewById<Button>(R.id.btnAdvancedVerification).setOnClickListener {
            startActivity(Intent(this, VerificationDocumentsActivity::class.java))
        }
        findViewById<Button>(R.id.btnAdvancedDashboard).setOnClickListener {
            startActivity(Intent(this, DashboardV3Activity::class.java))
        }
    }
}
