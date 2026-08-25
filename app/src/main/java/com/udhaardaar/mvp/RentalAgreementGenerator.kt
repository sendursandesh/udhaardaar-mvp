package com.udhaardaar.mvp

/**
 * Generates a readable rental/lease agreement from structured transaction data.
 * This is a document-generation layer; production legal review/e-sign integration
 * must be added before representing the document as a legally executed agreement.
 */
object RentalAgreementGenerator {
    data class Terms(
        val landlordName: String,
        val tenantName: String,
        val propertyAddress: String,
        val monthlyRent: String,
        val securityDeposit: String,
        val startDate: String,
        val endDate: String,
        val noticePeriod: String,
        val escalation: String,
        val maintenance: String,
        val utilities: String,
        val paymentFrequency: String,
        val latePaymentTerms: String,
        val additionalTerms: String
    )

    fun generate(t: Terms): String = buildString {
        appendLine("RENTAL / LEASE AGREEMENT")
        appendLine()
        appendLine("Landlord: ${t.landlordName}")
        appendLine("Tenant: ${t.tenantName}")
        appendLine("Property: ${t.propertyAddress}")
        appendLine()
        appendLine("COMMERCIAL TERMS")
        appendLine("Monthly rent: ${t.monthlyRent}")
        appendLine("Security deposit: ${t.securityDeposit}")
        appendLine("Tenancy period: ${t.startDate} to ${t.endDate}")
        appendLine("Notice period: ${t.noticePeriod}")
        appendLine("Rent escalation: ${t.escalation}")
        appendLine("Maintenance: ${t.maintenance}")
        appendLine("Utilities: ${t.utilities}")
        appendLine("Payment frequency: ${t.paymentFrequency}")
        appendLine("Late-payment terms: ${t.latePaymentTerms}")
        appendLine()
        appendLine("ADDITIONAL TERMS")
        appendLine(t.additionalTerms.ifBlank { "None" })
        appendLine()
        appendLine("DIGITAL RECORD")
        appendLine("This document is generated from data entered by the parties in Udhaardaar. Parties must review all terms and provide the required consent/verification before the record is treated as confirmed.")
    }
}
