package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject

/** End-to-end V5 credit registration: parties -> terms -> documents -> consent -> registration. */
class V5CreditRegistrationActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val otpService by lazy { V5OtpConsentService(this) }
    private val consents = linkedMapOf<String,String>()
    private var dpnCreated = false
    private var guaranteeCreated = false
    private lateinit var status: TextView
    private lateinit var direction: Spinner
    private lateinit var lender: EditText
    private lateinit var borrower: EditText
    private lateinit var guarantor: EditText
    private lateinit var guarantorMobile: EditText
    private lateinit var amount: EditText
    private lateinit var roi: EditText
    private lateinit var start: EditText
    private lateinit var end: EditText

    private fun e(h:String,max:Int=0)=EditText(this).apply{
        hint=h;setSingleLine(true);setTextSize(14f);setPadding(12,4,12,4)
        if(max>0)filters=arrayOf(android.text.InputFilter.LengthFilter(max))
        setOnFocusChangeListener { v,has -> if(has) v.post { v.requestRectangleOnScreen(Rect(0,0,v.width,v.height),true) } }
    }
    private fun add(r:LinearLayout,v:View,h:Int=46){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,2,0,2)})}
    private fun btn(s:String,fn:()->Unit)=Button(this).apply{text=s;isAllCaps=false;setTextSize(13f);setOnClickListener{fn()}}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
    private fun show(){
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(16,10,16,16)}
        r.addView(TextView(this).apply{text="UDHAARDAAR V5 • CREDIT REGISTRATION";textSize=20f;setTextColor(Color.rgb(24,58,92))})
        r.addView(TextView(this).apply{text="Parties → terms → documents → consent → confirmation";textSize=12f})
        direction=Spinner(this).apply{adapter=ArrayAdapter(this@V5CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Credit Given","Credit Received"))};add(r,direction,42)
        lender=e("Lender / creditor full name or profile ID *"); borrower=e("Borrower / debtor full name or profile ID *"); guarantor=e("Guarantor name (optional)"); guarantorMobile=e("Guarantor mobile (required if guarantor added)",10); amount=e("Principal amount *"); roi=e("Annual ROI %"); start=e("Start date YYYY-MM-DD *"); end=e("End date YYYY-MM-DD *")
        listOf(lender,borrower,guarantor,guarantorMobile,amount,roi,start,end).forEach{add(r,it)}
        add(r,btn("CREATE / REFRESH DIGITAL DOCUMENT PACKET"){prepareDocuments()},48)
        add(r,btn("CONSENT: LENDER"){requestConsent("LENDER",lender.text.toString())},46)
        add(r,btn("CONSENT: BORROWER"){requestConsent("BORROWER",borrower.text.toString())},46)
        add(r,btn("CONSENT: GUARANTOR"){if(guarantor.text.toString().trim().isEmpty())toast("No guarantor added") else requestConsent("GUARANTOR",guarantor.text.toString())},46)
        status=TextView(this).apply{text="Required: DPN + required guarantee + lender/borrower consent + guarantor consent when applicable.";textSize=12f};add(r,status,66)
        add(r,btn("CONFIRM & REGISTER CREDIT"){register()},48);add(r,btn("HOME"){goHome()},46);add(r,btn("BACK"){finish()},46)
        val scroll=ScrollView(this).apply{isFillViewport=true;isSmoothScrollingEnabled=true;addView(r)}
        ViewCompat.setOnApplyWindowInsetsListener(scroll){v,insets->val ime=insets.getInsets(WindowInsetsCompat.Type.ime()).bottom;v.setPadding(v.paddingLeft,v.paddingTop,v.paddingRight,ime);insets}
        setContentView(scroll);scroll.post{scroll.scrollTo(0,0)}
    }
    private fun prepareDocuments(){
        val id="CR-${System.currentTimeMillis()}";val l=lender.text.toString().trim();val b=borrower.text.toString().trim();val a=amount.text.toString().toDoubleOrNull()?:0.0;val rr=roi.text.toString().toDoubleOrNull()?:0.0
        if(l.length<2||b.length<2||a<=0||start.text.toString().isBlank()||end.text.toString().isBlank()){toast("Complete lender, borrower, amount and dates first");return}
        store.replace("documents",JSONObject().apply{put("id","DPN-$id");put("type","Demand Promissory Note");put("creditId",id);put("content",V5GuarantorAndDocuments.generateDpnTemplate(id,b,l,a,rr,start.text.toString(),end.text.toString()));put("status","DRAFT");put("createdAt",System.currentTimeMillis())});dpnCreated=true;guaranteeCreated=false
        if(guarantor.text.toString().trim().isNotEmpty()){
            if(!Regex("^[6-9][0-9]{9}$").matches(guarantorMobile.text.toString())){toast("Enter valid guarantor mobile");dpnCreated=false;return}
            val gp=V5GuarantorAndDocuments.GuarantorProfile("G-${System.currentTimeMillis()}",guarantor.text.toString(),guarantorMobile.text.toString(),"");val gd=V5GuarantorAndDocuments.generateGuaranteeTemplate(id,gp,b,a)
            store.replace("documents",JSONObject().apply{put("id","GUA-$id");put("type","Guarantor Guarantee");put("creditId",id);put("content",gd);put("status","DRAFT");put("createdAt",System.currentTimeMillis())});guaranteeCreated=true
        }
        status.text=if(guaranteeCreated) "Digital packet CREATED: DPN + Guarantor Guarantee. Obtain required OTP consents." else "Digital packet CREATED: DPN. Obtain required OTP consents.";toast("Digital document packet created before registration")
    }
    private fun requestConsent(party:String,recipient:String){
        if(recipient.trim().length<2){toast("Enter $party details first");return};if(party=="GUARANTOR"&&!Regex("^[6-9][0-9]{9}$").matches(guarantorMobile.text.toString())){toast("Enter valid guarantor mobile");return}
        val id=otpService.issue("CR-PENDING","CREDIT_${party}_CONSENT",recipient);val otp=store.find("consents",id)?.optString("otp","")?:"";val input=e("Enter 6-digit OTP",6)
        val d=AlertDialog.Builder(this).setTitle("$party CONSENT OTP").setMessage("Demo OTP: $otp\nLive SMS requires configured provider.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{if(otpService.verify(id,input.text.toString())){consents[party]=id;status.text="$party consent VERIFIED";d.dismiss()}else input.error="Incorrect OTP"}};d.show()
    }
    private fun register(){
        val l=lender.text.toString().trim();val b=borrower.text.toString().trim();val a=amount.text.toString().toDoubleOrNull()?:0.0
        if(l.length<2||b.length<2||a<=0){toast("Complete lender, borrower and amount");return};if(!dpnCreated){toast("Create digital DPN before registering");return};if(guarantor.text.toString().trim().isNotEmpty()&&!guaranteeCreated){toast("Create guarantor guarantee before registering");return};if(!consents.containsKey("LENDER")||!consents.containsKey("BORROWER")){toast("Lender and borrower OTP consent are required");return};if(guarantor.text.toString().trim().isNotEmpty()&&!consents.containsKey("GUARANTOR")){toast("Guarantor OTP consent is required");return}
        val id="CR-${System.currentTimeMillis()}";store.replace("credits",JSONObject().apply{put("id",id);put("direction",direction.selectedItem.toString());put("lender",l);put("borrower",b);put("guarantor",guarantor.text.toString());put("amount",a);put("roi",roi.text.toString());put("start",start.text.toString());put("end",end.text.toString());put("dpn","DPN-$id");put("guarantee",if(guaranteeCreated)"GUA-$id" else "");put("lenderConsent",consents["LENDER"]);put("borrowerConsent",consents["BORROWER"]);put("guarantorConsent",consents["GUARANTOR"]?:"");put("status","REGISTERED");put("registeredAt",System.currentTimeMillis())});toast("Credit registered after required documents and consents");goHome()
    }
    private fun goHome(){startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP));finish()}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
