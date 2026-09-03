package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

/** Bilateral informal-repayment service. No direct mark-paid path is exposed. */
class V5RepaymentService(context: Context) {
    private val store=V5LocalStore(context)
    fun request(creditId:String,initiatedBy:String,amount:Double,date:String,method:String,reference:String,evidenceId:String?=null):String{
        require(amount>0&&creditId.isNotBlank()&&date.isNotBlank())
        val credit=store.find("credits",creditId) ?: throw IllegalArgumentException("Credit not found")
        require(credit.optString("status")!="CLOSED") { "Credit is already closed" }
        require(amount <= credit.optDouble("outstanding",credit.optDouble("totalPayable",credit.optDouble("principal",0.0))) + 0.0001) { "Repayment exceeds current outstanding amount" }
        val initiator=V5ConsentAndScore.Party.valueOf(initiatedBy.uppercase());require(initiator==V5ConsentAndScore.Party.BORROWER||initiator==V5ConsentAndScore.Party.LENDER)
        val counterparty=if(initiator==V5ConsentAndScore.Party.BORROWER)V5ConsentAndScore.Party.LENDER else V5ConsentAndScore.Party.BORROWER
        val id="RP-${System.currentTimeMillis()}";val otp=(100000+Random.nextInt(900000)).toString();val now=System.currentTimeMillis()
        store.add("repayment_requests",JSONObject().apply{put("id",id);put("creditId",creditId);put("initiatedBy",initiator.name);put("counterparty",counterparty.name);put("amount",amount);put("date",date);put("method",method);put("reference",reference);put("evidenceId",evidenceId?:"");put("otp",otp);put("status","COUNTERPARTY_OTP_PENDING");put("createdAt",now);put("updatedAt",now)})
        return id
    }
    fun confirm(requestId:String,otp:String,expectedOtp:String):Boolean{
        val r=store.find("repayment_requests",requestId)?:return false
        if(r.optString("status")!="COUNTERPARTY_OTP_PENDING"||otp.length!=6||otp!=expectedOtp)return false
        val creditId=r.optString("creditId");val credit=store.find("credits",creditId)?:return false
        val old=credit.optDouble("repaid",0.0);val paid=r.optDouble("amount",0.0);val principal=credit.optDouble("principal",0.0);val total=credit.optDouble("totalPayable",principal);val outstanding=credit.optDouble("outstanding",total)
        if(paid<=0||paid>outstanding+0.0001)return false
        val now=System.currentTimeMillis();val newPaid=old+paid;val newOutstanding=(total-newPaid).coerceAtLeast(0.0)
        r.put("status","CONFIRMED");r.put("consentedAt",now);r.put("updatedAt",now);r.remove("otp");store.replace("repayment_requests",r)
        credit.put("repaid",newPaid);credit.put("outstanding",newOutstanding);credit.put("lastRepaymentAt",now);credit.put("updatedAt",now);if(newOutstanding<=0.0001)credit.put("status","CLOSED");store.replace("credits",credit)
        store.add("repayment_history",JSONObject().apply{put("id","RPH-${System.currentTimeMillis()}");put("requestId",requestId);put("creditId",creditId);put("amount",paid);put("date",r.optString("date"));put("method",r.optString("method"));put("reference",r.optString("reference"));put("initiatedBy",r.optString("initiatedBy"));put("counterparty",r.optString("counterparty"));put("confirmedAt",now)})
        store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",requestId);put("event","COUNTERPARTY_OTP_CONSENTED_AND_REPAYMENT_APPLIED");put("creditId",creditId);put("at",now);put("amount",paid);put("outstandingAfter",newOutstanding)})
        recalculateScore(credit.optString("borrower"))
        return true
    }
    private fun recalculateScore(borrower:String){
        if(borrower.isBlank())return
        val credits=store.all("credits").filter{it.optString("borrower").equals(borrower,true)};if(credits.none{it.optString("consentId").isNotBlank()})return
        val histories=store.all("repayment_history").filter{it.optString("creditId") in credits.map{c->c.optString("id")}}
        val score=V5ConsentAndScore.calculateScore(true,credits.size,credits.count{it.optString("status")=="CLOSED"},0,histories.size,0,0.0) ?: return
        store.all("profiles").filter{it.optString("name").equals(borrower,true)||it.optString("id")==borrower}.forEach{p->p.put("score",score.score);p.put("scoreBand",score.band);p.put("scoreUpdatedAt",System.currentTimeMillis());store.replace("profiles",p)}
        store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",borrower);put("event","UDHAARDAAR_SCORE_UPDATED_AFTER_CONFIRMED_REPAYMENT");put("at",System.currentTimeMillis());put("score",score.score);put("band",score.band)})
    }
}
