package com.udhaardaar.mvp

object ArthSaathiCoreEngine {
    fun validateMobile(mobile: String): Boolean = mobile.matches(Regex("\\d{10}"))
}
