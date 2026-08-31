package com.udhaardaar.mvp

import android.content.Context

/** Application service shared by V5 screens; keeps critical business rules out of click handlers. */
class V5ApplicationService(context: Context) {
    private val repo = V5WorkflowRepository(context)
    private val repayments = V5RepaymentRepository(context)

    fun grantScoreAfterBorrowerConsent(creditId: String, consentId: String): Boolean {
        val ok = repo.markBorrowerConsent(creditId, consentId)
        if (ok) repo.appendAudit(creditId, "BORROWER_SCORE_CONSENT_OTP_VERIFIED", consentId, "Udhaardaar Score unlocked")
        return ok
    }

    fun requestInformalRepayment(creditId: String, initiatedBy: String, amount: Double, date: String, method: String, reference: String, evidenceId: String? = null): String {
        val id = repayments.request(creditId, initiatedBy, amount, date, method, reference, evidenceId)
        repo.appendAudit(creditId, "REPAYMENT_REQUESTED", initiatedBy, "request=$id; amount=$amount")
        return id
    }

    fun confirmInformalRepayment(requestId: String, otp: String, expectedOtp: String): Boolean = repayments.confirm(requestId, otp, expectedOtp)
}
