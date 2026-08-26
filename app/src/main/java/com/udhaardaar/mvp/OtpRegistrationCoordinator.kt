package com.udhaardaar.mvp

/** Coordinates a specific party OTP challenge with the registration gate. */
object OtpRegistrationCoordinator {
    fun start(partyId: Long, phone: String, challengeId: String): OtpVerificationStore.Challenge =
        OtpVerificationStore.begin(partyId, phone.trim(), challengeId)

    fun verify(challengeId: String): Boolean =
        OtpVerificationStore.markVerified(challengeId)

    fun consumeForRegistration(partyId: Long, phone: String): Boolean =
        OtpVerificationStore.consume(partyId, phone.trim())

    fun clear() = OtpVerificationStore.clear()
}
