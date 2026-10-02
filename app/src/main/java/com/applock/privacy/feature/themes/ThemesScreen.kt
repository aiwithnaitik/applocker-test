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
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppOutlinedButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary

data class LockThemeItem(
    val id: String,
    val name: String,
    val description: String,
    val previewGradient: Brush,
    val accentColor: Color
)

@Composable
fun ThemesScreen(
    modifier: Modifier = Modifier
) {
    var selectedThemeId by remember { mutableStateOf("sapphire_glass") }
    val scrollState = rememberScrollState()

    val themes = listOf(
        LockThemeItem(
            id = "sapphire_glass",
            name = "Sapphire Glass (Default)",
            description = "Translucent glowing cyan & sapphire inspired by official AppLock icon.",
            previewGradient = Brush.verticalGradient(listOf(Color(0xFF0084FF), Color(0xFF030714))),
            accentColor = ElectricCyan
        ),
        LockThemeItem(
            id = "cyber_neon",
            name = "Cyber Neon",
            description = "Electric ultraviolet and magenta neon aesthetics.",
            previewGradient = Brush.verticalGradient(listOf(Color(0xFFC026D3), Color(0xFF1E1B4B))),
            accentColor = Color(0xFFF43F5E)
        ),
        LockThemeItem(
            id = "emerald_matrix",
            name = "Emerald Matrix",
            description = "Deep cyber emerald green terminal look.",
            previewGradient = Brush.verticalGradient(listOf(Color(0xFF059669), Color(0xFF022C22))),
            accentColor = EmeraldSecure
        ),
        LockThemeItem(
            id = "obsidian_dark",
            name = "Obsidian Stealth",
            description = "Pure pitch black with subtle smoked silver buttons.",
            previewGradient = Brush.verticalGradient(listOf(Color(0xFF334155), Color(0xFF020617))),
            accentColor = Color(0xFF94A3B8)
        )
    )

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        AppTopBar(title = "Lock Themes")

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Select Lock Screen Theme",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
        Text(
            text = "Personalize your PIN & pattern keypad appearance.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted
        )

        Spacer(modifier = Modifier.height(16.dp))

        themes.forEach { theme ->
            val isSelected = selectedThemeId == theme.id

            AppGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedThemeId = theme.id }
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
                                .size(50.dp)
                                .clip(RoundedCornerShape(14.dp))
                                .background(theme.previewGradient)
                                .border(1.dp, theme.accentColor.copy(alpha = 0.5f), RoundedCornerShape(14.dp)),
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
                            Text(
                                text = theme.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
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

                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = "Selected",
                            tint = ElectricCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Custom Wallpaper Teaser
        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Custom Background Wallpaper",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Set a custom photo or live blur effect from your phone gallery.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )
            Spacer(modifier = Modifier.height(14.dp))
            AppOutlinedButton(
                text = "Upload Wallpaper (Phase 11)",
                onClick = {},
                enabled = false
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}
