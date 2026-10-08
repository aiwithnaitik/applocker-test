package com.applock.privacy.feature.blocker

import android.content.Intent
import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Block
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Security
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.fragment.app.FragmentActivity
import com.applock.privacy.core.security.AuthResult
import com.applock.privacy.core.security.BiometricHelper
import com.applock.privacy.core.security.SecurityManager
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.components.AppStatusBadge
import com.applock.privacy.core.ui.theme.AppLockTheme
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.RoseDestructive
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.core.ui.theme.TextSecondary
import com.applock.privacy.data.local.AppPreferencesDataSource
import com.applock.privacy.feature.auth.PinDotsIndicator
import com.applock.privacy.feature.auth.PinKeypad
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class BlockedSiteActivity : FragmentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val domain = intent.getStringExtra(EXTRA_DOMAIN) ?: "Website"
        val category = intent.getStringExtra(EXTRA_CATEGORY) ?: "Restricted"
        val preferencesDataSource = AppPreferencesDataSource(this)

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                goHome()
            }
        })

        setContent {
            AppLockTheme {
                BlockedSiteScreen(
                    domain = domain,
                    category = category,
                    preferencesDataSource = preferencesDataSource,
                    activity = this,
                    onGoHome = { goHome() },
                    onBypassSuccess = {
                        WebsiteBlockerBypass.bypass(domain)
                        finish()
                    }
                )
            }
        }
    }

    private fun goHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        startActivity(homeIntent)
        finish()
    }

    companion object {
        const val EXTRA_DOMAIN = "extra_domain"
        const val EXTRA_CATEGORY = "extra_category"
    }
}

@Composable
private fun BlockedSiteScreen(
    domain: String,
    category: String,
    preferencesDataSource: AppPreferencesDataSource,
    activity: FragmentActivity,
    onGoHome: () -> Unit,
    onBypassSuccess: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var isVerifyingPin by remember { mutableStateOf(false) }
    var enteredPin by remember { mutableStateOf("") }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundDeep)
            .padding(24.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.fillMaxWidth()
        ) {
            // Shield warning icon
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(RoseDestructive.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Block,
                    contentDescription = null,
                    tint = RoseDestructive,
                    modifier = Modifier.size(46.dp)
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Website Blocked",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold,
                color = TextPrimary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = domain,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = ElectricCyan
            )

            Spacer(modifier = Modifier.height(10.dp))

            AppStatusBadge(
                text = category,
                color = RoseDestructive,
                showDot = true
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Access to this website is blocked by your AppLock Shield privacy and focus rules.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(28.dp))

            if (isVerifyingPin) {
                // PIN input mode for 10-minute bypass
                PinDotsIndicator(
                    pinLength = 4,
                    enteredLength = enteredPin.length,
                    isError = isError
                )

                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = errorMessage ?: "",
                        color = RoseDestructive,
                        fontSize = 13.sp,
                        fontWeight = FontWeight.Medium
                    )
                }

                Spacer(modifier = Modifier.height(18.dp))

                PinKeypad(
                    onNumberClick = { digitInt ->
                        val digit = digitInt.toString()
                        if (enteredPin.length < 4) {
                            val newPin = enteredPin + digit
                            enteredPin = newPin
                            isError = false
                            errorMessage = null

                            if (newPin.length == 4) {
                                coroutineScope.launch {
                                    val result = SecurityManager.verifyPinWithResult(preferencesDataSource, newPin)
                                    when (result) {
                                        is AuthResult.Success -> {
                                            onBypassSuccess()
                                        }
                                        is AuthResult.Failure -> {
                                            isError = true
                                            errorMessage = "Incorrect PIN"
                                            delay(650)
                                            enteredPin = ""
                                            isError = false
                                        }
                                        is AuthResult.LockedOut -> {
                                            isError = true
                                            errorMessage = "Locked out. Try again in ${result.remainingSeconds}s"
                                            enteredPin = ""
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (enteredPin.isNotEmpty()) {
                            enteredPin = enteredPin.dropLast(1)
                            isError = false
                            errorMessage = null
                        }
                    },
                    onBiometricClick = {
                        if (BiometricHelper.isBiometricAvailable(activity)) {
                            BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                title = "Unlock $domain",
                                subtitle = "Authenticate to temporarily unblock",
                                negativeButtonText = "Cancel",
                                onSuccess = onBypassSuccess,
                                onError = { err -> errorMessage = err }
                            )
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedButton(
                    onClick = {
                        isVerifyingPin = false
                        enteredPin = ""
                        errorMessage = null
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Cancel", color = TextSecondary)
                }
            } else {
                // Primary action: Return to safety
                Button(
                    onClick = onGoHome,
                    colors = ButtonDefaults.buttonColors(containerColor = EmeraldSecure),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Home, contentDescription = null, tint = Color.Black)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Leave Website",
                        fontWeight = FontWeight.Bold,
                        color = Color.Black,
                        fontSize = 16.sp
                    )
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Bypass option: Unlock with PIN
                OutlinedButton(
                    onClick = {
                        if (BiometricHelper.isBiometricAvailable(activity)) {
                            BiometricHelper.showBiometricPrompt(
                                activity = activity,
                                title = "Unlock $domain",
                                subtitle = "Authenticate to temporarily unblock (10 mins)",
                                negativeButtonText = "Use PIN",
                                onSuccess = onBypassSuccess,
                                onError = {
                                    isVerifyingPin = true
                                }
                            )
                        } else {
                            isVerifyingPin = true
                        }
                    },
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                ) {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = TextPrimary)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Unlock with PIN (10 min)",
                        fontWeight = FontWeight.SemiBold,
                        color = TextPrimary
                    )
                }
            }
        }
    }
}
