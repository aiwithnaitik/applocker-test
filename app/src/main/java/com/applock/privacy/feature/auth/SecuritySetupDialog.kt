package com.applock.privacy.feature.auth

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.applock.privacy.core.security.SecurityManager
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.BrightAzure
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.RoseDestructive
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LockTypeChoice {
    PIN_4,
    PIN_6,
    PATTERN
}

private enum class SetupStep {
    VERIFY_OLD,
    SELECT_TYPE,
    ENTER_NEW,
    CONFIRM_NEW
}

@Composable
fun SecuritySetupDialog(
    preferencesDataSource: AppPreferencesDataSource,
    onDismissRequest: () -> Unit,
    onSecurityConfigured: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    val hasPin by preferencesDataSource.hasPinConfiguredFlow.collectAsState(initial = false)
    val hasPattern by preferencesDataSource.hasPatternConfiguredFlow.collectAsState(initial = false)
    val currentLockType by preferencesDataSource.lockTypeFlow.collectAsState(initial = "pin")
    val currentPinLength by preferencesDataSource.pinLengthFlow.collectAsState(initial = 4)

    val hasExistingSecurity = hasPin || hasPattern

    var step by remember {
        mutableStateOf(if (hasExistingSecurity) SetupStep.VERIFY_OLD else SetupStep.SELECT_TYPE)
    }

    var selectedChoice by remember { mutableStateOf(LockTypeChoice.PIN_4) }

    // Old pass verification state
    var oldEnteredPin by remember { mutableStateOf("") }
    var oldErrorMessage by remember { mutableStateOf<String?>(null) }
    var isOldError by remember { mutableStateOf(false) }

    // New pass creation state
    var newEnteredPin by remember { mutableStateOf("") }
    var newEnteredPattern by remember { mutableStateOf("") }
    var confirmedPin by remember { mutableStateOf("") }
    var confirmedPattern by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    val defaultTheme = remember { AppThemeCatalog.getThemeById("pure_light") }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BackgroundDeep)
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.fillMaxWidth()
            ) {
                // Header Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(BrightAzure.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = BrightAzure,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                AnimatedContent(
                    targetState = step,
                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                    label = "setupStepAnim"
                ) { currentStep ->
                    when (currentStep) {
                        SetupStep.VERIFY_OLD -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Enter Current Pass",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = if (currentLockType == "pattern") "Draw your current pattern to reset" else "Enter your current $currentPinLength-digit PIN to reset",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                if (isOldError && oldErrorMessage != null) {
                                    Text(
                                        text = oldErrorMessage!!,
                                        color = RoseDestructive,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                if (currentLockType == "pattern") {
                                    PatternLockView(
                                        theme = defaultTheme,
                                        isError = isOldError,
                                        onPatternComplete = { pattern ->
                                            coroutineScope.launch {
                                                val verified = SecurityManager.verifyPattern(preferencesDataSource, pattern)
                                                if (verified) {
                                                    isOldError = false
                                                    oldErrorMessage = null
                                                    step = SetupStep.SELECT_TYPE
                                                } else {
                                                    isOldError = true
                                                    oldErrorMessage = "Incorrect pattern. Try again."
                                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                    delay(700)
                                                    isOldError = false
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    PinDotsIndicator(
                                        pinLength = currentPinLength,
                                        enteredLength = oldEnteredPin.length,
                                        isError = isOldError
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    PinKeypad(
                                        onNumberClick = { digit ->
                                            if (oldEnteredPin.length < currentPinLength) {
                                                val updated = oldEnteredPin + digit
                                                oldEnteredPin = updated
                                                isOldError = false
                                                oldErrorMessage = null

                                                if (updated.length == currentPinLength) {
                                                    coroutineScope.launch {
                                                        val verified = SecurityManager.verifyPin(preferencesDataSource, updated)
                                                        if (verified) {
                                                            isOldError = false
                                                            oldErrorMessage = null
                                                            step = SetupStep.SELECT_TYPE
                                                        } else {
                                                            isOldError = true
                                                            oldErrorMessage = "Incorrect PIN. Try again."
                                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                            delay(700)
                                                            oldEnteredPin = ""
                                                            isOldError = false
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDeleteClick = {
                                            if (oldEnteredPin.isNotEmpty()) {
                                                oldEnteredPin = oldEnteredPin.dropLast(1)
                                                isOldError = false
                                                oldErrorMessage = null
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        SetupStep.SELECT_TYPE -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = "Choose Lock Type",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Select 1 method to protect all locked apps",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                LockTypeOptionCard(
                                    title = "4-Digit PIN",
                                    subtitle = "4 numeric digits • Fast & convenient",
                                    icon = Icons.Default.Pin,
                                    isSelected = selectedChoice == LockTypeChoice.PIN_4,
                                    onClick = { selectedChoice = LockTypeChoice.PIN_4 }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                LockTypeOptionCard(
                                    title = "6-Digit PIN",
                                    subtitle = "6 numeric digits • Highest password security",
                                    icon = Icons.Default.Pin,
                                    isSelected = selectedChoice == LockTypeChoice.PIN_6,
                                    onClick = { selectedChoice = LockTypeChoice.PIN_6 }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                LockTypeOptionCard(
                                    title = "Pattern Lock",
                                    subtitle = "Connect dots on 3x3 grid • Fluid gesture",
                                    icon = Icons.Default.Grid3x3,
                                    isSelected = selectedChoice == LockTypeChoice.PATTERN,
                                    onClick = { selectedChoice = LockTypeChoice.PATTERN }
                                )

                                Spacer(modifier = Modifier.height(24.dp))

                                AppGradientButton(
                                    text = "Continue",
                                    onClick = {
                                        newEnteredPin = ""
                                        newEnteredPattern = ""
                                        confirmedPin = ""
                                        confirmedPattern = ""
                                        errorMessage = null
                                        isError = false
                                        step = SetupStep.ENTER_NEW
                                    },
                                    modifier = Modifier.fillMaxWidth(),
                                    height = 48.dp
                                )
                            }
                        }

                        SetupStep.ENTER_NEW -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (selectedChoice) {
                                        LockTypeChoice.PIN_4 -> "Enter 4-Digit PIN"
                                        LockTypeChoice.PIN_6 -> "Enter 6-Digit PIN"
                                        LockTypeChoice.PATTERN -> "Draw Pattern"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Set your new security pass",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                if (selectedChoice == LockTypeChoice.PATTERN) {
                                    PatternLockView(
                                        theme = defaultTheme,
                                        isError = false,
                                        onPatternComplete = { pattern ->
                                            if (pattern.split("-").size < 4) {
                                                errorMessage = "Connect at least 4 dots"
                                                isError = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            } else {
                                                newEnteredPattern = pattern
                                                errorMessage = null
                                                isError = false
                                                step = SetupStep.CONFIRM_NEW
                                            }
                                        }
                                    )
                                } else {
                                    val targetLen = if (selectedChoice == LockTypeChoice.PIN_4) 4 else 6
                                    PinDotsIndicator(
                                        pinLength = targetLen,
                                        enteredLength = newEnteredPin.length,
                                        isError = isError
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    PinKeypad(
                                        onNumberClick = { digit ->
                                            if (newEnteredPin.length < targetLen) {
                                                val updated = newEnteredPin + digit
                                                newEnteredPin = updated
                                                if (updated.length == targetLen) {
                                                    errorMessage = null
                                                    isError = false
                                                    step = SetupStep.CONFIRM_NEW
                                                }
                                            }
                                        },
                                        onDeleteClick = {
                                            if (newEnteredPin.isNotEmpty()) {
                                                newEnteredPin = newEnteredPin.dropLast(1)
                                            }
                                        }
                                    )
                                }
                            }
                        }

                        SetupStep.CONFIRM_NEW -> {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Text(
                                    text = when (selectedChoice) {
                                        LockTypeChoice.PIN_4, LockTypeChoice.PIN_6 -> "Confirm PIN"
                                        LockTypeChoice.PATTERN -> "Confirm Pattern"
                                    },
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = "Re-enter pass to confirm",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextMuted
                                )

                                Spacer(modifier = Modifier.height(18.dp))

                                if (isError && errorMessage != null) {
                                    Text(
                                        text = errorMessage!!,
                                        color = RoseDestructive,
                                        fontSize = 13.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Spacer(modifier = Modifier.height(10.dp))
                                }

                                if (selectedChoice == LockTypeChoice.PATTERN) {
                                    PatternLockView(
                                        theme = defaultTheme,
                                        isError = isError,
                                        onPatternComplete = { pattern ->
                                            if (pattern == newEnteredPattern) {
                                                coroutineScope.launch {
                                                    SecurityManager.setupNewPattern(preferencesDataSource, pattern)
                                                    onSecurityConfigured()
                                                    onDismissRequest()
                                                }
                                            } else {
                                                errorMessage = "Patterns did not match. Please try again."
                                                isError = true
                                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                coroutineScope.launch {
                                                    delay(800)
                                                    newEnteredPattern = ""
                                                    isError = false
                                                    errorMessage = null
                                                    step = SetupStep.ENTER_NEW
                                                }
                                            }
                                        }
                                    )
                                } else {
                                    val targetLen = if (selectedChoice == LockTypeChoice.PIN_4) 4 else 6
                                    PinDotsIndicator(
                                        pinLength = targetLen,
                                        enteredLength = confirmedPin.length,
                                        isError = isError
                                    )

                                    Spacer(modifier = Modifier.height(20.dp))

                                    PinKeypad(
                                        onNumberClick = { digit ->
                                            if (confirmedPin.length < targetLen) {
                                                val updated = confirmedPin + digit
                                                confirmedPin = updated
                                                if (updated.length == targetLen) {
                                                    if (updated == newEnteredPin) {
                                                        coroutineScope.launch {
                                                            SecurityManager.setupNewPin(preferencesDataSource, updated)
                                                            onSecurityConfigured()
                                                            onDismissRequest()
                                                        }
                                                    } else {
                                                        errorMessage = "PINs did not match. Please try again."
                                                        isError = true
                                                        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                                        coroutineScope.launch {
                                                            delay(800)
                                                            newEnteredPin = ""
                                                            confirmedPin = ""
                                                            isError = false
                                                            errorMessage = null
                                                            step = SetupStep.ENTER_NEW
                                                        }
                                                    }
                                                }
                                            }
                                        },
                                        onDeleteClick = {
                                            if (confirmedPin.isNotEmpty()) {
                                                confirmedPin = confirmedPin.dropLast(1)
                                            }
                                        }
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun LockTypeOptionCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(if (isSelected) BrightAzure.copy(alpha = 0.12f) else SurfaceCard)
            .border(
                1.5.dp,
                if (isSelected) BrightAzure else BorderSubtle.copy(alpha = 0.5f),
                RoundedCornerShape(14.dp)
            )
            .clickable(onClick = onClick)
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(if (isSelected) BrightAzure.copy(alpha = 0.2f) else Color.White.copy(alpha = 0.05f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = if (isSelected) BrightAzure else TextPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) BrightAzure else TextPrimary,
                        fontSize = 15.sp
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.5.sp
                    )
                }
            }

            if (isSelected) {
                Box(
                    modifier = Modifier
                        .size(22.dp)
                        .clip(CircleShape)
                        .background(BrightAzure),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }
        }
    }
}
