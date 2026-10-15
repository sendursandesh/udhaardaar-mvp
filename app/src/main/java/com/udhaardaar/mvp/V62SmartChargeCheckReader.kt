package com.udhaardaar.mvp

import java.util.Locale

/**
 * Local smart reader for ChargeCheck. It combines page-aware OCR with charge/transaction
 * heuristics and keeps the source text so every extracted value can be reviewed.
 * The extraction boundary is intentionally replaceable by a secured AI service later.
 */
object V62SmartChargeCheckReader {
    data class Finding(
        val category: String,
        val description: String,
        val amount: Double,
        val status: String,
        val sourceText: String,
        val page: String = ""
    )

    data class Result(val findings: List<Finding>, val summary: String)

    private val amountRegex = Regex("(?:₹|rs\\.?|inr)\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)", RegexOption.IGNORE_CASE)
    private val bareAmountRegex = Regex("(?<![A-Za-z0-9])([0-9][0-9,]{2,}(?:\\.[0-9]{1,2})?)(?![A-Za-z0-9])")

    private val sanctionRules = listOf(
        "PROCESSING_FEE" to Regex("processing fee|processing charges|origination fee", RegexOption.IGNORE_CASE),
        "DOCUMENTATION_FEE" to Regex("documentation fee|document charges|admin(?:istration)? fee", RegexOption.IGNORE_CASE),
        "LEGAL_VALUATION" to Regex("legal fee|valuation fee|technical fee|technical charges", RegexOption.IGNORE_CASE),
        "INSURANCE" to Regex("insurance premium|insurance charges|insurance", RegexOption.IGNORE_CASE),
        "GST_TAX" to Regex("gst|goods and services tax|tax", RegexOption.IGNORE_CASE),
        "PREPAYMENT" to Regex("prepayment|foreclosure|part[- ]?prepayment", RegexOption.IGNORE_CASE),
        "PENALTY" to Regex("penal|penalty|default interest|late payment", RegexOption.IGNORE_CASE),
        "BOUNCE" to Regex("bounce|return charge|dishonou?r", RegexOption.IGNORE_CASE),
        "ANNUAL_RENEWAL" to Regex("annual fee|renewal fee|renewal charges", RegexOption.IGNORE_CASE),
        "INTEREST" to Regex("interest|int\\.", RegexOption.IGNORE_CASE),
        "OTHER_CHARGE" to Regex("fee|charge|charges", RegexOption.IGNORE_CASE)
    )

    private val debitRules = listOf(
        "PROCESSING_FEE" to Regex("processing fee|processing charges|origination fee", RegexOption.IGNORE_CASE),
        "DOCUMENTATION_FEE" to Regex("documentation|document charges|admin fee|administration fee", RegexOption.IGNORE_CASE),
        "LEGAL_VALUATION" to Regex("legal|valuation|technical", RegexOption.IGNORE_CASE),
        "INSURANCE" to Regex("insurance", RegexOption.IGNORE_CASE),
        "GST_TAX" to Regex("gst|tax", RegexOption.IGNORE_CASE),
        "PREPAYMENT" to Regex("prepayment|foreclosure", RegexOption.IGNORE_CASE),
        "PENALTY" to Regex("penal|penalty|late|default", RegexOption.IGNORE_CASE),
        "BOUNCE" to Regex("bounce|return|dishonou?r", RegexOption.IGNORE_CASE),
        "ANNUAL_RENEWAL" to Regex("annual|renewal", RegexOption.IGNORE_CASE),
        "INTEREST" to Regex("interest|int\\.", RegexOption.IGNORE_CASE),
        "OTHER_CHARGE" to Regex("fee|charge|charges", RegexOption.IGNORE_CASE)
    )

    fun readSanction(text: String): Result {
        val findings = mutableListOf<Finding>()
        text.lines().forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach
            val rule = sanctionRules.firstOrNull { it.second.containsMatchIn(line) } ?: return@forEach
            val amount = extractAmount(line) ?: return@forEach
            val category = rule.first
            findings.add(Finding(category, line.take(180), amount, "SANCTIONED", line, pageOf(line)))
        }
        val unique = findings.distinctBy { "${it.category}|${it.amount}|${it.sourceText}" }
        return Result(unique, "Found ${unique.size} sanctioned charge/fee/interest candidates. Review each against the sanction document.")
    }

    fun readStatement(text: String): Result {
        val findings = mutableListOf<Finding>()
        text.lines().forEach { raw ->
            val line = raw.trim()
            if (line.isBlank()) return@forEach
            val rule = debitRules.firstOrNull { it.second.containsMatchIn(line) } ?: return@forEach
            val amount = extractLastAmount(line) ?: return@forEach
            val category = rule.first
            findings.add(Finding(category, line.take(180), amount, "DEBITED", line, pageOf(line)))
        }
        val unique = findings.distinctBy { "${it.category}|${it.amount}|${it.sourceText}" }
        return Result(unique, "Found ${unique.size} charge/interest debit candidates. Review each against the account statement.")
    }

    private fun extractAmount(line: String): Double? {
        val labelled = amountRegex.findAll(line).mapNotNull { it.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() }.toList()
        if (labelled.isNotEmpty()) return labelled.maxOrNull()
        return bareAmountRegex.findAll(line).mapNotNull { it.groupValues[1].replace(",", "").toDoubleOrNull() }.lastOrNull()
    }

    private fun extractLastAmount(line: String): Double? =
        amountRegex.findAll(line).mapNotNull { it.groupValues.getOrNull(1)?.replace(",", "")?.toDoubleOrNull() }.lastOrNull()
            ?: bareAmountRegex.findAll(line).mapNotNull { it.groupValues[1].replace(",", "").toDoubleOrNull() }.lastOrNull()

    private fun pageOf(line: String): String = Regex("\\[PAGE (\\d+)\\]", RegexOption.IGNORE_CASE).find(line)?.groupValues?.getOrNull(1)?.let { "PAGE $it" }.orEmpty()

    fun categoryLabel(category: String): String = category.replace('_', ' ').lowercase(Locale.getDefault()).replaceFirstChar { it.titlecase() }
}
