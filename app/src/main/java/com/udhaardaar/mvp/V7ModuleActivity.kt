package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/**
 * ArthSaathi V7 canonical module router.
 *
 * Rules:
 * 1. One owner per major journey.
 * 2. Address/location is profile data, never a top-level module.
 * 3. No duplicate menu entries.
 * 4. MIS, Portfolio, Switch Analysis, Market Data, Scenario, ERP/Tally and
 *    Reports are distinct tools with distinct destinations.
 * 5. Every user-facing destination is V7-owned; no legacy compatibility route exists.
 */
class V7ModuleActivity : V7SessionActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render(intent.getStringExtra("module") ?: "MORE")
    }

    private fun render(key: String) {
        when (key) {
            "RECORD", "CREDIT", "REPAYMENT", "ASSETS" -> {
                startActivity(Intent(this, V7NativeModuleActivity::class.java).putExtra("module", key))
                finish()
                return
            }
        }

        val title = when (key) {
            "PROTECT" -> "Protect"
            "GROW" -> "Grow"
            "TTMM" -> "TTMM"
            "QR_KHATA" -> "QR Khata"
            "LEGAL" -> "Legal & Claims"
            "MORE" -> "More"
            else -> key
        }
        val sub = when (key) {
            "PROTECT" -> "Insurance & protection"
            "GROW" -> "Portfolio & insights"
            "TTMM" -> "Group expenses"
            "QR_KHATA" -> "Scan & record credit"
            "LEGAL" -> "Claims & legal help"
            else -> "More financial tools"
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(7), dp(10), dp(22))
            setBackgroundColor(ArthSaathiV7Design.CREAM)
        }
        val top = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ArthSaathiV7Design.bg(this@V7ModuleActivity)
            setPadding(dp(9), dp(7), dp(9), dp(10))
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        row.addView(ArthSaathiV7Design.text(this, "←", 30f, Color.WHITE).apply {
            contentDescription = "Back"
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(dp(40), dp(45)))
        row.addView(ArthSaathiV7Design.brand(this, 23f, true), LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(row)
        top.addView(ArthSaathiV7Design.text(this, title, 21f, Color.WHITE, true))
        top.addView(ArthSaathiV7Design.text(this, sub, 10.5f, ArthSaathiV7Design.GOLD_PALE),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
        root.addView(top)

        when (key) {
            "GROW" -> grow(root)
            "PROTECT" -> protect(root)
            "TTMM" -> ttmm(root)
            "QR_KHATA" -> qr(root)
            "LEGAL" -> legal(root)
            else -> more(root)
        }

        root.addView(
            ArthSaathiV7Design.goldButton(this, "Back to Home") { finish() },
            LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(13) }
        )
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun addGrid(root: LinearLayout, items: List<Triple<String, String, String>>, actions: List<() -> Unit>) {
        var i = 0
        while (i < items.size) {
            val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL }
            for (j in 0..1) {
                val index = i + j
                if (index < items.size) {
                    val t = items[index]
                    row.addView(
                        ArthSaathiV7Design.tile(this, t.first, t.second, t.third, actions[index]),
                        LinearLayout.LayoutParams(0, dp(116), 1f).apply { if (j == 1) leftMargin = dp(5) }
                    )
                }
            }
            root.addView(row, LinearLayout.LayoutParams(-1, dp(116)).apply { if (i > 0) topMargin = dp(5) })
            i += 2
        }
    }

    private fun grow(root: LinearLayout) {
        // Exactly one entry for each intelligence function.
        addGrid(root, listOf(
            Triple("◉", "Portfolio", "Holdings & returns"),
            Triple("▥", "MIS", "Your financial position"),
            Triple("↔", "Switch Analysis", "Compare alternatives"),
            Triple("◌", "Scenarios", "Try scenarios"),
            Triple("⌁", "Market Data", "Current market values"),
            Triple("⇄", "ERP / Tally", "Import & export"),
            Triple("▤", "Reports", "Statements & reports")
        ), listOf(
            { openTool("PORTFOLIO") },
            { openTool("MIS") },
            { openTool("OPPORTUNITY") },
            { openTool("SCENARIO") },
            { openTool("MARKET") },
            { openTool("INTEGRATION") },
            { openTool("REPORTS") }
        ))
    }

    private fun protect(root: LinearLayout) {
        // Address deliberately omitted: it belongs to Profile/Record editing.
        addGrid(root, listOf(
            Triple("☂", "Insurance", "Policies & nominees"),
            Triple("▣", "Documents", "Important documents"),
            Triple("♡", "Family & Access", "Family & access"),
            Triple("!", "Alerts", "Due dates & reminders")
        ), listOf(
            { openTool("INSURANCE") },
            { openTool("DOCUMENTS") },
            { openTool("PEOPLE") },
            { V7AlertEngine.evaluate(this); Toast.makeText(this, "Alerts evaluated from recorded events and due dates.", Toast.LENGTH_SHORT).show() }
        ))
    }

    private fun ttmm(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("👥", "Create / Open Group", "Groups & members"),
            Triple("₹", "Record Contribution", "Contributions"),
            Triple("↔", "Settle", "Settle balances"),
            Triple("▤", "History", "Past expenses")
        ), listOf(
            { openTool("TTMM_CREATE") },
            { openTool("TTMM_CONTRIBUTION") },
            { openTool("TTMM_SETTLE") },
            { openTool("TTMM_HISTORY") }
        ))
    }

    private fun qr(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("▦", "Scan / Identify", "Scan details"),
            Triple("₹", "Record Khata", "Add entry"),
            Triple("✓", "Consent", "Confirm consent"),
            Triple("↻", "Balance", "Balance & history")
        ), listOf(
            { openTool("QR_SCAN") },
            { openTool("QR_RECORD") },
            { openTool("QR_CONSENT") },
            { openTool("QR_BALANCE") }
        ))
    }

    private fun legal(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("⚖", "Legal Assistance", "Cases & evidence"),
            Triple("♙", "Advocate Directory", "Find an advocate"),
            Triple("▤", "Claim Assistance", "Claims & documents"),
            Triple("◉", "AI Financial Advisor", "Financial guidance")
        ), listOf(
            { openTool("LEGAL") },
            { openTool("ADVOCATE") },
            { openTool("CLAIM") },
            { openTool("AI") }
        ))
    }

    private fun more(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("CC", "ChargeCheck", "Compare charges"),
            Triple("LI", "Liability Vault", "Loans & dues"),
            Triple("💳", "Revenue & Payments", "Charges & payments"),
            Triple("BE", "Government Benefits", "Benefits"),
            Triple("⚙", "Security & Consent", "Privacy & consent"),
            Triple("◈", "Credit Score", "Explainable score"),
            Triple("⚖", "Will, Inheritance & Claims", "Family wealth & succession")
        ), listOf(
            { openTool("CHARGECHECK") },
            { startActivity(Intent(this, V7NativeModuleActivity::class.java).putExtra("module", "LIABILITIES")) },
            { openTool("REVENUE") },
            { openTool("BENEFITS") },
            { openTool("SECURITY") },
            { openTool("SCORE") },
            { openTool("WILL") }
        ))
    }

    private fun openTool(tool: String) {
        startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", tool))
    }
}
