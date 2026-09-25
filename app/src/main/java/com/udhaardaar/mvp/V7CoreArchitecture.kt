package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import android.net.Uri
import java.util.UUID
import java.util.Locale
import kotlin.math.max

/** ArthSaathi V7 non-regulatory canonical core. */
object V7Core {
    const val VERSION = "7.0"
    const val TAGLINE = "Navigate Your Financial Journey"
    const val PROMISE = "Your Asset. Your Record. Your Right."

    object Keys {
        const val PEOPLE="v7_people"; const val BUSINESSES="v7_businesses"; const val RELATIONSHIPS="v7_relationships"
        const val ADDRESSES="v7_addresses"; const val ASSETS="v7_assets"; const val LIABILITIES="v7_liabilities"
        const val HOLDINGS="v7_holdings"; const val MARKET="v7_market"; const val SCENARIOS="v7_scenarios"
        const val DOCUMENTS="v7_documents"; const val POLICIES="v7_policies"; const val NOMINEES="v7_nominees"
        const val CLAIMS="v7_claims"; const val LEGAL="v7_legal"; const val PROFESSIONALS="v7_professionals"
        const val ALERTS="v7_alerts"; const val CONSENTS="v7_consents"; const val AUDIT="v7_audit"
        const val INVOICES="v7_invoices"; const val PAYMENTS="v7_payments"; const val REVENUE="v7_revenue"
        const val SERVICES="v7_services"; const val PORTFOLIOS="v7_portfolios"; const val TTMM="v7_ttmm"
        const val QR="v7_qr"; const val TRADE="v7_trade"; const val FORMAL="v7_formal"; const val WILL="v7_will"
        const val REPAYMENTS="v7_repayments"; const val FUNDING="v7_funding"
    }

