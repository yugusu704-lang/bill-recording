package com.localbill.recording.ui.components

import android.app.DatePickerDialog
import android.widget.Toast
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.ui.theme.PrismBlack
import com.localbill.recording.ui.theme.PrismBorder
import com.localbill.recording.ui.theme.PrismSlate
import com.localbill.recording.ui.theme.PrismSpectralBrush
import com.localbill.recording.ui.theme.PrismTextSecondary
import com.localbill.recording.ui.theme.PrismTextTertiary
import com.localbill.recording.ui.theme.PrismWhite
import com.localbill.recording.util.DateTimeUtils
import java.time.LocalDate
import java.time.LocalDateTime
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorBottomSheet(
    allMainCategories: List<CategoryEntity>,
    subCategoriesMap: Map<Long, List<CategoryEntity>>,
    editingRecord: RecordWithCategory? = null,
    onDismiss: () -> Unit,
    onSaveRecord: (amount: Double, categoryId: Long, subCategoryId: Long?, note: String, timestamp: Long, imagePath: String?) -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val context = LocalContext.current
    val haptic = LocalHapticFeedback.current

    var expression by remember {
        mutableStateOf(
            if (editingRecord != null) String.format(Locale.US, "%.2f", editingRecord.record.amount) else "0"
        )
    }

    var selectedMainCategory by remember {
        mutableStateOf(
            editingRecord?.category ?: allMainCategories.firstOrNull()
        )
    }

    var selectedSubCategory by remember {
        mutableStateOf(editingRecord?.subCategory)
    }

    var noteText by remember { mutableStateOf(editingRecord?.record?.note ?: "") }
    var selectedTimestamp by remember {
        mutableLongStateOf(editingRecord?.record?.timestamp ?: System.currentTimeMillis())
    }

    LaunchedEffect(allMainCategories) {
        if (selectedMainCategory == null && allMainCategories.isNotEmpty()) {
            selectedMainCategory = allMainCategories.first()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = PrismWhite,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        dragHandle = null
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(bottom = 8.dp)
        ) {
            // 1. 顶部棱镜流光指示线
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .background(PrismSpectralBrush)
            )

            // 2. 标题栏
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 18.dp, vertical = 10.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingRecord != null) "编辑账单" else "记一笔",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = PrismBlack
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "关闭", tint = PrismTextSecondary, modifier = Modifier.size(18.dp))
                }
            }

            // 3. 大字号金额展示
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    text = "¥",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PrismTextSecondary,
                    modifier = Modifier.padding(end = 6.dp, bottom = 4.dp)
                )
                Text(
                    text = expression,
                    style = MaterialTheme.typography.displayLarge.copy(
                        fontSize = if (expression.length > 8) 30.sp else 38.sp,
                        fontWeight = FontWeight.ExtraBold,
                        letterSpacing = (-0.5).sp
                    ),
                    color = PrismBlack,
                    maxLines = 1
                )
            }

            // 4. 两级分类选择器
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 6.dp)
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    allMainCategories.forEach { mainCat ->
                        val isSelected = selectedMainCategory?.id == mainCat.id
                        val catColor = Color(mainCat.colorHex)

                        Row(
                            modifier = Modifier
                                .clip(RoundedCornerShape(12.dp))
                                .background(if (isSelected) PrismSlate else Color.Transparent)
                                .border(
                                    width = 1.dp,
                                    color = if (isSelected) catColor else PrismBorder,
                                    shape = RoundedCornerShape(12.dp)
                                )
                                .clickable {
                                    haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                    if (selectedMainCategory?.id != mainCat.id) {
                                        selectedMainCategory = mainCat
                                        selectedSubCategory = null
                                    }
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            CategoryIconBadge(
                                iconName = mainCat.iconName,
                                colorHex = mainCat.colorHex,
                                size = 26.dp,
                                iconSize = 14.dp,
                                cornerRadius = 6.dp
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mainCat.name,
                                style = MaterialTheme.typography.labelLarge,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) PrismBlack else PrismTextSecondary
                            )
                        }
                    }
                }

                // 子分类标签
                val currentSubList = selectedMainCategory?.let { subCategoriesMap[it.id] } ?: emptyList()
                AnimatedVisibility(
                    visible = currentSubList.isNotEmpty(),
                    enter = expandVertically() + fadeIn(),
                    exit = shrinkVertically() + fadeOut()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(rememberScrollState())
                            .padding(start = 16.dp, end = 16.dp, top = 6.dp),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentSubList.forEach { subCat ->
                            val isSubSelected = selectedSubCategory?.id == subCat.id
                            val subColor = Color(subCat.colorHex)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSubSelected) subColor else PrismSlate)
                                    .border(1.dp, if (isSubSelected) subColor else PrismBorder, RoundedCornerShape(8.dp))
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedSubCategory = if (isSubSelected) null else subCat
                                    }
                                    .padding(horizontal = 10.dp, vertical = 4.dp)
                            ) {
                                Text(
                                    text = subCat.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Medium,
                                    color = if (isSubSelected) Color.White else PrismTextSecondary
                                )
                            }
                        }
                    }
                }
            }

            // 5. 日期与备注行
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                val ldt = DateTimeUtils.toLocalDateTime(selectedTimestamp)
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(10.dp))
                        .background(PrismSlate)
                        .border(1.dp, PrismBorder, RoundedCornerShape(10.dp))
                        .clickable {
                            val currentDate = ldt.toLocalDate()
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    val newLdt = LocalDateTime.of(newDate, ldt.toLocalTime())
                                    selectedTimestamp = DateTimeUtils.toMillis(newLdt)
                                },
                                currentDate.year,
                                currentDate.monthValue - 1,
                                currentDate.dayOfMonth
                            ).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.CalendarToday,
                        contentDescription = "日期",
                        tint = PrismBlack,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "${ldt.monthValue}/${ldt.dayOfMonth}",
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        color = PrismBlack
                    )
                }

                OutlinedTextField(
                    value = noteText,
                    onValueChange = { noteText = it },
                    placeholder = {
                        Text(text = "添加备注...", style = MaterialTheme.typography.bodySmall, color = PrismTextTertiary)
                    },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyMedium,
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = null,
                            tint = PrismTextSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    },
                    modifier = Modifier
                        .weight(1f)
                        .height(44.dp),
                    shape = RoundedCornerShape(10.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = PrismBlack,
                        unfocusedBorderColor = PrismBorder,
                        focusedContainerColor = PrismSlate,
                        unfocusedContainerColor = PrismSlate
                    )
                )
            }

            // 6. Lightcore 棱镜流光数字键盘
            LightcoreTactileKeyboard(
                expression = expression,
                haptic = haptic,
                onExpressionChange = { expression = it },
                onComplete = {
                    val finalAmount = evaluateExpression(expression)
                    if (finalAmount == null || finalAmount <= 0.0) {
                        Toast.makeText(context, "请输入有效的支出金额", Toast.LENGTH_SHORT).show()
                        return@LightcoreTactileKeyboard
                    }

                    val mainCat = selectedMainCategory
                    if (mainCat == null) {
                        Toast.makeText(context, "请选择支出分类", Toast.LENGTH_SHORT).show()
                        return@LightcoreTactileKeyboard
                    }

                    onSaveRecord(
                        finalAmount,
                        mainCat.id,
                        selectedSubCategory?.id,
                        noteText,
                        selectedTimestamp,
                        editingRecord?.record?.imagePath
                    )
                    onDismiss()
                }
            )
        }
    }
}

