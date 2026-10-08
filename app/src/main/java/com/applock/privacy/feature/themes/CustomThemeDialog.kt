package com.applock.privacy.feature.themes

import android.net.Uri
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
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material3.BasicAlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.theme.CustomThemeConfig
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.PillShape
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import java.util.UUID

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CustomThemeDialog(
    onDismiss: () -> Unit,
    onSaveTheme: (CustomThemeConfig) -> Unit
) {
    var themeName by remember { mutableStateOf("My Custom Theme") }
    var selectedImageUri by remember { mutableStateOf<Uri?>(null) }

    val accentPalette = listOf(
        0xFF00F0FFL, // Electric Cyan
        0xFF00FF88L, // Neon Emerald
        0xFFFF007FL, // Cyber Magenta
        0xFFFFD700L, // Royal Gold
        0xFFFF5252L, // Crimson
        0xFFBF55ECL, // Amethyst
        0xFFFFFFFFL  // Pure White
    )

    var selectedAccentHex by remember { mutableStateOf(accentPalette[0]) }

    val bgPresets = listOf(
        Pair(0xFF030714L, 0xFF081426L), // Sapphire Dark
        Pair(0xFF000000L, 0xFF0D0F14L), // Pitch Obsidian
        Pair(0xFF0D041AL, 0xFF1E0A3CL), // Ultraviolet
        Pair(0xFF02140DL, 0xFF052B1EL), // Emerald Dark
        Pair(0xFF1A0208L, 0xFF330510L)  // Crimson Velvet
    )

    var selectedBgPreset by remember { mutableStateOf(bgPresets[0]) }

    val imagePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedImageUri = uri
    }

    BasicAlertDialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(24.dp))
                .background(SurfaceCard)
                .border(1.dp, BorderSubtle, RoundedCornerShape(24.dp))
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Create Custom Theme",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )
                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "Close", tint = TextMuted)
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Theme Name input
            OutlinedTextField(
                value = themeName,
                onValueChange = { themeName = it },
                label = { Text("Theme Name", color = TextMuted) },
                singleLine = true,
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = TextPrimary,
                    unfocusedTextColor = TextPrimary,
                    focusedBorderColor = ElectricCyan,
                    unfocusedBorderColor = BorderSubtle
                )
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Wallpaper Image Picker
            Text(
                text = "Lock Screen Wallpaper",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(6.dp))

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFF0B132B))
                    .clickable { imagePickerLauncher.launch("image/*") }
                    .padding(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    imageVector = Icons.Default.AddPhotoAlternate,
                    contentDescription = null,
                    tint = ElectricCyan,
                    modifier = Modifier.size(24.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = if (selectedImageUri != null) "Custom Wallpaper Selected" else "Choose from Gallery",
                        color = TextPrimary,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                    Text(
                        text = if (selectedImageUri != null) "Tap to change image" else "Select wallpaper image for lock screen",
                        color = TextMuted,
                        fontSize = 11.sp
                    )
                }
                if (selectedImageUri != null) {
                    Text(
                        text = "Remove",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.clickable { selectedImageUri = null }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Accent Color Picker
            Text(
                text = "Accent & Key Color",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.SemiBold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(8.dp))

            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(accentPalette) { hex ->
                    val color = Color(hex)
                    val isSelected = selectedAccentHex == hex
                    Box(
                        modifier = Modifier
                            .size(38.dp)
                            .clip(CircleShape)
                            .background(color)
                            .border(
                                width = if (isSelected) 3.dp else 1.dp,
                                color = if (isSelected) Color.White else Color.Transparent,
                                shape = CircleShape
                            )
                            .clickable { selectedAccentHex = hex },
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.Check,
                                contentDescription = null,
                                tint = if (hex == 0xFFFFFFFFL) Color.Black else Color.White,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Background Gradient Presets
            if (selectedImageUri == null) {
                Text(
                    text = "Background Atmosphere",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.SemiBold,
                    color = TextPrimary
                )
                Spacer(modifier = Modifier.height(8.dp))

                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(bgPresets) { preset ->
                        val isSelected = selectedBgPreset == preset
                        Box(
                            modifier = Modifier
                                .size(width = 60.dp, height = 40.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(Color(preset.second))
                                .border(
                                    width = if (isSelected) 2.dp else 1.dp,
                                    color = if (isSelected) ElectricCyan else BorderSubtle,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedBgPreset = preset },
                            contentAlignment = Alignment.Center
                        ) {
                            if (isSelected) {
                                Icon(
                                    imageVector = Icons.Default.Check,
                                    contentDescription = null,
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(16.dp)
                                )
                            }
                        }
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            } else {
                Spacer(modifier = Modifier.height(16.dp))
            }

            AppGradientButton(
                text = "Save & Apply Theme",
                onClick = {
                    val config = CustomThemeConfig(
                        id = "custom_${UUID.randomUUID().toString().take(8)}",
                        name = themeName.ifBlank { "My Custom Theme" },
                        imageUri = selectedImageUri?.toString(),
                        bgStartHex = selectedBgPreset.first,
                        bgEndHex = selectedBgPreset.second,
                        accentHex = selectedAccentHex,
                        cardHex = 0xFF0D1B2A,
                        keyHex = 0xFF14243B,
                        textHex = 0xFFFFFFFF
                    )
                    onSaveTheme(config)
                    onDismiss()
                },
                height = 46.dp
            )
        }
    }
}
