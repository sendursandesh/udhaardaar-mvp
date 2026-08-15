package com.udhaardaar.mvp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddUdhaarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: UdhaarDatabaseHelper

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_udhaar)

        databaseHelper = UdhaarDatabaseHelper(this)

        val personName = findViewById<EditText>(R.id.etPersonName)
        val amount = findViewById<EditText>(R.id.etAmount)
        val dueDate = findViewById<EditText>(R.id.etDueDate)
        val notes = findViewById<EditText>(R.id.etNotes)
        val saveButton = findViewById<Button>(R.id.btnSaveUdhaar)

        saveButton.setOnClickListener {

            val name = personName.text.toString().trim()
            val amountText = amount.text.toString().trim()
            val date = dueDate.text.toString().trim()
            val noteText = notes.text.toString().trim()

            if (name.isEmpty()) {
                personName.error = "Enter person/customer name"
                return@setOnClickListener
            }

            if (amountText.isEmpty()) {
                amount.error = "Enter amount"
                return@setOnClickListener
            }

            val amountValue = amountText.toDoubleOrNull()

            if (amountValue == null || amountValue <= 0) {
                amount.error = "Enter a valid amount"
                return@setOnClickListener
            }

            val result = databaseHelper.addRecord(
                name,
                amountValue,
                date,
                noteText
            )

            if (result != -1L) {

                Toast.makeText(
                    this,
                    "Udhaar record saved successfully",
                    Toast.LENGTH_LONG
                ).show()

                finish()

            } else {

                Toast.makeText(
                    this,
                    "Unable to save record",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
