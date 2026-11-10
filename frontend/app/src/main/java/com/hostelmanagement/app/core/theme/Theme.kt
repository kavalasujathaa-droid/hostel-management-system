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
    primary = BrandTeal,
    onPrimary = SurfaceWhite,
    primaryContainer = SoftTeal,
    onPrimaryContainer = BrandTealDark,
    secondary = BrandOrange,
    onSecondary = SurfaceWhite,
    secondaryContainer = SoftOrange,
    onSecondaryContainer = BrandOrangeDark,
    tertiary = BrandOrange,
    onTertiary = SurfaceWhite,
    tertiaryContainer = SoftOrange,
    onTertiaryContainer = BrandOrangeDark,
    background = CanvasBackground,
    onBackground = TextPrimary,
    surface = SurfaceWhite,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantNeutral,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderStrong,
    error = StatusOverdueText,
    onError = SurfaceWhite,
    errorContainer = StatusOverdueBg,
    onErrorContainer = StatusOverdueText
)

private val DarkColorScheme = darkColorScheme(
    primary = BrandTealLight,
    onPrimary = BrandCharcoal,
    primaryContainer = BrandTealDark,
    onPrimaryContainer = SurfaceWhite,
    secondary = BrandOrangeLight,
    onSecondary = BrandCharcoal,
    secondaryContainer = BrandOrangeDark,
    onSecondaryContainer = SurfaceWhite,
    tertiary = BrandOrangeLight,
    onTertiary = BrandCharcoal,
    tertiaryContainer = SoftOrange,
    onTertiaryContainer = BrandOrangeDark,
    background = Color(0xFF141618),
    onBackground = SurfaceWhite,
    surface = Color(0xFF1C1E22),
    onSurface = SurfaceWhite,
    surfaceVariant = Color(0xFF26292E),
    onSurfaceVariant = Color(0xFFA6ABB0),
    outline = Color(0xFF373B42),
    outlineVariant = Color(0xFF4C525C),
    error = Color(0xFFEF4444),
    onError = SurfaceWhite
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
