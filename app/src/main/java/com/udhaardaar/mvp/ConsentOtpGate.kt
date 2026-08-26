package com.udhaardaar.mvp

/**
 * Registration gate used before a credit is committed.
 * The real OTP provider supplies the verification result; this class keeps
 * registration blocked until both consent and provider verification are true.
 */
object ConsentOtpGate {
    data class State(
        val consentGranted: Boolean = false,
        val otpVerified: Boolean = false
    ) {
        val canRegister: Boolean get() = consentGranted && otpVerified
    }

    fun grantConsent(state: State, granted: Boolean): State =
        state.copy(consentGranted = granted)

    fun markOtpVerified(state: State, verified: Boolean): State =
        state.copy(otpVerified = verified)

    fun requireReady(state: State): Result<Unit> =
        when {
            !state.consentGranted -> Result.failure(IllegalStateException("Consent is required"))
            !state.otpVerified -> Result.failure(IllegalStateException("OTP verification is required"))
            else -> Result.success(Unit)
        }
}
