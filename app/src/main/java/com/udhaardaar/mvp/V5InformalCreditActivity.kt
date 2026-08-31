package com.udhaardaar.mvp

import android.app.AlertDialog
import android.os.Bundle
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import kotlin.random.Random

class V5InformalCreditActivity : AppCompatActivity() {
    private val repo by lazy { V5WorkflowRepository(this) }
    private val consent by lazy { V5OtpConsentService(this) }
    private fun e(h:String)=EditText(this).apply{hint=h;setSingleLine(true)}
    private fun put(r:LinearLayout,v:android.view.View){r.addView(v,LinearLayout.LayoutParams(-1,58).apply{setMargins(0,5,0,5)})}
    override fun onCreate(b:Bundle?){super.onCreate(b);show()}
    private fun show(){val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(24,20,24,24)}
        r.addView(TextView(this).apply{text="Udhaardaar V5 • Informal Credit";textSize=23f})
        r.addView(TextView(this).apply{text="Borrower → terms → consent → registration";textSize=13f})
        val name=e("Borrower name / profile ID *"); val type=Spinner(this).apply{adapter=ArrayAdapter(this@V5InformalCreditActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Personal Credit","Business Credit","Trade Credit","Advance","Other"))}
        val direction=Spinner(this).apply{adapter=ArrayAdapter(this@V5InformalCreditActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("Credit Given","Credit Received"))}
        val principal=e("Principal amount *"); val roi=e("Annual ROI %"); val method=Spinner(this).apply{adapter=ArrayAdapter(this@V5InformalCreditActivity,android.R.layout.simple_spinner_dropdown_item,arrayOf("EMI","Principal + Interest","Bullet / Full payment"))}; val start=e("Start date YYYY-MM-DD *"); val end=e("End date YYYY-MM-DD *")
        put(r,name);put(r,type);put(r,direction);put(r,principal);put(r,roi);put(r,method);put(r,start);put(r,end)
        put(r,Button(this).apply{text="SEND OTP CONSENT + REGISTER";setOnClickListener{
            val p=principal.text.toString().toDoubleOrNull(); if(name.text.trim().length<2||p==null||p<=0||start.text.isBlank()||end.text.isBlank()){Toast.makeText(this@V5InformalCreditActivity,"Complete required fields",Toast.LENGTH_LONG).show();return@setOnClickListener}
            val id="CR-${System.currentTimeMillis()}"; val cid=consent.issue(id,"INFORMAL_CREDIT_BORROWER_CONSENT",name.text.toString()); val otp=readOtp(cid)
            val input=e("Enter 6-digit OTP"); AlertDialog.Builder(this@V5InformalCreditActivity).setTitle("Counterparty consent").setMessage("Demo OTP: $otp\nProduction SMS gateway is required for live OTP delivery.").setView(input).setNegativeButton("CANCEL",null).setPositiveButton("CONFIRM",null).create().also{d->d.setOnShowListener{d.getButton(-1).setOnClickListener{if(consent.verify(cid,input.text.toString())){repo.appendAudit(id,"BORROWER_CONSENT_OTP_VERIFIED",name.text.toString(),"informal credit consented");Toast.makeText(this@V5InformalCreditActivity,"Consent verified; credit registered",Toast.LENGTH_LONG).show();finish()}else input.error="Incorrect OTP"}};d.show()}
        }}); put(r,Button(this).apply{text="BACK";setOnClickListener{finish()}});setContentView(ScrollView(this).apply{addView(r)}) }
    private fun readOtp(id:String):String { val c=V5LocalStore(this).find("consents",id); return c?.optString("otp","") ?: "" }
}
