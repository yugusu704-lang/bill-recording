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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import com.localbill.recording.data.model.PeriodType
import com.localbill.recording.ui.components.BezierTrendChart
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.components.DonutPieChart
import com.localbill.recording.ui.theme.PrismBlack
import com.localbill.recording.ui.theme.PrismBorder
import com.localbill.recording.ui.theme.PrismLavender
import com.localbill.recording.ui.theme.PrismPink
import com.localbill.recording.ui.theme.PrismSlate
import com.localbill.recording.ui.theme.PrismSpectralBrush
import com.localbill.recording.ui.theme.PrismTextSecondary
import com.localbill.recording.ui.theme.PrismTextTertiary
import com.localbill.recording.ui.theme.PrismWhite
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
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. 周期分段切换器
            item {
                LightcorePeriodTabs(
                    selectedType = uiState.periodType,
                    onSelectType = { viewModel.setPeriodType(it) }
                )
            }

            // 2. 日期导航条
            item {
                LightcoreDateNavigator(
                    title = uiState.periodTitle,
                    onPrev = { viewModel.navigatePeriod(-1) },
                    onNext = { viewModel.navigatePeriod(1) }
                )
            }

            // 3. 统计核心指标总览卡片
            item {
                LightcoreMetricsCard(
                    totalAmount = uiState.summary.totalAmount,
                    dailyAverage = uiState.summary.dailyAverage,
                    recordCount = uiState.summary.recordCount,
                    topCategoryName = uiState.summary.topCategoryName,
                    periodType = uiState.periodType
                )
            }

            // 4. 贝塞尔微光走势图 (复刻图例 9)
            item {
                BezierTrendChart(
                    points = uiState.trendPoints
                )
            }

            // 5. 全彩光谱折射环形图 (复刻图例 9)
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
                    Column(modifier = Modifier.padding(top = 4.dp)) {
                        Text(
                            text = "分类支出榜单",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = PrismBlack
                        )
                        Text(
                            text = "点击分类查看细分子类构成与占比",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrismTextSecondary
                        )
                    }
                }

                items(
                    items = uiState.categoryAggregations,
                    key = { it.mainCategory.id }
                ) { aggregation ->
                    LightcoreRankingItem(
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
        LightcoreCategoryDetailDialog(
            aggregation = detail,
            onDismiss = { selectedCategoryForDetail = null }
        )
    }
}

/**
 * 分段切换器 (日 / 周 / 月)
 */
@Composable
private fun LightcorePeriodTabs(
    selectedType: PeriodType,
    onSelectType: (PeriodType) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(PrismSlate)
            .border(1.dp, PrismBorder, RoundedCornerShape(12.dp))
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        PeriodType.values().forEach { type ->
            val isSelected = type == selectedType
            Box(
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(9.dp))
                    .background(if (isSelected) PrismWhite else Color.Transparent)
                    .border(
                        width = if (isSelected) 1.dp else 0.dp,
                        color = if (isSelected) PrismBorder else Color.Transparent,
                        shape = RoundedCornerShape(9.dp)
                    )
                    .clickable { onSelectType(type) }
                    .padding(vertical = 6.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "按${type.label}",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                    color = if (isSelected) PrismBlack else PrismTextSecondary
                )
            }
        }
    }
}

/**
 * 日期导航器
 */
@Composable
private fun LightcoreDateNavigator(
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
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrismWhite)
                .border(1.dp, PrismBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onPrev),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronLeft,
                contentDescription = "上一周期",
                tint = PrismBlack,
                modifier = Modifier.size(18.dp)
            )
        }

        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            color = PrismBlack
        )

        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(RoundedCornerShape(10.dp))
                .background(PrismWhite)
                .border(1.dp, PrismBorder, RoundedCornerShape(10.dp))
                .clickable(onClick = onNext),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = "下一周期",
                tint = PrismBlack,
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

/**
 * 指标总览卡片
 */
@Composable
private fun LightcoreMetricsCard(
    totalAmount: Double,
    dailyAverage: Double,
    recordCount: Int,
    topCategoryName: String?,
    periodType: PeriodType
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PrismBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrismWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = "周期总支出 (Total Spending)",
                style = MaterialTheme.typography.labelSmall,
                color = PrismTextTertiary,
                letterSpacing = 0.5.sp
            )
            Spacer(modifier = Modifier.height(4.dp))
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PrismBlack,
                    modifier = Modifier.padding(end = 4.dp, bottom = 2.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.2f", totalAmount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 30.sp,
                        fontWeight = FontWeight.ExtraBold
                    ),
                    color = PrismBlack
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PrismBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = if (periodType == PeriodType.DAY) "消费笔数" else "日均支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrismTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (periodType == PeriodType.DAY) "${recordCount} 笔" else "¥ " + String.format(Locale.US, "%.2f", dailyAverage),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrismBlack
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "首要开销",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrismTextSecondary
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = topCategoryName ?: "暂无支出",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = if (topCategoryName != null) PrismLavender else PrismBlack
                    )
                }
            }
        }
    }
}

/**
 * 分类排行条目卡片
 */
@Composable
private fun LightcoreRankingItem(
    item: CategoryAggregation,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PrismBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrismWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
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
                        size = 34.dp,
                        iconSize = 16.dp,
                        cornerRadius = 8.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = item.mainCategory.name,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PrismBlack
                        )
                        Text(
                            text = if (item.subCategoryBreakdowns.isNotEmpty())
                                "含 ${item.subCategoryBreakdowns.size} 个细分子类"
                            else "${item.count} 笔明细",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrismTextSecondary
                        )
                    }
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", item.totalAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrismBlack
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
                    .background(PrismSlate)
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
private fun LightcoreCategoryDetailDialog(
    aggregation: CategoryAggregation,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = PrismWhite,
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
                        color = PrismBlack
                    )
                    Text(
                        text = "总计 ¥ " + String.format(Locale.US, "%.2f", aggregation.totalAmount),
                        style = MaterialTheme.typography.bodySmall,
                        color = PrismTextSecondary
                    )
                }
            }
        },
        text = {
            if (aggregation.subCategoryBreakdowns.isEmpty()) {
                Text(
                    text = "该分类下暂无细分子类记录",
                    style = MaterialTheme.typography.bodyMedium,
                    color = PrismTextSecondary
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
                                        color = PrismBlack
                                    )
                                    Text(
                                        text = "${subItem.count} 笔",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = PrismTextSecondary
                                    )
                                }
                            }

                            Column(horizontalAlignment = Alignment.End) {
                                Text(
                                    text = "¥ " + String.format(Locale.US, "%.2f", subItem.amount),
                                    style = MaterialTheme.typography.titleSmall,
                                    fontWeight = FontWeight.Bold,
                                    color = PrismBlack
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
                Text(text = "关闭", fontWeight = FontWeight.Bold, color = PrismBlack)
            }
        }
    )
}
