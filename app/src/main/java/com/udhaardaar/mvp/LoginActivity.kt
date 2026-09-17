package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.WindowManager
import android.widget.*
import android.graphics.drawable.GradientDrawable
import kotlin.random.Random

/** V6.2 secure entry styled to the supplied ArthSaathi reference. */
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

    private fun shell(): LinearLayout = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        gravity = Gravity.CENTER_HORIZONTAL
        setPadding(dp(20), dp(18), dp(20), dp(26))
        background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(0xfffff4cf.toInt(), ArthSaathiV62Design.BG, 0xfffffdf7.toInt()))
    }

    private fun logoBlock(r: LinearLayout) {
        val logo = ImageView(this).apply {
            setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "ArthSaathi logo"
        }
        r.addView(logo, LinearLayout.LayoutParams(dp(108), dp(108)).apply { topMargin = dp(2) })
        r.addView(ArthSaathiV62Design.brandWordmark(this, 28f), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(0) })
        r.addView(ArthSaathiV62Design.text(this, ArthSaathiV62Design.TAGLINE, 10f, ArthSaathiV62Design.NAVY), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
    }

    private fun modeTabs(r: LinearLayout) {
        val tabs = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            background = ArthSaathiV62Design.card(0xfffff7df.toInt(), 14, d)
            setPadding(dp(3), dp(3), dp(3), dp(3))
        }
        tabs.addView(button("Login", ArthSaathiV62Design.GOLD_DEEP) { showLogin() }, LinearLayout.LayoutParams(0, dp(45), 1f).apply { rightMargin = dp(2) })
        tabs.addView(button("Sign Up", ArthSaathiV62Design.PALE_GOLD) { showRegister() }, LinearLayout.LayoutParams(0, dp(45), 1f).apply { leftMargin = dp(2) })
        r.addView(tabs, LinearLayout.LayoutParams(-1, dp(51)).apply { topMargin = dp(14) })
    }

    private fun showLogin() {
        val r = shell()
        logoBlock(r)
        modeTabs(r)
        val m = input("Enter mobile number")
        r.addView(m, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        r.addView(button("Send OTP", ArthSaathiV62Design.GOLD_DEEP) {
            val x = m.text.toString()
            if (validMobile(x)) otp("Secure login OTP", x) { go(x) }
            else Toast.makeText(this, "Enter a valid 10-digit Indian mobile number", Toast.LENGTH_LONG).show()
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        r.addView(ArthSaathiV62Design.text(this, "or", 11f, ArthSaathiV62Design.MUTED).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        r.addView(button("G  Continue with Google", ArthSaathiV62Design.WHITE) {
            Toast.makeText(this, "Google sign-in will use the configured identity provider.", Toast.LENGTH_SHORT).show()
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })
        r.addView(ArthSaathiV62Design.text(this, "By continuing, you agree to our\nTerms & Privacy Policy", 9f, ArthSaathiV62Design.MUTED).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        r.addView(ArthSaathiV62Design.text(this, "Hindi / हिन्दी", 12f, ArthSaathiV62Design.TEAL).apply { gravity = Gravity.CENTER; setOnClickListener { language() } }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(12) })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun showRegister() {
        val r = shell()
        logoBlock(r)
        modeTabs(r)
        r.addView(ArthSaathiV62Design.text(this, "Create your profile", 17f, ArthSaathiV62Design.NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(14) })
        val n = ArthSaathiV62Design.input(this, "Full name")
        val m = input("Enter mobile number")
        r.addView(n, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        r.addView(m, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
        r.addView(button("Verify Mobile + Create Account", ArthSaathiV62Design.GOLD_DEEP) {
            val x = m.text.toString()
            if (n.text.trim().length >= 2 && validMobile(x)) otp("Verify mobile", x) {
                prefs.edit().putString("name_$x", n.text.toString().trim()).putBoolean("logged_in", true).putString("current_mobile", x).apply()
                openHome()
            } else Toast.makeText(this, "Enter a valid name and 10-digit mobile number", Toast.LENGTH_LONG).show()
        }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(10) })
        r.addView(button("Back to Sign In", ArthSaathiV62Design.NAVY) { showLogin() }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun validMobile(x: String) = x.matches(Regex("[6-9][0-9]{9}"))

    private fun otp(title: String, mobile: String, done: () -> Unit) {
        val code = (100000 + Random.nextInt(900000)).toString()
        val e = ArthSaathiV62Design.input(this, "Enter 6-digit OTP").apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            filters = arrayOf(InputFilter.LengthFilter(6))
        }
        val dialog = AlertDialog.Builder(this)
            .setTitle(title)
            .setMessage("Demo OTP: $code\nLive SMS delivery can be connected through the OTP provider boundary for production.")
            .setView(e)
            .setNegativeButton("CANCEL", null)
            .setPositiveButton("VERIFY", null)
            .create()
        dialog.setOnShowListener {
            dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (e.text.toString() == code) { dialog.dismiss(); done() } else e.error = "Incorrect OTP"
            }
        }
        dialog.show()
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
