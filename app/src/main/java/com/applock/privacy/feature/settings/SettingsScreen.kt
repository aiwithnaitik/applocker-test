package com.applock.privacy.feature.settings

import android.Manifest
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.Settings
import androidx.activity.compose.BackHandler
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
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Divider
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
import com.applock.privacy.core.ui.theme.BrightAzure
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
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.auth.PatternSetupDialog
import com.applock.privacy.feature.auth.PinSetupDialog
import kotlinx.coroutines.launch

enum class SettingsCategory(
    val title: String,
    val summary: String,
    val icon: ImageVector,
    val accentColor: Color
) {
    SECURITY(
        title = "Authentication & Security",
        summary = "PIN, Pattern, Biometrics & Decoy mode",
        icon = Icons.Default.Lock,
        accentColor = Color(0xFF0284C7)
    ),
    EXPERIENCE(
        title = "Lock Screen Experience",
        summary = "Stealth lines, tactile haptics & immediate relock",
        icon = Icons.Default.LockClock,
        accentColor = Color(0xFF7C3AED)
    ),
    LOCKOUT(
        title = "Failed Attempt Lockout",
        summary = "Failed limits threshold & lockout cooldown timer",
        icon = Icons.Default.LockClock,
        accentColor = Color(0xFFD97706)
    ),
    INTRUDER(
        title = "Intruder Detection & Selfies",
        summary = "Front camera photo captures & audit incident logs",
        icon = Icons.Default.CameraAlt,
        accentColor = Color(0xFF059669)
    ),
    ALARM(
        title = "Intruder Siren Alarm",
        summary = "Loud audio siren trigger & sound test preview",
        icon = Icons.Default.NotificationsActive,
        accentColor = Color(0xFFE11D48)
    ),
    SHIELD(
        title = "Privacy & Content Shield",
        summary = "Notification message concealment & web blocker",
        icon = Icons.Default.Shield,
        accentColor = Color(0xFF2563EB)
    ),
    PERMISSIONS(
        title = "System Permissions Center",
        summary = "Usage stats, display overlays & battery whitelist",
        icon = Icons.Default.Security,
        accentColor = Color(0xFF0D9488)
    ),
    SYSTEM(
        title = "App Updates & Diagnostics",
        summary = "Scan for GitHub OTA updates & test onboarding reset",
        icon = Icons.Default.SystemUpdate,
        accentColor = Color(0xFF475569)
    )
}

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

    // Phase 14: Customization
    val isPatternVisible by preferencesDataSource.isPatternVisibleFlow.collectAsState(initial = true)
    val isHapticEnabled by preferencesDataSource.isHapticEnabledFlow.collectAsState(initial = true)
    val relockImmediately by preferencesDataSource.relockImmediatelyFlow.collectAsState(initial = true)

    // Phase 15: Lockout Protection
    val failedThreshold by preferencesDataSource.failedAttemptThresholdFlow.collectAsState(initial = 3)
    val lockoutDuration by preferencesDataSource.lockoutDurationSecondsFlow.collectAsState(initial = 60)

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
    val isNotificationShieldEnabled by preferencesDataSource.isNotificationShieldEnabledFlow.collectAsState(initial = false)
    val isWebsiteBlockerEnabled by preferencesDataSource.isWebsiteBlockerEnabledFlow.collectAsState(initial = true)

    var showPinDialog by remember { mutableStateOf(false) }
    var showPatternDialog by remember { mutableStateOf(false) }
    var permissionStatus by remember { mutableStateOf(PermissionManager.getPermissionStatus(context)) }
    var selectedCategory by remember { mutableStateOf<SettingsCategory?>(null) }
    val scrollState = rememberScrollState()

    BackHandler(enabled = selectedCategory != null) {
        selectedCategory = null
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
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
        // Top Bar: Shows Back arrow if inside a category, plain title without logo otherwise
        if (selectedCategory == null) {
            AppTopBar(
                title = "Settings",
                showLogo = false
            )
        } else {
            AppTopBar(
                title = selectedCategory!!.title,
                showLogo = false,
                onNavigateBack = { selectedCategory = null }
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        if (selectedCategory == null) {
            // ── ROOT SETTINGS VIEW ──────────────────────────────────────────
            // 1. Upgraded Luxury "Upgrade to AppLock Pro" Hero Card
            ProVipSettingsCard(
                isProUser = isProUser,
                onClick = onNavigateToProSubscription
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 2. Category Hub Section
            Text(
                text = "SETTINGS CATEGORIES",
                style = MaterialTheme.typography.labelSmall,
                color = TextMuted,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                fontSize = 11.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                for (category in SettingsCategory.values()) {
                    val badgeText = when (category) {
                        SettingsCategory.SECURITY -> if (hasPinConfigured || hasPatternConfigured) "Configured" else "Setup"
                        SettingsCategory.EXPERIENCE -> if (isHapticEnabled) "Haptics On" else null
                        SettingsCategory.LOCKOUT -> "$failedThreshold Attempts"
                        SettingsCategory.INTRUDER -> if (isIntruderEnabled) "Active" else "Off"
                        SettingsCategory.ALARM -> if (isAlarmEnabled) "Active" else "Off"
                        SettingsCategory.SHIELD -> if (isNotificationShieldEnabled || isWebsiteBlockerEnabled) "Protected" else null
                        SettingsCategory.PERMISSIONS -> if (permissionStatus.isCorePermissionsGranted) "Granted" else "Action"
                        SettingsCategory.SYSTEM -> "OTA"
                    }

                    CategoryItemCard(
                        category = category,
                        badgeText = badgeText,
                        onClick = { selectedCategory = category }
                    )
                }
            }

            Spacer(modifier = Modifier.height(28.dp))
        } else {
            // ── CATEGORY DETAIL SETTINGS VIEW ───────────────────────────────
            Text(
                text = selectedCategory!!.summary,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 12.sp,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            when (selectedCategory!!) {
                SettingsCategory.SECURITY -> {
                    AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                        SettingNavigationItem(
                            title = "Stealth Disguise Decoy",
                            subtitle = "Conceal lock screen behind Crash alert or Calculator",
                            icon = Icons.Default.VisibilityOff,
                            onClick = onNavigateToDisguiseCover
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderSubtle.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingNavigationItem(
                            title = if (hasPinConfigured) "Change Security PIN" else "Set Security PIN",
                            subtitle = if (hasPinConfigured) "Salted SHA-256 PIN active" else "Configure master PIN",
                            icon = Icons.Default.Pin,
                            onClick = { showPinDialog = true }
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderSubtle.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingNavigationItem(
                            title = if (hasPatternConfigured) "Change Lock Pattern" else "Set Lock Pattern",
                            subtitle = if (hasPatternConfigured) "Interactive 3x3 pattern active" else "Configure stealth unlock pattern",
                            icon = Icons.Default.Grid3x3,
                            onClick = { showPatternDialog = true }
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

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderSubtle.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        val next = if (lockType == "pin") "pattern" else "pin"
                                        preferencesDataSource.setLockType(next)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Lock,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Default Lock Screen Mode",
                                        style = MaterialTheme.typography.bodyLarge,
                                        fontWeight = FontWeight.SemiBold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "Preferred style: ${lockType.uppercase()}",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            AppStatusBadge(
                                text = lockType.uppercase(),
                                isPositive = true
                            )
                        }
                    }
                }

                SettingsCategory.EXPERIENCE -> {
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
                            title = "Tactile Haptics",
                            subtitle = "Vibrate on keypad clicks and pattern nodes",
                            icon = Icons.Default.Vibration,
                            checked = isHapticEnabled,
                            onCheckedChange = { enabled ->
                                coroutineScope.launch {
                                    preferencesDataSource.setHapticEnabled(enabled)
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
                }

                SettingsCategory.LOCKOUT -> {
                    AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        val nextThreshold = when (failedThreshold) {
                                            3 -> 5
                                            5 -> 10
                                            else -> 3
                                        }
                                        preferencesDataSource.setFailedAttemptThreshold(nextThreshold)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
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
                            AppStatusBadge(text = "$failedThreshold Attempts", isPositive = true)
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderSubtle.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable {
                                    coroutineScope.launch {
                                        val nextDuration = when (lockoutDuration) {
                                            30 -> 60
                                            60 -> 300
                                            else -> 30
                                        }
                                        preferencesDataSource.setLockoutDurationSeconds(nextDuration)
                                    }
                                }
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Column {
                                Text(
                                    text = "Lockout Cooldown Duration",
                                    fontWeight = FontWeight.SemiBold,
                                    color = TextPrimary,
                                    fontSize = 15.sp
                                )
                                Text(
                                    text = if (lockoutDuration >= 60) "${lockoutDuration / 60} minute(s)" else "$lockoutDuration seconds",
                                    color = TextMuted,
                                    fontSize = 12.sp
                                )
                            }
                            AppStatusBadge(
                                text = if (lockoutDuration >= 60) "${lockoutDuration / 60}m" else "${lockoutDuration}s",
                                isPositive = true
                            )
                        }
                    }
                }

                SettingsCategory.INTRUDER -> {
                    AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                        SettingToggleItem(
                            title = "Capture Intruder Selfie",
                            subtitle = "Silently snap front camera photo on failed unlock",
                            icon = Icons.Default.CameraAlt,
                            checked = isIntruderEnabled,
                            onCheckedChange = { enabled ->
                                if (enabled) {
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
                }

                SettingsCategory.ALARM -> {
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
                }

                SettingsCategory.SHIELD -> {
                    AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                        SettingNavigationItem(
                            title = "Notification Shield",
                            subtitle = if (isNotificationShieldEnabled) "Enabled • Conceals sensitive notifications" else "Disabled",
                            icon = Icons.Default.NotificationsActive,
                            onClick = onNavigateToNotificationShield
                        )

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderSubtle.copy(alpha = 0.5f))
                        Spacer(modifier = Modifier.height(10.dp))

                        SettingNavigationItem(
                            title = "Website Blocker",
                            subtitle = if (isWebsiteBlockerEnabled) "Active • Browser domain interceptor" else "Disabled",
                            icon = Icons.Default.Public,
                            onClick = onNavigateToWebsiteBlocker
                        )
                    }
                }

                SettingsCategory.PERMISSIONS -> {
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
                }

                SettingsCategory.SYSTEM -> {
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
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Back button pill at bottom
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9))
                    .clickable { selectedCategory = null }
                    .padding(vertical = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "← Return to Categories",
                    color = ElectricCyan,
                    fontWeight = FontWeight.SemiBold,
                    fontSize = 13.5.sp
                )
            }

            Spacer(modifier = Modifier.height(26.dp))
        }
    }

    if (showPinDialog) {
        PinSetupDialog(
            preferencesDataSource = preferencesDataSource,
            onDismissRequest = { showPinDialog = false },
            onPinCreated = { showPinDialog = false }
        )
    }

    if (showPatternDialog) {
        PatternSetupDialog(
            preferencesDataSource = preferencesDataSource,
            onDismissRequest = { showPatternDialog = false },
            onPatternCreated = { showPatternDialog = false }
        )
    }
}

/**
 * Upgraded Luxury "Upgrade to AppLock Pro" Hero Card
 */
@Composable
private fun ProVipSettingsCard(
    isProUser: Boolean,
    onClick: () -> Unit
) {
    if (isProUser) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(Color(0xFFECFDF5), Color(0xFFF0FDF4), Color(0xFFFFFFFF))
                    )
                )
                .border(1.dp, EmeraldSecure.copy(alpha = 0.4f), RoundedCornerShape(20.dp))
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(13.dp))
                            .background(EmeraldSecure.copy(alpha = 0.15f))
                            .border(1.dp, EmeraldSecure.copy(alpha = 0.35f), RoundedCornerShape(13.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.WorkspacePremium,
                            contentDescription = null,
                            tint = EmeraldSecure,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(13.dp))

                    Column {
                        Text(
                            text = "AppLock VIP Member",
                            color = TextPrimary,
                            fontSize = 15.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "All premium protections active & unlocked",
                            color = TextSecondary,
                            fontSize = 12.sp
                        )
                    }
                }

                AppStatusBadge(
                    text = "VIP ACTIVE",
                    color = EmeraldSecure
                )
            }
        }
    } else {
        // Luxury Obsidian & Amber-Gold Gradient Pro Showcase Card
        val goldAccent = Color(0xFFF59E0B)
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(20.dp))
                .background(
                    Brush.linearGradient(
                        colors = listOf(
                            Color(0xFF0F172A), // Slate 900
                            Color(0xFF1E293B), // Slate 800
                            Color(0xFF191F33)
                        )
                    )
                )
                .border(
                    1.2.dp,
                    Brush.horizontalGradient(
                        colors = listOf(
                            goldAccent.copy(alpha = 0.6f),
                            ElectricCyan.copy(alpha = 0.45f),
                            goldAccent.copy(alpha = 0.3f)
                        )
                    ),
                    RoundedCornerShape(20.dp)
                )
                .clickable(onClick = onClick)
                .padding(16.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically, modifier = Modifier.weight(1f)) {
                        Box(
                            modifier = Modifier
                                .size(46.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(goldAccent.copy(alpha = 0.18f))
                                .border(1.2.dp, goldAccent.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.WorkspacePremium,
                                contentDescription = null,
                                tint = Color(0xFFFBBF24),
                                modifier = Modifier.size(26.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(13.dp))

                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    text = "Upgrade to AppLock Pro",
                                    color = Color.White,
                                    fontSize = 15.5.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(7.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(PillShape)
                                        .background(goldAccent)
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "VIP",
                                        color = Color(0xFF0F172A),
                                        fontSize = 9.5.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = "Unlock Decoy Covers, unlimited custom themes & VIP tools",
                                color = Color(0xFF94A3B8),
                                fontSize = 11.5.sp
                            )
                        }
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(
                                Brush.horizontalGradient(
                                    listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                                )
                            )
                            .padding(horizontal = 11.dp, vertical = 6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "UPGRADE →",
                            color = Color.White,
                            fontSize = 10.5.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Feature Highlights Row
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    val features = listOf("✦ Decoys", "✦ Themes", "✦ Siren", "✦ No Ads")
                    for (f in features) {
                        Box(
                            modifier = Modifier
                                .clip(PillShape)
                                .background(Color(0xFF334155).copy(alpha = 0.5f))
                                .border(0.8.dp, Color(0xFF475569), PillShape)
                                .padding(horizontal = 8.dp, vertical = 3.dp)
                        ) {
                            Text(
                                text = f,
                                color = Color(0xFFE2E8F0),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * Interactive category hub card in root settings.
 */
@Composable
private fun CategoryItemCard(
    category: SettingsCategory,
    badgeText: String?,
    onClick: () -> Unit
) {
    AppGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
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
                        .size(42.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(category.accentColor.copy(alpha = 0.13f))
                        .border(1.dp, category.accentColor.copy(alpha = 0.35f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = category.icon,
                        contentDescription = null,
                        tint = category.accentColor,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(13.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = category.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 14.5.sp
                        )
                        if (badgeText != null) {
                            Spacer(modifier = Modifier.width(8.dp))
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(category.accentColor.copy(alpha = 0.12f))
                                    .border(1.dp, category.accentColor.copy(alpha = 0.25f), PillShape)
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = badgeText,
                                    color = category.accentColor,
                                    fontSize = 9.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = category.summary,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextMuted,
                        fontSize = 11.5.sp,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = Color(0xFF94A3B8),
                modifier = Modifier.size(20.dp)
            )
        }
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
