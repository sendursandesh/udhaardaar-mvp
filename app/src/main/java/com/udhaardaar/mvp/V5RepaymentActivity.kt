package com.udhaardaar.mvp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class V5RepaymentActivity : AppCompatActivity() {
    private val service by lazy { V5RepaymentService(this) }
    private val store by lazy { V5LocalStore(this) }
    private fun e(h: String, max: Int = 0) = EditText(this).apply {
        hint = h
        setSingleLine(true)
        textSize = 14f
        minHeight = 52
        if (max > 0) filters = arrayOf(android.text.InputFilter.LengthFilter(max))
    }
    private fun add(r: LinearLayout, v: android.view.View, h: Int = 52) {
        r.addView(v, LinearLayout.LayoutParams(-1, h).apply { setMargins(0, 3, 0, 3) })
    }
    override fun onCreate(b: Bundle?) { super.onCreate(b); show() }

    private fun show() {
        val r = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(18, 12, 18, 18) }
        add(r, TextView(this).apply { text = "UDHAARDAAR V5 • REPAYMENT CENTRE"; textSize = 21f; setTextColor(Color.rgb(24, 58, 92)) }, 58)
        add(r, TextView(this).apply { text = "Informal: initiator → counterparty OTP → confirmed. Formal: bank evidence, no counterparty OTP."; textSize = 12f }, 58)

        val credit = e("Credit ID *")
        val amount = e("Repayment amount *")
        val date = e("Payment date YYYY-MM-DD *")
        val ref = e("Transaction reference")
        val evidence = e("Bank/account statement evidence reference")
        val kind = Spinner(this).apply { adapter = ArrayAdapter(this@V5RepaymentActivity, android.R.layout.simple_spinner_dropdown_item, listOf("INFORMAL", "FORMAL")) }
        val initiator = Spinner(this).apply { adapter = ArrayAdapter(this@V5RepaymentActivity, android.R.layout.simple_spinner_dropdown_item, listOf("BORROWER", "LENDER")) }
        val mode = Spinner(this).apply { adapter = ArrayAdapter(this@V5RepaymentActivity, android.R.layout.simple_spinner_dropdown_item, listOf("EMI", "PRINCIPAL_PLUS_INTEREST", "BULLET_PRINCIPAL_ONLY", "BULLET_INTEREST_MONTHLY", "BULLET_PRINCIPAL_PLUS_INTEREST_AT_END")) }
        val direction = Spinner(this).apply { adapter = ArrayAdapter(this@V5RepaymentActivity, android.R.layout.simple_spinner_dropdown_item, listOf("PAYABLE", "RECEIVABLE")) }
        listOf(credit, amount, date, ref, evidence).forEach { add(r, it) }
        add(r, kind); add(r, initiator); add(r, mode); add(r, direction)

        add(r, Button(this).apply {
            text = "CHECK CREDIT / OUTSTANDING"
            setOnClickListener {
                val c = store.find("credits", credit.text.toString().trim())
                if (c == null) toast("Credit not found") else {
                    direction.setSelection(if (c.optString("direction") == "Credit Received") 0 else 1)
                    toast("Outstanding: ₹${c.optDouble("outstanding", c.optDouble("amount", 0.0))}")
                }
            }
        })

        add(r, Button(this).apply {
            text = "RECORD REPAYMENT"
            setOnClickListener {
                val cid = credit.text.toString().trim()
                val paid = amount.text.toString().toDoubleOrNull()
                if (cid.isBlank() || paid == null || paid <= 0 || date.text.isBlank()) { toast("Complete credit, amount and date"); return@setOnClickListener }
                val c = store.find("credits", cid)
                if (c == null) { toast("Credit ID not found"); return@setOnClickListener }
                val outstanding = c.optDouble("outstanding", c.optDouble("amount", 0.0))
                if (paid > outstanding) { toast("Repayment exceeds outstanding ₹$outstanding"); return@setOnClickListener }
                if (kind.selectedItem.toString() == "INFORMAL") {
                    try {
                        val req = service.request(cid, initiator.selectedItem.toString(), paid, date.text.toString(), mode.selectedItem.toString(), ref.text.toString(), evidence.text.toString())
                        val code = store.find("repayment_requests", req)?.optString("otp", "") ?: ""
                        val input = e("Counterparty OTP", 6)
                        val d = AlertDialog.Builder(this@V5RepaymentActivity)
                            .setTitle("Counterparty consent required")
                            .setMessage("Demo OTP: $code\nLive SMS provider required for production.")
                            .setView(input).setNegativeButton("CANCEL", null).setPositiveButton("CONFIRM", null).create()
                        d.setOnShowListener {
                            d.getButton(-1).setOnClickListener {
                                if (service.confirm(req, input.text.toString(), code)) { toast("Informal repayment CONFIRMED; outstanding recalculated"); d.dismiss() } else input.error = "Incorrect OTP"
                            }
                        }
                        d.show()
                    } catch (ex: Exception) { toast(ex.message ?: "Unable to create repayment request") }
                } else {
                    if (evidence.text.isBlank()) { toast("Formal repayment requires bank/account evidence"); return@setOnClickListener }
                    store.add("repayments", JSONObject().apply {
                        put("id", "RP-${System.currentTimeMillis()}"); put("creditId", cid); put("amount", paid); put("date", date.text.toString())
                        put("method", mode.selectedItem.toString()); put("direction", direction.selectedItem.toString()); put("reference", ref.text.toString())
                        put("evidence", evidence.text.toString()); put("status", "CONFIRMED_FORMAL"); put("consent", "NOT_REQUIRED"); put("createdAt", System.currentTimeMillis())
                    })
                    c.put("outstanding", (outstanding - paid).coerceAtLeast(0.0)); c.put("lastRepaymentAt", System.currentTimeMillis()); store.replace("credits", c)
                    toast("Formal repayment recorded; counterparty OTP not required")
                }
            }
        })
        add(r, TextView(this).apply { text = "Payable • Receivable • Pending Consent • Confirmed • Settled"; textSize = 12f }, 42)
        add(r, Button(this).apply { text = "VIEW SAVED REPAYMENTS"; setOnClickListener { refreshList() } })
        add(r, Button(this).apply { text = "BACK"; setOnClickListener { finish() } })
        setContentView(ScrollView(this).apply { addView(r) })
    }

    private fun refreshList() {
        val all = (store.all("repayments") + store.all("repayment_requests")).filter { it.optString("creditId").isNotBlank() }
        val summary = all.joinToString("\n\n") { x -> "${x.optString("id")} • ${x.optString("status")}\nCredit: ${x.optString("creditId")} • ₹${x.optDouble("amount")}\nDirection: ${x.optString("direction", if (x.optString("initiatedBy") == "BORROWER") "PAYABLE" else "RECEIVABLE")}" }.ifBlank { "No repayments recorded" }
        AlertDialog.Builder(this).setTitle("Repayment history").setMessage(summary).setPositiveButton("OK", null).show()
    }
    private fun toast(s: String) = Toast.makeText(this, s, Toast.LENGTH_LONG).show()
}
