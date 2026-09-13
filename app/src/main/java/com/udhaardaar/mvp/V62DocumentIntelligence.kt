package com.udhaardaar.mvp

/**
 * Document intelligence contract used by Insurance, Asset Vault, Rental/Lease and Credit.
 * The implementation may start locally and later call a secure AI service without changing screens.
 */
object V62DocumentIntelligence {
    data class Request(
        val documentType: String,
        val originalUri: String,
        val requestedFields: List<String>,
        val criticalTermCategories: List<String>
    )

    data class Result(
        val documentId: String,
        val extracted: List<V62Extraction>,
        val overallConfidence: Double,
        val reviewRequired: Boolean,
        val summary: String = ""
    )

    val insuranceFields = listOf(
        "policyNumber", "insurer", "policyType", "policyholder", "insured",
        "premium", "premiumFrequency", "sumAssured", "startDate", "endDate",
        "maturityDate", "nextPremiumDue", "nominee", "status", "exclusions",
        "waitingPeriods", "claimConditions", "riders", "surrenderOrLoanTerms", "criticalTerms"
    )

    val assetFields = listOf(
        "owner", "coOwners", "propertyDescription", "address", "registrationNo",
        "area", "surveyOrPlotNo", "encumbrance", "mortgage", "bank", "branch",
        "accountHolder", "accountNumber", "ifsc", "accountType", "statementDate", "balance"
    )

    val leaseFields = listOf(
        "landlord", "tenant", "property", "rent", "securityDeposit", "startDate",
        "endDate", "renewal", "noticePeriod", "escalation", "maintenance",
        "utilities", "lockIn", "termination", "specialConditions"
    )

    val invoiceFields = listOf(
        "invoiceNumber", "invoiceDate", "supplier", "buyer", "gstin", "items",
        "taxableValue", "tax", "total", "dueDate", "paymentTerms", "purchaseOrder"
    )

    val criticalCategories = listOf(
        "EXPIRY_OR_DUE_DATE", "PAYMENT_OBLIGATION", "INTEREST_OR_CHARGES",
        "EXCLUSION", "WAITING_PERIOD", "CLAIM_CONDITION", "NOTICE_OR_TERMINATION",
        "LOCK_IN", "ESCALATION", "ENCUMBRANCE_OR_MORTGAGE", "NOMINEE",
        "OWNERSHIP", "LIABILITY", "PENALTY", "RENEWAL"
    )

    fun request(documentType: String, originalUri: String): Request {
        val fields = when (documentType) {
            "INSURANCE_POLICY" -> insuranceFields
            "PROPERTY_PAPER", "BANK_PASSBOOK" -> assetFields
            "LEASE_DEED", "RENT_AGREEMENT" -> leaseFields
            "INVOICE" -> invoiceFields
            else -> emptyList()
        }
        return Request(documentType, originalUri, fields, criticalCategories)
    }
}
