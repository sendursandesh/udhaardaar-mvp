package com.udhaardaar.mvp

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ArthSaathiInvoiceOcrParserInstrumentedTest {
    @Test fun extractsLikelyMerchantAmountInvoiceReferenceAndDate() {
        val text = """
            ABC Industrial Supplies
            TAX INVOICE
            Invoice No: INV-2026-44
            Invoice Date: 19/10/2026
            GSTIN: 20ABCDE1234F1Z5
            Grand Total: ₹12,450.00
        """.trimIndent()

        val parsed = ArthSaathiCoreEngine.parseInvoiceText(text)
        assertEquals("ABC Industrial Supplies", parsed.optString("merchant"))
        assertEquals("12450.00", parsed.optString("amount"))
        assertEquals("INV-2026-44", parsed.optString("reference"))
        assertEquals("19/10/2026", parsed.optString("date"))
        assertTrue(parsed.optString("note").contains("GSTIN"))
    }
}
