package com.udhaardaar.mvp

/** Central V5 input validation used by profile and credit flows. */
object V5Validation {
    fun mobile(value: String): Boolean = Regex("^[6-9][0-9]{9}$").matches(value.trim())
    fun pan(value: String): Boolean = Regex("^[A-Z]{5}[0-9]{4}[A-Z]$").matches(value.trim().uppercase())
    fun aadhaar(value: String): Boolean = Regex("^[2-9][0-9]{11}$").matches(value.trim())
    fun pin(value: String): Boolean = Regex("^[1-9][0-9]{5}$").matches(value.trim())
    fun gstin(value: String): Boolean = Regex("^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z][1-9A-Z]Z[0-9A-Z]$").matches(value.trim().uppercase())
}
