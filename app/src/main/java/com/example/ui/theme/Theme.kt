package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

val DarkColorScheme = darkColorScheme(
    primary = DarkPrimary,
    onPrimary = Color(0xFF00391A),
    primaryContainer = DarkElevated,
    onPrimaryContainer = DarkText,
    secondary = DarkAccentGold,
    onSecondary = Color(0xFF3F2E00),
    background = DarkBackground,
    onBackground = DarkText,
    surface = DarkSurface,
    onSurface = DarkText,
    surfaceVariant = DarkElevated,
    onSurfaceVariant = DarkTextSecondary,
    outline = Color(0xFF2C3E55)
)

val LightColorScheme = lightColorScheme(
    primary = LightPrimary,
    onPrimary = Color.White,
    primaryContainer = Color(0xFFE2F5E9),
    onPrimaryContainer = LightPrimaryDark,
    secondary = LightAccentGold,
    onSecondary = Color(0xFF2B1D00),
    background = LightBackground,
    onBackground = LightText,
    surface = LightSurface,
    onSurface = LightText,
    surfaceVariant = Color(0xFFEAEFEB),
    onSurfaceVariant = LightTextSecondary,
    outline = Color(0xFFD0D7D2)
)

@Composable
fun MahiCricProTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
