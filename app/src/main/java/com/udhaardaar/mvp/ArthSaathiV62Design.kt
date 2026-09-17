package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.content.res.ColorStateList
import android.text.InputFilter
import android.text.InputType
import android.text.SpannableString
import android.text.Spanned
import android.text.style.ForegroundColorSpan
import android.view.Gravity
import android.view.View
import android.widget.*

/** Single visual source used by every ArthSaathi V6.2 screen. */
object ArthSaathiV62Design {
    val NAVY = Color.rgb(14,38,70)
    val NAVY_2 = Color.rgb(23,75,131)
    val BLUE = Color.rgb(44,103,218)
    val TEAL = Color.rgb(12,155,145)
    val GOLD = Color.rgb(242,182,50)
    val GOLD_DEEP = Color.rgb(216,148,8)
    val GOLD_BRIGHT = Color.rgb(255,226,138)
    val GOLD_DARK = Color.rgb(169,109,0)
    val GREEN = Color.rgb(24,139,94)
    val RED = Color.rgb(190,68,76)
    val BG = Color.rgb(255,248,231)
    val WHITE = Color.WHITE
    val MUTED = Color.rgb(92,108,124)
    val BORDER = Color.rgb(224,213,185)
    val PALE_BLUE = Color.rgb(246,241,221)
    val PALE_GOLD = Color.rgb(255,248,226)
    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Navigate Your Financial Journey"
    const val PILLARS = "Plan • Protect • Grow • Nominate"
    const val LOGO_RESOURCE = "@drawable/arthsaathi_logo"

    private fun density(c: Context)=c.resources.displayMetrics.density
    fun dp(v:Int,d:Float)=(v*d).toInt()

