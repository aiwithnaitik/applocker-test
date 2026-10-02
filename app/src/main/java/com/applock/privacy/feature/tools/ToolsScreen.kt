package com.applock.privacy.feature.tools

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.AmberWarning
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BrightAzure
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.RoseDestructive
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary

@Composable
fun ToolsScreen(
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        AppTopBar(title = "Privacy Tools")

        Spacer(modifier = Modifier.height(16.dp))

        // Tool 1: Intruder Selfie
        ToolCard(
            title = "Intruder Selfie",
            description = "Silently snaps a photo of anyone entering an incorrect PIN or pattern.",
            icon = Icons.Default.CameraAlt,
            iconTint = RoseDestructive,
            statusText = "PHASE 14",
            statusColor = RoseDestructive
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 2: Notification Shield
        ToolCard(
            title = "Notification Shield",
            description = "Conceals incoming messages and content previews from the lock screen.",
            icon = Icons.Default.NotificationsActive,
            iconTint = AmberWarning,
            statusText = "PHASE 16",
            statusColor = AmberWarning
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 3: Fake Disguise
        ToolCard(
            title = "Disguise Cover",
            description = "Displays a realistic 'App Has Stopped' crash window or calculator decoy.",
            icon = Icons.Default.VisibilityOff,
            iconTint = BrightAzure,
            statusText = "PHASE 17",
            statusColor = BrightAzure
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 4: Private Media Vault
        ToolCard(
            title = "Media Vault",
            description = "Encrypted private vault to hide sensitive photos, videos, and files.",
            icon = Icons.Default.FolderSpecial,
            iconTint = ElectricCyan,
            statusText = "PHASE 18",
            statusColor = ElectricCyan
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Tool 5: Uninstall Protection
        ToolCard(
            title = "Uninstall Protection",
            description = "Prevents unauthorized users from uninstalling AppLock to bypass security.",
            icon = Icons.Default.SecurityUpdateGood,
            iconTint = EmeraldSecure,
            statusText = "PHASE 19",
            statusColor = EmeraldSecure
        )

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun ToolCard(
    title: String,
    description: String,
    icon: ImageVector,
    iconTint: Color,
    statusText: String,
    statusColor: Color
) {
    AppGlassCard(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.Top,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                modifier = Modifier.weight(1f),
                verticalAlignment = Alignment.Top
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = description,
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary,
                        lineHeight = 18.sp
                    )
                }
            }

            AppStatusBadge(
                text = statusText,
                color = statusColor,
                showDot = false
            )
        }
    }
}
