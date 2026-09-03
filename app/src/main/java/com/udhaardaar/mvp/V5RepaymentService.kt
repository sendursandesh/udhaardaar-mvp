package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

class V5RepaymentService(context: Context) {
    private val store=V5LocalStore(context)
    fun request(creditId:String,initiatedBy:String,amount:Double,date:String,method:String,reference:String,evidenceId:String?=null):String{
        require(amount>0&&creditId.isNotBlank()&&date.isNotBlank())
        val initiator=V5ConsentAndScore.Party.valueOf(initiatedBy.uppercase());require(initiator==V5ConsentAndScore.Party.BORROWER||initiator==V5ConsentAndScore.Party.LENDER)
        val counterparty=if(initiator==V5ConsentAndScore.Party.BORROWER)V5ConsentAndScore.Party.LENDER else V5ConsentAndScore.Party.BORROWER
        val id="RP-${System.currentTimeMillis()}";val otp=(100000+Random.nextInt(900000)).toString()
        store.add("repayment_requests",JSONObject().apply{put("id",id);put("creditId",creditId);put("initiatedBy",initiator.name);put("counterparty",counterparty.name);put("amount",amount);put("date",date);put("method",method);put("reference",reference);put("evidenceId",evidenceId?:"");put("otp",otp);put("status","COUNTERPARTY_OTP_PENDING");put("createdAt",System.currentTimeMillis())});return id
    }
    fun confirm(requestId:String,otp:String,expectedOtp:String):Boolean{
        val r=store.find("repayment_requests",requestId)?:return false
        if(r.optString("status")!="COUNTERPARTY_OTP_PENDING"||otp.length!=6||otp!=expectedOtp)return false
        r.put("status","CONFIRMED");r.put("consentedAt",System.currentTimeMillis());store.replace("repayment_requests",r)
        val creditId=r.optString("creditId");val credit=store.find("credits",creditId)
        if(credit!=null){val old=credit.optDouble("repaid",0.0);val paid=r.optDouble("amount",0.0);val principal=credit.optDouble("principal",0.0);val total=credit.optDouble("totalPayable",principal);val newPaid=old+paid;credit.put("repaid",newPaid);credit.put("outstanding",(total-newPaid).coerceAtLeast(0.0));credit.put("lastRepaymentAt",System.currentTimeMillis());credit.put("updatedAt",System.currentTimeMillis());if(total>0&&newPaid>=total)credit.put("status","CLOSED");store.replace("credits",credit)}
        store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",requestId);put("event","COUNTERPARTY_OTP_CONSENTED_AND_REPAYMENT_APPLIED");put("creditId",creditId);put("at",System.currentTimeMillis());put("amount",r.optDouble("amount",0.0))});return true
    }
}
