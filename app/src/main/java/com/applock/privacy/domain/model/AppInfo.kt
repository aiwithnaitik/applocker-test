package com.applock.privacy.domain.model

/**
 * Domain representation of an installed or protected application.
 */
data class AppInfo(
    val packageName: String,
    val appName: String,
    val isLocked: Boolean = false,
    val isSystemApp: Boolean = false
)
