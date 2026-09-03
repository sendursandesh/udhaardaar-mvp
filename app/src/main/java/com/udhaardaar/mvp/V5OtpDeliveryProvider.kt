package com.udhaardaar.mvp

/** OTP transport contract. Demo delivery is UI-visible only; production must inject an HTTPS/SMS implementation. */
interface V5OtpDeliveryProvider { fun deliver(recipient:String, purpose:String, otp:String):Boolean }
class V5DemoOtpDeliveryProvider:V5OtpDeliveryProvider { override fun deliver(recipient:String,purpose:String,otp:String)=true }
/** Production adapter contract; actual SMS vendor endpoint/credentials belong on a backend, never in the APK. */
class V5BackendOtpDeliveryProvider(private val httpsEndpoint:String,private val authToken:String):V5OtpDeliveryProvider {
 override fun deliver(recipient:String,purpose:String,otp:String):Boolean { require(httpsEndpoint.startsWith("https://")); require(authToken.isNotBlank()); return true }
}
