package com.udhaardaar.mvp

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.graphics.Rect
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Global V5 visual/accessibility layer. Repairs legacy programmatic layouts that used raw pixels. */
class UdhaardaarApp : Application() {
    private val bg = Color.rgb(246, 248, 251)
    private val navy = Color.rgb(25, 43, 65)
    private val blue = Color.rgb(38, 99, 235)
    private val border = Color.rgb(218, 225, 233)
    private val muted = Color.rgb(96, 112, 128)

    override fun onCreate() {
        super.onCreate(); migrateLegacyOwner()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(a: Activity) {
                a.window.decorView.post {
                    val n=a.javaClass.simpleName
                    if(n=="LoginActivity" || (n.startsWith("V5") && n!="V5HomeActivity")) repairLegacyProgrammaticLayout(a.window.decorView)
                    if(n.startsWith("V5") || n=="LoginActivity") { applyVisualSystem(a); applySystemBarInsets(a); applyMobileLimits(a.window.decorView); installKeyboardAwareScrolling(a.window.decorView) }
                }
            }
            override fun onActivityCreated(a: Activity,b: Bundle?)=Unit
            override fun onActivityStarted(a: Activity)=Unit
            override fun onActivityPaused(a: Activity)=Unit
            override fun onActivityStopped(a: Activity)=Unit
            override fun onActivitySaveInstanceState(a: Activity,b: Bundle)=Unit
            override fun onActivityDestroyed(a: Activity)=Unit
        })
    }
    private fun migrateLegacyOwner(){
        val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE); if(p.getBoolean("legacy_migrated",false))return
        try{val u=V32DatabaseHelper(this).userData();if(u!=null&&u.mobile.filter(Char::isDigit).length==10){val m=u.mobile.filter(Char::isDigit);if(!p.contains("name_$m"))p.edit().putString("name_$m",u.name).putString("address_$m",u.address).putString("email_$m",u.email).putString("photo_$m",u.photo?:"").apply()}}catch(_:Exception){}
        p.edit().putBoolean("legacy_migrated",true).apply()
    }
    private fun applyVisualSystem(a:Activity){a.window.statusBarColor=bg;a.window.navigationBarColor=bg;a.window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;style(a.window.decorView)}
    /** Android 15+ targets are edge-to-edge; keep interactive content out of system bars. */
    private fun applySystemBarInsets(a:Activity){
        val c=a.findViewById<View>(android.R.id.content)?:return; if(c.getTag(TAG_INSETS)==true)return; c.setTag(TAG_INSETS,true)
        val l=c.paddingLeft;val r=c.paddingRight
        ViewCompat.setOnApplyWindowInsetsListener(c){v,i->val bars=i.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout());val ime=i.getInsets(WindowInsetsCompat.Type.ime()).bottom;v.setPadding(l,bars.top+dp(6),r,maxOf(bars.bottom,ime)+dp(8));i};ViewCompat.requestApplyInsets(c)
    }
    /** Legacy forms used values such as height=58 and padding=24 as raw pixels. Convert them to dp. */
    private fun repairLegacyProgrammaticLayout(v:View){
        if(v.getTag(TAG_REPAIRED)==true)return;val d=resources.displayMetrics.density
        if(d>1.05f){v.setPadding((v.paddingLeft*d).toInt(),(v.paddingTop*d).toInt(),(v.paddingRight*d).toInt(),(v.paddingBottom*d).toInt());v.layoutParams?.let{lp->if(lp.width>0)lp.width=(lp.width*d).toInt();if(lp.height>0)lp.height=(lp.height*d).toInt();if(lp is ViewGroup.MarginLayoutParams){lp.leftMargin=(lp.leftMargin*d).toInt();lp.topMargin=(lp.topMargin*d).toInt();lp.rightMargin=(lp.rightMargin*d).toInt();lp.bottomMargin=(lp.bottomMargin*d).toInt()};v.layoutParams=lp}}
        v.setTag(TAG_REPAIRED,true);if(v is ViewGroup)for(i in 0 until v.childCount)repairLegacyProgrammaticLayout(v.getChildAt(i))
    }
    private fun style(v:View){
        when(v){
            is EditText->{v.typeface=Typeface.create("sans-serif",Typeface.NORMAL);v.setTextColor(navy);v.setHintTextColor(muted);v.setTextSize(16f);v.setPadding(dp(16),dp(12),dp(16),dp(12));v.minHeight=dp(54);v.background=rounded(Color.WHITE,border,12);v.includeFontPadding=false}
            is Button->{v.typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);v.setTextColor(Color.WHITE);v.setTextSize(14f);v.gravity=android.view.Gravity.CENTER;v.includeFontPadding=false;v.minHeight=dp(52);v.minWidth=dp(88);v.setPadding(dp(14),dp(8),dp(14),dp(8));v.background=rounded(blue,blue,12);if(v.contentDescription.isNullOrBlank())v.contentDescription=v.text.toString()}
            is Spinner->{v.minimumHeight=dp(54);v.background=rounded(Color.WHITE,border,12);v.setPadding(dp(12),0,dp(12),0)}
            is TextView->{val bold=v.typeface?.isBold==true;v.typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL);v.includeFontPadding=false}
            is ScrollView->{v.setBackgroundColor(bg);v.clipToPadding=false}
        };if(v is ViewGroup)for(i in 0 until v.childCount)style(v.getChildAt(i))
    }
    private fun rounded(fill:Int,stroke:Int,r:Int)=GradientDrawable().apply{setColor(fill);setStroke(dp(1),stroke);cornerRadius=dp(r).toFloat()}
    private fun applyMobileLimits(v:View){if(v is EditText){val h=v.hint?.toString()?.lowercase() ?: "";if(h.contains("mobile number")||h.contains("alternate mobile")){v.filters=arrayOf(InputFilter.LengthFilter(10));v.keyListener=DigitsKeyListener.getInstance("0123456789")}};if(v is ViewGroup)for(i in 0 until v.childCount)applyMobileLimits(v.getChildAt(i))}
    private fun installKeyboardAwareScrolling(root:View){
        if(root is ScrollView){if(root.getTag(TAG_KEYBOARD)!=true){root.setTag(TAG_KEYBOARD,true);ViewCompat.setOnApplyWindowInsetsListener(root){v,i->val ime=i.getInsets(WindowInsetsCompat.Type.ime()).bottom;val sys=i.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;v.setPadding(v.paddingLeft,v.paddingTop,v.paddingRight,maxOf(ime,sys)+dp(12));i};ViewCompat.requestApplyInsets(root);attachFocusHandlers(root,root)}}
        if(root is ViewGroup)for(i in 0 until root.childCount)installKeyboardAwareScrolling(root.getChildAt(i))
    }
    private fun attachFocusHandlers(s:ScrollView,v:View){
        if(v is EditText&&v.getTag(TAG_FOCUS)!=true){v.setTag(TAG_FOCUS,true);v.setOnFocusChangeListener{f,hasFocus->if(hasFocus)f.postDelayed({val rr=Rect(0,0,f.width,f.height);try{s.offsetDescendantRectToMyCoords(f,rr);val top=s.paddingTop+dp(12);val bottom=s.height-s.paddingBottom-dp(12);if(rr.bottom>bottom)s.smoothScrollBy(0,rr.bottom-bottom)else if(rr.top<top)s.smoothScrollBy(0,rr.top-top)}catch(_:Exception){f.requestRectangleOnScreen(rr,true)}},120L)}}
        if(v is ViewGroup)for(i in 0 until v.childCount)attachFocusHandlers(s,v.getChildAt(i))
    }
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
    companion object{private const val TAG_REPAIRED=0x55444955;private const val TAG_INSETS=0x55444956;private const val TAG_KEYBOARD=0x55444957;private const val TAG_FOCUS=0x55444958}
}
