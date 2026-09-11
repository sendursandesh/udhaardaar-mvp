package com.udhaardaar.mvp

import android.app.Activity
import android.app.Application
import android.graphics.Rect
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Button
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class UdhaardaarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        migrateLegacyOwner()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                activity.window.decorView.post {
                    applyMobileLimits(activity.window.decorView)
                    installWatermark(activity)
                    normalizeV5Home(activity)
                    installKeyboardAwareScrolling(activity.window.decorView)
                }
            }
            override fun onActivityCreated(a: Activity, b: Bundle?) {}
            override fun onActivityStarted(a: Activity) {}
            override fun onActivityPaused(a: Activity) {}
            override fun onActivityStopped(a: Activity) {}
            override fun onActivitySaveInstanceState(a: Activity, b: Bundle) {}
            override fun onActivityDestroyed(a: Activity) {}
        })
    }

    private fun migrateLegacyOwner() {
        val prefs = getSharedPreferences("udhaardaar_accounts", MODE_PRIVATE)
        if (prefs.getBoolean("legacy_migrated", false)) return
        try {
            val db = V32DatabaseHelper(this)
            val u = db.userData()
            if (u != null && u.mobile.filter(Char::isDigit).length == 10) {
                val m = u.mobile.filter(Char::isDigit)
                if (!prefs.contains("name_$m")) {
                    prefs.edit().putString("name_$m", u.name).putString("address_$m", u.address).putString("email_$m", u.email).putString("photo_$m", u.photo ?: "").apply()
                }
            }
        } catch (_: Exception) {}
        prefs.edit().putBoolean("legacy_migrated", true).apply()
    }

    private fun installWatermark(activity: Activity) {
        val content = activity.findViewById<ViewGroup>(android.R.id.content) ?: return
        if (content.findViewWithTag<View>("udhaardaar_watermark") != null) return
        val logo = ImageView(activity).apply {
            tag = "udhaardaar_watermark"
            setImageResource(com.udhaardaar.mvp.R.drawable.udhaardaar_logo)
            alpha = 0.075f
            scaleType = ImageView.ScaleType.CENTER_INSIDE
            isClickable = false
            isFocusable = false
        }
        content.addView(logo, ViewGroup.LayoutParams(dp(170), dp(170)))
        logo.post {
            logo.x = ((content.width - logo.width) / 2f).coerceAtLeast(0f)
            logo.y = ((content.height - logo.height) / 2f).coerceAtLeast(0f)
        }
        content.post { logo.bringToFront() }
    }

    private fun normalizeV5Home(activity: Activity) {
        if (activity !is V5HomeActivity) return
        val root = activity.window.decorView.findViewById<View>(android.R.id.content) ?: return
        ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val base = (v.tag as? Int) ?: v.paddingTop.also { v.tag = it }
            v.setPadding(v.paddingLeft, base + bars.top, v.paddingRight, v.paddingBottom)
            insets
        }
        ViewCompat.requestApplyInsets(root)
        val scroll = root.findFirstScrollView() ?: return
        val container = scroll.getChildAt(0) as? LinearLayout ?: return
        if (container.getTag(R.id.v5_grid_normalized) == true) return

        val original = (0 until container.childCount).map { container.getChildAt(it) }
        container.removeAllViews()
        var i = 0
        while (i < original.size) {
            val child = original[i]
            if (!isHomeAction(child)) {
                styleHomeNonAction(child)
                container.addView(child)
                i++
                continue
            }
            val row = LinearLayout(activity).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                setPadding(0, dp(3), 0, dp(3))
            }
            var count = 0
            while (i < original.size && isHomeAction(original[i]) && count < 2) {
                val action = original[i]
                styleHomeAction(action)
                val lp = LinearLayout.LayoutParams(0, dp(82), 1f)
                lp.setMargins(if (count == 0) 0 else dp(5), dp(3), if (count == 1) 0 else dp(5), dp(3))
                action.layoutParams = lp
                row.addView(action)
                count++
                i++
            }
            if (count == 1) {
                val spacer = Space(activity)
                row.addView(spacer, LinearLayout.LayoutParams(0, dp(82), 1f).apply { setMargins(dp(5), dp(3), 0, dp(3)) })
            }
            container.addView(row)
        }
        container.setTag(R.id.v5_grid_normalized, true)
        container.requestLayout()
    }

    private fun isHomeAction(v: View): Boolean =
        v is Button || (v is LinearLayout && v.isClickable)

    private fun styleHomeNonAction(v: View) {
        if (v is TextView) {
            v.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            v.setTextColor(if (v.text.toString().contains("UDHAARDAAR", true)) Color.rgb(0,145,135) else Color.rgb(24,58,92))
            v.setPadding(0, dp(7), 0, dp(4))
        } else if (v is LinearLayout) {
            v.minimumHeight = dp(76)
        }
    }

    private fun styleHomeAction(v: View) {
        val bg = GradientDrawable().apply {
            setColor(Color.WHITE)
            setStroke(dp(1), Color.rgb(210,222,232))
            cornerRadius = dp(16).toFloat()
        }
        v.background = bg
        v.elevation = dp(2).toFloat()
        v.minimumHeight = dp(78)
        v.setPadding(dp(10), dp(7), dp(10), dp(7))
        v.alpha = 1f
        if (v is Button) {
            v.isAllCaps = false
            v.textSize = 13f
            v.setTextColor(Color.rgb(24,58,92))
            v.gravity = Gravity.CENTER
            v.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
            v.maxLines = 3
        } else if (v is LinearLayout) {
            v.gravity = Gravity.CENTER_VERTICAL
            for (j in 0 until v.childCount) {
                val inner = v.getChildAt(j)
                if (inner is TextView) {
                    inner.typeface = Typeface.create("sans-serif-medium", Typeface.NORMAL)
                    inner.setTextColor(Color.rgb(24,58,92))
                    inner.textSize = if (j == 0) 14f else 10f
                    inner.maxLines = if (j == 0) 2 else 2
                    inner.ellipsize = android.text.TextUtils.TruncateAt.END
                    inner.setPadding(0, dp(1), 0, dp(1))
                }
            }
        }
    }

    private fun View.findFirstScrollView(): ScrollView? {
        if (this is ScrollView) return this
        if (this is ViewGroup) for (i in 0 until childCount) {
            val found = getChildAt(i).findFirstScrollView()
            if (found != null) return found
        }
        return null
    }

    private fun applyMobileLimits(view: View) {
        if (view is EditText) {
            val hint = view.hint?.toString()?.lowercase() ?: ""
            if (hint.contains("mobile number") || hint.contains("alternate mobile")) {
                view.filters = arrayOf(InputFilter.LengthFilter(10))
                view.keyListener = DigitsKeyListener.getInstance("0123456789")
            }
        }
        if (view is ViewGroup) for (i in 0 until view.childCount) applyMobileLimits(view.getChildAt(i))
    }

    private fun installKeyboardAwareScrolling(root: View) {
        if (root is ScrollView) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val ime = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val sys = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, maxOf(ime, sys) + dp(24))
                insets
            }
            ViewCompat.requestApplyInsets(root)
            attachFocusHandlers(root, root)
        }
        if (root is ViewGroup) for (i in 0 until root.childCount) installKeyboardAwareScrolling(root.getChildAt(i))
    }

    private fun attachFocusHandlers(scroll: ScrollView, view: View) {
        if (view is EditText) view.setOnFocusChangeListener { focused, has ->
            if (has) scroll.postDelayed({
                val rect = Rect(0, 0, focused.width, focused.height)
                try {
                    scroll.offsetDescendantRectToMyCoords(focused, rect)
                    val top = scroll.paddingTop + dp(12)
                    val bottom = scroll.height - scroll.paddingBottom - dp(12)
                    if (rect.bottom > bottom) scroll.smoothScrollBy(0, rect.bottom - bottom)
                    else if (rect.top < top) scroll.smoothScrollBy(0, rect.top - top)
                } catch (_: Exception) { focused.requestRectangleOnScreen(rect, true) }
            }, 180)
        }
        if (view is ViewGroup) for (i in 0 until view.childCount) attachFocusHandlers(scroll, view.getChildAt(i))
    }
    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()
}