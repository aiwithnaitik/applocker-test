package com.applock.privacy.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

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
    val glowColor: Color,
    val isPremium: Boolean = false,
    val priceInr: Int = 0,
    val backgroundImageUri: String? = null,
    val isCustom: Boolean = false
) {
    val backgroundBrush: Brush
        get() = Brush.verticalGradient(listOf(bgStart, bgEnd))
}

data class CustomThemeConfig(
    val id: String,
    val name: String,
    val imageUri: String? = null,
    val bgStartHex: Long = 0xFF030714,
    val bgEndHex: Long = 0xFF081426,
    val accentHex: Long = 0xFF00F0FF,
    val cardHex: Long = 0xFF0A192F,
    val keyHex: Long = 0xFF0E223D,
    val textHex: Long = 0xFFFFFFFF
) {
    fun toAppTheme(): AppTheme {
        return AppTheme(
            id = id,
            name = name,
            description = "Custom theme created by you",
            bgStart = Color(bgStartHex),
            bgEnd = Color(bgEndHex),
            accentColor = Color(accentHex),
            cardColor = Color(cardHex),
            keyColor = Color(keyHex),
            textColor = Color(textHex),
            glowColor = Color(accentHex).copy(alpha = 0.25f),
            backgroundImageUri = imageUri,
            isCustom = true
        )
    }

    fun toJson(): JSONObject {
        return JSONObject().apply {
            put("id", id)
            put("name", name)
            put("imageUri", imageUri ?: "")
            put("bgStartHex", bgStartHex)
            put("bgEndHex", bgEndHex)
            put("accentHex", accentHex)
            put("cardHex", cardHex)
            put("keyHex", keyHex)
            put("textHex", textHex)
        }
    }

    companion object {
        fun fromJson(json: JSONObject): CustomThemeConfig {
            val img = json.optString("imageUri", "")
            return CustomThemeConfig(
                id = json.getString("id"),
                name = json.getString("name"),
                imageUri = if (img.isNotEmpty()) img else null,
                bgStartHex = json.optLong("bgStartHex", 0xFF030714),
                bgEndHex = json.optLong("bgEndHex", 0xFF081426),
                accentHex = json.optLong("accentHex", 0xFF00F0FF),
                cardHex = json.optLong("cardHex", 0xFF0A192F),
                keyHex = json.optLong("keyHex", 0xFF0E223D),
                textHex = json.optLong("textHex", 0xFFFFFFFF)
            )
        }
    }
}

object AppThemeCatalog {

    val PureLight = AppTheme(
        id = "pure_light",
        name = "Pure Light",
        description = "Crisp minimalist porcelain white with vibrant ocean azure.",
        bgStart = Color(0xFFF8FAFC),
        bgEnd = Color(0xFFF1F5F9),
        accentColor = Color(0xFF0284C7),
        cardColor = Color(0xFFFFFFFF),
        keyColor = Color(0xFFE2E8F0),
        textColor = Color(0xFF0F172A),
        glowColor = Color(0xFF0284C7).copy(alpha = 0.2f),
        isPremium = false
    )

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
        glowColor = Color(0xFF00F0FF).copy(alpha = 0.25f),
        isPremium = false
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
        glowColor = Color(0xFFFF007F).copy(alpha = 0.25f),
        isPremium = false
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
        glowColor = Color(0xFF00FF88).copy(alpha = 0.25f),
        isPremium = false
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
        glowColor = Color(0xFFE2E8F0).copy(alpha = 0.2f),
        isPremium = false
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
        glowColor = Color(0xFFFFD700).copy(alpha = 0.25f),
        isPremium = true,
        priceInr = 9
    )

    val CrimsonRoyale = AppTheme(
        id = "crimson_royale",
        name = "Crimson Royale",
        description = "Deep velvet garnet with blazing ruby scarlet neon.",
        bgStart = Color(0xFF1A0208),
        bgEnd = Color(0xFF330510),
        accentColor = Color(0xFFFF1E56),
        cardColor = Color(0xFF3D0915),
        keyColor = Color(0xFF570D1F),
        textColor = Color(0xFFFFFFFF),
        glowColor = Color(0xFFFF1E56).copy(alpha = 0.25f),
        isPremium = true,
        priceInr = 9
    )

    val CelestialAmethyst = AppTheme(
        id = "celestial_amethyst",
        name = "Celestial Amethyst",
        description = "Cosmic violet nebula with ethereal lilac and diamond glow.",
        bgStart = Color(0xFF0F0728),
        bgEnd = Color(0xFF1D0E47),
        accentColor = Color(0xFFBF55EC),
        cardColor = Color(0xFF2A1661),
        keyColor = Color(0xFF3A1F82),
        textColor = Color(0xFFFFFFFF),
        glowColor = Color(0xFFBF55EC).copy(alpha = 0.25f),
        isPremium = true,
        priceInr = 9
    )

    val allThemes: List<AppTheme> = listOf(
        PureLight,
        Sapphire,
        CyberNeon,
        EmeraldMatrix,
        ObsidianStealth,
        ImperialGold,
        CrimsonRoyale,
        CelestialAmethyst
    )

    fun getThemeById(id: String, customThemes: List<CustomThemeConfig> = emptyList()): AppTheme {
        val custom = customThemes.find { it.id == id }
        if (custom != null) {
            return custom.toAppTheme()
        }
        return allThemes.find { it.id == id } ?: PureLight
    }

    fun parseCustomThemesJson(jsonString: String?): List<CustomThemeConfig> {
        if (jsonString.isNullOrEmpty()) return emptyList()
        val list = mutableListOf<CustomThemeConfig>()
        try {
            val array = JSONArray(jsonString)
            for (i in 0 until array.length()) {
                list.add(CustomThemeConfig.fromJson(array.getJSONObject(i)))
            }
        } catch (_: Exception) {}
        return list
    }

    fun serializeCustomThemes(list: List<CustomThemeConfig>): String {
        val array = JSONArray()
        list.forEach { array.put(it.toJson()) }
        return array.toString()
    }
}
