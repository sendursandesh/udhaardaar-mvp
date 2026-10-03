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
 * 4. MIS, Portfolio, Opportunity Cost, Market Data, Scenario, ERP/Tally and
 *    Reports are distinct tools with distinct destinations.
 * 5. Native V7 modules are opened directly; compatibility engines are reached
 *    through an isolated compatibility boundary when unavoidable.
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
            "GROW" -> "Grow — Financial Intelligence"
            "TTMM" -> "TTMM — Share & Settle"
            "QR_KHATA" -> "QR Udhaar Khata"
            "LEGAL" -> "Legal & Claims"
            "MORE" -> "More Services"
            else -> key
        }
        val sub = when (key) {
            "PROTECT" -> "Insurance, documents, alerts and protection actions."
            "GROW" -> "Portfolio, MIS, scenarios, market data and integrations."
            "TTMM" -> "Shared expenses, contributions, settlements and history."
            "QR_KHATA" -> "Scan, record and maintain a consented merchant credit ledger."
            "LEGAL" -> "Claims, legal assistance, advocates and financial AI."
            else -> "Supporting ArthSaathi services."
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
            ArthSaathiV7Design.goldButton(this, "Back to Financial Command Centre") { finish() },
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
            Triple("◉", "Portfolio Intelligence", "Holdings, allocation, value and performance"),
            Triple("▥", "MIS Dashboard", "Assets, liabilities, credit, revenue and financial position"),
            Triple("↔", "Opportunity Cost", "AI-assisted analysis of possible portfolio switches"),
            Triple("◌", "Scenarios", "Test assumptions before making a decision"),
            Triple("⌁", "Market Data", "External values with source, timestamp and freshness"),
            Triple("⇄", "ERP / Tally", "Explicit import/export and connector boundary"),
            Triple("▤", "Reports", "Statements and generated financial reports")
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
            Triple("☂", "Insurance", "Policies, renewal, nominee and claims"),
            Triple("▣", "Documents", "Evidence, versions and expiry tracking"),
            Triple("♡", "Family & Access", "People, relationships and authorised access"),
            Triple("✓", "Claims", "Ownership, evidence and claim lifecycle"),
            Triple("!", "Alerts", "Due dates, renewals and document actions")
        ), listOf(
            { openTool("INSURANCE") },
            { openTool("DOCUMENTS") },
            { openTool("PEOPLE") },
            { openTool("CLAIM") },
            { V7AlertEngine.evaluate(this); Toast.makeText(this, "Alerts evaluated from recorded events and due dates.", Toast.LENGTH_SHORT).show() }
        ))
    }

    private fun ttmm(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("👥", "Create / Open Group", "Shared expense group and members"),
            Triple("₹", "Record Contribution", "Who paid, how much and for what"),
            Triple("↔", "Settle", "Pending balances and settlement records"),
            Triple("▤", "History", "Transparent expense and settlement history")
        ), listOf(
            { openTool("TTMM") },
            { openTool("TTMM") },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.TTMM) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.TTMM) }
        ))
    }

    private fun qr(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("▦", "Scan / Identify", "Capture party and transaction details"),
            Triple("₹", "Record Khata", "Credit or repayment ledger entry"),
            Triple("✓", "Consent", "Consent-controlled record mutation"),
            Triple("↻", "Balance", "Outstanding and transaction history")
        ), listOf(
            { openTool("QR") },
            { openTool("QR") },
            { openTool("QR") },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.QR_KHATA) }
        ))
    }

    private fun legal(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("⚖", "Legal Assistance", "Issue, parties, timeline and evidence"),
            Triple("♙", "Advocate Directory", "Search by city and practice domain"),
            Triple("▤", "Claim Assistance", "Ownership, nominee/heir and documents"),
            Triple("◉", "AI Financial Advisor", "Answers from recorded financial information")
        ), listOf(
            { openTool("LEGAL") },
            { openTool("ADVOCATE") },
            { openTool("CLAIM") },
            { openTool("AI") }
        ))
    }

    private fun more(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("CC", "ChargeCheck", "Sanctioned vs actual charges"),
            Triple("LI", "Liability Vault", "Loans and other obligations"),
            Triple("💳", "Revenue & Payments", "Service charges, invoices and payment records"),
            Triple("BE", "Government Benefits", "Eligibility and benefit records"),
            Triple("RE", "Rental & Lease", "Lease relationships and documents"),
            Triple("⚙", "Security & Consent", "Consent, audit and protected sharing"),
            Triple("◈", "Credit Score", "Consent-gated explainable internal score"),
            Triple("⚖", "Will & Legacy", "Create a will record and link recorded assets")
        ), listOf(
            { openTool("CHARGECHECK") },
            { startActivity(Intent(this, V7NativeModuleActivity::class.java).putExtra("module", "LIABILITIES")) },
            { openTool("REVENUE") },
            { openTool("BENEFITS") },
            { openTool("RENTAL") },
            { openTool("SECURITY") },
            { openTool("SCORE") },
            { openTool("WILL") }
        ))
    }

    private fun openTool(tool: String) {
        startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", tool))
    }
}
