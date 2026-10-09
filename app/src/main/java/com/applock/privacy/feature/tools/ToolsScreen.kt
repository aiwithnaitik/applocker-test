package com.applock.privacy.feature.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
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
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.SecurityUpdateGood
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.AmberWarning
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.BrightAzure
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.PillShape
import com.applock.privacy.core.ui.theme.RoseDestructive
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary

@Composable
fun ToolsScreen(
    onNavigateToIntruderLogs: () -> Unit = {},
    onNavigateToNotificationShield: () -> Unit = {},
    onNavigateToWebsiteBlocker: () -> Unit = {},
    onNavigateToPrivateBrowser: () -> Unit = {},
    onNavigateToDisguiseCover: () -> Unit = {},
    onNavigateToMediaVault: () -> Unit = {},
    onNavigateToUninstallProtection: () -> Unit = {},
    onNavigateToProSubscription: () -> Unit = {},
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

        Text(
            text = "Essential security utilities to defend your media, identity & web browsing.",
            style = MaterialTheme.typography.bodySmall,
            color = TextMuted,
            fontSize = 12.5.sp,
            modifier = Modifier.padding(bottom = 14.dp)
        )

        // ── 1. SPOTLIGHT HERO: PRIVATE MEDIA VAULT ──────────────────────────────
        FeaturedHeroToolCard(
            title = "Private Media Vault",
            subtitle = "Hide personal photos & videos with AES-256 encrypted storage, invisible to gallery.",
            badgeText = "ENCRYPTED VAULT",
            icon = Icons.Default.FolderSpecial,
            accentColor = Color(0xFF7C3AED), // Royal Violet
            onClick = onNavigateToMediaVault
        )

        Spacer(modifier = Modifier.height(18.dp))

        // ── SECTION HEADER ───────────────────────────────────────────────────
        Text(
            text = "SECURITY UTILITIES",
            style = MaterialTheme.typography.labelSmall,
            color = TextMuted,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp,
            fontSize = 11.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // ── 2. 2-COLUMN BENTO GRID: 6 CORE TOOLS ─────────────────────────────
        // Row 1: Intruder Selfie & Fake Disguise
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoToolCard(
                modifier = Modifier.weight(1f),
                title = "Intruder Selfie",
                subtitle = "Auto-captures photo & time of failed unlocks",
                tag = "Watchdog",
                icon = Icons.Default.CameraAlt,
                accentColor = EmeraldSecure,
                onClick = onNavigateToIntruderLogs
            )
            BentoToolCard(
                modifier = Modifier.weight(1f),
                title = "Fake Disguise",
                subtitle = "Decoy crash screen & calculator masks",
                tag = "Stealth Decoy",
                icon = Icons.Default.VisibilityOff,
                accentColor = RoseDestructive,
                onClick = onNavigateToDisguiseCover
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 2: Website Blocker & Private Browser
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoToolCard(
                modifier = Modifier.weight(1f),
                title = "Web Blocker",
                subtitle = "Intercepts adult, addictive & unsafe domains",
                tag = "Focus Shield",
                icon = Icons.Default.Language,
                accentColor = ElectricCyan,
                onClick = onNavigateToWebsiteBlocker
            )
            BentoToolCard(
                modifier = Modifier.weight(1f),
                title = "Private Browser",
                subtitle = "Zero-trace web tabs with instant shredding",
                tag = "Incognito",
                icon = Icons.Default.Explore,
                accentColor = BrightAzure,
                onClick = onNavigateToPrivateBrowser
            )
        }

        Spacer(modifier = Modifier.height(12.dp))

        // Row 3: Notification Shield & Uninstall Protection
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            BentoToolCard(
                modifier = Modifier.weight(1f),
                title = "Notification Shield",
                subtitle = "Conceals locked app preview banners",
                tag = "Anti-Peek",
                icon = Icons.Default.NotificationsActive,
                accentColor = AmberWarning,
                onClick = onNavigateToNotificationShield
            )
            BentoToolCard(
                modifier = Modifier.weight(1f),
                title = "Uninstall Shield",
                subtitle = "Blocks unauthorized deletion or bypass",
                tag = "Anti-Tamper",
                icon = Icons.Default.SecurityUpdateGood,
                accentColor = Color(0xFF0D9488), // Teal
                onClick = onNavigateToUninstallProtection
            )
        }

        Spacer(modifier = Modifier.height(18.dp))

        // ── 3. BOTTOM HERO: PRO VIP SUITE ────────────────────────────────────
        ProVipBannerCard(
            onClick = onNavigateToProSubscription
        )

        Spacer(modifier = Modifier.height(28.dp))
    }
}

/**
 * Full-width spotlight hero card for premier features like Private Media Vault.
 */
