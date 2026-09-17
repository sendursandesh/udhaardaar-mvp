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

/** ArthSaathi V6.2 reference-driven visual system. */
object ArthSaathiV62Design {
    val NAVY = Color.rgb(14, 38, 70)
    val NAVY_2 = Color.rgb(20, 57, 101)
    val TEAL = Color.rgb(12, 155, 145)
    val BLUE = Color.rgb(44, 103, 218)
    val GOLD = Color.rgb(242, 182, 50)
    val GOLD_DEEP = Color.rgb(216, 148, 8)
    val GOLD_BRIGHT = Color.rgb(255, 226, 138)
    val GOLD_DARK = Color.rgb(169, 109, 0)
    val GREEN = Color.rgb(24, 139, 94)
    val RED = Color.rgb(190, 68, 76)
    val BG = Color.rgb(255, 248, 231)
    val WHITE = Color.WHITE
    val MUTED = Color.rgb(92, 108, 124)
    val BORDER = Color.rgb(224, 213, 185)
    val PALE_TEAL = Color.rgb(235, 248, 246)
    val PALE_BLUE = Color.rgb(246, 241, 221)
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
        letterSpacing = if (size <= 11f) .02f else 0f
    }

    fun brandWordmark(c: Context, size: Float = 25f): TextView = text(c, BRAND, size, NAVY, true).apply {
        val s = SpannableString(BRAND)
        s.setSpan(ForegroundColorSpan(NAVY), 0, 4, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        s.setSpan(ForegroundColorSpan(GOLD_DEEP), 4, BRAND.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        text = s
        gravity = Gravity.CENTER
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

    fun button(c: Context, s: String, color: Int = GOLD_DEEP, click: () -> Unit) = Button(c).apply {
        text = s
        textSize = 14f
        setTextColor(if (color == GOLD || color == GOLD_DEEP) NAVY else WHITE)
        typeface = Typeface.create("sans-serif", Typeface.BOLD)
        isAllCaps = false
        minHeight = dp(50, density(c))
        minimumHeight = dp(50, density(c))
        stateListAnimator = null
        elevation = dp(2, density(c)).toFloat()
        background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT, intArrayOf(color, if (color == GOLD_DEEP) GOLD else color)).apply {
            cornerRadius = dp(15, density(c)).toFloat()
            setStroke(dp(1, density(c)), if (color == GOLD || color == GOLD_DEEP) GOLD_DARK else BORDER)
        }
        backgroundTintList = null
        setPadding(dp(16, density(c)), 0, dp(16, density(c)), 0)
        setOnClickListener { click() }
    }

    /** Reference-style compact brand header for inner screens. */
    fun title(c: Context, name: String, subtitle: String): LinearLayout {
        val d = density(c)
        val outer = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(dp(10, d), dp(7, d), dp(10, d), dp(9, d))
            background = card(WHITE, 20, d)
            elevation = dp(2, d).toFloat()
        }
        outer.addView(ImageView(c).apply {
            setImageResource(com.udhaardaar.mvp.R.drawable.arthsaathi_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "ArthSaathi logo"
        }, LinearLayout.LayoutParams(dp(66, d), dp(66, d)))
        outer.addView(brandWordmark(c, 22f), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(1, d) })
        outer.addView(text(c, subtitle, 10f, NAVY, false), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3, d) })
        outer.addView(text(c, PILLARS, 9.5f, GOLD_DARK, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        return outer
    }

    fun pageHeader(c: Context, eyebrow: String, name: String, subtitle: String): LinearLayout {
        val d = density(c)
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(15, d), dp(12, d), dp(15, d), dp(12, d))
            background = card(PALE_BLUE, 18, d)
        }
        box.addView(text(c, eyebrow.uppercase(), 9.5f, TEAL, true))
        box.addView(text(c, name, 22f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        box.addView(text(c, subtitle, 11f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
        return box
    }

    fun section(c: Context, s: String) = text(c, s.uppercase(), 10.5f, NAVY, true).apply {
        setPadding(0, dp(12, density(c)), 0, dp(5, density(c)))
        letterSpacing = .075f
    }

    fun input(c: Context, hint: String) = EditText(c).apply {
        this.hint = hint
        textSize = 15f
        setSingleLine(true)
        setTextColor(NAVY)
        setHintTextColor(MUTED)
        minHeight = dp(50, density(c))
        setPadding(dp(14, density(c)), dp(8, density(c)), dp(14, density(c)), dp(8, density(c)))
        background = card(WHITE, 13, density(c))
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
            setPadding(dp(12, d), dp(10, d), dp(10, d), dp(9, d))
            background = card(WHITE, 14, d)
            elevation = dp(1, d).toFloat()
            addView(text(c, value, 18f, accent, true))
            addView(text(c, label, 8.5f, MUTED, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3, d) })
        }
    }

    fun featureCard(c: Context, icon: String, title: String, click: () -> Unit): LinearLayout {
        val d = density(c)
        return LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(6, d), dp(9, d), dp(6, d), dp(8, d))
            background = card(WHITE, 15, d)
            elevation = dp(1, d).toFloat()
            setOnClickListener { click() }
            addView(text(c, icon, 23f, BLUE, false), LinearLayout.LayoutParams(-1, dp(29, d)))
            addView(text(c, title, 10f, NAVY, true).apply { gravity = Gravity.CENTER }, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(4, d) })
        }
    }

    fun moduleCard(c: Context, number: String, title: String, description: String, accent: Int, click: () -> Unit): LinearLayout {
        val d = density(c)
        return LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(13, d), dp(11, d), dp(12, d), dp(11, d))
            background = card(WHITE, 16, d)
            elevation = dp(1, d).toFloat()
            setOnClickListener { click() }
            addView(text(c, number, 9f, accent, true))
            addView(text(c, title, 14f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(5, d) })
            addView(text(c, description, 9.5f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3, d) })
        }
    }

    fun bottomNav(c: Context, selected: String, actions: Map<String, () -> Unit>): LinearLayout {
        val d = density(c)
        val nav = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER
            setPadding(dp(4, d), dp(5, d), dp(4, d), dp(6, d))
            background = card(WHITE, 18, d)
            elevation = dp(7, d).toFloat()
        }
        val labels = listOf("⌂\nHome", "▣\nCredit", "↔\nRepay", "▣\nVault", "☰\nMore")
        labels.forEach { raw ->
            val key = raw.substringAfterLast("\n")
            val tv = text(c, raw, 9f, if (key == selected) GOLD_DARK else NAVY, key == selected).apply { gravity = Gravity.CENTER; setPadding(0, dp(3,d), 0, 0) }
            tv.setOnClickListener { actions[key]?.invoke() }
            nav.addView(tv, LinearLayout.LayoutParams(0, dp(52,d), 1f))
        }
        return nav
    }
}
