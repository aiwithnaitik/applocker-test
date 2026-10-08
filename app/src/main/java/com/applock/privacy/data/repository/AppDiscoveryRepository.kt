package com.applock.privacy.data.repository

import android.content.Context
import android.content.Intent
import android.content.pm.ApplicationInfo
import android.content.pm.PackageManager
import android.os.Build
import androidx.compose.ui.graphics.asImageBitmap
import androidx.core.graphics.drawable.toBitmap
import com.applock.privacy.data.model.AppInfo
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AppDiscoveryRepository(private val context: Context) {

    private val packageManager: PackageManager = context.packageManager
    private var cachedApps: List<AppInfo>? = null

    /**
     * Discovers all launchable applications installed on the device.
     */
    suspend fun getInstalledApps(forceRefresh: Boolean = false): List<AppInfo> = withContext(Dispatchers.IO) {
        if (!forceRefresh && cachedApps != null) {
            return@withContext cachedApps!!
        }

        val selfPackage = context.packageName
        val mainIntent = Intent(Intent.ACTION_MAIN, null).apply {
            addCategory(Intent.CATEGORY_LAUNCHER)
        }

        val flags = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PackageManager.MATCH_ALL
        } else {
            0
        }

        val resolveInfos = packageManager.queryIntentActivities(mainIntent, flags)
        val seenPackages = mutableSetOf<String>()
        val result = mutableListOf<AppInfo>()

        for (resolveInfo in resolveInfos) {
            val pkg = resolveInfo.activityInfo.packageName
            if (pkg == selfPackage || seenPackages.contains(pkg)) {
                continue
            }
            seenPackages.add(pkg)

            val appName = try {
                resolveInfo.loadLabel(packageManager).toString()
            } catch (_: Exception) {
                resolveInfo.activityInfo.name
            }

            val isSystem = try {
                val appInfo = resolveInfo.activityInfo.applicationInfo
                (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) != 0
            } catch (_: Exception) {
                false
            }

            val iconBitmap = try {
                val drawable = resolveInfo.loadIcon(packageManager)
                drawable.toBitmap(width = 96, height = 96).asImageBitmap()
            } catch (_: Exception) {
                null
            }

            result.add(
                AppInfo(
                    packageName = pkg,
                    appName = appName,
                    isSystemApp = isSystem,
                    isLocked = false,
                    iconBitmap = iconBitmap
                )
            )
        }

        val sorted = result.sortedWith(
            compareBy<AppInfo> { it.isSystemApp }
                .thenBy { it.appName.lowercase() }
        )

        cachedApps = sorted
        sorted
    }
}
