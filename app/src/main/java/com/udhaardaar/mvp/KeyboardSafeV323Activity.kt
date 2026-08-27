package com.udhaardaar.mvp

import android.graphics.Rect
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import androidx.core.view.WindowCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.NestedScrollView

/** Keyboard-safe wrapper for V3.3. Supports both ScrollView and NestedScrollView. */
class KeyboardSafeV323Activity : V323Activity() {
    private var scroll: ViewGroup? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onCreate(savedInstanceState)
        scroll = findScrollable(window.decorView)
        scroll?.let { attachFocusHandlers(it) }
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { _, insets ->
            val imeBottom = insets.getInsets(WindowInsetsCompat.Type.ime()).bottom
            val systemBottom = insets.getInsets(WindowInsetsCompat.Type.systemBars()).bottom
            scroll?.let { sv ->
                sv.setPadding(sv.paddingLeft, sv.paddingTop, sv.paddingRight, maxOf(imeBottom, systemBottom) + dp(32))
                val focused = sv.findFocus()
                if (focused != null && imeBottom > 0) {
                    focused.postDelayed({ ensureVisible(sv, focused, imeBottom) }, 80)
                }
            }
            insets
        }
        ViewCompat.requestApplyInsets(window.decorView)
    }

    private fun dp(v: Int): Int = (v * resources.displayMetrics.density).toInt()

    private fun findScrollable(root: View): ViewGroup? {
        if (root is NestedScrollView) return root
        if (root is android.widget.ScrollView) return root
        if (root is ViewGroup) {
            for (i in 0 until root.childCount) {
                findScrollable(root.getChildAt(i))?.let { return it }
            }
        }
        return null
    }

    private fun attachFocusHandlers(view: View) {
        if (view is EditText) {
            view.setOnFocusChangeListener { focusedView, hasFocus ->
                if (hasFocus) focusedView.postDelayed({
                    val sv = scroll ?: return@postDelayed
                    val ime = WindowInsetsCompat.toWindowInsetsCompat(window.decorView.rootWindowInsets ?: return@postDelayed)
                        .getInsets(WindowInsetsCompat.Type.ime()).bottom
                    ensureVisible(sv, focusedView, ime)
                }, 120)
            }
        }
        if (view is ViewGroup) for (i in 0 until view.childCount) attachFocusHandlers(view.getChildAt(i))
    }

    private fun ensureVisible(sv: ViewGroup, focused: View, imeBottom: Int) {
        val rect = Rect()
        focused.getGlobalVisibleRect(rect)
        val keyboard = if (imeBottom > 0) imeBottom else dp(280)
        val visibleBottom = resources.displayMetrics.heightPixels - keyboard - dp(24)
        val delta = when {
            rect.bottom > visibleBottom -> rect.bottom - visibleBottom + dp(36)
            rect.top < dp(72) -> rect.top - dp(72)
            else -> 0
        }
        if (delta != 0) when (sv) {
            is NestedScrollView -> sv.smoothScrollBy(0, delta)
            is android.widget.ScrollView -> sv.smoothScrollBy(0, delta)
        }
    }
}
