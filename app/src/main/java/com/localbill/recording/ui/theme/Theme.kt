package com.localbill.recording.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val HobonichiTechoColorScheme = lightColorScheme(
    primary = PillarCulture,          // 抹茶绿
    onPrimary = TomoePaperBg,
    primaryContainer = PillarCultureLight,
    onPrimaryContainer = PillarCulture,
    secondary = HankoRed,             // 朱红印章
    onSecondary = TomoePaperBg,
    secondaryContainer = HankoSealBg,
    onSecondaryContainer = HankoRed,
    background = TomoePaperBg,
    onBackground = SumiDark,
    surface = TomoePaperPage,
    onSurface = SumiDark,
    surfaceVariant = TomoePaperBg,
    onSurfaceVariant = SumiMedium,
    outline = TomoeBorder
)

@Composable
fun BillRecordingTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = HobonichiTechoColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = TomoePaperBg.toArgb()
            window.navigationBarColor = TomoePaperBg.toArgb()
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
