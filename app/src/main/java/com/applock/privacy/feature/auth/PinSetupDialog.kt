package com.applock.privacy.feature.auth

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.applock.privacy.core.security.SecurityManager
import com.applock.privacy.core.ui.components.AppGlassCard
import com.applock.privacy.core.ui.theme.BackgroundDeep
import com.applock.privacy.core.ui.theme.ElectricCyan
import com.applock.privacy.core.ui.theme.SurfaceCard
import com.applock.privacy.core.ui.theme.TextMuted
import com.applock.privacy.core.ui.theme.TextPrimary
import com.applock.privacy.data.local.AppPreferencesDataSource
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun PinSetupDialog(
    preferencesDataSource: AppPreferencesDataSource,
    onDismissRequest: () -> Unit,
    onPinCreated: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    var step by remember { mutableStateOf(1) } // 1: Create, 2: Confirm
    var firstPin by remember { mutableStateOf("") }
    var currentPin by remember { mutableStateOf("") }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isError by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismissRequest,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .clip(RoundedCornerShape(24.dp))
                .background(BackgroundDeep)
                .padding(20.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Lock,
                            contentDescription = null,
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(onClick = onDismissRequest) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Cancel",
                            tint = TextMuted
                        )
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    text = if (step == 1) "Create Security PIN" else "Confirm Security PIN",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = TextPrimary
                )

                Spacer(modifier = Modifier.height(6.dp))

                Text(
                    text = if (step == 1)
                        "Enter a 4-digit code to protect your apps"
                    else
                        "Re-enter the 4-digit PIN to verify",
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextMuted,
                    fontSize = 13.sp
                )

                Spacer(modifier = Modifier.height(24.dp))

                PinDotsIndicator(
                    pinLength = 4,
                    enteredLength = currentPin.length,
                    isError = isError
                )

                Spacer(modifier = Modifier.height(12.dp))

                if (errorMessage != null) {
                    Text(
                        text = errorMessage ?: "",
                        color = Color(0xFFFF5252),
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Medium
                    )
                } else {
                    Spacer(modifier = Modifier.height(18.dp))
                }

                Spacer(modifier = Modifier.height(16.dp))

                PinKeypad(
                    onNumberClick = { digit ->
                        if (currentPin.length < 4) {
                            val newPin = currentPin + digit
                            currentPin = newPin
                            errorMessage = null
                            isError = false

                            if (newPin.length == 4) {
                                if (step == 1) {
                                    firstPin = newPin
                                    coroutineScope.launch {
                                        delay(250)
                                        step = 2
                                        currentPin = ""
                                    }
                                } else {
                                    if (newPin == firstPin) {
                                        coroutineScope.launch {
                                            SecurityManager.setupNewPin(preferencesDataSource, newPin)
                                            delay(200)
                                            onPinCreated()
                                        }
                                    } else {
                                        isError = true
                                        errorMessage = "PINs do not match. Please try again."
                                        coroutineScope.launch {
                                            delay(800)
                                            step = 1
                                            firstPin = ""
                                            currentPin = ""
                                            isError = false
                                            errorMessage = null
                                        }
                                    }
                                }
                            }
                        }
                    },
                    onDeleteClick = {
                        if (currentPin.isNotEmpty()) {
                            currentPin = currentPin.dropLast(1)
                            isError = false
                            errorMessage = null
                        }
                    }
                )

                Spacer(modifier = Modifier.height(12.dp))
            }
        }
    }
}
