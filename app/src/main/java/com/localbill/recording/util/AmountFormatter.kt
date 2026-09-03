package com.localbill.recording.util

import java.util.Locale

fun formatAmount(amount: Double): String {
    return String.format(Locale.US, "%,.2f", amount)
}

fun formatPercent(percentage: Float): String {
    return String.format(Locale.US, "%.1f%%", percentage)
}
