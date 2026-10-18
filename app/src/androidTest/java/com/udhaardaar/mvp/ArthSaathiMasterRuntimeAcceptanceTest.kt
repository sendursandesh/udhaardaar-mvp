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
import java.io.ByteArrayInputStream
import java.io.File
import java.security.MessageDigest

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

    @Test fun creditIntelligenceHidesFinancialHistoryUntilConsentIsVerified() {
        val party = "Consent gate QA " + System.currentTimeMillis()
        ArthSaathiCoreEngine.saveModule("CREDIT", mapOf(
            "party" to party, "nature" to "Personal Loan", "amount" to 24680.13,
            "totalPayable" to 26000.0, "paid" to 100.0,
            "status" to "ACTIVE", "consentStatus" to "PENDING"
        ))
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.CREDIT_INTELLIGENCE)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val visible = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Counterparty should be identifiable without disclosing financial history", visible.contains(party))
                assertTrue("Pending consent must explain why details are hidden", visible.contains("Protected details hidden"))
                assertFalse("Unconsented principal must not be displayed", visible.contains("24680.13"))
                assertFalse("Unconsented payable must not be displayed", visible.contains("26000.00"))
                assertTrue("Screen must route the user to the canonical consent workflow",
                    visible.contains("OPEN SECURITY & CONSENT"))
            }
        }
    }

    @Test fun creditIntelligenceShowsFinancialSummaryAfterVerifiedConsentState() {
        val party = "Consent granted QA " + System.currentTimeMillis()
        ArthSaathiCoreEngine.saveModule("CREDIT", mapOf(
            "party" to party, "nature" to "Personal Loan", "amount" to 24680.13,
            "totalPayable" to 26000.0, "paid" to 100.0,
            "status" to "ACTIVE", "consentStatus" to "CONSENTED"
        ))
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.CREDIT_INTELLIGENCE)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val visible = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Verified consent should unlock the registered financial summary", visible.contains("Registered amount: ₹24680.13"))
                assertTrue("Verified consent should show the calculated outstanding", visible.contains("Outstanding: ₹25900.00"))
            }
        }
    }

    @Test fun documentVaultEncryptsFilesAndRestoresOriginalBytesWithIntegrityHash() {
        val original = "ArthSaathi secure document acceptance test".toByteArray(Charsets.UTF_8)
        val stored = ArthSaathiDocumentStorage.store(
            context, ByteArrayInputStream(original), "acceptance.txt"
        )
        assertEquals("SHA-256 must be represented by 64 hexadecimal characters", 64, stored.sha256.length)
        val encrypted = File(context.filesDir, "arthsaathi_documents/${stored.fileName}")
        assertTrue("Encrypted document must persist in app-private storage", encrypted.isFile)
        assertFalse("Stored file must not contain plaintext source bytes",
            encrypted.readBytes().contentEquals(original))
        val preview = ArthSaathiDocumentStorage.decryptToCache(context, stored.fileName, stored.extension)
        assertArrayEquals("Decrypted preview must match the original file bytes", original, preview.readBytes())
        val expectedHash = MessageDigest.getInstance("SHA-256").digest(original)
            .joinToString("") { "%02x".format(it.toInt() and 0xff) }
        assertEquals("Integrity hash must match the original document", expectedHash, stored.sha256)

        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.DOCUMENT_VAULT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                assertNotNull("Document upload action is missing",
                    findButton(activity.window.decorView, "CHOOSE & STORE DOCUMENT"))
            }
        }
    }

    @Test fun homeSnapshotReflectsCanonicalAssetRecords() {
        val before = ArthSaathiCoreEngine.mis().assets
        val assetName = "Home snapshot QA " + System.currentTimeMillis()
        ArthSaathiCoreEngine.saveModule("ASSET", mapOf("f0" to assetName, "f2" to 2345.67, "value" to 2345.67))
        val expected = before + 2345.67
        ActivityScenario.launch<ArthSaathiHomeActivity>(
            Intent(context, ArthSaathiHomeActivity::class.java)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val visible = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Home snapshot must show recorded assets; text=$visible",
                    visible.contains(String.format(java.util.Locale.US, "%,.2f", expected)))
                assertTrue("Home should show the approved Plan Today dashboard banner", visible.contains("Plan Today"))
                assertTrue("Home should provide the canonical quick actions", visible.contains("Register Credit"))
            }
        }
    }

    @Test fun advocateDirectorySearchesSavedContactsByDomainAndCity() {
        val advocateName = "Advocate QA " + System.currentTimeMillis()
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.ADVOCATES)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Advocate / firm name")).setText(advocateName)
                requireNotNull(findEdit(root, "Legal domain / expertise")).setText("Civil Litigation")
                requireNotNull(findEdit(root, "City / locality")).setText("Ranchi")
                requireNotNull(findEdit(root, "Phone / email contact")).setText("9876501144")
                requireNotNull(findButton(root, "SAVE ADVOCATE CONTACT")).performClick()
                val search = requireNotNull(findEdit(activity.window.decorView, "Search name / legal domain / city"))
                search.setText("Ranchi")
                val visible = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Directory must filter saved contacts by city", visible.contains(advocateName))
                assertTrue("Directory result should include legal domain", visible.contains("Civil Litigation"))
                assertTrue("Unverified contacts must not be represented as independently verified", visible.contains("Unverified"))
            }
        }
    }

    @Test fun advisorUsesExplainableAlertsFromCanonicalRecords() {
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.AI_ADVISOR)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val visible = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Advisor must show recorded financial position", visible.contains("Recorded financial position"))
                assertTrue("Advisor must disclose that suggestions do not execute transactions", visible.contains("no money movement"))
            }
        }
    }

    @Test fun claimsCanAdvanceThroughAuditedLifecycleStages() {
        val claimName = "Claim lifecycle QA " + System.currentTimeMillis()
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.CLAIMS)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Claim / asset / benefit")).setText(claimName)
                requireNotNull(findEdit(root, "Claimant / nominee / authorised person")).setText("QA Claimant")
                requireNotNull(findEdit(root, "Institution / source")).setText("QA Institution")
                requireNotNull(findEdit(root, "Evidence / document references")).setText("evidence-ref-001")
                requireNotNull(findButton(root, "SAVE CLAIM / UPDATE RECORD")).performClick()
                assertTrue(allText(activity.window.decorView).joinToString(" | ").contains("IDENTIFIED"))
                requireNotNull(findButton(activity.window.decorView, "ADVANCE TO DOCUMENTS PENDING")).performClick()
                assertTrue("Claim stage should update in the shared record", allText(activity.window.decorView).joinToString(" | ").contains("DOCUMENTS_PENDING"))
            }
        }
        val saved = ArthSaathiDataStore.records()
        assertTrue((0 until saved.length()).mapNotNull { saved.optJSONObject(it) }.any {
            it.optString("type") == "CLAIM" && it.optString("f0") == claimName && it.optString("f3") == "DOCUMENTS_PENDING"
        })
    }

    @Test fun legacyPlanningCreatesExplicitDraftAndEncryptedSummaryDocument() {
        val assetName = "Legacy QA asset " + System.currentTimeMillis()
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.WILL_LEGACY)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Asset / property / account")).setText(assetName)
                requireNotNull(findEdit(root, "Intended beneficiary / nominee")).setText("QA Beneficiary")
                requireNotNull(findEdit(root, "Proposed executor / trusted person")).setText("QA Executor")
                requireNotNull(findEdit(root, "Instructions / wishes (optional)")).setText("Review with counsel")
                requireNotNull(findButton(root, "SAVE DRAFT & CREATE SUMMARY DOCUMENT")).performClick()
                val visible = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Legacy screen must disclose draft-only status", visible.contains("DRAFT_REQUIRES_LEGAL_REVIEW"))
                assertTrue("Saved plan must appear on the same screen", visible.contains(assetName))
            }
        }
        val saved = ArthSaathiDataStore.records()
        val legacy = (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }
            .first { it.optString("type") == "LEGACY" && it.optString("f0") == assetName }
        val document = (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }
            .first { it.optString("type") == "DOCUMENT" && it.optString("relatedId") == legacy.optString("id") }
        assertEquals("ANDROID_KEYSTORE_AES_GCM", document.optString("storage"))
        assertTrue(File(context.filesDir, "arthsaathi_documents/" + document.optString("f3")).isFile)
    }

    @Test fun financialRecordsAreEncryptedAtRestAndRemainReadableAfterReinitialization() {
        val marker = "PLAINTEXT_MARKER_" + System.currentTimeMillis()
        ArthSaathiCoreEngine.saveModule("ASSET", mapOf("f0" to marker, "value" to 456.78, "f2" to 456.78))
        val prefs = context.getSharedPreferences("arthsaathi_master_data", Context.MODE_PRIVATE)
        val encrypted = prefs.getString("records_encrypted_v1", null)
        assertTrue("Encrypted record payload must exist", !encrypted.isNullOrBlank())
        assertFalse("Financial record contents must not be visible in plaintext preferences", encrypted!!.contains(marker))
        assertEquals("Legacy plaintext preference must be removed after migration/write", null, prefs.getString("records", null))
        ArthSaathiDataStore.initialize(context)
        assertTrue("Encrypted records must decrypt after store reinitialization",
            (0 until ArthSaathiDataStore.records().length()).mapNotNull { ArthSaathiDataStore.records().optJSONObject(it) }
                .any { it.optString("f0") == marker })
    }

    @Test fun emiScheduleReconcilesPrincipalInterestAndFinalPaymentOnAndroidRuntime() {
        val schedule = ArthSaathiCoreEngine.schedule(100000.0, 12.0, 12)
        assertEquals(12, schedule.length())
        assertEquals(0.0, schedule.getJSONObject(11).optDouble("balance"), 0.001)
        val paid = (0 until schedule.length()).sumOf { schedule.getJSONObject(it).optDouble("emi") }
        val principal = (0 until schedule.length()).sumOf { schedule.getJSONObject(it).optDouble("principal") }
        val interest = (0 until schedule.length()).sumOf { schedule.getJSONObject(it).optDouble("interest") }
        assertEquals(100000.0, principal, 0.02)
        assertEquals(paid, principal + interest, 0.03)
        assertTrue(schedule.getJSONObject(11).optDouble("emi") > 0.0)
    }

    @Test fun installedAppLauncherUsesCanonicalSecureArthSaathiEntryPoint() {
        val launch = context.packageManager.getLaunchIntentForPackage(context.packageName)
        assertNotNull("App must expose one launcher entry point", launch)
        val resolved = context.packageManager.resolveActivity(launch!!, android.content.pm.PackageManager.MATCH_DEFAULT_ONLY)
        assertEquals("Canonical secure login must be the installed launcher",
            ArthSaathiLoginActivity::class.java.name, resolved?.activityInfo?.name)
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
                    assertNotNull("Frozen logo missing on canonical module ${module.id}",
                        activity.window.decorView.findViewWithTag<android.widget.ImageView>("arthsaathi-canonical-logo"))
                }
            }
        }
    }

    @Test fun canonicalShellShowsFrozenBrandAndGuidedKeyboardSafeCreditForm() {
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.REGISTER_CREDIT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                val image = root.findViewWithTag<android.widget.ImageView>("arthsaathi-canonical-logo")
                assertNotNull("Frozen ArthSaathi logo must appear on canonical screens", image)
                assertEquals("ArthSaathi logo", image!!.contentDescription)
                val visibleText = allText(root).joinToString(" | ")
                assertTrue("Frozen tagline missing", visibleText.contains("Navigate Your Financial Journey"))
                assertTrue("User guidance missing", visibleText.contains("use Next to move forward"))
                val name = requireNotNull(findEdit(root, "Borrower / counterparty name"))
                val mobileField = requireNotNull(findEdit(root, "Counterparty mobile (10 digits)"))
                assertEquals("All form fields should use next-field keyboard navigation",
                    android.view.inputmethod.EditorInfo.IME_ACTION_NEXT, name.imeOptions and android.view.inputmethod.EditorInfo.IME_MASK_ACTION)
                assertEquals("Mobile must remain capped at 10 characters", 10,
                    (mobileField.filters.filterIsInstance<android.text.InputFilter.LengthFilter>().firstOrNull()?.max ?: -1))
                name.requestFocus()
                name.onEditorAction(android.view.inputmethod.EditorInfo.IME_ACTION_NEXT)
                assertTrue("Next action should move focus to the next field", mobileField.hasFocus())
            }
        }
    }

    @Test fun assetSavePersistsAndMisReflectsSameRecordedValue() {
        val marker = System.currentTimeMillis().toString()
        val assetName = "QA Asset Runtime $marker"
        val ownerName = "QA Owner $marker"
        val nomineeName = "QA Nominee $marker"
        val owner = ArthSaathiCoreEngine.createPerson(ownerName, "9876501201", "Family")
        val nominee = ArthSaathiCoreEngine.createPerson(nomineeName, "9876501202", "Family")
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.ASSET_VAULT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Asset / account / property name")).setText(assetName)
                requireNotNull(findEdit(root, "Current value (₹)")).setText("12345.67")
                requireNotNull(findEdit(root, "Document Vault title / ID (optional)")).setText("QA evidence reference")
                val spinners = findSpinners(root)
                assertTrue("Asset category, owner and nominee selectors must be present", spinners.size >= 3)
                spinners[0].setSelection(0) // Financial Asset
                val ownerIndex = (0 until spinners[1].adapter.count).firstOrNull {
                    spinners[1].adapter.getItem(it).toString().contains(ownerName)
                }
                val nomineeIndex = (0 until spinners[2].adapter.count).firstOrNull {
                    spinners[2].adapter.getItem(it).toString().contains(nomineeName)
                }
                assertNotNull("Owner selector must include saved people", ownerIndex)
                assertNotNull("Nominee selector must include saved people", nomineeIndex)
                spinners[1].setSelection(requireNotNull(ownerIndex))
                spinners[2].setSelection(requireNotNull(nomineeIndex))
                requireNotNull(findButton(root, "SAVE ASSET")).performClick()
                val refreshed = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Saved asset should appear on the same module screen: $refreshed", refreshed.contains(assetName))
            }
        }

        val saved = ArthSaathiDataStore.records()
        val asset = (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }.firstOrNull {
            it.optString("type") == "ASSET" && it.optString("f0") == assetName
        }
        assertNotNull("Saved asset was not persisted", asset)
        val savedAsset = requireNotNull(asset)
        assertEquals(12345.67, savedAsset.optDouble("f2"), 0.005)
        assertEquals(owner.id, savedAsset.optString("ownerPersonId"))
        assertEquals(nominee.id, savedAsset.optString("nomineePersonId"))

        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.MIS)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val text = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("MIS should show the recorded financial asset", text.contains("Financial assets"))
                assertTrue("MIS should reflect the saved asset value: $text", text.contains("12345.67"))
            }
        }

        val afterRelaunch = ArthSaathiDataStore.records()
        assertTrue((0 until afterRelaunch.length()).mapNotNull { afterRelaunch.optJSONObject(it) }.any {
            it.optString("type") == "ASSET" && it.optString("f0") == assetName &&
                it.optString("ownerPersonId") == owner.id && it.optString("nomineePersonId") == nominee.id
        })
    }

    @Test fun groupKhataCalculatesPersistsAndDisplaysEqualExpenseShares() {
        val groupName = "QA Group Runtime ${System.currentTimeMillis()}"
        val groupExpensesBefore = ArthSaathiCoreEngine.mis().groupExpenses
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.GROUP_KHATA)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Group / activity name")).setText(groupName)
                requireNotNull(findEdit(root, "Total expense (₹)")).setText("5000")
                requireNotNull(findEdit(root, "Number of members")).setText("10")
                requireNotNull(findEdit(root, "Member names (comma-separated)")).setText("A, B, C, D, E, F, G, H, I, J")
                requireNotNull(findEdit(root, "Purpose / notes")).setText("Gully cricket")
                requireNotNull(findButton(root, "CALCULATE EQUAL SHARE")).performClick()
                assertTrue(allText(root).joinToString(" | ").contains("Equal share per member: ₹500.00"))
                requireNotNull(findButton(root, "SAVE GROUP EXPENSE")).performClick()
                val refreshed = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Saved group expense not displayed after save: $refreshed", refreshed.contains(groupName))
                assertTrue("Saved equal share not displayed: $refreshed", refreshed.contains("Equal share per member: ₹500.00"))
            }
        }
        val saved = ArthSaathiDataStore.records()
        assertTrue("Group expense did not persist or MIS-compatible fields were lost",
            (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }.any {
                it.optString("type") == "GROUP_EXPENSE" && it.optString("f0") == groupName &&
                    it.optDouble("f1") == 5000.0 && it.optInt("f2") == 10
            })
        assertEquals("MIS group expense total must use saved total", groupExpensesBefore + 5000.0,
            ArthSaathiCoreEngine.mis().groupExpenses, 0.001)
        val groupRecord = (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }
            .first { it.optString("type") == "GROUP_EXPENSE" && it.optString("f0") == groupName }
        val groupId = groupRecord.optString("id")
        ArthSaathiCoreEngine.saveModule("GROUP_CONTRIBUTION", mapOf("f0" to "A", "f1" to 5000.0), mapOf("parentId" to groupId))
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.GROUP_KHATA)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val text = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Contributor should be shown as owed the overpayment", text.contains("A: is owed ₹4500.00"))
                assertTrue("A non-contributor should owe their equal share", text.contains("B: owes ₹500.00"))
            }
        }
        ArthSaathiCoreEngine.saveModule("GROUP_SETTLEMENT",
            mapOf("f0" to "B", "f1" to "A", "f2" to 500.0), mapOf("parentId" to groupId))
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java).putExtra("module", ArthSaathiNavigation.GROUP_KHATA)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val text = allText(activity.window.decorView).joinToString(" | ")
                assertTrue("Settled debtor balance should return to zero", text.contains("B: owes ₹0.00") || text.contains("B: is owed ₹0.00"))
                assertTrue("Creditor balance should reduce by settlement", text.contains("A: is owed ₹4000.00"))
            }
        }
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
                requireNotNull(findEdit(root, "ROI %")).setText("150")
                requireNotNull(findEdit(root, "Method: Cash / UPI / NEFT / Other")).setText("UPI")
                requireNotNull(findEdit(root, "Frequency (Monthly / Quarterly / Annual)")).setText("Monthly")
                requireNotNull(findEdit(root, "Tenure (months)")).setText("12")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
                assertEquals("Enter an amount greater than zero",
                    findEdit(root, "Principal / amount")?.error)
                assertEquals(before, ArthSaathiDataStore.records().length())
                requireNotNull(findEdit(root, "Principal / amount")).setText("15000")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
                assertEquals("ROI must be between 0 and 100%",
                    findEdit(root, "ROI %")?.error)
                assertEquals(before, ArthSaathiDataStore.records().length())
                requireNotNull(findEdit(root, "ROI %")).setText("12.5")
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
        val agreementBytes = "lease agreement acceptance fixture".toByteArray(Charsets.UTF_8)
        val agreementFile = ArthSaathiDocumentStorage.store(context, ByteArrayInputStream(agreementBytes), "lease-agreement.txt")
        ArthSaathiCoreEngine.saveModule("DOCUMENT", mapOf(
            "f0" to "Lease Agreement QA", "f1" to "Lease agreement",
            "f3" to agreementFile.fileName, "f4" to agreementFile.sha256,
            "sizeBytes" to agreementFile.sizeBytes, "extension" to agreementFile.extension,
            "mimeType" to "text/plain", "encrypted" to true, "storage" to "ANDROID_KEYSTORE_AES_GCM"
        ))
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
                requireNotNull(findEdit(root, "ROI %")).setText("0")
                requireNotNull(findEdit(root, "Method: Cash / UPI / NEFT / Other")).setText("BANK")
                requireNotNull(findEdit(root, "Frequency (Monthly / Quarterly / Annual)")).setText("Monthly")
                requireNotNull(findEdit(root, "Tenure (months)")).setText("12")
                requireNotNull(findEdit(root, "Document title / ID (optional)")).setText("Lease Agreement QA")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
                assertEquals("A valid 10-digit mobile number is required for borrower consent",
                    findEdit(root, "Counterparty mobile (10 digits)")?.error)
                assertEquals(before, ArthSaathiDataStore.records().length())
                requireNotNull(findEdit(root, "Counterparty mobile (10 digits)")).setText("9876501187")
                // The credit type spinner is selected explicitly by its canonical visible option.
                val sp = findSpinner(activity.window.decorView)
                val leaseIndex = (0 until sp.count).firstOrNull {
                    sp.getItemAtPosition(it).toString().contains("Rental", true)
                }
                if (leaseIndex != null) sp.setSelection(leaseIndex)
            }
            InstrumentationRegistry.getInstrumentation().waitForIdleSync()
            scenario.onActivity { activity ->
                requireNotNull(findButton(activity.window.decorView, "REGISTER CREDIT")).performClick()
            }
        }
        val after = ArthSaathiDataStore.records()
        assertTrue((0 until after.length()).mapNotNull { after.optJSONObject(it) }.any {
            it.optString("type") == "CREDIT" && it.optString("party") == "Lease QA" &&
                it.optDouble("roi") == 0.0 && it.optDouble("totalPayable") == 120000.0
        })
    }

    private fun findSpinner(root: View): android.widget.Spinner {
        if (root is android.widget.Spinner) return root
        if (root is ViewGroup) for (i in 0 until root.childCount) {
            try { return findSpinner(root.getChildAt(i)) } catch (_: NoSuchElementException) { }
        }
        throw NoSuchElementException("Credit type spinner missing")
    }

    private fun findSpinners(root: View): List<android.widget.Spinner> {
        val result = mutableListOf<android.widget.Spinner>()
        fun visit(view: View) {
            if (view is android.widget.Spinner) result += view
            if (view is ViewGroup) for (i in 0 until view.childCount) visit(view.getChildAt(i))
        }
        visit(root)
        return result
    }


    @Test fun personIdentityLookupSupportsMobilePanAadhaarAndCreditLinkage() {
        val marker = System.currentTimeMillis().toString()
        val name = "Identity QA $marker"
        val mobileNumber = "98765" + marker.takeLast(5)
        val pan = "QATST" + marker.takeLast(4) + "A"
        val aadhaar = "23" + marker.takeLast(10)
        val person = ArthSaathiCoreEngine.createPerson(
            name = name, mobile = mobileNumber, role = "Borrower",
            pan = pan, aadhaar = aadhaar
        )
        assertEquals(person.id, ArthSaathiCoreEngine.findPersonByIdentity(mobileNumber)?.optString("id"))
        assertEquals(person.id, ArthSaathiCoreEngine.findPersonByIdentity(pan)?.optString("id"))
        assertEquals(person.id, ArthSaathiCoreEngine.findPersonByIdentity(aadhaar)?.optString("id"))
        assertTrue(ArthSaathiCoreEngine.validateAadhaar(aadhaar))
        assertFalse(ArthSaathiCoreEngine.validateAadhaar("123456789012"))

        val created = ArthSaathiCoreEngine.createCredit(
            person.id, name, "Personal Loan / Hand Loan", 1000.0, 0.0,
            "UPI", "Monthly", "", null, null, mobileNumber, "",
            "Principal + Interest", 12
        )
        assertTrue(created.ok)
        assertEquals(person.id, ArthSaathiCoreEngine.find(requireNotNull(created.id))?.optString("partyId"))
    }

    @Test fun registrationSearchesExistingPersonByAadhaarAndLinksCreditWithoutShowingHistory() {
        val marker = System.currentTimeMillis().toString()
        val name = "Borrower Aadhaar QA $marker"
        val aadhaar = "23" + marker.takeLast(10)
        val mobileNumber = "98765" + marker.takeLast(5)
        val person = ArthSaathiCoreEngine.createPerson(
            name = name, mobile = mobileNumber, role = "Borrower",
            pan = "PQRSX" + marker.takeLast(4) + "A", aadhaar = aadhaar
        )
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.REGISTER_CREDIT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Borrower / counterparty name")).setText(aadhaar)
                requireNotNull(findEdit(root, "Principal / amount")).setText("1000")
                requireNotNull(findEdit(root, "ROI %")).setText("0")
                requireNotNull(findEdit(root, "Method: Cash / UPI / NEFT / Other")).setText("UPI")
                requireNotNull(findEdit(root, "Frequency (Monthly / Quarterly / Annual)")).setText("Monthly")
                requireNotNull(findEdit(root, "Tenure (months)")).setText("12")
                requireNotNull(findButton(root, "REGISTER CREDIT")).performClick()
            }
        }
        val saved = ArthSaathiDataStore.records()
        assertTrue("Credit should link to the matched person profile",
            (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }.any {
                it.optString("type") == "CREDIT" && it.optString("partyId") == person.id &&
                    it.optString("party") == name && it.optString("partyMobile") == mobileNumber
            })
    }

    @Test fun misSeparatesFinancialNonFinancialAssetsAndPortfolioComposition() {
        val before = ArthSaathiCoreEngine.mis()
        ArthSaathiCoreEngine.saveModule("ASSET", mapOf("f0" to "Financial QA", "f1" to "Financial Asset", "f2" to 1000.0, "value" to 1000.0))
        ArthSaathiCoreEngine.saveModule("ASSET", mapOf("f0" to "Non-financial QA", "f1" to "Non-financial Asset", "f2" to 2000.0, "value" to 2000.0))
        ArthSaathiCoreEngine.saveModule("PORTFOLIO", mapOf("f0" to "Portfolio QA", "f1" to "Mutual Fund", "f2" to 2800.0, "f3" to 3000.0))
        val after = ArthSaathiCoreEngine.mis()
        assertEquals(1000.0, after.financialAssets - before.financialAssets, 0.005)
        assertEquals(2000.0, after.nonFinancialAssets - before.nonFinancialAssets, 0.005)
        assertEquals(3000.0, after.portfolioValue - before.portfolioValue, 0.005)
        assertEquals(6000.0, after.assets - before.assets, 0.005)
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
                requireNotNull(findEdit(root, "Address / locality")).setText("Ranchi")
                requireNotNull(findEdit(root, "6-digit PIN code (optional)")).setText("834001")
                requireNotNull(findButton(root, "SAVE PERSON")).performClick()
            }
        }
        val after = ArthSaathiDataStore.records()
        assertTrue((0 until after.length()).mapNotNull { after.optJSONObject(it) }.any {
            it.optString("type") == "PERSON" && it.optString("name") == "People QA" &&
                it.optString("mobile") == "9876501177" && it.optString("role") == "Family"
        })
    }


    @Test fun peopleScreenValidatesAndPersistsPanAndGstin() {
        val personName = "Tax QA " + System.currentTimeMillis()
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.PEOPLE)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Full name")).setText(personName)
                requireNotNull(findEdit(root, "10-digit mobile")).setText("9876501178")
                requireNotNull(findEdit(root, "Role / relationship")).setText("QA")
                requireNotNull(findEdit(root, "Address / locality")).setText("Ranchi")
                val pan = requireNotNull(findEdit(root, "PAN (optional)"))
                val gstin = requireNotNull(findEdit(root, "GSTIN (optional)"))
                pan.setText("bad-pan")
                gstin.setText("20ABCDE1234F1Z5")
                requireNotNull(findButton(root, "SAVE PERSON")).performClick()
                assertNotNull("Malformed PAN must be rejected", pan.error)
                pan.setText("ABCDE1234F")
                gstin.setText("bad-gstin")
                requireNotNull(findButton(root, "SAVE PERSON")).performClick()
                assertNotNull("Malformed GSTIN must be rejected", gstin.error)
                pan.setText("abcde1234f")
                gstin.setText("20abcde1234f1z5")
                val pin = requireNotNull(findEdit(root, "6-digit PIN code (optional)"))
                pin.setText("123")
                requireNotNull(findButton(root, "SAVE PERSON")).performClick()
                assertNotNull("Malformed PIN must be rejected", pin.error)
                pin.setText("834001")
                requireNotNull(findButton(root, "SAVE PERSON")).performClick()
            }
        }
        val saved = ArthSaathiDataStore.records()
        assertTrue("Validated PAN/GSTIN must persist with the person record",
            (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }.any {
                it.optString("type") == "PERSON" && it.optString("name") == personName &&
                    it.optString("pan") == "ABCDE1234F" && it.optString("gstin") == "20ABCDE1234F1Z5" && it.optString("pin") == "834001"
            })
    }

    @Test fun creditContractualPayableIncludesConfiguredInterestAndEmi() {
        val beforeOutstanding = ArthSaathiCoreEngine.mis().outstanding
        val flat = ArthSaathiCoreEngine.createCredit(
            null, "Flat interest QA", "Personal Loan / Hand Loan", 10000.0, 12.0,
            "UPI", "Principal + Interest", "", null, null, "9876501177", "",
            "Principal + Interest", 12
        )
        val flatRecord = requireNotNull(ArthSaathiCoreEngine.find(requireNotNull(flat.id)))
        assertEquals(11200.0, flatRecord.optDouble("totalPayable"), 0.01)
        val emiCredit = ArthSaathiCoreEngine.createCredit(
            null, "EMI QA", "Personal Loan / Hand Loan", 10000.0, 12.0,
            "UPI", "EMI", "", null, null, "9876501176", "", "EMI", 12
        )
        val emiRecord = requireNotNull(ArthSaathiCoreEngine.find(requireNotNull(emiCredit.id)))
        assertEquals(888.49, emiRecord.optDouble("expectedEmi"), 0.02)
        assertEquals(10661.88, emiRecord.optDouble("totalPayable"), 0.05)
        assertEquals("Unconsented credit drafts must not inflate registered-credit MIS", beforeOutstanding, ArthSaathiCoreEngine.mis().outstanding, 0.1)
        assertEquals("PENDING_CONSENT", flatRecord.optString("status"))
        val bullet = ArthSaathiCoreEngine.createCredit(
            null, "Bullet QA", "Personal Loan / Hand Loan", 10000.0, 12.0,
            "NEFT", "Bullet", "", null, null, "9876501175", "", "Bullet", 12
        )
        val bulletRecord = requireNotNull(ArthSaathiCoreEngine.find(requireNotNull(bullet.id)))
        assertEquals("Bullet interest must be included in contractual payable", 11200.0,
            bulletRecord.optDouble("totalPayable"), 0.01)
    }

    @Test fun paymentSetupSavesOnlyNonSecretPreLiveConfiguration() {
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.REVENUE)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                requireNotNull(findEdit(root, "Gateway provider / payment partner")).setText("Sandbox QA Provider")
                requireNotNull(findEdit(root, "Merchant / account ID (non-secret)")).setText("MID-QA-001")
                requireNotNull(findEdit(root, "Bank name")).setText("QA Bank")
                requireNotNull(findEdit(root, "Bank account holder")).setText("Test Holder")
                requireNotNull(findEdit(root, "Bank account last 4 digits")).setText("1234")
                requireNotNull(findEdit(root, "IFSC (11 characters)")).setText("SBIN0001234")
                requireNotNull(findEdit(root, "Settlement account / UPI reference")).setText("test@upi")
                requireNotNull(findButton(root, "SAVE PAYMENT SETUP")).performClick()
            }
        }
        val saved = ArthSaathiDataStore.records()
        val config = (0 until saved.length()).mapNotNull { saved.optJSONObject(it) }
            .lastOrNull { it.optString("type") == "REVENUE_CONFIG" }
        assertNotNull(config)
        assertEquals("Sandbox QA Provider", config!!.optString("gatewayProvider"))
        assertEquals("1234", config.optString("accountLast4"))
        assertFalse("Pre-live setup must not claim an active gateway", config.optBoolean("gatewayConnected"))
        assertFalse("Full bank account number must never be stored", config.has("accountNumber"))
    }

    @Test fun qrKhataScreenExposesScannerAndLocalSaveControls() {
        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.QR_KHATA)
        ).use { scenario ->
            scenario.onActivity { activity ->
                val root = activity.window.decorView
                assertNotNull(findButton(root, "SCAN QR CODE"))
                assertNotNull(findButton(root, "SAVE QR / INVOICE RECORD"))
                assertNotNull(findEdit(root, "Merchant / customer"))
                assertNotNull(findEdit(root, "QR / invoice reference"))
                assertNotNull(findEdit(root, "Invoice date (from QR)"))
            }
        }
    }

    @Test fun qrPayloadParserExtractsUpiAndJsonInvoiceFields() {
        val upi = ArthSaathiCoreEngine.parseQrPayload("upi://pay?pa=shop%40upi&pn=Corner%20Shop&am=125.50&tr=INV-9&tn=Groceries")
        assertEquals("Corner Shop", upi.optString("merchant"))
        assertEquals("125.50", upi.optString("amount"))
        assertEquals("INV-9", upi.optString("reference"))
        assertEquals("Groceries", upi.optString("note"))
        val json = ArthSaathiCoreEngine.parseQrPayload("{\"vendor\":\"Store\",\"total\":\"75\",\"invoiceNo\":\"A-12\",\"description\":\"Stationery\",\"invoiceDate\":\"2026-10-15\"}")
        assertEquals("Store", json.optString("merchant"))
        assertEquals("75", json.optString("amount"))
        assertEquals("A-12", json.optString("reference"))
        assertEquals("Stationery", json.optString("note"))
        assertEquals("2026-10-15", json.optString("date"))
    }

    @Test fun consentFailsClosedWhenNoRealOtpProviderIsConfigured() {
        val created = ArthSaathiCoreEngine.createCredit(
            null, "Consent QA", "Personal Loan / Hand Loan", 5000.0, 5.0,
            "UPI", "Monthly", "", null, null, "9876501166", "QA Guarantor", "Principal + Interest", 12
        )
        val createdId = requireNotNull(created.id)
        val requested = ArthSaathiCoreEngine.requestConsent(createdId, "lender")
        assertFalse("Consent request must not pretend an OTP was sent", requested.ok)
        val confirmed = ArthSaathiCoreEngine.confirmConsent(createdId, "123456", "borrower")
        assertFalse("A typed numeric code must not bypass provider verification", confirmed.ok)
        assertEquals("PENDING", ArthSaathiCoreEngine.find(createdId)!!.optString("consentStatus"))
        val forged = ArthSaathiCoreEngine.update(createdId, mapOf("consentStatus" to "CONSENTED"))
        assertFalse("Generic record updates must not forge borrower consent", forged.ok)
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
            "UPI", "Monthly", "", null, null, "9876501165", "QA Guarantor", "Principal + Interest", 12
        )
        val createdId = requireNotNull(created.id)
        assertTrue(ArthSaathiCoreEngine.requestConsent(createdId, "lender").ok)
        assertFalse(ArthSaathiCoreEngine.confirmConsent(createdId, "123456", "borrower").ok)
        assertEquals("REQUESTED", ArthSaathiCoreEngine.find(createdId)!!.optString("consentStatus"))
        assertTrue(ArthSaathiCoreEngine.confirmConsent(createdId, "654321", "borrower").ok)
        assertEquals("CONSENTED", ArthSaathiCoreEngine.find(createdId)!!.optString("consentStatus"))
        assertEquals("Credit becomes active only after verified consent", "ACTIVE",
            ArthSaathiCoreEngine.find(createdId)!!.optString("status"))
    }


    @Test fun consentUiConnectsProviderVerificationToRepaymentAndMis() {
        val party = "UI Consent QA " + System.currentTimeMillis()
        val created = ArthSaathiCoreEngine.createCredit(
            null, party, "Personal Loan / Hand Loan", 1000.0, 0.0,
            "UPI", "Monthly", "", null, null, "9876501164", "QA Guarantor", "EMI", 12
        )
        val id = requireNotNull(created.id)
        ArthSaathiOtpService.installProvider(object : ArthSaathiOtpProvider {
            override fun requestCode(mobile: String, purpose: String, recordId: String): String? =
                if (mobile == "9876501164" && purpose == "CREDIT_CONSENT" && recordId == id)
                    "UI-CONSENT-CHALLENGE" else null
            override fun verifyCode(challengeId: String, code: String): Boolean =
                challengeId == "UI-CONSENT-CHALLENGE" && code == "654321"
        })

        var blockedBeforeConsent = false
        try {
            ArthSaathiCoreEngine.repayment(id, 100.0, "UPI", "QA")
        } catch (_: IllegalArgumentException) {
            blockedBeforeConsent = true
        }
        assertTrue("Repayment must be blocked before verified consent", blockedBeforeConsent)

        ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
            Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", ArthSaathiNavigation.SECURITY_CONSENT)
        ).use { scenario ->
            scenario.onActivity { activity ->
                var root = activity.window.decorView
                requireNotNull(findButton(root, "REQUEST CONSENT OTP • " + party)).performClick()
                root = activity.window.decorView
                requireNotNull(findEdit(root, "OTP for " + party)).setText("111111")
                requireNotNull(findButton(root, "VERIFY CONSENT • " + party)).performClick()
                assertEquals("REQUESTED", ArthSaathiCoreEngine.find(id)!!.optString("consentStatus"))
                requireNotNull(findEdit(activity.window.decorView, "OTP for " + party)).setText("654321")
                requireNotNull(findButton(activity.window.decorView, "VERIFY CONSENT • " + party)).performClick()
            }
        }

        assertEquals("CONSENTED", ArthSaathiCoreEngine.find(id)!!.optString("consentStatus"))
        val repayment = ArthSaathiCoreEngine.repayment(id, 100.0, "UPI", "QA")
        assertTrue("Verified consent should unlock a valid repayment", repayment.ok)
        assertTrue("MIS must reflect the repayment ledger entry",
            ArthSaathiCoreEngine.mis().repayments >= 100.0)
    }

    @Test fun loginCannotAuthenticateWithoutConfiguredOtpProvider() {
        ArthSaathiSession.logout(context)
        ArthSaathiOtpService.clearProviderForTests()
        ActivityScenario.launch<ArthSaathiLoginActivity>(
            Intent(context, ArthSaathiLoginActivity::class.java)
        ).use { scenario ->
            scenario.onActivity { activity ->
                requireNotNull(findEdit(activity.window.decorView, "10-digit mobile number")).setText("9876501155")
                requireNotNull(findButton(activity.window.decorView, "SEND VERIFICATION OTP")).performClick()
                assertFalse("Mobile-only login must never create a session", ArthSaathiSession.isLoggedIn(context))
                assertTrue(allText(activity.window.decorView).any { it.contains("not configured", true) })
            }
        }
    }

    @Test fun loginRequiresProviderVerifiedChallengeBeforeCreatingSession() {
        ArthSaathiSession.logout(context)
        ArthSaathiOtpService.installProvider(object : ArthSaathiOtpProvider {
            override fun requestCode(mobile: String, purpose: String, recordId: String): String? =
                if (mobile == "9876501154" && purpose == "LOGIN") "LOGIN-CHALLENGE" else null
            override fun verifyCode(challengeId: String, code: String): Boolean =
                challengeId == "LOGIN-CHALLENGE" && code == "654321"
        })
        ActivityScenario.launch<ArthSaathiLoginActivity>(
            Intent(context, ArthSaathiLoginActivity::class.java)
        ).use { scenario ->
            scenario.onActivity { activity ->
                requireNotNull(findEdit(activity.window.decorView, "10-digit mobile number")).setText("9876501154")
                requireNotNull(findButton(activity.window.decorView, "SEND VERIFICATION OTP")).performClick()
                requireNotNull(findEdit(activity.window.decorView, "Verification code")).setText("111111")
                requireNotNull(findButton(activity.window.decorView, "VERIFY & SIGN IN")).performClick()
                assertFalse("Wrong OTP must not authenticate", ArthSaathiSession.isLoggedIn(context))
                requireNotNull(findEdit(activity.window.decorView, "Verification code")).setText("654321")
                requireNotNull(findButton(activity.window.decorView, "VERIFY & SIGN IN")).performClick()
            }
        }
        assertTrue("Correct provider-verified OTP should create a session", ArthSaathiSession.isLoggedIn(context))
        assertEquals("9876501154", ArthSaathiSession.mobile(context))
        ArthSaathiSession.logout(context)
    }


    @Test fun misCountsOnlyCompletedBenefitsAndNotMerelyApprovedClaims() {
        val baseline = ArthSaathiCoreEngine.mis().benefits
        val marker = System.currentTimeMillis().toString()
        ArthSaathiCoreEngine.saveModule("BENEFIT", mapOf(
            "f0" to "QA approved-but-unpaid benefit $marker",
            "f1" to "QA",
            "f2" to 1234.56,
            "f3" to "APPROVED"
        ))
        assertEquals("Approved but not received benefits must not count as generated value",
            baseline, ArthSaathiCoreEngine.mis().benefits, 0.001)

        ArthSaathiCoreEngine.saveModule("BENEFIT", mapOf(
            "f0" to "QA completed benefit $marker",
            "f1" to "QA",
            "f2" to 2345.67,
            "f3" to "COMPLETED"
        ))
        assertEquals("Completed benefit should be reflected in MIS",
            baseline + 2345.67, ArthSaathiCoreEngine.mis().benefits, 0.02)
    }


    @Test fun switchAnalysisIncludesCostDifferenceInNetOpportunityCalculation() {
        val result = ArthSaathiCoreEngine.switchAnalysis(
            value = 100000.0,
            currentReturn = 5.0,
            alternativeReturn = 8.0,
            currentCost = 500.0,
            alternativeCost = 900.0
        )
        assertEquals(3.0, result.optDouble("returnDifferencePct"), 0.001)
        assertEquals(3000.0, result.optDouble("annualOpportunityDifference"), 0.01)
        assertEquals(400.0, result.optDouble("costDifference"), 0.01)
        assertEquals(2600.0, result.optDouble("netIndicativeDifference"), 0.01)
    }

}
