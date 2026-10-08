package com.applock.privacy.feature.onboarding

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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.launch

data class FeatureHighlight(
    val title: String,
    val description: String,
    val icon: ImageVector
)

@Composable
fun OnboardingCompleteScreen(
    preferencesDataSource: AppPreferencesDataSource,
    onNavigateToHome: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scrollState = rememberScrollState()

    val highlights = listOf(
        FeatureHighlight(
            title = "Personal App Shield",
            description = "Protect WhatsApp, Photos, Instagram, Banking, and any selected apps.",
            icon = Icons.Default.Lock
        ),
        FeatureHighlight(
            title = "Fast & Frictionless",
            description = "Rapid authentication with fingerprint, face biometric, or secret PIN.",
            icon = Icons.Default.FlashOn
        ),
        FeatureHighlight(
            title = "Strictly Device-Local",
            description = "Zero trackers, zero cloud syncing. Your security data never leaves your device.",
            icon = Icons.Default.Security
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppStatusBadge(
                text = "STEP 2 OF 2",
                color = ElectricCyan,
                showDot = false
            )
        }

        Spacer(modifier = Modifier.height(28.dp))

        // Hero glowing shield emblem
        Box(
            modifier = Modifier
                .size(96.dp)
                .clip(CircleShape)
                .background(ElectricCyan.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape)
                    .background(ElectricCyan.copy(alpha = 0.25f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Shield,
                    contentDescription = "Security Shield",
                    tint = ElectricCyan,
                    modifier = Modifier.size(40.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "You're All Set!",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Your privacy foundation is ready. You can now select apps to lock and manage security preferences.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            lineHeight = 20.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(28.dp))

        highlights.forEach { highlight ->
            AppGlassCard(
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(SurfaceCard),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = highlight.icon,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(22.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = highlight.title,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = highlight.description,
                            style = MaterialTheme.typography.bodyMedium,
                            color = TextSecondary,
                            fontSize = 12.sp,
                            lineHeight = 16.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppGradientButton(
            text = "Get Started",
            onClick = {
                coroutineScope.launch {
                    preferencesDataSource.setOnboardingCompleted(true)
                    onNavigateToHome()
                }
            },
            modifier = Modifier.fillMaxWidth(),
            height = 52.dp
        )

        Spacer(modifier = Modifier.height(20.dp))
    }
}
