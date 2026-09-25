package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/**
 * One-way compatibility projection from the retained V5/V6.2 engines into the
 * canonical V7 data model. It is idempotent and never writes to legacy storage.
 *
 * During migration, legacy modules may still be the implementation surface, but
 * their completed records must become visible to the V7 command centre, metrics,
 * audit and downstream modules through this projection.
 */
object V7LegacyProjectionSync {
    private const val SOURCE = "V5_V62_PROJECTION"

    fun sync(c: Context): SyncResult {
        val owner = V62Integration.currentUserId(c)
        val result = SyncResult()

        syncPeople(c, owner, result)
        val relationshipMap = syncRelationships(c, owner, result)
        syncRepayments(c, owner, relationshipMap, result)
        syncAssets(c, owner, result)
        syncLiabilities(c, owner, result)
        syncDocuments(c, owner, result)
        syncPolicies(c, owner, result)
        syncClaims(c, owner, result)
        syncNominees(c, owner, result)
        syncTrade(c, owner, relationshipMap, result)
        syncFormal(c, owner, relationshipMap, result)
        syncRentals(c, owner, relationshipMap, result)
        syncFunding(c, owner, result)
        syncQr(c, owner, relationshipMap, result)
        syncAlerts(c, owner, result)
        return result
    }

    data class SyncResult(
        var people: Int = 0,
        var relationships: Int = 0,
        var repayments: Int = 0,
        var assets: Int = 0,
        var liabilities: Int = 0,
        var documents: Int = 0,
        var policies: Int = 0,
        var claims: Int = 0,
        var nominees: Int = 0,
        var trade: Int = 0,
        var formal: Int = 0,
        var rentals: Int = 0,
        var funding: Int = 0,
        var qr: Int = 0,
        var alerts: Int = 0
    )

    private fun sourceId(o: JSONObject): String = o.optString("id")

    private fun upsert(c: Context, key: String, source: JSONObject, mapped: JSONObject): Boolean {
        val sid = sourceId(source)
        if (sid.isBlank()) return false
        mapped.put("sourceSystem", SOURCE)
        mapped.put("sourceRecordId", sid)
        mapped.put("ownerUserId", V7Core.user(c))
        val existing = V7Core.all(c, key).firstOrNull {
            it.optString("sourceSystem") == SOURCE && it.optString("sourceRecordId") == sid
        }
        if (existing == null) {
            V7Core.add(c, key, mapped)
            return true
        }
        mapped.put("id", existing.optString("id"))
        V7Core.replace(c, key, mapped)
        return false
    }

