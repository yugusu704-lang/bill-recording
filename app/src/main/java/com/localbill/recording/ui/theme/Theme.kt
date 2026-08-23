package com.localbill.recording.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = InkPrimary,
    onPrimary = PaperSurface,
    primaryContainer = PaperSurfaceSubtle,
    onPrimaryContainer = InkPrimary,
    secondary = AccentSageGreen,
    onSecondary = PaperSurface,
    secondaryContainer = PaperSurfaceSubtle,
    onSecondaryContainer = AccentSageGreen,
    background = PaperLight,
    onBackground = InkPrimary,
    surface = PaperSurface,
    onSurface = InkPrimary,
    surfaceVariant = PaperSurfaceSubtle,
    onSurfaceVariant = InkSecondary,
    outline = PaperBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = InkDarkPrimary,
    onPrimary = PaperDark,
    primaryContainer = SurfaceDarkSubtle,
    onPrimaryContainer = InkDarkPrimary,
    secondary = AccentSageGreen,
    onSecondary = PaperDark,
    secondaryContainer = SurfaceDarkSubtle,
    onSecondaryContainer = InkDarkPrimary,
    background = PaperDark,
    onBackground = InkDarkPrimary,
    surface = SurfaceDark,
    onSurface = InkDarkPrimary,
    surfaceVariant = SurfaceDarkSubtle,
    onSurfaceVariant = InkDarkSecondary,
    outline = BorderDark
)

@Composable
fun BillRecordingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = !darkTheme
                isAppearanceLightNavigationBars = !darkTheme
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
