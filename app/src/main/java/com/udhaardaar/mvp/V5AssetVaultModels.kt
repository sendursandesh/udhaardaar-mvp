package com.udhaardaar.mvp

/** Asset-vault and succession/claim contracts for the V5 infrastructure. */
object V5AssetVaultModels {
    enum class AssetClass { FINANCIAL, NON_FINANCIAL }
    enum class ClaimStatus { IDENTIFIED, DOCUMENTS_PENDING, PREPARED, SUBMITTED, QUERY_RECEIVED, APPROVED, TRANSFERRED, DISPUTED, CLOSED }

    data class FinancialAsset(
        val id: String,
        val ownerId: String,
        val category: String,
        val institution: String,
        val maskedReference: String,
        val value: Double?,
        val maturityDate: String? = null,
        val nomineeId: String? = null,
        val documentIds: List<String> = emptyList()
    )

    data class NonFinancialAsset(
        val id: String,
        val ownerId: String,
        val category: String,
        val title: String,
        val location: String?,
        val ownershipReference: String?,
        val value: Double?,
        val nomineeId: String? = null,
        val documentIds: List<String> = emptyList()
    )

    data class TrustedPerson(
        val id: String,
        val ownerId: String,
        val personProfileId: String,
        val permissions: Set<String>
    )

    data class SuccessionEvent(
        val id: String,
        val ownerId: String,
        val date: String,
        val deathCertificateDocumentId: String?,
        val createdBy: String
    )

    data class AssetClaim(
        val id: String,
        val assetId: String,
        val claimantId: String,
        val relationship: String,
        val status: ClaimStatus,
        val institution: String,
        val requiredDocuments: List<String>,
        val suppliedDocuments: List<String>,
        val nextAction: String?
    )

    val financialCategories = listOf("Bank / Deposit", "Insurance", "Shares / Securities", "Mutual Fund", "Bond", "Pension / Retirement", "Receivable / Loan", "Other")
    val nonFinancialCategories = listOf("Land / Property", "House / Building", "Vehicle", "Jewellery / Valuables", "Business Interest", "Intellectual Property", "Other")
    val trustedPermissions = listOf("EMERGENCY_DISCOVERY", "VIEW_ASSETS", "VIEW_DOCUMENTS", "PREPARE_CLAIM")
}
