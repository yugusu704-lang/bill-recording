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
import com.localbill.recording.ui.theme.MatchaPrimary
import com.localbill.recording.ui.theme.SumiInk
import com.localbill.recording.ui.theme.SumiSecondary
import com.localbill.recording.ui.theme.SumiTertiary
import com.localbill.recording.ui.theme.WashiBorder
import com.localbill.recording.ui.theme.WashiCardBg
import com.localbill.recording.ui.theme.WashiPaperSubtle
import java.util.Locale
import kotlin.math.atan2
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
                color = SumiSecondary
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

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WashiCardBg)
            .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "分类支出占比",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = SumiInk,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // 环形图与中心指标
            Box(
                modifier = Modifier.size(190.dp),
                contentAlignment = Alignment.Center
            ) {
                Canvas(
                    modifier = Modifier
                        .size(175.dp)
                        .pointerInput(aggregations) {
                            detectTapGestures { offset ->
                                val center = Offset(size.width / 2f, size.height / 2f)
                                val dx = offset.x - center.x
                                val dy = offset.y - center.y
                                val dist = sqrt(dx * dx + dy * dy)
                                val outerRadius = size.width / 2f
                                val innerRadius = outerRadius - 28.dp.toPx()

                                if (dist in innerRadius..outerRadius) {
                                    var angle = Math.toDegrees(atan2(dy.toDouble(), dx.toDouble())).toFloat()
                                    if (angle < 0) angle += 360f
                                    val rotAngle = (angle + 90f) % 360f

                                    var curAngle = 0f
                                    for (i in aggregations.indices) {
                                        val sweep = (aggregations[i].totalAmount / totalAmount * 360f).toFloat()
                                        if (rotAngle in curAngle..(curAngle + sweep)) {
                                            selectedIndex = i
                                            onCategoryClick?.invoke(aggregations[i])
                                            break
                                        }
                                        curAngle += sweep
                                    }
                                }
                            }
                        }
                ) {
                    val strokeWidthBase = 22.dp.toPx()
                    val strokeWidthSelected = 28.dp.toPx()
                    val outerPadding = 14.dp.toPx()
                    val arcSize = Size(size.width - outerPadding * 2, size.height - outerPadding * 2)
                    val arcTopLeft = Offset(outerPadding, outerPadding)

                    var startAngle = -90f

                    aggregations.forEachIndexed { index, agg ->
                        val sweepAngle = ((agg.totalAmount / totalAmount) * 360f * progress.value).toFloat()
                        val isSelected = index == safeSelectedIndex
                        val catColor = Color(agg.mainCategory.colorHex)

                        drawArc(
                            color = if (isSelected) catColor else catColor.copy(alpha = 0.85f),
                            startAngle = startAngle,
                            sweepAngle = (sweepAngle - 2.5f).coerceAtLeast(0.5f),
                            useCenter = false,
                            topLeft = arcTopLeft,
                            size = arcSize,
                            style = Stroke(
                                width = if (isSelected) strokeWidthSelected else strokeWidthBase,
                                cap = StrokeCap.Round
                            )
                        )
                        startAngle += sweepAngle
                    }
                }

                // 中心聚焦数据卡
                val selCategory = aggregations[safeSelectedIndex]
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = selCategory.mainCategory.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SumiInk
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", selCategory.percentage),
                        style = MaterialTheme.typography.headlineMedium,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color(selCategory.mainCategory.colorHex)
                    )
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", selCategory.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 底部图例列表 (FlowRow 弹性排列)
            FlowRow(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                aggregations.forEachIndexed { index, agg ->
                    val isSelected = index == safeSelectedIndex
                    val catColor = Color(agg.mainCategory.colorHex)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(if (isSelected) catColor.copy(alpha = 0.12f) else WashiPaperSubtle)
                            .border(
                                width = if (isSelected) 1.dp else 0.dp,
                                color = if (isSelected) catColor else Color.Transparent,
                                shape = RoundedCornerShape(8.dp)
                            )
                            .pointerInput(Unit) {
                                detectTapGestures {
                                    selectedIndex = index
                                    onCategoryClick?.invoke(agg)
                                }
                            }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(catColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = agg.mainCategory.name,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) SumiInk else SumiSecondary
                            )
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = String.format(Locale.US, "%.1f%%", agg.percentage),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold,
                                color = if (isSelected) catColor else SumiTertiary
                            )
                        }
                    }
                }
            }
        }
    }
}
