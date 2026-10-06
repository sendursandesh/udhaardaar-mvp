package com.udhaardaar.mvp

import android.app.Activity
import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputFilter
import android.text.InputType
import android.widget.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/** Canonical executable surface for the consolidated ArthSaathi architecture. */
class ArthSaathiMasterModuleActivity : Activity() {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val records get() = ArthSaathiDataStore.records()

    override fun onCreate(state: Bundle?) {
        super.onCreate(state)
        render(intent.getStringExtra("module") ?: ArthSaathiNavigation.HOME)
    }

    private fun render(id: String) {
        val m = ArthSaathiArchitectureRegistry.canonicalModule(id)
        val root = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL; setPadding(24,24,24,24) }
        root.addView(TextView(this).apply { text = m?.title ?: "ArthSaathi"; textSize = 26f })
        root.addView(TextView(this).apply { text = "Canonical destination • " + (m?.area ?: "HOME"); textSize = 14f })
        val scroll = ScrollView(this)
        val body = LinearLayout(this).apply { orientation = LinearLayout.VERTICAL }
        scroll.addView(body); root.addView(scroll, LinearLayout.LayoutParams(-1,0,1f))
        root.addView(Button(this).apply { text = "← Home"; setOnClickListener { finish() } })
        setContentView(root)
        when(id) {
            ArthSaathiNavigation.HOME -> home(body)
            ArthSaathiNavigation.REGISTER_CREDIT -> registerCredit(body)
            ArthSaathiNavigation.LOANS_UDHAAR -> loans(body)
            ArthSaathiNavigation.REPAYMENT -> repayment(body)
            ArthSaathiNavigation.REPAYMENT_SCHEDULE -> simple(body,"REPAYMENT_SCHEDULE","Repayment Schedule",listOf("Account","Installment / EMI","Due date","Principal","Interest","Status"))
            ArthSaathiNavigation.QR_KHATA -> simple(body,"QR_KHATA","QR Khata",listOf("Merchant / customer","Amount","QR / invoice reference","Note"))
            ArthSaathiNavigation.GROUP_KHATA -> simple(body,"GROUP_EXPENSE","Group Khata / Group Expenses",listOf("Group","Total expense","Members","Purpose / notes"))
            ArthSaathiNavigation.MIS -> mis(body)
            ArthSaathiNavigation.SWITCH_ANALYSIS -> switchAnalysis(body)
            ArthSaathiNavigation.PEOPLE -> simple(body,"PERSON","People & Relationships",listOf("Name","Mobile","Relationship / role","Address / PIN"))
            ArthSaathiNavigation.BORROWER_PROFILE -> simple(body,"BORROWER_PROFILE","Borrower / Counterparty Profile",listOf("Name","Mobile","Aadhaar / PAN reference","Business / personal","Consent status"))
            ArthSaathiNavigation.LOCATION_ADDRESS -> simple(body,"LOCATION_ADDRESS","PIN / Maps Address",listOf("PIN code","Address","City","State","Maps reference"))
            ArthSaathiNavigation.ASSET_VAULT -> simple(body,"ASSET","Asset Vault",listOf("Asset","Category","Current value","Nominee / owner","Evidence"))
            ArthSaathiNavigation.LIABILITY_VAULT -> simple(body,"LIABILITY","Liability Vault",listOf("Liability","Lender","Outstanding","Due date","Evidence"))
            ArthSaathiNavigation.PORTFOLIO -> simple(body,"PORTFOLIO","Portfolio & Investments",listOf("Investment","Category","Invested value","Current value","Risk / horizon"))
            ArthSaathiNavigation.PROTECTION -> simple(body,"PROTECTION","Insurance & Protection",listOf("Policy / cover","Provider","Coverage","Expiry / renewal","Eligibility"))
            ArthSaathiNavigation.BENEFITS -> simple(body,"BENEFIT","Benefits & Refunds",listOf("Benefit / refund","Source","Value","Status","Evidence"))
            ArthSaathiNavigation.CHARGECHECK -> simple(body,"CHARGECHECK","ChargeCheck",listOf("Account","Sanctioned charge","Actual charge","Variance","Evidence"))
            ArthSaathiNavigation.CLAIMS -> simple(body,"CLAIM","Claim Assistance",listOf("Claim / asset","Claimant","Nominee","Status","Evidence / action"))
            ArthSaathiNavigation.WILL_LEGACY -> simple(body,"LEGACY","Will / Inheritance / Legacy",listOf("Asset","Beneficiary","Executor","Instruction","Draft version"))
            ArthSaathiNavigation.LEGAL -> simple(body,"LEGAL_CASE","Legal Help",listOf("Domain","City / locality","Issue","Related module","Status"))
            ArthSaathiNavigation.ADVOCATES -> simple(body,"ADVOCATE","Advocate Directory",listOf("Advocate / firm","Domain","City","Verification","Contact"))
            ArthSaathiNavigation.DOCUMENT_VAULT -> simple(body,"DOCUMENT","Document Vault",listOf("Document","Type","Related account/module","Evidence status","Reference"))
            ArthSaathiNavigation.DOCUMENT_CAPTURE -> simple(body,"DOCUMENT_CAPTURE","Document / Invoice Capture",listOf("Capture type","Invoice / document number","Date","Amount","QR / OCR reference"))
            ArthSaathiNavigation.AI_ADVISOR -> simple(body,"ADVISOR_ALERT","ArthSaathi AI Advisor",listOf("Signal","Reason","Related record","Risk / horizon","User action"))
            ArthSaathiNavigation.REVENUE -> simple(body,"REVENUE","Revenue & Payments",listOf("Service / transaction","Charge","Payment status","Gateway reference","Notes"))
            ArthSaathiNavigation.SECURITY_CONSENT -> simple(body,"CONSENT","Security & Consent",listOf("Protected action","Subject","Consent status","Timestamp","Audit reference"))
            ArthSaathiNavigation.OTP_CONSENT -> simple(body,"OTP_CONSENT","OTP / Consent Gateway",listOf("Protected action","Mobile","Consent purpose","OTP provider reference","Verification status"))
            ArthSaathiNavigation.INTEGRATIONS -> simple(body,"INTEGRATION","Integrations",listOf("Provider","Capability","Environment","Status","Configuration reference"))
            else -> body.addView(TextView(this).apply { text = "Canonical module"; textSize = 17f })
        }
    }

    private fun home(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "Your Money. Your Records. Your Rights.\n\nOne canonical destination per module. Register Credit creates new credits; Loans & Udhaar owns existing accounts; Repayment Centre is the single repayment engine; MIS is the single cross-module information layer."
            textSize = 16f
        })
        ArthSaathiArchitectureRegistry.modules.filter { it.id != "HOME" }.groupBy { it.area }.forEach { (area, list) ->
            body.addView(TextView(this).apply { text = area; textSize = 18f; setPadding(0,20,0,6) })
            list.forEach { mod ->
                body.addView(Button(this).apply {
                    text = mod.title
                    setOnClickListener { startActivity(android.content.Intent(this@ArthSaathiMasterModuleActivity, ArthSaathiMasterModuleActivity::class.java).putExtra("module",mod.id)) }
                })
            }
        }
    }

    private fun registerCredit(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "NEW CREDIT ONLY\nIdentify party → nature → credit-specific fields → method → repayment → guarantor → digital document → consent/OTP → registration."
            textSize = 16f
        })
        val party = field(body,"Borrower / counterparty name")
        val mobile = field(body,"Counterparty mobile (10 digits)",false,10)
        val nature = Spinner(this).also {
            it.adapter = ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,ArthSaathiArchitectureRegistry.creditNatureOptions); body.addView(it)
        }
        val amount = field(body,"Principal / amount",true)
        val roi = field(body,"ROI % (not used for lease)",true)
        val method = field(body,"Method: Cash / UPI / NEFT / Other")
        val terms = field(body,"Repayment method / terms")
        val guarantor = field(body,"Guarantor (optional)")
        val due = field(body,"Due date"); due.setOnClickListener { pickDate(due) }
        val doc = field(body,"Digital document / agreement reference")
        body.addView(Button(this).apply {
            text = "REGISTER CREDIT"
            setOnClickListener {
                if(party.text.toString().trim().isEmpty()){ party.error="Required"; return@setOnClickListener }
                if(amount.text.toString().trim().isEmpty()){ amount.error="Required"; return@setOnClickListener }
                val lease = nature.selectedItem.toString().contains("Rental")
                val o=JSONObject()
                o.put("id","CR-"+System.currentTimeMillis()); o.put("type","CREDIT")
                o.put("party",party.text.toString().trim()); o.put("mobile",mobile.text.toString())
                o.put("nature",nature.selectedItem.toString()); o.put("amount",amount.text.toString().toDoubleOrNull()?:0.0)
                o.put("roi",if(lease)0.0 else (roi.text.toString().toDoubleOrNull()?:0.0))
                o.put("method",method.text.toString()); o.put("repaymentTerms",terms.text.toString())
                o.put("guarantor",guarantor.text.toString()); o.put("dueDate",due.text.toString())
                o.put("document",doc.text.toString()); o.put("paid",0.0); o.put("status","ACTIVE")
                o.put("consentStatus","PENDING"); o.put("createdAt",System.currentTimeMillis())
                ArthSaathiDataStore.append(o)
                Toast.makeText(this@ArthSaathiMasterModuleActivity,"Recorded. Production OTP/consent remains required for protected registration.",Toast.LENGTH_LONG).show()
                render(ArthSaathiNavigation.LOANS_UDHAAR)
            }
        })
    }

    private fun loans(body: LinearLayout) {
        body.addView(TextView(this).apply { text="ACCOUNT CENTRE — active and closed credits. Tap an account for full details."; textSize=16f })
        var found=false
        for(i in 0 until records.length()){
            val o=records.getJSONObject(i); if(o.optString("type")!="CREDIT") continue
            found=true
            val outstanding=(o.optDouble("amount")-o.optDouble("paid")).coerceAtLeast(0.0)
            body.addView(Button(this).apply { text=o.optString("party")+" • "+o.optString("nature")+"\nOutstanding ₹"+money(outstanding); setOnClickListener{detail(o)} })
        }
        if(!found) body.addView(TextView(this).apply{text="\nNo credit accounts registered yet."})
    }

    private fun detail(o:JSONObject){
        val out=(o.optDouble("amount")-o.optDouble("paid")).coerceAtLeast(0.0)
        AlertDialog.Builder(this).setTitle("Account "+o.optString("id")).setMessage(
            "Party: "+o.optString("party")+"\nNature: "+o.optString("nature")+"\nAmount: ₹"+money(o.optDouble("amount"))+
            "\nROI: "+o.optDouble("roi")+"%\nMethod: "+o.optString("method")+"\nTerms: "+o.optString("repaymentTerms")+
            "\nGuarantor: "+o.optString("guarantor")+"\nDue: "+o.optString("dueDate")+"\nDocument: "+o.optString("document")+
            "\nPaid: ₹"+money(o.optDouble("paid"))+"\nOutstanding: ₹"+money(out)+"\nConsent: "+o.optString("consentStatus")+"\nStatus: "+o.optString("status")
        ).setPositiveButton("OK",null).show()
    }

    private fun repayment(body:LinearLayout){
        body.addView(TextView(this).apply{text="SINGLE REPAYMENT ENGINE\nProduction repayment changes must be consent-gated.";textSize=16f})
        for(i in 0 until records.length()){
            val o=records.getJSONObject(i); if(o.optString("type")!="CREDIT"||o.optString("status")=="CLOSED") continue
            body.addView(Button(this).apply{text="Record repayment • "+o.optString("party");setOnClickListener{
                val e=EditText(this@ArthSaathiMasterModuleActivity).apply{hint="Repayment amount";inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL}
                AlertDialog.Builder(this@ArthSaathiMasterModuleActivity).setTitle("Consent-gated repayment").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM"){_,_->
                    val paid=(o.optDouble("paid")+(e.text.toString().toDoubleOrNull()?:0.0)).coerceAtMost(o.optDouble("amount"))
                    o.put("paid",paid); if(paid>=o.optDouble("amount"))o.put("status","CLOSED"); ArthSaathiDataStore.replace(records)
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,"Saved; production OTP authorization must be connected.",Toast.LENGTH_LONG).show(); render(ArthSaathiNavigation.REPAYMENT)
                }.show()
            }})
        }
    }

    private fun mis(body:LinearLayout){
        var credit=0.0;var paid=0.0;var group=0.0;var assets=0.0;var benefits=0.0
        for(i in 0 until records.length()){val o=records.getJSONObject(i);when(o.optString("type")){
            "CREDIT"->{credit+=o.optDouble("amount");paid+=o.optDouble("paid")}
            "GROUP_EXPENSE"->group+=o.optDouble("f1")
            "ASSET","PORTFOLIO"->assets+=o.optDouble("f2",o.optDouble("f3"))
            "BENEFIT"->benefits+=o.optDouble("f2")
        }}
        body.addView(TextView(this).apply{text="MIS / MONEY REPORT\n\nACTUAL RECORDED NUMBERS FIRST\nCredits registered: ₹"+money(credit)+"\nRepayments recorded: ₹"+money(paid)+"\nCredit outstanding: ₹"+money((credit-paid).coerceAtLeast(0.0))+"\nGroup expenses: ₹"+money(group)+"\nAssets / portfolio: ₹"+money(assets)+"\nBenefits / refunds value generated: ₹"+money(benefits)+"\n\nCharts are presentation layers over recorded values.";textSize=16f})
    }

    private fun switchAnalysis(body:LinearLayout){
        body.addView(TextView(this).apply{text="PORTFOLIO SWITCH ANALYSIS\nCompare return, cost, risk and opportunity cost before suggesting a switch. No automatic money movement.";textSize=16f})
        val current=field(body,"Current annual return %",true);val alternative=field(body,"Alternative annual return %",true);val value=field(body,"Amount considered",true)
        body.addView(Button(this).apply{text="CALCULATE OPPORTUNITY COST";setOnClickListener{
            val c=current.text.toString().toDoubleOrNull()?:0.0;val a=alternative.text.toString().toDoubleOrNull()?:0.0;val v=value.text.toString().toDoubleOrNull()?:0.0
            AlertDialog.Builder(this@ArthSaathiMasterModuleActivity).setTitle("Explainable comparison").setMessage("Return difference: "+(a-c)+" percentage points\nIndicative annual opportunity difference: ₹"+money(v*(a-c)/100.0)+"\nRisk, cost and eligibility must also be reviewed.").setPositiveButton("OK",null).show()
        }})
    }

    private fun simple(body:LinearLayout,type:String,title:String,hints:List<String>){
        body.addView(TextView(this).apply{text=title+"\n";textSize=17f})
        val fields=hints.map{field(body,it,it.contains("value",true)||it.contains("charge",true)||it.contains("outstanding",true))}
        body.addView(Button(this).apply{text="SAVE";setOnClickListener{
            val o=JSONObject();o.put("id",type+"-"+System.currentTimeMillis());o.put("type",type);fields.forEachIndexed{idx,e->o.put("f"+idx,e.text.toString())};o.put("createdAt",System.currentTimeMillis());ArthSaathiDataStore.append(o)
            Toast.makeText(this@ArthSaathiMasterModuleActivity,title+" record saved",Toast.LENGTH_SHORT).show()
        }})
    }

    private fun field(parent:LinearLayout,hint:String,number:Boolean=false,max:Int?=null):EditText{
        return EditText(this).apply{this.hint=hint;if(number)inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL;if(max!=null)filters=arrayOf(InputFilter.LengthFilter(max));parent.addView(this)}
    }
    private fun pickDate(target:EditText){val c=Calendar.getInstance();DatePickerDialog(this,{_,y,m,d->target.setText(dateFormat.format(GregorianCalendar(y,m,d).time))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}
    private fun money(v:Double)=String.format(Locale.US,"%.2f",v)
}
