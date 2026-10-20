package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONArray
import org.json.JSONObject
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

class ArthSaathiOfflineWorkspaceInstrumentedTest {
    private val context: Context get() = ApplicationProvider.getApplicationContext()

    @Before fun setUp() {
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.logout(context)
        ArthSaathiSession.loginOffline(context)
        ArthSaathiDataStore.replace(JSONArray())
    }

    @After fun tearDown() {
        ArthSaathiDataStore.replace(JSONArray())
        ArthSaathiSession.logout(context)
    }

    @Test fun offlineWorkspaceHasStableDeviceOwnerAndNoVerifiedMobile() {
        val firstOwner = ArthSaathiSession.ownerId(context)
        assertNotNull(firstOwner)
        assertTrue(firstOwner!!.startsWith("local:"))
        assertTrue(ArthSaathiSession.isLoggedIn(context))
        assertTrue(ArthSaathiSession.isOfflineWorkspace(context))
        assertEquals("", ArthSaathiSession.mobile(context))
    }

    @Test fun offlineRecordsRemainDeviceScopedAcrossLogoutAndReentry() {
        ArthSaathiDataStore.append(JSONObject()
            .put("id", "offline-workspace-test")
            .put("type", "OFFLINE_WORKSPACE_TEST")
            .put("label", "device-local"))
        assertEquals(1, ArthSaathiDataStore.records().length())

        val owner = ArthSaathiSession.ownerId(context)
        ArthSaathiSession.logout(context)
        assertEquals(0, ArthSaathiDataStore.records().length())

        ArthSaathiSession.loginOffline(context)
        assertEquals(owner, ArthSaathiSession.ownerId(context))
        assertEquals(1, ArthSaathiDataStore.records().length())
        assertTrue(ArthSaathiSession.isOfflineWorkspace(context))
    }

    @Test fun providerSessionDoesNotRetainOfflineWorkspaceFlag() {
        ArthSaathiSession.login(context, "9876501199")
        assertFalse(ArthSaathiSession.isOfflineWorkspace(context))
        assertEquals("9876501199", ArthSaathiSession.mobile(context))
        assertEquals("9876501199", ArthSaathiSession.ownerId(context))
    }
}
