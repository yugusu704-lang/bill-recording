package com.localbill.recording.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.model.TrendPoint
import com.localbill.recording.ui.theme.PrimaryGreen
import com.localbill.recording.ui.theme.PrimaryGreenLight
import java.util.Locale

@Composable
fun BezierTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = PrimaryGreen,
    gradientStartColor: Color = PrimaryGreenLight.copy(alpha = 0.45f),
    gradientEndColor: Color = PrimaryGreenLight.copy(alpha = 0.0f)
) {
    if (points.isEmpty()) {
        Box(
            modifier = modifier
                .fillMaxWidth()
                .height(180.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = "暂无消费走势数据",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        return
    }

    var selectedIndex by remember {
        val currentIdx = points.indexOfFirst { it.isCurrent }
        mutableIntStateOf(if (currentIdx >= 0) currentIdx else (points.size - 1).coerceAtLeast(0))
    }
    // 当 points 长度变化时，确保下标不越界
    val safeSelectedIndex = selectedIndex.coerceIn(0, (points.size - 1).coerceAtLeast(0))

    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(
            targetValue = 1f,
            animationSpec = tween(durationMillis = 600, easing = FastOutSlowInEasing)
        )
    }


    val maxAmount = remember(points) {
        (points.maxOfOrNull { it.amount } ?: 1.0).coerceAtLeast(10.0)
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
                .padding(16.dp)
        ) {
            // 顶部交互提示浮层
            val currentPoint = points.getOrNull(safeSelectedIndex) ?: points.last()

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "支出走势",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = "${currentPoint.label} ${if (currentPoint.subLabel.isNotEmpty()) "(${currentPoint.subLabel})" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.6f))
                        .padding(horizontal = 12.dp, vertical = 6.dp)
                ) {
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", currentPoint.amount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onPrimaryContainer
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas 绘制平滑贝塞尔曲线与渐变区域
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(160.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(160.dp)
                        .pointerInput(points) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (points.size - 1).coerceAtLeast(1)
                                val tappedIndex = (offset.x / spacing + 0.5f).toInt()
                                    .coerceIn(0, points.size - 1)
                                selectedIndex = tappedIndex
                            }
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val paddingBottom = 24.dp.toPx()
                    val chartHeight = height - paddingBottom
                    val stepX = if (points.size > 1) width / (points.size - 1) else width

                    // 绘制水平辅助虚线
                    val lineCount = 3
                    for (i in 0..lineCount) {
                        val y = chartHeight * (i.toFloat() / lineCount)
                        drawLine(
                            color = Color.LightGray.copy(alpha = 0.35f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 1.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(10f, 10f), 0f)
                        )
                    }

                    if (points.size == 1) {
                        // 单个数据点绘制
                        val pt = points[0]
                        val ratio = (pt.amount / maxAmount).toFloat().coerceIn(0f, 1f) * progress.value
                        val cy = chartHeight - (ratio * chartHeight * 0.8f)
                        drawCircle(color = lineColor, radius = 6.dp.toPx(), center = Offset(width / 2, cy))
                        return@Canvas
                    }

                    // 构造数据坐标点
                    val coordinates = points.mapIndexed { index, point ->
                        val x = index * stepX
                        val ratio = (point.amount / maxAmount).toFloat().coerceIn(0f, 1f) * progress.value
                        val y = chartHeight - (ratio * (chartHeight - 16.dp.toPx())) - 8.dp.toPx()
                        Offset(x, y)
                    }

                    // 绘制平滑贝塞尔曲线
                    val strokePath = Path()
                    val fillPath = Path()

                    strokePath.moveTo(coordinates[0].x, coordinates[0].y)
                    fillPath.moveTo(coordinates[0].x, chartHeight)
                    fillPath.lineTo(coordinates[0].x, coordinates[0].y)

                    for (i in 0 until coordinates.size - 1) {
                        val p0 = coordinates[i]
                        val p1 = coordinates[i + 1]

                        val controlPoint1 = Offset(p0.x + (p1.x - p0.x) / 2f, p0.y)
                        val controlPoint2 = Offset(p0.x + (p1.x - p0.x) / 2f, p1.y)

                        strokePath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p1.x, p1.y
                        )
                        fillPath.cubicTo(
                            controlPoint1.x, controlPoint1.y,
                            controlPoint2.x, controlPoint2.y,
                            p1.x, p1.y
                        )
                    }

                    fillPath.lineTo(coordinates.last().x, chartHeight)
                    fillPath.close()

                    // 绘制填充渐变
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientStartColor, gradientEndColor),
                            startY = 0f,
                            endY = chartHeight
                        )
                    )

                    // 绘制曲线本身
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(
                            width = 3.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // 绘制选中点的指示器
                    if (safeSelectedIndex in coordinates.indices) {
                        val selCoord = coordinates[safeSelectedIndex]

                        // 垂直指示线
                        drawLine(
                            color = lineColor.copy(alpha = 0.5f),
                            start = Offset(selCoord.x, 0f),
                            end = Offset(selCoord.x, chartHeight),
                            strokeWidth = 1.5.dp.toPx(),
                            pathEffect = PathEffect.dashPathEffect(floatArrayOf(8f, 8f), 0f)
                        )

                        // 外发光环
                        drawCircle(
                            color = lineColor.copy(alpha = 0.25f),
                            radius = 10.dp.toPx(),
                            center = selCoord
                        )
                        // 内圆点
                        drawCircle(
                            color = Color.White,
                            radius = 5.dp.toPx(),
                            center = selCoord
                        )
                        drawCircle(
                            color = lineColor,
                            radius = 3.5.dp.toPx(),
                            center = selCoord
                        )
                    }
                }
            }

            // X 轴时间标签
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                val step = if (points.size > 7) (points.size / 5).coerceAtLeast(1) else 1
                points.forEachIndexed { index, point ->
                    if (index % step == 0 || index == points.size - 1) {
                        Text(
                            text = point.label,
                            style = TextStyle(
                                fontSize = 11.sp,
                                fontWeight = if (index == safeSelectedIndex) FontWeight.Bold else FontWeight.Normal,
                                color = if (index == safeSelectedIndex) lineColor else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        )
                    }
                }
            }

        }
    }
}
