package com.applock.privacy.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Grid3x3
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockClock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.Pin
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.Divider
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.applock.privacy.core.alarm.AlarmPlayer
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppSwitch
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.PillShape
import com.applock.privacy.core.ui.theme.RoseDestructive
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.core.updater.AppUpdateManager
import com.applock.privacy.core.updater.UpdateState
import com.applock.privacy.core.monitoring.AppLockSession
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.auth.SecuritySetupDialog
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateToPermissions: () -> Unit = {},
    onNavigateToIntruderLogs: () -> Unit = {},
    onNavigateToNotificationShield: () -> Unit = {},
    onNavigateToWebsiteBlocker: () -> Unit = {},
    onNavigateToDisguiseCover: () -> Unit = {},
    onNavigateToProSubscription: () -> Unit = {},
    onResetOnboarding: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }

    val isProUser by preferencesDataSource.isProUserFlow.collectAsState(initial = false)
    val isBiometricsEnabled by preferencesDataSource.isBiometricEnabledFlow.collectAsState(initial = true)

    val hasPinConfigured by preferencesDataSource.hasPinConfiguredFlow.collectAsState(initial = false)
    val hasPatternConfigured by preferencesDataSource.hasPatternConfiguredFlow.collectAsState(initial = false)
    val lockType by preferencesDataSource.lockTypeFlow.collectAsState(initial = "pin")
    val pinLength by preferencesDataSource.pinLengthFlow.collectAsState(initial = 4)

    // Phase 14: Customization
    val isPatternVisible by preferencesDataSource.isPatternVisibleFlow.collectAsState(initial = true)
    val relockImmediately by preferencesDataSource.relockImmediatelyFlow.collectAsState(initial = true)

    // Phase 15: Lockout Protection
    val failedThreshold by preferencesDataSource.failedAttemptThresholdFlow.collectAsState(initial = 3)
    val lockoutDuration by preferencesDataSource.lockoutDurationSecondsFlow.collectAsState(initial = -1)

    // Phase 16: Intruder Detection
    val isIntruderEnabled by preferencesDataSource.isIntruderDetectionEnabledFlow.collectAsState(initial = false)
    val intruderThreshold by preferencesDataSource.intruderThresholdFlow.collectAsState(initial = 3)
    val intruderLogs by preferencesDataSource.intruderLogsFlow.collectAsState(initial = emptyList())

    // Phase 17: Intruder Alarm
    val isAlarmEnabled by preferencesDataSource.isAlarmEnabledFlow.collectAsState(initial = false)
    val alarmThreshold by preferencesDataSource.alarmThresholdFlow.collectAsState(initial = 3)
    val alarmDuration by preferencesDataSource.alarmDurationFlow.collectAsState(initial = 30)
    val isAlarmPlaying by AlarmPlayer.isAlarmActive.collectAsState()

    // Phase 18 & 19
    val isWebsiteBlockerEnabled by preferencesDataSource.isWebsiteBlockerEnabledFlow.collectAsState(initial = true)

    var showSecurityDialog by remember { mutableStateOf(false) }
    var permissionStatus by remember { mutableStateOf(PermissionManager.getPermissionStatus(context)) }
    val scrollState = rememberScrollState()

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        AppLockSession.setTransientSystemAction(false)
        coroutineScope.launch {
            preferencesDataSource.setIntruderDetectionEnabled(isGranted)
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = PermissionManager.getPermissionStatus(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        AppTopBar(
            title = "Settings",
            showLogo = false
        )

        Spacer(modifier = Modifier.height(14.dp))

        // Authentication & Security
        Text(
            text = "Authentication & Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingNavigationItem(
                title = "Stealth Disguise Decoy",
                subtitle = "Conceal lock screen behind fake Crash alert",
                icon = Icons.Default.VisibilityOff,
                onClick = onNavigateToDisguiseCover
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationItem(
                title = "Lock Type & Passcode",
                subtitle = when {
                    hasPatternConfigured -> "Active • Pattern Lock (Tap to change)"
                    hasPinConfigured -> "Active • $pinLength-Digit PIN (Tap to change)"
                    else -> "Configure single security lock"
                },
                icon = Icons.Default.Shield,
                onClick = { showSecurityDialog = true }
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleItem(
                title = "Biometric Authentication",
                subtitle = "Unlock instantly using Fingerprint or Face",
                icon = Icons.Default.Fingerprint,
                checked = isBiometricsEnabled,
                onCheckedChange = { checked ->
                    coroutineScope.launch {
                        preferencesDataSource.setBiometricEnabled(checked)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Lock Screen Experience
        Text(
            text = "Lock Screen Experience",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingToggleItem(
                title = "Stealth Pattern Mode",
                subtitle = "Hide connecting lines while drawing pattern",
                icon = Icons.Default.VisibilityOff,
                checked = !isPatternVisible,
                onCheckedChange = { hideLines ->
                    coroutineScope.launch {
                        preferencesDataSource.setPatternVisible(!hideLines)
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            SettingToggleItem(
                title = "Immediate Relock",
                subtitle = "Re-lock protected apps the second you exit them",
                icon = Icons.Default.LockClock,
                checked = relockImmediately,
                onCheckedChange = { immediate ->
                    coroutineScope.launch {
                        preferencesDataSource.setRelockImmediately(immediate)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Failed Attempt Lockout Protection
        Text(
            text = "Failed Attempt Lockout",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            var showAttemptDropdown by remember { mutableStateOf(false) }

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showAttemptDropdown = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lockout Attempt Threshold",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Lock out after $failedThreshold incorrect attempts",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppStatusBadge(text = "$failedThreshold Attempts", isPositive = true)
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                    }
                }

                DropdownMenu(
                    expanded = showAttemptDropdown,
                    onDismissRequest = { showAttemptDropdown = false }
                ) {
                    (1..10).forEach { attempt ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = "$attempt Attempt${if (attempt > 1) "s" else ""}",
                                    color = if (failedThreshold == attempt) ElectricCyan else TextPrimary,
                                    fontWeight = if (failedThreshold == attempt) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                showAttemptDropdown = false
                                coroutineScope.launch {
                                    preferencesDataSource.setFailedAttemptThreshold(attempt)
                                }
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            var showDurationDropdown by remember { mutableStateOf(false) }
            val durationOptions = listOf(
                -1 to "Smart Lockout (Auto: 1m, 5m)",
                1 to "1 second",
                10 to "10 seconds",
                20 to "20 seconds",
                30 to "30 seconds",
                50 to "50 seconds",
                60 to "60 seconds",
                120 to "120 seconds"
            )

            Box(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable { showDurationDropdown = true }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Lockout Cooldown Duration",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (lockoutDuration == -1) "Smart Lockout: auto-scales from 1m to 5m+" else "$lockoutDuration seconds cooldown",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AppStatusBadge(
                            text = if (lockoutDuration == -1) "Smart" else "${lockoutDuration}s",
                            isPositive = true
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.ArrowDropDown,
                            contentDescription = null,
                            tint = ElectricCyan
                        )
                    }
                }

                DropdownMenu(
                    expanded = showDurationDropdown,
                    onDismissRequest = { showDurationDropdown = false }
                ) {
                    durationOptions.forEach { (sec, label) ->
                        DropdownMenuItem(
                            text = {
                                Text(
                                    text = label,
                                    color = if (lockoutDuration == sec) ElectricCyan else TextPrimary,
                                    fontWeight = if (lockoutDuration == sec) FontWeight.Bold else FontWeight.Normal
                                )
                            },
                            onClick = {
                                showDurationDropdown = false
                                coroutineScope.launch {
                                    preferencesDataSource.setLockoutDurationSeconds(sec)
                                }
                            }
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Intruder Detection & Selfies
        Text(
            text = "Intruder Detection & Selfies",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingToggleItem(
                title = "Capture Intruder Selfie",
                subtitle = "Silently snap front camera photo on failed unlock",
                icon = Icons.Default.CameraAlt,
                checked = isIntruderEnabled,
                onCheckedChange = { enabled ->
                    if (enabled) {
                        AppLockSession.setTransientSystemAction(true)
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    } else {
                        coroutineScope.launch {
                            preferencesDataSource.setIntruderDetectionEnabled(false)
                        }
                    }
                }
            )

            if (isIntruderEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderSubtle.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                val next = when (intruderThreshold) {
                                    1 -> 3
                                    3 -> 5
                                    else -> 1
                                }
                                preferencesDataSource.setIntruderThreshold(next)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Photo Capture Threshold",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Snap photo after $intruderThreshold failed attempt(s)",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    AppStatusBadge(text = "$intruderThreshold Attempt(s)", isPositive = true)
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationItem(
                title = "View Intruder Logs",
                subtitle = if (intruderLogs.isNotEmpty()) "${intruderLogs.size} incident(s) captured" else "No security incidents logged",
                icon = Icons.Default.Security,
                onClick = onNavigateToIntruderLogs
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Intruder Siren Alarm
        Text(
            text = "Intruder Siren Alarm",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingToggleItem(
                title = "Sound Intruder Alarm",
                subtitle = "Sound loud siren when consecutive wrong attempts occur",
                icon = Icons.Default.NotificationsActive,
                checked = isAlarmEnabled,
                onCheckedChange = { enabled ->
                    coroutineScope.launch {
                        preferencesDataSource.setAlarmEnabled(enabled)
                    }
                }
            )

            if (isAlarmEnabled) {
                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderSubtle.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                val next = when (alarmThreshold) {
                                    2 -> 3
                                    3 -> 5
                                    else -> 2
                                }
                                preferencesDataSource.setAlarmThreshold(next)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Alarm Trigger Threshold",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Trigger siren after $alarmThreshold failed unlock(s)",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    AppStatusBadge(text = "$alarmThreshold Attempt(s)", isPositive = true)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderSubtle.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            coroutineScope.launch {
                                val next = when (alarmDuration) {
                                    15 -> 30
                                    30 -> 60
                                    else -> 15
                                }
                                preferencesDataSource.setAlarmDuration(next)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "Alarm Max Duration",
                            fontWeight = FontWeight.SemiBold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = "Siren stops on legitimate unlock or after $alarmDuration seconds",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    AppStatusBadge(text = "${alarmDuration}s", isPositive = true)
                }

                Spacer(modifier = Modifier.height(10.dp))
                Divider(color = BorderSubtle.copy(alpha = 0.5f))
                Spacer(modifier = Modifier.height(10.dp))

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clickable {
                            if (isAlarmPlaying) {
                                AlarmPlayer.stop()
                            } else {
                                AlarmPlayer.play(context, alarmDuration)
                            }
                        }
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = if (isAlarmPlaying) "Stop Siren Preview" else "Test Alarm Sound",
                            fontWeight = FontWeight.SemiBold,
                            color = if (isAlarmPlaying) RoseDestructive else TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (isAlarmPlaying) "Tap to silence alarm immediately" else "Preview the alarm sound that plays on intrusion",
                            color = TextMuted,
                            fontSize = 12.sp
                        )
                    }
                    Icon(
                        imageVector = if (isAlarmPlaying) Icons.Default.VolumeOff else Icons.Default.VolumeUp,
                        contentDescription = null,
                        tint = if (isAlarmPlaying) RoseDestructive else ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Privacy & Content Shield
        Text(
            text = "Privacy & Content Shield",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingNavigationItem(
                title = "Website Blocker",
                subtitle = if (isWebsiteBlockerEnabled) "Active • Browser domain interceptor" else "Disabled",
                icon = Icons.Default.Public,
                onClick = onNavigateToWebsiteBlocker
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Permissions
        Text(
            text = "System Permissions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            PermissionItem(
                title = "Usage Access",
                description = "Required to detect foreground app switches",
                isGranted = permissionStatus.hasUsageAccess,
                onClick = {
                    val intent = Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            PermissionItem(
                title = "Display Over Other Apps",
                description = "Required to present lock shield over apps",
                isGranted = permissionStatus.hasOverlayPermission,
                onClick = {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
                        Uri.parse("package:${context.packageName}")
                    ).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            PermissionItem(
                title = "Battery Optimization Whitelist",
                description = "Prevents system from terminating monitor service",
                isGranted = permissionStatus.isIgnoringBatteryOptimizations,
                onClick = {
                    val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    context.startActivity(intent)
                }
            )

            Spacer(modifier = Modifier.height(12.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(12.dp))

            PermissionItem(
                title = "Install Unknown Apps (For OTA)",
                description = "Enables 1-tap in-app seamless APK updates",
                isGranted = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    context.packageManager.canRequestPackageInstalls()
                } else true,
                onClick = {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        val intent = Intent(
                            Settings.ACTION_MANAGE_UNKNOWN_APP_SOURCES,
                            Uri.parse("package:${context.packageName}")
                        ).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK
                        }
                        context.startActivity(intent)
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // App Updates & Diagnostics
        Text(
            text = "App Updates & Diagnostics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            val updateManager = remember { AppUpdateManager(context) }
            val updateState by updateManager.updateState.collectAsState()

            SettingNavigationItem(
                title = "Check for Updates",
                subtitle = when (updateState) {
                    is UpdateState.Checking -> "Scanning GitHub Releases..."
                    is UpdateState.Downloading -> "Downloading APK..."
                    is UpdateState.ReadyToInstall -> "Tap to launch installer!"
                    is UpdateState.UpdateAvailable -> "Update available! Tap to download"
                    is UpdateState.UpToDate -> "Version is up to date"
                    is UpdateState.Error -> (updateState as UpdateState.Error).message
                    else -> "Tap to scan or re-download latest build"
                },
                icon = Icons.Default.SystemUpdate,
                onClick = {
                    coroutineScope.launch {
                        val info = updateManager.checkForUpdates(force = true)
                        if (info != null) {
                            updateManager.downloadAndInstall(info)
                        }
                    }
                }
            )

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = BorderSubtle.copy(alpha = 0.5f))
            Spacer(modifier = Modifier.height(10.dp))

            SettingNavigationItem(
                title = "Restart Onboarding Flow",
                subtitle = "Reset onboarding & test setup flow",
                icon = Icons.Default.Refresh,
                onClick = {
                    coroutineScope.launch {
                        preferencesDataSource.resetOnboarding()
                        onResetOnboarding?.invoke()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(26.dp))
    }

    if (showSecurityDialog) {
        SecuritySetupDialog(
            preferencesDataSource = preferencesDataSource,
            onDismissRequest = { showSecurityDialog = false },
            onSecurityConfigured = { showSecurityDialog = false }
        )
    }
}

@Composable
private fun SettingToggleItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onCheckedChange(!checked) }
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
        AppSwitch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SettingNavigationItem(
    title: String,
    subtitle: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = ElectricCyan,
                modifier = Modifier.size(22.dp)
            )
            Spacer(modifier = Modifier.width(14.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
        Icon(
            imageVector = Icons.Default.ChevronRight,
            contentDescription = null,
            tint = TextMuted,
            modifier = Modifier.size(20.dp)
        )
    }
}

@Composable
private fun PermissionItem(
    title: String,
    description: String,
    isGranted: Boolean,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary,
                fontSize = 12.sp
            )
        }
        Spacer(modifier = Modifier.width(8.dp))
        AppStatusBadge(
            text = if (isGranted) "Granted" else "Action Required",
            isPositive = isGranted
        )
    }
}
