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
                    cursor.getString(cursor.getColumnIndexOrThrow("person_name"))

                val amount =
                    cursor.getDouble(cursor.getColumnIndexOrThrow("amount"))

                val dueDate =
                    cursor.getString(cursor.getColumnIndexOrThrow("due_date"))

                val status =
                    cursor.getString(cursor.getColumnIndexOrThrow("status"))

                builder.append("Name: ")
                    .append(name)
                    .append("\n")

                builder.append("Amount: ₹")
                    .append(amount)
                    .append("\n")

                builder.append("Due Date: ")
                    .append(
                        if (dueDate.isEmpty()) "Not specified"
                        else dueDate
                    )
                    .append("\n")

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
