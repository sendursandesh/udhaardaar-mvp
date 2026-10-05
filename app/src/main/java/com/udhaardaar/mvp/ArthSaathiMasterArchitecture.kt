package com.udhaardaar.mvp

/**
 * ArthSaathi consolidated master architecture — 5 historical architecture layers
 * reconciled into one product contract.
 *
 * Rules:
 * 1. Register Credit creates a new relationship; Loans & Udhaar owns existing accounts.
 * 2. Shared engines have one owner: repayment, consent, documents, people, MIS and audit.
 * 3. A feature may appear as a shortcut, but its canonical destination is unique.
 * 4. Legacy V6/V7 implementations may provide engines, but user navigation follows this contract.
 */
object ArthSaathiMasterArchitecture {
    enum class Area {
        HOME_COMMAND_CENTRE,
        RECORD,
        CREDIT,
        ASSETS,
        GROW,
        PROTECT,
        CLAIM_LEGACY,
        LEGAL,
        PEOPLE,
        DOCUMENT_INTELLIGENCE,
        AI_ADVISOR,
        REVENUE_PAYMENT,
        PLATFORM_SECURITY
    }

    data class Module(
        val id: String,
        val area: Area,
        val title: String,
        val description: String,
        val route: String,
        val canonical: Boolean = true
    )

    val modules: List<Module> = listOf(
        Module("HOME", Area.HOME_COMMAND_CENTRE, "Home / Command Centre", "Financial snapshot, alerts, actions and intelligence", "HOME"),
        Module("REGISTER_CREDIT", Area.RECORD, "Register Credit", "Create a new credit relationship by nature of credit", "REGISTER_CREDIT"),
        Module("LOANS_UDHAAR", Area.CREDIT, "Loans & Udhaar", "Existing active and closed credit accounts", "CREDIT"),
        Module("QR_KHATA", Area.RECORD, "QR Khata / Trade Credit", "Scan, record and track merchant or trade credit", "QR_KHATA"),
        Module("GROUP_KHATA", Area.RECORD, "Group Khata / Group Expenses", "Shared expenses, contributions, balances and settlement", "GROUP_KHATA"),
        Module("REPAYMENT", Area.CREDIT, "Repayment Centre", "Schedules, EMI, principal-plus-interest, payments and closure", "REPAYMENT"),
        Module("PEOPLE", Area.PEOPLE, "People & Relationships", "Profiles, family, borrowers, lenders and guarantors", "PEOPLE"),
        Module("ADDRESS", Area.PEOPLE, "Address & Location", "PIN/Maps/GPS-assisted address capture and confirmation", "PEOPLE", false),
        Module("ASSET_VAULT", Area.ASSETS, "Asset Vault", "Financial and non-financial assets, nominees and evidence", "ASSETS"),
        Module("LIABILITY_VAULT", Area.ASSETS, "Liability Vault", "Loans, obligations and outstanding liabilities", "LIABILITIES"),
        Module("PORTFOLIO", Area.GROW, "Portfolio & Investments", "Holdings, value, return and portfolio constitution", "PORTFOLIO"),
        Module("MIS", Area.DOCUMENT_INTELLIGENCE, "MIS / Money Report", "First-hand portfolio, asset, liability and value-generated information", "MIS"),
        Module("SWITCH_ANALYSIS", Area.GROW, "Portfolio Switch Analysis", "Return, opportunity cost and risk-based comparison", "OPPORTUNITY"),
        Module("SCENARIO", Area.GROW, "What-If / Scenario", "Future financial scenarios", "SCENARIO"),
        Module("MARKET", Area.GROW, "Market Values", "Recorded market/reference values", "MARKET"),
        Module("PROTECTION", Area.PROTECT, "Insurance & Protection", "Policies, renewals, claims and protection records", "PROTECT"),
        Module("BENEFITS", Area.PROTECT, "Benefits & Refunds", "Benefits/refunds completed and value generated", "BENEFITS"),
        Module("CHARGECHECK", Area.PROTECT, "ChargeCheck", "Compare sanctioned/promised charges with actual charges", "CHARGECHECK"),
        Module("CLAIMS", Area.CLAIM_LEGACY, "Claim Assistance", "Claim preparation, evidence and tracking", "CLAIM"),
        Module("WILL_LEGACY", Area.CLAIM_LEGACY, "Will / Inheritance / Legacy", "Nomination, inheritance and legacy records", "WILL"),
        Module("LEGAL", Area.LEGAL, "Legal Help", "Legal assistance and case/document support", "LEGAL"),
        Module("ADVOCATES", Area.LEGAL, "Advocate Directory", "Search advocates by city and practice area", "ADVOCATE"),
        Module("DOCUMENT_VAULT", Area.DOCUMENT_INTELLIGENCE, "Document Vault", "Digital agreements, evidence, OCR and downloadable records", "DOCUMENTS"),
        Module("AI_ADVISOR", Area.AI_ADVISOR, "ArthSaathi AI Advisor", "Explain, compare and suggest using recorded information", "AI"),
        Module("REVENUE", Area.REVENUE_PAYMENT, "Revenue & Payments", "User charges, invoices, gateway configuration and reconciliation", "REVENUE"),
        Module("SECURITY_CONSENT", Area.PLATFORM_SECURITY, "Security & Consent", "OTP, authorization, privacy and audit controls", "SECURITY"),
        Module("INTEGRATIONS", Area.PLATFORM_SECURITY, "Integrations", "Accounting/ERP/export boundaries and future connectors", "INTEGRATION")
    )

    val creditNatureOptions = listOf(
        "Personal Loan / Hand Loan",
        "Trade Credit / Udhaar",
        "Rental / Lease",
        "Formal Loan / Bank Credit",
        "Other Receivable / Payable"
    )

    val registerCreditFlow = listOf(
        "Identify/search borrower, lender or business",
        "Select nature of credit",
        "Enter credit-specific terms",
        "Set lending/payment method",
        "Calculate repayment terms",
        "Add guarantor where applicable",
        "Create digital document / evidence",
        "Obtain required consent and OTP confirmation",
        "Register the new credit account",
        "Open the registered account in Loans & Udhaar"
    )

    val accountDetailFlow = listOf(
        "Account identity and parties",
        "Original terms and documents",
        "Repayment schedule",
        "Payments and transaction history",
        "Outstanding and overdue",
        "Consent/audit history",
        "Guarantor and linked evidence",
        "Closure / settlement status"
    )

    fun module(id: String): Module? = modules.firstOrNull { it.id == id }
}
