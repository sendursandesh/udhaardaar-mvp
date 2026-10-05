package com.arthsaathi.master

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArthSaathiMasterArchitectureTest{
 @Test fun allRoutesUnique(){val r=ArthSaathiArchitectureRegistry.modules.map{it.route};assertEquals(r.size,r.toSet().size)}
 @Test fun requiredModulesPresent(){listOf("REGISTER_CREDIT","LOANS_UDHAAR","GROUP_KHATA","MIS","ASSET_VAULT","CHARGECHECK","WILL_LEGACY","LEGAL","REVENUE","AI_ADVISOR").forEach{id->assertTrue(ArthSaathiArchitectureRegistry.modules.any{it.id==id})}}
 @Test fun registerIsNotExistingRepository(){assertTrue(ArthSaathiNavigation.REGISTER_CREDIT!=ArthSaathiNavigation.LOANS_UDHAAR)}
}
