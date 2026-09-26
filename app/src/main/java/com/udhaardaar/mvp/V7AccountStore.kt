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
    }

    fun logout(c: Context) {
        store(c).remove(SESSION, SESSION_ID)
    }

    /**
     * One-time migration of the old account preference into encrypted V7 storage.
     * The old preference is cleared after successful migration.
     */
    fun migrateLegacyPreferences(c: Context) {
        val legacy = c.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE)
        val mobile = legacy.getString("current_mobile", "").orEmpty().trim()
        if (mobile.isNotBlank() && account(c, mobile) == null) {
            val name = legacy.getString("name_$mobile", "User").orEmpty().ifBlank { "User" }
            create(c, name, mobile)
        }
        if (mobile.isNotBlank() && !isLoggedIn(c)) login(c, mobile)
        legacy.edit().clear().commit()
    }
}