    fun user(c: Context) = c.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE)
        .getString("current_mobile","")?.trim().orEmpty().ifBlank { "self" }
    fun id(prefix:String)= prefix + "-" + UUID.randomUUID()
    fun now()=System.currentTimeMillis()
    fun store(c:Context)=V7LocalStore(c.applicationContext)
    fun all(c:Context,key:String)=store(c).all(key)
    fun find(c:Context,key:String,id:String)=all(c,key).firstOrNull{it.optString("id")==id}
    fun add(c:Context,key:String,o:org.json.JSONObject){
        o.put("ownerUserId",user(c));o.put("updatedAt",now());store(c).add(key,o)
        audit(c,"CREATE",key,o.optString("id"),"");publish(key,o.optString("id"))
    }
    fun replace(c:Context,key:String,o:org.json.JSONObject){
        if(o.optString("ownerUserId").isBlank())o.put("ownerUserId",user(c))
        o.put("updatedAt",now());store(c).replace(key,o);audit(c,"UPDATE",key,o.optString("id"),"");publish(key,o.optString("id"))
    }
    fun publish(entity:String,id:String){
        val mapped = when(entity) {
            Keys.PEOPLE -> V7Architecture.Event.PERSON_CHANGED
            Keys.RELATIONSHIPS -> V7Architecture.Event.RELATIONSHIP_CHANGED
            Keys.ADDRESSES -> V7Architecture.Event.ADDRESS_CHANGED
            Keys.REPAYMENTS -> V7Architecture.Event.REPAYMENT_CHANGED
            Keys.PAYMENTS -> V7Architecture.Event.REPAYMENT_CHANGED
            Keys.ASSETS -> V7Architecture.Event.ASSET_CHANGED
            Keys.LIABILITIES -> V7Architecture.Event.LIABILITY_CHANGED
            Keys.DOCUMENTS -> V7Architecture.Event.DOCUMENT_CHANGED
            Keys.CONSENTS -> V7Architecture.Event.CONSENT_GRANTED
            Keys.POLICIES -> V7Architecture.Event.POLICY_CHANGED
            Keys.CLAIMS -> V7Architecture.Event.CLAIM_CHANGED
            Keys.NOMINEES, Keys.WILL -> V7Architecture.Event.NOMINEE_CHANGED
            Keys.HOLDINGS -> V7Architecture.Event.HOLDING_CHANGED
            Keys.FUNDING -> V7Architecture.Event.FUNDING_CHANGED
            Keys.ALERTS -> V7Architecture.Event.ALERT_CREATED
            else -> V7Architecture.Event.DOCUMENT_CHANGED
        }
        V7Architecture.Events.publish(V7Architecture.EventRecord(mapped,id,userPlaceholder()))
    }

    private fun userPlaceholder():String = "V7_CORE"
    fun audit(c:Context,action:String,entity:String,entityId:String,detail:String){
        store(c).add(Keys.AUDIT,org.json.JSONObject().apply{
            put("id",id("AUD"));put("ownerUserId",user(c));put("action",action);put("entity",entity)
            put("entityId",entityId);put("detail",detail);put("timestamp",now())
        })
    }
    fun consent(c:Context,subjectId:String,purpose:String,documentVersion:String,verified:Boolean)=org.json.JSONObject().apply{
        put("id",id("CONS"));put("subjectId",subjectId);put("purpose",purpose);put("documentVersion",documentVersion)
        put("verified",verified);put("status",if(verified)"GRANTED" else "PENDING");put("createdAt",now());put("withdrawn",false)
    }.also{add(c,Keys.CONSENTS,it)}
    fun hasConsent(c:Context,subjectId:String,purpose:String)=all(c,Keys.CONSENTS).any{
        it.optString("subjectId")==subjectId&&it.optString("purpose")==purpose&&it.optBoolean("verified")&&
            it.optString("status")=="GRANTED"&&!it.optBoolean("withdrawn")&&it.optLong("expiresAt",Long.MAX_VALUE)>now()
    }
    fun metrics(c:Context):org.json.JSONObject{
        val a=all(c,Keys.ASSETS);val h=all(c,Keys.HOLDINGS);val l=all(c,Keys.LIABILITIES);val r=all(c,Keys.RELATIONSHIPS)
        val p=all(c,Keys.PAYMENTS);val pol=all(c,Keys.POLICIES);val claims=all(c,Keys.CLAIMS);val docs=all(c,Keys.DOCUMENTS)
        val assets=a.filter{it.optString("status","ACTIVE") !in setOf("SOLD","DISPOSED","TRANSFERRED")}.sumOf{it.optDouble("currentValue",it.optDouble("value",0.0))}
        val invested=h.sumOf{it.optDouble("invested",it.optDouble("cost",0.0))}
        val current=h.sumOf{it.optDouble("currentValue",it.optDouble("value",0.0))}
        val liabilities=l.sumOf{it.optDouble("outstanding",it.optDouble("amount",0.0))}
        val receivable=r.filter{it.optString("direction")=="RECEIVABLE"&&it.optString("status")!="CLOSED"}.sumOf{it.optDouble("outstanding",0.0)}
        val payable=r.filter{it.optString("direction")=="PAYABLE"&&it.optString("status")!="CLOSED"}.sumOf{it.optDouble("outstanding",0.0)}
        return org.json.JSONObject().apply{
            put("assets",assets);put("invested",invested);put("portfolioValue",current);put("portfolioGain",current-invested)
            // Portfolio holdings are financial assets; do not double-count them in net worth.
            put("liabilities",liabilities);put("netWorth",assets+receivable-payable-liabilities);put("receivables",receivable);put("payables",payable)
            put("activeCredits",r.count{it.optString("status")!="CLOSED"});put("policies",pol.size);put("claimsPending",claims.count{it.optString("status") !in setOf("CLOSED","RESOLVED")})
            put("documents",docs.size);put("payments",p.sumOf{it.optDouble("amount",0.0)})
        }
    }
}

