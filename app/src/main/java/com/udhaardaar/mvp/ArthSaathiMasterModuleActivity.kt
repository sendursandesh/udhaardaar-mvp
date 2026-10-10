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
            ArthSaathiNavigation.QR_KHATA -> simple(body,"QR_KHATA","QR Khata",listOf("Merchant / customer","Amount","QR / invoice reference","Note"))
            ArthSaathiNavigation.GROUP_KHATA -> simple(body,"GROUP_EXPENSE","Group Khata / Group Expenses",listOf("Group","Total expense","Members","Purpose / notes"))
            ArthSaathiNavigation.MIS -> mis(body)
            ArthSaathiNavigation.SWITCH_ANALYSIS -> switchAnalysis(body)
            ArthSaathiNavigation.PEOPLE -> people(body)
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
            ArthSaathiNavigation.AI_ADVISOR -> simple(body,"ADVISOR_ALERT","ArthSaathi AI Advisor",listOf("Signal","Reason","Related record","Risk / horizon","User action"))
            ArthSaathiNavigation.REVENUE -> simple(body,"REVENUE","Revenue & Payments",listOf("Service / transaction","Charge","Payment status","Gateway reference","Notes"))
            ArthSaathiNavigation.SECURITY_CONSENT -> securityConsent(body)
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
                val partyName = party.text.toString().trim()
                if (partyName.isEmpty()) { party.error = "Required"; return@setOnClickListener }
                val partyMobile = mobile.text.toString().trim()
                if (partyMobile.isNotEmpty() && !ArthSaathiCoreEngine.validateMobile(partyMobile)) {
                    mobile.error = "Enter a 10-digit mobile number"; return@setOnClickListener
                }
                val principal = amount.text.toString().trim().toDoubleOrNull()
                if (principal == null || !principal.isFinite() || principal <= 0.0) {
                    amount.error = "Enter an amount greater than zero"; return@setOnClickListener
                }
                val lease = nature.selectedItem.toString().contains("Rental", true) ||
                    nature.selectedItem.toString().contains("Lease", true)
                val rate = if (lease) 0.0 else roi.text.toString().trim().toDoubleOrNull()
                if (rate == null || !rate.isFinite() || rate < 0.0 || rate > 100.0) {
                    roi.error = "ROI must be between 0 and 100%"; return@setOnClickListener
                }
                if (method.text.toString().trim().isEmpty()) {
                    method.error = "Enter how the credit was given"; return@setOnClickListener
                }
                if (terms.text.toString().trim().isEmpty()) {
                    terms.error = "Enter repayment terms"; return@setOnClickListener
                }
                try {
                    val result = ArthSaathiCoreEngine.createCredit(
                        null, partyName, nature.selectedItem.toString(), principal, rate,
                        method.text.toString().trim(), terms.text.toString().trim(),
                        due.text.toString().trim(), null, doc.text.toString().trim(),
                        partyMobile, guarantor.text.toString().trim()
                    )
                    if (!result.ok) {
                        Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_LONG).show()
                        return@setOnClickListener
                    }
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        "Credit registered. Consent is still pending; repayment updates remain locked until verified consent.",
                        Toast.LENGTH_LONG).show()
                    render(ArthSaathiNavigation.LOANS_UDHAAR)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        e.message ?: "Please review the credit details.", Toast.LENGTH_LONG).show()
                } catch (e: IllegalStateException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        e.message ?: "Sign in again before saving.", Toast.LENGTH_LONG).show()
                }
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
            "\nROI: "+o.optDouble("roi")+"%\nMethod: "+o.optString("method")+"\nTerms: "+o.optString("terms", o.optString("repaymentTerms"))+
            "\nGuarantor: "+o.optString("guarantorName", o.optString("guarantor"))+"\nDue: "+o.optString("dueDate")+"\nDocument: "+o.optString("documentId", o.optString("document"))+
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
                    val consent = o.optString("consentStatus")
                    if (consent != "CONSENTED") { Toast.makeText(this@ArthSaathiMasterModuleActivity,"Consent is required before repayment. Use Security & Consent first.",Toast.LENGTH_LONG).show(); return@setPositiveButton }
                    val result = ArthSaathiCoreEngine.repayment(o.optString("id"), e.text.toString().toDoubleOrNull() ?: 0.0, "USER_ENTERED", "user")
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,result.message,Toast.LENGTH_LONG).show(); render(ArthSaathiNavigation.REPAYMENT)
                }.show()
            }})
        }
    }

    /** Consent is a real provider-backed workflow, not a generic editable status field. */
    private fun securityConsent(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "SECURITY & CONSENT\nRequest a trusted-provider OTP for the borrower and verify it here. A typed status alone never grants consent."
            textSize = 16f
        })
        val credits = (0 until records.length()).mapNotNull { records.optJSONObject(it) }
            .filter { it.optString("type") == "CREDIT" }
        if (credits.isEmpty()) {
            body.addView(TextView(this).apply { text = "No credit records are available for consent." })
            return
        }
        credits.forEach { record ->
            val party = record.optString("party", "Borrower")
            val status = record.optString("consentStatus", "PENDING")
            body.addView(TextView(this).apply {
                text = "\n" + party + " • " + record.optString("nature") +
                    "\nMobile: " + record.optString("partyMobile", "Not provided") +
                    "\nConsent status: " + status
                textSize = 14f
            })
            if (status != "CONSENTED") {
                if (status == "REQUESTED") {
                    val code = field(body, "OTP for " + party, false, 8)
                    body.addView(Button(this).apply {
                        text = "VERIFY CONSENT • " + party
                        setOnClickListener {
                            val result = ArthSaathiCoreEngine.confirmConsent(
                                record.optString("id"), code.text.toString().trim(), "borrower"
                            )
                            Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_LONG).show()
                            if (result.ok) render(ArthSaathiNavigation.SECURITY_CONSENT)
                        }
                    })
                }
                body.addView(Button(this).apply {
                    text = if (status == "REQUESTED") "RESEND CONSENT OTP • " + party else "REQUEST CONSENT OTP • " + party
                    setOnClickListener {
                        val result = ArthSaathiCoreEngine.requestConsent(record.optString("id"), "lender")
                        Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_LONG).show()
                        if (result.ok) render(ArthSaathiNavigation.SECURITY_CONSENT)
                    }
                })
            }
        }
    }

    private fun mis(body:LinearLayout){
        // One source of truth: MIS reads the same canonical account-scoped ledger as every module.
        val metrics = ArthSaathiCoreEngine.mis()
        body.addView(TextView(this).apply {
            text = "MIS / MONEY REPORT\n\nACTUAL RECORDED NUMBERS FIRST" +
                "\nCredits registered: ₹" + money(metrics.credits) +
                "\nRepayments recorded: ₹" + money(metrics.repayments) +
                "\nCredit outstanding: ₹" + money(metrics.outstanding) +
                "\nGroup expenses: ₹" + money(metrics.groupExpenses) +
                "\nAssets / portfolio (current value): ₹" + money(metrics.assets) +
                "\nLiabilities recorded: ₹" + money(metrics.liabilities) +
                "\nBenefits / refunds value generated: ₹" + money(metrics.benefits) +
                "\nArthSaathi service revenue: ₹" + money(metrics.revenue) +
                "\n\nCharts are presentation layers over recorded values."
            textSize = 16f
        })
    }

    private fun switchAnalysis(body:LinearLayout){
        body.addView(TextView(this).apply{text="PORTFOLIO SWITCH ANALYSIS\nCompare return, cost, risk and opportunity cost before suggesting a switch. No automatic money movement.";textSize=16f})
        val current=field(body,"Current annual return %",true);val alternative=field(body,"Alternative annual return %",true);val value=field(body,"Amount considered",true)
        body.addView(Button(this).apply{text="CALCULATE OPPORTUNITY COST";setOnClickListener{
            val c=current.text.toString().toDoubleOrNull()?:0.0;val a=alternative.text.toString().toDoubleOrNull()?:0.0;val v=value.text.toString().toDoubleOrNull()?:0.0
            AlertDialog.Builder(this@ArthSaathiMasterModuleActivity).setTitle("Explainable comparison").setMessage("Return difference: "+(a-c)+" percentage points\nIndicative annual opportunity difference: ₹"+money(v*(a-c)/100.0)+"\nRisk, cost and eligibility must also be reviewed.").setPositiveButton("OK",null).show()
        }})
    }

    private fun people(body: LinearLayout) {
        body.addView(TextView(this).apply {
            text = "Create a person record. Mobile numbers are validated before they are stored."
            textSize = 15f
        })
        val name = field(body, "Full name")
        val mobile = field(body, "10-digit mobile", false, 10)
        val role = field(body, "Role / relationship")
        val address = field(body, "Address / PIN")
        body.addView(Button(this).apply {
            text = "SAVE PERSON"
            setOnClickListener {
                if (name.text.toString().trim().isEmpty()) {
                    name.error = "Name is required"; return@setOnClickListener
                }
                val phone = mobile.text.toString().trim()
                if (phone.isNotEmpty() && !ArthSaathiCoreEngine.validateMobile(phone)) {
                    mobile.error = "Enter a valid 10-digit mobile number"; return@setOnClickListener
                }
                try {
                    val result = ArthSaathiCoreEngine.createPerson(
                        name.text.toString().trim(), phone, role.text.toString().trim(),
                        address.text.toString().trim()
                    )
                    Toast.makeText(this@ArthSaathiMasterModuleActivity, result.message, Toast.LENGTH_SHORT).show()
                    render(ArthSaathiNavigation.PEOPLE)
                } catch (e: IllegalArgumentException) {
                    Toast.makeText(this@ArthSaathiMasterModuleActivity,
                        e.message ?: "Review the person details.", Toast.LENGTH_LONG).show()
                }
            }
        })
        body.addView(TextView(this).apply { text = "Saved people"; textSize = 18f; setPadding(0, 20, 0, 6) })
        val saved = records
        var count = 0
        for (i in 0 until saved.length()) {
            val record = saved.optJSONObject(i) ?: continue
            if (record.optString("type") != "PERSON") continue
            count++
            body.addView(TextView(this).apply {
                text = record.optString("name") + " • " + record.optString("mobile") +
                    if (record.optString("role").isNotBlank()) " • " + record.optString("role") else ""
                setPadding(0, 8, 0, 8)
            })
        }
        if (count == 0) body.addView(TextView(this).apply { text = "No people saved yet." })
    }

    private fun simple(body:LinearLayout,type:String,title:String,hints:List<String>){
        body.addView(TextView(this).apply{text=title+"\n";textSize=17f})
        val fields=hints.map { hint ->
            val numeric = isNumericHint(hint)
            val edit = field(body, hint, numeric)
            if (!numeric && (hint.contains("date", true) || hint.contains("expiry", true) ||
                    hint.contains("renewal", true) || hint.contains("due", true))) {
                edit.isFocusable = false
                edit.isClickable = true
                edit.setOnClickListener { pickDate(edit) }
            }
            edit
        }
        body.addView(Button(this).apply{text="SAVE";setOnClickListener{
            if (fields.isNotEmpty() && fields[0].text.toString().trim().isEmpty()) {
                fields[0].error = "Required"; return@setOnClickListener
            }
            val values = linkedMapOf<String, Any?>()
            fields.forEachIndexed { idx, edit ->
                val value = edit.text.toString().trim()
                if (isNumericHint(hints[idx])) {
                    val number = value.toDoubleOrNull()
                    if (number == null || !number.isFinite()) {
                        edit.error = "Enter a valid number"; return@setOnClickListener
                    }
                    if (number < 0.0 && !hints[idx].contains("variance", true)) {
                        edit.error = "Value cannot be negative"; return@setOnClickListener
                    }
                    if ((hints[idx].contains("rate", true) || hints[idx].contains("ROI", true)) &&
                        number !in 0.0..100.0) {
                        edit.error = "Rate must be between 0 and 100%"; return@setOnClickListener
                    }
                    values["f$idx"] = number
                } else {
                    values["f$idx"] = value
                }
            }
            try {
                ArthSaathiCoreEngine.saveModule(type, values)
                Toast.makeText(this@ArthSaathiMasterModuleActivity,title+" record saved",Toast.LENGTH_SHORT).show()
                render(intent.getStringExtra("module") ?: ArthSaathiNavigation.HOME)
            } catch (e: IllegalArgumentException) {
                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                    e.message ?: "Please review the details.", Toast.LENGTH_LONG).show()
            } catch (e: IllegalStateException) {
                Toast.makeText(this@ArthSaathiMasterModuleActivity,
                    e.message ?: "Sign in again before saving.", Toast.LENGTH_LONG).show()
            }
        }})
    }

    private fun isNumericHint(hint: String): Boolean =
        listOf("value", "amount", "principal", "expense", "coverage", "invested",
            "charge", "outstanding", "original", "rate", "roi", "variance", "total")
            .any { hint.contains(it, true) }

    private fun field(parent:LinearLayout,hint:String,number:Boolean=false,max:Int?=null):EditText{
        return EditText(this).apply{
            this.hint=hint
            if(number) inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL or InputType.TYPE_NUMBER_FLAG_SIGNED
            if(max!=null) filters=arrayOf(InputFilter.LengthFilter(max))
            parent.addView(this)
        }
    }
    private fun pickDate(target:EditText){val c=Calendar.getInstance();DatePickerDialog(this,{_,y,m,d->target.setText(dateFormat.format(GregorianCalendar(y,m,d).time))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()}
    private fun money(v:Double)=String.format(Locale.US,"%.2f",v)
}
