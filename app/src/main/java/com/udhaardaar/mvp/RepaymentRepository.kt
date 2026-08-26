package com.udhaardaar.mvp

/**
 * Persistence boundary for repayments. UI code must call this facade instead of
 * writing repayment rows directly, so authorization is enforced before mutation.
 * The actual database implementation can be supplied without weakening the rule.
 */
class RepaymentRepository(private val writer: (RepaymentService.RepaymentReceipt) -> Boolean) {

    fun record(request: RepaymentService.RepaymentRequest): RepaymentService.Result {
        return when (val result = RepaymentService.record(request)) {
            is RepaymentService.Result.Rejected -> result
            is RepaymentService.Result.Success -> {
                if (writer(result.receipt)) result
                else RepaymentService.Result.Rejected("Repayment could not be saved")
            }
        }
    }
}
