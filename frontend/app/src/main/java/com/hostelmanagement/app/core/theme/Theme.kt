package com.hostelmanagement.app.core.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = PrimaryPurple,
    onPrimary = WhiteSurface,
    primaryContainer = LightLavender,
    onPrimaryContainer = DeepPurple,
    secondary = LavenderAccent,
    onSecondary = WhiteSurface,
    secondaryContainer = LightLavender,
    onSecondaryContainer = DeepPurple,
    tertiary = BrandLogoOrange,
    onTertiary = WhiteSurface,
    background = PageBackground,
    onBackground = TextPrimary,
    surface = WhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantNeutral,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderStrong,
    error = StatusOverdueText,
    onError = WhiteSurface,
    errorContainer = StatusOverdueBg,
    onErrorContainer = StatusOverdueText
)

private val DarkColorScheme = darkColorScheme(
    primary = LavenderAccent,
    onPrimary = DeepPurple,
    primaryContainer = DeepPurple,
    onPrimaryContainer = LightLavender,
    secondary = LavenderAccent,
    onSecondary = DeepPurple,
    background = PageBackground,
    onBackground = TextPrimary,
    surface = WhiteSurface,
    onSurface = TextPrimary,
    surfaceVariant = SurfaceVariantNeutral,
    onSurfaceVariant = TextSecondary,
    outline = BorderSubtle,
    outlineVariant = BorderStrong,
    error = StatusOverdueText,
    onError = WhiteSurface
)

@Composable
fun HostelManagementTheme(
    darkTheme: Boolean = false, // Enforce light lavender & white theme as standard
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = WhiteSurface.toArgb()

            val windowInsetsController = WindowCompat.getInsetsController(window, view)
            windowInsetsController.isAppearanceLightStatusBars = true
            windowInsetsController.isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
