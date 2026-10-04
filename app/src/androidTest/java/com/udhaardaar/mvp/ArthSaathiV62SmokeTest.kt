package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ArthSaathiV7RoutingSmokeTest {
    private lateinit var context: Context
    private val testMobile = "9876543210"

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", testMobile)
    }

    @After fun tearDown() {
        V7AccountStore.logout(context)
        V7LocalStore(context).remove("v7_accounts", testMobile)
    }

    private fun assertResumes(activity: Class<out android.app.Activity>, intent: Intent? = null) {
        val scenario = if (intent == null) ActivityScenario.launch(activity)
        else ActivityScenario.launch<android.app.Activity>(intent)
        scenario.use {
            it.onActivity { a ->
                val decor = requireNotNull(a.window?.decorView) { "Window missing for " + activity.simpleName }
                require(decor.isShown) {
                    "Window not visible after resume for " + activity.simpleName
                }
            }
        }
    }

    @Test fun loginScreenActuallyRenders() {
        assertResumes(LoginActivity::class.java)
    }

    @Test fun v7HomeAndPrimaryJourneysDoNotCrash() {
        V7AccountStore.create(context, "Test User", testMobile)
        V7AccountStore.login(context, testMobile)

        assertResumes(V7HomeActivity::class.java)

        val modules = listOf("RECORD", "CREDIT", "ASSETS", "GROW", "PROTECT", "LEGAL", "MORE")
        for (module in modules) {
            // V7ModuleActivity is a routing shell. Native modules intentionally
            // redirect to V7NativeModuleActivity and finish the shell, so the
            // smoke test must assert the canonical destination rather than the
            // transient router activity.
            val destination = when (V7MasterVisionRegistry.find(module)?.destination) {
                V7MasterVisionRegistry.Destination.NATIVE_MODULE -> V7NativeModuleActivity::class.java
                V7MasterVisionRegistry.Destination.MODULE_ROUTER -> V7ModuleActivity::class.java
                null -> error("Unregistered V7 module: $module")
            }

            assertResumes(
                destination,
                Intent(context, destination).putExtra("module", module)
            )
        }

        val tools = listOf("PORTFOLIO", "MIS", "OPPORTUNITY", "SCENARIO", "MARKET", "REPORTS", "REVENUE", "ADVOCATE", "CLAIM", "AI", "SECURITY", "TTMM_CREATE", "TTMM_CONTRIBUTION", "TTMM_SETTLE", "TTMM_HISTORY", "QR_SCAN", "QR_RECORD", "QR_CONSENT", "QR_BALANCE")
        for (tool in tools) {
            assertResumes(
                V7ToolsActivity::class.java,
                Intent(context, V7ToolsActivity::class.java).putExtra("tool", tool)
            )
        }
    }

    @Test fun v7NavigationDoesNotDuplicateTopLevelModules() {
        V7AccountStore.create(context, "Test User", testMobile)
        V7AccountStore.login(context, testMobile)

        assertResumes(V7HomeActivity::class.java)
        ActivityScenario.launch(V7HomeActivity::class.java).use { scenario ->
            scenario.onActivity { a ->
                val root = a.window?.decorView
                requireNotNull(root) { "V7 home window missing" }
                require(root.isShown) { "V7 home is not visible" }
            }
        }
    }
}
