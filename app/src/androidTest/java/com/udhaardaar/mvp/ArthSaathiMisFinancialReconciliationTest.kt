package com.udhaardaar.mvp

import androidx.test.core.app.ApplicationProvider
import android.content.Context
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/** Regression tests for reconciled outstanding, completed benefits, and cash-collected revenue. */
class ArthSaathiMisFinancialReconciliationTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before fun setUp() {
        ArthSaathiOtpService.clearProviderForTests()
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.login(context, "9876501188")
    }

    @After fun tearDown() {
        ArthSaathiOtpService.clearProviderForTests()
        ArthSaathiSession.logout(context)
    }

    @Test fun unrelatedRepaymentsDoNotReduceCreditOutstandingAndPendingAmountsAreExcluded() {
        val baseline = ArthSaathiCoreEngine.mis()
        val created = ArthSaathiCoreEngine.createCredit(
            null, "MIS Reconciliation QA", "Personal Loan / Hand Loan", 1000.0, 0.0,
            "UPI", "Monthly", "", null, null, "9876501189", "QA Guarantor",
            "Principal + Interest", 12
        )
        val id = requireNotNull(created.id)
        assertEquals("A new credit remains a draft until verified consent", "PENDING_CONSENT",
            ArthSaathiCoreEngine.find(id)!!.optString("status"))
        assertEquals("Pending-consent drafts must not affect registered-credit MIS",
            baseline.outstanding, ArthSaathiCoreEngine.mis().outstanding, 0.005)

        ArthSaathiOtpService.installProvider(object : ArthSaathiOtpProvider {
            override fun requestCode(mobile: String, purpose: String, recordId: String): String? =
                if (mobile == "9876501189" && purpose == "CREDIT_CONSENT" && recordId == id)
                    "MIS-RECONCILIATION-CHALLENGE" else null
            override fun verifyCode(challengeId: String, code: String): Boolean =
                challengeId == "MIS-RECONCILIATION-CHALLENGE" && code == "654321"
        })
        assertTrue(ArthSaathiCoreEngine.requestConsent(id, "lender").ok)
        assertTrue(ArthSaathiCoreEngine.confirmConsent(id, "654321", "borrower").ok)
        val before = ArthSaathiCoreEngine.mis()
        assertEquals("Consented credit must enter registered-credit MIS", 1000.0,
            before.outstanding - baseline.outstanding, 0.005)

        // Deliberately orphaned: an unlinked repayment must not reduce any credit balance.
        ArthSaathiCoreEngine.saveModule("REPAYMENT", mapOf("amount" to 500.0))
        ArthSaathiCoreEngine.saveModule("REVENUE", mapOf("f1" to 777.0, "f2" to "PENDING"))
        ArthSaathiCoreEngine.saveModule("REVENUE", mapOf("f1" to 250.0, "f2" to "PAID"))
        ArthSaathiCoreEngine.saveModule("REVENUE", mapOf("f1" to 40.0, "f2" to "REFUNDED"))
        ArthSaathiCoreEngine.saveModule("CHARGECHECK", mapOf("f1" to 100.0, "f2" to 125.0, "f3" to 25.0))
        ArthSaathiCoreEngine.saveModule("BENEFIT", mapOf("f2" to 888.0, "f3" to "PENDING"))
        ArthSaathiCoreEngine.saveModule("BENEFIT", mapOf("f2" to 333.0, "f3" to "COMPLETED"))

        val after = ArthSaathiCoreEngine.mis()
        assertEquals("Unlinked repayments must not reduce credit outstanding",
            before.outstanding, after.outstanding, 0.005)
        assertEquals(210.0, after.revenue - before.revenue, 0.005)
        assertEquals(125.0, after.actualCharges - before.actualCharges, 0.005)
        assertEquals(25.0, after.chargeVariance - before.chargeVariance, 0.005)
        assertEquals(333.0, after.benefits - before.benefits, 0.005)
    }
}
