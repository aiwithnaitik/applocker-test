package com.applock.privacy.feature.onboarding

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
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
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.R
import com.applock.privacy.core.monitoring.AppMonitorService
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.core.security.SecurityManager
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.PillShape
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.auth.PatternLockView
import com.applock.privacy.feature.auth.PinDotsIndicator
import com.applock.privacy.feature.auth.PinKeypad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

enum class LockTypeOption(val title: String, val id: String) {
    PIN_4("4-digit PIN", "pin4"),
    PIN_6("6-digit PIN", "pin6"),
    PATTERN("Pattern", "pattern")
}

@Composable
fun OnboardingSetLockScreen(
    preferencesDataSource: AppPreferencesDataSource,
    onNavigateToHome: () -> Unit
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val haptic = LocalHapticFeedback.current

    var selectedLockType by remember { mutableStateOf(LockTypeOption.PIN_4) }
    var isDropdownExpanded by remember { mutableStateOf(false) }

    // Setup state
    var firstEnteredPin by remember { mutableStateOf("") }
    var confirmEnteredPin by remember { mutableStateOf("") }
    var isConfirmingPin by remember { mutableStateOf(false) }

    var firstPattern by remember { mutableStateOf<String?>(null) }
    var isConfirmingPattern by remember { mutableStateOf(false) }

    var isConfiguredSuccessfully by remember { mutableStateOf(false) }
    var isError by remember { mutableStateOf(false) }
    var statusMessage by remember { mutableStateOf<String?>("Create your unlock code") }

    val pinTargetLength = if (selectedLockType == LockTypeOption.PIN_4) 4 else 6

    fun resetInputState(newLockType: LockTypeOption? = null) {
        if (newLockType != null) {
            selectedLockType = newLockType
        }
        firstEnteredPin = ""
        confirmEnteredPin = ""
        isConfirmingPin = false
        firstPattern = null
        isConfirmingPattern = false
        isConfiguredSuccessfully = false
        isError = false
        statusMessage = when (selectedLockType) {
            LockTypeOption.PIN_4 -> "Enter a 4-digit PIN"
            LockTypeOption.PIN_6 -> "Enter a 6-digit PIN"
            LockTypeOption.PATTERN -> "Draw a pattern (connect 4+ dots)"
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // ── TOP HEADER SECTION ──────────────────────────────────────────────
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "3/3",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = ElectricCyan,
                    fontSize = 15.sp
                )

                Text(
                    text = "Set Later",
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = TextMuted,
                    fontSize = 13.sp,
                    modifier = Modifier
                        .clip(PillShape)
                        .clickable {
                            coroutineScope.launch {
                                preferencesDataSource.setOnboardingCompleted(true)
                                preferencesDataSource.setAppMonitorActive(true)
                                if (PermissionManager.hasUsageStatsPermission(context) && PermissionManager.hasOverlayPermission(context)) {
                                    AppMonitorService.start(context)
                                }
                                onNavigateToHome()
                            }
                        }
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = "Set Lock",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Text(
                text = "Choose your lock type and configure your master passkey.",
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.5.sp
            )
        }

        // ── CENTER: PURE WHITE LIVE PREVIEW CARD ────────────────────────────
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color.White)
                .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(22.dp))
                .padding(horizontal = 16.dp, vertical = 12.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                // Symmetrical horizontal row: App Logo & Protected Title
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(34.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(10.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "AppLock",
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(10.dp))

                    Column {
                        Text(
                            text = "AppLock",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF0F172A),
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Protected by AppLock Shield",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF64748B),
                            fontSize = 10.5.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // ──────────────────────────────────────────────────────────
                // Dropdown Selector: Under "Protected by AppLock Shield"
                // ──────────────────────────────────────────────────────────
                Box {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(PillShape)
                            .background(Color(0xFFF1F5F9))
                            .border(1.dp, Color(0xFFCBD5E1), PillShape)
                            .clickable { isDropdownExpanded = true }
                            .padding(horizontal = 12.dp, vertical = 5.dp)
                    ) {
                        Icon(
                            imageVector = when (selectedLockType) {
                                LockTypeOption.PATTERN -> Icons.Default.Grid3x3
                                else -> Icons.Default.Pin
                            },
                            contentDescription = null,
                            tint = Color(0xFF0284C7),
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = selectedLockType.title,
                            color = Color(0xFF0F172A),
                            fontWeight = FontWeight.Bold,
                            fontSize = 12.sp
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = "Select Lock Type",
                            tint = Color(0xFF475569),
                            modifier = Modifier.size(16.dp)
                        )
                    }

                    DropdownMenu(
                        expanded = isDropdownExpanded,
                        onDismissRequest = { isDropdownExpanded = false },
                        modifier = Modifier
                            .background(Color.White)
                            .border(1.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    ) {
                        LockTypeOption.entries.forEach { option ->
                            DropdownMenuItem(
                                text = {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Icon(
                                            imageVector = when (option) {
                                                LockTypeOption.PATTERN -> Icons.Default.Grid3x3
                                                else -> Icons.Default.Pin
                                            },
                                            contentDescription = null,
                                            tint = if (selectedLockType == option) Color(0xFF0284C7) else Color(0xFF64748B),
                                            modifier = Modifier.size(15.dp)
                                        )
                                        Spacer(modifier = Modifier.width(8.dp))
                                        Text(
                                            text = option.title,
                                            color = if (selectedLockType == option) Color(0xFF0284C7) else Color(0xFF0F172A),
                                            fontWeight = if (selectedLockType == option) FontWeight.Bold else FontWeight.Medium,
                                            fontSize = 12.5.sp
                                        )
                                    }
                                },
                                onClick = {
                                    isDropdownExpanded = false
                                    resetInputState(option)
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(6.dp))

                // Status Prompt
                Text(
                    text = when {
                        isConfiguredSuccessfully -> "✓ Lock Configured Successfully!"
                        isError -> statusMessage ?: "Incorrect input"
                        isConfirmingPin || isConfirmingPattern -> "Confirm your code"
                        else -> statusMessage ?: "Enter your code"
                    },
                    color = when {
                        isConfiguredSuccessfully -> EmeraldSecure
                        isError -> Color(0xFFFF3B30)
                        else -> Color(0xFF334155)
                    },
                    fontSize = 11.5.sp,
                    fontWeight = FontWeight.SemiBold
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ──────────────────────────────────────────────────────────
                // Input Component (Pattern or PIN)
                // ──────────────────────────────────────────────────────────
                if (selectedLockType == LockTypeOption.PATTERN) {
                    PatternLockView(
                        theme = AppThemeCatalog.PureLight,
                        isError = isError,
                        enabled = !isConfiguredSuccessfully,
                        isPatternVisible = true,
                        onPatternComplete = { pattern ->
                            if (isConfiguredSuccessfully) return@PatternLockView
                            if (pattern.length < 4) {
                                isError = true
                                statusMessage = "Connect at least 4 dots"
                                coroutineScope.launch {
                                    delay(700)
                                    isError = false
                                    statusMessage = "Draw pattern again"
                                }
                                return@PatternLockView
                            }

                            if (!isConfirmingPattern) {
                                firstPattern = pattern
                                isConfirmingPattern = true
                                statusMessage = "Draw pattern again to confirm"
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            } else {
                                if (pattern == firstPattern) {
                                    isConfiguredSuccessfully = true
                                    isError = false
                                    statusMessage = "Pattern configured!"
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        SecurityManager.setupNewPattern(preferencesDataSource, pattern)
                                        preferencesDataSource.setLockType("pattern")
                                    }
                                } else {
                                    isError = true
                                    statusMessage = "Patterns do not match. Try again."
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    coroutineScope.launch {
                                        delay(800)
                                        resetInputState()
                                    }
                                }
                            }
                        },
                        modifier = Modifier.size(190.dp)
                    )
                } else {
                    // PIN Mode (4-digit or 6-digit)
                    val activePin = if (isConfirmingPin) confirmEnteredPin else firstEnteredPin

                    PinDotsIndicator(
                        pinLength = pinTargetLength,
                        enteredLength = activePin.length,
                        isError = isError,
                        activeColor = Color(0xFF0284C7),
                        emptyBorderColor = Color(0xFFCBD5E1)
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    PinKeypad(
                        onNumberClick = { digit ->
                            if (isConfiguredSuccessfully) return@PinKeypad
                            if (!isConfirmingPin) {
                                if (firstEnteredPin.length < pinTargetLength) {
                                    firstEnteredPin += digit
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                    if (firstEnteredPin.length == pinTargetLength) {
                                        isConfirmingPin = true
                                        statusMessage = "Confirm your $pinTargetLength-digit PIN"
                                    }
                                }
                            } else {
                                if (confirmEnteredPin.length < pinTargetLength) {
                                    confirmEnteredPin += digit
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)

                                    if (confirmEnteredPin.length == pinTargetLength) {
                                        if (confirmEnteredPin == firstEnteredPin) {
                                            isConfiguredSuccessfully = true
                                            isError = false
                                            statusMessage = "PIN Configured!"
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            coroutineScope.launch {
                                                SecurityManager.setupNewPin(preferencesDataSource, confirmEnteredPin)
                                                preferencesDataSource.setPinLength(pinTargetLength)
                                                preferencesDataSource.setLockType("pin")
                                            }
                                        } else {
                                            isError = true
                                            statusMessage = "PINs do not match. Try again."
                                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                            coroutineScope.launch {
                                                delay(800)
                                                resetInputState()
                                            }
                                        }
                                    }
                                }
                            }
                        },
                        onDeleteClick = {
                            if (isConfiguredSuccessfully) return@PinKeypad
                            if (isConfirmingPin) {
                                if (confirmEnteredPin.isNotEmpty()) {
                                    confirmEnteredPin = confirmEnteredPin.dropLast(1)
                                } else {
                                    isConfirmingPin = false
                                    firstEnteredPin = ""
                                    statusMessage = "Enter a $pinTargetLength-digit PIN"
                                }
                            } else {
                                if (firstEnteredPin.isNotEmpty()) {
                                    firstEnteredPin = firstEnteredPin.dropLast(1)
                                }
                            }
                        },
                        keyColor = Color(0xFFF1F5F9),
                        textColor = Color(0xFF0F172A),
                        borderColor = Color(0xFFE2E8F0),
                        keySize = 48.dp,
                        rowSpacing = 6.dp,
                        fontSize = 20.sp
                    )
                }
            }
        }

        // ── BOTTOM BUTTON ───────────────────────────────────────────────────
        Column(modifier = Modifier.fillMaxWidth()) {
            AppGradientButton(
                text = if (isConfiguredSuccessfully) "Get Started" else "Continue",
                onClick = {
                    coroutineScope.launch {
                        preferencesDataSource.setOnboardingCompleted(true)
                        preferencesDataSource.setAppMonitorActive(true)
                        if (PermissionManager.hasUsageStatsPermission(context) && PermissionManager.hasOverlayPermission(context)) {
                            AppMonitorService.start(context)
                        }
                        onNavigateToHome()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                height = 46.dp
            )
        }
    }
}
