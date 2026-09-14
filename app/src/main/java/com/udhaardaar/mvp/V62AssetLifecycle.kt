package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Asset Vault lifecycle: acquire -> hold -> pledge/encumber -> sell/release -> close. */
object V62AssetLifecycle {
    const val ACTIVE = "ACTIVE"; const val PLEDGED = "PLEDGED"; const val ENCUMBERED = "ENCUMBERED"; const val UNDER_SALE = "UNDER_SALE"; const val SOLD = "SOLD"; const val RELEASED = "RELEASED"; const val TRANSFERRED = "TRANSFERRED"; const val GIFTED = "GIFTED"; const val DISPOSED = "DISPOSED"
    fun status(asset: JSONObject) = asset.optString("lifecycleStatus", ACTIVE)
    fun sell(c: Context, assetId: String, saleDate: String, saleValue: Double, buyerName: String, paymentStatus: String, saleDocumentId: String = "") = update(c, assetId, SOLD, false) { o -> o.put("saleDate", saleDate); o.put("saleValue", saleValue); o.put("buyerName", buyerName.trim()); o.put("salePaymentStatus", paymentStatus); if (saleDocumentId.isNotBlank()) o.put("saleDocumentId", saleDocumentId) }
    fun release(c: Context, assetId: String, releaseDate: String, reason: String, releasedFrom: String, chargeReference: String, releaseDocumentId: String = "") = update(c, assetId, RELEASED, true) { o -> o.put("releaseDate", releaseDate); o.put("releaseReason", reason.trim()); o.put("releasedFrom", releasedFrom.trim()); o.put("chargeReference", chargeReference.trim()); if (releaseDocumentId.isNotBlank()) o.put("releaseDocumentId", releaseDocumentId) }
    private fun update(c: Context, assetId: String, newStatus: String, remainsCurrent: Boolean, mutate: (JSONObject) -> Unit): Boolean {
        val store = V62Store.store(c); val asset = store.find(V62Store.ASSETS, assetId) ?: return false; val oldStatus = status(asset)
        if (oldStatus == SOLD || oldStatus == TRANSFERRED || oldStatus == GIFTED || oldStatus == DISPOSED) return false
        val history = asset.optJSONArray("lifecycleHistory") ?: JSONArray(); history.put(JSONObject().apply { put("from", oldStatus); put("to", newStatus); put("at", System.currentTimeMillis()) })
        asset.put("lifecycleStatus", newStatus); asset.put("lifecycleHistory", history); asset.put("currentAsset", remainsCurrent); mutate(asset); store.replace(V62Store.ASSETS, asset)
        V62EventBus.publish(ArthSaathiV62Domain.Event(V62Events.ASSET_CHANGED, assetId)); return true
    }
}
