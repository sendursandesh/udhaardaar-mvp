package com.udhaardaar.mvp

import org.json.JSONObject

class ArthSaathiWorkflowRepository {
    fun registerCredit(input: JSONObject): JSONObject {
        require(input.optString("nature").isNotBlank()) { "Nature of credit is required" }
        val account = JSONObject(input.toString())
        account.put("recordType", "CREDIT_ACCOUNT")
        account.put("id", "AS-" + System.currentTimeMillis())
        account.put("status", "ACTIVE")
        ArthSaathiDataStore.append(account)
        return account
    }

    fun existingCreditAccounts(): List<JSONObject> {
        val result = mutableListOf<JSONObject>()
        val all = ArthSaathiDataStore.records()
        for (i in 0 until all.length()) {
            val item = all.optJSONObject(i) ?: continue
            if (item.optString("recordType") == "CREDIT_ACCOUNT") result += item
        }
        return result
    }
}
