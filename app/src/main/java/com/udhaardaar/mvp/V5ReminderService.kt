package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/** Unified obligation reminder records for credit and lease domains. */
class V5ReminderService(context:Context){
 private val store=V5LocalStore(context)
 fun schedule(entityId:String,domain:String,dueDate:String,label:String){
  require(entityId.isNotBlank()&&dueDate.isNotBlank()&&label.isNotBlank())
  store.add("reminders",JSONObject().apply{put("id","REM-${System.currentTimeMillis()}");put("entityId",entityId);put("domain",domain);put("dueDate",dueDate);put("label",label);put("status","UPCOMING")})
 }
 fun overdue(entityId:String)=store.all("reminders").filter{it.optString("entityId")==entityId&&it.optString("status")=="OVERDUE"}
}
