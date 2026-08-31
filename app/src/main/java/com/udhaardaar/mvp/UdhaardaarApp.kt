package com.udhaardaar.mvp

import android.app.Activity
import android.app.Application
import android.graphics.Rect
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ScrollView
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
