package com.udhaardaar.mvp

import android.content.Context

/** V5 application service. UI calls this facade; it owns repositories and critical transitions. */
class V5IntegratedFlows(context: Context) {
    private val appContext = context.applicationContext
    private val repo = V5WorkflowRepository(appContext)
    private val repayments = V5RepaymentRepository(appContext)
    private val credits = V5CreditRepository(appContext)

    data class CreditDraft(
        val id: String, val borrowerId: String, val direction: String, val type: String,
        val principal: Double, val roi: Double, val start: String, val end: String,
        val repaymentMethod: String, val guarantorIds: List<String> = emptyList()
    )

    fun registerCreditAfterConsent(c: CreditDraft, borrowerConsentId: String): String {
        require(c.borrowerId.isNotBlank() && c.principal > 0 && borrowerConsentId.isNotBlank())
        val id = credits.create(V5Credit(c.id, c.borrowerId, c.direction, c.type, c.principal, c.roi, c.repaymentMethod, c.start, c.end, "OTP_VERIFIED"))
        repo.appendAudit(id, "CREDIT_REGISTERED_AFTER_BORROWER_CONSENT", borrowerConsentId, "type=${c.type};direction=${c.direction}")
        return id
    }

    fun borrowerScoreConsent(creditId: String, consentId: String): Boolean {
        val ok = credits.markBorrowerConsent(creditId, consentId)
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
}
