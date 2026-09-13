package com.udhaardaar.mvp

/** V6.2 connected financial architecture contract. */
object V62ArchitectureSpec {
    const val VERSION = "6.2"
    val modules = listOf("Home", "Credit", "Repayment", "Rental & Lease", "TTMM", "Asset Vault", "Insurance", "MIS", "Legacy", "Legal", "AI Advisor")
    val roles = listOf("LENDER", "BORROWER", "SUPPLIER", "BUYER", "SELLER", "LANDLORD", "TENANT", "GUARANTOR")
    val relationshipTypes = listOf("PERSONAL_CREDIT", "TRADE_CREDIT", "RENTAL", "LEASE", "GUARANTEE")
    val documentTypes = listOf("INVOICE", "PROMISSORY_NOTE", "LEASE_DEED", "RENT_AGREEMENT", "INSURANCE_POLICY", "PROPERTY_PAPER", "BANK_PASSBOOK", "OTHER")
    val consentEvents = listOf("HISTORY_SHARING", "CREDIT_REGISTRATION", "DOCUMENT_CONFIRMATION")
    val repaymentModes = listOf("EMI", "PRINCIPAL_PLUS_INTEREST", "BULLET")
    val bulletInterestPeriods = listOf("NONE", "MONTHLY", "QUARTERLY", "HALF_YEARLY", "YEARLY")
    val aiExtractionFields = listOf("field", "value", "confidence", "sourcePage", "sourceText", "userConfirmed", "userEdited")
    val misMetrics = listOf("assetAllocation", "returns", "risk", "idleFunds", "averageInvestment", "investmentPeriod", "charges", "interestReceived", "opportunityCost", "appSavings")
    val eventFlow = listOf("RELATIONSHIP_CHANGED", "REPAYMENT_CHANGED", "DOCUMENT_ADDED", "ASSET_CHANGED", "POLICY_CHANGED", "TTMM_EXPENSE_CHANGED", "CONSENT_CHANGED")
}
