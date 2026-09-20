package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*

class V62HomeActivity : androidx.appcompat.app.AppCompatActivity() {
    private val d get()=resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()
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

        root=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(14),dp(8),dp(14),dp(22))
            background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(0xffffe8a6.toInt(),ArthSaathiV62Design.BG,0xfffffdf7.toInt()))
        }
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})

        add(ArthSaathiV62Design.masthead(this,false),0)

        val welcome=LinearLayout(this).apply{
            orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(10),dp(7),dp(10),dp(7))
            background=ArthSaathiV62Design.card(0xfffffbef.toInt(),15,d)
        }
        val avatar=TextView(this).apply{
            text=name.trim().firstOrNull()?.uppercase() ?: "U";textSize=12f;setTextColor(ArthSaathiV62Design.NAVY)
            gravity=Gravity.CENTER;typeface=android.graphics.Typeface.DEFAULT_BOLD
            background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(ArthSaathiV62Design.GOLD_BRIGHT);setStroke(dp(1),ArthSaathiV62Design.GOLD)}
        }
        welcome.addView(avatar,LinearLayout.LayoutParams(dp(34),dp(34)))
        val wt=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(8),0,0,0)}
        wt.addView(ArthSaathiV62Design.text(this,"Hello $name,",15f,ArthSaathiV62Design.NAVY,true))
        wt.addView(ArthSaathiV62Design.text(this,"Good to see you!",9.5f,ArthSaathiV62Design.MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)})
        welcome.addView(wt,LinearLayout.LayoutParams(0,-2,1f))
        welcome.addView(ArthSaathiV62Design.script(this,"Plan • Protect • Grow • Nominate",9.5f,ArthSaathiV62Design.GOLD_DARK),LinearLayout.LayoutParams(dp(126),-2))
        add(welcome,7)

        val hero=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL;setPadding(dp(17),dp(14),dp(17),dp(14))
            background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(ArthSaathiV62Design.NAVY_2,ArthSaathiV62Design.NAVY,0xff0b1d35.toInt())).apply{cornerRadius=dp(20).toFloat()}
            elevation=dp(2).toFloat()
        }
        hero.addView(ArthSaathiV62Design.text(this,"Plan Today",20f,ArthSaathiV62Design.WHITE,true))
        hero.addView(ArthSaathiV62Design.text(this,"For a Brighter Tomorrow",12f,ArthSaathiV62Design.GOLD_BRIGHT,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)})
        hero.addView(ArthSaathiV62Design.script(this,"Secure Wealth • Stronger Generations",11f,ArthSaathiV62Design.GOLD_BRIGHT),LinearLayout.LayoutParams(-1,-2).apply{gravity=Gravity.LEFT;topMargin=dp(5)})
        hero.addView(ArthSaathiV62Design.button(this,"VIEW FINANCIAL INSIGHTS",ArthSaathiV62Design.GOLD_DEEP){open(V62MISActivity::class.java)},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        add(hero,8)

        // Main dashboard grid follows the saved design draft: 3 columns, compact icon tiles,
        // and all nine primary actions visible without duplicating them elsewhere.
        fun tileRow(items:List<Pair<String,()->Unit>>) {
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            items.forEachIndexed { i, item ->
                row.addView(
                    ArthSaathiV62Design.featureCard(this,item.first.split("|")[0],item.first.split("|")[1],item.second),
                    LinearLayout.LayoutParams(0,dp(92),1f).apply {
                        if(i>0) leftMargin=dp(4)
                        if(i<items.lastIndex) rightMargin=dp(4)
                    }
                )
            }
            add(row,6)
        }
        tileRow(listOf(
            "CR|Register\nCredit" to {open(V62CreditRegistrationActivity::class.java)},
            "RP|Repayment" to {open(V62RepaymentActivity::class.java)},
            "AV|Asset Vault" to {open(V62AssetVaultActivity::class.java)}
        ))
        tileRow(listOf(
            "PR|Protect" to {open(V62InsuranceActivity::class.java)},
            "GR|Grow" to {open(V62ExtendedModulesActivity::class.java)},
            "IN|Insurance" to {open(V62InsuranceActivity::class.java)}
        ))
        tileRow(listOf(
            "LA|Legal &\nAssistance" to {open(V62LegacyLegalAIActivity::class.java)},
            "TT|TTMM\nShare & Settle" to {open(V62TTMMActivity::class.java)},
            "MI|MIS &\nInsights" to {open(V62MISActivity::class.java)}
        ))

        val m=V62MisEngine.metrics(this)
        val snap=LinearLayout(this).apply{
            orientation=LinearLayout.VERTICAL;setPadding(dp(12),dp(9),dp(12),dp(9))
            background=ArthSaathiV62Design.card(0xfffffbef.toInt(),15,d)
            setOnClickListener{open(V62MISActivity::class.java)}
        }
        val st=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        st.addView(ArthSaathiV62Design.text(this,"Your Financial Snapshot",13f,ArthSaathiV62Design.NAVY,true),LinearLayout.LayoutParams(0,-2,1f))
        st.addView(ArthSaathiV62Design.text(this,"View MIS ›",10f,ArthSaathiV62Design.GOLD_DARK,true))
        snap.addView(st)
        val nums=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        nums.addView(ArthSaathiV62Design.statCard(this,"Current Assets","₹ "+m.optDouble("assetValue").toLong(),ArthSaathiV62Design.GOLD_DARK),LinearLayout.LayoutParams(0,dp(70),1f).apply{rightMargin=dp(4)})
        nums.addView(ArthSaathiV62Design.statCard(this,"Active Credits",""+m.optInt("activeRelationships"),ArthSaathiV62Design.BLUE),LinearLayout.LayoutParams(0,dp(70),1f).apply{leftMargin=dp(4);rightMargin=dp(4)})
        nums.addView(ArthSaathiV62Design.statCard(this,"Liabilities","₹ "+m.optDouble("liabilities").toLong(),ArthSaathiV62Design.RED),LinearLayout.LayoutParams(0,dp(70),1f).apply{leftMargin=dp(4)})
        snap.addView(nums,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})
        add(snap,8)

        add(ArthSaathiV62Design.bottomNav(this,"Home",mapOf(
            "Home" to {},
            "Credit" to {open(V62CreditRegistrationActivity::class.java)},
            "Repay" to {open(V62RepaymentActivity::class.java)},
            "Vault" to {open(V62AssetVaultActivity::class.java)},
            "More" to {open(V62ExtendedModulesActivity::class.java)}
        )),10)
    }
}
