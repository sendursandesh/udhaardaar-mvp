package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/**
 * Single persistence boundary for V6.2. Screens should use this boundary instead
 * of maintaining module-specific copies. V5LocalStore remains the physical store
 * during the migration; the contract is intentionally backend-ready.
 */
class V62Repository(context: Context) {
    private val store = V5LocalStore(context.applicationContext)

    fun relationships() = store.all(ArthSaathiV62Core.RELATIONSHIPS)
    fun repayments() = store.all(ArthSaathiV62Core.REPAYMENTS)
    fun assets() = store.all(ArthSaathiV62Core.ASSETS)
    fun insurance() = store.all(ArthSaathiV62Core.INSURANCE)
    fun documents() = store.all(ArthSaathiV62Core.DOCUMENTS)
    fun ttmm() = store.all(ArthSaathiV62Core.TTMM)
    fun savings() = store.all(ArthSaathiV62Core.SAVINGS)

    fun saveRelationship(value: JSONObject) {
        store.replace(ArthSaathiV62Core.RELATIONSHIPS, value)
        V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED, value.optString("id")))
    }

    fun saveRepayment(value: JSONObject) {
        store.replace(ArthSaathiV62Core.REPAYMENTS, value)
        V62EventBus.publish(V62Event(V62Events.REPAYMENT_CHANGED, value.optString("id")))
    }

    fun saveAsset(value: JSONObject) {
        store.replace(ArthSaathiV62Core.ASSETS, value)
        V62EventBus.publish(V62Event(V62Events.ASSET_CHANGED, value.optString("id")))
    }

    fun savePolicy(value: JSONObject) {
        store.replace(ArthSaathiV62Core.INSURANCE, value)
        V62EventBus.publish(V62Event(V62Events.POLICY_CHANGED, value.optString("id")))
    }

    fun saveDocument(value: JSONObject) {
        store.replace(ArthSaathiV62Core.DOCUMENTS, value)
        V62EventBus.publish(V62Event(V62Events.DOCUMENT_ADDED, value.optString("id")))
    }

    fun saveTtmmExpense(value: JSONObject) {
        store.replace(ArthSaathiV62Core.TTMM, value)
        V62EventBus.publish(V62Event(V62Events.TTMM_EXPENSE_CHANGED, value.optString("id")))
    }

    fun historyConsentGranted(counterpartyId: String, relationshipId: String): Boolean =
        store.all(ArthSaathiV62Core.CONSENTS).any {
            it.optString("eventType") == "HISTORY_SHARING" &&
            it.optString("counterpartyId") == counterpartyId &&
            it.optString("relationshipId") == relationshipId &&
            it.optString("status") == "VERIFIED"
        }

    fun creditRegistrationConsentGranted(relationshipId: String): Boolean =
        store.all(ArthSaathiV62Core.CONSENTS).any {
            it.optString("eventType") == "CREDIT_REGISTRATION" &&
            it.optString("relationshipId") == relationshipId &&
            it.optString("status") == "VERIFIED"
        }

    fun saveConsent(value: JSONObject) {
        store.replace(ArthSaathiV62Core.CONSENTS, value)
        V62EventBus.publish(V62Event(V62Events.CONSENT_CHANGED, value.optString("id")))
    }

    /** Counterparty creation is deliberately transaction-scoped. */
    fun saveCounterpartyForRelationship(value: JSONObject, relationshipType: String): Boolean {
        if (relationshipType !in V62ArchitectureSpec.relationshipTypes) return false
        if (value.optString("transactionContext").isBlank()) return false
        store.replace(ArthSaathiV62Core.COUNTERPARTIES, value)
        return true
    }
}
