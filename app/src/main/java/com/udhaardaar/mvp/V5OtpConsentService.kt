package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

/** Consent state machine with provider boundary. Production provider must be supplied by backend configuration. */
class V5OtpConsentService(context:Context) {
 private val store=V5LocalStore(context)
 fun issue(entityId:String,purpose:String,recipientParty:String):String{val id="CONS-${System.currentTimeMillis()}";val otp=(100000+Random.nextInt(900000)).toString();store.add("consents",JSONObject().apply{put("id",id);put("entityId",entityId);put("purpose",purpose);put("party",recipientParty);put("otp",otp);put("status","OTP_SENT");put("deliveryTransport","BACKEND_SMS_PROVIDER");put("issuedAt",System.currentTimeMillis())});return id}
 fun verify(consentId:String,otp:String):Boolean{val c=store.find("consents",consentId)?:return false;if(c.optString("status")!="OTP_SENT"||c.optString("otp")!=otp)return false;c.put("status","OTP_VERIFIED");c.put("verifiedAt",System.currentTimeMillis());c.remove("otp");store.replace("consents",c);store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",c.optString("entityId"));put("event",c.optString("purpose")+"_OTP_VERIFIED");put("party",c.optString("party"));put("at",System.currentTimeMillis())});return true}
 fun isVerified(id:String)=store.find("consents",id)?.optString("status")=="OTP_VERIFIED"
}
