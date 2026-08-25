package com.udhaardaar.mvp

import java.text.SimpleDateFormat
import java.util.Locale

/** Shared launch-grade rules used by profile, credit, document and consent flows. */
object V3Compliance {
    private val pan = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val gstin = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    private val email = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
    private val pin = Regex("^[1-9][0-9]{5}$")
    private val mobile = Regex("^[6-9][0-9]{9}$")
    private val aadhaar = Regex("^[0-9]{12}$")

    fun validMobile(v: String) = mobile.matches(v.trim())
    fun validAlternateMobile(v: String) = v.isBlank() || validMobile(v)
    fun validAadhaar(v: String) = v.isBlank() || aadhaar.matches(v.trim())
    fun validPin(v: String) = pin.matches(v.trim())
    fun validPan(v: String) = v.isBlank() || pan.matches(v.trim().uppercase(Locale.US))
    fun validGstin(v: String) = v.isBlank() || gstin.matches(v.trim().uppercase(Locale.US))
    fun validEmail(v: String) = v.isBlank() || email.matches(v.trim())
    fun validRoi(v: Double) = v.isFinite() && v >= 0.0 && v <= 100.0
    fun validAmount(v: Double) = v.isFinite() && v > 0.0
    fun validTenorMonths(v: Int) = v in 1..240

    fun agreementDate(v: String): Boolean = try {
        SimpleDateFormat("yyyy-MM-dd", Locale.US).apply { isLenient = false }.parse(v) != null
    } catch (_: Exception) { false }

    fun informalCreditScore(closed: Int, total: Int, onTime: Int, overdue: Int): Int {
        if (total <= 0) return 0
        val closure = (closed.toDouble() / total).coerceIn(0.0, 1.0)
        val timely = (onTime.toDouble() / total).coerceIn(0.0, 1.0)
        val penalty = (overdue.toDouble() / total).coerceIn(0.0, 1.0)
        return (300 + 500 * (0.45 * closure + 0.55 * timely - 0.50 * penalty)).toInt().coerceIn(300, 850)
    }
}

data class RentalLeaseTerms(
    val landlord: String,
    val tenant: String,
    val propertyAddress: String,
    val monthlyRent: Double,
    val securityDeposit: Double,
    val startDate: String,
    val endDate: String,
    val noticePeriodDays: Int,
    val escalationPercent: Double,
    val maintenanceBy: String,
    val utilitiesBy: String,
    val paymentFrequency: String,
    val otherTerms: String
)

object RentalLeaseAgreementBuilder {
    fun build(t: RentalLeaseTerms): String = """
        UDHAARDAAR DIGITAL RENT / LEASE AGREEMENT
        Generated from the terms entered by the parties.

        LANDLORD: ${t.landlord}
        TENANT: ${t.tenant}
        PROPERTY: ${t.propertyAddress}

        MONTHLY RENT: ₹${"%.2f".format(Locale.US, t.monthlyRent)}
        SECURITY DEPOSIT: ₹${"%.2f".format(Locale.US, t.securityDeposit)}
        TERM: ${t.startDate} to ${t.endDate}
        NOTICE PERIOD: ${t.noticePeriodDays} days
        RENT ESCALATION: ${t.escalationPercent}%
        MAINTENANCE: ${t.maintenanceBy}
        UTILITIES: ${t.utilitiesBy}
        PAYMENT FREQUENCY: ${t.paymentFrequency}

        OTHER AGREED TERMS:
        ${t.otherTerms.ifBlank { "None" }}

        The parties should review these terms before giving consent.
        This generated record is not represented as legal advice or as a substitute
        for execution/stamping/registration requirements applicable to the transaction.
    """.trimIndent()
}