@Composable
private fun LightcoreTactileKeyboard(
    expression: String,
    haptic: HapticFeedback,
    onExpressionChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    val keys = remember {
        listOf(
            listOf("7", "8", "9", "+"),
            listOf("4", "5", "6", "-"),
            listOf("1", "2", "3", "C"),
            listOf(".", "0", "⌫", "完成")
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        keys.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                rowKeys.forEach { key ->
                    LightcoreKeypadButton(
                        key = key,
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp),
                        onClick = {
                            haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                            handleKeyPress(key, expression, onExpressionChange, onComplete)
                        }
                    )
                }
            }
        }
    }
}

@Composable
private fun LightcoreKeypadButton(
    key: String,
    modifier: Modifier = Modifier,
    onClick: () -> Unit
) {
    val isActionKey = key == "完成"
    val isOpKey = key in listOf("+", "-", "C", "⌫")

    val bgModifier = if (isActionKey) {
        Modifier.background(PrismSpectralBrush)
    } else {
        Modifier
            .background(if (isOpKey) PrismSlate else PrismWhite)
            .border(1.dp, PrismBorder, RoundedCornerShape(10.dp))
    }

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .then(bgModifier)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (key == "⌫") {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.Backspace,
                contentDescription = "删除",
                tint = PrismBlack,
                modifier = Modifier.size(18.dp)
            )
        } else {
            Text(
                text = key,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = if (isActionKey || isOpKey) FontWeight.Bold else FontWeight.SemiBold,
                    fontSize = if (isActionKey) 15.sp else 19.sp
                ),
                color = if (isActionKey) Color.White else PrismBlack
            )
        }
    }
}

