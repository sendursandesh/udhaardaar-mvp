package com.arthsaathi.master

import android.app.Application

class ArthSaathiApp : Application() {
    override fun onCreate() {
        super.onCreate()
        ArthSaathiDataStore.initialize(this)
    }
}
