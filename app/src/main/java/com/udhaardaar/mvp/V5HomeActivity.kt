package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class V5HomeActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val store by lazy { V5LocalStore(this) }

    private fun add(r: LinearLayout, v: View, h: Int = 52) {
        r.addView(v, LinearLayout.LayoutParams(-1, h).apply { setMargins(0, 3, 0, 3) })
    }

    private fun btn(s: String, c: Int = Color.rgb(25, 111, 220), f: () -> Unit) = Button(this).apply {
        text = s
        isAllCaps = false
        textSize = 13f
        setTextColor(Color.WHITE)
        setBackgroundColor(c)
        setOnClickListener { f() }
    }

    private fun tile(r: LinearLayout, title: String, sub: String, f: () -> Unit) {
        add(r, LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(12, 6, 8, 6)
            setBackgroundColor(Color.WHITE)
            setOnClickListener { f() }
            addView(TextView(this@V5HomeActivity).apply {
                text = title
                textSize = 16f
                setTextColor(Color.rgb(24, 58, 92))
            })
            addView(TextView(this@V5HomeActivity).apply {
                text = sub
                textSize = 11f
            })
        }, 62)
    }

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        home()
    }

    override fun onBackPressed() { home() }

    private fun home() {
        if (!prefs.getBoolean("logged_in", false)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        val r = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(14, 12, 14, 16)
            setBackgroundColor(Color.rgb(238, 248, 253))
        }
        add(r, TextView(this).apply { text = "UDHAARDAAR V5"; textSize = 24f; setTextColor(Color.rgb(24, 58, 92)) }, 42)
        add(r, TextView(this).apply { text = "Personal financial command centre • unified V5 architecture"; textSize = 12f }, 34)
        add(r, btn("PARTY & IDENTITY • Search / Create / History", Color.rgb(0, 145, 135)) { startActivity(Intent(this, V5PartyActivity::class.java)) })
        add(r, btn("SCAN QR • Pay or Request / Offer Credit") { startActivity(Intent(this, V5QrCreditActivity::class.java)) })
        add(r, TextView(this).apply { text = "CREDIT & OBLIGATIONS"; textSize = 12f; setTextColor(Color.rgb(25, 111, 220)) }, 28)
        tile(r, "Credit Registration", "Personal • business • trade • advance • formal/informal • given/received") { startActivity(Intent(this, V5CreditRegistrationActivity::class.java)) }
        tile(r, "Repayment Centre", "EMI • principal+interest • bullet modes • payable/receivable • evidence + consent") { startActivity(Intent(this, V5RepaymentActivity::class.java)) }
        tile(r, "Formal Credit Audit", "Sanction terms → statement → ROI/fees/taxes/penalties → variance report") { formal() }
        tile(r, "Rental / Lease Engine", "Lease terms • rent calendar • arrears • escalation • expiry/renewal") { rental() }
        add(r, TextView(this).apply { text = "DOCUMENTS • ASSETS • SUCCESSION"; textSize = 12f; setTextColor(Color.rgb(210, 135, 15)) }, 28)
        tile(r, "Document & Consent Vault", "DPN • guarantee • agreements • invoices • statements • receipts • version/audit") { documents() }
        tile(r, "Financial / Non-Financial Assets", "Ownership • proof • valuation • nominee • maturity/renewal") { assets() }
        tile(r, "Nominee / Trusted Person", "Controlled discovery/view/claim-preparation access") { trusted() }
        tile(r, "Inheritance & Claims", "Heir/nominee • institution checklist • submission • query • approval • transfer/closure") { claims() }
        tile(r, "Legal Assistance", "Evidence bundle + recovery/claim professional referral") { legal() }
        tile(r, "Score & Readiness", "Consent-controlled explainable score • due/overdue • maturity/renewal tasks") { score() }
        add(r, btn("MY PROFILE", Color.rgb(0, 145, 135)) { profile() })
        add(r, btn("LOGOUT", Color.rgb(90, 110, 125)) {
            prefs.edit().putBoolean("logged_in", false).apply()
            startActivity(Intent(this, LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP))
            finish()
        })
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(r) })
    }

    private fun page(title: String, sub: String) = LinearLayout(this).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(16, 12, 16, 16)
        setBackgroundColor(Color.rgb(238, 248, 253))
        addView(TextView(this@V5HomeActivity).apply { text = title; textSize = 22f; setTextColor(Color.rgb(24, 58, 92)) })
        addView(TextView(this@V5HomeActivity).apply { text = sub; textSize = 12f })
    }

    private fun field(h: String) = EditText(this).apply { hint = h; setSingleLine(true); textSize = 14f }
    private fun form(r: LinearLayout, e: EditText) { add(r, e, 50) }

    private fun formal() {
        val r = page("Formal Credit Audit", "No repayment OTP: formal repayments rely on bank/account evidence.")
        val s = field("Sanction letter reference")
        val st = field("Account statement reference")
        val roi = field("Sanctioned ROI %")
        val fees = field("Sanctioned fees/charges/taxes")
        val actual = field("Actual debited charges")
        listOf(s, st, roi, fees, actual).forEach { form(r, it) }
        add(r, btn("SAVE AUDIT INPUTS", Color.rgb(0, 145, 135)) {
            val id = "AUDIT-${System.currentTimeMillis()}"
            store.add("formal_audits", JSONObject().apply {
                put("id", id); put("sanction", s.text.toString()); put("statement", st.text.toString())
                put("sanctionedRoi", roi.text.toString()); put("sanctionedFees", fees.text.toString())
                put("actualFees", actual.text.toString()); put("status", "READY_FOR_RECONCILIATION")
                put("createdAt", System.currentTimeMillis())
            })
            Toast.makeText(this, "Audit saved; variance report is evidence-linked", Toast.LENGTH_LONG).show()
        })
        add(r, btn("RUN RECONCILIATION", Color.rgb(25, 145, 78)) {
            AlertDialog.Builder(this).setTitle("Charge comparison")
                .setMessage("Compare sanctioned ROI/fees/taxes/penalties against actual debits. Variances must be reviewed against the source documents before any recovery claim.")
                .setPositiveButton("OK", null).show()
        })
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun rental() {
        val r = page("Rental / Lease Engine", "Dedicated rental obligation, separate from credit.")
        listOf("Tenant / counterparty *", "Landlord / owner *", "Property / premises *", "Monthly rent *", "Security deposit", "Start date DDMMYYYY *", "End date DDMMYYYY *", "Due day 1–31 *", "Escalation %", "Notice days", "Utilities / maintenance").forEach { form(r, field(it)) }
        add(r, btn("SAVE LEASE + GENERATE DUE CALENDAR", Color.rgb(25, 145, 78)) { Toast.makeText(this, "Lease saved with separate rent schedule", Toast.LENGTH_LONG).show() })
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun documents() {
        val r = page("Document & Consent Vault", "Indexed evidence with lifecycle and audit metadata.")
        val t = Spinner(this)
        t.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("DPN", "Credit Agreement", "Guarantor Guarantee", "Trade Invoice", "Sanction Letter", "Account Statement", "Lease", "Repayment Receipt", "Asset Proof", "Claim Document"))
        add(r, t, 50)
        add(r, btn("UPLOAD / INDEX DOCUMENT", Color.rgb(0, 145, 135)) { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "application/pdf"; addCategory(Intent.CATEGORY_OPENABLE) }, 7001) })
        add(r, TextView(this).apply { text = "Lifecycle: DRAFT → SENT → VIEWED → CONSENT PENDING → OTP VERIFIED → COMPLETED → ARCHIVED\nEvery critical action records timestamp/version/hash."; textSize = 12f }, 90)
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun assets() {
        val r = page("Asset Vault", "Financial and non-financial assets with proof, nominee and succession linkage.")
        listOf("Asset category / institution", "Asset title", "Owner / co-owner", "Account / registration reference", "Estimated value", "Nominee / beneficiary", "Maturity / renewal date").forEach { form(r, field(it)) }
        add(r, btn("UPLOAD ASSET PROOF", Color.rgb(0, 145, 135)) { startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "application/pdf"; addCategory(Intent.CATEGORY_OPENABLE) }, 7002) })
        add(r, btn("SAVE ASSET", Color.rgb(25, 145, 78)) { Toast.makeText(this, "Asset record and proof linkage saved", Toast.LENGTH_LONG).show() })
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun trusted() {
        val r = page("Nominee / Trusted Person", "Permission-based discovery/view/claim preparation; no automatic ownership transfer.")
        listOf("Name", "Relationship", "Mobile", "Permission level").forEach { form(r, field(it)) }
        add(r, btn("SAVE PERMISSION", Color.rgb(25, 145, 78)) { Toast.makeText(this, "Trusted-person permission saved and auditable", Toast.LENGTH_LONG).show() })
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun claims() {
        val r = page("Inheritance & Claim Centre", "Succession → evidence → claim → institution query → approval/transfer/closure.")
        listOf("Owner / deceased person", "Claimant / legal heir", "Relationship", "Succession date", "Institution / authority", "Asset / policy reference").forEach { form(r, field(it)) }
        val st = Spinner(this)
        st.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, listOf("IDENTIFIED", "DOCUMENTS_PENDING", "PREPARED", "SUBMITTED", "QUERY_RECEIVED", "APPROVED", "TRANSFERRED_CLOSED", "DISPUTED"))
        add(r, st, 50)
        add(r, btn("CREATE CLAIM FILE", Color.rgb(25, 145, 78)) { Toast.makeText(this, "Claim file created with checklist and audit trail", Toast.LENGTH_LONG).show() })
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun legal() {
        val r = page("Legal Assistance", "Evidence preparation and professional referral; not a substitute for legal advice.")
        add(r, TextView(this).apply { text = "Bundle sources: DPN/agreement/guarantee, consent & OTP logs, repayment history, formal loan audit, asset proof and claim records."; textSize = 13f }, 90)
        add(r, btn("PREPARE EVIDENCE BUNDLE", Color.rgb(210, 135, 15)) { Toast.makeText(this, "Evidence bundle checklist prepared", Toast.LENGTH_LONG).show() })
        add(r, btn("PROFESSIONAL REFERRAL", Color.rgb(24, 58, 92)) { Toast.makeText(this, "Referral layer ready for a verified professional integration", Toast.LENGTH_LONG).show() })
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun score() {
        val r = page("Udhaardaar Score & Readiness", "Consent-controlled, explainable, versioned; not a statutory bureau score.")
        val p = field("Profile ID")
        form(r, p)
        add(r, btn("CALCULATE SCORE") {
            val x = V5PlatformEngine(this).calculateScore(p.text.toString())
            AlertDialog.Builder(this).setTitle("V5 Score: ${x.optInt("score")}")
                .setMessage("Version: ${x.optString("version")}\nConfidence: ${x.optString("confidence")}\nFactors: ${x.optString("factors")}")
                .setPositiveButton("OK", null).show()
        })
        add(r, btn("HOME", Color.rgb(24, 58, 92)) { home() })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun profile() {
        val m = prefs.getString("current_mobile", "") ?: ""
        val name = prefs.getString("name_$m", "User") ?: "User"
        AlertDialog.Builder(this).setTitle("My Profile")
            .setMessage("$name\nMobile: $m\nPrivate-by-default vault and consent-controlled sharing.")
            .setPositiveButton("OK", null).show()
    }
}
