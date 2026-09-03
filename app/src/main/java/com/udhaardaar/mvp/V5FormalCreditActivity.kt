package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject

class V5FormalCreditActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val repo by lazy { V5WorkflowRepository(this) }
    private val consent by lazy { V5OtpConsentService(this) }
    private fun e(h:String)=EditText(this@V5FormalCreditActivity).apply{hint=h;setSingleLine(true);setPadding(14,10,14,10)}
    private fun add(r:LinearLayout,v:View,h:Int=56){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
    private fun show(){val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,16,20,28)};add(r,TextView(this).apply{text="UDHAARDAAR V5 • Formal Credit";textSize=23f},62);add(r,TextView(this).apply{text="Institutional credit • sanction terms • borrower consent • shared repayment ledger • audit";textSize=13f},58)
        val borrower=e("Borrower / party profile ID *");val institution=e("Bank / NBFC / institution *");val sanction=e("Sanction amount ₹ *").apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL};val roi=e("Sanctioned annual ROI % *").apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL};val tenure=e("Tenure months *").apply{inputType=InputType.TYPE_CLASS_NUMBER};val ref=e("Sanction / account reference");val docs=e("Sanction/account document IDs");listOf(borrower,institution,sanction,roi,tenure,ref,docs).forEach{add(r,it)}
        add(r,Button(this).apply{text="REGISTER FORMAL CREDIT AFTER CONSENT";setOnClickListener{register(borrower,institution,sanction,roi,tenure,ref,docs)}},64);add(r,Button(this).apply{text="OPEN CHARGE COMPARISON";setOnClickListener{startActivity(Intent(this@V5FormalCreditActivity,V5ChargeComparisonActivity::class.java))}},56);add(r,Button(this).apply{text="BACK";setOnClickListener{finish()}},54);setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})}
    private fun register(b:EditText,i:EditText,a:EditText,r:EditText,t:EditText,ref:EditText,docs:EditText){val profile=b.text.toString().trim();val amount=a.text.toString().toDoubleOrNull();val rate=r.text.toString().removeSuffix("%").toDoubleOrNull();val months=t.text.toString().toIntOrNull();if(profile.isBlank()||i.text.isBlank()||amount==null||amount<=0||rate==null||rate<0||rate>100||months==null||months<1){toast("Complete valid borrower, institution, amount, ROI and tenure");return};val id="CR-FORMAL-${System.currentTimeMillis()}";val consentId=consent.issue(id,"FORMAL_CREDIT_CONSENT",profile);val code=store.find("consents",consentId)?.optString("otp","")?:"";val input=e("6-digit OTP");AlertDialog.Builder(this).setTitle("Formal credit consent").setMessage("Review sanction terms before confirmation.\nDemo OTP: $code").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(consent.verify(consentId,input.text.toString())){val now=System.currentTimeMillis();val total=amount;store.replace("formal_credits",JSONObject().apply{put("id",id);put("borrowerProfileId",profile);put("institution",i.text.toString().trim());put("sanctionAmount",amount);put("sanctionedRoi",rate);put("tenureMonths",months);put("reference",ref.text.toString().trim());put("documentIds",docs.text.toString().trim());put("consentId",consentId);put("status","REGISTERED");put("createdAt",now);put("updatedAt",now)});repo.saveCredit(id,profile,"Formal Credit","Credit Received",amount,rate,"Bullet / Full payment","", "",i.text.toString().trim(),ref.text.toString().trim(),"","",now,total);store.find("credits",id)?.let{it.put("formalCredit",true);it.put("institution",i.text.toString().trim());it.put("documentIds",docs.text.toString().trim());it.put("consentId",consentId);store.replace("credits",it)};repo.appendAudit(id,"FORMAL_CREDIT_REGISTERED","$profile","borrower consent OTP verified; shared credit ledger created");toast("Formal credit registered after consent and linked to shared repayment ledger");d.dismiss();finish()}else input.error="Incorrect OTP"}};d.show()}}
    private fun toast(s:String)=Toast.makeText(this@V5FormalCreditActivity,s,Toast.LENGTH_LONG).show()
}
