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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ReceiptLong
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
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
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.ui.components.CalculatorBottomSheet
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.theme.MatchaPrimary
import com.localbill.recording.ui.theme.SakuraAccent
import com.localbill.recording.ui.theme.SakuraSoft
import com.localbill.recording.ui.theme.SumiInk
import com.localbill.recording.ui.theme.SumiSecondary
import com.localbill.recording.ui.theme.SumiTertiary
import com.localbill.recording.ui.theme.WashiBorder
import com.localbill.recording.ui.theme.WashiCardBg
import com.localbill.recording.ui.theme.WashiPaperBg
import com.localbill.recording.ui.theme.WashiPaperSubtle
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
        containerColor = WashiPaperBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    isBottomSheetOpen = true
                },
                containerColor = MatchaPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .shadow(8.dp, CircleShape, spotColor = MatchaPrimary.copy(alpha = 0.35f))
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
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            // 1. 和纸手账本月财务总览大卡片
            item {
                WashiFinancialOverviewCard(
                    monthAmount = uiState.monthExpense,
                    todayAmount = uiState.todayExpense,
                    weekAmount = uiState.weekExpense,
                    totalCount = uiState.totalRecordCount
                )
            }

            // 2. 流水明细列表标题
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "流水明细",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SumiInk
                    )
                    Text(
                        text = "本月共 ${uiState.totalRecordCount} 笔",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiSecondary
                    )
                }
            }

            if (uiState.groupedDays.isEmpty()) {
                item {
                    WashiEmptyStateCard(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                    )
                }
            } else {
                uiState.groupedDays.forEach { dayGroup ->
                    item(
                        key = "header_${dayGroup.date}",
                        contentType = "date_header"
                    ) {
                        WashiDateHeader(
                            date = dayGroup.date,
                            dayTotal = dayGroup.totalAmount
                        )
                    }

                    items(
                        items = dayGroup.records,
                        key = { it.record.id },
                        contentType = { "record_item" }
                    ) { recordItem ->
                        WashiRecordItem(
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
            containerColor = Color.White,
            title = { Text(text = "删除此账单？", fontWeight = FontWeight.Bold, color = SumiInk) },
            text = {
                Text(
                    text = "即将删除「${recordItem.displayCategoryName}」支出 ¥ ${
                        String.format(Locale.US, "%.2f", recordItem.record.amount)
                    }，此操作不可撤销。",
                    color = SumiSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(recordItem.record.id)
                        recordToDelete = null
                    }
                ) {
                    Text(text = "确认删除", color = SakuraAccent, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(text = "取消", color = SumiSecondary)
                }
            }
        )
    }
}

/**
 * 和纸财务总览大卡片 (温润和风宣纸质感)
 */
@Composable
private fun WashiFinancialOverviewCard(
    monthAmount: Double,
    todayAmount: Double,
    weekAmount: Double,
    totalCount: Int
) {
    val now = LocalDate.now()
    val monthTitle = "${now.year} 年 ${now.monthValue} 月"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .background(WashiCardBg)
            .border(0.8.dp, WashiBorder, RoundedCornerShape(18.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "🌸 ${monthTitle} · 支出总览",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MatchaPrimary
                    )
                }

                Text(
                    text = "记账 ${totalCount} 笔",
                    style = MaterialTheme.typography.labelSmall,
                    color = SumiSecondary
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // 本月总支出大字号
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = SumiInk,
                    modifier = Modifier.padding(end = 4.dp, bottom = 2.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.2f", monthAmount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = SumiInk
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = WashiBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 今日支出与本周支出对比
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(text = "今日支出", style = MaterialTheme.typography.bodySmall, color = SumiSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", todayAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SumiInk
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(text = "本周支出", style = MaterialTheme.typography.bodySmall, color = SumiSecondary)
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", weekAmount),
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = SumiInk
                    )
                }
            }
        }
    }
}

/**
 * 和风日期表头
 */
@Composable
private fun WashiDateHeader(
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
                color = SumiInk
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = weekday,
                style = MaterialTheme.typography.bodySmall,
                color = SumiSecondary
            )
        }

        Text(
            text = "当日 ¥ " + String.format(Locale.US, "%.2f", dayTotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Medium,
            color = SumiSecondary
        )
    }
}

/**
 * 和纸单条流水账单卡片
 */
@Composable
private fun WashiRecordItem(
    item: RecordWithCategory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = remember(item.record.timestamp) {
        DateTimeUtils.toLocalDateTime(item.record.timestamp).format(DateTimeUtils.TIME_FORMATTER)
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(WashiCardBg)
            .border(0.8.dp, WashiBorder, RoundedCornerShape(14.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 10.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
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
                            color = SumiInk
                        )

                        if (item.displaySubCategoryName != null) {
                            Spacer(modifier = Modifier.width(6.dp))
                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(4.dp))
                                    .background(Color(item.displayColorHex).copy(alpha = 0.1f))
                                    .border(0.8.dp, Color(item.displayColorHex).copy(alpha = 0.25f), RoundedCornerShape(4.dp))
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
                            color = SumiTertiary
                        )
                        if (item.record.note.isNotBlank()) {
                            Text(
                                text = " · ${item.record.note}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SumiSecondary,
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
                    color = SumiInk
                )

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "删除",
                        tint = SumiTertiary,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

@Composable
private fun WashiEmptyStateCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WashiCardBg)
            .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ReceiptLong,
                contentDescription = null,
                tint = MatchaPrimary.copy(alpha = 0.6f),
                modifier = Modifier.size(42.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "暂无近期账单",
                style = MaterialTheme.typography.titleSmall,
                color = SumiInk,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "点击右下角开启和风手账记账",
                style = MaterialTheme.typography.bodySmall,
                color = SumiSecondary
            )
        }
    }
}
