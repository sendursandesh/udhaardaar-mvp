package com.udhaardaar.mvp

import java.util.concurrent.CopyOnWriteArraySet

/** Shared in-process event hub for all ArthSaathi V6.2 modules. */
object V62EventBus {
    private val listeners = CopyOnWriteArraySet<(V62Event) -> Unit>()
    fun subscribe(listener: (V62Event) -> Unit) { listeners += listener }
    fun unsubscribe(listener: (V62Event) -> Unit) { listeners -= listener }
    fun publish(event: V62Event) { listeners.forEach { listener -> runCatching { listener(event) } } }
}

/** Canonical event names; every persisted cross-module change uses one of these. */
object V62Events {
    const val PROFILE_CHANGED = "PROFILE_CHANGED"
    const val FAMILY_CHANGED = "FAMILY_CHANGED"
    const val ADDRESS_CHANGED = "ADDRESS_CHANGED"
    const val RELATIONSHIP_CHANGED = "RELATIONSHIP_CHANGED"
    const val REPAYMENT_CHANGED = "REPAYMENT_CHANGED"
    const val DOCUMENT_ADDED = "DOCUMENT_ADDED"
    const val ASSET_CHANGED = "ASSET_CHANGED"
    const val LIABILITY_CHANGED = "LIABILITY_CHANGED"
    const val POLICY_CHANGED = "POLICY_CHANGED"
    const val TTMM_EXPENSE_CHANGED = "TTMM_EXPENSE_CHANGED"
    const val CONSENT_CHANGED = "CONSENT_CHANGED"
    const val NOMINEE_CHANGED = "NOMINEE_CHANGED"
    const val CLAIM_CHANGED = "CLAIM_CHANGED"
    const val WILL_CHANGED = "WILL_CHANGED"
    const val TRADE_CREDIT_IMPORTED = "TRADE_CREDIT_IMPORTED"
    const val FORMAL_LOAN_CHANGED = "FORMAL_LOAN_CHANGED"
    const val CHARGECHECK_CHANGED = "CHARGECHECK_CHANGED"
    const val FUNDING_REQUEST_CHANGED = "FUNDING_REQUEST_CHANGED"
    const val ALERT_CREATED = "ALERT_CREATED"
}
