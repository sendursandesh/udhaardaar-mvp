package com.udhaardaar.mvp

object ProfileValidation {
    private val pan = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$")
    private val gstin = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$")
    private val mobile = Regex("^[6-9][0-9]{9}$")
    private val pin = Regex("^[1-9][0-9]{5}$")
    private val aadhaar = Regex("^[2-9][0-9]{11}$")

    fun pan(value: String): Boolean = pan.matches(value.trim().uppercase())
    fun gstin(value: String): Boolean = gstin.matches(value.trim().uppercase())
    fun mobile(value: String): Boolean = mobile.matches(value.filter(Char::isDigit))
    fun pin(value: String): Boolean = pin.matches(value.trim())
    fun aadhaar(value: String): Boolean = aadhaar.matches(value.filter(Char::isDigit))
    fun roi(value: Double): Boolean = value.isFinite() && value in 0.0..100.0
}
