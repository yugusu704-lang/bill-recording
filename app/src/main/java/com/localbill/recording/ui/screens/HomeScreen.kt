package com.localbill.recording.ui.screens

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.Category
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.NotificationsNone
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
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
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.ui.components.CalculatorBottomSheet
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.theme.PrismBlack
import com.localbill.recording.ui.theme.PrismBorder
import com.localbill.recording.ui.theme.PrismCyan
import com.localbill.recording.ui.theme.PrismLavender
import com.localbill.recording.ui.theme.PrismLuminousBrush
import com.localbill.recording.ui.theme.PrismPink
import com.localbill.recording.ui.theme.PrismSlate
import com.localbill.recording.ui.theme.PrismSpectralBrush
import com.localbill.recording.ui.theme.PrismTextSecondary
import com.localbill.recording.ui.theme.PrismTextTertiary
import com.localbill.recording.ui.theme.PrismWhite
import com.localbill.recording.ui.viewmodel.HomeViewModel
import com.localbill.recording.util.DateTimeUtils
import java.time.LocalDate
import java.util.Locale

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()

    var isBottomSheetOpen by remember { mutableStateOf(false) }
    var editingRecord by remember { mutableStateOf<RecordWithCategory?>(null) }
    var recordToDelete by remember { mutableStateOf<RecordWithCategory?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    isBottomSheetOpen = true
                },
                containerColor = PrismBlack,
                contentColor = PrismWhite,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记一笔",
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // 1. Lightcore 顶部总览卡片 (复刻原型：Welcome + Balance + Tag + Sparkline)
            item {
                LightcoreBalanceOverviewCard(
                    monthAmount = uiState.monthExpense,
                    todayAmount = uiState.todayExpense,
                    weekAmount = uiState.weekExpense,
                    totalCount = uiState.totalRecordCount
                )
            }

            // 2. 快捷操作微胶囊 (Quick Actions)
            item {
                LightcoreQuickActions(
                    onAddClick = {
                        editingRecord = null
                        isBottomSheetOpen = true
                    }
                )
            }

            // 3. 流水明细列表 (Recent Activity 悬浮白底卡片容器)
            item {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "近期明细 (Recent Activity)",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrismBlack
                    )
                    Text(
                        text = "共 ${uiState.totalRecordCount} 笔",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrismTextSecondary
                    )
                }
            }

            if (uiState.groupedDays.isEmpty()) {
                item {
                    EmptyStateView(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(200.dp)
                    )
                }
            } else {
                uiState.groupedDays.forEach { dayGroup ->
                    item(
                        key = "header_${dayGroup.date}",
                        contentType = "date_header"
                    ) {
                        LightcoreDateHeader(
                            date = dayGroup.date,
                            dayTotal = dayGroup.totalAmount
                        )
                    }

                    items(
                        items = dayGroup.records,
                        key = { it.record.id },
                        contentType = { "record_item" }
                    ) { recordItem ->
                        LightcoreRecordItem(
                            item = recordItem,
                            onClick = {
                                editingRecord = recordItem
                                isBottomSheetOpen = true
                            },
                            onDelete = {
                                recordToDelete = recordItem
                            }
                        )
                    }
                }
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (isBottomSheetOpen) {
        CalculatorBottomSheet(
            allMainCategories = uiState.mainCategories,
            subCategoriesMap = uiState.subCategoriesMap,
            editingRecord = editingRecord,
            onDismiss = {
                isBottomSheetOpen = false
                editingRecord = null
            },
            onSaveRecord = { amount, categoryId, subCategoryId, note, timestamp, imagePath ->
                viewModel.saveRecord(
                    amount = amount,
                    categoryId = categoryId,
                    subCategoryId = subCategoryId,
                    note = note,
                    timestamp = timestamp,
                    imagePath = imagePath
                )
            }
        )
    }

    recordToDelete?.let { recordItem ->
        AlertDialog(
            onDismissRequest = { recordToDelete = null },
            title = { Text(text = "删除此账单？", fontWeight = FontWeight.Bold) },
            text = {
                Text(
                    text = "即将删除「${recordItem.displayCategoryName}」支出 ¥ ${
                        String.format(Locale.US, "%.2f", recordItem.record.amount)
                    }，此操作不可撤销。"
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(recordItem.record.id)
                        recordToDelete = null
                    }
                ) {
                    Text(text = "确认删除", color = MaterialTheme.colorScheme.error, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(text = "取消")
                }
            }
        )
    }
}

