package com.applock.privacy.core.ui.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

// Azure & Cyan Brand Colors (Clean, vibrant, high-contrast on light backgrounds)
val ElectricCyan = Color(0xFF0284C7)      // Sky 600 - deep vibrant cyan/azure
val BrightAzure = Color(0xFF2563EB)       // Blue 600 - bold primary
val DeepSapphire = Color(0xFF1D4ED8)      // Blue 700 - deep royal accent
val NeonIce = Color(0xFFE0F2FE)           // Sky 100 - soft pastel chip background

// Gradients
val PrimaryGradient = Brush.horizontalGradient(
    colors = listOf(BrightAzure, ElectricCyan)
)
val CardGradient = Brush.verticalGradient(
    colors = listOf(Color(0xFFFFFFFF), Color(0xFFF8FAFC))
)
val GlassBorderGradient = Brush.linearGradient(
    colors = listOf(Color(0xFFCBD5E1), Color(0xFFE2E8F0), Color(0xFF93C5FD))
)
val GlowGradient = Brush.radialGradient(
    colors = listOf(Color(0x262563EB), Color(0x00F8FAFC))
)

// Background & Surface (Light Theme)
val BackgroundDeep = Color(0xFFF8FAFC)     // Slate 50 - clean modern background
val BackgroundSurface = Color(0xFFF1F5F9)  // Slate 100 - subtle section contrast
val SurfaceCard = Color(0xFFFFFFFF)        // Crisp white card
val SurfaceCardHover = Color(0xFFF1F5F9)   // Slate 100 hover/pressed
val SurfaceGlass = Color(0xF7FFFFFF)       // Clean translucent white
val CardBackground = SurfaceCard           // Backwards compatibility alias

// Functional & Semantic
val EmeraldSecure = Color(0xFF059669)     // Emerald 600
val AmberWarning = Color(0xFFD97706)      // Amber 600
val RoseDestructive = Color(0xFFE11D48)   // Rose 600

// Text (Deep, crisp slate contrast)
val TextPrimary = Color(0xFF0F172A)        // Slate 900
val TextSecondary = Color(0xFF475569)      // Slate 600
val TextMuted = Color(0xFF64748B)          // Slate 500
val TextAccent = ElectricCyan

// Borders & Dividers
val BorderSubtle = Color(0xFFCBD5E1)       // Slate 300 - clear, crisp light border
val BorderHighlight = Color(0xFF38BDF8)    // Sky 400 - highlighted active border
val DividerDark = Color(0xFFE2E8F0)        // Slate 200 - divider
