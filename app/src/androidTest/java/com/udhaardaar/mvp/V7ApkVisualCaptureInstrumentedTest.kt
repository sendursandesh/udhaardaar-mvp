package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File

/**
 * Capture the actually configured canonical ArthSaathi architecture. These screenshots
 * are pulled into the workflow artifact for human visual review; the old V7 router
 * is deliberately not used as a proxy for the current launcher.
 */
class V7ApkVisualCaptureInstrumentedTest {
    private lateinit var context: Context
    private lateinit var device: UiDevice
    private val mobile = "9876507788"
    private lateinit var captureDir: File

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Keep screenshots in app-private storage so scoped-storage/emulator mount quirks cannot hide evidence.
        // The CI script extracts them with `run-as` after instrumentation finishes.
        captureDir = File(context.filesDir, "arthsaathi_canonical")
        ArthSaathiSession.logout(context)
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.login(context, mobile)
        captureDir.mkdirs()
        captureDir.listFiles()?.forEach { it.delete() }
    }

    @After fun tearDown() {
        ArthSaathiSession.logout(context)
    }

    @Test fun captureEveryCanonicalArthSaathiModuleScreen() {
        val modules = ArthSaathiArchitectureRegistry.modules
        assertTrue("Canonical module registry must not be empty", modules.isNotEmpty())
        modules.forEach { module ->
            ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
                Intent(context, ArthSaathiMasterModuleActivity::class.java)
                    .putExtra("module", module.id)
            ).use { scenario ->
                scenario.onActivity { activity ->
                    assertTrue("Screen not visible during visual capture: ${module.id}",
                        activity.window.decorView.isShown)
                    assertTrue("Canonical logo missing on ${module.id}",
                        activity.window.decorView.findViewWithTag<android.widget.ImageView>(
                            "arthsaathi-canonical-logo") != null)
                }
                // Capture before ActivityScenario closes; otherwise the screenshot could
                // show the previous task instead of the module under review.
                device.waitForIdle()
                val target = File(captureDir, "${module.id}.png")
                assertTrue("Screenshot capture failed for ${module.id}", device.takeScreenshot(target))
                assertTrue("Screenshot file missing or empty for ${module.id}",
                    target.exists() && target.length() > 0)
            }
        }
        // Also capture the real signed-in launcher home; module-only captures are not a substitute
        // for the screen users see immediately after authentication.
        ActivityScenario.launch<ArthSaathiHomeActivity>(
            Intent(context, ArthSaathiHomeActivity::class.java)
        ).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue("Real launcher home did not become visible", activity.window.decorView.isShown)
                val logo = activity.window.decorView.findViewWithTag<android.widget.ImageView>(
                    "arthsaathi-canonical-logo")
                assertTrue("Canonical logo missing from real launcher home",
                    logo != null && logo.contentDescription == "ArthSaathi approved journey logo")
            }
            device.waitForIdle()
            val target = File(captureDir, "LAUNCHER_HOME.png")
            assertTrue("Screenshot capture failed for real launcher home", device.takeScreenshot(target))
            assertTrue("Real launcher home screenshot is missing or empty", target.exists() && target.length() > 0)
        }
        assertTrue("Expected one screenshot for every canonical module plus real launcher home",
            (captureDir.listFiles()?.count { it.extension.equals("png", true) } ?: 0) == modules.size + 1)
    }
}
