package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test

class V7Step1To5IntegrationInstrumentedTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test fun canonicalRecordConsentRepaymentAndMetricsFlow() {
        val store = V7LocalStore(context)
        store.all(V7Core.Keys.REPAYMENTS).forEach { store.remove(V7Core.Keys.REPAYMENTS, it.optString("id")) }

        val person = V7Records.person(context, "V7 QA Person", "9876543210")
        val relationship = V7Records.relationship(
            context, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 10000.0, 12.0, "PRINCIPAL_PLUS_INTEREST", "QA"
        )

        val consent = V7Architecture.LocalConsentService(context).request(
            V7Architecture.ConsentRequest(
                subjectId = person.optString("id"),
                purpose = "REPAYMENT_UPDATE",
                scope = "relationship:" + relationship.optString("id"),
                actorId = V7Core.user(context),
                expiresAt = System.currentTimeMillis() + 60_000L
            )
        )
        assertNull(V7Architecture.LocalConsentService(context).grant(consent.id, false))

        val granted = V7Architecture.LocalConsentService(context).grant(consent.id, true)
        assertNotNull(granted)
        assertTrue(V7Core.hasConsent(context, person.optString("id"), "REPAYMENT_UPDATE"))

        V7Records.repayment(
            context, relationship.optString("id"), 2500.0, 2500.0, 0.0,
            "UPI", true
        )
        val updated = V7Core.find(context, V7Core.Keys.RELATIONSHIPS, relationship.optString("id"))
        assertEquals(7500.0, updated!!.optDouble("outstanding"), 0.005)
        assertTrue(V7Core.all(context, V7Core.Keys.REPAYMENTS).any {
            it.optString("relationshipId") == relationship.optString("id") &&
                it.optDouble("amount") == 2500.0
        })

        val metrics = V7Core.metrics(context)
        assertTrue(metrics.optDouble("receivables") >= 7500.0)
        assertTrue(metrics.optInt("activeCredits") >= 1)
    }

    @Test fun repaymentCannotMutateWithoutActiveConsent() {
        val person = V7Records.person(context, "V7 Consent Guard QA", "9876543212")
        val relationship = V7Records.relationship(
            context, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 5000.0, 0.0, "PRINCIPAL_PLUS_INTEREST", "QA"
        )
        V7Records.repayment(
            context, relationship.optString("id"), 1000.0, 1000.0, 0.0,
            "CASH", true
        )
        val unchanged = V7Core.find(context, V7Core.Keys.RELATIONSHIPS, relationship.optString("id"))
        assertEquals(5000.0, unchanged!!.optDouble("outstanding"), 0.005)
        assertFalse(V7Core.all(context, V7Core.Keys.REPAYMENTS).any {
            it.optString("relationshipId") == relationship.optString("id") &&
                it.optDouble("amount") == 1000.0
        })
    }

    @Test fun expiredConsentIsNotActive() {
        val person = V7Records.person(context, "V7 Expiry QA", "9876543211")
        val service = V7Architecture.LocalConsentService(context)
        val consent = service.request(
            V7Architecture.ConsentRequest(
                subjectId = person.optString("id"),
                purpose = "HISTORY_VIEW",
                scope = "profile",
                actorId = V7Core.user(context),
                expiresAt = System.currentTimeMillis() - 1_000L
            )
        )
        assertNotNull(service.grant(consent.id, true))
        assertFalse(V7Core.hasConsent(context, person.optString("id"), "HISTORY_VIEW"))
        assertNull(service.getActive(person.optString("id"), "HISTORY_VIEW"))
    }
}
