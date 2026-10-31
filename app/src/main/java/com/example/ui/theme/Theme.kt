package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val DarkColorScheme = darkColorScheme(
    primary = RoyalGoldLight,
    onPrimary = DeepMaroonDark,
    primaryContainer = DeepMaroonLight,
    onPrimaryContainer = RoyalGoldLight,
    secondary = RoyalGold,
    onSecondary = DeepMaroonDark,
    secondaryContainer = DeepMaroon,
    onSecondaryContainer = RoyalGoldLight,
    tertiary = RoyalGoldLight,
    background = DarkMaroonBackground,
    surface = DarkMaroonSurface,
    surfaceVariant = DarkMaroonSurfaceVariant,
    onBackground = DarkMaroonOnSurface,
    onSurface = DarkMaroonOnSurface,
    outline = RoyalGoldDark
)

private val LightColorScheme = lightColorScheme(
    primary = DeepMaroon,
    onPrimary = Color.White,
    primaryContainer = MaroonContainer,
    onPrimaryContainer = OnMaroonContainer,
    secondary = RoyalGoldDark,
    onSecondary = Color.White,
    secondaryContainer = GoldContainer,
    onSecondaryContainer = OnGoldContainer,
    tertiary = RoyalGold,
    background = IvoryBackground,
    surface = IvorySurface,
    surfaceVariant = IvorySurfaceVariant,
    onBackground = IvoryOnBackground,
    onSurface = IvoryOnSurface,
    outline = IvoryOutline,
    outlineVariant = IvoryOutlineVariant
)

@Composable
fun MyApplicationTheme(
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
