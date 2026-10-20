package com.udhaardaar.mvp

import org.json.JSONArray
import org.json.JSONObject
import java.util.UUID
import java.util.Locale
import kotlin.math.pow
import kotlin.math.ceil
import kotlin.math.round

object ArthSaathiCoreEngine {
 data class Result(val ok:Boolean,val id:String?=null,val message:String="")
 data class Mis(val credits:Double,val repayments:Double,val outstanding:Double,val assets:Double,val liabilities:Double,val benefits:Double,val groupExpenses:Double,val revenue:Double,val actualCharges:Double,val chargeVariance:Double,val financialAssets:Double=0.0,val nonFinancialAssets:Double=0.0,val portfolioValue:Double=0.0,val groupCollected:Double=0.0,val groupOutstandingShares:Double=0.0)
 private fun id(p:String)=p+"-"+UUID.randomUUID().toString().take(8).uppercase()
 private fun n()=System.currentTimeMillis()
 private fun m(v:Double)=round(v*100)/100
 fun validateMobile(v:String)=v.matches(Regex("[6-9][0-9]{9}"))
 fun validateEmail(v:String)=v.isBlank() || v.trim().matches(Regex("(?i)[A-Z0-9._%+\\-]+@[A-Z0-9.\\-]+\\.[A-Z]{2,}"))
 fun validatePan(v:String)=v.isBlank()||v.matches(Regex("[A-Z]{5}[0-9]{4}[A-Z]"))
 fun validateGstin(v:String)=v.isBlank()||v.matches(Regex("[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]"))
 fun validateAadhaar(v:String)=v.isBlank()||v.matches(Regex("[2-9][0-9]{11}"))

