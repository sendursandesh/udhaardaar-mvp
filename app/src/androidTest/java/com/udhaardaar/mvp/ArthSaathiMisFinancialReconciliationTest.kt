package com.udhaardaar.mvp

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test

/** Regression tests for reconciled outstanding, completed benefits, and cash-collected revenue. */
class ArthSaathiMisFinancialReconciliationTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before fun setUp() {
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.login(context, "9876501188")
    }

    @Test fun unrelatedRepaymentsDoNotReduceCreditOutstandingAndPendingAmountsAreExcluded() {
        val before = ArthSaathiCoreEngine.mis()
        ArthSaathiCoreEngine.createCredit(
            null, "MIS Reconciliation QA", "Personal Credit", 1000.0, 0.0,
            "UPI", "Due on demand"
        )
        // Deliberately orphaned: an unlinked repayment must not reduce any credit balance.
        ArthSaathiCoreEngine.saveModule("REPAYMENT", mapOf("amount" to 500.0))
        ArthSaathiCoreEngine.saveModule("REVENUE", mapOf("f1" to 777.0, "f2" to "PENDING"))
        ArthSaathiCoreEngine.saveModule("REVENUE", mapOf("f1" to 250.0, "f2" to "PAID"))
        ArthSaathiCoreEngine.saveModule("BENEFIT", mapOf("f2" to 888.0, "f3" to "PENDING"))
        ArthSaathiCoreEngine.saveModule("BENEFIT", mapOf("f2" to 333.0, "f3" to "COMPLETED"))

        val after = ArthSaathiCoreEngine.mis()
        assertEquals(1000.0, after.outstanding - before.outstanding, 0.005)
        assertEquals(1027.0, after.revenue - before.revenue, 0.005)
        assertEquals(333.0, after.benefits - before.benefits, 0.005)
    }
}
