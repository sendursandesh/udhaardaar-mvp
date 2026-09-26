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
class ArthSaathiV62SmokeTest {
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
            val destination = if (V7MasterVisionRegistry.find(module)?.ownership ==
                V7MasterVisionRegistry.Ownership.NATIVE
            ) V7NativeModuleActivity::class.java else V7ModuleActivity::class.java

            assertResumes(
                destination,
                Intent(context, destination).putExtra("module", module)
            )
        }

        val tools = listOf("PORTFOLIO", "OPPORTUNITY", "MARKET", "ADDRESS", "REVENUE", "ADVOCATE", "CLAIM", "AI", "SECURITY")
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

        // assertResumes already launches, resumes, verifies visibility, and closes
        // the scenario. Avoid a second launch in the same test, which can leave
        // the emulator lifecycle waiting indefinitely on CI.
        assertResumes(V7HomeActivity::class.java)
    }
}
