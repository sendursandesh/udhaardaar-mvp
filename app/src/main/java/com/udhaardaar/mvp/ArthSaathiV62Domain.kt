package com.udhaardaar.mvp

/** Shared domain contracts for every ArthSaathi V6.2 module. */
object ArthSaathiV62Domain {
    enum class Module {
        HOME, PROFILE, FAMILY, CONTACTS, ADDRESS, INFORMAL_CREDIT, QR_KHATA,
        CREDIT_SCORE, TRADE_CREDIT, ACCOUNTING_INTEGRATION, FORMAL_CREDIT,
        FUNDING_MARKETPLACE, REPAYMENT, GUARANTOR, CHARGECHECK, ASSET_VAULT,
        LIABILITY_VAULT, INSURANCE, GOVERNMENT_BENEFITS, RENTAL_LEASE, TTMM,
        DOCUMENT_VAULT, MIS, REPORTS, CLAIM_ASSISTANCE, WILL_LEGACY, LEGAL,
        AI_ADVISOR, ALERTS, CONSENT, SECURITY, INTEGRATIONS
    }

    enum class RelationshipType { PERSONAL_CREDIT, TRADE_CREDIT, RENTAL, LEASE, GUARANTEE, FORMAL_LOAN, FUNDING_REQUEST }
    enum class CreditMode { EMI, PRINCIPAL_PLUS_INTEREST, BULLET }
    enum class SplitMode { EQUAL, CUSTOM, PERCENTAGE, SHARES }

    data class PersonRef(
        val id: String,
        val displayName: String,
        val mobile: String? = null,
        val pan: String? = null,
        val gstin: String? = null,
        val role: String? = null
    )

    data class Address(
        val line1: String = "",
        val line2: String = "",
        val locality: String = "",
        val city: String = "",
        val district: String = "",
        val state: String = "",
        val pinCode: String = "",
        val latitude: Double? = null,
        val longitude: Double? = null,
        val source: String = "MANUAL",
        val userConfirmed: Boolean = false
    )

    data class CreditTerms(
        val principal: Double,
        val annualRatePercent: Double,
        val mode: CreditMode,
        val periodicity: String,
        val startDate: String,
        val endDate: String,
        val emi: Double = 0.0,
        val guarantorId: String? = null
    )

    data class Relationship(
        val id: String,
        val ownerId: String,
        val counterparty: PersonRef,
        val type: RelationshipType,
        val terms: CreditTerms? = null,
        val outstanding: Double = 0.0,
        val status: String = "DRAFT",
        val createdAt: Long = System.currentTimeMillis()
    )

    data class TradeCreditRecord(
        val relationshipId: String,
        val invoiceNumber: String,
        val invoiceDate: String,
        val invoiceAmount: Double,
        val creditPeriodDays: Int,
        val dueDate: String,
        val outstandingAmount: Double,
        val overdueAmount: Double = 0.0,
        val creditLimit: Double? = null,
        val gstin: String? = null,
        val pan: String? = null,
        val sourceSystem: String,
        val sourceRecordId: String,
        val importedAt: Long = System.currentTimeMillis()
    )

    data class FormalCreditOffer(
        val providerId: String,
        val providerName: String,
        val productName: String,
        val principal: Double,
        val annualRatePercent: Double,
        val tenureMonths: Int,
        val emi: Double,
        val processingFee: Double,
        val documentationFee: Double,
        val insuranceCharge: Double,
        val prepaymentCharge: Double,
        val lateFee: Double,
        val taxes: Double,
        val totalCost: Double,
        val effectiveCostPercent: Double
    )

    data class ChargeCheckResult(
        val accountOrLoanId: String,
        val sanctionedInterest: Double,
        val actualInterest: Double,
        val processingFee: Double,
        val documentationFee: Double,
        val insuranceCharge: Double,
        val taxes: Double,
        val penalties: Double,
        val otherCharges: Double,
        val refunds: Double,
        val variance: Double,
        val evidenceDocumentId: String? = null,
        val status: String = "REVIEW_REQUIRED"
    )

    data class FundingRequest(
        val id: String,
        val requesterId: String,
        val purpose: String,
        val amount: Double,
        val tenureMonths: Int?,
        val consentGranted: Boolean = false,
        val status: String = "DRAFT"
    )

    data class KhataEntry(
        val id: String,
        val relationshipId: String,
        val amount: Double,
        val direction: String,
        val description: String,
        val entryDate: String,
        val evidenceDocumentId: String? = null,
        val confirmed: Boolean = false
    )

    data class DocumentEvidence(
        val id: String,
        val type: String,
        val originalUri: String,
        val extractedText: String = "",
        val sourceSystem: String? = null,
        val sourceRecordId: String? = null,
        val userVerified: Boolean = false
    )

    data class Event(val type: String, val entityId: String, val timestamp: Long = System.currentTimeMillis(), val metadata: Map<String, String> = emptyMap())

    data class ConsentRecord(
        val id: String,
        val subjectId: String,
        val eventType: String,
        val granted: Boolean,
        val otpVerified: Boolean,
        val timestamp: Long = System.currentTimeMillis()
    )

    /** One registry prevents a feature from disappearing into an isolated screen. */
    val moduleRegistry: Map<Module, String> = Module.values().associateWith { it.name }
}
