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
        containerColor = TomoePaperBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingRecord = null
                    isBottomSheetOpen = true
                },
                containerColor = PillarCulture,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .shadow(6.dp, CircleShape, spotColor = PillarCulture.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = "记一笔",
                        modifier = Modifier.size(20.dp)
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
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // 1. 月度总览手账大卡片
            item(key = "home_cover_card", contentType = "header") {
                HobonichiCoverCard(
                    monthAmount = uiState.monthExpense,
                    todayAmount = uiState.todayExpense,
                    weekAmount = uiState.weekExpense,
                    totalCount = uiState.totalRecordCount
                )
            }

            // 2. 流水明细手账内页标题
            item(key = "home_section_title", contentType = "header") {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 2.dp, end = 2.dp, top = 4.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        WashiTapeTab(
                            title = "每日流水",
                            tapeColor = PillarCulture.copy(alpha = 0.15f),
                            textColor = PillarCulture
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "手账明细",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SumiDark
                        )
                    }
                    Text(
                        text = "本月累计 ${uiState.totalRecordCount} 笔",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiMedium
                    )
                }
            }

            if (uiState.groupedDays.isEmpty()) {
                item(key = "empty_state") {
                    HobonichiEmptyStateCard(
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
                        HobonichiDateBanner(
                            date = dayGroup.date,
                            dayTotal = dayGroup.totalAmount
                        )
                    }

                    items(
                        items = dayGroup.records,
                        key = { it.record.id },
                        contentType = { "record_item" }
                    ) { recordItem ->
                        HobonichiRecordCard(
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
            title = { Text(text = "划去此条记账？", fontWeight = FontWeight.Bold, color = SumiDark) },
            text = {
                Text(
                    text = "即将划去「${recordItem.displayCategoryName}」支出 ¥ ${
                        String.format(Locale.US, "%.2f", recordItem.record.amount)
                    }，此手账记录将从本地清除。",
                    color = SumiMedium
                )
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.deleteRecord(recordItem.record.id)
                        recordToDelete = null
                    }
                ) {
                    Text(text = "确认划去", color = HankoRed, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { recordToDelete = null }) {
                    Text(text = "保留", color = SumiMedium)
                }
            }
        )
    }
}

/**
 * 封面月度总览大卡片
 */
@Composable
private fun HobonichiCoverCard(
    monthAmount: Double,
    todayAmount: Double,
    weekAmount: Double,
    totalCount: Int
) {
    val now = remember { LocalDate.now() }
    val monthTitle = remember { "${now.year} 年 ${now.monthValue} 月" }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TomoePaperPage)
            .border(1.dp, TomoeBorder, RoundedCornerShape(16.dp))
            .padding(18.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    WashiTapeTab(
                        title = "月度总览",
                        tapeColor = HankoRed.copy(alpha = 0.12f),
                        textColor = HankoRed
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "${monthTitle} · 支出总览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SumiDark
                    )
                }

                HankoStampBadge(
                    text = if (monthAmount > 0) "已" else "初",
                    size = 28.dp,
                    angle = -6f
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 本月总支出
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge.copy(fontWeight = FontWeight.Bold),
                    color = PillarCulture,
                    modifier = Modifier.padding(end = 6.dp, bottom = 2.dp)
                )
                Text(
                    text = String.format(Locale.US, "%.2f", monthAmount),
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = 36.sp,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = SumiDark
                )
            }

            Spacer(modifier = Modifier.height(14.dp))
            HorizontalDivider(color = TomoeBorder.copy(alpha = 0.6f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(12.dp))

            // 今日支出 & 本周支出
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "今日支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", todayAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SumiDark
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "本周支出",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiMedium
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "¥ " + String.format(Locale.US, "%.2f", weekAmount),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SumiDark
                    )
                }
            }
        }
    }
}

/**
 * 手账日期标签横幅
 */
@Composable
private fun HobonichiDateBanner(
    date: LocalDate,
    dayTotal: Double
) {
    val today = remember { LocalDate.now() }
    val yesterday = remember { today.minusDays(1) }

    val dateTitle = remember(date) {
        when (date) {
            today -> "今天 · ${date.monthValue}月${date.dayOfMonth}日"
            yesterday -> "昨天 · ${date.monthValue}月${date.dayOfMonth}日"
            else -> "${date.monthValue}月${date.dayOfMonth}日"
        }
    }
    val weekday = remember(date) { DateTimeUtils.formatWeekday(date) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 8.dp, bottom = 2.dp, start = 2.dp, end = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            WashiTapeTab(
                title = weekday,
                tapeColor = if (date == today) PillarCulture.copy(alpha = 0.2f) else TomoeBorder.copy(alpha = 0.5f),
                textColor = if (date == today) PillarCulture else SumiMedium
            )
            Spacer(modifier = Modifier.width(8.dp))
            Text(
                text = dateTitle,
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = SumiDark
            )
        }

        Text(
            text = "当日 ¥ " + String.format(Locale.US, "%.2f", dayTotal),
            style = MaterialTheme.typography.bodySmall,
            fontWeight = FontWeight.Bold,
            color = SumiMedium
        )
    }
}

/**
 * 单条记账便签小卡 (高性能优化版本)
 */
@Composable
private fun HobonichiRecordCard(
    item: RecordWithCategory,
    onClick: () -> Unit,
    onDelete: () -> Unit
) {
    val timeStr = remember(item.record.timestamp) {
        DateTimeUtils.toLocalDateTime(item.record.timestamp).format(DateTimeUtils.TIME_FORMATTER)
    }
    val pillar = item.resolvedPillar
    val cleanNote = item.cleanNote

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(TomoePaperPage)
            .border(0.8.dp, TomoeBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 14.dp, vertical = 12.dp)
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
                            fontWeight = FontWeight.Bold,
                            color = SumiDark
                        )

                        Spacer(modifier = Modifier.width(6.dp))

                        WashiTapeTab(
                            title = pillar.title,
                            tapeColor = pillar.containerColor,
                            textColor = pillar.color
                        )

                        if (item.displaySubCategoryName != null) {
                            Spacer(modifier = Modifier.width(4.dp))
                            Text(
                                text = "· ${item.displaySubCategoryName}",
                                style = MaterialTheme.typography.labelSmall,
                                color = SumiMedium
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(3.dp))

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(
                            text = timeStr,
                            style = MaterialTheme.typography.bodySmall,
                            color = SumiLight
                        )
                        if (cleanNote.isNotBlank()) {
                            Text(
                                text = "  ${cleanNote}",
                                style = MaterialTheme.typography.bodySmall,
                                color = SumiMedium,
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
                    color = SumiDark
                )

                Spacer(modifier = Modifier.width(4.dp))

                IconButton(
                    onClick = onDelete,
                    modifier = Modifier.size(26.dp)
                ) {
                    Icon(
                        imageVector = Icons.Default.DeleteOutline,
                        contentDescription = "划去",
                        tint = SumiLight,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }
    }
}

/**
 * 空状态插画便签
 */
@Composable
private fun HobonichiEmptyStateCard(modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(TomoePaperPage)
            .border(0.8.dp, TomoeBorder, RoundedCornerShape(16.dp)),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            HankoStampBadge(text = "空", size = 44.dp, angle = -10f)
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                text = "今日尚未记账，静候落笔",
                style = MaterialTheme.typography.titleSmall,
                color = SumiDark,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = "“生活如流水，每一笔都是时间的印记。”",
                style = MaterialTheme.typography.bodySmall,
                color = SumiMedium
            )
        }
    }
}
