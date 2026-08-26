package com.udhaardaar.mvp

/**
 * Central PIN lookup boundary. Keep the UI independent from the eventual
 * government/address provider so the provider can be replaced without changing forms.
 */
object PinLocationResolver {
    data class Location(val pin: String, val city: String, val state: String)

    interface Provider {
        fun lookup(pin: String): Location?
    }

    private var provider: Provider? = null

    fun setProvider(value: Provider?) { provider = value }

    fun lookup(pin: String): Location? {
        val normalized = pin.trim()
        if (!ProfileValidation.pin(normalized)) return null
        return provider?.lookup(normalized)
    }
}
