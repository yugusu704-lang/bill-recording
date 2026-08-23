package com.localbill.recording.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.model.CategoryAggregation
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@Composable
fun DonutPieChart(
    aggregations: List<CategoryAggregation>,
    totalAmount: Double,
    modifier: Modifier = Modifier,
    onCategoryClick: ((CategoryAggregation) -> Unit)? = null
) {
    if (aggregations.isEmpty() || totalAmount <= 0.0) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无分类支出数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember { mutableIntStateOf(0) }
    val safeSelectedIndex = selectedIndex.coerceIn(0, (aggregations.size - 1).coerceAtLeast(0))
    val progress = remember { Animatable(0f) }

    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }


    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "分类占比",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "共 ${aggregations.size} 个分类",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 环形图与中心文字
            Box(
                modifier = Modifier.size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .pointerInput(aggregations) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val touchAngle = (Math.toDegrees(
                                    atan2((offset.y - center.y).toDouble(), (offset.x - center.x).toDouble())
                                ) + 360) % 360
                                val adjustedAngle = (touchAngle + 90) % 360

                                var currentSweep = 0f
                                for (i in aggregations.indices) {
                                    val sweep = (aggregations[i].percentage / 100f) * 360f
                                    if (adjustedAngle >= currentSweep && adjustedAngle <= currentSweep + sweep) {
                                        selectedIndex = i
                                        onCategoryClick?.invoke(aggregations[i])
                                        break
                                    }
                                    currentSweep += sweep
                                }
                            }
                        }
                ) {
                    val strokeWidth = 24.dp.toPx()
                    val diameter = size.minDimension - strokeWidth - 16.dp.toPx()
                    val arcSize = Size(diameter, diameter)
                    val topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )

                    var startAngle = -90f

                    aggregations.forEachIndexed { index, item ->
                        val sweepAngle = (item.percentage / 100f) * 360f * progress.value
                        val isSelected = index == safeSelectedIndex
                        val currentStrokeWidth = if (isSelected) strokeWidth + 6.dp.toPx() else strokeWidth
                        val itemColor = Color(item.mainCategory.colorHex)

                        drawArc(
                            color = itemColor,
                            startAngle = startAngle,
                            sweepAngle = (sweepAngle - 2.5f).coerceAtLeast(0.1f),
                            useCenter = false,
                            topLeft = topLeft,
                            size = arcSize,
                            style = Stroke(width = currentStrokeWidth, cap = StrokeCap.Round)
                        )

                        startAngle += sweepAngle
                    }
                }

                // 中心内容展示当前选中的分类信息
                val selectedItem = aggregations.getOrNull(safeSelectedIndex) ?: aggregations.first()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = selectedItem.mainCategory.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", selectedItem.percentage),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(selectedItem.mainCategory.colorHex)
                    )
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", selectedItem.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // 底部图例清单（支持点击联动）
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aggregations.forEachIndexed { index, item ->
                    val isSelected = index == safeSelectedIndex
                    val catColor = Color(item.mainCategory.colorHex)


                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                if (isSelected) MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
                                else Color.Transparent
                            )
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(10.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            CategoryIconBadge(
                                iconName = item.mainCategory.iconName,
                                colorHex = item.mainCategory.colorHex,
                                size = 32.dp,
                                iconSize = 18.dp,
                                cornerRadius = 8.dp
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = item.mainCategory.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${item.count} 笔支出",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                        }

                        Column(horizontalAlignment = Alignment.End) {
                            Text(
                                text = "¥ " + String.format(Locale.US, "%.2f", item.totalAmount),
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            Text(
                                text = String.format(Locale.US, "%.1f%%", item.percentage),
                                style = MaterialTheme.typography.bodySmall,
                                color = catColor,
                                fontWeight = FontWeight.Medium
                            )
                        }
                    }
                }
            }
        }
    }
}
