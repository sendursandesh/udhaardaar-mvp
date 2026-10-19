package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test

/**
 * Full canonical-route smoke test for the configured ArthSaathi 8.0 runtime.
 * Every registered module must open through the canonical activity, render its
 * own title, and retain the approved brand mark.
 */
class ArthSaathiCanonicalModuleRouteTest {
    private lateinit var context: Context

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.logout(context)
        // Test-only authenticated session; production login still requires provider-verified OTP.
        ArthSaathiSession.login(context, "9876501199")
    }

    @After fun tearDown() {
        ArthSaathiSession.logout(context)
    }

    private fun findTaggedView(view: View, tag: String): View? {
        if (view.tag == tag) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTaggedView(view.getChildAt(i), tag)?.let { return it }
            }
        }
        return null
    }

    private fun findTaggedText(view: View, tag: String): TextView? {
        if (view.tag == tag && view is TextView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTaggedText(view.getChildAt(i), tag)?.let { return it }
            }
        }
        return null
    }

    @Test fun everyCanonicalModuleRouteRendersItsOwnPageTitleAndLogo() {
        val modules = ArthSaathiArchitectureRegistry.modules
        assertTrue("Canonical architecture registry must not be empty", modules.isNotEmpty())
        assertEquals("Each module ID must be unique", modules.size, modules.map { it.id }.distinct().size)
        assertEquals("Each module route must have one canonical owner", modules.size, modules.map { it.route }.distinct().size)

        modules.forEach { module ->
            val intent = Intent(context, ArthSaathiMasterModuleActivity::class.java)
                .putExtra("module", module.id)
            ActivityScenario.launch<ArthSaathiMasterModuleActivity>(intent).use { scenario ->
                scenario.onActivity { activity ->
                    val title = findTaggedText(activity.window.decorView, "canonical-page-title")
                    assertNotNull("Missing canonical page title for module ${module.id}", title)
                    assertEquals(
                        "Wrong title or route linkage for module ${module.id}",
                        module.title,
                        title?.text?.toString()
                    )
                    assertNotNull(
                        "Approved ArthSaathi logo is missing on module ${module.id}",
                        findTaggedView(activity.window.decorView, "arthsaathi-canonical-logo")
                    )
                }
            }
        }
    }
}
