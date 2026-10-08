package com.applock.privacy.feature.disguise

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.theme.BackgroundDeep

import androidx.compose.runtime.rememberCoroutineScope
import kotlinx.coroutines.launch

@Composable
fun CalculatorDecoyCover(
    onBypass: () -> Unit,
    onVerifyPin: (suspend (String) -> Boolean)? = null,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current
    val scope = rememberCoroutineScope()

    var displayValue by remember { mutableStateOf("0") }
    var rawInputBuffer by remember { mutableStateOf("") }
    var hintVisible by remember { mutableStateOf(false) }


    fun exitToHome() {
        val homeIntent = Intent(Intent.ACTION_MAIN).apply {
            addCategory(Intent.CATEGORY_HOME)
            flags = Intent.FLAG_ACTIVITY_NEW_TASK
        }
        context.startActivity(homeIntent)
    }

    fun onDigit(d: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        rawInputBuffer += d
        if (displayValue == "0" || displayValue == "Error") {
            displayValue = d
        } else {
            displayValue += d
        }
    }

    fun onOperator(op: String) {
        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
        if (!displayValue.endsWith(" ") && displayValue != "Error") {
            displayValue += " $op "
        }
    }

    fun onClear() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)
        displayValue = "0"
        rawInputBuffer = ""
    }

    fun evaluateMath(expr: String): String {
        return try {
            val tokens = expr.trim().split("\\s+".toRegex())
            if (tokens.size < 3) return expr

            var result = tokens[0].toDoubleOrNull() ?: return expr
            var i = 1
            while (i < tokens.size - 1) {
                val op = tokens[i]
                val nextVal = tokens[i + 1].toDoubleOrNull() ?: return expr
                when (op) {
                    "+" -> result += nextVal
                    "-" -> result -= nextVal
                    "×" -> result *= nextVal
                    "÷" -> {
                        if (nextVal == 0.0) return "Error"
                        result /= nextVal
                    }
                }
                i += 2
            }
            if (result == result.toLong().toDouble()) {
                result.toLong().toString()
            } else {
                "%.4f".format(result).trimEnd('0').trimEnd('.')
            }
        } catch (_: Exception) {
            "Error"
        }
    }

    fun onEquals() {
        haptic.performHapticFeedback(HapticFeedbackType.LongPress)

        val cleanDigits = rawInputBuffer.filter { it.isDigit() }
        if (cleanDigits.isNotEmpty() && onVerifyPin != null) {
            scope.launch {
                if (onVerifyPin(cleanDigits)) {
                    onBypass()
                    return@launch
                } else {
                    displayValue = evaluateMath(displayValue)
                    rawInputBuffer = ""
                }
            }
        } else {
            // Otherwise, perform real calculation
            displayValue = evaluateMath(displayValue)
            rawInputBuffer = ""
        }
    }


    Column(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xFF17171C))
            .padding(horizontal = 20.dp, vertical = 24.dp)
    ) {
        // Subtle top controls
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { exitToHome() }) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Exit to Home",
                    tint = Color(0xFF71717A)
                )
            }

            Text(
                text = "Calculator",
                color = Color(0xFFA1A1AA),
                fontSize = 16.sp,
                fontWeight = FontWeight.Medium
            )

            IconButton(onClick = { hintVisible = !hintVisible }) {
                Icon(
                    imageVector = Icons.Default.Info,
                    contentDescription = "Info",
                    tint = Color(0xFF71717A)
                )
            }
        }

        AnimatedVisibility(
            visible = hintVisible,
            enter = fadeIn()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .background(Color(0xFF27272A), CircleShape)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Disguise Decoy: Enter your PIN & tap '=' to unlock.",
                    color = Color(0xFF38BDF8),
                    fontSize = 12.sp,
                    textAlign = TextAlign.Center
                )
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // Display Screen
        Text(
            text = displayValue,
            color = Color.White,
            fontSize = if (displayValue.length > 9) 36.sp else 48.sp,
            fontWeight = FontWeight.Light,
            textAlign = TextAlign.End,
            maxLines = 2,
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 24.dp, end = 8.dp)
        )

        // Calculator Keypad Grid
        val buttonSpacing = 14.dp

        // Row 1: C, ±, %, ÷
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalcButton(text = "C", bg = Color(0xFF3F3F46), fg = Color(0xFFF43F5E), modifier = Modifier.weight(1f), onClick = { onClear() })
            CalcButton(text = "±", bg = Color(0xFF3F3F46), fg = Color.White, modifier = Modifier.weight(1f), onClick = { /* noop */ })
            CalcButton(text = "%", bg = Color(0xFF3F3F46), fg = Color.White, modifier = Modifier.weight(1f), onClick = { onOperator("%") })
            CalcButton(text = "÷", bg = Color(0xFF2563EB), fg = Color.White, modifier = Modifier.weight(1f), onClick = { onOperator("÷") })
        }

        Spacer(modifier = Modifier.height(buttonSpacing))

        // Row 2: 7, 8, 9, ×
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalcButton(text = "7", modifier = Modifier.weight(1f), onClick = { onDigit("7") })
            CalcButton(text = "8", modifier = Modifier.weight(1f), onClick = { onDigit("8") })
            CalcButton(text = "9", modifier = Modifier.weight(1f), onClick = { onDigit("9") })
            CalcButton(text = "×", bg = Color(0xFF2563EB), fg = Color.White, modifier = Modifier.weight(1f), onClick = { onOperator("×") })
        }

        Spacer(modifier = Modifier.height(buttonSpacing))

        // Row 3: 4, 5, 6, -
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalcButton(text = "4", modifier = Modifier.weight(1f), onClick = { onDigit("4") })
            CalcButton(text = "5", modifier = Modifier.weight(1f), onClick = { onDigit("5") })
            CalcButton(text = "6", modifier = Modifier.weight(1f), onClick = { onDigit("6") })
            CalcButton(text = "-", bg = Color(0xFF2563EB), fg = Color.White, modifier = Modifier.weight(1f), onClick = { onOperator("-") })
        }

        Spacer(modifier = Modifier.height(buttonSpacing))

        // Row 4: 1, 2, 3, +
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalcButton(text = "1", modifier = Modifier.weight(1f), onClick = { onDigit("1") })
            CalcButton(text = "2", modifier = Modifier.weight(1f), onClick = { onDigit("2") })
            CalcButton(text = "3", modifier = Modifier.weight(1f), onClick = { onDigit("3") })
            CalcButton(text = "+", bg = Color(0xFF2563EB), fg = Color.White, modifier = Modifier.weight(1f), onClick = { onOperator("+") })
        }

        Spacer(modifier = Modifier.height(buttonSpacing))

        // Row 5: 0, ., =
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(buttonSpacing)
        ) {
            CalcButton(text = "0", modifier = Modifier.weight(2f), onClick = { onDigit("0") })
            CalcButton(text = ".", modifier = Modifier.weight(1f), onClick = { onDigit(".") })
            CalcButton(text = "=", bg = Color(0xFF10B981), fg = Color.White, modifier = Modifier.weight(1f), onClick = { onEquals() })
        }

        Spacer(modifier = Modifier.height(16.dp))
    }
}

@Composable
private fun CalcButton(
    text: String,
    modifier: Modifier = Modifier,
    bg: Color = Color(0xFF27272A),
    fg: Color = Color.White,
    onClick: () -> Unit
) {
    Box(
        modifier = modifier
            .aspectRatio(if (text == "0") 2.1f else 1f)
            .clip(CircleShape)
            .background(bg)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            color = fg,
            fontSize = 24.sp,
            fontWeight = FontWeight.Medium
        )
    }
}
