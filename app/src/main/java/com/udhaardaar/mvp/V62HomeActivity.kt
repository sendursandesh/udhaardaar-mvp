package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*

/** ArthSaathi V6.2 home: Plan • Protect • Grow • Nominate, with live vault metrics. */
class V62HomeActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get()=resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
    private val store by lazy{V5LocalStore(this)}
    private lateinit var root:LinearLayout
    private fun open(c:Class<*>)=startActivity(Intent(this,c))
    private fun add(v:View,top:Int=7)=ArthSaathiV62Design.add(root,v,top)

    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    override fun onResume(){super.onResume();if(!isFinishing)render()}

    private fun render(){
        val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE)
        if(!p.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val owner=V62Integration.currentUserId(this);val mobile=p.getString("current_mobile","")? : "";val name=p.getString("name_$mobile","User")? : "User"
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(8),dp(12),dp(18));background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xfffff1c8.toInt(),ArthSaathiV62Design.BG,0xfffffdf7.toInt()))}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})

        val top=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(3),dp(2),dp(3),dp(2))}
        top.addView(ImageView(this).apply{setImageResource(R.drawable.arthsaathi_logo);scaleType=ImageView.ScaleType.CENTER_INSIDE;contentDescription="ArthSaathi logo"},LinearLayout.LayoutParams(dp(52),dp(52)))
        val id=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),0,0,0)}
        id.addView(ArthSaathiV62Design.brandWordmark(this,19f));id.addView(ArthSaathiV62Design.text(this,ArthSaathiV62Design.TAGLINE,8.5f,ArthSaathiV62Design.NAVY),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)})
        top.addView(id,LinearLayout.LayoutParams(0,-2,1f));top.addView(ArthSaathiV62Design.text(this,"Plan • Protect • Grow • Nominate",8f,ArthSaathiV62Design.GOLD_DARK,true).apply{gravity=Gravity.CENTER;setPadding(dp(4),0,dp(2),0)},LinearLayout.LayoutParams(dp(120),-2));add(top,0)

        val hour=java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);val greeting=when(hour){in 5..11->"Good Morning";in 12..16->"Good Afternoon";in 17..20->"Good Evening";else->"Good Night"}
        add(ArthSaathiV62Design.text(this,"$greeting, $name",21f,ArthSaathiV62Design.NAVY,true),9)
        add(ArthSaathiV62Design.text(this,"Good to see you!",10f,ArthSaathiV62Design.MUTED),3)

        val hero=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(17),dp(14),dp(10),dp(14));background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(ArthSaathiV62Design.GOLD_BRIGHT,0xffffcf61.toInt(),ArthSaathiV62Design.GOLD)).apply{cornerRadius=dp(20).toFloat()};elevation=dp(2).toFloat()}
        val hc=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        hc.addView(ArthSaathiV62Design.text(this,"Plan Today",20f,ArthSaathiV62Design.NAVY,true));hc.addView(ArthSaathiV62Design.text(this,"For a Brighter Tomorrow",11.5f,ArthSaathiV62Design.NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3)});hc.addView(ArthSaathiV62Design.text(this,"Secure Wealth • Stronger Generations",9f,ArthSaathiV62Design.GOLD_DARK),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        hero.addView(hc,LinearLayout.LayoutParams(0,-2,1f));hero.addView(ArthSaathiV62Design.text(this,"›",34f,ArthSaathiV62Design.NAVY,true).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(dp(42),dp(56)));add(hero,10)

        fun row(vararg items:Triple<String,String,()->Unit>){val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};items.forEach{it->r.addView(ArthSaathiV62Design.featureCard(this,it.first,it.second,it.third),LinearLayout.LayoutParams(0,dp(90),1f).apply{leftMargin=dp(3);rightMargin=dp(3)})};add(r,6)}
        row(Triple("CR","Register\nCredit",{open(V62CreditRegistrationActivity::class.java)}),Triple("RP","Repayment",{open(V62RepaymentActivity::class.java)}),Triple("AV","Asset Vault",{open(V62AssetVaultActivity::class.java)}))
        row(Triple("PR","Protect",{open(V62InsuranceActivity::class.java)}),Triple("GR","Grow",{open(V62CreditIntelligenceActivity::class.java)}),Triple("LG","Legacy",{open(V62LegacyLegalAIActivity::class.java)}))
        row(Triple("LA","Legal\nAssistance",{open(V62LegacyLegalAIActivity::class.java)}),Triple("NO","Nominee",{open(V62LegacyLegalAIActivity::class.java)}),Triple("AI","Insights",{open(V62MISActivity::class.java)}))

        val m=V62MisEngine.metrics(this);add(ArthSaathiV62Design.section(this,"YOUR FINANCIAL SNAPSHOT"),8)
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        stats.addView(ArthSaathiV62Design.statCard(this,"Total Assets","₹ ${m.optDouble("assetValue").toLong()}",ArthSaathiV62Design.GOLD_DARK),LinearLayout.LayoutParams(0,dp(78),1f).apply{rightMargin=dp(4)})
        stats.addView(ArthSaathiV62Design.statCard(this,"Liabilities","₹ ${m.optDouble("liabilities").toLong()}",ArthSaathiV62Design.RED),LinearLayout.LayoutParams(0,dp(78),1f).apply{leftMargin=dp(4)})
        add(stats,4);add(ArthSaathiV62Design.text(this,"Active credits: ${m.optInt("activeRelationships")}   •   Insurance policies: ${store.all(V62Store.INSURANCE).count{it.optString("ownerUserId")==owner}}",9f,ArthSaathiV62Design.MUTED),5)

        add(ArthSaathiV62Design.section(this,"FINANCIAL CENTRE"),8)
        add(ArthSaathiV62Design.moduleCard(this,"01","Credit Intelligence","Consent-based history • exposure • behaviour",ArthSaathiV62Design.BLUE){open(V62CreditIntelligenceActivity::class.java)},3)
        add(ArthSaathiV62Design.moduleCard(this,"02","ChargeCheck","Compare sanctioned charges with actual debits",ArthSaathiV62Design.TEAL){open(V62ChargeCheckActivity::class.java)},3)
        add(ArthSaathiV62Design.moduleCard(this,"03","QR Khata","Scan • capture • consent • ledger",ArthSaathiV62Design.GOLD_DEEP){open(V62ExtendedModulesActivity::class.java)},3)
        add(ArthSaathiV62Design.moduleCard(this,"04","TTMM Shared Money","Group expenses • shares • settlement",ArthSaathiV62Design.BLUE){open(V62TTMMActivity::class.java)},3)
        add(ArthSaathiV62Design.moduleCard(this,"05","Legal + Legacy + AI","Advocates • claims • will • portfolio review",ArthSaathiV62Design.TEAL){open(V62LegacyLegalAIActivity::class.java)},3)

        add(ArthSaathiV62Design.bottomNav(this,"Home",mapOf("Home" to {},"Credit" to {open(V62CreditRegistrationActivity::class.java)},"Repay" to {open(V62RepaymentActivity::class.java)},"Vault" to {open(V62AssetVaultActivity::class.java)},"More" to {open(V62ExtendedModulesActivity::class.java)})),12)
    }
}
