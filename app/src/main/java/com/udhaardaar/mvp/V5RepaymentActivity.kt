package com.udhaardaar.mvp

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import org.json.JSONObject
import java.text.SimpleDateFormat
import java.util.*

class V5RepaymentActivity : AppCompatActivity() {
 private val service by lazy { V5RepaymentService(this) }; private val store by lazy { V5LocalStore(this) }
 private fun edit(h:String)=EditText(this).apply{hint=h;textSize=15f;setSingleLine(true);setPadding(14,10,14,10);imeOptions=android.view.inputmethod.EditorInfo.IME_ACTION_NEXT}
 private fun add(root:LinearLayout,v:View,h:Int=54){root.addView(v,LinearLayout.LayoutParams(-1,h).apply{setMargins(0,5,0,5)})}
 override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
 private fun show(){val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(22,18,22,30)};val scroll=ScrollView(this).apply{isFillViewport=true;isSmoothScrollingEnabled=true;addView(root)}
  add(root,TextView(this).apply{text="UDHAARDAAR V5 • Repayment Centre";textSize=23f},60);add(root,TextView(this).apply{text="Receivable / payable • partial repayment • counterparty OTP consent";textSize=13f},48)
  val credits=store.all("credits");val ids=credits.map{it.optString("id")}.filter{it.isNotBlank()};add(root,TextView(this).apply{text="REGISTERED CREDITS (${ids.size})";textSize=13f},38);val credit=edit("Credit ID *");val summary=TextView(this).apply{textSize=14f}
  if(ids.isNotEmpty()){val spinner=Spinner(this);spinner.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,ids);spinner.onItemSelectedListener=object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){summary.text=""};override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){credit.setText(ids[pos]);updateSummary(summary,credits[pos])}};add(root,spinner,54)}else add(root,TextView(this).apply{text="No V5 credits yet. Register a credit first."},55)
  add(root,credit);add(root,summary,80);val initiator=Spinner(this).apply{adapter=ArrayAdapter(this@V5RepaymentActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("LENDER","BORROWER"))};val amount=edit("Repayment amount *").apply{inputType=InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL};val date=edit("Payment date * (tap to select)");val method=edit("Method: UPI / Bank / Cash / Other");val reference=edit("Transaction reference");add(root,initiator);add(root,amount);add(root,date);add(root,method);add(root,reference);date.setText(today());date.setOnClickListener{pickDate(date)}
  add(root,Button(this).apply{text="REQUEST REPAYMENT + OTP";setOnClickListener{val a=amount.text.toString().toDoubleOrNull();val selected=store.find("credits",credit.text.toString());if(selected==null||a==null||a<=0||date.text.isNullOrBlank()){toast("Complete a valid credit, amount and payment date");return@setOnClickListener};val outstanding=selected.optDouble("outstanding",selected.optDouble("principal",0.0));if(a>outstanding){toast("Repayment cannot exceed outstanding ₹$outstanding");return@setOnClickListener};val id=service.request(credit.text.toString(),initiator.selectedItem.toString(),a,date.text.toString(),method.text.toString(),reference.text.toString());val otp=store.find("repayment_requests",id)?.optString("otp","")?:"";val input=edit("Counterparty OTP");val dialog=AlertDialog.Builder(this@V5RepaymentActivity).setTitle("Counterparty consent required").setMessage("Demo OTP: $otp\nProduction SMS gateway is required for live OTP delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create();dialog.setOnShowListener{dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener{if(service.confirm(id,input.text.toString(),otp)){dialog.dismiss();toast("Repayment confirmed; outstanding, status and audit updated");show()}else input.error="Incorrect OTP"}};dialog.show()}},60);add(root,Button(this).apply{text="BACK";setOnClickListener{finish()}},54);listOf<View>(credit,amount,date,method,reference).forEach{v->v.setOnFocusChangeListener{view,has->if(has)view.post{scroll.smoothScrollTo(0,(view.bottom-scroll.height/3).coerceAtLeast(0))}}};setContentView(scroll)}
 private fun today()=SimpleDateFormat("yyyy-MM-dd",Locale.US).format(Date());private fun pickDate(target:EditText){val c=Calendar.getInstance();DatePickerDialog(this,{_,y,m,d->target.setText(String.format(Locale.US,"%04d-%02d-%02d",y,m+1,d))},c.get(Calendar.YEAR),c.get(Calendar.MONTH),c.get(Calendar.DAY_OF_MONTH)).show()};private fun updateSummary(v:TextView,c:JSONObject){val principal=c.optDouble("principal",0.0);val outstanding=c.optDouble("outstanding",principal);v.text="${c.optString("direction")} • ${c.optString("type")}\nPrincipal ₹$principal • Total payable ₹${c.optDouble("totalPayable",principal)}\nRepaid ₹${c.optDouble("repaid",0.0)} • Outstanding ₹$outstanding\nStatus: ${c.optString("status")}"};private fun toast(s:String)=Toast.makeText(this,s,Toast.LENGTH_LONG).show()
}
