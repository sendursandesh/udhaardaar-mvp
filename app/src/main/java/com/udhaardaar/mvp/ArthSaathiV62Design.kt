package com.udhaardaar.mvp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

/** Canonical ArthSaathi V6.2 visual system used by every modern module screen. */
object ArthSaathiV62Design {
    val NAVY = Color.rgb(14, 38, 70)
    val TEAL = Color.rgb(12, 155, 145)
    val BLUE = Color.rgb(44, 103, 218)
    val GOLD = Color.rgb(208, 160, 42)
    val GREEN = Color.rgb(24, 139, 94)
    val RED = Color.rgb(190, 68, 76)
    val BG = Color.rgb(246, 249, 252)
    val WHITE = Color.WHITE
    val MUTED = Color.rgb(92, 108, 124)
    val BORDER = Color.rgb(220, 228, 236)
    val PALE_TEAL = Color.rgb(235, 248, 246)
    val PALE_BLUE = Color.rgb(238, 244, 255)
    val PALE_GOLD = Color.rgb(252, 247, 232)
    val PALE_RED = Color.rgb(253, 240, 241)

    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Navigate Your Financial Journey"
    const val PILLARS = "Plan • Protect • Grow • Nominate"
    const val LOGO_RESOURCE = "@drawable/arthsaathi_logo"

    fun dp(v: Int, d: Float) = (v * d).toInt()
    private fun density(c: Context) = c.resources.displayMetrics.density

    fun text(c: Context, s: String, size: Float = 14f, color: Int = NAVY, bold: Boolean = false) = TextView(c).apply {
        text = s
        textSize = size
        setTextColor(color)
        includeFontPadding = false
        typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        letterSpacing = if (size <= 11f) .025f else 0f
    }

    fun card(fill: Int = WHITE, radius: Int = 18, d: Float = 1f) = GradientDrawable().apply {
        setColor(fill)
        setStroke(dp(1, d), BORDER)
        cornerRadius = dp(radius, d).toFloat()
    }

    /** Premium dark hero with a subtle navy-to-blue visual transition. */
    fun hero(fill: Int = NAVY, radius: Int = 22, d: Float = 1f) = GradientDrawable().apply {
        colors = intArrayOf(fill, BLUE)
        orientation = GradientDrawable.Orientation.TL_BR
        cornerRadius = dp(radius, d).toFloat()
    }

    /** Consistent rounded action control with strong touch target and restrained elevation. */
    fun button(c: Context, s: String, color: Int = BLUE, click: () -> Unit) = Button(c).apply {
        text = s
        textSize = 13f
        setTextColor(WHITE)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        isAllCaps = false
        minHeight = dp(48, density(c))
        minimumHeight = dp(48, density(c))
        stateListAnimator = null
        elevation = dp(2, density(c)).toFloat()
        background = card(color, 15, density(c))
        backgroundTintList = ColorStateList.valueOf(color)
        setPadding(dp(16, density(c)), 0, dp(16, density(c)), 0)
        setOnClickListener { click() }
    }

    /** Branded module header: journey mark, module identity and product promise. */
    fun title(c: Context, name: String, subtitle: String): LinearLayout {
        val d = density(c)
        val outer = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14, d), dp(12, d), dp(14, d), dp(12, d))
            background = card(PALE_BLUE, 20, d)
            elevation = dp(2, d).toFloat()
        }
        val row = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        row.addView(ImageView(c).apply {
            setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "ArthSaathi — Navigate Your Financial Journey"
        }, LinearLayout.LayoutParams(dp(62, d), dp(62, d)))
        val copy = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12, d), 0, 0, 0) }
        copy.addView(text(c, BRAND, 12f, TEAL, true))
        copy.addView(text(c, name, 20f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3, d) })
        copy.addView(text(c, subtitle, 10.5f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4, d) })
        row.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
        outer.addView(row)
        outer.addView(text(c, TAGLINE, 9.5f, BLUE, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8, d) })
        outer.addView(text(c, PILLARS, 9.5f, GOLD, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3, d) })
        return outer
    }

    fun pageHeader(c: Context, eyebrow: String, name: String, subtitle: String): LinearLayout {
        val d = density(c)
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(17, d), dp(15, d), dp(17, d), dp(15, d))
            background = card(PALE_BLUE, 18, d)
        }
        box.addView(text(c, eyebrow.uppercase(), 9.5f, TEAL, true))
        box.addView(text(c, name, 22f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        box.addView(text(c, subtitle, 11f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        return box
    }

    fun section(c: Context, s: String) = text(c, s.uppercase(), 10.5f, MUTED, true).apply {
        setPadding(0, dp(15, density(c)), 0, dp(6, density(c)))
        letterSpacing = .075f
    }

    fun input(c: Context, hint: String) = EditText(c).apply {
        this.hint = hint
        textSize = 15f
        setSingleLine(true)
        setTextColor(NAVY)
        setHintTextColor(MUTED)
        minHeight = dp(52, density(c))
        setPadding(dp(14, density(c)), dp(8, density(c)), dp(14, density(c)), dp(8, density(c)))
        background = card(WHITE, 14, density(c))
    }

    fun add(root: LinearLayout, v: View, top: Int = 7) {
        root.addView(v, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top, density(root.context)) })
    }

    fun statCard(c: Context, label: String, value: String, accent: Int): LinearLayout {
        val d = density(c)
        return LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(14, d), dp(12, d), dp(10, d), dp(10, d))
            background = card(WHITE, 16, d)
            elevation = dp(1, d).toFloat()
            addView(text(c, value, 20f, accent, true))
            addView(text(c, label.uppercase(), 9f, MUTED, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4, d) })
        }
    }

    fun moduleCard(c: Context, number: String, title: String, description: String, accent: Int, click: () -> Unit): LinearLayout {
        val d = density(c)
        return LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15, d), dp(14, d), dp(14, d), dp(13, d))
            background = card(WHITE, 18, d)
            elevation = dp(2, d).toFloat()
            setOnClickListener { click() }
            addView(TextView(c).apply {
                text = number
                textSize = 10f
                setTextColor(accent)
                typeface = Typeface.create("sans-serif-medium", Typeface.BOLD)
            })
            addView(text(c, title, 15f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(7, d) })
            addView(text(c, description, 10f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4, d) })
        }
    }
}
