package com.udhaardaar.mvp

import android.content.Context
import android.net.Uri
import org.json.JSONObject

/** Compatibility facade over the connected V6.2 domain layer. */
object ArthSaathiV62Core {
    const val COUNTERPARTIES=V62Store.COUNTERPARTIES; const val RELATIONSHIPS=V62Store.RELATIONSHIPS; const val REPAYMENTS=V62Store.REPAYMENTS
    const val DOCUMENTS=V62Store.DOCUMENTS; const val INSURANCE=V62Store.INSURANCE; const val ASSETS=V62Store.ASSETS; const val TTMM=V62Store.TTMM_EXPENSES
    const val MIS="v62_mis"; const val SAVINGS=V62Store.SAVINGS; const val CONSENTS=V62Store.CONSENTS
    fun id(prefix:String)=V62Store.id(prefix)
    fun saveDocument(context:Context,uri:Uri,category:String,ownerId:String="self")=V62Documents.retain(context,uri,category,"").apply{put("ownerId",ownerId)}
    fun recordSavings(context:Context,source:String,saved:Double,note:String){
        if(saved<=0)return
        val id=id("SAVE")
        V62Store.add(context,V62Store.SAVINGS,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(context));put("source",source);put("amount",saved);put("note",note);put("date",System.currentTimeMillis())})
        V62EventBus.publish(V62Event(V62Events.ALERT_CREATED,id,metadata=mapOf("category" to "SAVINGS")))
    }
}
interface ArthSaathiDocumentIntelligence{fun classify(category:String,extractedText:String):String;fun extract(category:String,extractedText:String):JSONObject}
class RuleBasedDocumentIntelligence:ArthSaathiDocumentIntelligence{override fun classify(category:String,extractedText:String)=category;override fun extract(category:String,extractedText:String)=JSONObject().apply{put("sourceCategory",category);put("rawTextAvailable",extractedText.isNotBlank());put("verificationRequired",true);put("confidence",0.0)}}
