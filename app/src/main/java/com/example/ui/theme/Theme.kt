package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val LightColorScheme = lightColorScheme(
    primary = KhoubzaForestGreen,
    onPrimary = Color.White,
    primaryContainer = KhoubzaGreenContainer,
    onPrimaryContainer = KhoubzaOnGreenContainer,
    secondary = KhoubzaWheatGold,
    onSecondary = Color.Black,
    secondaryContainer = KhoubzaGoldContainer,
    onSecondaryContainer = KhoubzaTextDarkBrown,
    tertiary = KhoubzaGoldDark,
    onTertiary = Color.White,
    background = KhoubzaWarmCream,
    onBackground = KhoubzaTextDarkBrown,
    surface = KhoubzaCardSurface,
    onSurface = KhoubzaTextDarkBrown,
    surfaceVariant = Color(0xFFF0EAE1),
    onSurfaceVariant = KhoubzaTextMutedBrown,
    outline = Color(0xFFD4C9BC),
    outlineVariant = Color(0xFFE5DDD2),
    error = KhoubzaDangerRed,
    onError = Color.White,
    errorContainer = KhoubzaDangerContainer,
    onErrorContainer = KhoubzaDangerRed
)

private val DarkColorScheme = darkColorScheme(
    primary = KhoubzaGoldLight,
    onPrimary = KhoubzaDarkBg,
    primaryContainer = KhoubzaDarkGreen,
    onPrimaryContainer = KhoubzaGoldLight,
    secondary = KhoubzaWheatGold,
    onSecondary = Color.Black,
    secondaryContainer = Color(0xFF3B3222),
    onSecondaryContainer = KhoubzaGoldLight,
    tertiary = KhoubzaLightGreen,
    onTertiary = Color.White,
    background = KhoubzaDarkBg,
    onBackground = KhoubzaTextLight,
    surface = KhoubzaDarkSurface,
    onSurface = KhoubzaTextLight,
    surfaceVariant = KhoubzaDarkSurfaceVariant,
    onSurfaceVariant = KhoubzaTextMutedLight,
    outline = Color(0xFF4A564F),
    outlineVariant = Color(0xFF333E38),
    error = Color(0xFFEF9A9A),
    onError = Color.Black,
    errorContainer = Color(0xFF4E1616),
    onErrorContainer = Color(0xFFFFCDD2)
)

@Composable
fun KhoubzaTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme

    MaterialTheme(
        colorScheme = colorScheme,
        typography = KhoubzaTypography,
        content = content
    )
}
