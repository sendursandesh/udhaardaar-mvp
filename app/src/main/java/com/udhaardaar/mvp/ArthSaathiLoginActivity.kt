package com.udhaardaar.mvp

import android.app.Activity
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast

/**
 * Production-safe login shell. A mobile number alone is never authentication:
 * the user must complete a challenge issued and verified by a configured
 * trusted OTP provider. No local/demo OTP is accepted.
 */
class ArthSaathiLoginActivity : Activity() {
    private val navy = Color.rgb(18, 58, 106)
    private val gold = Color.rgb(201, 138, 10)
    private val muted = Color.rgb(100, 116, 139)
    private var pendingMobile: String? = null
    private var pendingChallenge: String? = null
    private lateinit var mobile: EditText
    private lateinit var otp: EditText
    private lateinit var status: TextView
    private lateinit var sendButton: Button
    private lateinit var verifyButton: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (ArthSaathiSession.isLoggedIn(this)) { openHome(); return }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(28), dp(32), dp(28), dp(28))
            setBackgroundColor(Color.rgb(245, 248, 252))
        }
        root.addView(ImageView(this).apply {
            setImageResource(R.drawable.arthsaathi_logo)
            contentDescription = "ArthSaathi approved journey logo"
            scaleType = ImageView.ScaleType.FIT_CENTER
        }, LinearLayout.LayoutParams(dp(96), dp(96)))
        root.addView(TextView(this).apply {
            text = "ArthSaathi"
            textSize = 29f
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            setTextColor(navy)
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Your Financial-Life Command Centre"
            textSize = 13f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(4), 0, dp(24))
        })
        mobile = EditText(this).apply {
            hint = "10-digit mobile number"
            inputType = InputType.TYPE_CLASS_PHONE
            filters = arrayOf(InputFilter.LengthFilter(10))
            setSingleLine(true)
        }
        root.addView(mobile, LinearLayout.LayoutParams(-1, dp(54)))
        sendButton = Button(this).apply {
            text = "SEND VERIFICATION OTP"
            setOnClickListener { requestLoginOtp() }
        }
        root.addView(sendButton, LinearLayout.LayoutParams(-1, dp(52)))
        otp = EditText(this).apply {
            hint = "Verification code"
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(8))
            setSingleLine(true)
            visibility = android.view.View.GONE
        }
        root.addView(otp, LinearLayout.LayoutParams(-1, dp(54)))
        verifyButton = Button(this).apply {
            text = "VERIFY & SIGN IN"
            visibility = android.view.View.GONE
            setOnClickListener { verifyLoginOtp() }
        }
        root.addView(verifyButton, LinearLayout.LayoutParams(-1, dp(52)))
        status = TextView(this).apply {
            text = "Secure sign-in requires a verified OTP."
            textSize = 12f
            setTextColor(muted)
            setPadding(0, dp(14), 0, 0)
        }
        root.addView(status, LinearLayout.LayoutParams(-1, -2))
        root.addView(TextView(this).apply {
            text = "Your mobile number alone does not sign you in."
            textSize = 11f
            setTextColor(muted)
            gravity = Gravity.CENTER
            setPadding(0, dp(18), 0, 0)
        })
        setContentView(root)
    }

    private fun requestLoginOtp() {
        val value = mobile.text.toString().trim()
        if (!ArthSaathiCoreEngine.validateMobile(value)) {
            mobile.error = "Enter a valid 10-digit mobile number"
            return
        }
        if (!ArthSaathiOtpService.isConfigured()) {
            status.text = "Secure OTP service is not configured. Sign-in is disabled until a trusted server provider is connected."
            Toast.makeText(this, "OTP service unavailable; no sign-in occurred", Toast.LENGTH_LONG).show()
            return
        }
        val challenge = ArthSaathiOtpService.request(value, "LOGIN", "LOGIN")
        if (challenge.isNullOrBlank()) {
            status.text = "The OTP provider could not issue a challenge. Please retry later."
            return
        }
        pendingMobile = value
        pendingChallenge = challenge
        otp.visibility = android.view.View.VISIBLE
        verifyButton.visibility = android.view.View.VISIBLE
        status.text = "Verification challenge issued. Enter the code sent by the configured provider."
        sendButton.isEnabled = false
        mobile.isEnabled = false
    }

    private fun verifyLoginOtp() {
        val challenge = pendingChallenge
        val value = pendingMobile
        val code = otp.text.toString().trim()
        if (challenge.isNullOrBlank() || value.isNullOrBlank()) {
            status.text = "Request a new verification code first."
            return
        }
        if (!code.matches(Regex("[0-9]{4,8}"))) {
            otp.error = "Enter the verification code"
            return
        }
        if (!ArthSaathiOtpService.verify(challenge, code)) {
            status.text = "Code not verified. You remain signed out; request a new code if it expires."
            return
        }
        ArthSaathiSession.login(this, value)
        pendingChallenge = null
        pendingMobile = null
        openHome()
    }

    private fun openHome() {
        startActivity(Intent(this, ArthSaathiHomeActivity::class.java))
        finish()
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
