package com.udhaardaar.mvp

/** Single gate that profile create/edit flows must pass before database writes. */
object ProfileSaveGuard {
    data class ProfileInput(
        val name: String,
        val mobile: String,
        val aadhaar: String = "",
        val pan: String = "",
        val gstin: String = "",
        val pin: String = ""
    )

    data class ValidationResult(
        val valid: Boolean,
        val field: String? = null,
        val message: String? = null,
        val normalized: ProfileInput? = null
    )

    fun validate(input: ProfileInput): ValidationResult {
        val name = input.name.trim()
        val mobile = input.mobile.filter(Char::isDigit)
        val aadhaar = input.aadhaar.filter(Char::isDigit)
        val pan = input.pan.trim().uppercase()
        val gstin = input.gstin.trim().uppercase()
        val pin = input.pin.trim()
        if (name.length < 2) return fail("name", "Enter a valid name")
        if (!ProfileValidation.mobile(mobile)) return fail("mobile", "Enter a valid 10-digit mobile number")
        if (aadhaar.isNotEmpty() && !ProfileValidation.aadhaar(aadhaar)) return fail("aadhaar", "Enter a valid 12-digit Aadhaar number")
        if (pan.isNotEmpty() && !ProfileValidation.pan(pan)) return fail("pan", "Enter a valid PAN")
        if (gstin.isNotEmpty() && !ProfileValidation.gstin(gstin)) return fail("gstin", "Enter a valid GSTIN")
        if (pin.isNotEmpty() && !ProfileValidation.pin(pin)) return fail("pin", "Enter a valid 6-digit PIN")
        return ValidationResult(true, normalized = ProfileInput(name, mobile, aadhaar, pan, gstin, pin))
    }

    private fun fail(field: String, message: String) = ValidationResult(false, field, message)
}
