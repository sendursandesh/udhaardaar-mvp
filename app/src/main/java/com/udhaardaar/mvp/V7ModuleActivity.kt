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
            "PROTECT" -> "Insurance & Protection"
            "GROW" -> "Investments & Returns"
            "TTMM" -> "Group Expenses"
            "QR_KHATA" -> "QR Udhaar Khata"
            "LEGAL" -> "Legal Help & Claims"
            "MORE" -> "More Services"
            else -> key
        }
        val sub = when (key) {
            "PROTECT" -> "Policies, renewals and claims"
            "GROW" -> "Portfolio, returns and financial tools"
            "TTMM" -> "Share a group expense and settle who owes whom"
            "QR_KHATA" -> "Scan a transaction and record an udhaar entry"
            "LEGAL" -> "Legal help, advocates and claims"
            else -> "Profile, records and other useful services"
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
            Triple("◉", "My Investments", "Holdings, value & returns"),
            Triple("▥", "My Money Report", "Assets, dues & net position"),
            Triple("↔", "Should I Switch?", "Compare return, cost & risk"),
            Triple("◌", "What-If Calculator", "Try future scenarios"),
            Triple("⌁", "Market Values", "Record current market values"),
            Triple("⇄", "Accounting Export", "Tally / ERP data export"),
            Triple("◉", "Money Guide", "Simple financial guidance"),
            Triple("▤", "Detailed Reports", "Statements & summaries")
        ), listOf(
            { openTool("PORTFOLIO") },
            { openTool("MIS") },
            { openTool("OPPORTUNITY") },
            { openTool("SCENARIO") },
            { openTool("MARKET") },
            { openTool("INTEGRATION") },
            { openTool("AI") },
            { openTool("REPORTS") }
        ))
    }

    private fun protect(root: LinearLayout) {
        // Address deliberately omitted: it belongs to Profile/Record editing.
        addGrid(root, listOf(
            Triple("☂", "Insurance Policies", "Policies, renewal & nominee"),
            Triple("▣", "Policy Documents", "Policy papers & evidence"),
            Triple("!", "Renewal & Due Alerts", "Upcoming dates & reminders"),
            Triple("⚖", "Insurance Claims", "Claim records & evidence")
        ), listOf(
            { openTool("INSURANCE") },
            { openTool("DOCUMENTS") },
            { openTool("ALERTS") },
            { openTool("CLAIM") }
        ))
    }

    private fun ttmm(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("👥", "Create / Open Group", "Groups & members"),
            Triple("₹", "Add Shared Expense", "Who paid, amount & split"),
            Triple("↔", "See Who Owes", "Balances to settle"),
            Triple("▤", "Expense History", "Past group expenses")
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
            Triple("⚖", "Legal Help", "Cases, documents & evidence"),
            Triple("♙", "Find an Advocate", "Search by city and legal area"),
            Triple("▤", "Claim Assistance", "Claims & supporting documents"),
            Triple("▤", "Inheritance Claims", "Family asset claim checklist")
        ), listOf(
            { openTool("LEGAL") },
            { openTool("ADVOCATE") },
            { openTool("CLAIM") },
            { openTool("WILL") }
        ))
    }

    private fun more(root: LinearLayout) {
        root.addView(ArthSaathiV7Design.section(this, "My Records & Family", "Keep your people, documents and shared expenses together."))
        addGrid(root, listOf(
            Triple("●", "My Profile & Family", "People, contacts & access"),
            Triple("▣", "My Documents", "Important papers & evidence"),
            Triple("👥", "Group Expenses", "Share, split & settle expenses"),
            Triple("▦", "QR Udhaar Khata", "Scan and record merchant credit"),
            Triple("LI", "My Loans & Dues", "Loans, liabilities & outstanding")
        ), listOf(
            { openTool("PEOPLE") },
            { openTool("DOCUMENTS") },
            { openModule("TTMM") },
            { openModule("QR_KHATA") },
            { startActivity(Intent(this, V7NativeModuleActivity::class.java).putExtra("module", "LIABILITIES")) }
        ))

        root.addView(ArthSaathiV7Design.section(this, "Money Checks & Services", "Useful checks, payments and benefits.").apply { setPadding(0, dp(10), 0, 0) })
        addGrid(root, listOf(
            Triple("CC", "Check Loan / Bank Charges", "Compare promised vs actual"),
            Triple("💳", "Payments & Charges", "Service charges & payments"),
            Triple("BE", "Benefits & Refunds", "Record money received")
        ), listOf(
            { openTool("CHARGECHECK") },
            { openTool("REVENUE") },
            { openTool("BENEFITS") }
        ))

        root.addView(ArthSaathiV7Design.section(this, "Security & Support", "Keep your records protected and get help when needed.").apply { setPadding(0, dp(10), 0, 0) })
        addGrid(root, listOf(
            Triple("✓", "Privacy & Consent", "Who can access what"),
            Triple("◈", "Credit Reliability Score", "Consent-based reliability view"),
            Triple("⚖", "Will & Inheritance", "Plan family assets & claims")
        ), listOf(
            { openTool("SECURITY") },
            { openTool("SCORE") },
            { openTool("WILL") }
        ))
    }

    private fun openTool(tool: String) {
        startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", tool))
    }
}
