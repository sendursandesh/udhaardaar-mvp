package com.udhaardaar.mvp

/** Sequential state transitions shared by the V5 UI and persistence layers. */
object V5EventFlow {
    enum class CreditState { DRAFT, BORROWER_IDENTIFIED, TERMS_ENTERED, SCORE_CONSENT_PENDING, SCORE_AVAILABLE, DOCUMENTS_PENDING, BORROWER_CONSENTED, GUARANTOR_PENDING, GUARANTOR_CONSENTED, REGISTERED, ACTIVE, CLOSED }
    enum class RepaymentState { SCHEDULED, REQUESTED_BY_LENDER, REQUESTED_BY_BORROWER, COUNTERPARTY_OTP_PENDING, CONFIRMED, PARTIAL, FULLY_PAID, DISPUTED }
    enum class ClaimState { IDENTIFIED, DOCUMENTS_PENDING, PREPARED, SUBMITTED, QUERY_RECEIVED, APPROVED, TRANSFERRED, DISPUTED, CLOSED }

    fun creditAfterBorrowerConsent(state:CreditState)=when(state){CreditState.SCORE_CONSENT_PENDING->CreditState.SCORE_AVAILABLE;else->state}
    fun creditAfterDocuments(state:CreditState)=when(state){CreditState.DOCUMENTS_PENDING->CreditState.BORROWER_CONSENTED;else->state}
    fun repaymentAfterRequest(by:String)=if(by.equals("LENDER",true))RepaymentState.REQUESTED_BY_LENDER else RepaymentState.REQUESTED_BY_BORROWER
    fun repaymentAfterCounterpartyOtp(state:RepaymentState)=when(state){RepaymentState.REQUESTED_BY_LENDER,RepaymentState.REQUESTED_BY_BORROWER,RepaymentState.COUNTERPARTY_OTP_PENDING->RepaymentState.CONFIRMED;else->state}
}
