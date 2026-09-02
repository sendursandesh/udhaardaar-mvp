package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import org.json.JSONObject

/** V5 credit registration: profile selection -> terms -> documents -> consent (informal only) -> registration. */
class V5CreditRegistrationActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val otp by lazy { V5OtpConsentService(this) }
    private val consents = linkedMapOf<String, String>()
    private var packetCreditId = ""
    private var dpnCreated = false
    private var guaranteeCreated = false
    private lateinit var kind: Spinner
    private lateinit var direction: Spinner
    private lateinit var lender: EditText
    private lateinit var borrower: EditText
    private lateinit var guarantor: EditText
    private lateinit var guarantorMobile: EditText
    private lateinit var amount: EditText
    private lateinit var roi: EditText
    private lateinit var start: EditText
    private lateinit var end: EditText
    private lateinit var method: Spinner
    private lateinit var status: TextView

    private fun dp(v:Int)= (v*resources.displayMetrics.density).toInt()
    private fun e(h:String,max:Int=0)=EditText(this).apply{hint=h;setSingleLine(true);textSize=16f;minHeight=dp(52);setPadding(dp(12),dp(8),dp(12),dp(8));if(max>0)filters=arrayOf(android.text.InputFilter.LengthFilter(max));setOnFocusChangeListener{v,f->if(f)v.post{v.requestRectangleOnScreen(Rect(0,0,v.width,v.height),true)}}}
    private fun add(r:LinearLayout,v:View,h:Int=0){r.addView(v,LinearLayout.LayoutParams(-1,if(h>0)dp(h) else ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,dp(5),0,dp(5))});v.minimumHeight=dp(52)}
    private fun btn(s:String,a:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=15f;minHeight=dp(54);setOnClickListener{a()}}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}

    private fun show(){
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(18),dp(18),dp(24))}
        add(r,TextView(this).apply{text="UDHAARDAAR V5 • CREDIT REGISTRATION";textSize=21f;setTextColor(Color.rgb(24,58,92))},62)
        add(r,TextView(this).apply{text="Find/create parties → final terms → generate documents → consent when required → register";textSize=13f},48)
        kind=Spinner(this).apply{adapter=ArrayAdapter(this@V5CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("INFORMAL","FORMAL"));onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){updateConsentUi()}}}
        direction=Spinner(this).apply{adapter=ArrayAdapter(this@V5CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Credit Given","Credit Received"))}
        add(r,kind);add(r,direction)
        lender=e("Lender / creditor name or profile ID *");borrower=e("Borrower / debtor name or profile ID *");guarantor=e("Guarantor name or profile ID (optional)");guarantorMobile=e("Guarantor mobile (required if added)",10);amount=e("Principal amount *");roi=e("Annual ROI %");start=e("Start date YYYY-MM-DD *");end=e("End date YYYY-MM-DD *")
        listOf(lender,borrower,guarantor,guarantorMobile,amount,roi,start,end).forEach{add(r,it)}
        method=Spinner(this).apply{adapter=ArrayAdapter(this@V5CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("EMI","Principal + Interest","Bullet / Full payment"))};add(r,method)
        add(r,btn("FIND / SELECT BORROWER PROFILE"){pickProfile(borrower,"Borrower")})
        add(r,btn("FIND / SELECT LENDER PROFILE"){pickProfile(lender,"Lender")})
        add(r,btn("FIND / SELECT GUARANTOR PROFILE"){pickProfile(guarantor,"Guarantor")})
        add(r,btn("CREATE NEW PARTY PROFILE"){startActivity(Intent(this,V5PartyActivity::class.java))})
        add(r,btn("CREATE / REFRESH DIGITAL DOCUMENT PACKET"){prepareDocuments()})
        status=TextView(this).apply{text="Documents must be generated before informal consent. Formal credits do not require counterparty OTP.";textSize=13f;setPadding(0,dp(8),0,dp(8))};add(r,status,78)
        add(r,btn("CONSENT: BORROWER"){requestConsent("BORROWER",borrower.text.toString())})
        add(r,btn("CONSENT: GUARANTOR"){if(guarantor.text.toString().trim().isEmpty())toast("No guarantor added")else requestConsent("GUARANTOR",guarantor.text.toString())})
        add(r,btn("CONFIRM & REGISTER CREDIT"){register()});add(r,btn("HOME"){goHome()});add(r,btn("BACK"){finish()})
        val scroll=ScrollView(this).apply{isFillViewport=true;addView(r,ViewGroup.LayoutParams(-1,-2))};ViewCompat.setOnApplyWindowInsetsListener(scroll){v,i->val b=i.getInsets(WindowInsetsCompat.Type.ime()).bottom;v.setPadding(v.paddingLeft,v.paddingTop,v.paddingRight,maxOf(b,dp(24)));i};setContentView(scroll)
    }

    private fun updateConsentUi(){
        val formal=kind.selectedItem?.toString()=="FORMAL"
        status.text=if(formal)"FORMAL credit: no lender/borrower/guarantor counterparty OTP. Register against bank/document evidence." else "INFORMAL credit: borrower OTP consent is mandatory; guarantor OTP consent is mandatory when a guarantor is present."
    }

    private fun pickProfile(target:EditText,role:String){
        val profiles=store.all("profiles");if(profiles.isEmpty()){AlertDialog.Builder(this).setTitle("No saved profiles").setMessage("Create the $role profile first.").setPositiveButton("CREATE") { _,_->startActivity(Intent(this,V5PartyActivity::class.java))}.setNegativeButton("CANCEL",null).show();return}
        val labels=profiles.map{"${it.optString("name")} • ${it.optString("mobile")} • ${it.optString("id")}"}.toTypedArray()
        AlertDialog.Builder(this).setTitle("Select $role").setItems(labels){_,which->target.setText(profiles[which].optString("id"));target.setTag(profiles[which].optString("id"));showProfileHistory(profiles[which])}.setNegativeButton("CANCEL",null).show()
    }
    private fun showProfileHistory(p:JSONObject){
        val id=p.optString("id");val related=store.all("credits").filter{it.optString("borrowerProfileId")==id||it.optString("lenderProfileId")==id};Toast.makeText(this,"${p.optString("name")}: ${related.size} prior credit record(s)",Toast.LENGTH_LONG).show()
    }
    private fun profileId(field:EditText)=field.tag?.toString()?.takeIf{it.isNotBlank()}?:store.all("profiles").firstOrNull{it.optString("id")==field.text.toString().trim()||it.optString("name").equals(field.text.toString().trim(),true)}?.optString("id","")?:""

    private fun prepareDocuments(){
        val lenderId=profileId(lender);val borrowerId=profileId(borrower);val principal=amount.text.toString().toDoubleOrNull()?:0.0
        if(lenderId.isBlank()||borrowerId.isBlank()){toast("Select existing lender and borrower profiles, or create them first");return}
        if(lenderId==borrowerId){toast("Lender and borrower cannot be the same profile");return}
        if(principal<=0||start.text.isBlank()||end.text.isBlank()){toast("Complete principal and dates first");return}
        if(guarantor.text.isNotBlank()&&!Regex("^[6-9][0-9]{9}$").matches(guarantorMobile.text.toString().trim())){toast("Enter valid guarantor mobile");return}
        packetCreditId="CR-${System.currentTimeMillis()}";dpnCreated=false;guaranteeCreated=false;consents.clear()
        val rate=roi.text.toString().toDoubleOrNull()?:0.0
        store.replace("documents",JSONObject().apply{put("id","DPN-$packetCreditId");put("type","Demand Promissory Note");put("creditId",packetCreditId);put("content",V5GuarantorAndDocuments.generateDpnTemplate(packetCreditId,borrower.text.toString(),lender.text.toString(),principal,rate,start.text.toString(),end.text.toString()));put("status","DRAFT");put("version",1);put("createdAt",System.currentTimeMillis())});dpnCreated=true
        val g=guarantor.text.toString().trim();if(g.isNotBlank()){store.replace("documents",JSONObject().apply{put("id","GUA-$packetCreditId");put("type","Guarantor Guarantee");put("creditId",packetCreditId);put("content",V5GuarantorAndDocuments.generateGuaranteeTemplate(packetCreditId,V5GuarantorAndDocuments.GuarantorProfile(guarantorId(guarantor),g,guarantorMobile.text.toString().trim(),""),borrower.text.toString(),principal));put("status","DRAFT");put("version",1);put("createdAt",System.currentTimeMillis())});guaranteeCreated=true}
        status.text="Packet $packetCreditId CREATED. DPN${if(guaranteeCreated)" + Guarantor Guarantee" else ""} ready for review before consent.";toast("Digital documents generated before consent")
    }
    private fun guarantorId(f:EditText)=profileId(f).ifBlank{"G-${System.currentTimeMillis()}"}

    private fun requestConsent(party:String,recipient:String){
        if(kind.selectedItem.toString()=="FORMAL"){toast("Formal credit does not require counterparty OTP");return}
        if(!dpnCreated||packetCreditId.isBlank()){toast("Generate digital documents before requesting consent");return}
        if(recipient.trim().length<2){toast("Select the $party profile first");return}
        if(party=="GUARANTOR"&&!Regex("^[6-9][0-9]{9}$").matches(guarantorMobile.text.toString().trim())){toast("Enter valid guarantor mobile");return}
        val id=otp.issue(packetCreditId,"CREDIT_${party}_CONSENT",recipient);val code=store.find("consents",id)?.optString("otp","")?:"";val input=e("Enter 6-digit OTP",6)
        val d=AlertDialog.Builder(this).setTitle("$party CONSENT OTP").setMessage("Demo OTP: $code\nLive SMS provider is required for production delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{if(otp.verify(id,input.text.toString())){consents[party]=id;store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",packetCreditId);put("event","${party}_CONSENT_OTP_VERIFIED");put("consentId",id);put("at",System.currentTimeMillis())});status.text="$party consent VERIFIED for $packetCreditId";d.dismiss()}else input.error="Incorrect OTP"}};d.show()
    }

    private fun register(){
        val lenderId=profileId(lender);val borrowerId=profileId(borrower);val principal=amount.text.toString().toDoubleOrNull()?:0.0;val formal=kind.selectedItem.toString()=="FORMAL";val g=guarantor.text.toString().trim()
        if(lenderId.isBlank()||borrowerId.isBlank()||principal<=0){toast("Select/create valid lender and borrower profiles and enter principal");return}
        if(!dpnCreated||packetCreditId.isBlank()){toast("Create the digital document packet first");return}
        if(g.isNotBlank()&&!guaranteeCreated){toast("Create guarantor guarantee first");return}
        if(!formal&&!consents.containsKey("BORROWER")){toast("Borrower OTP consent is mandatory for informal credit");return}
        if(!formal&&g.isNotBlank()&&!consents.containsKey("GUARANTOR")){toast("Guarantor OTP consent is mandatory when a guarantor is present");return}
        val creditId=packetCreditId
        store.replace("credits",JSONObject().apply{put("id",creditId);put("creditType",if(formal)"FORMAL" else "INFORMAL");put("direction",direction.selectedItem.toString());put("lenderProfileId",lenderId);put("borrowerProfileId",borrowerId);put("lender",lender.text.toString());put("borrower",borrower.text.toString());put("guarantor",g);put("amount",principal);put("outstanding",principal);put("roi",roi.text.toString().replace("%",""));put("repaymentMethod",method.selectedItem.toString());put("start",start.text.toString());put("end",end.text.toString());put("dpn","DPN-$creditId");put("guarantee",if(guaranteeCreated)"GUA-$creditId" else "");put("borrowerConsent",consents["BORROWER"]?:"NOT_REQUIRED_FORMAL");put("guarantorConsent",consents["GUARANTOR"]?:"NOT_REQUIRED_FORMAL");put("status","REGISTERED");put("registeredAt",System.currentTimeMillis())})
        store.all("documents").filter{it.optString("creditId")==creditId}.forEach{it.put("status","EXECUTED_PENDING_ARCHIVE");it.put("executedAt",System.currentTimeMillis());store.replace("documents",it)}
        store.add("audit",JSONObject().apply{put("id","AUD-${System.currentTimeMillis()}");put("entityId",creditId);put("event","CREDIT_REGISTERED");put("at",System.currentTimeMillis());put("details",if(formal)"formal credit; counterparty OTP not required" else "informal credit; required OTP consents verified")})
        toast("Credit registered successfully");goHome()
    }
    private fun goHome(){startActivity(Intent(this,V5HomeActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP));finish()}
    private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
