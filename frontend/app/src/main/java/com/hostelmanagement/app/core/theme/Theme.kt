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
    primary = Slate900,
    onPrimary = PureWhite,
    primaryContainer = Slate100,
    onPrimaryContainer = Slate900,
    secondary = Slate700,
    onSecondary = PureWhite,
    secondaryContainer = Slate100,
    onSecondaryContainer = Slate900,
    background = Slate50,
    onBackground = Slate900,
    surface = PureWhite,
    onSurface = Slate900,
    surfaceVariant = Slate100,
    onSurfaceVariant = Slate600,
    outline = Slate300,
    outlineVariant = Slate200,
    error = StatusOverdueText,
    onError = PureWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = PureWhite,
    onPrimary = Slate950,
    primaryContainer = Slate800,
    onPrimaryContainer = PureWhite,
    secondary = Slate400,
    onSecondary = Slate950,
    secondaryContainer = Slate800,
    onSecondaryContainer = PureWhite,
    background = Color(0xFF0B0F19),
    onBackground = PureWhite,
    surface = Color(0xFF111827),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF1F2937),
    onSurfaceVariant = Slate400,
    outline = Slate700,
    outlineVariant = Color(0xFF374151),
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
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = AppTypography,
        shapes = AppShapes,
        content = content
    )
}
