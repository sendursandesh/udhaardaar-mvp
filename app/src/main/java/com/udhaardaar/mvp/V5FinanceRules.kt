package com.udhaardaar.mvp

import java.text.ParsePosition
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
        require(validDate(startDate) && validDate(endDate)) { "Dates must be in yyyy-MM-dd format" }
        require(endDateOnOrAfterStart(startDate, endDate)) { "End date cannot be before start date" }

        val periods = monthsBetween(startDate, endDate).coerceAtLeast(1)
        val normalizedMethod = repaymentMethod.trim().lowercase(Locale.ROOT)
        val ratePerMonth = roiPercent / 100.0 / 12.0
        val totalPayable: Double
        val emi: Double

        if (normalizedMethod.contains("principal") && normalizedMethod.contains("interest")) {
            val totalInterest = principal * (roiPercent / 100.0) * (periods / 12.0)
            totalPayable = principal + totalInterest
            emi = totalPayable / periods
        } else {
            totalPayable = if (ratePerMonth == 0.0) {
                principal
            } else {
                val factor = (1 + ratePerMonth).pow(periods)
                principal * ratePerMonth * factor / (factor - 1) * periods
            }
            emi = totalPayable / periods
        }

        return CreditPlan(
            principal = principal,
            totalPayable = totalPayable,
            totalInterest = (totalPayable - principal).coerceAtLeast(0.0),
            emi = emi
        )
    }

    fun validDate(value: String): Boolean {
        if (!Regex("^\\d{4}-\\d{2}-\\d{2}$").matches(value)) return false
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val position = ParsePosition(0)
        return format.parse(value, position) != null && position.index == value.length
    }

    fun endDateOnOrAfterStart(startDate: String, endDate: String): Boolean {
        if (!validDate(startDate) || !validDate(endDate)) return false
        return parseDate(endDate) >= parseDate(startDate)
    }

    fun monthsBetween(startDate: String, endDate: String): Int {
        val start = parseDate(startDate) ?: return 0
        val end = parseDate(endDate) ?: return 0
        val years = end.get(Calendar.YEAR) - start.get(Calendar.YEAR)
        val months = end.get(Calendar.MONTH) - start.get(Calendar.MONTH)
        var result = years * 12 + months
        if (end.get(Calendar.DAY_OF_MONTH) < start.get(Calendar.DAY_OF_MONTH)) result--
        return result.coerceAtLeast(0)
    }

    private fun parseDate(value: String): Calendar? {
        if (!validDate(value)) return null
        val format = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val date = format.parse(value) ?: return null
        return Calendar.getInstance(Locale.US).apply { time = date }
    }
}
