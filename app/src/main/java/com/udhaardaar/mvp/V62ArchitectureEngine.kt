package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import java.util.UUID

/** Shared V6.2 persistence and domain boundary. */
object V62Store {
    const val USERS = "v62_users"; const val COUNTERPARTIES = "v62_counterparties"; const val RELATIONSHIPS = "v62_relationships"; const val REPAYMENTS = "v62_repayments"; const val CONSENTS = "v62_consents"; const val DOCUMENTS = "v62_documents"; const val ASSETS = "v62_assets"; const val LIABILITIES = "v62_liabilities"; const val INSURANCE = "v62_insurance"; const val RENTALS = "v62_rentals"; const val TTMM_GROUPS = "v62_ttmm_groups"; const val TTMM_EXPENSES = "v62_ttmm_expenses"; const val TTMM_SETTLEMENTS = "v62_ttmm_settlements"; const val TTMM_CONTRIBUTIONS = "v62_ttmm_contributions"; const val QR_KHATA = "v62_qr_khata"; const val NOMINEES = "v62_nominees"; const val WILLS = "v62_wills"; const val CLAIMS = "v62_claims"; const val CHARGECHECK = "v62_chargecheck"; const val TRADE_CREDIT = "v62_trade_credit"; const val FORMAL_CREDIT = "v62_formal_credit"; const val FUNDING_REQUESTS = "v62_funding_requests"; const val PEOPLE = "v62_people"; const val ADDRESSES = "v62_addresses"; const val BENEFITS = "v62_benefits"; const val SAVINGS = "v62_savings"; const val ALERTS = "v62_alerts"
    fun id(prefix: String) = prefix + "-" + UUID.randomUUID().toString()
    fun store(c: Context) = V5LocalStore(c.applicationContext)
    fun add(c: Context, key: String, o: JSONObject) = store(c).add(key, o)
    fun all(c: Context, key: String) = store(c).all(key)
    fun replace(c: Context, key: String, o: JSONObject) = store(c).replace(key, o)
}

object V62Relationships {
    fun createCounterparty(c: Context, relationshipId: String, role: String, name: String, mobile: String = "", pan: String = "", aadhaar: String = "", gstin: String = "") = JSONObject().apply { put("id", V62Store.id("CP")); put("role", role); put("name", name.trim()); put("mobile", mobile.trim()); put("pan", pan.trim().uppercase()); put("aadhaar", aadhaar.trim()); put("gstin", gstin.trim().uppercase()); put("transactionContext", relationshipId); put("createdAt", System.currentTimeMillis()) }.also { V62Store.add(c, V62Store.COUNTERPARTIES, it) }
}

object V62MisEngine {
    private fun completedStatus(s: String) = s.uppercase() in setOf("COMPLETED", "SUCCESS", "SUCCESSFUL", "CLAIMED", "REFUNDED", "RECOVERED", "PAID")

    fun metrics(c: Context): JSONObject {
        val owner = V62Integration.currentUserId(c)
        val allAssets = V62Store.all(c, V62Store.ASSETS).filter { it.optString("ownerUserId", "") == owner }
        val currentAssets = allAssets.filter { it.optBoolean("currentAsset", true) && it.optString("lifecycleStatus", "ACTIVE") !in setOf("SOLD", "TRANSFERRED", "GIFTED", "DISPOSED") }
        val repayments = V62Store.all(c, V62Store.REPAYMENTS).filter { it.optString("recordedBy", it.optString("ownerUserId", "")) == owner }
        val savings = V62Store.all(c, V62Store.SAVINGS).filter { it.optString("ownerUserId", "") == owner }
        val rel = V62Store.all(c, V62Store.RELATIONSHIPS).filter { it.optString("ownerUserId", "") == owner }
        val liabilities = V62Store.all(c, V62Store.LIABILITIES).filter { it.optString("ownerUserId", "") == owner }
        val benefits = V62Store.all(c, V62Store.BENEFITS).filter { it.optString("ownerUserId", "") == owner && completedStatus(it.optString("status")) }
        val chargeChecks = V62Store.all(c, V62Store.CHARGECHECK).filter { it.optString("ownerUserId", "") == owner && completedStatus(it.optString("claimStatus", it.optString("status"))) }
        val claims = V62Store.all(c, V62Store.CLAIMS).filter { it.optString("ownerUserId", "") == owner && completedStatus(it.optString("status")) }
        val benefitValue = benefits.sumOf { it.optDouble("valueGenerated", it.optDouble("amount", it.optDouble("benefitValue", 0.0))) }
        val refundValue = chargeChecks.sumOf { it.optDouble("refunds", it.optDouble("refundAmount", 0.0)) }
        val recoveryValue = claims.sumOf { it.optDouble("recoveredAmount", it.optDouble("claimAmount", it.optDouble("amount", 0.0))) }
        val ttmmExpenses = V62Store.all(c, V62Store.TTMM_EXPENSES).filter { it.optString("ownerUserId", "") == owner }
        val ttmmContributions = V62Store.all(c, V62Store.TTMM_CONTRIBUTIONS).filter { it.optString("ownerUserId", "") == owner && it.optString("status", "RECORDED") == "RECORDED" }
        val ttmmSettlements = V62Store.all(c, V62Store.TTMM_SETTLEMENTS).filter { it.optString("ownerUserId", "") == owner && it.optString("status", "SETTLED") == "SETTLED" }
        val groupContributed = ttmmContributions.sumOf { it.optDouble("amount", 0.0) } + ttmmSettlements.sumOf { it.optDouble("amount", 0.0) }
        val groupExpenseTotal = ttmmExpenses.sumOf { it.optDouble("amount", 0.0) }
        return JSONObject().apply {
            put("assetValue", currentAssets.sumOf { it.optDouble("value", 0.0) })
            put("historicalAssetValue", allAssets.sumOf { it.optDouble("value", 0.0) })
            put("interestReceived", repayments.sumOf { it.optDouble("interest", 0.0) })
            put("charges", currentAssets.sumOf { it.optDouble("charges", 0.0) } + repayments.sumOf { it.optDouble("charges", 0.0) })
            put("appSavings", savings.sumOf { it.optDouble("amount", 0.0) })
            put("idleFunds", currentAssets.filter { it.optBoolean("idle", false) }.sumOf { it.optDouble("value", 0.0) })
            put("activeRelationships", rel.count { it.optString("status") != "CLOSED" })
            put("liabilities", liabilities.sumOf { it.optDouble("outstanding", 0.0) })
            put("informalCreditExposure", rel.filter { it.optString("status") != "CLOSED" }.sumOf { it.optDouble("outstanding", it.optDouble("amount", 0.0)) })
            put("completedBenefitValue", benefitValue); put("completedRefundValue", refundValue); put("completedRecoveryValue", recoveryValue)
            put("valueGenerated", benefitValue + refundValue + recoveryValue)
            put("ttmmExpenseTotal", groupExpenseTotal); put("ttmmContributed", groupContributed); put("ttmmOutstanding", (groupExpenseTotal - groupContributed).coerceAtLeast(0.0))
        }
    }
}