package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

/** V5 repayment service. Informal confirmations are counterparty-consented; confirmed payments update outstanding. */
class V5RepaymentService(context:Context){
    private val store=V5LocalStore(context)
    fun request(creditId:String,initiatedBy:String,amount:Double,date:String,method:String,reference:String,evidenceId:String?=null):String{
        require(amount>0&&creditId.isNotBlank()&&date.isNotBlank())
        val credit=store.find("credits",creditId)?:throw IllegalArgumentException("Credit not found")
        val initiator=V5ConsentAndScore.Party.valueOf(initiatedBy.uppercase())
        require(initiator==V5ConsentAndScore.Party.BORROWER||initiator==V5ConsentAndScore.Party.LENDER)
        val outstanding=credit.optDouble("outstanding",credit.optDouble("amount",0.0))
        require(outstanding>0&&amount<=outstanding)
        val counterparty=if(initiator==V5ConsentAndScore.Party.BORROWER)V5ConsentAndScore.Party.LENDER else V5ConsentAndScore.Party.BORROWER
        val id="RP-${System.currentTimeMillis()}";val code=(100000+Random.nextInt(900000)).toString()
        store.add("repayment_requests",JSONObject().apply{put("id",id);put("creditId",creditId);put("initiatedBy",initiator.name);put("counterparty",counterparty.name);put("amount",amount);put("date",date);put("method",method);put("reference",reference);put("evidenceId",evidenceId?:"");put("otp",code);put("status","COUNTERPARTY_OTP_PENDING");put("createdAt",System.currentTimeMillis())})
        return id
    }
    fun confirm(requestId:String,otp:String,expectedOtp:String):Boolean{
        val r=store.find("repayment_requests",requestId)?:return false
        if(r.optString("status")!="COUNTERPARTY_OTP_PENDING"||otp.length!=6||otp!=expectedOtp)return false
        val creditId=r.optString("creditId");val credit=store.find("credits",creditId)?:return false
        val outstanding=credit.optDouble("outstanding",credit.optDouble("amount",0.0));val paid=r.optDouble("amount",0.0);if(paid<=0||paid>outstanding)return false
        r.put("status","CONFIRMED");r.put("consentedAt",System.currentTimeMillis());store.replace("repayment_requests",r)
        credit.put("outstanding",(outstanding-paid).coerceAtLeast(0.0));credit.put("lastRepaymentAt",System.currentTimeMillis());store.replace("credits",credit)
        store.add("repayments",JSONObject().apply{put("id",requestId);put("creditId",creditId);put("amount",paid);put("date",r.optString("date"));put("method",r.optString("method"));put("reference",r.optString("reference"));put("direction",if(r.optString("initiatedBy")=="BORROWER")"PAYABLE" else "RECEIVABLE");put("status","CONFIRMED_INFORMAL");put("consent","COUNTERPARTY_OTP");put("consentId",requestId);put("outstandingAfter",credit.optDouble("outstanding"));put("createdAt",System.currentTimeMillis())})
        store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",requestId);put("event","COUNTERPARTY_OTP_CONSENTED");put("at",System.currentTimeMillis())})
        return true
    }
}
