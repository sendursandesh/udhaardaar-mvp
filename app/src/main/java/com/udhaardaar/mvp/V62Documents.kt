package com.udhaardaar.mvp

import android.content.Context
import android.net.Uri
import org.json.JSONObject

/** Central document-retention boundary used by all V6.2 modules. */
object V62Documents {
    fun retain(context: Context, uri: Uri, category: String, extractedText: String = ""): JSONObject {
        val id = V62Store.id("DOC")
        val value = JSONObject().apply {
            put("id", id)
            put("uri", uri.toString())
            put("category", category)
            put("extractedText", extractedText)
            put("createdAt", System.currentTimeMillis())
            put("verified", false)
        }
        V62Store.replace(context, V62Store.DOCUMENTS, value)
        return value
    }
}
