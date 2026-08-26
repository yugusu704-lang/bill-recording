package com.localbill.recording.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val JapaneseWashiColorScheme = lightColorScheme(
    primary = MatchaPrimary,
    onPrimary = WashiPaperBg,
    primaryContainer = MatchaContainer,
    onPrimaryContainer = MatchaPrimary,
    secondary = SakuraAccent,
    onSecondary = WashiPaperBg,
    secondaryContainer = SakuraSoft,
    onSecondaryContainer = SakuraAccent,
    background = WashiPaperBg,
    onBackground = SumiInk,
    surface = WashiCardBg,
    onSurface = SumiInk,
    surfaceVariant = WashiPaperSubtle,
    onSurfaceVariant = SumiSecondary,
    outline = WashiBorder
)

@Composable
fun BillRecordingTheme(
    darkTheme: Boolean = false, // 锁定日式和风小清新纸质质感
    content: @Composable () -> Unit
) {
    val colorScheme = JapaneseWashiColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = WashiPaperBg.toArgb()
            window.navigationBarColor = WashiPaperBg.toArgb()
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
