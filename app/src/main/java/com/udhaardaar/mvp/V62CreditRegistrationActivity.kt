package com.udhaardaar.mvp

import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.os.Bundle
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.util.Calendar
import java.util.Locale
import kotlin.math.pow
import kotlin.random.Random

class V62CreditRegistrationActivity:AppCompatActivity(){
    private val store by lazy{V5LocalStore(this)}
    private val draft by lazy{getSharedPreferences("v62_credit_draft",MODE_PRIVATE)}
    private val d get()=resources.displayMetrics.density
    private lateinit var root:LinearLayout
    private var selected:JSONObject?=null
    private var step=1
    private var relationshipId=""
    private var docUri=""
    private var documentId=""
    private var invoiceText=""
    private fun add(v:android.view.View,g:Int=7)=ArthSaathiV62Design.add(root,v,g)
    private fun input(h:String)=ArthSaathiV62Design.input(this,h)
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);relationshipId=draft.getString("relationshipId","").orEmpty();render()}
    private fun render(){
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding((16*d).toInt(),(8*d).toInt(),(16*d).toInt(),(28*d).toInt());setBackgroundColor(ArthSaathiV62Design.BG)}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
        add(ArthSaathiV62Design.title(this,"Register Credit","Search • Agree • Record"),2)
        add(ArthSaathiV62Design.text(this,"Borrower history is shown only after separate OTP consent. Supporting evidence is optional for hand loans.",11f,ArthSaathiV62Design.MUTED),6)
        add(ArthSaathiV62Design.text(this,"STEP $step OF 4",12f,ArthSaathiV62Design.TEAL,true),8)
        when(step){1->profile();2->terms();3->guarantorAndEvidence();else->promissory()}
    }
    private fun profile(){
        add(ArthSaathiV62Design.section(this,"1 • BORROWER / COUNTERPARTY"),6)
        add(ArthSaathiV62Design.button(this,"SEARCH / CREATE BORROWER",ArthSaathiV62Design.TEAL){chooseCounterparty()},8)
        selected?.let{cp->add(ArthSaathiV62Design.text(this,"Selected: ${cp.optString("name")} • ${cp.optString("mobile")}",15f,ArthSaathiV62Design.NAVY,true),6);if(draft.getBoolean("historyConsentVerified",false))add(ArthSaathiV62Design.text(this,historySummary(cp.optString("id")),12f,ArthSaathiV62Design.NAVY),6)else add(ArthSaathiV62Design.button(this,"REQUEST HISTORY CONSENT OTP",ArthSaathiV62Design.TEAL){historyConsent()},6)}
        add(ArthSaathiV62Design.button(this,"CONTINUE TO TERMS →",ArthSaathiV62Design.GREEN){if(selected==null)Toast.makeText(this,"Select or create the borrower first.",Toast.LENGTH_SHORT).show()else if(!draft.getBoolean("historyConsentVerified",false))Toast.makeText(this,"Borrower history consent OTP is required.",Toast.LENGTH_LONG).show()else{step=2;render()}},10)
    }
    private fun chooseCounterparty(){
        val q=input("Search by name / mobile / PAN / Aadhaar / GSTIN")
        val list=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        val wrap=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(q);addView(list)}
        val dlg=AlertDialog.Builder(this).setTitle("Borrower search").setView(wrap).setNegativeButton("CANCEL",null).create()
        fun refresh(){list.removeAllViews();val rows=V62Integration.findCounterparties(this,q.text.toString()).take(15);if(rows.isEmpty())list.addView(ArthSaathiV62Design.button(this,"CREATE NEW BORROWER",ArthSaathiV62Design.TEAL){dlg.dismiss();createCounterparty()})else rows.forEach{p->list.addView(ArthSaathiV62Design.button(this,"${p.optString("name")} • ${p.optString("mobile")}",ArthSaathiV62Design.NAVY){dlg.dismiss();selectCounterparty(p)})}}
        q.addTextChangedListener(object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){};override fun onTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){refresh()};override fun afterTextChanged(e:android.text.Editable?) {}});dlg.show();refresh()
    }
    private fun selectCounterparty(cp:JSONObject){selected=cp;relationshipId=V62Store.id("REL");draft.edit().clear().putString("relationshipId",relationshipId).putBoolean("historyConsentVerified",false).apply();store.add(V62Store.RELATIONSHIPS,JSONObject().apply{put("id",relationshipId);put("ownerUserId",V62Integration.currentUserId(this@V62CreditRegistrationActivity));put("counterpartyId",cp.optString("id"));put("status","DRAFT");put("type","PERSONAL_CREDIT")});render()}
    private fun createCounterparty(){
        val n=input("Name / business name *");val m=input("Mobile *");val pan=input("PAN (ABCDE1234F)");val aad=input("Aadhaar (12 digits)");val gst=input("GSTIN (15 characters)")
        val w=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;addView(n);addView(m);addView(pan);addView(aad);addView(gst)}
        AlertDialog.Builder(this).setTitle("Create borrower").setView(w).setNegativeButton("CANCEL",null).setPositiveButton("CREATE"){_,_->
            val pv=pan.text.toString().trim().uppercase(Locale.US);val av=aad.text.toString().trim();val gv=gst.text.toString().trim().uppercase(Locale.US)
            if(n.text.trim().length<2||!V62UserFlow.validMobile(m.text.toString())|| (pv.isNotBlank()&&!V62UserFlow.validPan(pv)) || (av.isNotBlank()&&!V62UserFlow.validAadhaar(av)) || (gv.isNotBlank()&&!V62UserFlow.validGstin(gv)))Toast.makeText(this,"Validate mobile, PAN, Aadhaar and GSTIN before creating the borrower.",Toast.LENGTH_LONG).show()
            else{val cp=V62Integration.createCounterparty(this,n.text.toString(),m.text.toString(),pv,av,gv,relationshipId);if(cp==null)Toast.makeText(this,"Borrower could not be created.",Toast.LENGTH_LONG).show()else selectCounterparty(cp)}
        }.show()
    }
    private fun historyConsent(){val cp=selected?:return;val otp=(100000..999999).random();val e=input("Enter 6-digit OTP");val dlg=AlertDialog.Builder(this).setTitle("Borrower history consent").setMessage("Demo OTP: $otp").setView(e).setNegativeButton("DECLINE",null).setPositiveButton("VERIFY",null).create();dlg.setOnShowListener{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()!=otp.toString()){e.error="Incorrect OTP";return@setOnClickListener};dlg.dismiss();draft.edit().putBoolean("historyConsentVerified",true).apply();V62Integration.recordConsent(this,cp.optString("id"),relationshipId,"HISTORY_SHARING",true);render()}};dlg.show()}
    private fun historySummary(id:String):String{
        val rels=store.all(V62Store.RELATIONSHIPS).filter{it.optString("counterpartyId")==id&&it.optString("id")!=relationshipId}
        val reps=store.all(V62Store.REPAYMENTS).filter{it.optString("counterpartyId")==id||rels.any{r->r.optString("id")==it.optString("relationshipId")}}
        val timely=reps.count{it.optBoolean("paidOnTime",false)};val delayed=reps.count{it.optBoolean("delayed",false)};val defaults=rels.count{it.optBoolean("defaulted",false)};val defaultAmt=rels.filter{it.optBoolean("defaulted",false)}.sumOf{it.optDouble("outstanding",0.0)}
        val totalMarks=rels.size*100;val score=V62Integration.score(this,id);val marks=(score.coerceIn(0,900)/9)
        return "CONSENTED CREDIT HISTORY\nLoans / relationships: ${rels.size}\nPayments made on time: $timely\nDelayed payments: $delayed\nDefaults: $defaults • ₹${String.format(Locale.US,"%.0f",defaultAmt)}\nPerformance marks: $marks / $totalMarks\nArthSaathi score: $score / 900"
    }
    private fun terms(){
        add(ArthSaathiV62Design.section(this,"2 • CREDIT TERMS"),6)
        val type=Spinner(this).apply{adapter=ArrayAdapter(this@V62CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Personal / Hand Loan","Business Trade Credit","Rental / Lease Credit","Other"))}
        val amount=input("Principal / invoice value ₹ *");val roi=input("Interest rate %");val method=Spinner(this).apply{adapter=ArrayAdapter(this@V62CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("EMI","Principal + Interest","Bullet / single payment"))};val period=Spinner(this).apply{adapter=ArrayAdapter(this@V62CreditRegistrationActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Monthly","Quarterly","Half-yearly","Yearly","One-time"))}
        val start=input("Start date — tap to select from calendar").apply{isFocusable=false;setOnClickListener{V62UserFlow.pickDate(this@V62CreditRegistrationActivity,this)}}
        val months=input("Number of EMI / instalments").apply{inputType=2}
        val end=input("End date — automatically calculated").apply{isFocusable=false}
        val emi=input("EMI / instalment amount — automatically calculated").apply{isFocusable=false}
        listOf(type,amount,roi,method,period,start,months,end,emi).forEach{add(it,4)}
        fun calc(){
            val p=amount.text.toString().replace(",","").toDoubleOrNull()?:0.0;val r=roi.text.toString().toDoubleOrNull()?:0.0;val n=months.text.toString().toIntOrNull()?:0;val sd=V62UserFlow.parseDate(start.text.toString())
            if(p<=0||n<=0||sd==null){end.setText("");emi.setText("");return}
            val stepMonths=when(period.selectedItemPosition){1->3;2->6;3->12;else->1};val cal=Calendar.getInstance().apply{time=sd;add(Calendar.MONTH,stepMonths*n)};end.setText(String.format(Locale.US,"%02d/%02d/%04d",cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH)+1,cal.get(Calendar.YEAR)))
            val freq=when(period.selectedItemPosition){0->12;1->4;2->2;3->1;else->12};val rate=r/100.0/freq;val pay=if(method.selectedItemPosition==2)p*(1+r/100.0*n/12.0) else if(rate==0.0)p/n else p*rate*(1+rate).pow(n)/((1+rate).pow(n)-1);emi.setText(String.format(Locale.US,"%.2f",pay))
        }
        val tw=object:android.text.TextWatcher{override fun beforeTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){};override fun onTextChanged(s:CharSequence?,a:Int,b:Int,c:Int){calc()};override fun afterTextChanged(e:android.text.Editable?) {}}
        amount.addTextChangedListener(tw);roi.addTextChangedListener(tw);months.addTextChangedListener(tw);start.addTextChangedListener(tw)
        add(ArthSaathiV62Design.button(this,"CONTINUE TO GUARANTOR / DOCUMENTS →",ArthSaathiV62Design.GREEN){val p=amount.text.toString().replace(",","").toDoubleOrNull();val n=months.text.toString().toIntOrNull();if(p==null||p<=0||n==null||n<=0||V62UserFlow.parseDate(start.text.toString())==null){Toast.makeText(this,"Enter amount, start date and number of instalments.",Toast.LENGTH_LONG).show()}else{draft.edit().putString("amount",amount.text.toString()).putString("roi",roi.text.toString()).putString("method",method.selectedItem.toString()).putString("period",period.selectedItem.toString()).putString("start",start.text.toString()).putString("end",end.text.toString()).putString("emi",emi.text.toString()).putString("months",months.text.toString()).putString("type",type.selectedItem.toString()).apply();step=3;render()}},10)
    }
    private fun guarantorAndEvidence(){
        add(ArthSaathiV62Design.section(this,"3 • GUARANTOR & OPTIONAL EVIDENCE"),6)
        val g=input("Guarantor name (optional)");val gm=input("Guarantor mobile");val gc=CheckBox(this).apply{text="Guarantor consent acknowledged"}
        add(g,4);add(gm,4);add(gc,5)
        add(ArthSaathiV62Design.button(this,"SAVE GUARANTOR",ArthSaathiV62Design.BLUE){if(g.text.isNotBlank()&&!V62UserFlow.validMobile(gm.text.toString()))Toast.makeText(this,"Enter a valid guarantor mobile.",Toast.LENGTH_LONG).show()else{draft.edit().putString("guarantorName",g.text.toString().trim()).putString("guarantorMobile",gm.text.toString().trim()).putBoolean("guarantorConsent",gc.isChecked||g.text.isBlank()).apply();Toast.makeText(this,"Guarantor details saved.",Toast.LENGTH_SHORT).show()}},6)
        add(ArthSaathiV62Design.text(this,"Supporting evidence is optional. A hand loan can be registered without any document.",11f,ArthSaathiV62Design.MUTED),8)
        add(ArthSaathiV62Design.button(this,"OPTIONAL: SCAN / ATTACH EVIDENCE",ArthSaathiV62Design.TEAL){pickDoc()},7)
        if(docUri.isNotBlank())add(ArthSaathiV62Design.text(this,"Evidence retained for reference.",11f,ArthSaathiV62Design.GREEN,true),5)
        add(ArthSaathiV62Design.button(this,"REVIEW PROMISSORY NOTE →",ArthSaathiV62Design.GREEN){if(draft.getString("guarantorName","").orEmpty().isNotBlank()&&!draft.getBoolean("guarantorConsent",false))Toast.makeText(this,"Guarantor acknowledgement is required.",Toast.LENGTH_LONG).show()else{step=4;render()}},10)
    }
    private fun promissory(){
        add(ArthSaathiV62Design.section(this,"4 • PROMISSORY NOTE & FINAL CONSENT"),6)
        val cp=selected?:return;val principal=draft.getString("amount","0").orEmpty().toDoubleOrNull()?:0.0;val roi=draft.getString("roi","0").orEmpty().toDoubleOrNull()?:0.0;val months=draft.getString("months","1").orEmpty().toIntOrNull()?:1;val emi=draft.getString("emi","0").orEmpty().toDoubleOrNull()?:0.0
        val note="BORROWER PROMISE TO PAY LENDER\n\nI, ${cp.optString("name")}, promise to pay to the lender the principal sum of ₹${String.format(Locale.US,"%.2f",principal)}, together with interest at ${String.format(Locale.US,"%.2f",roi)}%, in $months instalments of ₹${String.format(Locale.US,"%.2f",emi)}.\n\nRepayment frequency: ${draft.getString("period","Monthly")}\nStart date: ${draft.getString("start","")}\nFinal payment date: ${draft.getString("end","")}\n\nI acknowledge the debt and promise to pay the lender according to the agreed schedule."
        add(ArthSaathiV62Design.text(this,note,13f,ArthSaathiV62Design.NAVY),8)
        add(ArthSaathiV62Design.text(this,"The executed note will be retained in ArthSaathi Documents as Word-compatible .doc and PDF files.",11f,ArthSaathiV62Design.MUTED),6)
        add(ArthSaathiV62Design.button(this,"FINAL CONSENT + REGISTER CREDIT",ArthSaathiV62Design.TEAL){finalConsent(note,cp,principal,roi,months,emi)},10)
    }
    private fun finalConsent(note:String,cp:JSONObject,principal:Double,roi:Double,months:Int,emi:Double){
        val otp=(100000..999999).random();val e=input("Enter 6-digit OTP");val dlg=AlertDialog.Builder(this).setTitle("Final credit registration consent").setMessage("Review the borrower promise before execution.\n\nDemo OTP: $otp").setView(e).setNegativeButton("CANCEL",null).setPositiveButton("VERIFY",null).create()
        dlg.setOnShowListener{dlg.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(e.text.toString()!=otp.toString()){e.error="Invalid OTP";return@setOnClickListener};dlg.dismiss();register(note,cp,principal,roi,months,emi)}};dlg.show()
    }
    private fun register(note:String,cp:JSONObject,principal:Double,roi:Double,months:Int,emi:Double){
        val owner=V62Integration.currentUserId(this);val type=draft.getString("type","Personal / Hand Loan").orEmpty();val id=relationshipId
        val rel=JSONObject().apply{put("id",id);put("ownerUserId",owner);put("counterpartyId",cp.optString("id"));put("ownerRole","LENDER");put("counterpartyRole","BORROWER");put("type",if(type.contains("Trade"))"TRADE_CREDIT" else if(type.contains("Rental"))"RENTAL" else "PERSONAL_CREDIT");put("principal",principal);put("amount",principal);put("roi",roi);put("roiPercent",roi);put("repaymentMode",draft.getString("method","EMI").orEmpty());put("method",draft.getString("method","EMI").orEmpty());put("periodicity",draft.getString("period","Monthly").orEmpty());put("period",draft.getString("period","Monthly").orEmpty());put("startDate",draft.getString("start","").orEmpty());put("endDate",draft.getString("end","").orEmpty());put("start",draft.getString("start","").orEmpty());put("end",draft.getString("end","").orEmpty());put("emi",emi);put("numberOfInstallments",months);put("guarantorName",draft.getString("guarantorName","").orEmpty());put("guarantorMobile",draft.getString("guarantorMobile","").orEmpty());put("supportingDocumentId",documentId);put("supportingDocumentUri",docUri);put("promissoryNote",note);put("historyConsent","VERIFIED");put("registrationConsent","VERIFIED");put("outstanding",principal);put("status","ACTIVE");put("createdAt",System.currentTimeMillis())}
        val schedule=org.json.JSONArray();val sd=V62UserFlow.parseDate(draft.getString("start",""));val period=draft.getString("period","Monthly").orEmpty();val stepMonths=when(period){"Quarterly"->3;"Half-yearly"->6;"Yearly"->12;else->1};if(sd!=null){for(i in 1..months){val cal=Calendar.getInstance().apply{time=sd;add(Calendar.MONTH,stepMonths*i)};schedule.put(JSONObject().apply{put("installment",i);put("dueDate",String.format(Locale.US,"%02d/%02d/%04d",cal.get(Calendar.DAY_OF_MONTH),cal.get(Calendar.MONTH)+1,cal.get(Calendar.YEAR)));put("dueAmount",emi);put("paidAmount",0.0);put("status","DUE")})}};rel.put("repaymentSchedule",schedule.toString())
        store.replace(V62Store.RELATIONSHIPS,rel);V62EventBus.publish(V62Event(V62Events.RELATIONSHIP_CHANGED,id))
        val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE);val mobile=p.getString("current_mobile","").orEmpty();val lender=p.getString("name_$mobile","Current lender").orEmpty();val files=V62PromissoryNote.create(this,cp.optString("name"),lender,principal,roi,months,emi,draft.getString("start",""),draft.getString("end",""),draft.getString("period","Monthly"));store.add(V62Store.DOCUMENTS,JSONObject().apply{put("id",V62Store.id("DOC"));put("ownerUserId",owner);put("type","PROMISSORY_NOTE_WORD");put("uri",files.word.toURI().toString());put("relationshipId",id);put("createdAt",System.currentTimeMillis())});store.add(V62Store.DOCUMENTS,JSONObject().apply{put("id",V62Store.id("PDF"));put("ownerUserId",owner);put("type","PROMISSORY_NOTE_PDF");put("uri",files.pdf.toURI().toString());put("relationshipId",id);put("createdAt",System.currentTimeMillis())})
        draft.edit().clear().apply();Toast.makeText(this,"Credit registered. Repayment schedule and promissory documents created.",Toast.LENGTH_LONG).show();finish()
    }
    private fun pickDoc(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="*/*";putExtra(Intent.EXTRA_ALLOW_MULTIPLE,false);addCategory(Intent.CATEGORY_OPENABLE)},77)}
    override fun onActivityResult(r:Int,c:Int,data:Intent?){super.onActivityResult(r,c,data);if(r==77&&c==Activity.RESULT_OK&&data?.data!=null){val uri=data.data!!;val retained=runCatching{ArthSaathiV62Core.saveDocument(this,uri,"CREDIT_SUPPORTING")}.getOrNull();docUri=retained?.optString("uri").orEmpty().ifBlank{uri.toString()};documentId=retained?.optString("id").orEmpty();runCatching{V62DocumentScanner.scan(this,uri,{text->invoiceText=text;runOnUiThread{render()}},{runOnUiThread{render()}})} }}
}