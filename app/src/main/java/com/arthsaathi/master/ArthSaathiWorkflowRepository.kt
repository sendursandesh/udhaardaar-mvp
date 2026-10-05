package com.arthsaathi.master

import org.json.JSONObject

class ArthSaathiWorkflowRepository {
    fun registerNewCredit(data:JSONObject):JSONObject {
        require(data.optString("nature").isNotBlank())
        val account=JSONObject(data.toString()).apply {
            put("recordType","CREDIT_ACCOUNT"); put("accountId","AS-"+System.currentTimeMillis()); put("status","ACTIVE")
        }
        ArthSaathiDataStore.add(account); return account
    }
    fun existingCredits():List<JSONObject> {
        val a=ArthSaathiDataStore.all(); val out=mutableListOf<JSONObject>()
        for(i in 0 until a.length()) if(a.optJSONObject(i)?.optString("recordType")=="CREDIT_ACCOUNT") out+=a.getJSONObject(i)
        return out
    }
}
