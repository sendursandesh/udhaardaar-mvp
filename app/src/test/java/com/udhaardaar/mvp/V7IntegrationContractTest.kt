package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.util.concurrent.atomic.AtomicInteger

class V7IntegrationContractTest {
    @Test fun canonicalContractAndEventsArePresent() {
        assertEquals("7.0.1", V7Architecture.CONTRACT_VERSION)
        assertTrue(V7Architecture.Event.values().contains(V7Architecture.Event.CREDIT_CREATED))
        assertTrue(V7Architecture.Event.values().contains(V7Architecture.Event.REPAYMENT_CHANGED))
        assertTrue(V7Architecture.Event.values().contains(V7Architecture.Event.CONSENT_GRANTED))
        assertTrue(V7Architecture.Event.values().contains(V7Architecture.Event.ASSET_CHANGED))
        assertTrue(V7Architecture.Event.values().contains(V7Architecture.Event.CLAIM_CHANGED))
    }

    @Test fun canonicalEventBusPublishesAndCanUnsubscribe() {
        val count = AtomicInteger(0)
        val closeable = V7Architecture.Events.subscribe { count.incrementAndGet() }
        V7Architecture.Events.publish(
            V7Architecture.EventRecord(
                V7Architecture.Event.ASSET_CHANGED,
                "asset-test",
                "test"
            )
        )
        assertEquals(1, count.get())
        closeable.close()
        V7Architecture.Events.publish(
            V7Architecture.EventRecord(
                V7Architecture.Event.ASSET_CHANGED,
                "asset-test-2",
                "test"
            )
        )
        assertEquals(1, count.get())
    }

    @Test fun consentExpiryAndRevocationSemanticsAreStrict() {
        val request = V7Architecture.ConsentRequest(
            subjectId = "person-1",
            purpose = "HISTORY_SHARING",
            scope = "SUMMARY",
            actorId = "actor-1",
            expiresAt = 2_000L,
            otpRequired = true
        )
        val active = V7Architecture.ConsentRecord(
            id = "cons-1",
            request = request,
            status = V7Architecture.ConsentRecord.Status.GRANTED,
            grantedAt = 1_000L,
            otpVerified = true
        )
        assertTrue(active.active(1_500L))
        assertFalse(active.active(2_000L))

        val revoked = active.copy(status = V7Architecture.ConsentRecord.Status.REVOKED, revokedAt = 1_600L)
        assertFalse(revoked.active(1_700L))
    }

    @Test fun canonicalEntityAndEventNamesHaveNoDuplicates() {
        val entities = V7Architecture.Entity.values().map { it.name }
        val events = V7Architecture.Event.values().map { it.name }
        assertEquals(entities.size, entities.toSet().size)
        assertEquals(events.size, events.toSet().size)
    }
}
