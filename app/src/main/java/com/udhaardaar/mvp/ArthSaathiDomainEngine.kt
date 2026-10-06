package com.udhaardaar.mvp

import org.json.JSONArray
import org.json.JSONObject
import kotlin.math.max

/** Independent business engine for the consolidated ArthSaathi build. */
object ArthSaathiDomainEngine {
    data class Result(val ok:Boolean,val message:String,val recordId:String?=null)

    fun registerCredit(input: Map<String,String>): Result {
        val party=input["party"].orEmpty().trim()
        val amount=input["amount"]?.toDoubleOrNull()
        val nature=input["nature"].orEmpty()
        if (party.isBlank()) return Result(false,"Counterparty is required")
        if (input["mobile"].orEmpty().length != 10) return Result(false,"Mobile must contain exactly 10 digits")
        if (amount == null || amount <= 0) return Result(false,"Amount must be greater than zero")
        if (nature.isBlank()) return Result(false,"Nature of credit is required")
        if (nature.contains("Rental",true) && (input["leaseDocument"].orEmpty().isBlank())) {
            return Result(false,"Lease agreement/document is required for rental/lease")
        }
        val id="CR-"+System.currentTimeMillis()
        val o=JSONObject().apply {
            put("id",id); put("type","CREDIT"); put("party",party); put("mobile",input["mobile"])
            put("nature",nature); put("amount",amount); put("roi",if(nature.contains("Rental",true)) 0.0 else input["roi"]?.toDoubleOrNull()?:0.0)
            put("method",input["method"].orEmpty()); put("repaymentMethod",input["repaymentMethod"].orEmpty())
            put("dueDate",input["dueDate"].orEmpty()); put("guarantor",input["guarantor"].orEmpty())
            put("document",input["document"].orEmpty()); put("leaseDocument",input["leaseDocument"].orEmpty())
            put("paid",0.0); put("status","PENDING_CONSENT"); put("consentStatus","PENDING")
            put("createdAt",System.currentTimeMillis())
        }
        ArthSaathiDataStore.append(o)
        return Result(true,"Credit registered pending consent",id)
    }

    fun recordRepayment(accountId:String, amount:Double, authorized:Boolean): Result {
        if (!authorized) return Result(false,"Authorization/OTP is required")
        if (amount <= 0) return Result(false,"Repayment must be greater than zero")
        val all=ArthSaathiDataStore.records()
        for(i in 0 until all.length()){
            val o=all.getJSONObject(i)
            if(o.optString("id") != accountId || o.optString("type")!="CREDIT") continue
            val outstanding=max(0.0,o.optDouble("amount")-o.optDouble("paid"))
            if(amount > outstanding) return Result(false,"Repayment exceeds outstanding amount")
            val paid=o.optDouble("paid")+amount
            o.put("paid",paid); o.put("lastRepayment",System.currentTimeMillis())
            o.put("consentStatus","VERIFIED")
            if(paid >= o.optDouble("amount")) o.put("status","CLOSED") else o.put("status","ACTIVE")
            ArthSaathiDataStore.replace(all)
            return Result(true,"Repayment recorded",accountId)
        }
        return Result(false,"Account not found")
    }

    fun outstanding(accountId:String):Double {
        val all=ArthSaathiDataStore.records()
        for(i in 0 until all.length()) {
            val o=all.getJSONObject(i)
            if(o.optString("id")==accountId) return max(0.0,o.optDouble("amount")-o.optDouble("paid"))
        }
        return 0.0
    }

    fun emi(principal:Double, annualRate:Double, months:Int):Double {
        require(principal >= 0 && annualRate >= 0 && months > 0)
        if(annualRate == 0.0) return principal/months
        val r=annualRate/1200.0
        return principal*r*(1+r).let { it.pow(months) }/((1+r).pow(months)-1)
    }

    private fun Double.pow(n:Int):Double = Math.pow(this,n.toDouble())

    fun opportunityCost(amount:Double,currentReturn:Double,alternativeReturn:Double):Double =
        amount*(alternativeReturn-currentReturn)/100.0

    fun variance(sanctioned:Double,actual:Double):Double = actual-sanctioned

    fun netPosition(assets:Double,portfolio:Double,liabilities:Double):Double =
        assets+portfolio-liabilities

    fun groupShare(total:Double,members:Int):Double {
        require(total >= 0 && members > 0)
        return total/members
    }

    fun validatePan(pan:String):Boolean =
        Regex("[A-Z]{5}[0-9]{4}[A-Z]").matches(pan.uppercase())

    fun validateGstin(gstin:String):Boolean =
        Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]").matches(gstin.uppercase())

    fun validatePin(pin:String):Boolean = Regex("[1-9][0-9]{5}").matches(pin)

    fun mis(records:JSONArray):Map<String,Double> {
        var credit=0.0; var repayment=0.0; var assets=0.0; var liabilities=0.0; var benefits=0.0; var revenue=0.0; var groupDues=0.0
        for(i in 0 until records.length()) {
            val o=records.getJSONObject(i)
            when(o.optString("type")) {
                "CREDIT" -> { credit += o.optDouble("amount"); repayment += o.optDouble("paid") }
                "ASSET","PORTFOLIO" -> assets += o.optDouble("value",o.optDouble("currentValue"))
                "LIABILITY" -> liabilities += o.optDouble("outstanding",o.optDouble("value"))
                "BENEFIT" -> benefits += o.optDouble("received",o.optDouble("value"))
                "REVENUE" -> if(o.optString("status")=="SUCCESS") revenue += o.optDouble("amount",o.optDouble("charge"))
                "GROUP_EXPENSE" -> groupDues += o.optDouble("outstanding")
            }
        }
        return mapOf(
            "CREDIT_GIVEN" to credit, "REPAYMENT" to repayment,
            "OUTSTANDING" to max(0.0,credit-repayment), "ASSETS" to assets,
            "LIABILITIES" to liabilities, "NET_POSITION" to assets-liabilities,
            "BENEFITS" to benefits, "GROUP_DUES" to groupDues, "REVENUE" to revenue
        )
    }
}
