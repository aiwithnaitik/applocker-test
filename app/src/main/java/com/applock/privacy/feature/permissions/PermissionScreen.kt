package com.applock.privacy.feature.permissions

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
import androidx.compose.foundation.verticalScroll
import android.os.Build
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.core.permission.PermissionStatus
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary

@Composable
fun PermissionScreen(
    onNavigateBack: () -> Unit
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var permissionStatus by remember { mutableStateOf(PermissionManager.getPermissionStatus(context)) }

    // Re-check permissions every time user returns from Android Settings
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

    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(8.dp))

        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            IconButton(
                onClick = onNavigateBack,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(SurfaceCard)
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowBack,
                    contentDescription = "Back",
                    tint = TextPrimary
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Text(
                text = "System Permissions",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        Text(
            text = "Required Privileges",
            style = MaterialTheme.typography.headlineSmall,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Android requires explicit user authorization so AppLock can monitor active apps and overlay security screens.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        // 1. Usage Access
        PermissionCard(
            title = "Usage Access",
            description = "Allows AppLock to detect in real-time when a protected app is opened so the shield can trigger.",
            icon = Icons.Default.QueryStats,
            isGranted = permissionStatus.hasUsageAccess,
            isRequired = true,
            onActionClick = {
                PermissionManager.openUsageAccessSettings(context)
            }
        )

        // Android 13/14/15 Restricted Settings Guidance Card
        if (!permissionStatus.hasUsageAccess && Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Spacer(modifier = Modifier.height(10.dp))
            AppGlassCard(
                modifier = Modifier.fillMaxWidth(),
                borderColor = androidx.compose.ui.graphics.Color(0xFFFFB74D)
            ) {
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Security,
                            contentDescription = null,
                            tint = androidx.compose.ui.graphics.Color(0xFFFFB74D),
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Blocked by Android 13+ 'Restricted setting'?",
                            color = androidx.compose.ui.graphics.Color(0xFFFFB74D),
                            fontWeight = FontWeight.Bold,
                            fontSize = 13.sp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Android blocks Usage Access on sideloaded apps by default. To unlock it:\n" +
                                "1. Tap 'Open App Info' below\n" +
                                "2. Tap the 3 dots (⋮) in the top-right corner\n" +
                                "3. Tap 'Allow restricted settings'\n" +
                                "4. Come back here and tap 'Grant' on Usage Access!",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 17.sp
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    AppGradientButton(
                        text = "Open App Info (3 Dots)",
                        onClick = { PermissionManager.openAppSettings(context) },
                        height = 36.dp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 2. Display Over Other Apps (Overlay)
        PermissionCard(
            title = "Display Over Other Apps",
            description = "Enables the lock screen overlay to appear on top of protected applications immediately.",
            icon = Icons.Default.Layers,
            isGranted = permissionStatus.hasOverlay,
            isRequired = true,
            onActionClick = {
                PermissionManager.openOverlaySettings(context)
            }
        )

        Spacer(modifier = Modifier.height(14.dp))

        // 3. Battery Optimization
        PermissionCard(
            title = "Keep Active in Background",
            description = "Prevents Android battery saver / doze mode from terminating the protection monitor.",
            icon = Icons.Default.BatteryAlert,
            isGranted = permissionStatus.isBatteryOptimizedIgnored,
            isRequired = false,
            onActionClick = {
                PermissionManager.requestIgnoreBatteryOptimization(context)
            }
        )

        // 4. OEM Auto-Start & Task Killer Protection (Phase 26)
        if (PermissionManager.isOemWithAggressiveTaskKiller()) {
            Spacer(modifier = Modifier.height(14.dp))
            PermissionCard(
                title = "Vendor Auto-Start Whitelist",
                description = "Grant background auto-launch permission for ${android.os.Build.MANUFACTURER.replaceFirstChar { it.uppercase() }} to prevent system task-killers from freezing AppLock.",
                icon = Icons.Default.Security,
                isGranted = permissionStatus.isBatteryOptimizedIgnored,
                isRequired = false,
                onActionClick = {
                    PermissionManager.openOemAutoStartSettings(context)
                }
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        AppGradientButton(
            text = if (permissionStatus.isCorePermissionsGranted) "All Set • Return" else "Continue to Home",
            onClick = onNavigateBack,
            modifier = Modifier.fillMaxWidth(),
            height = 52.dp
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun PermissionCard(
    title: String,
    description: String,
    icon: ImageVector,
    isGranted: Boolean,
    isRequired: Boolean,
    onActionClick: () -> Unit
) {
    AppGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onActionClick() },
        borderColor = if (isGranted) EmeraldSecure.copy(alpha = 0.5f) else ElectricCyan.copy(alpha = 0.5f)
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
                            .background(if (isGranted) EmeraldSecure.copy(alpha = 0.16f) else ElectricCyan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = null,
                            tint = if (isGranted) EmeraldSecure else ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Text(
                            text = if (isRequired) "Core Requirement" else "Recommended",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 11.sp
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
                        text = "GRANT",
                        color = ElectricCyan,
                        showDot = false
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = description,
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                fontSize = 12.sp,
                lineHeight = 17.sp
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = if (isGranted) "✓ Granted in system settings" else "Tap here to configure in Android Settings →",
                style = MaterialTheme.typography.bodyMedium,
                color = if (isGranted) EmeraldSecure else ElectricCyan,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}
