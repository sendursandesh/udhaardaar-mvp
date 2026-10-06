package com.udhaardaar.mvp

object ArthSaathiMisModel {
    data class Metric(val id:String,val label:String,val sourceModules:Set<String>,val unit:String,val formula:String,val chart:String)
    val metrics=listOf(
        Metric("CREDIT_GIVEN","Credit registered",setOf("REGISTER_CREDIT"),"INR","sum registered principal","number"),
        Metric("OUTSTANDING","Credit outstanding",setOf("LOANS_UDHAAR","REPAYMENT"),"INR","principal minus recorded repayments","number"),
        Metric("REPAYMENT","Repayments",setOf("REPAYMENT"),"INR","sum authorized repayments","number"),
        Metric("ASSETS","Assets",setOf("ASSET_VAULT","PORTFOLIO"),"INR","sum current values","pie"),
        Metric("LIABILITIES","Liabilities",setOf("LIABILITY_VAULT","LOANS_UDHAAR"),"INR","sum outstanding liabilities","pie"),
        Metric("BENEFITS","Benefits and refunds value",setOf("BENEFITS"),"INR","sum approved and received values","number"),
        Metric("NET_POSITION","Indicative net position",setOf("ASSET_VAULT","PORTFOLIO","LIABILITY_VAULT"),"INR","assets plus portfolio minus liabilities","pie"),
        Metric("GROUP_DUES","Group contributions outstanding",setOf("GROUP_KHATA"),"INR","sum unpaid member shares","number"),
        Metric("REVENUE","ArthSaathi revenue",setOf("REVENUE"),"INR","sum successful service charges","number"),
        Metric("CHARGE_VARIANCE","Charge variance",setOf("CHARGECHECK"),"INR","actual minus sanctioned","number")
    )
    init{check(metrics.map{it.id}.distinct().size==metrics.size)}
}