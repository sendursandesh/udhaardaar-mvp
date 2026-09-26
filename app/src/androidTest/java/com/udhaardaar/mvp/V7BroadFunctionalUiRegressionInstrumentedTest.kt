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
    @Test fun nativeIdentityRejectsBadAadhaarAndGstinThenAcceptsBoth() {
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "RECORD")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Full name").setText("Identity QA")
                edit(root, "10-digit mobile").setText("9876504323")
                edit(root, "Aadhaar (optional)").setText("1234")
                edit(root, "GSTIN (optional)").setText("BAD")
                button(root, "Save Person in V7").performClick()
                assertEquals("Enter a valid 12-digit Aadhaar", edit(root, "Aadhaar (optional)").error)
                edit(root, "Aadhaar (optional)").setText("123456789012")
                button(root, "Save Person in V7").performClick()
                assertEquals("Enter a valid GSTIN", edit(root, "GSTIN (optional)").error)
                edit(root, "GSTIN (optional)").setText("10ABCDE1234F1Z5")
                button(root, "Save Person in V7").performClick()
                assertTrue(V7Core.all(context, V7Core.Keys.PEOPLE).any { it.optString("mobile") == "9876504323" })
            }
        }
    }

    @Test fun nativeCreditNormalizesRepaymentStructureAndRejectsExcessPrecision() {
        val person = V7Records.person(context, "Structure QA", "9876504324")
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "CREDIT")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Amount").setText("25000")
                edit(root, "ROI % (e.g. 12.00)").setText("12.345")
                button(root, "Register Credit in V7").performClick()
                assertEquals("ROI allows maximum 2 decimal places", edit(root, "ROI % (e.g. 12.00)").error)
                edit(root, "ROI % (e.g. 12.00)").setText("12.50")
                edit(root, "Repayment structure (EMI / Principal + Interest)").setText("something unexpected")
                button(root, "Register Credit in V7").performClick()
                assertTrue(V7Core.all(context, V7Core.Keys.RELATIONSHIPS).any {
                    it.optString("partyId") == person.optString("id") &&
                    it.optString("repaymentMethod") == "PRINCIPAL_PLUS_INTEREST"
                })
            }
        }
    }

    @Test fun nativeRepaymentRejectsInvalidMethodAndOverpaymentBeforeMutation() {
        val person = V7Records.person(context, "Repayment UI", "9876504325")
        val rel = V7Records.relationship(context, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 5000.0, 12.0, "PRINCIPAL_PLUS_INTEREST", "UI")
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "REPAYMENT")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Principal amount").setText("6000")
                edit(root, "Method (CASH / UPI / NEFT)").setText("CHEQUE")
                button(root, "Record Repayment").performClick()
                assertEquals("Use CASH, UPI or NEFT", edit(root, "Method (CASH / UPI / NEFT)").error)
                edit(root, "Method (CASH / UPI / NEFT)").setText("UPI")
                edit(root, "Principal amount").setText("6000")
                button(root, "Record Repayment").performClick()
                assertEquals("Repayment cannot exceed outstanding", edit(root, "Principal amount").error)
                assertEquals(5000.0, V7Core.find(context, V7Core.Keys.RELATIONSHIPS, rel.optString("id"))!!.optDouble("outstanding"), 0.005)
            }
        }
    }

    @Test fun nativeAssetAndLiabilityRejectNegativeValues() {
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "ASSETS")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Asset type (property / vehicle / deposit / other)").setText("Property")
                edit(root, "Current value").setText("-1")
                button(root, "Save Asset in V7").performClick()
                assertEquals("Enter a valid value", edit(root, "Current value").error)
            }
        }
        ActivityScenario.launch<Activity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", "LIABILITIES")
        ).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window.decorView
                edit(root, "Liability type").setText("Loan")
                edit(root, "Original amount").setText("-1")
                edit(root, "Outstanding").setText("0")
                button(root, "Save Liability in V7").performClick()
                assertEquals("Enter valid amounts; outstanding cannot exceed original amount", edit(root, "Original amount").error)
            }
        }
    }

    @Test fun v7HomeRendersAfterSessionAndNativeRoutesDoNotExposeLegacyActivitiesDirectly() {
        ActivityScenario.launch<V7HomeActivity>(Intent(context, V7HomeActivity::class.java)).use { scenario ->
            scenario.onActivity { a ->
                assertTrue(a.window.decorView.findViewsWithText("Financial Snapshot").isNotEmpty())
                assertTrue(V7MasterVisionRegistry.find("CREDIT")?.ownership == V7MasterVisionRegistry.Ownership.NATIVE)
                assertTrue(V7MasterVisionRegistry.find("RECORD")?.ownership == V7MasterVisionRegistry.Ownership.NATIVE)
            }
        }
    }

    private fun android.view.View.findViewsWithText(value: String): List<android.view.View> {
        val out = mutableListOf<android.view.View>()
        fun walk(v: android.view.View) {
            if (v is android.widget.TextView && v.text?.toString() == value) out += v
            if (v is ViewGroup) for (i in 0 until v.childCount) walk(v.getChildAt(i))
        }
        walk(this)
        return out
    }

}
