package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import kotlin.random.Random

/** V5 repayment service: informal entries become ledger entries only after counterparty OTP consent. */
class V5RepaymentService(private val context: Context) {
    private val store = V5LocalStore(context)

    /** Secure entry point: the initiating party is derived from the signed-in account, not from a UI selector. */
    fun requestForCurrentUser(creditId:String, amount:Double, date:String, method:String, reference:String, evidenceId:String?=null):String {
        val accounts = context.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE)
        val mobile = accounts.getString("current_mobile", "").orEmpty()
        require(mobile.isNotBlank()) { "Please sign in before recording repayment" }
        val credit = store.find("credits", creditId) ?: error("Credit not found")
        val borrowerMobile = credit.optString("borrowerMobile")
        val lenderMobile = credit.optString("lenderMobile")
        val initiator = when {
            mobile == borrowerMobile -> "BORROWER"
            mobile == lenderMobile -> "LENDER"
            else -> error("This account is not a party to the selected credit")
        }
        return requestInternal(credit, creditId, initiator, amount, date, method, reference, evidenceId)
    }

    /** Legacy-compatible entry point retained, but it now also verifies the current account. */
    fun request(creditId:String, initiatedBy:String, amount:Double, date:String, method:String, reference:String, evidenceId:String?=null):String {
        val accounts = context.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE)
        val mobile = accounts.getString("current_mobile", "").orEmpty()
        val credit = store.find("credits", creditId) ?: error("Credit not found")
        val role = initiatedBy.uppercase()
        val allowed = (role == "BORROWER" && mobile == credit.optString("borrowerMobile")) ||
                (role == "LENDER" && mobile == credit.optString("lenderMobile"))
        require(allowed) { "Current account is not authorised to initiate this repayment" }
        return requestInternal(credit, creditId, role, amount, date, method, reference, evidenceId)
    }

    private fun requestInternal(credit:JSONObject, creditId:String, initiator:String, amount:Double, date:String, method:String, reference:String, evidenceId:String?):String {
        require(amount>0 && creditId.isNotBlank() && date.isNotBlank())
        val outstanding = credit.optDouble("outstanding", credit.optDouble("totalPayable", credit.optDouble("amount",0.0)))
        require(amount <= outstanding + 0.01) { "Repayment exceeds outstanding amount" }
        val counterparty = if(initiator=="BORROWER") "LENDER" else "BORROWER"
        val id="RP-${System.currentTimeMillis()}";val otp=(100000+Random.nextInt(900000)).toString()
        store.add("repayment_requests",JSONObject().apply{
            put("id",id);put("creditId",creditId);put("initiatedBy",initiator);put("counterparty",counterparty)
            put("amount",amount);put("date",date);put("method",method);put("reference",reference);put("evidenceId",evidenceId?:"")
            put("otp",otp);put("status","COUNTERPARTY_OTP_PENDING");put("createdAt",System.currentTimeMillis())
        })
        return id
    }

    fun confirm(requestId:String,otp:String,expectedOtp:String):Boolean {
        val r=store.find("repayment_requests",requestId)?:return false
        if(r.optString("status")!="COUNTERPARTY_OTP_PENDING"||otp.length!=6||otp!=expectedOtp)return false
        val creditId=r.optString("creditId");val credit=store.find("credits",creditId)?:return false
        val requested=r.optDouble("amount",0.0);val oldOutstanding=credit.optDouble("outstanding",credit.optDouble("totalPayable",credit.optDouble("amount",0.0)))
        if(requested<=0||requested>oldOutstanding+0.01)return false
        val newOutstanding=(oldOutstanding-requested).coerceAtLeast(0.0)
        val ledgerId="LED-${System.currentTimeMillis()}";r.put("status","CONFIRMED");r.put("consentedAt",System.currentTimeMillis());r.put("confirmedLedgerId",ledgerId);store.replace("repayment_requests",r)
        store.add("repayments",JSONObject().apply{put("id",ledgerId);put("requestId",requestId);put("creditId",creditId);put("counterparty",r.optString("counterparty"));put("amount",requested);put("date",r.optString("date"));put("method",r.optString("method"));put("direction",if(r.optString("initiatedBy")=="BORROWER")"PAYABLE" else "RECEIVABLE");put("reference",r.optString("reference"));put("status","CONFIRMED");put("consent","OTP_VERIFIED");put("consentedAt",r.optLong("consentedAt"));put("createdAt",System.currentTimeMillis())})
        credit.put("outstanding",newOutstanding);credit.put("status",if(newOutstanding<=0.01)"SETTLED" else "ACTIVE");credit.put("lastRepaymentAt",System.currentTimeMillis());store.replace("credits",credit)
        store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",requestId);put("event","COUNTERPARTY_OTP_CONSENTED_AND_LEDGER_POSTED");put("at",System.currentTimeMillis())})
        return true
    }
}
