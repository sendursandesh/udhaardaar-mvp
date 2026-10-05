package com.udhaardaar.mvp

import android.content.Context

object ArthSaathiSession {
    private const val PREFS = "arthsaathi_session"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_MOBILE = "mobile"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOGGED_IN, false)

    fun login(context: Context, mobile: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LOGGED_IN, true).putString(KEY_MOBILE, mobile).apply()
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun mobile(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MOBILE, null)
}
