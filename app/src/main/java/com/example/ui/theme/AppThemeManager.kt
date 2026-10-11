package com.example.ui.theme

import android.content.Context
import androidx.compose.ui.graphics.Color
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class AppThemeColors(
    val id: String,
    val isDark: Boolean,
    val background: Color,
    val surface: Color,
    val surfaceContainerLowest: Color,
    val surfaceContainerLow: Color,
    val surfaceContainer: Color,
    val surfaceContainerHigh: Color,
    val surfaceContainerHighest: Color,
    val primary: Color,
    val onPrimary: Color,
    val secondary: Color,
    val onSecondary: Color,
    val onBackground: Color,
    val onSurface: Color,
    val onSurfaceVariant: Color,
    val outline: Color,
    val outlineVariant: Color,
    val border: Color,
    val borderDivider: Color,
    // Book cover colors
    val coverBackground: Color,
    val coverBorder: Color,
    val coverTextColor: Color,
    val coverProgressColor: Color = Color(0xFF4A443E),
    val accent: Color,
    // Serif typography requirement for light paper theme
    val useSerifFont: Boolean = false,
    // Subtle dot matrix paper texture
    val showDotGridBackground: Boolean = false,
    val dotColor: Color = Color.Transparent,
    // Botanical foliage (Leaves and branches background texture)
    val showBotanicalBackground: Boolean = false,
    val botanicalColor: Color = Color.Transparent,
    // Realistic Oasis and Wonderland background
    val showOasisBackground: Boolean = false,
    val showWonderlandBackground: Boolean = showOasisBackground,
    // Search capsule colors
    val searchBarBackground: Color,
    val searchBarPlaceholder: Color,
    // Tab pill segmented control colors
    val tabTrackBackground: Color,
    val tabSelectedPillBackground: Color,
    val tabSelectedTextColor: Color,
    val tabUnselectedTextColor: Color
)

/**
 * 3-Theme Architecture:
 * 1. DARK: Minimalist Pitch Black theme
 * 2. LIGHT: Paper Cream Light theme (米白色纸意)
 * 3. GREEN: Forest Sage & Botanical theme (青枝绿叶自然风)
 */
