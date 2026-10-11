package com.example.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.staticCompositionLocalOf

val LocalAppColors = staticCompositionLocalOf {
    AppThemePreset.DARK.colors
}

object AppTheme {
    val colors: AppThemeColors
        @Composable
        @ReadOnlyComposable
        get() = LocalAppColors.current
}

@Composable
fun MyApplicationTheme(
    themePreset: AppThemePreset = AppThemePreset.DARK,
    content: @Composable () -> Unit
) {
    val colors = themePreset.colors
    val colorScheme = if (colors.isDark) {
        darkColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.surfaceContainer,
            onPrimaryContainer = colors.onSurface,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            background = colors.background,
            surface = colors.surface,
            surfaceContainerLowest = colors.surfaceContainerLowest,
            surfaceContainerLow = colors.surfaceContainerLow,
            surfaceContainer = colors.surfaceContainer,
            surfaceContainerHigh = colors.surfaceContainerHigh,
            surfaceContainerHighest = colors.surfaceContainerHighest,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant
        )
    } else {
        lightColorScheme(
            primary = colors.primary,
            onPrimary = colors.onPrimary,
            primaryContainer = colors.surfaceContainer,
            onPrimaryContainer = colors.onSurface,
            secondary = colors.secondary,
            onSecondary = colors.onSecondary,
            background = colors.background,
            surface = colors.surface,
            surfaceContainerLowest = colors.surfaceContainerLowest,
            surfaceContainerLow = colors.surfaceContainerLow,
            surfaceContainer = colors.surfaceContainer,
            surfaceContainerHigh = colors.surfaceContainerHigh,
            surfaceContainerHighest = colors.surfaceContainerHighest,
            onBackground = colors.onBackground,
            onSurface = colors.onSurface,
            onSurfaceVariant = colors.onSurfaceVariant,
            outline = colors.outline,
            outlineVariant = colors.outlineVariant
        )
    }

    val typography = if (colors.useSerifFont) SerifTypography else DefaultTypography

    CompositionLocalProvider(LocalAppColors provides colors) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = typography,
            content = content
        )
    }
}
