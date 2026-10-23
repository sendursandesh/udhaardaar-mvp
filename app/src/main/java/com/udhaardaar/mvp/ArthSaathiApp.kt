package com.udhaardaar.mvp

import android.app.Application

class ArthSaathiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ArthSaathiSession.migrateSessionIfNeeded(this)
        ArthSaathiDataStore.initialize(this)
    }
}
