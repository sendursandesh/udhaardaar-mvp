package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding
import androidx.core.widget.NestedScrollView
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID

class RegisterCreditV3Activity : AppCompatActivity() {
    private lateinit var db: V3DatabaseHelper
    private lateinit var scroll: NestedScrollView
    private lateinit var partySpinner: Spinner
    private lateinit var typeSpinner: Spinner
    private lateinit var directionSpinner: Spinner
    private lateinit var methodSpinner: Spinner
    private lateinit var periodicitySpinner: Spinner
    private lateinit var lendingSpinner
    private lateinit var amount: EditText
    private lateinit var roi: EditText
    private lateinit var repayment: EditText
    private lateinit var start: EditText
    private lateinit var end: EditText
    private lateinit var invoice: EditText
    private lateinit var transactionRef: EditText
    private lateinit var notes: EditText
    private lateinit var consent: CheckBox
    private lateinit var calculation: TextView
    private lateinit var history: TextView

    private val partyIds = mutableListOf<Long>()
    private val partyPhones = mutableListOf<String>()
    private var sessionToken = ""
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

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
        lendingSpinner = findViewById(R.id.spinnerLendingMethod)
        amount = findViewById(R.id.etCreditAmount)
        roi = findViewById(R.id.etInterestRate)
        repayment = findViewById(R.id.etRepaymentAmount)
        start = findViewById(R.id.etCreditStartDate)
        end = findViewById(R.id.etCreditEndDate)
        invoice = findViewById(R.id.etInvoiceNumber)
        transactionRef = findViewById(R.id.etTransactionReference)
        notes = findViewById(R.id.etCreditNotes)
        consent = findViewById(R.id.cbConsent)
        calculation = findViewById(R.id.tvCalculation)
        history = findViewById(R.id.tvHistorySummary)

        setSpinner(typeSpinner, arrayOf("Personal Credit", "Business Credit", "Trade Credit", "Advance", "Rental / Lease", "Formal Loan", "Other"))
        setSpinner(directionSpinner, arrayOf("Credit Given", "Credit Received"))
        setSpinner(methodSpinner, arrayOf("EMI", "Principal + Interest", "Fixed Repayment", "Bullet / Maturity"))
        setSpinner(periodicitySpinner, arrayOf("Monthly", "Weekly", "Fortnightly", "Quarterly", "Half-yearly", "Yearly", "One-time"))
        setSpinner(lendingSpinner, arrayOf("Cash", "UPI", "NEFT", "RTGS", "IMPS", "Bank Transfer", "Cheque", "Other"))

        loadParties()
        sessionToken = UUID.randomUUID().toString()
        ConsentOtpGate.beginSession(sessionToken)

        start.setOnClickListener { pickDate(start) }
        end.setOnClickListener { pickDate(end) }
        methodSpinner.onItemSelectedListener = listener { calculateRepayment() }
        periodicitySpinner.onItemSelectedListener = listener { calculateRepayment() }
        partySpinner.onItemSelectedListener = listener {
            if (partySpinner.selectedItemPosition in partyIds.indices) {
                showHistory(partyIds[partySpinner.selectedItemPosition])
            }
        }