object V7Records {
    fun person(c:Context,name:String,mobile:String="",pan:String="",aadhaar:String="",gstin:String="")=org.json.JSONObject().apply{
        put("id",V7Core.id("PERSON"));put("name",name.trim());put("mobile",mobile.trim());put("pan",pan.trim().uppercase())
        put("aadhaar",aadhaar.trim());put("gstin",gstin.trim().uppercase());put("createdAt",V7Core.now())
    }.also{V7Core.add(c,V7Core.Keys.PEOPLE,it)}
    fun address(c:Context,ownerId:String,label:String,address:String,pin:String,state:String="",district:String="",city:String="",source:String="MANUAL",lat:Double=0.0,lon:Double=0.0)=org.json.JSONObject().apply{
        put("id",V7Core.id("ADDR"));put("ownerId",ownerId);put("label",label);put("address",address);put("pin",pin);put("state",state);put("district",district);put("city",city);put("source",source);put("latitude",lat);put("longitude",lon);put("isPrimary",false)
    }.also{V7Core.add(c,V7Core.Keys.ADDRESSES,it)}
    fun asset(c:Context,ownerId:String,type:String,description:String,value:Double,documentId:String="",nomineeId:String="")=org.json.JSONObject().apply{
        put("id",V7Core.id("ASSET"));put("ownerId",ownerId);put("type",type);put("description",description);put("value",value);put("currentValue",value);put("documentId",documentId);put("nomineeId",nomineeId);put("status","ACTIVE")
    }.also{V7Core.add(c,V7Core.Keys.ASSETS,it)}
    fun liability(c:Context,ownerId:String,type:String,amount:Double,outstanding:Double,rate:Double=0.0)=org.json.JSONObject().apply{
        put("id",V7Core.id("LIAB"));put("ownerId",ownerId);put("type",type);put("amount",amount);put("outstanding",outstanding);put("rate",rate);put("status","ACTIVE")
    }.also{V7Core.add(c,V7Core.Keys.LIABILITIES,it)}
    fun holding(c:Context,portfolioId:String,instrument:String,category:String,invested:Double,current:Double,risk:String="",liquidity:String="",lockIn:String="")=org.json.JSONObject().apply{
        put("id",V7Core.id("HOLD"));put("portfolioId",portfolioId);put("instrument",instrument);put("category",category);put("invested",invested);put("currentValue",current);put("risk",risk);put("liquidity",liquidity);put("lockIn",lockIn)
    }.also{V7Core.add(c,V7Core.Keys.HOLDINGS,it)}
    fun relationship(c:Context,partyId:String,type:String,direction:String,amount:Double,roi:Double,repayment:String,purpose:String)=org.json.JSONObject().apply{
        put("id",V7Core.id("REL"));put("partyId",partyId);put("type",type);put("direction",direction);put("amount",amount);put("outstanding",amount);put("roiPercent",roi);put("repaymentStructure",repayment);put("purpose",purpose);put("status","ACTIVE");put("consentRequired",true)
    }.also{V7Core.add(c,V7Core.Keys.RELATIONSHIPS,it)}
    fun repayment(c:Context,relationshipId:String,amount:Double,principal:Double,interest:Double,method:String,consentVerified:Boolean)=
        org.json.JSONObject().apply{
            put("id",V7Core.id("REPAY"));put("relationshipId",relationshipId);put("amount",amount)
            put("principal",principal);put("interest",interest);put("method",method.uppercase())
            put("consentVerified",consentVerified);put("timestamp",V7Core.now())
        }.also { repayment ->
            val relationship = V7Core.find(c,V7Core.Keys.RELATIONSHIPS,relationshipId) ?: return@also
            val consentRequired = relationship.optBoolean("consentRequired", true)
            val partyId = relationship.optString("partyId").orEmpty()
            val activeConsent = partyId.isNotBlank() && V7Core.hasConsent(c, partyId, "REPAYMENT_UPDATE")
            val validMethod = repayment.optString("method") in setOf("CASH","UPI","NEFT","BANK_TRANSFER","NACH","CHEQUE","OTHER")
            val validAmounts = amount > 0.0 && principal >= 0.0 && interest >= 0.0 && principal + interest <= amount + 0.005
            val outstanding = relationship.optDouble("outstanding",relationship.optDouble("amount",0.0))
            val validPrincipal = principal <= outstanding + 0.005
            if (!consentVerified || (consentRequired && !activeConsent) || !validMethod || !validAmounts || !validPrincipal) return@also
            val newOutstanding = (outstanding-principal).coerceAtLeast(0.0)
            relationship.put("outstanding",newOutstanding)
            relationship.put("lastRepaymentAt",V7Core.now())
            relationship.put("status",if(newOutstanding<=0.005)"CLOSED" else "ACTIVE")
            V7Core.replace(c,V7Core.Keys.RELATIONSHIPS,relationship)
            V7Core.add(c,V7Core.Keys.REPAYMENTS,repayment)
        }
}

