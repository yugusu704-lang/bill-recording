package com.localbill.recording.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import com.localbill.recording.ui.theme.PrismBlack
import com.localbill.recording.ui.theme.PrismBorder
import com.localbill.recording.ui.theme.PrismSlate
import com.localbill.recording.ui.theme.PrismTextSecondary
import com.localbill.recording.ui.theme.PrismTextTertiary
import com.localbill.recording.ui.theme.PrismWhite
import java.util.Locale
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin
import kotlin.math.sqrt

@OptIn(ExperimentalLayoutApi::class)
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
                color = PrismTextSecondary
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
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PrismBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = PrismWhite
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
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
                    color = PrismBlack
                )
                Text(
                    text = "共 ${aggregations.size} 个分类",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrismTextSecondary
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 环形图与中心数据
            Box(
                modifier = Modifier
                    .size(200.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(200.dp)
                        .pointerInput(aggregations) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val outerRadius = size.width / 2f
                                val innerRadius = outerRadius - 26.dp.toPx()

                                if (dist in innerRadius..outerRadius) {
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angle < 0) angle += 360f
                                    var angleFromTop = (angle + 90f) % 360f

                                    var currentAngle = 0f
                                    for (i in aggregations.indices) {
                                        val sweep = (aggregations[i].percentage / 100f * 360f).toFloat()
                                        if (angleFromTop in currentAngle..(currentAngle + sweep)) {
                                            selectedIndex = i
                                            onCategoryClick?.invoke(aggregations[i])
                                            break
                                        }
                                        currentAngle += sweep
                                    }
                                }
                            }
                        }
                ) {
                    val strokeWidthPx = 22.dp.toPx()
                    val diameter = size.minDimension - strokeWidthPx - 6.dp.toPx()
                    val topLeft = Offset(
                        (size.width - diameter) / 2f,
                        (size.height - diameter) / 2f
                    )
                    val arcSize = Size(diameter, diameter)

                    // 绘制浅灰背景环
                    drawArc(
                        color = PrismSlate,
                        startAngle = 0f,
                        sweepAngle = 360f,
                        useCenter = false,
                        topLeft = topLeft,
                        size = arcSize,
                        style = Stroke(width = strokeWidthPx, cap = StrokeCap.Round)
                    )

                    var currentStartAngle = -90f
                    aggregations.forEachIndexed { index, item ->
                        val sweepAngle = (item.percentage / 100f * 360f).toFloat() * progress.value
                        val isSelected = index == safeSelectedIndex
                        val catColor = Color(item.mainCategory.colorHex)

                        val arcStroke = if (isSelected) strokeWidthPx + 4.dp.toPx() else strokeWidthPx

                        if (sweepAngle > 0f) {
                            drawArc(
                                color = catColor,
                                startAngle = currentStartAngle + 1.5f,
                                sweepAngle = (sweepAngle - 3f).coerceAtLeast(0.5f),
                                useCenter = false,
                                topLeft = topLeft,
                                size = arcSize,
                                style = Stroke(width = arcStroke, cap = StrokeCap.Round)
                            )
                        }
                        currentStartAngle += sweepAngle
                    }
                }

                // 中心统计
                val selectedItem = aggregations.getOrNull(safeSelectedIndex) ?: aggregations.first()
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Text(
                        text = String.format(Locale.US, "%.1f%%", selectedItem.percentage),
                        style = MaterialTheme.typography.headlineMedium.copy(
                            fontSize = 24.sp,
                            fontWeight = FontWeight.ExtraBold
                        ),
                        color = Color(selectedItem.mainCategory.colorHex)
                    )
                    Text(
                        text = selectedItem.mainCategory.name,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = PrismBlack
                    )
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", selectedItem.totalAmount),
                        style = MaterialTheme.typography.labelSmall,
                        color = PrismTextSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 底部图例胶囊列表
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                aggregations.forEachIndexed { index, item ->
                    val isSelected = index == safeSelectedIndex
                    val color = Color(item.mainCategory.colorHex)

                    Row(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) PrismSlate else Color.Transparent)
                            .border(
                                width = 1.dp,
                                color = if (isSelected) color.copy(alpha = 0.5f) else PrismBorder,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(7.dp)
                                .clip(CircleShape)
                                .background(color)
                        )
                        Spacer(modifier = Modifier.width(5.dp))
                        Text(
                            text = item.mainCategory.name,
                            style = MaterialTheme.typography.labelSmall,
                            color = PrismBlack,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = String.format(Locale.US, "%.0f%%", item.percentage),
                            style = MaterialTheme.typography.labelSmall,
                            color = PrismTextSecondary
                        )
                    }
                }
            }
        }
    }
}
