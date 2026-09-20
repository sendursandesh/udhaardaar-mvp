package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*

/** User-first V6.2 dashboard. Each capability has one primary entry point; MIS is first-class. */
class V62HomeActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get()=resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
    private val store by lazy{V5LocalStore(this)}
    private lateinit var root:LinearLayout
    private fun add(v:android.view.View,top:Int=7)=ArthSaathiV62Design.add(root,v,top)
    private fun open(c:Class<*>)=startActivity(Intent(this,c))
    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    override fun onResume(){super.onResume();if(!isFinishing)render()}
    private fun render(){
        val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE)
        if(!p.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val mobile=p.getString("current_mobile","").orEmpty()
        val name=p.getString("name_$mobile","User").orEmpty().ifBlank{"User"}
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(8),dp(16),dp(26));background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xfffff1c8.toInt(),ArthSaathiV62Design.BG,0xfffffdf7.toInt()))}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(3),dp(3),dp(3),dp(3))}
        top.addView(ImageView(this).apply{setImageResource(R.drawable.arthsaathi_logo);scaleType=ImageView.ScaleType.CENTER_INSIDE},LinearLayout.LayoutParams(dp(58),dp(58)))
        val id=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),0,0,0)}
        id.addView(ArthSaathiV62Design.brandWordmark(this,23f))
        id.addView(ArthSaathiV62Design.text(this,ArthSaathiV62Design.TAGLINE,9.5f,ArthSaathiV62Design.NAVY),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)})
        top.addView(id,LinearLayout.LayoutParams(0,-2,1f))
        top.addView(ArthSaathiV62Design.text(this,ArthSaathiV62Design.PILLARS,9f,ArthSaathiV62Design.GOLD_DARK,true).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(125),-2))
        add(top,0)
        add(ArthSaathiV62Design.text(this,"Good Morning, $name",23f,ArthSaathiV62Design.NAVY,true),12)
        add(ArthSaathiV62Design.text(this,"Your financial journey in one connected place.",12f,ArthSaathiV62Design.MUTED),3)
        val hero=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(17),dp(14),dp(17),dp(14));background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(ArthSaathiV62Design.GOLD_BRIGHT,0xffffcf61.toInt(),ArthSaathiV62Design.GOLD)).apply{cornerRadius=dp(20).toFloat()};elevation=dp(2).toFloat()}
        hero.addView(ArthSaathiV62Design.text(this,"Plan Today",21f,ArthSaathiV62Design.NAVY,true))
        hero.addView(ArthSaathiV62Design.text(this,"Record • Protect • Grow • Claim",12f,ArthSaathiV62Design.NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4)})
        hero.addView(ArthSaathiV62Design.button(this,"OPEN MIS & FINANCIAL INTELLIGENCE",ArthSaathiV62Design.NAVY){open(V62MISActivity::class.java)},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        add(hero,10)
        add(ArthSaathiV62Design.section(this,"CORE WORKFLOWS"),10)
        val row1=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        row1.addView(ArthSaathiV62Design.featureCard(this,"CR","Register Credit"){open(V62CreditRegistrationActivity::class.java)},LinearLayout.LayoutParams(0,dp(94),1f).apply{rightMargin=dp(4)})
        row1.addView(ArthSaathiV62Design.featureCard(this,"RP","Repayment"){open(V62RepaymentActivity::class.java)},LinearLayout.LayoutParams(0,dp(94),1f).apply{leftMargin=dp(4);rightMargin=dp(4)})
        row1.addView(ArthSaathiV62Design.featureCard(this,"AV","Asset Vault"){open(V62AssetVaultActivity::class.java)},LinearLayout.LayoutParams(0,dp(94),1f).apply{leftMargin=dp(4)})
        add(row1,5)
        add(ArthSaathiV62Design.section(this,"CONNECTED MODULES"),10)
        val row2=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        row2.addView(ArthSaathiV62Design.featureCard(this,"PR","Protection"){open(V62InsuranceActivity::class.java)},LinearLayout.LayoutParams(0,dp(94),1f).apply{rightMargin=dp(4)})
        row2.addView(ArthSaathiV62Design.featureCard(this,"TT","Shared Money"){open(V62TTMMActivity::class.java)},LinearLayout.LayoutParams(0,dp(94),1f).apply{leftMargin=dp(4);rightMargin=dp(4)})
        row2.addView(ArthSaathiV62Design.featureCard(this,"QR","QR Khata"){open(V62QRKhataActivity::class.java)},LinearLayout.LayoutParams(0,dp(94),1f).apply{leftMargin=dp(4)})
        add(row2,5)
        add(ArthSaathiV62Design.button(this,"FINANCIAL CENTRE — ALL OTHER SERVICES",ArthSaathiV62Design.BLUE){open(V62ExtendedModulesActivity::class.java)},10)
        add(ArthSaathiV62Design.button(this,"LEGACY • LEGAL • NOMINEE • AI",ArthSaathiV62Design.TEAL){open(V62LegacyLegalAIActivity::class.java)},6)
        val m=V62MisEngine.metrics(this)
        add(ArthSaathiV62Design.section(this,"FINANCIAL SNAPSHOT"),12)
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        stats.addView(ArthSaathiV62Design.statCard(this,"Current Assets","₹ ${m.optDouble("assetValue").toLong()}",ArthSaathiV62Design.GOLD_DARK),LinearLayout.LayoutParams(0,dp(82),1f).apply{rightMargin=dp(4)})
        stats.addView(ArthSaathiV62Design.statCard(this,"Liabilities","₹ ${m.optDouble("liabilities").toLong()}",ArthSaathiV62Design.RED),LinearLayout.LayoutParams(0,dp(82),1f).apply{leftMargin=dp(4)})
        add(stats,5)
        add(ArthSaathiV62Design.text(this,"Active credits: ${m.optInt("activeRelationships")}   •   Insurance: ${store.all(V62Store.INSURANCE).count{it.optString("ownerUserId")==V62Integration.currentUserId(this)}}",10.5f,ArthSaathiV62Design.MUTED),5)
        add(ArthSaathiV62Design.bottomNav(this,"Home",mapOf("Home" to {},"Credit" to {open(V62CreditRegistrationActivity::class.java)},"Repay" to {open(V62RepaymentActivity::class.java)},"Vault" to {open(V62AssetVaultActivity::class.java)},"More" to {open(V62ExtendedModulesActivity::class.java)})),14)
    }
}