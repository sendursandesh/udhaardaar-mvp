package com.udhaardaar.mvp

/**
 * Central access rules for credit records.
 * A record is never visible/editable merely because a user knows its identifier.
 */
object AccessControl {
    enum class Role { LENDER, BORROWER, GUARANTOR, ADMIN }
    enum class Action { VIEW, EDIT_REPAYMENT, RECORD_REPAYMENT, VIEW_DOCUMENT }

    data class CreditParty(
        val userId: String,
        val role: Role,
        val consentGranted: Boolean = false,
        val consentRevoked: Boolean = false
    )

    fun isAuthorised(
        requester: CreditParty,
        parties: List<CreditParty>,
        action: Action
    ): Boolean {
        if (requester.consentRevoked) return false
        if (requester.role == Role.ADMIN) return action == Action.VIEW
        if (!parties.any { it.userId == requester.userId && it.role == requester.role }) return false

        return when (action) {
            Action.VIEW, Action.VIEW_DOCUMENT ->
                requester.consentGranted || requester.role == Role.LENDER
            Action.RECORD_REPAYMENT, Action.EDIT_REPAYMENT ->
                requester.consentGranted &&
                    (requester.role == Role.LENDER || requester.role == Role.BORROWER)
        }
    }

    fun canRecordRepayment(requester: CreditParty, parties: List<CreditParty>): Boolean =
        isAuthorised(requester, parties, Action.RECORD_REPAYMENT)
}
