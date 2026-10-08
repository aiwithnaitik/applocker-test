package com.applock.privacy.feature.auth

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
import com.applock.privacy.core.theme.AppTheme
import kotlin.math.hypot

@Composable
fun PatternLockView(
    theme: AppTheme,
    isError: Boolean = false,
    enabled: Boolean = true,
    isPatternVisible: Boolean = true,
    onPatternComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val haptic = LocalHapticFeedback.current
    val selectedDots = remember { mutableStateListOf<Int>() }
    var currentDragPosition by remember { mutableStateOf<Offset?>(null) }

    val dotColor by animateColorAsState(
        targetValue = when {
            isError -> Color(0xFFFF5252)
            else -> theme.accentColor
        },
        animationSpec = tween(200),
        label = "PatternAccentColor"
    )

    val density = LocalDensity.current
    val hitRadiusPx = with(density) { 36.dp.toPx() }
    val dotRadiusPx = with(density) { 9.dp.toPx() }
    val glowRadiusPx = with(density) { 22.dp.toPx() }
    val strokeWidthPx = with(density) { 4.5.dp.toPx() }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .aspectRatio(1f)
            .padding(16.dp)
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput

                detectDragGestures(
                    onDragStart = { startOffset ->
                        selectedDots.clear()
                        currentDragPosition = startOffset
                        val size = this.size
                        val cellW = size.width / 3f
                        val cellH = size.height / 3f

                        for (i in 0 until 9) {
                            val row = i / 3
                            val col = i % 3
                            val center = Offset((col + 0.5f) * cellW, (row + 0.5f) * cellH)
                            if (hypot(startOffset.x - center.x, startOffset.y - center.y) <= hitRadiusPx) {
                                selectedDots.add(i)
                                haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                break
                            }
                        }
                    },
                    onDrag = { change, _ ->
                        change.consume()
                        currentDragPosition = change.position
                        val size = this.size
                        val cellW = size.width / 3f
                        val cellH = size.height / 3f

                        for (i in 0 until 9) {
                            if (!selectedDots.contains(i)) {
                                val row = i / 3
                                val col = i % 3
                                val center = Offset((col + 0.5f) * cellW, (row + 0.5f) * cellH)
                                if (hypot(change.position.x - center.x, change.position.y - center.y) <= hitRadiusPx) {
                                    selectedDots.add(i)
                                    haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                                    break
                                }
                            }
                        }
                    },
                    onDragEnd = {
                        currentDragPosition = null
                        if (selectedDots.isNotEmpty()) {
                            val patternString = selectedDots.joinToString("-")
                            onPatternComplete(patternString)
                        }
                    },
                    onDragCancel = {
                        currentDragPosition = null
                        selectedDots.clear()
                    }
                )
            }
    ) {
        Canvas(modifier = Modifier.matchParentSize()) {
            val cellW = size.width / 3f
            val cellH = size.height / 3f

            fun getDotCenter(index: Int): Offset {
                val row = index / 3
                val col = index % 3
                return Offset((col + 0.5f) * cellW, (row + 0.5f) * cellH)
            }

            // Draw connecting lines between consecutive selected dots
            if ((isPatternVisible || isError) && selectedDots.size > 1) {
                for (i in 0 until selectedDots.size - 1) {
                    val p1 = getDotCenter(selectedDots[i])
                    val p2 = getDotCenter(selectedDots[i + 1])
                    drawLine(
                        color = dotColor.copy(alpha = 0.85f),
                        start = p1,
                        end = p2,
                        strokeWidth = strokeWidthPx,
                        cap = StrokeCap.Round
                    )
                }
            }

            // Draw elastic line from last dot to current finger touch point
            if (isPatternVisible && selectedDots.isNotEmpty() && currentDragPosition != null) {
                val lastDotCenter = getDotCenter(selectedDots.last())
                drawLine(
                    color = dotColor.copy(alpha = 0.5f),
                    start = lastDotCenter,
                    end = currentDragPosition!!,
                    strokeWidth = strokeWidthPx * 0.8f,
                    cap = StrokeCap.Round
                )
            }

            // Draw 3x3 dots
            for (i in 0 until 9) {
                val center = getDotCenter(i)
                val isSelected = selectedDots.contains(i)

                if (isSelected) {
                    // Outer glow ring
                    drawCircle(
                        color = dotColor.copy(alpha = 0.25f),
                        radius = glowRadiusPx,
                        center = center
                    )
                    // Inner glowing dot
                    drawCircle(
                        color = dotColor,
                        radius = dotRadiusPx * 1.3f,
                        center = center
                    )
                } else {
                    // Subtle unselected dot
                    drawCircle(
                        color = theme.keyColor.copy(alpha = 0.9f),
                        radius = dotRadiusPx,
                        center = center
                    )
                    drawCircle(
                        color = theme.textColor.copy(alpha = 0.35f),
                        radius = dotRadiusPx * 0.5f,
                        center = center
                    )
                }
            }
        }
    }
}
