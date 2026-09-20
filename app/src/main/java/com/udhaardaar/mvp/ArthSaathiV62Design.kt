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

/**
 * ArthSaathi V6.2 visual master.
 *
 * Source of truth: user-supplied approved 16/17 Sep design board.
 * Visual rules: sunrise-gold/cream canvas, navy + gold brand, clean sans UI,
 * script reserved for the Journey tagline, rounded gold action buttons,
 * compact cards, consistent top identity and five-item bottom navigation.
 */
object ArthSaathiV62Design {
    val NAVY=Color.rgb(14,38,70)
    val NAVY_2=Color.rgb(23,75,131)
    val BLUE=Color.rgb(44,103,218)
    val TEAL=Color.rgb(12,155,145)
    val GOLD=Color.rgb(242,182,50)
    val GOLD_DEEP=Color.rgb(216,148,8)
    val GOLD_BRIGHT=Color.rgb(255,226,138)
    val GOLD_DARK=Color.rgb(169,109,0)
    val GREEN=Color.rgb(24,139,94)
    val RED=Color.rgb(190,68,76)
    val BG=Color.rgb(255,248,231)
    val WHITE=Color.WHITE
    val MUTED=Color.rgb(92,108,124)
    val BORDER=Color.rgb(224,213,185)
    val PALE_BLUE=Color.rgb(246,241,221)
    val PALE_GOLD=Color.rgb(255,248,226)

    const val BRAND="ArthSaathi"
    const val TAGLINE="Navigate Your Financial Journey"
    const val PILLARS="Plan • Protect • Grow • Nominate"
    const val LOGO_RESOURCE="@drawable/arthsaathi_logo"

    private fun density(c:Context)=c.resources.displayMetrics.density
    fun dp(v:Int,d:Float)=(v*d).toInt()

    /** Clean UI typography used throughout the app. */
    fun text(c:Context,s:String,size:Float=14f,color:Int=NAVY,bold:Boolean=false):TextView {
        return TextView(c).apply {
            text=s
            textSize=size
            setTextColor(color)
            includeFontPadding=false
            typeface=Typeface.create("sans-serif",if(bold)Typeface.BOLD else Typeface.NORMAL)
            letterSpacing=if(size<=11f).012f else 0f
        }
    }

    /** Script is intentionally limited to the brand journey line, as in the approved board. */
    fun script(c:Context,s:String,size:Float=13f,color:Int=GOLD_DARK):TextView {
        return TextView(c).apply {
            text=s
            textSize=size
            setTextColor(color)
            includeFontPadding=false
            typeface=Typeface.create("cursive",Typeface.NORMAL)
            gravity=Gravity.CENTER
        }
    }

