package com.hostelmanagement.app.core.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = BrandPrimary,
    onPrimary = PureWhite,
    primaryContainer = BrandContainer,
    onPrimaryContainer = BrandOnContainer,
    secondary = BrandPrimaryDark,
    onSecondary = PureWhite,
    secondaryContainer = BrandContainer,
    onSecondaryContainer = BrandOnContainer,
    background = SurfaceNeutral,
    onBackground = TextPrimary,
    surface = PureWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantNeutral,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderStrong,
    error = StatusOverdueText,
    onError = PureWhite,
    errorContainer = StatusOverdueBg,
    onErrorContainer = StatusOverdueText
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandPrimaryLight,
    onPrimary = PureWhite,
    primaryContainer = BrandOnContainer,
    onPrimaryContainer = PureWhite,
    secondary = BrandPrimary,
    onSecondary = PureWhite,
    secondaryContainer = Color(0xFF261D64),
    onSecondaryContainer = PureWhite,
    background = Color(0xFF0F0E17),
    onBackground = PureWhite,
    surface = Color(0xFF181726),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF252338),
    onSurfaceVariant = TextMuted,
    outline = Color(0xFF3B3857),
    outlineVariant = Color(0xFF4F4C70),
    error = Color(0xFFEF4444),
    onError = PureWhite
)

@Composable
fun HostelManagementTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.surface.toArgb()
            window.navigationBarColor = colorScheme.surface.toArgb()

            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = !darkTheme
            windowInsetsController.isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
