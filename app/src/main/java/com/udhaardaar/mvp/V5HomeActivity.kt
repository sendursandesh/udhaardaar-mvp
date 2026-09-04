package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/** Clean V5 command centre. Keeps every existing module reachable without turning the home screen into a wall of buttons. */
class V5HomeActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val bg = Color.rgb(246, 248, 251)
    private val navy = Color.rgb(25, 43, 65)
    private val blue = Color.rgb(38, 99, 235)
    private val teal = Color.rgb(0, 145, 135)
    private val green = Color.rgb(22, 137, 75)
    private val red = Color.rgb(190, 65, 65)
    private val amber = Color.rgb(188, 122, 18)
    private val border = Color.rgb(220, 227, 235)
    private val muted = Color.rgb(94, 109, 124)

    private fun dp(v: Int) = (v * resources.displayMetrics.density).toInt()
    private fun rounded(fill: Int, stroke: Int = fill, radius: Int = 14) = GradientDrawable().apply { setColor(fill); setStroke(dp(1), stroke); cornerRadius = dp(radius).toFloat() }
    private fun text(s: String, size: Float, color: Int = navy, bold: Boolean = false) = TextView(this).apply { text = s; textSize = size; setTextColor(color); typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL) }

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); home() }
    @Suppress("DEPRECATION") override fun onBackPressed() { home() }

    private fun home() {
        if (!prefs.getBoolean("logged_in", false)) { startActivity(Intent(this, LoginActivity::class.java)); finish(); return }
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setBackgroundColor(bg); setPadding(dp(16), dp(12), dp(16), dp(12)) }
        val scroll = ScrollView(this).apply { isFillViewport = true; addView(root) }
        val frame = FrameLayout(this)
        frame.addView(scroll, FrameLayout.LayoutParams(-1, -1))
        frame.addView(bottomNav(), FrameLayout.LayoutParams(-1, dp(66), Gravity.BOTTOM))
        setContentView(frame)

        val m = prefs.getString("current_mobile", "") ?: ""
        val name = prefs.getString("name_$m", "User") ?: "User"
        header(root, "Good morning, $name", "Your credit, repayment and document command centre")
        summary(root)
        label(root, "QUICK ACTIONS")
        quickGrid(root)
        label(root, "CREDIT & PEOPLE")
        row(root, "Register Informal Credit", "Personal • business • trade • advance", blue) { open(V5InformalCreditActivity::class.java) }
        row(root, "Formal Credit", "Bank / NBFC • sanction • borrower consent", blue) { open(V5FormalCreditActivity::class.java) }
        row(root, "Borrower Profiles", "Search • create • linked credit history", teal) { open(V5BorrowerActivity::class.java) }
        row(root, "Repayment Centre", "Payable • receivable • due • OTP-confirmed repayment", green) { open(V5RepaymentActivity::class.java) }
        label(root, "DOCUMENTS & PROTECTION")
        row(root, "Digital Documents & Consent", "DPN • guarantee • review • OTP • audit", amber) { documents() }
        row(root, "Guarantor Profile & Consent", "Link guarantor • guarantee • OTP consent", red) { open(V5GuarantorConsentActivity::class.java) }
        row(root, "Sanction vs Account Charge Check", "OCR • compare • review • report", amber) { open(V5ChargeComparisonActivity::class.java) }
        label(root, "ASSETS, SUCCESSION & SUPPORT")
        row(root, "Asset Vault", "Financial / non-financial assets and evidence", teal) { open(V5AssetVaultActivity::class.java) }
        row(root, "Liability Vault", "Loans • cards • tax • payables • guarantees", red) { startActivity(Intent(this, V5AssetVaultActivity::class.java).putExtra("openCategory", "LIABILITY")) }
        row(root, "Inheritance & Claim Centre", "Heir / nominee • evidence • claim lifecycle", red) { open(V5DeathClaimLegalActivity::class.java) }
        row(root, "Legal Assistance", "Evidence bundles • case tracking", red) { support("LEGAL") }
        row(root, "Notifications & Readiness", "Due dates • renewals • maturity • tasks", green) { support("NOTIFICATIONS") }
        val profile = textButton("MY PROFILE / PARTY DIRECTORY", Color.WHITE, blue) { open(V5BorrowerActivity::class.java) }
        root.addView(profile, lp(52, 12))
        val logout = textButton("LOG OUT", Color.WHITE, Color.rgb(90, 105, 120)) { prefs.edit().putBoolean("logged_in", false).remove("current_mobile").apply(); startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)); finish() }
        root.addView(logout, lp(52, 8))
        root.addView(Space(this), lp(1, 78))
    }

    private fun header(root: LinearLayout, title: String, sub: String) {
        val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(16), dp(16), dp(16)); background = rounded(Color.WHITE, border) }
        card.addView(text("UDHAARDAAR", 12f, teal, true))
        card.addView(text(title, 24f, navy, true), lp(-2, 2))
        card.addView(text(sub, 13f, muted), lp(-2, 0))
        root.addView(card, lp(-2, 0))
        root.addView(Space(this), lp(1, 10))
    }

    private fun summary(root: LinearLayout) {
        val grid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        grid.addView(metric("TO RECEIVE", "₹0", green), LinearLayout.LayoutParams(0, dp(92), 1f).apply { setMargins(0, 0, dp(6), 0) })
        grid.addView(metric("TO PAY", "₹0", red), LinearLayout.LayoutParams(0, dp(92), 1f).apply { setMargins(dp(6), 0, 0, 0) })
        root.addView(grid)
        root.addView(metricWide("ACTIVE CREDIT RECORDS", "0", blue), lp(-2, 8))
    }

    private fun metric(label: String, value: String, accent: Int) = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(10), dp(14), dp(10)); background = rounded(Color.WHITE, border); addView(text(label, 11f, muted, true)); addView(text(value, 22f, accent, true), lp(-2, 2)) }
    private fun metricWide(label: String, value: String, accent: Int) = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(12), dp(14), dp(12)); background = rounded(Color.WHITE, border); addView(text(label, 12f, muted, true), LinearLayout.LayoutParams(0, -2, 1f)); addView(text(value, 20f, accent, true)) }

    private fun quickGrid(root: LinearLayout) {
        val grid = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        grid.addView(quick("+", "Give Credit", blue) { open(V5InformalCreditActivity::class.java) }, cell())
        grid.addView(quick("↙", "Receive Credit", teal) { open(V5FormalCreditActivity::class.java) }, cell())
        grid.addView(quick("₹", "Repayment", green) { open(V5RepaymentActivity::class.java) }, cell())
        grid.addView(quick("▣", "Documents", amber) { documents() }, cell())
        root.addView(grid, lp(-2, 4))
    }
    private fun cell() = LinearLayout.LayoutParams(0, dp(84), 1f).apply { setMargins(dp(3), 0, dp(3), 0) }
    private fun quick(icon: String, title: String, accent: Int, click: () -> Unit) = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER; setPadding(dp(4), dp(6), dp(4), dp(4)); background = rounded(Color.WHITE, border); setOnClickListener { click() }; addView(text(icon, 22f, accent, true)); addView(text(title, 11f, navy, true)) }

    private fun label(root: LinearLayout, s: String) { root.addView(text(s, 11f, muted, true), lp(-2, 14)) }
    private fun row(root: LinearLayout, title: String, sub: String, accent: Int, click: () -> Unit) {
        val card = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL; setPadding(dp(14), dp(11), dp(12), dp(11)); background = rounded(Color.WHITE, border); setOnClickListener { click() } }
        val mark = TextView(this).apply { text = "•"; textSize = 25f; setTextColor(accent); gravity = Gravity.CENTER }
        card.addView(mark, LinearLayout.LayoutParams(dp(26), -1))
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        body.addView(text(title, 15f, navy, true))
        body.addView(text(sub, 11f, muted), lp(-2, 3))
        card.addView(body, LinearLayout.LayoutParams(0, -2, 1f))
        card.addView(text("›", 26f, accent, false))
        root.addView(card, lp(-2, 5))
    }

    private fun textButton(label: String, fg: Int, fill: Int, click: () -> Unit) = TextView(this).apply { text = label; textSize = 14f; typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); setTextColor(fg); gravity = Gravity.CENTER; background = rounded(fill, fill, 12); setOnClickListener { click() } }

    private fun bottomNav(): View {
        val bar = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER; setPadding(dp(6), dp(5), dp(6), dp(5)); background = rounded(Color.WHITE, border, 16) }
        listOf("Home", "Credits", "Repay", "Docs", "More").forEachIndexed { index, label ->
            val item = TextView(this).apply { text = label; textSize = 11f; gravity = Gravity.CENTER; typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL); setTextColor(if (index == 0) blue else muted); setOnClickListener { when (label) { "Home" -> home(); "Credits" -> open(V5InformalCreditActivity::class.java); "Repay" -> open(V5RepaymentActivity::class.java); "Docs" -> documents(); "More" -> support("NOTIFICATIONS") } } }
            bar.addView(item, LinearLayout.LayoutParams(0, dp(56), 1f))
        }
        return bar
    }

    private fun documents() { val r = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(12), dp(16), dp(20)); background = rounded(Color.WHITE, border) }; val s = ScrollView(this); val title = text("Digital Documents & Consent", 23f, navy, true); r.addView(title, lp(-2, 6)); r.addView(text("Review first • explicit OTP consent • timestamped archive", 13f, muted)); r.addView(text("Required order: terms → document draft → review → consent → OTP verification → registration → archive.", 14f, navy), lp(-2, 18)); r.addView(textButton("GENERATE / REVIEW DOCUMENT PACKET", Color.WHITE, blue) { val id = "DOC-${System.currentTimeMillis()}"; V5LocalStore(this).add("documents", org.json.JSONObject().apply { put("id", id); put("type", "DPN_GUARANTEE_PACKET"); put("status", "DRAFT"); put("version", 1); put("createdAt", System.currentTimeMillis()) }); AlertDialog.Builder(this).setTitle("Document packet created").setMessage("Draft packet $id is versioned and timestamped. Review and consent are required before protected credit registration.").setPositiveButton("OK", null).show() }, lp(-2, 58)); r.addView(textButton("OPEN GUARANTOR CONSENT WORKFLOW", Color.WHITE, teal) { open(V5GuarantorConsentActivity::class.java) }, lp(-2, 58)); r.addView(textButton("BACK TO HOME", navy, Color.WHITE) { home() }, lp(-2, 52)); s.addView(r); setContentView(s) }
    private fun support(mode: String) { startActivity(Intent(this, V5SupportWorkflowsActivity::class.java).putExtra("mode", mode)) }
    private fun open(c: Class<*>) { startActivity(Intent(this, c)) }
    private fun lp(h: Int, top: Int) = LinearLayout.LayoutParams(-1, if (h < 0) LinearLayout.LayoutParams.WRAP_CONTENT else dp(h)).apply { setMargins(0, dp(top), 0, 0) }
}
