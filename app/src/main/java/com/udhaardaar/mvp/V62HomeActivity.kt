package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*

/** ArthSaathi V6.2 premium command centre. */
class V62HomeActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
    private val store by lazy { V5LocalStore(this) }
    private lateinit var root:LinearLayout
    private fun open(c:Class<*>)=startActivity(Intent(this,c))
    private fun add(v:View,top:Int=7)=ArthSaathiV62Design.add(root,v,top)
    private fun module(n:String,name:String,desc:String,accent:Int,go:()->Unit)=ArthSaathiV62Design.moduleCard(this,n,name,desc,accent,go)
    private fun row(a:View,b:View){val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};r.addView(a,LinearLayout.LayoutParams(0,-2,1f).apply{rightMargin=dp(5)});r.addView(b,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(5)});add(r,5)}
    override fun onCreate(b:Bundle?){super.onCreate(b);render()}
    override fun onResume(){super.onResume();if(!isFinishing)render()}
    private fun render(){
        val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE)
        if(!p.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val owner=V62Integration.currentUserId(this)
        root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(10),dp(16),dp(30));setBackgroundColor(ArthSaathiV62Design.BG)}
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
        add(ArthSaathiV62Design.title(this,ArthSaathiV62Design.BRAND,ArthSaathiV62Design.TAGLINE),2)
        add(ArthSaathiV62Design.text(this,ArthSaathiV62Design.PILLARS,10f,ArthSaathiV62Design.GOLD,true),2)
        val mobile=p.getString("current_mobile","") ?: "";val name=p.getString("name_$mobile","User") ?: "User"
        val hour=java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting=when(hour){in 5..11->"Good Morning";in 12..16->"Good Afternoon";in 17..20->"Good Evening";else->"Good Night"}
        add(ArthSaathiV62Design.text(this,"$greeting, $name",22f,ArthSaathiV62Design.NAVY,true),12)
        add(ArthSaathiV62Design.text(this,"Your financial life, connected in one intelligent view.",12f,ArthSaathiV62Design.MUTED),3)
        val relationships=store.all(V62Store.RELATIONSHIPS).filter{it.optString("ownerUserId")==owner}
        val assets=store.all(V62Store.ASSETS).filter{it.optString("ownerUserId")==owner}
        val policies=store.all(V62Store.INSURANCE).filter{it.optString("ownerUserId")==owner}
        val expenses=store.all(V62Store.TTMM_EXPENSES).filter{it.optString("ownerUserId",owner)==owner}
        val hero=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(18),dp(17),dp(18),dp(17));background=ArthSaathiV62Design.hero(ArthSaathiV62Design.NAVY,22,d)}
        hero.addView(ArthSaathiV62Design.text(this,"FINANCIAL COMMAND CENTRE",10f,ArthSaathiV62Design.GOLD,true))
        hero.addView(ArthSaathiV62Design.text(this,"Plan smarter. Protect what matters.\nGrow with clarity.",18f,Color.WHITE,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        hero.addView(ArthSaathiV62Design.text(this,"One connected view of credit, assets, protection and shared money.",10.5f,0xffd9e5f0.toInt()),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        add(hero,12)
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        stats.addView(ArthSaathiV62Design.statCard(this,"Relationships",relationships.size.toString(),ArthSaathiV62Design.BLUE),LinearLayout.LayoutParams(0,dp(78),1f).apply{rightMargin=dp(4)})
        stats.addView(ArthSaathiV62Design.statCard(this,"Assets",assets.count{it.optBoolean("currentAsset",true)}.toString(),ArthSaathiV62Design.TEAL),LinearLayout.LayoutParams(0,dp(78),1f).apply{leftMargin=dp(4);rightMargin=dp(4)})
        stats.addView(ArthSaathiV62Design.statCard(this,"Policies",policies.size.toString(),ArthSaathiV62Design.GREEN),LinearLayout.LayoutParams(0,dp(78),1f).apply{leftMargin=dp(4)})
        add(stats,9)
        add(ArthSaathiV62Design.section(this,"YOUR FINANCIAL JOURNEY"),10)
        row(module("01","Credit & Udhaar","Lend • borrow • trade",ArthSaathiV62Design.BLUE){open(V62CreditRegistrationActivity::class.java)},module("02","Credit Intelligence","Score • exposure • behaviour",ArthSaathiV62Design.TEAL){open(V62CreditIntelligenceActivity::class.java)})
        row(module("03","Repayment Centre","Schedule • collect • history",ArthSaathiV62Design.GREEN){open(V62RepaymentActivity::class.java)},module("04","Asset Vault","Property • bank • lifecycle",ArthSaathiV62Design.TEAL){open(V62AssetVaultActivity::class.java)})
        row(module("05","Insurance & Protection","Policies • verify • alerts",ArthSaathiV62Design.GREEN){open(V62InsuranceActivity::class.java)},module("06","Rental / Lease","Landlord • tenant • dues",ArthSaathiV62Design.GOLD){open(V62RentalLeaseActivity::class.java)})
        row(module("07","TTMM Shared Money","Group money • settle",ArthSaathiV62Design.BLUE){open(V62TTMMActivity::class.java)},module("08","MIS & Analytics","Charts • tables • risk",ArthSaathiV62Design.TEAL){open(V62MISActivity::class.java)})
        add(ArthSaathiV62Design.section(this,"PROTECT • LEGACY • ASSIST"),10)
        row(module("09","Legacy • Legal • AI","Will • claims • advice",ArthSaathiV62Design.RED){open(V62LegacyLegalAIActivity::class.java)},module("10","Financial Centre","Formal • QR • ChargeCheck • funding",ArthSaathiV62Design.GOLD){open(V62ExtendedModulesActivity::class.java)})
        val advisor=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(15),dp(14),dp(15),dp(14));background=ArthSaathiV62Design.card(ArthSaathiV62Design.PALE_TEAL,18,d)}
        advisor.addView(ArthSaathiV62Design.text(this,"✦",25f,ArthSaathiV62Design.TEAL,true),LinearLayout.LayoutParams(dp(34),dp(48)))
        val copy=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};copy.addView(ArthSaathiV62Design.text(this,"AI FINANCIAL ADVISOR",10f,ArthSaathiV62Design.TEAL,true));copy.addView(ArthSaathiV62Design.text(this,"Connected suggestions from your recorded financial data.",11f,ArthSaathiV62Design.NAVY),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4)});advisor.addView(copy,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(7)});add(advisor,12)
        add(ArthSaathiV62Design.button(this,"OPEN FINANCIAL CENTRE",ArthSaathiV62Design.NAVY){open(V62ExtendedModulesActivity::class.java)},9)
    }
}