    /** The supplied logo asset remains the single icon source; no substitute icon is used. */
    fun logo(c:Context,size:Int=72):ImageView {
        return ImageView(c).apply {
            setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo)
            scaleType=ImageView.ScaleType.CENTER_INSIDE
            contentDescription="ArthSaathi logo"
            adjustViewBounds=true
        }
    }

    /** Brand wordmark: clean, high-contrast, navy Arth + gold Saathi. */
    fun brandWordmark(c:Context,size:Float=25f):TextView {
        val view=text(c,BRAND,size,NAVY,true).apply { typeface = Typeface.create("serif", Typeface.BOLD) }
        val ss=SpannableString(BRAND)
        ss.setSpan(ForegroundColorSpan(NAVY),0,4,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        ss.setSpan(ForegroundColorSpan(GOLD_DEEP),4,BRAND.length,Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        view.text=ss
        view.gravity=Gravity.CENTER
        return view
    }

    fun card(fill:Int=WHITE,radius:Int=18,d:Float=1f)=GradientDrawable().apply{
        setColor(fill)
        setStroke(dp(1,d),BORDER)
        cornerRadius=dp(radius,d).toFloat()
    }

    fun hero(fill:Int=NAVY,radius:Int=22,d:Float=1f)=GradientDrawable().apply{
        setColor(fill)
        cornerRadius=dp(radius,d).toFloat()
    }

    /** Approved gold pill/button treatment used for primary actions. */
    fun button(c:Context,s:String,color:Int=GOLD_DEEP,click:()->Unit):Button {
        val d=density(c)
        val primary=color==GOLD || color==GOLD_DEEP
        return Button(c).apply {
            text=s
            textSize=13.5f
            setTextColor(if(primary) NAVY else WHITE)
            typeface=Typeface.create("sans-serif",Typeface.BOLD)
            isAllCaps=false
            minHeight=dp(46,d)
            minimumHeight=dp(46,d)
            stateListAnimator=null
            elevation=dp(1,d).toFloat()
            background=GradientDrawable(
                GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(color,if(primary) GOLD else color)
            ).apply{
                cornerRadius=dp(18,d).toFloat()
                setStroke(dp(1,d),if(primary) GOLD_DARK else BORDER)
            }
            setPadding(dp(15,d),0,dp(15,d),0)
            setOnClickListener{click()}
        }
    }

    /** Compact top bar used by internal module pages: back, module, user. */
    fun moduleTopBar(c:Context,module:String,onBack:()->Unit,onUser:()->Unit):LinearLayout {
        val d=density(c)
        val bar=LinearLayout(c).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(4,d),dp(5,d),dp(4,d),dp(5,d))
        }
        val back=TextView(c).apply{
            text="‹"
            textSize=34f
            setTextColor(NAVY)
            gravity=Gravity.CENTER
            typeface=Typeface.DEFAULT
            setOnClickListener{onBack()}
        }
        bar.addView(back,LinearLayout.LayoutParams(dp(42,d),dp(46,d)))
        bar.addView(text(c,module,18f,NAVY,true),LinearLayout.LayoutParams(0,dp(46,d),1f).apply{gravity=Gravity.CENTER_VERTICAL})
        val mobile=c.getSharedPreferences("udhaardaar_accounts",Context.MODE_PRIVATE).getString("current_mobile","").orEmpty()
        val user=c.getSharedPreferences("udhaardaar_accounts",Context.MODE_PRIVATE).getString("name_$mobile","User").orEmpty().ifBlank{"User"}
        val avatar=TextView(c).apply{
            text=user.trim().firstOrNull()?.uppercase() ?: "U"
            textSize=12f
            setTextColor(NAVY)
            gravity=Gravity.CENTER
            typeface=Typeface.DEFAULT_BOLD
            background=GradientDrawable().apply{
                shape=GradientDrawable.OVAL
                setColor(GOLD_BRIGHT)
                setStroke(dp(1,d),GOLD)
            }
            setOnClickListener{onUser()}
        }
        bar.addView(avatar,LinearLayout.LayoutParams(dp(36,d),dp(36,d)))
        return bar
    }

    /** Splash/login masthead follows the supplied board: large mark, wordmark, tagline, pillars. */
    fun masthead(c:Context,large:Boolean=true):LinearLayout {
        val d=density(c)
        val box=LinearLayout(c).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_HORIZONTAL
            setPadding(dp(8,d),dp(if(large) 4 else 0,d),dp(8,d),dp(4,d))
        }
        box.addView(logo(c,if(large)118 else 82),LinearLayout.LayoutParams(dp(if(large)118 else 82,d),dp(if(large)118 else 82,d)))
        box.addView(brandWordmark(c,if(large)31f else 24f),LinearLayout.LayoutParams(-1,-2))
        box.addView(script(c,TAGLINE,if(large)12.5f else 10.5f,GOLD_DARK),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(2,d)})
        box.addView(text(c,PILLARS,if(large)10f else 9f,GOLD_DARK,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4,d)})
        return box
    }

    /** Compatibility helper retained for existing module screens. */
    fun title(c:Context,name:String,subtitle:String):LinearLayout {
        val d=density(c)
        val box=LinearLayout(c).apply{orientation=LinearLayout.VERTICAL}
        box.addView(moduleTopBar(c,name,{(c as? android.app.Activity)?.finish()},{ }),LinearLayout.LayoutParams(-1,-2))
        box.addView(text(c,subtitle,11f,MUTED),LinearLayout.LayoutParams(-1,-2).apply{
            leftMargin=dp(47,d);rightMargin=dp(8,d);bottomMargin=dp(5,d)
        })
        return box
    }

    fun pageHeader(c:Context,eyebrow:String,name:String,subtitle:String):LinearLayout {
        val d=density(c)
        val box=LinearLayout(c).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(14,d),dp(11,d),dp(14,d),dp(11,d))
            background=card(PALE_BLUE,17,d)
        }
        box.addView(text(c,eyebrow.uppercase(),9.5f,TEAL,true))
        box.addView(text(c,name,21f,NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4,d)})
        box.addView(text(c,subtitle,11f,MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4,d)})
        return box
    }

    fun section(c:Context,s:String)=text(c,s.uppercase(),10.5f,NAVY,true).apply{
        setPadding(0,dp(11,density(c)),0,dp(5,density(c)))
        letterSpacing=.07f
    }

    fun input(c:Context,hint:String):EditText {
        val d=density(c)
        val e=EditText(c)
        e.hint=hint
        e.textSize=14.5f
        e.setSingleLine(true)
        e.setTextColor(NAVY)
        e.setHintTextColor(MUTED)
        e.minHeight=dp(48,d)
        e.setPadding(dp(13,d),dp(7,d),dp(13,d),dp(7,d))
        e.background=card(WHITE,12,d)
        val h=hint.lowercase()
        when{
            h.contains("mobile")-> {e.inputType=InputType.TYPE_CLASS_PHONE;e.filters=arrayOf(InputFilter.LengthFilter(10))}
            h.contains("aadhaar")-> {e.inputType=InputType.TYPE_CLASS_NUMBER;e.filters=arrayOf(InputFilter.LengthFilter(12))}
            h.contains("gstin")-> {e.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;e.filters=arrayOf(InputFilter.LengthFilter(15))}
            h.contains("pan")-> {e.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS;e.filters=arrayOf(InputFilter.LengthFilter(10))}
            h.contains("pin code")||h.contains("pincode")||h=="pin"->{e.inputType=InputType.TYPE_CLASS_NUMBER;e.filters=arrayOf(InputFilter.LengthFilter(6))}
            h.contains("otp")-> {e.inputType=InputType.TYPE_CLASS_NUMBER;e.filters=arrayOf(InputFilter.LengthFilter(6))}
            h.contains("email")-> {e.inputType=InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS;e.filters=arrayOf(InputFilter.LengthFilter(120))}
            else->e.filters=arrayOf(InputFilter.LengthFilter(160))
        }
        return e
    }

    fun add(root:LinearLayout,v:View,top:Int=7){
        root.addView(v,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(top,density(root.context))})
    }

    fun statCard(c:Context,label:String,value:String,accent:Int):LinearLayout {
        val d=density(c)
        val box=LinearLayout(c).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER_VERTICAL
            setPadding(dp(11,d),dp(9,d),dp(9,d),dp(8,d))
            background=card()
            elevation=dp(1,d).toFloat()
        }
        box.addView(text(c,value,17f,accent,true))
        box.addView(text(c,label,8.5f,MUTED,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)})
        return box
    }

    /** Screenshot-matching compact tile: icon badge, title, no duplicate action button. */
    fun featureCard(c:Context,icon:String,title:String,click:()->Unit):LinearLayout {
        val d=density(c)
        val box=LinearLayout(c).apply{
            orientation=LinearLayout.VERTICAL
            gravity=Gravity.CENTER
            setPadding(dp(5,d),dp(8,d),dp(5,d),dp(7,d))
            background=card(WHITE,14,d)
            elevation=dp(1,d).toFloat()
            setOnClickListener{click()}
        }
        val badge=TextView(c).apply{
            text=icon
            textSize=11f
            setTextColor(NAVY)
            gravity=Gravity.CENTER
            typeface=Typeface.DEFAULT_BOLD
            background=GradientDrawable().apply{
                shape=GradientDrawable.OVAL
                setColor(PALE_GOLD)
                setStroke(dp(1,d),GOLD)
            }
        }
        box.addView(badge,LinearLayout.LayoutParams(dp(38,d),dp(38,d)))
        val label=text(c,title,9.5f,NAVY,true)
        label.gravity=Gravity.CENTER
        box.addView(label,LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(5,d)})
        return box
    }

    fun moduleCard(c:Context,number:String,title:String,description:String,accent:Int,click:()->Unit):LinearLayout {
        val d=density(c)
        val box=LinearLayout(c).apply{
            orientation=LinearLayout.VERTICAL
            setPadding(dp(12,d),dp(10,d),dp(11,d),dp(10,d))
            background=card(WHITE,15,d)
            elevation=dp(1,d).toFloat()
            setOnClickListener{click()}
        }
        box.addView(text(c,number,9f,accent,true))
        box.addView(text(c,title,14f,NAVY,true),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(4,d)})
        box.addView(text(c,description,9.5f,MUTED),LinearLayout.LayoutParams(-1,-2).apply{topMargin=dp(3,d)})
        return box
    }

    fun bottomNav(c:Context,selected:String,actions:Map<String,()->Unit>):LinearLayout {
        val d=density(c)
        val nav=LinearLayout(c).apply{
            orientation=LinearLayout.HORIZONTAL
            gravity=Gravity.CENTER
            setPadding(dp(4,d),dp(4,d),dp(4,d),dp(5,d))
            background=GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,intArrayOf(NAVY,GOLD_DEEP,NAVY)).apply{
                cornerRadius=dp(19,d).toFloat()
            }
            elevation=dp(7,d).toFloat()
        }
        val items=listOf("HOME" to "Home","CR" to "Credit","RP" to "Repay","AV" to "Vault","MORE" to "More")
        for((icon,key) in items){
            val tv=TextView(c).apply{
                text="$icon\n$key"
                textSize=8f
                setTextColor(if(key==selected) WHITE else GOLD_BRIGHT)
                gravity=Gravity.CENTER
                typeface=Typeface.create("sans-serif",if(key==selected)Typeface.BOLD else Typeface.NORMAL)
                setOnClickListener{actions[key]?.invoke()}
            }
            nav.addView(tv,LinearLayout.LayoutParams(0,dp(48,d),1f))
        }
        return nav
    }
}
