package com.applock.privacy.feature.lock

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import com.applock.privacy.core.alarm.AlarmPlayer
import com.applock.privacy.core.monitoring.AppLockSession
import com.applock.privacy.core.security.AuthResult
import com.applock.privacy.core.security.BiometricHelper
import com.applock.privacy.core.security.SecurityManager
import com.applock.privacy.core.theme.AppTheme
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.ui.theme.AppLockTheme
import com.applock.privacy.core.ui.theme.PillShape
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.auth.PatternLockView
import com.applock.privacy.feature.auth.PinDotsIndicator
import com.applock.privacy.feature.auth.PinKeypad
import com.applock.privacy.feature.intruder.IntruderCaptureManager
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class LockActivity : FragmentActivity() {

    companion object {
        const val EXTRA_PACKAGE_NAME = "extra_target_package_name"

        fun start(context: Context, packageName: String) {
            val intent = Intent(context, LockActivity::class.java).apply {
                putExtra(EXTRA_PACKAGE_NAME, packageName)
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            }
            context.startActivity(intent)
        }
    }

    private var targetPackage: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        AppLockSession.isLockActivityShowing = true

        targetPackage = intent.getStringExtra(EXTRA_PACKAGE_NAME) ?: ""

        // Prevent bypass via system back button by routing to Android launcher home
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                val homeIntent = Intent(Intent.ACTION_MAIN).apply {
                    addCategory(Intent.CATEGORY_HOME)
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK
                }
                startActivity(homeIntent)
                AppLockSession.clearSession()
                AppLockSession.isLockActivityShowing = false
                finish()
            }
        })


        val preferencesDataSource = AppPreferencesDataSource(this)
        val packageManager = packageManager

        val appName = try {
            val appInfo = packageManager.getApplicationInfo(targetPackage, 0)
            packageManager.getApplicationLabel(appInfo).toString()
        } catch (_: Exception) {
            "Protected Application"
        }

        val appIconBitmap = try {
            val drawable = packageManager.getApplicationIcon(targetPackage)
            drawable.toBitmap(width = 120, height = 120).asImageBitmap()
        } catch (_: Exception) {
            null
        }

        setContent {
            AppLockTheme {
                val currentThemeId by preferencesDataSource.selectedThemeIdFlow.collectAsState(initial = "pure_light")
                val customThemes by preferencesDataSource.customThemesFlow.collectAsState(initial = emptyList())
                val activeTheme = remember(currentThemeId, customThemes) {
                    AppThemeCatalog.getThemeById(currentThemeId, customThemes)
                }

                val defaultLockType by preferencesDataSource.lockTypeFlow.collectAsState(initial = "pin")
                val hasPattern by preferencesDataSource.hasPatternConfiguredFlow.collectAsState(initial = false)
                val isPatternVisible by preferencesDataSource.isPatternVisibleFlow.collectAsState(initial = true)
                val isHapticEnabled by preferencesDataSource.isHapticEnabledFlow.collectAsState(initial = true)
                val lockoutUntil by preferencesDataSource.lockoutUntilTimestampFlow.collectAsState(initial = 0L)

                val disguiseModeStr by preferencesDataSource.disguiseModeFlow.collectAsState(initial = "NONE")
                val isAppLockOnly by preferencesDataSource.isDisguiseAppLockOnlyFlow.collectAsState(initial = false)
                var disguiseBypassed by remember { mutableStateOf(false) }

                val disguiseMode = remember(disguiseModeStr) {
                    com.applock.privacy.feature.disguise.DisguiseMode.fromId(disguiseModeStr)
                }
                val shouldShowDisguise = !disguiseBypassed &&
                    disguiseMode != com.applock.privacy.feature.disguise.DisguiseMode.NONE &&
                    (!isAppLockOnly || targetPackage == packageName)

                if (shouldShowDisguise) {
                    when (disguiseMode) {
                        com.applock.privacy.feature.disguise.DisguiseMode.CRASH_DIALOG -> {
                            com.applock.privacy.feature.disguise.FakeCrashCover(
                                appName = appName,
                                onBypass = { disguiseBypassed = true }
                            )
                        }
                        com.applock.privacy.feature.disguise.DisguiseMode.CALCULATOR -> {
                            com.applock.privacy.feature.disguise.CalculatorDecoyCover(
                                onBypass = {
                                    AlarmPlayer.stop()
                                    AppLockSession.unlockPackage(targetPackage)
                                    AppLockSession.isLockActivityShowing = false
                                    finish()
                                },
                                onVerifyPin = { typedPin ->
                                    SecurityManager.verifyPin(preferencesDataSource, typedPin)
                                }
                            )
                        }
                        else -> {
                            disguiseBypassed = true
                        }
                    }
                } else {
                    LockScreenContent(
                        appName = appName,
                        appIconBitmap = appIconBitmap,
                        targetPackage = targetPackage,
                        theme = activeTheme,
                        initialLockType = defaultLockType,
                        hasPatternConfigured = hasPattern,
                        isPatternVisible = isPatternVisible,
                        isHapticEnabled = isHapticEnabled,
                        lockoutUntilTimestamp = lockoutUntil,
                        preferencesDataSource = preferencesDataSource,
                        onUnlockSuccess = {
                            AlarmPlayer.stop()
                            AppLockSession.unlockPackage(targetPackage)
                            AppLockSession.isLockActivityShowing = false
                            finish()
                        },
                        activity = this
                    )
                }
            }
        }
    }


    override fun onDestroy() {
        super.onDestroy()
        AlarmPlayer.stop()
        AppLockSession.isLockActivityShowing = false
    }
}

