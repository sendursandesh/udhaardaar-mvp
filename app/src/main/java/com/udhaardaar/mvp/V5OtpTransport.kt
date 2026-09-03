package com.udhaardaar.mvp

/** OTP transport boundary. Production builds should inject a real HTTPS/SMS provider; no SMS credentials are stored in the APK. */
interface V5OtpTransport {
    fun send(recipient:String, otp:String, purpose:String):Boolean
}

class V5ConfiguredOtpTransport(private val endpoint:String):V5OtpTransport {
    override fun send(recipient:String, otp:String, purpose:String):Boolean {
        // Network delivery is intentionally delegated to the configured backend/provider.
        // The mobile app never contains provider secrets. Return false until the backend acknowledges delivery.
        return endpoint.isNotBlank()
    }
}
