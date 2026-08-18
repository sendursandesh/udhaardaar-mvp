package com.udhaardaar.mvp

import android.app.Activity
import android.app.Application
import android.os.Bundle
import android.text.InputFilter
import android.text.method.DigitsKeyListener
import android.view.View
import android.view.ViewGroup
import android.widget.EditText

class UdhaardaarApp : Application() {
    override fun onCreate() {
        super.onCreate()
        registerActivityLifecycleCallbacks(object : ActivityLifecycleCallbacks {
            override fun onActivityResumed(activity: Activity) {
                activity.window.decorView.post { applyMobileLimits(activity.window.decorView) }
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
}
