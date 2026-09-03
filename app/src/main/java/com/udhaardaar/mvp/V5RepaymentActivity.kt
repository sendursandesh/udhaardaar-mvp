package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V5RepaymentActivity:AppCompatActivity(){
 private val service by lazy{V5RepaymentService(this)};private val store by lazy{V5LocalStore(this)}
 private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true);setPadding(16,10,16,10);imeOptions=android.view.inputmethod.EditorInfo.IME_ACTION_NEXT}
 override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);show()}
 private fun show(){
  val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,20,24,28)};val scroll=ScrollView(this).apply{isFillViewport=true;isSmoothScrollingEnabled=true;addView(root)}
  root.addView(TextView(this).apply{text="Udhaardaar V5 • Repayment Centre";textSize=23f});root.addView(TextView(this).apply{text="Receivable / payable • partial repayment • counterparty OTP consent";textSize=13f})
  val credits=store.all("credits");root.addView(TextView(this).apply{text="REGISTERED CREDITS (${credits.size})";textSize=13f})
  val credit=e("Credit ID *");val ids=credits.map{it.optString("id")}.filter{it.isNotBlank()};if(ids.isNotEmpty()){val sp=Spinner(this);sp.adapter=ArrayAdapter(this,android.R.layout.simple_spinner_dropdown_item,ids);sp.setOnItemSelectedListener(object:AdapterView.OnItemSelectedListener{override fun onNothingSelected(p:AdapterView<*>?){};override fun onItemSelected(p:AdapterView<*>?,v:View?,pos:Int,id:Long){credit.setText(ids[pos]);updateSummary(root,credits[pos])}});root.addView(sp,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)})}else root.addView(TextView(this).apply{text="No V5 credit is registered yet. Register a credit first."})
  root.addView(credit,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)});val by=Spinner(this).apply{adapter=ArrayAdapter(this@V5RepaymentActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("LENDER","BORROWER"))};val amount=e("Repayment amount *");val date=e("Payment date YYYY-MM-DD *");val method=e("Method: UPI / Bank / Cash / Other");val ref=e("Transaction reference");listOf<View>(by,amount,date,method,ref).forEach{root.addView(it,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)})}
  root.addView(Button(this).apply{text="REQUEST REPAYMENT + OTP";setOnClickListener{val a=amount.text.toString().toDoubleOrNull();if(credit.text.isBlank()||a==null||a<=0||date.text.isBlank()){Toast.makeText(this@V5RepaymentActivity,"Complete required fields",Toast.LENGTH_LONG).show();return@setOnClickListener};val id=service.request(credit.text.toString(),by.selectedItem.toString(),a,date.text.toString(),method.text.toString(),ref.text.toString());val otp=store.find("repayment_requests",id)?.optString("otp","")?:"";val input=e("Counterparty OTP");val d=AlertDialog.Builder(this@V5RepaymentActivity).setTitle("Counterparty consent").setMessage("Demo OTP: $otp\nProduction SMS gateway is required for live OTP delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create();d.setOnShowListener{d.getButton(-1).setOnClickListener{if(service.confirm(id,input.text.toString(),otp)){Toast.makeText(this@V5RepaymentActivity,"Repayment confirmed and outstanding updated",Toast.LENGTH_LONG).show();show()}else input.error="Incorrect OTP"}};d.show()}}},LinearLayout.LayoutParams(-1,58).apply{setMargins(0,8,0,8)})
  root.addView(Button(this).apply{text="BACK";setOnClickListener{finish()}},LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)})
  fun focusFix(v:View){v.setOnFocusChangeListener{view,has->if(has)view.post{scroll.smoothScrollTo(0,(view.bottom-scroll.height/3).coerceAtLeast(0))}}};listOf<View>(credit,amount,date,method,ref).forEach{focusFix(it)};setContentView(scroll)
 }
 private fun updateSummary(root:LinearLayout,c:org.json.JSONObject){root.findViewsWithText("Outstanding",android.view.View.FIND_VIEWS_WITH_TEXT);val s="${c.optString("direction")} • ${c.optString("type")} • Principal ₹${c.optDouble("principal",0.0)} • Outstanding ₹${c.optDouble("outstanding",c.optDouble("principal",0.0))}";val t=TextView(this).apply{text=s;textSize=14f};root.addView(t,3)}
}
