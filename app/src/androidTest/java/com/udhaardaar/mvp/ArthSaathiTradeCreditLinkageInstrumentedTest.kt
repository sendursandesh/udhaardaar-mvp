package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArthSaathiTradeCreditLinkageInstrumentedTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before fun setUp() {
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.logout(context)
        context.getSharedPreferences("arthsaathi_master_data", Context.MODE_PRIVATE).edit().clear().commit()
        ArthSaathiSession.login(context, "9876501199")
    }

    @After fun tearDown() {
        ArthSaathiSession.logout(context)
        context.getSharedPreferences("arthsaathi_master_data", Context.MODE_PRIVATE).edit().clear().commit()
    }

    @Test fun tradeCreditRetainsItsQrInvoiceRelationship() {
        val invoice = ArthSaathiCoreEngine.saveModule("QR_KHATA", mapOf(
            "f0" to "Test Supplier",
            "f1" to 5000.0,
            "f2" to "INV-TEST-100",
            "f3" to "Test invoice"
        ))
        val invoiceId = invoice.id
        assertTrue(invoice.ok && !invoiceId.isNullOrBlank())

        val credit = ArthSaathiCoreEngine.createCredit(
            null,
            "Test Supplier",
            "Trade Credit / Udhaar",
            5000.0,
            0.0,
            "UPI",
            "Monthly",
            "",
            null,
            null,
            "9876501188",
            "",
            "EMI (reducing balance)",
            3,
            invoiceId
        )
        assertTrue(credit.ok && !credit.id.isNullOrBlank())
        assertEquals(invoiceId, ArthSaathiCoreEngine.find(credit.id!!)?.optString("sourceRecordId"))
        assertTrue(ArthSaathiCoreEngine.linked(invoiceId!!).any { it.optString("id") == credit.id })
    }
}
