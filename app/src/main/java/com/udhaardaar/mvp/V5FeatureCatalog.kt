package com.udhaardaar.mvp

/**
 * V5 product contract. UI modules are intentionally separated from the legacy V4 screen.
 * This catalog is the single feature registry used while wiring the V5 navigation and data layer.
 */
object V5FeatureCatalog {
    val modules = listOf(
        "Dashboard",
        "Account & Authentication",
        "Person / Business Profiles",
        "Informal Credit",
        "Formal Credit",
        "Trade Credit & Invoice",
        "Rental / Lease",
        "Repayment Centre",
        "DPN & Guarantor Guarantee",
        "OTP / Digital Consent",
        "Document & Audit Vault",
        "Financial Asset Vault",
        "Non-Financial Asset Vault",
        "Nominee / Beneficiary / Trusted Person",
        "Inheritance & Succession",
        "Asset Claim Assistance",
        "Legal Assistance",
        "Statement OCR & Sanction-vs-Actual Charge Audit",
        "Notifications & Reminders",
        "Credit / Exposure Analytics",
        "Privacy, Permissions & Audit",
        "Scalable API / Sync boundary"
    )

    val informalCreditTypes = listOf(
        "Personal Credit", "Business Credit", "Trade Credit", "Advance", "Other"
    )

    val formalCreditTypes = listOf(
        "Bank Loan", "NBFC Loan", "Credit Card", "Business Loan", "Other Formal Credit"
    )

    val rentalTypes = listOf(
        "Residential Lease", "Commercial Lease", "Equipment Rental", "Vehicle Rental", "Other Lease"
    )

    val consentStates = listOf("NOT_REQUIRED", "PENDING", "OTP_SENT", "OTP_VERIFIED", "DECLINED", "EXPIRED")

    val documentTypes = listOf(
        "DPN", "GUARANTEE", "SANCTION_LETTER", "ACCOUNT_STATEMENT", "INVOICE",
        "LEASE_AGREEMENT", "REPAYMENT_RECEIPT", "ASSET_PROOF", "IDENTITY_PROOF",
        "NOMINATION", "LEGAL_CLAIM", "OTHER"
    )
}
