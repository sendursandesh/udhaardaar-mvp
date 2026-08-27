package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class RecordsActivity : AppCompatActivity() {
    private lateinit var databaseHelper: UdhaarDatabaseHelper
    private lateinit var repaymentRepository: RepaymentRepository

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)
        databaseHelper = UdhaarDatabaseHelper(this)
        repaymentRepository = RepaymentRepository { receipt -> databaseHelper.persistAuthorisedRepayment(receipt) }
        val container = findViewById<LinearLayout>(R.id.recordsContainer)
        val empty = findViewById<TextView>(R.id.tvNoRecords)
        val cursor = databaseHelper.readableDatabase.query("udhaar_records", null, null, null, null, null, "id DESC")
        cursor.use {
            if (it.count == 0) {
                empty.text = "No Udhaar records yet."
                return
            }
            empty.text = ""
            while (it.moveToNext()) {
                val id = it.getLong(it.getColumnIndexOrThrow("id"))
                val name = it.getString(it.getColumnIndexOrThrow("person_name"))
                val amount = it.getDouble(it.getColumnIndexOrThrow("amount"))
                val roi = it.getDouble(it.getColumnIndexOrThrow("roi"))
                val method = it.getString(it.getColumnIndexOrThrow("repayment_method"))
                val periodicity = it.getString(it.getColumnIndexOrThrow("periodicity"))
                val status = it.getString(it.getColumnIndexOrThrow("status"))
                val outstanding = (amount - databaseHelper.totalRepayments(id)).coerceAtLeast(0.0)
                val lenderId = it.getString(it.getColumnIndexOrThrow("lender_user_id"))
                val borrowerId = it.getString(it.getColumnIndexOrThrow("borrower_user_id"))
                val consent = it.getInt(it.getColumnIndexOrThrow("consent_granted")) == 1
                val revoked = it.getInt(it.getColumnIndexOrThrow("consent_revoked")) == 1
                val card = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16, 16, 16, 16) }
                val summary = TextView(this).apply {
                    text = "Name: $name\nPrincipal: ₹${"%.2f".format(amount)}\nROI: ${"%.2f".format(roi)}%\nMethod: $method\nPeriodicity: $periodicity\nOutstanding: ₹${"%.2f".format(outstanding)}\nStatus: $status"
                    textSize = 15f
                }
                val repay = Button(this).apply { text = "Record repayment" }
                repay.setOnClickListener { showRepaymentDialog(id, outstanding, lenderId, borrowerId, consent, revoked) }
                card.addView(summary)
                card.addView(repay)
                container.addView(card)
            }
        }
    }

    private fun showRepaymentDialog(creditId: Long, outstanding: Double, lenderId: String?, borrowerId: String?, consent: Boolean, revoked: Boolean) {
        if (outstanding <= 0.0) {
            Toast.makeText(this, "This credit has no outstanding amount.", Toast.LENGTH_SHORT).show()
            return
        }
        val input = EditText(this).apply { hint = "Repayment amount"; inputType = android.text.InputType.TYPE_CLASS_NUMBER or android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL }
        AlertDialog.Builder(this)
            .setTitle("Record repayment")
            .setMessage("Outstanding: ₹${"%.2f".format(outstanding)}")
            .setView(input)
            .setNegativeButton("Cancel", null)
            .setPositiveButton("Continue") { _, _ ->
                val amount = input.text.toString().toDoubleOrNull() ?: 0.0
                val userId = getSharedPreferences("session", MODE_PRIVATE).getString("user_id", null)
                val roleName = getSharedPreferences("session", MODE_PRIVATE).getString("role", null)
                val role = roleName?.let { runCatching { AccessControl.Role.valueOf(it) }.getOrNull() }
                if (userId == null || role == null) {
                    Toast.makeText(this, "Please sign in before recording a repayment.", Toast.LENGTH_LONG).show()
                    return@setPositiveButton
                }
                val requester = AccessControl.CreditParty(userId, role, consentGranted = consent, consentRevoked = revoked)
                val parties = listOfNotNull(
                    lenderId?.takeIf { it.isNotBlank() }?.let { AccessControl.CreditParty(it, AccessControl.Role.LENDER, consentGranted = consent, consentRevoked = revoked) },
                    borrowerId?.takeIf { it.isNotBlank() }?.let { AccessControl.CreditParty(it, AccessControl.Role.BORROWER, consentGranted = consent, consentRevoked = revoked) }
                )
                when (val result = repaymentRepository.record(RepaymentService.RepaymentRequest(creditId, amount, outstanding, requester, parties))) {
                    is RepaymentService.Result.Success -> { Toast.makeText(this, "Repayment recorded.", Toast.LENGTH_SHORT).show(); recreate() }
                    is RepaymentService.Result.Rejected -> Toast.makeText(this, result.reason, Toast.LENGTH_LONG).show()
                }
            }.show()
    }
}
