package com.udhaardaar.mvp

import android.database.Cursor
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecordsActivity : AppCompatActivity() {
    private lateinit var databaseHelper: UdhaarDatabaseHelper
    private lateinit var output: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)
        databaseHelper = UdhaarDatabaseHelper(this)
        output = findViewById(R.id.tvNoRecords)
        val search = findViewById<EditText>(R.id.etSearch)
        findViewById<Button>(R.id.btnSearch).setOnClickListener { loadRecords(search.text.toString().trim()) }
        loadRecords("")
    }

    private fun loadRecords(term: String) {
        val db = databaseHelper.readableDatabase
        val selection: String?
        val args: Array<String>?
        if (term.isEmpty()) {
            selection = null; args = null
        } else {
            selection = "person_name LIKE ? OR borrower_mobile LIKE ? OR borrower_aadhaar LIKE ? OR borrower_pan LIKE ? OR unique_credit_id LIKE ?"
            val q = "%$term%"; args = arrayOf(q, q, q, q, q)
        }
        val cursor: Cursor = db.query("udhaar_records", null, selection, args, null, null, "id DESC")
        if (cursor.count == 0) {
            output.text = if (term.isEmpty()) "No Udhaar records yet." else "No matching borrower / credit history found."
            cursor.close(); return
        }
        val builder = StringBuilder()
        while (cursor.moveToNext()) {
            val id = cursor.getString(cursor.getColumnIndexOrThrow("unique_credit_id"))
            val name = cursor.getString(cursor.getColumnIndexOrThrow("person_name"))
            val mobile = cursor.getString(cursor.getColumnIndexOrThrow("borrower_mobile"))
            val aadhaar = cursor.getString(cursor.getColumnIndexOrThrow("borrower_aadhaar"))
            val pan = cursor.getString(cursor.getColumnIndexOrThrow("borrower_pan"))
            val type = cursor.getString(cursor.getColumnIndexOrThrow("credit_type"))
            val amount = cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))
            val method = cursor.getString(cursor.getColumnIndexOrThrow("repayment_method"))
            val status = cursor.getString(cursor.getColumnIndexOrThrow("status"))
            val guarantor = cursor.getString(cursor.getColumnIndexOrThrow("guarantor_available"))
            val otp = cursor.getInt(cursor.getColumnIndexOrThrow("otp_verified")) == 1
            builder.append("Credit ID: ").append(id).append("\n")
                .append("Borrower: ").append(name).append("\n")
                .append("Mobile: ").append(mobile).append("\n")
                .append("Aadhaar: ").append(if (aadhaar.isEmpty()) "—" else aadhaar).append("\n")
                .append("PAN: ").append(if (pan.isEmpty()) "—" else pan).append("\n")
                .append("Type: ").append(type).append("\n")
                .append("Principal: ₹").append(String.format("%.2f", amount)).append("\n")
                .append("Repayment: ").append(if (method == "EMI") "EMI" else "Principal + Interest").append("\n")
                .append("Guarantor: ").append(guarantor).append("\n")
                .append("OTP verified: ").append(if (otp) "YES" else "NO").append("\n")
                .append("Status: ").append(status).append("\n")
                .append("------------------------------\n\n")
        }
        output.text = builder.toString()
        cursor.close()
    }
}
