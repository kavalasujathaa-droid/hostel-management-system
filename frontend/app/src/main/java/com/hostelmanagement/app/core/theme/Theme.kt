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
    primary = Purple600,
    onPrimary = PureWhite,
    primaryContainer = Purple50,
    onPrimaryContainer = Purple900,
    secondary = Purple500,
    onSecondary = PureWhite,
    secondaryContainer = Purple100,
    onSecondaryContainer = Purple950,
    background = PurpleAppBg,
    onBackground = Purple950,
    surface = PureWhite,
    onSurface = Purple950,
    surfaceVariant = Purple50,
    onSurfaceVariant = Slate600,
    outline = BorderSubtle,
    outlineVariant = Purple100,
    error = StatusOverdueText,
    onError = PureWhite
)

private val DarkColorScheme = darkColorScheme(
    primary = Purple400,
    onPrimary = Purple950,
    primaryContainer = Purple900,
    onPrimaryContainer = PureWhite,
    secondary = Purple300,
    onSecondary = Purple950,
    secondaryContainer = Purple800,
    onSecondaryContainer = PureWhite,
    background = Color(0xFF0F0B1E),
    onBackground = PureWhite,
    surface = Color(0xFF171329),
    onSurface = PureWhite,
    surfaceVariant = Color(0xFF241D3B),
    onSurfaceVariant = Purple200,
    outline = Purple800,
    outlineVariant = Color(0xFF3B2F5C),
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
        typography = Typography,
        content = content
    )
}
