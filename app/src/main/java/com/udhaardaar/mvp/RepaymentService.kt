package com.udhaardaar.mvp

import java.time.Instant

/** Single enforcement point for repayment mutations. */
object RepaymentService {
    data class RepaymentRequest(
        val creditId: Long,
        val amount: Double,
        val outstandingBefore: Double,
        val requester: AccessControl.CreditParty,
        val parties: List<AccessControl.CreditParty>,
        val note: String = ""
    )

    data class RepaymentReceipt(
        val creditId: Long,
        val amount: Double,
        val recordedBy: String,
        val recordedAt: String,
        val note: String
    )

    sealed class Result {
        data class Success(val receipt: RepaymentReceipt) : Result()
        data class Rejected(val reason: String) : Result()
    }

    fun record(request: RepaymentRequest): Result {
        if (request.creditId <= 0L) return Result.Rejected("Invalid credit record")
        if (!request.amount.isFinite() || request.amount <= 0.0) return Result.Rejected("Repayment amount must be greater than zero")
        if (!request.outstandingBefore.isFinite() || request.outstandingBefore < 0.0) return Result.Rejected("Invalid outstanding amount")
        if (request.amount > request.outstandingBefore + 0.005) return Result.Rejected("Repayment cannot exceed outstanding amount")
        if (!AccessControl.canRecordRepayment(request.requester, request.parties)) {
            return Result.Rejected("You are not authorised to record this repayment")
        }
        return Result.Success(
            RepaymentReceipt(
                creditId = request.creditId,
                amount = request.amount,
                recordedBy = request.requester.userId,
                recordedAt = Instant.now().toString(),
                note = request.note.trim()
            )
        )
    }
}
