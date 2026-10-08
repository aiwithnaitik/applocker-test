package com.applock.privacy.feature.disguise

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
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.components.AppButton
import com.applock.privacy.core.ui.components.AppCard
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.components.AppSwitch
import com.applock.privacy.core.ui.components.AppTopBar
import com.applock.privacy.core.ui.theme.AmberWarning
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.BorderSubtle
import com.applock.privacy.core.ui.theme.BrightAzure
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.launch

@Composable
fun DisguiseCoverScreen(
    onNavigateBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val preferencesDataSource = remember { AppPreferencesDataSource(context) }

    val currentModeStr by preferencesDataSource.disguiseModeFlow.collectAsState(initial = "NONE")
    val isAppLockOnly by preferencesDataSource.isDisguiseAppLockOnlyFlow.collectAsState(initial = false)
    val selectedMode = DisguiseMode.fromId(currentModeStr)

    var previewMode by remember { mutableStateOf<DisguiseMode?>(null) }

    // If active preview is selected, show fullscreen preview with dismiss
    if (previewMode == DisguiseMode.CRASH_DIALOG) {
        FakeCrashCover(
            appName = "WhatsApp",
            onBypass = { previewMode = null }
        )
        return
    }

    if (previewMode == DisguiseMode.CALCULATOR) {
        CalculatorDecoyCover(
            onBypass = { previewMode = null }
        )
        return
    }

    val scrollState = rememberScrollState()

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(horizontal = 16.dp)
            .verticalScroll(scrollState)
    ) {
        AppTopBar(
            title = "Disguise Decoy",
            onNavigateBack = onNavigateBack
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Hero info card
        AppGlassCard {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(CircleShape)
                        .background(BrightAzure.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.VisibilityOff,
                        contentDescription = null,
                        tint = BrightAzure,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                Column {
                    Text(
                        text = "Stealth Camouflage",
                        color = TextPrimary,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Conceal the lock screen behind a working decoy so unauthorized users never suspect your apps are locked.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "SELECT DECOY COVER",
            color = TextMuted,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            letterSpacing = 1.sp
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 1: None
        DecoyOptionCard(
            mode = DisguiseMode.NONE,
            isSelected = selectedMode == DisguiseMode.NONE,
            icon = Icons.Default.Security,
            iconTint = TextMuted,
            onClick = {
                scope.launch { preferencesDataSource.setDisguiseMode(DisguiseMode.NONE.id) }
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 2: Fake Crash Dialog
        DecoyOptionCard(
            mode = DisguiseMode.CRASH_DIALOG,
            isSelected = selectedMode == DisguiseMode.CRASH_DIALOG,
            icon = Icons.Default.WarningAmber,
            iconTint = AmberWarning,
            onClick = {
                scope.launch { preferencesDataSource.setDisguiseMode(DisguiseMode.CRASH_DIALOG.id) }
            },
            onPreview = {
                previewMode = DisguiseMode.CRASH_DIALOG
            }
        )

        Spacer(modifier = Modifier.height(10.dp))

        // Option 3: Calculator Decoy
        DecoyOptionCard(
            mode = DisguiseMode.CALCULATOR,
            isSelected = selectedMode == DisguiseMode.CALCULATOR,
            icon = Icons.Default.Calculate,
            iconTint = ElectricCyan,
            onClick = {
                scope.launch { preferencesDataSource.setDisguiseMode(DisguiseMode.CALCULATOR.id) }
            },
            onPreview = {
                previewMode = DisguiseMode.CALCULATOR
            }
        )

        Spacer(modifier = Modifier.height(20.dp))

        // Target Scope Setting
        AppCard {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "AppLock Only",
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                    Text(
                        text = "Apply disguise cover only to AppLock itself. Other protected apps will show the normal lock screen.",
                        color = TextSecondary,
                        fontSize = 12.sp,
                        lineHeight = 16.sp
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                AppSwitch(
                    checked = isAppLockOnly,
                    onCheckedChange = { checked ->
                        scope.launch { preferencesDataSource.setDisguiseAppLockOnly(checked) }
                    }
                )
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        // Secret Bypass Guidance
        AppGlassCard {
            Column {
                Text(
                    text = "Secret Bypass Instructions",
                    color = EmeraldSecure,
                    fontSize = 14.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "• Fake Crash Dialog: Long-press 'Close app' for 1.5 seconds, or tap the error title 3 times.\n• Calculator Decoy: Type your PIN and tap '=' to unlock seamlessly.\n• Normal users tapping 'Close app' or doing math will never see the lock screen.",
                    color = TextSecondary,
                    fontSize = 12.sp,
                    lineHeight = 18.sp
                )
            }
        }

        Spacer(modifier = Modifier.height(30.dp))
    }
}

@Composable
private fun DecoyOptionCard(
    mode: DisguiseMode,
    isSelected: Boolean,
    icon: ImageVector,
    iconTint: Color,
    onClick: () -> Unit,
    onPreview: (() -> Unit)? = null
) {
    val borderColor = if (isSelected) BrightAzure else Color.Transparent

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(SurfaceCard)
            .border(1.5.dp, if (isSelected) BrightAzure else BorderSubtle, RoundedCornerShape(16.dp))
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(iconTint.copy(alpha = 0.12f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        tint = iconTint,
                        modifier = Modifier.size(22.dp)
                    )
                }

                Spacer(modifier = Modifier.width(12.dp))

                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = mode.title,
                        color = TextPrimary,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = mode.subtitle,
                        color = if (isSelected) BrightAzure else TextMuted,
                        fontSize = 12.sp
                    )
                }

                RadioButton(
                    selected = isSelected,
                    onClick = onClick,
                    colors = RadioButtonDefaults.colors(
                        selectedColor = BrightAzure,
                        unselectedColor = TextMuted
                    )
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = mode.description,
                color = TextSecondary,
                fontSize = 12.sp,
                lineHeight = 16.sp
            )

            if (onPreview != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onPreview)
                        .background(BrightAzure.copy(alpha = 0.1f))
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.RemoveRedEye,
                        contentDescription = "Preview",
                        tint = BrightAzure,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Test Preview",
                        color = BrightAzure,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}
