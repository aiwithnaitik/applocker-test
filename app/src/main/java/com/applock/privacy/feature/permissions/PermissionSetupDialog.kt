package com.applock.privacy.feature.permissions

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedContent
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.BatteryAlert
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Layers
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.QueryStats
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.applock.privacy.core.monitoring.AppLockSession
import com.applock.privacy.core.monitoring.AppMonitorService
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.theme.AmberWarning
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary

private enum class WizardStage {
    PROMPT,        // Initial Cancel or Allow popup
    USAGE_ACCESS,  // Guided popup for Usage Access
    OVERLAY,       // Guided popup for Display Over Other Apps
    COMPLETED      // Protection Shield Armed
}

/**
 * Unified Cancel-or-Allow Pop-up Wizard for AppLock permissions.
 *
 * Automatically chains:
 * 1. Native Android OS pop-ups for runtime permissions (Notifications, Camera, Storage)
 * 2. Native Android OS Battery Optimization prompt
 * 3. Guided sequential Cancel/Allow pop-ups for Usage Access & Display Over Other Apps
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PermissionSetupDialog(
    onDismiss: () -> Unit,
    onComplete: () -> Unit = {}
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasUsageAccess by remember { mutableStateOf(PermissionManager.hasUsageStatsPermission(context)) }
    var hasOverlay by remember { mutableStateOf(PermissionManager.hasOverlayPermission(context)) }
    var isBatteryIgnored by remember { mutableStateOf(PermissionManager.isIgnoringBatteryOptimizations(context)) }

    fun computeCurrentStage(): WizardStage {
        return when {
            !hasUsageAccess -> WizardStage.USAGE_ACCESS
            !hasOverlay -> WizardStage.OVERLAY
            else -> WizardStage.COMPLETED
        }
    }

    var currentStage by remember { mutableStateOf(WizardStage.PROMPT) }

    // Re-evaluate on return from system settings or dialogs
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                AppLockSession.isTransientSystemAction = false
                hasUsageAccess = PermissionManager.hasUsageStatsPermission(context)
                hasOverlay = PermissionManager.hasOverlayPermission(context)
                isBatteryIgnored = PermissionManager.isIgnoringBatteryOptimizations(context)

                if (currentStage != WizardStage.PROMPT) {
                    currentStage = computeCurrentStage()
                    if (currentStage == WizardStage.COMPLETED) {
                        AppMonitorService.start(context)
                    }
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Launcher for Android native runtime OS Allow/Don't Allow popups
    val runtimePermissionsLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) {
        AppLockSession.isTransientSystemAction = false
        // Request battery optimization native dialog next if needed
        if (!isBatteryIgnored) {
            AppLockSession.isTransientSystemAction = true
            PermissionManager.requestIgnoreBatteryOptimization(context)
        }
        currentStage = computeCurrentStage()
    }

    fun startOsPermissionPopups() {
        val permissionsToAsk = buildList {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.POST_NOTIFICATIONS)
                }
            }
            if (ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
                add(Manifest.permission.CAMERA)
            }
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.READ_MEDIA_IMAGES)
                }
            } else {
                if (ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                    add(Manifest.permission.READ_EXTERNAL_STORAGE)
                }
            }
        }

        if (permissionsToAsk.isNotEmpty()) {
            AppLockSession.isTransientSystemAction = true
            runtimePermissionsLauncher.launch(permissionsToAsk.toTypedArray())
        } else {
            if (!isBatteryIgnored) {
                AppLockSession.isTransientSystemAction = true
                PermissionManager.requestIgnoreBatteryOptimization(context)
            }
            currentStage = computeCurrentStage()
        }
    }

    BasicAlertDialog(
        onDismissRequest = onDismiss
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(22.dp)
        ) {
            AnimatedContent(
                targetState = currentStage,
                label = "WizardStageAnimation"
            ) { stage ->
                when (stage) {
                    WizardStage.PROMPT -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(ElectricCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Shield,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "AppLock Setup",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Allow Protection Privileges",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = TextSecondary,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))

                            Text(
                                text = "To protect your apps and display the PIN/Pattern lock screen, Android requires a few security privileges.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = 19.sp,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(10.dp))

                            Text(
                                text = "• System Pop-up: Allow notifications and camera\n" +
                                       "• Step 1: Usage Access (detects opened apps)\n" +
                                       "• Step 2: Overlay (displays lock screen)",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextMuted,
                                lineHeight = 18.sp,
                                fontSize = 12.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel", color = TextSecondary)
                                }

                                Button(
                                    onClick = { startOsPermissionPopups() },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                                ) {
                                    Text(
                                        "Allow & Continue",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    WizardStage.USAGE_ACCESS -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(AmberWarning.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.QueryStats,
                                            contentDescription = null,
                                            tint = AmberWarning,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Step 1 of 2: Usage Access",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Core Protection Privilege",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = AmberWarning,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Allows AppLock to detect when a locked app is brought to foreground so the security lock screen appears before unauthorized viewing.\n\n" +
                                       "Tap 'Allow' below, select AppLock in Settings, and toggle the switch ON.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = 19.sp,
                                fontSize = 13.sp
                            )

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                                Spacer(modifier = Modifier.height(10.dp))
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clip(RoundedCornerShape(8.dp))
                                        .background(Color(0xFF1E293B))
                                        .padding(10.dp)
                                ) {
                                    Text(
                                        text = "ℹ️ Android 13+ note: If the switch is disabled, tap the 3 dots (⋮) in App Info and tap 'Allow restricted settings'.",
                                        color = TextMuted,
                                        fontSize = 11.sp,
                                        lineHeight = 15.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel", color = TextSecondary)
                                }

                                Button(
                                    onClick = {
                                        AppLockSession.isTransientSystemAction = true
                                        PermissionManager.openUsageAccessSettings(context)
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = AmberWarning)
                                ) {
                                    Text(
                                        "Allow & Open",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    WizardStage.OVERLAY -> {
                        Column {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Box(
                                        modifier = Modifier
                                            .size(42.dp)
                                            .clip(CircleShape)
                                            .background(ElectricCyan.copy(alpha = 0.15f)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Layers,
                                            contentDescription = null,
                                            tint = ElectricCyan,
                                            modifier = Modifier.size(24.dp)
                                        )
                                    }
                                    Spacer(modifier = Modifier.width(12.dp))
                                    Column {
                                        Text(
                                            text = "Step 2 of 2: Display Over Apps",
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            color = TextPrimary
                                        )
                                        Text(
                                            text = "Core Protection Privilege",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = ElectricCyan,
                                            fontSize = 12.sp
                                        )
                                    }
                                }

                                IconButton(onClick = onDismiss) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Cancel",
                                        tint = TextMuted
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Enables the secure lock screen overlay to appear over protected applications, preventing unauthorized access.\n\n" +
                                       "Tap 'Allow' below and toggle 'Allow display over other apps' ON.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = 19.sp,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(20.dp))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(10.dp)
                            ) {
                                OutlinedButton(
                                    onClick = onDismiss,
                                    modifier = Modifier.weight(1f),
                                    shape = RoundedCornerShape(12.dp)
                                ) {
                                    Text("Cancel", color = TextSecondary)
                                }

                                Button(
                                    onClick = {
                                        AppLockSession.isTransientSystemAction = true
                                        PermissionManager.openOverlaySettings(context)
                                    },
                                    modifier = Modifier.weight(1.3f),
                                    shape = RoundedCornerShape(12.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan)
                                ) {
                                    Text(
                                        "Allow & Open",
                                        color = Color.Black,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }
                        }
                    }

                    WizardStage.COMPLETED -> {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(54.dp)
                                    .clip(CircleShape)
                                    .background(EmeraldSecure.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = EmeraldSecure,
                                    modifier = Modifier.size(32.dp)
                                )
                            }

                            Spacer(modifier = Modifier.height(14.dp))

                            Text(
                                text = "Protection Shield Armed! 🛡️",
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )

                            Spacer(modifier = Modifier.height(8.dp))

                            Text(
                                text = "All core privileges are active. Whenever you or anyone opens a protected app, AppLock will instantly present your secure lock screen.",
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                lineHeight = 19.sp,
                                fontSize = 13.sp
                            )

                            Spacer(modifier = Modifier.height(22.dp))

                            AppGradientButton(
                                text = "Done & Arm Protection",
                                onClick = {
                                    AppMonitorService.start(context)
                                    onComplete()
                                    onDismiss()
                                },
                                modifier = Modifier.fillMaxWidth(),
                                height = 48.dp
                            )
                        }
                    }
                }
            }
        }
    }
}
