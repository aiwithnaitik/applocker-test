package com.applock.privacy

import android.app.Application

/**
 * Main Application class for AppLock.
 * Manages global application state and component initialization.
 */
class AppLockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // Foundation initialization for logging and dependency management
    }
}
