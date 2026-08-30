package com.udhaardaar.mvp

import android.app.Activity
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions
import java.io.BufferedReader
import java.io.InputStreamReader
import java.util.Locale

class FormalLoanAuditActivity : AppCompatActivity() {
    private lateinit var db: FormalLoanAuditDb
    private var sanctionUri: Uri? = null
    private var statementUri: Uri? = null
    private var loanId = -1L

    private lateinit var result: TextView
    private lateinit var lender: EditText
    private lateinit var account: EditText
    private lateinit var sanctioned: EditText
    private lateinit var disbursed: EditText
    private lateinit var roi: EditText
    private lateinit var tenure: EditText
    private lateinit var emi: EditText
    private lateinit var processing: EditText
    private lateinit var documentation: EditText
    private lateinit var insurance: EditText
    private lateinit var penal: EditText
    private lateinit var bounce: EditText
    private lateinit var prepayment: EditText
    private lateinit var other: EditText

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_formal_loan_audit)
        db = FormalLoanAuditDb(this)

        lender = findViewById(R.id.etLender)
        account = findViewById(R.id.etAccount)
        sanctioned = findViewById(R.id.etSanctioned)
        disbursed = findViewById(R.id.etDisbursed)
        roi = findViewById(R.id.etRoi)
        tenure = findViewById(R.id.etTenure)
        emi = findViewById(R.id.etEmi)
        processing = findViewById(R.id.etProcessing)
        documentation = findViewById(R.id.etDocumentation)
        insurance = findViewById(R.id.etInsurance)
        penal = findViewById(R.id.etPenal)
        bounce = findViewById(R.id.etBounce)
        prepayment = findViewById(R.id.etPrepayment)
        other = findViewById(R.id.etOther)
        result = findViewById(R.id.tvAuditResult)

        findViewById<Button>(R.id.btnSanction).setOnClickListener { pickDocument(10) }
        findViewById<Button>(R.id.btnOcr).setOnClickListener { ocrSanction() }
        findViewById<Button>(R.id.btnSaveBaseline).setOnClickListener { saveBaseline() }
        findViewById<Button>(R.id.btnStatement).setOnClickListener { pickDocument(11) }
        findViewById<Button>(R.id.btnCompare).setOnClickListener { compareCsv() }
    }

    private fun pickDocument(requestCode: Int) {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            type = "*/*"
            addCategory(Intent.CATEGORY_OPENABLE)
            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION)
        }
        startActivityForResult(intent, requestCode)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode != Activity.RESULT_OK) return
        val uri = data?.data ?: return
        try {
            val flags = data.flags and Intent.FLAG_GRANT_READ_URI_PERMISSION
            contentResolver.takePersistableUriPermission(uri, flags)
        } catch (_: Exception) {
            // Some document providers do not support persistable permissions.
        }
        if (requestCode == 10) {
            sanctionUri = uri
            toast("Sanction letter attached: ${displayName(uri)}")
        } else if (requestCode == 11) {
            statementUri = uri
            toast("Statement attached: ${displayName(uri)}")
        }
    }

    private fun ocrSanction() {
        val uri = sanctionUri ?: run {
            toast("Upload a sanction letter image first")
            return
        }
        try {
            val image = InputImage.fromFilePath(this, uri)
            TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
                .process(image)
                .addOnSuccessListener { text ->
                    parseTerms(text.text)
                    result.text = "Sanction document OCR completed. Review the detected terms before saving the baseline."
                }
                .addOnFailureListener {
                    toast("OCR could not read this document. Enter terms manually.")
                }
        } catch (_: Exception) {
            toast("This file cannot be OCR-read. Use a clear JPG/PNG page.")
        }
    }

    private fun parseTerms(text: String) {
        val normalized = text.replace("₹", "").replace(",", "")
        fun find(pattern: Regex): Double? =
            pattern.find(normalized)?.groupValues?.getOrNull(1)?.toDoubleOrNull()

        find(Regex("(?i)(?:sanctioned|sanction amount)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { sanctioned.setText(it.toString()) }
        find(Regex("(?i)(?:disbursed|disbursement amount)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { disbursed.setText(it.toString()) }
        find(Regex("(?i)(?:rate of interest|interest rate|ROI)[^0-9]{0,20}([0-9]+(?:\\.[0-9]+)?)"))?.let { roi.setText(it.toString()) }
        find(Regex("(?i)(?:processing fee|processing charges)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { processing.setText(it.toString()) }
        find(Regex("(?i)(?:documentation fee|documentation charges)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { documentation.setText(it.toString()) }
        find(Regex("(?i)(?:insurance)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { insurance.setText(it.toString()) }
        find(Regex("(?i)(?:penal interest|penal rate)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { penal.setText(it.toString()) }
        find(Regex("(?i)(?:bounce charge|bounce charges)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { bounce.setText(it.toString()) }
        find(Regex("(?i)(?:prepayment|foreclosure)[^0-9]{0,30}([0-9]+(?:\\.[0-9]+)?)"))?.let { prepayment.setText(it.toString()) }
    }

    private fun saveBaseline() {
        val values = ContentValues().apply {
            put("lender", lender.text.toString().trim())
            put("account_no", account.text.toString().trim())
            put("loan_type", "Formal Loan")
            put("sanctioned", number(sanctioned))
            put("disbursed", number(disbursed))
            put("roi", number(roi))
            put("tenure_months", number(tenure).toInt())
            put("emi", number(emi))
            put("processing_fee", number(processing))
            put("documentation_fee", number(documentation))
            put("insurance", number(insurance))
            put("penal_rate", number(penal))
            put("bounce_charge", number(bounce))
            put("prepayment_charge", number(prepayment))
            put("other_charge", number(other))
            put("sanction_uri", sanctionUri?.toString())
            put("created_at", FormalLoanAuditDb.now())
        }
        try {
            loanId = db.addLoan(values)
            toast("Sanction baseline saved")
            result.text = "Baseline saved. Now upload the statement and compare."
        } catch (_: Exception) {
            toast("Unable to save the sanction baseline")
        }
    }

    private fun compareCsv() {
        if (loanId <= 0L) {
            toast("Save the sanction baseline first")
            return
        }
        val uri = statementUri ?: run {
            toast("Upload a statement first")
            return
        }

        db.clearEntries(loanId)
        var totalActual = 0.0
        var totalExpected = 0.0
        var flags = 0

        try {
            val input = contentResolver.openInputStream(uri) ?: throw IllegalStateException()
            BufferedReader(InputStreamReader(input)).use { reader ->
                reader.lineSequence().drop(1).forEach { line ->
                    val parts = line.split(",")
                    if (parts.size < 3) return@forEach
                    val date = parts[0].trim()
                    val description = parts[1].trim()
                    val actual = parts[2].trim().toDoubleOrNull() ?: return@forEach
                    val type = if (parts.size > 3) parts[3].trim().uppercase(Locale.US) else classify(description)
                    val expected = expectedCharge(type)
                    val variance = if (expected > 0.0) actual - expected else 0.0
                    val review = when {
                        expected == 0.0 && type != "EMI" -> "REVIEW"
                        kotlin.math.abs(variance) > 0.01 -> "REVIEW"
                        else -> "MATCH"
                    }

                    db.addEntry(ContentValues().apply {
                        put("loan_id", loanId)
                        put("entry_date", date)
                        put("description", description)
                        put("amount", actual)
                        put("charge_type", type)
                        put("expected", expected)
                        put("variance", variance)
                        put("review", review)
                    })

                    if (type != "EMI") {
                        totalActual += actual
                        totalExpected += expected
                    }
                    if (review == "REVIEW") flags++
                }
            }

            result.text = "CHARGE AUDIT\n\n" +
                "Expected charges: ₹${money(totalExpected)}\n" +
                "Actual charge entries: ₹${money(totalActual)}\n" +
                "Variance: ₹${money(totalActual - totalExpected)}\n" +
                "Items requiring review: $flags\n\n" +
                "REVIEW means the statement differs from the stored sanction baseline and requires human/contract verification; it is not an automatic finding of wrongful charging."
            toast("Statement comparison completed")
        } catch (_: Exception) {
            toast("Upload a CSV statement with columns: date,description,amount,charge_type")
        }
    }

    private fun classify(description: String): String = when {
        description.contains("interest", true) -> "INTEREST"
        description.contains("processing", true) -> "PROCESSING"
        description.contains("bounce", true) -> "BOUNCE"
        description.contains("penal", true) || description.contains("late", true) -> "PENAL"
        description.contains("insurance", true) -> "INSURANCE"
        description.contains("documentation", true) -> "DOCUMENTATION"
        description.contains("prepayment", true) || description.contains("foreclosure", true) -> "PREPAYMENT"
        description.contains("emi", true) || description.contains("repayment", true) -> "EMI"
        else -> "OTHER"
    }

    private fun expectedCharge(type: String): Double = when (type) {
        "PROCESSING" -> number(processing)
        "DOCUMENTATION" -> number(documentation)
        "INSURANCE" -> number(insurance)
        "BOUNCE" -> number(bounce)
        "PREPAYMENT" -> number(prepayment)
        "OTHER" -> number(other)
        else -> 0.0
    }

    private fun number(field: EditText): Double =
        field.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0

    private fun money(value: Double): String = String.format(Locale.US, "%,.2f", value)

    private fun displayName(uri: Uri): String {
        var name = "document"
        contentResolver.query(uri, null, null, null, null)?.use { cursor ->
            if (cursor.moveToFirst()) {
                val index = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                if (index >= 0) name = cursor.getString(index)
            }
        }
        return name
    }

    private fun toast(message: String) =
        Toast.makeText(this, message, Toast.LENGTH_LONG).show()
}
