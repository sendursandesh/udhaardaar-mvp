package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.widget.*
import org.json.JSONObject
import java.util.Locale
import kotlin.random.Random

/** V6.2 Financial Centre. QR Khata uses an identified relationship and explicit OTP consent before ledger mutation. */
class V62ExtendedModulesActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private lateinit var root: LinearLayout
    private val store by lazy { V5LocalStore(this) }
    private lateinit var khataParty: EditText
    private lateinit var khataAmount: EditText
    private lateinit var khataNote: EditText
    private lateinit var khataDirection: Spinner

    private fun dp(v:Int)=(v*d).toInt()
    private fun input(h:String)=ArthSaathiV62Design.input(this,h)
    private fun btn(s:String,c:Int=ArthSaathiV62Design.BLUE,go:()->Unit)=ArthSaathiV62Design.button(this,s,c,go)
    private fun add(v:android.view.View,t:Int=8)=ArthSaathiV62Design.add(root,v,t)

    override fun onCreate(b:Bundle?){
        super.onCreate(b)
        window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        when(intent.getStringExtra("openSection")){
            "FORMAL" -> formal()
            "FUNDING" -> funding()
            "CHARGECHECK" -> startActivity(Intent(this,V62ChargeCheckActivity::class.java))
            "QR_KHATA" -> khata()
            "TTMM" -> startActivity(Intent(this,V62TTMMActivity::class.java))
            "CREDIT_INTELLIGENCE" -> startActivity(Intent(this,V62CreditIntelligenceActivity::class.java))
            "LIABILITY" -> liability()
            "PEOPLE" -> people()
            "ADDRESS" -> address()
            "BENEFITS" -> benefits()
            "DOCUMENTS" -> documents()
            "REPORTS" -> reports()
            "RENTAL" -> startActivity(Intent(this,V62RentalLeaseActivity::class.java))
            else -> menu()
        }
    }

    private fun shell(title:String,sub:String){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(90));setBackgroundColor(ArthSaathiV62Design.BG)}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
        add(ArthSaathiV62Design.title(this,title,sub),0)
    }

    private fun menu(){
        shell("More • Financial Centre","All ArthSaathi services arranged as compact sub-folders")
        fun row(items:List<Pair<String,()->Unit>>) {
            val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            items.forEachIndexed { i,item ->
                val parts=item.first.split("|",limit=2)
                r.addView(
                    ArthSaathiV62Design.featureCard(this,parts[0],parts[1],item.second),
                    LinearLayout.LayoutParams(0,dp(88),1f).apply {
                        if(i>0) leftMargin=dp(4)
                        if(i<items.lastIndex) rightMargin=dp(4)
                    }
                )
            }
            add(r,5)
        }

        add(ArthSaathiV62Design.section(this,"RECORD • FINANCE • CHECK"),8)
        row(listOf(
            "FC|Formal\nCredit" to {openSection("FORMAL")},
            "FU|Funding /\nLending" to {openSection("FUNDING")},
            "CC|ChargeCheck" to {openSection("CHARGECHECK")}
        ))
        row(listOf(
            "QK|QR Udhaar\nKhata" to {openSection("QR_KHATA")},
            "TT|Together •\nShare & Settle" to {openSection("TTMM")},
            "CI|Credit\nIntelligence" to {openSection("CREDIT_INTELLIGENCE")}
        ))

        add(ArthSaathiV62Design.section(this,"PEOPLE • ASSETS • PROTECTION"),10)
        row(listOf(
            "LI|Liability\nVault" to {openSection("LIABILITY")},
            "PF|Family • Contacts" to {openSection("PEOPLE")},
            "AD|Address &\nLocation" to {openSection("ADDRESS")}
        ))
        row(listOf(
            "BE|Schemes &\nBenefits" to {openSection("BENEFITS")},
            "RE|Rental &\nLease" to {openSection("RENTAL")},
            "DC|Documents &\nNotes" to {openSection("DOCUMENTS")}
        ))

        add(ArthSaathiV62Design.section(this,"REPORT • SUPPORT • RETURN"),10)
        row(listOf(
            "RP|Reports &\nStatements" to {openSection("REPORTS")},
            "BK|Back to\nHome" to {finish()}
        ))
        add(ArthSaathiV62Design.text(this,"Legacy, Legal Assistance and AI Financial Advisor are intentionally grouped in the single Legacy & Claims hub on Home. Protect and MIS are also single-owner Home modules.",11.5f,ArthSaathiV62Design.MUTED),8)
    }

    private fun openSection(section:String) {
        if(section=="RENTAL") {
            startActivity(Intent(this,V62RentalLeaseActivity::class.java))
        } else {
            startActivity(Intent(this,V62ExtendedModulesActivity::class.java).apply { putExtra("openSection",section) })
        }
        finish()
    }

    private fun formal(){
        shell("Formal Credit","Record offers and hand off to ChargeCheck")
        val p=input("Bank / NBFC / institution");val n=input("Loan product");val a=input("Principal amount ₹");val r=input("Annual interest %");val t=input("Tenure months");val pf=input("Processing + other charges ₹")
        listOf(p,n,a,r,t,pf).forEach{add(it)}
        add(btn("SAVE FORMAL CREDIT OFFER"){val principal=a.text.toString().replace(",","").toDoubleOrNull();if(p.text.isBlank()||n.text.isBlank()||principal==null||principal<=0){Toast.makeText(this,"Enter institution, product and valid principal.",Toast.LENGTH_LONG).show();return@btn};val id=V62Store.id("FC");store.add(V62Store.FORMAL_CREDIT,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("provider",p.text.toString().trim());put("product",n.text.toString().trim());put("principal",principal);put("rate",r.text.toString());put("tenure",t.text.toString());put("charges",pf.text.toString());put("status","RECORDED");put("createdAt",System.currentTimeMillis())});V62EventBus.publish(V62Event(V62Events.FORMAL_LOAN_CHANGED,id));Toast.makeText(this,"Formal credit offer saved",Toast.LENGTH_SHORT).show()})
        add(btn("OPEN CHARGECHECK",ArthSaathiV62Design.GOLD){startActivity(Intent(this,V62ChargeCheckActivity::class.java))})
        add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun funding(){
        shell("Funding / Lending","User-initiated, consented profile sharing")
        val purpose=input("Purpose of funding *");val amount=input("Amount required ₹ *");val tenure=input("Preferred tenure months");val source=input("Preferred source")
        listOf(purpose,amount,tenure,source).forEach{add(it)}
        add(btn("REQUEST FUNDING — CONSENT + OTP",ArthSaathiV62Design.TEAL){
            if(purpose.text.isBlank()||amount.text.toString().replace(",","").toDoubleOrNull()?.let{it>0}!=true){Toast.makeText(this,"Purpose and a valid amount are required.",Toast.LENGTH_LONG).show();return@btn}
            val code=(100000+Random.nextInt(900000)).toString();val e=input("Enter 6-digit OTP")
            AlertDialog.Builder(this).setTitle("Funding profile-sharing consent").setMessage("Demo OTP: $code\nOnly the profile data required for this request will be shared with selected providers.").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create().also{dlg->dlg.setOnShowListener{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()!=code){e.error="Incorrect OTP";return@setOnClickListener};dlg.dismiss();val id=V62Store.id("FUND");store.add(V62Store.FUNDING_REQUESTS,JSONObject().apply{put("id",id);put("requesterId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("purpose",purpose.text.toString().trim());put("amount",amount.text.toString().replace(",","").toDoubleOrNull()?:0.0);put("tenure",tenure.text.toString());put("source",source.text.toString());put("consent","VERIFIED");put("status","REQUESTED");put("createdAt",System.currentTimeMillis())});V62Integration.recordConsent(this,V62Integration.currentUserId(this),id,"FUNDING_PROFILE_SHARING",true);V62EventBus.publish(V62Event(V62Events.FUNDING_REQUEST_CHANGED,id));Toast.makeText(this,"Funding request created",Toast.LENGTH_SHORT).show()}};dlg.show()}
        })
        add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun khata(){
        shell("QR Udhaar Khata","Scan / identify → consent → ledger → balance")
        khataParty=input("Counterparty name / mobile / profile ID *");khataAmount=input("Amount ₹ *");khataDirection=Spinner(this).apply{adapter=ArrayAdapter(this@V62ExtendedModulesActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("CREDIT","REPAYMENT"))};khataNote=input("Invoice / items / note")
        listOf(khataParty,khataAmount,khataDirection,khataNote).forEach{add(it,4)}
        add(btn("SCAN QR / IDENTIFY PARTY",ArthSaathiV62Design.TEAL){startActivityForResult(Intent(this,QrCreditScannerActivity::class.java),700)},8)
        add(btn("RECORD KHATA ENTRY — CONSENT + OTP",ArthSaathiV62Design.GREEN){recordKhata()})
        add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun recordKhata(){
        val a=khataAmount.text.toString().replace(",","").toDoubleOrNull()
        if(khataParty.text.isBlank()||a==null||a<=0){Toast.makeText(this,"Identify the party and enter a valid amount.",Toast.LENGTH_LONG).show();return}
        val cp=V62Integration.findCounterparties(this,khataParty.text.toString()).firstOrNull()
        if(cp==null){Toast.makeText(this,"Identify an existing V6.2 counterparty first. QR scan alone never exposes private history.",Toast.LENGTH_LONG).show();return}
        val owner=V62Integration.currentUserId(this);val isCredit=khataDirection.selectedItem.toString()=="CREDIT"
        val rel=store.all(V62Store.RELATIONSHIPS).firstOrNull{it.optString("ownerUserId")==owner&&it.optString("counterpartyId")==cp.optString("id")&&it.optString("type") in listOf("PERSONAL_CREDIT","TRADE_CREDIT")&&it.optString("status")!="CLOSED"}
        if(!isCredit&&rel==null){Toast.makeText(this,"No active credit relationship exists for this repayment.",Toast.LENGTH_LONG).show();return}
        if(!isCredit&&a>(rel?.optDouble("outstanding",0.0)?:0.0)+0.0001){Toast.makeText(this,"Repayment cannot exceed current outstanding principal.",Toast.LENGTH_LONG).show();return}
        val code=(100000+Random.nextInt(900000)).toString();val e=input("Enter 6-digit OTP")
        AlertDialog.Builder(this).setTitle("QR Khata consent").setMessage("Demo OTP: $code\nConfirm this ledger entry and the related balance update.").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create().also{dlg->dlg.setOnShowListener{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()!=code){e.error="Incorrect OTP";return@setOnClickListener};dlg.dismiss();commitKhata(cp,a,isCredit,rel,owner)}};dlg.show()}
    }

    private fun commitKhata(cp:JSONObject,a:Double,isCredit:Boolean,existing:JSONObject?,owner:String){
        val rel=existing?:JSONObject().apply{put("id",V62Store.id("REL"));put("ownerUserId",owner);put("counterpartyId",cp.optString("id"));put("counterpartyMobile",cp.optString("mobile"));put("type","PERSONAL_CREDIT");put("principal",0.0);put("amount",0.0);put("outstanding",0.0);put("status","ACTIVE");put("createdAt",System.currentTimeMillis());put("consentStatus","OTP_VERIFIED")}
        val oldOutstanding=rel.optDouble("outstanding",0.0);val oldPrincipal=rel.optDouble("amount",rel.optDouble("principal",0.0));val after=(oldOutstanding+if(isCredit)a else -a).coerceAtLeast(0.0)
        if(isCredit){rel.put("amount",oldPrincipal+a);rel.put("principal",oldPrincipal+a)}
        rel.put("outstanding",after);rel.put("status",if(after<=0.0&&oldPrincipal+a>0.0)"CLOSED" else "ACTIVE");rel.put("lastQrKhataAt",System.currentTimeMillis())
        if(existing==null)store.add(V62Store.RELATIONSHIPS,rel) else store.replace(V62Store.RELATIONSHIPS,rel)
        val id=V62Store.id("QK")
        store.add(V62Store.QR_KHATA,JSONObject().apply{put("id",id);put("relationshipId",rel.optString("id"));put("counterpartyId",cp.optString("id"));put("party",cp.optString("name"));put("amount",a);put("direction",if(isCredit)"CREDIT" else "REPAYMENT");put("note",khataNote.text.toString().trim());put("consentVerified",true);put("recordedBy",owner);put("createdAt",System.currentTimeMillis())})
        V62Integration.recordConsent(this,cp.optString("id"),rel.optString("id"),"QR_KHATA_CONFIRMATION",true)
        if(!isCredit){store.add(V62Store.REPAYMENTS,JSONObject().apply{put("id",V62Store.id("PAY"));put("relationshipId",rel.optString("id"));put("counterpartyId",cp.optString("id"));put("amount",a);put("principal",a);put("interest",0.0);put("date",java.text.SimpleDateFormat("yyyy-MM-dd",Locale.US).format(java.util.Date()));put("consentVerified",true);put("recordedBy",owner);put("createdAt",System.currentTimeMillis())});V62EventBus.publish(V62Event(V62Events.REPAYMENT_CHANGED,id))}
        V62EventBus.publish(V62Event(V62Events.QR_KHATA_CHANGED,id));V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED,rel.optString("id")));Toast.makeText(this,"QR Khata entry saved. Balance updated after OTP consent.",Toast.LENGTH_LONG).show()
    }

    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==700&&resultCode==RESULT_OK&&data!=null&&::khataParty.isInitialized){khataParty.setText(data.getStringExtra("vendor").orEmpty());khataAmount.setText(data.getStringExtra("amount").orEmpty());val inv=data.getStringExtra("invoice").orEmpty();val date=data.getStringExtra("date").orEmpty();khataNote.setText(listOf(inv,date).filter{it.isNotBlank()}.joinToString(" • "));Toast.makeText(this,"QR identified. Review details, then confirm with OTP.",Toast.LENGTH_LONG).show()}}

    private fun people(){shell("Family & Contacts","People connected to your financial journey");val n=input("Full name *");val m=input("Mobile");val p=input("PAN");val rel=input("Family / contact relationship");val role=input("Role");listOf(n,m,p,rel,role).forEach{add(it)};add(btn("SAVE PERSON / CONTACT"){if(n.text.isBlank())Toast.makeText(this,"Name required",Toast.LENGTH_SHORT).show()else{val id=V62Store.id("PERSON");store.add(V62Store.PEOPLE,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("name",n.text.toString().trim());put("mobile",m.text.toString().trim());put("pan",p.text.toString().trim().uppercase(Locale.getDefault()));put("relation",rel.text.toString().trim());put("role",role.text.toString().trim())});V62EventBus.publish(V62Event(V62Events.PROFILE_CHANGED,id));Toast.makeText(this,"Person saved",Toast.LENGTH_SHORT).show()}});add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})}

    private fun address(){shell("Address & Location","Location-assisted capture with user confirmation");val pin=input("PIN code");val line=input("Address line");val city=input("City");val district=input("District");val state=input("State");listOf(pin,line,city,district,state).forEach{add(it)};add(btn("SAVE CONFIRMED ADDRESS"){if(pin.text.length!=6||city.text.isBlank()||state.text.isBlank())Toast.makeText(this,"Enter a valid 6-digit PIN, city and state.",Toast.LENGTH_LONG).show()else{val id=V62Store.id("ADDR");store.add(V62Store.ADDRESSES,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("pin",pin.text.toString());put("line",line.text.toString());put("city",city.text.toString());put("district",district.text.toString());put("state",state.text.toString());put("source","USER_CONFIRMED")});V62EventBus.publish(V62Event(V62Events.ADDRESS_CHANGED,id));Toast.makeText(this,"Address saved",Toast.LENGTH_SHORT).show()}});add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})}

    private fun liability(){shell("Liability Vault","Obligations included in financial snapshot");val n=input("Liability / loan name *");val l=input("Lender");val o=input("Outstanding ₹");val r=input("Interest %");val due=input("Next due date");listOf(n,l,o,r,due).forEach{add(it)};add(btn("SAVE LIABILITY",ArthSaathiV62Design.RED){val out=o.text.toString().replace(",","").toDoubleOrNull();if(n.text.isBlank()||out==null||out<0)Toast.makeText(this,"Name and valid outstanding are required",Toast.LENGTH_LONG).show()else{val id=V62Store.id("LIAB");store.add(V62Store.LIABILITIES,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("name",n.text.toString().trim());put("lender",l.text.toString().trim());put("outstanding",out);put("rate",r.text.toString());put("due",due.text.toString())});V62EventBus.publish(V62Event(V62Events.LIABILITY_CHANGED,id));if(due.text.isNotBlank())V62Integration.addAlert(this,"LIABILITY_DUE","Liability due date: ${due.text}",id,"ACTION");Toast.makeText(this,"Liability saved",Toast.LENGTH_SHORT).show()}});add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})}

    private fun benefits(){shell("Government Schemes & Benefits","Eligibility records and reminders");val n=input("Scheme / benefit *");val p=input("Provider");val c=input("Eligibility / condition");listOf(n,p,c).forEach{add(it)};add(btn("SAVE BENEFIT & CONDITIONS",ArthSaathiV62Design.GREEN){if(n.text.isBlank())Toast.makeText(this,"Scheme name required",Toast.LENGTH_SHORT).show()else{val id=V62Store.id("BEN");store.add(V62Store.BENEFITS,JSONObject().apply{put("id",id);put("ownerUserId",V62Integration.currentUserId(this@V62ExtendedModulesActivity));put("name",n.text.toString().trim());put("provider",p.text.toString().trim());put("condition",c.text.toString().trim());put("acknowledged",true)});V62EventBus.publish(V62Event(V62Events.PROFILE_CHANGED,id));Toast.makeText(this,"Benefit saved",Toast.LENGTH_SHORT).show()}});add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})}

    private fun documents(){
        shell("Documents & Executed Notes","Promissory notes • evidence • policies")
        val owner=V62Integration.currentUserId(this)
        val docs=store.all(V62Store.DOCUMENTS).filter{it.optString("ownerUserId")==owner}.sortedByDescending{it.optLong("createdAt")}
        if(docs.isEmpty()) add(ArthSaathiV62Design.text(this,"No documents recorded yet.",12f,ArthSaathiV62Design.MUTED),10)
        docs.forEach{d->
            val type=d.optString("type","DOCUMENT"); val uri=d.optString("uri")
            add(ArthSaathiV62Design.text(this,"$type\n$uri",11f,ArthSaathiV62Design.NAVY,true),5)
        }
        add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})
    }

    private fun reports(){shell("Reports & Statements","Connected V6.2 records");val counts=listOf("Relationships" to V62Store.RELATIONSHIPS,"Repayments" to V62Store.REPAYMENTS,"Assets" to V62Store.ASSETS,"Insurance" to V62Store.INSURANCE,"Rental / Lease" to V62Store.RENTALS,"TTMM expenses" to V62Store.TTMM_EXPENSES,"QR Khata" to V62Store.QR_KHATA,"Liabilities" to V62Store.LIABILITIES,"ChargeCheck" to V62Store.CHARGECHECK,"Funding requests" to V62Store.FUNDING_REQUESTS,"Documents" to V62Store.DOCUMENTS);add(ArthSaathiV62Design.text(this,counts.joinToString("\n"){(n,k)->"$n: ${store.all(k).size}"},15f,ArthSaathiV62Design.NAVY,true),10);add(btn("BACK",ArthSaathiV62Design.NAVY){menu()})}

    override fun onBackPressed(){finish()}
}
