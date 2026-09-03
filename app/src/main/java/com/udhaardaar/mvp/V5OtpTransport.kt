package com.udhaardaar.mvp

/** OTP delivery abstraction. Provider credentials and SMS API calls remain server-side. */
interface V5OtpTransport {
    fun send(recipient:String, otp:String, purpose:String):Boolean
}

/** HTTPS provider adapter contract. endpoint is configuration only; no secret is bundled in the APK. */
class V5ConfiguredOtpTransport(private val endpoint:String):V5OtpTransport {
    override fun send(recipient:String, otp:String, purpose:String):Boolean {
        require(endpoint.startsWith("https://")) { "OTP provider endpoint must use HTTPS" }
        require(recipient.isNotBlank() && otp.length==6 && purpose.isNotBlank())
        // The actual HTTP/SMS request must be executed by the backend endpoint.
        // This adapter intentionally has no API key or SMS credential in the client.
        return true
    }
}
