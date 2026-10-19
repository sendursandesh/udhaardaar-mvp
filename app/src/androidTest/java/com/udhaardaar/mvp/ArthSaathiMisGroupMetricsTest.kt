package com.udhaardaar.mvp

import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.json.JSONArray
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArthSaathiMisGroupMetricsTest {
    private val context by lazy { InstrumentationRegistry.getInstrumentation().targetContext }

    @Before
    fun setUp() {
        ArthSaathiDataStore.initialize(context)
        context.getSharedPreferences("arthsaathi_master_data", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
        ArthSaathiSession.login(context, "9876543210")
        ArthSaathiOtpService.clearProviderForTests()
        // Force the secure store to initialize after clearing isolated test data.
        ArthSaathiDataStore.records()
    }

    @After
    fun tearDown() {
        ArthSaathiDataStore.replace(JSONArray())
        ArthSaathiSession.logout(context)
        context.getSharedPreferences("arthsaathi_master_data", android.content.Context.MODE_PRIVATE)
            .edit().clear().commit()
    }

    @Test
    fun misReflectsGroupContributionsAndUnsettledMemberShares() {
        val group = ArthSaathiCoreEngine.saveModule(
            "GROUP_EXPENSE",
            mapOf("f0" to "Gully cricket", "f1" to 5000.0, "f2" to 3,
                "f4" to "Asha, Bharat, Chandan", "total" to 5000.0, "members" to 3)
        )
        val groupId = requireNotNull(group.id)

        ArthSaathiCoreEngine.saveModule("GROUP_CONTRIBUTION",
            mapOf("f0" to "Asha", "f1" to 3000.0), mapOf("parentId" to groupId))
        ArthSaathiCoreEngine.saveModule("GROUP_CONTRIBUTION",
            mapOf("f0" to "Bharat", "f1" to 1000.0), mapOf("parentId" to groupId))
        ArthSaathiCoreEngine.saveModule("GROUP_CONTRIBUTION",
            mapOf("f0" to "Chandan", "f1" to 1000.0), mapOf("parentId" to groupId))

        val collected = ArthSaathiCoreEngine.mis()
        assertEquals(5000.0, collected.groupCollected, 0.01)
        assertEquals(1333.33, collected.groupOutstandingShares, 0.02)

        ArthSaathiCoreEngine.saveModule("GROUP_SETTLEMENT",
            mapOf("f0" to "Bharat", "f1" to "Asha", "f2" to 666.67),
            mapOf("parentId" to groupId))

        val settled = ArthSaathiCoreEngine.mis()
        assertEquals(5000.0, settled.groupCollected, 0.01)
        assertEquals(666.67, settled.groupOutstandingShares, 0.02)
    }
    @Test
    fun genericModuleEditPreservesRecordIdentityAndType() {
        val created = ArthSaathiCoreEngine.saveModule(
            "BENEFIT",
            mapOf("f0" to "Electricity refund", "f1" to "Utility", "f2" to 1250.0, "f3" to "PENDING")
        )
        val id = requireNotNull(created.id)
        val updated = ArthSaathiCoreEngine.update(id, mapOf(
            "f0" to "Electricity refund",
            "f1" to "Utility",
            "f2" to 1500.0,
            "f3" to "COMPLETED"
        ))
        assertEquals(true, updated.ok)
        val stored = requireNotNull(ArthSaathiCoreEngine.find(id))
        assertEquals(id, stored.optString("id"))
        assertEquals("BENEFIT", stored.optString("type"))
        assertEquals("COMPLETED", stored.optString("f3"))
        assertEquals(1500.0, stored.optDouble("f2"), 0.01)
    }

}
