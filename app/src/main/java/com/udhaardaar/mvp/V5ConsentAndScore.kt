package com.udhaardaar.mvp

import java.security.MessageDigest
import kotlin.math.max

/** Core V5 business rules for bilateral consent, documents and the explainable Udhaardaar Score. */
object V5ConsentAndScore {
    enum class Party { BORROWER, LENDER, GUARANTOR }
    enum class ConsentState { NOT_REQUIRED, PENDING, OTP_VERIFIED, DECLINED, EXPIRED }

    data class RepaymentRequest(
        val id: String,
        val creditId: String,
        val initiatedBy: Party,
        val counterparty: Party,
        val amount: Double,
        val paymentDate: String,
        val method: String,
        val reference: String,
        val evidenceDocumentId: String? = null,
        val state: ConsentState = ConsentState.PENDING
    )

    data class ConsentRecord(
        val documentId: String,
        val party: Party,
        val state: ConsentState,
        val otpVerifiedAt: Long? = null,
        val documentHash: String? = null
    )

    data class ScoreResult(
        val score: Int,
        val band: String,
        val factors: List<String>,
        val disclaimer: String = "Udhaardaar Score is an internal assessment based on records available in Udhaardaar; it is not a statutory credit-bureau score."
    )

    fun requiresInformalRepaymentConsent(): Boolean = true

    fun createRepaymentRequest(
        creditId: String,
        initiatedBy: Party,
        amount: Double,
        paymentDate: String,
        method: String,
        reference: String,
        evidenceDocumentId: String? = null
    ): RepaymentRequest {
        require(creditId.isNotBlank())
        require(amount > 0)
        require(paymentDate.isNotBlank())
        val counterparty = when (initiatedBy) {
            Party.BORROWER -> Party.LENDER
            Party.LENDER -> Party.BORROWER
            Party.GUARANTOR -> Party.LENDER
        }
        return RepaymentRequest(
            id = "RP-${System.currentTimeMillis()}", creditId = creditId,
            initiatedBy = initiatedBy, counterparty = counterparty,
            amount = amount, paymentDate = paymentDate, method = method,
            reference = reference, evidenceDocumentId = evidenceDocumentId
        )
    }

    fun confirmRepaymentWithOtp(request: RepaymentRequest, otpEntered: String, expectedOtp: String): RepaymentRequest {
        require(request.state == ConsentState.PENDING) { "Repayment request is not pending consent" }
        require(otpEntered.length == 6 && otpEntered == expectedOtp) { "Counterparty OTP verification failed" }
        return request.copy(state = ConsentState.OTP_VERIFIED)
    }

    fun sha256(content: ByteArray): String = MessageDigest.getInstance("SHA-256").digest(content).joinToString("") { "%02x".format(it) }

    /** Score only after explicit borrower consent; factors are explainable and bounded. */
    fun calculateScore(
        borrowerConsentVerified: Boolean,
        totalCredits: Int,
        completedCredits: Int,
        overdueCount: Int,
        repaymentEvents: Int,
        disputedEvents: Int,
        averageDaysLate: Double
    ): ScoreResult? {
        if (!borrowerConsentVerified) return null
        val completion = if (totalCredits == 0) 0.5 else (completedCredits.toDouble() / totalCredits).coerceIn(0.0, 1.0)
        val punctuality = (1.0 - (averageDaysLate / 90.0)).coerceIn(0.0, 1.0)
        val disputes = (1.0 - disputedEvents.toDouble() / max(1, repaymentEvents)).coerceIn(0.0, 1.0)
        val activity = (repaymentEvents.toDouble() / max(1, totalCredits * 4)).coerceIn(0.0, 1.0)
        val overduePenalty = (overdueCount.toDouble() / max(1, totalCredits * 3)).coerceIn(0.0, 1.0)
        val raw = 300 + (completion * 300) + (punctuality * 180) + (disputes * 100) + (activity * 120) - (overduePenalty * 220)
        val score = raw.toInt().coerceIn(300, 900)
        val band = when {
            score >= 800 -> "Excellent"
            score >= 700 -> "Strong"
            score >= 600 -> "Moderate"
            score >= 500 -> "Needs attention"
            else -> "High attention"
        }
        val factors = listOf(
            "Completed obligations: $completedCredits of $totalCredits",
            "Overdue records: $overdueCount",
            "Average reported delay: ${"%.1f".format(averageDaysLate)} days",
            "Repayment events: $repaymentEvents",
            "Disputed events: $disputedEvents"
        )
        return ScoreResult(score, band, factors)
    }
}
