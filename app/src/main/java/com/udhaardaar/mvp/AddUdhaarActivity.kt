package com.udhaardaar.mvp

import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class AddUdhaarActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_udhaar)

        val personName = findViewById<EditText>(R.id.etPersonName)
        val amount = findViewById<EditText>(R.id.etAmount)
        val dueDate = findViewById<EditText>(R.id.etDueDate)
        val notes = findViewById<EditText>(R.id.etNotes)
        val saveButton = findViewById<Button>(R.id.btnSaveUdhaar)

        saveButton.setOnClickListener {

            val name = personName.text.toString().trim()
            val amountValue = amount.text.toString().trim()

            if (name.isEmpty()) {
                personName.error = "Enter person/customer name"
                return@setOnClickListener
            }

            if (amountValue.isEmpty()) {
                amount.error = "Enter amount"
                return@setOnClickListener
            }

            Toast.makeText(
                this,
                "Udhaar record saved successfully",
                Toast.LENGTH_LONG
            ).show()

            finish()
        }
    }
}
