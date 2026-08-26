package com.localbill.recording.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.model.CategoryAggregation
import com.localbill.recording.data.model.KakeiboPillarStat
import com.localbill.recording.data.model.PeriodType
import com.localbill.recording.ui.components.BezierTrendChart
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.components.DonutPieChart
import com.localbill.recording.ui.components.HankoStampBadge
import com.localbill.recording.ui.components.WashiTapeTab
import com.localbill.recording.ui.theme.HankoRed
import com.localbill.recording.ui.theme.PillarCulture
import com.localbill.recording.ui.theme.SumiDark
import com.localbill.recording.ui.theme.SumiLight
import com.localbill.recording.ui.theme.SumiMedium
import com.localbill.recording.ui.theme.TomoeBorder
import com.localbill.recording.ui.theme.TomoePaperBg
import com.localbill.recording.ui.theme.TomoePaperPage
import com.localbill.recording.ui.viewmodel.StatisticsViewModel
import java.util.Locale

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategoryForDetail by remember { mutableStateOf<CategoryAggregation?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = TomoePaperBg
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 周期分段切换器 (日 / 周 / 月)
            item {
                HobonichiPeriodTabs(
                    selectedType = uiState.periodType,
                    onSelectType = { viewModel.setPeriodType(it) }
                )
            }

            // 2. 日期导航条
            item {
                HobonichiDateNavigator(
                    title = uiState.periodTitle,
                    onPrev = { viewModel.navigatePeriod(-1) },
                    onNext = { viewModel.navigatePeriod(1) }
                )
            }

            // 3. 统计核心指标总览卡片
            item {
                HobonichiMetricsCard(
                    totalAmount = uiState.summary.totalAmount,
                    dailyAverage = uiState.summary.dailyAverage,
                    recordCount = uiState.summary.recordCount,
                    topCategoryName = uiState.summary.topCategoryName,
                    periodType = uiState.periodType
                )
            }

            // 4. 日本家计簿四大消费性质分析卡片
            if (uiState.summary.kakeiboPillars.isNotEmpty()) {
                item {
                    KakeiboPillarsOverviewCard(
                        pillars = uiState.summary.kakeiboPillars,
                        totalAmount = uiState.summary.totalAmount
                    )
                }
            }

            // 5. 月末反思便签
            item {
                HobonichiReflectionCard(quote = uiState.summary.reflectionQuote)
            }

            // 6. 和风平滑贝塞尔走势图
            item {
                BezierTrendChart(
                    points = uiState.trendPoints
                )
            }

            // 7. 和风全彩环形图
            item {
                DonutPieChart(
                    aggregations = uiState.categoryAggregations,
                    totalAmount = uiState.summary.totalAmount,
                    onCategoryClick = { cat ->
                        selectedCategoryForDetail = cat
                    }
                )
            }

            // 8. 分类支出明细榜单
            if (uiState.categoryAggregations.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 4.dp, start = 2.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            WashiTapeTab(
                                title = "明细",
                                tapeColor = PillarCulture.copy(alpha = 0.15f),
                                textColor = PillarCulture
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "分类明细手账",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SumiDark
                            )
                        }
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "轻触分类展开细分子类占比与构成",
                            style = MaterialTheme.typography.bodySmall,
                            color = SumiMedium
                        )
                    }
                }

                items(
                    items = uiState.categoryAggregations,
                    key = { it.mainCategory.id }
                ) { aggregation ->
                    HobonichiRankingItem(
                        item = aggregation,
                        onClick = { selectedCategoryForDetail = aggregation }
                    )
                }
            }

            item {
                Spacer(modifier = Modifier.height(32.dp))
            }
        }
    }

    // 子分类下钻弹窗
    selectedCategoryForDetail?.let { detail ->
        HobonichiCategoryDetailDialog(
            aggregation = detail,
            onDismiss = { selectedCategoryForDetail = null }
        )
    }
}

/**
 * 分段切换器 (按日 / 按周 / 按月)
 */
@Composable
private fun HobonichiPeriodTabs(
    selectedType: PeriodType,
    onSelectType: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(TomoePaperPage)
            .border(0.8.dp, TomoeBorder, RoundedCornerShape(10.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeriodType.values().forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) PillarCulture else Color.Transparent)
                    .clickable { onSelectType(type) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "按${type.label}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else SumiMedium
                )
            }
        }
    }
}

/**
 * 日期导航器
 */
@Composable
private fun HobonichiDateNavigator(
    title: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TomoePaperPage)
                .border(0.8.dp, TomoeBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onPrev),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一周期",
                tint = SumiDark,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = SumiDark
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(TomoePaperPage)
                .border(0.8.dp, TomoeBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一周期",
                tint = SumiDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 指标总览卡片
 */
@Composable
private fun HobonichiMetricsCard(
    totalAmount: Double,
    dailyAverage: Double,
    recordCount: Int,
    topCategoryName: String?,
    periodType: PeriodType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TomoePaperPage)
            .border(0.8.dp, TomoeBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "周期支出总计",
                    style = MaterialTheme.typography.labelSmall,
                    color = SumiMedium
                )
                HankoStampBadge(text = "评", size = 24.dp, angle = 5f)
            }

            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SumiDark,
                    modifier = Modifier.padding(end = 4.dp, bottom = 2.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.2f", totalAmount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = SumiDark,
                    fontFamily = FontFamily.Monospace
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = TomoeBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (periodType == PeriodType.DAY) "消费笔数" else "日均支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (periodType == PeriodType.DAY) "${recordCount} 笔" else "¥ " + String.format(Locale.US, "%.2f", dailyAverage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SumiDark,
                        fontFamily = FontFamily.Monospace
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "主要开销",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = topCategoryName ?: "暂无支出",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (topCategoryName != null) PillarCulture else SumiDark
                    )
                }
            }
        }
    }
}

