package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

object ArthSaathiDataStore {
    private const val PREFS = "arthsaathi_master_data"
    private const val KEY_RECORDS = "records"
    private lateinit var appContext: Context

    fun initialize(context: Context) { appContext = context.applicationContext }
    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    @Synchronized fun records(): JSONArray = JSONArray(prefs().getString(KEY_RECORDS, "[]"))

    @Synchronized fun append(record: JSONObject) {
        val all = records()
        all.put(record)
        prefs().edit().putString(KEY_RECORDS, all.toString()).apply()
    }

    @Synchronized fun replace(records: JSONArray) {
        prefs().edit().putString(KEY_RECORDS, records.toString()).apply()
    }
}
