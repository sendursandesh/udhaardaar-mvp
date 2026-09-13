package com.udhaardaar.mvp

import java.util.concurrent.CopyOnWriteArraySet

/** Lightweight in-process event hub. A later backend sync layer can publish the same V62Event contract. */
object V62EventBus {
    private val listeners = CopyOnWriteArraySet<(V62Event) -> Unit>()

    fun subscribe(listener: (V62Event) -> Unit) { listeners += listener }
    fun unsubscribe(listener: (V62Event) -> Unit) { listeners -= listener }

    fun publish(event: V62Event) {
        listeners.forEach { listener -> runCatching { listener(event) } }
    }
}

/** One place for event names so modules cannot silently diverge. */
object V62Events {
    const val RELATIONSHIP_CHANGED = "RELATIONSHIP_CHANGED"
    const val REPAYMENT_CHANGED = "REPAYMENT_CHANGED"
    const val DOCUMENT_ADDED = "DOCUMENT_ADDED"
    const val ASSET_CHANGED = "ASSET_CHANGED"
    const val POLICY_CHANGED = "POLICY_CHANGED"
    const val TTMM_EXPENSE_CHANGED = "TTMM_EXPENSE_CHANGED"
    const val CONSENT_CHANGED = "CONSENT_CHANGED"
    const val NOMINEE_CHANGED = "NOMINEE_CHANGED"
    const val CLAIM_CHANGED = "CLAIM_CHANGED"
    const val WILL_CHANGED = "WILL_CHANGED"
}