 fun createPerson(name:String,mobile:String="",role:String="",address:String="",pan:String="",gstin:String="",pin:String="",email:String="",aadhaar:String=""):Result {
  require(name.isNotBlank());require(mobile.isEmpty()||validateMobile(mobile));require(validateEmail(email)) { "Enter a valid email address" }
  val normalizedPan=pan.trim().uppercase(Locale.US)
  val normalizedGstin=gstin.trim().uppercase(Locale.US)
  val normalizedPin=pin.trim()
  val normalizedAadhaar=aadhaar.trim()
  require(validateAadhaar(normalizedAadhaar)) { "Aadhaar must contain 12 digits and cannot start with 0 or 1" }
  require(validatePan(normalizedPan)) { "Enter a valid PAN" }
  require(validateGstin(normalizedGstin)) { "Enter a valid GSTIN" }
  require(normalizedPin.isBlank() || normalizedPin.matches(Regex("[1-9][0-9]{5}"))) { "Enter a valid 6-digit Indian PIN code" }
  val o=JSONObject().apply{put("id",id("PER"));put("type","PERSON");put("name",name.trim());put("mobile",mobile);put("role",role);put("address",address);put("pan",normalizedPan);put("gstin",normalizedGstin);put("pin",normalizedPin);put("email",email.trim());put("aadhaar",normalizedAadhaar);put("createdAt",n());put("updatedAt",n())}
  save(o,"CREATE_PERSON");return Result(true,o.getString("id"),"Person saved")
 }
 fun findPersonByIdentity(query:String):JSONObject? {
  val q=query.trim()
  if(q.isBlank()) return null
  val people=ArthSaathiDataStore.records()
  for(i in 0 until people.length()){
   val person=people.optJSONObject(i)?:continue
   if(person.optString("type")!="PERSON") continue
   if(person.optString("id").equals(q,true) ||
      person.optString("name").equals(q,true) ||
      person.optString("mobile")==q ||
      person.optString("pan").equals(q,true) ||
      person.optString("aadhaar")==q) return person
  }
  return null
 }
 fun createCredit(partyId:String?,partyName:String,nature:String,amount:Double,roi:Double,method:String,terms:String,dueDate:String="",guarantorId:String?=null,documentId:String?=null,partyMobile:String="",guarantorName:String="",repaymentMode:String=terms,tenureMonths:Int=0,sourceRecordId:String?=null):Result {
  require(partyName.isNotBlank());require(nature.isNotBlank());require(amount.isFinite() && amount in 0.01..1.0e15) { "Amount must be between ₹0.01 and ₹1,000,000,000,000,000" };require(roi.isFinite() && roi in 0.0..100.0)
  require(validateMobile(partyMobile)) { "A valid 10-digit counterparty mobile number is required for consent" }
  require(method.isNotBlank());require(terms.isNotBlank());require(tenureMonths in 0..600) { "Tenure cannot exceed 600 months" }
  if(nature.contains("Rental",true)||nature.contains("Lease",true))require(roi==0.0)
  val mode=repaymentMode.trim().ifBlank { terms.trim() }
  val isLease=nature.contains("Rental",true)||nature.contains("Lease",true)
  val isEmi = mode.contains("EMI", true)
  val isPrincipalInterest = mode.contains("Principal + Interest", true)
  val isBullet = mode.contains("Bullet", true)
  val isRentalMode = mode.contains("Rental", true) || mode.contains("Lease", true)
  if (isLease) {
   require(isRentalMode) { "Rental / lease credit must use the rental calculation method" }
   require(tenureMonths in 1..600) { "Enter the lease duration in months (1 to 600)" }
   require(!documentId.isNullOrBlank()) { "A lease agreement document reference is required" }
  } else {
   require(!isRentalMode) { "Select a credit nature that matches the rental / lease method" }
   require(isEmi || isPrincipalInterest || isBullet) { "Select EMI, Principal + Interest, or Bullet repayment calculation" }
   require(tenureMonths in 1..600) { "Enter a tenure from 1 to 600 months for this repayment plan" }
  }
  val emiAmount=if(!isLease && isEmi) emi(amount,roi,tenureMonths) else 0.0
  val leasePeriods = when {
   terms.contains("Annual", true) || terms.contains("Yearly", true) -> ceil(tenureMonths / 12.0).toInt()
   terms.contains("Half-yearly", true) || terms.contains("Semi-annual", true) -> ceil(tenureMonths / 6.0).toInt()
   terms.contains("Quarterly", true) -> ceil(tenureMonths / 3.0).toInt()
   else -> tenureMonths
  }.coerceAtLeast(1)
  val totalPayable=when {
    isLease -> m(amount * leasePeriods)
    isEmi -> schedule(amount, roi, tenureMonths).let { plan -> m((0 until plan.length()).sumOf { plan.optJSONObject(it)?.optDouble("emi") ?: 0.0 }) }
    isPrincipalInterest || isBullet -> m(amount + amount*roi*tenureMonths/1200.0)
    else -> amount
  }
  require(totalPayable.isFinite() && totalPayable >= amount) { "Calculated payable amount is outside the supported range" }
  val o=JSONObject().apply{put("id",id("CR"));put("type","CREDIT");put("partyId",partyId?:"");put("party",partyName);put("partyMobile",partyMobile);put("nature",nature);put("amount",m(amount));put("roi",m(roi));put("method",method);put("terms",terms);put("repaymentMode",mode);put("tenureMonths",tenureMonths);put("expectedEmi",emiAmount);put("totalPayable",totalPayable);put("dueDate",dueDate);put("guarantorId",guarantorId?:"");put("guarantorName",guarantorName);put("documentId",documentId?:"");put("sourceRecordId",sourceRecordId?:"");put("paid",0.0);put("status","PENDING_CONSENT");put("consentStatus","PENDING");put("createdAt",n());put("updatedAt",n())}
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
  o.put("status","ACTIVE");o.put("registeredAt",n());o.remove("consentChallengeId")
  replace(o);audit(recordId,"CONSENT_CONFIRMED",actor);audit(recordId,"CREDIT_REGISTERED",actor)
  return Result(true,recordId,"Consent confirmed; credit is now registered")
 }
 fun requestRepaymentOtp(recordId:String,actor:String):Result {
  val o=find(recordId)?:return Result(false,message="Credit not found")
  if(o.optString("type")!="CREDIT")return Result(false,message="Repayment is supported for credit records only")
  if(o.optString("status")!="ACTIVE")return Result(false,message="Credit is not registered and active")
  if(o.optString("consentStatus")!="CONSENTED")return Result(false,message="Verified borrower consent is required before repayment")
  val mobile=o.optString("partyMobile")
  if(!validateMobile(mobile))return Result(false,message="A valid borrower mobile number is required for repayment verification")
  val challenge=ArthSaathiOtpService.request(mobile,"REPAYMENT_CONFIRMATION",recordId)
      ?:return Result(false,message="Repayment OTP service is not configured or could not issue a challenge; no repayment was recorded")
  // Bind the server challenge to this exact credit and purpose locally as well.
  // A challenge issued for another credit cannot be replayed against this record.
  o.put("repaymentChallengeId",challenge)
  o.put("repaymentChallengePurpose","REPAYMENT_CONFIRMATION")
  o.put("repaymentChallengeRequestedAt",n())
  o.put("updatedAt",n())
  replace(o)
  audit(recordId,"REPAYMENT_OTP_REQUESTED",actor)
  return Result(true,challenge,"Repayment verification code requested from the configured provider")
 }

