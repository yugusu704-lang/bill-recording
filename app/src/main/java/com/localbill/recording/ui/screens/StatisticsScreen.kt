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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.model.CategoryAggregation
import com.localbill.recording.data.model.EngelCoefficient
import com.localbill.recording.data.model.PeriodType
import com.localbill.recording.ui.components.BezierTrendChart
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.components.DonutPieChart
import com.localbill.recording.ui.theme.DeepGreen
import com.localbill.recording.ui.theme.TextDark
import com.localbill.recording.ui.theme.TextSecondary
import com.localbill.recording.ui.theme.TextTertiary
import com.localbill.recording.ui.theme.WarmBorder
import com.localbill.recording.ui.theme.WarmBone
import com.localbill.recording.ui.theme.WarmSurface
import com.localbill.recording.ui.viewmodel.StatisticsViewModel
import com.localbill.recording.util.formatAmount
import com.localbill.recording.util.formatPercent

@Composable
fun StatisticsScreen(
    viewModel: StatisticsViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    var selectedCategoryForDetail by remember { mutableStateOf<CategoryAggregation?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmBone
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item(key = "statistics_header") {
                StatisticsHeader(periodTitle = uiState.periodTitle)
            }

            item(key = "period_tabs") {
                StatisticsPeriodTabs(
                    selectedType = uiState.periodType,
                    onSelectType = { viewModel.setPeriodType(it) }
                )
            }

            item(key = "date_navigator") {
                StatisticsDateNavigator(
                    title = uiState.periodTitle,
                    onPrev = { viewModel.navigatePeriod(-1) },
                    onNext = { viewModel.navigatePeriod(1) }
                )
            }

            item(key = "metrics_card") {
                MetricsCard(
                    totalAmount = uiState.summary.totalAmount,
                    dailyAverage = uiState.summary.dailyAverage,
                    recordCount = uiState.summary.recordCount,
                    topCategoryName = uiState.summary.topCategoryName,
                    periodType = uiState.periodType
                )
            }

            item(key = "engel_card") {
                EngelCard(
                    engel = uiState.summary.engelCoefficient,
                    periodType = uiState.periodType
                )
            }

            item(key = "trend_chart") {
                BezierTrendChart(points = uiState.trendPoints)
            }

            item(key = "donut_chart") {
                DonutPieChart(
                    aggregations = uiState.categoryAggregations,
                    totalAmount = uiState.summary.totalAmount,
                    onCategoryClick = { cat ->
                        selectedCategoryForDetail = cat
                    }
                )
            }

            if (uiState.categoryAggregations.isNotEmpty()) {
                item(key = "ranking_title") {
                    Column(modifier = Modifier.padding(top = 2.dp, start = 2.dp)) {
                        Text(
                            text = "分类明细",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = TextDark
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = "轻触分类展开细分子类占比",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                items(
                    items = uiState.categoryAggregations,
                    key = { it.mainCategory.id }
                ) { aggregation ->
                    RankingItem(
                        item = aggregation,
                        onClick = { selectedCategoryForDetail = aggregation }
                    )
                }
            }

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(24.dp))
            }
        }
    }

    selectedCategoryForDetail?.let { detail ->
        CategoryDetailDialog(
            aggregation = detail,
            onDismiss = { selectedCategoryForDetail = null }
        )
    }
}

@Composable
private fun StatisticsHeader(periodTitle: String) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = "统计",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = periodTitle,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
    }
}

@Composable
private fun StatisticsPeriodTabs(
    selectedType: PeriodType,
    onSelectType: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeriodType.values().forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) DeepGreen else Color.Transparent)
                    .clickable { onSelectType(type) }
                    .padding(vertical = 8.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "按${type.label}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) Color.White else TextSecondary
                )
            }
        }
    }
}

@Composable
private fun StatisticsDateNavigator(
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
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(WarmSurface)
                .border(1.dp, WarmBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onPrev),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一周期",
                tint = TextDark,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )

        Box(
            modifier = Modifier
                .size(36.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(WarmSurface)
                .border(1.dp, WarmBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一周期",
                tint = TextDark,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun MetricsCard(
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
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "周期支出总计",
                style = MaterialTheme.typography.labelMedium,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(6.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = DeepGreen,
                    modifier = Modifier.padding(end = 4.dp, bottom = 3.dp)
                )
                Text(
                    text = formatAmount(totalAmount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 32.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = WarmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (periodType == PeriodType.DAY) "消费笔数" else "日均支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (periodType == PeriodType.DAY) "${recordCount} 笔" else "¥ " + formatAmount(dailyAverage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "主要开销",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = topCategoryName ?: "暂无支出",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (topCategoryName != null) DeepGreen else TextDark
                    )
                }
            }
        }
    }
}

@Composable
private fun EngelCard(
    engel: EngelCoefficient,
    periodType: PeriodType
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(DeepGreen.copy(alpha = 0.06f))
            .border(1.dp, DeepGreen.copy(alpha = 0.18f), RoundedCornerShape(16.dp))
            .padding(16.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "恩格尔系数",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    if (periodType == PeriodType.DAY) {
                        Text(
                            text = "单日食品占比仅供参考",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }
                Text(
                    text = engel.levelLabel,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    color = DeepGreen,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(DeepGreen.copy(alpha = 0.1f))
                        .padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = formatPercent(engel.percentage),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = DeepGreen
                )
            }

            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = engel.description,
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = DeepGreen.copy(alpha = 0.12f), thickness = 1.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                EngelMiniStat(label = "食品支出", amount = engel.foodAmount)
                EngelMiniStat(label = "总支出", amount = engel.totalAmount)
            }
        }
    }
}

@Composable
private fun EngelMiniStat(
    label: String,
    amount: Double
) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.bodySmall,
            color = TextSecondary
        )
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = "¥ " + formatAmount(amount),
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            color = TextDark
        )
    }
}

@Composable
private fun RankingItem(
    item: CategoryAggregation,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
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
                            color = TextDark
                        )
                        Text(
                            text = if (item.subCategoryBreakdowns.isNotEmpty())
                                "含 ${item.subCategoryBreakdowns.size} 个细分子类"
                            else "${item.count} 笔账单",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥ " + formatAmount(item.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = formatPercent(item.percentage),
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
                    .background(WarmBorder)
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

@Composable
private fun CategoryDetailDialog(
    aggregation: CategoryAggregation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = WarmSurface,
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
                        color = TextDark
                    )
                    Text(
                        text = "总计 ¥ " + formatAmount(aggregation.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                if (aggregation.subCategoryBreakdowns.isEmpty()) {
                    Text(
                        text = "该分类没有细分子类数据。",
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondary
                    )
                } else {
                    aggregation.subCategoryBreakdowns.forEach { sub ->
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = sub.category.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = TextDark
                            )
                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥ " + formatAmount(sub.amount),
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = TextDark
                                )
                                Text(
                                    text = formatPercent(sub.percentage),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondary
                                )
                            }
                        }
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "知道了", color = DeepGreen, fontWeight = FontWeight.Bold)
            }
        }
    )
}
