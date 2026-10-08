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
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Palette
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.theme.AppTheme
import com.applock.privacy.core.theme.AppThemeCatalog
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.PillShape
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

    var previewThemeId by remember { mutableStateOf(persistedThemeId) }
    val previewTheme = remember(previewThemeId, persistedThemeId) {
        AppThemeCatalog.getThemeById(if (previewThemeId.isNotEmpty()) previewThemeId else persistedThemeId)
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

        Spacer(modifier = Modifier.height(20.dp))

        // Themes Catalog
        Text(
            text = "Standard Themes",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = "All basic themes are 100% free. Choose a theme to customize the lock screen.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(14.dp))

        AppThemeCatalog.allThemes.forEach { theme ->
            val isPersisted = persistedThemeId == theme.id
            val isPreviewing = previewThemeId == theme.id

            AppGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable {
                        previewThemeId = theme.id
                        coroutineScope.launch {
                            preferencesDataSource.setSelectedTheme(theme.id)
                        }
                    },
                borderColor = if (isPersisted) theme.accentColor else if (isPreviewing) theme.accentColor.copy(alpha = 0.5f) else null
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
                        // Theme Swatch Box
                        Box(
                            modifier = Modifier
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(theme.backgroundBrush)
                                .border(1.5.dp, theme.accentColor, RoundedCornerShape(14.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Palette,
                                contentDescription = null,
                                tint = theme.accentColor,
                                modifier = Modifier.size(24.dp)
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
                                if (isPersisted) {
                                    Spacer(modifier = Modifier.width(8.dp))
                                    AppStatusBadge(
                                        text = "APPLIED",
                                        color = EmeraldSecure,
                                        showDot = true
                                    )
                                }
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = theme.description,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp,
                                lineHeight = 16.sp
                            )
                        }
                    }

                    if (isPersisted) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Active",
                            tint = theme.accentColor,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(80.dp))
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
            .border(1.5.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(theme.accentColor.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Lock,
                    contentDescription = null,
                    tint = theme.accentColor,
                    modifier = Modifier.size(20.dp)
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "${theme.name} Preview",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = theme.textColor
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Mini PIN dots preview
            Row(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                for (i in 0 until 4) {
                    Box(
                        modifier = Modifier
                            .size(12.dp)
                            .clip(CircleShape)
                            .background(if (i < 2) theme.accentColor else Color.Transparent)
                            .border(1.dp, theme.accentColor, CircleShape)
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Mini keypad sample buttons
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                listOf("1", "2", "3").forEach { num ->
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(theme.keyColor),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = num,
                            color = theme.textColor,
                            fontSize = 12.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }
}