/**
 * 日本家计簿四大消费性质分析卡片
 */
@Composable
private fun KakeiboPillarsOverviewCard(
    pillars: List<KakeiboPillarStat>,
    totalAmount: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TomoePaperPage)
            .border(0.8.dp, TomoeBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WashiTapeTab(
                        title = "消费四支柱",
                        tapeColor = PillarCulture.copy(alpha = 0.15f),
                        textColor = PillarCulture
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "家计簿 · 消费性质分析",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SumiDark
                    )
                }

                Text(
                    text = "理性与心动",
                    style = MaterialTheme.typography.labelSmall,
                    color = SumiMedium
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                pillars.forEach { stat ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(10.dp))
                            .background(stat.pillar.containerColor)
                            .border(0.8.dp, stat.pillar.color.copy(alpha = 0.3f), RoundedCornerShape(10.dp))
                            .padding(8.dp)
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = stat.pillar.title,
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = stat.pillar.color
                            )
                            Text(
                                text = "${stat.pillar.subTitle}",
                                fontSize = 9.sp,
                                color = SumiMedium
                            )
                            Spacer(modifier = Modifier.height(4.dp))
                            Text(
                                text = "¥" + String.format(Locale.US, "%.0f", stat.amount),
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = SumiDark,
                                fontFamily = FontFamily.Monospace
                            )
                            Text(
                                text = String.format(Locale.US, "%.0f%%", stat.percentage),
                                fontSize = 10.sp,
                                fontWeight = FontWeight.Bold,
                                color = stat.pillar.color
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 月末反思便签
 */
@Composable
private fun HobonichiReflectionCard(quote: String) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(PillarCulture.copy(alpha = 0.08f))
            .border(0.8.dp, PillarCulture.copy(alpha = 0.25f), RoundedCornerShape(14.dp))
            .padding(14.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HankoStampBadge(text = "良", size = 32.dp, angle = -8f)
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(
                    text = "月末复盘 · 手账手记",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = PillarCulture
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = quote,
                    style = MaterialTheme.typography.bodySmall,
                    color = SumiDark,
                    lineHeight = 18.sp
                )
            }
        }
    }
}

/**
 * 分类排行条目卡片
 */
@Composable
private fun HobonichiRankingItem(
    item: CategoryAggregation,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TomoePaperPage)
            .border(0.8.dp, TomoeBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = item.mainCategory.iconName,
                        colorHex = item.mainCategory.colorHex,
                        size = 36.dp,
                        iconSize = 18.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.mainCategory.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.Bold,
                            color = SumiDark
                        )
                        Text(
                            text = if (item.subCategoryBreakdowns.isNotEmpty())
                                "含 ${item.subCategoryBreakdowns.size} 个细分子类"
                            else "${item.count} 笔账单",
                            style = MaterialTheme.typography.bodySmall,
                            color = SumiMedium
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", item.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SumiDark,
                        fontFamily = FontFamily.Monospace
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", item.percentage),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.Bold,
                        color = Color(item.mainCategory.colorHex)
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp))
                    .background(TomoeBorder.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (item.percentage / 100f).coerceIn(0f, 1f))
                        .height(3.dp)
                        .clip(RoundedCornerShape(1.5.dp))
                        .background(Color(item.mainCategory.colorHex))
                )
            }
        }
    }
}

/**
 * 子分类明细下钻弹窗
 */
@Composable
private fun HobonichiCategoryDetailDialog(
    aggregation: CategoryAggregation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryIconBadge(
                    iconName = aggregation.mainCategory.iconName,
                    colorHex = aggregation.mainCategory.colorHex,
                    size = 32.dp,
                    iconSize = 16.dp,
                    cornerRadius = 8.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${aggregation.mainCategory.name} · 子类构成",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SumiDark
                    )
                    Text(
                        text = "总计 ¥ " + String.format(Locale.US, "%.2f", aggregation.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiMedium
                    )
                }
            }
        },
        text = {
            if (aggregation.subCategoryBreakdowns.isEmpty()) {
                Text(
                    text = "该分类下暂无细分子类记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = SumiMedium
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    aggregation.subCategoryBreakdowns.forEach { subItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 4.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryIconBadge(
                                    iconName = subItem.category.iconName,
                                    colorHex = subItem.category.colorHex,
                                    size = 26.dp,
                                    iconSize = 13.dp,
                                    cornerRadius = 6.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = subItem.category.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = SumiDark
                                    )
                                    Text(
                                        text = "${subItem.count} 笔",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = SumiMedium
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥ " + String.format(Locale.US, "%.2f", subItem.amount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = SumiDark,
                                    fontFamily = FontFamily.Monospace
                                )
                                Text(
                                    text = String.format(Locale.US, "占 %.1f%%", subItem.percentage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = Color(subItem.category.colorHex)
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "关闭", fontWeight = FontWeight.Bold, color = PillarCulture)
            }
        }
    )
}
