package com.applock.privacy.feature.onboarding

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.R
import com.applock.privacy.core.common.AppConstants
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first

@Composable
fun SplashScreen(
    preferencesDataSource: AppPreferencesDataSource,
    onNavigateToHome: () -> Unit,
    onNavigateToOnboarding: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "SplashLogoPulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.95f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "PulseScale"
    )

    LaunchedEffect(Unit) {
        // Fast splash screen as mandated by PRD Section 5.1 (does not artificially delay user)
        delay(900)
        val isOnboardingCompleted = preferencesDataSource.isOnboardingCompletedFlow.first()
        if (isOnboardingCompleted) {
            onNavigateToHome()
        } else {
            onNavigateToOnboarding()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                modifier = Modifier
                    .size(108.dp)
                    .scale(pulseScale)
                    .clip(RoundedCornerShape(28.dp)),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.app_logo),
                    contentDescription = "AppLock Logo",
                    modifier = Modifier.size(108.dp)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "AppLock",
                style = MaterialTheme.typography.headlineLarge,
                fontWeight = FontWeight.Bold,
                color = TextPrimary,
                letterSpacing = 1.sp
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Personal Privacy & App Shield",
                style = MaterialTheme.typography.bodyMedium,
                color = TextMuted,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(36.dp))

            CircularProgressIndicator(
                color = ElectricCyan,
                strokeWidth = 2.5.dp,
                modifier = Modifier.size(28.dp)
            )
        }
    }
}
