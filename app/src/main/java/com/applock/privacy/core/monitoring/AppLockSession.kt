package com.applock.privacy.core.monitoring

import java.util.Collections

/**
 * In-memory session manager tracking temporary unlock states and lock screen presentation.
 */
object AppLockSession {

    private val unlockedPackages = Collections.synchronizedSet(mutableSetOf<String>())
    @Volatile var isLockActivityShowing: Boolean = false

    fun unlockPackage(packageName: String) {
        unlockedPackages.add(packageName)
    }

    fun isPackageUnlocked(packageName: String): Boolean {
        return unlockedPackages.contains(packageName)
    }

    fun lockPackage(packageName: String) {
        unlockedPackages.remove(packageName)
    }

    fun clearSession() {
        unlockedPackages.clear()
    }
}
