package com.localbill.recording.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
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

@Composable
fun BillRecordingTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = MinimalBillColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            WindowCompat.getInsetsController(window, view).apply {
                isAppearanceLightStatusBars = true
                isAppearanceLightNavigationBars = true
            }
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
