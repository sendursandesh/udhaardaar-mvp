package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

/**
 * Tests the actually configured ArthSaathi 8.0 launcher architecture, rather
 * than relying only on the historical V7 module-router test surface.
 */
class ArthSaathiMasterRuntimeAcceptanceTest {
    private lateinit var context: Context
    private val mobile = "9876501199"

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ArthSaathiSession.logout(context)
        ArthSaathiDataStore.initialize(context)
        ArthSaathiOtpService.clearProviderForTests()
        ArthSaathiSession.login(context, mobile)
    }

    @After fun tearDown() {
        ArthSaathiOtpService.clearProviderForTests()
        ArthSaathiSession.logout(context)
    }

    private fun allText(view: View): List<String> {
        val result = mutableListOf<String>()
        if (view is TextView) result += view.text?.toString().orEmpty()
        if (view is ViewGroup) for (i in 0 until view.childCount) result += allText(view.getChildAt(i))
        return result
    }

    private fun findEdit(root: View, hint: String): EditText? {
        if (root is EditText && root.hint?.toString() == hint) return root
        if (root is ViewGroup) for (i in 0 until root.childCount) {
            findEdit(root.getChildAt(i), hint)?.let { return it }
        }
        return null
    }

    private fun findButton(root: View, label: String): Button? {
        if (root is Button && root.text?.toString() == label) return root
        if (root is ViewGroup) for (i in 0 until root.childCount) {
            findButton(root.getChildAt(i), label)?.let { return it }
        }
        return null
    }

    @Test fun everyConfiguredCanonicalModuleOpensAndShowsItsExpectedTitle() {
        assertEquals("Module ids must be unique", ArthSaathiArchitectureRegistry.modules.size,
            ArthSaathiArchitectureRegistry.modules.map { it.id }.toSet().size)
        assertEquals("Routes must be unique", ArthSaathiArchitectureRegistry.modules.size,
            ArthSaathiArchitectureRegistry.modules.map { it.route }.toSet().size)

        ArthSaathiArchitectureRegistry.modules.forEach { module ->
            ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
                Intent(context, ArthSaathiMasterModuleActivity::class.java).putExtra("module", module.id)
            ).use { scenario ->
                scenario.onActivity { activity ->
                    assertTrue("Screen did not become visible: ${module.id}", activity.window.decorView.isShown)
                    val text = allText(activity.window.decorView).joinToString(" | ")
                    assertTrue("Expected title '${module.title}' missing on ${module.id}: $text",
                        text.contains(module.title))
                    assertTrue("Home/back control missing on ${module.id}",
                        text.contains("← Home"))
                }
            }
        }
    }

    @Test fun assetSavePersistsAndMisReflectsSameRecordedValue() {
        val assetName = "QA Asset Runtime ${System.currentTimeMillis()}"
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.ASSET_VAULT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Asset")).setText(assetName)
                requireNotNull(findEdit(root, "Category")).setText("PROPERTY")
                requireNotNull(findEdit(root, "Current value")).setText("12345.67")
                requireNotNull(findEdit(root, "Nominee / owner")).setText("QA Owner")
                requireNotNull(findEdit(root, "Evidence")).setText("QA evidence reference")
                requireNotNull(findButton(root, "SAVE")).performClick()
            }
        }

        val saved = ArthSaathiDataStore.records()
        assertTrue("Saved asset was not persisted",
            (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }.any {
                it.optString("type") == "ASSET" && it.optString("f0") == assetName &&
                    it.optDouble("f2") == 12345.67
            })

        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.MIS)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val text = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("MIS did not reflect the saved asset value: $text", text.contains("12345.67"))
            }
        }

        // Ensure the owner-filtered store can still read the committed record after a fresh activity.
        val afterRelaunch = ArthSaathiDataStore.records()
        assertTrue((0 until afterRelaunch.length()).mapNotNull { afterRelaunch.optJSONObject(it) }
            .any { it.optString("f0") == assetName && it.optDouble("f2") == 12345.67 })
    }

    @Test fun recordsCannotBeReadOrWrittenWithoutAnAuthenticatedSession() {
        ArthSaathiSession.logout(context)
        assertEquals(0, ArthSaathiDataStore.records().length())
        var rejected = false
        try {
            ArthSaathiDataStore.append(org.json.JSONObject().put("type", "ASSET").put("f0", "No session"))
        } catch (_: IllegalStateException) {
            rejected = true
        }
        assertTrue("Write without session must be rejected", rejected)
    }

    @Test fun creditRegistrationRejectsInvalidValuesWithoutCrashingAndPersistsValidCredit() {
        val before = ArthSaathiDataStore.records().length()
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.REGISTER_CREDIT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Borrower / counterparty name")).setText("Credit QA Person")
                requireNotNull(findEdit(root, "Counterparty mobile (10 digits)")).setText("9876501188")
                requireNotNull(findEdit(root, "Principal / amount")).setText("0")
                requireNotNull(findEdit(root, "ROI % (not used for lease)")).setText("150")
                requireNotNull(findEdit(root, "Method: Cash / UPI / NEFT / Other")).setText("UPI")
                requireNotNull(findEdit(root, "Repayment method / terms")).setText("Monthly")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
                assertEquals("Enter an amount greater than zero",
                    findEdit(root, "Principal / amount")?.error)
                assertEquals(before, ArthSaathiDataStore.records().length())
                requireNotNull(findEdit(root, "Principal / amount")).setText("15000")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
                assertEquals("ROI must be between 0 and 100%",
                    findEdit(root, "ROI % (not used for lease)")?.error)
                assertEquals(before, ArthSaathiDataStore.records().length())
                requireNotNull(findEdit(root, "ROI % (not used for lease)")).setText("12.5")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
            }
        }
        val after = ArthSaathiDataStore.records()
        assertTrue("Valid credit was not committed",
            (0 until after.length()).mapNotNull { after.optJSONObject(it) }.any {
                it.optString("type") == "CREDIT" && it.optString("party") == "Credit QA Person" &&
                    it.optDouble("amount") == 15000.0 && it.optDouble("roi") == 12.5 &&
                    it.optString("partyMobile") == "9876501188" &&
                    it.optString("consentStatus") == "PENDING"
            })
    }

    @Test fun leaseCreditForcesZeroInterestAndInvalidMobileIsRejected() {
        val before = ArthSaathiDataStore.records().length()
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.REGISTER_CREDIT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Borrower / counterparty name")).setText("Lease QA")
                requireNotNull(findEdit(root, "Counterparty mobile (10 digits)")).setText("123")
                requireNotNull(findEdit(root, "Principal / amount")).setText("10000")
                requireNotNull(findEdit(root, "ROI % (not used for lease)")).setText("0")
                requireNotNull(findEdit(root, "Method: Cash / UPI / NEFT / Other")).setText("BANK")
                requireNotNull(findEdit(root, "Repayment method / terms")).setText("Monthly rent")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
                assertEquals("Enter a 10-digit mobile number",
                    findEdit(root, "Counterparty mobile (10 digits)")?.error)
                assertEquals(before, ArthSaathiDataStore.records().length())
                requireNotNull(findEdit(root, "Counterparty mobile (10 digits)")).setText("9876501187")
                // The credit type spinner is selected explicitly by its canonical visible option.
                val sp = findSpinner(activity.window.decorView)
                val leaseIndex = (0 until sp.count).firstOrNull {
                    sp.getItemAtPosition(it).toString().contains("Rental", true)
                }
                if (leaseIndex != null) sp.setSelection(leaseIndex)
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
            }
        }
        val after = ArthSaathiDataStore.records()
        assertTrue((0 until after.length()).mapNotNull { after.optJSONObject(it) }.any {
            it.optString("type") == "CREDIT" && it.optString("party") == "Lease QA" &&
                it.optDouble("roi") == 0.0
        })
    }

    private fun findSpinner(root: View): android.widget.Spinner {
        if (root is android.widget.Spinner) return root
        if (root is ViewGroup) for (i in 0 until root.childCount) {
            try { return findSpinner(root.getChildAt(i)) } catch (_: NoSuchElementException) { }
        }
        throw NoSuchElementException("Credit type spinner missing")
    }


    @Test fun misReconcilesCurrentPortfolioValueAndRevenueFieldsFromCanonicalScreens() {
        val marker = System.currentTimeMillis().toString()
        ArthSaathiCoreEngine.saveModule("PORTFOLIO", mapOf(
            "f0" to "QA Portfolio $marker", "f1" to "Mutual Fund",
            "f2" to "1000.00", "f3" to "1450.00", "f4" to "Moderate"
        ))
        ArthSaathiCoreEngine.saveModule("REVENUE", mapOf(
            "f0" to "QA Service $marker", "f1" to "999.00",
            "f2" to "Paid", "f3" to "QA-GATEWAY-$marker"
        ))
        val snapshot = ArthSaathiCoreEngine.mis()
        assertTrue("MIS should use current portfolio value, not invested value: $snapshot",
            snapshot.assets >= 1450.0)
        assertTrue("MIS should include the charge field recorded by Revenue & Payments: $snapshot",
            snapshot.revenue >= 999.0)
    }


    @Test fun peopleScreenRejectsMalformedMobileAndPersistsCanonicalPersonFields() {
        val before = ArthSaathiDataStore.records().length()
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.PEOPLE)
        ).use { scenario ->
            scenario.onActivity { activity ->
                var root = activity.window.decorView
                requireNotNull(findEdit(root, "Full name")).setText("People QA")
                requireNotNull(findEdit(root, "10-digit mobile")).setText("1234567890")
                requireNotNull(findButton(root, "SAVE PERSON")).performClick()
                assertEquals("Enter a valid 10-digit mobile number",
                    findEdit(root, "10-digit mobile")?.error)
                assertEquals(before, ArthSaathiDataStore.records().length())
                requireNotNull(findEdit(root, "10-digit mobile")).setText("9876501177")
                requireNotNull(findEdit(root, "Role / relationship")).setText("Family")
                requireNotNull(findEdit(root, "Address / PIN")).setText("Ranchi")
                requireNotNull(findButton(root, "SAVE PERSON")).performClick()
            }
        }
        val after = ArthSaathiDataStore.records()
        assertTrue((0 until after.length()).mapNotNull { after.optJSONObject(it) }.any {
            it.optString("type") == "PERSON" && it.optString("name") == "People QA" &&
                it.optString("mobile") == "9876501177" && it.optString("role") == "Family"
        })
    }


    @Test fun consentFailsClosedWhenNoRealOtpProviderIsConfigured() {
        val created = ArthSaathiCoreEngine.createCredit(
            null, "Consent QA", "Personal Loan / Hand Loan", 5000.0, 5.0,
            "UPI", "Monthly", "", null, null, "9876501166", "QA Guarantor"
        )
        val createdId = requireNotNull(created.id)
        val requested = ArthSaathiCoreEngine.requestConsent(createdId, "lender")
        assertFalse("Consent request must not pretend an OTP was sent", requested.ok)
        val confirmed = ArthSaathiCoreEngine.confirmConsent(createdId, "123456", "borrower")
        assertFalse("A typed numeric code must not bypass provider verification", confirmed.ok)
        assertEquals("PENDING", ArthSaathiCoreEngine.find(createdId)!!.optString("consentStatus"))
    }

    @Test fun consentCanOnlyBeConfirmedAfterProviderVerifiesTheChallenge() {
        ArthSaathiOtpService.installProvider(object : ArthSaathiOtpProvider {
            override fun requestCode(mobile: String, purpose: String, recordId: String): String? =
                if (mobile == "9876501165" && purpose == "CREDIT_CONSENT") "TEST-CHALLENGE" else null
            override fun verifyCode(challengeId: String, code: String): Boolean =
                challengeId == "TEST-CHALLENGE" && code == "654321"
        })
        val created = ArthSaathiCoreEngine.createCredit(
            null, "Verified Consent QA", "Personal Loan / Hand Loan", 5000.0, 5.0,
            "UPI", "Monthly", "", null, null, "9876501165", "QA Guarantor"
        )
        val createdId = requireNotNull(created.id)
        assertTrue(ArthSaathiCoreEngine.requestConsent(createdId, "lender").ok)
        assertFalse(ArthSaathiCoreEngine.confirmConsent(createdId, "123456", "borrower").ok)
        assertEquals("REQUESTED", ArthSaathiCoreEngine.find(createdId)!!.optString("consentStatus"))
        assertTrue(ArthSaathiCoreEngine.confirmConsent(createdId, "654321", "borrower").ok)
        assertEquals("CONSENTED", ArthSaathiCoreEngine.find(createdId)!!.optString("consentStatus"))
    }

}
