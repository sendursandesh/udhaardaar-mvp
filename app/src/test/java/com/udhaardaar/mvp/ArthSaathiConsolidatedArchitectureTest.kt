package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArthSaathiConsolidatedArchitectureTest {
    @Test
    fun registryHasNoStructuralErrors() {
        assertTrue(ArthSaathiConsolidatedArchitecture.verifyRegistry().toString(),
            ArthSaathiConsolidatedArchitecture.verifyRegistry().isEmpty())
    }

    @Test
    fun moduleIdsAreUniqueAndAllHaveSaveViewContracts() {
        val modules=ArthSaathiConsolidatedArchitecture.modules
        assertEquals(modules.size,modules.map{it.id}.toSet().size)
        assertTrue(modules.all{it.supportsSave && it.supportsView})
    }

    @Test
    fun noV6PrimaryEntryUsesLegacyActivity() {
        assertTrue(ArthSaathiConsolidatedArchitecture.modules.none{
            it.entryPoint.startsWith("V3") || it.entryPoint.startsWith("V4") || it.entryPoint.startsWith("V5")
        })
    }

    @Test
    fun criticalModulesArePresent() {
        val ids=ArthSaathiConsolidatedArchitecture.modules.map{it.id}.toSet()
        listOf(
            "INFORMAL_CREDIT","CREDIT_BUREAU","QR_KHATA","ASSET_VAULT",
            "INSURANCE_PROTECT","CHARGECHECK","GROUP_KHATA","WILL_LEGACY",
            "LEGAL_ASSISTANCE","AI_ADVISOR","MIS","RENTAL_LEASE","REVENUE"
        ).forEach{assertTrue("Missing $it",it in ids)}
    }
}
