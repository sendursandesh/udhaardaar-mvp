package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

/** V5 repayment service: informal entries become ledger entries only after counterparty OTP consent. */
class V5RepaymentService(context: Context) {
    private val store = V5LocalStore(context)
    fun request(creditId:String, initiatedBy:String, amount:Double, date:String, method:String, reference:String, evidenceId:String?=null):String {
        require(amount>0 && creditId.isNotBlank() && date.isNotBlank())
        require(store.find("credits",creditId)!=null){"Credit not found"}
        val initiator=V5ConsentAndScore.Party.valueOf(initiatedBy.uppercase())
        require(initiator==V5ConsentAndScore.Party.BORROWER || initiator==V5ConsentAndScore.Party.LENDER)
        val counterparty=if(initiator==V5ConsentAndScore.Party.BORROWER)V5ConsentAndScore.Party.LENDER else V5ConsentAndScore.Party.BORROWER
        val id="RP-${System.currentTimeMillis()}";val otp=(100000+Random.nextInt(900000)).toString()
        store.add("repayment_requests",JSONObject().apply{put("id",id);put("creditId",creditId);put("initiatedBy",initiator.name);put("counterparty",counterparty.name);put("amount",amount);put("date",date);put("method",method);put("reference",reference);put("evidenceId",evidenceId?:"");put("otp",otp);put("status","COUNTERPARTY_OTP_PENDING");put("createdAt",System.currentTimeMillis())})
        return id
    }
    fun confirm(requestId:String,otp:String,expectedOtp:String):Boolean {
        val r=store.find("repayment_requests",requestId)?:return false
        if(r.optString("status")!="COUNTERPARTY_OTP_PENDING"||otp.length!=6||otp!=expectedOtp)return false
        val creditId=r.optString("creditId");val credit=store.find("credits",creditId)?:return false
        val requested=r.optDouble("amount",0.0);val oldOutstanding=credit.optDouble("outstanding",credit.optDouble("totalPayable",credit.optDouble("amount",0.0)));val newOutstanding=(oldOutstanding-requested).coerceAtLeast(0.0)
        val ledgerId="LED-${System.currentTimeMillis()}";r.put("status","CONFIRMED");r.put("consentedAt",System.currentTimeMillis());r.put("confirmedLedgerId",ledgerId);store.replace("repayment_requests",r)
        store.add("repayments",JSONObject().apply{put("id",ledgerId);put("requestId",requestId);put("creditId",creditId);put("counterparty",r.optString("counterparty"));put("amount",requested);put("date",r.optString("date"));put("method",r.optString("method"));put("direction",if(r.optString("initiatedBy")=="BORROWER")"PAYABLE" else "RECEIVABLE");put("reference",r.optString("reference"));put("status","CONFIRMED");put("consent","OTP_VERIFIED");put("consentedAt",r.optLong("consentedAt"));put("createdAt",System.currentTimeMillis())})
        credit.put("outstanding",newOutstanding);credit.put("status",if(newOutstanding<=0.01)"SETTLED" else "ACTIVE");credit.put("lastRepaymentAt",System.currentTimeMillis());store.replace("credits",credit)
        store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",requestId);put("event","COUNTERPARTY_OTP_CONSENTED_AND_LEDGER_POSTED");put("at",System.currentTimeMillis())})
        return true
    }
}
