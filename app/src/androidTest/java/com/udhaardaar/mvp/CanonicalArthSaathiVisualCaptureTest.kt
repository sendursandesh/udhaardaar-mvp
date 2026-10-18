package com.udhaardaar.mvp

import android.content.ContentValues
import android.content.Context
import android.os.Environment
import android.provider.MediaStore
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
        // Store evidence in app-internal files so CI can retrieve it reliably via adb run-as,
        // without depending on Android/data visibility rules on the host shell.
        captureDir = File(context.filesDir, "arthsaathi_canonical")
        captureDir.deleteRecursively()
        check(captureDir.mkdirs() || captureDir.isDirectory) { "Could not create screenshot evidence directory: $captureDir" }
        ArthSaathiSession.logout(context)
        ArthSaathiDataStore.initialize(context)
        ArthSaathiSession.login(context, mobile)
    }

    @After fun tearDown() {
        ArthSaathiSession.logout(context)
    }

    /** Export visual evidence to shared Pictures storage so it survives test-runner app cleanup. */
    private fun exportScreenshotForHost(source: File) {
        val values = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, source.name)
            put(MediaStore.Images.Media.MIME_TYPE, "image/png")
            put(MediaStore.Images.Media.RELATIVE_PATH, "${Environment.DIRECTORY_PICTURES}/arthsaathi_canonical")
            put(MediaStore.Images.Media.IS_PENDING, 1)
        }
        val uri = context.contentResolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values)
            ?: error("Could not create shared screenshot record for ${source.name}")
        try {
            val output = context.contentResolver.openOutputStream(uri)
                ?: error("Could not open shared screenshot output for ${source.name}")
            output.use { stream ->
                source.inputStream().use { input -> input.copyTo(stream) }
            }
            context.contentResolver.update(uri, ContentValues().apply {
                put(MediaStore.Images.Media.IS_PENDING, 0)
            }, null, null)
        } catch (failure: Throwable) {
            context.contentResolver.delete(uri, null, null)
            throw failure
        }
    }

    @Test fun captureEveryCanonicalArthSaathiModuleScreen() {
        val modules = ArthSaathiArchitectureRegistry.modules
        assertTrue("Canonical module registry must not be empty", modules.isNotEmpty())
        // Capture the real unauthenticated login screen before restoring the QA session.
        ArthSaathiSession.logout(context)
        ActivityScenario.launch<ArthSaathiLoginActivity>(
            Intent(context, ArthSaathiLoginActivity::class.java)
        ).use { scenario ->
            scenario.onActivity { activity ->
                assertTrue("Login screen did not become visible", activity.window.decorView.isShown)
                val logo = activity.window.decorView.findViewWithTag<android.widget.ImageView>("arthsaathi-canonical-logo")
                assertTrue("Approved logo missing from login screen",
                    logo != null && logo.contentDescription == "ArthSaathi approved journey logo")
            }
            device.waitForIdle()
            val target = File(captureDir, "LOGIN.png")
            assertTrue("Could not capture login screenshot", device.takeScreenshot(target))
            assertTrue("Login screenshot missing or empty", target.isFile && target.length() > 0L)
            exportScreenshotForHost(target)
        }
        ArthSaathiSession.login(context, mobile)
        modules.forEach { module ->
            ActivityScenario.launch<ArthSaathiMasterModuleActivity>(
                Intent(context, ArthSaathiMasterModuleActivity::class.java)
                    .putExtra("module", module.id)
            ).use { scenario ->
                scenario.onActivity { activity ->
                    assertTrue("Screen not visible during visual capture: ${module.id}",
                        activity.window.decorView.isShown)
                    val pageTitle = activity.window.decorView.findViewWithTag<android.widget.TextView>("canonical-page-title")
                    assertTrue("Canonical title missing or incorrect on ${module.id}: expected ${module.title}, got ${pageTitle?.text}",
                        pageTitle != null && pageTitle.text.toString() == module.title)
                    assertTrue("Canonical logo missing on ${module.id}",
                        activity.window.decorView.findViewWithTag<android.widget.ImageView>(
                            "arthsaathi-canonical-logo") != null)
                }
                // Capture before ActivityScenario closes; otherwise the screenshot could
                // show the previous task instead of the module under review.
                device.waitForIdle()
                val target = File(captureDir, "${module.id}.png")
                assertTrue("Could not capture screenshot for ${module.id}", device.takeScreenshot(target))
                assertTrue("Screenshot missing or empty for ${module.id}", target.isFile && target.length() > 0L)
                exportScreenshotForHost(target)
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
            assertTrue("Could not capture launcher home screenshot", device.takeScreenshot(target))
            assertTrue("Real launcher home screenshot is missing or empty", target.isFile && target.length() > 0L)
            exportScreenshotForHost(target)
        }
        val screenshots = captureDir.listFiles()?.filter { it.isFile && it.extension == "png" }.orEmpty()
        assertTrue("Expected one screenshot for every canonical module plus real launcher home",
            screenshots.size == modules.size + 2)
    }
}
