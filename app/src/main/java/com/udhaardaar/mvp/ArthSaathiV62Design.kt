package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.*

object ArthSaathiV62Design {
    val NAVY=Color.rgb(14,38,70); val NAVY_2=Color.rgb(23,75,131); val BLUE=Color.rgb(44,103,218)
    val TEAL=Color.rgb(12,155,145); val GOLD=Color.rgb(242,182,50); val GOLD_DEEP=Color.rgb(216,148,8)
    val GOLD_BRIGHT=Color.rgb(255,226,138); val GOLD_DARK=Color.rgb(169,109,0); val GREEN=Color.rgb(24,139,94)
    val RED=Color.rgb(190,68,76); val BG=Color.rgb(255,248,231); val WHITE=Color.WHITE
    val MUTED=Color.rgb(92,108,124); val BORDER=Color.rgb(224,213,185); val PALE_BLUE=Color.rgb(246,241,221); val PALE_GOLD=Color.rgb(255,248,226)
    const val BRAND="ArthSaathi"; const val TAGLINE="Navigate Your Financial Journey"; const val PILLARS="Plan • Protect • Grow • Nominate"; const val OWNERSHIP_TAGLINE="Your Asset. Your Record. Your Right."; const val OWNERSHIP_TAGS="OWN • RECORD • PROTECT • CLAIM"; const val LOGO_RESOURCE="@drawable/arthsaathi_logo"
    private fun density(c:Context)=c.resources.displayMetrics.density
    fun dp(v:Int,d:Float)=(v*d).toInt()

