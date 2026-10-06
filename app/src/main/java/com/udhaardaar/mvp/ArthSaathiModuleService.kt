package com.udhaardaar.mvp

import org.json.JSONObject

/** Shared lifecycle/persistence service used by every consolidated module. */
object ArthSaathiModuleService {
    enum class Action { CREATE, UPDATE, CLOSE, REOPEN, DELETE, SUBMIT, APPROVE, REJECT, SETTLE }

    fun create(type:String, fields:Map<String,Any?>): String {
        val id=type+"-"+System.currentTimeMillis()
        val o=JSONObject().apply {
            put("id",id); put("type",type); put("status","DRAFT"); put("createdAt",System.currentTimeMillis()); put("updatedAt",System.currentTimeMillis())
            fields.forEach { (k,v) -> put(k,v) }
        }
        ArthSaathiDataStore.append(o); return id
    }

    fun update(id:String, fields:Map<String,Any?>): ArthSaathiDomainEngine.Result {
        val all=ArthSaathiDataStore.records()
        for(i in 0 until all.length()) {
            val o=all.getJSONObject(i)
            if(o.optString("id")!=id) continue
            fields.forEach { (k,v) -> o.put(k,v) }
            o.put("updatedAt",System.currentTimeMillis())
            ArthSaathiDataStore.replace(all)
            return ArthSaathiDomainEngine.Result(true,"Updated",id)
        }
        return ArthSaathiDomainEngine.Result(false,"Record not found")
    }

    fun transition(id:String, action:Action, authorized:Boolean=true): ArthSaathiDomainEngine.Result {
        if(!authorized) return ArthSaathiDomainEngine.Result(false,"Authorization required")
        val all=ArthSaathiDataStore.records()
        for(i in 0 until all.length()) {
            val o=all.getJSONObject(i)
            if(o.optString("id")!=id) continue
            val next=when(action) {
                Action.CREATE -> "DRAFT"
                Action.UPDATE -> o.optString("status","DRAFT")
                Action.SUBMIT -> "PENDING"
                Action.APPROVE -> "ACTIVE"
                Action.REJECT -> "REJECTED"
                Action.CLOSE,Action.SETTLE -> "CLOSED"
                Action.REOPEN -> "ACTIVE"
                Action.DELETE -> "DELETED"
            }
            o.put("status",next); o.put("updatedAt",System.currentTimeMillis())
            ArthSaathiDataStore.replace(all)
            return ArthSaathiDomainEngine.Result(true,"Status updated to $next",id)
        }
        return ArthSaathiDomainEngine.Result(false,"Record not found")
    }

    fun find(type:String):List<JSONObject> {
        val out=mutableListOf<JSONObject>(); val all=ArthSaathiDataStore.records()
        for(i in 0 until all.length()) if(all.getJSONObject(i).optString("type")==type) out += all.getJSONObject(i)
        return out
    }
}
