package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONArray
import org.json.JSONObject

/** Lightweight V5 persistence boundary. Replaceable by Room/API repository without changing UI contracts. */
class V5LocalStore(context: Context) {
    private val p = context.getSharedPreferences("v5_store", Context.MODE_PRIVATE)
    private fun read(key:String)=JSONArray(p.getString(key,"[]"))
    private fun write(key:String,a:JSONArray)=p.edit().putString(key,a.toString()).apply()
    fun add(key:String, value:JSONObject){ val a=read(key);a.put(value);write(key,a) }
    fun all(key:String):List<JSONObject>{ val a=read(key);return (0 until a.length()).mapNotNull{a.optJSONObject(it)} }
    fun find(key:String,id:String)=all(key).firstOrNull{it.optString("id")==id}
    fun replace(key:String,value:JSONObject){val a=read(key);for(i in 0 until a.length())if(a.optJSONObject(i)?.optString("id")==value.optString("id")){a.put(i,value);write(key,a);return};a.put(value);write(key,a)}
    fun remove(key:String,id:String){val a=read(key);for(i in a.length()-1 downTo 0)if(a.optJSONObject(i)?.optString("id")==id)a.remove(i);write(key,a)}
}
