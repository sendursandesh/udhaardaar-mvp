package com.udhaardaar.mvp

import android.app.Activity
import android.app.Application
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ScrollView
import android.widget.Spinner
import android.widget.TextView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UdhaardaarApp : Application() {
    private val v5Bg=Color.rgb(246,249,252);private val v5Navy=Color.rgb(24,58,92);private val v5Blue=Color.rgb(25,111,220);private val v5Border=Color.rgb(205,215,225)
    override fun onCreate(){super.onCreate();migrateLegacyOwner();registerActivityLifecycleCallbacks(object:ActivityLifecycleCallbacks{
        override fun onActivityResumed(a:Activity){a.window.decorView.post{if(a::class.java.simpleName.startsWith("V5"))applyV5VisualSystem(a);applyMobileLimits(a.window.decorView);installKeyboardAwareScrolling(a.window.decorView)}}
        override fun onActivityCreated(a:Activity,b:Bundle?){ };override fun onActivityStarted(a:Activity){};override fun onActivityPaused(a:Activity){};override fun onActivityStopped(a:Activity){};override fun onActivitySaveInstanceState(a:Activity,b:Bundle){};override fun onActivityDestroyed(a:Activity){}
    })}
    private fun migrateLegacyOwner(){val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE);if(p.getBoolean("legacy_migrated",false))return;try{val u=V32DatabaseHelper(this).userData();if(u!=null&&u.mobile.filter(Char::isDigit).length==10){val m=u.mobile.filter(Char::isDigit);if(!p.contains("name_$m"))p.edit().putString("name_$m",u.name).putString("address_$m",u.address).putString("email_$m",u.email).putString("photo_$m",u.photo?:"").apply()}}catch(_:Exception){};p.edit().putBoolean("legacy_migrated",true).apply()}
    private fun applyV5VisualSystem(a:Activity){a.window.statusBarColor=v5Bg;a.window.navigationBarColor=v5Bg;a.window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;style(a.window.decorView)}
    private fun style(v:View){when(v){is EditText->{v.setTextColor(v5Navy);v.setHintTextColor(Color.rgb(105,120,135));v.setPadding(dp(16),dp(10),dp(16),dp(10));v.minHeight=dp(52);v.background=rounded(Color.WHITE,v5Border,12)};is Button->{v.setTextColor(Color.WHITE);v.minHeight=dp(52);v.minWidth=dp(88);v.setPadding(dp(14),dp(8),dp(14),dp(8));v.background=rounded(v5Blue,v5Blue,12);if(v.contentDescription.isNullOrBlank())v.contentDescription=v.text.toString()};is Spinner->{v.minimumHeight=dp(52);v.background=rounded(Color.WHITE,v5Border,12);v.setPadding(dp(12),0,dp(12),0)};is TextView->{v.setTextColor(v5Navy);if(v.textSize<14f)v.setTextSize(14f)};is ScrollView->v.setBackgroundColor(v5Bg)};if(v is ViewGroup)for(i in 0 until v.childCount)style(v.getChildAt(i))}
    private fun rounded(fill:Int,stroke:Int,r:Int)=GradientDrawable().apply{setColor(fill);setStroke(dp(1),stroke);cornerRadius=dp(r).toFloat()}
    private fun applyMobileLimits(v:View){if(v is EditText){val h=v.hint?.toString()?.lowercase() ?: "";if(h.contains("mobile number")||h.contains("alternate mobile")){v.filters=arrayOf(InputFilter.LengthFilter(10));v.keyListener=DigitsKeyListener.getInstance("0123456789")}};if(v is ViewGroup)for(i in 0 until v.childCount)applyMobileLimits(v.getChildAt(i))}
    private fun installKeyboardAwareScrolling(root:View){if(root is ScrollView){ViewCompat.setOnApplyWindowInsetsListener(root){v,i->val ime=i.getInsets(WindowInsetsCompat.Type.ime()).bottom;val sys=i.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;v.setPadding(v.paddingLeft,v.paddingTop,v.paddingRight,maxOf(ime,sys)+dp(24));i};ViewCompat.requestApplyInsets(root);attachFocusHandlers(root,root)};if(root is ViewGroup)for(i in 0 until root.childCount)installKeyboardAwareScrolling(root.getChildAt(i))}
    private fun attachFocusHandlers(s:ScrollView,v:View){if(v is EditText)v.setOnFocusChangeListener{f,h->if(h)s.postDelayed{val r=Rect(0,0,f.width,f.height);try{s.offsetDescendantRectToMyCoords(f,r);val top=s.paddingTop+dp(12);val bottom=s.height-s.paddingBottom-dp(12);if(r.bottom>bottom)s.smoothScrollBy(0,r.bottom-bottom)else if(r.top<top)s.smoothScrollBy(0,r.top-top)}catch(_:Exception){f.requestRectangleOnScreen(r,true)}},180)};if(v is ViewGroup)for(i in 0 until v.childCount)attachFocusHandlers(s,v.getChildAt(i))}
    private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt()
}
