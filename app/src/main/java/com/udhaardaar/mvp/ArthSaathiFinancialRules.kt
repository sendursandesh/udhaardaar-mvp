package com.udhaardaar.mvp

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.math.pow

/** Pure financial rules shared by credit registration and regression tests. */
object ArthSaathiFinancialRules {
    data class RepaymentPlan(val emi: Double, val totalPayable: Double, val interest: Double, val instalments: Int)

    fun plan(principal: Double, annualRoiPercent: Double, months: Int, method: String): RepaymentPlan {
        require(principal > 0.0) { "Principal must be positive" }
        require(annualRoiPercent in 0.0..100.0) { "ROI must be between 0 and 100%" }
        require(months in 1..240) { "Repayment period must be 1–240 months" }
        val normalized = method.trim().lowercase(Locale.US)
        return when {
            normalized == "emi" && annualRoiPercent > 0.0 -> {
                val r = annualRoiPercent / 1200.0
                val factor = (1 + r).pow(months)
                val emi = principal * r * factor / (factor - 1)
                val total = emi * months
                RepaymentPlan(emi, total, total - principal, months)
            }
            normalized == "emi" -> RepaymentPlan(principal / months, principal, 0.0, months)
            normalized.contains("principal") && normalized.contains("interest") -> {
                val total = principal + principal * annualRoiPercent * months / 1200.0
                RepaymentPlan(total / months, total, total - principal, months)
            }
            else -> RepaymentPlan(principal, principal, 0.0, 1)
        }
    }

    fun endDate(start: String, months: Int): String {
        require(months in 1..240)
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        val parsed = sdf.parse(start) ?: throw IllegalArgumentException("Invalid start date")
        val cal = Calendar.getInstance().apply { time = parsed; add(Calendar.MONTH, months) }
        return sdf.format(cal.time)
    }

    fun validDate(value: String): Boolean = runCatching {
        val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }
        sdf.parse(value)?.let { it.time <= Date(Long.MAX_VALUE).time } == true
    }.getOrDefault(false)
}
