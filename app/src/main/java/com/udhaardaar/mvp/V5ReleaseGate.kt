package com.udhaardaar.mvp

/**
 * Central release-gate marker for V5. The UI and persistence layers should use
 * these event types when writing immutable transaction evidence.
 */
object V5ReleaseGate {
    const val SCHEMA_VERSION = 5
    const val EVENT_CREATED = "CREATED"
    const val EVENT_UPDATED = "UPDATED"
    const val EVENT_QR_SCANNED = "QR_SCANNED"
    const val EVENT_DOCUMENT_GENERATED = "DOCUMENT_GENERATED"
    const val EVENT_OTP_CONSENT = "OTP_CONSENT"
    const val EVENT_REPAYMENT = "REPAYMENT"

    fun timestampUtc(): String = java.time.Instant.now().toString()
}
