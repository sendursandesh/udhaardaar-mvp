package com.udhaardaar.mvp

import android.content.Context

/** V5 application service. UI calls this facade; it owns the repositories and critical transitions. */
class V5IntegratedFlows(context: Context) {
    private val repo = V5WorkflowRepository(context.applicationContext)
    private val repayments = V5RepaymentRepository(context.applicationContext)

    data class CreditDraft(
        val id: String, val borrowerId: String, val direction: String, val type: String,
        val principal: Double, val roi: Double, val start: String, val end: String,
        val repaymentMethod: String, val guarantorIds: List<String> = emptyList()
    )

    fun registerCreditAfterConsent(c: CreditDraft, borrowerConsentId: String): String {
        require(c.borrowerId.isNotBlank())
        require(c.principal > 0)
        require(borrowerConsentId.isNotBlank())
        val id = V5CreditRepository(contextOf()).create(V5Credit(c.id, c.borrowerId, c.direction, c.type, c.principal, c.roi, c.repaymentMethod, c.start, c.end, "OTP_VERIFIED"))
        repo.appendAudit(id, "CREDIT_REGISTERED_AFTER_BORROWER_CONSENT", borrowerConsentId, "type=${c.type};direction=${c.direction}")
        return id
    }

    fun borrowerScoreConsent(creditId: String, consentId: String): Boolean {
        val ok = V5CreditRepository(contextOf()).markBorrowerConsent(creditId, consentId)
        if (ok) repo.appendAudit(creditId, "BORROWER_SCORE_CONSENT_OTP_VERIFIED", consentId, "Score may now be displayed")
        return ok
    }

    fun recordInformalRepaymentRequest(creditId: String, initiatedBy: String, amount: Double, date: String, method: String, reference: String, evidenceId: String? = null): String {
        val id = repayments.request(creditId, initiatedBy, amount, date, method, reference, evidenceId)
        repo.appendAudit(creditId, "REPAYMENT_REQUESTED", initiatedBy, "request=$id;amount=$amount")
        return id
    }

    fun confirmInformalRepayment(requestId: String, otp: String, expectedOtp: String, actorId: String): Boolean {
        val ok = repayments.confirm(requestId, otp, expectedOtp)
        if (ok) repo.appendAudit(requestId, "COUNTERPARTY_OTP_VERIFIED_REPAYMENT_CONFIRMED", actorId, "request=$requestId")
        return ok
    }

    fun calculateScoreAfterConsent(creditId: String, borrowerConsentVerified: Boolean, totalCredits: Int, completedCredits: Int, overdueCount: Int, repaymentEvents: Int, disputedEvents: Int, averageDaysLate: Double): V5ConsentAndScore.ScoreResult? {
        require(borrowerConsentVerified) { "Borrower OTP consent is required before score display" }
        return V5ConsentAndScore.calculateScore(borrowerConsentVerified, totalCredits, completedCredits, overdueCount, repaymentEvents, disputedEvents, averageDaysLate)
    }

    private fun contextOf(): Context = repoContext
    private val repoContext: Context
        get() = throw UnsupportedOperationException("Use the Context-injected V5IntegratedFlows constructor implementation")
}
