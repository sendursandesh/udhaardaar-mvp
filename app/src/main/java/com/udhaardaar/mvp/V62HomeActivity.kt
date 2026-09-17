package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

/** ArthSaathi V6.2 home rebuilt around the supplied approved mobile design reference. */
class V62HomeActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()
    private val store by lazy { V5LocalStore(this) }
    private lateinit var root: LinearLayout
    private fun open(c: Class<*>) = startActivity(Intent(this, c))
    private fun add(v: View, top: Int = 7) = ArthSaathiV62Design.add(root, v, top)

    override fun onCreate(b: Bundle?) { super.onCreate(b); render() }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }

    private fun render() {
        val p = getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE)
        if (!p.getBoolean("logged_in", false)) {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }
        val owner = V62Integration.currentUserId(this)
        val mobile = p.getString("current_mobile", "") ?: ""
        val name = p.getString("name_$mobile", "User") ?: "User"
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(12), dp(9), dp(12), dp(18))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(0xfffff6d9.toInt(), ArthSaathiV62Design.BG, 0xfffffdf4.toInt()))
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })

        // Compact branded top bar, matching the reference dashboard composition.
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(4), dp(3), dp(4), dp(3))
        }
        top.addView(ImageView(this).apply {
            setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "ArthSaathi"
        }, LinearLayout.LayoutParams(dp(48), dp(48)))
        val identity = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(7), 0, 0, 0) }
        identity.addView(ArthSaathiV62Design.brandWordmark(this, 18f))
        identity.addView(ArthSaathiV62Design.text(this, "Navigate Your Financial Journey", 8.5f, ArthSaathiV62Design.NAVY), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(1) })
        top.addView(identity, LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(ArthSaathiV62Design.text(this, "♧", 25f, ArthSaathiV62Design.GOLD_DARK, false).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(44), dp(48)))
        add(top, 0)

        val greet = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val hour = java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting = when (hour) { in 5..11 -> "Good Morning"; in 12..16 -> "Good Afternoon"; in 17..20 -> "Good Evening"; else -> "Good Night" }
        greet.addView(ArthSaathiV62Design.text(this, "$greeting, $name", 20f, ArthSaathiV62Design.NAVY, true))
        greet.addView(ArthSaathiV62Design.text(this, "Good to see you!", 10f, ArthSaathiV62Design.MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
        add(greet, 7)

        // The reference's signature "Plan Today" hero card.
        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(15), dp(12), dp(10), dp(12))
            background = GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
                intArrayOf(ArthSaathiV62Design.GOLD_BRIGHT, 0xffffd46a.toInt())).apply { cornerRadius = dp(17).toFloat() }
            elevation = dp(2).toFloat()
        }
        val hc = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        hc.addView(ArthSaathiV62Design.text(this, "Plan Today", 19f, ArthSaathiV62Design.NAVY, true))
        hc.addView(ArthSaathiV62Design.text(this, "For a Brighter Tomorrow", 11f, ArthSaathiV62Design.NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(2) })
        hc.addView(ArthSaathiV62Design.text(this, "Secure Wealth • Stronger Generations", 9f, ArthSaathiV62Design.GOLD_DARK), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })
        hero.addView(hc, LinearLayout.LayoutParams(0, -2, 1f))
        hero.addView(ArthSaathiV62Design.text(this, "›", 34f, ArthSaathiV62Design.NAVY, false).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(dp(42), dp(54)))
        add(hero, 10)

        val relationships = store.all(V62Store.RELATIONSHIPS).filter { it.optString("ownerUserId") == owner }
        val assets = store.all(V62Store.ASSETS).filter { it.optString("ownerUserId") == owner }
        val policies = store.all(V62Store.INSURANCE).filter { it.optString("ownerUserId") == owner }

        fun featureRow(vararg items: Pair<String, () -> Unit>) {
            val r = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            items.forEach { item ->
                val card = ArthSaathiV62Design.featureCard(this, item.first.substringBefore('|'), item.first.substringAfter('|'), item.second)
                r.addView(card, LinearLayout.LayoutParams(0, dp(82), 1f).apply { leftMargin = dp(3); rightMargin = dp(3) })
            }
            add(r, 6)
        }

        featureRow("▣|Register\nCredit" to { open(V62CreditRegistrationActivity::class.java) },
            "↔|Repayment" to { open(V62RepaymentActivity::class.java) },
            "▤|Asset Vault" to { open(V62AssetVaultActivity::class.java) })
        featureRow("⬟|Protect" to { open(V62InsuranceActivity::class.java) },
            "▥|Grow" to { open(V62CreditIntelligenceActivity::class.java) },
            "♟|Legacy" to { open(V62LegacyLegalAIActivity::class.java) })
        featureRow("⚖|Legal\nAssistance" to { open(V62LegacyLegalAIActivity::class.java) },
            "●|Nominee" to { open(V62LegacyLegalAIActivity::class.java) },
            "✦|Insights" to { open(V62MISActivity::class.java) })

        add(ArthSaathiV62Design.section(this, "YOUR FINANCIAL SNAPSHOT"), 8)
        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        stats.addView(ArthSaathiV62Design.statCard(this, "Total Assets", "₹ ${assets.sumOf { it.optDouble("value", 0.0) }.toLong()}", ArthSaathiV62Design.GOLD_DARK), LinearLayout.LayoutParams(0, dp(76), 1f).apply { rightMargin = dp(4) })
        stats.addView(ArthSaathiV62Design.statCard(this, "Active Credits", relationships.size.toString(), ArthSaathiV62Design.BLUE), LinearLayout.LayoutParams(0, dp(76), 1f).apply { leftMargin = dp(4) })
        add(stats, 4)
        add(ArthSaathiV62Design.text(this, "Protection policies: ${policies.size}   •   Financial relationships: ${relationships.size}", 9f, ArthSaathiV62Design.MUTED), 5)

        // All ten V6.2 modules remain reachable; the reference dashboard only surfaces the primary shortcuts.
        add(ArthSaathiV62Design.section(this, "ALL FINANCIAL MODULES"), 9)
        fun module(title: String, desc: String, go: () -> Unit) = ArthSaathiV62Design.moduleCard(this, "", title, desc, ArthSaathiV62Design.GOLD_DEEP, go)
        add(module("Credit & Udhaar", "Lend • borrow • trade", { open(V62CreditRegistrationActivity::class.java) }), 3)
        add(module("Credit Intelligence", "Score • exposure • behaviour", { open(V62CreditIntelligenceActivity::class.java) }), 3)
        add(module("Repayment Centre", "Schedule • collect • history", { open(V62RepaymentActivity::class.java) }), 3)
        add(module("Asset Vault", "Property • bank • lifecycle", { open(V62AssetVaultActivity::class.java) }), 3)
        add(module("Insurance & Protection", "Policies • verify • alerts", { open(V62InsuranceActivity::class.java) }), 3)
        add(module("Rental / Lease", "Landlord • tenant • dues", { open(V62RentalLeaseActivity::class.java) }), 3)
        add(module("TTMM Shared Money", "Group money • settle", { open(V62TTMMActivity::class.java) }), 3)
        add(module("MIS & Analytics", "Charts • tables • risk", { open(V62MISActivity::class.java) }), 3)
        add(module("Legacy • Legal • AI", "Will • claims • advice", { open(V62LegacyLegalAIActivity::class.java) }), 3)
        add(module("Financial Centre", "Formal • QR • ChargeCheck • funding", { open(V62ExtendedModulesActivity::class.java) }), 3)

        val nav = ArthSaathiV62Design.bottomNav(this, "Home", mapOf(
            "Home" to { },
            "Credit" to { open(V62CreditRegistrationActivity::class.java) },
            "Repay" to { open(V62RepaymentActivity::class.java) },
            "Vault" to { open(V62AssetVaultActivity::class.java) },
            "More" to { open(V62ExtendedModulesActivity::class.java) }
        ))
        add(nav, 14)
    }
}
