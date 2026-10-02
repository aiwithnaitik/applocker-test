package com.applock.privacy.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Sapphire & Cyan Brand Colors inspired by Logo
val ElectricCyan = Color(0xFF00E5FF)
val BrightAzure = Color(0xFF0084FF)
val DeepSapphire = Color(0xFF1D4ED8)
val NeonIce = Color(0xFFBAE6FD)

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(BrightAzure, ElectricCyan)
)
val CardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFF0D1B3E), Color(0xFF070F24))
)
val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(Color(0x6638BDF8), Color(0x1A1E3A8A), Color(0x3300E5FF))
)
val GlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x4D00D2FF), Color(0x00040816))
)

// Background & Surface
val BackgroundDeep = Color(0xFF030714)
val BackgroundSurface = Color(0xFF070F26)
val SurfaceCard = Color(0xFF0B1736)
val SurfaceCardHover = Color(0xFF11224D)
val SurfaceGlass = Color(0xCC0B1736)

// Functional & Semantic
val EmeraldSecure = Color(0xFF10B981)
val AmberWarning = Color(0xFFF59E0B)
val RoseDestructive = Color(0xFFF43F5E)

// Text
val TextPrimary = Color(0xFFF8FAFC)
val TextSecondary = Color(0xFF94A3B8)
val TextMuted = Color(0xFF64748B)
val TextAccent = ElectricCyan

// Borders & Dividers
val BorderSubtle = Color(0x3338BDF8)
val BorderHighlight = Color(0x6600E5FF)
val DividerDark = Color(0xFF1E293B)
