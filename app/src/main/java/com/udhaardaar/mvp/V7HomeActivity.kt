package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.widget.*
/** ArthSaathi V7 home projected from the consolidated master architecture. */
class V7HomeActivity : V7SessionActivity() {
    private val d get() = resources.displayMetrics.density
    private fun dp(v:Int)=(v*d).toInt()

    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);render()}
    override fun onResume(){super.onResume();if(!isFinishing)render()}

    private fun render(){
        if(!V7AccountStore.isLoggedIn(this)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(10),dp(7),dp(10),dp(10));setBackgroundColor(ArthSaathiV7Design.CREAM)}
        val header=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;background=ArthSaathiV7Design.bg(this@V7HomeActivity);setPadding(dp(8),dp(7),dp(8),dp(9))}
        header.addView(ArthSaathiV7Design.masthead(this));root.addView(header)

        val name=V7AccountStore.currentName(this).ifBlank{"User"}
        val welcome=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(11),dp(8),dp(11),dp(8));background=ArthSaathiV7Design.card(this@V7HomeActivity,Color.WHITE,15)}
        welcome.addView(ArthSaathiV7Design.logo(this,40),LinearLayout.LayoutParams(dp(40),dp(40)))
        val wt=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(9),0,0,0)}
        wt.addView(ArthSaathiV7Design.text(this,"Hello $name",15f,ArthSaathiV7Design.NAVY,true))
        wt.addView(ArthSaathiV7Design.text(this,"Your financial command centre",9.5f,ArthSaathiV7Design.MUTED))
        welcome.addView(wt,LinearLayout.LayoutParams(0,-2,1f))
        welcome.addView(ArthSaathiV7Design.text(this,"Plan • Protect • Grow • Nominate",8.5f,ArthSaathiV7Design.GOLD,true).apply{gravity=Gravity.CENTER})
        root.addView(welcome,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})

        val plan=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(13),dp(10),dp(13),dp(10));background=GradientDrawable(GradientDrawable.Orientation.TL_BR,intArrayOf(ArthSaathiV7Design.GOLD_PALE,Color.WHITE)).apply{cornerRadius=dp(15).toFloat();setStroke(dp(1),ArthSaathiV7Design.GOLD)}}
        val words=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        words.addView(ArthSaathiV7Design.text(this,"Plan Today",15f,ArthSaathiV7Design.NAVY,true))
        words.addView(ArthSaathiV7Design.text(this,"For a Brighter Tomorrow",10f,ArthSaathiV7Design.MUTED))
        plan.addView(words,LinearLayout.LayoutParams(0,-2,1f));plan.addView(ArthSaathiV7Design.text(this,"›",28f,ArthSaathiV7Design.GOLD,true));plan.setOnClickListener{openModule("GROW")}
        root.addView(plan,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(7)})

        val m=V7Core.metrics(this)
        root.addView(ArthSaathiV7Design.section(this,"Your Financial Snapshot"))
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        listOf("₹ %.2f".format(m.optDouble("assets")) to "Total Assets","₹ %d".format(m.optInt("activeCredits")) to "Active Credits").forEachIndexed{i,p->
            stats.addView(ArthSaathiV7Design.stat(this,p.first,p.second,if(i==1)ArthSaathiV7Design.GREEN else ArthSaathiV7Design.GOLD),LinearLayout.LayoutParams(0,dp(70),1f).apply{if(i>0)leftMargin=dp(6)})
        }
        root.addView(stats)
        root.addView(ArthSaathiV7Design.section(this,"What do you want to do?"))
        addGrid(root,ArthSaathiV7MasterVision.dashboard.map{tile->Triple(tile.icon,tile.title,tile.subtitle) to {openRoute(tile.route)}})

        root.addView(ArthSaathiV7Design.bottomNav(this,"Home",mapOf(
            "Home" to {},
            "Credit" to {openRoute("CREDIT")},
            "Repay" to {openRoute("REPAYMENT")},
            "Vault" to {openRoute("ASSETS")},
            "More" to {openRoute("MORE")}
        )),LinearLayout.LayoutParams(-1,dp(62)).apply{topMargin=dp(9)})
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(root)})
    }

    private fun addGrid(root:LinearLayout,items:List<Pair<Triple<String,String,String>,()->Unit>>){
        var i=0
        while(i<items.size){
            val row=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
            for(j in 0..2){val index=i+j;if(index<items.size){val item=items[index];row.addView(ArthSaathiV7Design.tile(this,item.first.first,item.first.second,item.first.third,item.second),LinearLayout.LayoutParams(0,dp(116),1f).apply{if(j>0)leftMargin=dp(5)})}}
            root.addView(row,LinearLayout.LayoutParams(-1,dp(116)).apply{if(i>0)topMargin=dp(5)});i+=3
        }
    }

    private fun openRoute(route:String){
        when(route){
            "REGISTER_CREDIT"->startActivity(Intent(this,RegisterCreditV3Activity::class.java))
            "CREDIT","REPAYMENT","ASSETS","PROTECT"->openModule(route)
            "GROW","LEGAL","GROUP_KHATA"->openModule(route)
            "WILL"->openTool("WILL")
            "MORE"->openModule("MORE")
            else->openModule(route)
        }
    }
    private fun openModule(key:String){startActivity(Intent(this,V7ModuleActivity::class.java).putExtra("module",key))}
    private fun openTool(key:String){startActivity(Intent(this,V7ToolsActivity::class.java).putExtra("tool",key))}
}
