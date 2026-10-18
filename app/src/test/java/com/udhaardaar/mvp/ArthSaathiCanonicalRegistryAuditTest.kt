package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

/** Release gate: every frozen V6.2 domain has a unique canonical runtime route. */
class ArthSaathiCanonicalRegistryAuditTest {
    @Test fun canonicalRoutesAreUniqueAndCoverPreviouslyUnmappedDomains() {
        val modules = ArthSaathiArchitectureRegistry.modules
        assertEquals("Every canonical module ID must be unique", modules.size, modules.map { it.id }.toSet().size)
        assertEquals("Every canonical route must be unique", modules.size, modules.map { it.route }.toSet().size)
        assertEquals("Every frozen runtime module is explicitly registered", 35, modules.size)
        val required = setOf(
            "HOME", "PROFILE", "FAMILY", "ADDRESS", "PEOPLE", "REGISTER_CREDIT", "LOANS_UDHAAR",
            "CREDIT_INTELLIGENCE", "QR_KHATA", "TRADE_CREDIT", "ACCOUNTING", "FORMAL_CREDIT",
            "FUNDING", "REPAYMENT", "GUARANTOR", "RENTAL", "GROUP_KHATA", "ASSET_VAULT",
            "LIABILITY_VAULT", "PORTFOLIO", "MIS", "SWITCH_ANALYSIS", "PROTECTION", "BENEFITS", "CHARGECHECK",
            "CLAIMS", "WILL_LEGACY", "LEGAL", "ADVOCATES", "DOCUMENT_VAULT", "AI_ADVISOR",
            "ALERTS", "SECURITY_CONSENT", "REVENUE", "INTEGRATIONS"
        )
        assertTrue("Canonical registry must cover all required product domains",
            modules.map { it.id }.toSet().containsAll(required))
        required.forEach { id ->
            assertNotNull("Missing canonical route for $id", ArthSaathiArchitectureRegistry.canonicalModule(id))
        }
    }
}
