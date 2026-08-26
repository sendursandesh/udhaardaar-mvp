package com.udhaardaar.mvp

/** Validation used before a borrower/business profile is accepted or saved. */
object ProfileIdentityValidator {
    data class Input(
        val name: String,
        val mobile: String,
        val aadhaar: String = "",
        val pan: String = "",
        val gstin: String = "",
        val pin: String = ""
    )

    sealed class Result {
        data object Valid : Result()
        data class Invalid(val field: String, val message: String) : Result()
    }

    fun validate(input: Input): Result {
        if (input.name.trim().length < 2) return Result.Invalid("name", "Enter a valid name")
        if (!ProfileValidation.mobile(input.mobile)) return Result.Invalid("mobile", "Enter a valid 10-digit mobile number")
        if (input.aadhaar.isNotBlank() && !ProfileValidation.aadhaar(input.aadhaar)) return Result.Invalid("aadhaar", "Enter a valid 12-digit Aadhaar number")
        if (input.pan.isNotBlank() && !ProfileValidation.pan(input.pan)) return Result.Invalid("pan", "Enter a valid PAN")
        if (input.gstin.isNotBlank() && !ProfileValidation.gstin(input.gstin)) return Result.Invalid("gstin", "Enter a valid GSTIN")
        if (input.pin.isNotBlank() && !ProfileValidation.pin(input.pin)) return Result.Invalid("pin", "Enter a valid 6-digit PIN")
        return Result.Valid
    }

    fun normalize(value: String): String = value.trim().uppercase()
}
