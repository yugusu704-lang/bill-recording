package com.localbill.recording.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.model.TrendPoint
import com.localbill.recording.ui.theme.ClaudeBorder
import com.localbill.recording.ui.theme.ClaudeInk
import com.localbill.recording.ui.theme.ClaudeTerracotta
import com.localbill.recording.ui.theme.ClaudeTextSecondary
import com.localbill.recording.ui.theme.ClaudeTextTertiary
import com.localbill.recording.ui.theme.PrismWarmSpectralBrush
import java.util.Locale

@Composable
fun BezierTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = ClaudeTerracotta,
    gradientStartColor: Color = ClaudeTerracotta.copy(alpha = 0.15f),
    gradientEndColor: Color = Color.Transparent
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
                color = ClaudeTextSecondary
            )
        }
        return
    }

    var selectedIndex by remember {
        val currentIdx = points.indexOfFirst { it.isCurrent }
        mutableIntStateOf(if (currentIdx >= 0) currentIdx else (points.size - 1).coerceAtLeast(0))
    }
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

    // 智能 X 轴关键里程碑抽样索引 (当数据点很多时，抽样显示 5 个代表性节点)
    val landmarkIndices = remember(points.size) {
        if (points.size <= 7) {
            points.indices.toList()
        } else {
            val last = points.size - 1
            listOf(
                0,
                (last * 0.25f).toInt(),
                (last * 0.50f).toInt(),
                (last * 0.75f).toInt(),
                last
            ).distinct()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White.copy(alpha = 0.78f))
            .border(1.2.dp, Color.White.copy(alpha = 0.92f), RoundedCornerShape(18.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            val currentPoint = points.getOrNull(safeSelectedIndex) ?: points.last()

            // 顶部实时触控数据卡片
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
                        color = ClaudeInk
                    )
                    Text(
                        text = "${currentPoint.label} ${if (currentPoint.subLabel.isNotEmpty()) "(${currentPoint.subLabel})" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = ClaudeTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.85f))
                        .border(1.dp, ClaudeBorder.copy(alpha = 0.6f), RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", currentPoint.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = ClaudeTerracotta
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Canvas 曲线图 (支持点击与平滑跟手左右连续滑动 Touch Scrubbing)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
            ) {
                Canvas(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(130.dp)
                        .pointerInput(points.size) {
                            detectTapGestures { offset ->
                                val spacing = size.width / (points.size - 1).coerceAtLeast(1)
                                selectedIndex = (offset.x / spacing + 0.5f).toInt().coerceIn(0, points.size - 1)
                            }
                        }
                        .pointerInput(points.size) {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    val spacing = size.width / (points.size - 1).coerceAtLeast(1)
                                    selectedIndex = (offset.x / spacing + 0.5f).toInt().coerceIn(0, points.size - 1)
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    val spacing = size.width / (points.size - 1).coerceAtLeast(1)
                                    selectedIndex = (change.position.x / spacing + 0.5f).toInt().coerceIn(0, points.size - 1)
                                }
                            )
                        }
                ) {
                    val width = size.width
                    val height = size.height
                    val stepX = if (points.size > 1) width / (points.size - 1) else width

                    // 绘制水平辅助线
                    val lineCount = 3
                    for (i in 0..lineCount) {
                        val y = height * (i.toFloat() / lineCount)
                        drawLine(
                            color = ClaudeBorder.copy(alpha = 0.4f),
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 0.8f
                        )
                    }

                    val coordPoints = points.mapIndexed { index, item ->
                        val x = index * stepX
                        val normalizedVal = (item.amount / maxAmount).coerceIn(0.0, 1.0).toFloat()
                        val y = height - (normalizedVal * (height - 16.dp.toPx()) * progress.value) - 8.dp.toPx()
                        Offset(x, y)
                    }

                    if (coordPoints.isNotEmpty()) {
                        val strokePath = Path()
                        val fillPath = Path()

                        strokePath.moveTo(coordPoints.first().x, coordPoints.first().y)
                        fillPath.moveTo(coordPoints.first().x, height)
                        fillPath.lineTo(coordPoints.first().x, coordPoints.first().y)

                        for (i in 0 until coordPoints.size - 1) {
                            val p0 = coordPoints[i]
                            val p1 = coordPoints[i + 1]
                            val controlX1 = (p0.x + p1.x) / 2
                            val controlY1 = p0.y
                            val controlX2 = (p0.x + p1.x) / 2
                            val controlY2 = p1.y

                            strokePath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                            fillPath.cubicTo(controlX1, controlY1, controlX2, controlY2, p1.x, p1.y)
                        }

                        fillPath.lineTo(coordPoints.last().x, height)
                        fillPath.close()

                        // 填充温润柔光渐变
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    ClaudeTerracotta.copy(alpha = 0.16f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = height
                            )
                        )

                        // 绘制陶土红折线
                        drawPath(
                            path = strokePath,
                            brush = PrismWarmSpectralBrush,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        val selectedCoord = coordPoints.getOrNull(safeSelectedIndex) ?: coordPoints.last()

                        // 垂直指示线
                        drawLine(
                            color = ClaudeBorder,
                            start = Offset(selectedCoord.x, 0f),
                            end = Offset(selectedCoord.x, height),
                            strokeWidth = 1.dp.toPx()
                        )

                        // 外圈光晕
                        drawCircle(
                            color = ClaudeTerracotta.copy(alpha = 0.25f),
                            radius = 8.dp.toPx(),
                            center = selectedCoord
                        )
                        // 实心彩点
                        drawCircle(
                            color = ClaudeTerracotta,
                            radius = 4.5.dp.toPx(),
                            center = selectedCoord
                        )
                        // 中心白芯
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = selectedCoord
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 智能金融级 X 轴里程碑展示 (5 节点法，彻底消除月度 31 天挤压乱象)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                landmarkIndices.forEach { idx ->
                    val pt = points[idx]
                    val isNearSelected = (idx == safeSelectedIndex)
                    val labelText = if (points.size > 7) {
                        // 月度模式下提取简洁的 "1日", "8日", "15日", "22日", "31日"
                        pt.label
                    } else {
                        pt.label
                    }

                    Text(
                        text = labelText,
                        style = MaterialTheme.typography.labelSmall.copy(
                            fontSize = 11.sp,
                            fontWeight = if (isNearSelected) FontWeight.Bold else FontWeight.Normal
                        ),
                        color = if (isNearSelected) ClaudeTerracotta else ClaudeTextTertiary,
                        textAlign = when (idx) {
                            0 -> TextAlign.Start
                            points.size - 1 -> TextAlign.End
                            else -> TextAlign.Center
                        }
                    )
                }
            }
        }
    }
}
