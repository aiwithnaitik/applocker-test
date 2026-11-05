package com.applock.privacy

import android.app.Application

/**
 * Main Application class for AppLock.
 * Manages global application state and component initialization.
 */
class AppLockApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        if (com.applock.privacy.core.permission.PermissionManager.hasUsageStatsPermission(this) &&
            com.applock.privacy.core.permission.PermissionManager.hasOverlayPermission(this)
        ) {
            com.applock.privacy.core.monitoring.AppMonitorService.start(this)
        }
    }
}