enum class AppThemePreset(
    val id: String,
    val title: String,
    val projectTag: String,
    val description: String,
    val colors: AppThemeColors
) {
    DARK(
        id = "theme_pitch_dark",
        title = "极简黑 (Pitch Dark)",
        projectTag = "黑色主题",
        description = "克制深邃的极简墨黑，纯黑底色与高对比文字，沉浸专注",
        colors = AppThemeColors(
            id = "theme_pitch_dark",
            isDark = true,
            background = Color(0xFF131313),
            surface = Color(0xFF131313),
            surfaceContainerLowest = Color(0xFF0E0E0E),
            surfaceContainerLow = Color(0xFF1B1B1B),
            surfaceContainer = Color(0xFF1F1F1F),
            surfaceContainerHigh = Color(0xFF2A2A2A),
            surfaceContainerHighest = Color(0xFF353535),
            primary = Color(0xFFFFFFFF),
            onPrimary = Color(0xFF131313),
            secondary = Color(0xFFC6C6CB),
            onSecondary = Color(0xFF2F3034),
            onBackground = Color(0xFFE2E2E2),
            onSurface = Color(0xFFE2E2E2),
            onSurfaceVariant = Color(0xFFA1A1A6),
            outline = Color(0xFF8E928F),
            outlineVariant = Color(0xFF333333),
            border = Color(0xFF272727),
            borderDivider = Color(0xFF1E1E1E),
            coverBackground = Color(0xFF262626),
            coverBorder = Color(0xFF3E3E3E),
            coverTextColor = Color(0xFFF5F5F3),
            coverProgressColor = Color(0xFFD4D4D8), // Refined platinum silver for dark theme
            accent = Color(0xFFFFFFFF),
            useSerifFont = false,
            showDotGridBackground = false,
            dotColor = Color.Transparent,
            showBotanicalBackground = false,
            botanicalColor = Color.Transparent,
            searchBarBackground = Color(0xFF1B1B1B),
            searchBarPlaceholder = Color(0xFF8E928F),
            tabTrackBackground = Color(0xFF1B1B1B),
            tabSelectedPillBackground = Color(0xFFFFFFFF),
            tabSelectedTextColor = Color(0xFF131313),
            tabUnselectedTextColor = Color(0xFF8E928F)
        )
    ),

    LIGHT(
        id = "theme_paper_cream",
        title = "暖白纸意 (Paper Cream)",
        projectTag = "浅色主题",
        description = "暖米纸质微点背景，全衬线字形排版，素雅温润",
        colors = AppThemeColors(
            id = "theme_paper_cream",
            isDark = false,
            background = Color(0xFFF7F4EE), // Warm rice paper cream
            surface = Color(0xFFF7F4EE),
            surfaceContainerLowest = Color(0xFFFFFFFF),
            surfaceContainerLow = Color(0xFFEFECE5),
            surfaceContainer = Color(0xFFEBE6DF),
            surfaceContainerHigh = Color(0xFFE3DFD7),
            surfaceContainerHighest = Color(0xFFDAD6CE),
            primary = Color(0xFF2C2A29), // Deep charcoal serif title & icons
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF827C75), // Muted warm gray for tags & authors
            onSecondary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF2C2A29),
            onSurface = Color(0xFF2C2A29),
            onSurfaceVariant = Color(0xFF827C75),
            outline = Color(0xFF9C968E),
            outlineVariant = Color(0xFFDDD7CE),
            border = Color(0xFFDDD7CD),
            borderDivider = Color(0xFFE5DFD5),
            coverBackground = Color(0xFFEBE6DF),
            coverBorder = Color(0xFFDDD7CD),
            coverTextColor = Color(0xFF2C2A29),
            coverProgressColor = Color(0xFF4A443E), // Refined deep charcoal bronze (NOT green)
            accent = Color(0xFF2C2A29),
            useSerifFont = true,
            showDotGridBackground = true,
            dotColor = Color(0xFFE2DDD4),
            showBotanicalBackground = false,
            botanicalColor = Color.Transparent,
            searchBarBackground = Color(0xFFEFECE5),
            searchBarPlaceholder = Color(0xFF9C968E),
            tabTrackBackground = Color(0xFFEFECE5),
            tabSelectedPillBackground = Color(0xFF2C2A29),
            tabSelectedTextColor = Color(0xFFFFFFFF),
            tabUnselectedTextColor = Color(0xFF827C75)
        )
    ),

    DARK_GREEN(
        id = "theme_dark_green",
        title = "墨绿 (Deep Evergreen)",
        projectTag = "默认推荐",
        description = "典雅纯净墨绿茶韵，纯净雅致无背景枝影，沉浸护眼",
        colors = AppThemeColors(
            id = "theme_dark_green",
            isDark = false,
            background = Color(0xFFF1F6F2), // Serene tea leaf green
            surface = Color(0xFFF1F6F2),
            surfaceContainerLowest = Color(0xFFFAFCFA),
            surfaceContainerLow = Color(0xFFE8F1EA),
            surfaceContainer = Color(0xFFDFEBE1),
            surfaceContainerHigh = Color(0xFFD4E3D7),
            surfaceContainerHighest = Color(0xFFC7D9CB),
            primary = Color(0xFF1D3B29), // Deep botanical evergreen
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF4F6E5A), // Muted sage moss
            onSecondary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1D3B29),
            onSurface = Color(0xFF1D3B29),
            onSurfaceVariant = Color(0xFF4F6E5A),
            outline = Color(0xFF7A9984),
            outlineVariant = Color(0xFFC5DACB),
            border = Color(0xFFC8DEC9),
            borderDivider = Color(0xFFD3E5D4),
            coverBackground = Color(0xFFD6E3D8),
            coverBorder = Color(0xFFBCD5BD),
            coverTextColor = Color(0xFF1D3B29),
            coverProgressColor = Color(0xFF6B5843), // Warm walnut sandalwood
            accent = Color(0xFF2C5E3F),
            useSerifFont = true,
            showDotGridBackground = false,
            dotColor = Color.Transparent,
            showBotanicalBackground = false, // 墨绿主题不要背景树枝图案
            botanicalColor = Color.Transparent,
            showOasisBackground = false,
            searchBarBackground = Color(0xFFE5EFE7),
            searchBarPlaceholder = Color(0xFF6E8C77),
            tabTrackBackground = Color(0xFFE5EFE7),
            tabSelectedPillBackground = Color(0xFF1D3B29),
            tabSelectedTextColor = Color(0xFFFFFFFF),
            tabUnselectedTextColor = Color(0xFF4F6E5A)
        )
    ),

    GREEN(
        id = "theme_bamboo_green",
        title = "青枝绿叶 (Forest Sage)",
        projectTag = "绿色主题",
        description = "幽深草木，青枝疏影：自然茶青护眼色调，伴有清雅枝叶水墨肌理",
        colors = AppThemeColors(
            id = "theme_bamboo_green",
            isDark = false,
            background = Color(0xFFF1F6F2), // Serene tea leaf green
            surface = Color(0xFFF1F6F2),
            surfaceContainerLowest = Color(0xFFFAFCFA),
            surfaceContainerLow = Color(0xFFE8F1EA),
            surfaceContainer = Color(0xFFDFEBE1),
            surfaceContainerHigh = Color(0xFFD4E3D7),
            surfaceContainerHighest = Color(0xFFC7D9CB),
            primary = Color(0xFF1D3B29), // Deep botanical evergreen
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF4F6E5A), // Muted sage moss
            onSecondary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF1D3B29),
            onSurface = Color(0xFF1D3B29),
            onSurfaceVariant = Color(0xFF4F6E5A),
            outline = Color(0xFF7A9984),
            outlineVariant = Color(0xFFC5DACB),
            border = Color(0xFFC8DEC9),
            borderDivider = Color(0xFFD3E5D4),
            coverBackground = Color(0xFFD6E3D8),
            coverBorder = Color(0xFFBCD5BD),
            coverTextColor = Color(0xFF1D3B29),
            coverProgressColor = Color(0xFF6B5843), // Warm walnut sandalwood
            accent = Color(0xFF2C5E3F),
            useSerifFont = true,
            showDotGridBackground = false,
            dotColor = Color.Transparent,
            showBotanicalBackground = true,
            botanicalColor = Color(0x421F452C),
            showOasisBackground = false,
            searchBarBackground = Color(0xFFE5EFE7),
            searchBarPlaceholder = Color(0xFF6E8C77),
            tabTrackBackground = Color(0xFFE5EFE7),
            tabSelectedPillBackground = Color(0xFF1D3B29),
            tabSelectedTextColor = Color(0xFFFFFFFF),
            tabUnselectedTextColor = Color(0xFF4F6E5A)
        )
    ),

    OASIS(
        id = "theme_oasis",
        title = "绿洲 (Oasis)",
        projectTag = "绿洲清泉",
        description = "热带绿洲清泉：芒果树繁枝硕果，小河流淌，层次分明高级雅致",
        colors = AppThemeColors(
            id = "theme_oasis",
            isDark = false,
            background = Color(0xFFEDF5EF), // Ethereal oasis morning green
            surface = Color(0xFFEDF5EF),
            surfaceContainerLowest = Color(0xFFFAFCFA),
            surfaceContainerLow = Color(0xFFE4F0E7),
            surfaceContainer = Color(0xFFDAE9DE),
            surfaceContainerHigh = Color(0xFFCFE2D4),
            surfaceContainerHighest = Color(0xFFC2D9C8),
            primary = Color(0xFF143823), // Deep enchanted spruce
            onPrimary = Color(0xFFFFFFFF),
            secondary = Color(0xFF4B6E57), // Muted sage moss
            onSecondary = Color(0xFFFFFFFF),
            onBackground = Color(0xFF143823),
            onSurface = Color(0xFF143823),
            onSurfaceVariant = Color(0xFF4B6E57),
            outline = Color(0xFF759981),
            outlineVariant = Color(0xFFC0D8C6),
            border = Color(0xFFC2DCC8),
            borderDivider = Color(0xFFCEE4D3),
            coverBackground = Color(0xFFD4E5D8),
            coverBorder = Color(0xFFB8D3BE),
            coverTextColor = Color(0xFF143823),
            coverProgressColor = Color(0xFF8C6635),
            accent = Color(0xFFFFA000), // Ripe golden mango amber
            useSerifFont = true,
            showDotGridBackground = false,
            dotColor = Color.Transparent,
            showBotanicalBackground = false,
            botanicalColor = Color.Transparent,
            showOasisBackground = true,
            searchBarBackground = Color(0xFFE1EEE4),
            searchBarPlaceholder = Color(0xFF6B8C75),
            tabTrackBackground = Color(0xFFE1EEE4),
            tabSelectedPillBackground = Color(0xFF143823),
            tabSelectedTextColor = Color(0xFFFFFFFF),
            tabUnselectedTextColor = Color(0xFF4B6E57)
        )
    );

    companion object {
        fun fromId(id: String?): AppThemePreset {
            return when (id) {
                DARK_GREEN.id -> DARK_GREEN
                GREEN.id -> GREEN
                OASIS.id, "theme_wonderland" -> OASIS
                DARK.id -> DARK
                LIGHT.id -> LIGHT
                else -> DARK_GREEN
            }
        }
    }
}

/**
 * Global App Theme Manager
 * Manages the theme architecture, persists user selection to SharedPreferences.
 */
object AppThemeManager {
    private const val PREFS_NAME = "app_theme_prefs"
    private const val KEY_THEME = "selected_theme_preset"

    private val _currentTheme = MutableStateFlow(AppThemePreset.DARK_GREEN)
    val currentTheme: StateFlow<AppThemePreset> = _currentTheme.asStateFlow()

    private var initialized = false

    fun init(context: Context) {
        if (initialized) return
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            val themeId = prefs.getString(KEY_THEME, AppThemePreset.DARK_GREEN.id)
            _currentTheme.value = AppThemePreset.fromId(themeId)
            initialized = true
        } catch (_: Exception) {
            _currentTheme.value = AppThemePreset.DARK_GREEN
        }
    }

    fun setTheme(context: Context, preset: AppThemePreset) {
        _currentTheme.value = preset
        try {
            val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            prefs.edit().putString(KEY_THEME, preset.id).apply()
        } catch (_: Exception) {}
    }
}
