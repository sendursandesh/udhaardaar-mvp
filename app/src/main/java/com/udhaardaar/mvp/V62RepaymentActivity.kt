package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class V62RepaymentActivity : androidx.appcompat.app.AppCompatActivity() {
    private val s by lazy { V5LocalStore(this) }
    private val d by lazy { resources.displayMetrics.density }
    private val root by lazy { LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(16.dp, 8.dp, 16.dp, 28.dp); setBackgroundColor(ArthSaathiV62Design.BG) } }
    private val Int.dp: Int get() = (this * d).toInt()

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); render() }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }

    private fun render() {
        root.removeAllViews()
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Repayment Centre", "Record • Reconcile • Update"), 2)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Only a party to the relationship may initiate a repayment update. OTP consent is recorded before the ledger changes.", 10f, ArthSaathiV62Design.MUTED), 8)
        val rows = s.all(V62Store.RELATIONSHIPS).filter { it.optString("status") != "CLOSED" }
        if (rows.isEmpty()) ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "No active relationships yet.", 13f, ArthSaathiV62Design.NAVY, true), 12)
        else rows.forEach { j ->
            val out = j.optDouble("outstanding", j.optDouble("amount", j.optDouble("principal", 0.0)))
            val owner = j.optString("ownerUserId")
            val cpMobile = j.optString("counterpartyMobile")
            val me = V62Integration.currentUserId(this)
            val authorised = me == owner || me == cpMobile
            val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(13.dp, 11.dp, 13.dp, 11.dp); background = ArthSaathiV62Design.card() }
            box.addView(ArthSaathiV62Design.text(this, "${j.optString("type", "CREDIT")} • ${j.optString("ownerRole", "USER")}", 10f, ArthSaathiV62Design.TEAL, true))
            box.addView(ArthSaathiV62Design.text(this, "₹${"%.2f".format(Locale.US, j.optDouble("principal", j.optDouble("amount", 0.0)))} • Outstanding ₹${"%.2f".format(Locale.US, out)}", 15f, ArthSaathiV62Design.NAVY, true))
            box.addView(ArthSaathiV62Design.text(this, "${j.optString("repaymentMode", j.optString("method"))} • ${j.optString("periodicity", j.optString("period"))} • EMI ₹${"%.2f".format(Locale.US, j.optDouble("emi", 0.0))}", 10f, ArthSaathiV62Design.MUTED))
            if (authorised && out > 0) box.addView(ArthSaathiV62Design.button(this, "RECORD REPAYMENT — CONSENT + OTP", ArthSaathiV62Design.GREEN) { record(j, out) })
            else box.addView(ArthSaathiV62Design.text(this, if (authorised) "Fully repaid." else "View only — you are not a registered party to this relationship.", 10f, ArthSaathiV62Design.MUTED, true))
            ArthSaathiV62Design.add(root, box, 7)
        }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun record(j: JSONObject, max: Double) {
        val amount = ArthSaathiV62Design.input(this, "Total amount paid ₹ (max ${"%.2f".format(Locale.US, max)})")
        val interest = ArthSaathiV62Design.input(this, "Interest component ₹ (optional)")
        val date = ArthSaathiV62Design.input(this, "Payment date YYYY-MM-DD")
        val w = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(amount); addView(interest); addView(date, LinearLayout.LayoutParams(-1, -2).apply { topMargin = 7.dp }) }
        AlertDialog.Builder(this).setTitle("Repayment consent").setMessage("The repayment is recorded against this relationship. The counterparty must approve the change with OTP before the ledger is updated.").setView(w).setNegativeButton("CANCEL", null).setPositiveButton("REQUEST OTP") { _, _ ->
            val p = amount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val i = interest.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val dt = date.text.toString().trim()
            if (p <= 0 || p > max || i < 0 || i > p || !validDate(dt)) Toast.makeText(this, "Enter valid repayment, interest and date.", Toast.LENGTH_LONG).show()
            else otp(j, p, i, dt)
        }.show()
    }

    private fun otp(j: JSONObject, p: Double, interest: Double, date: String) {
        val code = (100000 + Random.nextInt(900000)).toString()
        val e = ArthSaathiV62Design.input(this, "Enter 6-digit OTP")
        AlertDialog.Builder(this).setTitle("Two-party consent OTP").setMessage("Demo OTP: $code").setView(e).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY", null).create().also { dlg ->
            dlg.setOnShowListener { dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (e.text.toString() != code) { e.error = "Incorrect OTP"; return@setOnClickListener }
                dlg.dismiss()
                val before = j.optDouble("outstanding", j.optDouble("amount", j.optDouble("principal", 0.0)))
                val principalPaid = p - interest
                j.put("outstanding", (before - principalPaid).coerceAtLeast(0.0))
                j.put("lastRepaymentAmount", p); j.put("lastRepaymentPrincipal", principalPaid); j.put("lastRepaymentInterest", interest); j.put("lastRepaymentDate", date); j.put("consentStatus", "OTP_VERIFIED")
                if (j.optDouble("outstanding", 0.0) <= 0.0) j.put("status", "CLOSED")
                s.replace(V62Store.RELATIONSHIPS, j)
                val paymentId = V62Store.id("PAY")
                s.add(V62Store.REPAYMENTS, JSONObject().apply {
                    put("id", paymentId); put("relationshipId", j.optString("id")); put("counterpartyId", j.optString("counterpartyId")); put("amount", p); put("principal", principalPaid); put("interest", interest); put("date", date); put("consentVerified", true); put("recordedBy", V62Integration.currentUserId(this@V62RepaymentActivity)); put("createdAt", System.currentTimeMillis())
                })
                V62Integration.recordConsent(this, j.optString("counterpartyId"), j.optString("id"), "REPAYMENT_CONFIRMATION", true)
                V62EventBus.publish(V62Event(V62Events.REPAYMENT_CHANGED, paymentId))
                V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, j.optString("id")))
                Toast.makeText(this, "Repayment recorded with consent.", Toast.LENGTH_LONG).show(); render()
            } }
            dlg.show()
        }
    }

    private fun validDate(s: String): Boolean = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(s) != null }.getOrDefault(false)
}
