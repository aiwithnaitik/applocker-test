package com.applock.privacy.feature.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.applock.privacy.core.monitoring.AppLockSession
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.AmberWarning
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary

@Composable
fun PermissionScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var permissionStatus by remember { mutableStateOf(PermissionManager.getPermissionStatus(context)) }
    var isAccessibilityGranted by remember { mutableStateOf(PermissionManager.isAccessibilityServiceEnabled(context)) }
    var isNotificationListenerGranted by remember { mutableStateOf(PermissionManager.isNotificationListenerGranted(context)) }
    var isCameraGranted by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        AppLockSession.isTransientSystemAction = false
        isCameraGranted = granted
    }

    // Refresh permissions whenever returning from system settings
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = PermissionManager.getPermissionStatus(context)
                isAccessibilityGranted = PermissionManager.isAccessibilityServiceEnabled(context)
                isNotificationListenerGranted = PermissionManager.isNotificationListenerGranted(context)
                isCameraGranted = ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    val isOemAggressive = remember { PermissionManager.isOemWithAggressiveTaskKiller() }

    val allPrivileges = remember(permissionStatus, isAccessibilityGranted, isNotificationListenerGranted, isCameraGranted) {
        listOf(
            permissionStatus.hasUsageAccess,
            permissionStatus.hasOverlay,
            isAccessibilityGranted,
            permissionStatus.isBatteryOptimizedIgnored,
            isNotificationListenerGranted,
            isCameraGranted
        )
    }

    val grantedCount = allPrivileges.count { it }
    val totalCount = allPrivileges.size
    val isCoreReady = permissionStatus.isCorePermissionsGranted
    val progressFraction = grantedCount.toFloat() / totalCount.toFloat()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
    ) {
        AppTopBar(
            title = "System Privileges",
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(14.dp))

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // Master Readiness Hero Card
            item {
                AppGlassCard(
                    modifier = Modifier.fillMaxWidth(),
                    borderColor = if (isCoreReady) EmeraldSecure.copy(alpha = 0.45f) else AmberWarning.copy(alpha = 0.45f)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(
                                modifier = Modifier.weight(1f),
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(46.dp)
                                        .clip(CircleShape)
                                        .background(if (isCoreReady) EmeraldSecure.copy(alpha = 0.15f) else AmberWarning.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = if (isCoreReady) Icons.Default.Shield else Icons.Default.Warning,
                                        contentDescription = null,
                                        tint = if (isCoreReady) EmeraldSecure else AmberWarning,
                                        modifier = Modifier.size(26.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(14.dp))
                                Column {
                                    Text(
                                        text = "Protection Readiness",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = "$grantedCount of $totalCount Privileges Active",
                                        color = TextSecondary,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            AppStatusBadge(
                                text = if (isCoreReady) "SHIELD ARMED" else "ACTION REQ",
                                color = if (isCoreReady) EmeraldSecure else AmberWarning,
                                showDot = true
                            )
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        LinearProgressIndicator(
                            progress = progressFraction,
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isCoreReady) EmeraldSecure else ElectricCyan,
                            trackColor = SurfaceCard,
                            strokeCap = StrokeCap.Round
                        )

                        Spacer(modifier = Modifier.height(10.dp))

                        Text(
                            text = if (isCoreReady)
                                "Core security privileges are active. AppLock operates 100% locally on your device with no remote transmission of app usage or personal data."
                            else
                                "Usage Access and Overlay permissions are required so AppLock can intercept locked apps before unauthorized viewing occurs.",
                            color = TextMuted,
                            fontSize = 12.sp,
                            lineHeight = 17.sp
                        )
                    }
                }
            }

            // Android 13/14/15 Restricted Settings Guidance Card
            if (!permissionStatus.hasUsageAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                item {
                    AppGlassCard(
                        modifier = Modifier.fillMaxWidth(),
                        borderColor = AmberWarning.copy(alpha = 0.6f)
                    ) {
                        Column {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "Android 13+ 'Restricted Setting' Guide",
                                    color = AmberWarning,
                                    fontWeight = FontWeight.Bold,
                                    fontSize = 14.sp
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = "Android grays out Usage Access on installed apps by default. To unlock it:\n\n" +
                                        "1. Tap 'Open App Info' below\n" +
                                        "2. Tap the 3 dots (⋮) in the top-right corner\n" +
                                        "3. Select 'Allow restricted settings'\n" +
                                        "4. Return here and tap 'Grant' on Usage Access!",
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 18.sp
                            )
                            Spacer(modifier = Modifier.height(12.dp))
                            Button(
                                onClick = {
                                    AppLockSession.isTransientSystemAction = true
                                    PermissionManager.openAppSettings(context)
                                },
                                colors = ButtonDefaults.buttonColors(containerColor = AmberWarning),
                                shape = RoundedCornerShape(10.dp),
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Text(
                                    text = "Open App Info (3 Dots)",
                                    color = Color.Black,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            }

            // SECTION 1: CORE PRIVILEGES
            item {
                SectionHeader(
                    title = "CORE SECURITY PRIVILEGES",
                    subtitle = "Mandatory for real-time foreground detection and lock dialog overlay"
                )
            }

            // 1. Usage Access
            item {
                DetailedPermissionCard(
                    title = "Usage Access",
                    tag = "Required • Real-Time Interception",
                    description = "Allows AppLock to detect in real-time when a protected app is brought to foreground so the security lock screen displays before content is viewed.",
                    privacyGuarantee = "Privacy Guarantee: Analyzed strictly on-device. No screen contents or personal browsing records are collected.",
                    icon = Icons.Default.QueryStats,
                    isGranted = permissionStatus.hasUsageAccess,
                    isRequired = true,
                    onGrantClick = {
                        AppLockSession.isTransientSystemAction = true
                        PermissionManager.openUsageAccessSettings(context)
                    }
                )
            }

            // 2. Display Over Other Apps
            item {
                DetailedPermissionCard(
                    title = "Display Over Other Apps",
                    tag = "Required • Lock Screen Overlay",
                    description = "Enables the secure lock overlay to appear immediately over protected applications, preventing glances and unauthorized access.",
                    privacyGuarantee = "Protection Guarantee: Displays only when a locked app or sensitive action is triggered.",
                    icon = Icons.Default.Layers,
                    isGranted = permissionStatus.hasOverlay,
                    isRequired = true,
                    onGrantClick = {
                        AppLockSession.isTransientSystemAction = true
                        PermissionManager.openOverlaySettings(context)
                    }
                )
            }

            // SECTION 2: HIGH-PERFORMANCE & RELIABILITY
            item {
                SectionHeader(
                    title = "ANTI-BYPASS & RELIABILITY",
                    subtitle = "Zero-latency defense and continuous background protection"
                )
            }

            // 3. Instant Shield (Accessibility Service)
            item {
                DetailedPermissionCard(
                    title = "Instant Shield (Anti-Bypass)",
                    tag = "Recommended • 0ms Latency",
                    description = "Enables instantaneous sub-millisecond interception so locked apps cannot be bypassed via quick app switching, split-screen multi-window, or PIP mode.",
                    privacyGuarantee = "Used exclusively for rapid window package transitions. Keystrokes and text inputs are never monitored.",
                    icon = Icons.Default.Bolt,
                    isGranted = isAccessibilityGranted,
                    isRequired = false,
                    onGrantClick = {
                        AppLockSession.isTransientSystemAction = true
                        PermissionManager.openAccessibilitySettings(context)
                    }
                )
            }

            // 4. Battery Optimization Exemption
            item {
                DetailedPermissionCard(
                    title = "Keep Active in Background",
                    tag = "Recommended • Background Persistence",
                    description = "Exempts AppLock from aggressive Android Doze mode and OS memory killers so locks trigger reliably even after hours of device standby.",
                    privacyGuarantee = "Consumes negligible battery (<1% per day) while maintaining real-time background vigilance.",
                    icon = Icons.Default.BatteryAlert,
                    isGranted = permissionStatus.isBatteryOptimizedIgnored,
                    isRequired = false,
                    onGrantClick = {
                        AppLockSession.isTransientSystemAction = true
                        PermissionManager.requestIgnoreBatteryOptimization(context)
                    }
                )
            }

            // 5. OEM Auto-Start & Background Whitelist (if applicable)
            if (isOemAggressive) {
                item {
                    DetailedPermissionCard(
                        title = "Vendor Auto-Start Whitelist",
                        tag = "Recommended • OEM Task Guard",
                        description = "Grant background auto-launch permission for ${Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} to prevent custom OEM task-killers from freezing AppLock.",
                        privacyGuarantee = "Essential for Xiaomi (MIUI/HyperOS), Samsung, Vivo, Oppo, and OnePlus devices.",
                        icon = Icons.Default.Tune,
                        isGranted = permissionStatus.isBatteryOptimizedIgnored,
                        isRequired = false,
                        onGrantClick = {
                            AppLockSession.isTransientSystemAction = true
                            PermissionManager.openOemAutoStartSettings(context)
                        }
                    )
                }
            }

            // SECTION 3: PRIVACY FEATURES
            item {
                SectionHeader(
                    title = "PRIVACY FEATURE PRIVILEGES",
                    subtitle = "Enables notification auto-hiding and intruder snapshot defense"
                )
            }

            // 6. Notification Shield Service
            item {
                DetailedPermissionCard(
                    title = "Notification Shield Service",
                    tag = "Optional • Notification Privacy",
                    description = "Automatically hides confidential message previews and sensitive contents from notifications belonging to locked apps.",
                    privacyGuarantee = "Notification previews are masked directly on the lockscreen and status bar without inspecting personal messages.",
                    icon = Icons.Default.NotificationsActive,
                    isGranted = isNotificationListenerGranted,
                    isRequired = false,
                    onGrantClick = {
                        AppLockSession.isTransientSystemAction = true
                        PermissionManager.openNotificationListenerSettings(context)
                    }
                )
            }

            // 7. Intruder Selfie Camera
            item {
                DetailedPermissionCard(
                    title = "Intruder Camera Access",
                    tag = "Optional • Intruder Snapshot",
                    description = "Captures a front-camera snapshot of anyone who inputs an incorrect PIN or pattern, stored securely in your private Intruder Log.",
                    privacyGuarantee = "Camera is accessed strictly during failed unlock attempts. No photos leave your local device.",
                    icon = Icons.Default.CameraAlt,
                    isGranted = isCameraGranted,
                    isRequired = false,
                    onGrantClick = {
                        AppLockSession.isTransientSystemAction = true
                        cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                    }
                )
            }

            // Bottom Action Button
            item {
                Spacer(modifier = Modifier.height(10.dp))
                AppGradientButton(
                    text = if (isCoreReady) "All Set • Return" else "Continue to Dashboard",
                    onClick = onNavigateBack,
                    modifier = Modifier.fillMaxWidth(),
                    height = 50.dp
                )
            }

            item {
                Spacer(modifier = Modifier.height(18.dp))
            }
        }
    }
}

@Composable
private fun SectionHeader(
    title: String,
    subtitle: String
) {
    Column(modifier = Modifier.padding(top = 6.dp, bottom = 2.dp)) {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            letterSpacing = 0.5.sp
        )
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun DetailedPermissionCard(
    title: String,
    tag: String,
    description: String,
    privacyGuarantee: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    onGrantClick: () -> Unit
) {
    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isGranted) EmeraldSecure.copy(alpha = 0.35f) else if (isRequired) ElectricCyan.copy(alpha = 0.4f) else BorderSubtle
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isGranted) EmeraldSecure.copy(alpha = 0.15f) else ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) EmeraldSecure else ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(12.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = tag,
                            color = if (isRequired) ElectricCyan else TextSecondary,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }

                if (isGranted) {
                    AppStatusBadge(
                        text = "ACTIVE",
                        color = EmeraldSecure,
                        showDot = true
                    )
                } else {
                    AppStatusBadge(
                        text = if (isRequired) "REQUIRED" else "RECOMMENDED",
                        color = if (isRequired) AmberWarning else ElectricCyan,
                        showDot = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            if (privacyGuarantee.isNotEmpty()) {
                Spacer(modifier = Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(SurfaceCard.copy(alpha = 0.6f))
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = privacyGuarantee,
                        color = TextMuted,
                        fontSize = 11.sp,
                        lineHeight = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            if (isGranted) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier.padding(vertical = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = EmeraldSecure,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Privilege granted & active",
                        color = EmeraldSecure,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            } else {
                Button(
                    onClick = onGrantClick,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = if (isRequired) ElectricCyan else SurfaceCard
                    ),
                    shape = RoundedCornerShape(10.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = if (isRequired) "Grant Privilege Now" else "Enable in System Settings",
                        color = if (isRequired) Color.Black else TextPrimary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 13.sp
                    )
                }
            }
        }
    }
}
