package com.udhaardaar.mvp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V5ContractTest {
    @Test fun formalRepaymentDoesNotRequireCounterpartyConsent() {
        assertFalse(V5AppContract.requiresRepaymentConsent("FORMAL"))
        assertTrue(V5AppContract.requiresRepaymentConsent("INFORMAL"))
    }

    @Test fun requiredFlowContractContainsCriticalUserRequirements() {
        val flows = V5AppContract.requiredFlows.toSet()
        assertTrue(flows.contains("INFORMAL_CREDIT_GIVEN_RECEIVED"))
        assertTrue(flows.contains("FORMAL_SANCTION_AND_STATEMENT_AUDIT"))
        assertTrue(flows.contains("BILATERAL_REPAYMENT_REQUEST_COUNTERPARTY_OTP"))
        assertTrue(flows.contains("FINANCIAL_ASSET_VAULT"))
        assertTrue(flows.contains("NON_FINANCIAL_ASSET_VAULT"))
        assertTrue(flows.contains("SUCCESSION_INHERITANCE_CLAIMS"))
        assertTrue(flows.contains("LEGAL_ASSISTANCE_EVIDENCE_BUNDLE"))
        assertTrue(flows.contains("DUE_OVERDUE_MATURITY_RENEWAL_REMINDERS"))
    }
}
