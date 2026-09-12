package com.udhaardaar.mvp

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

/** ArthSaathi V6.2: polished, simple, multilingual command centre. */
class V62HomeActivity : AppCompatActivity() {
    private val navy = Color.rgb(18,48,76); private val teal = Color.rgb(0,145,135); private val blue = Color.rgb(38,99,235)
    private val green = Color.rgb(22,137,75); private val red = Color.rgb(190,65,65); private val amber = Color.rgb(188,122,18)
    private val bg = Color.rgb(246,249,252); private val muted = Color.rgb(92,108,124); private val border = Color.rgb(220,228,236)
    private val prefs by lazy { getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE) }
    private val store by lazy { V5LocalStore(this) }
    private val photoRequest = 6201
    private fun dp(v:Int) = (v * resources.displayMetrics.density).toInt()
    private fun text(s:String, size:Float, color:Int=navy, bold:Boolean=false) = TextView(this).apply { text=s; textSize=size; setTextColor(color); typeface=Typeface.create("sans-serif", if(bold) Typeface.BOLD else Typeface.NORMAL) }
    private fun bg(fill:Int=Color.WHITE, radius:Int=16, stroke:Boolean=true) = GradientDrawable().apply { setColor(fill); cornerRadius=dp(radius).toFloat(); if(stroke) setStroke(dp(1), border) }
    private fun add(r:LinearLayout, v:View, top:Int=8) { r.addView(v, LinearLayout.LayoutParams(-1,-2).apply { topMargin=dp(top) }) }
    private fun navCard(title:String, sub:String, accent:Int, click:()->Unit) = LinearLayout(this).apply {
        orientation=LinearLayout.HORIZONTAL; gravity=Gravity.CENTER_VERTICAL; setPadding(dp(14),dp(13),dp(12),dp(13)); background=bg(); setOnClickListener{click()}
        addView(TextView(this@V62HomeActivity).apply{text="●";textSize=19f;setTextColor(accent);gravity=Gravity.CENTER}, LinearLayout.LayoutParams(dp(32),dp(45)))
        val b=LinearLayout(this@V62HomeActivity).apply{orientation=LinearLayout.VERTICAL}; b.addView(text(title,15f,navy,true)); b.addView(text(sub,11f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3)}); addView(b,LinearLayout.LayoutParams(0,-2,1f)); addView(text("›",28f,accent))
    }
    private fun action(title:String, accent:Int, click:()->Unit) = TextView(this).apply { text=title; textSize=14f; setTextColor(Color.WHITE); gravity=Gravity.CENTER; typeface=Typeface.DEFAULT_BOLD; minHeight=dp(54); background=bg(accent,14,false); setOnClickListener{click()} }

    override fun onCreate(b:Bundle?) { super.onCreate(b); window.setSoftInputMode(android.view.WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE); render() }
    override fun onResume(){ super.onResume(); if(::dummy.isInitialized) render() }
    private lateinit var dummy:TextView

    private fun render() {
        if(!prefs.getBoolean("logged_in",false)){startActivity(Intent(this,LoginActivity::class.java));finish();return}
        val r=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(16),dp(12),dp(16),dp(30));setBackgroundColor(bg)}
        val header=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(14),dp(12),dp(12),dp(12));background=bg()}
        val photo=ImageView(this).apply{setImageResource(R.drawable.udhaardaar_logo);scaleType=ImageView.ScaleType.CENTER_CROP;setPadding(dp(3),dp(3),dp(3),dp(3));contentDescription="Profile photo";setOnClickListener{pickPhoto()}}
        val m=prefs.getString("current_mobile","")?:""; val name=prefs.getString("name_$m","User")?:"User"; val lang=LanguageManager.get(this)
        prefs.getString("photo_$m",null)?.let{runCatching{photo.setImageURI(Uri.parse(it))}}
        header.addView(photo,LinearLayout.LayoutParams(dp(62),dp(62))); val hb=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL}; hb.addView(text(LanguageManager.t(this,"app"),24f,navy,true)); hb.addView(text(LanguageManager.t(this,"tagline"),11f,teal,true)); hb.addView(text(name,12f,muted),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4)}); header.addView(hb,LinearLayout.LayoutParams(0,-2,1f).apply{leftMargin=dp(12)}); header.addView(TextView(this).apply{text=if(lang==LanguageManager.HI)"हि" else "EN";textSize=13f;setTextColor(blue);gravity=Gravity.CENTER;background=bg();setOnClickListener{languageDialog()}},LinearLayout.LayoutParams(dp(44),dp(44)))
        r.addView(header)
        val hour=java.util.Calendar.getInstance().get(java.util.Calendar.HOUR_OF_DAY); val greet=when(hour){in 5..11->"good_morning";in 12..16->"good_afternoon";in 17..20->"good_evening";else->"good_night"}; add(r,text("${LanguageManager.t(this,greet)}, $name",20f,navy,true),14); add(r,text(LanguageManager.t(this,"overview"),12f,muted),3)
        val metrics=LinearLayout(this).apply{orientation=LinearLayout.HORIZONTAL}; metrics.addView(metric(LanguageManager.t(this,"to_receive"),"₹0",green),LinearLayout.LayoutParams(0,dp(84),1f).apply{rightMargin=dp(5)}); metrics.addView(metric(LanguageManager.t(this,"to_pay"),"₹0",red),LinearLayout.LayoutParams(0,dp(84),1f).apply{leftMargin=dp(5)}); add(r,metrics,14)
        add(r,text(LanguageManager.t(this,"quick_actions"),11f,muted,true),18); add(r,action("＋  ${LanguageManager.t(this,"register")}",blue){startActivity(Intent(this,V61RectifiedActivity::class.java))},7)
        add(r,navCard(LanguageManager.t(this,"search"),LanguageManager.t(this,"search_sub"),teal){startActivity(Intent(this,V61RectifiedActivity::class.java))},8)
        add(r,text(LanguageManager.t(this,"financial_tools"),11f,muted,true),18)
        add(r,navCard(LanguageManager.t(this,"repay_title"),LanguageManager.t(this,"repay_sub"),green){startActivity(Intent(this,V61RectifiedActivity::class.java))})
        add(r,navCard(LanguageManager.t(this,"vault_title"),LanguageManager.t(this,"vault_sub"),teal){startActivity(Intent(this,V61RectifiedActivity::class.java))})
        add(r,navCard(LanguageManager.t(this,"insurance"),LanguageManager.t(this,"insurance_sub"),green){startActivity(Intent(this,V61RectifiedActivity::class.java))})
        add(r,text(LanguageManager.t(this,"support"),11f,muted,true),18)
        add(r,navCard(LanguageManager.t(this,"legacy"),LanguageManager.t(this,"legacy_sub"),amber){startActivity(Intent(this,V61RectifiedActivity::class.java))})
        add(r,navCard(LanguageManager.t(this,"legal"),LanguageManager.t(this,"legal_sub"),red){startActivity(Intent(this,V61RectifiedActivity::class.java))})
        add(r,navCard(LanguageManager.t(this,"ttmm"),LanguageManager.t(this,"ttmm_sub"),blue){startActivity(Intent(this,V61RectifiedActivity::class.java))})
        add(r,navCard(LanguageManager.t(this,"ai"),LanguageManager.t(this,"ai_sub"),blue){startActivity(Intent(this,V61RectifiedActivity::class.java))})
        add(r,text(LanguageManager.t(this,"rural"),11f,muted),18)
        dummy=text("V6.2",1f); r.addView(dummy,LinearLayout.LayoutParams(1,1)); add(r,action("⚙  ${LanguageManager.t(this,"language")} / ${LanguageManager.t(this,"settings")}",navy){languageDialog()},14); add(r,action(LanguageManager.t(this,"logout"),Color.rgb(90,105,120)){prefs.edit().putBoolean("logged_in",false).apply();startActivity(Intent(this,LoginActivity::class.java));finish()},8)
        setContentView(ScrollView(this).apply{isFillViewport=true;addView(r)})
    }
    private fun metric(label:String,value:String,color:Int)=TextView(this).apply{text="$label\n$value";textSize=12f;setTextColor(color);typeface=Typeface.DEFAULT_BOLD;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(14),dp(8),dp(14),dp(8));background=bg()}
    private fun languageDialog(){
        val options=arrayOf(LanguageManager.t(this,"english"),LanguageManager.t(this,"hindi")); val current=if(LanguageManager.get(this)==LanguageManager.HI)1 else 0
        AlertDialog.Builder(this).setTitle(LanguageManager.t(this,"choose_language")).setSingleChoiceItems(options,current){d,w->LanguageManager.set(this,if(w==1)LanguageManager.HI else LanguageManager.EN);d.dismiss();Toast.makeText(this,LanguageManager.t(this,"language_saved"),Toast.LENGTH_SHORT).show();render()}.setNegativeButton(LanguageManager.t(this,"cancel"),null).show()
    }
    private fun pickPhoto(){startActivityForResult(Intent(Intent.ACTION_OPEN_DOCUMENT).apply{type="image/*";addCategory(Intent.CATEGORY_OPENABLE)},photoRequest)}
    override fun onActivityResult(requestCode:Int,resultCode:Int,data:Intent?){super.onActivityResult(requestCode,resultCode,data);if(requestCode==photoRequest&&resultCode==RESULT_OK){val uri=data?.data?:return;val m=prefs.getString("current_mobile","")?:"";runCatching{contentResolver.takePersistableUriPermission(uri,Intent.FLAG_GRANT_READ_URI_PERMISSION)};prefs.edit().putString("photo_$m",uri.toString()).apply();render()}}
}
