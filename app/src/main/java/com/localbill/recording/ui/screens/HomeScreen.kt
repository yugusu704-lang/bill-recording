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
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Info
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
import com.localbill.recording.ui.components.CalculatorBottomSheet
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.components.MinimalCalendarSheet
import com.localbill.recording.ui.theme.DeepGreen
import com.localbill.recording.ui.theme.TextDark
import com.localbill.recording.ui.theme.TextSecondary
import com.localbill.recording.ui.theme.TextTertiary
import com.localbill.recording.ui.theme.WarmBorder
import com.localbill.recording.ui.theme.WarmBone
import com.localbill.recording.ui.theme.WarmSurface
import com.localbill.recording.ui.viewmodel.DayGroupItem
import com.localbill.recording.ui.viewmodel.HomeViewModel
import com.localbill.recording.util.DateTimeUtils
import com.localbill.recording.util.formatAmount
import java.time.LocalDate
import java.time.LocalTime

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    modifier: Modifier = Modifier,
    initialAddRecord: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedDate = uiState.selectedDate

    var isCalendarOpen by remember { mutableStateOf(false) }
    var isBottomSheetOpen by remember { mutableStateOf(initialAddRecord) }
    var editingRecord by remember { mutableStateOf<RecordWithCategory?>(null) }
    var recordToDelete by remember { mutableStateOf<RecordWithCategory?>(null) }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WarmBone,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    isBottomSheetOpen = true
                },
                containerColor = DeepGreen,
                contentColor = Color.White,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "记一笔",
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "记一笔",
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item(key = "home_header", contentType = "header") {
                HomeHeader(
                    selectedDate = selectedDate,
                    onOpenCalendar = { isCalendarOpen = true },
                    onBackToMonth = { viewModel.clearSelectedDate() }
                )
            }

            item(key = "home_summary", contentType = "summary") {
                if (selectedDate == null) {
                    MonthSummaryCard(
                        monthAmount = uiState.monthExpense,
                        todayAmount = uiState.todayExpense,
                        weekAmount = uiState.weekExpense
                    )
                } else {
                    DaySummaryCard(
                        date = selectedDate,
                        dayAmount = uiState.selectedDateExpense,
                        dayCount = uiState.selectedDateCount
                    )
                }
            }

            item(key = "home_section_title", contentType = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = if (selectedDate == null) "本月明细" else "当日明细",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    Text(
                        text = "${uiState.totalRecordCount} 笔",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondary
                    )
                }
            }

            if (uiState.groupedDays.isEmpty()) {
                item(key = "home_empty_state") {
                    ModernEmptyState(
                        text = if (selectedDate == null) "本月还没有账单，记下第一笔吧" else "这一天没有消费记录",
                        onAction = {
                            editingRecord = null
                            isBottomSheetOpen = true
                        }
                    )
                }
            } else {
                uiState.groupedDays.forEach { dayGroup ->
                    item(key = "header_${dayGroup.date}", contentType = "date_header") {
                        DateBanner(
                            date = dayGroup.date,
                            dayTotal = dayGroup.totalAmount
                        )
                    }

                    items(
                        items = dayGroup.records,
                        key = { it.record.id },
                        contentType = { "record_item" }
                    ) { recordItem ->
                        RecordCard(
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

            item(key = "bottom_spacer") {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (isCalendarOpen) {
        MinimalCalendarSheet(
            initialSelectedDate = selectedDate ?: LocalDate.now(),
            datesWithRecords = uiState.datesWithRecords,
            onDismiss = { isCalendarOpen = false },
            onDateSelected = { date ->
                viewModel.selectDate(date)
            }
        )
    }

    if (isBottomSheetOpen) {
        CalculatorBottomSheet(
            allMainCategories = uiState.mainCategories,
            subCategoriesMap = uiState.subCategoriesMap,
            editingRecord = editingRecord,
            defaultTimestamp = selectedDate?.atTime(LocalTime.now())?.let {
                DateTimeUtils.toMillis(it)
            } ?: System.currentTimeMillis(),
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
            containerColor = WarmSurface,
            title = { Text(text = "删除这笔账单？", fontWeight = FontWeight.Bold, color = TextDark) },
            text = {
                Text(
                    text = "即将删除「${recordItem.displayCategoryName}」支出 ¥ ${formatAmount(recordItem.record.amount)}，此账单将从本地清除。",
                    color = TextSecondary
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(recordItem.record.id)
                        recordToDelete = null
                    }
                ) {
                    Text(text = "删除", color = Color(0xFF9F2F2D), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(text = "保留", color = TextSecondary)
                }
            }
        )
    }
}

@Composable
private fun HomeHeader(
    selectedDate: LocalDate?,
    onOpenCalendar: () -> Unit,
    onBackToMonth: () -> Unit
) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(
                    text = "账单",
                    style = MaterialTheme.typography.headlineMedium,
                    fontWeight = FontWeight.Bold,
                    color = TextDark
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = if (selectedDate == null) {
                        val today = LocalDate.now()
                        "${today.year} 年 ${today.monthValue} 月"
                    } else {
                        "${selectedDate.monthValue} 月 ${selectedDate.dayOfMonth} 日 · 单日账单"
                    },
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondary
                )
            }

            Box(
                modifier = Modifier
                    .size(40.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(WarmSurface)
                    .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
                    .clickable(onClick = onOpenCalendar),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.CalendarToday,
                    contentDescription = "打开日历",
                    tint = TextDark,
                    modifier = Modifier.size(20.dp)
                )
            }
        }

        if (selectedDate != null) {
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(
                onClick = onBackToMonth,
                contentPadding = PaddingValues(horizontal = 0.dp)
            ) {
                Text(
                    text = "回到本月",
                    color = DeepGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
private fun MonthSummaryCard(
    monthAmount: Double,
    todayAmount: Double,
    weekAmount: Double
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "本月支出",
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
                    text = formatAmount(monthAmount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = WarmBorder, thickness = 1.dp)
            Spacer(modifier = Modifier.height(12.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                SummaryMiniItem(label = "今日支出", amount = todayAmount)
                SummaryMiniItem(label = "本周支出", amount = weekAmount, alignEnd = true)
            }
        }
    }
}

@Composable
private fun DaySummaryCard(
    date: LocalDate,
    dayAmount: Double,
    dayCount: Int
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Text(
                text = "${date.monthValue} 月 ${date.dayOfMonth} 日支出",
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
                    text = formatAmount(dayAmount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 34.sp,
                        fontWeight = FontWeight.Bold
                    ),
                    color = TextDark
                )
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text(
                text = "${dayCount} 笔消费记录",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondary
            )
        }
    }
}

@Composable
private fun SummaryMiniItem(
    label: String,
    amount: Double,
    alignEnd: Boolean = false
) {
    Column(horizontalAlignment = if (alignEnd) Alignment.End else Alignment.Start) {
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
private fun DateBanner(
    date: LocalDate,
    dayTotal: Double
) {
    val today = LocalDate.now()
    val yesterday = today.minusDays(1)

    val title = when (date) {
        today -> "今天"
        yesterday -> "昨天"
        else -> "${date.monthValue}月${date.dayOfMonth}日"
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp, start = 2.dp, end = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = title,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = DateTimeUtils.formatWeekday(date),
                style = MaterialTheme.typography.bodySmall,
                color = TextTertiary
            )
        }
        Text(
            text = "¥ " + formatAmount(dayTotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = TextSecondary
        )
    }
}

@Composable
private fun RecordCard(
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
            .clip(RoundedCornerShape(12.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp),
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
                        fontWeight = FontWeight.Bold,
                        color = TextDark
                    )
                    if (item.displaySubCategoryName != null) {
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "· ${item.displaySubCategoryName}",
                            style = MaterialTheme.typography.labelSmall,
                            color = TextSecondary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(3.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = timeStr,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextTertiary
                    )
                    if (item.cleanNote.isNotBlank()) {
                        Text(
                            text = "  ${item.cleanNote}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondary,
                            maxLines = 1
                        )
                    }
                }
            }
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                text = "- ¥ " + formatAmount(item.record.amount),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = TextDark
            )

            Spacer(modifier = Modifier.width(4.dp))

            IconButton(
                onClick = onDelete,
                modifier = Modifier.size(26.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.DeleteOutline,
                    contentDescription = "删除",
                    tint = TextTertiary,
                    modifier = Modifier.size(16.dp)
                )
            }
        }
    }
}

@Composable
private fun ModernEmptyState(
    text: String,
    onAction: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(180.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(WarmSurface)
            .border(1.dp, WarmBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Info,
                contentDescription = null,
                tint = TextTertiary,
                modifier = Modifier.size(32.dp)
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = text,
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondary
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextButton(onClick = onAction) {
                Text(
                    text = "记一笔",
                    color = DeepGreen,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}





