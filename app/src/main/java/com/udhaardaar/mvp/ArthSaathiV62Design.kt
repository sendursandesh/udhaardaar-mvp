package com.udhaardaar.mvp

import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.view.View
import android.widget.*

/**
 * Frozen ArthSaathi V6.2 visual language.
 * Do not replace the logo with a generic banking/finance icon.
 * The logo resource is the approved compass + A gateway + journey-road + destination-star mark.
 */
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

    const val BRAND = "ArthSaathi"
    const val TAGLINE = "Navigate Your Financial Journey"
    const val PILLARS = "Plan • Protect • Grow • Nominate"
    const val LOGO_RESOURCE = "@drawable/udhaardaar_logo"

    fun dp(v: Int, d: Float) = (v * d).toInt()

    fun text(c: android.content.Context, s: String, size: Float = 14f, color: Int = NAVY, bold: Boolean = false) =
        TextView(c).apply {
            text = s
            textSize = size
            setTextColor(color)
            includeFontPadding = false
            typeface = Typeface.create("sans-serif", if (bold) Typeface.BOLD else Typeface.NORMAL)
        }

    fun card(fill: Int = WHITE, radius: Int = 16, d: Float) =
        GradientDrawable().apply {
            setColor(fill)
            setStroke(dp(1, d), BORDER)
            cornerRadius = dp(radius, d).toFloat()
        }

    fun button(c: android.content.Context, s: String, color: Int = BLUE, click: () -> Unit) =
        Button(c).apply {
            text = s
            textSize = 13f
            setTextColor(WHITE)
            isAllCaps = false
            minHeight = dp(48, resources.displayMetrics.density)
            background = card(color, 14, resources.displayMetrics.density)
            setOnClickListener { click() }
        }

    fun title(c: android.content.Context, name: String, subtitle: String): LinearLayout {
        val d = resources(c)
        val h = LinearLayout(c).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            setPadding(dp(8, d), dp(6, d), dp(8, d), dp(6, d))
        }
        h.addView(
            ImageView(c).apply {
                setImageResource(com.udhaardaar.mvp.R.drawable.udhaardaar_logo)
                scaleType = ImageView.ScaleType.CENTER_INSIDE
                contentDescription = "ArthSaathi approved journey logo"
            },
            LinearLayout.LayoutParams(dp(52, d), dp(52, d))
        )
        val b = LinearLayout(c).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(dp(10, d), 0, 0, 0)
        }
        b.addView(text(c, name, 20f, NAVY, true))
        b.addView(text(c, subtitle, 9.5f, TEAL, true), LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(3, d) })
        h.addView(b, LinearLayout.LayoutParams(0, -2, 1f))
        return h
    }

    fun resources(c: android.content.Context) = c.resources.displayMetrics.density

    fun section(c: android.content.Context, s: String) =
        text(c, s, 11f, MUTED, true).apply { setPadding(0, dp(14, resources(c)), 0, dp(4, resources(c))) }

    fun input(c: android.content.Context, hint: String) =
        EditText(c).apply {
            this.hint = hint
            textSize = 15f
            setSingleLine(true)
            setTextColor(NAVY)
            setHintTextColor(MUTED)
            minHeight = dp(50, resources.displayMetrics.density)
            setPadding(dp(12, resources.displayMetrics.density), dp(8, resources.displayMetrics.density), dp(12, resources.displayMetrics.density), dp(8, resources.displayMetrics.density))
            background = card(WHITE, 13, resources.displayMetrics.density)
        }

    fun input(c: android.content.Context) = input(c, "")

    fun add(root: LinearLayout, v: View, top: Int = 7) {
        root.addView(v, LinearLayout.LayoutParams(-1, -2).apply { topMargin = dp(top, root.resources.displayMetrics.density) })
    }
}
