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
        // Create the privacy/audit/agreement foundation without destroying existing user data.
        runCatching { V3Foundation.ensure(V32DatabaseHelper(this).writableDatabase) }
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
                val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val systemBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, maxOf(imeBottom, systemBottom) + dp(24))
                insets
            }
            ViewCompat.requestApplyInsets(root)
            attachFocusHandlers(root, root)
        }
        if (root is ViewGroup) for (i in 0 until root.childCount) installKeyboardAwareScrolling(root.getChildAt(i))
    }

    private fun attachFocusHandlers(scroll: ScrollView, view: View) {
        if (view is EditText) {
            view.setOnFocusChangeListener { focusedView, hasFocus ->
                if (hasFocus) scroll.postDelayed({
                    val rect = Rect(0, 0, focusedView.width, focusedView.height)
                    try {
                        scroll.offsetDescendantRectToMyCoords(focusedView, rect)
                        val top = scroll.paddingTop + dp(12)
                        val bottom = scroll.height - scroll.paddingBottom - dp(12)
                        when { rect.bottom > bottom -> scroll.smoothScrollBy(0, rect.bottom - bottom); rect.top < top -> scroll.smoothScrollBy(0, rect.top - top) }
                    } catch (_: Exception) { focusedView.requestRectangleOnScreen(rect, true) }
                }, 180)
            }
        }
        if (view is ViewGroup) for (i in 0 until view.childCount) attachFocusHandlers(scroll, view.getChildAt(i))
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
