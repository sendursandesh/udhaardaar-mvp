package com.udhaardaar.mvp

import android.app.Activity
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView

class ArthSaathiLoginActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ArthSaathiSession.isLoggedIn(this)) { openHome(); return }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(32,48,32,32) }
        root.addView(TextView(this).apply { text = "ArthSaathi"; textSize = 28f })
        val mobile = EditText(this).apply {
            hint = "10-digit mobile number"
            inputType = InputType.TYPE_CLASS_PHONE
            filters = arrayOf(InputFilter.LengthFilter(10))
        }
        root.addView(mobile)
        root.addView(Button(this).apply {
            text = "CONTINUE"
            setOnClickListener {
                val value = mobile.text.toString()
                if (!value.matches(Regex("[6-9][0-9]{9}"))) mobile.error = "Enter a valid 10-digit mobile number"
                else {
                    ArthSaathiSession.login(this@ArthSaathiLoginActivity, value)
                    openHome()
                }
            }
        })
        setContentView(root)
    }
    private fun openHome() {
        startActivity(android.content.Intent(this, ArthSaathiHomeActivity::class.java)); finish()
    }
}
