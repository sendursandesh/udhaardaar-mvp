package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/** V5 credit-registration service: score access is explicitly consent-gated. */
class V5CreditService(context: Context) {
    private val store = V5LocalStore(context)

    fun createDraft(credit: V5Credit): String {
        require(credit.principal > 0 && credit.profileId.isNotBlank())
        val id = credit.id.ifBlank { "CR-${System.currentTimeMillis()}" }
        store.add("credits", JSONObject().apply {
            put("id", id); put("profileId", credit.profileId); put("direction", credit.direction); put("type", credit.creditType)
            put("principal", credit.principal); put("roi", credit.roiPercent); put("repaymentMethod", credit.repaymentMethod)
            put("start", credit.startDate); put("end", credit.endDate); put("status", "DRAFT"); put("scoreConsent", "PENDING")
        })
        return id
    }

    fun recordBorrowerScoreConsent(creditId: String, consentEventId: String): Boolean {
        val c = store.find("credits", creditId) ?: return false
        if (consentEventId.isBlank()) return false
        c.put("scoreConsent", "OTP_VERIFIED"); c.put("scoreConsentEventId", consentEventId); c.put("scoreConsentAt", System.currentTimeMillis())
        store.replace("credits", c)
        store.add("audit", JSONObject().apply { put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", creditId); put("event", "BORROWER_SCORE_CONSENTED"); put("actor", consentEventId); put("at", System.currentTimeMillis()) })
        return true
    }

    fun canDisplayScore(creditId: String): Boolean = store.find("credits", creditId)?.optString("scoreConsent") == "OTP_VERIFIED"
}
