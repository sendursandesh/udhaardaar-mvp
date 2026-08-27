package com.udhaardaar.mvp

/** Registration gate used before a credit is committed. */
object ConsentOtpGate {
    data class State(
        val consentGranted: Boolean = false,
        val otpVerified: Boolean = false
    ) {
        val canRegister: Boolean get() = consentGranted && otpVerified
    }

    private var registrationAuthorised = false

    fun grantConsent(state: State, granted: Boolean): State =
        state.copy(consentGranted = granted).also { registrationAuthorised = it.canRegister }

    fun markOtpVerified(state: State, verified: Boolean): State =
        state.copy(otpVerified = verified).also { registrationAuthorised = it.canRegister }

    fun requireReady(state: State): Result<Unit> = when {
        !state.consentGranted -> Result.failure(IllegalStateException("Consent is required"))
        !state.otpVerified -> Result.failure(IllegalStateException("OTP verification is required"))
        else -> Result.success(Unit)
    }

    fun isRegistrationAuthorised(): Boolean = registrationAuthorised

    fun clearAfterRegistration() { registrationAuthorised = false }
}