object V7PortfolioEngine {
    fun createPortfolio(c:Context,name:String,riskProfile:String,constitution:String)=org.json.JSONObject().apply{
        put("id",V7Core.id("PORT"));put("name",name);put("riskProfile",riskProfile);put("constitution",constitution)
    }.also{V7Core.add(c,V7Core.Keys.PORTFOLIOS,it)}
    fun recordMarketData(c:Context,instrument:String,source:String,value:Double,capturedAt:Long=V7Core.now(),freshnessMinutes:Int=60)=org.json.JSONObject().apply{
        put("id",V7Core.id("MKT"));put("instrument",instrument);put("source",source);put("value",value);put("capturedAt",capturedAt);put("freshnessMinutes",freshnessMinutes);put("freshUntil",capturedAt+freshnessMinutes*60000L)
    }.also{V7Core.add(c,V7Core.Keys.MARKET,it)}
    fun opportunityCost(currentAmount:Double,currentReturnPercent:Double,alternativeReturnPercent:Double,years:Double,costPercent:Double=0.0,exitCost:Double=0.0,taxCost:Double=0.0)=org.json.JSONObject().apply{
        val current=currentAmount*Math.pow(1+currentReturnPercent/100.0,years);val alternative=currentAmount*Math.pow(1+alternativeReturnPercent/100.0,years)
        val costs=exitCost+taxCost+alternative*costPercent/100.0
        put("currentProjectedValue",current);put("alternativeProjectedValue",alternative);put("grossOpportunityCost",max(0.0,alternative-current));put("estimatedCosts",costs);put("netOpportunityDifference",alternative-current-costs);put("years",years);put("calculatedAt",V7Core.now())
    }
    fun scenario(c:Context,portfolioId:String,label:String,currentReturn:Double,alternativeReturn:Double,years:Double,exitCost:Double,taxCost:Double)=opportunityCost(1.0,currentReturn,alternativeReturn,years,0.0,exitCost,taxCost).apply{
        put("id",V7Core.id("SCEN"));put("portfolioId",portfolioId);put("label",label);put("status","ANALYSIS_ONLY")
    }.also{V7Core.add(c,V7Core.Keys.SCENARIOS,it)}
}

object V7LocationEngine {
    data class Result(val pinValid:Boolean,val state:String,val district:String,val city:String,val postOffice:String,val message:String)
    fun validatePin(pin:String)=pin.matches(Regex("[1-9][0-9]{5}"))
    fun resolvePin(pin:String):Result=if(!validatePin(pin))Result(false,"","","","","Enter a valid 6-digit PIN code.") else Result(true,"","","","","PIN format validated. Confirm locality from the postal/map source before saving.")
    fun mapIntent(context:Context,query:String):Intent=Intent(Intent.ACTION_VIEW,Uri.parse("geo:0,0?q="+Uri.encode(query)))
}

