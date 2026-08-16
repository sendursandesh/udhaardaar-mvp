package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class DashboardV3Activity : AppCompatActivity() {

    private lateinit var databaseHelper: V3DatabaseHelper

    private lateinit var creditGiven: TextView
    private lateinit var creditReceived: TextView
    private lateinit var outstanding: TextView
    private lateinit var overdue: TextView
    private lateinit var upcomingRepayments: TextView
    private lateinit var recentActivity: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContentView(R.layout.activity_dashboard_v3)

        databaseHelper = V3DatabaseHelper(this)

        creditGiven = findViewById(R.id.tvCreditGiven)
        creditReceived = findViewById(R.id.tvCreditReceived)
        outstanding = findViewById(R.id.tvOutstanding)
        overdue = findViewById(R.id.tvOverdue)

        upcomingRepayments =
            findViewById(R.id.tvUpcomingRepayments)

        recentActivity =
            findViewById(R.id.tvRecentActivity)

        val registerCredit =
            findViewById<Button>(R.id.btnRegisterCredit)

        val recordRepayment =
            findViewById<Button>(R.id.btnRecordRepayment)

        registerCredit.setOnClickListener {

            val intent = Intent(
                this,
                AddUdhaarActivity::class.java
            )

            startActivity(intent)
        }

        recordRepayment.setOnClickListener {

            // Repayment screen will be connected
            // in the next V3 phase.
        }
    }

    override fun onResume() {
        super.onResume()

        updateDashboard()
    }

    private fun updateDashboard() {

        val db = databaseHelper.readableDatabase

        var given = 0.0
        var received = 0.0
        var outstandingAmount = 0.0

        val cursor = db.rawQuery(
            """
            SELECT
                direction,
                SUM(principal_amount)
            FROM credits
            GROUP BY direction
            """.trimIndent(),
            null
        )

        while (cursor.moveToNext()) {

            val direction = cursor.getString(0)
            val amount = cursor.getDouble(1)

            if (direction == "GIVEN") {
                given += amount
            }

            if (direction == "RECEIVED") {
                received += amount
            }
        }

        cursor.close()

        val repaymentCursor = db.rawQuery(
            """
            SELECT
                c.principal_amount,
                COALESCE(
                    SUM(r.amount),
                    0
                )
            FROM credits c
            LEFT JOIN repayments r
                ON c.id = r.credit_id
            WHERE c.status = 'ACTIVE'
            GROUP BY c.id
            """.trimIndent(),
            null
        )

        while (repaymentCursor.moveToNext()) {

            val principal =
                repaymentCursor.getDouble(0)

            val repaid =
                repaymentCursor.getDouble(1)

            outstandingAmount +=
                (principal - repaid).coerceAtLeast(0.0)
        }

        repaymentCursor.close()

        creditGiven.text =
            formatCurrency(given)

        creditReceived.text =
            formatCurrency(received)

        outstanding.text =
            formatCurrency(outstandingAmount)

        overdue.text =
            formatCurrency(0.0)

        upcomingRepayments.text =
            "No upcoming repayments"

        recentActivity.text =
            "No recent activity"
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
