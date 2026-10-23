package com.udhaardaar.mvp

import android.content.Context
import android.content.Intent
import android.content.ComponentName
import android.widget.EditText
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.uiautomator.By
import androidx.test.uiautomator.UiDevice
import androidx.test.uiautomator.Until
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

/**
 * Recovery-baseline acceptance checks: login is the first screen, sign-up is reachable,
 * and the built-in demo OTP can complete a test login and open the V6.2 home.
 * This does not claim that live SMS OTP delivery is configured.
 */
@RunWith(AndroidJUnit4::class)
class ArthSaathiRecoveryUiAcceptanceTest {
    private lateinit var device: UiDevice
    private lateinit var context: Context
    private val packageName = "com.udhaardaar.mvp"

    @Before
    fun launchCleanLogin() {
        device = UiDevice.getInstance(InstrumentationRegistry.getInstrumentation())
        context = InstrumentationRegistry.getInstrumentation().targetContext
        context.getSharedPreferences("udhaardaar_accounts", Context.MODE_PRIVATE)
            .edit().clear().commit()
        device.executeShellCommand("am force-stop $packageName")
        val intent = context.packageManager.getLaunchIntentForPackage(packageName)
        assertNotNull("Launcher activity must exist for $packageName", intent)
        context.startActivity(intent!!.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK))
        assertTrue("Login screen should open on a clean launch",
            device.wait(Until.hasObject(By.text("Send OTP")), 10_000))
    }

    @Test
    fun cleanLaunchShowsLoginAndSignupEntry() {
        assertTrue(device.hasObject(By.text("Login")))
        assertTrue(device.hasObject(By.text("Sign Up")))
        assertTrue(device.hasObject(By.text("Send OTP")))
        assertTrue(device.hasObject(By.textContains("ArthSaathi")))
        assertTrue("Approved logo must be exposed accessibly", device.hasObject(By.desc("ArthSaathi logo")))
    }

    @Test
    fun signUpScreenIsReachable() {
        device.findObject(By.text("Sign Up")).click()
        assertTrue(device.wait(Until.hasObject(By.text("Create your profile")), 5_000))
        assertTrue(device.hasObject(By.text("Verify Mobile + Create Account")))
        assertTrue(device.hasObject(By.text("Back to Sign In")))
    }

    @Test
    fun demoOtpCompletesLoginAndOpensHome() {
        val mobileField = device.findObject(By.clazz(EditText::class.java.name))
        assertNotNull("Mobile input must be available", mobileField)
        mobileField!!.setText("9876501199")
        device.findObject(By.text("Send OTP")).click()

        val otpMessage = device.wait(Until.findObject(By.textContains("Demo OTP:")), 5_000)
        assertNotNull("Demo OTP dialog should be shown for the explicit test flow", otpMessage)
        val match = Regex("Demo OTP: ([0-9]{6})").find(otpMessage!!.text)
        assertNotNull("Dialog should show the six-digit demo code", match)
        val otpInput = device.findObjects(By.clazz(EditText::class.java.name)).lastOrNull()
        assertNotNull("OTP input should be available", otpInput)
        otpInput!!.setText(match!!.groupValues[1])
        device.findObject(By.text("VERIFY")).click()

        assertTrue("Successful demo verification should open V6.2 home",
            device.wait(Until.hasObject(By.text("Plan Today")), 10_000))
        assertTrue("Home should expose at least one primary module",
            device.hasObject(By.textContains("Register")))
        assertTrue("Home tab should remain visible", device.hasObject(By.textContains("Home")))
        assertTrue("Credit tab should remain visible", device.hasObject(By.textContains("Credit")))
        assertTrue("Repay tab should remain visible", device.hasObject(By.textContains("Repay")))
        assertTrue("Vault tab should remain visible", device.hasObject(By.textContains("Vault")))
        assertTrue("More tab should remain visible", device.hasObject(By.textContains("More")))
        assertTrue("Home logo must remain visible", device.hasObject(By.desc("ArthSaathi logo")))
    }
}
