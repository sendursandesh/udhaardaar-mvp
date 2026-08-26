package com.udhaardaar.mvp

/**
 * Single persistence boundary for repayment mutations.
 * UI/controller code must use this facade; authorization is checked before the writer is called.
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
