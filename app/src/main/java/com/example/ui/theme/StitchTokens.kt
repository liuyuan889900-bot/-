package com.example.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.unit.dp

/**
 * Design System Tokens linked to the active AppThemePreset
 * Supports:
 * 1. DARK: Minimalist Pitch Dark
 * 2. LIGHT: Paper Cream (1:1 screenshot replica)
 */
object StitchTokens {
    val Background: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.background

    val Surface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surface

    val SurfaceContainerLowest: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surfaceContainerLowest

    val SurfaceContainerLow: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surfaceContainerLow

    val SurfaceContainer: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surfaceContainer

    val SurfaceContainerHigh: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surfaceContainerHigh

    val SurfaceContainerHighest: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surfaceContainerHighest

    val Primary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.primary

    val OnPrimary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.onPrimary

    val PrimaryFixed: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.primary

    val PrimaryFixedDim: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.secondary

    val Secondary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.secondary

    val OnSecondary: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.onSecondary

    val SecondaryContainer: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.surfaceContainerHigh

    val OnSurface: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.onSurface

    val OnSurfaceVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.onSurfaceVariant

    val Outline: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.outline

    val OutlineVariant: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.outlineVariant

    val Border: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.border

    val BorderDivider: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.borderDivider

    val GoldAccent: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.accent

    // Book Cover tokens
    val CoverBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.coverBackground

    val CoverBorder: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.coverBorder

    val CoverTextColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.coverTextColor

    val CoverProgress: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.coverProgressColor

    // Global Font Family: switches to Serif when in Light Paper theme
    val AppFontFamily: FontFamily
        @Composable
        @ReadOnlyComposable
        get() = if (LocalAppColors.current.useSerifFont) FontFamily.Serif else FontFamily.Default

    val IsDark: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.isDark

    val ShowDotGridBackground: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.showDotGridBackground

    val DotColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.dotColor

    val ShowBotanicalBackground: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.showBotanicalBackground

    val BotanicalColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.botanicalColor

    val ShowWonderlandBackground: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.showWonderlandBackground

    val ShowOasisBackground: Boolean
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.showOasisBackground

    val Accent: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.accent

    // Search bar tokens
    val SearchBarBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.searchBarBackground

    val SearchBarPlaceholder: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.searchBarPlaceholder

    // Segment tab tokens
    val TabTrackBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.tabTrackBackground

    val TabSelectedPillBackground: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.tabSelectedPillBackground

    val TabSelectedTextColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.tabSelectedTextColor

    val TabUnselectedTextColor: Color
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current.tabUnselectedTextColor

    // Standard spacing
    val SpaceXs = 4.dp
    val SpaceSm = 8.dp
    val SpaceMd = 16.dp
    val SpaceLg = 24.dp
    val SpaceXl = 40.dp
    val MarginMobile = 18.dp
}
