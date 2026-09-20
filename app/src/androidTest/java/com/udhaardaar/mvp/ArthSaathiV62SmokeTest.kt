package com.udhaardaar.mvp

import android.content.Context
import androidx.test.core.app.ActivityScenario
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.matcher.ViewMatchers.withText
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

    @After fun tearDown() { prefs.edit().clear().commit() }

    @Test fun loginScreenActuallyRenders() {
        ActivityScenario.launch(LoginActivity::class.java).use {
            onView(withText("SEND OTP")).check(androidx.test.espresso.assertion.ViewAssertions.matches(
                androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
            ))
            onView(withText("ArthSaathi")).check(androidx.test.espresso.assertion.ViewAssertions.matches(
                androidx.test.espresso.matcher.ViewMatchers.isDisplayed()
            ))
        }
    }

    @Test fun homeAndPrimaryModulesDoNotCrash() {
        prefs.edit().putString("name_9876543210","Test User")
            .putBoolean("logged_in",true).putString("current_mobile","9876543210").commit()

        val activities = listOf(
            V62HomeActivity::class.java,
            V62CreditRegistrationActivity::class.java,
            V62RepaymentActivity::class.java,
            V62AssetVaultActivity::class.java,
            V62InsuranceActivity::class.java,
            V62RentalLeaseActivity::class.java,
            V62TTMMActivity::class.java,
            V62QRKhataActivity::class.java,
            V62MISActivity::class.java,
            V62CreditIntelligenceActivity::class.java,
            V62ChargeCheckActivity::class.java,
            V62LegacyLegalAIActivity::class.java,
            V62ExtendedModulesActivity::class.java
        )

        for (activity in activities) {
            ActivityScenario.launch(activity).use { scenario ->
                scenario.onActivity { requireNotNull(it.window?.decorView) { "Window missing for module: ${activity.simpleName}" } }
                // Exercise resume/re-render paths because several V6.2 screens refresh their
                // source-of-truth data from onResume/event callbacks.
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.STARTED)
                scenario.moveToState(androidx.lifecycle.Lifecycle.State.RESUMED)
                scenario.onActivity { requireNotNull(it.window?.decorView) { "Window missing after resume: ${activity.simpleName}" } }
            }
        }
    }
}
