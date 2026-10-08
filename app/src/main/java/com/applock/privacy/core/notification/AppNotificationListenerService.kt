package com.applock.privacy.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.os.Build
import android.service.notification.NotificationListenerService
import android.service.notification.StatusBarNotification
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.applock.privacy.R
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AppNotificationListenerService : NotificationListenerService() {

    private val serviceScope = CoroutineScope(Dispatchers.IO)
    private lateinit var preferencesDataSource: AppPreferencesDataSource

    override fun onCreate() {
        super.onCreate()
        preferencesDataSource = AppPreferencesDataSource(applicationContext)
        createNotificationChannel()
        Log.i(TAG, "AppNotificationListenerService started")
    }

    override fun onNotificationPosted(sbn: StatusBarNotification?) {
        super.onNotificationPosted(sbn)
        if (sbn == null) return

        val targetPackage = sbn.packageName ?: return

        // Never intercept notifications from ourselves or system UI
        if (targetPackage == packageName || targetPackage == "android" || targetPackage == "com.android.systemui") {
            return
        }

        // Avoid re-masking our own posted notifications
        if (sbn.tag == SHIELD_NOTIFICATION_TAG) {
            return
        }

        serviceScope.launch {
            try {
                val isShieldEnabled = preferencesDataSource.isNotificationShieldEnabledFlow.first()
                if (!isShieldEnabled) return@launch

                val lockedPackages = preferencesDataSource.lockedPackagesFlow.first()
                if (lockedPackages.contains(targetPackage)) {
                    Log.i(TAG, "Intercepting sensitive notification from locked package: $targetPackage")

                    // Extract app name and icon
                    val pm = packageManager
                    val appName = try {
                        val appInfo = pm.getApplicationInfo(targetPackage, 0)
                        pm.getApplicationLabel(appInfo).toString()
                    } catch (_: Exception) {
                        "Protected App"
                    }

                    val appIcon = try {
                        val drawable = pm.getApplicationIcon(targetPackage)
                        drawableToBitmap(drawable)
                    } catch (_: Exception) {
                        null
                    }

                    // Cancel the original sensitive notification
                    cancelNotification(sbn.key)

                    // Post privacy-masked notification
                    postMaskedNotification(targetPackage, appName, appIcon, sbn.id)
                }
            } catch (e: Exception) {
                Log.e(TAG, "Failed handling notification interception", e)
            }
        }
    }

    private fun postMaskedNotification(targetPackage: String, appName: String, appIcon: Bitmap?, notificationId: Int) {
        val launchIntent = packageManager.getLaunchIntentForPackage(targetPackage) ?: Intent()
        val pendingIntent = PendingIntent.getActivity(
            this,
            targetPackage.hashCode(),
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val builder = NotificationCompat.Builder(this, CHANNEL_SHIELD_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(appName)
            .setContentText("New message hidden for privacy")
            .setSubText("AppLock Privacy Shield")
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        if (appIcon != null) {
            builder.setLargeIcon(appIcon)
        }

        try {
            val notificationManager = NotificationManagerCompat.from(this)
            notificationManager.notify(SHIELD_NOTIFICATION_TAG, notificationId, builder.build())
        } catch (e: SecurityException) {
            Log.e(TAG, "Notification permission missing to post masked notification", e)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "AppLock Privacy Shield"
            val descriptionText = "Masks sensitive notification contents from locked applications"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_SHIELD_ID, name, importance).apply {
                description = descriptionText
            }
            val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun drawableToBitmap(drawable: Drawable): Bitmap {
        if (drawable is BitmapDrawable && drawable.bitmap != null) {
            return drawable.bitmap
        }
        val bitmap = Bitmap.createBitmap(
            if (drawable.intrinsicWidth > 0) drawable.intrinsicWidth else 96,
            if (drawable.intrinsicHeight > 0) drawable.intrinsicHeight else 96,
            Bitmap.Config.ARGB_8888
        )
        val canvas = Canvas(bitmap)
        drawable.setBounds(0, 0, canvas.width, canvas.height)
        drawable.draw(canvas)
        return bitmap
    }

    companion object {
        const val TAG = "AppNotificationListener"
        const val CHANNEL_SHIELD_ID = "applock_notification_shield_channel"
        const val SHIELD_NOTIFICATION_TAG = "applock_shield_tag"
    }
}