object V7LegalEngine {
    fun professional(c:Context,name:String,city:String,state:String,domain:String,court:String,language:String,mode:String,verification:String)=org.json.JSONObject().apply{
        put("id",V7Core.id("PRO"));put("name",name);put("city",city);put("state",state);put("domain",domain);put("court",court);put("language",language);put("consultationMode",mode);put("verificationStatus",verification)
    }.also{V7Core.add(c,V7Core.Keys.PROFESSIONALS,it)}
    fun search(c:Context,query:String="",city:String="",domain:String="")=V7Core.all(c,V7Core.Keys.PROFESSIONALS).filter{
        (query.isBlank()||it.optString("name").contains(query,true)||it.optString("domain").contains(query,true))&&(city.isBlank()||it.optString("city").equals(city,true))&&(domain.isBlank()||it.optString("domain").equals(domain,true))
    }
    fun claim(c:Context,assetId:String,claimant:String,nomineeOrHeir:String,institution:String,amount:Double)=org.json.JSONObject().apply{
        put("id",V7Core.id("CLAIM"));put("assetId",assetId);put("claimant",claimant);put("nomineeOrHeir",nomineeOrHeir);put("institution",institution);put("amount",amount);put("status","PREPARING")
    }.also{V7Core.add(c,V7Core.Keys.CLAIMS,it)}
}

object V7RevenueEngine {
    enum class PaymentStatus{CREATED,SUCCESS,FAILED,REFUNDED,RECONCILED}
    fun service(c:Context,name:String,price:Double,currency:String="INR",active:Boolean=true)=org.json.JSONObject().apply{
        put("id",V7Core.id("SVC"));put("name",name);put("price",price);put("currency",currency);put("active",active)
    }.also{V7Core.add(c,V7Core.Keys.SERVICES,it)}
    fun invoice(c:Context,serviceId:String,amount:Double,description:String)=org.json.JSONObject().apply{
        put("id",V7Core.id("INV"));put("serviceId",serviceId);put("amount",amount);put("description",description);put("status","ISSUED");put("createdAt",V7Core.now())
    }.also{V7Core.add(c,V7Core.Keys.INVOICES,it)}
    fun payment(c:Context,invoiceId:String,amount:Double,gateway:String,reference:String,status:PaymentStatus)=org.json.JSONObject().apply{
        put("id",V7Core.id("PAY"));put("invoiceId",invoiceId);put("amount",amount);put("gateway",gateway);put("reference",reference);put("status",status.name);put("timestamp",V7Core.now())
    }.also{V7Core.add(c,V7Core.Keys.PAYMENTS,it)}
    fun reconcile(c:Context,paymentId:String,settlementReference:String){
        V7Core.find(c,V7Core.Keys.PAYMENTS,paymentId)?.apply{
            put("status",PaymentStatus.RECONCILED.name);put("settlementReference",settlementReference);put("reconciledAt",V7Core.now());V7Core.replace(c,V7Core.Keys.PAYMENTS,this)
            V7Core.add(c,V7Core.Keys.REVENUE,org.json.JSONObject().apply{put("id",V7Core.id("REV"));put("paymentId",paymentId);put("amount",this@apply.optDouble("amount"));put("settlementReference",settlementReference)})
        }
    }
}

object V7AlertEngine {
    fun create(c:Context,type:String,message:String,entityId:String="",priority:String="NORMAL")=org.json.JSONObject().apply{
        put("id",V7Core.id("ALERT"));put("type",type);put("message",message);put("entityId",entityId);put("priority",priority);put("status","OPEN");put("createdAt",V7Core.now())
    }.also{V7Core.add(c,V7Core.Keys.ALERTS,it)}
    fun evaluate(c:Context){
        V7Core.all(c,V7Core.Keys.LIABILITIES).filter{it.optDouble("outstanding")>0&&it.optString("dueDate").isNotBlank()}.forEach{create(c,"LIABILITY_DUE","Payment due: "+it.optString("type"),it.optString("id"),"HIGH")}
        V7Core.all(c,V7Core.Keys.POLICIES).filter{it.optString("renewalDate").isNotBlank()}.forEach{create(c,"INSURANCE_RENEWAL","Insurance renewal: "+it.optString("policyNumber"),it.optString("id"),"HIGH")}
    }
}
