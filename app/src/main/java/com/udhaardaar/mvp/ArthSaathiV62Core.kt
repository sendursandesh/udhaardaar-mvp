package com.udhaardaar.mvp

import android.content.Context
import android.net.Uri
import org.json.JSONObject
import java.util.UUID

object ArthSaathiV62Core {
    const val COUNTERPARTIES = "v62_counterparties"
    const val RELATIONSHIPS = "v62_relationships"
    const val REPAYMENTS = "v62_repayments"
    const val DOCUMENTS = "v62_documents"
    const val INSURANCE = "v62_insurance"
    const val ASSETS = "v62_assets"
    const val TTMM = "v62_ttmm"
    const val MIS = "v62_mis"
    const val SAVINGS = "v62_savings"
    const val CONSENTS = "v62_consents"

    fun id(prefix: String) = "$prefix-${UUID.randomUUID()}"

    fun saveDocument(context: Context, uri: Uri, category: String, ownerId: String = "self"): JSONObject {
        val d = JSONObject().apply {
            put("id", id("DOC")); put("ownerId", ownerId); put("category", category)
            put("uri", uri.toString()); put("originalRetained", true)
            put("status", "NEEDS_REVIEW"); put("aiConfidence", 0.0)
            put("createdAt", System.currentTimeMillis())
        }
        V5LocalStore(context).add(DOCUMENTS, d); return d
    }

    fun recordSavings(context: Context, source: String, saved: Double, note: String) {
        if (saved <= 0) return
        V5LocalStore(context).add(SAVINGS, JSONObject().apply {
            put("id", id("SAVE")); put("source", source); put("amount", saved)
            put("note", note); put("date", System.currentTimeMillis())
        })
    }
}

/** AI/OCR boundary. Production implementation can plug in OCR + document classifiers without changing screens. */
interface ArthSaathiDocumentIntelligence {
    fun classify(category: String, extractedText: String): String
    fun extract(category: String, extractedText: String): JSONObject
}

class RuleBasedDocumentIntelligence : ArthSaathiDocumentIntelligence {
    override fun classify(category: String, extractedText: String) = category
    override fun extract(category: String, extractedText: String): JSONObject = JSONObject().apply {
        put("sourceCategory", category); put("rawTextAvailable", extractedText.isNotBlank())
        put("verificationRequired", true); put("confidence", 0.0)
    }
}
