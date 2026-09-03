package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.*
import org.json.JSONObject

class V5FormalCreditActivity : androidx.appcompat.app.AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true);setPadding(14,10,14,10)}
    private fun add(r:LinearLayout,v:View,h:Int=56){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
    private fun show(){val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,16,20,28)}
        add(r,TextView(this).apply{text="UDHAARDAAR V5 • Formal Credit";textSize=23f},62)
        add(r,TextView(this).apply{text="Institutional credit record • sanction terms • actual charges • consent • audit";textSize=13f},52)
        val borrower=e("Borrower / party profile ID *");val institution=e("Bank / NBFC / institution *");val sanction=e("Sanction amount ₹ *").apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL};val roi=e("Sanctioned annual ROI % *").apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL};val tenure=e("Tenure months *").apply{inputType=InputType.TYPE_CLASS_NUMBER};val ref=e("Sanction / account reference");val docs=e("Sanction/account document IDs");
        listOf(borrower,institution,sanction,roi,tenure,ref,docs).forEach{add(r,it)}
        add(r,Button(this).apply{text="REGISTER FORMAL CREDIT AFTER CONSENT";setOnClickListener{val amount=sanction.text.toString().toDoubleOrNull();val rate=roi.text.toString().removeSuffix("%").toDoubleOrNull();val months=tenure.text.toString().toIntOrNull();if(borrower.text.isBlank()||institution.text.isBlank()||amount==null||amount<=0||rate==null||rate<0||rate>100||months==null||months<1){toast("Complete valid borrower, institution, amount, ROI and tenure");return@setOnClickListener};val id="FCR-${System.currentTimeMillis()}";val c=V5OtpConsentService(this).issue(id,"FORMAL_CREDIT_CONSENT",borrower.text.toString());val otp=store.find("consents",c)?.optString("otp","")?:"";val input=e("6-digit OTP");AlertDialog.Builder(this).setTitle("Formal credit consent").setMessage("Review sanction terms before confirmation.\nDemo OTP: $otp\nProduction SMS gateway required for live OTP delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(-1).setOnClickListener{if(V5OtpConsentService(this).verify(c,input.text.toString())){store.add("formal_credits",JSONObject().apply{put("id",id);put("borrowerProfileId",borrower.text.toString());put("institution",institution.text.toString());put("sanctionAmount",amount);put("sanctionedRoi",rate);put("tenureMonths",months);put("reference",ref.text.toString());put("documentIds",docs.text.toString());put("consentId",c);put("status","REGISTERED");put("createdAt",System.currentTimeMillis());put("updatedAt",System.currentTimeMillis())});store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",id);put("event","FORMAL_CREDIT_REGISTERED");put("party",borrower.text.toString());put("timestamp",System.currentTimeMillis())});toast("Formal credit registered after consent");d.dismiss();finish()}else input.error="Incorrect OTP"}};d.show()}}},64)
        add(r,Button(this).apply{text="OPEN CHARGE COMPARISON";setOnClickListener{startActivity(android.content.Intent(this,V5ChargeComparisonActivity::class.java))}},56);add(r,Button(this).apply{text="BACK";setOnClickListener{finish()}},54);setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
