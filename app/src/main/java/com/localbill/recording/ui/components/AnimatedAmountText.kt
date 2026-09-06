package com.localbill.recording.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import java.util.Locale

@Composable
fun AnimatedAmountText(
    amount: Double,
    modifier: Modifier = Modifier,
    style: TextStyle = MaterialTheme.typography.displayMedium,
    color: Color = MaterialTheme.colorScheme.onSurface,
    showPrefix: Boolean = true,
    prefix: String = "¥"
) {
    val animatedAmount by animateFloatAsState(
        targetValue = amount.toFloat(),
        animationSpec = tween(durationMillis = 450, easing = FastOutSlowInEasing),
        label = "AmountCountUp"
    )

    val formattedStr = String.format(Locale.US, "%.2f", animatedAmount)

    Row(
        modifier = modifier,
        verticalAlignment = Alignment.Bottom
    ) {
        if (showPrefix) {
            Text(
                text = "$prefix ",
                style = style.copy(fontSize = style.fontSize * 0.7f, fontWeight = FontWeight.Bold),
                color = color
            )
        }
        Text(
            text = formattedStr,
            style = style,
            color = color
        )
    }
}
