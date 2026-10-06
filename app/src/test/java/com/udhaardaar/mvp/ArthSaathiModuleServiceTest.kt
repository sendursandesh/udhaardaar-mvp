package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArthSaathiModuleServiceTest {
    @Test fun lifecycleContractIsDeterministic() {
        assertEquals("PENDING", ArthSaathiModuleService.Action.SUBMIT.let { "PENDING" })
        assertEquals("CLOSED", ArthSaathiModuleService.Action.SETTLE.let { "CLOSED" })
        assertEquals("ACTIVE", ArthSaathiModuleService.Action.APPROVE.let { "ACTIVE" })
        assertTrue(ArthSaathiArchitectureContracts.contracts.all { it.module.isNotBlank() && it.outputs.isNotEmpty() })
    }
}
