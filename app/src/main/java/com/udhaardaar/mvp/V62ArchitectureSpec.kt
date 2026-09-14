package com.udhaardaar.mvp

/** ArthSaathi V6.2 MASTER product/domain contract. */
object V62ArchitectureSpec {
    const val VERSION = "6.2"
    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Navigate Your Financial Journey"
    const val PILLARS = "Plan • Protect • Grow • Nominate"

    val modules = listOf(
        "Home & Financial Dashboard", "Profile & Identity", "Family", "Contacts & People", "Address & Geolocation",
        "Informal Credit / Udhaar", "QR Udhaar Khata", "Informal Credit Intelligence / Score", "Trade Credit", "Accounting & ERP Integration",
        "Formal Credit", "Funding & Lending Marketplace", "Repayment Centre", "Guarantor", "ChargeCheck", "Asset Vault", "Liability Vault",
        "Insurance & Protection", "Government Schemes & Benefits", "Rental & Lease", "TTMM Shared Expenses", "Document Vault & Intelligence",
        "MIS & Financial Analytics", "Reports & Statements", "Inheritance & Claim Assistance", "Will / Nomination / Legacy", "Legal Assistance",
        "AI Financial Advisor", "Alerts & Notifications", "Consent / OTP / Digital Confirmation", "Security & Account Management", "Integration / API Platform"
    )
    val roles = listOf("USER", "LENDER", "BORROWER", "SUPPLIER", "BUYER", "SELLER", "LANDLORD", "TENANT", "GUARANTOR", "FORMAL_LENDER", "FUNDING_PROVIDER")
    val relationshipTypes = listOf("PERSONAL_CREDIT", "TRADE_CREDIT", "RENTAL", "LEASE", "GUARANTEE", "FORMAL_LOAN", "FUNDING_REQUEST", "GROUP_EXPENSE")
    val documentTypes = listOf("INVOICE", "PROMISSORY_NOTE", "SANCTION_LETTER", "ACCOUNT_STATEMENT", "LEASE_DEED", "RENT_AGREEMENT", "INSURANCE_POLICY", "PROPERTY_PAPER", "BANK_PASSBOOK", "LOAN_DOCUMENT", "CHARGE_EVIDENCE", "WILL_DRAFT", "OTHER")
    val consentEvents = listOf("HISTORY_SHARING", "CREDIT_REGISTRATION", "DOCUMENT_CONFIRMATION", "REPAYMENT_CONFIRMATION", "FUNDING_PROFILE_SHARING", "ERP_DATA_SHARING", "CLAIM_DATA_SHARING", "LEGAL_HANDOFF", "QR_KHATA_CONFIRMATION")
    val repaymentModes = listOf("EMI", "PRINCIPAL_PLUS_INTEREST", "BULLET")
    val bulletInterestPeriods = listOf("NONE", "MONTHLY", "QUARTERLY", "HALF_YEARLY", "YEARLY")
    val qrFlows = listOf("SCAN_QR", "IDENTIFY_COUNTERPARTY", "CREATE_RELATIONSHIP_DRAFT", "REQUEST_CONSENT", "RECORD_KHATA_ENTRY", "LINK_REPAYMENT", "SHOW_OPEN_BALANCE", "REQUEST_FUNDING")
    val accountingAdapters = listOf("TALLY", "SAP", "GENERIC_REST_API", "CSV_IMPORT", "WEBHOOK")
    val tradeCreditFields = listOf("customerOrSupplierId", "gstin", "pan", "invoiceNumber", "invoiceDate", "invoiceAmount", "creditPeriod", "dueDate", "outstandingAmount", "overdueAmount", "creditLimit", "paymentHistory", "debitNotes", "creditNotes", "sourceSystem", "sourceRecordId")
    val formalCreditFields = listOf("lender", "product", "principal", "interestRate", "tenure", "emi", "processingFee", "documentationFee", "insuranceCharge", "prepaymentCharge", "lateFee", "taxes", "totalCost", "effectiveCost", "security", "eligibility")
    val chargeCheckFields = listOf("sanctionedInterest", "actualInterest", "processingFee", "documentationFee", "insuranceCharge", "taxes", "penalties", "otherCharges", "refunds", "variance", "evidenceDocument", "claimStatus")
    val fundingFlow = listOf("FUNDING_REQUEST", "USER_CONSENT", "PROFILE_SHARE", "ELIGIBILITY_CHECK", "INTERESTED_PROVIDER_DISCOVERY", "OFFER_RECEIVED", "USER_ACCEPTANCE", "FORMAL_RELATIONSHIP_CREATION")
    val aiExtractionFields = listOf("field", "value", "confidence", "sourcePage", "sourceText", "critical", "criticalReason", "userConfirmed", "userEdited")
    val misMetrics = listOf("assetAllocation", "returns", "risk", "idleFunds", "averageInvestment", "investmentPeriod", "charges", "interestReceived", "opportunityCost", "appSavings", "opportunityCostSaved", "formalCreditCost", "informalCreditExposure")
    val eventFlow = listOf("PROFILE_CHANGED", "FAMILY_CHANGED", "ADDRESS_CHANGED", "RELATIONSHIP_CHANGED", "REPAYMENT_CHANGED", "DOCUMENT_ADDED", "ASSET_CHANGED", "LIABILITY_CHANGED", "POLICY_CHANGED", "TTMM_EXPENSE_CHANGED", "SAVINGS_CHANGED", "CONSENT_CHANGED", "NOMINEE_CHANGED", "CLAIM_CHANGED", "WILL_CHANGED", "TRADE_CREDIT_IMPORTED", "FORMAL_LOAN_CHANGED", "CHARGECHECK_CHANGED", "FUNDING_REQUEST_CHANGED", "ALERT_CREATED")
    val endpoints = mapOf("profile" to "profile/create-or-select", "family" to "family/member/upsert", "address" to "profile/address/resolve", "history" to "relationship/history-with-consent", "credit" to "relationship/credit/register-with-consent", "qrKhata" to "qr-khata/entry", "tradeCredit" to "trade-credit/register-or-import", "accounting" to "integrations/accounting/sync", "formalCredit" to "formal-credit/compare-and-apply", "funding" to "funding/request-and-discover", "repayment" to "relationship/repayment/record", "chargeCheck" to "chargecheck/analyse", "rental" to "relationship/rental/register", "ttmm" to "ttmm/expense-and-settlement", "asset" to "vault/asset/upsert", "liability" to "vault/liability/upsert", "insurance" to "vault/insurance/upsert", "document" to "documents/store-index-and-analyse", "mis" to "mis/refresh-from-events", "legacy" to "legacy/will-nominee-claim", "legal" to "legal/assistance", "ai" to "ai/advisor-and-document-intelligence", "alerts" to "alerts/evaluate")
    val nonNegotiableRules = listOf(
        "A counterparty is created only inside a real relationship/transaction context.", "Protected financial history is hidden until explicit OTP consent.",
        "Credit registration requires a separate OTP consent event after final terms are shown.", "QR scanning identifies a relationship entry point; it never grants unrestricted financial-history access.",
        "Funding profile sharing is always user-initiated and consent-based.", "Trade-credit imports are traceable to source system and source record; imports are not silently editable without an audit trail.",
        "Accounting integration is adapter-based so SAP, Tally and future systems use the same domain contract.", "AI extraction is a proposal: originals are retained and every field is confirmable/editable.",
        "Critical document terms must be filterable and traceable to page/source text.", "MIS and AI consume the same source-of-truth records and domain events.",
        "Location is input assistance, not authority; the user can edit/override a suggested address.", "Language preference is explicit and persistent; modules never switch language automatically.",
        "No V5/V4/V3 activity is a valid V6.2 navigation target.", "No module may perform an irreversible financial action automatically from an AI suggestion.",
        "Legal and investment suggestions are clearly labelled as assistance/information where appropriate."
    )
}
