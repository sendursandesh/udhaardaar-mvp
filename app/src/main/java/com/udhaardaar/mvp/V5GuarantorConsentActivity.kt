package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import kotlin.random.Random

/** Guarantor creation/linking with a bilateral OTP consent checkpoint. */
class V5GuarantorConsentActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val repo by lazy { V5WorkflowRepository(this) }
    private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true);setPadding(14,10,14,10)}
    private fun add(r:LinearLayout,v:android.view.View,h:Int=54){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
    private fun show(){val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(22,18,22,30)};add(r,TextView(this).apply{text="UDHAARDAAR V5 • Guarantor";textSize=23f},60);add(r,TextView(this).apply{text="Create/link guarantor → generate guarantee draft → guarantor OTP consent → audit";textSize=13f},58);val n=e("Guarantor full name *");val m=e("Guarantor mobile *");m.inputType=InputType.TYPE_CLASS_PHONE;val rel=e("Relationship");val credit=e("Credit ID to guarantee (optional)");add(r,n);add(r,m);add(r,rel);add(r,credit);add(r,Button(this).apply{text="CREATE / LINK + GENERATE GUARANTEE";setOnClickListener{create(n,m,rel,credit)}});add(r,Button(this).apply{text="BACK";setOnClickListener{finish()}});setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})}
    private fun create(n:EditText,m:EditText,rel:EditText,credit:EditText){val name=n.text.toString().trim();val mobile=m.text.toString().trim();if(name.length<2||!V5Validation.mobile(mobile)){m.error="Valid 10-digit mobile required";return};val id="G-${System.currentTimeMillis()}";repo.saveGuarantor(V5GuarantorAndDocuments.GuarantorProfile(id,name,mobile,"",relationship=rel.text.toString().trim()));val cid=credit.text.toString().trim().ifBlank{"UNLINKED"};val docId="GUA-DOC-${System.currentTimeMillis()}";val draft=if(cid!="UNLINKED")V5GuarantorAndDocuments.generateGuaranteeTemplate(cid,V5GuarantorAndDocuments.GuarantorProfile(id,name,mobile,"",relationship=rel.text.toString().trim()),"Borrower / linked credit",0.0) else "GUARANTEE DRAFT\nGuarantor: $name\nCredit: to be linked";store.add("documents",JSONObject().apply{put("id",docId);put("type","GUARANTOR_GUARANTEE");put("status","CONSENT_PENDING");put("version",1);put("createdAt",System.currentTimeMillis());put("guarantorId",id);put("creditId",cid);put("content",draft)});val otp=(100000+Random.nextInt(900000)).toString();store.add("consents",JSONObject().apply{put("id","CONS-${System.currentTimeMillis()}");put("entityId",docId);put("purpose","GUARANTOR_GUARANTEE_CONSENT");put("party",name);put("otp",otp);put("status","OTP_SENT");put("issuedAt",System.currentTimeMillis())});val input=e("Enter guarantor OTP");AlertDialog.Builder(this).setTitle("Guarantor consent").setMessage("Guarantee draft generated before consent.\nDemo OTP: $otp\nReview the draft before confirming. Production SMS gateway required for live delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(-1).setOnClickListener{if(input.text.toString()==otp){store.all("consents").firstOrNull{it.optString("entityId")==docId}?.let{it.put("status","OTP_VERIFIED");it.put("verifiedAt",System.currentTimeMillis());store.replace("consents",it)};store.find("documents",docId)?.let{it.put("status","COMPLETED");it.put("consentedAt",System.currentTimeMillis());store.replace("documents",it)};repo.appendAudit(docId,"GUARANTOR_GUARANTEE_OTP_CONSENTED_AND_ARCHIVED",id,"guarantor=$name; credit=$cid; timestamp=${System.currentTimeMillis()}");d.dismiss();Toast.makeText(this,"Guarantor guarantee consented and archived",Toast.LENGTH_LONG).show()}else input.error="Incorrect OTP"}};d.show()}}
}
