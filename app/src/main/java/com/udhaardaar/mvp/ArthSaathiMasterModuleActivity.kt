package com.udhaardaar.mvp

import android.app.Activity
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.widget.*
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class ArthSaathiMasterModuleActivity : Activity() {
    private val dateFormat = SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
    private val records get() = ArthSaathiDataStore.records()

    override fun onCreate(savedInstanceState: Bundle?) { super.onCreate(savedInstanceState); render(intent.getStringExtra("module") ?: ArthSaathiNavigation.HOME) }

    private fun render(id:String) {
        val m=ArthSaathiArchitectureRegistry.canonicalModule(id)
        val root=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL; setPadding(28,30,28,28) }
        root.addView(TextView(this).apply { text=m?.title ?: "ArthSaathi"; textSize=25f })
        root.addView(TextView(this).apply { text=m?.area ?: "Command Centre"; textSize=14f; setPadding(0,8,0,16) })
        val body=LinearLayout(this).apply { orientation=LinearLayout.VERTICAL }
        root.addView(ScrollView(this).apply { addView(body) }, LinearLayout.LayoutParams(-1,0,1f))
        root.addView(Button(this).apply { text="← Home"; setOnClickListener { finish() } })
        setContentView(root)
        when(id) {
            ArthSaathiNavigation.REGISTER_CREDIT -> registerCredit(body)
            ArthSaathiNavigation.LOANS_UDHAAR -> loans(body)
            ArthSaathiNavigation.REPAYMENT -> repayment(body)
            ArthSaathiNavigation.GROUP_KHATA -> groupKhata(body)
            ArthSaathiNavigation.MIS -> mis(body)
            ArthSaathiNavigation.HOME -> home(body)
            else -> informative(body,m?.title ?: id)
        }
    }

    private fun home(body:LinearLayout) {
        body.addView(TextView(this).apply { text="Canonical Command Centre\nOne destination per module • no legacy routing"; textSize=17f })
        ArthSaathiArchitectureRegistry.modules.filter { it.id!=ArthSaathiNavigation.HOME }.groupBy { it.area }.forEach { (area,mods) ->
            body.addView(TextView(this).apply { text=area; textSize=18f; setPadding(0,22,0,6) })
            mods.forEach { mod -> body.addView(Button(this).apply { text=mod.title; setOnClickListener { startActivity(android.content.Intent(this@ArthSaathiMasterModuleActivity, ArthSaathiMasterModuleActivity::class.java).putExtra("module",mod.id)) } }) }
        }
    }

    private fun registerCredit(body:LinearLayout) {
        body.addView(TextView(this).apply { text="Register Credit = new credit only"; textSize=17f })
        val party=field(body,"Borrower / counterparty name")
        val nature=Spinner(this).also { it.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,ArthSaathiArchitectureRegistry.creditNatureOptions); body.addView(it) }
        val amount=field(body,"Principal / amount",true); val roi=field(body,"ROI % if applicable",true)
        val method=field(body,"Method: Cash / UPI / NEFT / Other"); val terms=field(body,"Repayment method / terms")
        val guarantor=field(body,"Guarantor (optional)"); val due=field(body,"Due date"); due.setOnClickListener { pickDate(due) }
        body.addView(Button(this).apply { text="REGISTER CREDIT"; setOnClickListener {
            if(party.text.toString().trim().isEmpty()){party.error="Required";return@setOnClickListener}; if(amount.text.toString().trim().isEmpty()){amount.error="Required";return@setOnClickListener}
            val o=JSONObject(); o.put("id","CR-"+System.currentTimeMillis()); o.put("type","CREDIT"); o.put("party",party.text.toString().trim()); o.put("nature",nature.selectedItem.toString()); o.put("amount",amount.text.toString().toDoubleOrNull()?:0.0); o.put("roi",roi.text.toString().toDoubleOrNull()?:0.0); o.put("method",method.text.toString()); o.put("repaymentTerms",terms.text.toString()); o.put("guarantor",guarantor.text.toString()); o.put("dueDate",due.text.toString()); o.put("paid",0.0); o.put("status","ACTIVE"); o.put("consentStatus","PENDING"); o.put("createdAt",System.currentTimeMillis()); ArthSaathiDataStore.append(o)
            Toast.makeText(this,"Credit registered. OTP/consent remains an authorization boundary.",Toast.LENGTH_LONG).show(); party.text.clear(); amount.text.clear(); roi.text.clear(); method.text.clear(); terms.text.clear(); guarantor.text.clear(); due.text.clear()
        } })
    }

    private fun loans(body:LinearLayout) {
        body.addView(TextView(this).apply { text="Previously registered active/closed accounts"; textSize=17f })
        val all=records; var found=false
        for(i in 0 until all.length){ val o=all.getJSONObject(i); if(o.optString("type")!="CREDIT") continue; found=true; val out=(o.optDouble("amount")-o.optDouble("paid")).coerceAtLeast(0.0); body.addView(Button(this).apply { text=o.optString("party")+" • "+o.optString("nature")+" • Outstanding ₹"+String.format(Locale.US,"%.2f",out); setOnClickListener { detail(o) } }) }
        if(!found) body.addView(TextView(this).apply { text="\nNo credit accounts registered yet." })
    }

    private fun detail(o:JSONObject){ val out=(o.optDouble("amount")-o.optDouble("paid")).coerceAtLeast(0.0); android.app.AlertDialog.Builder(this).setTitle("Account "+o.optString("id")).setMessage("Party: "+o.optString("party")+"\nNature: "+o.optString("nature")+"\nAmount: ₹"+o.optDouble("amount")+"\nROI: "+o.optDouble("roi")+"%\nMethod: "+o.optString("method")+"\nTerms: "+o.optString("repaymentTerms")+"\nGuarantor: "+o.optString("guarantor")+"\nDue: "+o.optString("dueDate")+"\nPaid: ₹"+o.optDouble("paid")+"\nOutstanding: ₹"+out+"\nConsent: "+o.optString("consentStatus")+"\nStatus: "+o.optString("status")).setPositiveButton("OK",null).show() }

    private fun repayment(body:LinearLayout){
        body.addView(TextView(this).apply { text="Single repayment engine"; textSize=17f })
        val all=records
        for(i in 0 until all.length){ val o=all.getJSONObject(i); if(o.optString("type")!="CREDIT" || o.optString("status")=="CLOSED") continue; body.addView(Button(this).apply { text="Record repayment • "+o.optString("party"); setOnClickListener {
            val e=EditText(this@ArthSaathiMasterModuleActivity).apply { hint="Repayment amount"; inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL }
            android.app.AlertDialog.Builder(this@ArthSaathiMasterModuleActivity).setTitle("Consent-gated repayment").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM"){_,_-> val paid=(o.optDouble("paid")+(e.text.toString().toDoubleOrNull()?:0.0)).coerceAtMost(o.optDouble("amount")); o.put("paid",paid); if(paid>=o.optDouble("amount"))o.put("status","CLOSED"); ArthSaathiDataStore.replace(all); Toast.makeText(this@ArthSaathiMasterModuleActivity,"Repayment saved; production OTP authorization must be connected.",Toast.LENGTH_LONG).show(); render(ArthSaathiNavigation.REPAYMENT) }.show()
        } }) }
    }

    private fun groupKhata(body:LinearLayout){
        body.addView(TextView(this).apply { text="Group Khata / Group Expenses"; textSize=17f })
        val group=field(body,"Group / activity name"); val total=field(body,"Total expense",true); val members=field(body,"Members / contributors"); val note=field(body,"Purpose / notes")
        body.addView(Button(this).apply { text="SAVE GROUP EXPENSE"; setOnClickListener { val o=JSONObject(); o.put("id","GR-"+System.currentTimeMillis()); o.put("type","GROUP_EXPENSE"); o.put("group",group.text.toString()); o.put("total",total.text.toString().toDoubleOrNull()?:0.0); o.put("members",members.text.toString()); o.put("note",note.text.toString()); ArthSaathiDataStore.append(o); Toast.makeText(this@ArthSaathiMasterModuleActivity,"Group expense saved",Toast.LENGTH_SHORT).show() } })
    }

    private fun mis(body:LinearLayout){
        var credit=0.0; var paid=0.0; var group=0.0; val all=records
        for(i in 0 until all.length){ val o=all.getJSONObject(i); when(o.optString("type")){"CREDIT"->{credit+=o.optDouble("amount");paid+=o.optDouble("paid")},"GROUP_EXPENSE"->{group+=o.optDouble("total")}} }
        val out=(credit-paid).coerceAtLeast(0.0)
        body.addView(TextView(this).apply { text="ACTUAL RECORDED NUMBERS\n\nCredits registered: ₹"+String.format(Locale.US,"%.2f",credit)+"\nRepayments recorded: ₹"+String.format(Locale.US,"%.2f",paid)+"\nCredit outstanding: ₹"+String.format(Locale.US,"%.2f",out)+"\nGroup expenses: ₹"+String.format(Locale.US,"%.2f",group)+"\n\nAsset/portfolio charts and Benefits/Refunds value generated are presentation layers over recorded data."; textSize=17f })
    }

    private fun informative(body:LinearLayout,title:String){ body.addView(TextView(this).apply { text="Canonical module: "+title+"\n\nThis module has one registered destination in the consolidated architecture. No V3/V4/V5/V6.2/V7 navigation contract is used."; textSize=17f }) }
    private fun field(parent:LinearLayout,hint:String,number:Boolean=false):EditText{ val e=EditText(this).apply{this.hint=hint;if(number)inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL};parent.addView(e);return e }
    private fun pickDate(target:EditText){ val c=Calendar.getInstance(); DatePickerDialog(this,{_,y,m,d->target.setText(dateFormat.format(GregorianCalendar(y,m,d).time))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show() }
}