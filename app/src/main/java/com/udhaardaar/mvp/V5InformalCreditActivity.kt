package com.udhaardaar.mvp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.math.pow

class V5InformalCreditActivity : AppCompatActivity() {
    private val repo by lazy { V5WorkflowRepository(this) }
    private val consent by lazy { V5OtpConsentService(this) }
    private val scanner = registerForActivityResult(ScanContract()) { result ->
        if (!result.contents.isNullOrBlank()) {
            qrRaw = result.contents
            applyQr(result.contents)
        }
    }
    private var qrRaw = ""
    private var documentConsentId = ""
    private var selectedProfileId = ""
    private var draftCreditId = ""
    private lateinit var name: EditText
    private lateinit var vendor: EditText
    private lateinit var invoice: EditText
    private lateinit var date: EditText
    private lateinit var principal: EditText
    private lateinit var roi: EditText
    private lateinit var start: EditText
    private lateinit var end: EditText
    private lateinit var tenor: EditText
    private lateinit var type: Spinner
    private lateinit var direction: Spinner
    private lateinit var method: Spinner
    private lateinit var scroll: ScrollView

    private fun edit(hint: String) = EditText(this).apply {
        this.hint = hint
        setSingleLine(true)
        setPadding(16, 10, 16, 10)
        imeOptions = android.view.inputmethod.EditorInfo.IME_ACTION_NEXT
    }

    private fun add(root: LinearLayout, view: View, height: Int = 58) {
        root.addView(view, LinearLayout.LayoutParams(-1, height).apply { setMargins(0, 5, 0, 5) })
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        showForm()
    }

    private fun showForm() {
        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(24, 18, 24, 28)
        }
        scroll = ScrollView(this).apply {
            isFillViewport = true
            isSmoothScrollingEnabled = true
            addView(root)
        }
        add(root, TextView(this).apply { text = "Udhaardaar V5 • Informal Credit"; textSize = 23f })
        add(root, TextView(this).apply { text = "1 Identify → 2 Terms → 3 Document consent → 4 Counterparty OTP → 5 Register"; textSize = 13f })
        add(root, Button(this).apply {
            text = "SCAN QR — AUTO-FILL CREDIT"
            setOnClickListener {
                scanner.launch(ScanOptions().apply {
                    setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                    setPrompt("Scan credit / invoice QR code")
                    setBeepEnabled(true)
                    setOrientationLocked(false)
                })
            }
        })
        name = edit("Borrower / party name or profile ID *")
        vendor = edit("Vendor / seller")
        invoice = edit("Invoice / bill number")
        date = edit("Invoice / transaction date")
        type = Spinner(this).apply { adapter = ArrayAdapter(this@V5InformalCreditActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Personal Credit", "Business Credit", "Trade Credit", "Advance", "Other")) }
        direction = Spinner(this).apply { adapter = ArrayAdapter(this@V5InformalCreditActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("Credit Given", "Credit Received")) }
        principal = edit("Principal / invoice amount *").apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        roi = edit("Annual ROI %").apply { inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
        method = Spinner(this).apply { adapter = ArrayAdapter(this@V5InformalCreditActivity, android.R.layout.simple_spinner_dropdown_item, arrayOf("EMI", "Principal + Interest", "Bullet / Full payment")) }
        tenor = edit("Repayment period in months *").apply { setText("12"); inputType = InputType.TYPE_CLASS_NUMBER }
        start = edit("Start date *")
        end = edit("End date (auto-calculated)").apply { isFocusable = false; isClickable = false }
        listOf<View>(name, vendor, invoice, date, type, direction, principal, roi, method, tenor, start, end).forEach { add(root, it) }
        start.setText(today())
        date.setText(today())
        updateEnd()
        start.setOnClickListener { pickDate(start) }
        date.setOnClickListener { pickDate(date) }
        tenor.setOnFocusChangeListener { _, _ -> updateEnd() }
        add(root, Button(this).apply { text = "CALCULATE END DATE"; setOnClickListener { updateEnd() } }, 50)
        add(root, Button(this).apply {
            text = "BORROWER PROFILES / SEARCH / CREATE"
            setOnClickListener { startActivityForResult(Intent(this@V5InformalCreditActivity, V5BorrowerActivity::class.java), 7001) }
        })
        add(root, Button(this).apply {
            text = "DPN / GUARANTOR CONSENT WORKFLOW"
            setOnClickListener { ensureDraft(); startActivity(Intent(this@V5InformalCreditActivity, V5GuarantorConsentActivity::class.java).putExtra("creditId", draftCreditId)) }
        })
        add(root, Button(this).apply { text = "CREATE / REVIEW DOCUMENT + GET CONSENT"; setOnClickListener { startDocumentConsent() } })
        add(root, Button(this).apply {
            text = "SEND OTP CONSENT + REGISTER"
            setOnClickListener {
                if (documentConsentId.isBlank()) {
                    Toast.makeText(this@V5InformalCreditActivity, "Document must be created, reviewed and consented first", Toast.LENGTH_LONG).show()
                    startDocumentConsent()
                } else registerAfterConsent()
            }
        })
        add(root, Button(this).apply { text = "BACK"; setOnClickListener { finish() } })
        listOf<View>(name, vendor, invoice, date, principal, roi, tenor, start).forEach { field ->
            field.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) v.post { scroll.smoothScrollTo(0, (v.bottom - scroll.height / 3).coerceAtLeast(0)) } }
        }
        setContentView(scroll)
    }

