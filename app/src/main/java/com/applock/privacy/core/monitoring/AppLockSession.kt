package com.applock.privacy.core.monitoring

import java.util.Collections

/**
 * In-memory session manager tracking temporary unlock states and lock screen presentation.
 */
object AppLockSession {

    private val unlockedPackages = Collections.synchronizedSet(mutableSetOf<String>())
    @Volatile var isLockActivityShowing: Boolean = false
    @Volatile var currentLockShowingPackage: String? = null
    @Volatile var activeUnlockedPackage: String? = null

    fun setLockActivityShowing(showing: Boolean, packageName: String? = null) {
        isLockActivityShowing = showing
        currentLockShowingPackage = if (showing) packageName else null
    }

    fun unlockPackage(packageName: String) {
        unlockedPackages.add(packageName)
        activeUnlockedPackage = packageName
        setLockActivityShowing(false, null)
    }

    fun isPackageUnlocked(packageName: String): Boolean {
        return unlockedPackages.contains(packageName)
    }

    fun lockPackage(packageName: String) {
        unlockedPackages.remove(packageName)
        if (activeUnlockedPackage == packageName) {
            activeUnlockedPackage = null
        }
    }

    /**
     * Called whenever a foreground package change is detected.
     * If the user switches away from the unlocked application (to launcher, recents, or another app),
     * immediately revoke unlock so reopening the app requires re-authentication.
     */
    fun onForegroundPackageChanged(newForegroundPackage: String?) {
        val current = activeUnlockedPackage
        if (current != null && newForegroundPackage != current && newForegroundPackage != "com.applock.privacy") {
            lockPackage(current)
            clearSession()
        }
    }

    fun clearSession() {
        unlockedPackages.clear()
        activeUnlockedPackage = null
        isLockActivityShowing = false
        currentLockShowingPackage = null
    }
}
