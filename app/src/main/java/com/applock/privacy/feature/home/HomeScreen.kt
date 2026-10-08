package com.applock.privacy.feature.home

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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.applock.privacy.R
import com.applock.privacy.core.monitoring.AppMonitorService
import com.applock.privacy.core.permission.PermissionManager
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppSwitch
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.AmberWarning
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.PillShape
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.core.updater.AppUpdateManager
import com.applock.privacy.core.updater.UpdateState
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.data.model.AppInfo
import com.applock.privacy.data.repository.AppDiscoveryRepository
import com.applock.privacy.feature.auth.PinSetupDialog
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AppFilterTab {
    ALL, LOCKED, UNLOCKED
}

@Composable
fun HomeScreen(
    onNavigateToPermissions: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }
    val discoveryRepository = remember { AppDiscoveryRepository(context) }
    val updateManager = remember { AppUpdateManager(context) }
    val lifecycleOwner = LocalLifecycleOwner.current

    val lockedPackages by preferencesDataSource.lockedPackagesFlow.collectAsState(initial = emptySet())
    val isAppMonitorActive by preferencesDataSource.isAppMonitorActiveFlow.collectAsState(initial = true)
    val updateState by updateManager.updateState.collectAsState()

    var permissionStatus by remember { mutableStateOf(PermissionManager.getPermissionStatus(context)) }
    var installedApps by remember { mutableStateOf<List<AppInfo>>(emptyList()) }
    var isLoadingApps by remember { mutableStateOf(true) }
    var searchQuery by remember { mutableStateOf("") }
    var selectedFilter by remember { mutableStateOf(AppFilterTab.ALL) }
    var showPinSetupDialog by remember { mutableStateOf(false) }
    var pendingLockPackage by remember { mutableStateOf<String?>(null) }

    // Re-check permissions and restart monitor service if needed on resume
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                permissionStatus = PermissionManager.getPermissionStatus(context)
                if (permissionStatus.isCorePermissionsGranted && isAppMonitorActive) {
                    AppMonitorService.start(context)
                }
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
        }
    }

    // Load installed apps in background
    LaunchedEffect(Unit) {
        isLoadingApps = true
        val apps = discoveryRepository.getInstalledApps()
        installedApps = apps
        isLoadingApps = false
        updateManager.checkForUpdates()
    }

    val filteredApps = remember(installedApps, lockedPackages, searchQuery, selectedFilter) {
        installedApps.filter { app ->
            val matchesSearch = searchQuery.isBlank() ||
                app.appName.contains(searchQuery, ignoreCase = true) ||
                app.packageName.contains(searchQuery, ignoreCase = true)

            val isLocked = lockedPackages.contains(app.packageName)
            val matchesFilter = when (selectedFilter) {
                AppFilterTab.ALL -> true
                AppFilterTab.LOCKED -> isLocked
                AppFilterTab.UNLOCKED -> !isLocked
            }

            matchesSearch && matchesFilter
        }
    }

    if (showPinSetupDialog) {
        PinSetupDialog(
            preferencesDataSource = preferencesDataSource,
            onDismissRequest = {
                showPinSetupDialog = false
                pendingLockPackage = null
            },
            onPinCreated = {
                showPinSetupDialog = false
                pendingLockPackage?.let { pkg ->
                    coroutineScope.launch {
                        preferencesDataSource.setPackageLocked(pkg, true)
                        if (permissionStatus.isCorePermissionsGranted) {
                            AppMonitorService.start(context)
                        }
                    }
                    pendingLockPackage = null
                }
            }
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
    ) {
        AppTopBar(
            title = "AppLock",
            actions = {
                AppStatusBadge(
                    text = if (isAppMonitorActive && permissionStatus.isCorePermissionsGranted) "SHIELD ACTIVE" else "PAUSED",
                    color = if (isAppMonitorActive && permissionStatus.isCorePermissionsGranted) EmeraldSecure else AmberWarning
                )
            }
        )

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Spacer(modifier = Modifier.height(2.dp))

                // In-App Update Banner
                when (val state = updateState) {
                    is UpdateState.UpdateAvailable -> {
                        AppGlassCard(
                            modifier = Modifier.fillMaxWidth(),
                            borderColor = ElectricCyan
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Box(
                                    modifier = Modifier
                                        .size(40.dp)
                                        .clip(CircleShape)
                                        .background(ElectricCyan.copy(alpha = 0.15f)),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.SystemUpdate,
                                        contentDescription = null,
                                        tint = ElectricCyan,
                                        modifier = Modifier.size(22.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.width(12.dp))

                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = "New Build Available!",
                                        style = MaterialTheme.typography.titleMedium,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary
                                    )
                                    Text(
                                        text = state.info.releaseName,
                                        style = MaterialTheme.typography.bodyMedium,
                                        color = ElectricCyan,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(10.dp))

                            AppGradientButton(
                                text = "1-Tap Update APK",
                                onClick = {
                                    coroutineScope.launch {
                                        updateManager.downloadAndInstall(state.info)
                                    }
                                },
                                height = 42.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    is UpdateState.Downloading -> {
                        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Downloading Update... ${(state.progress * 100).toInt()}%",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            LinearProgressIndicator(
                                progress = { state.progress },
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(6.dp)
                                    .clip(PillShape),
                                color = ElectricCyan,
                                trackColor = SurfaceCard
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    is UpdateState.ReadyToInstall -> {
                        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                            Text(
                                text = "Update Ready to Install",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = EmeraldSecure
                            )
                            Spacer(modifier = Modifier.height(6.dp))
                            AppGradientButton(
                                text = "Install Now",
                                onClick = { updateManager.launchInstaller(state.apkFile) },
                                height = 42.dp
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                    else -> {}
                }

                // Action Required Permission Alert Card
                if (!permissionStatus.isCorePermissionsGranted) {
                    AppGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { onNavigateToPermissions() },
                        borderColor = AmberWarning
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(CircleShape)
                                    .background(AmberWarning.copy(alpha = 0.2f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Warning,
                                    contentDescription = null,
                                    tint = AmberWarning,
                                    modifier = Modifier.size(24.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(12.dp))

                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Permissions Required",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Text(
                                    text = "Enable Usage Access & Overlay so AppLock can intercept protected apps.",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextMuted,
                                    fontSize = 12.sp,
                                    lineHeight = 16.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        AppGradientButton(
                            text = "Grant System Permissions →",
                            onClick = onNavigateToPermissions,
                            height = 42.dp
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                }

                // Master Shield Card
                AppGlassCard(modifier = Modifier.fillMaxWidth()) {
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
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(14.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Image(
                                    painter = painterResource(id = R.drawable.app_logo),
                                    contentDescription = "AppLock",
                                    modifier = Modifier.size(52.dp)
                                )
                            }

                            Spacer(modifier = Modifier.width(14.dp))

                            Column {
                                Text(
                                    text = if (isAppMonitorActive) "AppLock Shield" else "Protection Paused",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = TextPrimary
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = if (isAppMonitorActive) "${lockedPackages.size} apps secured in real-time" else "Tap toggle to activate protection",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextSecondary,
                                    fontSize = 12.sp
                                )
                            }
                        }

                        AppSwitch(
                            checked = isAppMonitorActive,
                            onCheckedChange = { active ->
                                coroutineScope.launch {
                                    preferencesDataSource.setAppMonitorActive(active)
                                    if (active && permissionStatus.isCorePermissionsGranted) {
                                        AppMonitorService.start(context)
                                    } else {
                                        AppMonitorService.stop(context)
                                    }
                                }
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // App Search Bar
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(48.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(SurfaceCard)
                        .border(1.dp, BorderSubtle, RoundedCornerShape(14.dp))
                        .padding(horizontal = 14.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "Search",
                            tint = TextMuted,
                            modifier = Modifier.size(20.dp)
                        )

                        Spacer(modifier = Modifier.width(10.dp))

                        BasicTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            modifier = Modifier.weight(1f),
                            textStyle = TextStyle(
                                color = TextPrimary,
                                fontSize = 14.sp
                            ),
                            cursorBrush = SolidColor(ElectricCyan),
                            decorationBox = { innerTextField ->
                                if (searchQuery.isEmpty()) {
                                    Text(
                                        text = "Search installed apps...",
                                        color = TextMuted,
                                        fontSize = 14.sp
                                    )
                                }
                                innerTextField()
                            }
                        )

                        if (searchQuery.isNotEmpty()) {
                            IconButton(
                                onClick = { searchQuery = "" },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "Clear",
                                    tint = TextMuted,
                                    modifier = Modifier.size(18.dp)
                                )
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                // Filter Tabs: All, Locked, Unlocked
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    val lockedCount = lockedPackages.size
                    val unlockedCount = (installedApps.size - lockedCount).coerceAtLeast(0)

                    FilterChip(
                        title = "All (${installedApps.size})",
                        isSelected = selectedFilter == AppFilterTab.ALL,
                        onClick = { selectedFilter = AppFilterTab.ALL },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        title = "Locked ($lockedCount)",
                        isSelected = selectedFilter == AppFilterTab.LOCKED,
                        onClick = { selectedFilter = AppFilterTab.LOCKED },
                        modifier = Modifier.weight(1f)
                    )

                    FilterChip(
                        title = "Unlocked ($unlockedCount)",
                        isSelected = selectedFilter == AppFilterTab.UNLOCKED,
                        onClick = { selectedFilter = AppFilterTab.UNLOCKED },
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            if (isLoadingApps) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = ElectricCyan,
                            strokeWidth = 2.5.dp,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            } else if (filteredApps.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 40.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = if (searchQuery.isNotEmpty()) "No applications matching \"$searchQuery\"" else "No applications found in this section",
                            color = TextMuted,
                            fontSize = 13.sp
                        )
                    }
                }
            } else {
                items(filteredApps, key = { it.packageName }) { app ->
                    val isLocked = lockedPackages.contains(app.packageName)

                    AppListItem(
                        app = app,
                        isLocked = isLocked,
                        onToggleLock = {
                            coroutineScope.launch {
                                val hasPin = preferencesDataSource.hasPinConfiguredFlow.first()
                                if (!hasPin && !isLocked) {
                                    // Needs PIN setup first!
                                    pendingLockPackage = app.packageName
                                    showPinSetupDialog = true
                                } else {
                                    preferencesDataSource.setPackageLocked(app.packageName, !isLocked)
                                    if (!isLocked && permissionStatus.isCorePermissionsGranted) {
                                        AppMonitorService.start(context)
                                    }
                                }
                            }
                        }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(80.dp))
            }
        }
    }
}

@Composable
private fun FilterChip(
    title: String,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .height(34.dp)
            .clip(PillShape)
            .background(if (isSelected) ElectricCyan.copy(alpha = 0.16f) else SurfaceCard)
            .border(
                1.dp,
                if (isSelected) ElectricCyan else BorderSubtle.copy(alpha = 0.5f),
                PillShape
            )
            .clickable { onClick() },
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
            color = if (isSelected) ElectricCyan else TextMuted,
            fontSize = 11.sp
        )
    }
}

@Composable
private fun AppListItem(
    app: AppInfo,
    isLocked: Boolean,
    onToggleLock: () -> Unit
) {
    AppGlassCard(
        modifier = Modifier.fillMaxWidth(),
        borderColor = if (isLocked) ElectricCyan.copy(alpha = 0.4f) else null
    ) {
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
                        .size(44.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(SurfaceCard),
                    contentAlignment = Alignment.Center
                ) {
                    if (app.iconBitmap != null) {
                        Image(
                            bitmap = app.iconBitmap,
                            contentDescription = app.appName,
                            modifier = Modifier.size(36.dp)
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Text(
                        text = app.appName,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        maxLines = 1
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (app.isSystemApp) "System App • ${app.packageName}" else app.packageName,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextMuted,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(8.dp))

            // Padlock Lock Toggle Icon Button
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(if (isLocked) ElectricCyan else SurfaceCard)
                    .border(
                        1.dp,
                        if (isLocked) ElectricCyan else BorderSubtle,
                        CircleShape
                    )
                    .clickable { onToggleLock() },
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isLocked) Icons.Default.Lock else Icons.Default.LockOpen,
                    contentDescription = if (isLocked) "Locked" else "Unlocked",
                    tint = if (isLocked) BackgroundDeep else TextMuted,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}
