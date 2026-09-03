package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

/** Optional multi-device sync boundary. Local data remains authoritative until a configured HTTPS endpoint is enabled. */
class V5SyncService(context: Context) {
    private val store = V5LocalStore(context)
    fun exportSnapshot(): JSONObject {
        val keys=listOf("profiles","credits","formal_credits","rental_leases","documents","guarantors","repayment_requests","repayment_history","assets","nominees","death_claim_cases","legal_cases","access_requests","access_grants","notifications","audit")
        return JSONObject().apply { put("schemaVersion",1);put("exportedAt",System.currentTimeMillis());keys.forEach{k->put(k,JSONArray(store.all(k)))} }
    }
    fun postSnapshot(httpsEndpoint:String, bearerToken:String?, onResult:(Boolean,String)->Unit) {
        Thread {
            try {
                require(httpsEndpoint.startsWith("https://")) { "Sync endpoint must use HTTPS" }
                val c=URL(httpsEndpoint).openConnection() as HttpURLConnection
                c.requestMethod="POST";c.doOutput=true;c.connectTimeout=15000;c.readTimeout=15000;c.setRequestProperty("Content-Type","application/json")
                if(!bearerToken.isNullOrBlank())c.setRequestProperty("Authorization","Bearer $bearerToken")
                c.outputStream.use{it.write(exportSnapshot().toString().toByteArray(Charsets.UTF_8))}
                val ok=c.responseCode in 200..299;onResult(ok,"HTTP ${c.responseCode}");c.disconnect()
            }catch(e:Exception){onResult(false,e.message?:"Sync failed")}
        }.start()
    }
}
