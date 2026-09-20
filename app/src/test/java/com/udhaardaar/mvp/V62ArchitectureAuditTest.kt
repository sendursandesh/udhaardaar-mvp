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
        assertEquals("Your Asset. Your Record. Your Right.", V62ArchitectureSpec.OWNERSHIP_TAGLINE)
        assertEquals("OWN • RECORD • PROTECT • CLAIM", V62ArchitectureSpec.OWNERSHIP_TAGS)
        assertTrue(V62ArchitectureSpec.lockedUserDecisions.size >= 15)
        assertTrue(V62ArchitectureSpec.modules.size >= 32)
        assertTrue(V62ArchitectureSpec.modules.contains("QR Udhaar Khata"))
        assertTrue(V62ArchitectureSpec.modules.contains("Asset Vault"))
        assertTrue(V62ArchitectureSpec.modules.contains("Liability Vault"))
        assertTrue(V62ArchitectureSpec.modules.contains("Insurance & Protection"))
        assertTrue(V62ArchitectureSpec.modules.contains("Will / Nomination / Legacy"))
        assertTrue(V62ArchitectureSpec.modules.contains("Legal Assistance"))
        assertTrue(V62ArchitectureSpec.modules.contains("AI Financial Advisor"))
        assertTrue(V62ArchitectureSpec.modules.contains("Group Contributions / Share & Settle"))
    }

    @Test fun navigationHasOneOwnerPerModuleAndNoMenuDuplicates() {
        val all = V62ModuleRegistry.routes
        val keys = all.map { it.key }
        assertEquals(keys.size, keys.toSet().size)

        val home = V62ModuleRegistry.homeRoutes.map { it.key }
        val more = V62ModuleRegistry.moreRoutes.map { it.key }
        assertEquals(6, home.size)
        assertTrue(home.containsAll(listOf("CREDIT","REPAYMENT","ASSET_VAULT","INSURANCE","MIS","LEGACY")))
        assertEquals(home.size, home.toSet().size)
        assertEquals(more.size, more.toSet().size)
        assertTrue(home.intersect(more.toSet()).isEmpty())

        assertTrue(V62ModuleRegistry.keysBySurface(V62ModuleRegistry.Surface.FLOW_ONLY)
            .containsAll(listOf("LEGAL","AI","GUARANTOR")))
        assertFalse(keys.any { it.startsWith("V5") || it.startsWith("V4") || it.startsWith("V3") })
    }

    @Test fun consentAndEventContractsCoverProtectedCrossModuleChanges() {
        assertTrue(V62ArchitectureSpec.consentEvents.containsAll(listOf("HISTORY_SHARING", "CREDIT_REGISTRATION", "REPAYMENT_CONFIRMATION", "FUNDING_PROFILE_SHARING", "QR_KHATA_CONFIRMATION")))
        assertTrue(V62ArchitectureSpec.eventFlow.containsAll(listOf("RELATIONSHIP_CHANGED", "REPAYMENT_CHANGED", "DOCUMENT_ADDED", "ASSET_CHANGED", "LIABILITY_CHANGED", "POLICY_CHANGED", "TTMM_EXPENSE_CHANGED", "TTMM_CONTRIBUTION_CHANGED", "TTMM_SETTLEMENT_CHANGED", "CONSENT_CHANGED", "NOMINEE_CHANGED", "CLAIM_CHANGED", "WILL_CHANGED", "CHARGECHECK_CHANGED", "FUNDING_REQUEST_CHANGED", "ALERT_CREATED")))
        assertEquals("TTMM_SETTLEMENT_CHANGED", V62Events.TTMM_SETTLEMENT_CHANGED)
    }

    @Test fun financialAndIntegrationContractsArePresent() {
        assertEquals(listOf("EMI", "PRINCIPAL_PLUS_INTEREST", "BULLET"), V62ArchitectureSpec.repaymentModes)
        assertTrue(V62ArchitectureSpec.accountingAdapters.containsAll(listOf("TALLY", "SAP", "GENERIC_REST_API", "CSV_IMPORT", "WEBHOOK")))
        assertTrue(V62ArchitectureSpec.tradeCreditFields.containsAll(listOf("invoiceNumber", "invoiceDate", "invoiceAmount", "dueDate", "outstandingAmount", "sourceSystem", "sourceRecordId")))
        assertTrue(V62ArchitectureSpec.chargeCheckFields.containsAll(listOf("sanctionedInterest", "actualInterest", "variance", "evidenceDocument")))
        assertTrue(V62ArchitectureSpec.misMetrics.containsAll(listOf("assetAllocation", "returns", "risk", "idleFunds", "charges", "interestReceived", "opportunityCostSaved", "completedBenefitValue", "completedRefundValue", "completedRecoveryValue", "valueGenerated")))
    }

    @Test fun ttmmAndMisContractsRemainConnected() {
        assertTrue(V62ArchitectureSpec.endpoints.containsKey("ttmmContribution"))
        assertEquals("ttmm/contribution/record", V62ArchitectureSpec.endpoints["ttmmContribution"])
        assertTrue(V62Store.TTMM_CONTRIBUTIONS.startsWith("v62_"))
    }

    @Test fun brandCopyAndVisualTokensRemainFrozenWithoutAndroidRuntimeDependencies() {
        assertEquals("ArthSaathi", V62ArchitectureSpec.BRAND)
        assertEquals("Navigate Your Financial Journey", V62ArchitectureSpec.TAGLINE)
        assertEquals("Your Asset. Your Record. Your Right.", ArthSaathiV62Design.OWNERSHIP_TAGLINE)
        assertEquals("OWN • RECORD • PROTECT • CLAIM", ArthSaathiV62Design.OWNERSHIP_TAGS)
        assertTrue(V62ArchitectureSpec.lockedUserDecisions.any { it.contains("TTMM user-facing name") })
        assertTrue(V62ArchitectureSpec.lockedUserDecisions.any { it.contains("MIS shows current portfolio") })
        assertTrue(V62ArchitectureSpec.modules.isNotEmpty())
    }
}
