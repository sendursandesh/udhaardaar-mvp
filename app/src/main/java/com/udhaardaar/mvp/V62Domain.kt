package com.udhaardaar.mvp

import java.util.UUID

data class V62Counterparty(
    val id: String = UUID.randomUUID().toString(),
    val displayName: String,
    val mobile: String = "",
    val pan: String = "",
    val aadhaarLast4: String = "",
    val gstin: String = ""
)

data class V62Relationship(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val ownerUserId: String,
    val counterpartyId: String,
    val ownerRole: String,
    val counterpartyRole: String,
    val principal: Double = 0.0,
    val roiPercent: Double = 0.0,
    val repaymentMode: String = "EMI",
    val periodicity: String = "MONTHLY",
    val startDate: String = "",
    val endDate: String = "",
    val finalPaymentDate: String = "",
    val bulletInterestPeriod: String = "NONE",
    val status: String = "DRAFT"
)

data class V62Consent(
    val id: String = UUID.randomUUID().toString(),
    val relationshipId: String,
    val counterpartyId: String,
    val eventType: String,
    val status: String = "PENDING",
    val otpReference: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val verifiedAt: Long? = null
)

data class V62Document(
    val id: String = UUID.randomUUID().toString(),
    val type: String,
    val originalUri: String,
    val sha256: String = "",
    val createdAt: Long = System.currentTimeMillis()
)

data class V62Extraction(
    val documentId: String,
    val field: String,
    val value: String,
    val confidence: Double,
    val sourcePage: Int? = null,
    val sourceText: String = "",
    val critical: Boolean = false,
    val criticalReason: String = "",
    val userConfirmed: Boolean = false,
    val userEdited: Boolean = false
)

data class V62Asset(
    val id: String = UUID.randomUUID().toString(),
    val ownerUserId: String,
    val type: String,
    val description: String = "",
    val value: Double = 0.0,
    val accountOrRegistrationNo: String = "",
    val institution: String = "",
    val nomineeId: String = "",
    val sourceDocumentId: String = ""
)

data class V62InsurancePolicy(
    val id: String = UUID.randomUUID().toString(),
    val ownerUserId: String,
    val policyNumber: String = "",
    val insurer: String = "",
    val policyType: String = "",
    val policyholder: String = "",
    val insured: String = "",
    val premium: Double = 0.0,
    val premiumFrequency: String = "",
    val sumAssured: Double = 0.0,
    val startDate: String = "",
    val endDate: String = "",
    val nextPremiumDue: String = "",
    val nominee: String = "",
    val exclusions: String = "",
    val waitingPeriods: String = "",
    val riders: String = "",
    val criticalTerms: String = "",
    val sourceDocumentId: String = ""
)

data class V62TtmmExpense(
    val id: String = UUID.randomUUID().toString(),
    val groupId: String,
    val description: String,
    val amount: Double,
    val payerId: String,
    val splitMode: String,
    val splitData: String,
    val createdAt: Long = System.currentTimeMillis()
)

// Canonical V6.2 event model lives in V62Event.kt so every module shares one contract.
