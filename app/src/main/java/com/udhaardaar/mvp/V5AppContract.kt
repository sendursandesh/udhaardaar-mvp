package com.udhaardaar.mvp

/** V5 end-to-end contract: UI, persistence and future API implementations must satisfy these rules. */
object V5AppContract {
    val requiredFlows = listOf(
        "LOGIN_CREATE_RECOVER_LOGOUT",
        "PROFILE_CREATE_PHOTO_IDENTITY_PIN_LOOKUP",
        "BORROWER_SEARCH_HISTORY_EXPOSURE",
        "GUARANTOR_CREATE_LINK_CONSENT",
        "INFORMAL_CREDIT_GIVEN_RECEIVED",
        "TRADE_INVOICE_EXTRACTION",
        "FORMAL_SANCTION_AND_STATEMENT_AUDIT",
        "RENTAL_LEASE_SEPARATE_WORKFLOW",
        "DPN_GENERATE_BORROWER_CONSENT_ARCHIVE",
        "GUARANTEE_GENERATE_GUARANTOR_CONSENT_ARCHIVE",
        "UDHAARDAAR_SCORE_AFTER_BORROWER_CONSENT",
        "BILATERAL_REPAYMENT_REQUEST_COUNTERPARTY_OTP",
        "PARTIAL_REPAYMENT_RECONCILIATION",
        "FINANCIAL_ASSET_VAULT",
        "NON_FINANCIAL_ASSET_VAULT",
        "NOMINEE_TRUSTED_PERSON_PERMISSIONS",
        "SUCCESSION_INHERITANCE_CLAIMS",
        "LEGAL_ASSISTANCE_EVIDENCE_BUNDLE",
        "DOCUMENT_VERSIONING_AUDIT_TRAIL",
        "DUE_OVERDUE_MATURITY_RENEWAL_REMINDERS",
        "PRIVACY_EXPORT_DELETE_AND_ACCESS_CONTROL"
    )

    /** Formal lender statement data is authoritative input; it does not invoke informal counterparty consent. */
    fun requiresRepaymentConsent(creditDomain: String) = creditDomain.equals("INFORMAL", true)
}
