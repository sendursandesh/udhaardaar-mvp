package com.udhaardaar.mvp

import android.app.AlertDialog
import android.content.Intent
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.net.Uri
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

/** ArthSaathi V6.2: compact, card-based, rural-friendly command centre. */
class V62HomeActivity : AppCompatActivity() {
    private val navy=Color.rgb(18,48,76); private val green=Color.rgb(16,111,72); private val gold=Color.rgb(205,157,28)
    private val teal=Color.rgb(0,145,135); private val blue=Color.rgb(38,99,235); private val red=Color.rgb(190,65,65)
    private val bg=Color.rgb(246,249,252); private val muted=Color.rgb(92,108,124); private val border=Color.rgb(220,228,236)
    private val prefs by lazy{getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE)}; private val photoRequest=6201
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    private fun text(s:String,size:Float,color:Int=navy,bold:Boolean=false)=TextView(this).apply{text=s;textSize=size;setTextColor(color);typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL)}
    private fun card(fill:Int=Color.WHITE,radius:Int=16)=GradientDrawable().apply{setColor(fill);cornerRadius=dp(radius).toFloat();setStroke(dp(1),border)}
    private fun action(label:String,accent:Int,click:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(8),dp(10),dp(8),dp(10));background=card();setOnClickListener{click()};addView(text(icon(label),24f,accent,true));addView(text(label,13f,navy,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5)})}
    private fun icon(label:String)=when{label.contains("Credit")||label.contains("उधार")->"₹";label.contains("Repay")||label.contains("भुगतान")->"↻";label.contains("Vault")||label.contains("संपत्ति")->"◆";else->"✦"}
    private fun openCore(){startActivity(Intent(this,V61RectifiedActivity::class.java))}
    override fun onCreate(b:Bundle?){super.onCreate(b);window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE);render()}
    override fun onResume(){super.onResume();if(!isFinishing)render()}
    private fun render(){
        if(!prefs.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val root=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(14),dp(10),dp(14),dp(8));setBackgroundColor(bg)}
        val m=prefs.getString("current_mobile","")?:"";val name=prefs.getString("name_$m","User")?:"User";val lang=LanguageManager.get(this)
        val header=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(10),dp(8),dp(8),dp(8));background=card()}
        val logo=ImageView(this).apply{setImageResource(R.drawable.udhaardaar_logo);scaleType=ImageView.ScaleType.CENTER_INSIDE;contentDescription="ArthSaathi logo";setOnClickListener{pickPhoto()}}
        prefs.getString("photo_$m",null)?.let{runCatching{logo.setImageURI(Uri.parse(it))}}
        header.addView(logo,LinearLayout.LayoutParams(dp(54),dp(54)))
        val hb=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};hb.addView(text(LanguageManager.t(this,"app"),22f,navy,true));hb.addView(text(LanguageManager.t(this,"tagline"),10f,teal,true));hb.addView(text(name,11f,muted,true));header.addView(hb,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(10)})
        header.addView(text(if(lang==LanguageManager.HI)"हि" else "EN",12f,blue,true).apply{gravity=Gravity.CENTER;background=card();setOnClickListener{languageDialog()}},LinearLayout.LayoutParams(dp(42),dp(42)))
        root.addView(header)
        val hour=java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY);val greet=when(hour){in 5..11->"good_morning";in 12..16->"good_afternoon";in 17..20->"good_evening";else->"good_night"};root.addView(text("${LanguageManager.t(this,greet)}, $name",18f,navy,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(10)})
        root.addView(text(LanguageManager.t(this,"overview"),11f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)})
        val metrics=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        metrics.addView(metric(LanguageManager.t(this,"to_receive"),"₹0",green),LinearLayout.LayoutParams(0,dp(62),1f).apply{rightMargin=dp(4);topMargin=dp(9)})
        metrics.addView(metric(LanguageManager.t(this,"to_pay"),"₹0",red),LinearLayout.LayoutParams(0,dp(62),1f).apply{leftMargin=dp(4);rightMargin=dp(4);topMargin=dp(9)})
        metrics.addView(metric("ACTIVE","0",blue),LinearLayout.LayoutParams(0,dp(62),1f).apply{leftMargin=dp(4);topMargin=dp(9)})
        root.addView(metrics)
        root.addView(text(LanguageManager.t(this,"quick_actions"),11f,muted,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(12)})
        val grid=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}
        grid.addView(action(LanguageManager.t(this,"register"),blue){openCore()},LinearLayout.LayoutParams(0,dp(88),1f).apply{rightMargin=dp(4);topMargin=dp(6)})
        grid.addView(action(LanguageManager.t(this,"repay_title"),green){openCore()},LinearLayout.LayoutParams(0,dp(88),1f).apply{leftMargin=dp(4);rightMargin=dp(4);topMargin=dp(6)})
        grid.addView(action(LanguageManager.t(this,"vault_title"),teal){openCore()},LinearLayout.LayoutParams(0,dp(88),1f).apply{leftMargin=dp(4);topMargin=dp(6)})
        root.addView(grid)
        val ai=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12),dp(9),dp(12),dp(9));background=card(Color.rgb(239,249,246));setOnClickListener{openCore()}}
        ai.addView(text("✦",22f,gold,true),LinearLayout.LayoutParams(dp(32),-2));val ab=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL};ab.addView(text(LanguageManager.t(this,"ai"),13f,navy,true));ab.addView(text(LanguageManager.t(this,"ai_sub"),10f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)});ai.addView(ab,LinearLayout.LayoutParams(0,-2,1f));ai.addView(text("›",22f,teal,true));root.addView(ai,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(8)})
        val nav=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(dp(4),dp(5),dp(4),dp(4));background=card()}
        nav.addView(bottom("⌂",LanguageManager.t(this,"home"),true){render()},LinearLayout.LayoutParams(0,dp(56),1f))
        nav.addView(bottom("₹",LanguageManager.t(this,"credit"),false){openCore()},LinearLayout.LayoutParams(0,dp(56),1f))
        nav.addView(bottom("↻",LanguageManager.t(this,"repay"),false){openCore()},LinearLayout.LayoutParams(0,dp(56),1f))
        nav.addView(bottom("◆",LanguageManager.t(this,"vault"),false){openCore()},LinearLayout.LayoutParams(0,dp(56),1f))
        nav.addView(bottom("☰",LanguageManager.t(this,"services"),false){servicesDialog()},LinearLayout.LayoutParams(0,dp(56),1f))
        root.addView(nav,LinearLayout.LayoutParams(-1,dp(66)).apply{topMargin=dp(8)})
        setContentView(root)
    }
    private fun metric(label:String,value:String,color:Int)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(5),dp(5),dp(5),dp(5));background=card();addView(text(label,9f,color,true));addView(text(value,17f,navy,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3)})}
    private fun bottom(symbol:String,label:String,selected:Boolean,click:()->Unit)=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setOnClickListener{click()};addView(text(symbol,20f,if(selected)teal else muted,true));addView(text(label,9f,if(selected)teal else muted,selected),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2)})}
    private fun servicesDialog(){val items=arrayOf(LanguageManager.t(this,"insurance"),LanguageManager.t(this,"legacy"),LanguageManager.t(this,"legal"),LanguageManager.t(this,"ttmm"),LanguageManager.t(this,"search"));AlertDialog.Builder(this).setTitle(LanguageManager.t(this,"services")).setItems(items){_,_->openCore()}.setNegativeButton(LanguageManager.t(this,"cancel"),null).show()}
    private fun languageDialog(){val options=arrayOf(LanguageManager.t(this,"english"),LanguageManager.t(this,"hindi"));val current=if(LanguageManager.get(this)==LanguageManager.HI)1 else 0;AlertDialog.Builder(this).setTitle(LanguageManager.t(this,"choose_language")).setSingleChoiceItems(options,current){d,w->LanguageManager.set(this,if(w==1)LanguageManager.HI else LanguageManager.EN);d.dismiss();render()}.setNegativeButton(LanguageManager.t(this,"cancel"),null).show()}
    private fun pickPhoto(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="image/*";addCategory(Intent.CATEGORY_OPENABLE)},photoRequest)}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==photoRequest&&resultCode==RESULT_OK){val uri=data?.data?:return;val m=prefs.getString("current_mobile","")?:"";runCatching{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)};prefs.edit().putString("photo_$m",uri.toString()).apply();render()}}
}
