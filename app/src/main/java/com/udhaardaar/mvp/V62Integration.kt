package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import java.util.Locale

/** Canonical V6.2 cross-module integration boundary. */
object V62Integration {
    private fun store(c: Context) = V5LocalStore(c.applicationContext)

    fun currentUserId(c: Context): String =
        c.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE)
            .getString("current_mobile", "")?.trim().orEmpty().ifBlank { "self" }

    fun findCounterparties(c: Context, query: String): List<JSONObject> {
        val q = query.trim().lowercase(Locale.getDefault())
        return store(c).all(V62Store.COUNTERPARTIES).filter { p ->
            q.isEmpty() || listOf("id", "name", "mobile", "pan", "aadhaar", "gstin", "profileId")
                .any { p.optString(it).lowercase(Locale.getDefault()).contains(q) }
        }
    }

    fun createCounterparty(c: Context, name: String, mobile: String, pan: String = "", aadhaar: String = "", gstin: String = "", transactionContext: String): JSONObject? {
        if (name.trim().length < 2 || !mobile.trim().matches(Regex("[6-9][0-9]{9}")) || transactionContext.isBlank()) return null
        val existing = findCounterparties(c, mobile).firstOrNull()
        if (existing != null) return existing
        return JSONObject().apply {
            put("id", V62Store.id("CP"))
            put("profileId", V62Store.id("PROFILE"))
            put("name", name.trim())
            put("mobile", mobile.trim())
            put("pan", pan.trim().uppercase(Locale.getDefault()))
            put("aadhaar", aadhaar.trim())
            put("gstin", gstin.trim().uppercase(Locale.getDefault()))
            put("transactionContext", transactionContext)
            put("createdBy", currentUserId(c))
            put("createdAt", System.currentTimeMillis())
        }.also { store(c).add(V62Store.COUNTERPARTIES, it); V62EventBus.publish(V62Event(V62Events.PROFILE_CHANGED, it.optString("id"))) }
    }

    fun recordConsent(c: Context, subjectId: String, relationshipId: String, eventType: String, otpVerified: Boolean): JSONObject {
        val o = JSONObject().apply {
            put("id", V62Store.id("CONSENT")); put("subjectId", subjectId); put("counterpartyId", subjectId)
            put("relationshipId", relationshipId); put("eventType", eventType)
            put("granted", otpVerified); put("otpVerified", otpVerified)
            put("status", if (otpVerified) "VERIFIED" else "PENDING")
            put("verifiedAt", if (otpVerified) System.currentTimeMillis() else 0L)
            put("createdAt", System.currentTimeMillis())
        }
        store(c).add(V62Store.CONSENTS, o)
        V62EventBus.publish(V62Event(V62Events.CONSENT_CHANGED, o.optString("id")))
        return o
    }

    fun relationship(c: Context, id: String): JSONObject? = store(c).find(V62Store.RELATIONSHIPS, id)

    fun publishRelationship(c: Context, relationship: JSONObject) {
        store(c).replace(V62Store.RELATIONSHIPS, relationship)
        V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, relationship.optString("id")))
    }

    fun publishDocument(c: Context, document: JSONObject) {
        store(c).replace(V62Store.DOCUMENTS, document)
        V62EventBus.publish(V62Event(V62Events.DOCUMENT_ADDED, document.optString("id")))
    }

    fun addAlert(c: Context, type: String, message: String, entityId: String = "", severity: String = "INFO") {
        val a = JSONObject().apply {
            put("id", V62Store.id("ALERT")); put("type", type); put("message", message)
            put("entityId", entityId); put("severity", severity); put("createdAt", System.currentTimeMillis()); put("acknowledged", false)
        }
        store(c).add(V62Store.ALERTS, a)
        V62EventBus.publish(V62Event(V62Events.ALERT_CREATED, a.optString("id")))
    }

    fun recalculateRelationshipOutstanding(c: Context, relationshipId: String): Double {
        val rel = relationship(c, relationshipId) ?: return 0.0
        val principal = rel.optDouble("amount", rel.optDouble("principal", 0.0))
        val repayments = store(c).all(V62Store.REPAYMENTS).filter { it.optString("relationshipId") == relationshipId }
        val outstanding = (principal - repayments.sumOf { it.optDouble("principal", it.optDouble("amount", 0.0)) }).coerceAtLeast(0.0)
        rel.put("outstanding", outstanding)
        rel.put("status", if (outstanding <= 0.0 && principal > 0.0) "CLOSED" else "ACTIVE")
        publishRelationship(c, rel)
        return outstanding
    }

    fun score(c: Context, counterpartyId: String): Int {
        val rels = store(c).all(V62Store.RELATIONSHIPS).filter { it.optString("counterpartyId") == counterpartyId }
        val reps = store(c).all(V62Store.REPAYMENTS).filter { it.optString("counterpartyId") == counterpartyId }
        val principal = rels.sumOf { it.optDouble("amount", it.optDouble("principal", 0.0)) }
        val repaid = reps.sumOf { it.optDouble("principal", it.optDouble("amount", 0.0)) }
        val overdue = rels.sumOf { it.optDouble("overdueAmount", 0.0) }
        return (750 + (if (principal > 0) ((repaid / principal) * 120).toInt() else 0) - (overdue / 1000).toInt() - rels.count { it.optString("status") == "DEFAULTED" } * 60)
            .coerceIn(300, 900)
    }
}
