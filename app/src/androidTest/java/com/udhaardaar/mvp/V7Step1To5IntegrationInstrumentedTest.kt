package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.After
import org.junit.Before
import org.junit.Test

class V7Step1To5IntegrationInstrumentedTest {
    private val context: Context
        get() = ApplicationProvider.getApplicationContext()
    private val testOwner = "9876501091"

    @Before fun setUpAuthenticatedOwner() {
        V7AccountStore.logout(context)
        if (V7AccountStore.account(context, testOwner) == null) V7AccountStore.create(context, "V7 Step 1–5 QA", testOwner)
        V7AccountStore.login(context, testOwner)
    }

    @After fun clearAuthenticatedOwner() {
        V7AccountStore.logout(context)
    }

    @Test fun canonicalRecordConsentRepaymentAndMetricsFlow() {
        val store = V7LocalStore(context)
        store.all(V7Core.Keys.REPAYMENTS).forEach { store.remove(V7Core.Keys.REPAYMENTS, it.optString("id")) }

        val person = V7Records.person(context, "V7 QA Person", "9876543210")
        val relationship = V7Records.relationship(
            context, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 10000.0, 12.0, "PRINCIPAL_PLUS_INTEREST", "QA"
        )

        val consent = V7Architecture.LocalConsentService(context).request(
            V7Architecture.ConsentRequest(
                subjectId = person.optString("id"),
                purpose = "REPAYMENT_UPDATE",
                scope = "relationship:" + relationship.optString("id"),
                actorId = V7Core.user(context),
                expiresAt = System.currentTimeMillis() + 60_000L
            )
        )
        assertNull(V7Architecture.LocalConsentService(context).grant(consent.id, false))

        val granted = V7Architecture.LocalConsentService(context).grant(consent.id, true)
        assertNotNull(granted)
        assertTrue(V7Core.hasConsent(context, person.optString("id"), "REPAYMENT_UPDATE"))

        V7Records.repayment(
            context, relationship.optString("id"), 2500.0, 2500.0, 0.0,
            "UPI", true
        )
        val updated = V7Core.find(context, V7Core.Keys.RELATIONSHIPS, relationship.optString("id"))
        assertEquals(7500.0, updated!!.optDouble("outstanding"), 0.005)
        assertTrue(V7Core.all(context, V7Core.Keys.REPAYMENTS).any {
            it.optString("relationshipId") == relationship.optString("id") &&
                it.optDouble("amount") == 2500.0
        })

        val metrics = V7Core.metrics(context)
        assertTrue(metrics.optDouble("receivables") >= 7500.0)
        assertTrue(metrics.optInt("activeCredits") >= 1)
    }

    @Test fun repaymentCannotMutateWithoutActiveConsent() {
        val person = V7Records.person(context, "V7 Consent Guard QA", "9876543212")
        val relationship = V7Records.relationship(
            context, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 5000.0, 0.0, "PRINCIPAL_PLUS_INTEREST", "QA"
        )
        V7Records.repayment(
            context, relationship.optString("id"), 1000.0, 1000.0, 0.0,
            "CASH", true
        )
        val unchanged = V7Core.find(context, V7Core.Keys.RELATIONSHIPS, relationship.optString("id"))
        assertEquals(5000.0, unchanged!!.optDouble("outstanding"), 0.005)
        assertFalse(V7Core.all(context, V7Core.Keys.REPAYMENTS).any {
            it.optString("relationshipId") == relationship.optString("id") &&
                it.optDouble("amount") == 1000.0
        })
    }

    @Test fun crossAccountUpdateCannotOverwriteAnotherOwnersRecord() {
        val ownerA = testOwner
        val ownerB = "9876501092"
        V7AccountStore.logout(context)
        if (V7AccountStore.account(context, ownerB) == null) {
            V7AccountStore.create(context, "V7 Isolation B", ownerB)
        }
        V7AccountStore.login(context, ownerA)
        val person = V7Records.person(context, "Owner A Original", "9876501093")
        val recordId = person.optString("id")

        V7AccountStore.login(context, ownerB)
        val rawRecord = V7LocalStore(context).find(V7Core.Keys.PEOPLE, recordId)
        assertNotNull("QA precondition: raw record exists in shared encrypted store", rawRecord)
        rawRecord!!.put("name", "Owner B Tampered")
        assertFalse("Foreign account must not be able to overwrite another owner's row",
            V7Core.replace(context, V7Core.Keys.PEOPLE, rawRecord))

        V7AccountStore.login(context, ownerA)
        assertEquals("Owner A Original",
            V7Core.find(context, V7Core.Keys.PEOPLE, recordId)?.optString("name"))
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", ownerB)
        V7AccountStore.login(context, ownerA)
    }

