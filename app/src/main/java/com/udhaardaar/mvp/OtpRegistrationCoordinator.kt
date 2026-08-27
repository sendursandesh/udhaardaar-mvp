package com.udhaardaar.mvp

/** Coordinates a party-bound, single-use OTP challenge with the registration gate. */
object OtpRegistrationCoordinator {
    fun start(partyId: Long, phone: String, challengeId: String): OtpVerificationStore.Challenge {
        require(partyId > 0) { "Invalid party" }
        require(phone.trim().isNotEmpty()) { "Phone is required" }
        require(challengeId.isNotBlank()) { "Challenge ID is required" }
        return OtpVerificationStore.begin(partyId, phone.trim(), challengeId)
    }

    fun verify(challengeId: String, sessionToken: String): Boolean {
        if (challengeId.isBlank() || sessionToken.isBlank()) return false
        return OtpVerificationStore.markVerified(challengeId) && ConsentOtpGate.markOtpVerified(sessionToken)
    }

    fun consumeForRegistration(partyId: Long, phone: String): Boolean =
        OtpVerificationStore.consume(partyId, phone.trim())

    fun clear() = OtpVerificationStore.clear()
}
