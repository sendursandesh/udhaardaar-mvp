package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.*

/** Canonical ArthSaathi V6.2 command centre. Account-scoped snapshot; no legacy navigation. */
class V62HomeActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
    private val store by lazy { V5LocalStore(this) }
    private lateinit var root:LinearLayout
    private fun open(c:Class<*>)=startActivity(Intent(this,c))
    private fun chip(label:String,value:String,accent:Int):View=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(13),dp(11),dp(13),dp(11));background=ArthSaathiV62Design.card();addView(ArthSaathiV62Design.text(this@V62HomeActivity,value,18f,accent,true));addView(ArthSaathiV62Design.text(this@V62HomeActivity,label,9.5f,ArthSaathiV62Design.MUTED,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3)})}
    private fun tile(name:String,sub:String,symbol:String,accent:Int,go:()->Unit):View=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(13),dp(11),dp(11),dp(10));background=ArthSaathiV62Design.card(ArthSaathiV62Design.WHITE,16,d);elevation=dp(1).toFloat();setOnClickListener{go()};addView(ArthSaathiV62Design.text(this@V62HomeActivity,symbol,20f,accent,true));addView(ArthSaathiV62Design.text(this@V62HomeActivity,name,12f,ArthSaathiV62Design.NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)});addView(ArthSaathiV62Design.text(this@V62HomeActivity,sub,9f,ArthSaathiV62Design.MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)})}
    private fun section(label:String)=ArthSaathiV62Design.text(this,label,10.5f,ArthSaathiV62Design.MUTED,true).apply{setPadding(0,dp(14),0,dp(4));letterSpacing=.08f}
    private fun add(v:View,top:Int=7)=root.addView(v,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(top)})
    private fun row(a:View,b:View){val r=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};r.addView(a,LinearLayout.LayoutParams(0,dp(91),1f).apply{rightMargin=dp(4)});r.addView(b,LinearLayout.LayoutParams(0,dp(91),1f).apply{leftMargin=dp(4)});add(r,5)}
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
        val mobile=p.getString("current_mobile","")?:"";val name=p.getString("name_$mobile","User")?:"User"
        val hour=java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);val greeting=when(hour){in 5..11->"Good Morning";in 12..16->"Good Afternoon";in 17..20->"Good Evening";else->"Good Night"}
        add(ArthSaathiV62Design.text(this,"$greeting, $name",21f,ArthSaathiV62Design.NAVY,true),13);add(ArthSaathiV62Design.text(this,"Your financial life, connected in one intelligent view.",12f,ArthSaathiV62Design.MUTED),3)
        val relationships=store.all(V62Store.RELATIONSHIPS).filter{it.optString("ownerUserId")==owner};val assets=store.all(V62Store.ASSETS).filter{it.optString("ownerUserId")==owner};val policies=store.all(V62Store.INSURANCE).filter{it.optString("ownerUserId")==owner};val expenses=store.all(V62Store.TTMM_EXPENSES).filter{it.optString("ownerUserId",owner)==owner}
        val hero=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(15),dp(16),dp(15));background=ArthSaathiV62Design.card(ArthSaathiV62Design.NAVY,20,d)}
        hero.addView(ArthSaathiV62Design.text(this,"FINANCIAL COMMAND CENTRE",9f,ArthSaathiV62Design.GOLD,true));hero.addView(ArthSaathiV62Design.text(this,"Plan smarter. Protect what matters. Grow with clarity.",16f,Color.WHITE,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(6)});hero.addView(ArthSaathiV62Design.text(this,"${relationships.size} relationships  •  ${assets.count{it.optBoolean("currentAsset",true)}} assets  •  ${policies.size} policies  •  ${expenses.size} shared expenses",10f,0xffd9e5f0.toInt()),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)});add(hero,11)
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL};stats.addView(chip("RELATIONSHIPS",relationships.size.toString(),ArthSaathiV62Design.BLUE),LinearLayout.LayoutParams(0,dp(76),1f).apply{rightMargin=dp(4)});stats.addView(chip("ASSETS",assets.size.toString(),ArthSaathiV62Design.TEAL),LinearLayout.LayoutParams(0,dp(76),1f).apply{leftMargin=dp(4);rightMargin=dp(4)});stats.addView(chip("POLICIES",policies.size.toString(),ArthSaathiV62Design.GREEN),LinearLayout.LayoutParams(0,dp(76),1f).apply{leftMargin=dp(4)});add(stats,8)
        add(section("YOUR FINANCIAL JOURNEY"),9)
        row(tile("Credit","Lend • borrow • trade","₹",ArthSaathiV62Design.BLUE){open(V62CreditRegistrationActivity::class.java)},tile("Credit Intelligence","Score • exposure • behaviour","◉",ArthSaathiV62Design.TEAL){open(V62CreditIntelligenceActivity::class.java)})
        row(tile("Repayment","Schedule • history","↻",ArthSaathiV62Design.GREEN){open(V62RepaymentActivity::class.java)},tile("Asset Vault","Property • bank • lifecycle","◆",ArthSaathiV62Design.TEAL){open(V62AssetVaultActivity::class.java)})
        row(tile("Insurance","Scan • verify • alert","+",ArthSaathiV62Design.GREEN){open(V62InsuranceActivity::class.java)},tile("Rental / Lease","Landlord • tenant","⌂",ArthSaathiV62Design.GOLD){open(V62RentalLeaseActivity::class.java)})
        row(tile("TTMM","Group money • settle","◈",ArthSaathiV62Design.BLUE){open(V62TTMMActivity::class.java)},tile("MIS","Charts • tables • risk","▥",ArthSaathiV62Design.TEAL){open(V62MISActivity::class.java)})
        add(section("PROTECT • LEGACY • ASSIST"),10)
        row(tile("Legacy • Legal • AI","Will • claims • advice","⚖",ArthSaathiV62Design.RED){open(V62LegacyLegalAIActivity::class.java)},tile("Financial Centre","Formal • QR • ChargeCheck • funding","✦",ArthSaathiV62Design.GOLD){open(V62ExtendedModulesActivity::class.java)})
        val advisor=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(15),dp(13),dp(15),dp(13));background=ArthSaathiV62Design.card(0xffeef8f5.toInt(),18,d)};advisor.addView(ArthSaathiV62Design.text(this,"✦  AI FINANCIAL ADVISOR",10f,ArthSaathiV62Design.TEAL,true));advisor.addView(ArthSaathiV62Design.text(this,"Connected suggestions from your recorded obligations, protection, savings, idle funds and legacy data.",11f,ArthSaathiV62Design.NAVY),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)});add(advisor,11);add(ArthSaathiV62Design.button(this,"OPEN ALL ARTHSAATHI V6.2 MODULES",ArthSaathiV62Design.NAVY){open(V62ExtendedModulesActivity::class.java)},10)
    }
}
