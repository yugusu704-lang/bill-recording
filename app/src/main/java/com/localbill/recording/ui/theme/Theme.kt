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
    primary = PrismBlack,
    onPrimary = PrismWhite,
    primaryContainer = PrismSlate,
    onPrimaryContainer = PrismBlack,
    secondary = PrismLavender,
    onSecondary = PrismWhite,
    secondaryContainer = PrismSlate,
    onSecondaryContainer = PrismBlack,
    background = PrismSnow,
    onBackground = PrismBlack,
    surface = PrismWhite,
    onSurface = PrismBlack,
    surfaceVariant = PrismSlate,
    onSurfaceVariant = PrismTextSecondary,
    outline = PrismBorder
)

private val DarkColorScheme = darkColorScheme(
    primary = PrismDarkTextPrimary,
    onPrimary = PrismDarkBackground,
    primaryContainer = PrismDarkSlate,
    onPrimaryContainer = PrismDarkTextPrimary,
    secondary = PrismLavender,
    onSecondary = PrismDarkBackground,
    secondaryContainer = PrismDarkSlate,
    onSecondaryContainer = PrismDarkTextPrimary,
    background = PrismDarkBackground,
    onBackground = PrismDarkTextPrimary,
    surface = PrismDarkSurface,
    onSurface = PrismDarkTextPrimary,
    surfaceVariant = PrismDarkSlate,
    onSurfaceVariant = PrismDarkTextSecondary,
    outline = PrismDarkBorder
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
