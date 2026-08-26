package com.udhaardaar.mvp

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.ScrollView
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

/** Keyboard-safe wrapper for V3.3. Keeps every focused field above the IME. */
class KeyboardSafeV323Activity : V323Activity() {
    private var scroll: ScrollView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onCreate(savedInstanceState)
        scroll = findScrollView(window.decorView)
        scroll?.isFillViewport = true
        attachFocusHandlers(scroll)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            scroll?.let { sv ->
                sv.setPadding(sv.paddingLeft, sv.paddingTop, sv.paddingRight, ime.bottom + dp(32))
                val focused = sv.findFocus()
                if (focused != null && ime.bottom > 0) {
                    focused.postDelayed({ ensureVisible(sv, focused, ime.bottom) }, 80)
                }
            }
            insets
        }
        ViewCompat.requestApplyInsets(window.decorView)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun findScrollView(root: View): ScrollView? {
        if (root is ScrollView) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                val found = findScrollView(root.getChildAt(i))
                if (found != null) return found
            }
        }
        return null
    }

    private fun attachFocusHandlers(view: View?) {
        if (view == null) return
        if (view is EditText) {
            view.setOnFocusChangeListener { focusedView, hasFocus ->
                if (hasFocus) {
                    focusedView.postDelayed({
                        val sv = scroll ?: return@postDelayed
                        val ime = WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets ?: return@postDelayed)
                            .getInsets(WindowInsetsCompat.Type.ime()).bottom
                        ensureVisible(sv, focusedView, ime)
                    }, 120)
                }
            }
        }
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) attachFocusHandlers(view.getChildAt(i))
        }
    }

    private fun ensureVisible(sv: ScrollView, focused: View, imeBottom: Int) {
        val rect = Rect()
        focused.getGlobalVisibleRect(rect)
        val keyboard = if (imeBottom > 0) imeBottom else dp(280)
        val visibleBottom = resources.displayMetrics.heightPixels - keyboard - dp(24)
        if (rect.bottom > visibleBottom) {
            sv.smoothScrollBy(0, rect.bottom - visibleBottom + dp(36))
        } else if (rect.top < dp(72)) {
            sv.smoothScrollBy(0, rect.top - dp(72))
        }
    }
}
