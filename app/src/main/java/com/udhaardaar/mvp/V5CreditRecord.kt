package com.udhaardaar.mvp

data class V5CreditRecord(
    val id:String="",
    val profileId:String,
    val direction:String,
    val creditType:String,
    val principal:Double,
    val roiPercent:Double,
    val repaymentMethod:String,
    val startDate:String,
    val endDate:String,
    val consentState:String="PENDING"
)
