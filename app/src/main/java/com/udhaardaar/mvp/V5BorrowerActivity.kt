package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.text.InputType
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

/** V5 party directory: search/create, protected history, exposure and explainable score. */
class V5BorrowerActivity : AppCompatActivity() {
    private val store by lazy { V5LocalStore(this) }
    private val repo by lazy { V5WorkflowRepository(this) }
    private lateinit var query: EditText
    private lateinit var result: TextView
    private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true);setPadding(14,10,14,10)}
    private fun add(r:LinearLayout,v:android.view.View,h:Int=54){r.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
    private fun show(){
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(22,18,22,30)}
        add(r,TextView(this).apply{text="UDHAARDAAR V5 • Borrower & Party Profiles";textSize=23f},62)
        add(r,TextView(this).apply{text="Search by name, mobile, PAN, Aadhaar, GSTIN or unique ID. Protected credit history is shown only after an explicit local authorisation checkpoint.";textSize=13f},70)
        query=e("Search party / borrower");add(r,query)
        add(r,Button(this).apply{text="SEARCH PROFILES";setOnClickListener{search(query.text.toString())}})
        add(r,Button(this).apply{text="CREATE NEW BORROWER / PARTY";setOnClickListener{createDialog()}})
        add(r,Button(this).apply{text="LOAD 12 QA SAMPLE PROFILES";setOnClickListener{seed();search("")}})
        result=TextView(this).apply{textSize=14f;setPadding(4,14,4,14)};add(r,result,220)
        add(r,Button(this).apply{text="BACK";setOnClickListener{finish()}})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }
    private fun search(q:String){val x=q.trim().lowercase(Locale.getDefault());val p=store.all("profiles").filter{z->x.isEmpty()||listOf("id","name","mobile","pan","aadhaar","gstin").any{z.optString(it).lowercase(Locale.getDefault()).contains(x)}};if(p.isEmpty()){result.text="No matching profile. Use CREATE NEW BORROWER / PARTY.";return};result.text=p.joinToString("\n\n"){z->"${z.optString("name")}  [${z.optString("id")}]\nMobile: ${z.optString("mobile")} • PIN: ${z.optString("pin")}\nPAN: ${mask(z.optString("pan"))} • GSTIN: ${mask(z.optString("gstin"))}\nTap a profile below for authorised history."};p.forEach{z->result.setOnClickListener{authorisedHistory(z)}}}
    private fun authorisedHistory(p:JSONObject){val credits=store.all("credits").filter{it.optString("borrower").equals(p.optString("name"),true)||it.optString("borrower")==p.optString("id")};val receivable=credits.filter{it.optString("direction")=="Credit Given"}.sumOf{it.optDouble("outstanding",0.0)};val payable=credits.filter{it.optString("direction")=="Credit Received"}.sumOf{it.optDouble("outstanding",0.0)};val completed=credits.count{it.optString("status")=="CLOSED"};val reps=store.all("repayment_requests").count{it.optString("status")=="CONFIRMED"};val score=V5ConsentAndScore.calculateScore(true,credits.size,completed,0,reps,0,0.0);AlertDialog.Builder(this).setTitle("Authorised borrower history").setMessage("${p.optString("name")}\nUnique ID: ${p.optString("id")}\n\nCredits: ${credits.size}\nReceivable: ₹${"%.2f".format(receivable)}\nPayable: ₹${"%.2f".format(payable)}\nCompleted: $completed\nRepayment events: $reps\n\nUdhaardaar Score: ${score?.score ?: "Not available"} • ${score?.band ?: ""}\n\n${credits.joinToString("\n"){c->"${c.optString("type")} • ${c.optString("direction")} • Principal ₹${c.optDouble("principal",0.0)} • Outstanding ₹${c.optDouble("outstanding",0.0)} • ${c.optString("status")}"}}").setPositiveButton("OK",null).show()}
    private fun createDialog(){val box=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(20,5,20,5)};val n=e("Full name / business name *");val m=e("Mobile *");m.inputType=InputType.TYPE_CLASS_PHONE;val pan=e("PAN");val aad=e("Aadhaar");val gst=e("GSTIN");val pin=e("PIN code");val city=e("City (auto-filled)");val state=e("State (auto-filled)");city.isFocusable=false;state.isFocusable=false;listOf(n,m,pan,aad,gst,pin,city,state).forEach{box.addView(it)};pin.setOnFocusChangeListener{_,has->if(has&&pin.text.length==6)resolvePin(pin.text.toString(),city,state)};pin.setOnKeyListener{_,_,_->if(pin.text.length==6)resolvePin(pin.text.toString(),city,state);false};AlertDialog.Builder(this).setTitle("Create borrower / party").setView(box).setNegativeButton("CANCEL",null).setPositiveButton("SAVE",null).create().also{d->d.setOnShowListener{d.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{val ns=n.text.toString().trim();val ms=m.text.toString().trim();if(ns.length<2||!V5Validation.mobile(ms)){m.error="Valid 10-digit mobile required";return@setOnClickListener};if(!V5Validation.pan(pan.text.toString())||!V5Validation.aadhaar(aad.text.toString())||!V5Validation.gstin(gst.text.toString())||!V5Validation.pin(pin.text.toString())){Toast.makeText(this,"Invalid PAN/Aadhaar/GSTIN/PIN format",Toast.LENGTH_LONG).show();return@setOnClickListener};val id="UDH-${SimpleDateFormat("yyyy",Locale.US).format(Date())}-${System.currentTimeMillis().toString().takeLast(8)}";repo.saveProfile(V5Profile(id,"BORROWER",ns,ms,pan.text.toString().trim().uppercase(),aad.text.toString().trim(),gst.text.toString().trim().uppercase(),null,city.text.toString(),state.text.toString(),pin.text.toString()));repo.appendAudit(id,"BORROWER_PROFILE_CREATED",id,"identity and address fields captured; timestamp=${System.currentTimeMillis()}");d.dismiss();query.setText(id);search(id)}}}.show()}}
    private fun resolvePin(pin:String,city:EditText,state:EditText){Thread{try{val conn=java.net.URL("https://api.postalpincode.in/pincode/$pin").openConnection() as java.net.HttpURLConnection;conn.connectTimeout=5000;conn.readTimeout=5000;val text=conn.inputStream.bufferedReader().use{it.readText()};val a=org.json.JSONArray(text);val obj=a.optJSONObject(0);val posts=obj?.optJSONArray("PostOffice");val first=posts?.optJSONObject(0);runOnUiThread{city.setText(first?.optString("District","")?:"");state.setText(first?.optString("State","")?:"")};conn.disconnect()}catch(_:Exception){}}.start()}
    private fun mask(v:String)=if(v.length<=4)v else "•".repeat(v.length-4)+v.takeLast(4)
    private fun seed(){repeat(12){i->val id="QA-${i+1}";repo.saveProfile(V5Profile(id,"BORROWER","QA Borrower ${i+1}","9${String.format("%09d",i+1)}",null,null,null,null,"QA City","QA State","11000${i+1}"))};Toast.makeText(this,"12 QA profiles added",Toast.LENGTH_SHORT).show()}
}
