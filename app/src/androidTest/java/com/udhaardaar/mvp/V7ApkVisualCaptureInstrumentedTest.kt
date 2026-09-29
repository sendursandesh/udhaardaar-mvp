package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.UiDevice
import org.junit.After
import org.junit.Before
import org.junit.Test
import java.io.File

class V7ApkVisualCaptureInstrumentedTest {
    private lateinit var context: Context
    private lateinit var device: UiDevice
    private val mobile = "9876507788"

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", mobile)
        V7AccountStore.create(context, "Visual QA", mobile)
        V7AccountStore.login(context, mobile)
        File("/sdcard/arthsaathi_v7").mkdirs()
    }

    @After fun tearDown() {
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", mobile)
    }

    private fun shot(name: String) {
        device.waitForIdle()
        device.takeScreenshot(File("/sdcard/arthsaathi_v7/$name.png"))
    }

    private fun home() {
        ActivityScenario.launch<V7HomeActivity>(Intent(context, V7HomeActivity::class.java)).use {
            shot("01-home")
        }
    }

    private fun native(module: String, name: String) {
        ActivityScenario.launch<V7NativeModuleActivity>(
            Intent(context, V7NativeModuleActivity::class.java).putExtra("module", module)
        ).use { shot(name) }
    }

    private fun tool(tool: String, name: String) {
        ActivityScenario.launch<V7ToolsActivity>(
            Intent(context, V7ToolsActivity::class.java).putExtra("tool", tool)
        ).use { shot(name) }
    }

    private fun module(module: String, name: String) {
        ActivityScenario.launch<V7ModuleActivity>(
            Intent(context, V7ModuleActivity::class.java).putExtra("module", module)
        ).use { shot(name) }
    }

    @Test fun captureRealApkV7Pages() {
        home()
        native("RECORD", "02-record")
        native("CREDIT", "03-credit")
        native("REPAYMENT", "04-repayment")
        native("ASSETS", "05-assets")
        native("LIABILITIES", "06-liabilities")
        tool("MIS", "07-mis")
        tool("SCORE", "08-score")
        tool("INTEGRATION", "09-integration")
        tool("REVENUE", "10-revenue")
        tool("ADDRESS", "11-address")
        module("PROTECT", "12-protect")
        module("GROW", "13-grow")
        module("LEGAL", "14-legal")
        module("TTMM", "15-ttmm")
        module("QR_KHATA", "16-qr-khata")
        module("MORE", "17-more")
    }
}
