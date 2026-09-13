package com.udhaardaar.mvp

import android.content.Context
import android.net.Uri
import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID

/** Single V6.2 domain boundary. Screens write here; downstream modules read the same records. */
object V62Store {
    const val USERS="v62_users"; const val COUNTERPARTIES="v62_counterparties"; const val RELATIONSHIPS="v62_relationships"
    const val REPAYMENTS="v62_repayments"; const val CONSENTS="v62_consents"; const val DOCUMENTS="v62_documents"
    const val ASSETS="v62_assets"; const val INSURANCE="v62_insurance"; const val RENTALS="v62_rentals"
    const val TTMM_GROUPS="v62_ttmm_groups"; const val TTMM_EXPENSES="v62_ttmm_expenses"; const val TTMM_SETTLEMENTS="v62_ttmm_settlements"
    const val NOMINEES="v62_nominees"; const val WILLS="v62_wills"; const val CLAIMS="v62_claims"; const val SAVINGS="v62_savings"; const val ALERTS="v62_alerts"
    fun id(p:String)=p+"-"+UUID.randomUUID().toString()
    fun store(c:Context)=V5LocalStore(c)
    fun add(c:Context,key:String,o:JSONObject)=store(c).add(key,o)
    fun all(c:Context,key:String)=store(c).all(key)
    fun replace(c:Context,key:String,o:JSONObject)=store(c).replace(key,o)
}

enum class V62Event { RELATIONSHIP_CHANGED, REPAYMENT_CHANGED, DOCUMENT_ADDED, ASSET_CHANGED, POLICY_CHANGED, TTMM_EXPENSE_CHANGED, CONSENT_CHANGED, NOMINEE_CHANGED, CLAIM_CHANGED, WILL_CHANGED }

object V62EventBus {
    private val listeners=mutableListOf<(V62Event)->Unit>()
    fun subscribe(l:(V62Event)->Unit){ synchronized(listeners){listeners.add(l)} }
    fun publish(e:V62Event){ synchronized(listeners.toList()){listeners.toList().forEach{runCatching{it(e)}}} }
}

object V62Relationships {
    fun createCounterparty(c:Context, relationshipId:String, role:String, name:String, mobile:String="", pan:String="", aadhaar:String="", gstin:String=""):JSONObject {
        require(relationshipId.isNotBlank())
        return JSONObject().apply{put("id",V62Store.id("CP"));put("relationshipId",relationshipId);put("role",role);put("name",name.trim());put("mobile",mobile.trim());put("pan",pan.trim().uppercase());put("aadhaar",aadhaar.trim());put("gstin",gstin.trim().uppercase());put("createdAt",System.currentTimeMillis())}.also{V62Store.add(c,V62Store.COUNTERPARTIES,it)}
    }
    fun createRelationship(c:Context,type:String,role:String,counterpartyId:String):JSONObject=JSONObject().apply{put("id",V62Store.id("REL"));put("type",type);put("userRole",role);put("counterpartyId",counterpartyId);put("status","DRAFT");put("createdAt",System.currentTimeMillis())}.also{V62Store.add(c,V62Store.RELATIONSHIPS,it);V62EventBus.publish(V62Event.RELATIONSHIP_CHANGED)}
}

object V62Consent {
    fun record(c:Context,relationshipId:String,event:String,channel:String="OTP"):JSONObject=JSONObject().apply{put("id",V62Store.id("CONS"));put("relationshipId",relationshipId);put("event",event);put("channel",channel);put("status","VERIFIED");put("verifiedAt",System.currentTimeMillis())}.also{V62Store.add(c,V62Store.CONSENTS,it);V62EventBus.publish(V62Event.CONSENT_CHANGED)}
    fun has(c:Context,relationshipId:String,event:String)=V62Store.all(c,V62Store.CONSENTS).any{it.optString("relationshipId")==relationshipId&&it.optString("event")==event&&it.optString("status")=="VERIFIED"}
}

object V62Documents {
    fun retain(c:Context,uri:Uri,type:String,relationshipId:String=""):JSONObject=JSONObject().apply{put("id",V62Store.id("DOC"));put("uri",uri.toString());put("type",type);put("relationshipId",relationshipId);put("originalRetained",true);put("status","AI_REVIEW");put("createdAt",System.currentTimeMillis())}.also{V62Store.add(c,V62Store.DOCUMENTS,it);V62EventBus.publish(V62Event.DOCUMENT_ADDED)}
}

