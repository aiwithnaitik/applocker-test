package com.applock.privacy.feature.settings

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Vibration
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.R
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppSwitch
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.auth.PinSetupDialog
import kotlinx.coroutines.launch

@Composable
fun SettingsScreen(
    onNavigateToPermissions: () -> Unit = {},
    onResetOnboarding: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    val coroutineScope = rememberCoroutineScope()
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }
    val isBiometricsEnabled by preferencesDataSource.isBiometricEnabledFlow.collectAsState(initial = true)
    val hasPinConfigured by preferencesDataSource.hasPinConfiguredFlow.collectAsState(initial = false)
    var isHapticEnabled by remember { mutableStateOf(true) }
    var showPinDialog by remember { mutableStateOf(false) }
    var permissionStatus by remember { mutableStateOf(PermissionManager.getPermissionStatus(context)) }
    val scrollState = rememberScrollState()

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

    if (showPinDialog) {
        PinSetupDialog(
            preferencesDataSource = preferencesDataSource,
            onDismissRequest = { showPinDialog = false },
            onPinCreated = { showPinDialog = false }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        AppTopBar(title = "Settings")

        Spacer(modifier = Modifier.height(16.dp))

        // Security Category
        Text(
            text = "Authentication & Security",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingToggleItem(
                title = "Biometric Authentication",
                subtitle = "Use fingerprint or face recognition to unlock",
                icon = Icons.Default.Fingerprint,
                checked = isBiometricsEnabled,
                onCheckedChange = {
                    coroutineScope.launch {
                        preferencesDataSource.setBiometricEnabled(it)
                    }
                }
            )

            Divider()

            SettingNavigationItem(
                title = if (hasPinConfigured) "Change Security PIN" else "Set Up Security PIN",
                subtitle = if (hasPinConfigured) "Update your 4-digit security code" else "Create a 4-digit code to lock apps",
                icon = Icons.Default.Lock,
                onClick = { showPinDialog = true }
            )

            Divider()

            SettingToggleItem(
                title = "Haptic Vibration",
                subtitle = "Vibrate on button taps and unlock success",
                icon = Icons.Default.Vibration,
                checked = isHapticEnabled,
                onCheckedChange = { isHapticEnabled = it }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // System Permissions Category
        Text(
            text = "System Permissions",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            PermissionStatusItem(
                title = "Usage Access",
                subtitle = "Detect when protected apps are opened",
                badgeText = if (permissionStatus.hasUsageAccess) "ACTIVE" else "GRANT",
                badgeColor = if (permissionStatus.hasUsageAccess) EmeraldSecure else ElectricCyan,
                onClick = onNavigateToPermissions
            )

            Divider()

            PermissionStatusItem(
                title = "Display Over Other Apps",
                subtitle = "Show lock screen over protected apps",
                badgeText = if (permissionStatus.hasOverlay) "ACTIVE" else "GRANT",
                badgeColor = if (permissionStatus.hasOverlay) EmeraldSecure else ElectricCyan,
                onClick = onNavigateToPermissions
            )

            Divider()

            PermissionStatusItem(
                title = "Battery Optimization",
                subtitle = "Keep protection active in background",
                badgeText = if (permissionStatus.isBatteryOptimizedIgnored) "OPTIMIZED" else "ENABLE",
                badgeColor = if (permissionStatus.isBatteryOptimizedIgnored) EmeraldSecure else ElectricCyan,
                onClick = onNavigateToPermissions
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // About Card
        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.app_logo),
                        contentDescription = "AppLock",
                        modifier = Modifier.size(46.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = "AppLock Secure",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Text(
                        text = "Version 1.0.0 • Phase 2 Foundation",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        fontSize = 12.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))
            Divider()
            Spacer(modifier = Modifier.height(4.dp))

            val updateManager = remember { com.applock.privacy.core.updater.AppUpdateManager(context) }
            val updateState by updateManager.updateState.collectAsState()

            SettingNavigationItem(
                title = "Check for Updates",
                subtitle = when (updateState) {
                    is com.applock.privacy.core.updater.UpdateState.Checking -> "Checking GitHub..."
                    is com.applock.privacy.core.updater.UpdateState.UpdateAvailable -> "Update available! Tap to download"
                    is com.applock.privacy.core.updater.UpdateState.UpToDate -> "App is up to date"
                    else -> "Tap to scan for new builds"
                },
                icon = androidx.compose.material.icons.Icons.Default.SystemUpdate,
                onClick = {
                    coroutineScope.launch {
                        val info = updateManager.checkForUpdates()
                        if (info != null && info.hasUpdate) {
                            updateManager.downloadAndInstall(info)
                        }
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Developer & Test Utilities
        Text(
            text = "Testing & Diagnostics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            SettingNavigationItem(
                title = "Restart Onboarding Flow",
                subtitle = "Reset onboarding state to test splash and intro screens",
                icon = Icons.Default.Refresh,
                onClick = {
                    coroutineScope.launch {
                        preferencesDataSource.resetOnboarding()
                        onResetOnboarding?.invoke()
                    }
                }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 12.sp
                )
            }
        }
        AppSwitch(checked = checked, onCheckedChange = onCheckedChange)
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
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.weight(1f),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(20.dp)
                )
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodyMedium,
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
private fun PermissionStatusItem(
    title: String,
    subtitle: String,
    badgeText: String,
    badgeColor: androidx.compose.ui.graphics.Color,
    onClick: () -> Unit = {}
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() }
            .padding(vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                fontSize = 12.sp
            )
        }
        AppStatusBadge(text = badgeText, color = badgeColor, showDot = false)
    }
}

@Composable
private fun Divider() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(1.dp)
            .background(BorderSubtle.copy(alpha = 0.4f))
    )
}