 /** Legacy entry point intentionally fails closed: callers must provide a provider-verified OTP. */
 fun repayment(recordId:String,amount:Double,method:String,actor:String):Result =
  Result(false,recordId,"Repayment OTP verification is required; no repayment was recorded")

 fun repayment(recordId:String,amount:Double,method:String,actor:String,challengeId:String,otp:String):Result{
  require(amount.isFinite() && amount>0);require(method.isNotBlank())
  val paymentAmount=m(amount);require(paymentAmount>0.0) { "Repayment must be at least ₹0.01" }
  val o=find(recordId)?:return Result(false,message="Credit not found")
  require(o.optString("type")=="CREDIT")
  require(o.optString("status")=="ACTIVE") { "Credit is not registered and active" }
  require(o.optString("consentStatus")=="CONSENTED") { "Verified borrower consent is required" }
  if(challengeId.isBlank() ||
      challengeId != o.optString("repaymentChallengeId") ||
      o.optString("repaymentChallengePurpose") != "REPAYMENT_CONFIRMATION") {
   return Result(false,recordId,"This repayment OTP challenge was not issued for this credit; no repayment was recorded")
  }
  if(!ArthSaathiOtpService.verify(challengeId,otp))
   return Result(false,recordId,"Repayment OTP was not verified; no repayment was recorded")
  val payable=o.optDouble("totalPayable",o.optDouble("amount"))
  val out=(payable-o.optDouble("paid")).coerceAtLeast(0.0)
  require(paymentAmount<=out+0.005) { "Repayment exceeds outstanding balance" }
  val tx=JSONObject().apply{
   put("id",id("TX"));put("type","REPAYMENT");put("recordId",recordId)
   put("amount",paymentAmount);put("method",method.trim());put("actor",actor)
   put("verificationChallengeId",challengeId);put("verificationPurpose","REPAYMENT_CONFIRMATION")
   put("verifiedAt",n());put("createdAt",n())
  }
  o.put("paid",m(o.optDouble("paid")+paymentAmount))
  if(o.optDouble("paid")+0.005>=payable)o.put("status","CLOSED")
  o.remove("repaymentChallengeId")
  o.remove("repaymentChallengePurpose")
  o.remove("repaymentChallengeRequestedAt")
  o.put("updatedAt",n())
  // Commit the balance change, repayment ledger row, and audit row in one
  // encrypted-store write to avoid a partial update if the app process stops.
  val current=ArthSaathiDataStore.records()
  val transaction=JSONArray()
  for(i in 0 until current.length()) {
   val existing=current.optJSONObject(i) ?: continue
   transaction.put(if(existing.optString("id")==recordId) o else existing)
  }
  transaction.put(tx)
  transaction.put(JSONObject().apply {
   put("id",id("AUD"));put("type","AUDIT");put("recordId",recordId)
   put("action","REPAYMENT");put("actor",actor);put("createdAt",n())
  })
  ArthSaathiDataStore.replace(transaction)
  return Result(true,tx.getString("id"),"Repayment recorded after verified OTP")
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
 fun parseInvoiceText(text:String):JSONObject {
  val raw=text.trim()
  val out=JSONObject().put("merchant","").put("amount","").put("reference","").put("note",raw.take(2000)).put("date","")
  if(raw.isBlank()) return out
  val lines=raw.lines().map{it.trim()}.filter{it.isNotBlank()}
  val labels=Regex("""(?i)\b(?:tax invoice|invoice no|invoice date|gstin|gst no|total|amount|bill to|ship to|phone|mobile|address|email|description|quantity|hsn|sac|subtotal|taxable value|cgst|sgst|igst|terms)\b""")
  val merchant=lines.firstOrNull { it.length in 2..100 && !labels.containsMatchIn(it) }
  if(merchant!=null) out.put("merchant",merchant)
  val totalPattern=Regex("""(?i)(?:grand\s+total|total\s+amount|invoice\s+value|amount\s+due|net\s+payable|total|payable)\s*[:\-]?\s*(?:₹|INR|Rs\.?\s*)?\s*([0-9][0-9,]*(?:\.[0-9]{1,2})?)""")
  val amount=totalPattern.find(raw)?.groupValues?.getOrNull(1)?.replace(",","")
    ?.takeIf { it.toDoubleOrNull()?.let { value -> value.isFinite() && value>0.0 }==true }
  if(amount!=null) out.put("amount",amount)
  val referencePattern=Regex("""(?im)^\s*(?:invoice\s*(?:no\.?|number|#)?|bill\s*(?:no\.?|number)|reference)\s*[:#\-]?\s*([A-Z0-9][A-Z0-9/\-]{2,})""")
  val reference=referencePattern.find(raw)?.groupValues?.getOrNull(1)
  if(!reference.isNullOrBlank()) out.put("reference",reference)
  val date=Regex("""\b(?:[0-3]?[0-9][/-][01]?[0-9][/-][0-9]{4}|[0-9]{4}-[01][0-9]-[0-3][0-9])\b""").find(raw)?.value
  if(!date.isNullOrBlank()) out.put("date",date)
  return out
 }

 fun saveModule(type:String,fields:Map<String,Any?>,links:Map<String,String> = emptyMap()):Result {
  val normalizedType=type.trim().uppercase(Locale.US)
  require(normalizedType.isNotBlank() && normalizedType.matches(Regex("[A-Z][A-Z0-9_]{0,39}"))) {
   "A valid module record type is required"
  }
  require(normalizedType != "CREDIT") {
   "Credits must be created through Register Credit so consent and repayment protections cannot be bypassed"
  }
  require(normalizedType !in setOf("PERSON","REPAYMENT","AUDIT")) {
   "This record type must be created through its dedicated validated workflow"
  }
  // These fields define record identity, ownership and lifecycle. Never let a
  // generic module form overwrite them, even when it submits a different type.
  val reserved=setOf("id","type","ownerUserId","createdAt","updatedAt")
  require(fields.keys.none { it in reserved } && links.keys.none { it in reserved }) {
   "Generic module fields cannot overwrite record identity, ownership or timestamps"
  }
  val o=JSONObject().apply {
   put("id",id(normalizedType.take(8)))
   put("type",normalizedType)
   put("createdAt",n())
   put("updatedAt",n())
  }
  fields.forEach { (k,v) -> o.put(k,v) }
  links.forEach { (k,v) -> o.put(k,v) }
  save(o,"CREATE_$normalizedType")
  return Result(true,o.getString("id"),"$normalizedType saved")
 }
 fun update(id:String,fields:Map<String,Any?>,actor:String="user"):Result{
  val o=find(id)?:return Result(false,message="Record not found")
  val reserved=setOf("id","type","ownerUserId","createdAt","updatedAt")
  if(fields.keys.any { it in reserved })
   return Result(false,id,"Record identity, ownership and creation timestamps cannot be changed through a generic update")
  val protectedCreditFields=setOf("consentStatus","consentChallengeId","consentActor","consentAt","consentRequestedAt","paid","status","amount","roi","totalPayable","expectedEmi","party","partyId","partyMobile","nature","method","terms","repaymentMode","tenureMonths","dueDate","guarantorId","guarantorName","documentId","registeredAt","repaymentChallengeId","repaymentChallengePurpose","repaymentChallengeRequestedAt","ownerUserId")
  if(o.optString("type")=="CREDIT" && fields.keys.any{it in protectedCreditFields})
   return Result(false,id,"Consent, repayment, closure and ownership fields can only change through their dedicated verified workflows")
  if(fields.containsKey("ownerUserId"))
   return Result(false,id,"Record ownership cannot be changed through a generic update")
  fields.forEach{(k,v)->o.put(k,v)}
  o.put("updatedAt",n());o.put("updatedBy",actor);replace(o);audit(id,"UPDATE",actor)
  return Result(true,id,"Updated")
 }
 fun find(id:String):JSONObject?{val a=ArthSaathiDataStore.records();for(i in 0 until a.length())if(a.optJSONObject(i)?.optString("id")==id)return a.getJSONObject(i);return null}
 fun linked(id:String):List<JSONObject>{val out=mutableListOf<JSONObject>();val a=ArthSaathiDataStore.records();for(i in 0 until a.length()){val o=a.optJSONObject(i)?:continue;if(o.optString("recordId")==id||o.optString("partyId")==id||o.optString("relatedId")==id||o.optString("parentId")==id||o.optString("sourceRecordId")==id||o.optString("relatedAssetId")==id||o.optString("relatedBenefitId")==id||o.optString("relatedDocumentId")==id||o.optString("personId")==id||o.optString("responsiblePersonId")==id||o.optString("nomineePersonId")==id)out+=o};return out}
 fun mis():Mis{
  val a=ArthSaathiDataStore.records()
  var c=0.0;var p=0.0;var outstanding=0.0;var asst=0.0;var liab=0.0;var ben=0.0;var grp=0.0;var rev=0.0;var charges=0.0;var chargeVariance=0.0;var financialAssets=0.0;var nonFinancialAssets=0.0;var portfolioValue=0.0;var groupCollected=0.0;var groupOutstandingShares=0.0
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
    "CREDIT"->{if(status(o,"status") in setOf("ACTIVE","CLOSED")){val principal=amount(o,"amount");c+=principal;val payable=amount(o,"totalPayable").takeIf { it>0.0 }?:principal;outstanding+=(payable-amount(o,"paid")).coerceAtLeast(0.0)}}
    "REPAYMENT"->p+=amount(o,"amount")
    "ASSET"->{val value=amount(o,"value","f2","currentValue");asst+=value;val category=status(o,"category","f1");if(category.contains("NON-FINANCIAL")||category.contains("NONFINANCIAL"))nonFinancialAssets+=value else if(category.contains("FINANCIAL"))financialAssets+=value else nonFinancialAssets+=value}
    "PORTFOLIO"->{val value=amount(o,"currentValue","f3","value","f2");asst+=value;portfolioValue+=value}
    "LIABILITY"->liab+=amount(o,"outstanding","f2","value")
    "BENEFIT"->{if(status(o,"status","f3") in completedBenefitStatuses)ben+=amount(o,"value","f2")}
    "GROUP_EXPENSE"->grp+=amount(o,"total","f1","value")
    "GROUP_CONTRIBUTION"->groupCollected+=amount(o,"f1","amount","value")
    "REVENUE"->{val state=status(o,"paymentStatus","f2","status");val value=amount(o,"amount","f1","value");if(state in receivedStatuses)rev+=value else if(state=="REFUNDED")rev-=value}
    "CHARGECHECK"->{charges+=amount(o,"actualCharge","f2");chargeVariance+=if(o.has("variance"))amount(o,"variance") else if(o.has("f3"))amount(o,"f3") else amount(o,"f2")-amount(o,"f1")}
   }
  }
  // Reconcile each group's equal-share obligation against recorded contributions and settlements.
  // A settlement transfers money between members; it changes individual net balances but not the total collected.
  for(i in 0 until a.length()){
   val expense=a.optJSONObject(i)?:continue
   if(expense.optString("type")!="GROUP_EXPENSE") continue
   val groupId=expense.optString("id")
   val total=amount(expense,"total","f1","value")
   val names=expense.optString("f4").split(",").map{it.trim()}.filter{it.isNotBlank()}.distinctBy{it.lowercase(Locale.US)}
   if(names.isEmpty()) continue
   val share=total/names.size
   val balances=linkedMapOf<String,Double>()
   names.forEach{balances[it]= -share}
   for(j in 0 until a.length()){
    val entry=a.optJSONObject(j)?:continue
    if(entry.optString("parentId")!=groupId) continue
    when(entry.optString("type")){
     "GROUP_CONTRIBUTION"->{
      val member=names.firstOrNull{it.equals(entry.optString("f0").trim(),true)}
      if(member!=null) balances[member]=(balances[member]?: -share)+amount(entry,"f1","amount","value")
     }
     "GROUP_SETTLEMENT"->{
      val debtor=names.firstOrNull{it.equals(entry.optString("f0").trim(),true)}
      val creditor=names.firstOrNull{it.equals(entry.optString("f1").trim(),true)}
      val value=amount(entry,"f2","amount","value")
      if(debtor!=null) balances[debtor]=(balances[debtor]?: -share)+value
      if(creditor!=null) balances[creditor]=(balances[creditor]?: -share)-value
     }
    }
   }
   groupOutstandingShares+=balances.values.filter{it < -0.005}.sumOf{-it}
  }
  return Mis(m(c),m(p),m(outstanding),m(asst),m(liab),m(ben),m(grp),m(rev),m(charges),m(chargeVariance),m(financialAssets),m(nonFinancialAssets),m(portfolioValue),m(groupCollected),m(groupOutstandingShares))
 }
 fun switchAnalysis(value:Double,currentReturn:Double,alternativeReturn:Double,currentCost:Double,alternativeCost:Double)=JSONObject().apply{val d=alternativeReturn-currentReturn;val cd=alternativeCost-currentCost;put("returnDifferencePct",m(d));put("annualOpportunityDifference",m(value*d/100));put("costDifference",m(cd));put("netIndicativeDifference",m(value*d/100-cd));put("requiresRiskReview",true);put("requiresUserDecision",true)}
 fun audit(recordId:String,action:String,actor:String){ArthSaathiDataStore.append(JSONObject().apply{put("id",id("AUD"));put("type","AUDIT");put("recordId",recordId);put("action",action);put("actor",actor);put("createdAt",n())})}
 private fun save(o:JSONObject,action:String){ArthSaathiDataStore.append(o);audit(o.getString("id"),action,"system")}
 private fun replace(o:JSONObject){val a=ArthSaathiDataStore.records();for(i in 0 until a.length())if(a.getJSONObject(i).optString("id")==o.optString("id")){a.put(i,o);break};ArthSaathiDataStore.replace(a)}
}
