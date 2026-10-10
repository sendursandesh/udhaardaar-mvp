package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/**
 * Account-scoped local record store.
 *
 * Ownerless legacy records are deliberately not visible to authenticated accounts.
 * A later migration may recover them only through an explicit, authenticated flow.
 */
object ArthSaathiDataStore {
    private const val PREFS = "arthsaathi_master_data"
    private const val KEY_RECORDS = "records"
    private const val OWNER = "ownerUserId"
    private lateinit var appContext: Context

    fun initialize(context: Context) { appContext = context.applicationContext }
    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    internal fun ownerCanRead(recordOwner: String?, activeOwner: String?): Boolean =
        !recordOwner.isNullOrBlank() && !activeOwner.isNullOrBlank() && recordOwner == activeOwner

    private fun activeOwner(): String? = ArthSaathiSession.mobile(appContext)?.takeIf { it.isNotBlank() }

    @Synchronized private fun allStored(): JSONArray = JSONArray(prefs().getString(KEY_RECORDS, "[]"))

    @Synchronized fun records(): JSONArray {
        val owner = activeOwner() ?: return JSONArray()
        val stored = allStored()
        val visible = JSONArray()
        for (i in 0 until stored.length()) {
            val record = stored.optJSONObject(i) ?: continue
            if (ownerCanRead(record.optString(OWNER, null), owner)) visible.put(JSONObject(record.toString()))
        }
        return visible
    }

    @Synchronized fun append(record: JSONObject) {
        val owner = activeOwner()
            ?: throw IllegalStateException("A signed-in account is required before saving records.")
        val item = JSONObject(record.toString())
        val declaredOwner = item.optString(OWNER)
        require(declaredOwner.isBlank() || declaredOwner == owner) {
            "Cannot create a record for a different account."
        }
        item.put(OWNER, owner)
        val all = allStored()
        all.put(item)
        prefs().edit().putString(KEY_RECORDS, all.toString()).apply()
    }

    /**
     * Replace only records belonging to the current account, preserving every other
     * account's records. Incoming records cannot change owner identity.
     */
    @Synchronized fun replace(records: JSONArray) {
        val owner = activeOwner()
            ?: throw IllegalStateException("A signed-in account is required before updating records.")
        val stored = allStored()
        val merged = JSONArray()
        for (i in 0 until stored.length()) {
            val item = stored.optJSONObject(i) ?: continue
            if (!ownerCanRead(item.optString(OWNER, null), owner)) merged.put(item)
        }
        for (i in 0 until records.length()) {
            val item = records.optJSONObject(i) ?: continue
            val declaredOwner = item.optString(OWNER)
            require(declaredOwner.isBlank() || declaredOwner == owner) {
                "Cannot update a record owned by a different account."
            }
            val copy = JSONObject(item.toString()).apply { put(OWNER, owner) }
            merged.put(copy)
        }
        prefs().edit().putString(KEY_RECORDS, merged.toString()).apply()
    }
}
