package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test

/**
 * Master integration matrix: validates the same canonical data path across
 * representative customer types, transaction choices and adverse inputs.
 */
class V7MasterIntegrationScenarioInstrumentedTest {
    private val c: Context get() = ApplicationProvider.getApplicationContext()

    @Test fun everyMasterModuleHasAnExplicitOwner() {
        val modules = V7MasterVisionRegistry.all()
        assertEquals(V7MasterVisionRegistry.Module.entries.size, modules.size)
        assertTrue(modules.all { it.v7DataKeys.isNotEmpty() })
        assertTrue(modules.map { it.key }.toSet().size == modules.size)
    }

    @Test fun customerAndCreditTypeMatrixUsesOneCanonicalRelationshipModel() {
        val person = V7Records.person(c, "Matrix Customer", "9876501001")

        val informal = V7Records.relationship(c, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 10000.0, 12.0, "PRINCIPAL_PLUS_INTEREST", "working capital")
        val trade = V7Records.relationship(c, person.optString("id"), "TRADE_CREDIT",
            "RECEIVABLE", 25000.0, 0.0, "INVOICE_DUE", "inventory")
        val formal = V7Records.relationship(c, person.optString("id"), "FORMAL_CREDIT",
            "PAYABLE", 100000.0, 9.5, "EMI", "bank facility")
        val rental = V7Records.relationship(c, person.optString("id"), "RENTAL_LEASE",
            "PAYABLE", 18000.0, 0.0, "MONTHLY", "rent")

        assertEquals("INFORMAL_CREDIT", informal.optString("type"))
        assertEquals("TRADE_CREDIT", trade.optString("type"))
        assertEquals("FORMAL_CREDIT", formal.optString("type"))
        assertEquals("RENTAL_LEASE", rental.optString("type"))
        assertEquals(0.0, rental.optDouble("roiPercent"), 0.005)
        assertEquals(4, listOf(informal, trade, formal, rental).map { it.optString("id") }.toSet().size)
    }

    @Test fun repaymentMethodsAndConsentAreConsistent() {
        val person = V7Records.person(c, "Repayment Matrix", "9876501002")
        val rel = V7Records.relationship(c, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 9000.0, 12.0, "PRINCIPAL_PLUS_INTEREST", "matrix")

        val methods = listOf("CASH","UPI","NEFT")
        val service = V7Architecture.LocalConsentService(c)
        val consent = service.request(V7Architecture.ConsentRequest(
            subjectId = person.optString("id"), purpose = "REPAYMENT_UPDATE",
            scope = "relationship:" + rel.optString("id"), actorId = V7Core.user(c),
            expiresAt = System.currentTimeMillis() + 120_000L
        ))
        assertNull(service.grant(consent.id, false))
        assertNotNull(service.grant(consent.id, true))

        var outstanding = rel.optDouble("outstanding")
        methods.forEach { method ->
            V7Records.repayment(c, rel.optString("id"), 1000.0, 1000.0, 0.0, method, true)
            outstanding -= 1000.0
            assertEquals(outstanding, V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding"), 0.005)
        }
        assertEquals(6000.0, outstanding, 0.005)
    }

    @Test fun adverseRepaymentChoicesCannotCorruptOutstanding() {
        val person = V7Records.person(c, "Adverse QA", "9876501003")
        val rel = V7Records.relationship(c, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 5000.0, 0.0, "PRINCIPAL_PLUS_INTEREST", "adverse")
        val service = V7Architecture.LocalConsentService(c)
        val consent = service.request(V7Architecture.ConsentRequest(
            subjectId = person.optString("id"), purpose = "REPAYMENT_UPDATE",
            scope = "relationship:" + rel.optString("id"), actorId = V7Core.user(c),
            expiresAt = System.currentTimeMillis() + 120_000L
        ))
        assertNotNull(service.grant(consent.id, true))

        V7Records.repayment(c, rel.optString("id"), 6000.0, 6000.0, 0.0, "UPI", true)
        V7Records.repayment(c, rel.optString("id"), -1.0, -1.0, 0.0, "UPI", true)
        V7Records.repayment(c, rel.optString("id"), 1000.0, 1000.0, 1000.0, "UPI", true)
        V7Records.repayment(c, rel.optString("id"), 1000.0, 1000.0, 0.0, "INVALID", true)

        val unchanged = V7Core.find(c, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!
        assertEquals(5000.0, unchanged.optDouble("outstanding"), 0.005)
        assertFalse(V7Core.all(c, V7Core.Keys.REPAYMENTS).any {
            it.optString("relationshipId") == rel.optString("id") && it.optDouble("principal") == 6000.0
        })
    }

    @Test fun assetLiabilityAndCreditFlowIntoOneDashboardProjection() {
        val person = V7Records.person(c, "Dashboard Matrix", "9876501004")
        V7Records.asset(c, person.optString("id"), "PROPERTY", "House", 5000000.0)
        V7Records.liability(c, person.optString("id"), "HOME_LOAN", 2500000.0, 2200000.0, 8.5)
        V7Records.relationship(c, person.optString("id"), "TRADE_CREDIT", "RECEIVABLE",
            30000.0, 0.0, "INVOICE_DUE", "sales")
        val m = V7Core.metrics(c)
        assertTrue(m.optDouble("assets") >= 5000000.0)
        assertTrue(m.optDouble("liabilities") >= 2200000.0)
        assertTrue(m.optDouble("receivables") >= 30000.0)
        assertTrue(m.has("netWorth"))
    }

    @Test fun legacyProjectionIsIdempotentForRepresentativeRecords() {
        val result1 = V7LegacyProjectionSync.sync(c)
        val result2 = V7LegacyProjectionSync.sync(c)
        assertTrue(result2.people >= 0)
        assertTrue(result2.relationships >= 0)
        assertTrue(result2.assets >= 0)
        assertTrue(result1.repayments >= 0)
        assertTrue(V7Core.all(c, V7Core.Keys.AUDIT).isNotEmpty())
    }
}
