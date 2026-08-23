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
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) applyMobileLimits(view.getChildAt(i))
        }
    }

    /**
     * Paytm-style form navigation: whenever a field receives focus, the containing
     * ScrollView automatically moves it above the keyboard. IME insets are also
     * added as bottom padding so the keyboard cannot cover the active field.
     */
    private fun installKeyboardAwareScrolling(root: View) {
        if (root is ScrollView) {
            ViewCompat.setOnApplyWindowInsetsListener(root) { v, insets ->
                val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
                val systemBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
                val bottom = maxOf(imeBottom, systemBottom)
                v.setPadding(v.paddingLeft, v.paddingTop, v.paddingRight, bottom + dp(24))
                insets
            }
            ViewCompat.requestApplyInsets(root)
            attachFocusHandlers(root, root)
        }
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) installKeyboardAwareScrolling(root.getChildAt(i))
        }
    }

    private fun attachFocusHandlers(scroll: ScrollView, view: View) {
        if (view is EditText) {
            view.setOnFocusChangeListener { focusedView, hasFocus ->
                if (hasFocus) {
                    scroll.postDelayed({
                        val rect = Rect(0, 0, focusedView.width, focusedView.height)
                        try {
                            scroll.offsetDescendantRectToMyCoords(focusedView, rect)
                            val visibleTop = scroll.paddingTop + dp(12)
                            val visibleBottom = scroll.height - scroll.paddingBottom - dp(12)
                            when {
                                rect.bottom > visibleBottom -> scroll.smoothScrollBy(0, rect.bottom - visibleBottom)
                                rect.top < visibleTop -> scroll.smoothScrollBy(0, rect.top - visibleTop)
                            }
                        } catch (_: Exception) {
                            focusedView.requestRectangleOnScreen(rect, true)
                        }
                    }, 180)
                }
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) attachFocusHandlers(scroll, view.getChildAt(i))
        }
    }

    private fun dp(value: Int): Int = (value * resources.displayMetrics.density).toInt()
}
