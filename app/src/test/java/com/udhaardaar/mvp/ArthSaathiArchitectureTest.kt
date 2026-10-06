package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ArthSaathiArchitectureTest {
    @Test fun canonicalModuleIdsAreUnique() {
        val ids = ArthSaathiArchitectureRegistry.modules.map { it.id }
        assertEquals(ids.size, ids.toSet().size)
    }

    @Test fun canonicalRoutesAreUnique() {
        val routes = ArthSaathiArchitectureRegistry.modules.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test fun criticalArchitecturalDistinctionsAreLocked() {
        assertEquals("RECORD", ArthSaathiArchitectureRegistry.canonicalModule("REGISTER_CREDIT")?.area)
        assertEquals("CREDIT", ArthSaathiArchitectureRegistry.canonicalModule("LOANS_UDHAAR")?.area)
        assertEquals("CREDIT", ArthSaathiArchitectureRegistry.canonicalModule("REPAYMENT")?.area)
        assertEquals("INTELLIGENCE", ArthSaathiArchitectureRegistry.canonicalModule("MIS")?.area)
        assertNotNull(ArthSaathiArchitectureRegistry.canonicalModule("GROUP_KHATA"))
    }

    @Test fun requiredMasterAreasExist() {
        val areas = ArthSaathiArchitectureRegistry.topLevelAreas().toSet()
        listOf("HOME","RECORD","CREDIT","ASSETS","GROW","PROTECT","CLAIM_LEGACY","LEGAL","PEOPLE","INTELLIGENCE","AI","REVENUE","PLATFORM")
            .forEach { assert(areas.contains(it)) { "Missing master area: $it" } }
    }
}