/** Deterministic fallback extractor. It is deliberately review-first; a production AI endpoint can implement the same contract. */
object V62DocumentIntelligence {
    data class Field(val name:String,val value:String,val confidence:Double,val critical:Boolean,val reason:String,val source:String="")
    private fun find(text:String,patterns:List<String>):String { for(p in patterns){Regex(p,RegexOption.IGNORE_CASE).find(text)?.let{return it.groupValues.last().trim()}};return "" }
    fun analyse(type:String,text:String):List<Field>{
        val t=text.replace("\n"," ")
        val out=mutableListOf<Field>()
        fun f(n:String,v:String,c:Double,k:Boolean,r:String){if(v.isNotBlank())out.add(Field(n,v,c,k,r))}
        when(type.uppercase()){
            "INSURANCE_POLICY"-> { f("Policy number",find(t,listOf("policy\\s*(?:no|number)\\s*[:#-]?\\s*([A-Z0-9/-]+)")),.90,true,"Policy identity");f("Insurer",find(t,listOf("(?:insurer|insurance company)\\s*[::-]?\\s*([A-Za-z0-9 &.-]+)")),.80,true,"Issuer");f("Sum assured",find(t,listOf("sum assured\\s*[:₹Rs. ]*([0-9,]+)")),.85,true,"Coverage");f("Next premium due",find(t,listOf("(?:next premium due|premium due)\\s*[: -]*([0-9A-Za-z/-]+)")),.80,true,"Payment obligation");f("Policy period",find(t,listOf("(?:policy period|period of insurance)\\s*[: -]*([^.;]{5,80})")),.70,true,"Validity");f("Exclusions / waiting period",find(t,listOf("(?:exclusions?|waiting period)\\s*[: -]*([^.;]{5,180})")),.65,true,"Coverage limitation") }
            "BANK_PASSBOOK"-> { f("Account number",find(t,listOf("(?:a/c|account)\\s*(?:no|number)?\\s*[:.-]?\\s*([0-9]{6,20})")),.95,true,"Bank account identifier");f("IFSC",find(t,listOf("IFSC\\s*[:.-]?\\s*([A-Z]{4}0[A-Z0-9]{6})")),.95,true,"Transfer identifier");f("Account holder",find(t,listOf("(?:name|account holder)\\s*[:.-]?\\s*([A-Za-z .]{3,60})")),.75,true,"Ownership") }
            "PROPERTY_PAPER"-> { f("Property description",find(t,listOf("(?:property|premises)\\s*(?:description)?\\s*[:.-]?\\s*(.{10,180})")),.65,true,"Property identity");f("Owner",find(t,listOf("(?:owner|registered owner)\\s*[:.-]?\\s*([A-Za-z .]{3,80})")),.80,true,"Ownership");f("Area",find(t,listOf("(?:area|plot area|built up area)\\s*[:.-]?\\s*([0-9., ]+\\s*(?:sq\\.?\\s*ft|sq\\.?\\s*m|acre|decimals)?)")),.75,true,"Property extent");f("Mortgage / encumbrance",find(t,listOf("(?:mortgage|encumbrance)\\s*[:.-]?\\s*(.{5,120})")),.60,true,"Title risk") }
            "LEASE_DEED","RENT_AGREEMENT"-> { f("Monthly rent",find(t,listOf("(?:monthly rent|rent)\\s*[:₹Rs. ]*([0-9,]+)")),.90,true,"Recurring obligation");f("Security deposit",find(t,listOf("security deposit\\s*[:₹Rs. ]*([0-9,]+)")),.85,true,"Financial exposure");f("Lock-in",find(t,listOf("lock[- ]in(?: period)?\\s*[: -]*([^.;]{3,60})")),.70,true,"Exit restriction");f("Notice period",find(t,listOf("notice period\\s*[: -]*([^.;]{3,60})")),.70,true,"Termination");f("Termination terms",find(t,listOf("termination\\s*(?:terms|clause)?\\s*[: -]*([^.;]{5,180})")),.65,true,"Exit terms") }
            "INVOICE"-> { f("Invoice number",find(t,listOf("invoice\\s*(?:no|number)\\s*[:#-]?\\s*([A-Z0-9/-]+)")),.95,true,"Invoice identity");f("GSTIN",find(t,listOf("GSTIN\\s*[:.-]?\\s*([0-9A-Z]{15})")),.95,true,"Tax identity");f("Total",find(t,listOf("(?:grand total|total)\\s*[:₹Rs. ]*([0-9,]+(?:\\.[0-9]{1,2})?)")),.90,true,"Payable amount") }
        }
        return out
    }
}

object V62MisEngine {
    fun metrics(c:Context):JSONObject { val a=V62Store.all(c,V62Store.ASSETS);val r=V62Store.all(c,V62Store.REPAYMENTS);val s=V62Store.all(c,V62Store.SAVINGS);val rel=V62Store.all(c,V62Store.RELATIONSHIPS);return JSONObject().apply{put("assetValue",a.sumOf{it.optDouble("value",0.0)});put("interestReceived",r.sumOf{it.optDouble("interest",0.0)});put("charges",a.sumOf{it.optDouble("charges",0.0)}+r.sumOf{it.optDouble("charges",0.0)});put("appSavings",s.sumOf{it.optDouble("amount",0.0)});put("idleFunds",a.filter{it.optBoolean("idle",false)}.sumOf{it.optDouble("value",0.0)});put("activeRelationships",rel.count{it.optString("status")!="CLOSED"})} }
}
