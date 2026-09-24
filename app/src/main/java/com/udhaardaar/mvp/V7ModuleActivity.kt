package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.Gravity
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V7ModuleActivity : AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v: Int) = (v * d).toInt()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        render(intent.getStringExtra("module") ?: "MORE")
    }

    private fun render(key: String) {
        val title = when (key) {
            "RECORD" -> "Record"
            "CREDIT" -> "Credit & Money Relationships"
            "REPAYMENT" -> "Repayment Centre"
            "PROTECT" -> "Protect"
            "GROW" -> "Grow"
            "ASSETS" -> "Assets"
            "TTMM" -> "Together • Share & Settle"
            "QR_KHATA" -> "QR Udhaar Khata"
            "LEGAL" -> "Legal & AI"
            "MORE" -> "More"
            else -> key
        }
        val sub = when (key) {
            "RECORD" -> "Your identity, family, people, addresses and evidence in one connected record."
            "CREDIT" -> "Informal, trade, formal and repayment relationships with consent and audit."
            "REPAYMENT" -> "Chronological dues, repayments, consent and closure."
            "PROTECT" -> "Protect your family, documents, rights and future."
            "GROW" -> "Understand your portfolio, performance and opportunity cost."
            "ASSETS" -> "Record what you own, its value, evidence and protection."
            "TTMM" -> "Share expenses clearly and settle with a record."
            "QR_KHATA" -> "Identify a relationship, record the transaction and confirm consent."
            "LEGAL" -> "Legal pathways, claims, professional connections and AI assistance."
            else -> "All supporting ArthSaathi services, without duplicate menu ownership."
        }

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10), dp(7), dp(10), dp(22))
            setBackgroundColor(ArthSaathiV7Design.CREAM)
        }

        val top = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            background = ArthSaathiV7Design.bg(this@V7ModuleActivity)
            setPadding(dp(9), dp(7), dp(9), dp(9))
        }
        val row = LinearLayout(this).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        row.addView(ArthSaathiV7Design.text(this, "‹", 36f, Color.WHITE).apply {
            setOnClickListener { finish() }
        }, LinearLayout.LayoutParams(dp(40), dp(45)))
        row.addView(ArthSaathiV7Design.brand(this, 23f, true), LinearLayout.LayoutParams(0, -2, 1f))
        top.addView(row)
        top.addView(ArthSaathiV7Design.text(this, title, 21f, Color.WHITE, true))
        top.addView(ArthSaathiV7Design.text(this, sub, 10.5f, ArthSaathiV7Design.GOLD_PALE),
            LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3) })
        root.addView(top)

        root.addView(ArthSaathiV7Design.section(this, title, sub), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(6) })

        when (key) {
            "RECORD" -> record(root)
            "CREDIT" -> credit(root)
            "REPAYMENT" -> repayment(root)
            "GROW" -> grow(root)
            "PROTECT" -> protect(root)
            "ASSETS" -> assets(root)
            "TTMM" -> ttmm(root)
            "QR_KHATA" -> qr(root)
            "LEGAL" -> legal(root)
            else -> more(root)
        }

        root.addView(ArthSaathiV7Design.goldButton(this, "Back to Financial Command Centre") { finish() },
            LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(13) })
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
                    row.addView(ArthSaathiV7Design.tile(this, t.first, t.second, t.third, actions[index]),
                        LinearLayout.LayoutParams(0, dp(116), 1f).apply { if (j == 1) leftMargin = dp(5) })
                }
            }
            root.addView(row, LinearLayout.LayoutParams(-1, dp(116)).apply { if (i > 0) topMargin = dp(5) })
            i += 2
        }
    }

    private fun record(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("ID", "Profile & Identity", "Identity, contact and primary record"),
            Triple("👥", "Family & Contacts", "People, relationships and authorised access"),
            Triple("⌂", "Address & Location", "PIN-assisted and map-assisted addresses"),
            Triple("▤", "Document Vault", "Evidence, versions, source and integrity")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.PEOPLE) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.PEOPLE) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "ADDRESS")) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.DOCUMENTS) }
        ))
    }

    private fun repayment(root: LinearLayout) {
        root.addView(ArthSaathiV7Design.section(this, "Repayment Centre", "Open the canonical repayment engine with chronological dues and consent-controlled updates."))
        root.addView(ArthSaathiV7Design.goldButton(this, "Open Repayment Centre") {
            V7LegacyAdapter.open(this, V7LegacyAdapter.Route.REPAYMENT)
        }, LinearLayout.LayoutParams(-1, dp(48)).apply { topMargin = dp(8) })
    }

    private fun credit(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("₹", "Udhaardaar / Informal Credit", "Consent-led credit registration and tracking"),
            Triple("▦", "QR Udhaar Khata", "Scan, identify, record and confirm"),
            Triple("▤", "Trade Credit", "Invoice-backed payable / receivable records"),
            Triple("FC", "Formal Credit", "Facilities, terms and outstanding"),
            Triple("↻", "Repayment Centre", "Chronological dues, repayments and closure"),
            Triple("♙", "Guarantor", "Linked guarantor and evidence records")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.CREDIT) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.QR_KHATA) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.TRADE) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.FORMAL_CREDIT) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.REPAYMENT) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.GUARANTOR) }
        ))
    }

    private fun grow(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("◉", "Portfolio Intelligence", "Holdings, value, allocation and performance"),
            Triple("▥", "MIS Dashboard", "First-hand financial information from your records"),
            Triple("↔", "Opportunity Cost", "Compare current portfolio with alternatives"),
            Triple("⌁", "Market Data", "Timestamped external data with freshness"),
            Triple("◌", "Scenarios", "Analyse before making a decision"),
            Triple("▤", "Reports", "Statements and financial reports")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.MIS) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.MIS) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "OPPORTUNITY")) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "MARKET")) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "OPPORTUNITY")) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.MIS) }
        ))
    }

    private fun protect(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("☂", "Insurance", "Policies, renewal, nominee and claims"),
            Triple("▣", "Documents", "Evidence, versions and expiry tracking"),
            Triple("♡", "Family", "People, authorised access and legacy"),
            Triple("✓", "Claims", "Ownership to claim closure"),
            Triple("⌂", "Address", "PIN and location-assisted address records"),
            Triple("!", "Alerts", "Due dates, renewals and document actions")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.INSURANCE) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.DOCUMENTS) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.PEOPLE) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.LEGAL) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "ADDRESS")) },
            { V7AlertEngine.evaluate(this); Toast.makeText(this, "Alerts evaluated from recorded events and due dates.", Toast.LENGTH_SHORT).show() }
        ))
    }

    private fun assets(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("▣", "Asset Vault", "Financial and non-financial assets"),
            Triple("⌁", "Valuation", "Record value and update history"),
            Triple("▤", "Evidence", "Link ownership documents"),
            Triple("♡", "Nominee", "Link nominee information"),
            Triple("↗", "Protection", "Connect insurance and safeguards"),
            Triple("!", "Claims", "Prepare ownership and claim records")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.ASSET_VAULT) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.ASSET_VAULT) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.DOCUMENTS) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.LEGAL) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.INSURANCE) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.LEGAL) }
        ))
    }

    private fun ttmm(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("👥", "Together • Share", "Create a shared expense group"),
            Triple("₹", "Contribute", "Record who paid and how much"),
            Triple("↔", "Settle", "See pending settlements"),
            Triple("▤", "History", "Keep a transparent record")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.TTMM) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.TTMM) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.TTMM) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.TTMM) }
        ))
    }

    private fun qr(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("▦", "Scan / Identify", "QR-based party and transaction capture"),
            Triple("₹", "Record Khata", "Credit or repayment ledger entry"),
            Triple("✓", "Consent", "OTP-confirmed mutation"),
            Triple("↻", "Balance", "Outstanding updates from the ledger")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.QR_SCANNER) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.QR_KHATA) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.QR_KHATA) },
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.QR_KHATA) }
        ))
    }

    private fun legal(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("⚖", "Legal Assistance", "Issue, parties, timeline and evidence"),
            Triple("♙", "Advocate Directory", "Search by city and practice domain"),
            Triple("▤", "Claim Assistance", "Ownership, nominee/heir and documents"),
            Triple("◉", "AI Financial Advisor", "Ask about your recorded financial information")
        ), listOf(
            { V7LegacyAdapter.open(this, V7LegacyAdapter.Route.LEGAL) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "ADVOCATE")) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "CLAIM")) },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "SECURITY")) }
        ))
    }

    private fun more(root: LinearLayout) {
        addGrid(root, listOf(
            Triple("CC", "ChargeCheck", "Compare sanctioned and actual charges"),
            Triple("LI", "Liability Vault", "Track loans and obligations"),
            Triple("RP", "Reports", "Statements and reports"),
            Triple("💳", "Revenue & Payments", "Services, invoices and payment records"),
            Triple("BE", "Government Schemes", "Eligibility and benefit records"),
            Triple("RE", "Rental & Lease", "Lease relationships and documents"),
            Triple("⚙", "Security & Consent", "Consent, audit and protected sharing")
        ), listOf(
            { openMore("CHARGECHECK") }, { openMore("LIABILITY") },
            { openMore("REPORTS") }, { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "REVENUE")) },
            { openMore("BENEFITS") }, { openMore("RENTAL") },
            { startActivity(Intent(this, V7ToolsActivity::class.java).putExtra("tool", "AI")) }
        ))
    }

    private fun openMore(section: String) {
        startActivity(Intent(this, V62ExtendedModulesActivity::class.java).putExtra("openSection", section))
    }
}
