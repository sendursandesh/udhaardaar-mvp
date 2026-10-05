package com.arthsaathi.master

import android.content.Context

object ArthSaathiSession {
    private const val PREFS = "arthsaathi_master_session"
    fun isLoggedIn(c: Context) = c.getSharedPreferences(PREFS,0).getBoolean("logged_in",false)
    fun login(c: Context,mobile:String) = c.getSharedPreferences(PREFS,0).edit()
        .putBoolean("logged_in",true).putString("mobile",mobile).apply()
    fun logout(c: Context) = c.getSharedPreferences(PREFS,0).edit().clear().apply()
    fun mobile(c: Context) = c.getSharedPreferences(PREFS,0).getString("mobile",null)
}
