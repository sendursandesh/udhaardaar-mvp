package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/** Informal repayments are request/consent driven. There is deliberately no mark-instalment-paid API. */
class V5RepaymentRepository(context: Context) {
    private val store = V5LocalStore(context)
    fun request(creditId:String, initiatedBy:String, amount:Double, date:String, method:String, reference:String, evidenceId:String?=null):String {
        val party=V5ConsentAndScore.Party.valueOf(initiatedBy.uppercase())
        val r=V5ConsentAndScore.createRepaymentRequest(creditId,party,amount,date,method,reference,evidenceId)
        store.add("repayment_requests", JSONObject().apply{
            put("id",r.id);put("creditId",r.creditId);put("initiatedBy",r.initiatedBy.name);put("counterparty",r.counterparty.name);put("amount",r.amount);put("paymentDate",r.paymentDate);put("method",r.method);put("reference",r.reference);put("evidenceDocumentId",r.evidenceDocumentId ?: JSONObject.NULL);put("state",r.state.name)
        })
        return r.id
    }
    fun confirm(requestId:String, otp:String, expectedOtp:String):Boolean {
        val o=store.find("repayment_requests",requestId) ?: return false
        val request=V5ConsentAndScore.RepaymentRequest(requestId,o.optString("creditId"),V5ConsentAndScore.Party.valueOf(o.optString("initiatedBy")),V5ConsentAndScore.Party.valueOf(o.optString("counterparty")),o.optDouble("amount"),o.optString("paymentDate"),o.optString("method"),o.optString("reference"),o.optString("evidenceDocumentId").takeIf{it.isNotBlank()&&it!="null"},V5ConsentAndScore.ConsentState.valueOf(o.optString("state")))
        return try { val confirmed=V5ConsentAndScore.confirmRepaymentWithOtp(request,otp,expectedOtp);o.put("state",confirmed.state.name);o.put("consentedAt",System.currentTimeMillis());store.replace("repayment_requests",o);true } catch(_:Exception){false}
    }
}
