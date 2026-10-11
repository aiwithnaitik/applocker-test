package com.applock.privacy.feature.auth

import android.app.Activity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.biometric.BiometricHelper
import com.applock.privacy.core.security.AuthResult
import com.applock.privacy.core.security.SecurityManager
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BrightAzure
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.RoseDestructive
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun AppLockMasterLockScreen(
    onUnlockSuccess: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }

    val lockType by preferencesDataSource.lockTypeFlow.collectAsState(initial = "pin")
    val pinLength by preferencesDataSource.pinLengthFlow.collectAsState(initial = 4)
    val isBiometricEnabled by preferencesDataSource.isBiometricEnabledFlow.collectAsState(initial = true)
    val defaultTheme = remember { AppThemeCatalog.getThemeById("pure_light") }

    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    // Auto-prompt biometric on launch if enabled and supported
    LaunchedEffect(Unit) {
        val activity = context as? Activity
        if (activity != null && isBiometricEnabled && BiometricHelper.isBiometricAvailable(activity)) {
            BiometricHelper.showBiometricPrompt(
                activity = activity,
                title = "Unlock AppLock",
                subtitle = "Verify fingerprint or face to open AppLock",
                negativeButtonText = "Use Pass",
                onSuccess = onUnlockSuccess,
                onError = {}
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxSize()
        ) {
            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(BrightAzure.copy(alpha = 0.16f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = null,
                    tint = BrightAzure,
                    modifier = Modifier.size(36.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "AppLock",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.ExtraBold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = if (lockType == "pattern") "Draw your pattern to access AppLock" else "Enter your PIN to access AppLock",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            if (isError && errorMessage != null) {
                Text(
                    text = errorMessage!!,
                    color = RoseDestructive,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.SemiBold
                )
            } else {
                Spacer(modifier = Modifier.height(20.dp))
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (lockType == "pattern") {
                PatternLockView(
                    theme = defaultTheme,
                    isError = isError,
                    onPatternComplete = { pattern ->
                        coroutineScope.launch {
                            val result = SecurityManager.verifyPatternWithResult(preferencesDataSource, pattern)
                            when (result) {
                                is AuthResult.Success -> {
                                    onUnlockSuccess()
                                }
                                is AuthResult.Failure -> {
                                    isError = true
                                    errorMessage = if (result.isNowLockedOut) "Locked out! Try again later." else "Incorrect pattern"
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                PinDotsIndicator(
                    pinLength = pinLength,
                    enteredLength = enteredPin.length,
                    isError = isError
                )

                Spacer(modifier = Modifier.height(28.dp))

                PinKeypad(
                    onNumberClick = { digit ->
                        if (enteredPin.length < pinLength) {
                            val updated = enteredPin + digit
                            enteredPin = updated
                            isError = false
                            errorMessage = null

                            if (updated.length == pinLength) {
                                coroutineScope.launch {
                                    val result = SecurityManager.verifyPinWithResult(preferencesDataSource, updated)
                                    when (result) {
                                        is AuthResult.Success -> {
                                            onUnlockSuccess()
                                        }
                                        is AuthResult.Failure -> {
                                            isError = true
                                            errorMessage = if (result.isNowLockedOut) "Locked out! Try again later." else "Incorrect PIN"
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
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
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                            errorMessage = null
                        }
                    },
                    onBiometricClick = {
                        val activity = context as? Activity ?: return@PinKeypad
                        if (BiometricHelper.isBiometricAvailable(activity)) {
                            BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                title = "Unlock AppLock",
                                subtitle = "Verify biometric identity",
                                negativeButtonText = "Use PIN",
                                onSuccess = onUnlockSuccess,
                                onError = { err -> errorMessage = err }
                            )
                        }
                    }
                )
            }
        }
    }
}