/**
 * 顶部总览卡片 (参考右下角 Mobile App 原型)
 */
@Composable
private fun LightcoreBalanceOverviewCard(
    monthAmount: Double,
    todayAmount: Double,
    weekAmount: Double,
    totalCount: Int
) {
    val now = LocalDate.now()
    val monthTitle = "${now.year} 年 ${now.monthValue} 月"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PrismBorder, RoundedCornerShape(20.dp)),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = PrismWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp)
        ) {
            // 顶部问候与图标微按钮
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "Welcome to Lightcore",
                        style = MaterialTheme.typography.bodySmall,
                        color = PrismTextSecondary
                    )
                    Text(
                        text = "本月累计开销 (Total Expense)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = PrismTextTertiary,
                        letterSpacing = 0.5.sp
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrismSlate)
                        .border(1.dp, PrismBorder, RoundedCornerShape(10.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.NotificationsNone,
                        contentDescription = null,
                        tint = PrismBlack,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 金额大字号排版 + Prism 渐变胶囊
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.Bottom,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.Bottom) {
                    Text(
                        text = "¥",
                        style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                        color = PrismBlack,
                        modifier = Modifier.padding(end = 4.dp, bottom = 2.dp)
                    )
                    Text(
                        text = String.format(Locale.US, "%.2f", monthAmount),
                        style = MaterialTheme.typography.displayLarge.copy(
                            fontSize = 32.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = (-0.5).sp
                        ),
                        color = PrismBlack
                    )
                }

                // 标志性 Prism 折射渐变微胶囊
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(PrismSpectralBrush)
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = "▲ ${monthTitle}",
                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 内嵌微光走势 Sparkline 曲线
            InlineSparklineChart(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
            )

            Spacer(modifier = Modifier.height(12.dp))
            HorizontalDivider(color = PrismBorder, thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(10.dp))

            // 今日与本周两栏微数据
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "今日 ", style = MaterialTheme.typography.bodySmall, color = PrismTextSecondary)
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", todayAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrismBlack
                    )
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(text = "本周 ", style = MaterialTheme.typography.bodySmall, color = PrismTextSecondary)
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", weekAmount),
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = PrismBlack
                    )
                }
            }
        }
    }
}

/**
 * 内嵌微走势折线 Sparkline
 */
@Composable
private fun InlineSparklineChart(modifier: Modifier = Modifier) {
    val progress = remember { Animatable(0f) }
    LaunchedEffect(Unit) {
        progress.animateTo(1f, animationSpec = tween(700, easing = FastOutSlowInEasing))
    }

    Canvas(modifier = modifier) {
        val width = size.width
        val height = size.height

        val dummyPoints = listOf(
            Offset(0f, height * 0.7f),
            Offset(width * 0.18f, height * 0.5f),
            Offset(width * 0.35f, height * 0.8f),
            Offset(width * 0.55f, height * 0.35f),
            Offset(width * 0.75f, height * 0.6f),
            Offset(width * 0.95f, height * 0.2f)
        )

        val strokePath = Path()
        val fillPath = Path()

        strokePath.moveTo(dummyPoints.first().x, dummyPoints.first().y)
        fillPath.moveTo(dummyPoints.first().x, height)
        fillPath.lineTo(dummyPoints.first().x, dummyPoints.first().y)

        for (i in 0 until dummyPoints.size - 1) {
            val p0 = dummyPoints[i]
            val p1 = dummyPoints[i + 1]
            val controlX = (p0.x + p1.x) / 2
            strokePath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
            fillPath.cubicTo(controlX, p0.y, controlX, p1.y, p1.x, p1.y)
        }

        fillPath.lineTo(dummyPoints.last().x, height)
        fillPath.close()

        drawPath(
            path = fillPath,
            brush = Brush.verticalGradient(
                colors = listOf(PrismLavender.copy(alpha = 0.15f * progress.value), Color.Transparent),
                startY = 0f,
                endY = height
            )
        )

        drawPath(
            path = strokePath,
            brush = PrismLuminousBrush,
            style = Stroke(width = 2.dp.toPx(), cap = StrokeCap.Round)
        )

        // 终点发光点
        val lastPt = dummyPoints.last()
        drawCircle(
            color = PrismLavender.copy(alpha = 0.3f),
            radius = 6.dp.toPx(),
            center = lastPt
        )
        drawCircle(
            color = PrismLavender,
            radius = 3.5.dp.toPx(),
            center = lastPt
        )
    }
}

