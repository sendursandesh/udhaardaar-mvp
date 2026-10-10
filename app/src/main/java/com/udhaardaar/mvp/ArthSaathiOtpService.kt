package com.udhaardaar.mvp

/**
 * Adapter boundary for a real SMS/OTP provider. Production code must install a
 * provider backed by a trusted server; the app deliberately has no local/demo OTP.
 */
interface ArthSaathiOtpProvider {
    /** Sends a code and returns a server-issued, non-secret challenge identifier. */
    fun requestCode(mobile: String, purpose: String, recordId: String): String?

    /** Verifies the code with the provider/server, including expiry and replay protection. */
    fun verifyCode(challengeId: String, code: String): Boolean
}

object ArthSaathiOtpService {
    @Volatile private var provider: ArthSaathiOtpProvider? = null

    fun installProvider(value: ArthSaathiOtpProvider) {
        provider = value
    }

    fun clearProviderForTests() {
        provider = null
    }

    fun isConfigured(): Boolean = provider != null

    internal fun request(mobile: String, purpose: String, recordId: String): String? =
        provider?.requestCode(mobile, purpose, recordId)

    internal fun verify(challengeId: String, code: String): Boolean =
        provider?.verifyCode(challengeId, code) == true
}
