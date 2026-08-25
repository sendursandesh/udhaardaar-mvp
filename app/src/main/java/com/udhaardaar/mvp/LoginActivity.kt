package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

/**
 * Secure entry point for Udhaardaar V4.
 * Authentication identity is the registered mobile number, never the profile name.
 * PIN is the fast everyday login; OTP remains the recovery / verification mechanism.
 * Demo builds display the OTP locally. A production release must replace this with an SMS OTP provider.
 */
class LoginActivity : AppCompatActivity() {
    private lateinit var db: V32DatabaseHelper
    private val prefs by lazy { getSharedPreferences("udhaardaar_v32", MODE_PRIVATE) }
    private var generatedOtp = ""
    private var pendingMobile = ""
    private val sky = Color.rgb(225, 244, 255)
    private val blue = Color.rgb(25, 111, 220)
    private val teal = Color.rgb(0, 145, 135)
    private val green = Color.rgb(25, 145, 78)
    private val navy = Color.rgb(24, 58, 92)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        db = V32DatabaseHelper(this)
        // Never auto-enter the application from a name/profile match.
        prefs.edit().putBoolean("logged_in", false).apply()
        loginPage()
    }

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun bg(fill: Int, stroke: Int = fill, radius: Int = 18) = GradientDrawable().apply {
        setColor(fill); setStroke(dp(1), stroke); cornerRadius = dp(radius).toFloat()
    }
    private fun label(text: String, size: Float = 16f, color: Int = navy) = TextView(this).apply {
        this.text = text; textSize = size; setTextColor(color); setPadding(dp(4), dp(5), dp(4), dp(5))
    }
    private fun field(hint: String, max: Int): EditText = EditText(this).apply {
        this.hint = hint; textSize = 17f; setSingleLine(true)
        setPadding(dp(14), dp(8), dp(14), dp(8)); background = bg(Color.WHITE, Color.rgb(190,210,225), 14)
        filters = arrayOf(InputFilter.LengthFilter(max)); inputType = InputType.TYPE_CLASS_PHONE
    }
    private fun button(text: String, color: Int, action: () -> Unit) = Button(this).apply {
        this.text = text; isAllCaps = false; textSize = 15f; setTextColor(Color.WHITE)
        background = bg(color, color, 18); setOnClickListener { action() }
    }
    private fun root() = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(20), dp(26), dp(20), dp(32)); setBackgroundColor(sky)
    }
    private fun logo(size: Int = 96) = ImageView(this).apply {
        layoutParams = LinearLayout.LayoutParams(dp(size), dp(size)).apply { gravity = Gravity.CENTER }
        setImageResource(R.drawable.udhaardaar_logo); scaleType = ImageView.ScaleType.CENTER_INSIDE
    }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()

    private fun loginPage() {
        val r = root()
        r.addView(logo())
        r.addView(label("Udhaardaar", 30f, navy).apply { gravity = Gravity.CENTER })
        r.addView(label("Your Credit. Your Trust. Our Record.", 14f, teal).apply { gravity = Gravity.CENTER })
        r.addView(Space(this).apply { minimumHeight = dp(18) })
        r.addView(label("Secure Login", 22f, navy).apply { gravity = Gravity.CENTER })
        r.addView(label("Login is based only on your registered mobile number.", 13f, Color.DKGRAY).apply { gravity = Gravity.CENTER })

        val mobile = field("Registered mobile number", 10)
        val pin = field("4–6 digit PIN", 6).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        val otp = field("Enter OTP", 6).apply { inputType = InputType.TYPE_CLASS_NUMBER; visibility = android.view.View.GONE }

        r.addView(mobile, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(8), 0, dp(5)) })
        r.addView(pin, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(5), 0, dp(5)) })
        r.addView(otp, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(5), 0, dp(5)) })

        r.addView(button("LOGIN WITH PIN", blue) {
            val m = mobile.text.toString().trim()
            val p = pin.text.toString().trim()
            if (m.length != 10) { toast("Enter the registered 10-digit mobile number"); return@button }
            val registered = db.userData()?.mobile?.filter(Char::isDigit) ?: ""
            if (registered != m) { toast("This mobile number is not registered with this Udhaardaar account"); return@button }
            val savedPin = prefs.getString("login_pin_$m", "") ?: ""
            if (savedPin.isBlank()) { toast("PIN is not set. Use OTP to create your PIN."); return@button }
            if (savedPin != p) { toast("Incorrect PIN"); return@button }
            enterApp(m)
        })
        r.addView(button("USE OTP / FORGOT PIN", teal) {
            val m = mobile.text.toString().trim()
            if (m.length != 10) { toast("Enter the registered 10-digit mobile number first"); return@button }
            val registered = db.userData()?.mobile?.filter(Char::isDigit) ?: ""
            if (registered != m) { toast("This mobile number is not registered"); return@button }
            pendingMobile = m
            generatedOtp = Random.nextInt(100000, 1000000).toString()
            otp.visibility = android.view.View.VISIBLE
            toast("Demo OTP: $generatedOtp")
        })
        r.addView(button("VERIFY OTP + SET / RESET PIN", green) {
            if (otp.visibility != android.view.View.VISIBLE || otp.text.toString() != generatedOtp || generatedOtp.isBlank()) {
                toast("Enter the correct OTP first"); return@button
            }
            val m = pendingMobile
            showSetPin(m)
        })
        r.addView(Space(this).apply { minimumHeight = dp(10) })
        r.addView(button("CREATE NEW ACCOUNT", navy) { createAccountPage() })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun showSetPin(mobile: String) {
        val r = root()
        r.addView(logo(72))
        r.addView(label("Set your Udhaardaar PIN", 22f, navy).apply { gravity = Gravity.CENTER })
        r.addView(label("Use a 4–6 digit PIN for quick future logins.", 13f, Color.DKGRAY).apply { gravity = Gravity.CENTER })
        val p1 = field("New PIN", 6).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        val p2 = field("Confirm PIN", 6).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        r.addView(p1, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(12), 0, dp(5)) })
        r.addView(p2, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(5), 0, dp(8)) })
        r.addView(button("SAVE PIN + LOGIN", green) {
            if (p1.text.length !in 4..6 || p1.text.toString() != p2.text.toString()) { toast("PINs must match and contain 4–6 digits"); return@button }
            prefs.edit().putString("login_pin_$mobile", p1.text.toString()).putBoolean("logged_in", true).apply()
            enterApp(mobile)
        })
        r.addView(button("BACK TO LOGIN", Color.rgb(90,110,125)) { loginPage() })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun createAccountPage() {
        val r = root()
        r.addView(logo(78))
        r.addView(label("Create Udhaardaar Account", 22f, navy).apply { gravity = Gravity.CENTER })
        r.addView(label("Mobile number becomes your secure login identity.", 13f, Color.DKGRAY).apply { gravity = Gravity.CENTER })
        val name = field("Full name *", 80).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS }
        val mobile = field("Mobile number *", 10)
        val address = field("Full address *", 160).apply { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_SENTENCES }
        val pin = field("Create 4–6 digit PIN *", 6).apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_VARIATION_PASSWORD }
        val otp = field("Enter OTP", 6).apply { inputType = InputType.TYPE_CLASS_NUMBER; visibility = android.view.View.GONE }
        listOf(name, mobile, address, pin).forEach { r.addView(it, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(4), 0, dp(4)) }) }
        r.addView(otp, LinearLayout.LayoutParams(-1, dp(58)).apply { setMargins(0, dp(4), 0, dp(4)) })
        r.addView(button("SEND OTP", blue) {
            val m = mobile.text.toString().trim()
            if (name.text.toString().trim().length < 2 || m.length != 10 || address.text.toString().trim().length < 5 || pin.text.length !in 4..6) {
                toast("Name, 10-digit mobile, address and 4–6 digit PIN are required"); return@button
            }
            val existing = db.userData()?.mobile?.filter(Char::isDigit) ?: ""
            if (existing.isNotBlank() && existing == m) { toast("An account already exists for this mobile. Use Login / OTP."); return@button }
            pendingMobile = m
            generatedOtp = Random.nextInt(100000, 1000000).toString()
            otp.visibility = android.view.View.VISIBLE
            toast("Demo OTP: $generatedOtp")
        })
        r.addView(button("VERIFY OTP + CREATE ACCOUNT", green) {
            if (otp.text.toString() != generatedOtp || generatedOtp.isBlank()) { toast("Incorrect OTP"); return@button }
            val m = pendingMobile
            db.saveUser("USR-${System.currentTimeMillis()}", name.text.toString().trim(), m, address.text.toString().trim(), "", null)
            prefs.edit().putString("login_pin_$m", pin.text.toString()).putBoolean("logged_in", true).apply()
            enterApp(m)
        })
        r.addView(button("BACK TO LOGIN", Color.rgb(90,110,125)) { loginPage() })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun enterApp(mobile: String) {
        prefs.edit().putBoolean("logged_in", true).apply()
        startActivity(Intent(this, V323Activity::class.java).apply { putExtra("authenticated_mobile", mobile) })
        finish()
    }
}
