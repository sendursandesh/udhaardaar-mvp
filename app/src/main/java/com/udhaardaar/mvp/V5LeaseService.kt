package com.udhaardaar.mvp

import android.content.Context
import org.json.JSONObject

/** Rental/lease is a separate domain; it never reuses personal-credit repayment fields. */
class V5LeaseService(context:Context){
 private val store=V5LocalStore(context)
 fun create(r:V5Rental):String{require(r.monthlyRent>=0&&r.deposit>=0&&r.startDate.isNotBlank()&&r.endDate.isNotBlank());val id=r.id.ifBlank{"LEASE-${System.currentTimeMillis()}"};store.replace("leases",JSONObject().apply{put("id",id);put("tenant",r.tenantProfileId);put("property",r.property);put("landlord",r.landlord);put("rent",r.monthlyRent);put("deposit",r.deposit);put("start",r.startDate);put("end",r.endDate);put("escalation",r.escalationPercent);put("noticeDays",r.noticeDays);put("document",r.documentId?:"")});return id}
}
