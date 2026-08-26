package com.udhaardaar.mvp

import java.time.LocalDate
import java.time.format.DateTimeFormatter

object CreditRules {
    data class ScheduleLine(val date: LocalDate, val amount: Double, val principal: Double, val interest: Double)

    fun emi(principal: Double, annualRate: Double, months: Int): Double {
        if (principal <= 0 || months <= 0) return 0.0
        val monthly = annualRate.coerceAtLeast(0.0) / 1200.0
        if (monthly == 0.0) return principal / months
        val factor = Math.pow(1.0 + monthly, months.toDouble())
        return principal * monthly * factor / (factor - 1.0)
    }

    fun simpleInterest(principal: Double, annualRate: Double, years: Double): Double =
        principal.coerceAtLeast(0.0) * annualRate.coerceAtLeast(0.0) / 100.0 * years.coerceAtLeast(0.0)

    fun remainingPrincipal(principal: Double, repayments: Double): Double =
        (principal - repayments).coerceAtLeast(0.0)

    fun chronological(lines: List<ScheduleLine>): List<ScheduleLine> = lines.sortedBy { it.date }

    fun date(value: String): LocalDate? = try {
        LocalDate.parse(value, DateTimeFormatter.ofPattern("dd MMM yyyy"))
    } catch (_: Exception) { null }
}
