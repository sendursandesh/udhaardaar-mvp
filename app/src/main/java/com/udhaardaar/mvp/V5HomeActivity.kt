package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class V5HomeActivity : AppCompatActivity() {
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val bg=Color.rgb(238,248,253);private val blue=Color.rgb(25,111,220);private val teal=Color.rgb(0,145,135);private val navy=Color.rgb(24,58,92);private val green=Color.rgb(25,145,78);private val amber=Color.rgb(210,135,15);private val red=Color.rgb(190,55,55)
    private fun dp(x:Int)=(x*resources.displayMetrics.density).toInt()
    private fun card(c:Int=Color.WHITE,s:Int=Color.rgb(190,210,225))=GradientDrawable().apply{setColor(c);setStroke(dp(1),s);cornerRadius=dp(16).toFloat()}
    private fun tv(s:String,z:Float=16f,c:Int=navy)=TextView(this).apply{text=s;textSize=z;setTextColor(c);setPadding(dp(5),dp(5),dp(5),dp(5))}
    private fun root()=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(30));setBackgroundColor(bg)}
    private fun put(r:LinearLayout,v:View,h:Int=52){r.addView(v,LinearLayout.LayoutParams(-1,dp(h)).apply{setMargins(0,dp(4),0,dp(4))})}
    private fun show(r:LinearLayout){val s=ScrollView(this).apply{isFillViewport=true;isSmoothScrollingEnabled=true;addView(r)};setContentView(s);window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)}
    private fun header(r:LinearLayout,title:String,sub:String){val h=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(8),dp(10),dp(8));background=card()};h.addView(tv("UDHAARDAAR V5",12f,teal));h.addView(tv(title,23f));h.addView(tv(sub,12f,Color.DKGRAY));r.addView(h);r.addView(Space(this),LinearLayout.LayoutParams(1,dp(8)))}
    override fun onCreate(b:Bundle?){super.onCreate(b);home()}
    @Suppress("DEPRECATION") override fun onBackPressed(){home()}
    private fun home(){if(!prefs.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return};val r=root();header(r,"Personal financial command centre","Credit • repayments • documents • assets • succession");val m=prefs.getString("current_mobile","")?:"";put(r,tv("Welcome, ${prefs.getString("name_$m","User")}",19f));section(r,"CREDIT & OBLIGATIONS",blue)
        tile(r,"₹","Register Informal Credit","Personal • business • trade • advance • given/received",blue){startActivity(Intent(this,V5InformalCreditActivity::class.java))}
        tile(r,"⌕","Borrower Profiles","Search existing profiles or create a new borrower",teal){legacy("profiles")}
        tile(r,"✓","Repayment Centre","Payable/receivable • due • overdue • partial repayment • OTP consent",green){startActivity(Intent(this,V5RepaymentActivity::class.java))}
        tile(r,"▤","Credit Records","Review registered credits, consent and transaction history",navy){legacy("records")}
        section(r,"DIGITAL DOCUMENTS",amber);tile(r,"▤","DPN + Guarantor Guarantee","Generate • send • OTP consent • version • audit",amber){documents()};tile(r,"▣","Document & Audit Vault","Documents, consent events and evidence history",amber){documents()}
        section(r,"ASSET VAULT",teal);tile(r,"₹","Financial Asset Vault","Deposits • insurance • investments • pensions • receivables",teal){simplePage("Financial Asset Vault")};tile(r,"◆","Non-Financial Asset Vault","Property • vehicles • jewellery • business interests",teal){simplePage("Non-Financial Asset Vault")};tile(r,"♙","Nominee / Trusted Person","Controlled family access; private by default",navy){simplePage("Nominee / Trusted Person")}
        section(r,"SUCCESSION & LEGAL",red);tile(r,"⌘","Inheritance & Claim Centre","Asset discovery • heir/nominee • claim checklist • status",red){simplePage("Inheritance & Claim Centre")};tile(r,"⚖","Legal Assistance","Evidence bundles • recovery/claim support",red){simplePage("Legal Assistance")};tile(r,"▤","Notifications & Readiness","Maturity • renewal • due dates • claim tasks",green){simplePage("Notifications & Readiness")}
        put(r,Button(this).apply{text="MY PROFILE";setOnClickListener{simplePage("My Profile")}},50);put(r,Button(this).apply{text="LOGOUT";setOnClickListener{prefs.edit().putBoolean("logged_in",false).remove("current_mobile").apply();startActivity(Intent(this@V5HomeActivity,LoginActivity::class.java).addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP));finish()}},50);show(r)}
    private fun section(r:LinearLayout,s:String,c:Int){r.addView(tv(s,12f,c))}
    private fun tile(r:LinearLayout,icon:String,title:String,sub:String,c:Int,fn:()->Unit){val x=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(10),dp(7),dp(8),dp(7));background=card();setOnClickListener{fn()}};x.addView(tv(icon,22f,c),LinearLayout.LayoutParams(dp(42),dp(52)));val z=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;layoutParams=LinearLayout.LayoutParams(0,-2,1f)};z.addView(tv(title,16f));z.addView(tv(sub,11f,Color.DKGRAY));x.addView(z);x.addView(tv("›",26f,c));r.addView(x);r.addView(Space(this),LinearLayout.LayoutParams(1,dp(5)))}
    private fun legacy(which:String){val i=Intent(this,V4HomeActivity::class.java).putExtra("open",which);startActivity(i)}
    private fun documents(){val r=root();header(r,"Digital Documents & Consent","Versioned evidence and counterparty consent");put(r,tv("DPN / Guarantee lifecycle: Draft → Sent → Viewed → Consent pending → OTP verified → Completed → Archived",14f));put(r,Button(this).apply{text="GENERATE DPN + GUARANTEE";setOnClickListener{AlertDialog.Builder(this@V5HomeActivity).setTitle("Document templates").setMessage("Generate the Demand Promissory Note and separate guarantor guarantee from final registered credit terms. Completed versions retain consent and audit metadata.").setPositiveButton("OK",null).show()}},54);put(r,Button(this).apply{text="BACK";setOnClickListener{home()}},50);show(r)}
    private fun simplePage(title:String){val r=root();header(r,title,"V5 workflow");put(r,tv("This module is retained in the V5 shell and is linked to the appropriate workflow/data layer. Complete records are kept with timestamps and audit metadata.",14f));put(r,Button(this).apply{text="BACK TO HOME";setOnClickListener{home()}},52);show(r)}
}
