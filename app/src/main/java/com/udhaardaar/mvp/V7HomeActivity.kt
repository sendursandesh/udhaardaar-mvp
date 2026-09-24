package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V7HomeActivity : AppCompatActivity() {
    private val p by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render() }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }

    private fun render() {
        if (!p.getBoolean("logged_in", false)) {
            startActivity(Intent(this, LoginActivity::class.java)); finish(); return
        }
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(6), dp(10), dp(18))
            setBackgroundColor(ArthSaathiV7Design.CREAM)
        }
        val header = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ArthSaathiV7Design.bg(this@V7HomeActivity)
            setPadding(dp(8), dp(7), dp(8), dp(10))
        }
        header.addView(ArthSaathiV7Design.masthead(this))
        header.addView(ArthSaathiV7Design.topNav(this, navActions()),
            LinearLayout.LayoutParams(-1, dp(70)).apply { topMargin = dp(5) })
        root.addView(header)

        val mobile = p.getString("current_mobile", "").orEmpty()
        val name = p.getString("name_$mobile", "User").orEmpty().ifBlank { "User" }
        val welcome = LinearLayout(this).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(11), dp(9), dp(11), dp(9))
            background = ArthSaathiV7Design.card(this@V7HomeActivity, Color.WHITE, 14)
        }
        val avatar = TextView(this).apply {
            text = name.trim().firstOrNull()?.uppercase() ?: "U"
            textSize = 15f; gravity = Gravity.CENTER
            setTextColor(ArthSaathiV7Design.NAVY); typeface = Typeface.DEFAULT_BOLD
            background = GradientDrawable().apply {
                shape = GradientDrawable.OVAL
                setColor(ArthSaathiV7Design.GOLD_PALE)
                setStroke(dp(1), ArthSaathiV7Design.GOLD)
            }
        }
        welcome.addView(avatar, LinearLayout.LayoutParams(dp(38), dp(38)))
        val wt = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(9), 0, 0, 0) }
        wt.addView(ArthSaathiV7Design.text(this, "Hello $name", 15f, ArthSaathiV7Design.NAVY, true))
        wt.addView(ArthSaathiV7Design.text(this, "Good to see you!", 9.5f, ArthSaathiV7Design.MUTED))
        welcome.addView(wt, LinearLayout.LayoutParams(0, -2, 1f))
        welcome.addView(ArthSaathiV7Design.text(this, "Smarter Finance\nStronger Tomorrow", 9f, ArthSaathiV7Design.GOLD, true).apply { gravity = Gravity.CENTER })
        root.addView(welcome, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })

        val hero = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15), dp(13), dp(15), dp(13))
            background = GradientDrawable(GradientDrawable.Orientation.TL_BR,
                intArrayOf(ArthSaathiV7Design.NAVY_3, ArthSaathiV7Design.NAVY)).apply { cornerRadius = dp(18).toFloat() }
        }
        hero.addView(ArthSaathiV7Design.text(this, "Plan Today", 20f, Color.WHITE, true))
        hero.addView(ArthSaathiV7Design.text(this, "For a Brighter Tomorrow", 12f, ArthSaathiV7Design.GOLD_2, true))
        hero.addView(ArthSaathiV7Design.text(this, ArthSaathiV7Design.PROMISE, 10.5f, ArthSaathiV7Design.GOLD_PALE),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) })
        root.addView(hero, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8) })

        val m = V7Core.metrics(this)
        root.addView(ArthSaathiV7Design.section(this, "Your Financial Snapshot", "Live from your recorded ArthSaathi data. At a glance. In control. Always."),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
        val stats = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
        listOf("₹ %.2f".format(m.optDouble("assets")) to "Total Assets", m.optInt("activeCredits").toString() to "Active Credits", "₹ %.2f".format(m.optDouble("liabilities")) to "Liabilities")
            .forEachIndexed { i, pair ->
                stats.addView(ArthSaathiV7Design.stat(this, pair.first, pair.second,
                    if (i == 1) ArthSaathiV7Design.GREEN else ArthSaathiV7Design.GOLD),
                    LinearLayout.LayoutParams(0, dp(76), 1f).apply { if (i > 0) leftMargin = dp(5) })
            }
        root.addView(stats)

        root.addView(ArthSaathiV7Design.section(this, "Major Journeys", "One function. One logical home."),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
        addGrid(root, listOf(
            Triple("₹", "Register Credit", "Record money relationships") to { open("CREDIT") },
            Triple("↻", "Repayment", "Track dues and repayments") to { open("REPAYMENT") },
            Triple("▣", "Asset Vault", "Record what you own") to { open("ASSET_VAULT") },
            Triple("◆", "Protect", "Insurance and protection") to { open("INSURANCE") },
            Triple("▥", "Grow", "Portfolio and financial intelligence") to { openModule("GROW") },
            Triple("▤", "Legal & Claims", "Protect and claim what matters") to { open("LEGACY") }
        ))

        val footer = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER
            setPadding(dp(8), dp(13), dp(8), dp(5))
        }
        footer.addView(ArthSaathiV7Design.text(this, "ArthSaathi", 18f, ArthSaathiV7Design.NAVY, true).apply { gravity = Gravity.CENTER })
        footer.addView(ArthSaathiV7Design.text(this, ArthSaathiV7Design.TAGLINE, 10f, ArthSaathiV7Design.GOLD, true).apply { gravity = Gravity.CENTER })
        footer.addView(ArthSaathiV7Design.text(this, "Organise Today  •  Make Informed Choices  •  Protect Tomorrow", 9f, ArthSaathiV7Design.MUTED).apply { gravity = Gravity.CENTER })
        root.addView(footer)
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun addGrid(root: LinearLayout, items: List<Pair<Triple<String, String, String>, () -> Unit>>) {
        var i = 0
        while (i < items.size) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (j in 0..1) {
                val index = i + j
                if (index < items.size) {
                    val item = items[index]
                    row.addView(ArthSaathiV7Design.tile(this, item.first.first, item.first.second, item.first.third, item.second),
                        LinearLayout.LayoutParams(0, dp(116), 1f).apply { if (j == 1) leftMargin = dp(5) })
                }
            }
            root.addView(row, LinearLayout.LayoutParams(-1, dp(116)).apply { if (i > 0) topMargin = dp(5) })
            i += 2
        }
    }

    private fun navActions(): Map<String, () -> Unit> = mapOf(
        "Home" to {},
        "Record" to { openModule("RECORD") },
        "Credit" to { openModule("CREDIT") },
        "Assets" to { openModule("ASSETS") },
        "Grow" to { openModule("GROW") },
        "Protect" to { openModule("PROTECT") },
        "Legal" to { openModule("LEGAL") },
        "More" to { startActivity(Intent(this, V7ModuleActivity::class.java).putExtra("module", "MORE")) }
    )

    private fun open(key: String) {
        val module = when (key) {
            "CREDIT" -> "CREDIT"
            "REPAYMENT" -> "REPAYMENT"
            "ASSET_VAULT" -> "ASSETS"
            "INSURANCE" -> "PROTECT"
            "LEGACY" -> "LEGAL"
            else -> "MORE"
        }
        startActivity(Intent(this, V7ModuleActivity::class.java).putExtra("module", module))
    }
    private fun openModule(key: String) { startActivity(Intent(this, V7ModuleActivity::class.java).putExtra("module", key)) }
}
