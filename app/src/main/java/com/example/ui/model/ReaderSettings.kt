package com.example.ui.model

import androidx.compose.ui.graphics.Color

enum class PaperTextureStyle {
    PARCHMENT, // 掌阅真实复古羊皮纸
    XUAN,      // 东方传统温润宣纸
    APPLE,     // 苹果手机阅读软件(Apple Books)自然纸质
    BAMBOO,    // 经典绿色护眼纸
    EINK,      // 墨水屏磨砂纸质
    NONE
}

enum class ReaderThemeType(
    val displayName: String,
    val backgroundColor: Color,
    val textColor: Color,
    val secondaryTextColor: Color,
    val isDark: Boolean = false,
    val textureStyle: PaperTextureStyle = PaperTextureStyle.NONE
) {
    XUAN_PAPER("宣纸水墨", Color(0xFFF7F4EE), Color(0xFF2C2A29), Color(0xFF827C75), false, PaperTextureStyle.XUAN),
    IREADER_PARCHMENT("掌阅羊皮", Color(0xFFEFE4D2), Color(0xFF3A2E20), Color(0xFF8A7660), false, PaperTextureStyle.PARCHMENT),
    APPLE_PAPER("苹果纸质", Color(0xFFF6F0E6), Color(0xFF2C2621), Color(0xFF7E756B), false, PaperTextureStyle.APPLE),
    EYE_GREEN("竹青护眼", Color(0xFFDCE8D8), Color(0xFF1F3325), Color(0xFF637C6A), false, PaperTextureStyle.BAMBOO),
    EINK("电子水墨", Color(0xFFEAE6DF), Color(0xFF181818), Color(0xFF666666), false, PaperTextureStyle.EINK),
    NIGHT("暗夜极简", Color(0xFF141414), Color(0xFFE2DFD8), Color(0xFF8E8B84), true, PaperTextureStyle.NONE);

    companion object {
        // Compatibility alias for previous references
        val PAPER_CREAM get() = XUAN_PAPER
        val CHARCOAL get() = NIGHT
        val OBSIDIAN get() = NIGHT
        val PURE_WHITE get() = XUAN_PAPER
        val SOFT_GRAY get() = EINK
    }
}

enum class PageTurnMode(val displayName: String) {
    NONE("极速翻页"),
    SIMULATION("真实书籍翻页"),
    SCROLL("上下滚动")
}

data class ReaderSettings(
    val fontSize: Int = 20,
    val lineSpacingMultiplier: Float = 1.7f,
    val horizontalPadding: Int = 24,
    val theme: ReaderThemeType = ReaderThemeType.XUAN_PAPER,
    val pageTurnMode: PageTurnMode = PageTurnMode.NONE,
    val useEpubOriginalFormat: Boolean = true,
    val showPageIndicatorAlways: Boolean = false,
    val woodShelfEnabled: Boolean = false,
    val isTraditional: Boolean = false,
    val fontType: com.example.ui.theme.ReaderFontType = com.example.ui.theme.ReaderFontType.SERIF,
    val isBold: Boolean = false,
    val encoding: String = "AUTO"
) {
    companion object {
        fun defaultForTheme(isDarkAppTheme: Boolean): ReaderSettings {
            return ReaderSettings(
                theme = if (isDarkAppTheme) ReaderThemeType.NIGHT else ReaderThemeType.XUAN_PAPER
            )
        }
    }
}
