package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

class V5CreditRepository(context:Context){
    private val store=V5LocalStore(context)
    fun create(c:V5CreditRecord):String{
        val id=c.id.ifBlank{"CR-${System.currentTimeMillis()}"}
        store.add("credits",JSONObject().apply{put("id",id);put("profileId",c.profileId);put("direction",c.direction);put("type",c.creditType);put("principal",c.principal);put("roi",c.roiPercent);put("repaymentMethod",c.repaymentMethod);put("start",c.startDate);put("end",c.endDate);put("consent",c.consentState)})
        return id
    }
    fun markBorrowerConsent(creditId:String,consentId:String):Boolean{val o=store.find("credits",creditId)?:return false;o.put("consent","OTP_VERIFIED");o.put("borrowerConsentId",consentId);o.put("consentedAt",System.currentTimeMillis());store.replace("credits",o);return true}
}