    private fun ensureDraft() { if (draftCreditId.isBlank()) draftCreditId = "CR-DRAFT-${System.currentTimeMillis()}" }
    private fun today() = SimpleDateFormat("yyyy-MM-dd", Locale.US).format(Date())

    private fun pickDate(target: EditText) {
        val base = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(target.text.toString()) }.getOrNull() ?: Date()
        val calendar = Calendar.getInstance().apply { time = base }
        DatePickerDialog(this, { _, y, m, d ->
            target.setText(String.format(Locale.US, "%04d-%02d-%02d", y, m + 1, d))
            if (target === start) updateEnd()
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun updateEnd() {
        val months = tenor.text.toString().toIntOrNull() ?: return
        val startDate = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).parse(start.text.toString()) }.getOrNull() ?: return
        val calendar = Calendar.getInstance().apply { time = startDate; add(Calendar.MONTH, months) }
        end.setText(SimpleDateFormat("yyyy-MM-dd", Locale.US).format(calendar.time))
    }

    private fun applyQr(raw: String) {
        try {
            val json = JSONObject(raw)
            name.setText(first(json, "party", "vendor", "vendor_name", "seller", "supplier", "merchant"))
            vendor.setText(first(json, "vendor", "vendor_name", "seller", "supplier", "merchant", "party"))
            invoice.setText(first(json, "invoice", "invoice_no", "invoice_number", "bill_no", "bill_number"))
            date.setText(first(json, "date", "invoice_date", "transaction_date"))
            principal.setText(first(json, "amount", "total", "total_amount", "invoice_amount", "grand_total"))
        } catch (_: Exception) {
            val map = raw.split('&', '\n', ';').mapNotNull { part ->
                val pair = part.split('=', ':', limit = 2)
                if (pair.size == 2) pair[0].trim().lowercase() to pair[1].trim() else null
            }.toMap()
            vendor.setText(map["vendor"] ?: map["seller"] ?: map["supplier"] ?: "")
            name.setText(map["party"] ?: vendor.text.toString())
            invoice.setText(map["invoice"] ?: map["invoice_no"] ?: "")
            date.setText(map["date"] ?: map["invoice_date"] ?: "")
            principal.setText(map["amount"] ?: map["total"] ?: map["total_amount"] ?: "")
        }
        Toast.makeText(this, "QR data captured. Review all fields before consent.", Toast.LENGTH_LONG).show()
    }

    private fun first(json: JSONObject, vararg keys: String): String = keys.firstNotNullOfOrNull { key ->
        if (json.has(key) && !json.isNull(key)) json.optString(key).takeIf { it.isNotBlank() } else null
    } ?: ""