/**
 * 4 组快捷操作微胶囊 (参考原型 4 个圆形/方形动作)
 */
@Composable
private fun LightcoreQuickActions(onAddClick: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        QuickActionButton(icon = Icons.Default.Add, label = "记一笔", isPrimary = true, onClick = onAddClick)
        QuickActionButton(icon = Icons.Default.Today, label = "今日明细", onClick = {})
        QuickActionButton(icon = Icons.Default.AutoGraph, label = "走势分析", onClick = {})
        QuickActionButton(icon = Icons.Default.Category, label = "分类管理", onClick = {})
    }
}

@Composable
private fun QuickActionButton(
    icon: ImageVector,
    label: String,
    isPrimary: Boolean = false,
    onClick: () -> Unit
) {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.clickable(onClick = onClick)
    ) {
        Box(
            modifier = Modifier
                .size(46.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isPrimary) PrismBlack else PrismSlate)
                .border(1.dp, PrismBorder, RoundedCornerShape(14.dp)),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = if (isPrimary) Color.White else PrismBlack,
                modifier = Modifier.size(20.dp)
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Medium,
            color = PrismTextSecondary
        )
    }
}

/**
 * 日期分隔头
 */
@Composable
private fun LightcoreDateHeader(
    date: LocalDate,
    dayTotal: Double
) {
    val today = remember { LocalDate.now() }
    val yesterday = remember { today.minusDays(1) }

    val dateTitle = when (date) {
        today -> "今天"
        yesterday -> "昨天"
        else -> "${date.monthValue}月${date.dayOfMonth}日"
    }
    val weekday = remember(date) { DateTimeUtils.formatWeekday(date) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 4.dp, start = 2.dp, end = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dateTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = PrismBlack
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodySmall,
                color = PrismTextSecondary
            )
        }

        Text(
            text = "当日 ¥ " + String.format(Locale.US, "%.2f", dayTotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = PrismTextSecondary
        )
    }
}

/**
 * 流水单条微光卡片条目 (参考原型 Recent Activity 项)
 */
@Composable
private fun LightcoreRecordItem(
    item: RecordWithCategory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = remember(item.record.timestamp) {
        DateTimeUtils.toLocalDateTime(item.record.timestamp).format(DateTimeUtils.TIME_FORMATTER)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, PrismBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(containerColor = PrismWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 12.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                CategoryIconBadge(
                    iconName = item.displayIconName,
                    colorHex = item.displayColorHex,
                    size = 38.dp,
                    iconSize = 18.dp,
                    cornerRadius = 10.dp
                )

                Spacer(modifier = Modifier.width(12.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.displayCategoryName,
                            style = MaterialTheme.typography.bodyLarge,
                            fontWeight = FontWeight.SemiBold,
                            color = PrismBlack
                        )

                        if (item.displaySubCategoryName != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(PrismSlate)
                                    .border(0.8.dp, PrismBorder, RoundedCornerShape(4.dp))
                                    .padding(horizontal = 4.dp, vertical = 1.dp)
                            ) {
                                Text(
                                    text = item.displaySubCategoryName!!,
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = Color(item.displayColorHex),
                                    fontWeight = FontWeight.Medium
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(2.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = PrismTextTertiary
                        )
                        if (item.record.note.isNotBlank()) {
                            Text(
                                text = " · ${item.record.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = PrismTextSecondary,
                                maxLines = 1
                            )
                        }
                    }
                }
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = "- ¥ " + String.format(Locale.US, "%.2f", item.record.amount),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrismBlack
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        tint = PrismTextTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    Card(
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, PrismBorder, RoundedCornerShape(16.dp)),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = PrismWhite),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Box(
            modifier = Modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.ReceiptLong,
                    contentDescription = null,
                    tint = PrismTextTertiary,
                    modifier = Modifier.size(42.dp)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "暂无近期账单",
                    style = MaterialTheme.typography.titleSmall,
                    color = PrismBlack,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "点击快捷按钮开启 Lightcore 极简记账",
                    style = MaterialTheme.typography.bodySmall,
                    color = PrismTextSecondary
                )
            }
        }
    }
}
