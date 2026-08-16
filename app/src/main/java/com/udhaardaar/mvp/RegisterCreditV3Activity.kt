package com.udhaardaar.mvp

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class RegisterCreditV3Activity : AppCompatActivity() {

    private lateinit var databaseHelper: V3DatabaseHelper

    private lateinit var partySpinner: Spinner
    private lateinit var creditTypeSpinner: Spinner
    private lateinit var directionSpinner: Spinner

    private lateinit var amount: EditText
    private lateinit var interestRate: EditText
    private lateinit var repaymentAmount: EditText
    private lateinit var startDate: EditText
    private lateinit var endDate: EditText
    private lateinit var notes: EditText

    private lateinit var historyTitle: TextView
    private lateinit var historySummary: TextView

    private val dateFormat =
        SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    private var partyIds = mutableListOf<Long>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_register_credit_v3)

        databaseHelper = V3DatabaseHelper(this)

        partySpinner = findViewById(R.id.spinnerParty)
        creditTypeSpinner = findViewById(R.id.spinnerCreditType)
        directionSpinner = findViewById(R.id.spinnerDirection)

        amount = findViewById(R.id.etCreditAmount)
        interestRate = findViewById(R.id.etInterestRate)
        repaymentAmount = findViewById(R.id.etRepaymentAmount)
        startDate = findViewById(R.id.etCreditStartDate)
        endDate = findViewById(R.id.etCreditEndDate)
        notes = findViewById(R.id.etCreditNotes)

        historyTitle = findViewById(R.id.tvHistoryTitle)
        historySummary = findViewById(R.id.tvHistorySummary)

        setupCreditTypes()
        setupDirection()
        setupParties()
        setupDates()

        partySpinner.setOnItemSelectedListener(
            object : android.widget.AdapterView.OnItemSelectedListener {

                override fun onItemSelected(
                    parent: android.widget.AdapterView<*>?,
                    view: View?,
                    position: Int,
                    id: Long
                ) {
                    if (position >= 0 &&
                        position < partyIds.size
                    ) {
                        showBorrowerHistory(
                            partyIds[position]
                        )
                    }
                }

                override fun onNothingSelected(
                    parent: android.widget.AdapterView<*>?
                ) {
                }
            }
        )

        findViewById<Button>(
            R.id.btnSaveCreditV3
        ).setOnClickListener {
            saveCredit()
        }
    }

    private fun setupCreditTypes() {

        val options = arrayOf(
            "Personal Credit",
            "Business Credit",
            "Trade Credit",
            "Advance",
            "Rental / Lease",
            "Other"
        )

        creditTypeSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options
            )
    }

    private fun setupDirection() {

        val options = arrayOf(
            "Credit Given",
            "Credit Received"
        )

        directionSpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                options
            )
    }

    private fun setupParties() {

        partyIds.clear()

        val names = mutableListOf<String>()

        val db = databaseHelper.readableDatabase

        val cursor = db.rawQuery(
            """
            SELECT id, name
            FROM parties
            ORDER BY name COLLATE NOCASE
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {

            partyIds.add(
                cursor.getLong(0)
            )

            names.add(
                cursor.getString(1)
            )
        }

        cursor.close()

        if (names.isEmpty()) {
            names.add("No borrower/party available")
        }

        partySpinner.adapter =
            ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                names
            )
    }

    private fun showBorrowerHistory(
        partyId: Long
    ) {

        val db = databaseHelper.readableDatabase

        var creditCount = 0
        var settledCount = 0
        var activeCount = 0

        var totalCredit = 0.0
        var totalRepaid = 0.0
        var outstanding = 0.0

        val creditCursor = db.rawQuery(
            """
            SELECT
                c.id,
                c.principal_amount,
                c.status,
                COALESCE(
                    (
                        SELECT SUM(r.amount)
                        FROM repayments r
                        WHERE r.credit_id = c.id
                    ),
                    0
                )
            FROM credits c
            WHERE c.party_id = ?
            ORDER BY c.start_date DESC
            """.trimIndent(),
            arrayOf(partyId.toString())
        )

        while (creditCursor.moveToNext()) {

            creditCount++

            val principal =
                creditCursor.getDouble(1)

            val status =
                creditCursor.getString(2)

            val repaid =
                creditCursor.getDouble(3)

            totalCredit += principal
            totalRepaid += repaid

            outstanding +=
                (principal - repaid)
                    .coerceAtLeast(0.0)

            if (status == "ACTIVE") {
                activeCount++
            }

            if (status == "SETTLED") {
                settledCount++
            }
        }

        creditCursor.close()

        if (creditCount == 0) {

            historyTitle.text =
                "Repayment History"

            historySummary.text =
                "No previous credit or repayment history available."

            return
        }

        historyTitle.text =
            "Borrower Repayment History"

        historySummary.text =
            """
            Previous credits: $creditCount
            Settled credits: $settledCount
            Active credits: $activeCount

            Total historical credit: ${formatCurrency(totalCredit)}
            Total repaid: ${formatCurrency(totalRepaid)}
            Current outstanding: ${formatCurrency(outstanding)}

            This information is shown before creating the new credit.
            """.trimIndent()
    }

    private fun setupDates() {

        startDate.setOnClickListener {
            showDatePicker(startDate)
        }

        endDate.setOnClickListener {
            showDatePicker(endDate)
        }
    }

    private fun showDatePicker(
        target: EditText
    ) {

        val calendar =
            Calendar.getInstance()

        DatePickerDialog(
            this,
            { _, year, month, day ->

                calendar.set(
                    year,
                    month,
                    day
                )

                target.setText(
                    dateFormat.format(
                        calendar.time
                    )
                )
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveCredit() {

        if (partyIds.isEmpty()) {

            Toast.makeText(
                this,
                "Please create a borrower/party first.",
                Toast.LENGTH_LONG
            ).show()

            return
        }

        val amountValue =
            amount.text.toString()
                .trim()
                .toDoubleOrNull()

        if (amountValue == null ||
            amountValue <= 0
        ) {
            amount.error =
                "Enter a valid credit amount"
            return
        }

        val interestValue =
            interestRate.text.toString()
                .trim()
                .toDoubleOrNull()
                ?: 0.0

        if (interestValue < 0) {
            interestRate.error =
                "Enter a valid interest rate"
            return
        }

        val repaymentValue =
            repaymentAmount.text.toString()
                .trim()
                .toDoubleOrNull()
                ?: 0.0

        if (repaymentValue <= 0) {
            repaymentAmount.error =
                "Enter the agreed repayment amount"
            return
        }

        val start =
            startDate.text.toString().trim()

        if (start.isEmpty()) {
            startDate.error =
                "Select start date"
            return
        }

        val end =
            endDate.text.toString().trim()

        val direction =
            if (directionSpinner.selectedItemPosition == 0)
                "GIVEN"
            else
                "RECEIVED"

        val creditType =
            creditTypeSpinner.selectedItem
                .toString()

        val periodicity =
            "MONTHLY"

        val now =
            SimpleDateFormat(
                "yyyy-MM-dd HH:mm:ss",
                Locale.getDefault()
            ).format(
                Calendar.getInstance().time
            )

        val result =
            databaseHelper.addCredit(
                partyId = partyIds[
                    partySpinner.selectedItemPosition
                ],
                creditType = creditType,
                direction = direction,
                principalAmount = amountValue,
                interestRate = interestValue,
                repaymentMethod =
                    "FIXED_REPAYMENT",
                repaymentAmount = repaymentValue,
                periodicity = periodicity,
                startDate = start,
                endDate =
                    if (end.isEmpty()) null
                    else end,
                nextDueDate =
                    if (end.isEmpty()) null
                    else end,
                notes =
                    notes.text.toString().trim(),
                createdAt = now
            )

        if (result != -1L) {

            Toast.makeText(
                this,
                "Credit registered successfully",
                Toast.LENGTH_LONG
            ).show()

            finish()

        } else {

            Toast.makeText(
                this,
                "Unable to register credit",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    private fun formatCurrency(
        amount: Double
    ): String {

        return "₹" +
            String.format(
                "%,.2f",
                amount
            )
    }
}
