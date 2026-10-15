package com.udhaardaar.mvp

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.InputFilter
import android.text.InputType
import android.view.Gravity
import android.view.View
import android.widget.*

/** Canonical ArthSaathi V6.2 visual system used by every modern module screen. */
object ArthSaathiV62Design {
    val NAVY = Color.rgb(14, 38, 70)
    val TEAL = Color.rgb(12, 155, 145)
    val BLUE = Color.rgb(44, 103, 218)
    /** Approved radiant/shining-gold treatment; geometry/layout remains unchanged. */
    val GOLD = Color.rgb(242, 182, 50)
    val GOLD_DEEP = Color.rgb(216, 148, 8)
    val GOLD_BRIGHT = Color.rgb(255, 226, 138)
    val GREEN = Color.rgb(24, 139, 94)
    val RED = Color.rgb(190, 68, 76)
    val BG = Color.rgb(246, 249, 252)
    val WHITE = Color.WHITE
    val MUTED = Color.rgb(92, 108, 124)
    val BORDER = Color.rgb(220, 228, 236)
    val PALE_TEAL = Color.rgb(235, 248, 246)
    val PALE_BLUE = Color.rgb(238, 244, 255)
    val PALE_GOLD = Color.rgb(255, 248, 226)
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

    fun hero(fill: Int = NAVY, radius: Int = 22, d: Float = 1f) = GradientDrawable().apply {
        setColor(fill)
        cornerRadius = dp(radius, d).toFloat()
    }

    fun button(c: Context, s: String, color: Int = BLUE, click: () -> Unit) = Button(c).apply {
        text = s
        textSize = 13f
        setTextColor(WHITE)
        typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
        isAllCaps = false
        minHeight = dp(50, density(c))
        minimumHeight = dp(50, density(c))
        stateListAnimator = null
        elevation = dp(2, density(c)).toFloat()
        background = card(color, 15, density(c))
        backgroundTintList = ColorStateList.valueOf(color)
        setPadding(dp(16, density(c)), 0, dp(16, density(c)), 0)
        setOnClickListener { click() }
    }

    /** Branded module header: logo, module name, product tagline and consistent hierarchy. */
    fun title(c: Context, name: String, subtitle: String): LinearLayout {
        val d = density(c)
        val outer = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(14, d), dp(12, d), dp(14, d), dp(12, d))
            background = card(WHITE, 20, d)
            elevation = dp(2, d).toFloat()
        }
        val row = LinearLayout(c).apply { orientation = LinearLayout.HORIZONTAL; gravity = Gravity.CENTER_VERTICAL }
        row.addView(ImageView(c).apply {
            setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "ArthSaathi logo"
        }, LinearLayout.LayoutParams(dp(58, d), dp(58, d)))
        val copy = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; setPadding(dp(12, d), 0, 0, 0) }
        copy.addView(text(c, name, 20f, NAVY, true))
        copy.addView(text(c, subtitle, 10.5f, TEAL, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        row.addView(copy, LinearLayout.LayoutParams(0, -2, 1f))
        outer.addView(row)
        outer.addView(text(c, PILLARS, 9.5f, GOLD, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(8, d) })
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

    /** Common field styling plus deterministic length/type guards for critical identifiers. */
    fun input(c: Context, hint: String) = EditText(c).apply {
        this.hint = hint
        textSize = 15f
        setSingleLine(true)
        setTextColor(NAVY)
        setHintTextColor(MUTED)
        minHeight = dp(52, density(c))
        setPadding(dp(14, density(c)), dp(8, density(c)), dp(14, density(c)), dp(8, density(c)))
        background = card(WHITE, 14, density(c))
        val h = hint.lowercase()
        when {
            h.contains("mobile") -> { inputType = InputType.TYPE_CLASS_PHONE; filters = arrayOf(InputFilter.LengthFilter(10)) }
            h.contains("aadhaar") -> { inputType = InputType.TYPE_CLASS_NUMBER; filters = arrayOf(InputFilter.LengthFilter(12)) }
            h.contains("gstin") -> { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS; filters = arrayOf(InputFilter.LengthFilter(15)) }
            h.contains("pan") -> { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS; filters = arrayOf(InputFilter.LengthFilter(10)) }
            h.contains("pin code") || h.contains("pincode") || h == "pin" -> { inputType = InputType.TYPE_CLASS_NUMBER; filters = arrayOf(InputFilter.LengthFilter(6)) }
            h.contains("otp") -> { inputType = InputType.TYPE_CLASS_NUMBER; filters = arrayOf(InputFilter.LengthFilter(6)) }
            h.contains("email") -> { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS; filters = arrayOf(InputFilter.LengthFilter(120)) }
            else -> filters = arrayOf(InputFilter.LengthFilter(160))
        }
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
