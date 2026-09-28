package com.udhaardaar.mvp

import android.content.Context
import android.security.keystore.KeyGenParameterSpec
import android.security.keystore.KeyProperties
import android.util.Base64
import org.json.JSONArray
import org.json.JSONObject
import java.nio.charset.StandardCharsets
import java.security.KeyStore
import javax.crypto.Cipher
import javax.crypto.KeyGenerator
import javax.crypto.SecretKey
import javax.crypto.spec.GCMParameterSpec

/**
 * V7-owned encrypted persistence boundary.
 *
 * V7 data lives in its own namespace and encryption key. It never reads or writes
 * the V5/V6.2 store. Legacy migration, when required, must happen through an
 * explicit adapter and never through the V7 repository implementation.
 */
class V7LocalStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs = appContext.getSharedPreferences("v7_store", Context.MODE_PRIVATE)
    private val alias = "arthsaathi_v7_store_key"

    private fun key(): SecretKey {
        val ks = KeyStore.getInstance("AndroidKeyStore").apply { load(null) }
        (ks.getKey(alias, null) as? SecretKey)?.let { return it }
        val kg = KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES, "AndroidKeyStore")
        kg.init(
            KeyGenParameterSpec.Builder(
                alias,
                KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT
            ).setBlockModes(KeyProperties.BLOCK_MODE_GCM)
                .setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE)
                .setRandomizedEncryptionRequired(true)
                .build()
        )
        return kg.generateKey()
    }

    private fun encrypt(value: String): String {
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(Cipher.ENCRYPT_MODE, key())
        val raw = cipher.iv + cipher.doFinal(value.toByteArray(StandardCharsets.UTF_8))
        return "ENC:" + Base64.encodeToString(raw, Base64.NO_WRAP)
    }

    private fun decrypt(value: String): String {
        if (!value.startsWith("ENC:")) return value
        val raw = Base64.decode(value.removePrefix("ENC:"), Base64.NO_WRAP)
        require(raw.size > 12) { "Invalid V7 encrypted record" }
        val cipher = Cipher.getInstance("AES/GCM/NoPadding")
        cipher.init(
            Cipher.DECRYPT_MODE,
            key(),
            GCMParameterSpec(128, raw.copyOfRange(0, 12))
        )
        return String(cipher.doFinal(raw.copyOfRange(12, raw.size)), StandardCharsets.UTF_8)
    }

    private fun read(key: String): JSONArray {
        check(!key.startsWith("v5_") && !key.startsWith("v62_")) {
            "V7 store cannot read legacy namespace: $key"
        }
        val raw = prefs.getString(key, null) ?: return JSONArray()
        return JSONArray(decrypt(raw))
    }

    private fun write(key: String, values: JSONArray) {
        check(!key.startsWith("v5_") && !key.startsWith("v62_")) {
            "V7 store cannot persist legacy namespace: $key"
        }
        prefs.edit().putString(key, encrypt(values.toString())).apply()
    }

    fun add(key: String, value: JSONObject) {
        val values = read(key)
        values.put(value)
        write(key, values)
    }

    fun all(key: String): List<JSONObject> {
        val values = read(key)
        return (0 until values.length()).mapNotNull { values.optJSONObject(it) }
            .filter { it.optString("status") != "DELETED" }
    }

    fun find(key: String, id: String): JSONObject? =
        all(key).firstOrNull { it.optString("id") == id }

    fun replace(key: String, value: JSONObject) {
        val values = read(key)
        for (i in 0 until values.length()) {
            if (values.optJSONObject(i)?.optString("id") == value.optString("id")) {
                values.put(i, value)
                write(key, values)
                return
            }
        }
        values.put(value)
        write(key, values)
    }

    fun remove(key: String, id: String): Boolean {
        val values = read(key)
        for (i in values.length() - 1 downTo 0) {
            if (values.optJSONObject(i)?.optString("id") == id) {
                values.remove(i)
                write(key, values)
                return true
            }
        }
        return false
    }
}