    private fun syncPeople(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.COUNTERPARTIES)
            .filter { it.optString("createdBy", it.optString("ownerUserId")) == owner }
            .forEach { p ->
                val mapped = JSONObject().apply {
                    put("id", V7Core.id("PERSON"))
                    put("name", p.optString("name"))
                    put("mobile", p.optString("mobile"))
                    put("pan", p.optString("pan"))
                    put("aadhaar", p.optString("aadhaar"))
                    put("gstin", p.optString("gstin"))
                    put("legacyProfileId", p.optString("profileId"))
                    put("role", p.optString("role"))
                }
                if (upsert(c, V7Core.Keys.PEOPLE, p, mapped)) r.people++
            }
        V62Store.all(c, V62Store.PEOPLE)
            .filter { it.optString("ownerUserId") == owner || it.optString("createdBy") == owner }
            .forEach { p ->
                val mapped = JSONObject().apply {
                    put("id", V7Core.id("PERSON"))
                    put("name", p.optString("name"))
                    put("mobile", p.optString("mobile"))
                    put("pan", p.optString("pan"))
                    put("aadhaar", p.optString("aadhaar"))
                    put("gstin", p.optString("gstin"))
                }
                if (upsert(c, V7Core.Keys.PEOPLE, p, mapped)) r.people++
            }
    }

    private fun syncRelationships(c: Context, owner: String, r: SyncResult): MutableMap<String, String> {
        val map = mutableMapOf<String, String>()
        V62Store.all(c, V62Store.RELATIONSHIPS)
            .filter { it.optString("ownerUserId") == owner }
            .forEach { x ->
                val mapped = JSONObject().apply {
                    put("id", V7Core.id("REL"))
                    put("partyId", x.optString("counterpartyId", x.optString("partyId")))
                    put("type", x.optString("type", "CREDIT"))
                    put("direction", x.optString("direction", "RECEIVABLE"))
                    put("amount", x.optDouble("amount", x.optDouble("principal", 0.0)))
                    put("outstanding", x.optDouble("outstanding", x.optDouble("amount", 0.0)))
                    put("roiPercent", x.optDouble("roiPercent", x.optDouble("roi", 0.0)))
                    put("repaymentStructure", x.optString("repaymentStructure", x.optString("repaymentMethod", "PRINCIPAL_PLUS_INTEREST")))
                    put("purpose", x.optString("purpose"))
                    put("status", x.optString("status", "ACTIVE"))
                    put("consentRequired", x.optBoolean("consentRequired", true))
                    put("sourceUpdatedAt", x.optLong("updatedAt", x.optLong("createdAt", V7Core.now())))
                }
                val isNew = upsert(c, V7Core.Keys.RELATIONSHIPS, x, mapped)
                val v7 = V7Core.all(c, V7Core.Keys.RELATIONSHIPS).firstOrNull {
                    it.optString("sourceSystem") == SOURCE && it.optString("sourceRecordId") == x.optString("id")
                }
                if (v7 != null) map[x.optString("id")] = v7.optString("id")
                if (isNew) r.relationships++
            }
        return map
    }

    private fun syncRepayments(c: Context, owner: String, relMap: Map<String, String>, r: SyncResult) {
        V62Store.all(c, V62Store.REPAYMENTS)
            .filter { it.optString("ownerUserId", it.optString("recordedBy")) == owner }
            .forEach { x ->
                val relId = relMap[x.optString("relationshipId")] ?: return@forEach
                val mapped = JSONObject().apply {
                    put("id", V7Core.id("REPAY"))
                    put("relationshipId", relId)
                    put("amount", x.optDouble("amount", 0.0))
                    put("principal", x.optDouble("principal", x.optDouble("amount", 0.0)))
                    put("interest", x.optDouble("interest", 0.0))
                    put("method", x.optString("method", "UNKNOWN"))
                    put("consentVerified", x.optBoolean("otpVerified", x.optBoolean("consentVerified", false)))
                    put("timestamp", x.optLong("timestamp", x.optLong("createdAt", V7Core.now())))
                }
                if (upsert(c, V7Core.Keys.REPAYMENTS, x, mapped)) r.repayments++
            }
    }

    private fun syncAssets(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.ASSETS).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("ASSET")); put("ownerId", owner)
                put("type", x.optString("type", "OTHER")); put("description", x.optString("description", x.optString("name")))
                put("value", x.optDouble("value", 0.0)); put("currentValue", x.optDouble("currentValue", x.optDouble("value", 0.0)))
                put("status", x.optString("lifecycleStatus", "ACTIVE"))
                put("documentId", x.optString("documentId")); put("nomineeId", x.optString("nomineeId"))
            }
            if (upsert(c, V7Core.Keys.ASSETS, x, mapped)) r.assets++
        }
    }

    private fun syncLiabilities(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.LIABILITIES).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("LIAB")); put("ownerId", owner)
                put("type", x.optString("type", "LIABILITY"))
                put("amount", x.optDouble("amount", 0.0))
                put("outstanding", x.optDouble("outstanding", x.optDouble("amount", 0.0)))
                put("rate", x.optDouble("rate", x.optDouble("roi", 0.0)))
                put("status", x.optString("status", "ACTIVE"))
            }
            if (upsert(c, V7Core.Keys.LIABILITIES, x, mapped)) r.liabilities++
        }
    }

    private fun syncDocuments(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.DOCUMENTS).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("DOC")); put("ownerId", owner)
                put("type", x.optString("type", "DOCUMENT")); put("name", x.optString("name", x.optString("fileName")))
                put("uri", x.optString("uri")); put("createdAt", x.optLong("createdAt", V7Core.now()))
                put("verified", x.optBoolean("verified", false))
            }
            if (upsert(c, V7Core.Keys.DOCUMENTS, x, mapped)) r.documents++
        }
    }

    private fun syncPolicies(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.INSURANCE).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("POL")); put("ownerId", owner)
                put("policyNumber", x.optString("policyNumber")); put("provider", x.optString("provider", x.optString("insurer")))
                put("type", x.optString("type", "INSURANCE")); put("status", x.optString("status", "ACTIVE"))
                put("premium", x.optDouble("premium", 0.0)); put("sumAssured", x.optDouble("sumAssured", 0.0))
            }
            if (upsert(c, V7Core.Keys.POLICIES, x, mapped)) r.policies++
        }
    }

    private fun syncClaims(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.CLAIMS).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("CLAIM")); put("ownerId", owner)
                put("assetId", x.optString("assetId")); put("claimant", x.optString("claimant"))
                put("amount", x.optDouble("claimAmount", x.optDouble("amount", 0.0)))
                put("status", x.optString("status", "OPEN")); put("institution", x.optString("institution"))
            }
            if (upsert(c, V7Core.Keys.CLAIMS, x, mapped)) r.claims++
        }
    }

    private fun syncNominees(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.NOMINEES).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("NOM")); put("ownerId", owner)
                put("name", x.optString("name")); put("relationship", x.optString("relationship"))
                put("mobile", x.optString("mobile")); put("sharePercent", x.optDouble("sharePercent", 100.0))
            }
            if (upsert(c, V7Core.Keys.NOMINEES, x, mapped)) r.nominees++
        }
    }

    private fun syncTrade(c: Context, owner: String, relMap: Map<String, String>, r: SyncResult) {
        V62Store.all(c, V62Store.TRADE_CREDIT).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("TRADE")); put("relationshipId", relMap[x.optString("relationshipId")] ?: "")
                put("invoiceNumber", x.optString("invoiceNumber")); put("invoiceAmount", x.optDouble("invoiceAmount", 0.0))
                put("outstandingAmount", x.optDouble("outstandingAmount", x.optDouble("invoiceAmount", 0.0)))
                put("gstin", x.optString("gstin")); put("requiresReview", x.optBoolean("requiresReview", true))
            }
            if (upsert(c, V7Core.Keys.TRADE, x, mapped)) r.trade++
        }
    }

    private fun syncFormal(c: Context, owner: String, relMap: Map<String, String>, r: SyncResult) {
        V62Store.all(c, V62Store.FORMAL_CREDIT).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("FORMAL")); put("relationshipId", relMap[x.optString("relationshipId")] ?: "")
                put("lender", x.optString("lender", x.optString("institution"))); put("facility", x.optString("facility", x.optString("type")))
                put("sanctioned", x.optDouble("sanctioned", x.optDouble("amount", 0.0)))
                put("outstanding", x.optDouble("outstanding", 0.0)); put("status", x.optString("status", "ACTIVE"))
            }
            if (upsert(c, V7Core.Keys.FORMAL, x, mapped)) r.formal++
        }
    }

    private fun syncRentals(c: Context, owner: String, relMap: Map<String, String>, r: SyncResult) {
        V62Store.all(c, V62Store.RENTALS).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("RENT")); put("relationshipId", relMap[x.optString("relationshipId")] ?: "")
                put("property", x.optString("property", x.optString("description"))); put("rent", x.optDouble("rent", x.optDouble("amount", 0.0)))
                put("deposit", x.optDouble("deposit", 0.0)); put("status", x.optString("status", "ACTIVE"))
                put("agreementDocumentId", x.optString("agreementDocumentId"))
            }
            if (upsert(c, V7Core.Keys.RELATIONSHIPS, x, mapped)) r.rentals++
        }
    }

    private fun syncFunding(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.FUNDING_REQUESTS).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("FUND")); put("requestType", x.optString("type", "FUNDING"))
                put("amount", x.optDouble("amount", 0.0)); put("purpose", x.optString("purpose"))
                put("status", x.optString("status", "DRAFT")); put("createdAt", x.optLong("createdAt", V7Core.now()))
            }
            if (upsert(c, V7Core.Keys.FUNDING, x, mapped)) r.funding++
        }
    }

    private fun syncQr(c: Context, owner: String, relMap: Map<String, String>, r: SyncResult) {
        V62Store.all(c, V62Store.QR_KHATA).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("QR")); put("relationshipId", relMap[x.optString("relationshipId")] ?: "")
                put("payload", x.optString("payload", x.optString("qrData"))); put("timestamp", x.optLong("timestamp", V7Core.now()))
                put("verified", x.optBoolean("verified", false))
            }
            if (upsert(c, V7Core.Keys.QR, x, mapped)) r.qr++
        }
    }

    private fun syncAlerts(c: Context, owner: String, r: SyncResult) {
        V62Store.all(c, V62Store.ALERTS).filter { it.optString("ownerUserId") == owner }.forEach { x ->
            val mapped = JSONObject().apply {
                put("id", V7Core.id("ALERT")); put("type", x.optString("type", "INFO"))
                put("message", x.optString("message")); put("entityId", x.optString("entityId"))
                put("severity", x.optString("severity", "INFO")); put("acknowledged", x.optBoolean("acknowledged", false))
                put("createdAt", x.optLong("createdAt", V7Core.now()))
            }
            if (upsert(c, V7Core.Keys.ALERTS, x, mapped)) r.alerts++
        }
    }
}
