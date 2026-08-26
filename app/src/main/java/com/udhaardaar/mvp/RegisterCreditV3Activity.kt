package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.Window
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterCreditV3Activity : AppCompatActivity() {
    private lateinit var db: V3DatabaseHelper
    private lateinit var scroll: ScrollView
    private lateinit var partySpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var directionSpinner: Spinner
    private lateinit var methodSpinner: Spinner
    private lateinit var periodicitySpinner: Spinner
    private lateinit var amount: EditText
    private lateinit var roi: EditText
    private lateinit var repayment: EditText
    private lateinit var start: EditText
    private lateinit var end: EditText
    private lateinit var invoice: EditText
    private lateinit var notes: EditText
    private lateinit var consent: CheckBox
    private lateinit var calculation: TextView
    private lateinit var history: TextView
    private var partyIds = mutableListOf<Long>()
    private val df = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        setContentView(R.layout.activity_register_credit_v3)
        db = V3DatabaseHelper(this)
        scroll = findViewById(R.id.registerScroll)
        partySpinner = findViewById(R.id.spinnerParty)
        typeSpinner = findViewById(R.id.spinnerCreditType)
        directionSpinner = findViewById(R.id.spinnerDirection)
        methodSpinner = findViewById(R.id.spinnerRepaymentMethod)
        periodicitySpinner = findViewById(R.id.spinnerPeriodicity)
        amount = findViewById(R.id.etCreditAmount)
        roi = findViewById(R.id.etInterestRate)
        repayment = findViewById(R.id.etRepaymentAmount)
        start = findViewById(R.id.etCreditStartDate)
        end = findViewById(R.id.etCreditEndDate)
        invoice = findViewById(R.id.etInvoiceNumber)
        notes = findViewById(R.id.etCreditNotes)
        consent = findViewById(R.id.cbConsent)
        calculation = findViewById(R.id.tvCalculation)
        history = findViewById(R.id.tvHistorySummary)

        installImeSafeScrolling()
        setSpinner(typeSpinner, arrayOf("Personal Credit", "Business Credit", "Trade Credit", "Advance", "Rental / Lease", "Other"))
        setSpinner(directionSpinner, arrayOf("Credit Given", "Credit Received"))
        setSpinner(methodSpinner, arrayOf("EMI", "Principal + Interest", "Fixed Repayment", "Bullet / Maturity"))
        setSpinner(periodicitySpinner, arrayOf("Monthly", "Weekly", "Fortnightly", "Quarterly", "Half-yearly", "Yearly", "One-time"))
        loadParties()
        start.setOnClickListener { pickDate(start) }
        end.setOnClickListener { pickDate(end) }

        val recalc = View.OnFocusChangeListener { _, hasFocus -> if (hasFocus) calculateRepayment() }
        amount.onFocusChangeListener = recalc
        roi.onFocusChangeListener = recalc
        methodSpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = calculateRepayment()
        }
        periodicitySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = calculateRepayment()
        }
        partySpinner.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                if (position in partyIds.indices) showHistory(partyIds[position])
            }
        }
        listOf(amount, roi, repayment, invoice, notes).forEach { field ->
            field.setOnFocusChangeListener { v, hasFocus -> if (hasFocus) bringFieldAboveIme(v) }
            field.setOnClickListener { if (field.isFocusable) bringFieldAboveIme(field) }
        }
        findViewById<Button>(R.id.btnSaveCreditV3).setOnClickListener { validateAndSave() }
    }

    private fun installImeSafeScrolling() {
        val baseBottom = scroll.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val system = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = baseBottom + maxOf(ime, system) + 32)
            insets
        }
        ViewCompat.requestApplyInsets(scroll)
    }

    private fun bringFieldAboveIme(view: View) {
        view.postDelayed({
            val rect = android.graphics.Rect()
            scroll.getWindowVisibleDisplayFrame(rect)
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val bottom = location[1] + view.height
            val safeBottom = rect.bottom - 32
            val delta = bottom - safeBottom
            if (delta > 0) scroll.smoothScrollBy(0, delta + 48)
        }, 120)
    }

    private fun setSpinner(spinner: Spinner, values: Array<String>) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)
    }

    private fun loadParties() {
        partyIds.clear()
        val names = mutableListOf<String>()
        db.readableDatabase.rawQuery("SELECT id,name FROM parties ORDER BY name COLLATE NOCASE", null).use { c ->
            while (c.moveToNext()) { partyIds.add(c.getLong(0)); names.add(c.getString(1)) }
        }
        if (names.isEmpty()) names.add("No borrower/party available — create a profile first")
        setSpinner(partySpinner, names.toTypedArray())
    }

    private fun showHistory(partyId: Long) {
        var count = 0; var active = 0; var settled = 0; var principal = 0.0; var repaid = 0.0
        db.readableDatabase.rawQuery("SELECT c.principal_amount,c.status,COALESCE((SELECT SUM(r.amount) FROM repayments r WHERE r.credit_id=c.id),0) FROM credits c WHERE c.party_id=? ORDER BY c.start_date DESC", arrayOf(partyId.toString())).use { c ->
            while (c.moveToNext()) { count++; principal += c.getDouble(0); repaid += c.getDouble(2); if (c.getString(1) == "ACTIVE") active++ else if (c.getString(1) == "SETTLED") settled++ }
        }
        history.text = if (count == 0) "No previous credit or repayment history available." else "Previous credits: $count\nActive: $active   Settled: $settled\nTotal credit: ${money(principal)}\nTotal repaid: ${money(repaid)}\nCurrent outstanding: ${money((principal - repaid).coerceAtLeast(0.0))}"
    }

    private fun pickDate(target: EditText) {
        val c = Calendar.getInstance()
        DatePickerDialog(this, { _, y, m, d -> c.set(y, m, d); target.setText(df.format(c.time)); bringFieldAboveIme(target) }, c.get(Calendar.YEAR), c.get(Calendar.MONTH), c.get(Calendar.DAY_OF_MONTH)).show()
    }

    private fun calculateRepayment() {
        val p = amount.text.toString().toDoubleOrNull() ?: return
        val r = roi.text.toString().toDoubleOrNull() ?: 0.0
        val method = methodSpinner.selectedItem?.toString() ?: "EMI"
        if (p <= 0 || r < 0) return
        if (method == "EMI") {
            val monthly = r / 1200.0
            val emi = if (monthly == 0.0) p / 12.0 else p * monthly * Math.pow(1 + monthly, 12.0) / (Math.pow(1 + monthly, 12.0) - 1)
            repayment.setText(String.format(Locale.US, "%.2f", emi))
            calculation.text = "Indicative EMI: ${money(emi)} per month. Review against agreed terms before registering."
        } else {
            val annualInterest = p * r / 100.0
            calculation.text = "Principal: ${money(p)}\nIndicative annual interest at ${String.format(Locale.US, "%.2f", r)}%: ${money(annualInterest)}\nEnter the agreed repayment amount."
        }
    }

    private fun validateAndSave() {
        if (partyIds.isEmpty() || partySpinner.selectedItemPosition !in partyIds.indices) return toast("Create/select a borrower or party first")
        if (!consent.isChecked) return toast("Consent is required before registering this credit")
        val p = amount.text.toString().toDoubleOrNull() ?: return field(amount, "Enter a valid amount")
        val rate = roi.text.toString().toDoubleOrNull() ?: 0.0
        val repay = repayment.text.toString().toDoubleOrNull() ?: return field(repayment, "Enter the agreed repayment amount")
        if (p <= 0) return field(amount, "Amount must be greater than zero")
        if (rate < 0 || rate > 100) return field(roi, "ROI must be between 0% and 100%")
        if (repay <= 0) return field(repayment, "Repayment must be greater than zero")
        if (start.text.isNullOrBlank()) return field(start, "Select the start date")
        if (end.text.isNotBlank() && !validDateOrder(start.text.toString(), end.text.toString())) return field(end, "End date must be on/after start date")
        val type = typeSpinner.selectedItem.toString()
        val direction = if (directionSpinner.selectedItemPosition == 0) "GIVEN" else "RECEIVED"
        val method = methodSpinner.selectedItem.toString().uppercase(Locale.getDefault()).replace(" + ", "_").replace(" ", "_")
        val periodicity = periodicitySpinner.selectedItem.toString().uppercase(Locale.getDefault())
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Calendar.getInstance().time)
        val id = db.addCredit(partyIds[partySpinner.selectedItemPosition], type, direction, p, rate, method, repay, periodicity, start.text.toString(), end.text.toString().ifBlank { null }, end.text.toString().ifBlank { null }, notes.text.toString().trim(), now, 0, invoice.text.toString().trim().ifBlank { null }, null)
        if (id > 0) { toast("Credit registered successfully"); finish() } else toast("Unable to register credit. Nothing was saved.")
    }

    private fun validDateOrder(a: String, b: String): Boolean = try { !df.parse(b).before(df.parse(a)) } catch (_: Exception) { false }
    private fun field(v: EditText, message: String) { v.error = message; v.requestFocus(); bringFieldAboveIme(v) }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
    private fun money(v: Double) = "₹" + String.format(Locale.US, "%,.2f", v)
}
