package com.udhaardaar.mvp

import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.Locale

class V5PartyActivity:AppCompatActivity(){
 private val store by lazy{V5LocalStore(this)}
 private val bg=Color.rgb(238,248,253);private val navy=Color.rgb(24,58,92);private val teal=Color.rgb(0,145,135);private val green=Color.rgb(25,145,78);private val blue=Color.rgb(25,111,220)
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
 private fun box()=android.graphics.drawable.GradientDrawable().apply{setColor(Color.WHITE);setStroke(dp(1),Color.rgb(205,218,228));cornerRadius=dp(14).toFloat()}
 private fun e(h:String)=EditText(this).apply{hint=h;textSize=15f;setSingleLine(true);minHeight=dp(50);setPadding(dp(12),dp(7),dp(12),dp(7));background=box()}
 private fun add(r:LinearLayout,v:View){r.addView(v,LinearLayout.LayoutParams(-1,ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,dp(4),0,dp(4))})}
 private fun b(s:String,c:Int,fn:()->Unit)=Button(this).apply{text=s;isAllCaps=false;textSize=14f;setTextColor(Color.WHITE);background=box().apply{setColor(c)};minHeight=dp(50);setOnClickListener{fn()}}
 override fun onCreate(x:Bundle?){super.onCreate(x);show()}
 private fun show(){
  val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(24));setBackgroundColor(bg)}
  add(r,TextView(this).apply{text="UDHAARDAAR V5";textSize=12f;setTextColor(teal)});add(r,TextView(this).apply{text="Party & Borrower Identity";textSize=22f;setTextColor(navy)});add(r,TextView(this).apply{text="Search an existing profile or create a new one. PIN can automatically fill city and state.";textSize=12f})
  val search=e("Search name / mobile / PAN / Aadhaar / GSTIN / ID");add(r,search);add(r,b("SEARCH PROFILE",teal){val q=search.text.toString().trim();val hits=store.all("profiles").filter{p->q.isNotBlank()&&listOf("id","name","mobile","pan","aadhaar","gstin").any{k->p.optString(k).equals(q,true)}};showHits(hits)})
  add(r,TextView(this).apply{text="CREATE / UPDATE PROFILE";textSize=17f;setTextColor(navy)})
  val type=Spinner(this);type.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,listOf("BORROWER","LENDER","SELLER","BUYER","GUARANTOR","NOMINEE","LEGAL HEIR","TRUSTED PERSON","BUSINESS","BANK/NBFC"));add(r,type)
  val name=e("Full name / legal entity *");val mobile=e("Mobile *");val pan=e("PAN (optional)");val aad=e("Aadhaar (12 digits, optional)");val gst=e("GSTIN (optional)");val pin=e("PIN code (6 digits)");val city=e("City — auto-filled from PIN");val state=e("State — auto-filled from PIN");listOf(name,mobile,pan,aad,gst,pin,city,state).forEach{add(r,it)}
  add(r,b("LOOK UP PIN",blue){lookupPin(pin,city,state)});pin.setOnFocusChangeListener{_,has->if(!has&&pin.text.toString().length==6)lookupPin(pin,city,state)}
  add(r,b("SAVE PROFILE",green){
   val n=name.text.toString().trim();val m=mobile.text.toString().trim();val p=pan.text.toString().trim().uppercase(Locale.US);val a=aad.text.toString().trim();val g=gst.text.toString().trim().uppercase(Locale.US);val pc=pin.text.toString().trim()
   if(n.length<2){name.error="Name required";return@b};if(!V5Validation.mobile(m)){mobile.error="Valid 10-digit mobile required";return@b};if(p.isNotEmpty()&&!V5Validation.pan(p)){pan.error="Invalid PAN";return@b};if(a.isNotEmpty()&&!V5Validation.aadhaar(a)){aad.error="Invalid Aadhaar";return@b};if(g.isNotEmpty()&&!V5Validation.gstin(g)){gst.error="Invalid GSTIN";return@b};if(pc.isNotEmpty()&&!V5Validation.pin(pc)){pin.error="Invalid PIN";return@b}
   val old=store.all("profiles").firstOrNull{it.optString("mobile")==m||(p.isNotEmpty()&&it.optString("pan").equals(p,true))};val id=if(old==null)"P-${System.currentTimeMillis()}" else old.optString("id");val created=if(old==null)System.currentTimeMillis() else old.optLong("createdAt",System.currentTimeMillis());val verified=if(old==null)false else old.optBoolean("verified",false)
   val obj=JSONObject();obj.put("id",id);obj.put("type",type.selectedItem.toString());obj.put("name",n);obj.put("mobile",m);obj.put("pan",p);obj.put("aadhaar",a);obj.put("gstin",g);obj.put("pin",pc);obj.put("city",city.text.toString().trim());obj.put("state",state.text.toString().trim());obj.put("verified",verified);obj.put("createdAt",created);obj.put("updatedAt",System.currentTimeMillis());store.replace("profiles",obj);Toast.makeText(this,if(old==null)"Profile created successfully" else "Profile updated successfully",Toast.LENGTH_LONG).show()
  })
  add(r,b("BACK",navy){finish()});setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
 }
 private fun showHits(hits:List<JSONObject>){if(hits.isEmpty()){Toast.makeText(this,"No profile found — create a new profile below",Toast.LENGTH_LONG).show();return};val msg=hits.joinToString("\n\n"){p->"ID: ${p.optString("id")}\n${p.optString("name")} • ${p.optString("type")}\nMobile: ${p.optString("mobile")}\nPAN: ${p.optString("pan","—")}\nCity/State: ${p.optString("city","—")} / ${p.optString("state","—")}"};AlertDialog.Builder(this).setTitle("Matching profiles").setMessage(msg).setPositiveButton("OK",null).show()}
 private fun lookupPin(pin:EditText,city:EditText,state:EditText){val p=pin.text.toString();if(p.length!=6){pin.error="Enter 6-digit PIN";return};Thread{try{val c=URL("https://api.postalpincode.in/pincode/$p").openConnection() as HttpURLConnection;c.connectTimeout=5000;c.readTimeout=5000;val s=BufferedReader(InputStreamReader(c.inputStream)).use{it.readText()};val m=Regex("\\\"Name\\\"\\s*:\\s*\\\"([^\\\"]+)\\\".*?\\\"State\\\"\\s*:\\s*\\\"([^\\\"]+)\\\"").find(s);runOnUiThread{if(m!=null){city.setText(m.groupValues[1]);state.setText(m.groupValues[2]);Toast.makeText(this,"City and state filled automatically",Toast.LENGTH_SHORT).show()}else Toast.makeText(this,"PIN not found",Toast.LENGTH_SHORT).show()}}catch(_:Exception){runOnUiThread{Toast.makeText(this,"PIN lookup unavailable — enter city/state manually",Toast.LENGTH_LONG).show()}}}.start()}
}