    @Test fun revenueReconciliationIsIdempotentAndRejectsFailedPayments() {
        val service = V7RevenueEngine.service(context, "Idempotency QA", 150.0)
        val invoice = V7RevenueEngine.invoice(context, service.optString("id"), 150.0, "Idempotency QA")
        val successful = V7RevenueEngine.payment(
            context, invoice.optString("id"), 150.0, "TEST_GATEWAY", "IDEMPOTENT-REF",
            V7RevenueEngine.PaymentStatus.SUCCESS
        )
        val paymentId = successful.optString("id")
        assertTrue(V7RevenueEngine.reconcile(context, paymentId, "SETTLEMENT-IDEMPOTENT"))
        assertTrue(V7RevenueEngine.reconcile(context, paymentId, "SETTLEMENT-IDEMPOTENT"))
        assertEquals(1, V7Core.all(context, V7Core.Keys.REVENUE).count {
            it.optString("paymentId") == paymentId
        })
        assertFalse(V7RevenueEngine.reconcile(context, paymentId, "DIFFERENT-SETTLEMENT"))

        val failed = V7RevenueEngine.payment(
            context, invoice.optString("id"), 150.0, "TEST_GATEWAY", "FAILED-REF",
            V7RevenueEngine.PaymentStatus.FAILED
        )
        assertFalse(V7RevenueEngine.reconcile(context, failed.optString("id"), "FAILED-SETTLEMENT"))
        assertEquals("FAILED", V7Core.find(context, V7Core.Keys.PAYMENTS, failed.optString("id"))?.optString("status"))
    }

    @Test fun repaymentAmountMustReconcileToPrincipalAndInterest() {
        val person = V7Records.person(context, "V7 Amount Reconciliation", "9876543290")
        val relationship = V7Records.relationship(
            context, person.optString("id"), "INFORMAL_CREDIT",
            "RECEIVABLE", 5000.0, 0.0, "PRINCIPAL_PLUS_INTEREST", "amount reconciliation"
        )
        val service = V7Architecture.LocalConsentService(context)
        val consent = service.request(
            V7Architecture.ConsentRequest(
                subjectId = person.optString("id"),
                purpose = "REPAYMENT_UPDATE",
                scope = "relationship:" + relationship.optString("id"),
                actorId = V7Core.user(context),
                expiresAt = System.currentTimeMillis() + 60_000L
            )
        )
        assertNotNull(service.grant(consent.id, true))

        V7Records.repayment(
            context, relationship.optString("id"), 1000.0, 600.0, 300.0, "UPI", true
        )
        val stored = V7Core.find(context, V7Core.Keys.RELATIONSHIPS, relationship.optString("id"))
        assertEquals(5000.0, stored!!.optDouble("outstanding"), 0.005)
        assertFalse(V7Core.all(context, V7Core.Keys.REPAYMENTS).any {
            it.optString("relationshipId") == relationship.optString("id")
        })
    }

    @Test fun repeatedAlertEvaluationDoesNotDuplicateOpenAlerts() {
        val liability = V7Records.liability(
            context, V7Core.user(context), "QA_DUE", 8000.0, 7000.0, 0.0
        )
        liability.put("dueDate", "2099-12-31")
        assertTrue(V7Core.replace(context, V7Core.Keys.LIABILITIES, liability))

        V7AlertEngine.evaluate(context)
        V7AlertEngine.evaluate(context)
        val matching = V7Core.all(context, V7Core.Keys.ALERTS).filter {
            it.optString("type") == "LIABILITY_DUE" &&
                it.optString("entityId") == liability.optString("id") &&
                it.optString("status") == "OPEN"
        }
        assertEquals("One open due alert per liability", 1, matching.size)
    }

    @Test fun netWorthReconcilesAssetVaultPortfolioCreditAndLiabilityDeltas() {
        val beforeMetrics = V7Core.metrics(context)
        val before = beforeMetrics.optDouble("netWorth")
        val beforePortfolio = beforeMetrics.optDouble("portfolioValue")
        val person = V7Records.person(context, "V7 Net Worth QA", "9876543291")
        V7Records.asset(context, person.optString("id"), "PROPERTY", "QA House", 500000.0)
        V7Records.liability(context, person.optString("id"), "HOME_LOAN", 300000.0, 200000.0, 8.5)
        val portfolio = V7PortfolioEngine.createPortfolio(context, "Net Worth QA Portfolio", "Moderate", "test")
        V7Records.holding(context, portfolio.optString("id"), "QA FUND", "MUTUAL_FUND", 100000.0, 120000.0)
        V7Records.relationship(context, person.optString("id"), "TRADE_CREDIT", "RECEIVABLE",
            30000.0, 0.0, "INVOICE_DUE", "QA receivable")
        V7Records.relationship(context, person.optString("id"), "FORMAL_CREDIT", "PAYABLE",
            10000.0, 0.0, "INVOICE_DUE", "QA payable")

        val after = V7Core.metrics(context)
        assertEquals("Asset + current holdings + receivable - payable - outstanding liability",
            440000.0, after.optDouble("netWorth") - before, 0.02)
        assertEquals("Portfolio subtotal includes current holding value",
            120000.0, after.optDouble("portfolioValue") - beforePortfolio, 0.02)
    }

    @Test fun expiredConsentIsNotActive() {
        val person = V7Records.person(context, "V7 Expiry QA", "9876543211")
        val service = V7Architecture.LocalConsentService(context)
        val consent = service.request(
            V7Architecture.ConsentRequest(
                subjectId = person.optString("id"),
                purpose = "HISTORY_VIEW",
                scope = "profile",
                actorId = V7Core.user(context),
                expiresAt = System.currentTimeMillis() - 1_000L
            )
        )
        assertNull("Expired consent must not be grantable", service.grant(consent.id, true))
        assertFalse(V7Core.hasConsent(context, person.optString("id"), "HISTORY_VIEW"))
        assertNull(service.getActive(person.optString("id"), "HISTORY_VIEW"))
    }
}
