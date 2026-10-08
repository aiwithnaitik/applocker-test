package com.applock.privacy.feature.themes

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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.theme.AppTheme
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.theme.CustomThemeConfig
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.PillShape
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.launch

@Composable
fun ThemesScreen(
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }
    val persistedThemeId by preferencesDataSource.selectedThemeIdFlow.collectAsState(initial = "sapphire_glass")
    val customThemes by preferencesDataSource.customThemesFlow.collectAsState(initial = emptyList())
    val unlockedThemes by preferencesDataSource.unlockedThemeIdsFlow.collectAsState(initial = setOf("sapphire_glass", "cyber_neon", "emerald_matrix", "obsidian_dark"))

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    var previewThemeId by remember { mutableStateOf(persistedThemeId) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var themeToUnlock by remember { mutableStateOf<AppTheme?>(null) }

    val previewTheme = remember(previewThemeId, persistedThemeId, customThemes) {
        val targetId = if (previewThemeId.isNotEmpty()) previewThemeId else persistedThemeId
        AppThemeCatalog.getThemeById(targetId, customThemes)
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        AppTopBar(title = "Lock Themes")

        Spacer(modifier = Modifier.height(14.dp))

        // Live Interactive Theme Preview Card
        Text(
            text = "Live Lock Screen Preview",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(8.dp))

        LiveThemePreviewCard(theme = previewTheme)

        Spacer(modifier = Modifier.height(16.dp))

        // Tabs: Studio Catalog vs My Themes
        TabRow(
            selectedTabIndex = selectedTabIndex,
            containerColor = Color.Transparent,
            contentColor = ElectricCyan,
            indicator = { tabPositions ->
                TabRowDefaults.SecondaryIndicator(
                    modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                    color = ElectricCyan,
                    height = 2.dp
                )
            },
            divider = {}
        ) {
            Tab(
                selected = selectedTabIndex == 0,
                onClick = { selectedTabIndex = 0 },
                text = {
                    Text(
                        text = "Studio Catalog",
                        fontWeight = if (selectedTabIndex == 0) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 0) ElectricCyan else TextMuted
                    )
                }
            )
            Tab(
                selected = selectedTabIndex == 1,
                onClick = { selectedTabIndex = 1 },
                text = {
                    Text(
                        text = "My Themes (${customThemes.size})",
                        fontWeight = if (selectedTabIndex == 1) FontWeight.Bold else FontWeight.Normal,
                        color = if (selectedTabIndex == 1) ElectricCyan else TextMuted
                    )
                }
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        if (selectedTabIndex == 0) {
            // Studio Catalog
            AppThemeCatalog.allThemes.forEach { theme ->
                val isSelected = theme.id == persistedThemeId
                val isPreviewing = theme.id == previewTheme.id
                val isLocked = theme.isPremium && !unlockedThemes.contains(theme.id)

                ThemeCatalogItem(
                    theme = theme,
                    isSelected = isSelected,
                    isPreviewing = isPreviewing,
                    isLocked = isLocked,
                    onPreviewClick = { previewThemeId = theme.id },
                    onApplyClick = {
                        if (isLocked) {
                            themeToUnlock = theme
                        } else {
                            coroutineScope.launch {
                                preferencesDataSource.setSelectedTheme(theme.id)
                                previewThemeId = theme.id
                            }
                        }
                    }
                )
                Spacer(modifier = Modifier.height(12.dp))
            }
        } else {
            // My Themes
            // Create New Theme Button Card
            AppGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { showCreateDialog = true }
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(24.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Create Custom Theme",
                        color = ElectricCyan,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            if (customThemes.isEmpty()) {
                AppGlassCard(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "No custom themes created yet. Tap above to pick your own photo wallpaper, custom accent colors, and personalize your lock screen!",
                        color = TextMuted,
                        fontSize = 13.sp,
                        lineHeight = 18.sp
                    )
                }
            } else {
                customThemes.forEach { customConfig ->
                    val customTheme = customConfig.toAppTheme()
                    val isSelected = customTheme.id == persistedThemeId
                    val isPreviewing = customTheme.id == previewTheme.id

                    AppGlassCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { previewThemeId = customTheme.id },
                        borderColor = if (isPreviewing) customTheme.accentColor else BorderSubtle
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(36.dp)
                                        .clip(CircleShape)
                                        .background(customTheme.accentColor),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Palette,
                                        contentDescription = null,
                                        tint = Color.Black,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text(
                                        text = customTheme.name,
                                        fontWeight = FontWeight.Bold,
                                        color = TextPrimary,
                                        fontSize = 15.sp
                                    )
                                    Text(
                                        text = if (customTheme.backgroundImageUri != null) "Photo Wallpaper" else "Custom Palette",
                                        color = TextMuted,
                                        fontSize = 12.sp
                                    )
                                }
                            }

                            Row(verticalAlignment = Alignment.CenterVertically) {
                                IconButton(
                                    onClick = {
                                        coroutineScope.launch {
                                            preferencesDataSource.deleteCustomTheme(customTheme.id)
                                        }
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete",
                                        tint = Color(0xFFFF5252).copy(alpha = 0.8f),
                                        modifier = Modifier.size(20.dp)
                                    )
                                }

                                if (isSelected) {
                                    AppStatusBadge(text = "Active", isPositive = true)
                                } else {
                                    AppGradientButton(
                                        text = "Apply",
                                        onClick = {
                                            coroutineScope.launch {
                                                preferencesDataSource.setSelectedTheme(customTheme.id)
                                                previewThemeId = customTheme.id
                                            }
                                        },
                                        height = 36.dp
                                    )
                                }
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(12.dp))
                }
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    if (showCreateDialog) {
        CustomThemeDialog(
            onDismiss = { showCreateDialog = false },
            onSaveTheme = { config ->
                coroutineScope.launch {
                    preferencesDataSource.saveCustomTheme(config)
                    previewThemeId = config.id
                }
            }
        )
    }

    themeToUnlock?.let { lockedTheme ->
        ThemeUnlockDialog(
            theme = lockedTheme,
            onDismiss = { themeToUnlock = null },
            onUnlocked = {
                coroutineScope.launch {
                    preferencesDataSource.unlockTheme(lockedTheme.id)
                    preferencesDataSource.setSelectedTheme(lockedTheme.id)
                    previewThemeId = lockedTheme.id
                }
            }
        )
    }
}

@Composable
private fun ThemeCatalogItem(
    theme: AppTheme,
    isSelected: Boolean,
    isPreviewing: Boolean,
    isLocked: Boolean,
    onPreviewClick: () -> Unit,
    onApplyClick: () -> Unit
) {
    AppGlassCard(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPreviewClick),
        borderColor = if (isPreviewing) theme.accentColor else BorderSubtle
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                // Color swatches preview circle
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(theme.backgroundBrush)
                        .border(1.5.dp, theme.accentColor, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(16.dp)
                            .clip(CircleShape)
                            .background(theme.accentColor)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = theme.name,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        if (theme.isPremium) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(PillShape)
                                    .background(Color(0xFFFFD700).copy(alpha = 0.2f))
                                    .padding(horizontal = 6.dp, vertical = 2.dp)
                            ) {
                                Text(
                                    text = if (isLocked) "PRO ₹${theme.priceInr}" else "PRO",
                                    color = Color(0xFFFFD700),
                                    fontSize = 10.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Text(
                        text = theme.description,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary,
                        fontSize = 11.sp,
                        maxLines = 1
                    )
                }
            }

            Spacer(modifier = Modifier.width(10.dp))

            if (isSelected) {
                AppStatusBadge(text = "Applied", isPositive = true)
            } else if (isLocked) {
                AppGradientButton(
                    text = "Unlock",
                    onClick = onApplyClick,
                    height = 36.dp
                )
            } else {
                AppGradientButton(
                    text = "Apply",
                    onClick = onApplyClick,
                    height = 36.dp
                )
            }
        }
    }
}

@Composable
private fun LiveThemePreviewCard(theme: AppTheme) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(theme.backgroundBrush)
            .border(1.dp, theme.glowColor, RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(theme.cardColor),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Palette,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Previewing: ${theme.name}",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Mini PIN Dots Preview
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                repeat(4) { index ->
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(if (index < 2) theme.accentColor else theme.textColor.copy(alpha = 0.25f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mini Keypad Row
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf("1", "2", "3").forEach { digit ->
                    Box(
                        modifier = Modifier
                            .size(width = 36.dp, height = 24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(theme.keyColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = digit,
                            color = theme.textColor,
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}
