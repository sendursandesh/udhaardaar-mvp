package com.udhaardaar.mvp

/**
 * ArthSaathi V6.2 architecture contract.
 * UI code should create/update domain records through the repository layer;
 * downstream modules consume domain events rather than copying data manually.
 */
object V62ArchitectureSpec {
    const val VERSION = "6.2"
    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Navigate Your Financial Journey"

    val journey = listOf("PLAN", "PROTECT", "GROW", "NOMINATE")
    val modules = listOf("Home", "Credit", "Repayment", "Rental & Lease", "TTMM", "Asset Vault", "Insurance", "MIS", "Legacy", "Legal", "AI Advisor")
    val roles = listOf("LENDER", "BORROWER", "SUPPLIER", "BUYER", "SELLER", "LANDLORD", "TENANT", "GUARANTOR")
    val relationshipTypes = listOf("PERSONAL_CREDIT", "TRADE_CREDIT", "RENTAL", "LEASE", "GUARANTEE")
    val documentTypes = listOf("INVOICE", "PROMISSORY_NOTE", "LEASE_DEED", "RENT_AGREEMENT", "INSURANCE_POLICY", "PROPERTY_PAPER", "BANK_PASSBOOK", "OTHER")
    val consentEvents = listOf("HISTORY_SHARING", "CREDIT_REGISTRATION", "DOCUMENT_CONFIRMATION")
    val repaymentModes = listOf("EMI", "PRINCIPAL_PLUS_INTEREST", "BULLET")
    val bulletInterestPeriods = listOf("NONE", "MONTHLY", "QUARTERLY", "HALF_YEARLY", "YEARLY")

    val aiExtractionFields = listOf("field", "value", "confidence", "sourcePage", "sourceText", "critical", "criticalReason", "userConfirmed", "userEdited")
    val misMetrics = listOf("assetAllocation", "returns", "risk", "idleFunds", "averageInvestment", "investmentPeriod", "charges", "interestReceived", "opportunityCost", "appSavings")
    val eventFlow = listOf("RELATIONSHIP_CHANGED", "REPAYMENT_CHANGED", "DOCUMENT_ADDED", "ASSET_CHANGED", "POLICY_CHANGED", "TTMM_EXPENSE_CHANGED", "CONSENT_CHANGED", "NOMINEE_CHANGED", "CLAIM_CHANGED", "WILL_CHANGED")

    /** Logical endpoints. These become API routes later without changing UI contracts. */
    val endpoints = mapOf(
        "profile" to "counterparty/create-or-select",
        "history" to "relationship/history-with-consent",
        "credit" to "relationship/credit/register-with-consent",
        "repayment" to "relationship/repayment/record",
        "rental" to "relationship/rental/register",
        "ttmm" to "ttmm/expense-and-settlement",
        "asset" to "vault/asset/upsert",
        "insurance" to "vault/insurance/upsert",
        "document" to "documents/store-and-index",
        "mis" to "mis/refresh-from-events",
        "legacy" to "legacy/will-nominee-claim",
        "legal" to "legal/assistance",
        "ai" to "ai/advisor-and-document-intelligence"
    )

    val nonNegotiableRules = listOf(
        "A counterparty is created only inside a real relationship/transaction context.",
        "Repayment history is hidden until the counterparty gives explicit OTP consent.",
        "Credit registration requires a separate OTP consent event after final terms are shown.",
        "AI extraction is a proposal: original documents are retained and every field is confirmable/editable.",
        "Critical document terms must be filterable and traceable to page/source text.",
        "A single source record feeds Home, Repayment, MIS, Alerts, Legacy and AI insights.",
        "Language preference is explicit and persistent; modules never switch language automatically.",
        "V5/V4/V3 activities are not valid V6.2 navigation targets."
    )
}
