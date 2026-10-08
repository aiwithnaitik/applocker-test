package com.applock.privacy.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.applock.privacy.core.ui.theme.EmeraldSecure
import com.applock.privacy.core.ui.theme.PillShape

@Composable
fun AppStatusBadge(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = EmeraldSecure,
    isPositive: Boolean = true,
    showDot: Boolean = true
) {
    val badgeColor = if (isPositive) color else Color(0xFFFF5252)

    Box(
        modifier = modifier
            .clip(PillShape)
            .background(badgeColor.copy(alpha = 0.15f))
            .border(1.dp, badgeColor.copy(alpha = 0.35f), PillShape)
            .padding(horizontal = 10.dp, vertical = 4.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (showDot) {
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(badgeColor)
                )
                Spacer(modifier = Modifier.width(6.dp))
            }
            Text(
                text = text,
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.SemiBold,
                color = badgeColor,
                fontSize = 11.sp
            )
        }
    }
}
