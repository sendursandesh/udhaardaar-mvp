package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/** Single persistence facade for sequential V5 flows. */
class V5WorkflowRepository(context: Context) {
    private val store = V5LocalStore(context)

    fun saveProfile(p: V5Profile) = store.replace("profiles", JSONObject().apply {
        put("id", p.id); put("type", p.type); put("name", p.name); put("mobile", p.mobile)
        put("pan", p.pan ?: ""); put("aadhaar", p.aadhaar ?: ""); put("gstin", p.gstin ?: "")
        put("photoUri", p.photoUri ?: ""); put("city", p.city ?: ""); put("state", p.state ?: ""); put("pin", p.pin ?: "")
    })

    fun saveGuarantor(g: V5GuarantorAndDocuments.GuarantorProfile) = store.replace("guarantors", JSONObject().apply {
        put("id", g.id); put("name", g.name); put("mobile", g.mobile); put("address", g.address)
        put("pan", g.pan ?: ""); put("aadhaar", g.aadhaar ?: ""); put("photoUri", g.photoUri ?: ""); put("relationship", g.relationship ?: "")
    })

    fun saveAsset(a: V5Asset) = store.replace("assets", JSONObject().apply {
        put("id", a.id); put("ownerProfileId", a.ownerProfileId); put("category", a.category); put("title", a.title)
        put("description", a.description); put("estimatedValue", a.estimatedValue ?: JSONObject.NULL)
        put("nomineeProfileId", a.nomineeProfileId ?: ""); put("documents", a.proofDocumentIds.joinToString(","))
    })

    fun saveClaim(c: V5Claim) = store.replace("claims", JSONObject().apply {
        put("id", c.id); put("assetId", c.assetId); put("claimantProfileId", c.claimantProfileId)
        put("relationship", c.relationship); put("status", c.status); put("legalProfessionalId", c.legalProfessionalId ?: "")
        put("requiredDocuments", c.requiredDocumentIds.joinToString(","))
    })

    fun markBorrowerConsent(creditId: String, consentId: String): Boolean {
        if (creditId.isBlank() || consentId.isBlank()) return false
        val credit = store.find("credits", creditId) ?: return false
        credit.put("scoreConsent", "OTP_VERIFIED")
        credit.put("scoreConsentEventId", consentId)
        credit.put("scoreConsentAt", System.currentTimeMillis())
        store.replace("credits", credit)
        return true
    }

    fun appendAudit(entityId: String, event: String, actorId: String, details: String) = store.add("audit", JSONObject().apply {
        put("id", "AUD-${System.currentTimeMillis()}"); put("entityId", entityId); put("event", event)
        put("actorId", actorId); put("at", System.currentTimeMillis()); put("details", details)
    })

    fun audit(entityId: String) = store.all("audit").filter { it.optString("entityId") == entityId }
}
