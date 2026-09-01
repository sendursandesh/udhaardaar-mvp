package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import java.security.MessageDigest
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.temporal.ChronoUnit

/** Central V5 business boundary. UI must never decide whether an obligation can become ACTIVE. */
class V5PlatformEngine(context: Context) {
    private val store = V5LocalStore(context)
    private val fmt = DateTimeFormatter.ISO_LOCAL_DATE

    fun audit(entity: String, event: String, actor: String, details: String = "", version: Int = 1) {
        val now = System.currentTimeMillis()
        val payload = "$entity|$event|$actor|$now|$version|$details"
        store.add("audit", JSONObject().apply {
            put("id", "AUD-$now-${(0..999).random()}")
            put("entityId", entity); put("event", event); put("actor", actor)
            put("timestamp", now); put("version", version); put("details", details)
            put("hash", sha(payload))
        })
    }

    fun sha(s: String): String = MessageDigest.getInstance("SHA-256")
        .digest(s.toByteArray()).joinToString("") { "%02x".format(it) }

    fun stampDuty(amount: Double, state: String = ""): JSONObject = JSONObject().apply {
        put("applicable", true); put("configuredAmount", 1.0); put("currency", "INR")
        put("state", state); put("rule", "CONFIGURABLE_PROMISSORY_NOTE_RULE")
        put("status", "PENDING_REFERENCE")
    }

    fun repaymentSchedule(principal: Double, roi: Double, start: String, end: String, method: String): List<JSONObject> {
        require(principal > 0 && start.isNotBlank() && end.isNotBlank())
        val s = LocalDate.parse(start, fmt)
        val e = LocalDate.parse(end, fmt)
        require(!e.isBefore(s)) { "End date cannot be before start date" }
        val months = (ChronoUnit.MONTHS.between(s.withDayOfMonth(1), e.withDayOfMonth(1)).coerceAtLeast(0L) + 1L).toInt()
        val list = mutableListOf<JSONObject>()
        val annual = roi / 100.0
        when (method) {
            "EMI" -> {
                val r = annual / 12.0
                val growth = Math.pow(1.0 + r, months.toDouble())
                val emi = if (r == 0.0) principal / months.toDouble() else principal * r * growth / (growth - 1.0)
                var bal = principal
                for (i in 1..months) {
                    val interest = bal * r
                    val principalPaid = minOf((emi - interest).coerceAtLeast(0.0), bal)
                    bal = (bal - principalPaid).coerceAtLeast(0.0)
                    list += JSONObject().apply {
                        put("no", i); put("due", s.plusMonths((i - 1).toLong()).format(fmt))
                        put("amount", emi); put("principal", principalPaid); put("interest", interest); put("outstanding", bal)
                    }
                }
            }
            "PRINCIPAL_PLUS_INTEREST" -> {
                for (i in 1..months) list += JSONObject().apply {
                    put("no", i); put("due", s.plusMonths((i - 1).toLong()).format(fmt))
                    put("amount", if (i == months) principal + principal * annual * months.toDouble() / 12.0 else principal * annual / 12.0)
                    put("principal", if (i == months) principal else 0.0)
                    put("interest", principal * annual / 12.0)
                }
            }
            "BULLET_PRINCIPAL_ONLY" -> list += JSONObject().apply {
                put("no", 1); put("due", e.format(fmt)); put("amount", principal); put("principal", principal); put("interest", 0.0)
            }
            "BULLET_INTEREST_MONTHLY" -> {
                for (i in 1..months) list += JSONObject().apply {
                    put("no", i); put("due", s.plusMonths((i - 1).toLong()).format(fmt))
                    put("amount", if (i == months) principal + principal * annual / 12.0 else principal * annual / 12.0)
                    put("principal", if (i == months) principal else 0.0); put("interest", principal * annual / 12.0)
                }
            }
            "BULLET_PRINCIPAL_PLUS_INTEREST_AT_END" -> list += JSONObject().apply {
                put("no", 1); put("due", e.format(fmt)); put("amount", principal * (1.0 + annual * months.toDouble() / 12.0))
                put("principal", principal); put("interest", principal * annual * months.toDouble() / 12.0)
            }
            else -> throw IllegalArgumentException("Unsupported repayment method: $method")
        }
        return list
    }

    fun creditGate(credit: JSONObject): Pair<Boolean, List<String>> {
        val missing = mutableListOf<String>()
        if (credit.optString("lender").length < 2) missing += "Verified lender"
        if (credit.optString("borrower").length < 2) missing += "Verified borrower"
        if (credit.optDouble("amount", 0.0) <= 0.0) missing += "Principal"
        if (credit.optString("scheduleId").isBlank()) missing += "Repayment schedule"
        if (credit.optString("dpn").isBlank()) missing += "DPN"
        if (credit.optString("documentStatus") != "COMPLETED") missing += "Document consent"
        if (credit.optString("lenderConsent").isBlank()) missing += "Lender consent"
        if (credit.optString("borrowerConsent").isBlank()) missing += "Borrower consent"
        if (credit.optString("stampStatus") != "COMPLETED") missing += "Stamp-duty execution record"
        return Pair(missing.isEmpty(), missing)
    }

    fun calculateScore(profileId: String): JSONObject {
        val credits = store.all("credits").filter {
            it.optString("borrowerProfileId") == profileId || it.optString("lenderProfileId") == profileId
        }
        val confirmed = store.all("repayment_requests").count { it.optString("status") == "CONFIRMED" }
        val overdue = credits.count { it.optString("status") == "OVERDUE" }
        val base = (600 + confirmed * 15 - overdue * 35).coerceIn(300, 900)
        return JSONObject().apply {
            put("profileId", profileId); put("score", base); put("version", "V5-1.0")
            put("calculatedAt", System.currentTimeMillis())
            put("factors", "Confirmed repayments; overdue exposure; recorded credit history")
            put("confidence", if (credits.isEmpty()) "LOW" else "MEDIUM")
        }
    }
}
