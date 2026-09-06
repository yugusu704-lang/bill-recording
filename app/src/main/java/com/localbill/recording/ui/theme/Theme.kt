package com.localbill.recording.ui.theme

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

private val MinimalBillColorScheme = lightColorScheme(
    primary = DeepGreen,
    onPrimary = WarmSurface,
    primaryContainer = DeepGreenLight,
    onPrimaryContainer = DeepGreen,
    secondary = TextDark,
    onSecondary = WarmSurface,
    secondaryContainer = DeepGreenSoft,
    onSecondaryContainer = TextDark,
    background = WarmBone,
    onBackground = TextDark,
    surface = WarmSurface,
    onSurface = TextDark,
    surfaceVariant = WarmBone,
    onSurfaceVariant = TextSecondary,
    outline = WarmBorder,
    error = DangerRed,
    onError = WarmSurface,
    errorContainer = DangerRedLight,
    onErrorContainer = DangerRed
)

private val DarkBillColorScheme = darkColorScheme(
    primary = DeepGreen,
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF27272A),
    onPrimaryContainer = Color(0xFFE4E4E7),
    secondary = Color(0xFFE4E4E7),
    onSecondary = Color(0xFF18181B),
    secondaryContainer = Color(0xFF27272A),
    onSecondaryContainer = Color(0xFFE4E4E7),
    background = Color(0xFF18181B),
    onBackground = Color(0xFFF4F4F5),
    surface = Color(0xFF27272A),
    onSurface = Color(0xFFF4F4F5),
    surfaceVariant = Color(0xFF18181B),
    onSurfaceVariant = Color(0xFFA1A1AA),
    outline = Color(0xFF3F3F46),
    error = DangerRed,
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFF3F1D1D),
    onErrorContainer = Color(0xFFFCA5A5)
)

@Composable
fun BillRecordingTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkBillColorScheme else MinimalBillColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            val barColor = (if (darkTheme) Color(0xFF18181B) else WarmBone).toArgb()
            window.statusBarColor = barColor
            window.navigationBarColor = barColor
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
