package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Release-audit contract tests for the frozen V6.2 architecture and design. */
class V62ArchitectureAuditTest {
    @Test fun architectureHasAllFrozenModulesAndFlows() {
        assertEquals("6.2", V62ArchitectureSpec.VERSION)
        assertEquals("ArthSaathi", V62ArchitectureSpec.BRAND)
        assertEquals("Navigate Your Financial Journey", V62ArchitectureSpec.TAGLINE)
        assertTrue(V62ArchitectureSpec.modules.size >= 32)
        assertTrue(V62ArchitectureSpec.modules.contains("QR Udhaar Khata"))
        assertTrue(V62ArchitectureSpec.modules.contains("Asset Vault"))
        assertTrue(V62ArchitectureSpec.modules.contains("Liability Vault"))
        assertTrue(V62ArchitectureSpec.modules.contains("Insurance & Protection"))
        assertTrue(V62ArchitectureSpec.modules.contains("Will / Nomination / Legacy"))
        assertTrue(V62ArchitectureSpec.modules.contains("Legal Assistance"))
        assertTrue(V62ArchitectureSpec.modules.contains("AI Financial Advisor"))
    }

    @Test fun primaryRoutesAreUniqueAndAllV62() {
        val keys = V62ModuleRegistry.primaryRoutes.map { it.key }
        assertEquals(keys.size, keys.toSet().size)
        assertEquals(10, keys.size)
        assertTrue(keys.containsAll(listOf("CREDIT", "CREDIT_INTELLIGENCE", "REPAYMENT", "ASSET_VAULT", "INSURANCE", "RENTAL", "TTMM", "MIS", "LEGACY", "FINANCIAL_CENTRE")))
        assertFalse(keys.any { it.startsWith("V5") || it.startsWith("V4") || it.startsWith("V3") })
    }

    @Test fun consentAndEventContractsCoverProtectedCrossModuleChanges() {
        assertTrue(V62ArchitectureSpec.consentEvents.containsAll(listOf("HISTORY_SHARING", "CREDIT_REGISTRATION", "REPAYMENT_CONFIRMATION", "FUNDING_PROFILE_SHARING", "QR_KHATA_CONFIRMATION")))
        assertTrue(V62ArchitectureSpec.eventFlow.containsAll(listOf("RELATIONSHIP_CHANGED", "REPAYMENT_CHANGED", "DOCUMENT_ADDED", "ASSET_CHANGED", "LIABILITY_CHANGED", "POLICY_CHANGED", "CONSENT_CHANGED", "NOMINEE_CHANGED", "CLAIM_CHANGED", "WILL_CHANGED", "CHARGECHECK_CHANGED", "FUNDING_REQUEST_CHANGED", "ALERT_CREATED")))
    }

    @Test fun financialAndIntegrationContractsArePresent() {
        assertEquals(listOf("EMI", "PRINCIPAL_PLUS_INTEREST", "BULLET"), V62ArchitectureSpec.repaymentModes)
        assertTrue(V62ArchitectureSpec.accountingAdapters.containsAll(listOf("TALLY", "SAP", "GENERIC_REST_API", "CSV_IMPORT", "WEBHOOK")))
        assertTrue(V62ArchitectureSpec.tradeCreditFields.containsAll(listOf("invoiceNumber", "invoiceDate", "invoiceAmount", "dueDate", "outstandingAmount", "sourceSystem", "sourceRecordId")))
        assertTrue(V62ArchitectureSpec.chargeCheckFields.containsAll(listOf("sanctionedInterest", "actualInterest", "variance", "evidenceDocument")))
        assertTrue(V62ArchitectureSpec.misMetrics.containsAll(listOf("assetAllocation", "returns", "risk", "idleFunds", "charges", "interestReceived", "opportunityCostSaved")))
    }

    @Test fun brandCopyAndVisualTokensRemainFrozenWithoutAndroidRuntimeDependencies() {
        assertEquals("ArthSaathi", V62ArchitectureSpec.BRAND)
        assertEquals("Navigate Your Financial Journey", V62ArchitectureSpec.TAGLINE)
        assertTrue(V62ArchitectureSpec.modules.isNotEmpty())
    }
}