    fun text(c:Context,s:String,size:Float=14f,color:Int=NAVY,bold:Boolean=false):TextView {
        return TextView(c).apply { text=s; textSize=size; setTextColor(color); includeFontPadding=false; typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL); letterSpacing=if(size<=11f).015f else 0f }
    }
    fun brandWordmark(c:Context,size:Float=25f):TextView {
        val view=text(c,BRAND,size,NAVY,true)
        val ss=SpannableString(BRAND); ss.setSpan(ForegroundColorSpan(NAVY),0,4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE); ss.setSpan(ForegroundColorSpan(GOLD_DEEP),4,BRAND.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text=ss; view.gravity=Gravity.CENTER; return view
    }
    fun card(fill:Int=WHITE,radius:Int=18,d:Float=1f)=GradientDrawable().apply{setColor(fill);setStroke(dp(1,d),BORDER);cornerRadius=dp(radius,d).toFloat()}
    fun hero(fill:Int=NAVY,radius:Int=22,d:Float=1f)=GradientDrawable().apply{setColor(fill);cornerRadius=dp(radius,d).toFloat()}
    fun button(c:Context,s:String,color:Int=GOLD_DEEP,click:()->Unit):Button {
        val d=density(c)
        return Button(c).apply { text=s;textSize=14f;setTextColor(if(color==GOLD||color==GOLD_DEEP)NAVY else WHITE);typeface=Typeface.DEFAULT_BOLD;isAllCaps=false;minHeight=dp(50,d);minimumHeight=dp(50,d);stateListAnimator=null;elevation=dp(2,d).toFloat();background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(color,if(color==GOLD_DEEP)GOLD else color)).apply{cornerRadius=dp(15,d).toFloat();setStroke(dp(1,d),if(color==GOLD||color==GOLD_DEEP)GOLD_DARK else BORDER)};setPadding(dp(16,d),0,dp(16,d),0);setOnClickListener{click()} }
    }
    fun title(c:Context,name:String,subtitle:String):LinearLayout {
        val d=density(c); val box=LinearLayout(c); box.orientation=LinearLayout.VERTICAL; box.gravity=Gravity.CENTER_HORIZONTAL; box.setPadding(dp(10,d),dp(7,d),dp(10,d),dp(9,d)); box.background=card(); box.elevation=dp(2,d).toFloat()
        val logo=ImageView(c);logo.setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo);logo.scaleType=ImageView.ScaleType.CENTER_INSIDE;logo.contentDescription="ArthSaathi logo";box.addView(logo,LinearLayout.LayoutParams(dp(62,d),dp(62,d)))
        box.addView(brandWordmark(c,22f),LinearLayout.LayoutParams(-1,-2));box.addView(text(c,subtitle,10f,NAVY),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)});box.addView(text(c,OWNERSHIP_TAGLINE,9.5f,NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4,d)});box.addView(text(c,OWNERSHIP_TAGS,8.5f,TEAL,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)});box.addView(text(c,PILLARS,9.5f,GOLD_DARK,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)})
        return box
    }
    fun pageHeader(c:Context,eyebrow:String,name:String,subtitle:String):LinearLayout {
        val d=density(c);val box=LinearLayout(c);box.orientation=LinearLayout.VERTICAL;box.setPadding(dp(15,d),dp(12,d),dp(15,d),dp(12,d));box.background=card(PALE_BLUE,18,d);box.addView(text(c,eyebrow.uppercase(),9.5f,TEAL,true));box.addView(text(c,name,22f,NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)});box.addView(text(c,subtitle,11f,MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)});return box
    }
    fun section(c:Context,s:String)=text(c,s.uppercase(),10.5f,NAVY,true).apply{setPadding(0,dp(12,density(c)),0,dp(5,density(c)));letterSpacing=.075f}
    fun input(c:Context,hint:String):EditText {
        val d=density(c);val e=EditText(c);e.hint=hint;e.textSize=15f;e.setSingleLine(true);e.setTextColor(NAVY);e.setHintTextColor(MUTED);e.minHeight=dp(50,d);e.setPadding(dp(14,d),dp(8,d),dp(14,d),dp(8,d));e.background=card(WHITE,13,d)
        val h=hint.lowercase();when{h.contains("mobile")-> {e.inputType=InputType.TYPE_CLASS_PHONE;e.filters=arrayOf(InputFilter.LengthFilter(10))};h.contains("aadhaar")-> {e.inputType=InputType.TYPE_CLASS_NUMBER;e.filters=arrayOf(InputFilter.LengthFilter(12))};h.contains("gstin")-> {e.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;e.filters=arrayOf(InputFilter.LengthFilter(15))};h.contains("pan")-> {e.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;e.filters=arrayOf(InputFilter.LengthFilter(10))};h.contains("pin code")||h.contains("pincode")||h=="pin"->{e.inputType=InputType.TYPE_CLASS_NUMBER;e.filters=arrayOf(InputFilter.LengthFilter(6))};h.contains("otp")-> {e.inputType=InputType.TYPE_CLASS_NUMBER;e.filters=arrayOf(InputFilter.LengthFilter(6))};h.contains("email")-> {e.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;e.filters=arrayOf(InputFilter.LengthFilter(120))};else->e.filters=arrayOf(InputFilter.LengthFilter(160))}
        return e
    }
    fun add(root:LinearLayout,v:View,top:Int=7){root.addView(v,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(top,density(root.context))})}
    fun statCard(c:Context,label:String,value:String,accent:Int):LinearLayout { val d=density(c);val box=LinearLayout(c);box.orientation=LinearLayout.VERTICAL;box.gravity=Gravity.CENTER_VERTICAL;box.setPadding(dp(12,d),dp(10,d),dp(10,d),dp(9,d));box.background=card();box.elevation=dp(1,d).toFloat();box.addView(text(c,value,18f,accent,true));box.addView(text(c,label,8.5f,MUTED,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)});return box }
    fun featureCard(c:Context,icon:String,title:String,click:()->Unit):LinearLayout {
        val d=density(c);val box=LinearLayout(c);box.orientation=LinearLayout.VERTICAL;box.gravity=Gravity.CENTER;box.setPadding(dp(6,d),dp(8,d),dp(6,d),dp(8,d));box.background=card();box.elevation=dp(1,d).toFloat();box.setOnClickListener{click()}
        val badge=TextView(c);badge.text=icon;badge.textSize=11f;badge.setTextColor(NAVY);badge.gravity=Gravity.CENTER;badge.typeface=Typeface.DEFAULT_BOLD;badge.background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(PALE_GOLD);setStroke(dp(1,d),GOLD)}
        box.addView(badge,LinearLayout.LayoutParams(dp(38,d),dp(38,d)));val label=text(c,title,9.5f,NAVY,true);label.gravity=Gravity.CENTER;box.addView(label,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)});return box
    }
    fun moduleCard(c:Context,number:String,title:String,description:String,accent:Int,click:()->Unit):LinearLayout { val d=density(c);val box=LinearLayout(c);box.orientation=LinearLayout.VERTICAL;box.setPadding(dp(13,d),dp(11,d),dp(12,d),dp(11,d));box.background=card();box.elevation=dp(1,d).toFloat();box.setOnClickListener{click()};box.addView(text(c,number,9f,accent,true));box.addView(text(c,title,14f,NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)});box.addView(text(c,description,9.5f,MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)});return box }
    fun bottomNav(c:Context,selected:String,actions:Map<String,()->Unit>):LinearLayout {
        val d=density(c);val nav=LinearLayout(c);nav.orientation=LinearLayout.HORIZONTAL;nav.gravity=Gravity.CENTER;nav.setPadding(dp(4,d),dp(5,d),dp(4,d),dp(6,d));nav.background=card();nav.elevation=dp(7,d).toFloat()
        val items=listOf("HOME" to "Home","CR" to "Credit","RP" to "Repay","AV" to "Vault","MORE" to "More")
        for((icon,key) in items){val tv=TextView(c);tv.text="$icon\n$key";tv.textSize=8.5f;tv.setTextColor(if(key==selected)GOLD_DARK else NAVY);tv.gravity=Gravity.CENTER;tv.typeface=Typeface.create("sans-serif",if(key==selected)Typeface.BOLD else Typeface.NORMAL);tv.setOnClickListener{actions[key]?.invoke()};nav.addView(tv,LinearLayout.LayoutParams(0,dp(52,d),1f))}
        return nav
    }
}