        listOf(amount, roi, repayment, invoice, transactionRef, notes).forEach { field ->
            field.setOnFocusChangeListener { view, hasFocus ->
                if (hasFocus) {
                    bringFieldAboveIme(view)
                    calculateRepayment()
                }
            }
        }
        consent.setOnCheckedChangeListener { _, checked -> ConsentOtpGate.grantConsent(checked) }
        findViewById<Button>(R.id.btnSaveCreditV3).setOnClickListener { validateAndSave() }
        installImeSafeScrolling()
    }

    private fun listener(action: () -> Unit) = object : AdapterView.OnItemSelectedListener {
        override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) = action()
    }

    private fun installImeSafeScrolling() {
        val base = scroll.paddingBottom
        ViewCompat.setOnApplyWindowInsetsListener(scroll) { view, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val system = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            view.updatePadding(bottom = base + maxOf(ime, system) + 32)
            insets
        }
        ViewCompat.requestApplyInsets(scroll)
    }

    private fun bringFieldAboveIme(view: View) {
        view.postDelayed({
            val visible = android.graphics.Rect()
            scroll.getWindowVisibleDisplayFrame(visible)
            val location = IntArray(2)
            view.getLocationInWindow(location)
            val distance = location[1] + view.height - (visible.bottom - 32)
            if (distance > 0) scroll.smoothScrollBy(0, distance + 48)
        }, 120)
    }

    private fun setSpinner(spinner: Spinner, values: Array<String>) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)
    }

    private fun loadParties() {
        partyIds.clear()
        partyPhones.clear()
        val names = mutableListOf<String>()
        db.readableDatabase.rawQuery(
            "SELECT id,name,COALESCE(mobile,'') FROM parties ORDER BY name COLLATE NOCASE",
            null
        ).use { cursor ->
            while (cursor.moveToNext()) {
                partyIds.add(cursor.getLong(0))
                names.add(cursor.getString(1))
                partyPhones.add(cursor.getString(2))
            }
        }
        if (names.isEmpty()) names.add("No borrower/party available — create a profile first")
        setSpinner(partySpinner, names.toTypedArray())
    }

    private fun showHistory(id: Long) {
        var count = 0
        var active = 0
        var settled = 0
        var principal = 0.0
        var repaid = 0.0
        db.readableDatabase.rawQuery(
            "SELECT c.principal_amount,c.status,COALESCE((SELECT SUM(r.amount) FROM repayments r WHERE r.credit_id=c.id),0) " +
                "FROM credits c INNER JOIN credit_parties cp ON cp.credit_id=c.id AND cp.party_id=? AND cp.role='BORROWER' " +
                "ORDER BY c.start_date DESC",
            arrayOf(id.toString())
        ).use { cursor ->
            while (cursor.moveToNext()) {
                count++
                principal += cursor.getDouble(0)
                repaid += cursor.getDouble(2)
                if (cursor.getString(1) == "ACTIVE") active++
                else if (cursor.getString(1) == "SETTLED") settled++
            }
        }
        history.text = if (count == 0) {
            "No previous credit or repayment history available."
        } else {
            "Previous credits: $count\nActive: $active   Settled: $settled\n" +
                "Total credit: ${money(principal)}\nTotal repaid: ${money(repaid)}\n" +
                "Current outstanding: ${money((principal - repaid).coerceAtLeast(0.0))}"
        }
    }

    private fun pickDate(target: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(
            this,
            { _, year, month, day ->
                calendar.set(year, month, day)
                target.setText(dateFormat.format(calendar.time))
                bringFieldAboveIme(target)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun periodsPerYear() = when (periodicitySpinner.selectedItem?.toString()) {
        "Weekly" -> 52
        "Fortnightly" -> 26
        "Quarterly" -> 4
        "Half-yearly" -> 2
        "Yearly" -> 1
        "One-time" -> 1
        else -> 12
    }

    private fun calculateRepayment() {
        val principal = amount.text.toString().toDoubleOrNull() ?: return
        val rate = roi.text.toString().toDoubleOrNull() ?: 0.0
        if (principal <= 0 || rate < 0) return
        val method = methodSpinner.selectedItem?.toString() ?: "EMI"
        val periods = periodsPerYear()
        if (method == "EMI") {
            val periodicRate = rate / (100.0 * periods)
            val emi = if (periodicRate == 0.0) {
                principal / periods
            } else {
                principal * periodicRate * Math.pow(1 + periodicRate, periods.toDouble()) /
                    (Math.pow(1 + periodicRate, periods.toDouble()) - 1)
            }
            repayment.setText(String.format(Locale.US, "%.2f", emi))
            calculation.text = "Indicative repayment: ${money(emi)} per ${periodicitySpinner.selectedItem.toString().lowercase(Locale.getDefault())}."
        } else {
            calculation.text = "Principal: ${money(principal)}\nIndicative annual interest: ${money(principal * rate / 100.0)}\nEnter the agreed repayment amount."
        }
    }

    private fun validateAndSave() {
        if (partyIds.isEmpty() || partySpinner.selectedItemPosition !in partyIds.indices) {
            toast("Create/select a party first")
            return
        }

        val creditType = typeSpinner.selectedItem?.toString() ?: "Other"
        val isFormal = creditType == "Formal Loan"

        if (!isFormal) {
            if (!consent.isChecked) {
                toast("For informal credit, counterparty consent confirmation is mandatory")
                return
            }
            if (!ConsentOtpGate.isRegistrationAuthorised()) {
                toast("OTP verification is required before registering this informal credit")
                return
            }
            if (partyPhones.getOrNull(partySpinner.selectedItemPosition).orEmpty().isBlank()) {
                toast("Selected party has no registered mobile number for OTP verification")
                return
            }
        }

        val partyId = partyIds[partySpinner.selectedItemPosition]
        val principal = amount.text.toString().toDoubleOrNull() ?: run {
            field(amount, "Enter a valid amount")
            return
        }
        val rate = roi.text.toString().toDoubleOrNull() ?: 0.0
        val repay = repayment.text.toString().toDoubleOrNull() ?: run {
            field(repayment, "Enter the agreed repayment amount")
            return
        }
        if (principal <= 0) {
            field(amount, "Amount must be greater than zero")
            return
        }
        if (rate < 0 || rate > 100) {
            field(roi, "ROI must be between 0% and 100%")
            return
        }
        if (repay <= 0) {
            field(repayment, "Repayment must be greater than zero")
            return
        }
        if (start.text.isBlank()) {
            field(start, "Select the start date")
            return
        }
        if (end.text.isNotBlank() && !validDateOrder(start.text.toString(), end.text.toString())) {
            field(end, "End date must be on/after start date")
            return
        }

        val direction = if (directionSpinner.selectedItemPosition == 0) "GIVEN" else "RECEIVED"
        val method = methodSpinner.selectedItem.toString()
            .uppercase(Locale.getDefault()).replace(" + ", "_").replace(" ", "_")
        val periodicity = periodicitySpinner.selectedItem.toString().uppercase(Locale.getDefault())
        val now = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.US).format(Calendar.getInstance().time)
        val nextDue = end.text.toString().ifBlank { null }
        val invoiceNumber = invoice.text.toString().trim().ifBlank { null }
        val transaction = transactionRef.text.toString().trim().ifBlank { null }
        val lending = lendingSpinner.selectedItem.toString().uppercase(Locale.getDefault()).replace(" ", "_")

        val id = db.addCredit(
            partyId,
            creditType,
            direction,
            principal,
            rate,
            method,
            repay,
            periodicity,
            start.text.toString(),
            end.text.toString().ifBlank { null },
            nextDue,
            notes.text.toString().trim(),
            now,
            0,
            invoiceNumber,
            null,
            lending,
            transaction
        )

        if (id <= 0) {
            toast("Unable to register credit. Nothing was saved.")
            return
        }

        // Due-date reminders are scheduled for both informal and formal credits.
        ReminderScheduler.schedule(this, id, start.text.toString(), 1)
        if (!isFormal) ConsentOtpGate.clearAfterRegistration()
        toast(if (isFormal) "Formal credit registered successfully" else "Credit registered successfully")
        finish()
    }

    private fun validDateOrder(first: String, second: String): Boolean = try {
        !dateFormat.parse(second).before(dateFormat.parse(first))
    } catch (_: Exception) {
        false
    }

    private fun field(view: EditText, message: String) {
        view.error = message
        view.requestFocus()
        bringFieldAboveIme(view)
    }

    private fun toast(message: String) = Toast.makeText(this, message, Toast.LENGTH_LONG).show()

    private fun money(value: Double) = "₹" + String.format(Locale.US, "%,.2f", value)
}