@Composable
private fun LockScreenContent(
    appName: String,
    appIconBitmap: androidx.compose.ui.graphics.ImageBitmap?,
    targetPackage: String,
    theme: AppTheme,
    initialLockType: String,
    hasPatternConfigured: Boolean,
    isPatternVisible: Boolean,
    isHapticEnabled: Boolean,
    lockoutUntilTimestamp: Long,
    preferencesDataSource: AppPreferencesDataSource,
    onUnlockSuccess: () -> Unit,
    activity: FragmentActivity
) {
    val haptic = LocalHapticFeedback.current
    var currentMode by remember { mutableStateOf(initialLockType) } // "pin" or "pattern"
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    val coroutineScope = rememberCoroutineScope()

    // Real-time lockout timer countdown
    var remainingLockoutSeconds by remember { mutableStateOf(0) }
    LaunchedEffect(lockoutUntilTimestamp) {
        while (true) {
            val now = System.currentTimeMillis()
            if (lockoutUntilTimestamp > now) {
                remainingLockoutSeconds = (((lockoutUntilTimestamp - now) / 1000L).toInt() + 1).coerceAtLeast(1)
            } else {
                remainingLockoutSeconds = 0
                break
            }
            delay(1000)
        }
    }

    val isLockedOut = remainingLockoutSeconds > 0

    // Auto-prompt biometrics if available, enabled, and not locked out
    LaunchedEffect(isLockedOut) {
        if (!isLockedOut) {
            val isBiometricEnabled = preferencesDataSource.isBiometricEnabledFlow.first()
            if (isBiometricEnabled && BiometricHelper.isBiometricAvailable(activity)) {
                delay(300)
                BiometricHelper.showBiometricPrompt(
                    activity = activity,
                    title = "Unlock $appName",
                    subtitle = "Verify biometric identity to continue",
                    negativeButtonText = "Use Code",
                    onSuccess = onUnlockSuccess,
                    onError = { /* silently allow manual PIN/Pattern */ }
                )
            }
        }
    }

    // Custom wallpaper image loading
    val customWallpaperBitmap = remember(theme.backgroundImageUri) {
        if (!theme.backgroundImageUri.isNullOrEmpty()) {
            try {
                val uri = Uri.parse(theme.backgroundImageUri)
                val stream = activity.contentResolver.openInputStream(uri)
                stream?.use { BitmapFactory.decodeStream(it)?.asImageBitmap() }
            } catch (_: Exception) {
                null
            }
        } else null
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundBrush),
        contentAlignment = Alignment.Center
    ) {
        // Render custom wallpaper if provided
        if (customWallpaperBitmap != null) {
            Image(
                bitmap = customWallpaperBitmap,
                contentDescription = null,
                contentScale = ContentScale.Crop,
                modifier = Modifier.fillMaxSize()
            )
            // Translucent glass dark overlay
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(Color.Black.copy(alpha = 0.65f))
            )
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp, vertical = 28.dp)
        ) {
            // App Icon
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(RoundedCornerShape(20.dp))
                    .background(theme.cardColor),
                contentAlignment = Alignment.Center
            ) {
                if (appIconBitmap != null) {
                    Image(
                        bitmap = appIconBitmap,
                        contentDescription = appName,
                        modifier = Modifier.size(60.dp)
                    )
                } else {
                    Box(
                        modifier = Modifier
                            .size(50.dp)
                            .clip(CircleShape)
                            .background(theme.glowColor)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = appName,
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = "Protected by AppLock Shield",
                style = MaterialTheme.typography.bodyMedium,
                color = theme.textColor.copy(alpha = 0.65f),
                fontSize = 12.sp
            )

            Spacer(modifier = Modifier.height(16.dp))

            val isAlarmActive by AlarmPlayer.isAlarmActive.collectAsState()
            if (isAlarmActive) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Color(0xFFFF1744).copy(alpha = 0.25f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockClock,
                        contentDescription = null,
                        tint = Color(0xFFFF1744),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Alarm Sounding — Unlock to Silence",
                        color = Color(0xFFFF1744),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
            }

            // Lockout banner or Error status
            if (isLockedOut) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Color(0xFFFF5252).copy(alpha = 0.18f))
                        .padding(horizontal = 14.dp, vertical = 6.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.LockClock,
                        contentDescription = null,
                        tint = Color(0xFFFF5252),
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Too many attempts. Retry in ${remainingLockoutSeconds}s",
                        color = Color(0xFFFF5252),
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            } else if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (currentMode == "pattern" && hasPatternConfigured) {
                // Pattern Lock Mode
                PatternLockView(
                    theme = theme,
                    isError = isError,
                    enabled = !isLockedOut,
                    isPatternVisible = isPatternVisible,
                    onPatternComplete = { pattern ->
                        if (isLockedOut) return@PatternLockView
                        coroutineScope.launch {
                            val result = SecurityManager.verifyPatternWithResult(preferencesDataSource, pattern)
                            when (result) {
                                is AuthResult.Success -> {
                                    if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    onUnlockSuccess()
                                }
                                is AuthResult.Failure -> {
                                    isError = true
                                    errorMessage = if (result.isNowLockedOut) "Locked out! Try again later." else "Incorrect pattern"
                                    if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                    // Intruder selfie check
                                    val isIntruderEnabled = preferencesDataSource.isIntruderDetectionEnabledFlow.first()
                                    val threshold = preferencesDataSource.intruderThresholdFlow.first()
                                    if (isIntruderEnabled && result.failedCount >= threshold) {
                                        IntruderCaptureManager.captureSilently(activity, targetPackage, appName, result.failedCount)
                                    }

                                    // Intruder alarm check
                                    val isAlarmEnabled = preferencesDataSource.isAlarmEnabledFlow.first()
                                    val alarmThreshold = preferencesDataSource.alarmThresholdFlow.first()
                                    val alarmDuration = preferencesDataSource.alarmDurationFlow.first()
                                    if (isAlarmEnabled && result.failedCount >= alarmThreshold) {
                                        AlarmPlayer.play(activity, alarmDuration)
                                    }

                                    delay(650)
                                    isError = false
                                    if (!result.isNowLockedOut) errorMessage = null
                                }
                                is AuthResult.LockedOut -> {
                                    isError = true
                                    errorMessage = "Locked out. Try again in ${result.remainingSeconds}s"
                                }
                            }
                        }
                    }
                )
            } else {
                // PIN Lock Mode
                PinDotsIndicator(
                    pinLength = 4,
                    enteredLength = enteredPin.length,
                    isError = isError
                )

                Spacer(modifier = Modifier.height(24.dp))

                PinKeypad(
                    onNumberClick = { digit ->
                        if (isLockedOut) return@PinKeypad
                        if (enteredPin.length < 4) {
                            val newPin = enteredPin + digit
                            enteredPin = newPin
                            isError = false
                            errorMessage = null
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                            if (newPin.length == 4) {
                                coroutineScope.launch {
                                    val result = SecurityManager.verifyPinWithResult(preferencesDataSource, newPin)
                                    when (result) {
                                        is AuthResult.Success -> {
                                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            onUnlockSuccess()
                                        }
                                        is AuthResult.Failure -> {
                                            isError = true
                                            errorMessage = if (result.isNowLockedOut) "Locked out! Try again later." else "Incorrect PIN"
                                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.LongPress)

                                            // Intruder selfie check
                                            val isIntruderEnabled = preferencesDataSource.isIntruderDetectionEnabledFlow.first()
                                            val threshold = preferencesDataSource.intruderThresholdFlow.first()
                                            if (isIntruderEnabled && result.failedCount >= threshold) {
                                                IntruderCaptureManager.captureSilently(activity, targetPackage, appName, result.failedCount)
                                            }

                                            // Intruder alarm check
                                            val isAlarmEnabled = preferencesDataSource.isAlarmEnabledFlow.first()
                                            val alarmThreshold = preferencesDataSource.alarmThresholdFlow.first()
                                            val alarmDuration = preferencesDataSource.alarmDurationFlow.first()
                                            if (isAlarmEnabled && result.failedCount >= alarmThreshold) {
                                                AlarmPlayer.play(activity, alarmDuration)
                                            }

                                            delay(650)
                                            enteredPin = ""
                                            isError = false
                                            if (!result.isNowLockedOut) errorMessage = null
                                        }
                                        is AuthResult.LockedOut -> {
                                            isError = true
                                            errorMessage = "Locked out. Try again in ${result.remainingSeconds}s"
                                            enteredPin = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (isLockedOut) return@PinKeypad
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                            errorMessage = null
                            if (isHapticEnabled) haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    onBiometricClick = {
                        if (isLockedOut) return@PinKeypad
                        if (BiometricHelper.isBiometricAvailable(activity)) {
                            BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                title = "Unlock $appName",
                                subtitle = "Verify biometric identity",
                                negativeButtonText = "Use Code",
                                onSuccess = onUnlockSuccess,
                                onError = { err -> errorMessage = err }
                            )
                        }
                    }
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Switch between PIN and Pattern if pattern is configured
            if (hasPatternConfigured) {
                Row(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(theme.keyColor.copy(alpha = 0.7f))
                        .clickable {
                            if (!isLockedOut) {
                                currentMode = if (currentMode == "pin") "pattern" else "pin"
                                enteredPin = ""
                                errorMessage = null
                                isError = false
                            }
                        }
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = if (currentMode == "pin") Icons.Default.Grid3x3 else Icons.Default.Pin,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = if (currentMode == "pin") "Switch to Pattern" else "Switch to PIN",
                        color = theme.accentColor,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
