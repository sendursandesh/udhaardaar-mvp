package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import androidx.lifecycle.Lifecycle
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
    private lateinit var prefs: android.content.SharedPreferences

    @Before fun setUp() {
        context = InstrumentationRegistry.getInstrumentation().targetContext
        prefs = context.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE)
        prefs.edit().clear().commit()
    }

    @After fun tearDown() {
        prefs.edit().clear().commit()
    }

    private fun assertResumes(activity: Class<out android.app.Activity>, intent: Intent? = null) {
        val scenario = if (intent == null) ActivityScenario.launch(activity)
        else ActivityScenario.launch<android.app.Activity>(intent)
        scenario.use {
            it.onActivity { a ->
                requireNotNull(a.window?.decorView) { "Window missing for " + activity.simpleName }
            }
            it.moveToState(Lifecycle.State.STARTED)
            it.moveToState(Lifecycle.State.RESUMED)
            it.onActivity { a ->
                require(a.window?.decorView?.isShown == true) {
                    "Window not visible after resume for " + activity.simpleName
                }
            }
        }
    }

    @Test fun loginScreenActuallyRenders() {
        assertResumes(LoginActivity::class.java)
    }

    @Test fun v7HomeAndPrimaryJourneysDoNotCrash() {
        prefs.edit()
            .putString("name_9876543210", "Test User")
            .putBoolean("logged_in", true)
            .putString("current_mobile", "9876543210")
            .commit()

        assertResumes(V7HomeActivity::class.java)

        val modules = listOf("RECORD", "CREDIT", "ASSETS", "GROW", "PROTECT", "LEGAL", "MORE")
        for (module in modules) {
            assertResumes(
                V7ModuleActivity::class.java,
                Intent(context, V7ModuleActivity::class.java).putExtra("module", module)
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
        prefs.edit()
            .putString("name_9876543210", "Test User")
            .putBoolean("logged_in", true)
            .putString("current_mobile", "9876543210")
            .commit()

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
