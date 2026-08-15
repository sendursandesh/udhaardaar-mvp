package com.udhaardaar.mvp

import android.app.Activity
import android.os.Bundle
import android.graphics.Color
import android.view.View
import android.widget.*
import org.json.JSONArray
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MainActivity : Activity() {

    private lateinit var root: LinearLayout
    private val prefsName = "udhaardaar_data"
    private val recordsKey = "records"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        showHome()
    }

    private fun baseLayout(): LinearLayout {
        root = LinearLayout(this)
        root.orientation = LinearLayout.VERTICAL
        root.setPadding(32, 32, 32, 32)
        root.setBackgroundColor(Color.WHITE)
        return root
    }

    private fun title(text: String): TextView {
        return TextView(this).apply {
            this.text = text
            textSize = 28f
            setTextColor(Color.BLACK)
            setPadding(0, 0, 0, 24)
        }
    }

    private fun button(text: String, action: () -> Unit): Button {
        return Button(this).apply {
            this.text = text
            setOnClickListener { action() }
            layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 8, 0, 8)
            }
        }
    }

    private fun showHome() {
        root = baseLayout()

        root.addView(title("Udhaardaar"))

        root.addView(TextView(this).apply {
            text = "Manage your credit records"
            textSize = 18f
            setTextColor(Color.DKGRAY)
            setPadding(0, 0, 0, 24)
        })

        root.addView(button("➕ Add New Credit") {
            showAddCredit()
        })

        root.addView(button("📋 Credit Records / History") {
            showRecords()
        })

        root.addView(button("📊 Summary") {
            showSummary()
        })

        setContentView(root)
    }

    private fun showAddCredit() {
        root = baseLayout()

        root.addView(title("Add New Credit"))

        val name = EditText(this).apply {
            hint = "Person / Business Name"
        }

        val mobile = EditText(this).apply {
            hint = "Mobile Number"
            inputType = 2
        }

        val amount = EditText(this).apply {
            hint = "Amount"
            inputType = 8194
        }

        val date = EditText(this).apply {
            hint = "Date (DD-MM-YYYY)"
            setText(currentDate())
        }

        val dueDate = EditText(this).apply {
            hint = "Due Date (DD-MM-YYYY)"
        }

        val notes = EditText(this).apply {
            hint = "Notes"
            minLines = 3
            gravity = 48
        }

        root.addView(name)
        root.addView(mobile)
        root.addView(amount)
        root.addView(date)
        root.addView(dueDate)
        root.addView(notes)

        root.addView(button("SAVE CREDIT") {
            val person = name.text.toString().trim()
            val amountValue = amount.text.toString().trim()

            if (person.isEmpty() || amountValue.isEmpty()) {
                Toast.makeText(
                    this,
                    "Please enter name and amount",
                    Toast.LENGTH_SHORT
                ).show()
                return@button
            }

            val record = JSONObject().apply {
                put("id", System.currentTimeMillis())
                put("name", person)
                put("mobile", mobile.text.toString())
                put("amount", amountValue.toDoubleOrNull() ?: 0.0)
                put("paid", 0.0)
                put("date", date.text.toString())
                put("dueDate", dueDate.text.toString())
                put("notes", notes.text.toString())
            }

            val records = getRecords()
            records.put(record)
            saveRecords(records)

            Toast.makeText(
                this,
                "Credit record saved",
                Toast.LENGTH_SHORT
            ).show()

            showRecords()
        })

        root.addView(button("← Back") {
            showHome()
        })

        setContentView(root)
    }

    private fun showRecords() {
        root = baseLayout()

        root.addView(title("Credit Records / History"))

        val records = getRecords()

        if (records.length == 0) {
            root.addView(TextView(this).apply {
                text = "No credit records yet.\n\nTap 'Add New Credit' to create your first record."
                textSize = 18f
                setPadding(0, 16, 0, 24)
            })
        } else {
            for (i in 0 until records.length) {
                val record = records.getJSONObject(i)

                val amount = record.optDouble("amount", 0.0)
                val paid = record.optDouble("paid", 0.0)
                val outstanding = amount - paid

                val status = when {
                    outstanding <= 0 -> "PAID"
                    paid > 0 -> "PARTIAL"
                    else -> "PENDING"
                }

                val summary = TextView(this).apply {
                    text = "${record.optString("name")}\n" +
                            "Amount: ₹${formatMoney(amount)}\n" +
                            "Outstanding: ₹${formatMoney(outstanding)}\n" +
                            "Status: $status"
                    textSize = 17f
                    setTextColor(Color.BLACK)
                    setPadding(16, 20, 16, 20)
                    setOnClickListener {
                        showRecordDetails(i)
                    }
                }

                root.addView(summary)

                val divider = View(this).apply {
                    setBackgroundColor(Color.LTGRAY)
                    layoutParams = LinearLayout.LayoutParams(
                        LinearLayout.LayoutParams.MATCH_PARENT,
                        2
                    )
                }

                root.addView(divider)
            }
        }

        root.addView(button("➕ Add New Credit") {
            showAddCredit()
        })

        root.addView(button("← Home") {
            showHome()
        })

        setContentView(root)
    }

    private fun showRecordDetails(index: Int) {
        val records = getRecords()
        val record = records.getJSONObject(index)

        root = baseLayout()

        root.addView(title("Credit Details"))

        val amount = record.optDouble("amount", 0.0)
        val paid = record.optDouble("paid", 0.0)
        val outstanding = amount - paid

        root.addView(TextView(this).apply {
            text = "Name: ${record.optString("name")}\n\n" +
                    "Mobile: ${record.optString("mobile")}\n\n" +
                    "Credit Amount: ₹${formatMoney(amount)}\n\n" +
                    "Paid: ₹${formatMoney(paid)}\n\n" +
                    "Outstanding: ₹${formatMoney(outstanding)}\n\n" +
                    "Date: ${record.optString("date")}\n\n" +
                    "Due Date: ${record.optString("dueDate")}\n\n" +
                    "Notes: ${record.optString("notes")}"
            textSize = 17f
            setTextColor(Color.BLACK)
        })

        if (outstanding > 0) {
            root.addView(button("💰 Add Payment") {
                addPayment(index)
            })
        }

        root.addView(button("← Back to Records") {
            showRecords()
        })

        setContentView(root)
    }

    private fun addPayment(index: Int) {
        val input = EditText(this).apply {
            hint = "Payment amount"
            inputType = 8194
        }

        AlertDialogBuilder(this)
            .setTitle("Add Payment")
            .setView(input)
            .setPositiveButton("Save") { _, _ ->
                val payment = input.text.toString().toDoubleOrNull() ?: 0.0

                if (payment <= 0) {
                    Toast.makeText(
                        this,
                        "Enter a valid payment amount",
                        Toast.LENGTH_SHORT
                    ).show()
                    return@setPositiveButton
                }

                val records = getRecords()
                val record = records.getJSONObject(index)

                val oldPaid = record.optDouble("paid", 0.0)
                val total = record.optDouble("amount", 0.0)

                record.put("paid", minOf(oldPaid + payment, total))

                saveRecords(records)
                showRecordDetails(index)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showSummary() {
        val records = getRecords()

        var total = 0.0
        var paid = 0.0

        for (i in 0 until records.length) {
            val record = records.getJSONObject(i)
            total += record.optDouble("amount", 0.0)
            paid += record.optDouble("paid", 0.0)
        }

        val outstanding = total - paid

        root = baseLayout()

        root.addView(title("Udhaardaar Summary"))

        root.addView(TextView(this).apply {
            text = "Total Credit: ₹${formatMoney(total)}\n\n" +
                    "Total Paid: ₹${formatMoney(paid)}\n\n" +
                    "Outstanding: ₹${formatMoney(outstanding)}\n\n" +
                    "Total Records: ${records.length}"
            textSize = 20f
            setTextColor(Color.BLACK)
        })

        root.addView(button("📋 View Records") {
            showRecords()
        })

        root.addView(button("← Home") {
            showHome()
        })

        setContentView(root)
    }

    private fun getRecords(): JSONArray {
        val json = getSharedPreferences(prefsName, MODE_PRIVATE)
            .getString(recordsKey, "[]") ?: "[]"

        return try {
            JSONArray(json)
        } catch (e: Exception) {
            JSONArray()
        }
    }

    private fun saveRecords(records: JSONArray) {
        getSharedPreferences(prefsName, MODE_PRIVATE)
            .edit()
            .putString(recordsKey, records.toString())
            .apply()
    }

    private fun currentDate(): String {
        return SimpleDateFormat(
            "dd-MM-yyyy",
            Locale.getDefault()
        ).format(Date())
    }

    private fun formatMoney(value: Double): String {
        return String.format(Locale.getDefault(), "%.2f", value)
    }

    private fun AlertDialogBuilder(activity: Activity): android.app.AlertDialog.Builder {
        return android.app.AlertDialog.Builder(activity)
    }
}
