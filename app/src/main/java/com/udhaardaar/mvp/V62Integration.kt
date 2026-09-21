package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject
import java.util.Locale

/** Cross-module integration facade used by the V6.2 modules. */
object V62Integration {
    private fun store(c: Context) = V5LocalStore(c.applicationContext)
    fun currentUserId(c: Context): String = c.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE).getString("current_mobile", "")?.trim().orEmpty().ifBlank { "self" }

    fun findCounterparties(c: Context, query: String): List<JSONObject> {
        val q=query.trim().lowercase(Locale.getDefault()); val owner=currentUserId(c)
        return store(c).all(V62Store.COUNTERPARTIES).filter { p -> p.optString("createdBy", "") == owner && (q.isEmpty() || listOf("id","name","mobile","pan","aadhaar","gstin","profileId").any { key -> p.optString(key).lowercase(Locale.getDefault()).contains(q) }) }
    }

    fun createCounterparty(c:Context,name:String,mobile:String,pan:String="",aadhaar:String="",gstin:String="",transactionContext:String):JSONObject? {
        val n=name.trim(); val m=mobile.trim(); if(n.length<2||!m.matches(Regex("[6-9][0-9]{9}")) )return null
        findCounterparties(c,m).firstOrNull()?.let{return it}
        val r=JSONObject().apply{put("id",V62Store.id("CP"));put("profileId",V62Store.id("PROFILE"));put("name",n);put("mobile",m);put("pan",pan.trim().uppercase(Locale.getDefault()));put("aadhaar",aadhaar.trim());put("gstin",gstin.trim().uppercase(Locale.getDefault()));put("transactionContext",transactionContext.trim());put("createdBy",currentUserId(c));put("createdAt",System.currentTimeMillis())}
        store(c).add(V62Store.COUNTERPARTIES,r);V62EventBus.publish(V62Event(V62Events.PROFILE_CHANGED,r.optString("id")));return r
    }

    fun recordConsent(c:Context,subjectId:String,relationshipId:String,eventType:String,otpVerified:Boolean):JSONObject {
        val o=JSONObject().apply{put("id",V62Store.id("CONSENT"));put("ownerUserId",currentUserId(c));put("subjectId",subjectId);put("counterpartyId",subjectId);put("relationshipId",relationshipId);put("eventType",eventType);put("granted",otpVerified);put("otpVerified",otpVerified);put("status",if(otpVerified)"VERIFIED" else "PENDING");put("verifiedAt",if(otpVerified)System.currentTimeMillis() else 0L);put("createdAt",System.currentTimeMillis())}
        store(c).add(V62Store.CONSENTS,o);V62EventBus.publish(V62Event(V62Events.CONSENT_CHANGED,o.optString("id")));return o
    }

    /** Central consent check for protected cross-module disclosures/mutations. */
    fun hasVerifiedConsent(c:Context, subjectId:String, relationshipId:String, eventType:String):Boolean =
        store(c).all(V62Store.CONSENTS).any {
            it.optString("subjectId") == subjectId &&
            it.optString("relationshipId") == relationshipId &&
            it.optString("eventType") == eventType &&
            it.optBoolean("otpVerified", false) &&
            it.optString("status") == "VERIFIED"
        }

    fun relationship(c:Context,id:String):JSONObject? { val o=store(c).find(V62Store.RELATIONSHIPS,id) ?: return null; return o.takeIf { it.optString("ownerUserId", "") == currentUserId(c) } }
    fun publishRelationship(c:Context,relationship:JSONObject){ if(relationship.optString("ownerUserId", "") != currentUserId(c)) return; store(c).replace(V62Store.RELATIONSHIPS,relationship);V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED,relationship.optString("id"))) }
    fun publishDocument(c:Context,document:JSONObject){ if(document.optString("ownerUserId", "") != currentUserId(c)) return; store(c).replace(V62Store.DOCUMENTS,document);V62EventBus.publish(V62Event(V62Events.DOCUMENT_ADDED,document.optString("id"))) }
    fun addAlert(c:Context,type:String,message:String,entityId:String="",severity:String="INFO"){val a=JSONObject().apply{put("id",V62Store.id("ALERT"));put("ownerUserId",currentUserId(c));put("type",type);put("message",message);put("entityId",entityId);put("severity",severity);put("createdAt",System.currentTimeMillis());put("acknowledged",false)};store(c).add(V62Store.ALERTS,a);V62EventBus.publish(V62Event(V62Events.ALERT_CREATED,a.optString("id")))}

    /** Outstanding is derived from every persisted repayment in this account, not only repayments created by this device/user action. */
    fun recalculateRelationshipOutstanding(c:Context,relationshipId:String):Double{
        val rel=relationship(c,relationshipId)?:return 0.0
        val principal=rel.optDouble("amount",rel.optDouble("principal",0.0))
        val repayments=store(c).all(V62Store.REPAYMENTS).filter{it.optString("relationshipId")==relationshipId}
        val paid=repayments.sumOf{it.optDouble("principal",it.optDouble("amount",0.0))}
        val outstanding=(principal-paid).coerceAtLeast(0.0)
        rel.put("outstanding",outstanding);rel.put("status",if(outstanding<=0.0&&principal>0.0)"CLOSED" else "ACTIVE")
        publishRelationship(c,rel);return outstanding
    }

    /** Score is based on the complete account-scoped relationship/repayment record. */
    fun score(c:Context,counterpartyId:String):Int{
        val owner=currentUserId(c)
        val rels=store(c).all(V62Store.RELATIONSHIPS).filter{it.optString("counterpartyId")==counterpartyId && it.optString("ownerUserId", "") == owner}
        val relationshipIds=rels.map{it.optString("id")}.toSet()
        val reps=store(c).all(V62Store.REPAYMENTS).filter{it.optString("counterpartyId")==counterpartyId || it.optString("relationshipId") in relationshipIds}
        val principal=rels.sumOf{it.optDouble("amount",it.optDouble("principal",0.0))}
        val repaid=reps.sumOf{it.optDouble("principal",it.optDouble("amount",0.0))}
        val overdue=rels.sumOf{it.optDouble("overdueAmount",0.0)}
        val repaymentBonus=if(principal>0.0)((repaid/principal)*120).toInt()else 0
        val overduePenalty=(overdue/1000.0).toInt();val defaultPenalty=rels.count{it.optString("status")=="DEFAULTED"}*60
        return(750+repaymentBonus-overduePenalty-defaultPenalty).coerceIn(300,900)
    }

    fun projectTradeCredit(c:Context,relationshipId:String,counterpartyId:String,ocrText:String):JSONObject{
        val rel=relationship(c,relationshipId) ?: return JSONObject(); if(rel.optString("counterpartyId")!=counterpartyId)return JSONObject()
        val invoiceRegex=Regex("(?i)(?:invoice(?: no| number)?|inv\\.?\\s*no\\.?)[^A-Za-z0-9]*([A-Z0-9/-]{3,})")
        val amountRegex=Regex("(?i)(?:grand total|total amount|invoice value)[^0-9₹]*₹?\\s*([0-9][0-9,]*(?:\\.[0-9]{1,2})?)")
        val gstRegex=Regex("\\b([0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][0-9A-Z]Z[0-9A-Z])\\b")
        val invoice=invoiceRegex.find(ocrText)?.groupValues?.getOrNull(1).orEmpty();val amount=amountRegex.find(ocrText)?.groupValues?.getOrNull(1)?.replace(",","")?.toDoubleOrNull()?:0.0;val gst=gstRegex.find(ocrText)?.groupValues?.getOrNull(1).orEmpty()
        val id=V62Store.id("TRADE")
        val record=JSONObject().apply{put("id",id);put("ownerUserId",currentUserId(c));put("relationshipId",relationshipId);put("counterpartyId",counterpartyId);put("invoiceNumber",invoice);put("invoiceAmount",amount);put("gstin",gst);put("outstandingAmount",amount);put("ocrSource","DOCUMENT_INTELLIGENCE");put("sourceSystem","ARTHSAATHI");put("sourceRecordId",relationshipId);put("importedAt",System.currentTimeMillis());put("requiresReview",true)}
        store(c).add(V62Store.TRADE_CREDIT,record);V62EventBus.publish(V62Event(V62Events.TRADE_CREDIT_IMPORTED,id));return record
    }
}
