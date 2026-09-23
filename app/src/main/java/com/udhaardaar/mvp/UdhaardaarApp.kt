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

/** V6.2 global safeguards: no runtime layout rewriting; non-blocking migration; keyboard-safe forms. */
class UdhaardaarApp:Application(){
 private val bg=Color.rgb(246,248,251);private val navy=Color.rgb(25,43,65);private val blue=Color.rgb(38,99,235);private val border=Color.rgb(218,225,233);private val muted=Color.rgb(96,112,128)
 override fun onCreate(){super.onCreate();V7LegacyEventBridge.install();Thread{migrateLegacyOwner()}.start();registerActivityLifecycleCallbacks(object:ActivityLifecycleCallbacks{override fun onActivityResumed(a:Activity){a.window.decorView.post{applyBars(a);applyMobileLimits(a.window.decorView);installKeyboard(a.window.decorView);style(a.window.decorView)}};override fun onActivityCreated(a:Activity,b:Bundle?)=Unit;override fun onActivityStarted(a:Activity)=Unit;override fun onActivityPaused(a:Activity)=Unit;override fun onActivityStopped(a:Activity)=Unit;override fun onActivitySaveInstanceState(a:Activity,b:Bundle)=Unit;override fun onActivityDestroyed(a:Activity)=Unit})}
 private fun migrateLegacyOwner(){val p=getSharedPreferences("udhaardaar_accounts",MODE_PRIVATE);if(p.getBoolean("legacy_migrated",false))return;runCatching{val u=V32DatabaseHelper(this).userData();if(u!=null){val m=u.mobile.filter(Char::isDigit);if(m.length==10&&!p.contains("name_$m"))p.edit().putString("name_$m",u.name).putString("address_$m",u.address).putString("email_$m",u.email).putString("photo_$m",u.photo?:"").apply()}};p.edit().putBoolean("legacy_migrated",true).apply()}
 private fun applyBars(a:Activity){a.window.statusBarColor=bg;a.window.navigationBarColor=bg;a.window.decorView.systemUiVisibility=View.SYSTEM_UI_FLAG_LIGHT_STATUS_BAR or View.SYSTEM_UI_FLAG_LIGHT_NAVIGATION_BAR;val c=a.findViewById<View>(android.R.id.content)?:return;if(c.getTag(TAG_BARS)==true)return;c.setTag(TAG_BARS,true);val l=c.paddingLeft;val r=c.paddingRight;ViewCompat.setOnApplyWindowInsetsListener(c){v,i->val bars=i.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout());val ime=i.getInsets(WindowInsetsCompat.Type.ime()).bottom;v.setPadding(l,bars.top+dp(4),r,maxOf(bars.bottom,ime)+dp(8));i};ViewCompat.requestApplyInsets(c)}
 private fun style(v:View){when(v){is EditText->{v.typeface=Typeface.create("sans-serif",Typeface.NORMAL);v.setTextColor(navy);v.setHintTextColor(muted);v.includeFontPadding=false;v.minHeight=dp(52);v.background=rounded(Color.WHITE,border,12)};is Button->{v.typeface=Typeface.create("sans-serif-medium",Typeface.NORMAL);v.includeFontPadding=false;v.minHeight=dp(50);v.setPadding(dp(12),dp(7),dp(12),dp(7))};is Spinner->{v.minimumHeight=dp(52);v.background=rounded(Color.WHITE,border,12)};is ScrollView->{v.clipToPadding=false}};if(v is ViewGroup)for(i in 0 until v.childCount)style(v.getChildAt(i))}
 private fun rounded(f:Int,s:Int,r:Int)=GradientDrawable().apply{setColor(f);setStroke(dp(1),s);cornerRadius=dp(r).toFloat()}
 private fun applyMobileLimits(v:View){if(v is EditText){val h=v.hint?.toString()?.lowercase()?:"";if(h.contains("mobile number")||h.contains("alternate mobile")){v.filters=arrayOf(InputFilter.LengthFilter(10));v.keyListener=DigitsKeyListener.getInstance("0123456789")}};if(v is ViewGroup)for(i in 0 until v.childCount)applyMobileLimits(v.getChildAt(i))}
 private fun installKeyboard(v:View){if(v is ScrollView&&v.getTag(TAG_IME)!=true){v.setTag(TAG_IME,true);ViewCompat.setOnApplyWindowInsetsListener(v){x,i->val ime=i.getInsets(WindowInsetsCompat.Type.ime()).bottom;val sys=i.getInsets(WindowInsetsCompat.Type.systemBars()).bottom;x.setPadding(x.paddingLeft,x.paddingTop,x.paddingRight,maxOf(ime,sys)+dp(28));i};ViewCompat.requestApplyInsets(v);focus(v,v)};if(v is ViewGroup)for(i in 0 until v.childCount)installKeyboard(v.getChildAt(i))}
 private fun focus(s:ScrollView,v:View){if(v is EditText&&v.getTag(TAG_FOCUS)!=true){v.setTag(TAG_FOCUS,true);v.setOnFocusChangeListener{f,has->if(has)f.postDelayed({val r=Rect(0,0,f.width,f.height);runCatching{s.offsetDescendantRectToMyCoords(f,r);val bottom=s.height-s.paddingBottom-dp(20);if(r.bottom>bottom)s.smoothScrollBy(0,r.bottom-bottom)}},180)}};if(v is ViewGroup)for(i in 0 until v.childCount)focus(s,v.getChildAt(i))}
 private fun dp(v:Int)=(v*resources.displayMetrics.density).toInt();companion object{private const val TAG_BARS=0x55444956;private const val TAG_IME=0x55444957;private const val TAG_FOCUS=0x55444958}
}
