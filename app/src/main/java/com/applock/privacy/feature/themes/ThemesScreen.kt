package com.applock.privacy.feature.themes

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RadioButtonChecked
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.applock.privacy.core.theme.AppTheme
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.theme.ThemeCategory
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
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

    val persistedThemeId by preferencesDataSource.selectedThemeIdFlow.collectAsState(initial = "pure_light")
    val isSmartThemeEnabled by preferencesDataSource.isSmartThemeEnabledFlow.collectAsState(initial = false)
    val customThemes by preferencesDataSource.customThemesFlow.collectAsState(initial = emptyList())
    val isProUser by preferencesDataSource.isProUserFlow.collectAsState(initial = false)

    var previewThemeId by remember { mutableStateOf(persistedThemeId) }
    var showCreateDialog by remember { mutableStateOf(false) }
    var selectedCategoryForSheet by remember { mutableStateOf<ThemeCategory?>(null) }

    val activeTheme = remember(previewThemeId, persistedThemeId, customThemes) {
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
        AppTopBar(
            title = "Themes",
            showLogo = false
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Split Top Section: Left Controls & Right Live Phone Mockup
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            // Left Column
            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(end = 12.dp)
            ) {
                Text(
                    text = if (isSmartThemeEnabled) "Smart\nTheme" else "Standard\nTheme",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.ExtraBold,
                    color = TextPrimary,
                    fontSize = 24.sp,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = "You can choose a theme from the advanced theme library or create a custom theme.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 12.5.sp,
                    lineHeight = 16.5.sp
                )

                Spacer(modifier = Modifier.height(14.dp))

                // Smart Theme Radio Option
                ThemeRadioOption(
                    title = "Smart Theme",
                    isSelected = isSmartThemeEnabled,
                    isVip = true,
                    onClick = {
                        coroutineScope.launch {
                            preferencesDataSource.setSmartThemeEnabled(true)
                        }
                    }
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Standard Theme Radio Option
                ThemeRadioOption(
                    title = "Standard Theme",
                    isSelected = !isSmartThemeEnabled,
                    isVip = false,
                    onClick = {
                        coroutineScope.launch {
                            preferencesDataSource.setSmartThemeEnabled(false)
                        }
                    }
                )
            }

            // Right Column: Phone Mockup Frame
            Column(
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                PhoneMockupPreview(theme = activeTheme)

                Spacer(modifier = Modifier.height(6.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = ElectricCyan,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = if (isSmartThemeEnabled) "Smart Active" else "Current Theme",
                        color = ElectricCyan,
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Custom Themes Row Card
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(16.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                .clickable { showCreateDialog = true }
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
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color(0xFFE0F2FE))
                            .border(1.dp, Color(0xFFBAE6FD), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Brush,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(13.dp))

                    Column {
                        Text(
                            text = "Custom Themes",
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            fontSize = 15.sp
                        )
                        Text(
                            text = if (customThemes.isNotEmpty()) "${customThemes.size} custom theme(s) active" else "Create a custom theme",
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
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(18.dp))
        Divider(color = BorderSubtle.copy(alpha = 0.45f))
        Spacer(modifier = Modifier.height(18.dp))

        // 10 Theme Categories Showcase
        Text(
            text = "Explore Theme Categories",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Column(
            verticalArrangement = Arrangement.spacedBy(14.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            ThemeCategory.values().forEach { category ->
                ThemeCategoryCard(
                    category = category,
                    onClick = { selectedCategoryForSheet = category }
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }

    // Category Theme Selection Dialog / Sheet
    selectedCategoryForSheet?.let { category ->
        CategoryThemesDialog(
            category = category,
            activeThemeId = persistedThemeId,
            onDismiss = { selectedCategoryForSheet = null },
            onSelectTheme = { theme ->
                coroutineScope.launch {
                    preferencesDataSource.setSelectedTheme(theme.id)
                    preferencesDataSource.setSmartThemeEnabled(false)
                    previewThemeId = theme.id
                    selectedCategoryForSheet = null
                }
            }
        )
    }

    if (showCreateDialog) {
        CustomThemeDialog(
            onDismiss = { showCreateDialog = false },
            onSaveTheme = { config ->
                coroutineScope.launch {
                    preferencesDataSource.saveCustomTheme(config)
                    preferencesDataSource.setSmartThemeEnabled(false)
                    previewThemeId = config.id
                }
            }
        )
    }
}

/**
 * Radio button row option matching screenshot design.
 */
@Composable
private fun ThemeRadioOption(
    title: String,
    isSelected: Boolean,
    isVip: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) ElectricCyan.copy(alpha = 0.08f) else Color.Transparent)
            .border(
                1.dp,
                if (isSelected) ElectricCyan.copy(alpha = 0.6f) else BorderSubtle.copy(alpha = 0.4f),
                RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier.fillMaxWidth()
        ) {
            Icon(
                imageVector = if (isSelected) Icons.Default.RadioButtonChecked else Icons.Default.RadioButtonUnchecked,
                contentDescription = null,
                tint = if (isSelected) ElectricCyan else TextMuted,
                modifier = Modifier.size(18.dp)
            )

            Spacer(modifier = Modifier.width(8.dp))

            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                color = if (isSelected) ElectricCyan else TextPrimary,
                fontSize = 13.5.sp
            )

            if (isVip) {
                Spacer(modifier = Modifier.width(6.dp))
                Box(
                    modifier = Modifier
                        .clip(PillShape)
                        .background(Color(0xFFF59E0B))
                        .padding(horizontal = 5.dp, vertical = 1.5.dp)
                ) {
                    Text(
                        text = "VIP",
                        color = Color(0xFF0F172A),
                        fontSize = 8.5.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

/**
 * Scaled mini phone screen mockup displaying active theme.
 */
@Composable
private fun PhoneMockupPreview(
    theme: AppTheme
) {
    Box(
        modifier = Modifier
            .width(124.dp)
            .height(232.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(theme.backgroundBrush)
            .border(1.5.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(horizontal = 10.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween,
            modifier = Modifier.fillMaxSize()
        ) {
            // Top lock icon
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(theme.cardColor.copy(alpha = 0.5f))
                        .border(1.dp, theme.accentColor.copy(alpha = 0.3f), RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = theme.accentColor,
                        modifier = Modifier.size(18.dp)
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = "enter your pin",
                    color = theme.textColor.copy(alpha = 0.7f),
                    fontSize = 7.5.sp
                )

                Spacer(modifier = Modifier.height(4.dp))

                // PIN dots
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    repeat(4) { idx ->
                        Box(
                            modifier = Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(if (idx < 2) theme.accentColor else theme.textColor.copy(alpha = 0.25f))
                        )
                    }
                }
            }

            // Keypad digits grid
            Column(
                verticalArrangement = Arrangement.spacedBy(4.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val rows = listOf(
                    listOf("1", "2", "3"),
                    listOf("4", "5", "6"),
                    listOf("7", "8", "9"),
                    listOf("0")
                )
                for (row in rows) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(6.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        for (digit in row) {
                            Box(
                                modifier = Modifier
                                    .size(18.dp)
                                    .clip(CircleShape)
                                    .background(theme.keyColor.copy(alpha = 0.85f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = digit,
                                    color = theme.textColor,
                                    fontSize = 8.5.sp,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Category Card matching screenshot layout:
 * Upper colored/patterned half and dark bottom bar with title + count badge.
 */
@Composable
private fun ThemeCategoryCard(
    category: ThemeCategory,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(1.dp, BorderSubtle.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
            .clickable(onClick = onClick)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // Upper Preview Half (~90dp)
            if (category == ThemeCategory.COLORS) {
                // Vertical color stripes preview matching screenshot
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                ) {
                    category.previewColors.forEach { col ->
                        Box(
                            modifier = Modifier
                                .weight(1f)
                                .fillMaxHeight()
                                .background(col)
                        )
                    }
                }
            } else if (category == ThemeCategory.CUTE) {
                // Cute pastel candy pattern banner
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(
                            Brush.linearGradient(
                                listOf(Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFFDF2F8), Color(0xFFFEF3C7))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Row(
                        horizontalArrangement = Arrangement.spacedBy(16.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(text = "🐰", fontSize = 28.sp)
                        Text(text = "🌈", fontSize = 28.sp)
                        Text(text = "✨", fontSize = 28.sp)
                        Text(text = "🎀", fontSize = 28.sp)
                        Text(text = "🍓", fontSize = 28.sp)
                    }
                }
            } else {
                // Gradient / scenic banner for other categories
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(90.dp)
                        .background(Brush.horizontalGradient(category.previewColors)),
                    contentAlignment = Alignment.Center
                ) {
                    val iconText = when (category) {
                        ThemeCategory.GAMING -> "🎮  👾  ⚡"
                        ThemeCategory.PHOTOS -> "🏔️  🌌  🌊"
                        ThemeCategory.COUPLES -> "💖  🌸  ✨"
                        ThemeCategory.MUSIC -> "🎵  🎧  🎚️"
                        ThemeCategory.LIFESTYLE -> "☕  🌿  🏛️"
                        ThemeCategory.EMOJI -> "🔥  ⭐  🎉"
                        ThemeCategory.GRADIENT -> "✦  ✦  ✦"
                        else -> "✦  ✦  ✦"
                    }
                    Text(
                        text = iconText,
                        fontSize = 24.sp
                    )
                }
            }

            // Lower Dark Bar matching screenshot
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(Color(0xFF0F172A))
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = category.title,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 15.sp
                )

                Text(
                    text = category.previewCount,
                    color = Color(0xFF94A3B8),
                    fontSize = 12.sp,
                    fontWeight = FontWeight.Medium
                )
            }
        }
    }
}

/**
 * Dialog displaying themes in the selected category.
 */
@Composable
private fun CategoryThemesDialog(
    category: ThemeCategory,
    activeThemeId: String,
    onDismiss: () -> Unit,
    onSelectTheme: (AppTheme) -> Unit
) {
    val themes = remember(category) {
        AppThemeCatalog.getThemesForCategory(category)
    }

    Dialog(onDismissRequest = onDismiss) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF0F172A))
                .border(1.dp, BorderSubtle.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
                .padding(18.dp)
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column {
                        Text(
                            text = "${category.title} Themes",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = Color.White,
                            fontSize = 18.sp
                        )
                        Text(
                            text = "${themes.size} themes available • Tap to apply",
                            style = MaterialTheme.typography.bodySmall,
                            color = Color(0xFF94A3B8),
                            fontSize = 12.sp
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close",
                            tint = Color.White,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))
                Divider(color = Color(0xFF334155))
                Spacer(modifier = Modifier.height(14.dp))

                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(360.dp)
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    themes.forEach { theme ->
                        val isSelected = theme.id == activeThemeId

                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(Color(0xFF1E293B))
                                .border(
                                    1.dp,
                                    if (isSelected) ElectricCyan else Color(0xFF334155),
                                    RoundedCornerShape(14.dp)
                                )
                                .clickable { onSelectTheme(theme) }
                                .padding(12.dp)
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
                                    // Mini Theme Preview Pill
                                    Box(
                                        modifier = Modifier
                                            .size(width = 38.dp, height = 48.dp)
                                            .clip(RoundedCornerShape(8.dp))
                                            .background(theme.backgroundBrush)
                                            .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(8.dp)),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Lock,
                                            contentDescription = null,
                                            tint = theme.accentColor,
                                            modifier = Modifier.size(14.dp)
                                        )
                                    }

                                    Spacer(modifier = Modifier.width(12.dp))

                                    Column {
                                        Text(
                                            text = theme.name,
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 14.5.sp
                                        )
                                        Spacer(modifier = Modifier.height(2.dp))
                                        Text(
                                            text = theme.description,
                                            color = Color(0xFF94A3B8),
                                            fontSize = 11.5.sp,
                                            maxLines = 1
                                        )
                                    }
                                }

                                if (isSelected) {
                                    Box(
                                        modifier = Modifier
                                            .size(24.dp)
                                            .clip(CircleShape)
                                            .background(ElectricCyan),
                                        contentAlignment = Alignment.Center
                                    ) {
                                        Icon(
                                            imageVector = Icons.Default.Check,
                                            contentDescription = null,
                                            tint = Color(0xFF0F172A),
                                            modifier = Modifier.size(16.dp)
                                        )
                                    }
                                } else {
                                    Box(
                                        modifier = Modifier
                                            .clip(PillShape)
                                            .background(Color(0xFF334155))
                                            .padding(horizontal = 10.dp, vertical = 5.dp)
                                    ) {
                                        Text(
                                            text = "APPLY",
                                            color = Color(0xFFE2E8F0),
                                            fontSize = 10.sp,
                                            fontWeight = FontWeight.Bold
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
}
