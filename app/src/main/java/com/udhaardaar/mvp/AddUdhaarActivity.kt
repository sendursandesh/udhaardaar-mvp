package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class AddUdhaarActivity : AppCompatActivity() {

    private lateinit var databaseHelper: UdhaarDatabaseHelper

    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var emiAmount: EditText

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_add_udhaar)

        databaseHelper = UdhaarDatabaseHelper(this)

        val personName = findViewById<EditText>(R.id.etPersonName)
        val amount = findViewById<EditText>(R.id.etAmount)
        val roi = findViewById<EditText>(R.id.etRoi)
        val notes = findViewById<EditText>(R.id.etNotes)

        startDate = findViewById(R.id.etStartDate)
        endDate = findViewById(R.id.etEndDate)
        emiAmount = findViewById(R.id.etEmiAmount)

        val repaymentMethod =
            findViewById<Spinner>(R.id.spinnerRepaymentMethod)

        val periodicity =
            findViewById<Spinner>(R.id.spinnerPeriodicity)

        val saveButton =
            findViewById<Button>(R.id.btnSaveUdhaar)

        val methodOptions = arrayOf(
            "Principal + Interest",
            "EMI"
        )

        repaymentMethod.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            methodOptions
        )

        val periodicityOptions = arrayOf(
            "Monthly",
            "Quarterly",
            "Half-yearly",
            "Yearly"
        )

        periodicity.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            periodicityOptions
        )

        repaymentMethod.onItemSelectedListener =
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    emiAmount.visibility =
                        if (position == 1) View.VISIBLE else View.GONE
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }

        startDate.setOnClickListener {
            showDatePicker(startDate)
        }

        endDate.setOnClickListener {
            showDatePicker(endDate)
        }

        saveButton.setOnClickListener {

            val name = personName.text.toString().trim()
            val amountText = amount.text.toString().trim()
            val roiText = roi.text.toString().trim()
            val startDateText = startDate.text.toString().trim()
            val endDateText = endDate.text.toString().trim()
            val notesText = notes.text.toString().trim()

            if (name.isEmpty()) {
                personName.error = "Enter person/customer name"
                return@setOnClickListener
            }

            if (amountText.isEmpty()) {
                amount.error = "Enter principal amount"
                return@setOnClickListener
            }

            val amountValue = amountText.toDoubleOrNull()

            if (amountValue == null || amountValue <= 0) {
                amount.error = "Enter a valid principal amount"
                return@setOnClickListener
            }

            if (roiText.isEmpty()) {
                roi.error = "Enter agreed ROI"
                return@setOnClickListener
            }

            val roiValue = roiText.toDoubleOrNull()

            if (roiValue == null || roiValue < 0) {
                roi.error = "Enter a valid ROI"
                return@setOnClickListener
            }

            if (startDateText.isEmpty()) {
                startDate.error = "Select commencing date"
                return@setOnClickListener
            }

            if (endDateText.isEmpty()) {
                endDate.error = "Select end date"
                return@setOnClickListener
            }

            val method =
                if (repaymentMethod.selectedItemPosition == 1)
                    "EMI"
                else
                    "PRINCIPAL_INTEREST"

            var emiValue = 0.0

            if (method == "EMI") {

                val emiText = emiAmount.text.toString().trim()

                if (emiText.isEmpty()) {
                    emiAmount.error = "Enter EMI amount"
                    return@setOnClickListener
                }

                emiValue = emiText.toDoubleOrNull() ?: 0.0

                if (emiValue <= 0) {
                    emiAmount.error = "Enter a valid EMI amount"
                    return@setOnClickListener
                }
            }

            val periodicityValue =
                periodicity.selectedItem.toString()

            val result = databaseHelper.addRecord(
                name,
                amountValue,
                roiValue,
                method,
                periodicityValue,
                startDateText,
                endDateText,
                emiValue,
                notesText
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

    private fun showDatePicker(target: EditText) {

        val calendar = Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->

                calendar.set(year, month, dayOfMonth)

                target.setText(
                    dateFormat.format(calendar.time)
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }
}
