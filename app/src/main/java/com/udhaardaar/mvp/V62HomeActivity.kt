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
    private fun open(key:String)=startActivity(V62ModuleRegistry.intent(this,key))
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
        hero.addView(ArthSaathiV62Design.text(this,"Financial command centre • portfolio • assets • liabilities • value generated",11.5f,ArthSaathiV62Design.GOLD_BRIGHT),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        add(hero,8)

        // Primary home actions match the approved design board: six actions only.
        fun tileRow(items:List<Pair<String,()->Unit>>) {
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            items.forEachIndexed { i,item ->
                row.addView(
                    ArthSaathiV62Design.featureCard(this,item.first.split("|")[0],item.first.split("|")[1],item.second),
                    LinearLayout.LayoutParams(0,dp(98),1f).apply { if(i>0) leftMargin=dp(5); if(i<items.lastIndex) rightMargin=dp(5) }
                )
            }
            add(row,7)
        }
        tileRow(listOf(
            "₹|Register Credit" to {open("CREDIT")},
            "↻|Repayment" to {open("REPAYMENT")},
            "▣|Asset Vault" to {open("ASSET_VAULT")}
        ))
        tileRow(listOf(
            "◆|Protect" to {open("INSURANCE")},
            "◉|MIS & Analytics" to {open("MIS")},
            "♙|Legacy & Claims" to {open("LEGACY")}
        ))
        add(ArthSaathiV62Design.text(this,"All other services are organised inside More so the Home screen stays simple.",11.5f,ArthSaathiV62Design.MUTED),8)

        add(ArthSaathiV62Design.bottomNav(this,"Home",mapOf(
            "Home" to {},
            "Profile" to {showProfile()},
            "Alerts" to {showAlerts()},
            "More" to { startActivity(Intent(this,V62ExtendedModulesActivity::class.java)) }
        )),10)
    }
    private fun showProfile() {
        val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE)
        val mobile=p.getString("current_mobile","").orEmpty()
        val name=p.getString("name_$mobile","User").orEmpty().ifBlank{"User"}
        val msg="Name: "+name+"\nMobile: "+mobile+"\n\nYour identity and account controls are kept separate from financial modules."
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Profile & Identity").setMessage(msg).setPositiveButton("OK",null).show()
    }

    private fun showAlerts() {
        val owner=V62Integration.currentUserId(this)
        val store=V5LocalStore(this)
        val alerts=store.all(V62Store.ALERTS).filter{it.optString("ownerUserId").isBlank() || it.optString("ownerUserId")==owner}.takeLast(20).reversed()
        val msg=if(alerts.isEmpty()) "No active alerts." else alerts.joinToString("\n\n"){a -> a.optString("severity","INFO")+": "+a.optString("message")}
        androidx.appcompat.app.AlertDialog.Builder(this).setTitle("Alerts & Notifications").setMessage(msg).setPositiveButton("OK",null).show()
    }

}
