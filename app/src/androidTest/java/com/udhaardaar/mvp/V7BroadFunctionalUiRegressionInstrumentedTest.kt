package com.udhaardaar.mvp

import android.app.Activity
import android.content.Context
import android.content.Intent
import org.junit.Assert.assertNotNull
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.*

class V7BroadFunctionalUiRegressionInstrumentedTest {
    private lateinit var context: Context
    private val mobile = "9876504321"

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        V7AccountStore.logout(context)
        ArthSaathiSession.logout(context)
        V7LocalStore(context).remove("v7_accounts", mobile)
        V7AccountStore.create(context, "UI Regression", mobile)
        V7AccountStore.login(context, mobile)
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.logout(context)
        ArthSaathiSession.login(context, mobile)
    }

    @After fun tearDown() {
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", mobile)
    }

    private fun <T : View> find(root: View, predicate: (View) -> Boolean): T? {
        if (predicate(root)) @Suppress("UNCHECKED_CAST") return root as T
        if (root is ViewGroup) for (i in 0 until root.childCount) {
            find<T>(root.getChildAt(i), predicate)?.let { return it }
        }
        return null
    }

    private fun edit(root: View, hint: String): EditText =
        requireNotNull(find<EditText>(root) { it is EditText && it.hint?.toString() == hint })

    private fun button(root: View, text: String): Button =
        requireNotNull(find<Button>(root) { it is Button && it.text?.toString() == text })

    @Test fun canonicalIdentityWorkflowRejectsInvalidIdentityAndPersistsValidIdentity() {
        ArthSaathiDataStore.initialize(context)
        try {
            ArthSaathiCoreEngine.createPerson("Invalid UI Person", "9876504321", pan = "BADPAN")
            fail("Invalid PAN must be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().contains("valid PAN"))
        }
        val created = ArthSaathiCoreEngine.createPerson(
            "UI Person", "9876504321", pan = "ABCDE1234F",
            aadhaar = "234567890123", gstin = "10ABCDE1234F1Z5"
        )
        val stored = requireNotNull(ArthSaathiCoreEngine.find(created.id!!))
        assertEquals("UI Person", stored.optString("name"))
        assertEquals("9876504321", stored.optString("mobile"))
    }

    @Test fun canonicalCreditWorkflowRejectsInvalidRoiAndCreatesConsentGatedCredit() {
        ArthSaathiDataStore.initialize(context)
        try {
            ArthSaathiCoreEngine.createCredit(
                null, "Invalid ROI QA", "Personal Loan / Hand Loan", 10000.0, 120.0,
                "UPI", "Monthly", "", null, null, "9876504322", "", "EMI", 12
            )
            fail("ROI above 100% must be rejected")
        } catch (expected: IllegalArgumentException) {
            assertTrue(expected.message.orEmpty().isNotBlank())
        }
        val created = ArthSaathiCoreEngine.createCredit(
            null, "Credit UI", "Personal Loan / Hand Loan", 10000.0, 12.0,
            "UPI", "Monthly", "", null, null, "9876504322", "", "EMI", 12
        )
        assertEquals("PENDING_CONSENT", requireNotNull(ArthSaathiCoreEngine.find(created.id!!)).optString("status"))
    }

    @Test fun liabilityRecordIsIncludedInMisWithoutChangingCreditOutstanding() {
        ArthSaathiDataStore.initialize(context)
        val before = ArthSaathiCoreEngine.mis()
        ArthSaathiCoreEngine.saveModule("LIABILITY", mapOf(
            "originalAmount" to 100000.0, "outstanding" to 120000.0, "rate" to 15.0
        ))
        val after = ArthSaathiCoreEngine.mis()
        assertEquals(before.liabilities + 120000.0, after.liabilities, 0.005)
        assertEquals(before.outstanding, after.outstanding, 0.005)
    }

    @Test fun canonicalHomeOpensAndShowsApprovedBranding() {
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.login(context, mobile)
        ActivityScenario.launch<ArthSaathiHomeActivity>(Intent(context, ArthSaathiHomeActivity::class.java)).use {
            it.onActivity { a ->
                assertTrue(a.window.decorView.isShown)
                assertNotNull(find<View>(a.window.decorView) { it.tag == "arthsaathi-canonical-logo" })
            }
        }
    }
}