private fun handleKeyPress(
    key: String,
    current: String,
    onExpressionChange: (String) -> Unit,
    onComplete: () -> Unit
) {
    when (key) {
        "C" -> onExpressionChange("0")
        "⌫" -> {
            if (current.length <= 1 || current == "0") {
                onExpressionChange("0")
            } else {
                onExpressionChange(current.dropLast(1))
            }
        }
        "完成" -> onComplete()
        "+", "-" -> {
            val lastChar = current.lastOrNull()
            if (lastChar != null && (lastChar == '+' || lastChar == '-')) {
                onExpressionChange(current.dropLast(1) + key)
            } else {
                val evaluated = evaluateExpression(current)
                if (evaluated != null) {
                    val formatted = if (evaluated % 1.0 == 0.0) evaluated.toLong().toString() else String.format(Locale.US, "%.2f", evaluated)
                    onExpressionChange(formatted + key)
                } else {
                    onExpressionChange(current + key)
                }
            }
        }
        "." -> {
            val parts = current.split("+", "-")
            val currentSegment = parts.lastOrNull() ?: ""
            if (!currentSegment.contains(".")) {
                onExpressionChange(if (currentSegment.isEmpty()) current + "0." else current + ".")
            }
        }
        else -> {
            if (current == "0") {
                onExpressionChange(key)
            } else {
                val parts = current.split("+", "-")
                val currentSegment = parts.lastOrNull() ?: ""
                if (currentSegment.contains(".") && currentSegment.substringAfter(".").length >= 2) {
                    return
                }
                onExpressionChange(current + key)
            }
        }
    }
}

fun evaluateExpression(expr: String): Double? {
    try {
        var clean = expr.trim()
        if (clean.endsWith("+") || clean.endsWith("-")) {
            clean = clean.dropLast(1)
        }
        if (clean.isEmpty()) return null

        var total = 0.0
        var currentNum = ""
        var currentOp = '+'

        for (i in clean.indices) {
            val ch = clean[i]
            if (ch.isDigit() || ch == '.') {
                currentNum += ch
            }
            if (ch == '+' || ch == '-' || i == clean.length - 1) {
                if (currentNum.isNotEmpty()) {
                    val num = currentNum.toDoubleOrNull() ?: 0.0
                    if (currentOp == '+') {
                        total += num
                    } else if (currentOp == '-') {
                        total -= num
                    }
                    currentNum = ""
                }
                currentOp = ch
            }
        }
        return (Math.round(total * 100.0) / 100.0)
    } catch (e: Exception) {
        return null
    }
}
