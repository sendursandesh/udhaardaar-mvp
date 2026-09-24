package com.udhaardaar.mvp

/**
 * Transitional compatibility bridge.
 * V7Architecture.Events is the canonical event source. Legacy V6.2 listeners
 * receive translated events only through this boundary during migration.
 */
object V7LegacyEventBridge {
    private var subscription: AutoCloseable? = null

    fun install() {
        if (subscription != null) return
        subscription = V7Architecture.Events.subscribe { event ->
            val legacy = when (event.event) {
                V7Architecture.Event.PROFILE_CHANGED,
                V7Architecture.Event.PERSON_CHANGED -> V62Events.PROFILE_CHANGED
                V7Architecture.Event.RELATIONSHIP_CHANGED,
                V7Architecture.Event.CREDIT_CREATED,
                V7Architecture.Event.CREDIT_CHANGED -> V62Events.RELATIONSHIP_CHANGED
                V7Architecture.Event.ADDRESS_CHANGED -> V62Events.ADDRESS_CHANGED
                V7Architecture.Event.REPAYMENT_CHANGED -> V62Events.REPAYMENT_CHANGED
                V7Architecture.Event.DOCUMENT_CHANGED -> V62Events.DOCUMENT_ADDED
                V7Architecture.Event.CONSENT_GRANTED,
                V7Architecture.Event.CONSENT_REVOKED -> V62Events.CONSENT_CHANGED
                V7Architecture.Event.ASSET_CHANGED -> V62Events.ASSET_CHANGED
                V7Architecture.Event.LIABILITY_CHANGED -> V62Events.LIABILITY_CHANGED
                V7Architecture.Event.POLICY_CHANGED -> V62Events.POLICY_CHANGED
                V7Architecture.Event.CLAIM_CHANGED -> V62Events.CLAIM_CHANGED
                V7Architecture.Event.NOMINEE_CHANGED -> V62Events.NOMINEE_CHANGED
                V7Architecture.Event.HOLDING_CHANGED -> V62Events.SAVINGS_CHANGED
                V7Architecture.Event.FUNDING_CHANGED -> V62Events.FUNDING_REQUEST_CHANGED
                V7Architecture.Event.ALERT_CREATED -> V62Events.ALERT_CREATED
                else -> null
            } ?: return@subscribe
            V62EventBus.publish(V62Event(legacy, event.entityId, event.timestamp))
        }
    }
}


/** Compatibility event vocabulary retained for the V6.2 migration boundary. */
object V62Events {
    const val PROFILE_CHANGED = "PROFILE_CHANGED"
    const val RELATIONSHIP_CHANGED = "RELATIONSHIP_CHANGED"
    const val ADDRESS_CHANGED = "ADDRESS_CHANGED"
    const val REPAYMENT_CHANGED = "REPAYMENT_CHANGED"
    const val DOCUMENT_ADDED = "DOCUMENT_ADDED"
    const val CONSENT_CHANGED = "CONSENT_CHANGED"
    const val ASSET_CHANGED = "ASSET_CHANGED"
    const val LIABILITY_CHANGED = "LIABILITY_CHANGED"
    const val POLICY_CHANGED = "POLICY_CHANGED"
    const val CLAIM_CHANGED = "CLAIM_CHANGED"
    const val NOMINEE_CHANGED = "NOMINEE_CHANGED"
    const val SAVINGS_CHANGED = "SAVINGS_CHANGED"
    const val FUNDING_REQUEST_CHANGED = "FUNDING_REQUEST_CHANGED"
    const val ALERT_CREATED = "ALERT_CREATED"
}

data class V62Event(val type: String, val entityId: String, val timestamp: Long)

object V62EventBus {
    private val listeners = mutableListOf<(V62Event) -> Unit>()
    @Synchronized fun subscribe(listener: (V62Event) -> Unit): AutoCloseable {
        listeners.add(listener)
        return AutoCloseable { synchronized(listeners) { listeners.remove(listener) } }
    }
    @Synchronized fun publish(event: V62Event) {
        listeners.toList().forEach { it(event) }
    }
}
