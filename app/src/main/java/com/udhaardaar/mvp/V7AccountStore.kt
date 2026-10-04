package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/**
 * Encrypted V7 account/session boundary.
 * Authentication/session identifiers are kept in the V7 Keystore-backed store,
 * not plaintext SharedPreferences.
 */
object V7AccountStore {
    private const val ACCOUNTS = "v7_accounts"
    private const val SESSION = "v7_session"
    private const val SESSION_ID = "CURRENT"

    private fun store(c: Context) = V7LocalStore(c.applicationContext)

    fun account(c: Context, mobile: String): JSONObject? =
        store(c).find(ACCOUNTS, mobile.trim())

    fun create(c: Context, name: String, mobile: String) {
        store(c).add(ACCOUNTS, JSONObject().apply {
            put("id", mobile.trim())
            put("mobile", mobile.trim())
            put("name", name.trim())
            put("createdAt", V7Core.now())
        })
    }

    fun currentMobile(c: Context): String =
        store(c).find(SESSION, SESSION_ID)?.optString("mobile").orEmpty()

    fun currentName(c: Context): String =
        account(c, currentMobile(c))?.optString("name").orEmpty()

    fun isLoggedIn(c: Context): Boolean = currentMobile(c).isNotBlank()

    fun login(c: Context, mobile: String) {
        store(c).replace(SESSION, JSONObject().apply {
            put("id", SESSION_ID)
            put("mobile", mobile.trim())
            put("loggedInAt", V7Core.now())
        })
        c.getSharedPreferences("v7_session_guard", Context.MODE_PRIVATE).edit()
            .putLong("last_activity", android.os.SystemClock.elapsedRealtime()).apply()
    }

    fun logout(c: Context) {
        store(c).remove(SESSION, SESSION_ID)
        c.getSharedPreferences("v7_session_guard", Context.MODE_PRIVATE).edit().clear().apply()
    }
}
