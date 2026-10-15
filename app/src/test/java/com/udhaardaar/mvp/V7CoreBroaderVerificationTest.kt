package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class V7CoreBroaderVerificationTest {
    private lateinit var c: Context
    private val mobile = "9876504999"

    @Before fun setup() {
        c = ApplicationProvider.getApplicationContext()
        V7AccountStore.logout(c)
        listOf(
            V7Core.Keys.PEOPLE, V7Core.Keys.RELATIONSHIPS, V7Core.Keys.REPAYMENTS,
            V7Core.Keys.CONSENTS, V7Core.Keys.ASSETS, V7Core.Keys.LIABILITIES, V7Core.Keys.AUDIT
        ).forEach { key ->
            V7Core.all(c, key).filter { it.optString("ownerUserId") == mobile }.forEach {
                V7Core.store(c).remove(key, it.optString("id"))
            }
        }
        if (V7AccountStore.account(c, mobile) == null) V7AccountStore.create(c, "Broader QA", mobile)
        V7AccountStore.login(c, mobile)
    }

    @Test fun repaymentCannotMutateWithoutActiveVerifiedConsent() {
        val person = V7Records.person(c, "Consent QA", "9876504998")
        val rel = V7Records.relationship(c, person.optString("id"), "INFORMAL_CREDIT", "RECEIVABLE", 10000.0, 12.0, "PRINCIPAL_PLUS_INTEREST", "QA")
        val before = V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding")
        V7Records.repayment(c, rel.optString("id"), 1000.0, 1000.0, 0.0, "UPI", true)
        assertEquals(before, V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding"), 0.001)
        assertTrue(V7Core.all(c, V7Core.Keys.REPAYMENTS).none { it.optString("relationshipId") == rel.optString("id") })

        val consent = V7Core.consent(c, person.optString("id"), "REPAYMENT_UPDATE", "V7-QA", true)
        consent.put("expiresAt", V7Core.now() + 60000L)
        V7Core.replace(c, V7Core.Keys.CONSENTS, consent)
        V7Records.repayment(c, rel.optString("id"), 1000.0, 1000.0, 0.0, "UPI", true)
        val after = V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding")
        assertEquals(9000.0, after, 0.001)
    }

    @Test fun repaymentRejectsInvalidMethodAndOverpayment() {
        val person = V7Records.person(c, "Boundary QA", "9876504997")
        val rel = V7Records.relationship(c, person.optString("id"), "INFORMAL_CREDIT", "RECEIVABLE", 5000.0, 10.0, "EMI", "QA")
        val consent = V7Core.consent(c, person.optString("id"), "REPAYMENT_UPDATE", "V7-QA", true)
        consent.put("expiresAt", V7Core.now() + 60000L)
        V7Core.replace(c, V7Core.Keys.CONSENTS, consent)

        V7Records.repayment(c, rel.optString("id"), 500.0, 500.0, 0.0, "BITCOIN", true)
        assertEquals(5000.0, V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding"), 0.001)

        V7Records.repayment(c, rel.optString("id"), 6000.0, 6000.0, 0.0, "UPI", true)
        assertEquals(5000.0, V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding"), 0.001)
    }

    @Test fun metricsDoNotDoubleCountHoldingsAndTrackReceivables() {
        V7Records.asset(c, V7AccountStore.currentMobile(c), "PROPERTY", "QA", 100000.0)
        V7Records.holding(c, "PORT-QA", "FUND", "MUTUAL_FUND", 20000.0, 25000.0)
        val person = V7Records.person(c, "Metric QA", "9876504996")
        V7Records.relationship(c, person.optString("id"), "INFORMAL_CREDIT", "RECEIVABLE", 10000.0, 0.0, "BULLET", "QA")
        val m = V7Core.metrics(c)
        assertEquals(100000.0, m.optDouble("assets"), 0.001)
        assertEquals(25000.0, m.optDouble("portfolioValue"), 0.001)
        assertEquals(10000.0, m.optDouble("receivables"), 0.001)
        assertEquals(110000.0, m.optDouble("netWorth"), 0.001)
    }

    @Test fun expiredConsentCannotAuthorizeMutation() {
        val person = V7Records.person(c, "Expiry QA", "9876504995")
        val rel = V7Records.relationship(c, person.optString("id"), "INFORMAL_CREDIT", "RECEIVABLE", 3000.0, 0.0, "BULLET", "QA")
        val consent = V7Core.consent(c, person.optString("id"), "REPAYMENT_UPDATE", "V7-QA", true)
        consent.put("expiresAt", V7Core.now() - 1L)
        V7Core.replace(c, V7Core.Keys.CONSENTS, consent)
        V7Records.repayment(c, rel.optString("id"), 500.0, 500.0, 0.0, "CASH", true)
        assertEquals(3000.0, V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding"), 0.001)
    }
}
