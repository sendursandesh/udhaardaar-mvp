package com.udhaardaar.mvp

import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/** ArthSaathi V6.2: compact premium financial-life command centre. */
class V62HomeActivity : AppCompatActivity() {
    private val navy=Color.rgb(16,45,82)
    private val blue=Color.rgb(42,103,221)
    private val teal=Color.rgb(12,171,158)
    private val green=Color.rgb(18,137,91)
    private val gold=Color.rgb(211,161,37)
    private val red=Color.rgb(193,67,72)
    private val bg=Color.rgb(246,249,252)
    private val muted=Color.rgb(92,108,124)
    private val border=Color.rgb(220,228,236)
    private val prefs by lazy{getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE)}

    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun rounded(fill:Int,stroke:Int=fill,r:Int=14)=GradientDrawable().apply{setColor(fill);setStroke(dp(1),stroke);cornerRadius=dp(r).toFloat()}
    private fun label(s:String,size:Float,color:Int=navy,bold:Boolean=false)=TextView(this).apply{
        text=s;textSize=size;setTextColor(color);typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL)
        includeFontPadding=false;gravity=Gravity.CENTER_VERTICAL
    }
    private fun margin(top:Int)=LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(top)}

    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);render()}
    override fun onResume(){super.onResume();if(!isFinishing&&prefs.getBoolean("logged_in",false))render()}

    private fun openCredit(){startActivity(Intent(this,V62CreditRegistrationActivity::class.java))}
    private fun openServices(mode:String="MENU"){startActivity(Intent(this,ArthSaathiModulesActivity::class.java).putExtra("mode",mode))}

    private fun render(){
        if(!prefs.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val mobile=prefs.getString("current_mobile","")?:""
        val name=prefs.getString("name_$mobile","User")?:"User"
        val first=name.trim().split(" ").firstOrNull()?.ifBlank{"U"}?:"U"
        val hour=java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY)
        val greeting=when(hour){in 5..11->"Good morning";in 12..16->"Good afternoon";in 17..20->"Good evening";else->"Good night"}

        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(10));setBackgroundColor(bg)}

        // Clean brand header: no oversized banner and no decorative script typography.
        val header=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(2),0,0,0)}
        header.addView(ImageView(this).apply{setImageResource(R.drawable.udhaardaar_logo);scaleType=ImageView.ScaleType.CENTER_INSIDE;contentDescription="ArthSaathi logo"},LinearLayout.LayoutParams(dp(48),dp(48)))
        val brand=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        brand.addView(label("ArthSaathi",22f,navy,true))
        brand.addView(label("Navigate Your Financial Journey",10f,teal,true),margin(2))
        header.addView(brand,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(10)})
        val avatar=TextView(this).apply{text=first.uppercase();textSize=14f;setTextColor(navy);typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER;background=rounded(Color.WHITE,border,30);contentDescription="Profile"}
        header.addView(avatar,LinearLayout.LayoutParams(dp(42),dp(42)))
        root.addView(header)

        // Journey hero: one clear financial snapshot instead of three disconnected boxes.
        val hero=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(14),dp(16),dp(14));background=rounded(navy,navy,20)}
        val heroTop=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        val hb=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        hb.addView(label("$greeting, $name",17f,Color.WHITE,true))
        hb.addView(label("Your financial journey at a glance",10f,Color.rgb(205,218,232),false),margin(3))
        heroTop.addView(hb,LinearLayout.LayoutParams(0,-2,1f))
        heroTop.addView(label("PLAN  •  PROTECT  •  GROW  •  NOMINATE",8f,Color.rgb(238,195,80),true))
        hero.addView(heroTop)
        val stats=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        stats.addView(heroStat("TO RECEIVE","₹0",Color.rgb(111,223,183)),LinearLayout.LayoutParams(0,dp(54),1f).apply{rightMargin=dp(5);topMargin=dp(12)})
        stats.addView(heroStat("TO PAY","₹0",Color.rgb(255,147,151)),LinearLayout.LayoutParams(0,dp(54),1f).apply{leftMargin=dp(5);rightMargin=dp(5);topMargin=dp(12)})
        stats.addView(heroStat("ACTIVE","0",Color.rgb(123,176,255)),LinearLayout.LayoutParams(0,dp(54),1f).apply{leftMargin=dp(5);topMargin=dp(12)})
        hero.addView(stats)
        root.addView(hero,margin(12))

        val section=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL}
        section.addView(label("Quick access",13f,navy,true),LinearLayout.LayoutParams(0,-2,1f))
        section.addView(label("All services  ›",10f,teal,true).apply{setOnClickListener{openServices("MENU")}})
        root.addView(section,margin(12))

        // Six compact actions preserve the agreed modules without making the home page long.
        val row1=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        row1.addView(action("Credit","Register / search",blue,"+"){openCredit()},actionLp(true))
        row1.addView(action("Repay","Dues / consent",green,"↻"){startActivity(Intent(this,V62RepaymentActivity::class.java))},actionLp(true))
        row1.addView(action("Vault","Assets / evidence",teal,"◆"){startActivity(Intent(this,V62AssetVaultActivity::class.java))},actionLp(false))
        root.addView(row1,margin(7))
        val row2=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        row2.addView(action("Protect","Insurance / benefits",green,"◈"){openServices("INSURANCE")},actionLp(true))
        row2.addView(action("Legacy","Will / nomination",gold,"◇"){openServices("WILL")},actionLp(true))
        row2.addView(action("Legal","Claims / lawyers",red,"⚖"){openServices("LEGAL")},actionLp(false))
        root.addView(row2,margin(7))

        val ai=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),dp(10),dp(12),dp(10));background=rounded(Color.rgb(237,248,245),Color.rgb(205,232,224),16);setOnClickListener{openServices("AI")}}
        ai.addView(label("✦",22f,gold,true),LinearLayout.LayoutParams(dp(34),-1))
        val aib=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}
        aib.addView(label("AI Financial Advisor",12f,navy,true))
        aib.addView(label("Explainable alerts  •  opportunities  •  reminders",9f,muted,false),margin(2))
        ai.addView(aib,LinearLayout.LayoutParams(0,-2,1f))
        ai.addView(label("›",22f,teal,true))
        root.addView(ai,margin(10))

        // Push navigation to the bottom of the screen rather than leaving an accidental blank page.
        val spacer=Space(this)
        root.addView(spacer,LinearLayout.LayoutParams(1,0,1f))
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(dp(4),dp(4),dp(4),dp(4));background=rounded(Color.WHITE,border,18)}
        nav.addView(navItem("⌂","Home",true){render()},navLp())
        nav.addView(navItem("₹","Credit",false){openCredit()},navLp())
        nav.addView(navItem("↻","Repay",false){startActivity(Intent(this,V62RepaymentActivity::class.java))},navLp())
        nav.addView(navItem("◆","Vault",false){startActivity(Intent(this,V62AssetVaultActivity::class.java))},navLp())
        nav.addView(navItem("☰","More",false){openServices("MENU")},navLp())
        root.addView(nav,margin(8))
        setContentView(root)
    }

    private fun heroStat(title:String,value:String,color:Int)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(9),dp(5),dp(9),dp(5));background=GradientDrawable().apply{setColor(Color.argb(35,255,255,255));cornerRadius=dp(10).toFloat()};addView(label(title,8f,color,true));addView(label(value,16f,Color.WHITE,true),margin(2))}
    private fun action(title:String,sub:String,accent:Int,symbol:String,click:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(11),dp(8),dp(7),dp(7));background=rounded(Color.WHITE,border,14);setOnClickListener{click()};addView(label(symbol,19f,accent,true));addView(label(title,11f,navy,true),margin(3));addView(label(sub,8f,muted,false),margin(2))}
    private fun actionLp(right:Boolean)=LinearLayout.LayoutParams(0,dp(76),1f).apply{if(right)rightMargin=dp(4)}
    private fun navItem(symbol:String,title:String,selected:Boolean,click:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setOnClickListener{click()};addView(label(symbol,18f,if(selected)teal else muted,true));addView(label(title,8f,if(selected)teal else muted,selected),margin(2))}
    private fun navLp()=LinearLayout.LayoutParams(0,dp(52),1f)
}
