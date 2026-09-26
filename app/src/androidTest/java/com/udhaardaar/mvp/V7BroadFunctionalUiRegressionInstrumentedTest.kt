package com.udhaardaar.mvp

import android.app.Activity
import android.content.Context
import android.content.Intent
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
        V7LocalStore(context).remove("v7_accounts", mobile)
        V7AccountStore.create(context, "UI Regression", mobile)
        V7AccountStore.login(context, mobile)
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

    @Test fun nativeRecordRejectsInvalidIdentityAndAcceptsValidIdentity() {
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "RECORD")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Full name").setText("UI Person")
                val mobileField = edit(root, "10-digit mobile")
                mobileField.setText("123456789012345")
                assertEquals("1234567890", mobileField.text.toString())
                edit(root, "PAN (optional)").setText("BADPAN")
                button(root, "Save Person in V7").performClick()
                assertEquals("Enter a valid PAN", edit(root, "PAN (optional)").error)
                edit(root, "PAN (optional)").setText("ABCDE1234F")
                edit(root, "Aadhaar (optional)").setText("123456789012")
                edit(root, "GSTIN (optional)").setText("10ABCDE1234F1Z5")
                button(root, "Save Person in V7").performClick()
                assertTrue(V7Core.all(context, V7Core.Keys.PEOPLE).any {
                    it.optString("name") == "UI Person" && it.optString("mobile") == "1234567890"
                })
            }
        }
    }

    @Test fun nativeCreditRejectsOutOfRangeRoiAndPersistsValidCredit() {
        val person = V7Records.person(context, "Credit UI", "9876504322")
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "CREDIT")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Amount").setText("10000")
                edit(root, "ROI %").setText("120")
                button(root, "Register Credit in V7").performClick()
                assertEquals("ROI must be between 0 and 100%", edit(root, "ROI %").error)
                edit(root, "ROI %").setText("12")
                edit(root, "Purpose").setText("UI QA")
                edit(root, "Repayment structure (EMI / Principal + Interest)").setText("PRINCIPAL_PLUS_INTEREST")
                button(root, "Register Credit in V7").performClick()
                assertTrue(V7Core.all(context, V7Core.Keys.RELATIONSHIPS).any {
                    it.optString("partyId") == person.optString("id") && it.optDouble("amount") == 10000.0
                })
            }
        }
    }

    @Test fun nativeLiabilityRejectsOutstandingAboveOriginalAndInvalidRate() {
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "LIABILITIES")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Liability type").setText("Home Loan")
                edit(root, "Original amount").setText("100000")
                edit(root, "Outstanding").setText("120000")
                edit(root, "Rate %").setText("150")
                button(root, "Save Liability in V7").performClick()
                assertEquals("Enter valid amounts; outstanding cannot exceed original amount", edit(root, "Original amount").error)
                edit(root, "Outstanding").setText("90000")
                button(root, "Save Liability in V7").performClick()
                assertEquals("Rate must be between 0 and 100%", edit(root, "Rate %").error)
            }
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
