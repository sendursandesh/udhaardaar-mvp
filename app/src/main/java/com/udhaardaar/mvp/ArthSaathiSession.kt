package com.udhaardaar.mvp

import android.content.Context
import java.util.UUID

/**
 * Session identity for the consolidated app.
 * Provider-verified sessions are keyed by the verified mobile number. Offline
 * workspaces use a persistent device-local owner ID and never imply OTP verification.
 */
object ArthSaathiSession {
    private const val PREFS = "arthsaathi_session"
    private const val KEY_LOGGED_IN = "logged_in"
    private const val KEY_MOBILE = "mobile"
    private const val KEY_OWNER_ID = "owner_user_id"
    private const val LOCAL_PREFS = "arthsaathi_local_identity"
    private const val KEY_LOCAL_OWNER_ID = "local_owner_id"
    private const val LOCAL_PREFIX = "local:"

    fun isLoggedIn(context: Context): Boolean =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean(KEY_LOGGED_IN, false)

    fun login(context: Context, mobile: String) {
        require(ArthSaathiCoreEngine.validateMobile(mobile)) { "A verified 10-digit mobile number is required." }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_MOBILE, mobile)
            .putString(KEY_OWNER_ID, mobile)
            .apply()
    }

    /** Starts a device-only workspace; it is not a verified identity or consent mechanism. */
    fun loginOffline(context: Context) {
        val localPrefs = context.getSharedPreferences(LOCAL_PREFS, Context.MODE_PRIVATE)
        var owner = localPrefs.getString(KEY_LOCAL_OWNER_ID, null)
        if (owner.isNullOrBlank()) {
            owner = LOCAL_PREFIX + UUID.randomUUID().toString()
            check(localPrefs.edit().putString(KEY_LOCAL_OWNER_ID, owner).commit()) {
                "Could not initialise the device-only workspace."
            }
        }
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
            .putBoolean(KEY_LOGGED_IN, true)
            .putString(KEY_MOBILE, "")
            .putString(KEY_OWNER_ID, owner)
            .putBoolean("offline_workspace", true)
            .apply()
    }

    fun isOfflineWorkspace(context: Context): Boolean =
        isLoggedIn(context) &&
            context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getBoolean("offline_workspace", false)

    fun ownerId(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (!prefs.getBoolean(KEY_LOGGED_IN, false)) return null
        return prefs.getString(KEY_OWNER_ID, null)
            ?: prefs.getString(KEY_MOBILE, null)?.takeIf { it.isNotBlank() }
    }

    fun logout(context: Context) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit().clear().apply()
    }

    fun mobile(context: Context): String? =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_MOBILE, null)
}
