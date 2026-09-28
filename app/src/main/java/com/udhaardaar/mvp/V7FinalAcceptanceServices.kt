package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

object V7ScoreEngine {
    data class Result(val score: Int, val band: String, val factors: List<String>)
    fun calculate(c: Context, partyId: String, borrowerConsentVerified: Boolean): Result? {
        if (!borrowerConsentVerified || partyId.isBlank()) return null
        val relationships = V7Core.all(c, V7Core.Keys.RELATIONSHIPS).filter { it.optString("partyId") == partyId }
        val ids = relationships.map { it.optString("id") }.toSet()
        val repayments = V7Core.all(c, V7Core.Keys.REPAYMENTS).filter { it.optString("relationshipId") in ids }
        val total = relationships.size
        val completed = relationships.count { it.optString("status") == "CLOSED" }
        val overdue = relationships.sumOf { it.optInt("overdueCount", 0) }
        val disputed = repayments.count { it.optString("status").equals("DISPUTED", true) }
        val lateDays = relationships.mapNotNull { it.optDouble("averageDaysLate", Double.NaN).takeUnless { x -> x.isNaN() } }
            .average().let { if (it.isNaN()) 0.0 else it }
        val completion = if (total == 0) 0.5 else (completed.toDouble() / total).coerceIn(0.0, 1.0)
        val punctuality = (1.0 - lateDays / 90.0).coerceIn(0.0, 1.0)
        val disputes = (1.0 - disputed.toDouble() / max(1, repayments.size)).coerceIn(0.0, 1.0)
        val activity = (repayments.size.toDouble() / max(1, total * 4)).coerceIn(0.0, 1.0)
        val overduePenalty = (overdue.toDouble() / max(1, total * 3)).coerceIn(0.0, 1.0)
        val score = (300 + completion * 300 + punctuality * 180 + disputes * 100 + activity * 120 - overduePenalty * 220).toInt().coerceIn(300, 900)
        val band = when { score >= 800 -> "Excellent"; score >= 700 -> "Strong"; score >= 600 -> "Moderate"; score >= 500 -> "Needs attention"; else -> "High attention" }
        return Result(score, band, listOf(
            "Completed obligations: " + completed + " of " + total,
            "Overdue records: " + overdue,
            "Average reported delay: " + "%.1f".format(lateDays) + " days",
            "Repayment events: " + repayments.size,
            "Disputed events: " + disputed
        ))
    }
}

object V7MIS {
    fun snapshot(c: Context): JSONObject {
        val base = V7Core.metrics(c)
        val services = V7Core.all(c, V7Core.Keys.SERVICES)
        val invoices = V7Core.all(c, V7Core.Keys.INVOICES)
        val payments = V7Core.all(c, V7Core.Keys.PAYMENTS)
        val reconciled = payments.filter { it.optString("status") == V7RevenueEngine.PaymentStatus.RECONCILED.name }
        return JSONObject().apply {
            put("assets", base.optDouble("assets")); put("liabilities", base.optDouble("liabilities"))
            put("receivables", base.optDouble("receivables")); put("payables", base.optDouble("payables"))
            put("netWorth", base.optDouble("netWorth")); put("portfolioValue", base.optDouble("portfolioValue"))
            put("portfolioGain", base.optDouble("portfolioGain")); put("activeCredits", base.optInt("activeCredits"))
            put("documents", base.optInt("documents")); put("claimsPending", base.optInt("claimsPending"))
            put("serviceCount", services.count { it.optBoolean("active", true)}); put("invoiceCount", invoices.size)
            put("paymentCount", payments.size); put("reconciledRevenue", reconciled.sumOf { it.optDouble("amount") })
            put("recordedPayments", payments.sumOf { it.optDouble("amount") }); put("auditEvents", V7Core.all(c, V7Core.Keys.AUDIT).size)
        }
    }
}

object V7ExternalIntegration {
    enum class Connector { GENERIC_ERP, TALLY_PRIME, ACCOUNTING_ERP }
    enum class Format { JSON, XML, CSV }
    data class ExportPackage(val connector: Connector, val format: Format, val payload: String, val recordCount: Int)

    fun export(c: Context, connector: Connector, format: Format): ExportPackage {
        val keys = listOf(V7Core.Keys.PEOPLE, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.REPAYMENTS, V7Core.Keys.ASSETS, V7Core.Keys.LIABILITIES, V7Core.Keys.INVOICES, V7Core.Keys.PAYMENTS)
        val rows = JSONArray()
        keys.forEach { key -> V7Core.all(c, key).forEach { o ->
            rows.put(JSONObject().apply { put("entity", key.removePrefix("v7_")); put("record", JSONObject(o.toString())) })
        }}
        val payload = when (format) {
            Format.JSON -> JSONObject().apply { put("schema","arthsaathi.v7"); put("connector",connector.name); put("generatedAt",V7Core.now()); put("records",rows) }.toString()
            Format.XML -> tallyXml(connector, rows)
            Format.CSV -> buildString {
                append("entity,id,ownerUserId,updatedAt\n")
                for (i in 0 until rows.length()) {
                    val item=rows.getJSONObject(i); val record=item.getJSONObject("record")
                    append(csv(item.getString("entity"))).append(',').append(csv(record.optString("id"))).append(',')
                        .append(csv(record.optString("ownerUserId"))).append(',').append(record.optLong("updatedAt")).append('\n')
                }
            }
        }
        return ExportPackage(connector, format, payload, rows.length())
    }
    private fun tallyXml(connector: Connector, rows: JSONArray): String {
        val out=StringBuilder("<ENVELOPE><HEADER><VERSION>1</VERSION><TALLYREQUEST>Import Data</TALLYREQUEST><TYPE>Data</TYPE><ID>ArthSaathi V7</ID></HEADER><BODY><DATA>")
        for (i in 0 until rows.length()) {
            val item=rows.getJSONObject(i); val record=item.getJSONObject("record")
            out.append("<TALLYMESSAGE><ARTHSAATHI><CONNECTOR>").append(xml(connector.name)).append("</CONNECTOR><ENTITY>")
                .append(xml(item.getString("entity"))).append("</ENTITY><ID>").append(xml(record.optString("id"))).append("</ID><AMOUNT>")
                .append(record.optDouble("amount",record.optDouble("currentValue",0.0))).append("</AMOUNT></ARTHSAATHI></TALLYMESSAGE>")
        }
        return out.append("</DATA></BODY></ENVELOPE>").toString()
    }
    private fun xml(v:String)=v.replace("&","&amp;").replace("<","&lt;").replace(">","&gt;")
    private fun csv(v:String)=""" + v.replace(""","""") + """
}
