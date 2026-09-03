package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V5HomeActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val bg=Color.rgb(238,248,253); private val navy=Color.rgb(24,58,92); private val blue=Color.rgb(25,111,220); private val teal=Color.rgb(0,145,135); private val green=Color.rgb(25,145,78); private val amber=Color.rgb(210,135,15); private val red=Color.rgb(190,55,55)
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun box(c:Int=Color.WHITE)=GradientDrawable().apply{setColor(c);setStroke(dp(1),Color.rgb(195,215,228));cornerRadius=dp(16).toFloat()}
    private fun tv(s:String,z:Float=14f,c:Int=navy)=TextView(this).apply{text=s;textSize=z;setTextColor(c);setPadding(dp(3),dp(3),dp(3),dp(3));includeFontPadding=true}
    private fun fieldButton(label:String,c:Int,fn:()->Unit)=Button(this).apply{text=label;textSize=14f;isAllCaps=false;setTextColor(Color.WHITE);background=box(c);minimumHeight=dp(52);setOnClickListener{fn()}}
    private fun add(r:LinearLayout,v:View,h:Int=0){r.addView(v,LinearLayout.LayoutParams(-1,if(h>0)dp(h) else ViewGroup.LayoutParams.WRAP_CONTENT).apply{setMargins(0,dp(4),0,dp(4))})}
    private fun shell(title:String,sub:String)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(28));setBackgroundColor(bg);add(this,tv("UDHAARDAAR V5",12f,teal));add(this,tv(title,23f));add(this,tv(sub,12f,Color.DKGRAY))}
    private fun tile(r:LinearLayout,title:String,sub:String,c:Int,fn:()->Unit){val b=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),dp(9),dp(8),dp(9));background=box();setOnClickListener{fn()}};val z=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f)};add(z,tv(title,16f));add(z,tv(sub,11f,Color.DKGRAY));b.addView(z);b.addView(tv("›",28f,c));add(r,b)}
    private fun section(r:LinearLayout,s:String,c:Int){add(r,tv(s,12f,c),30)}
    private fun show(r:LinearLayout){val scroll=ScrollView(this).apply{isFillViewport=true;isSmoothScrollingEnabled=true;addView(r)};setContentView(scroll);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)}
    override fun onCreate(b:Bundle?){super.onCreate(b);home()}
    @Suppress("DEPRECATION") override fun onBackPressed(){home()}
    private fun home(){
        if(!prefs.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val m=prefs.getString("current_mobile","")?:""; val r=shell("Financial Command Centre","Guided access to credit, repayment, documents, assets and claims")
        val welcome=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;padding(dp(12),dp(10),dp(12),dp(10));background=box()}
        add(welcome,tv("Welcome, ${prefs.getString("name_$m","User")}",18f));add(welcome,tv("Mobile: $m",12f,Color.DKGRAY));add(r,welcome)
        section(r,"PARTY & IDENTITY",teal);add(r,fieldButton("Find / Create Borrower • View Credit History",teal){startActivity(Intent(this@V5HomeActivity,V5PartyActivity::class.java))});add(r,fieldButton("Scan QR • Auto-fill Credit / Invoice",blue){startActivity(Intent(this@V5HomeActivity,V5QrCreditActivity::class.java))});add(r,fieldButton("TTMM • Take The Money, Mate",Color.rgb(155,72,180)){startActivity(Intent(this@V5HomeActivity,V5TtmmActivity::class.java))})
        section(r,"CREDIT & OBLIGATIONS",blue);tile(r,"Credit Registration","Borrower → terms → repayment → documents → OTP → registration",blue){startActivity(Intent(this@V5HomeActivity,V5CreditRegistrationActivity::class.java))};tile(r,"Repayment Centre","Payable / receivable • due / overdue • partial repayment • OTP consent",green){startActivity(Intent(this@V5HomeActivity,V5RepaymentActivity::class.java))};tile(r,"Formal Credit Audit","Sanction letter + account statement → reconciliation",navy){simplePage("Formal Credit Audit","Upload and compare sanctioned vs actual terms")};tile(r,"Rental / Lease Engine","Lease dates • rent • due day • arrears • renewal",teal){simplePage("Rental / Lease Engine","Dedicated lease workflow and due-date schedule")}
        section(r,"DOCUMENTS • ASSETS • SUCCESSION",amber);tile(r,"DPN + Guarantor Guarantee","Generate • consent • version • timestamp • audit",amber){documents()};tile(r,"Financial Asset Vault","Deposits • insurance • investments • pensions • receivables",teal){simplePage("Financial Asset Vault","Ownership, proof, value, nominee and maturity")};tile(r,"Non-Financial Asset Vault","Property • vehicles • jewellery • business interests",teal){simplePage("Non-Financial Asset Vault","Ownership, proof, value and trusted access")};tile(r,"Nominee / Trusted Person","Controlled family access and claim preparation",navy){simplePage("Nominee / Trusted Person","Permission-controlled access")};tile(r,"Inheritance & Claim Centre","Evidence → heir/nominee → institution → claim → closure",red){simplePage("Inheritance & Claim Centre","Guided claim checklist and evidence trail")};tile(r,"Legal Assistance","Evidence bundle • recovery/claim support",red){simplePage("Legal Assistance","Evidence preparation and professional referral")};tile(r,"Score & Readiness","Repayment behaviour • overdue • documents • upcoming dues",green){simplePage("Score & Readiness","Explainable readiness factors")}
        add(r,fieldButton("MY PROFILE",teal){simplePage("My Profile","Account owner and privacy controls")});add(r,fieldButton("LOGOUT",Color.rgb(90,110,125)){prefs.edit().putBoolean("logged_in",false).remove("current_mobile").apply();startActivity(Intent(this@V5HomeActivity,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()});show(r)
    }
    private fun documents(){val r=shell("Digital Documents & Consent","Protected document lifecycle and audit metadata");add(r,tv("DPN / Guarantee lifecycle: Draft → Sent → Viewed → Consent pending → OTP verified → Completed → Archived",14f,navy),90);add(r,fieldButton("OPEN DOCUMENT & CONSENT VAULT",amber){startActivity(Intent(this@V5HomeActivity,V5PartyActivity::class.java))});add(r,fieldButton("BACK TO HOME",navy){home()});show(r)}
    private fun simplePage(title:String,sub:String){val r=shell(title,sub);add(r,tv("This V5 module is part of the unified workflow. Records use timestamps and audit metadata; the detailed workflow is opened from its dedicated feature when available.",14f,navy),100);add(r,fieldButton("BACK TO HOME",navy){home()});show(r)}
}
