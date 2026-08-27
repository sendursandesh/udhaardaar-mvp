package com.udhaardaar.mvp

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow

data class RepaymentPlan(val instalment: Double, val totalInterest: Double, val periods: Int, val periodicRate: Double)

object CreditRules {
    data class ScheduleLine(val date: Date, val amount: Double, val principal: Double, val interest: Double)

    fun periodsPerYear(periodicity: String): Int = when (periodicity.trim().uppercase(Locale.ROOT)) {
        "WEEKLY" -> 52
        "FORTNIGHTLY" -> 26
        "QUARTERLY" -> 4
        "HALF-YEARLY", "HALFYEARLY" -> 2
        "YEARLY", "ANNUAL" -> 1
        "ONE-TIME", "ONETIME", "BULLET", "MATURITY" -> 1
        else -> 12
    }

    fun periodsBetween(start: Date, end: Date, periodicity: String): Int {
        if (end.before(start)) return 0
        val cal = Calendar.getInstance().apply { time = start }
        var count = 0
        val step = when (periodicity.trim().uppercase(Locale.ROOT)) {
            "WEEKLY" -> Calendar.WEEK_OF_YEAR to 1
            "FORTNIGHTLY" -> Calendar.WEEK_OF_YEAR to 2
            "QUARTERLY" -> Calendar.MONTH to 3
            "HALF-YEARLY", "HALFYEARLY" -> Calendar.MONTH to 6
            "YEARLY", "ANNUAL" -> Calendar.YEAR to 1
            else -> Calendar.MONTH to 1
        }
        while (cal.time.before(end) && count < 1000) { cal.add(step.first, step.second); count++ }
        return if (cal.time == end) count else count.coerceAtLeast(1)
    }

    fun emi(principal: Double, annualRate: Double, periods: Int, periodicity: String = "MONTHLY"): Double {
        if (principal <= 0 || periods <= 0) return 0.0
        val rate = annualRate.coerceAtLeast(0.0) / 100.0 / periodsPerYear(periodicity)
        if (rate == 0.0) return principal / periods
        val factor = (1.0 + rate).pow(periods.toDouble())
        return principal * rate * factor / (factor - 1.0)
    }

    fun buildPlan(principal: Double, annualRate: Double, periodicity: String, start: Date, end: Date): RepaymentPlan {
        val n = periodsBetween(start, end, periodicity).coerceAtLeast(1)
        val payment = emi(principal, annualRate, n, periodicity)
        return RepaymentPlan(payment, (payment * n - principal).coerceAtLeast(0.0), n, annualRate / periodsPerYear(periodicity))
    }

    fun simpleInterest(principal: Double, annualRate: Double, years: Double): Double =
        principal.coerceAtLeast(0.0) * annualRate.coerceAtLeast(0.0) / 100.0 * years.coerceAtLeast(0.0)

    fun remainingPrincipal(principal: Double, repayments: Double): Double = (principal - repayments).coerceAtLeast(0.0)
    fun chronological(lines: List<ScheduleLine>): List<ScheduleLine> = lines.sortedBy { it.date }
    fun parseDate(value: String): Date? = try { SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).parse(value) } catch (_: Exception) { null }
}
