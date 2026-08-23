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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.ui.components.AnimatedAmountText
import com.localbill.recording.ui.components.CalculatorBottomSheet
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.theme.PrimaryGreen
import com.localbill.recording.ui.theme.PrimaryGreenDark
import com.localbill.recording.ui.theme.PrimaryGreenLight
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    isBottomSheetOpen = true
                },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Add,
                    contentDescription = "记一笔",
                    modifier = Modifier.size(28.dp)
                )
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // 1. 顶部 Header 与 统计总览大卡片
            OverviewHeaderCard(
                todayAmount = uiState.todayExpense,
                weekAmount = uiState.weekExpense,
                monthAmount = uiState.monthExpense
            )

            // 2. 流水明细列表
            if (uiState.groupedDays.isEmpty()) {
                EmptyStateView(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                )
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 6.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    uiState.groupedDays.forEach { dayGroup ->
                        item(
                            key = "header_${dayGroup.date}",
                            contentType = "date_header"
                        ) {
                            DateGroupHeader(
                                date = dayGroup.date,
                                dayTotal = dayGroup.totalAmount
                            )
                        }

                        items(
                            items = dayGroup.records,
                            key = { it.record.id },
                            contentType = { "record_item" }
                        ) { recordItem ->
                            RecordListItem(
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
                    item {
                        Spacer(modifier = Modifier.height(72.dp))
                    }
                }
            }
        }
    }

    // 记账/编辑底部抽屉
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

    // 删除确认对话框
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
 * 顶部总览卡片
 */
@Composable
private fun OverviewHeaderCard(
    todayAmount: Double,
    weekAmount: Double,
    monthAmount: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 10.dp)
            .clip(RoundedCornerShape(22.dp))
            .background(
                Brush.linearGradient(
                    colors = listOf(PrimaryGreenDark, PrimaryGreen, PrimaryGreenLight)
                )
            )
            .padding(18.dp)
    ) {
        Column {
            Text(
                text = "本月总支出",
                style = MaterialTheme.typography.bodyMedium,
                color = Color.White.copy(alpha = 0.85f)
            )
            Spacer(modifier = Modifier.height(2.dp))
            AnimatedAmountText(
                amount = monthAmount,
                style = MaterialTheme.typography.displayLarge.copy(fontSize = 30.sp, color = Color.White),
                color = Color.White
            )

            Spacer(modifier = Modifier.height(14.dp))

            // 今日与本周两栏对比
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = "今日支出",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", todayAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "本周支出",
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.8f)
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", weekAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            }
        }
    }
}

/**
 * 分组日期表头
 */
@Composable
private fun DateGroupHeader(
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
            .padding(horizontal = 4.dp, vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dateTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        Text(
            text = "支出 ¥ " + String.format(Locale.US, "%.2f", dayTotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

/**
 * 账单单条卡片
 */
@Composable
private fun RecordListItem(
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
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
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
                    iconSize = 20.dp,
                    cornerRadius = 10.dp
                )

                Spacer(modifier = Modifier.width(10.dp))

                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = item.displayCategoryName,
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold,
                            color = MaterialTheme.colorScheme.onSurface
                        )

                        if (item.displaySubCategoryName != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(
                                        Color(item.displayColorHex).copy(alpha = 0.15f)
                                    )
                                    .padding(horizontal = 5.dp, vertical = 1.dp)
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
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        if (item.record.note.isNotBlank()) {
                            Text(
                                text = " · ${item.record.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
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
                    color = MaterialTheme.colorScheme.onSurface
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(30.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        tint = MaterialTheme.colorScheme.outline,
                        modifier = Modifier.size(17.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun EmptyStateView(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier,
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.ReceiptLong,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.outline,
                modifier = Modifier.size(56.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "还没有记账记录",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                fontWeight = FontWeight.Medium
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击右下角按钮，开启你的极简记账生活",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
            )
        }
    }
}
