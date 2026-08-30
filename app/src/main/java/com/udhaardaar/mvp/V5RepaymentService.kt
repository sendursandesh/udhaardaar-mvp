package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/** V5 service: an informal repayment is only confirmed after counterparty OTP consent. */
class V5RepaymentService(context: Context) {
    private val store = V5LocalStore(context)

    fun request(creditId: String, initiatedBy: String, amount: Double, date: String, method: String, reference: String, evidenceId: String? = null): String {
        require(amount > 0 && creditId.isNotBlank() && date.isNotBlank())
        val initiator = V5ConsentAndScore.Party.valueOf(initiatedBy.uppercase())
        require(initiator == V5ConsentAndScore.Party.BORROWER || initiator == V5ConsentAndScore.Party.LENDER)
        val counterparty = if (initiator == V5ConsentAndScore.Party.BORROWER) V5ConsentAndScore.Party.LENDER else V5ConsentAndScore.Party.BORROWER
        val id = "RP-${System.currentTimeMillis()}"
        store.add("repayment_requests", JSONObject().apply {
            put("id", id); put("creditId", creditId); put("initiatedBy", initiator.name); put("counterparty", counterparty.name)
            put("amount", amount); put("date", date); put("method", method); put("reference", reference); put("evidenceId", evidenceId ?: "")
            put("status", "COUNTERPARTY_OTP_PENDING"); put("createdAt", System.currentTimeMillis())
        })
        return id
    }

    fun confirm(requestId: String, otp: String, expectedOtp: String): Boolean {
        val r = store.find("repayment_requests", requestId) ?: return false
        if (r.optString("status") != "COUNTERPARTY_OTP_PENDING") return false
        if (otp.length != 6 || otp != expectedOtp) return false
        r.put("status", "CONFIRMED"); r.put("consentedAt", System.currentTimeMillis())
        store.replace("repayment_requests", r)
        store.add("audit", JSONObject().apply { put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", requestId); put("event", "COUNTERPARTY_OTP_CONSENTED"); put("at", System.currentTimeMillis()) })
        return true
    }
}
