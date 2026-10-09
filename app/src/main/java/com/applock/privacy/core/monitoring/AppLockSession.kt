package com.applock.privacy.core.monitoring

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import java.util.Collections

/**
 * In-memory session manager tracking temporary unlock states, transitions, and lock screen presentation.
 */
object AppLockSession {

    private val unlockedPackages = Collections.synchronizedSet(mutableSetOf<String>())
    @Volatile var isLockActivityShowing: Boolean = false
    @Volatile var currentLockShowingPackage: String? = null
    @Volatile var activeUnlockedPackage: String? = null
    @Volatile var lastUnlockTimestamp: Long = 0L

    /** Grace window (in milliseconds) immediately following a successful unlock */
    const val POST_UNLOCK_GRACE_PERIOD_MS = 2500L

    fun setLockActivityShowing(showing: Boolean, packageName: String? = null) {
        isLockActivityShowing = showing
        currentLockShowingPackage = if (showing) packageName else null
    }

    fun unlockPackage(packageName: String) {
        unlockedPackages.add(packageName)
        activeUnlockedPackage = packageName
        lastUnlockTimestamp = System.currentTimeMillis()
        setLockActivityShowing(false, null)
    }

    fun isPackageUnlocked(packageName: String): Boolean {
        return unlockedPackages.contains(packageName)
    }

    fun isGracePeriodActive(): Boolean {
        return (System.currentTimeMillis() - lastUnlockTimestamp) < POST_UNLOCK_GRACE_PERIOD_MS
    }

    fun lockPackage(packageName: String) {
        unlockedPackages.remove(packageName)
        if (activeUnlockedPackage == packageName) {
            activeUnlockedPackage = null
        }
    }

    /**
     * Checks if a package is a transient system package, virtual keyboard,
     * or permission controller that should never trigger an app relock.
     */
    fun isSystemOrTransientPackage(packageName: String?, ownPackageName: String = "com.applock.privacy"): Boolean {
        if (packageName.isNullOrBlank()) return true
        if (packageName == ownPackageName) return true
        if (packageName == "android") return true
        if (packageName == "com.android.systemui") return true
        if (packageName.startsWith("com.google.android.gms")) return true
        if (packageName.startsWith("com.google.android.permissioncontroller") ||
            packageName.startsWith("com.android.permissioncontroller")
        ) return true
        if (packageName == "com.android.documentsui" || packageName == "com.google.android.documentsui") return true
        if (packageName == "com.google.android.packageinstaller" || packageName == "com.android.packageinstaller") return true

        // Virtual keyboards and IMEs
        val lower = packageName.lowercase()
        if (lower.contains("inputmethod") ||
            lower.contains("keyboard") ||
            lower.contains("honeyboard") ||
            lower.contains("gboard") ||
            lower.contains("swiftkey") ||
            lower.contains("ime")
        ) return true

        return false
    }

    /**
     * Resolves all installed launcher (home screen) package names on the device.
     */
    fun getInstalledLauncherPackages(packageManager: PackageManager): Set<String> {
        val launchers = mutableSetOf<String>()
        try {
            val homeIntent = Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_HOME)
            val resolveInfos = packageManager.queryIntentActivities(homeIntent, PackageManager.MATCH_ALL)
            for (info in resolveInfos) {
                info.activityInfo?.packageName?.let { launchers.add(it) }
            }
        } catch (_: Exception) {}

        // Add common OEM launcher package names as fallback
        launchers.add("com.google.android.apps.nexuslauncher")
        launchers.add("com.android.launcher")
        launchers.add("com.android.launcher2")
        launchers.add("com.android.launcher3")
        launchers.add("com.sec.android.app.launcher")
        launchers.add("com.miui.home")
        launchers.add("com.oppo.launcher")
        launchers.add("com.oneplus.launcher")
        launchers.add("com.huawei.android.launcher")
        launchers.add("com.coloros.launcher")
        launchers.add("com.vivo.launcher")
        launchers.add("com.transsion.hilauncher")
        launchers.add("com.motorola.launcher3")
        launchers.add("com.asus.launcher")
        return launchers
    }

    /**
     * Evaluates if a foreground package switch warrants locking the currently unlocked application.
     */
    fun onForegroundPackageChanged(
        newForegroundPackage: String?,
        launcherPackages: Set<String> = emptySet(),
        ownPackageName: String = "com.applock.privacy"
    ) {
        if (isGracePeriodActive()) return
        if (isSystemOrTransientPackage(newForegroundPackage, ownPackageName)) return

        val current = activeUnlockedPackage ?: return
        if (newForegroundPackage == current || newForegroundPackage == ownPackageName) return

        // User switched to launcher (Home / Recents) or to another distinct application
        clearSession()
    }

    fun clearSession() {
        unlockedPackages.clear()
        activeUnlockedPackage = null
        isLockActivityShowing = false
        currentLockShowingPackage = null
    }
}
