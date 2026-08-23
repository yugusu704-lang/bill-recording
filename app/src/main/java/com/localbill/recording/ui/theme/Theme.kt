package com.localbill.recording.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val ClaudeGlassColorScheme = lightColorScheme(
    primary = ClaudeInk,
    onPrimary = ClaudeWarmBg,
    primaryContainer = ClaudeWarmBgSubtle,
    onPrimaryContainer = ClaudeInk,
    secondary = ClaudeTerracotta,
    onSecondary = ClaudeWarmBg,
    secondaryContainer = ClaudeWarmBgSubtle,
    onSecondaryContainer = ClaudeTerracotta,
    background = ClaudeWarmBg,
    onBackground = ClaudeInk,
    surface = GlassCardBackground,
    onSurface = ClaudeInk,
    surfaceVariant = ClaudeWarmBgSubtle,
    onSurfaceVariant = ClaudeTextSecondary,
    outline = ClaudeBorder
)

@Composable
fun BillRecordingTheme(
    darkTheme: Boolean = false, // 默认锁定纯净 Claude 暖奶油玻璃风，防止系统深色模式强制变纯黑
    content: @Composable () -> Unit
) {
    val colorScheme = ClaudeGlassColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = ClaudeWarmBg.toArgb()
            window.navigationBarColor = ClaudeWarmBg.toArgb()
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
