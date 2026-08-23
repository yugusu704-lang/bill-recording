package com.localbill.recording.ui.screens

import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronLeft
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
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
import com.localbill.recording.data.model.PeriodType
import com.localbill.recording.ui.components.AnimatedAmountText
import com.localbill.recording.ui.components.BezierTrendChart
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.components.DonutPieChart
import com.localbill.recording.ui.theme.AccentTerracotta
import com.localbill.recording.ui.theme.InkPrimary
import com.localbill.recording.ui.theme.InkQuaternary
import com.localbill.recording.ui.theme.InkSecondary
import com.localbill.recording.ui.theme.InkTertiary
import com.localbill.recording.ui.theme.PaperBorder
import com.localbill.recording.ui.theme.PaperSurfaceSubtle
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
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 顶部周期切换 (北欧极简下划线/暖色胶囊)
            item {
                EditorialPeriodTabs(
                    selectedType = uiState.periodType,
                    onSelectType = { viewModel.setPeriodType(it) }
                )
            }

            // 2. 日期导航条
            item {
                EditorialDateNavigator(
                    title = uiState.periodTitle,
                    onPrev = { viewModel.navigatePeriod(-1) },
                    onNext = { viewModel.navigatePeriod(1) }
                )
            }

            // 3. 核心统计指标 (呼吸感大字)
            item {
                EditorialMetricsOverview(
                    totalAmount = uiState.summary.totalAmount,
                    dailyAverage = uiState.summary.dailyAverage,
                    recordCount = uiState.summary.recordCount,
                    topCategoryName = uiState.summary.topCategoryName,
                    periodType = uiState.periodType
                )
            }

            // 4. 走势图
            item {
                BezierTrendChart(
                    points = uiState.trendPoints,
                    lineColor = InkPrimary,
                    gradientStartColor = InkPrimary.copy(alpha = 0.08f),
                    gradientEndColor = Color.Transparent
                )
            }

            // 5. 环形占比图
            item {
                DonutPieChart(
                    aggregations = uiState.categoryAggregations,
                    totalAmount = uiState.summary.totalAmount,
                    onCategoryClick = { cat ->
                        selectedCategoryForDetail = cat
                    }
                )
            }

            // 6. 分类支出明细清单
            if (uiState.categoryAggregations.isNotEmpty()) {
                item {
                    Column(modifier = Modifier.padding(top = 8.dp)) {
                        Text(
                            text = "分类支出榜单",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = InkPrimary
                        )
                        Text(
                            text = "点击分类查看细分子类消费构成",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                items(
                    items = uiState.categoryAggregations,
                    key = { it.mainCategory.id }
                ) { aggregation ->
                    EditorialRankingItem(
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
        EditorialCategoryDetailDialog(
            aggregation = detail,
            onDismiss = { selectedCategoryForDetail = null }
        )
    }
}

/**
 * 极简周期切换
 */
@Composable
private fun EditorialPeriodTabs(
    selectedType: PeriodType,
    onSelectType: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PaperSurfaceSubtle)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeriodType.values().forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent)
                    .clickable { onSelectType(type) }
                    .padding(vertical = 7.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "按${type.label}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                    color = if (isSelected) InkPrimary else InkSecondary
                )
            }
        }
    }
}

/**
 * 日期导航器
 */
@Composable
private fun EditorialDateNavigator(
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
        IconButton(
            onClick = onPrev,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一周期",
                tint = InkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = InkPrimary
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier.size(32.dp)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一周期",
                tint = InkPrimary,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

/**
 * 杂志风格指标总览
 */
@Composable
private fun EditorialMetricsOverview(
    totalAmount: Double,
    dailyAverage: Double,
    recordCount: Int,
    topCategoryName: String?,
    periodType: PeriodType
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(18.dp)
    ) {
        Text(
            text = "周期总支出",
            style = MaterialTheme.typography.labelSmall,
            color = InkSecondary,
            letterSpacing = 1.sp
        )
        Spacer(modifier = Modifier.height(4.dp))
        Row(verticalAlignment = Alignment.Bottom) {
            Text(

                text = "¥",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = String.format(Locale.US, "%.2f", totalAmount),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 32.sp,
                    fontWeight = FontWeight.ExtraBold
                ),
                color = InkPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))
        HorizontalDivider(color = PaperBorder, thickness = 0.5.dp)
        Spacer(modifier = Modifier.height(12.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = if (periodType == PeriodType.DAY) "消费笔数" else "日均支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (periodType == PeriodType.DAY) "${recordCount} 笔" else "¥ " + String.format(Locale.US, "%.2f", dailyAverage),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "主要开销",
                    style = MaterialTheme.typography.bodySmall,
                    color = InkSecondary
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = topCategoryName ?: "暂无支出",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = if (topCategoryName != null) AccentTerracotta else InkPrimary
                )
            }
        }
    }
}

/**
 * 极简分类排行条目
 */
@Composable
private fun EditorialRankingItem(
    item: CategoryAggregation,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 8.dp, horizontal = 4.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                CategoryIconBadge(
                    iconName = item.mainCategory.iconName,
                    colorHex = item.mainCategory.colorHex,
                    size = 32.dp,
                    iconSize = 16.dp,
                    cornerRadius = 8.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = item.mainCategory.name,
                        style = MaterialTheme.typography.bodyLarge,
                        fontWeight = FontWeight.SemiBold,
                        color = InkPrimary
                    )
                    Text(
                        text = if (item.subCategoryBreakdowns.isNotEmpty())
                            "含 ${item.subCategoryBreakdowns.size} 个子分类明细"
                        else "${item.count} 笔支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }

            Column(horizontalAlignment = Alignment.End) {
                Text(
                    text = "¥ " + String.format(Locale.US, "%.2f", item.totalAmount),
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
                Text(
                    text = String.format(Locale.US, "%.1f%%", item.percentage),
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium,
                    color = Color(item.mainCategory.colorHex)
                )
            }
        }

        // 极细进度条
        Spacer(modifier = Modifier.height(8.dp))
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(3.dp)
                .clip(RoundedCornerShape(1.5.dp))
                .background(PaperSurfaceSubtle)
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

/**
 * 子分类明细下钻弹窗
 */
@Composable
private fun EditorialCategoryDetailDialog(
    aggregation: CategoryAggregation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.fillMaxWidth()
            ) {
                CategoryIconBadge(
                    iconName = aggregation.mainCategory.iconName,
                    colorHex = aggregation.mainCategory.colorHex,
                    size = 32.dp,
                    iconSize = 18.dp,
                    cornerRadius = 8.dp
                )
                Spacer(modifier = Modifier.width(10.dp))
                Column {
                    Text(
                        text = "${aggregation.mainCategory.name} · 子类明细",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "合计 ¥ " + String.format(Locale.US, "%.2f", aggregation.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = InkSecondary
                    )
                }
            }
        },
        text = {
            if (aggregation.subCategoryBreakdowns.isEmpty()) {
                Text(
                    text = "该分类下暂无细分子类消费记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = InkSecondary
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
                                    size = 24.dp,
                                    iconSize = 14.dp,
                                    cornerRadius = 6.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = subItem.category.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.Medium,
                                        color = InkPrimary
                                    )
                                    Text(
                                        text = "${subItem.count} 笔",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = InkSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥ " + String.format(Locale.US, "%.2f", subItem.amount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = InkPrimary
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
                Text(text = "关闭", fontWeight = FontWeight.Bold, color = InkPrimary)
            }
        }
    )
}
