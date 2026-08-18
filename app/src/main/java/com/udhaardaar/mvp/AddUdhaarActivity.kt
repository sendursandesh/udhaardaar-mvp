package com.udhaardaar.mvp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.random.Random

class AddUdhaarActivity : AppCompatActivity() {
    private lateinit var databaseHelper: UdhaarDatabaseHelper
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var emiAmount: EditText
    private var borrowerPhotoUri = ""
    private var guarantorPhotoUri = ""
    private var invoiceUri = ""
    private var otpVerified = false
    private var generatedOtp = ""
    private var photoTarget = 0
    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_udhaar)
        databaseHelper = UdhaarDatabaseHelper(this)

        val creditType = findViewById<Spinner>(R.id.spinnerCreditType)
        val personName = findViewById<EditText>(R.id.etPersonName)
        val mobile = findViewById<EditText>(R.id.etBorrowerMobile)
        val address = findViewById<EditText>(R.id.etBorrowerAddress)
        val aadhaar = findViewById<EditText>(R.id.etBorrowerAadhaar)
        val pan = findViewById<EditText>(R.id.etBorrowerPan)
        val amount = findViewById<EditText>(R.id.etAmount)
        val roi = findViewById<EditText>(R.id.etRoi)
        val repaymentMethod = findViewById<Spinner>(R.id.spinnerRepaymentMethod)
        val periodicity = findViewById<Spinner>(R.id.spinnerPeriodicity)
        val guarantor = findViewById<Spinner>(R.id.spinnerGuarantor)
        val guarantorName = findViewById<EditText>(R.id.etGuarantorName)
        val guarantorMobile = findViewById<EditText>(R.id.etGuarantorMobile)
        val guarantorAddress = findViewById<EditText>(R.id.etGuarantorAddress)
        val consent = findViewById<CheckBox>(R.id.cbConsent)
        val notes = findViewById<EditText>(R.id.etNotes)
        val otpStatus = findViewById<TextView>(R.id.tvOtpStatus)
        startDate = findViewById(R.id.etStartDate)
        endDate = findViewById(R.id.etEndDate)
        emiAmount = findViewById(R.id.etEmiAmount)

        setSpinner(creditType, arrayOf("Personal Credit", "Trade Credit", "Rental / Other Credit"))
        setSpinner(repaymentMethod, arrayOf("Principal + Interest", "EMI"))
        setSpinner(periodicity, arrayOf("Monthly", "Quarterly", "Half-yearly", "Yearly"))
        setSpinner(guarantor, arrayOf("Guarantor not available", "Guarantor available"))

        repaymentMethod.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                emiAmount.visibility = if (position == 1) View.VISIBLE else View.GONE
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }
        guarantor.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val visible = position == 1
                listOf(guarantorName, guarantorMobile, guarantorAddress,
                    findViewById<Button>(R.id.btnGuarantorPhoto), findViewById<TextView>(R.id.tvGuarantorPhoto))
                    .forEach { it.visibility = if (visible) View.VISIBLE else View.GONE }
            }
            override fun onNothingSelected(parent: AdapterView<*>?) = Unit
        }

        startDate.setOnClickListener { showDatePicker(startDate) }
        endDate.setOnClickListener { showDatePicker(endDate) }
        findViewById<Button>(R.id.btnBorrowerPhoto).setOnClickListener { photoTarget = 1; chooseFile("image/*") }
        findViewById<Button>(R.id.btnGuarantorPhoto).setOnClickListener { photoTarget = 2; chooseFile("image/*") }
        findViewById<Button>(R.id.btnInvoice).setOnClickListener { photoTarget = 3; chooseFile("application/pdf,image/*") }

        findViewById<Button>(R.id.btnVerifyOtp).setOnClickListener {
            generatedOtp = Random.nextInt(100000, 1000000).toString()
            Toast.makeText(this, "Demo OTP: $generatedOtp", Toast.LENGTH_LONG).show()
            val input = EditText(this).apply { hint = "Enter OTP"; inputType = 2 }
            AlertDialog.Builder(this).setTitle("OTP Verification")
                .setMessage("In production this OTP will be sent to the registered mobile number.")
                .setView(input).setPositiveButton("VERIFY") { _, _ ->
                    otpVerified = input.text.toString().trim() == generatedOtp
                    otpStatus.text = if (otpVerified) "✓ OTP verified" else "OTP verification failed"
                }.setNegativeButton("CANCEL", null).show()
        }

        findViewById<Button>(R.id.btnSaveUdhaar).setOnClickListener {
            val name = personName.text.toString().trim()
            val amountValue = amount.text.toString().trim().toDoubleOrNull()
            val roiValue = roi.text.toString().trim().toDoubleOrNull() ?: 0.0
            if (name.isEmpty()) { personName.error = "Enter borrower name"; return@setOnClickListener }
            if (amountValue == null || amountValue <= 0) { amount.error = "Enter valid principal"; return@setOnClickListener }
            if (startDate.text.isNullOrBlank()) { startDate.error = "Select start date"; return@setOnClickListener }
            if (endDate.text.isNullOrBlank()) { endDate.error = "Select end date"; return@setOnClickListener }
            if (!consent.isChecked) { consent.error = "Consent is required"; return@setOnClickListener }
            if (!otpVerified) { otpStatus.error = "Verify OTP before registration"; return@setOnClickListener }

            val method = if (repaymentMethod.selectedItemPosition == 1) "EMI" else "PRINCIPAL_INTEREST"
            val emi = if (method == "EMI") emiAmount.text.toString().trim().toDoubleOrNull() ?: 0.0 else 0.0
            if (method == "EMI" && emi <= 0) { emiAmount.error = "Enter valid EMI"; return@setOnClickListener }
            val guarantorAvailable = guarantor.selectedItemPosition == 1
            if (guarantorAvailable && guarantorName.text.toString().trim().isEmpty()) { guarantorName.error = "Enter guarantor name"; return@setOnClickListener }

            val result = databaseHelper.addRecordV32(
                creditType = when (creditType.selectedItemPosition) { 1 -> "TRADE"; 2 -> "OTHER"; else -> "PERSONAL" },
                personName = name, mobile = mobile.text.toString().trim(), address = address.text.toString().trim(),
                aadhaar = aadhaar.text.toString().trim(), pan = pan.text.toString().trim().uppercase(), borrowerPhotoUri = borrowerPhotoUri,
                amount = amountValue, roi = roiValue, repaymentMethod = method,
                periodicity = periodicity.selectedItem.toString(), startDate = startDate.text.toString(), endDate = endDate.text.toString(),
                emiAmount = emi, guarantorAvailable = guarantorAvailable, guarantorName = guarantorName.text.toString().trim(),
                guarantorMobile = guarantorMobile.text.toString().trim(), guarantorAddress = guarantorAddress.text.toString().trim(),
                guarantorPhotoUri = guarantorPhotoUri, invoiceUri = invoiceUri, consentStatus = "CONSENTED", otpVerified = true,
                notes = notes.text.toString().trim()
            )
            if (result != -1L) {
                Toast.makeText(this, "Credit registered successfully", Toast.LENGTH_LONG).show()
                finish()
            } else Toast.makeText(this, "Unable to register credit", Toast.LENGTH_LONG).show()
        }
    }

    private fun setSpinner(spinner: Spinner, values: Array<String>) {
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, values)
    }

    private fun chooseFile(type: String) {
        startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply { this.type = type; addCategory(Intent.CATEGORY_OPENABLE) }, 100)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode != 100 || resultCode != RESULT_OK) return
        val uri: Uri = data?.data ?: return
        when (photoTarget) {
            1 -> { borrowerPhotoUri = uri.toString(); findViewById<TextView>(R.id.tvBorrowerPhoto).text = "Borrower photo selected" }
            2 -> { guarantorPhotoUri = uri.toString(); findViewById<TextView>(R.id.tvGuarantorPhoto).text = "Guarantor photo selected" }
            3 -> { invoiceUri = uri.toString(); findViewById<TextView>(R.id.tvInvoice).text = "Invoice/document selected" }
        }
    }

    private fun showDatePicker(target: EditText) {
        val calendar = Calendar.getInstance()
        DatePickerDialog(this, { _, year, month, day ->
            calendar.set(year, month, day); target.setText(dateFormat.format(calendar.time))
        }, calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH)).show()
    }
}
