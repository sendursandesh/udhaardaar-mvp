package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V5RepaymentActivity:AppCompatActivity(){
 private val service by lazy{V5RepaymentService(this)}
 private val store by lazy{V5LocalStore(this)}
 private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true)}
 private fun put(r:LinearLayout,v:android.view.View){r.addView(v,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)})}
 override fun onCreate(b:Bundle?){super.onCreate(b);val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,20,24,24)};r.addView(TextView(this).apply{text="Udhaardaar V5 • Repayment Centre";textSize=23f});r.addView(TextView(this).apply{text="Informal repayment is confirmed only after counterparty OTP consent.";textSize=13f});val credit=e("Credit ID *");val by=Spinner(this).apply{adapter=ArrayAdapter(this@V5RepaymentActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("LENDER","BORROWER"))};val amount=e("Repayment amount *");val date=e("Payment date YYYY-MM-DD *");val method=e("Method: UPI / Bank / Cash / Other");val ref=e("Transaction reference");put(r,credit);put(r,by);put(r,amount);put(r,date);put(r,method);put(r,ref);put(r,Button(this).apply{text="REQUEST REPAYMENT + OTP";setOnClickListener{val a=amount.text.toString().toDoubleOrNull();if(credit.text.isBlank()||a==null||a<=0||date.text.isBlank()){Toast.makeText(this@V5RepaymentActivity,"Complete required fields",Toast.LENGTH_LONG).show();return@setOnClickListener};val id=service.request(credit.text.toString(),by.selectedItem.toString(),a,date.text.toString(),method.text.toString(),ref.text.toString());val otp=readOtp(id);val input=e("Counterparty OTP");AlertDialog.Builder(this@V5RepaymentActivity).setTitle("Counterparty consent").setMessage("Demo OTP: $otp").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(-1).setOnClickListener{if(service.confirm(id,input.text.toString(),otp)){Toast.makeText(this@V5RepaymentActivity,"Repayment confirmed with counterparty consent",Toast.LENGTH_LONG).show();finish()}else input.error="Incorrect OTP"}};d.show()}}});put(r,Button(this).apply{text="BACK";setOnClickListener{finish()}});setContentView(ScrollView(this).apply{addView(r)})}
 private fun readOtp(id:String):String=store.find("repayment_requests",id)?.optString("otp","")? : ""
}
