package com.udhaardaar.mvp

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec
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
    private const val KEY_RECORDS_ENCRYPTED = "records_encrypted_v1"
    private const val RECORDS_KEY_ALIAS = "arthsaathi_master_records_v1"
    private const val OWNER = "ownerUserId"
    private lateinit var appContext: Context

    fun initialize(context: Context) { appContext = context.applicationContext }
    private fun prefs() = appContext.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    internal fun ownerCanRead(recordOwner: String?, activeOwner: String?): Boolean =
        !recordOwner.isNullOrBlank() && !activeOwner.isNullOrBlank() && recordOwner == activeOwner

    private fun activeOwner(): String? = ArthSaathiSession.ownerId(appContext)?.takeIf { it.isNotBlank() }

    /**
     * Financial and identity records are encrypted at rest with AES-GCM using a
     * non-exportable Android Keystore key. A one-time migration encrypts the earlier
     * plaintext preference and removes it; decryption failures fail closed.
     */
    @Synchronized private fun allStored(): JSONArray {
        val preferences = prefs()
        val encrypted = preferences.getString(KEY_RECORDS_ENCRYPTED, null)
        if (!encrypted.isNullOrBlank()) return JSONArray(decryptRecords(encrypted))
        val legacyPlaintext = preferences.getString(KEY_RECORDS, "[]") ?: "[]"
        val migrated = encryptRecords(legacyPlaintext)
        val committed = preferences.edit()
            .putString(KEY_RECORDS_ENCRYPTED, migrated)
            .remove(KEY_RECORDS)
            .commit()
        check(committed) { "Secure record-store migration could not be committed." }
        return JSONArray(legacyPlaintext)
    }

    @Synchronized private fun saveStored(records: JSONArray) {
        val committed = prefs().edit()
            .putString(KEY_RECORDS_ENCRYPTED, encryptRecords(records.toString()))
            .remove(KEY_RECORDS)
            .commit()
        check(committed) { "Secure record-store update could not be committed." }
    }

    private fun encryptRecords(plaintext: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, recordsKey())
        val encrypted = cipher.doFinal(plaintext.toByteArray(StandardCharsets.UTF_8))
        val envelope = byteArrayOf(cipher.iv.size.toByte()) + cipher.iv + encrypted
        return Base64.encodeToString(envelope, Base64.NO_WRAP)
    }

    private fun decryptRecords(encoded: String): String {
        val envelope = Base64.decode(encoded, Base64.NO_WRAP)
        require(envelope.isNotEmpty()) { "Encrypted record store is empty." }
        val ivSize = envelope[0].toInt() and 0xff
        require(ivSize in 12..16 && envelope.size > 1 + ivSize) { "Encrypted record store format is invalid." }
        val iv = envelope.copyOfRange(1, 1 + ivSize)
        val encrypted = envelope.copyOfRange(1 + ivSize, envelope.size)
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.DECRYPT_MODE, recordsKey(), GCMParameterSpec(128, iv))
        return String(cipher.doFinal(encrypted), StandardCharsets.UTF_8)
    }

    private fun recordsKey(): SecretKey {
        val keyStore = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (keyStore.getKey(RECORDS_KEY_ALIAS, null) as? SecretKey)?.let { return it }
        val generator = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        generator.init(
            KeyGenParameterSpec.Builder(
                RECORDS_KEY_ALIAS,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return generator.generateKey()
    }

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
        saveStored(all)
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
        saveStored(merged)
    }
}