@Composable
private fun FeaturedHeroToolCard(
    title: String,
    subtitle: String,
    badgeText: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.08f),
                        SurfaceCard,
                        accentColor.copy(alpha = 0.04f)
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.4f),
                        BorderSubtle.copy(alpha = 0.7f),
                        accentColor.copy(alpha = 0.2f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(50.dp)
                    .clip(RoundedCornerShape(15.dp))
                    .background(accentColor.copy(alpha = 0.14f))
                    .border(1.2.dp, accentColor.copy(alpha = 0.35f), RoundedCornerShape(15.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(26.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextPrimary,
                        fontSize = 15.5.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Box(
                        modifier = Modifier
                            .clip(PillShape)
                            .background(accentColor.copy(alpha = 0.12f))
                            .border(1.dp, accentColor.copy(alpha = 0.3f), PillShape)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = badgeText,
                            color = accentColor,
                            fontSize = 9.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
                Spacer(modifier = Modifier.height(3.dp))
                Text(
                    text = subtitle,
                    style = MaterialTheme.typography.bodySmall,
                    color = TextMuted,
                    fontSize = 11.5.sp,
                    lineHeight = 15.5.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(accentColor.copy(alpha = 0.12f))
                    .border(1.dp, accentColor.copy(alpha = 0.3f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ArrowForward,
                    contentDescription = "Open",
                    tint = accentColor,
                    modifier = Modifier.size(15.dp)
                )
            }
        }
    }
}

/**
 * Modern 2-column bento tile for security tools.
 */
@Composable
private fun BentoToolCard(
    title: String,
    subtitle: String,
    tag: String,
    icon: ImageVector,
    accentColor: Color,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxHeight()
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.06f),
                        SurfaceCard
                    )
                )
            )
            .border(
                1.dp,
                Brush.verticalGradient(
                    colors = listOf(
                        accentColor.copy(alpha = 0.35f),
                        BorderSubtle.copy(alpha = 0.6f)
                    )
                ),
                RoundedCornerShape(20.dp)
            )
            .clickable(onClick = onClick)
            .padding(13.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Top Row: Glowing squircle icon & action arrow
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(RoundedCornerShape(12.dp))
                        .background(accentColor.copy(alpha = 0.13f))
                        .border(1.dp, accentColor.copy(alpha = 0.3f), RoundedCornerShape(12.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(Color(0xFFF1F5F9))
                        .border(1.dp, Color(0xFFE2E8F0), CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowForward,
                        contentDescription = null,
                        tint = accentColor,
                        modifier = Modifier.size(13.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Subtitle Tag
            Text(
                text = tag.uppercase(),
                style = MaterialTheme.typography.labelSmall,
                color = accentColor,
                fontWeight = FontWeight.Bold,
                fontSize = 9.sp,
                letterSpacing = 0.7.sp
            )

            Spacer(modifier = Modifier.height(2.dp))

            // Title
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                fontSize = 14.sp,
                maxLines = 1
            )

            Spacer(modifier = Modifier.height(3.dp))

            // Subtitle description
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = TextMuted,
                fontSize = 11.sp,
                lineHeight = 14.5.sp,
                maxLines = 2
            )
        }
    }
}

/**
 * Bottom Pro VIP Access Banner.
 */
@Composable
private fun ProVipBannerCard(
    onClick: () -> Unit
) {
    val goldAccent = Color(0xFFD97706) // Deep Gold / Amber 600

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        Color(0xFF0F172A), // Slate 900
                        Color(0xFF1E293B)  // Slate 800
                    )
                )
            )
            .border(
                1.dp,
                Brush.horizontalGradient(
                    colors = listOf(
                        Color(0xFFF59E0B).copy(alpha = 0.5f),
                        ElectricCyan.copy(alpha = 0.4f),
                        Color(0xFFF59E0B).copy(alpha = 0.25f)
                    )
                ),
                RoundedCornerShape(22.dp)
            )
            .clickable(onClick = onClick)
            .padding(15.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(Color(0xFFF59E0B).copy(alpha = 0.18f))
                    .border(1.2.dp, Color(0xFFF59E0B).copy(alpha = 0.45f), RoundedCornerShape(14.dp)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.WorkspacePremium,
                    contentDescription = null,
                    tint = Color(0xFFFBBF24),
                    modifier = Modifier.size(25.dp)
                )
            }

            Spacer(modifier = Modifier.width(13.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "Pro VIP Suite",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    fontSize = 15.sp
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = "Unlimited custom themes, stealth covers & priority defense.",
                    style = MaterialTheme.typography.bodySmall,
                    color = Color(0xFF94A3B8),
                    fontSize = 11.5.sp,
                    lineHeight = 15.sp
                )
            }

            Spacer(modifier = Modifier.width(8.dp))

            Box(
                modifier = Modifier
                    .clip(PillShape)
                    .background(
                        Brush.horizontalGradient(
                            listOf(Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    .padding(horizontal = 11.dp, vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "VIP →",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 11.5.sp
                )
            }
        }
    }
}