    fun text(c:Context,s:String,size:Float=14f,color:Int=NAVY,bold:Boolean=false)=TextView(c).apply{
        text=s;textSize=size;setTextColor(color);includeFontPadding=false
        typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL)
        letterSpacing=if(size<=11f).015f else 0f
    }

    fun brandWordmark(c:Context,size:Float=25f)=text(c,BRAND,size,NAVY,true).apply{
        val ss=SpannableString(BRAND)
        ss.setSpan(ForegroundColorSpan(NAVY),0,4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(GOLD_DEEP),4,BRAND.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text=ss;gravity=Gravity.CENTER
    }

    fun card(fill:Int=WHITE,radius:Int=18,d:Float=1f)=GradientDrawable().apply{
        setColor(fill);setStroke(dp(1,d),BORDER);cornerRadius=dp(radius,d).toFloat()
    }
    fun hero(fill:Int=NAVY,radius:Int=22,d:Float=1f)=GradientDrawable().apply{setColor(fill);cornerRadius=dp(radius,d).toFloat()}

    fun button(c:Context,s:String,color:Int=GOLD_DEEP,click:()->Unit)=Button(c).apply{
        text=s;textSize=14f;setTextColor(if(color==GOLD||color==GOLD_DEEP)NAVY else WHITE)
        typeface=Typeface.create("sans-serif",Typeface.BOLD);isAllCaps=false
        minHeight=dp(50,density(c));minimumHeight=dp(50,density(c));stateListAnimator=null;elevation=dp(2,density(c)).toFloat()
        background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(color,if(color==GOLD_DEEP)GOLD else color)).apply{cornerRadius=dp(15,density(c)).toFloat();setStroke(dp(1,density(c)),if(color==GOLD||color==GOLD_DEEP)GOLD_DARK else BORDER)}
        backgroundTintList=null;setPadding(dp(16,density(c)),0,dp(16,density(c)),0);setOnClickListener{click()}
    }

    fun title(c:Context,name:String,subtitle:String):LinearLayout{
        val d=density(c);return LinearLayout(c).apply{
            orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(dp(10,d),dp(7,d),dp(10,d),dp(9,d));background=card();elevation=dp(2,d).toFloat()
            addView(ImageView(c).apply{setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo);scaleType=ImageView.ScaleType.CENTER_INSIDE;contentDescription="ArthSaathi logo"},LinearLayout.LayoutParams(dp(62,d),dp(62,d)))
            addView(brandWordmark(c,22f),LinearLayout.LayoutParams(-1,-2));addView(text(c,subtitle,10f,NAVY),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)})
            addView(text(c,PILLARS,9.5f,GOLD_DARK,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)})
        }
    }

    fun pageHeader(c:Context,eyebrow:String,name:String,subtitle:String):LinearLayout{
        val d=density(c);return LinearLayout(c).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(15,d),dp(12,d),dp(15,d),dp(12,d));background=card(PALE_BLUE,18,d)
            addView(text(c,eyebrow.uppercase(),9.5f,TEAL,true));addView(text(c,name,22f,NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)});addView(text(c,subtitle,11f,MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)})}
    }
    fun section(c:Context,s:String)=text(c,s.uppercase(),10.5f,NAVY,true).apply{setPadding(0,dp(12,density(c)),0,dp(5,density(c)));letterSpacing=.075f}

    fun input(c:Context,hint:String)=EditText(c).apply{
        this.hint=hint;textSize=15f;setSingleLine(true);setTextColor(NAVY);setHintTextColor(MUTED);minHeight=dp(50,density(c));setPadding(dp(14,density(c)),dp(8,density(c)),dp(14,density(c)),dp(8,density(c)));background=card(WHITE,13,density(c))
        when{hint.lowercase().contains("mobile")-> {inputType=InputType.TYPE_CLASS_PHONE;filters=arrayOf(InputFilter.LengthFilter(10))}
            hint.lowercase().contains("aadhaar")-> {inputType=InputType.TYPE_CLASS_NUMBER;filters=arrayOf(InputFilter.LengthFilter(12))}
            hint.lowercase().contains("gstin")-> {inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;filters=arrayOf(InputFilter.LengthFilter(15))}
            hint.lowercase().contains("pan")-> {inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;filters=arrayOf(InputFilter.LengthFilter(10))}
            hint.lowercase().contains("pin code")||hint.lowercase().contains("pincode")||hint.lowercase()=="pin"->{inputType=InputType.TYPE_CLASS_NUMBER;filters=arrayOf(InputFilter.LengthFilter(6))}
            hint.lowercase().contains("otp")-> {inputType=InputType.TYPE_CLASS_NUMBER;filters=arrayOf(InputFilter.LengthFilter(6))}
            hint.lowercase().contains("email")-> {inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;filters=arrayOf(InputFilter.LengthFilter(120))}
            else->filters=arrayOf(InputFilter.LengthFilter(160))}
    }
    fun add(root:LinearLayout,v:View,top:Int=7){root.addView(v,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(top,density(root.context))}}

    fun statCard(c:Context,label:String,value:String,accent:Int):LinearLayout{val d=density(c);return LinearLayout(c).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_VERTICAL;setPadding(dp(12,d),dp(10,d),dp(10,d),dp(9,d));background=card();elevation=dp(1,d).toFloat();addView(text(c,value,18f,accent,true));addView(text(c,label,8.5f,MUTED,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)})}}

    /** Clean monogram badges avoid device-dependent emoji fonts while preserving the approved compact pictogram rhythm. */
    fun featureCard(c:Context,icon:String,title:String,click:()->Unit):LinearLayout{val d=density(c);return LinearLayout(c).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER;setPadding(dp(6,d),dp(8,d),dp(6,d),dp(8,d));background=card();elevation=dp(1,d).toFloat();setOnClickListener{click()}
        val badge=TextView(c).apply{text=icon;textSize=11f;setTextColor(NAVY);gravity=Gravity.CENTER;typeface=Typeface.DEFAULT_BOLD;background=GradientDrawable().apply{shape=GradientDrawable.OVAL;setColor(PALE_GOLD);setStroke(dp(1,d),GOLD)}}
        addView(badge,LinearLayout.LayoutParams(dp(38,d),dp(38,d)));addView(text(c,title,9.5f,NAVY,true).apply{gravity=Gravity.CENTER},LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)})}}

    fun moduleCard(c:Context,number:String,title:String,description:String,accent:Int,click:()->Unit):LinearLayout{val d=density(c);return LinearLayout(c).apply{orientation=LinearLayout.VERTICAL;setPadding(dp(13,d),dp(11,d),dp(12,d),dp(11,d));background=card();elevation=dp(1,d).toFloat();setOnClickListener{click()};addView(text(c,number,9f,accent,true));addView(text(c,title,14f,NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)});addView(text(c,description,9.5f,MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)})}}

    fun bottomNav(c:Context,selected:String,actions:Map<String,()->Unit>):LinearLayout{val d=density(c);return LinearLayout(c).apply{orientation=LinearLayout.HORIZONTAL;gravity=Gravity.CENTER;setPadding(dp(4,d),dp(5,d),dp(4,d),dp(6,d));background=card();elevation=dp(7,d).toFloat()
        listOf("HOME" to "Home","CR" to "Credit","RP" to "Repay","AV" to "Vault","MORE" to "More").forEach{(icon,key)->addView(TextView(c).apply{text="$icon\n$key";textSize=8.5f;setTextColor(if(key==selected)GOLD_DARK else NAVY);gravity=Gravity.CENTER;typeface=Typeface.create("sans-serif",if(key==selected)Typeface.BOLD else Typeface.NORMAL);setOnClickListener{actions[key]?.invoke()}},LinearLayout.LayoutParams(0,dp(52,d),1f))}}
}
