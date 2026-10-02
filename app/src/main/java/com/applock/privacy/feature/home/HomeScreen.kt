package com.applock.privacy.feature.home

import androidx.compose.foundation.Image
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.R
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
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary

@Composable
fun HomeScreen(
    modifier: Modifier = Modifier
) {
    var isMasterProtectionEnabled by remember { mutableStateOf(true) }
    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        AppTopBar(
            title = "AppLock",
            actions = {
                AppStatusBadge(
                    text = if (isMasterProtectionEnabled) "SHIELD ON" else "PAUSED",
                    color = if (isMasterProtectionEnabled) EmeraldSecure else AmberWarning
                )
            }
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero Glassmorphism Shield Card
        AppGlassCard(
            modifier = Modifier.fillMaxWidth()
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
                            .size(60.dp)
                            .clip(RoundedCornerShape(16.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Image(
                            painter = painterResource(id = R.drawable.app_logo),
                            contentDescription = "AppLock Padlock",
                            modifier = Modifier.size(60.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    Column {
                        Text(
                            text = if (isMasterProtectionEnabled) "System Shielded" else "Protection Paused",
                            style = MaterialTheme.typography.titleLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = if (isMasterProtectionEnabled) "App monitor standing by" else "Tap toggle to enable",
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary
                        )
                    }
                }

                AppSwitch(
                    checked = isMasterProtectionEnabled,
                    onCheckedChange = { isMasterProtectionEnabled = it }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Security Metrics Row
        Text(
            text = "Protection Metrics",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            MetricCard(
                title = "Locked",
                value = "0 Apps",
                icon = Icons.Default.Lock,
                accentColor = ElectricCyan,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Security",
                value = "98%",
                icon = Icons.Default.Security,
                accentColor = EmeraldSecure,
                modifier = Modifier.weight(1f)
            )

            MetricCard(
                title = "Intruders",
                value = "0 Alert",
                icon = Icons.Default.RemoveRedEye,
                accentColor = AmberWarning,
                modifier = Modifier.weight(1f)
            )
        }

        Spacer(modifier = Modifier.height(24.dp))

        // Recommended Apps Quick Lock Card
        Text(
            text = "Suggested Privacy Targets",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(12.dp))

        AppGlassCard(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "Secure Your Sensitive Apps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "Instant 1-tap lock protection for social, messaging, and financial apps.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted
            )

            Spacer(modifier = Modifier.height(16.dp))

            val sampleApps = listOf(
                Pair("WhatsApp", "Messaging & Calls"),
                Pair("Photos & Gallery", "Private Media"),
                Pair("Instagram", "Social Feed"),
                Pair("Banking & Wallet", "Finance")
            )

            sampleApps.forEachIndexed { index, (appName, category) ->
                var isAppLocked by remember { mutableStateOf(false) }

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(SurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Default.Shield,
                                contentDescription = null,
                                tint = if (isAppLocked) ElectricCyan else TextMuted,
                                modifier = Modifier.size(20.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Column {
                            Text(
                                text = appName,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = TextPrimary
                            )
                            Text(
                                text = category,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextMuted,
                                fontSize = 12.sp
                            )
                        }
                    }

                    AppSwitch(
                        checked = isAppLocked,
                        onCheckedChange = { isAppLocked = it }
                    )
                }

                if (index < sampleApps.size - 1) {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(1.dp)
                            .background(BorderSubtle.copy(alpha = 0.5f))
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            AppGradientButton(
                text = "Lock All Suggested Apps",
                onClick = { /* Will connect to Phase 6 App Selection */ }
            )
        }

        Spacer(modifier = Modifier.height(24.dp))
    }
}

@Composable
private fun MetricCard(
    title: String,
    value: String,
    icon: ImageVector,
    accentColor: androidx.compose.ui.graphics.Color,
    modifier: Modifier = Modifier
) {
    AppGlassCard(
        modifier = modifier
    ) {
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp)
            )
        }

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = title,
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            fontSize = 11.sp
        )

        Text(
            text = value,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )
    }
}
