package com.applock.privacy.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

data class AppTheme(
    val id: String,
    val name: String,
    val description: String,
    val bgStart: Color,
    val bgEnd: Color,
    val accentColor: Color,
    val cardColor: Color,
    val keyColor: Color,
    val textColor: Color,
    val glowColor: Color
) {
    val backgroundBrush: Brush
        get() = Brush.verticalGradient(listOf(bgStart, bgEnd))
}

object AppThemeCatalog {

    val Sapphire = AppTheme(
        id = "sapphire_glass",
        name = "Sapphire Glass",
        description = "Deep midnight navy with electric cyan neon accents.",
        bgStart = Color(0xFF030714),
        bgEnd = Color(0xFF081426),
        accentColor = Color(0xFF00F0FF),
        cardColor = Color(0xFF0A192F),
        keyColor = Color(0xFF0E223D),
        textColor = Color(0xFFFFFFFF),
        glowColor = Color(0xFF00F0FF).copy(alpha = 0.25f)
    )

    val CyberNeon = AppTheme(
        id = "cyber_neon",
        name = "Cyber Neon",
        description = "Ultraviolet obsidian with electric magenta and rose glow.",
        bgStart = Color(0xFF0D041A),
        bgEnd = Color(0xFF1E0A3C),
        accentColor = Color(0xFFFF007F),
        cardColor = Color(0xFF220D3D),
        keyColor = Color(0xFF321359),
        textColor = Color(0xFFFFFFFF),
        glowColor = Color(0xFFFF007F).copy(alpha = 0.25f)
    )

    val EmeraldMatrix = AppTheme(
        id = "emerald_matrix",
        name = "Emerald Matrix",
        description = "Cyberpunk obsidian with vivid neon emerald green.",
        bgStart = Color(0xFF02140D),
        bgEnd = Color(0xFF052B1E),
        accentColor = Color(0xFF00FF88),
        cardColor = Color(0xFF093325),
        keyColor = Color(0xFF0D4734),
        textColor = Color(0xFFFFFFFF),
        glowColor = Color(0xFF00FF88).copy(alpha = 0.25f)
    )

    val ObsidianStealth = AppTheme(
        id = "obsidian_dark",
        name = "Obsidian Stealth",
        description = "Matte pitch black with smoked platinum highlights.",
        bgStart = Color(0xFF000000),
        bgEnd = Color(0xFF0D0F14),
        accentColor = Color(0xFFE2E8F0),
        cardColor = Color(0xFF131720),
        keyColor = Color(0xFF1E2433),
        textColor = Color(0xFFFFFFFF),
        glowColor = Color(0xFFE2E8F0).copy(alpha = 0.2f)
    )

    val ImperialGold = AppTheme(
        id = "imperial_gold",
        name = "Imperial Gold",
        description = "Rich bronze onyx with radiant royal amber gold.",
        bgStart = Color(0xFF120B03),
        bgEnd = Color(0xFF2B1B08),
        accentColor = Color(0xFFFFD700),
        cardColor = Color(0xFF2A1C0A),
        keyColor = Color(0xFF3D2910),
        textColor = Color(0xFFFFFFFF),
        glowColor = Color(0xFFFFD700).copy(alpha = 0.25f)
    )

    val allThemes: List<AppTheme> = listOf(
        Sapphire,
        CyberNeon,
        EmeraldMatrix,
        ObsidianStealth,
        ImperialGold
    )

    fun getThemeById(id: String): AppTheme {
        return allThemes.find { it.id == id } ?: Sapphire
    }
}
