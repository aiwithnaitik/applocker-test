package com.applock.privacy.core.monitoring

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.app.usage.UsageEvents
import android.app.usage.UsageStatsManager
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationCompat
import com.applock.privacy.MainActivity
import com.applock.privacy.R
import com.applock.privacy.core.common.AppConstants
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.lock.LockActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class AppMonitorService : Service() {

    companion object {
        fun start(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(intent)
            } else {
                context.startService(intent)
            }
        }

        fun stop(context: Context) {
            val intent = Intent(context, AppMonitorService::class.java)
            context.stopService(intent)
        }
    }

    private val serviceScope = CoroutineScope(Dispatchers.Default + Job())
    private lateinit var preferencesDataSource: AppPreferencesDataSource
    private var screenOffReceiver: BroadcastReceiver? = null

    @Volatile
    private var isScreenOn = true

    @Volatile
    private var cachedIsMonitorEnabled = true

    @Volatile
    private var cachedLockedPackages: Set<String> = emptySet()

    override fun onCreate() {
        super.onCreate()
        preferencesDataSource = AppPreferencesDataSource(this)
        createNotificationChannel()
        startAsForeground()
        registerScreenStateReceiver()
        observePreferencesHotState()
        startMonitoringLoop()
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        serviceScope.cancel()
        screenOffReceiver?.let {
            try {
                unregisterReceiver(it)
            } catch (_: Exception) {}
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                AppConstants.CHANNEL_ID_MONITOR,
                "AppLock Protection Shield",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "Notifies that background app monitoring and security are running"
                setShowBadge(false)
            }
            val manager = getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    private fun startAsForeground() {
        val launchIntent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            launchIntent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification: Notification = NotificationCompat.Builder(this, AppConstants.CHANNEL_ID_MONITOR)
            .setContentTitle("AppLock Shield Active")
            .setContentText("Keeping your selected personal apps locked & secure")
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            startForeground(
                AppConstants.NOTIFICATION_ID_MONITOR,
                notification,
                ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC
            )
        } else {
            startForeground(AppConstants.NOTIFICATION_ID_MONITOR, notification)
        }
    }

    private fun observePreferencesHotState() {

        serviceScope.launch {
            preferencesDataSource.isAppMonitorActiveFlow.collect { enabled ->
                cachedIsMonitorEnabled = enabled
            }
        }
        serviceScope.launch {
            preferencesDataSource.lockedPackagesFlow.collect { packages ->
                cachedLockedPackages = packages
            }
        }
    }

    private fun registerScreenStateReceiver() {
        screenOffReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                when (intent?.action) {
                    Intent.ACTION_SCREEN_OFF -> {
                        isScreenOn = false
                        // Lock all temporarily unlocked apps when device screen turns off
                        AppLockSession.clearSession()
                    }
                    Intent.ACTION_SCREEN_ON -> {
                        isScreenOn = true
                    }
                }
            }
        }
        val filter = IntentFilter().apply {
            addAction(Intent.ACTION_SCREEN_OFF)
            addAction(Intent.ACTION_SCREEN_ON)
        }
        registerReceiver(screenOffReceiver, filter)
    }

    private fun startMonitoringLoop() {
        serviceScope.launch {
            val usageStatsManager = getSystemService(Context.USAGE_STATS_SERVICE) as? UsageStatsManager
            var lastObservedPackage: String? = null

            while (isActive) {
                // Phase 24 Optimization: Screen-Off Battery Saver
                // Completely pause active polling while screen is dark/idle
                if (!isScreenOn) {
                    delay(1500)
                    continue
                }

                var nextDelay = 300L

                try {
                    val hasUsagePermission = PermissionManager.hasUsageStatsPermission(this@AppMonitorService)

                    if (cachedIsMonitorEnabled && hasUsagePermission && usageStatsManager != null) {
                        val locked = cachedLockedPackages

                        if (locked.isNotEmpty()) {
                            val foregroundPackage = detectForegroundPackage(usageStatsManager)

                            // Immediate Relock: If user exited or switched away from the unlocked app, revoke unlock!
                            AppLockSession.onForegroundPackageChanged(foregroundPackage)

                            if (foregroundPackage != null &&
                                foregroundPackage != packageName &&
                                locked.contains(foregroundPackage)
                            ) {
                                val isAlreadyUnlocked = AppLockSession.isPackageUnlocked(foregroundPackage)
                                val isLockShowing = AppLockSession.isLockActivityShowing

                                if (!isAlreadyUnlocked && !isLockShowing) {
                                    LockActivity.start(this@AppMonitorService, foregroundPackage)
                                    nextDelay = 150L // Tight polling during intercept
                                }
                            }

                            // Adaptive rate: if user stays on the same package, relax delay to 350ms
                            if (foregroundPackage == lastObservedPackage) {
                                nextDelay = 350L
                            } else {
                                lastObservedPackage = foregroundPackage
                                nextDelay = 180L
                            }
                        }
                    }
                } catch (_: Exception) {
                    // Prevent any polling exception from crashing foreground service
                }
                delay(nextDelay)
            }
        }
    }

    private fun detectForegroundPackage(usageStatsManager: UsageStatsManager): String? {
        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 4000, now)
        val event = UsageEvents.Event()
        var lastForeground: String? = null

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            if (event.eventType == UsageEvents.Event.ACTIVITY_RESUMED) {
                lastForeground = event.packageName
            }
        }

        if (lastForeground != null) {
            return lastForeground
        }

        // Secondary fallback
        val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 6000, now)
        return stats?.maxByOrNull { it.lastTimeUsed }?.packageName
    }
}


