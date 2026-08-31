package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import kotlin.random.Random

/** End-to-end V5 credit registration. A credit cannot be registered until required parties consent and documents exist. */
class V5CreditRegistrationActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val otpService by lazy { V5OtpConsentService(this) }
    private val credits by lazy { V5CreditRepository(this) }
    private val consents = linkedMapOf<String,String>()
    private var dpnCreated = false
    private var guaranteeCreated = false
    private var documentUri = ""
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

    private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true);setPadding(16,8,16,8)}
    private fun add(r:LinearLayout,v:View,h:Int=58){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
    private fun btn(s:String,fn:()->Unit)=Button(this).apply{text=s;isAllCaps=false;setOnClickListener{fn()}}
    override fun onCreate(b:Bundle?){super.onCreate(b);show()}
    private fun show(){
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,18,20,24)}
        r.addView(TextView(this).apply{text="UDHAARDAAR V5 • CREDIT REGISTRATION";textSize=23f;setTextColor(Color.rgb(24,58,92))})
        r.addView(TextView(this).apply{text="Parties → terms → documents → consent → confirmation";textSize=13f})
        direction=Spinner(this).apply{adapter=ArrayAdapter(this@V5CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Credit Given","Credit Received"))}
        add(r,direction)
        lender=e("Lender / creditor full name or profile ID *"); borrower=e("Borrower / debtor full name or profile ID *"); guarantor=e("Guarantor name (optional)"); guarantorMobile=e("Guarantor mobile (required if guarantor added)",10); amount=e("Principal amount *"); roi=e("Annual ROI %"); start=e("Start date YYYY-MM-DD *"); end=e("End date YYYY-MM-DD *")
        listOf(lender,borrower,guarantor,guarantorMobile,amount,roi,start,end).forEach{add(r,it)}
        add(r,btn("CREATE / REFRESH DIGITAL DOCUMENT PACKET"){prepareDocuments()})
        add(r,btn("CONSENT: LENDER"){requestConsent("LENDER",lender.text.toString())})
        add(r,btn("CONSENT: BORROWER"){requestConsent("BORROWER",borrower.text.toString())})
        add(r,btn("CONSENT: GUARANTOR"){if(guarantor.text.trim().isEmpty()){toast("No guarantor added")}else requestConsent("GUARANTOR",guarantor.text.toString())})
        status=TextView(this).apply{text="Required before confirmation: DPN + required guarantee + lender/borrower consent + guarantor consent when applicable.";textSize=14f};add(r,status,90)
        add(r,btn("CONFIRM & REGISTER CREDIT"){register()})
        add(r,btn("HOME"){goHome()})
        add(r,btn("BACK",){finish()})
        setContentView(ScrollView(this).apply{addView(r)})
    }
    private fun prepareDocuments(){
        val id="CR-${System.currentTimeMillis()}"; val l=lender.text.toString().trim();val b=borrower.text.toString().trim();val a=amount.text.toString().toDoubleOrNull()?:0.0;val r=roi.text.toString().toDoubleOrNull()?:0.0
        if(l.length<2||b.length<2||a<=0||start.text.isBlank()||end.text.isBlank()){toast("Complete lender, borrower, amount and dates first");return}
        val dpn=V5GuarantorAndDocuments.generateDpnTemplate(id,b,l,a,r,start.text.toString(),end.text.toString())
        store.replace("documents",JSONObject().apply{put("id","DPN-$id");put("type","Demand Promissory Note");put("creditId",id);put("content",dpn);put("status","DRAFT");put("createdAt",System.currentTimeMillis())})
        dpnCreated=true
        if(guarantor.text.trim().isNotEmpty()){
            if(guarantorMobile.text.length!=10){toast("Enter guarantor mobile");return}
            val gp=V5GuarantorAndDocuments.GuarantorProfile("G-${System.currentTimeMillis()}",guarantor.text.toString(),guarantorMobile.text.toString(),"")
            val gdoc=V5GuarantorAndDocuments.generateGuaranteeTemplate(id,gp,b,a)
            store.replace("documents",JSONObject().apply{put("id","GUA-$id");put("type","Guarantor Guarantee");put("creditId",id);put("content",gdoc);put("status","DRAFT");put("createdAt",System.currentTimeMillis())})
            guaranteeCreated=true
        }
        status.text="Digital packet CREATED: DPN${if(guaranteeCreated?" + Guarantor Guarantee":"")}. Now obtain required OTP consents."
        toast("Digital document packet created before registration")
    }
    private fun requestConsent(party:String,recipient:String){
        if(recipient.trim().length<2){toast("Enter $party details first");return}
        if(party=="GUARANTOR" && guarantorMobile.text.length!=10){toast("Enter valid guarantor mobile");return}
        val entity="CR-PENDING";val id=otpService.issue(entity,"CREDIT_${party}_CONSENT",recipient);val otp=store.find("consents",id)?.optString("otp","")?:"";val input=e("Enter 6-digit OTP")
        AlertDialog.Builder(this).setTitle("$party CONSENT OTP").setMessage("Demo OTP: $otp\nLive SMS requires configured provider.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create().also{d->d.setOnShowListener{d.getButton(-1).setOnClickListener{if(otpService.verify(id,input.text.toString())){consents[party]=id;status.text="$party consent VERIFIED";d.dismiss()}else input.error="Incorrect OTP"}};d.show()}
    }
    private fun register(){
        val l=lender.text.toString().trim();val b=borrower.text.toString().trim();val a=amount.text.toString().toDoubleOrNull()?:0.0
        if(l.length<2||b.length<2||a<=0){toast("Complete lender, borrower and amount");return}
        if(!dpnCreated){toast("Create digital DPN before registering");return}
        if(guarantor.text.trim().isNotEmpty() && !guaranteeCreated){toast("Create guarantor guarantee before registering");return}
        if(!consents.containsKey("LENDER")||!consents.containsKey("BORROWER")){toast("Lender and borrower OTP consent are required");return}
        if(guarantor.text.trim().isNotEmpty()&&!consents.containsKey("GUARANTOR")){toast("Guarantor OTP consent is required");return}
        val id="CR-${System.currentTimeMillis()}";store.replace("credits",JSONObject().apply{put("id",id);put("direction",direction.selectedItem.toString());put("lender",l);put("borrower",b);put("guarantor",guarantor.text.toString());put("amount",a);put("roi",roi.text.toString());put("start",start.text.toString());put("end",end.text.toString());put("dpn","DPN-$id");put("guarantee",if(guaranteeCreated)"GUA-$id" else "");put("lenderConsent",consents["LENDER"]);put("borrowerConsent",consents["BORROWER"]);put("guarantorConsent",consents["GUARANTOR"]?:"");put("status","REGISTERED");put("registeredAt",System.currentTimeMillis())})
        toast("Credit registered only after required documents and consents");goHome()
    }
    private fun goHome(){startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP));finish()}
}
