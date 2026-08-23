package com.localbill.recording.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.model.TrendPoint
import com.localbill.recording.ui.theme.PrismBlack
import com.localbill.recording.ui.theme.PrismBorder
import com.localbill.recording.ui.theme.PrismCyan
import com.localbill.recording.ui.theme.PrismLavender
import com.localbill.recording.ui.theme.PrismLuminousBrush
import com.localbill.recording.ui.theme.PrismPink
import com.localbill.recording.ui.theme.PrismSlate
import com.localbill.recording.ui.theme.PrismTextSecondary
import com.localbill.recording.ui.theme.PrismTextTertiary
import com.localbill.recording.ui.theme.PrismWhite
import java.util.Locale

@Composable
fun BezierTrendChart(
    points: List<TrendPoint>,
    modifier: Modifier = Modifier,
    lineColor: Color = PrismLavender,
    gradientStartColor: Color = PrismLavender.copy(alpha = 0.15f),
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
                color = PrismTextSecondary
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
                .padding(16.dp)
        ) {
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
                        color = PrismBlack
                    )
                    Text(
                        text = "${currentPoint.label} ${if (currentPoint.subLabel.isNotEmpty()) "(${currentPoint.subLabel})" else ""}",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrismTextSecondary
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrismSlate)
                        .border(1.dp, PrismBorder, RoundedCornerShape(8.dp))
                        .padding(horizontal = 10.dp, vertical = 4.dp)
                ) {
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", currentPoint.amount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrismBlack
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

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
                    val paddingBottom = 22.dp.toPx()
                    val chartHeight = height - paddingBottom
                    val stepX = if (points.size > 1) width / (points.size - 1) else width

                    // 绘制极细辅助虚线
                    val lineCount = 3
                    for (i in 0..lineCount) {
                        val y = chartHeight * (i.toFloat() / lineCount)
                        drawLine(
                            color = PrismBorder,
                            start = Offset(0f, y),
                            end = Offset(width, y),
                            strokeWidth = 0.8f
                        )
                    }

                    val coordPoints = points.mapIndexed { index, item ->
                        val x = index * stepX
                        val normalizedVal = (item.amount / maxAmount).coerceIn(0.0, 1.0).toFloat()
                        val y = chartHeight - (normalizedVal * chartHeight * 0.85f * progress.value) - 8.dp.toPx()
                        Offset(x, y)
                    }

                    if (coordPoints.isNotEmpty()) {
                        val strokePath = Path()
                        val fillPath = Path()

                        strokePath.moveTo(coordPoints.first().x, coordPoints.first().y)
                        fillPath.moveTo(coordPoints.first().x, chartHeight)
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

                        fillPath.lineTo(coordPoints.last().x, chartHeight)
                        fillPath.close()

                        // 填充柔和微光渐变
                        drawPath(
                            path = fillPath,
                            brush = Brush.verticalGradient(
                                colors = listOf(
                                    PrismLavender.copy(alpha = 0.18f),
                                    PrismCyan.copy(alpha = 0.05f),
                                    Color.Transparent
                                ),
                                startY = 0f,
                                endY = chartHeight
                            )
                        )

                        // 绘制五彩棱镜折射渐变线条
                        drawPath(
                            path = strokePath,
                            brush = PrismLuminousBrush,
                            style = Stroke(
                                width = 2.5.dp.toPx(),
                                cap = StrokeCap.Round
                            )
                        )

                        // 绘制选中的发光脉冲点
                        val selectedCoord = coordPoints.getOrNull(safeSelectedIndex) ?: coordPoints.last()

                        // 垂直指示线
                        drawLine(
                            color = PrismBorder,
                            start = Offset(selectedCoord.x, 0f),
                            end = Offset(selectedCoord.x, chartHeight),
                            strokeWidth = 1.dp.toPx()
                        )

                        // 外圈光晕
                        drawCircle(
                            color = PrismLavender.copy(alpha = 0.35f),
                            radius = 8.dp.toPx(),
                            center = selectedCoord
                        )
                        // 中圈彩色
                        drawCircle(
                            color = PrismLavender,
                            radius = 4.5.dp.toPx(),
                            center = selectedCoord
                        )
                        // 中心白芯
                        drawCircle(
                            color = Color.White,
                            radius = 2.dp.toPx(),
                            center = selectedCoord
                        )

                        // 绘制底部 X 轴标签
                        val textPaint = android.graphics.Paint().apply {
                            color = android.graphics.Color.parseColor("#94A3B8")
                            textSize = 10.sp.toPx()
                            isAntiAlias = true
                            textAlign = android.graphics.Paint.Align.CENTER
                        }

                        points.forEachIndexed { index, pt ->
                            val x = index * stepX
                            val y = height - 4.dp.toPx()
                            drawContext.canvas.nativeCanvas.drawText(
                                pt.label,
                                x,
                                y,
                                textPaint
                            )
                        }
                    }
                }
            }
        }
    }
}
