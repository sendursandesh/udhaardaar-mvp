package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test

class V7Step6NativeMigrationInstrumentedTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()

    @Test fun registryOnlyMarksImplementedNativeModules() {
        val native = V7MasterVisionRegistry.native().map { it.key }.toSet()
        assertTrue(native.containsAll(setOf("RECORD", "CREDIT", "ASSETS", "LIABILITIES", "REPAYMENT")))
        assertFalse(native.contains("PROTECT"))
        assertFalse(native.contains("GROW"))
        assertFalse(native.contains("LEGAL"))
        assertFalse(native.contains("TRADE"))
        assertFalse(native.contains("MIS"))
    }

    @Test fun nativeRecordAssetAndLiabilityAreV7Owned() {
        val person = V7Records.person(context, "Step6 Native QA", "9876501234")
        val asset = V7Records.asset(context, person.optString("id"), "OTHER", "Native QA asset", 1234.0)
        val liability = V7Records.liability(context, person.optString("id"), "TEST", 5000.0, 4500.0, 10.0)

        assertEquals("Step6 Native QA", V7Core.find(context, V7Core.Keys.PEOPLE, person.optString("id"))?.optString("name"))
        assertEquals(1234.0, V7Core.find(context, V7Core.Keys.ASSETS, asset.optString("id"))?.optDouble("currentValue") ?: 0.0, 0.005)
        assertEquals(4500.0, V7Core.find(context, V7Core.Keys.LIABILITIES, liability.optString("id"))?.optDouble("outstanding") ?: 0.0, 0.005)

        assertFalse(context.getSharedPreferences("v5_store", Context.MODE_PRIVATE)
            .all.keys.any { it.startsWith("v7_") })
    }

    @Test fun nativeCreditAndRepaymentUseCanonicalV7Data() {
        val person = V7Records.person(context, "Step6 Credit QA", "9876501235")
        val relationship = V7Records.relationship(
            context, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 6000.0, 12.0, "PRINCIPAL_PLUS_INTEREST", "Step6"
        )
        assertEquals(6000.0, relationship.optDouble("outstanding"), 0.005)

        V7Records.repayment(context, relationship.optString("id"), 1000.0, 1000.0, 0.0, "UPI", true)
        val unchanged = V7Core.find(context, V7Core.Keys.RELATIONSHIPS, relationship.optString("id"))
        assertEquals(6000.0, unchanged?.optDouble("outstanding") ?: 0.0, 0.005)

        val consent = V7Architecture.LocalConsentService(context).request(
            V7Architecture.ConsentRequest(
                subjectId = person.optString("id"),
                purpose = "REPAYMENT_UPDATE",
                scope = "relationship:" + relationship.optString("id"),
                actorId = V7Core.user(context),
                expiresAt = System.currentTimeMillis() + 60_000L
            )
        )
        assertNotNull(V7Architecture.LocalConsentService(context).grant(consent.id, true))

        V7Records.repayment(context, relationship.optString("id"), 1000.0, 1000.0, 0.0, "UPI", true)
        val updated = V7Core.find(context, V7Core.Keys.RELATIONSHIPS, relationship.optString("id"))
        assertEquals(5000.0, updated?.optDouble("outstanding") ?: 0.0, 0.005)
    }
}
