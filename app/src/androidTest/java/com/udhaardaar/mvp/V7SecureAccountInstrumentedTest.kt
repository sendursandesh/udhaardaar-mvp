package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.*
import org.junit.Test

class V7SecureAccountInstrumentedTest {
    private val c: Context get() = ApplicationProvider.getApplicationContext()

    @Test fun accountAndSessionRoundTripThroughEncryptedV7Store() {
        V7AccountStore.logout(c)
        V7AccountStore.create(c, "Secure QA", "9876509999")
        assertEquals("Secure QA", V7AccountStore.account(c, "9876509999")?.optString("name"))
        V7AccountStore.login(c, "9876509999")
        assertTrue(V7AccountStore.isLoggedIn(c))
        assertEquals("9876509999", V7AccountStore.currentMobile(c))
        assertEquals("Secure QA", V7AccountStore.currentName(c))
        assertFalse(c.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE).contains("current_mobile"))
        V7AccountStore.logout(c)
        assertFalse(V7AccountStore.isLoggedIn(c))
    }

    @Test fun v7AccountStoreUsesV7EncryptedNamespace() {
        V7AccountStore.create(c, "Namespace QA", "9876509998")
        val prefs = c.getSharedPreferences("v7_store", Context.MODE_PRIVATE)
        assertTrue(prefs.contains("v7_accounts"))
        val raw = prefs.getString("v7_accounts", "") ?: ""
        assertTrue(raw.startsWith("ENC:"))
    }
}
