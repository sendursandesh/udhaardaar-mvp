package com.udhaardaar.mvp

import android.content.Context
import android.net.Uri
import org.json.JSONObject

/** Central document-retention boundary used by every V6.2 module. */
object V62Documents {
    fun retain(context: Context, uri: Uri, category: String, extractedText: String = ""): JSONObject {
        val id = V62Store.id("DOC")
        runCatching { context.contentResolver.takePersistableUriPermission(uri, android.content.Intent.FLAG_GRANT_READ_URI_PERMISSION) }
        val value = JSONObject().apply {
            put("id", id); put("ownerUserId", V62Integration.currentUserId(context)); put("uri", uri.toString()); put("category", category); put("extractedText", extractedText); put("createdAt", System.currentTimeMillis()); put("verified", false); put("userConfirmed", false)
        }
        V62Store.add(context, V62Store.DOCUMENTS, value)
        V62EventBus.publish(V62Event(V62Events.DOCUMENT_ADDED, id))
        return value
    }
}
