package com.applock.privacy.feature.lock

import android.content.Context
import android.content.Intent
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Grid3x3
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
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmap
import androidx.fragment.app.FragmentActivity
import com.applock.privacy.core.monitoring.AppLockSession
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
                val currentThemeId by preferencesDataSource.selectedThemeIdFlow.collectAsState(initial = "sapphire_glass")
                val activeTheme = remember(currentThemeId) { AppThemeCatalog.getThemeById(currentThemeId) }
                val defaultLockType by preferencesDataSource.lockTypeFlow.collectAsState(initial = "pin")
                val hasPattern by preferencesDataSource.hasPatternConfiguredFlow.collectAsState(initial = false)

                LockScreenContent(
                    appName = appName,
                    appIconBitmap = appIconBitmap,
                    targetPackage = targetPackage,
                    theme = activeTheme,
                    initialLockType = defaultLockType,
                    hasPatternConfigured = hasPattern,
                    preferencesDataSource = preferencesDataSource,
                    onUnlockSuccess = {
                        AppLockSession.unlockPackage(targetPackage)
                        AppLockSession.isLockActivityShowing = false
                        finish()
                    },
                    activity = this
                )
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
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

    // Auto-prompt biometrics if available and enabled
    LaunchedEffect(Unit) {
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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .padding(horizontal = 24.dp, vertical = 28.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
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

            Spacer(modifier = Modifier.height(18.dp))

            if (errorMessage != null) {
                Text(
                    text = errorMessage ?: "",
                    color = Color(0xFFFF5252),
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Spacer(modifier = Modifier.height(18.dp))
            }

            Spacer(modifier = Modifier.height(10.dp))

            if (currentMode == "pattern" && hasPatternConfigured) {
                // Pattern Lock Mode
                PatternLockView(
                    theme = theme,
                    isError = isError,
                    onPatternComplete = { pattern ->
                        coroutineScope.launch {
                            val isCorrect = SecurityManager.verifyPattern(preferencesDataSource, pattern)
                            if (isCorrect) {
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                onUnlockSuccess()
                            } else {
                                isError = true
                                errorMessage = "Incorrect pattern"
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                delay(650)
                                isError = false
                                errorMessage = null
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
                        if (enteredPin.length < 4) {
                            val newPin = enteredPin + digit
                            enteredPin = newPin
                            isError = false
                            errorMessage = null
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                            if (newPin.length == 4) {
                                coroutineScope.launch {
                                    val isCorrect = SecurityManager.verifyPin(preferencesDataSource, newPin)
                                    if (isCorrect) {
                                        onUnlockSuccess()
                                    } else {
                                        isError = true
                                        errorMessage = "Incorrect PIN"
                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                        delay(650)
                                        enteredPin = ""
                                        isError = false
                                        errorMessage = null
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                            errorMessage = null
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                        }
                    },
                    onBiometricClick = {
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
                            currentMode = if (currentMode == "pin") "pattern" else "pin"
                            enteredPin = ""
                            errorMessage = null
                            isError = false
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
