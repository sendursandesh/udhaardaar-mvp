package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.json.JSONObject
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class V7PersistenceBoundaryTest {
    @Test fun v7StoreUsesDedicatedNamespaceAndRoundTripsEncryptedData() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        val prefs = context.getSharedPreferences("v7_store", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()

        val store = V7LocalStore(context)
        val value = JSONObject().put("id", "boundary-1").put("name", "Test")
        store.add("v7_boundary_test", value)

        assertEquals("Test", store.find("v7_boundary_test", "boundary-1")?.getString("name"))
        assertTrue(prefs.contains("v7_boundary_test"))
        assertTrue(!context.getSharedPreferences("v5_store", Context.MODE_PRIVATE).contains("v7_boundary_test"))
    }

    @Test(expected = IllegalStateException::class)
    fun v7StoreRejectsLegacyNamespaces() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        V7LocalStore(context).add("v62_should_never_be_written", JSONObject().put("id", "x"))
    }
}
