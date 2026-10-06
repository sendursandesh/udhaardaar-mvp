package com.udhaardaar.mvp

import android.app.Activity
import android.os.Bundle

/** Canonical module entry. No legacy-version routing is permitted. */
class ArthSaathiModulesActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val moduleId = intent.getStringExtra("module") ?: ArthSaathiNavigation.HOME
        openCanonical(moduleId)
    }

    private fun openCanonical(moduleId: String) {
        startActivity(android.content.Intent(this, ArthSaathiMasterModuleActivity::class.java)
            .putExtra("module", moduleId))
        finish()
    }
}
