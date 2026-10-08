package com.applock.privacy.core.monitoring

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class BootReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        if (action == Intent.ACTION_BOOT_COMPLETED ||
            action == Intent.ACTION_MY_PACKAGE_REPLACED ||
            action == "android.intent.action.QUICKBOOT_POWERON"
        ) {
            val pendingResult = goAsync()
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val preferences = AppPreferencesDataSource(context)
                    val isMonitorActive = preferences.isAppMonitorActiveFlow.first()
                    val hasUsage = PermissionManager.hasUsageStatsPermission(context)
                    val hasOverlay = PermissionManager.hasOverlayPermission(context)

                    if (isMonitorActive && hasUsage && hasOverlay) {
                        AppMonitorService.start(context)
                    }
                } finally {
                    pendingResult.finish()
                }
            }
        }
    }
}
