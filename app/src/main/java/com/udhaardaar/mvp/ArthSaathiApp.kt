package com.udhaardaar.mvp

import android.app.Application

class ArthSaathiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ArthSaathiDataStore.initialize(this)\n        ArthSaathiArchitectureGuard.verify()
    }
}
