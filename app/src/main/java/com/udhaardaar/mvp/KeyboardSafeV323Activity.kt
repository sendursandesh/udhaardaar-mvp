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

/** Keyboard-safe wrapper for the V3.3 UI. Keeps the focused field above the IME. */
class KeyboardSafeV323Activity : V323Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        WindowCompat.setDecorFitsSystemWindows(window, true)
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE or WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)
        super.onCreate(savedInstanceState)
        ViewCompat.setOnApplyWindowInsetsListener(window.decorView) { v, insets ->
            val ime = insets.getInsets(WindowInsetsCompat.Type.ime())
            val sv = findScrollView(v)
            if (sv != null) {
                sv.setPadding(sv.paddingLeft, sv.paddingTop, sv.paddingRight, ime.bottom + dp(24))
                val focused = sv.findFocus()
                if (focused is EditText && ime.bottom > 0) {
                    focused.postDelayed({ ensureVisible(sv, focused, ime.bottom) }, 60)
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
            for (i in 0 until root.childCount) findScrollView(root.getChildAt(i))?.let { return it }
        }
        return null
    }

    private fun ensureVisible(scroll: ScrollView, focused: View, imeBottom: Int) {
        val rect = Rect()
        focused.getGlobalVisibleRect(rect)
        val visibleBottom = resources.displayMetrics.heightPixels - imeBottom - dp(16)
        if (rect.bottom > visibleBottom) {
            scroll.smoothScrollBy(0, rect.bottom - visibleBottom + dp(24))
        } else if (rect.top < dp(72)) {
            scroll.smoothScrollBy(0, rect.top - dp(72))
        }
    }
}
