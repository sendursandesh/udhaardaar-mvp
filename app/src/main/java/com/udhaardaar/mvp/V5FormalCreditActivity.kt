package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.*
import org.json.JSONObject

class V5FormalCreditActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private fun e(h:String)=EditText(this@V5FormalCreditActivity).apply{hint=h;setSingleLine(true);setPadding(14,10,14,10)}
    private fun add(r:LinearLayout,v:View,h:Int=56){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
    private fun show(){
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,16,20,28)}
        add(r,TextView(this).apply{text="UDHAARDAAR V5 • Formal Credit";textSize=23f},62)
        add(r,TextView(this).apply{text="Institutional credit • sanction terms • borrower consent • audit";textSize=13f},52)
        val borrower=e("Borrower / party profile ID *")
        val institution=e("Bank / NBFC / institution *")
        val sanction=e("Sanction amount ₹ *").apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL}
        val roi=e("Sanctioned annual ROI % *").apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL}
        val tenure=e("Tenure months *").apply{inputType=InputType.TYPE_CLASS_NUMBER}
        val ref=e("Sanction / account reference")
        val docs=e("Sanction/account document IDs")
        listOf(borrower,institution,sanction,roi,tenure,ref,docs).forEach{add(r,it)}
        add(r,Button(this).apply{text="REGISTER FORMAL CREDIT AFTER CONSENT";setOnClickListener{register(borrower,institution,sanction,roi,tenure,ref,docs)}},64)
        add(r,Button(this).apply{text="OPEN CHARGE COMPARISON";setOnClickListener{startActivity(Intent(this@V5FormalCreditActivity,V5ChargeComparisonActivity::class.java))}},56)
        add(r,Button(this).apply{text="BACK";setOnClickListener{finish()}},54)
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }
    private fun register(b:EditText,i:EditText,a:EditText,r:EditText,t:EditText,ref:EditText,docs:EditText){
        val amount=a.text.toString().toDoubleOrNull();val rate=r.text.toString().removeSuffix("%").toDoubleOrNull();val months=t.text.toString().toIntOrNull()
        if(b.text.isBlank()||i.text.isBlank()||amount==null||amount<=0||rate==null||rate<0||rate>100||months==null||months<1){toast("Complete valid borrower, institution, amount, ROI and tenure");return}
        val id="FCR-${System.currentTimeMillis()}";val consentId=V5OtpConsentService(this).issue(id,"FORMAL_CREDIT_CONSENT",b.text.toString());val code=store.find("consents",consentId)?.optString("otp","")?:"";val input=e("6-digit OTP")
        AlertDialog.Builder(this).setTitle("Formal credit consent").setMessage("Review sanction terms before confirmation.\nDemo OTP: $code").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(V5OtpConsentService(this).verify(consentId,input.text.toString())){store.add("formal_credits",JSONObject().apply{put("id",id);put("borrowerProfileId",b.text.toString().trim());put("institution",i.text.toString().trim());put("sanctionAmount",amount);put("sanctionedRoi",rate);put("tenureMonths",months);put("reference",ref.text.toString().trim());put("documentIds",docs.text.toString().trim());put("consentId",consentId);put("status","REGISTERED");put("createdAt",System.currentTimeMillis());put("updatedAt",System.currentTimeMillis())});V5WorkflowRepository(this).appendAudit(id,"FORMAL_CREDIT_REGISTERED",b.text.toString(),"borrower consent OTP verified");toast("Formal credit registered after consent");d.dismiss();finish()}else input.error="Incorrect OTP"}};d.show()}
    }
    private fun toast(s:String)=Toast.makeText(this@V5FormalCreditActivity,s,Toast.LENGTH_LONG).show()
}
