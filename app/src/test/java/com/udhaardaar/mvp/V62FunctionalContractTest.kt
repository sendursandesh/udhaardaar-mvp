package com.udhaardaar.mvp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V62FunctionalContractTest {
    @Test fun identityValidationIsStrict(){
        assertTrue(V62UserFlow.validPan("ABCDE1234F"));assertFalse(V62UserFlow.validPan("1234567890"));
        assertTrue(V62UserFlow.validAadhaar("234567890123"));assertFalse(V62UserFlow.validAadhaar("1234567890"));
        assertTrue(V62UserFlow.validGstin("22ABCDE1234F1Z5"));assertFalse(V62UserFlow.validGstin("12345"));
        assertTrue(V62UserFlow.validMobile("9876543210"));assertFalse(V62UserFlow.validMobile("1234567890"))
    }
    @Test fun repaymentDateAndScheduleRulesExist(){
        val a=java.text.SimpleDateFormat("dd/MM/yyyy",java.util.Locale.US).parse("20/09/2026")!!
        val b=java.text.SimpleDateFormat("dd/MM/yyyy",java.util.Locale.US).parse("20/03/2027")!!
        assertTrue(V62UserFlow.monthsBetween(a,b)>=6)
    }
}