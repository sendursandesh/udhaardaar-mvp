package com.udhaardaar.mvp

import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*
import android.text.InputFilter
import android.text.InputType

object ArthSaathiV7Design {
    val NAVY = Color.rgb(5, 24, 48)
    val NAVY_2 = Color.rgb(11, 43, 79)
    val NAVY_3 = Color.rgb(18, 61, 103)
    val GOLD = Color.rgb(242, 182, 50)
    val GOLD_2 = Color.rgb(255, 210, 80)
    val GOLD_PALE = Color.rgb(255, 239, 183)
    val CREAM = Color.rgb(255, 250, 239)
    val WHITE = Color.WHITE
    val MUTED = Color.rgb(101, 115, 130)
    val GREEN = Color.rgb(34, 145, 93)
    val RED = Color.rgb(194, 70, 75)
    val TEAL = Color.rgb(23, 153, 145)

    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Navigate Your Financial Journey"
    const val PILLARS = "Plan • Protect • Grow • Nominate"
    const val PROMISE = "Your Asset. Your Record. Your Right."

    private fun d(c: Context) = c.resources.displayMetrics.density
    fun dp(c: Context, v: Int) = (v * d(c)).toInt()

    fun bg(c: Context): GradientDrawable =
        GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            intArrayOf(NAVY, NAVY_2, NAVY)).apply { cornerRadius = dp(c, 24).toFloat() }

    fun card(c: Context, fill: Int = WHITE, radius: Int = 16): GradientDrawable =
        GradientDrawable().apply {
            setColor(fill)
            cornerRadius = dp(c, radius).toFloat()
            setStroke(dp(c, 1), Color.argb(45, 5, 24, 48))
        }

    fun text(c: Context, s: String, size: Float = 14f, color: Int = NAVY, bold: Boolean = false): TextView =
        TextView(c).apply {
            text = s
            textSize = size
            setTextColor(color)
            includeFontPadding = false
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        }

    fun goldButton(c: Context, label: String, action: () -> Unit): Button =
        Button(c).apply {
            text = label
            textSize = 14.5f
            isAllCaps = false
            setTextColor(NAVY)
            typeface = Typeface.create("sans-serif", Typeface.BOLD)
            minHeight = dp(c, 46)
            stateListAnimator = null
            background = GradientDrawable(GradientDrawable.Orientation.LEFT_RIGHT,
                intArrayOf(GOLD_2, GOLD)).apply {
                cornerRadius = dp(c, 14).toFloat()
            }
            setOnClickListener { action() }
        }

    fun outlineButton(c: Context, label: String, action: () -> Unit): Button =
        Button(c).apply {
            text = label
            textSize = 14f
            isAllCaps = false
            setTextColor(WHITE)
            typeface = Typeface.DEFAULT_BOLD
            minHeight = dp(c, 44)
            stateListAnimator = null
            background = GradientDrawable().apply {
                setColor(Color.TRANSPARENT)
                setStroke(dp(c, 1), GOLD)
                cornerRadius = dp(c, 14).toFloat()
            }
            setOnClickListener { action() }
        }

    fun logo(c: Context, size: Int = 78): ImageView =
        ImageView(c).apply {
            setImageResource(R.drawable.arthsaathi_logo)
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            contentDescription = "ArthSaathi logo"
            adjustViewBounds = true
            layoutParams = LinearLayout.LayoutParams(dp(c, size), dp(c, size))
        }

    fun brand(c: Context, size: Float = 25f, light: Boolean = false): TextView {
        val v = text(c, BRAND, size, if (light) WHITE else NAVY, true)
        val ss = android.text.SpannableString(BRAND)
        ss.setSpan(android.text.style.ForegroundColorSpan(if (light) WHITE else NAVY), 0, 4, 33)
        ss.setSpan(android.text.style.ForegroundColorSpan(GOLD_2), 4, BRAND.length, 33)
        v.text = ss
        return v
    }

    fun masthead(c: Context): LinearLayout {
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(c, 8), dp(c, 7), dp(c, 8), dp(c, 7))
        }
        box.addView(logo(c, 58))
        val words = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; gravity = Gravity.CENTER_VERTICAL }
        words.addView(brand(c, 25f, true))
        words.addView(text(c, TAGLINE, 10.5f, GOLD_PALE, false))
        box.addView(words, LinearLayout.LayoutParams(0, -2, 1f))
        val pillars = text(c, PILLARS, 9.5f, GOLD_2, true).apply { gravity = Gravity.CENTER }
        box.addView(pillars, LinearLayout.LayoutParams(dp(c, 142), -2))
        return box
    }

    fun navItem(c: Context, icon: String, label: String, action: () -> Unit): LinearLayout {
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(dp(c, 3), dp(c, 5), dp(c, 3), dp(c, 5))
            setOnClickListener { action() }
        }
        val i = text(c, icon, 19f, GOLD_2, true).apply { gravity = Gravity.CENTER }
        box.addView(i, LinearLayout.LayoutParams(-1, dp(c, 27)))
        box.addView(text(c, label, 9.5f, WHITE, true).apply { gravity = Gravity.CENTER },
            LinearLayout.LayoutParams(-1, -2))
        return box
    }

    fun topNav(c: Context, actions: Map<String, () -> Unit>): HorizontalScrollView {
        val row = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            background = GradientDrawable().apply {
                setColor(NAVY_2); setStroke(dp(c, 1), GOLD); cornerRadius = dp(c, 15).toFloat()
            }
            setPadding(dp(c, 4), dp(c, 2), dp(c, 4), dp(c, 2))
        }
        // Single-owner navigation: each major journey has one logical home.
        // Secondary functions belong inside their parent module, not as duplicate top-level buttons.
        val items = listOf(
            "⌂" to "Home", "▤" to "Record", "₹" to "Credit", "◉" to "Assets",
            "▥" to "Grow", "♜" to "Protect", "⚖" to "Legal", "•••" to "More"
        )
        items.forEach { (icon, label) ->
            row.addView(navItem(c, icon, label) { actions[label]?.invoke() },
                LinearLayout.LayoutParams(dp(c, 76), dp(c, 66)))
        }
        return HorizontalScrollView(c).apply {
            isHorizontalScrollBarEnabled = false
            addView(row)
        }
    }

    fun section(c: Context, title: String, subtitle: String = ""): LinearLayout {
        val box = LinearLayout(c).apply { orientation = LinearLayout.VERTICAL; setPadding(2, dp(c, 10), 2, dp(c, 5)) }
        box.addView(text(c, title.uppercase(), 12f, NAVY, true))
        if (subtitle.isNotBlank()) box.addView(text(c, subtitle, 10.5f, MUTED))
        return box
    }

    fun tile(c: Context, icon: String, title: String, subtitle: String, action: () -> Unit): LinearLayout {
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(c, 11), dp(c, 10), dp(c, 10), dp(c, 10))
            background = card(c, WHITE, 15)
            elevation = dp(c, 1).toFloat()
            setOnClickListener { action() }
        }
        box.addView(text(c, icon, 20f, GOLD, true))
        box.addView(text(c, title, 13.5f, NAVY, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, 6) })
        box.addView(text(c, subtitle, 9.5f, MUTED), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, 3) })
        return box
    }

    fun stat(c: Context, value: String, label: String, accent: Int = GOLD): LinearLayout {
        val box = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(c, 10), dp(c, 9), dp(c, 10), dp(c, 9))
            background = card(c, WHITE, 13)
        }
        box.addView(text(c, value, 17f, accent, true))
        box.addView(text(c, label, 9f, MUTED, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(c, 3) })
        return box
    }

    fun input(c: Context, hint: String): EditText = EditText(c).apply {
        this.hint = hint
        textSize = 15f
        setTextColor(NAVY)
        setHintTextColor(MUTED)
        minHeight = dp(c, 50)
        setSingleLine(true)
        background = card(c, WHITE, 12)
        setPadding(dp(c, 12), dp(c, 6), dp(c, 12), dp(c, 6))
        val h = hint.lowercase()
        when {
            "mobile" in h -> { inputType = InputType.TYPE_CLASS_PHONE; filters = arrayOf(InputFilter.LengthFilter(10)) }
            "pin" in h -> { inputType = InputType.TYPE_CLASS_NUMBER; filters = arrayOf(InputFilter.LengthFilter(6)) }
            "otp" in h -> { inputType = InputType.TYPE_CLASS_NUMBER; filters = arrayOf(InputFilter.LengthFilter(6)) }
            "aadhaar" in h -> { inputType = InputType.TYPE_CLASS_NUMBER; filters = arrayOf(InputFilter.LengthFilter(12)) }
            "pan" in h -> { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS; filters = arrayOf(InputFilter.LengthFilter(10)) }
            "gstin" in h -> { inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_CHARACTERS; filters = arrayOf(InputFilter.LengthFilter(15)) }
        }
    }
}
