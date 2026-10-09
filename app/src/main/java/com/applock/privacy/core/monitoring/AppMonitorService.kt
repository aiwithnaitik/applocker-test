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
import android.content.pm.PackageManager
import android.content.pm.ResolveInfo
import android.content.pm.ServiceInfo
import android.os.Build
import android.app.AlarmManager
import android.os.SystemClock
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

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        // Anti-Kill Watchdog: If user swipes AppLock away from Recents or task killer intervenes, revive immediately
        val restartServiceIntent = Intent(applicationContext, AppMonitorService::class.java).also {
            it.setPackage(packageName)
        }
        val restartPendingIntent = PendingIntent.getService(
            this, 101, restartServiceIntent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        alarmManager?.set(
            AlarmManager.ELAPSED_REALTIME,
            SystemClock.elapsedRealtime() + 500,
            restartPendingIntent
        )
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
            val packageManager = packageManager
            val launcherPackages = AppLockSession.getInstalledLauncherPackages(packageManager)
            var lastObservedPackage: String? = null

            while (isActive) {
                // Pause active polling while screen is dark/idle to save battery
                if (!isScreenOn) {
                    delay(1200)
                    continue
                }

                var nextDelay = 80L

                try {
                    val hasUsagePermission = PermissionManager.hasUsageStatsPermission(this@AppMonitorService)

                    if (cachedIsMonitorEnabled && hasUsagePermission && usageStatsManager != null) {
                        val locked = cachedLockedPackages

                        if (locked.isNotEmpty()) {
                            val activePackage = AppLockSession.activeUnlockedPackage
                            val isGrace = AppLockSession.isGracePeriodActive()
                            val state = inspectForegroundState(
                                usageStatsManager = usageStatsManager,
                                activeUnlockedPackage = activePackage,
                                launcherPackages = launcherPackages,
                                lastUnlockTimestamp = AppLockSession.lastUnlockTimestamp,
                                isGracePeriod = isGrace
                            )

                            // 1. Immediate Relock: If user exited the unlocked app (pressed Home, opened Recents tabs, locked screen)
                            if (!isGrace && state.isTargetAppBackgrounded) {
                                AppLockSession.clearSession()
                            }

                            val foregroundPackage = state.foregroundPackage

                            // 2. If user is currently on home launcher
                            if (!isGrace && foregroundPackage != null && launcherPackages.contains(foregroundPackage)) {
                                AppLockSession.clearSession()
                            }

                            // 3. Intercept protected application
                            if (foregroundPackage != null &&
                                foregroundPackage != packageName &&
                                !launcherPackages.contains(foregroundPackage) &&
                                !AppLockSession.isSystemOrTransientPackage(foregroundPackage, packageName) &&
                                locked.contains(foregroundPackage)
                            ) {
                                val isAlreadyUnlocked = AppLockSession.isPackageUnlocked(foregroundPackage)
                                val currentShowing = AppLockSession.currentLockShowingPackage

                                if (!isAlreadyUnlocked && currentShowing != foregroundPackage) {
                                    AppLockSession.setLockActivityShowing(true, foregroundPackage)
                                    LockActivity.start(this@AppMonitorService, foregroundPackage)
                                    nextDelay = 50L
                                }
                            }

                            if (foregroundPackage == lastObservedPackage) {
                                nextDelay = 100L
                            } else {
                                lastObservedPackage = foregroundPackage
                                nextDelay = 70L
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

    private data class MonitoringState(
        val foregroundPackage: String?,
        val isTargetAppBackgrounded: Boolean
    )

    private fun inspectForegroundState(
        usageStatsManager: UsageStatsManager,
        activeUnlockedPackage: String?,
        launcherPackages: Set<String>,
        lastUnlockTimestamp: Long,
        isGracePeriod: Boolean
    ): MonitoringState {
        if (isGracePeriod) {
            return MonitoringState(
                foregroundPackage = activeUnlockedPackage,
                isTargetAppBackgrounded = false
            )
        }

        val now = System.currentTimeMillis()
        val events = usageStatsManager.queryEvents(now - 3500, now)
        val event = UsageEvents.Event()

        var latestResumedPkg: String? = null
        var latestResumedTime: Long = 0L

        var activePkgPostUnlockPausedOrStoppedTime: Long = 0L
        var activePkgPostUnlockResumedTime: Long = 0L

        val validPostUnlockThreshold = lastUnlockTimestamp + 600L

        while (events.hasNextEvent()) {
            events.getNextEvent(event)
            val pkg = event.packageName ?: continue
            val time = event.timeStamp

            when (event.eventType) {
                UsageEvents.Event.ACTIVITY_RESUMED -> {
                    if (time >= latestResumedTime) {
                        latestResumedTime = time
                        latestResumedPkg = pkg
                    }
                    if (pkg == activeUnlockedPackage && time >= validPostUnlockThreshold && time >= activePkgPostUnlockResumedTime) {
                        activePkgPostUnlockResumedTime = time
                    }
                }
                UsageEvents.Event.ACTIVITY_PAUSED,
                UsageEvents.Event.ACTIVITY_STOPPED -> {
                    if (pkg == activeUnlockedPackage && time >= validPostUnlockThreshold && time >= activePkgPostUnlockPausedOrStoppedTime) {
                        activePkgPostUnlockPausedOrStoppedTime = time
                    }
                }
            }
        }

        // If the active unlocked package was paused or stopped after its last resumed time, it is backgrounded!
        val isBackgrounded = if (activeUnlockedPackage != null && !isGracePeriod) {
            (activePkgPostUnlockPausedOrStoppedTime > activePkgPostUnlockResumedTime && activePkgPostUnlockPausedOrStoppedTime > 0L) ||
            (latestResumedPkg != null &&
             !AppLockSession.isSystemOrTransientPackage(latestResumedPkg, packageName) &&
             latestResumedPkg != activeUnlockedPackage &&
             latestResumedPkg != packageName)
        } else {
            false
        }

        val finalForeground = latestResumedPkg ?: run {
            val stats = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, now - 5000, now)
            stats?.maxByOrNull { it.lastTimeUsed }?.packageName
        }

        return MonitoringState(
            foregroundPackage = finalForeground,
            isTargetAppBackgrounded = isBackgrounded
        )
    }
}


