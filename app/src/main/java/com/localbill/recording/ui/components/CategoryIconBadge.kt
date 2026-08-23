package com.localbill.recording.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.Checkroom
import androidx.compose.material.icons.filled.DeliveryDining
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.Eco
import androidx.compose.material.icons.filled.Flatware
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocalAtm
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Restaurant
import androidx.compose.material.icons.filled.School
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material.icons.filled.SoupKitchen
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Work
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.localbill.recording.ui.theme.GlassCardBackground
import com.localbill.recording.ui.theme.GlassCardBorder

object CategoryIcons {
    val AVAILABLE_ICONS = listOf(
        "school" to Icons.Default.School,
        "restaurant" to Icons.Default.Restaurant,
        "soup_kitchen" to Icons.Default.SoupKitchen,
        "delivery_dining" to Icons.Default.DeliveryDining,
        "flatware" to Icons.Default.Flatware,
        "eco" to Icons.Default.Eco,
        "directions_car" to Icons.Default.DirectionsCar,
        "checkroom" to Icons.Default.Checkroom,
        "shopping_cart" to Icons.Default.ShoppingCart,
        "sports_esports" to Icons.Default.SportsEsports,
        "movie" to Icons.Default.Movie,
        "local_hospital" to Icons.Default.LocalHospital,
        "home" to Icons.Default.Home,
        "work" to Icons.Default.Work,
        "local_atm" to Icons.Default.LocalAtm,
        "phone_android" to Icons.Default.PhoneAndroid
    )

    fun getIcon(name: String): ImageVector {
        return AVAILABLE_ICONS.find { it.first == name }?.second ?: Icons.Default.Category
    }
}

@Composable
fun CategoryIconBadge(
    iconName: String,
    colorHex: Long,
    modifier: Modifier = Modifier,
    size: Dp = 38.dp,
    iconSize: Dp = 18.dp,
    cornerRadius: Dp = 10.dp
) {
    val baseColor = Color(colorHex)

    Box(
        modifier = modifier
            .size(size)
            .clip(RoundedCornerShape(cornerRadius))
            .background(Color.White.copy(alpha = 0.85f))
            .border(
                width = 1.dp,
                color = Color.White,
                shape = RoundedCornerShape(cornerRadius)
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = CategoryIcons.getIcon(iconName),
            contentDescription = null,
            tint = baseColor,
            modifier = Modifier.size(iconSize)
        )
    }
}
