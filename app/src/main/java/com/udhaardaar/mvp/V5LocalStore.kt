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

/** Encrypted V5 persistence boundary. Existing plaintext records remain readable and become encrypted on next write. */
class V5LocalStore(context: Context) {
    private val p=context.getSharedPreferences("v5_store",Context.MODE_PRIVATE)
    private val alias="udhaardaar_v5_store_key"
    private fun key():SecretKey{val ks=KeyStore.getInstance("AndroidKeyStore").apply{load(null)};(ks.getKey(alias,null) as? SecretKey)?.let{return it};val kg=KeyGenerator.getInstance(KeyProperties.KEY_ALGORITHM_AES,"AndroidKeyStore");kg.init(KeyGenParameterSpec.Builder(alias,KeyProperties.PURPOSE_ENCRYPT or KeyProperties.PURPOSE_DECRYPT).setBlockModes(KeyProperties.BLOCK_MODE_GCM).setEncryptionPaddings(KeyProperties.ENCRYPTION_PADDING_NONE).setRandomizedEncryptionRequired(true).build());return kg.generateKey()}
    private fun enc(v:String):String{val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.ENCRYPT_MODE,key());val raw=c.iv+c.doFinal(v.toByteArray(StandardCharsets.UTF_8));return "ENC:"+Base64.encodeToString(raw,Base64.NO_WRAP)}
    private fun dec(v:String):String{if(!v.startsWith("ENC:"))return v;val raw=Base64.decode(v.removePrefix("ENC:"),Base64.NO_WRAP);val c=Cipher.getInstance("AES/GCM/NoPadding");c.init(Cipher.DECRYPT_MODE,key(),GCMParameterSpec(128,raw.copyOfRange(0,12)));return String(c.doFinal(raw.copyOfRange(12,raw.size)),StandardCharsets.UTF_8)}
    private fun read(key:String)=runCatching{JSONArray(dec(p.getString(key,"[]")!!))}.getOrElse{JSONArray()}
    private fun write(key:String,a:JSONArray)=p.edit().putString(key,enc(a.toString())).apply()
    fun add(key:String,value:JSONObject){val a=read(key);a.put(value);write(key,a)}
    fun all(key:String):List<JSONObject>{val a=read(key);return(0 until a.length()).mapNotNull{a.optJSONObject(it)}}
    fun find(key:String,id:String)=all(key).firstOrNull{it.optString("id")==id}
    fun replace(key:String,value:JSONObject){val a=read(key);for(i in 0 until a.length())if(a.optJSONObject(i)?.optString("id")==value.optString("id")){a.put(i,value);write(key,a);return};a.put(value);write(key,a)}
    fun remove(key:String,id:String){val a=read(key);for(i in a.length()-1 downTo 0)if(a.optJSONObject(i)?.optString("id")==id)a.remove(i);write(key,a)}
}
