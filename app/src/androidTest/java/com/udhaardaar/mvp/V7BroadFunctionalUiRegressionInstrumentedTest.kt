package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.*
import androidx.test.espresso.assertion.ViewAssertions.*
import androidx.test.espresso.matcher.ViewMatchers.*
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.Assert.assertTrue

class V7BroadFunctionalUiRegressionInstrumentedTest {
    private lateinit var context: Context
    private val mobile = "9876504321"

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", mobile)
        V7AccountStore.create(context, "UI Regression", mobile)
        V7AccountStore.login(context, mobile)
    }

    @After fun tearDown() {
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", mobile)
    }

    @Test fun nativeRecordRejectsInvalidIdentityAndAcceptsValidIdentity() {
        ActivityScenario.launch<android.app.Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "RECORD")
        ).use {
            onView(withHint("Full name")).perform(typeText("UI Person"))
            onView(withHint("10-digit mobile")).perform(typeText("123456789012345"))
            onView(withHint("10-digit mobile")).check(matches(withText("1234567890")))
            onView(withHint("PAN (optional)")).perform(typeText("BADPAN"))
            onView(withText("Save Person in V7")).perform(click())
            onView(withHint("PAN (optional)")).check(matches(hasErrorText("Enter a valid PAN")))
            onView(withHint("PAN (optional)")).perform(clearText(), typeText("ABCDE1234F"))
            onView(withHint("Aadhaar (optional)")).perform(typeText("123456789012"))
            onView(withHint("GSTIN (optional)")).perform(typeText("10ABCDE1234F1Z5"))
            onView(withText("Save Person in V7")).perform(click())
            onView(withText("UI Person  •  1234567890")).check(matches(isDisplayed()))
        }
    }

    @Test fun nativeCreditRejectsOutOfRangeRoiAndPersistsValidCredit() {
        val person = V7Records.person(context, "Credit UI", "9876504322")
        ActivityScenario.launch<android.app.Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "CREDIT")
        ).use {
            onView(withHint("Amount")).perform(typeText("10000"))
            onView(withHint("ROI %")).perform(typeText("120"))
            onView(withText("Register Credit in V7")).perform(click())
            onView(withHint("ROI %")).check(matches(hasErrorText("ROI must be between 0 and 100%")))
            onView(withHint("ROI %")).perform(clearText(), typeText("12"))
            onView(withHint("Purpose")).perform(typeText("UI QA"))
            onView(withHint("Repayment structure (EMI / Principal + Interest)")).perform(typeText("PRINCIPAL_PLUS_INTEREST"))
            onView(withText("Register Credit in V7")).perform(click())
            onView(withText("Credit relationship saved in V7.")).check(matches(isDisplayed()))
            assertTrue(V7Core.all(context, V7Core.Keys.RELATIONSHIPS).any {
                it.optString("partyId") == person.optString("id") && it.optDouble("amount") == 10000.0
            })
        }
    }

    @Test fun nativeLiabilityRejectsOutstandingAboveOriginalAndInvalidRate() {
        ActivityScenario.launch<android.app.Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "LIABILITIES")
        ).use {
            onView(withHint("Liability type")).perform(typeText("Home Loan"))
            onView(withHint("Original amount")).perform(typeText("100000"))
            onView(withHint("Outstanding")).perform(typeText("120000"))
            onView(withHint("Rate %")).perform(typeText("150"))
            onView(withText("Save Liability in V7")).perform(click())
            onView(withHint("Original amount")).check(matches(hasErrorText("Enter valid amounts; outstanding cannot exceed original amount")))
            onView(withHint("Outstanding")).perform(clearText(), typeText("90000"))
            onView(withText("Save Liability in V7")).perform(click())
            onView(withHint("Rate %")).check(matches(hasErrorText("Rate must be between 0 and 100%")))
        }
    }

    @Test fun secureWindowFlagIsAppliedAcrossV7Activities() {
        ActivityScenario.launch<V7HomeActivity>(
            Intent(context, V7HomeActivity::class.java)
        ).use {
            it.onActivity { a ->
                val secure = a.window.attributes.flags and android.view.WindowManager.LayoutParams.FLAG_SECURE
                assertTrue(secure != 0)
            }
        }
    }
}
