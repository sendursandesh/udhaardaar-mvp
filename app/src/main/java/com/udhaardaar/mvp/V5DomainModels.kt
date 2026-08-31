package com.udhaardaar.mvp

import java.time.Instant

/** V5 domain objects. Kept transport-friendly so a future API can replace local persistence. */
data class V5Profile(
    val id: String,
    val type: String,
    val name: String,
    val mobile: String,
    val pan: String? = null,
    val aadhaar: String? = null,
    val gstin: String? = null,
    val photoUri: String? = null,
    val city: String? = null,
    val state: String? = null,
    val pin: String? = null
)

data class V5Credit(
    val id: String,
    val profileId: String,
    val direction: String,
    val creditType: String,
    val principal: Double,
    val roiPercent: Double,
    val repaymentMethod: String,
    val startDate: String,
    val endDate: String,
    val consentState: String,
    val documentIds: List<String> = emptyList()
)

data class V5Rental(
    val id: String,
    val tenantProfileId: String,
    val property: String,
    val landlord: String,
    val monthlyRent: Double,
    val deposit: Double,
    val startDate: String,
    val endDate: String,
    val escalationPercent: Double = 0.0,
    val noticeDays: Int = 0,
    val documentId: String? = null
)

data class V5Document(
    val id: String,
    val type: String,
    val uri: String,
    val sha256: String? = null,
    val createdAt: Instant = Instant.now(),
    val version: Int = 1
)

data class V5Asset(
    val id: String,
    val ownerProfileId: String,
    val category: String,
    val title: String,
    val description: String,
    val estimatedValue: Double? = null,
    val proofDocumentIds: List<String> = emptyList(),
    val nomineeProfileId: String? = null
)

data class V5Claim(
    val id: String,
    val assetId: String,
    val claimantProfileId: String,
    val relationship: String,
    val status: String,
    val requiredDocumentIds: List<String> = emptyList(),
    val legalProfessionalId: String? = null
)

data class V5ChargeComparison(
    val sanctionDocumentId: String,
    val statementDocumentId: String,
    val sanctionedRoi: Double?,
    val actualRoi: Double?,
    val sanctionedFees: Double,
    val actualFees: Double,
    val variance: Double,
    val findings: List<String>
)

object V5Validation {
    private val pan = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val aadhaar = Regex("^[2-9][0-9]{11}$")
    private val gstin = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    private val mobile = Regex("^[6-9][0-9]{9}$")
    private val pin = Regex("^[1-9][0-9]{5}$")

    fun pan(value: String) = value.trim().uppercase().let { it.isEmpty() || pan.matches(it) }
    fun aadhaar(value: String) = value.isEmpty() || aadhaar.matches(value)
    fun gstin(value: String) = value.trim().uppercase().let { it.isEmpty() || gstin.matches(it) }
    fun mobile(value: String) = mobile.matches(value.trim())
    fun pin(value: String) = pin.matches(value.trim())
}
