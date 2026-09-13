package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import java.util.UUID

/** Shared V6.2 persistence and domain boundary. */
object V62Store {
    const val USERS="v62_users"
    const val COUNTERPARTIES="v62_counterparties"
    const val RELATIONSHIPS="v62_relationships"
    const val REPAYMENTS="v62_repayments"
    const val CONSENTS="v62_consents"
    const val DOCUMENTS="v62_documents"
    const val ASSETS="v62_assets"
    const val INSURANCE="v62_insurance"
    const val RENTALS="v62_rentals"
    const val TTMM_GROUPS="v62_ttmm_groups"
    const val TTMM_EXPENSES="v62_ttmm_expenses"
    const val TTMM_SETTLEMENTS="v62_ttmm_settlements"
    const val NOMINEES="v62_nominees"
    const val WILLS="v62_wills"
    const val CLAIMS="v62_claims"
    const val SAVINGS="v62_savings"
    const val ALERTS="v62_alerts"
    fun id(prefix:String)=prefix+"-"+UUID.randomUUID().toString()
    fun store(c:Context)=V5LocalStore(c)
    fun add(c:Context,key:String,o:JSONObject)=store(c).add(key,o)
    fun all(c:Context,key:String)=store(c).all(key)
    fun replace(c:Context,key:String,o:JSONObject)=store(c).replace(key,o)
}

object V62Relationships {
    fun createCounterparty(c:Context, relationshipId:String, role:String, name:String, mobile:String="", pan:String="", aadhaar:String="", gstin:String="")=JSONObject().apply{
        put("id",V62Store.id("CP"));put("relationshipId",relationshipId);put("role",role);put("name",name.trim());put("mobile",mobile.trim());put("pan",pan.trim().uppercase());put("aadhaar",aadhaar.trim());put("gstin",gstin.trim().uppercase());put("createdAt",System.currentTimeMillis())
    }.also{V62Store.add(c,V62Store.COUNTERPARTIES,it)}
    fun createRelationship(c:Context,type:String,role:String,counterpartyId:String)=JSONObject().apply{
        put("id",V62Store.id("REL"));put("type",type);put("userRole",role);put("counterpartyId",counterpartyId);put("status","DRAFT");put("createdAt",System.currentTimeMillis())
    }.also{V62Store.add(c,V62Store.RELATIONSHIPS,it)}
}

object V62MisEngine {
    fun metrics(c:Context):JSONObject {
        val a=V62Store.all(c,V62Store.ASSETS); val r=V62Store.all(c,V62Store.REPAYMENTS); val s=V62Store.all(c,V62Store.SAVINGS); val rel=V62Store.all(c,V62Store.RELATIONSHIPS)
        return JSONObject().apply {
            put("assetValue",a.sumOf{it.optDouble("value",0.0)})
            put("interestReceived",r.sumOf{it.optDouble("interest",0.0)})
            put("charges",a.sumOf{it.optDouble("charges",0.0)}+r.sumOf{it.optDouble("charges",0.0)})
            put("appSavings",s.sumOf{it.optDouble("amount",0.0)})
            put("idleFunds",a.filter{it.optBoolean("idle",false)}.sumOf{it.optDouble("value",0.0)})
            put("activeRelationships",rel.count{it.optString("status")!="CLOSED"})
        }
    }
}
