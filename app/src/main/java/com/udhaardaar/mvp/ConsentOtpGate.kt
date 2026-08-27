package com.udhaardaar.mvp

/** Session-scoped registration gate. OTP must be verified for this exact session. */
object ConsentOtpGate {
    data class State(
        val sessionToken: String? = null,
        val consentGranted: Boolean = false,
        val otpVerified: Boolean = false
    ) { val canRegister: Boolean get() = sessionToken != null && consentGranted && otpVerified }

    @Volatile private var current = State()

    @Synchronized fun beginSession(token: String) { current = State(sessionToken = token) }
    @Synchronized fun grantConsent(granted: Boolean) { current = current.copy(consentGranted = granted, otpVerified = if (granted) current.otpVerified else false) }
    @Synchronized fun markOtpVerified(token: String): Boolean {
        if (current.sessionToken == null || current.sessionToken != token || !current.consentGranted) return false
        current = current.copy(otpVerified = true)
        return true
    }
    fun isRegistrationAuthorised(): Boolean = current.canRegister
    fun requireReady(): Result<Unit> = when {
        current.sessionToken == null -> Result.failure(IllegalStateException("Registration session is not active"))
        !current.consentGranted -> Result.failure(IllegalStateException("Consent is required"))
        !current.otpVerified -> Result.failure(IllegalStateException("OTP verification is required"))
        else -> Result.success(Unit)
    }
    @Synchronized fun clearAfterRegistration() { current = State() }
}
