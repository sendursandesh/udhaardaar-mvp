package com.udhaardaar.mvp

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.Locale
import kotlin.math.pow
import kotlin.math.round

object ArthSaathiCoreEngine {
 data class Result(val ok:Boolean,val id:String?=null,val message:String="")
 data class Mis(val credits:Double,val repayments:Double,val outstanding:Double,val assets:Double,val liabilities:Double,val benefits:Double,val groupExpenses:Double,val revenue:Double,val actualCharges:Double,val chargeVariance:Double)
 private fun id(p:String)=p+"-"+UUID.randomUUID().toString().take(8).uppercase()
 private fun n()=System.currentTimeMillis()
 private fun m(v:Double)=round(v*100)/100
 fun validateMobile(v:String)=v.matches(Regex("[6-9][0-9]{9}"))
 fun validatePan(v:String)=v.isBlank()||v.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))
 fun validateGstin(v:String)=v.isBlank()||v.matches(Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]"))

 fun createPerson(name:String,mobile:String="",role:String="",address:String="",pan:String="",gstin:String="",pin:String=""):Result {
  require(name.isNotBlank());require(mobile.isEmpty()||validateMobile(mobile))
  val normalizedPan=pan.trim().uppercase(Locale.US)
  val normalizedGstin=gstin.trim().uppercase(Locale.US)
  val normalizedPin=pin.trim()
  require(validatePan(normalizedPan)) { "Enter a valid PAN" }
  require(validateGstin(normalizedGstin)) { "Enter a valid GSTIN" }
  require(normalizedPin.isBlank() || normalizedPin.matches(Regex("[1-9][0-9]{5}"))) { "Enter a valid 6-digit Indian PIN code" }
  val o=JSONObject().apply{put("id",id("PER"));put("type","PERSON");put("name",name.trim());put("mobile",mobile);put("role",role);put("address",address);put("pan",normalizedPan);put("gstin",normalizedGstin);put("pin",normalizedPin);put("createdAt",n());put("updatedAt",n())}
  save(o,"CREATE_PERSON");return Result(true,o.getString("id"),"Person saved")
 }
 fun createCredit(partyId:String?,partyName:String,nature:String,amount:Double,roi:Double,method:String,terms:String,dueDate:String="",guarantorId:String?=null,documentId:String?=null,partyMobile:String="",guarantorName:String="",repaymentMode:String=terms,tenureMonths:Int=0):Result {
  require(partyName.isNotBlank());require(nature.isNotBlank());require(amount.isFinite() && amount in 0.01..1.0e15) { "Amount must be between ₹0.01 and ₹1,000,000,000,000,000" };require(roi.isFinite() && roi in 0.0..100.0)
  require(partyMobile.isBlank() || validateMobile(partyMobile))
  require(method.isNotBlank());require(terms.isNotBlank());require(tenureMonths in 0..600) { "Tenure cannot exceed 600 months" }
  if(nature.contains("Rental",true)||nature.contains("Lease",true))require(roi==0.0)
  val mode=repaymentMode.trim().ifBlank { terms.trim() }
  val isLease=nature.contains("Rental",true)||nature.contains("Lease",true)
  if (!isLease && (mode.contains("EMI",true) || mode.contains("Principal + Interest",true))) require(tenureMonths in 1..600) { "Enter a tenure from 1 to 600 months for this repayment plan" }
  val emiAmount=if(!isLease && mode.contains("EMI",true) && tenureMonths>0) emi(amount,roi,tenureMonths) else 0.0
  val totalPayable=when {
    isLease -> amount
    mode.contains("EMI",true) && tenureMonths>0 -> schedule(amount, roi, tenureMonths).let { plan -> m((0 until plan.length()).sumOf { plan.optJSONObject(it)?.optDouble("emi") ?: 0.0 }) }
    mode.contains("Principal + Interest",true) && tenureMonths>0 -> m(amount + amount*roi*tenureMonths/1200.0)
    else -> amount
  }
  require(totalPayable.isFinite() && totalPayable >= amount) { "Calculated payable amount is outside the supported range" }
  val o=JSONObject().apply{put("id",id("CR"));put("type","CREDIT");put("partyId",partyId?:"");put("party",partyName);put("partyMobile",partyMobile);put("nature",nature);put("amount",m(amount));put("roi",m(roi));put("method",method);put("terms",terms);put("repaymentMode",mode);put("tenureMonths",tenureMonths);put("expectedEmi",emiAmount);put("totalPayable",totalPayable);put("dueDate",dueDate);put("guarantorId",guarantorId?:"");put("guarantorName",guarantorName);put("documentId",documentId?:"");put("paid",0.0);put("status","ACTIVE");put("consentStatus","PENDING");put("createdAt",n());put("updatedAt",n())}
  save(o,"CREATE_CREDIT");return Result(true,o.getString("id"),"Credit created")
 }
 fun requestConsent(recordId:String,actor:String):Result {
  val o=find(recordId)?:return Result(false,message="Record not found")
  if(o.optString("type")!="CREDIT")return Result(false,message="Consent is supported for credit records only")
  val mobile=o.optString("partyMobile")
  if(!validateMobile(mobile))return Result(false,message="A valid borrower mobile number is required before requesting consent")
  val challenge=ArthSaathiOtpService.request(mobile,"CREDIT_CONSENT",recordId)
      ?:return Result(false,message="OTP service is not configured or the provider could not issue a challenge; no consent was recorded")
  o.put("consentStatus","REQUESTED");o.put("consentActor",actor);o.put("consentChallengeId",challenge);o.put("consentRequestedAt",n())
  replace(o);audit(recordId,"CONSENT_REQUESTED",actor)
  return Result(true,recordId,"Consent OTP requested from the configured provider")
 }
 fun confirmConsent(recordId:String,otp:String,actor:String):Result {
  if(!otp.matches(Regex("\\d{4,8}")))return Result(false,message="Enter the verification code sent by the configured provider")
  val o=find(recordId)?:return Result(false,message="Record not found")
  if(o.optString("consentStatus")!="REQUESTED")return Result(false,message="No pending consent challenge exists")
  val challenge=o.optString("consentChallengeId")
  if(challenge.isBlank()||!ArthSaathiOtpService.verify(challenge,otp))
      return Result(false,message="OTP could not be verified; consent remains pending")
  o.put("consentStatus","CONSENTED");o.put("consentActor",actor);o.put("consentAt",n())
  o.remove("consentChallengeId")
  replace(o);audit(recordId,"CONSENT_CONFIRMED",actor)
  return Result(true,recordId,"Consent confirmed by OTP provider")
 }
 fun repayment(recordId:String,amount:Double,method:String,actor:String):Result{
  require(amount.isFinite() && amount>0);require(method.isNotBlank())
  val paymentAmount=m(amount);require(paymentAmount>0.0) { "Repayment must be at least ₹0.01" }
  val o=find(recordId)?:return Result(false,message="Credit not found");require(o.optString("type")=="CREDIT");require(o.optString("consentStatus")=="CONSENTED")
  val payable=o.optDouble("totalPayable",o.optDouble("amount"))
  val out=(payable-o.optDouble("paid")).coerceAtLeast(0.0);require(paymentAmount<=out+0.005)
  val tx=JSONObject().apply{put("id",id("TX"));put("type","REPAYMENT");put("recordId",recordId);put("amount",paymentAmount);put("method",method);put("actor",actor);put("createdAt",n())}
  save(tx,"REPAYMENT");o.put("paid",m(o.optDouble("paid")+paymentAmount));if(o.optDouble("paid")+0.005>=payable)o.put("status","CLOSED");o.put("updatedAt",n());replace(o);return Result(true,tx.getString("id"),"Repayment recorded")
 }
 fun emi(p:Double,rate:Double,months:Int):Double{require(p.isFinite() && p in 0.01..1.0e15) { "Principal must be between ₹0.01 and ₹1,000,000,000,000,000" };require(rate.isFinite() && rate in 0.0..100.0) { "Annual rate must be between 0 and 100%" };require(months in 1..600) { "Tenure must be between 1 and 600 months" };if(rate==0.0)return maxOf(m(p/months),0.01);val r=rate/1200.0;val discount=(1.0+r).pow(-months.toDouble());val raw=p*r/(1.0-discount);require(raw.isFinite() && raw>0.0) { "EMI calculation exceeded the supported range" };return maxOf(m(raw),0.01)}
 fun schedule(p:Double,rate:Double,months:Int):JSONArray{val a=JSONArray();var balance=p;val payment=emi(p,rate,months);val r=rate/1200.0;for(i in 1..months){val interest=if(r==0.0)0.0 else m(balance*r);val last=i==months;val due=if(last||payment>=balance+interest)m(balance+interest) else payment;val principal=if(balance<=0.0)0.0 else if(last||due>=balance+interest)m(balance) else m((due-interest).coerceAtLeast(0.0).coerceAtMost(balance));balance=if(principal>=balance-0.005)0.0 else m((balance-principal).coerceAtLeast(0.0));a.put(JSONObject().apply{put("installment",i);put("emi",due);put("interest",interest);put("principal",principal);put("balance",balance)})};return a}
 fun parseQrPayload(payload:String):JSONObject {
  val out=JSONObject().put("merchant","").put("amount","").put("reference","").put("note","").put("date","")
  val raw=payload.trim()
  if(raw.isEmpty()) return out
  try {
   val json=JSONObject(raw)
   val merchant=listOf("merchant","vendor","payeeName","pn","name","customer").firstNotNullOfOrNull { k -> json.optString(k).takeIf { it.isNotBlank() } }
   val amount=listOf("amount","am","total","value").firstNotNullOfOrNull { k -> json.optString(k).takeIf { it.isNotBlank() && it.toDoubleOrNull()?.let { n -> n.isFinite() && n>0.0 }==true } }
   val reference=listOf("reference","invoice","invoiceNo","transactionId","tr","txnId","id").firstNotNullOfOrNull { k -> json.optString(k).takeIf { it.isNotBlank() } }
   val note=listOf("note","description","tn","purpose","item").firstNotNullOfOrNull { k -> json.optString(k).takeIf { it.isNotBlank() } }
   val date=listOf("date","invoiceDate","issuedAt","timestamp","ts").firstNotNullOfOrNull { k -> json.optString(k).takeIf { it.isNotBlank() } }
   if(merchant!=null) out.put("merchant",merchant)
   if(amount!=null) out.put("amount",amount)
   if(reference!=null) out.put("reference",reference)
   if(note!=null) out.put("note",note)
   if(date!=null) out.put("date",date)
   return out
  } catch (_:Exception) {}
  val uri=android.net.Uri.parse(raw)
  val queryNames=listOf("pn","am","tr","tn","pa","mc","tid")
  if(uri.scheme.equals("upi",true) || uri.scheme.equals("http",true) || uri.scheme.equals("https",true)) {
   val name=uri.getQueryParameter("pn") ?: uri.getQueryParameter("merchant") ?: uri.getQueryParameter("name")
   val amount=uri.getQueryParameter("am") ?: uri.getQueryParameter("amount")
   val ref=uri.getQueryParameter("tr") ?: uri.getQueryParameter("tid") ?: uri.getQueryParameter("invoice")
   val note=uri.getQueryParameter("tn") ?: uri.getQueryParameter("note")
   val date=uri.getQueryParameter("date") ?: uri.getQueryParameter("invoiceDate") ?: uri.getQueryParameter("timestamp")
   if(!name.isNullOrBlank()) out.put("merchant",name)
   if(!amount.isNullOrBlank() && amount.toDoubleOrNull()?.let { it.isFinite() && it>0.0 }==true) out.put("amount",amount)
   if(!ref.isNullOrBlank()) out.put("reference",ref)
   if(!note.isNullOrBlank()) out.put("note",note)
   if(!date.isNullOrBlank()) out.put("date",date)
   if(out.optString("reference").isBlank()) uri.getQueryParameter("pa")?.let { out.put("reference",it) }
  }
  return out
 }
 fun saveModule(type:String,fields:Map<String,Any?>,links:Map<String,String> = emptyMap()):Result{val o=JSONObject().apply{put("id",id(type.take(8).uppercase()));put("type",type);put("createdAt",n());put("updatedAt",n())};fields.forEach{(k,v)->o.put(k,v)};links.forEach{(k,v)->o.put(k,v)};save(o,"CREATE_$type");return Result(true,o.getString("id"),"$type saved")}
 fun update(id:String,fields:Map<String,Any?>,actor:String="user"):Result{
  val o=find(id)?:return Result(false,message="Record not found")
  val protectedCreditFields=setOf("consentStatus","consentChallengeId","consentActor","consentAt","consentRequestedAt","paid","status","ownerUserId")
  if(o.optString("type")=="CREDIT" && fields.keys.any{it in protectedCreditFields})
   return Result(false,id,"Consent, repayment, closure and ownership fields can only change through their dedicated verified workflows")
  if(fields.containsKey("ownerUserId"))
   return Result(false,id,"Record ownership cannot be changed through a generic update")
  fields.forEach{(k,v)->o.put(k,v)}
  o.put("updatedAt",n());o.put("updatedBy",actor);replace(o);audit(id,"UPDATE",actor)
  return Result(true,id,"Updated")
 }
 fun find(id:String):JSONObject?{val a=ArthSaathiDataStore.records();for(i in 0 until a.length())if(a.optJSONObject(i)?.optString("id")==id)return a.getJSONObject(i);return null}
 fun linked(id:String):List<JSONObject>{val out=mutableListOf<JSONObject>();val a=ArthSaathiDataStore.records();for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;if(o.optString("recordId")==id||o.optString("partyId")==id||o.optString("relatedId")==id||o.optString("parentId")==id)out+=o};return out}
 fun mis():Mis{
  val a=ArthSaathiDataStore.records()
  var c=0.0;var p=0.0;var outstanding=0.0;var asst=0.0;var liab=0.0;var ben=0.0;var grp=0.0;var rev=0.0;var charges=0.0;var chargeVariance=0.0
  fun amount(o:JSONObject,vararg keys:String):Double {
   for(key in keys){if(o.has(key)&&!o.isNull(key)){val v=o.optDouble(key,Double.NaN);if(v.isFinite())return v}}
   return 0.0
  }
  fun status(o:JSONObject,vararg keys:String):String {
   for(key in keys){val v=o.optString(key).trim();if(v.isNotBlank())return v.uppercase(Locale.US)}
   return ""
  }
  val receivedStatuses=setOf("PAID","COMPLETED","SUCCESS","SETTLED","RECEIVED","COLLECTED")
  val completedBenefitStatuses=receivedStatuses+setOf("CLAIMED","REFUNDED")
  for(i in 0 until a.length()){
   val o=a.optJSONObject(i)?:continue
   when(o.optString("type")){
    "CREDIT"->{val principal=amount(o,"amount");c+=principal;val payable=amount(o,"totalPayable").takeIf { it>0.0 }?:principal;outstanding+=(payable-amount(o,"paid")).coerceAtLeast(0.0)}
    "REPAYMENT"->p+=amount(o,"amount")
    "ASSET"->asst+=amount(o,"value","f2","currentValue")
    "PORTFOLIO"->asst+=amount(o,"currentValue","f3","value","f2")
    "LIABILITY"->liab+=amount(o,"outstanding","f2","value")
    "BENEFIT"->{if(status(o,"status","f3") in completedBenefitStatuses)ben+=amount(o,"value","f2")}
    "GROUP_EXPENSE"->grp+=amount(o,"total","f1","value")
    "REVENUE"->{val state=status(o,"paymentStatus","f2","status");val value=amount(o,"amount","f1","value");if(state in receivedStatuses)rev+=value else if(state=="REFUNDED")rev-=value}
    "CHARGECHECK"->{charges+=amount(o,"actualCharge","f2");chargeVariance+=if(o.has("variance"))amount(o,"variance") else if(o.has("f3"))amount(o,"f3") else amount(o,"f2")-amount(o,"f1")}
   }
  }
  return Mis(m(c),m(p),m(outstanding),m(asst),m(liab),m(ben),m(grp),m(rev),m(charges),m(chargeVariance))
 }
 fun switchAnalysis(value:Double,currentReturn:Double,alternativeReturn:Double,currentCost:Double,alternativeCost:Double)=JSONObject().apply{val d=alternativeReturn-currentReturn;val cd=alternativeCost-currentCost;put("returnDifferencePct",m(d));put("annualOpportunityDifference",m(value*d/100));put("costDifference",m(cd));put("netIndicativeDifference",m(value*d/100-cd));put("requiresRiskReview",true);put("requiresUserDecision",true)}
 fun audit(recordId:String,action:String,actor:String){ArthSaathiDataStore.append(JSONObject().apply{put("id",id("AUD"));put("type","AUDIT");put("recordId",recordId);put("action",action);put("actor",actor);put("createdAt",n())})}
 private fun save(o:JSONObject,action:String){ArthSaathiDataStore.append(o);audit(o.getString("id"),action,"system")}
 private fun replace(o:JSONObject){val a=ArthSaathiDataStore.records();for(i in 0 until a.length())if(a.getJSONObject(i).optString("id")==o.optString("id")){a.put(i,o);break};ArthSaathiDataStore.replace(a)}
}
