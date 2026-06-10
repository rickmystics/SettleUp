package com.example.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color

private val SettleUpLightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = Color.White,
    primaryContainer = BrandNetBalanceCardBg,
    onPrimaryContainer = BrandTertiary,
    secondary = BrandSecondary,
    onSecondary = Color.White,
    tertiary = BrandTertiary,
    onTertiary = Color.White,
    background = BrandDarkBackground,
    onBackground = TextPrimary,
    surface = BrandCardSurface,
    onSurface = TextPrimary,
    surfaceVariant = BrandCardSurfaceElevated,
    onSurfaceVariant = TextSecondary,
    error = SettleRed,
    onError = Color.White
)

private val SettleUpDarkColorScheme = darkColorScheme(
    primary = Color(0xFFD0BCFF),        // Soft Lavender for dark mode readability
    onPrimary = Color(0xFF381E72),
    primaryContainer = Color(0xFF4F378B),
    onPrimaryContainer = Color(0xFFEADDFF),
    secondary = Color(0xFFCCC2DC),
    onSecondary = Color(0xFF332D41),
    tertiary = Color(0xFFEFB8C8),
    onTertiary = Color(0xFF492532),
    background = Color(0xFF141218),     // Standard M3 Dark Charcoal Background
    onBackground = Color(0xFFE6E1E9),
    surface = Color(0xFF25232A),        // Dark Purple-charcoal card surface
    onSurface = Color(0xFFE6E1E9),
    surfaceVariant = Color(0xFF49454F),
    onSurfaceVariant = Color(0xFFCAC4D0),
    error = SettleRed,
    onError = Color.White
)

@Composable
fun MyApplicationTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    dynamicColor: Boolean = false, // Force our custom high-fidelity brand colors!
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) {
        SettleUpDarkColorScheme
    } else {
        SettleUpLightColorScheme
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
