package com.udhaardaar.mvp

/**
 * Short-lived registration gate. Registration is allowed only after the
 * current consent state AND a verified OTP challenge are both present.
 * OTP verification itself must come from OtpRegistrationCoordinator; this
 * class never generates, guesses, or accepts an OTP on its own.
 */
object ConsentOtpGate {
    data class State(
        val consentGranted: Boolean = false,
        val otpVerified: Boolean = false
    ) {
        val canRegister: Boolean get() = consentGranted && otpVerified
    }

    @Volatile private var current = State()

    fun grantConsent(granted: Boolean) {
        current = current.copy(consentGranted = granted)
    }

    fun markOtpVerified(verified: Boolean) {
        current = current.copy(otpVerified = verified)
    }

    fun isRegistrationAuthorised(): Boolean = current.canRegister

    fun requireReady(): Result<Unit> = when {
        !current.consentGranted -> Result.failure(IllegalStateException("Consent is required"))
        !current.otpVerified -> Result.failure(IllegalStateException("OTP verification is required"))
        else -> Result.success(Unit)
    }

    fun clearAfterRegistration() {
        current = State()
    }
}
