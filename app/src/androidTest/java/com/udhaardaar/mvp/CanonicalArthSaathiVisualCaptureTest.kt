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
class CanonicalArthSaathiVisualCaptureTest {
    private lateinit var context: Context
    private lateinit var device: UiDevice
    private val mobile = "9876507788"
    private lateinit var captureDir: File

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        // Use emulator shell-owned shared storage for evidence; CI can pull it after test teardown.
        captureDir = File("/sdcard/arthsaathi_canonical")
        device.executeShellCommand("rm -rf /sdcard/arthsaathi_canonical && mkdir -p /sdcard/arthsaathi_canonical")
        ArthSaathiSession.logout(context)
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.login(context, mobile)
        device.executeShellCommand("mkdir -p /sdcard/arthsaathi_canonical")
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
                device.executeShellCommand("screencap -p ${target.absolutePath}")
                val bytes = device.executeShellCommand("stat -c %s ${target.absolutePath}").trim().toLongOrNull() ?: 0L
                assertTrue("Screenshot missing or empty for ${module.id}", bytes > 0L)
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
            device.executeShellCommand("screencap -p ${target.absolutePath}")
            val bytes = device.executeShellCommand("stat -c %s ${target.absolutePath}").trim().toLongOrNull() ?: 0L
            assertTrue("Real launcher home screenshot is missing or empty", bytes > 0L)
        }
        assertTrue("Expected one screenshot for every canonical module plus real launcher home",
            device.executeShellCommand("ls /sdcard/arthsaathi_canonical").lineSequence().count { it.trim().endsWith(".png") } == modules.size + 1)
    }
}
