package com.arthsaathi.master

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ArthSaathiDataStore {
    private const val PREFS = "arthsaathi_master_store"
    private const val RECORDS = "records"
    private lateinit var context: Context

    fun initialize(c: Context) { context = c.applicationContext }
    private fun prefs() = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized fun all(): JSONArray = JSONArray(prefs().getString(RECORDS, "[]"))
    @Synchronized fun add(value: JSONObject) {
        val a = all(); a.put(value)
        prefs().edit().putString(RECORDS, a.toString()).apply()
    }
}
