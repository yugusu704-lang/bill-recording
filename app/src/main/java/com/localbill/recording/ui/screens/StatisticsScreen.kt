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
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.TrendingUp
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
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
import com.localbill.recording.ui.theme.PrimaryGreen
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
        modifier = modifier.fillMaxSize()
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 周期选择 Tab (日 / 周 / 月)
            item {
                PeriodSelectorTab(
                    selectedType = uiState.periodType,
                    onSelectType = { viewModel.setPeriodType(it) }
                )
            }

            // 2. 日期区间切换导航器
            item {
                DateRangeNavigator(
                    title = uiState.periodTitle,
                    onPrev = { viewModel.navigatePeriod(-1) },
                    onNext = { viewModel.navigatePeriod(1) }
                )
            }

            // 3. 统计汇总指标卡片 (总支出 / 日均支出 / 支出笔数)
            item {
                StatisticsSummaryCards(
                    totalAmount = uiState.summary.totalAmount,
                    dailyAverage = uiState.summary.dailyAverage,
                    recordCount = uiState.summary.recordCount,
                    topCategoryName = uiState.summary.topCategoryName,
                    periodType = uiState.periodType
                )
            }

            // 4. 支出走势图 (平滑贝塞尔曲线)
            item {
                BezierTrendChart(
                    points = uiState.trendPoints
                )
            }

            // 5. 分类占比环形图
            item {
                DonutPieChart(
                    aggregations = uiState.categoryAggregations,
                    totalAmount = uiState.summary.totalAmount,
                    onCategoryClick = { cat ->
                        selectedCategoryForDetail = cat
                    }
                )
            }

            // 6. 分类支出排行榜列表
            if (uiState.categoryAggregations.isNotEmpty()) {
                item {
                    Text(
                        text = "支出排行榜（点击查看子分类明细）",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(top = 8.dp, bottom = 4.dp)
                    )
                }

                items(
                    items = uiState.categoryAggregations,
                    key = { it.mainCategory.id }
                ) { aggregation ->
                    CategoryRankingItem(
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

    // 子分类明细下钻弹窗
    selectedCategoryForDetail?.let { detail ->
        CategoryDetailDialog(
            aggregation = detail,
            onDismiss = { selectedCategoryForDetail = null }
        )
    }
}

/**
 * 周期切换 Tab (日/周/月)
 */
@Composable
private fun PeriodSelectorTab(
    selectedType: PeriodType,
    onSelectType: (PeriodType) -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp)),
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.6f),
        shape = RoundedCornerShape(16.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            PeriodType.values().forEach { type ->
                val isSelected = type == selectedType
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .background(
                            if (isSelected) MaterialTheme.colorScheme.surface else Color.Transparent
                        )
                        .clickable { onSelectType(type) }
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "按${type.label}统计",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                        color = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

/**
 * 日期区间导航栏
 */
@Composable
private fun DateRangeNavigator(
    title: String,
    onPrev: () -> Unit,
    onNext: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onPrev,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一周期",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.onSurface
        )

        IconButton(
            onClick = onNext,
            modifier = Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.surface)
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一周期",
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

/**
 * 汇总指标卡片
 */
@Composable
private fun StatisticsSummaryCards(
    totalAmount: Double,
    dailyAverage: Double,
    recordCount: Int,
    topCategoryName: String?,
    periodType: PeriodType
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        // 总支出大卡
        Card(
            modifier = Modifier.weight(1.2f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = "总支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                AnimatedAmountText(
                    amount = totalAmount,
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrimaryGreen
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "共 ${recordCount} 笔消费",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 日均与大头卡
        Card(
            modifier = Modifier.weight(1f),
            shape = RoundedCornerShape(18.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = if (periodType == PeriodType.DAY) "单笔最高" else "日均支出",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "¥ " + String.format(Locale.US, "%.2f", dailyAverage),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (topCategoryName != null) "最大类: $topCategoryName" else "暂无分类",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1
                )
            }
        }
    }
}

/**
 * 分类排行条目
 */
@Composable
private fun CategoryRankingItem(
    item: CategoryAggregation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = item.mainCategory.iconName,
                        colorHex = item.mainCategory.colorHex,
                        size = 38.dp,
                        iconSize = 20.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.mainCategory.name,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                        Text(
                            text = if (item.subCategoryBreakdowns.isNotEmpty())
                                "含 ${item.subCategoryBreakdowns.size} 个子类明细 (点击展开)"
                            else "共 ${item.count} 笔支出",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", item.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = String.format(Locale.US, "%.1f%%", item.percentage),
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = Color(item.mainCategory.colorHex)
                    )
                }
            }

            // 进度占比条
            Spacer(modifier = Modifier.height(10.dp))
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(6.dp)
                    .clip(RoundedCornerShape(3.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(fraction = (item.percentage / 100f).coerceIn(0f, 1f))
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp))
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
private fun CategoryDetailDialog(
    aggregation: CategoryAggregation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = aggregation.mainCategory.iconName,
                        colorHex = aggregation.mainCategory.colorHex,
                        size = 36.dp,
                        iconSize = 20.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "${aggregation.mainCategory.name} · 子类明细",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "总支出 ¥ " + String.format(Locale.US, "%.2f", aggregation.totalAmount),
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGreen
                        )
                    }
                }
            }
        },
        text = {
            if (aggregation.subCategoryBreakdowns.isEmpty()) {
                Text(
                    text = "该分类下暂无细分子类消费记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Column(
                    modifier = Modifier.fillMaxWidth(),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    aggregation.subCategoryBreakdowns.forEach { subItem ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
                                .padding(horizontal = 12.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                CategoryIconBadge(
                                    iconName = subItem.category.iconName,
                                    colorHex = subItem.category.colorHex,
                                    size = 28.dp,
                                    iconSize = 16.dp,
                                    cornerRadius = 6.dp
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(
                                        text = subItem.category.name,
                                        style = MaterialTheme.typography.bodyMedium,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                    Text(
                                        text = "${subItem.count} 笔",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥ " + String.format(Locale.US, "%.2f", subItem.amount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = String.format(Locale.US, "占大类 %.1f%%", subItem.percentage),
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
                Text(text = "关闭")
            }
        }
    )
}
