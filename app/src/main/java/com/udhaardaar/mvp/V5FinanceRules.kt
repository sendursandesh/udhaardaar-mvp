package com.udhaardaar.mvp

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow

object V5FinanceRules {
    data class CreditPlan(
        val principal: Double,
        val totalPayable: Double,
        val totalInterest: Double,
        val emi: Double
    )

    fun calculateCreditPlan(
        principal: Double,
        roiPercent: Double,
        periodicity: String,
        startDate: String,
        endDate: String,
        repaymentMethod: String
    ): CreditPlan {
        require(principal > 0.0) { "Principal must be greater than zero" }
        require(roiPercent >= 0.0) { "ROI cannot be negative" }
        require(periodicity.isNotBlank()) { "Periodicity is required" }
        require(repaymentMethod.isNotBlank()) { "Repayment method is required" }
        require(validDate(startDate) && validDate(endDate)) { "Dates must be in yyyy-MM-dd format" }
        require(endDateOnOrAfterStart(startDate, endDate)) { "End date cannot be before start date" }

        val periods = monthsBetween(startDate, endDate).coerceAtLeast(1)
        val method = repaymentMethod.trim().lowercase(Locale.ROOT)
        val monthlyRate = roiPercent / 100.0 / 12.0

        val totalPayable = when {
            method.contains("principal") && method.contains("interest") ->
                principal + principal * (roiPercent / 100.0) * (periods / 12.0)
            monthlyRate == 0.0 -> principal
            else -> {
                val factor = (1.0 + monthlyRate).pow(periods.toDouble())
                val installment = principal * monthlyRate * factor / (factor - 1.0)
                installment * periods
            }
        }

        return CreditPlan(
            principal = principal,
            totalPayable = totalPayable,
            totalInterest = (totalPayable - principal).coerceAtLeast(0.0),
            emi = totalPayable / periods
        )
    }

    fun validDate(value: String): Boolean = parseDate(value) != null

    fun endDateOnOrAfterStart(startDate: String, endDate: String): Boolean {
        val start = parseDate(startDate) ?: return false
        val end = parseDate(endDate) ?: return false
        return end.timeInMillis >= start.timeInMillis
    }

    fun monthsBetween(startDate: String, endDate: String): Int {
        if (!validDate(startDate) || !validDate(endDate)) return 0
        val startYear = startDate.substring(0, 4).toInt()
        val startMonth = startDate.substring(5, 7).toInt()
        val startDay = startDate.substring(8, 10).toInt()
        val endYear = endDate.substring(0, 4).toInt()
        val endMonth = endDate.substring(5, 7).toInt()
        val endDay = endDate.substring(8, 10).toInt()
        var result = (endYear - startYear) * 12 + (endMonth - startMonth)
        if (endDay < startDay) result--
        return result.coerceAtLeast(0)
    }

    private fun parseDate(value: String): Calendar? {
        if (!Regex("""^\d{4}-\d{2}-\d{2}$""").matches(value)) return null
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        return try {
            val parsed = format.parse(value) ?: return null
            if (format.format(parsed) != value) return null
            Calendar.getInstance(Locale.US).apply {
                time = parsed
                set(Calendar.HOUR_OF_DAY, 0)
                set(Calendar.MINUTE, 0)
                set(Calendar.SECOND, 0)
                set(Calendar.MILLISECOND, 0)
            }
        } catch (_: Exception) {
            null
        }
    }
}