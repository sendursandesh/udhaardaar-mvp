package com.udhaardaar.mvp

import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.DateTimeParseException
import java.time.temporal.ChronoUnit
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
        val normalizedMethod = repaymentMethod.trim().lowercase()
        val ratePerPeriod = roiPercent / 100.0 / 12.0
        val totalPayable: Double
        val emi: Double

        if (normalizedMethod.contains("principal") && normalizedMethod.contains("interest")) {
            val totalInterest = principal * (roiPercent / 100.0) * (periods / 12.0)
            totalPayable = principal + totalInterest
            emi = totalPayable / periods
        } else {
            totalPayable = if (ratePerPeriod == 0.0) {
                principal
            } else {
                principal * ratePerPeriod * (1 + ratePerPeriod).pow(periods) /
                    ((1 + ratePerPeriod).pow(periods) - 1)
                    * periods
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
        return try {
            LocalDate.parse(value, DateTimeFormatter.ISO_LOCAL_DATE)
            true
        } catch (_: DateTimeParseException) {
            false
        }
    }

    fun endDateOnOrAfterStart(startDate: String, endDate: String): Boolean {
        return try {
            !LocalDate.parse(endDate).isBefore(LocalDate.parse(startDate))
        } catch (_: DateTimeParseException) {
            false
        }
    }

    fun monthsBetween(startDate: String, endDate: String): Int {
        return try {
            ChronoUnit.MONTHS.between(LocalDate.parse(startDate), LocalDate.parse(endDate)).toInt()
        } catch (_: DateTimeParseException) {
            0
        }
    }
}
