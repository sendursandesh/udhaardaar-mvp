package com.udhaardaar.mvp

import org.junit.Assert.*
import org.junit.Test

class ArthSaathiDomainEngineTest {
    @Test fun validationVectors() {
        assertTrue(ArthSaathiDomainEngine.validatePan("ABCDE1234F"))
        assertFalse(ArthSaathiDomainEngine.validatePan("ABC123"))
        assertTrue(ArthSaathiDomainEngine.validateGstin("22ABCDE1234F1Z5"))
        assertTrue(ArthSaathiDomainEngine.validatePin("834001"))
    }

    @Test fun financialCalculations() {
        assertEquals(1000.0, ArthSaathiDomainEngine.emi(12000.0,0.0,12),0.001)
        assertEquals(1000.0, ArthSaathiDomainEngine.opportunityCost(100000.0,8.0,9.0),0.001)
        assertEquals(500.0, ArthSaathiDomainEngine.variance(1500.0,2000.0),0.001)
        assertEquals(700.0, ArthSaathiDomainEngine.netPosition(1000.0,500.0,800.0),0.001)
        assertEquals(250.0, ArthSaathiDomainEngine.groupShare(1000.0,4),0.001)
    }

    @Test fun architectureStillGuardsCanonicalSeparation() {
        ArthSaathiArchitectureGuard.verify()
        assertEquals("RECORD",ArthSaathiArchitectureRegistry.canonicalModule("REGISTER_CREDIT")?.area)
        assertEquals("CREDIT",ArthSaathiArchitectureRegistry.canonicalModule("LOANS_UDHAAR")?.area)
        assertEquals("CREDIT",ArthSaathiArchitectureRegistry.canonicalModule("REPAYMENT")?.area)
        assertEquals("INTELLIGENCE",ArthSaathiArchitectureRegistry.canonicalModule("MIS")?.area)
    }

    @Test fun masterFlowRegistryHasNoDuplicateIds() {
        assertEquals(
            ArthSaathiFlowRegistry.flows.size,
            ArthSaathiFlowRegistry.flows.map { it.id }.distinct().size
        )
        assertTrue(ArthSaathiFlowRegistry.flows.all { it.steps.isNotEmpty() })
    }

    @Test fun misAggregationIsCrossModule() {
        val rows=org.json.JSONArray().apply {
            put(org.json.JSONObject().put("type","CREDIT").put("amount",10000).put("paid",2500))
            put(org.json.JSONObject().put("type","ASSET").put("value",5000))
            put(org.json.JSONObject().put("type","LIABILITY").put("outstanding",2000))
            put(org.json.JSONObject().put("type","BENEFIT").put("received",300))
            put(org.json.JSONObject().put("type","REVENUE").put("status","SUCCESS").put("amount",100))
        }
        val m=ArthSaathiDomainEngine.mis(rows)
        assertEquals(7500.0,m["OUTSTANDING"]!!,0.001)
        assertEquals(8000.0,m["NET_POSITION"]!!,0.001)
        assertEquals(300.0,m["BENEFITS"]!!,0.001)
        assertEquals(100.0,m["REVENUE"]!!,0.001)
    }
}
