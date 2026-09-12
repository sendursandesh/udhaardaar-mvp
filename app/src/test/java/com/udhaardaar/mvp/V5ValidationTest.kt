package com.udhaardaar.mvp

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class V5ValidationTest {
    @Test fun panRejectsArbitraryDigitsAndAcceptsValidStructure() {
        assertFalse(V5Validation.pan("123456789012"))
        assertFalse(V5Validation.pan("ABCDE12345"))
        assertTrue(V5Validation.pan("ABCDE1234F"))
        assertTrue(V5Validation.pan(""))
    }

    @Test fun aadhaarRejectsWrongLengthAndChecksum() {
        assertFalse(V5Validation.aadhaar("123456789012"))
        assertFalse(V5Validation.aadhaar("234567890123"))
        assertTrue(V5Validation.aadhaar("234567890124"))
        assertTrue(V5Validation.aadhaar(""))
    }

    @Test fun gstinRejectsArbitraryValuesAndChecksChecksum() {
        assertFalse(V5Validation.gstin("123"))
        assertFalse(V5Validation.gstin("20ABCDE1234F1Z1"))
        assertFalse(V5Validation.gstin("99ABCDE1234F1Z1A"))
        assertTrue(V5Validation.gstin("20ABCDE1234F1ZE"))
        assertTrue(V5Validation.gstin(""))
    }

    @Test fun mobileAndPinHaveStrictIndianFormats() {
        assertTrue(V5Validation.mobile("9876543210"))
        assertFalse(V5Validation.mobile("1234567890"))
        assertFalse(V5Validation.mobile("987654321"))
        assertTrue(V5Validation.pin("834001"))
        assertFalse(V5Validation.pin("012345"))
        assertFalse(V5Validation.pin("12345"))
    }
}
