package com.udhaardaar.mvp

import android.content.ContentValues
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class UdhaardaarIntegrationQaTest {
    private val context get() = InstrumentationRegistry.getInstrumentation().targetContext
    private lateinit var db: V32DatabaseHelper

    @Before fun setUp() {
        context.deleteDatabase("udhaardaar_v32.db")
        context.deleteDatabase("udhaardaar_v3.db")
        context.deleteDatabase("formal_loan_audit.db")
        context.deleteDatabase("udhaardaar_v4_assets.db")
        db = V32DatabaseHelper(context)
    }

    @After fun tearDown() { db.close() }

    @Test fun fifteenDummyProfilesCreditsSearchHistoryAndRepayments() {
        val borrowerIds = mutableListOf<Long>()
        val guarantorIds = mutableListOf<Long>()
        repeat(15) { i ->
            borrowerIds += db.upsertProfile(null, "BORROWER", "QA-BOR-${i + 1}", "QA Borrower ${i + 1}", "900000${i.toString().padStart(4, '0')}", "", "QA Address ${i + 1}", "Ranchi", "Jharkhand", "83400${i}", "ABCDE${(i % 10)}${i}F", "1234567890${i.toString().padStart(2, '0')}", "20ABCDE${i % 10}1Z5", null)
            guarantorIds += db.upsertProfile(null, "GUARANTOR", "QA-GUA-${i + 1}", "QA Guarantor ${i + 1}", "910000${i.toString().padStart(4, '0')}", "", "QA G Address ${i + 1}", "Ranchi", "Jharkhand", "83410${i}", "FGHIJ${(i % 10)}${i}K", "2234567890${i.toString().padStart(2, '0')}", "20FGHIJ${i % 10}1Z5", null)
        }
        assertEquals(15, db.searchProfiles("BORROWER", "QA Borrower").size)
        assertEquals(1, db.searchProfiles("BORROWER", "QA-BOR-7").size)
        assertEquals(1, db.searchProfiles("BORROWER", "9000000007").size)

        val creditIds = mutableListOf<Long>()
        repeat(15) { i ->
            val principal = 10000.0 + i * 1000.0
            val interest = principal * 0.12
            val payable = principal + interest
            val id = db.addCredit(borrowerIds[i], guarantorIds[i], if (i % 2 == 0) "PERSONAL" else "BUSINESS", if (i % 3 == 0) "GIVEN" else "RECEIVED", principal, 12.0, 12, "EMI", payable / 12.0, interest, payable, "2026-01-01", "2026-12-01", "QA-INV-${i + 1}", null, true)
            creditIds += id
            db.createSchedule(id, payable / 12.0, 12, "2026-12-01")
        }
        assertEquals(15, db.credits(null).size)
        assertEquals(5, db.credits("GIVEN").size)
        assertEquals(10, db.credits("RECEIVED").size)

        val summary = db.borrowerSummary(borrowerIds[0])
        assertEquals(10000.0, summary.total, 0.01)
        assertTrue(summary.outstanding > 10000.0)

        val schedule = db.schedules(creditIds[0], false)
        assertEquals(12, schedule.size)
        val accepted = db.recordPayment(schedule.first().id, creditIds[0], schedule.first().amount)
        assertEquals(schedule.first().amount, accepted, 0.01)
        assertEquals(11, db.schedules(creditIds[0], false).count { it.status == "DUE" || it.status == "OVERDUE" })

        val borrower = AccessControl.CreditParty("QA-BOR-1", AccessControl.Role.BORROWER, consentGranted = true)
        val lender = AccessControl.CreditParty("QA-LENDER", AccessControl.Role.LENDER, consentGranted = true)
        val parties = listOf(lender, borrower)
        assertTrue(AccessControl.canRecordRepayment(borrower, parties))
        assertTrue(AccessControl.canRecordRepayment(lender, parties))
        assertTrue(!AccessControl.canRecordRepayment(AccessControl.CreditParty("INTRUDER", AccessControl.Role.BORROWER, true), parties))
        assertTrue(!AccessControl.canRecordRepayment(borrower.copy(consentGranted = false), parties))
    }

    @Test fun fifteenFormalLoanAuditBaselinesAndStatementEntries() {
        val audit = FormalLoanAuditDb(context)
        repeat(15) { i ->
            val loanId = audit.addLoan(ContentValues().apply {
                put("lender", "QA Bank ${i + 1}")
                put("account_no", "QA-LOAN-${i + 1}")
                put("loan_type", "Formal Loan")
                put("sanctioned", 500000.0 + i * 10000)
                put("disbursed", 490000.0 + i * 10000)
                put("roi", 10.5 + i * 0.1)
                put("tenure_months", 60)
                put("emi", 11000.0 + i * 100)
                put("processing_fee", 5000.0)
                put("documentation_fee", 1000.0)
                put("insurance", 2500.0)
                put("penal_rate", 2.0)
                put("bounce_charge", 500.0)
                put("prepayment_charge", 2.0)
                put("other_charge", 0.0)
                put("created_at", FormalLoanAuditDb.now())
            })
            assertTrue(loanId > 0)
            repeat(15) { row ->
                audit.addEntry(ContentValues().apply {
                    put("loan_id", loanId)
                    put("entry_date", "2026-08-${(row % 28) + 1}")
                    put("description", if (row % 2 == 0) "Processing charge" else "EMI repayment")
                    put("amount", if (row % 2 == 0) 5000.0 else 11000.0)
                    put("charge_type", if (row % 2 == 0) "PROCESSING" else "EMI")
                    put("expected", if (row % 2 == 0) 5000.0 else 0.0)
                    put("variance", 0.0)
                    put("review", "MATCH")
                })
            }
            audit.entries(loanId).use { cursor ->
                var count = 0
                while (cursor.moveToNext()) count++
                assertEquals(15, count)
            }
        }
        audit.latestLoan().use { assertTrue(it.moveToFirst()) }
        audit.close()
    }

    @Test fun fifteenAssetsAndInheritanceContactsPersistAndAggregate() {
        val vault = AssetVaultDb(context)
        repeat(15) { i ->
            val assetId = vault.addAsset(
                if (i % 2 == 0) "PROPERTY" else "FINANCIAL",
                "QA Asset ${i + 1}",
                "QA Institution ${i + 1}",
                "QA-ASSET-${i + 1}",
                100000.0 + i * 25000.0,
                "QA Nominee ${i + 1}",
                "SPOUSE",
                "QA Heir ${i + 1}",
                "QA inheritance note ${i + 1}",
                null
            )
            assertTrue(assetId > 0)
            val familyId = vault.addFamily("QA Family ${i + 1}", if (i % 2 == 0) "SPOUSE" else "CHILD", "900000${i.toString().padStart(4, '0')}", "qa${i + 1}@example.com", "QA contact")
            assertTrue(familyId > 0)
        }
        assertEquals(15, vault.assetCount())
        assertEquals(4650000.0, vault.totalValue(), 0.01)
        vault.close()
    }

    @Test fun launchCriticalActivitiesWithoutCrash() {
        val activities = listOf(
            LoginActivity::class.java,
            KeyboardSafeV323Activity::class.java,
            V323Activity::class.java,
            DashboardV3Activity::class.java,
            VerificationDocumentsActivity::class.java,
            RegisterCreditV3Activity::class.java,
            MainActivity::class.java,
            AddUdhaarActivity::class.java,
            RecordsActivity::class.java
        )
        activities.forEach { activityClass ->
            ActivityScenario.launch(activityClass).use { scenario ->
                assertTrue("Activity ${activityClass.simpleName} did not reach RESUMED", scenario.state.isAtLeast(androidx.lifecycle.Lifecycle.State.RESUMED))
            }
        }
    }

    @Test fun v3NormalizedDatabaseCreditLinkageWorks() {
        val helper = V3DatabaseHelper(context)
        val now = "2026-08-28 12:00:00"
        val lender = helper.writableDatabase.insert("parties", null, ContentValues().apply { put("party_type", "PERSON"); put("name", "QA Lender"); put("mobile", "9111111111"); put("created_at", now) })
        val borrower = helper.writableDatabase.insert("parties", null, ContentValues().apply { put("party_type", "PERSON"); put("name", "QA Borrower"); put("mobile", "9222222222"); put("created_at", now) })
        val credit = helper.addCreditBetween(lender, borrower, "PERSONAL", 25000.0, 12.0, "EMI", 2220.0, "MONTHLY", "2026-08-28", "2027-08-28", "2026-09-28", "QA", now)
        assertTrue(credit > 0)
        val count = helper.readableDatabase.rawQuery("SELECT COUNT(*) FROM credit_parties WHERE credit_id=? AND party_id=? AND role='BORROWER'", arrayOf(credit.toString(), borrower.toString())).use { it.moveToFirst(); it.getInt(0) }
        assertEquals(1, count)
        helper.close()
    }
}
