package com.udhaardaar.mvp

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.os.SystemClock
import androidx.appcompat.app.AppCompatActivity

/** Canonical V7 inactivity/session guard. */
abstract class V7SessionActivity : AppCompatActivity() {
    companion object {
        const val TIMEOUT_MS = 15L * 60L * 1000L
        private const val PREFS = "v7_session_guard"
        private const val LAST_ACTIVITY = "last_activity"
    }

    private val handler = Handler(Looper.getMainLooper())
    private val expiryCheck = object : Runnable {
        override fun run() {
            if (!isFinishing && V7AccountStore.isLoggedIn(this@V7SessionActivity)) {
                if (expired()) forceLogout() else handler.postDelayed(this, 30_000L)
            }
        }
    }

    private fun markActivity() {
        getSharedPreferences(PREFS, MODE_PRIVATE).edit()
            .putLong(LAST_ACTIVITY, SystemClock.elapsedRealtime()).apply()
    }

    private fun expired(): Boolean {
        val last = getSharedPreferences(PREFS, MODE_PRIVATE)
            .getLong(LAST_ACTIVITY, 0L)
        return last == 0L || SystemClock.elapsedRealtime() - last >= TIMEOUT_MS
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (!V7AccountStore.isLoggedIn(this) || expired()) {
            forceLogout()
            return
        }
        markActivity()
    }

    override fun onResume() {
        super.onResume()
        if (isFinishing) return
        if (!V7AccountStore.isLoggedIn(this) || expired()) {
            forceLogout()
        } else {
            markActivity()
            handler.removeCallbacks(expiryCheck)
            handler.postDelayed(expiryCheck, 30_000L)
        }
    }

    override fun onPause() {
        super.onPause()
        handler.removeCallbacks(expiryCheck)
    }

    override fun onDestroy() {
        handler.removeCallbacksAndMessages(null)
        super.onDestroy()
    }

    override fun onUserInteraction() {
        super.onUserInteraction()
        if (!isFinishing && V7AccountStore.isLoggedIn(this)) markActivity()
    }

    private fun forceLogout() {
        V7AccountStore.logout(this)
        getSharedPreferences(PREFS, MODE_PRIVATE).edit().clear().apply()
        startActivity(Intent(this, LoginActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            putExtra("session_expired", true)
        })
        finish()
    }
}
