package com.applock.privacy.core.theme

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import org.json.JSONArray
import org.json.JSONObject

enum class ThemeCategory(
    val id: String,
    val title: String,
    val previewCount: String,
    val previewColors: List<Color>
) {
    COLORS(
        "colors",
        "Colors",
        "10 Themes",
        listOf(
            Color(0xFFEF4444),
            Color(0xFF3B82F6),
            Color(0xFF10B981),
            Color(0xFFF97316),
            Color(0xFF8B5CF6),
            Color(0xFFFBBF24)
        )
    ),
    CLASSIC(
        "classic",
        "Classic",
        "8 Themes",
        listOf(
            Color(0xFF0284C7),
            Color(0xFF00F0FF),
            Color(0xFFFF007F),
            Color(0xFF00FF88),
            Color(0xFFBF55EC)
        )
    ),
    PHOTOS(
        "photos",
        "Photos",
        "6 Themes",
        listOf(
            Color(0xFF1E3A8A),
            Color(0xFF065F46),
            Color(0xFF831843),
            Color(0xFF4C1D95)
        )
    ),
    GAMING(
        "gaming",
        "Gaming",
        "5 Themes",
        listOf(
            Color(0xFF00FFCC),
            Color(0xFFFF0055),
            Color(0xFF7928CA),
            Color(0xFF0070F3)
        )
    ),
    COUPLES(
        "couples",
        "Couples",
        "5 Themes",
        listOf(
            Color(0xFFFDA4AF),
            Color(0xFFF472B6),
            Color(0xFFE879F9),
            Color(0xFFFB7185)
        )
    ),
    MUSIC(
        "music",
        "Music",
        "5 Themes",
        listOf(
            Color(0xFFF43F5E),
            Color(0xFF8B5CF6),
            Color(0xFF06B6D4),
            Color(0xFFE11D48)
        )
    ),
    LIFESTYLE(
        "lifestyle",
        "Lifestyle",
        "5 Themes",
        listOf(
            Color(0xFF64748B),
            Color(0xFF78716C),
            Color(0xFF475569),
            Color(0xFF334155)
        )
    ),
    CUTE(
        "cute",
        "Cute",
        "6 Themes",
        listOf(
            Color(0xFFFDE047),
            Color(0xFFF472B6),
            Color(0xFF67E8F9),
            Color(0xFFA7F3D0)
        )
    ),
    EMOJI(
        "emoji",
        "Emoji",
        "5 Themes",
        listOf(
            Color(0xFFFFB703),
            Color(0xFFFB8500),
            Color(0xFFFF006E),
            Color(0xFF8338EC)
        )
    ),
    GRADIENT(
        "gradient",
        "Gradient",
        "6 Themes",
        listOf(
            Color(0xFF38BDF8),
            Color(0xFF818CF8),
            Color(0xFFC084FC),
            Color(0xFFF472B6)
        )
    )
}

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
    val isCustom: Boolean = false,
    val category: ThemeCategory = ThemeCategory.CLASSIC
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
            isCustom = true,
            category = ThemeCategory.CLASSIC
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

    // 1. Classic themes
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
        category = ThemeCategory.CLASSIC
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
        category = ThemeCategory.CLASSIC
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
        category = ThemeCategory.CLASSIC
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
        category = ThemeCategory.CLASSIC
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
        category = ThemeCategory.CLASSIC
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
        priceInr = 9,
        category = ThemeCategory.CLASSIC
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
        priceInr = 9,
        category = ThemeCategory.CLASSIC
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
        priceInr = 9,
        category = ThemeCategory.CLASSIC
    )

    // 2. Solid Colors
    val ColorsList = listOf(
        AppTheme("color_crimson", "Crimson Red", "Solid vibrant ruby red tone", Color(0xFFDC2626), Color(0xFFB91C1C), Color(0xFFFFFFFF), Color(0xFFEF4444), Color(0xFF991B1B), Color(0xFFFFFFFF), Color(0xFFDC2626).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_ocean", "Ocean Blue", "Clean pure cobalt blue shade", Color(0xFF2563EB), Color(0xFF1D4ED8), Color(0xFFFFFFFF), Color(0xFF3B82F6), Color(0xFF1E40AF), Color(0xFFFFFFFF), Color(0xFF2563EB).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_emerald", "Emerald Green", "Lush emerald meadow tone", Color(0xFF059669), Color(0xFF047857), Color(0xFFFFFFFF), Color(0xFF10B981), Color(0xFF065F46), Color(0xFFFFFFFF), Color(0xFF059669).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_sunset", "Sunset Orange", "Energetic warm citrus orange", Color(0xFFEA580C), Color(0xFFC2410C), Color(0xFFFFFFFF), Color(0xFFF97316), Color(0xFF9A3412), Color(0xFFFFFFFF), Color(0xFFEA580C).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_purple", "Royal Violet", "Regal majesty solid purple", Color(0xFF7C3AED), Color(0xFF6D28D9), Color(0xFFFFFFFF), Color(0xFF8B5CF6), Color(0xFF5B21B6), Color(0xFFFFFFFF), Color(0xFF7C3AED).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_amber", "Warm Amber", "Warm golden honey tone", Color(0xFFD97706), Color(0xFFB45309), Color(0xFFFFFFFF), Color(0xFFF59E0B), Color(0xFF92400E), Color(0xFFFFFFFF), Color(0xFFD97706).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_slate", "Midnight Slate", "Clean muted graphite slate", Color(0xFF334155), Color(0xFF1E293B), Color(0xFF38BDF8), Color(0xFF475569), Color(0xFF0F172A), Color(0xFFFFFFFF), Color(0xFF38BDF8).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_rose", "Rose Blush", "Gentle vivid scarlet rose", Color(0xFFE11D48), Color(0xFFBE123C), Color(0xFFFFFFFF), Color(0xFFF43F5E), Color(0xFF9F1239), Color(0xFFFFFFFF), Color(0xFFE11D48).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_teal", "Deep Teal", "Modern oceanic teal blue", Color(0xFF0D9488), Color(0xFF0F766E), Color(0xFFFFFFFF), Color(0xFF14B8A6), Color(0xFF115E59), Color(0xFFFFFFFF), Color(0xFF0D9488).copy(alpha = 0.3f), category = ThemeCategory.COLORS),
        AppTheme("color_indigo", "Vivid Indigo", "Intense twilight deep indigo", Color(0xFF4F46E5), Color(0xFF4338CA), Color(0xFFFFFFFF), Color(0xFF6366F1), Color(0xFF3730A3), Color(0xFFFFFFFF), Color(0xFF4F46E5).copy(alpha = 0.3f), category = ThemeCategory.COLORS)
    )

    // 3. Photos Themes
    val PhotosList = listOf(
        AppTheme("photo_galaxy", "Deep Galaxy", "Astral deep cosmos wallpaper overlay", Color(0xFF020617), Color(0xFF1E1B4B), Color(0xFFA855F7), Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFFFFFFFF), Color(0xFFA855F7).copy(alpha = 0.25f), category = ThemeCategory.PHOTOS),
        AppTheme("photo_aurora", "Aurora Borealis", "Polar northern lights emerald sky", Color(0xFF022C22), Color(0xFF064E3B), Color(0xFF34D399), Color(0xFF065F46), Color(0xFF047857), Color(0xFFFFFFFF), Color(0xFF34D399).copy(alpha = 0.25f), category = ThemeCategory.PHOTOS),
        AppTheme("photo_horizon", "Mountain Dusk", "Silhouetted alpine peaks at twilight", Color(0xFF18181B), Color(0xFF312E81), Color(0xFF818CF8), Color(0xFF27272A), Color(0xFF3F3F46), Color(0xFFFFFFFF), Color(0xFF818CF8).copy(alpha = 0.25f), category = ThemeCategory.PHOTOS),
        AppTheme("photo_ocean", "Pacific Abyss", "Mysterious sapphire ocean currents", Color(0xFF082F49), Color(0xFF0C4A6E), Color(0xFF38BDF8), Color(0xFF0369A1), Color(0xFF075985), Color(0xFFFFFFFF), Color(0xFF38BDF8).copy(alpha = 0.25f), category = ThemeCategory.PHOTOS),
        AppTheme("photo_forest", "Misty Pines", "Evergreen rainforest morning fog", Color(0xFF052E16), Color(0xFF14532D), Color(0xFF4ADE80), Color(0xFF166534), Color(0xFF15803D), Color(0xFFFFFFFF), Color(0xFF4ADE80).copy(alpha = 0.25f), category = ThemeCategory.PHOTOS),
        AppTheme("photo_desert", "Sahara Night", "Starlit desert dunes under moonlight", Color(0xFF451A03), Color(0xFF78350F), Color(0xFFFBBF24), Color(0xFF92400E), Color(0xFFB45309), Color(0xFFFFFFFF), Color(0xFFFBBF24).copy(alpha = 0.25f), category = ThemeCategory.PHOTOS)
    )

    // 4. Gaming Themes
    val GamingList = listOf(
        AppTheme("gaming_arcade", "Cyber Arcade", "Retro synth neon arcade lights", Color(0xFF0D001A), Color(0xFF2D004F), Color(0xFF00FFCC), Color(0xFF3B0764), Color(0xFF581C87), Color(0xFFFFFFFF), Color(0xFF00FFCC).copy(alpha = 0.3f), category = ThemeCategory.GAMING),
        AppTheme("gaming_mecha", "Neon Strike", "High-velocity mecha battle red", Color(0xFF09090B), Color(0xFF18181B), Color(0xFFEF4444), Color(0xFF27272A), Color(0xFF3F3F46), Color(0xFFFFFFFF), Color(0xFFEF4444).copy(alpha = 0.3f), category = ThemeCategory.GAMING),
        AppTheme("gaming_pixel", "Retro 8-Bit", "Vintage golden dungeon quest", Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFFFACC15), Color(0xFF3730A3), Color(0xFF4338CA), Color(0xFFFFFFFF), Color(0xFFFACC15).copy(alpha = 0.3f), category = ThemeCategory.GAMING),
        AppTheme("gaming_esports", "Apex Predator", "Championship tournament emerald", Color(0xFF111827), Color(0xFF1F2937), Color(0xFF10B981), Color(0xFF374151), Color(0xFF4B5563), Color(0xFFFFFFFF), Color(0xFF10B981).copy(alpha = 0.3f), category = ThemeCategory.GAMING),
        AppTheme("gaming_vapor", "Vaporwave Grid", "Sunset wireframe retro grid", Color(0xFF1A0B2E), Color(0xFF3B0764), Color(0xFFF43F5E), Color(0xFF4C1D95), Color(0xFF6B21A8), Color(0xFFFFFFFF), Color(0xFFF43F5E).copy(alpha = 0.3f), category = ThemeCategory.GAMING)
    )

    // 5. Couples Themes
    val CouplesList = listOf(
        AppTheme("couple_pastellove", "Pastel Love", "Gentle romantic rose pastels", Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFFB7185), Color(0xFFFFFFFF), Color(0xFFFECDD3), Color(0xFF881337), Color(0xFFFB7185).copy(alpha = 0.25f), category = ThemeCategory.COUPLES),
        AppTheme("couple_velvet", "Velvet Blossom", "Deep violet and magenta roses", Color(0xFF3B0764), Color(0xFF701A75), Color(0xFFF472B6), Color(0xFF86198F), Color(0xFFA21CAF), Color(0xFFFFFFFF), Color(0xFFF472B6).copy(alpha = 0.25f), category = ThemeCategory.COUPLES),
        AppTheme("couple_sunset", "Dusk Romance", "Warm crimson evening twilight", Color(0xFF4C0519), Color(0xFF831843), Color(0xFFFDA4AF), Color(0xFF9F1239), Color(0xFFBE123C), Color(0xFFFFFFFF), Color(0xFFFDA4AF).copy(alpha = 0.25f), category = ThemeCategory.COUPLES),
        AppTheme("couple_lavender", "Lavender Heart", "Soothing botanical purple lavender", Color(0xFF2E1065), Color(0xFF581C87), Color(0xFFC084FC), Color(0xFF6B21A8), Color(0xFF7E22CE), Color(0xFFFFFFFF), Color(0xFFC084FC).copy(alpha = 0.25f), category = ThemeCategory.COUPLES),
        AppTheme("couple_cotton", "Cotton Candy", "Sweet dreamy carnival pink", Color(0xFFFDF2F8), Color(0xFFFCE7F3), Color(0xFFEC4899), Color(0xFFFFFFFF), Color(0xFFFBCFE8), Color(0xFF831843), Color(0xFFEC4899).copy(alpha = 0.25f), category = ThemeCategory.COUPLES)
    )

    // 6. Music Themes
    val MusicList = listOf(
        AppTheme("music_vinyl", "Vinyl Classic", "Golden retro vinyl records", Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFFF59E0B), Color(0xFF334155), Color(0xFF475569), Color(0xFFFFFFFF), Color(0xFFF59E0B).copy(alpha = 0.25f), category = ThemeCategory.MUSIC),
        AppTheme("music_synth", "Synthwave 80s", "Neon fuchsia dancefloor vibe", Color(0xFF1E0A3C), Color(0xFF3B0764), Color(0xFFEC4899), Color(0xFF581C87), Color(0xFF6B21A8), Color(0xFFFFFFFF), Color(0xFFEC4899).copy(alpha = 0.25f), category = ThemeCategory.MUSIC),
        AppTheme("music_equalizer", "Bass Spectrum", "Pulsing audio equalizer cyan", Color(0xFF030712), Color(0xFF111827), Color(0xFF06B6D4), Color(0xFF1F2937), Color(0xFF374151), Color(0xFFFFFFFF), Color(0xFF06B6D4).copy(alpha = 0.25f), category = ThemeCategory.MUSIC),
        AppTheme("music_electric", "Electro Beats", "Electrifying stadium EDM purple", Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFFA855F7), Color(0xFF312E81), Color(0xFF3730A3), Color(0xFFFFFFFF), Color(0xFFA855F7).copy(alpha = 0.25f), category = ThemeCategory.MUSIC),
        AppTheme("music_acoustic", "Warm Acoustic", "Woodtone acoustic guitar warmth", Color(0xFF29180C), Color(0xFF452410), Color(0xFFFBBF24), Color(0xFF5E3219), Color(0xFF783E1E), Color(0xFFFFFFFF), Color(0xFFFBBF24).copy(alpha = 0.25f), category = ThemeCategory.MUSIC)
    )

    // 7. Lifestyle Themes
    val LifestyleList = listOf(
        AppTheme("life_zen", "Minimal Zen", "Scandinavian monochrome clarity", Color(0xFFF8FAFC), Color(0xFFF1F5F9), Color(0xFF475569), Color(0xFFFFFFFF), Color(0xFFE2E8F0), Color(0xFF0F172A), Color(0xFF475569).copy(alpha = 0.15f), category = ThemeCategory.LIFESTYLE),
        AppTheme("life_nordic", "Nordic Slate", "Cold minimalist northern graphite", Color(0xFF0F172A), Color(0xFF1E293B), Color(0xFF94A3B8), Color(0xFF334155), Color(0xFF475569), Color(0xFFFFFFFF), Color(0xFF94A3B8).copy(alpha = 0.25f), category = ThemeCategory.LIFESTYLE),
        AppTheme("life_latte", "Mocha Espresso", "Roasted espresso cafe tones", Color(0xFF1C1917), Color(0xFF292524), Color(0xFFD97706), Color(0xFF44403C), Color(0xFF57534E), Color(0xFFFFFFFF), Color(0xFFD97706).copy(alpha = 0.25f), category = ThemeCategory.LIFESTYLE),
        AppTheme("life_urban", "Urban Concrete", "Modern architectural stone gray", Color(0xFF18181B), Color(0xFF27272A), Color(0xFFA1A1AA), Color(0xFF3F3F46), Color(0xFF52525B), Color(0xFFFFFFFF), Color(0xFFA1A1AA).copy(alpha = 0.25f), category = ThemeCategory.LIFESTYLE),
        AppTheme("life_sage", "Botanical Sage", "Calm eucalyptus tea greenery", Color(0xFF064E3B), Color(0xFF065F46), Color(0xFF6EE7B7), Color(0xFF047857), Color(0xFF059669), Color(0xFFFFFFFF), Color(0xFF6EE7B7).copy(alpha = 0.25f), category = ThemeCategory.LIFESTYLE)
    )

    // 8. Cute Themes
    val CuteList = listOf(
        AppTheme("cute_kawaii", "Kawaii Bunny", "Cheerful pastel peach bunny theme", Color(0xFFFFF7ED), Color(0xFFFFEDD5), Color(0xFFF97316), Color(0xFFFFFFFF), Color(0xFFFED7AA), Color(0xFF7C2D12), Color(0xFFF97316).copy(alpha = 0.25f), category = ThemeCategory.CUTE),
        AppTheme("cute_bubble", "Bubble Tea", "Sweet golden caramel boba pastels", Color(0xFFFEF3C7), Color(0xFFFDE68A), Color(0xFFD97706), Color(0xFFFFFFFF), Color(0xFFFCD34D), Color(0xFF78350F), Color(0xFFD97706).copy(alpha = 0.25f), category = ThemeCategory.CUTE),
        AppTheme("cute_cloud", "Fluffy Cloud", "Dreamy sky blue marshmallow cloud", Color(0xFFF0F9FF), Color(0xFFE0F2FE), Color(0xFF0284C7), Color(0xFFFFFFFF), Color(0xFFBAE6FD), Color(0xFF0C4A6E), Color(0xFF0284C7).copy(alpha = 0.25f), category = ThemeCategory.CUTE),
        AppTheme("cute_matcha", "Matcha Panda", "Playful soft matcha green cream", Color(0xFFF0FDF4), Color(0xFFDCFCE7), Color(0xFF16A34A), Color(0xFFFFFFFF), Color(0xFFBBF7D0), Color(0xFF14532D), Color(0xFF16A34A).copy(alpha = 0.25f), category = ThemeCategory.CUTE),
        AppTheme("cute_strawberry", "Sweet Berry", "Strawberry shortcake pastel glaze", Color(0xFFFFF1F2), Color(0xFFFFE4E6), Color(0xFFE11D48), Color(0xFFFFFFFF), Color(0xFFFECDD3), Color(0xFF881337), Color(0xFFE11D48).copy(alpha = 0.25f), category = ThemeCategory.CUTE),
        AppTheme("cute_honey", "Honey Bear", "Golden honeycomb sweet honey", Color(0xFFFEFCE8), Color(0xFFFEF08A), Color(0xFFCA8A04), Color(0xFFFFFFFF), Color(0xFFFDE047), Color(0xFF713F12), Color(0xFFCA8A04).copy(alpha = 0.25f), category = ThemeCategory.CUTE)
    )

    // 9. Emoji Themes
    val EmojiList = listOf(
        AppTheme("emoji_sparks", "Sparkles ✨", "Enchanted starry magical glow", Color(0xFF1E1B4B), Color(0xFF312E81), Color(0xFFFACC15), Color(0xFF3730A3), Color(0xFF4338CA), Color(0xFFFFFFFF), Color(0xFFFACC15).copy(alpha = 0.25f), category = ThemeCategory.EMOJI),
        AppTheme("emoji_fire", "Fire Blaze 🔥", "Blazing fiery energetic embers", Color(0xFF450A0A), Color(0xFF7F1D1D), Color(0xFFF97316), Color(0xFF991B1B), Color(0xFFB91C1C), Color(0xFFFFFFFF), Color(0xFFF97316).copy(alpha = 0.25f), category = ThemeCategory.EMOJI),
        AppTheme("emoji_hearts", "Neon Hearts 💖", "Glowing pink hearts party", Color(0xFF500724), Color(0xFF831843), Color(0xFFF472B6), Color(0xFF9F1239), Color(0xFFBE123C), Color(0xFFFFFFFF), Color(0xFFF472B6).copy(alpha = 0.25f), category = ThemeCategory.EMOJI),
        AppTheme("emoji_stars", "Starry Night ⭐", "Twinkling midnight cosmic stars", Color(0xFF020617), Color(0xFF0F172A), Color(0xFF38BDF8), Color(0xFF1E293B), Color(0xFF334155), Color(0xFFFFFFFF), Color(0xFF38BDF8).copy(alpha = 0.25f), category = ThemeCategory.EMOJI),
        AppTheme("emoji_party", "Party Popper 🎉", "Festival celebration confetti neon", Color(0xFF1E0A3C), Color(0xFF3B0764), Color(0xFFEC4899), Color(0xFF581C87), Color(0xFF6B21A8), Color(0xFFFFFFFF), Color(0xFFEC4899).copy(alpha = 0.25f), category = ThemeCategory.EMOJI)
    )

    // 10. Gradient Themes
    val GradientList = listOf(
        AppTheme("grad_aurora", "Aurora Teal", "Teal to indigo mystic gradient", Color(0xFF0D9488), Color(0xFF4338CA), Color(0xFF5EEAD4), Color(0xFF115E59), Color(0xFF0F766E), Color(0xFFFFFFFF), Color(0xFF5EEAD4).copy(alpha = 0.25f), category = ThemeCategory.GRADIENT),
        AppTheme("grad_twilight", "Twilight Glow", "Violet to radiant magenta sunset", Color(0xFF7C3AED), Color(0xFFDB2777), Color(0xFFF472B6), Color(0xFF6D28D9), Color(0xFF831843), Color(0xFFFFFFFF), Color(0xFFF472B6).copy(alpha = 0.25f), category = ThemeCategory.GRADIENT),
        AppTheme("grad_hyper", "Hyper Azure", "Sky blue to cyan velocity shine", Color(0xFF0284C7), Color(0xFF06B6D4), Color(0xFF67E8F9), Color(0xFF0369A1), Color(0xFF0891B2), Color(0xFFFFFFFF), Color(0xFF67E8F9).copy(alpha = 0.25f), category = ThemeCategory.GRADIENT),
        AppTheme("grad_citrus", "Citrus Sun", "Blazing sunrise amber gold", Color(0xFFEA580C), Color(0xFFEAB308), Color(0xFFFEF08A), Color(0xFFC2410C), Color(0xFFCA8A04), Color(0xFFFFFFFF), Color(0xFFFEF08A).copy(alpha = 0.25f), category = ThemeCategory.GRADIENT),
        AppTheme("grad_cosmic", "Cosmic Violet", "Ultraviolet nebula starlight fade", Color(0xFF4C1D95), Color(0xFF831843), Color(0xFFE879F9), Color(0xFF5B21B6), Color(0xFF701A75), Color(0xFFFFFFFF), Color(0xFFE879F9).copy(alpha = 0.25f), category = ThemeCategory.GRADIENT),
        AppTheme("grad_emerald", "Lush Radiance", "Emerald green to ocean teal", Color(0xFF059669), Color(0xFF0D9488), Color(0xFF6EE7B7), Color(0xFF047857), Color(0xFF115E59), Color(0xFFFFFFFF), Color(0xFF6EE7B7).copy(alpha = 0.25f), category = ThemeCategory.GRADIENT)
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
    ) + ColorsList + PhotosList + GamingList + CouplesList + MusicList + LifestyleList + CuteList + EmojiList + GradientList

    fun getThemesForCategory(category: ThemeCategory): List<AppTheme> {
        return allThemes.filter { it.category == category }
    }

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
