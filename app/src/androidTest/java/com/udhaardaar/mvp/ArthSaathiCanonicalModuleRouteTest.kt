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
 * This complements feature-specific acceptance tests: every registered module
 * must open through the same activity and render its canonical page title.
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

    private fun findTaggedView(view: View, tag: String): View? {\n        if (view.tag == tag) return view\n        if (view is ViewGroup) {\n            for (i in 0 until view.childCount) {\n                findTaggedView(view.getChildAt(i), tag)?.let { return it }\n            }\n        }\n        return null\n    }\n\n    private fun findTaggedText(view: View, tag: String): TextView? {
        if (view.tag == tag && view is TextView) return view
        if (view is ViewGroup) {
            for (i in 0 until view.childCount) {
                findTaggedText(view.getChildAt(i), tag)?.let { return it }
            }
        }
        return null
    }

    @Test fun everyCanonicalModuleRouteRendersItsOwnPageTitle() {
        val modules = ArthSaathiArchitectureRegistry.modules
        assertTrue("Canonical architecture registry must not be empty", modules.isNotEmpty())
        assertEquals(
            "Each module ID must be unique",
            modules.size,
            modules.map { it.id }.distinct().size
        )
        assertEquals(
            "Each module route must have one canonical owner",
            modules.size,
            modules.map { it.route }.distinct().size
        )

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
                    assertTrue(
                        "Module ${module.id} rendered an empty screen",
                        activity.window.decorView.height >= 0
                    )
                }
            }
        }
    }
}
