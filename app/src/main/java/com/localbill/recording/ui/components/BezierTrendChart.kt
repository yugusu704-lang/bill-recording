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
import com.localbill.recording.ui.theme.MatchaPrimary
import com.localbill.recording.ui.theme.SakuraAccent
import com.localbill.recording.ui.theme.SumiInk
import com.localbill.recording.ui.theme.SumiSecondary
import com.localbill.recording.ui.theme.SumiTertiary
import com.localbill.recording.ui.theme.WashiBorder
import com.localbill.recording.ui.theme.WashiCardBg
import com.localbill.recording.ui.theme.WashiPaperSubtle
import com.localbill.recording.ui.theme.YamabukiGold
import java.util.Locale

@Composable
fun BezierTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = MatchaPrimary,
    gradientStartColor: Color = MatchaPrimary.copy(alpha = 0.18f),
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
                color = SumiSecondary
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

    val landmarkIndices = remember(points.size) {
        if (points.size <= 7) {
            points.indices.toList()
        } else {
            val last = points.size - 1
            // 均匀抽取 3~4 个不拥挤的关键里程碑
            val mid = (last / 2)
            listOf(0, mid, last).distinct().sorted()
        }
    }

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WashiCardBg)
            .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 顶部交互提示
            val selectedPoint = points[safeSelectedIndex]
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "支出走势 (滑动查看)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                        color = SumiInk
                    )
                    Text(
                        text = "${selectedPoint.label} ${selectedPoint.subLabel}",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiSecondary
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", selectedPoint.amount),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = if (selectedPoint.amount > 0) MatchaPrimary else SumiTertiary
                    )
                    if (selectedPoint.isCurrent) {
                        Text(
                            text = "今日",
                            style = MaterialTheme.typography.labelSmall,
                            color = SakuraAccent,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // 画布主体
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(130.dp)
                    .pointerInput(points) {
                        detectTapGestures { offset ->
                            val n = points.size
                            if (n <= 1) {
                                selectedIndex = 0
                            } else {
                                val stepX = size.width / (n - 1).toFloat()
                                val nearest = (offset.x / stepX).toInt().coerceIn(0, n - 1)
                                selectedIndex = nearest
                            }
                        }
                    }
                    .pointerInput(points) {
                        detectDragGestures { change, _ ->
                            change.consume()
                            val n = points.size
                            if (n <= 1) {
                                selectedIndex = 0
                            } else {
                                val stepX = size.width / (n - 1).toFloat()
                                val nearest = (change.position.x / stepX).toInt().coerceIn(0, n - 1)
                                selectedIndex = nearest
                            }
                        }
                    }
            ) {
                Canvas(modifier = Modifier.matchParentSize()) {
                    val w = size.width
                    val h = size.height
                    val n = points.size
                    val topPadding = 16.dp.toPx()
                    val bottomPadding = 8.dp.toPx()
                    val usableHeight = h - topPadding - bottomPadding

                    // 绘制水平辅助虚线 (3条)
                    for (i in 0..2) {
                        val y = topPadding + usableHeight * (i / 2f)
                        drawLine(
                            color = WashiBorder.copy(alpha = 0.6f),
                            start = Offset(0f, y),
                            end = Offset(w, y),
                            strokeWidth = 1.dp.toPx()
                        )
                    }

                    if (n == 1) {
                        val centerOffset = Offset(w / 2f, h - bottomPadding - (points[0].amount / maxAmount).toFloat() * usableHeight * progress.value)
                        drawCircle(
                            color = lineColor,
                            radius = 6.dp.toPx(),
                            center = centerOffset
                        )
                        return@Canvas
                    }

                    val stepX = w / (n - 1).toFloat()
                    val offsets = points.mapIndexed { index, point ->
                        val x = index * stepX
                        val normalizedVal = (point.amount / maxAmount).toFloat().coerceIn(0f, 1f)
                        val y = h - bottomPadding - (normalizedVal * usableHeight * progress.value)
                        Offset(x, y)
                    }

                    // 构建贝塞尔平滑路径
                    val strokePath = Path()
                    val fillPath = Path()

                    strokePath.moveTo(offsets[0].x, offsets[0].y)
                    fillPath.moveTo(offsets[0].x, h)
                    fillPath.lineTo(offsets[0].x, offsets[0].y)

                    for (i in 0 until offsets.size - 1) {
                        val p0 = offsets[i]
                        val p1 = offsets[i + 1]
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

                    fillPath.lineTo(offsets.last().x, h)
                    fillPath.close()

                    // 绘制渐变填充
                    drawPath(
                        path = fillPath,
                        brush = Brush.verticalGradient(
                            colors = listOf(gradientStartColor, gradientEndColor),
                            startY = topPadding,
                            endY = h
                        )
                    )

                    // 绘制曲线
                    drawPath(
                        path = strokePath,
                        color = lineColor,
                        style = Stroke(
                            width = 2.4.dp.toPx(),
                            cap = StrokeCap.Round
                        )
                    )

                    // 绘制选中点指示器
                    val selOffset = offsets[safeSelectedIndex]
                    // 垂直游标细线
                    drawLine(
                        color = SakuraAccent.copy(alpha = 0.5f),
                        start = Offset(selOffset.x, topPadding),
                        end = Offset(selOffset.x, h),
                        strokeWidth = 1.2.dp.toPx()
                    )

                    // 焦点光晕与圆点
                    drawCircle(
                        color = SakuraAccent.copy(alpha = 0.2f),
                        radius = 10.dp.toPx(),
                        center = selOffset
                    )
                    drawCircle(
                        color = Color.White,
                        radius = 5.dp.toPx(),
                        center = selOffset
                    )
                    drawCircle(
                        color = SakuraAccent,
                        radius = 3.5.dp.toPx(),
                        center = selOffset
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // X 轴里程碑式文字标签 (两端对齐与居中平铺)
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                if (points.isNotEmpty()) {
                    landmarkIndices.forEach { idx ->
                        val point = points[idx]
                        val text = if (point.subLabel.isNotEmpty()) point.subLabel else point.label
                        val isSelected = idx == safeSelectedIndex

                        Text(
                            text = text,
                            fontSize = 11.sp,
                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                            color = if (isSelected) MatchaPrimary else SumiTertiary
                        )
                    }
                }
            }
        }
    }
}
