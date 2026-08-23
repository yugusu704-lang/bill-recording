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
import androidx.compose.material3.FloatingActionButton
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
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.ui.components.AnimatedAmountText
import com.localbill.recording.ui.components.CalculatorBottomSheet
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.theme.InkPrimary
import com.localbill.recording.ui.theme.InkQuaternary
import com.localbill.recording.ui.theme.InkSecondary
import com.localbill.recording.ui.theme.InkTertiary
import com.localbill.recording.ui.theme.PaperBorder

import com.localbill.recording.ui.viewmodel.HomeViewModel
import com.localbill.recording.util.DateTimeUtils
import java.time.LocalDate
import java.time.YearMonth
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // 1. 杂志排版式顶部财务概览 (Editorial Header)
            EditorialHeader(
                todayAmount = uiState.todayExpense,
                weekAmount = uiState.weekExpense,
                monthAmount = uiState.monthExpense,
                totalCount = uiState.totalRecordCount
            )

            HorizontalDivider(
                color = PaperBorder,
                thickness = 0.6.dp,
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 8.dp)
            )

            // 2. 流水明细列表 (无生硬方框，纯粹通透的杂志列表)
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
                    contentPadding = PaddingValues(horizontal = 20.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    uiState.groupedDays.forEach { dayGroup ->
                        item(
                            key = "header_${dayGroup.date}",
                            contentType = "date_header"
                        ) {
                            EditorialDateHeader(
                                date = dayGroup.date,
                                dayTotal = dayGroup.totalAmount
                            )
                        }

                        items(
                            items = dayGroup.records,
                            key = { it.record.id },
                            contentType = { "record_item" }
                        ) { recordItem ->
                            EditorialRecordItem(
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

    // 记账抽屉
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
 * 杂志出版物风格的顶部排版
 */
@Composable
private fun EditorialHeader(
    todayAmount: Double,
    weekAmount: Double,
    monthAmount: Double,
    totalCount: Int
) {
    val now = LocalDate.now()
    val monthTitle = "${now.year} 年 ${now.monthValue} 月"

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 24.dp, end = 24.dp, top = 20.dp, bottom = 12.dp)
    ) {
        // 顶部小标题
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = monthTitle,
                style = MaterialTheme.typography.labelSmall,
                color = InkSecondary,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Text(
                text = "MONTHLY LEDGER",
                style = MaterialTheme.typography.labelSmall,
                color = InkTertiary,
                letterSpacing = 1.5.sp
            )
        }

        Spacer(modifier = Modifier.height(8.dp))

        // 大字号当月总支出
        Row(
            verticalAlignment = Alignment.Bottom,
            modifier = Modifier.fillMaxWidth()
        ) {

            Text(
                text = "¥",
                style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                color = InkPrimary,
                modifier = Modifier.padding(end = 4.dp)
            )
            Text(
                text = String.format(Locale.US, "%.2f", monthAmount),
                style = MaterialTheme.typography.displayLarge.copy(
                    fontSize = 38.sp,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = (-1).sp
                ),
                color = InkPrimary
            )
        }

        Spacer(modifier = Modifier.height(14.dp))

        // 简练的今日与本周晴雨表行
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "今日 ", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Text(
                    text = "¥ " + String.format(Locale.US, "%.2f", todayAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
            }

            Text(text = "·", color = InkTertiary)

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = "本周 ", style = MaterialTheme.typography.bodySmall, color = InkSecondary)
                Text(
                    text = "¥ " + String.format(Locale.US, "%.2f", weekAmount),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = InkPrimary
                )
            }

            Text(text = "·", color = InkTertiary)

            Text(
                text = "共 ${totalCount} 笔",
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
        }
    }
}

/**
 * 极简日期分隔线
 */
@Composable
private fun EditorialDateHeader(
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
            .padding(top = 16.dp, bottom = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = dateTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = InkPrimary
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodySmall,
                color = InkSecondary
            )
        }

        Text(
            text = "当日 ¥ " + String.format(Locale.US, "%.2f", dayTotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = InkSecondary
        )
    }
}

/**
 * 去卡片化极简流水条目
 */
@Composable
private fun EditorialRecordItem(
    item: RecordWithCategory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = remember(item.record.timestamp) {
        DateTimeUtils.toLocalDateTime(item.record.timestamp).format(DateTimeUtils.TIME_FORMATTER)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable(onClick = onClick)
            .padding(vertical = 10.dp, horizontal = 4.dp),
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
                size = 36.dp,
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
                        color = InkPrimary
                    )

                    if (item.displaySubCategoryName != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "/ ${item.displaySubCategoryName}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(2.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = InkTertiary
                    )
                    if (item.record.note.isNotBlank()) {
                        Text(
                            text = " · ${item.record.note}",
                            style = MaterialTheme.typography.bodySmall,
                            color = InkSecondary,
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
                color = InkPrimary
            )

            IconButton(
                onClick = onDelete,
                modifier = Modifier
                    .padding(start = 4.dp)
                    .size(28.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "删除",
                    tint = InkQuaternary,
                    modifier = Modifier.size(16.dp)
                )
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
                tint = InkQuaternary,
                modifier = Modifier.size(48.dp)
            )
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "空白账页",
                style = MaterialTheme.typography.titleMedium,
                color = InkSecondary,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击右下角按钮，记录生活中的每一笔开销",
                style = MaterialTheme.typography.bodySmall,
                color = InkTertiary
            )
        }
    }
}
