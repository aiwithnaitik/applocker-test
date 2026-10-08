package com.applock.privacy.feature.onboarding

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
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MoreHoriz
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppGradientButton
import com.applock.privacy.core.ui.components.AppOutlinedButton
import com.applock.privacy.core.ui.components.AppStatusBadge
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

data class OnboardingReason(
    val id: String,
    val title: String,
    val subtitle: String,
    val icon: ImageVector
)

@Composable
fun OnboardingReasonScreen(
    preferencesDataSource: AppPreferencesDataSource,
    onNavigateNext: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var selectedReasonId by remember { mutableStateOf<String?>("privacy") }
    val scrollState = rememberScrollState()

    val reasons = listOf(
        OnboardingReason(
            id = "privacy",
            title = "I want more privacy",
            subtitle = "Keep private apps hidden from prying eyes",
            icon = Icons.Default.Security
        ),
        OnboardingReason(
            id = "personal_apps",
            title = "I want to protect personal apps",
            subtitle = "Lock WhatsApp, Photos, Instagram, and Banking",
            icon = Icons.Default.PhoneAndroid
        ),
        OnboardingReason(
            id = "shared_phone",
            title = "I share my phone with others",
            subtitle = "Prevent family, friends, or kids from opening sensitive apps",
            icon = Icons.Default.Group
        ),
        OnboardingReason(
            id = "extra_security",
            title = "I want extra security",
            subtitle = "Secondary PIN/biometric authentication layer",
            icon = Icons.Default.Lock
        ),
        OnboardingReason(
            id = "other",
            title = "Other",
            subtitle = "General device safety & privacy utility",
            icon = Icons.Default.MoreHoriz
        )
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 20.dp, vertical = 16.dp)
            .verticalScroll(scrollState)
    ) {
        Spacer(modifier = Modifier.height(16.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppStatusBadge(
                text = "STEP 1 OF 2",
                color = ElectricCyan,
                showDot = false
            )

            Text(
                text = "Skip",
                style = MaterialTheme.typography.bodyMedium,
                fontWeight = FontWeight.SemiBold,
                color = TextMuted,
                modifier = Modifier
                    .clip(PillShape)
                    .clickable {
                        coroutineScope.launch {
                            preferencesDataSource.saveOnboardingReason("Skipped")
                            onNavigateNext()
                        }
                    }
                    .padding(horizontal = 12.dp, vertical = 6.dp)
            )
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Why are you using AppLock?",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextPrimary
        )

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = "Help us understand your privacy priorities. This can be skipped and will not restrict any features.",
            style = MaterialTheme.typography.bodyMedium,
            color = TextMuted,
            lineHeight = 20.sp
        )

        Spacer(modifier = Modifier.height(24.dp))

        reasons.forEach { reason ->
            val isSelected = selectedReasonId == reason.id

            AppGlassCard(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { selectedReasonId = reason.id },
                borderColor = if (isSelected) ElectricCyan else null
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
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(if (isSelected) ElectricCyan.copy(alpha = 0.2f) else SurfaceCard),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = reason.icon,
                                contentDescription = null,
                                tint = if (isSelected) ElectricCyan else TextMuted,
                                modifier = Modifier.size(22.dp)
                            )
                        }

                        Spacer(modifier = Modifier.width(14.dp))

                        Column {
                            Text(
                                text = reason.title,
                                style = MaterialTheme.typography.bodyLarge,
                                fontWeight = FontWeight.Bold,
                                color = TextPrimary
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                text = reason.subtitle,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextSecondary,
                                fontSize = 12.sp
                            )
                        }
                    }

                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(CircleShape)
                            .background(if (isSelected) ElectricCyan else SurfaceCard)
                            .border(1.dp, if (isSelected) ElectricCyan else BorderSubtle, CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        if (isSelected) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = "Selected",
                                tint = BackgroundDeep,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            AppOutlinedButton(
                text = "Skip",
                onClick = {
                    coroutineScope.launch {
                        preferencesDataSource.saveOnboardingReason("Skipped")
                        onNavigateNext()
                    }
                },
                modifier = Modifier.weight(1f),
                height = 50.dp
            )

            AppGradientButton(
                text = "Continue",
                onClick = {
                    coroutineScope.launch {
                        preferencesDataSource.saveOnboardingReason(selectedReasonId ?: "General")
                        onNavigateNext()
                    }
                },
                modifier = Modifier.weight(1.5f),
                height = 50.dp
            )
        }

        Spacer(modifier = Modifier.height(20.dp))
    }
}
