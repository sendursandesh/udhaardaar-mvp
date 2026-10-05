package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Test

class ArthSaathiIndependentArchitectureTest {
    @Test fun canonicalRoutesAreUnique() {
        val routes = ArthSaathiArchitectureRegistry.modules.map { it.route }
        assertEquals(routes.size, routes.toSet().size)
    }

    @Test fun requiredCoreModulesExist() {
        listOf(
            "REGISTER_CREDIT","LOANS_UDHAAR","REPAYMENT","GROUP_KHATA","ASSET_VAULT",
            "MIS","CHARGECHECK","WILL_LEGACY","LEGAL","DOCUMENT_VAULT","AI_ADVISOR",
            "REVENUE","SECURITY_CONSENT"
        ).forEach { assertNotNull(ArthSaathiArchitectureRegistry.canonicalModule(it)) }
    }

    @Test fun registerAndExistingAccountAreDistinct() {
        assertEquals("REGISTER_CREDIT", ArthSaathiArchitectureRegistry.canonicalModule("REGISTER_CREDIT")?.route)
        assertEquals("LOANS_UDHAAR", ArthSaathiArchitectureRegistry.canonicalModule("LOANS_UDHAAR")?.route)
    }
}
