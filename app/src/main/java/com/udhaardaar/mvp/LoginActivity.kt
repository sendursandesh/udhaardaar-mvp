package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class LoginActivity : AppCompatActivity() {
    private lateinit var db: V32DatabaseHelper
    private var otp = ""
    private val prefs by lazy { getSharedPreferences("udhaardaar_v32", MODE_PRIVATE) }
    private val sky = Color.rgb(225, 244, 255)
    private val blue = Color.rgb(25, 111, 220)
    private val green = Color.rgb(25, 145, 78)
    private val navy = Color.rgb(24, 58, 92)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(fill: Int, stroke: Int = fill, radius: Int = 18) = GradientDrawable().apply { setColor(fill); setStroke(dp(1), stroke); cornerRadius = dp(radius).toFloat() }
    private fun field(hint: String, max: Int = 0) = EditText(this).apply {
        this.hint = hint; textSize = 16f; setSingleLine(true); setPadding(dp(14), dp(8), dp(14), dp(8)); background = bg(Color.WHITE, Color.rgb(190,210,225), 14)
        if (max > 0) filters = arrayOf(android.text.InputFilter.LengthFilter(max))
        inputType = android.text.InputType.TYPE_CLASS_PHONE
    }
    private fun button(text: String, color: Int, action: () -> Unit) = Button(this).apply {
        this.text = text; isAllCaps = false; textSize = 15f; setTextColor(Color.WHITE); background = bg(color, color, 18); setOnClickListener { action() }
    }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or android.view.WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        db = V32DatabaseHelper(this)
        if (db.hasUser() && prefs.getBoolean("logged_in", false)) {
            startActivity(Intent(this, V323Activity::class.java)); finish(); return
        }
        loginScreen()
    }

    private fun loginScreen() {
        val root = ScrollView(this).apply { setBackgroundColor(sky); isFillViewport = true }
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL; setPadding(dp(20), dp(28), dp(20), dp(32)) }
        val logo = ImageView(this).apply { layoutParams = LinearLayout.LayoutParams(dp(100), dp(100)).apply { gravity = Gravity.CENTER }; setImageResource(R.drawable.udhaardaar_logo); scaleType = ImageView.ScaleType.CENTER_INSIDE; contentDescription = "Udhaardaar logo" }
        body.addView(logo)
        body.addView(TextView(this).apply { text = "Udhaardaar"; textSize = 30f; setTextColor(navy); gravity = Gravity.CENTER; setPadding(0, dp(4), 0, dp(2)) })
        body.addView(TextView(this).apply { text = "Your Credit. Your Trust. Our Record."; textSize = 14f; setTextColor(Color.rgb(0,145,135)); gravity = Gravity.CENTER; setPadding(0,0,0,dp(18)) })
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(18), dp(16), dp(20)); background = bg(Color.WHITE, Color.rgb(185,218,235), 24) }
        card.addView(TextView(this).apply { text = "Secure Login"; textSize = 21f; setTextColor(navy) })
        card.addView(TextView(this).apply { text = "Login with your registered mobile number. Your name is never used as authentication."; textSize = 12f; setTextColor(Color.DKGRAY); setPadding(0,dp(4),0,dp(12)) })
        val mobile = field("Registered mobile number *", 10)
        card.addView(mobile, LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0,dp(3),0,dp(6)) })
        val otpBox = field("Enter 6-digit OTP", 6).apply { visibility = View.GONE; inputType = android.text.InputType.TYPE_CLASS_NUMBER }
        card.addView(otpBox, LinearLayout.LayoutParams(-1, dp(56)).apply { setMargins(0,dp(3),0,dp(6)) })
        card.addView(button("SEND OTP", blue) {
            val entered = mobile.text.toString().trim()
            val user = db.userData()
            if (entered.length != 10) { toast("Enter exactly 10 digits"); return@button }
            if (user == null || user.mobile != entered) { toast("Mobile number is not registered. Please create your profile first."); return@button }
            otp = Random.nextInt(100000, 1000000).toString(); otpBox.visibility = View.VISIBLE
            toast("Demo OTP: $otp")
        }, LinearLayout.LayoutParams(-1, dp(52)).apply { setMargins(0,dp(4),0,dp(6)) })
        card.addView(button("VERIFY & LOGIN", green) {
            if (otp.isBlank() || otpBox.text.toString() != otp) { toast("Enter the correct OTP"); return@button }
            prefs.edit().putBoolean("logged_in", true).apply()
            startActivity(Intent(this, V323Activity::class.java)); finish()
        }, LinearLayout.LayoutParams(-1, dp(52)).apply { setMargins(0,dp(2),0,dp(6)) })
        card.addView(button("NEW USER — CREATE PROFILE", Color.rgb(0,145,135)) {
            startActivity(Intent(this, V323Activity::class.java).putExtra("open_registration", true))
        }, LinearLayout.LayoutParams(-1, dp(52)).apply { setMargins(0,dp(2),0,0) })
        body.addView(card, LinearLayout.LayoutParams(-1, -2))
        body.addView(TextView(this).apply { text = "Privacy first • Consent based access • Digital records"; textSize = 11f; setTextColor(Color.DKGRAY); gravity = Gravity.CENTER; setPadding(0,dp(18),0,0) })
        root.addView(body)
        setContentView(root)
    }
}
