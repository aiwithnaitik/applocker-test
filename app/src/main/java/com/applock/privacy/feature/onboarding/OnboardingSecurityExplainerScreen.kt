package com.applock.privacy.feature.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary

@Composable
fun OnboardingSecurityExplainerScreen(
    onNavigateNext: () -> Unit
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        // Step indicator as clean text
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.Start,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "2/3",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = ElectricCyan,
                fontSize = 16.sp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Central Dynamic Security Showcase Animation
        AnimatedSecurityShieldDemo()

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Lock Any App. Total Privacy.",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            text = "Whenever you or anyone opens a protected app, AppLock instantly activates a security shield that only your secret code or biometric can unlock.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            textAlign = TextAlign.Center,
            lineHeight = 21.sp,
            modifier = Modifier.padding(horizontal = 8.dp)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Uniform feature security highlight cards
        val securityFeatures = listOf(
            SecurityPillItem(
                icon = Icons.Default.Shield,
                iconTint = ElectricCyan,
                title = "Zero-Bypass Interception",
                desc = "Locks instantly on launch, app-switch, or recent tabs"
            ),
            SecurityPillItem(
                icon = Icons.Default.Bolt,
                iconTint = EmeraldSecure,
                title = "Hardware Speed",
                desc = "Direct OS event interception with 0% battery impact"
            ),
            SecurityPillItem(
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF60A5FA),
                title = "Local Device Encryption",
                desc = "Your PIN and biometric never touch any cloud or server"
            )
        )

        securityFeatures.forEach { item ->
            AppGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(72.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxSize(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(item.iconTint.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = item.icon,
                            contentDescription = null,
                            tint = item.iconTint,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column(
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text(
                            text = item.title,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextPrimary,
                            maxLines = 1
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = item.desc,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            fontSize = 11.5.sp,
                            maxLines = 1
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        AppGradientButton(
            text = "Continue",
            onClick = onNavigateNext,
            modifier = Modifier.fillMaxWidth(),
            height = 52.dp
        )

        Spacer(modifier = Modifier.height(16.dp))
    }
}

private data class SecurityPillItem(
    val icon: ImageVector,
    val iconTint: Color,
    val title: String,
    val desc: String
)

@Composable
private fun AnimatedSecurityShieldDemo() {
    val infiniteTransition = rememberInfiniteTransition(label = "SecurityShieldDemo")

    // Ripple expansion
    val rippleScale by infiniteTransition.animateFloat(
        initialValue = 0.9f,
        targetValue = 1.35f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleScale"
    )

    val rippleAlpha by infiniteTransition.animateFloat(
        initialValue = 0.6f,
        targetValue = 0.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(2200, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "RippleAlpha"
    )

    // Pulsing lock scale
    val lockScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "LockScale"
    )

    Box(
        modifier = Modifier
            .size(190.dp),
        contentAlignment = Alignment.Center
    ) {
        // Outer glowing ripple wave
        Box(
            modifier = Modifier
                .size(140.dp)
                .scale(rippleScale)
                .clip(CircleShape)
                .background(ElectricCyan.copy(alpha = rippleAlpha))
        )

        // Middle aura glow
        Box(
            modifier = Modifier
                .size(130.dp)
                .clip(CircleShape)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            ElectricCyan.copy(alpha = 0.28f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Simulated Phone Mockup / Central Protected Card
        Box(
            modifier = Modifier
                .size(112.dp)
                .scale(lockScale)
                .clip(RoundedCornerShape(28.dp))
                .background(SurfaceCard)
                .border(1.5.dp, ElectricCyan.copy(alpha = 0.65f), RoundedCornerShape(28.dp)),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Box(
                    modifier = Modifier
                        .size(54.dp)
                        .clip(CircleShape)
                        .background(
                            Brush.linearGradient(
                                listOf(ElectricCyan, EmeraldSecure)
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = "Shield Locked",
                        tint = BackgroundDeep,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Spacer(modifier = Modifier.height(8.dp))

                Row(
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(7.dp)
                            .clip(CircleShape)
                            .background(EmeraldSecure)
                    )
                    Spacer(modifier = Modifier.width(5.dp))
                    Text(
                        text = "PROTECTED",
                        color = EmeraldSecure,
                        fontSize = 10.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 0.8.sp
                    )
                }
            }
        }
    }
}
