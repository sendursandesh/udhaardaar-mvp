package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {

    private lateinit var databaseHelper: UdhaarDatabaseHelper

    private lateinit var toReceive: TextView
    private lateinit var toPay: TextView
    private lateinit var activeRecords: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        databaseHelper = UdhaarDatabaseHelper(this)

        toReceive = findViewById(R.id.tvToReceive)
        toPay = findViewById(R.id.tvToPay)
        activeRecords = findViewById(R.id.tvActiveRecords)

        val addUdhaar = findViewById<Button>(R.id.btnAddUdhaar)
        val viewRecords = findViewById<Button>(R.id.btnViewRecords)

        addUdhaar.setOnClickListener {
            val intent = Intent(this, AddUdhaarActivity::class.java)
            startActivity(intent)
        }

        viewRecords.setOnClickListener {
            val intent = Intent(this, RecordsActivity::class.java)
            startActivity(intent)
        }
    }

    override fun onResume() {
        super.onResume()

        if (::databaseHelper.isInitialized) {
            updateDashboard()
        }
    }

    private fun updateDashboard() {

        val db = databaseHelper.readableDatabase

        var totalReceivable = 0.0
        var activeCount = 0

        val cursor = db.rawQuery(
            """
            SELECT amount
            FROM udhaar_records
            WHERE status = ?
            """.trimIndent(),
            arrayOf("UNPAID")
        )

        while (cursor.moveToNext()) {
            totalReceivable += cursor.getDouble(0)
            activeCount++
        }

        cursor.close()

        toReceive.text =
            "To Receive: ₹${String.format("%.2f", totalReceivable)}"

        toPay.text = "To Pay: ₹0.00"

        activeRecords.text =
            "Active Records: $activeCount"
    }
}
