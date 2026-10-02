package com.applock.privacy.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.applock.privacy.core.ui.theme.GlassBorderGradient
import com.applock.privacy.core.ui.theme.SquircleShape
import com.applock.privacy.core.ui.theme.SurfaceGlass

@Composable
fun AppGlassCard(
    modifier: Modifier = Modifier,
    shape: RoundedCornerShape = SquircleShape,
    borderBrush: Brush = GlassBorderGradient,
    borderColor: Color? = null,
    borderWidth: Dp = 1.dp,
    backgroundColor: Color = SurfaceGlass,
    onClick: (() -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val effectiveBorderStroke = if (borderColor != null) {
        BorderStroke(borderWidth, borderColor)
    } else {
        BorderStroke(borderWidth, borderBrush)
    }
    val clickableModifier = if (onClick != null) {
        Modifier.clickable(onClick = onClick)
    } else {
        Modifier
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(backgroundColor)
            .border(effectiveBorderStroke, shape)
            .then(clickableModifier)
            .padding(18.dp)
    ) {
        Column {
            content()
        }
    }
}
