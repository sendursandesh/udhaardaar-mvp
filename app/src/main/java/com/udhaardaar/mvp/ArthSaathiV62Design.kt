package com.udhaardaar.mvp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

/** Canonical ArthSaathi V6.2 visual system. Clean fintech hierarchy, consistent across every V6.2 screen. */
object ArthSaathiV62Design {
    val NAVY = Color.rgb(12, 35, 64)
    val TEAL = Color.rgb(0, 143, 135)
    val BLUE = Color.rgb(42, 101, 214)
    val GOLD = Color.rgb(201, 153, 43)
    val GREEN = Color.rgb(25, 133, 88)
    val RED = Color.rgb(190, 68, 76)
    val BG = Color.rgb(245, 248, 251)
    val WHITE = Color.WHITE
    val MUTED = Color.rgb(91, 106, 121)
    val BORDER = Color.rgb(219, 227, 235)
    val PALE_TEAL = Color.rgb(235, 248, 246)
    val PALE_BLUE = Color.rgb(237, 244, 255)
    val PALE_GOLD = Color.rgb(252, 247, 234)
    val PALE_RED = Color.rgb(253, 241, 242)

    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Navigate Your Financial Journey"
    const val PILLARS = "Plan • Protect • Grow • Nominate"
    const val LOGO_RESOURCE = "@drawable/arthsaathi_logo"

    fun dp(v: Int, d: Float) = (v * d).toInt()
    private fun density(c: Context) = c.resources.displayMetrics.density
    private fun face(weight: Int = Typeface.NORMAL) = Typeface.create("sans-serif", weight)
    private fun medium() = Typeface.create("sans-serif-medium", Typeface.NORMAL)

    fun text(c: Context, s: String, size: Float = 14f, color: Int = NAVY, bold: Boolean = false) = TextView(c).apply {
        text = s
        textSize = size
        setTextColor(color)
        includeFontPadding = false
        typeface = if (bold) medium() else face()
        letterSpacing = if (size <= 11f) .04f else .0f
    }

    fun card(fill: Int = WHITE, radius: Int = 16, d: Float = 1f) = GradientDrawable().apply {
        setColor(fill)
        setStroke(dp(1, d), BORDER)
        cornerRadius = dp(radius, d).toFloat()
    }

    fun hero(fill: Int = NAVY, radius: Int = 20, d: Float = 1f) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius, d).toFloat()
    }

    fun button(c: Context, s: String, color: Int = BLUE, click: () -> Unit) = Button(c).apply {
        text = s
        textSize = 13f
        setTextColor(WHITE)
        typeface = medium()
        isAllCaps = false
        minHeight = dp(48, density(c))
        minimumHeight = dp(48, density(c))
        stateListAnimator = null
        elevation = dp(2, density(c)).toFloat()
        background = GradientDrawable().apply { setColor(color); cornerRadius = dp(12, density(c)).toFloat() }
        backgroundTintList = null
        setPadding(dp(16, density(c)), 0, dp(16, density(c)), 0)
        setOnClickListener { click() }
    }

    /** Compact branded header. The logo is the product mark; pages add their own module title below. */
    fun title(c: Context, name: String, subtitle: String): LinearLayout {
        val d = density(c)
        val outer = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8, d), dp(6, d), dp(8, d), dp(7, d))
        }
        outer.addView(ImageView(c).apply {
            setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "ArthSaathi logo"
        }, LinearLayout.LayoutParams(dp(48, d), dp(48, d)))
        val copy = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(10, d), 0, 0, 0) }
        copy.addView(text(c, name, 22f, NAVY, true))
        copy.addView(text(c, subtitle, 10.5f, TEAL, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4, d) })
        outer.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
        return outer
    }

    fun pageHeader(c: Context, eyebrow: String, name: String, subtitle: String): LinearLayout {
        val d = density(c)
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(16, d), dp(14, d), dp(16, d), dp(14, d))
            background = card(WHITE, 16, d)
            elevation = dp(1, d).toFloat()
        }
        box.addView(text(c, eyebrow.uppercase(), 9f, TEAL, true))
        box.addView(text(c, name, 21f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        box.addView(text(c, subtitle, 11f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        return box
    }

    fun section(c: Context, s: String) = text(c, s.uppercase(), 10f, MUTED, true).apply {
        setPadding(0, dp(13, density(c)), 0, dp(6, density(c)))
        letterSpacing = .11f
    }

    fun input(c: Context, hint: String) = EditText(c).apply {
        this.hint = hint
        textSize = 15f
        setSingleLine(true)
        setTextColor(NAVY)
        setHintTextColor(Color.rgb(125, 139, 153))
        minHeight = dp(52, density(c))
        setPadding(dp(14, density(c)), dp(8, density(c)), dp(14, density(c)), dp(8, density(c)))
        background = card(WHITE, 12, density(c))
    }

    fun add(root: LinearLayout, v: View, top: Int = 7) {
        root.addView(v, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top, density(root.context)) })
    }

    fun statCard(c: Context, label: String, value: String, accent: Int): LinearLayout {
        val d = density(c)
        return LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(13, d), dp(11, d), dp(10, d), dp(10, d))
            background = card(WHITE, 14, d)
            elevation = dp(1, d).toFloat()
            addView(text(c, value, 20f, accent, true))
            addView(text(c, label.uppercase(), 8.5f, MUTED, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4, d) })
        }
    }

    fun moduleCard(c: Context, number: String, title: String, description: String, accent: Int, click: () -> Unit): LinearLayout {
        val d = density(c)
        return LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15, d), dp(13, d), dp(13, d), dp(12, d))
            background = card(WHITE, 16, d)
            elevation = dp(1, d).toFloat()
            setOnClickListener { click() }
            addView(TextView(c).apply {
                text = number
                textSize = 9.5f
                setTextColor(accent)
                typeface = medium()
                setPadding(0, 0, 0, 0)
            })
            addView(text(c, title, 15f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7, d) })
            addView(text(c, description, 10f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4, d) })
        }
    }
}
