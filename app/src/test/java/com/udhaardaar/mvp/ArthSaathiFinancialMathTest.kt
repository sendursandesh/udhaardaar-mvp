package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

/** Numerical guardrails for the financial calculations used by credit registration and repayment. */
class ArthSaathiFinancialMathTest {
    @Test fun zeroInterestEmiDividesPrincipalAcrossTenure() {
        assertEquals(1000.0, ArthSaathiCoreEngine.emi(12000.0, 0.0, 12), 0.01)
    }

    @Test fun reducingBalanceEmiMatchesStandardBenchmark() {
        // ₹100,000 at 12% annual interest for 12 months: approximately ₹8,884.88.
        assertEquals(8884.88, ArthSaathiCoreEngine.emi(100000.0, 12.0, 12), 0.02)
    }

    @Test fun emailValidationAcceptsOptionalOrValidAddressAndRejectsMalformedAddress() {
        assertTrue(ArthSaathiCoreEngine.validateEmail(""))
        assertTrue(ArthSaathiCoreEngine.validateEmail("user@example.com"))
        assertTrue(ArthSaathiCoreEngine.validateEmail("finance.team+qa@example.co.in"))
        assertTrue(!ArthSaathiCoreEngine.validateEmail("not-an-email"))
        assertTrue(!ArthSaathiCoreEngine.validateEmail("user@invalid"))
    }

    @Test fun invalidOrExtremeInputsAreRejected() {
        assertRejected { ArthSaathiCoreEngine.emi(Double.NaN, 12.0, 12) }
        assertRejected { ArthSaathiCoreEngine.emi(1000.0, 101.0, 12) }
        assertRejected { ArthSaathiCoreEngine.emi(1000.0, 12.0, 601) }
    }

    private fun assertRejected(block: () -> Unit) {
        try {
            block()
            throw AssertionError("Expected invalid financial input to be rejected")
        } catch (_: IllegalArgumentException) {
            // Expected validation guard.
        }
    }
}
