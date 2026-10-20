package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArthSaathiClaimRelationshipInstrumentedTest {
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

    @Test fun claimKeepsAssetBenefitAndDocumentLinks() {
        val asset = ArthSaathiCoreEngine.saveModule("ASSET", mapOf("f0" to "Family House", "f1" to "Non-financial", "f2" to 2500000.0))
        val benefit = ArthSaathiCoreEngine.saveModule("BENEFIT", mapOf("f0" to "Insurance refund", "f1" to "Insurer", "f2" to 12000.0, "f3" to "PENDING"))
        val document = ArthSaathiCoreEngine.saveModule("DOCUMENT", mapOf("f0" to "Policy copy", "f1" to "Policy"))
        assertTrue(asset.ok && benefit.ok && document.ok)

        val claim = ArthSaathiCoreEngine.saveModule(
            "CLAIM",
            mapOf("f0" to "Insurance recovery", "f1" to "Nominee", "f3" to "IDENTIFIED"),
            mapOf(
                "relatedAssetId" to asset.id!!,
                "relatedBenefitId" to benefit.id!!,
                "relatedDocumentId" to document.id!!
            )
        )
        assertTrue(claim.ok)
        val saved = ArthSaathiCoreEngine.find(claim.id!!)!!
        assertEquals(asset.id, saved.optString("relatedAssetId"))
        assertEquals(benefit.id, saved.optString("relatedBenefitId"))
        assertEquals(document.id, saved.optString("relatedDocumentId"))
        assertTrue(ArthSaathiCoreEngine.linked(asset.id!!).any { it.optString("id") == claim.id })
        assertTrue(ArthSaathiCoreEngine.linked(benefit.id!!).any { it.optString("id") == claim.id })
        assertTrue(ArthSaathiCoreEngine.linked(document.id!!).any { it.optString("id") == claim.id })
    }
    @Test fun legalCaseLinksToSelectedSavedRecord() {
        val asset = ArthSaathiCoreEngine.saveModule("ASSET", mapOf("f0" to "Business Premises", "f1" to "Non-financial", "f2" to 900000.0))
        val legalCase = ArthSaathiCoreEngine.saveModule(
            "LEGAL_CASE",
            mapOf("f0" to "Property", "f1" to "Ranchi", "f2" to "Title document review", "f3" to "OPEN"),
            mapOf("relatedId" to asset.id!!)
        )
        assertTrue(legalCase.ok)
        assertEquals(asset.id, ArthSaathiCoreEngine.find(legalCase.id!!)?.optString("relatedId"))
        assertTrue(ArthSaathiCoreEngine.linked(asset.id!!).any { it.optString("id") == legalCase.id })
    }

}
