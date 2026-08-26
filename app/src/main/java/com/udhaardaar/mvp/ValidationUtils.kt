package com.udhaardaar.mvp

object ValidationUtils {
    private val pan = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val gstin = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    private val mobile = Regex("^[6-9][0-9]{9}$")
    private val pincode = Regex("^[1-9][0-9]{5}$")
    private val aadhaar = Regex("^[2-9][0-9]{11}$")

    fun normalizePan(value: String) = value.trim().uppercase()
    fun normalizeGstin(value: String) = value.trim().uppercase()
    fun isPanValid(value: String) = pan.matches(normalizePan(value))
    fun isGstinValid(value: String) = gstin.matches(normalizeGstin(value))
    fun isMobileValid(value: String) = mobile.matches(value.trim())
    fun isPincodeValid(value: String) = pincode.matches(value.trim())
    fun isAadhaarValid(value: String) = aadhaar.matches(value.replace(" ", "").trim())
    fun isRoiValid(value: Double) = value in 0.0..100.0
    fun isAmountValid(value: Double) = value.isFinite() && value > 0.0
}
