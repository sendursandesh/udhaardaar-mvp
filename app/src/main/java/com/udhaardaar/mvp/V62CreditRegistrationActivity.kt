package com.udhaardaar.mvp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.util.Locale
import kotlin.math.pow

class V62CreditRegistrationActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val draft by lazy { getSharedPreferences("v62_credit_draft", MODE_PRIVATE) }
    private val d get() = resources.displayMetrics.density
    private lateinit var root: LinearLayout
    private var selected: JSONObject? = null
    private var step = 1
    private var relationshipId = ""
    private var docUri = ""
    private var documentId = ""
    private var invoiceText = ""

    private fun add(v: android.view.View, gap: Int = 7) = ArthSaathiV62Design.add(root, v, gap)
    private fun input(h: String) = ArthSaathiV62Design.input(this, h)

    override fun onCreate(b: Bundle?) {
        super.onCreate(b)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        relationshipId = draft.getString("relationshipId", "").orEmpty()
        render()
    }

    private fun render() {
        root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(ArthSaathiV62Design.dp(16, d), ArthSaathiV62Design.dp(10, d), ArthSaathiV62Design.dp(16, d), ArthSaathiV62Design.dp(28, d))
            setBackgroundColor(ArthSaathiV62Design.BG)
        }
        add(ArthSaathiV62Design.title(this, "Register Financial Relationship", "Search • Verify • Agree • Record"), 2)
        add(ArthSaathiV62Design.text(this, "Counterparty history is disclosed only after consent + OTP. Supporting evidence, guarantor details and the promissory note are completed before separate registration consent.", 10f, ArthSaathiV62Design.MUTED), 5)
        add(ArthSaathiV62Design.text(this, "STEP $step OF 4", 10f, ArthSaathiV62Design.TEAL, true), 8)
        when (step) { 1 -> profile(); 2 -> terms(); 3 -> evidence(); else -> consent() }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun profile() {
        add(ArthSaathiV62Design.section(this, "1 • COUNTERPARTY"), 8)
        add(ArthSaathiV62Design.button(this, "SEARCH / CREATE COUNTERPARTY", ArthSaathiV62Design.TEAL) { chooseCounterparty() }, 10)
        selected?.let { cp ->
            add(ArthSaathiV62Design.text(this, "Selected: ${cp.optString("name")} • ${cp.optString("mobile")}", 14f, ArthSaathiV62Design.NAVY, true), 7)
            if (draft.getBoolean("historyConsentVerified", false)) add(ArthSaathiV62Design.text(this, historySummary(cp.optString("id")), 11f, ArthSaathiV62Design.NAVY), 6)
            else add(ArthSaathiV62Design.button(this, "REQUEST HISTORY-CONSENT OTP", ArthSaathiV62Design.TEAL) { historyConsent() }, 7)
        }
        add(ArthSaathiV62Design.button(this, "CONTINUE TO TERMS →", ArthSaathiV62Design.GREEN) {
            when {
                selected == null -> Toast.makeText(this, "Select or create a counterparty first.", Toast.LENGTH_SHORT).show()
                !draft.getBoolean("historyConsentVerified", false) -> Toast.makeText(this, "History consent OTP is mandatory.", Toast.LENGTH_LONG).show()
                else -> { step = 2; render() }
            }
        }, 12)
    }

    private fun chooseCounterparty() {
        val q = input("Search name / mobile / PAN / Aadhaar / GSTIN / profile ID")
        val list = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        val wrap = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(q); addView(list) }
        val dlg = AlertDialog.Builder(this).setTitle("Registered counterparty").setView(wrap).setNegativeButton("CANCEL", null).create()
        fun refresh() {
            list.removeAllViews()
            val rows = V62Integration.findCounterparties(this, q.text.toString()).take(15)
            if (rows.isEmpty()) {
                list.addView(ArthSaathiV62Design.button(this, "CREATE NEW COUNTERPARTY IN THIS CREDIT", ArthSaathiV62Design.TEAL) { dlg.dismiss(); createCounterparty() })
            } else rows.forEach { p ->
                list.addView(ArthSaathiV62Design.button(this, "${p.optString("name")} • ${p.optString("mobile")}", ArthSaathiV62Design.NAVY) { dlg.dismiss(); selectCounterparty(p) })
            }
        }
        q.addTextChangedListener(object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, c: Int, x: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { refresh() }
            override fun afterTextChanged(e: android.text.Editable?) {}
        })
        dlg.show(); refresh()
    }

    private fun selectCounterparty(cp: JSONObject) {
        selected = cp
        relationshipId = V62Store.id("REL")
        draft.edit().clear().putString("relationshipId", relationshipId).putBoolean("historyConsentVerified", false).apply()
        store.add(V62Store.RELATIONSHIPS, JSONObject().apply {
            put("id", relationshipId)
            put("ownerUserId", V62Integration.currentUserId(this@V62CreditRegistrationActivity))
            put("counterpartyId", cp.optString("id"))
            put("status", "DRAFT")
            put("type", "PERSONAL_CREDIT")
            put("createdAt", System.currentTimeMillis())
        })
        render()
    }

    private fun createCounterparty() {
        val n = input("Name / business name *")
        val m = input("Mobile *")
        val pan = input("PAN")
        val aadhaar = input("Aadhaar")
        val gst = input("GSTIN")
        val w = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(n); addView(m); addView(pan); addView(aadhaar); addView(gst) }
        AlertDialog.Builder(this).setTitle("Create counterparty inside this credit").setView(w).setNegativeButton("CANCEL", null).setPositiveButton("CREATE") { _, _ ->
            if (relationshipId.isBlank()) relationshipId = V62Store.id("REL")
            val cp = V62Integration.createCounterparty(this, n.text.toString(), m.text.toString(), pan.text.toString(), aadhaar.text.toString(), gst.text.toString(), relationshipId)
            if (cp == null) Toast.makeText(this, "Enter a valid name and Indian mobile number.", Toast.LENGTH_LONG).show() else selectCounterparty(cp)
        }.show()
    }

    private fun historyConsent() {
        val cp = selected ?: return
        if (cp.optString("mobile").isBlank()) { Toast.makeText(this, "Valid counterparty mobile is required.", Toast.LENGTH_LONG).show(); return }
        val otp = (100000..999999).random()
        val entry = input("Enter 6-digit OTP")
        val dlg = AlertDialog.Builder(this).setTitle("History-sharing consent").setMessage("Consent is required to disclose recorded history, outstanding behaviour and the internal ArthSaathi score.\n\nDemo OTP: $otp").setView(entry).setNegativeButton("DECLINE", null).setPositiveButton("VERIFY", null).create()
        dlg.setOnShowListener { dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (entry.text.toString() == otp.toString()) {
                dlg.dismiss(); draft.edit().putBoolean("historyConsentVerified", true).apply()
                V62Integration.recordConsent(this, cp.optString("id"), relationshipId, "HISTORY_SHARING", true)
                render()
            } else entry.error = "Incorrect OTP"
        } }
        dlg.show()
    }

    private fun historySummary(id: String): String {
        val rels = store.all(V62Store.RELATIONSHIPS).filter { it.optString("counterpartyId") == id && it.optString("id") != relationshipId }
        val repayments = store.all(V62Store.REPAYMENTS).filter { it.optString("counterpartyId") == id || rels.any { r -> r.optString("id") == it.optString("relationshipId") } }
        val exposure = rels.sumOf { it.optDouble("amount", it.optDouble("principal", 0.0)) }
        val repaid = repayments.sumOf { it.optDouble("principal", it.optDouble("amount", 0.0)) }
        return "CONSENTED HISTORY\nRelationships: ${rels.size}\nRecorded exposure: ₹${"%.2f".format(Locale.US, exposure)}\nRecorded repayments: ₹${"%.2f".format(Locale.US, repaid)}\nArthSaathi Score (internal): ${V62Integration.score(this, id)}"
    }

    private fun terms() {
        add(ArthSaathiV62Design.section(this, "2 • TERMS & REPAYMENT"), 8)
        val type = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Personal / Hand Loan", "Business Trade Credit — Supplier / Seller", "Rental / Lease Credit", "Other")) }
        val amount = input("Principal / invoice value ₹ *")
        val roi = input("Interest rate % — 0 = interest-free")
        val method = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI", "Principal + Interest", "Bullet / single payment")) }
        val period = Spinner(this).apply { adapter = ArrayAdapter(this@V62CreditRegistrationActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Monthly", "Quarterly", "Half-yearly", "Yearly", "One-time")) }
        val start = input("Start date DD/MM/YYYY")
        val end = input("End / final payment date DD/MM/YYYY")
        val emi = input("Auto-calculated EMI").apply { isFocusable = false }
        listOf(type, amount, roi, method, period, start, end, emi).forEach { add(it, 4) }
        fun calc() {
            if (method.selectedItemPosition != 0) { emi.setText(""); return }
            val p = amount.text.toString().replace(",", "").toDoubleOrNull()
            val r = roi.text.toString().toDoubleOrNull() ?: 0.0
            val sd = parse(start.text.toString()); val ed = parse(end.text.toString())
            val freq = when (period.selectedItemPosition) { 0 -> 12; 1 -> 4; 2 -> 2; 3 -> 1; else -> 0 }
            if (p == null || p <= 0 || sd == null || ed == null || !ed.after(sd) || freq == 0) { emi.setText(""); return }
            val n = maxOf(1, Math.round(((ed.time - sd.time).toDouble() / (1000 * 60 * 60 * 24 * 30.4375)) / (12.0 / freq)).toInt())
            val rate = r / 100.0 / freq
            val pay = if (rate == 0.0) p / n else p * rate * (1 + rate).pow(n) / ((1 + rate).pow(n) - 1)
            emi.setText(String.format(Locale.US, "%.2f", pay))
        }
        val tw = object : android.text.TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) {}
            override fun onTextChanged(s: CharSequence?, a: Int, b: Int, c: Int) { calc() }
            override fun afterTextChanged(e: android.text.Editable?) {}
        }
        amount.addTextChangedListener(tw); roi.addTextChangedListener(tw); start.addTextChangedListener(tw); end.addTextChangedListener(tw)
        add(ArthSaathiV62Design.button(this, "CONTINUE →", ArthSaathiV62Design.GREEN) {
            val p = amount.text.toString().replace(",", "").toDoubleOrNull()
            val sd = parse(start.text.toString()); val ed = parse(end.text.toString())
            if (p == null || p <= 0 || sd == null || ed == null || !sd.before(ed)) {
                Toast.makeText(this, "Enter valid amount and dates.", Toast.LENGTH_LONG).show(); return@button
            }
            draft.edit().putString("amount", amount.text.toString()).putString("roi", roi.text.toString()).putString("method", method.selectedItem.toString()).putString("period", period.selectedItem.toString()).putString("start", start.text.toString()).putString("end", end.text.toString()).putString("emi", emi.text.toString()).putString("type", type.selectedItem.toString()).apply()
            step = 3; render()
        }, 12)
    }

    private fun evidence() {
        add(ArthSaathiV62Design.section(this, "3 • GUARANTOR & SUPPORTING DOCUMENT"), 8)
        val gName = input("Guarantor name (optional)")
        val gMobile = input("Guarantor mobile")
        val gConsent = CheckBox(this).apply { text = "Guarantor consent acknowledged (only if a guarantor is added)" }
        add(gName, 4); add(gMobile, 4); add(gConsent, 7)
        add(ArthSaathiV62Design.button(this, "SAVE GUARANTOR DETAILS", ArthSaathiV62Design.BLUE) {
            if (gName.text.isNotBlank() && !gMobile.text.toString().matches(Regex("[6-9][0-9]{9}"))) Toast.makeText(this, "Enter a valid guarantor mobile.", Toast.LENGTH_LONG).show()
            else { draft.edit().putString("guarantorName", gName.text.toString().trim()).putString("guarantorMobile", gMobile.text.toString().trim()).putBoolean("guarantorConsent", gConsent.isChecked || gName.text.isBlank()).apply(); Toast.makeText(this, "Guarantor details saved.", Toast.LENGTH_SHORT).show() }
        }, 6)
        add(ArthSaathiV62Design.text(this, "Attach the invoice/supporting evidence before final consent. Trade-credit invoices can be OCR-read locally and remain reviewable.", 10f, ArthSaathiV62Design.MUTED), 8)
        add(ArthSaathiV62Design.button(this, "SCAN / ATTACH INVOICE OR EVIDENCE", ArthSaathiV62Design.TEAL) { pickDoc() }, 8)
        if (docUri.isNotBlank()) add(ArthSaathiV62Design.text(this, "Original retained: $docUri\nOCR text: ${if (invoiceText.isBlank()) "pending" else "available for review"}", 10f, ArthSaathiV62Design.GREEN, true), 6)
        add(ArthSaathiV62Design.button(this, "REVIEW PROMISSORY NOTE →", ArthSaathiV62Design.GREEN) {
            val guarantorRequired = draft.getString("guarantorName", "").orEmpty().isNotBlank()
            if (docUri.isBlank()) Toast.makeText(this, "Attach supporting evidence first.", Toast.LENGTH_LONG).show()
            else if (guarantorRequired && !draft.getBoolean("guarantorConsent", false)) Toast.makeText(this, "Guarantor acknowledgement is required.", Toast.LENGTH_LONG).show()
            else { step = 4; render() }
        }, 12)
    }

    private fun consent() {
        add(ArthSaathiV62Design.section(this, "4 • PROMISSORY NOTE & FINAL OTP"), 8)
        val cp = selected ?: run { Toast.makeText(this, "Counterparty missing.", Toast.LENGTH_SHORT).show(); return }
        val note = "I promise to pay ${cp.optString("name")} an amount of ₹${draft.getString("amount", "").orEmpty()} under ${draft.getString("method", "").orEmpty()} terms.\n\nInterest: ${draft.getString("roi", "0").orEmpty()}%\nPeriodicity: ${draft.getString("period", "").orEmpty()}\nFinal date: ${draft.getString("end", "").orEmpty()}\nCalculated EMI: ${draft.getString("emi", "").orEmpty()}\nGuarantor: ${draft.getString("guarantorName", "None").orEmpty()}"
        add(ArthSaathiV62Design.text(this, "PROMISSORY NOTE\n\n$note", 13f, ArthSaathiV62Design.NAVY), 8)
        add(ArthSaathiV62Design.button(this, "REQUEST CONSENT OTP", ArthSaathiV62Design.TEAL) { registerAfterOtp(note) }, 10)
    }

    private fun registerAfterOtp(note: String) {
        val cp = selected ?: return
        val otp = (100000..999999).random()
        val entry = input("Enter 6-digit OTP")
        val dlg = AlertDialog.Builder(this).setTitle("Registration consent OTP").setMessage("Review the promissory note before authorising registration.\n\nDemo OTP: $otp").setView(entry).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY & REGISTER", null).create()
        dlg.setOnShowListener { dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            if (entry.text.toString() == otp.toString()) { dlg.dismiss(); V62Integration.recordConsent(this, cp.optString("id"), relationshipId, "CREDIT_REGISTRATION", true); register(note, cp) }
            else entry.error = "Invalid OTP"
        } }
        dlg.show()
    }

    private fun register(note: String, cp: JSONObject) {
        val principal = draft.getString("amount", "0").orEmpty().toDoubleOrNull() ?: 0.0
        val creditType = draft.getString("type", "").orEmpty()
        val rel = JSONObject().apply {
            put("id", relationshipId); put("ownerUserId", V62Integration.currentUserId(this@V62CreditRegistrationActivity)); put("counterpartyId", cp.optString("id"))
            put("ownerRole", "USER"); put("counterpartyRole", "BORROWER")
            put("type", when { creditType.contains("Trade") -> "TRADE_CREDIT"; creditType.contains("Rental") -> "RENTAL"; else -> "PERSONAL_CREDIT" })
            put("principal", principal); put("amount", principal)
            put("roiPercent", draft.getString("roi", "0").orEmpty().toDoubleOrNull() ?: 0.0); put("roi", draft.getString("roi", "0").orEmpty().toDoubleOrNull() ?: 0.0)
            put("repaymentMode", draft.getString("method", "EMI").orEmpty()); put("method", draft.getString("method", "EMI").orEmpty())
            put("periodicity", draft.getString("period", "Monthly").orEmpty()); put("period", draft.getString("period", "Monthly").orEmpty())
            put("startDate", draft.getString("start", "").orEmpty()); put("start", draft.getString("start", "").orEmpty()); put("endDate", draft.getString("end", "").orEmpty()); put("end", draft.getString("end", "").orEmpty())
            put("emi", draft.getString("emi", "0").orEmpty().toDoubleOrNull() ?: 0.0)
            put("guarantorName", draft.getString("guarantorName", "").orEmpty()); put("guarantorMobile", draft.getString("guarantorMobile", "").orEmpty())
            put("supportingDocumentId", documentId); put("supportingDocumentUri", docUri); put("invoiceOcrText", invoiceText.take(12000))
            put("promissoryNote", note); put("historyConsent", "VERIFIED"); put("registrationConsent", "VERIFIED"); put("outstanding", principal); put("status", "ACTIVE"); put("createdAt", System.currentTimeMillis())
        }
        store.replace(V62Store.RELATIONSHIPS, rel)
        V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, relationshipId))
        draft.edit().clear().apply()
        Toast.makeText(this, "Financial relationship registered successfully", Toast.LENGTH_LONG).show()
        finish()
    }

    private fun pickDoc() {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { type = "*/*"; putExtra(Intent.EXTRA_ALLOW_MULTIPLE, false); addCategory(Intent.CATEGORY_OPENABLE) }, 77)
    }

    override fun onActivityResult(r: Int, c: Int, data: Intent?) {
        super.onActivityResult(r, c, data)
        if (r == 77 && c == Activity.RESULT_OK && data?.data != null) {
            val uri = data.data!!
            val retained = runCatching { ArthSaathiV62Core.saveDocument(this, uri, "CREDIT_SUPPORTING") }.getOrNull()
            docUri = retained?.optString("uri").orEmpty().ifBlank { uri.toString() }
            documentId = retained?.optString("id").orEmpty()
            V62DocumentScanner.scan(this, uri, { text -> invoiceText = text; runOnUiThread { render() } }, { e -> runOnUiThread { Toast.makeText(this, "OCR failed: ${e.message}", Toast.LENGTH_LONG).show(); render() } })
        }
    }

    private fun parse(s: String) = runCatching { java.text.SimpleDateFormat("dd/MM/yyyy", Locale.US).apply { isLenient = false }.parse(s) }.getOrNull()
}
