package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

/** V6.2 secure entry: approved ArthSaathi gold visual + mobile number + OTP. */
class LoginActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()

    private fun input(h: String) = ArthSaathiV62Design.input(this, h).apply {
        inputType = InputType.TYPE_CLASS_PHONE
        filters = arrayOf(InputFilter.LengthFilter(10))
        imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
    }

    private fun button(s: String, fill: Int, click: () -> Unit) = ArthSaathiV62Design.button(this, s, fill, click)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        if (prefs.getBoolean("logged_in", false)) { openHome(); return }
        showLogin()
    }

    private fun add(r: LinearLayout, v: android.view.View, top: Int = 8) = ArthSaathiV62Design.add(r, v, top)

    private fun shell(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(dp(18), dp(14), dp(18), dp(28))
        setBackgroundColor(ArthSaathiV62Design.BG)
    }

    private fun showLogin() {
        val r = shell()
        add(r, ArthSaathiV62Design.title(this, ArthSaathiV62Design.BRAND, ArthSaathiV62Design.TAGLINE), 0)
        add(r, ArthSaathiV62Design.pageHeader(this, "SECURE ACCESS", "Welcome back", "Enter your registered mobile number and verify with OTP."), 16)
        val m = input("10-digit registered mobile number")
        add(r, m, 14)
        add(r, button("SEND OTP", ArthSaathiV62Design.GOLD_DEEP) {
            val x = m.text.toString()
            if (validMobile(x)) otp("Secure login OTP", x) { go(x) }
            else Toast.makeText(this, "Enter a valid 10-digit Indian mobile number", Toast.LENGTH_LONG).show()
        }, 12)
        add(r, button("CREATE PROFILE / ACCOUNT", ArthSaathiV62Design.TEAL) { showRegister() }, 8)
        add(r, TextView(this).apply {
            text = "Hindi / हिन्दी"
            textSize = 13f
            setTextColor(ArthSaathiV62Design.TEAL)
            gravity = Gravity.CENTER
            setPadding(0, dp(14), 0, dp(14))
            setOnClickListener { language() }
        }, 8)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun showRegister() {
        val r = shell()
        add(r, ArthSaathiV62Design.title(this, ArthSaathiV62Design.BRAND, "Create a verified financial profile"), 0)
        add(r, ArthSaathiV62Design.pageHeader(this, "NEW PROFILE", "Create your account", "One verified profile for your V6.2 financial records."), 14)
        val n = ArthSaathiV62Design.input(this, "Full name")
        val m = input("10-digit mobile number")
        add(r, n, 14); add(r, m)
        add(r, button("VERIFY MOBILE + CREATE ACCOUNT", ArthSaathiV62Design.GOLD_DEEP) {
            val x = m.text.toString()
            if (n.text.trim().length >= 2 && validMobile(x)) otp("Verify mobile", x) {
                prefs.edit().putString("name_$x", n.text.toString().trim()).putBoolean("logged_in", true).putString("current_mobile", x).apply()
                openHome()
            } else Toast.makeText(this, "Enter a valid name and 10-digit mobile number", Toast.LENGTH_LONG).show()
        }, 14)
        add(r, button("BACK TO SIGN IN", ArthSaathiV62Design.NAVY) { showLogin() }, 8)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun validMobile(x: String) = x.matches(Regex("[6-9][0-9]{9}"))

    /** Demo OTP keeps QA deterministic; a production SMS provider remains a replaceable delivery boundary. */
    private fun otp(title: String, mobile: String, done: () -> Unit) {
        val code = (100000 + Random.nextInt(900000)).toString()
        val e = ArthSaathiV62Design.input(this, "Enter 6-digit OTP").apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(6))
        }
        val d = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Demo OTP: $code\nLive SMS delivery can be connected through the OTP provider boundary for production.")
            .setView(e)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("VERIFY", null)
            .create()
        d.setOnShowListener {
            d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (e.text.toString() == code) { d.dismiss(); done() } else e.error = "Incorrect OTP"
            }
        }
        d.show()
    }

    private fun language() {
        AlertDialog.Builder(this).setTitle("Language / भाषा").setItems(arrayOf("English", "हिन्दी")) { _, w ->
            LanguageManager.set(this, if (w == 1) LanguageManager.HI else LanguageManager.EN)
            showLogin()
        }.show()
    }

    private fun go(m: String) { prefs.edit().putBoolean("logged_in", true).putString("current_mobile", m).apply(); openHome() }
    private fun openHome() { startActivity(Intent(this, V62HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP)); finish() }
}
