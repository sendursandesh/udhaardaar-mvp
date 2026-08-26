package com.udhaardaar.mvp

import java.util.concurrent.atomic.AtomicReference

/**
 * Short-lived in-process OTP state. A production OTP provider should replace
 * the verification callback, but the registration flow must consume a verified
 * challenge for the specific party/phone rather than a global boolean.
 */
object OtpVerificationStore {
    data class Challenge(
        val challengeId: String,
        val partyId: Long,
        val phone: String,
        val expiresAtMillis: Long,
        val verified: Boolean = false,
        val consumed: Boolean = false
    )

    private val current = AtomicReference<Challenge?>(null)

    fun begin(partyId: Long, phone: String, challengeId: String, ttlMillis: Long = 5 * 60 * 1000L): Challenge {
        val challenge = Challenge(challengeId, partyId, phone, System.currentTimeMillis() + ttlMillis)
        current.set(challenge)
        return challenge
    }

    fun markVerified(challengeId: String): Boolean {
        val value = current.get() ?: return false
        if (value.challengeId != challengeId || System.currentTimeMillis() > value.expiresAtMillis || value.consumed) return false
        current.set(value.copy(verified = true))
        return true
    }

    fun consume(partyId: Long, phone: String): Boolean {
        val value = current.get() ?: return false
        if (!value.verified || value.consumed || value.partyId != partyId || value.phone != phone || System.currentTimeMillis() > value.expiresAtMillis) return false
        current.set(value.copy(consumed = true))
        return true
    }

    fun clear() { current.set(null) }
}