    private fun startDocumentConsent() {
        ensureDraft()
        val party = name.text.toString().trim()
        val amount = principal.text.toString().trim().toDoubleOrNull()
        val months = tenor.text.toString().toIntOrNull() ?: 0
        if (party.length < 2 || amount == null || amount <= 0 || months !in 1..240) {
            Toast.makeText(this, "Enter valid party, amount and repayment period first", Toast.LENGTH_LONG).show(); return
        }
        updateEnd()
        val docId = "DPN-${System.currentTimeMillis()}"
        val content = "UDHAARDAAR DIGITAL CREDIT DOCUMENT\nVersion: 1\nDraft Credit: $draftCreditId\nParty: $party\nType: ${type.selectedItem}\nDirection: ${direction.selectedItem}\nPrincipal: ₹$amount\nROI: ${roi.text}%\nMethod: ${method.selectedItem}\nStart: ${start.text}\nEnd: ${end.text}\nVendor: ${vendor.text}\nInvoice: ${invoice.text}"
        val store = V5LocalStore(this)
        store.add("documents", JSONObject().apply { put("id", docId); put("creditId", draftCreditId); put("type", "DIGITAL_CREDIT_DPN"); put("status", "CONSENT_PENDING"); put("version", 1); put("createdAt", System.currentTimeMillis()); put("content", content) })
        documentConsentId = consent.issue(docId, "DIGITAL_CREDIT_DOCUMENT_CONSENT", party)
        val otp = store.find("consents", documentConsentId)?.optString("otp", "") ?: ""
        val input = edit("Enter 6-digit document-consent OTP")
        val dialog = AlertDialog.Builder(this).setTitle("Review digital document before credit").setMessage("$content\n\nDemo OTP: $otp\nProduction SMS gateway is required for live delivery.").setView(input).setNegativeButton("CANCEL", null).setPositiveButton("CONSENT", null).create()
        dialog.setOnShowListener {
            dialog.getButton(-1).setOnClickListener {
                if (consent.verify(documentConsentId, input.text.toString())) {
                    store.find("documents", docId)?.let { it.put("status", "COMPLETED"); it.put("consentedAt", System.currentTimeMillis()); store.replace("documents", it) }
                    repo.appendAudit(docId, "DIGITAL_CREDIT_DOCUMENT_CONSENTED_BEFORE_REGISTRATION", party, "creditId=$draftCreditId; version=1")
                    Toast.makeText(this, "Document consent completed. Credit may now proceed to counterparty OTP.", Toast.LENGTH_LONG).show()
                    dialog.dismiss()
                } else input.error = "Incorrect OTP"
            }
        }
        dialog.show()
    }

    private fun registerAfterConsent() {
        ensureDraft()
        val amount = principal.text.toString().trim().toDoubleOrNull()
        val months = tenor.text.toString().toIntOrNull() ?: 0
        val rate = roi.text.toString().trim().removeSuffix("%").toDoubleOrNull() ?: 0.0
        if (selectedProfileId.isBlank()) { Toast.makeText(this, "Select a borrower profile before registration", Toast.LENGTH_LONG).show(); return }
        if (name.text.trim().length < 2 || amount == null || amount <= 0 || months !in 1..240 || rate !in 0.0..100.0) { Toast.makeText(this, "Complete valid party, amount, ROI and repayment period", Toast.LENGTH_LONG).show(); return }
        updateEnd()
        val q = rate / 1200.0
        val emi = if (method.selectedItem.toString() == "EMI" && q > 0) amount * q * (1 + q).pow(months) / ((1 + q).pow(months) - 1) else amount / months
        val total = if (method.selectedItem.toString() == "EMI" && q > 0) emi * months else if (method.selectedItem.toString() == "EMI") amount else amount + amount * rate * months / 1200.0
        val id = draftCreditId
        val party = name.text.toString().trim()
        val cid = consent.issue(id, "INFORMAL_CREDIT_BORROWER_CONSENT", party)
        val otp = V5LocalStore(this).find("consents", cid)?.optString("otp", "") ?: ""
        val input = edit("Enter 6-digit counterparty OTP")
        val dialog = AlertDialog.Builder(this).setTitle("Counterparty consent").setMessage("Digital document already consented.\nDemo OTP: $otp\nReview final terms before confirming.").setView(input).setNegativeButton("CANCEL", null).setPositiveButton("CONFIRM", null).create()
        dialog.setOnShowListener {
            dialog.getButton(-1).setOnClickListener {
                if (consent.verify(cid, input.text.toString())) {
                    repo.saveCredit(id, selectedProfileId, type.selectedItem.toString(), direction.selectedItem.toString(), amount, rate, method.selectedItem.toString(), start.text.toString(), end.text.toString(), vendor.text.toString(), invoice.text.toString(), qrRaw, cid, System.currentTimeMillis(), total)
                    V5LocalStore(this).find("credits", id)?.let { it.put("borrowerName", party); it.put("documentConsentId", documentConsentId); it.put("borrowerProfileId", selectedProfileId); V5LocalStore(this).replace("credits", it) }
                    repo.appendAudit(id, "CREDIT_REGISTERED_AFTER_DOCUMENT_AND_COUNTERPARTY_CONSENT", party, "profileId=$selectedProfileId; documentConsent=$documentConsentId; totalPayable=$total")
                    Toast.makeText(this, "Document consent and counterparty OTP verified; credit registered", Toast.LENGTH_LONG).show()
                    dialog.dismiss(); finish()
                } else input.error = "Incorrect OTP"
            }
        }
        dialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == 7001 && resultCode == RESULT_OK) {
            selectedProfileId = data?.getStringExtra("profileId").orEmpty()
            val selectedName = data?.getStringExtra("profileName").orEmpty()
            if (selectedName.isNotBlank()) name.setText(selectedName)
            Toast.makeText(this, "Borrower profile selected: $selectedName", Toast.LENGTH_SHORT).show()
        }
    }
}
