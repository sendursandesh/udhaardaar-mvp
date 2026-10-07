package com.udhaardaar.mvp

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import kotlin.math.pow
import kotlin.math.round

object ArthSaathiCoreEngine {
 data class Result(val ok:Boolean,val id:String?=null,val message:String="")
 data class Mis(val credits:Double,val repayments:Double,val outstanding:Double,val assets:Double,val liabilities:Double,val benefits:Double,val groupExpenses:Double,val revenue:Double)
 private fun id(p:String)=p+"-"+UUID.randomUUID().toString().take(8).uppercase()
 private fun n()=System.currentTimeMillis()
 private fun m(v:Double)=round(v*100)/100
 fun validateMobile(v:String)=v.matches(Regex("\\d{10}"))
 fun validatePan(v:String)=v.isBlank()||v.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))
 fun validateGstin(v:String)=v.isBlank()||v.matches(Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]"))

 fun createPerson(name:String,mobile:String="",role:String="",address:String=""):Result {
  require(name.isNotBlank());require(mobile.isEmpty()||validateMobile(mobile))
  val o=JSONObject().apply{put("id",id("PER"));put("type","PERSON");put("name",name.trim());put("mobile",mobile);put("role",role);put("address",address);put("createdAt",n());put("updatedAt",n())}
  save(o,"CREATE_PERSON");return Result(true,o.getString("id"),"Person saved")
 }
 fun createCredit(partyId:String?,partyName:String,nature:String,amount:Double,roi:Double,method:String,terms:String,dueDate:String="",guarantorId:String?=null,documentId:String?=null):Result {
  require(partyName.isNotBlank());require(nature.isNotBlank());require(amount>0);require(roi>=0)
  if(nature.contains("Rental",true)||nature.contains("Lease",true))require(roi==0.0)
  val o=JSONObject().apply{put("id",id("CR"));put("type","CREDIT");put("partyId",partyId?:"");put("party",partyName);put("nature",nature);put("amount",m(amount));put("roi",m(roi));put("method",method);put("terms",terms);put("dueDate",dueDate);put("guarantorId",guarantorId?:"");put("documentId",documentId?:"");put("paid",0.0);put("status","ACTIVE");put("consentStatus","PENDING");put("createdAt",n());put("updatedAt",n())}
  save(o,"CREATE_CREDIT");return Result(true,o.getString("id"),"Credit created")
 }
 fun requestConsent(recordId:String,actor:String):Result{val o=find(recordId)?:return Result(false,message="Record not found");o.put("consentStatus","REQUESTED");o.put("consentActor",actor);o.put("consentRequestedAt",n());replace(o);audit(recordId,"CONSENT_REQUESTED",actor);return Result(true,recordId,"Consent requested")}
 fun confirmConsent(recordId:String,otp:String,actor:String):Result{require(otp.matches(Regex("\\d{4,8}")));val o=find(recordId)?:return Result(false,message="Record not found");require(o.optString("consentStatus")=="REQUESTED");o.put("consentStatus","CONSENTED");o.put("consentActor",actor);o.put("consentAt",n());replace(o);audit(recordId,"CONSENT_CONFIRMED",actor);return Result(true,recordId,"Consent confirmed")}
 fun repayment(recordId:String,amount:Double,method:String,actor:String):Result{
  require(amount>0);val o=find(recordId)?:return Result(false,message="Credit not found");require(o.optString("type")=="CREDIT");require(o.optString("consentStatus")=="CONSENTED")
  val out=(o.optDouble("amount")-o.optDouble("paid")).coerceAtLeast(0.0);require(amount<=out+0.005)
  val tx=JSONObject().apply{put("id",id("TX"));put("type","REPAYMENT");put("recordId",recordId);put("amount",m(amount));put("method",method);put("actor",actor);put("createdAt",n())}
  save(tx,"REPAYMENT");o.put("paid",m(o.optDouble("paid")+amount));if(o.optDouble("paid")+0.005>=o.optDouble("amount"))o.put("status","CLOSED");o.put("updatedAt",n());replace(o);return Result(true,tx.getString("id"),"Repayment recorded")
 }
 fun emi(p:Double,rate:Double,months:Int):Double{require(p>0&&rate>=0&&months>0);if(rate==0.0)return m(p/months);val r=rate/1200;return m(p*r*(1+r).pow(months)/((1+r).pow(months)-1))}
 fun schedule(p:Double,rate:Double,months:Int):JSONArray{val a=JSONArray();var b=p;val e=emi(p,rate,months);val r=rate/1200;for(i in 1..months){val interest=if(r==0.0)0.0 else b*r;val principal=if(i==months)b else (e-interest).coerceAtLeast(0.0);b=(b-principal).coerceAtLeast(0.0);a.put(JSONObject().apply{put("installment",i);put("emi",m(e));put("interest",m(interest));put("principal",m(principal));put("balance",m(b))})};return a}
 fun saveModule(type:String,fields:Map<String,Any?>,links:Map<String,String> = emptyMap()):Result{val o=JSONObject().apply{put("id",id(type.take(8).uppercase()));put("type",type);put("createdAt",n());put("updatedAt",n())};fields.forEach{(k,v)->put(k,v)};links.forEach{(k,v)->put(k,v)};save(o,"CREATE_$type");return Result(true,o.getString("id"),"$type saved")}
 fun update(id:String,fields:Map<String,Any?>,actor:String="user"):Result{val o=find(id)?:return Result(false,message="Record not found");fields.forEach{(k,v)->o.put(k,v)};o.put("updatedAt",n());o.put("updatedBy",actor);replace(o);audit(id,"UPDATE",actor);return Result(true,id,"Updated")}
 fun find(id:String):JSONObject?{val a=ArthSaathiDataStore.records();for(i in 0 until a.length())if(a.optJSONObject(i)?.optString("id")==id)return a.getJSONObject(i);return null}
 fun linked(id:String):List<JSONObject>{val out=mutableListOf<JSONObject>();val a=ArthSaathiDataStore.records();for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;if(o.optString("recordId")==id||o.optString("partyId")==id||o.optString("relatedId")==id||o.optString("parentId")==id)out+=o};return out}
 fun mis():Mis{val a=ArthSaathiDataStore.records();var c=0.0;var p=0.0;var asst=0.0;var liab=0.0;var ben=0.0;var grp=0.0;var rev=0.0;for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;when(o.optString("type")){"CREDIT"->c+=o.optDouble("amount");"REPAYMENT"->p+=o.optDouble("amount");"ASSET","PORTFOLIO"->asst+=o.optDouble("value",o.optDouble("currentValue",0.0));"LIABILITY"->liab+=o.optDouble("outstanding",o.optDouble("value",0.0));"BENEFIT"->ben+=o.optDouble("value",0.0);"GROUP_EXPENSE"->grp+=o.optDouble("total",o.optDouble("value",0.0));"REVENUE"->rev+=o.optDouble("amount",o.optDouble("value",0.0))}};return Mis(m(c),m(p),m((c-p).coerceAtLeast(0.0)),m(asst),m(liab),m(ben),m(grp),m(rev))}
 fun switchAnalysis(value:Double,currentReturn:Double,alternativeReturn:Double,currentCost:Double,alternativeCost:Double)=JSONObject().apply{val d=alternativeReturn-currentReturn;val cd=alternativeCost-currentCost;put("returnDifferencePct",m(d));put("annualOpportunityDifference",m(value*d/100));put("costDifference",m(cd));put("netIndicativeDifference",m(value*d/100-cd));put("requiresRiskReview",true);put("requiresUserDecision",true)}
 fun audit(recordId:String,action:String,actor:String){ArthSaathiDataStore.append(JSONObject().apply{put("id",id("AUD"));put("type","AUDIT");put("recordId",recordId);put("action",action);put("actor",actor);put("createdAt",n())})}
 private fun save(o:JSONObject,action:String){ArthSaathiDataStore.append(o);audit(o.getString("id"),action,"system")}
 private fun replace(o:JSONObject){val a=ArthSaathiDataStore.records();for(i in 0 until a.length())if(a.getJSONObject(i).optString("id")==o.optString("id")){a.put(i,o);break};ArthSaathiDataStore.replace(a)}
}
