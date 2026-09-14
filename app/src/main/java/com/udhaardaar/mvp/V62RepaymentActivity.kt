package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*
import kotlin.random.Random

class V62RepaymentActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val density by lazy { resources.displayMetrics.density }
    private fun dp(v: Int) = (v * density).toInt()
    private lateinit var root: LinearLayout

    override fun onCreate(b: Bundle?) { super.onCreate(b); window.setSoftInputMode(16); render() }
    override fun onResume() { super.onResume(); if (!isFinishing) render() }

    private fun render() {
        root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(16), dp(8), dp(16), dp(28)); setBackgroundColor(ArthSaathiV62Design.BG) }
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.title(this, "Repayment Centre", "Record • Reconcile • Update"), 2)
        ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "Only a registered party may initiate a repayment update. OTP consent is recorded before the ledger changes.", 10f, ArthSaathiV62Design.MUTED), 8)
        val rows = store.all(V62Store.RELATIONSHIPS).filter { it.optString("status") != "CLOSED" }
        if (rows.isEmpty()) ArthSaathiV62Design.add(root, ArthSaathiV62Design.text(this, "No active relationships yet.", 13f, ArthSaathiV62Design.NAVY, true), 12) else rows.forEach { renderRelationship(it) }
        setContentView(ScrollView(this).apply { isFillViewport = true; addView(root) })
    }

    private fun renderRelationship(j: JSONObject) {
        val outstanding = j.optDouble("outstanding", j.optDouble("amount", j.optDouble("principal", 0.0)))
        val owner = j.optString("ownerUserId")
        val cp = store.find(V62Store.COUNTERPARTIES, j.optString("counterpartyId"))
        val cpMobile = cp?.optString("mobile").orEmpty()
        val current = V62Integration.currentUserId(this)
        val authorised = current == owner || current == cpMobile
        val box = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(13), dp(11), dp(13), dp(11)); background = ArthSaathiV62Design.card() }
        box.addView(ArthSaathiV62Design.text(this, "${j.optString("type", "CREDIT")} • ${j.optString("ownerRole", "USER")}", 10f, ArthSaathiV62Design.TEAL, true))
        box.addView(ArthSaathiV62Design.text(this, "₹${money(j.optDouble("principal", j.optDouble("amount", 0.0)))} • Outstanding ₹${money(outstanding)}", 15f, ArthSaathiV62Design.NAVY, true))
        box.addView(ArthSaathiV62Design.text(this, "${j.optString("repaymentMode", j.optString("method"))} • ${j.optString("periodicity", j.optString("period"))} • EMI ₹${money(j.optDouble("emi", 0.0))}", 10f, ArthSaathiV62Design.MUTED))
        if (authorised && outstanding > 0) box.addView(ArthSaathiV62Design.button(this, "RECORD REPAYMENT — CONSENT + OTP", ArthSaathiV62Design.GREEN) { record(j, outstanding) }) else box.addView(ArthSaathiV62Design.text(this, if (authorised) "Fully repaid." else "View only — you are not a registered party to this relationship.", 10f, ArthSaathiV62Design.MUTED, true))
        ArthSaathiV62Design.add(root, box, 7)
    }

    private fun record(j: JSONObject, max: Double) {
        val amount = ArthSaathiV62Design.input(this, "Total amount paid ₹ (max ${money(max)})")
        val interest = ArthSaathiV62Design.input(this, "Interest component ₹ (optional)")
        val date = ArthSaathiV62Design.input(this, "Payment date YYYY-MM-DD")
        val w = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; addView(amount); addView(interest); addView(date, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7) }) }
        AlertDialog.Builder(this).setTitle("Repayment consent").setMessage("The repayment is recorded against this relationship. Counterparty consent is required with OTP before the ledger is updated.").setView(w).setNegativeButton("CANCEL", null).setPositiveButton("REQUEST OTP") { _, _ ->
            val paid = amount.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val interestValue = interest.text.toString().replace(",", "").toDoubleOrNull() ?: 0.0
            val paymentDate = date.text.toString().trim()
            if (paid <= 0 || paid > max || interestValue < 0 || interestValue > paid || !validDate(paymentDate)) Toast.makeText(this, "Enter valid repayment, interest and date.", Toast.LENGTH_LONG).show() else otp(j, paid, interestValue, paymentDate)
        }.show()
    }

    private fun otp(j: JSONObject, paid: Double, interest: Double, date: String) {
        val code = (100000 + Random.nextInt(900000)).toString()
        val entry = ArthSaathiV62Design.input(this, "Enter 6-digit OTP")
        val dlg = AlertDialog.Builder(this).setTitle("Two-party consent OTP").setMessage("Demo OTP: $code").setView(entry).setNegativeButton("CANCEL", null).setPositiveButton("VERIFY", null).create()
        dlg.setOnShowListener {
            dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
                if (entry.text.toString() != code) { entry.error = "Incorrect OTP"; return@setOnClickListener }
                dlg.dismiss()
                val before = j.optDouble("outstanding", j.optDouble("amount", j.optDouble("principal", 0.0)))
                val principalPaid = paid - interest
                val after = (before - principalPaid).coerceAtLeast(0.0)
                j.put("outstanding", after); j.put("lastRepaymentAmount", paid); j.put("lastRepaymentPrincipal", principalPaid); j.put("lastRepaymentInterest", interest); j.put("lastRepaymentDate", date); j.put("consentStatus", "OTP_VERIFIED")
                if (after <= 0.0) j.put("status", "CLOSED")
                store.replace(V62Store.RELATIONSHIPS, j)
                val paymentId = V62Store.id("PAY")
                store.add(V62Store.REPAYMENTS, JSONObject().apply { put("id", paymentId); put("relationshipId", j.optString("id")); put("counterpartyId", j.optString("counterpartyId")); put("amount", paid); put("principal", principalPaid); put("interest", interest); put("date", date); put("consentVerified", true); put("recordedBy", V62Integration.currentUserId(this@V62RepaymentActivity)); put("createdAt", System.currentTimeMillis()) })
                V62Integration.recordConsent(this, j.optString("counterpartyId"), j.optString("id"), "REPAYMENT_CONFIRMATION", true)
                V62EventBus.publish(V62Event(V62Events.REPAYMENT_CHANGED, paymentId)); V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, j.optString("id")))
                Toast.makeText(this, "Repayment recorded with consent.", Toast.LENGTH_LONG).show(); render()
            }
        }
        dlg.show()
    }

    private fun money(v: Double) = String.format(Locale.US, "%.2f", v)
    private fun validDate(value: String): Boolean = runCatching { SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(value) != null }.getOrDefault(false)
}
