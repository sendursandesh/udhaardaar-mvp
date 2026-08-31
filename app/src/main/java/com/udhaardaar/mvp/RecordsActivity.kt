package com.udhaardaar.mvp

import android.database.Cursor
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class RecordsActivity : AppCompatActivity() {

    private lateinit var databaseHelper: UdhaarDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_records)

        databaseHelper = UdhaarDatabaseHelper(this)

        val recordsText = findViewById<TextView>(R.id.tvNoRecords)

        val cursor: Cursor = databaseHelper.readableDatabase.query(
            "udhaar_records",
            null,
            null,
            null,
            null,
            null,
            "id DESC"
        )

        if (cursor.count == 0) {

            recordsText.text = "No Udhaar records yet."

        } else {

            val builder = StringBuilder()

            while (cursor.moveToNext()) {

                val name =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("person_name")
                    )

                val amount =
                    cursor.getDouble(
                        cursor.getColumnIndexOrThrow("amount")
                    )

                val roi =
                    cursor.getDouble(
                        cursor.getColumnIndexOrThrow("roi")
                    )

                val method =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("repayment_method")
                    )

                val periodicity =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("periodicity")
                    )

                val startDate =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("start_date")
                    )

                val endDate =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("end_date")
                    )

                val emiAmount =
                    cursor.getDouble(
                        cursor.getColumnIndexOrThrow("emi_amount")
                    )

                val notes =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("notes")
                    )

                val status =
                    cursor.getString(
                        cursor.getColumnIndexOrThrow("status")
                    )

                builder.append("Name: ")
                    .append(name)
                    .append("\n")

                builder.append("Principal: ₹")
                    .append(String.format("%.2f", amount))
                    .append("\n")

                builder.append("Agreed ROI: ")
                    .append(String.format("%.2f", roi))
                    .append("%\n")

                builder.append("Repayment Method: ")
                    .append(
                        if (method == "EMI")
                            "EMI"
                        else
                            "Principal + Interest"
                    )
                    .append("\n")

                builder.append("Periodicity: ")
                    .append(periodicity)
                    .append("\n")

                builder.append("Commencing From: ")
                    .append(startDate)
                    .append("\n")

                builder.append("End Date: ")
                    .append(endDate)
                    .append("\n")

                if (method == "EMI") {
                    builder.append("EMI Amount: ₹")
                        .append(
                            String.format("%.2f", emiAmount)
                        )
                        .append("\n")
                }

                if (notes.isNotEmpty()) {
                    builder.append("Notes: ")
                        .append(notes)
                        .append("\n")
                }

                builder.append("Status: ")
                    .append(status)
                    .append("\n")

                builder.append("----------------------\n\n")
            }

            recordsText.text = builder.toString()
        }

        cursor.close()
    }
}
