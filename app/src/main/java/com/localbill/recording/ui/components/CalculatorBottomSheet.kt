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
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Backspace
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.EditNote
import androidx.compose.material3.ExperimentalMaterial3Api
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.ui.theme.MatchaPrimary
import com.localbill.recording.ui.theme.SakuraAccent
import com.localbill.recording.ui.theme.SakuraSoft
import com.localbill.recording.ui.theme.SumiInk
import com.localbill.recording.ui.theme.SumiSecondary
import com.localbill.recording.ui.theme.SumiTertiary
import com.localbill.recording.ui.theme.WashiBorder
import com.localbill.recording.ui.theme.WashiPaperBg
import com.localbill.recording.ui.theme.WashiPaperSubtle
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

    var note by remember {
        mutableStateOf(editingRecord?.record?.note ?: "")
    }

    var selectedTimestamp by remember {
        mutableLongStateOf(editingRecord?.record?.timestamp ?: System.currentTimeMillis())
    }

    var isNoteInputVisible by remember {
        mutableStateOf(editingRecord?.record?.note?.isNotEmpty() == true)
    }

    LaunchedEffect(selectedMainCategory) {
        val currentSub = selectedSubCategory
        if (currentSub != null && currentSub.parentId != selectedMainCategory?.id) {
            selectedSubCategory = null
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = WashiPaperBg,
        dragHandle = null,
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
                .padding(horizontal = 18.dp, vertical = 12.dp)
        ) {
            // 顶部和风手账栏：标题与关闭
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = if (editingRecord == null) "🌸 记一笔 (和风手账)" else "✏️ 编辑记账",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = SumiInk
                )
                IconButton(onClick = onDismiss, modifier = Modifier.size(32.dp)) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "关闭",
                        tint = SumiSecondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 1. 金额展示与算式面板 (和纸纯白质感)
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(16.dp))
                    .background(Color.White)
                    .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp))
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = if (isExpression(expression)) "= 实时计算" else "金额",
                            style = MaterialTheme.typography.labelSmall,
                            color = SumiSecondary
                        )

                        // 关联选中的分类指示
                        selectedMainCategory?.let { main ->
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Box(
                                    modifier = Modifier
                                        .size(6.dp)
                                        .clip(RoundedCornerShape(3.dp))
                                        .background(Color(main.colorHex))
                                )
                                Spacer(modifier = Modifier.width(4.dp))
                                Text(
                                    text = if (selectedSubCategory != null) "${main.name} · ${selectedSubCategory?.name}" else main.name,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.SemiBold,
                                    color = Color(main.colorHex)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.Bottom
                    ) {
                        Text(
                            text = "¥",
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MatchaPrimary
                        )
                        Text(
                            text = expression,
                            style = MaterialTheme.typography.displayMedium.copy(fontSize = 32.sp),
                            fontWeight = FontWeight.Bold,
                            color = SumiInk
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 2. 主分类横向选择 (和纸胶囊)
            val mainScrollState = rememberScrollState()
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(mainScrollState),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                allMainCategories.forEach { mainCat ->
                    val isSelected = selectedMainCategory?.id == mainCat.id
                    val catColor = Color(mainCat.colorHex)

                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .background(if (isSelected) catColor.copy(alpha = 0.14f) else Color.White)
                            .border(
                                width = if (isSelected) 1.2.dp else 0.8.dp,
                                color = if (isSelected) catColor else WashiBorder,
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                selectedMainCategory = mainCat
                            }
                            .padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = CategoryIcons.getIcon(mainCat.iconName),
                                contentDescription = null,
                                tint = if (isSelected) catColor else SumiSecondary,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = mainCat.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal,
                                color = if (isSelected) catColor else SumiInk
                            )
                        }
                    }
                }
            }

            // 3. 子分类横向选择 (如果有)
            val currentSubList = selectedMainCategory?.let { subCategoriesMap[it.id] } ?: emptyList()
            AnimatedVisibility(
                visible = currentSubList.isNotEmpty(),
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    val subScrollState = rememberScrollState()
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .horizontalScroll(subScrollState),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        currentSubList.forEach { subCat ->
                            val isSubSelected = selectedSubCategory?.id == subCat.id
                            val subColor = Color(subCat.colorHex)

                            Box(
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(if (isSubSelected) subColor.copy(alpha = 0.15f) else WashiPaperSubtle)
                                    .border(
                                        width = if (isSubSelected) 1.dp else 0.dp,
                                        color = if (isSubSelected) subColor else Color.Transparent,
                                        shape = RoundedCornerShape(8.dp)
                                    )
                                    .clickable {
                                        haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                        selectedSubCategory = if (isSubSelected) null else subCat
                                    }
                                    .padding(horizontal = 10.dp, vertical = 6.dp)
                            ) {
                                Text(
                                    text = subCat.name,
                                    style = MaterialTheme.typography.bodySmall,
                                    fontWeight = if (isSubSelected) FontWeight.Bold else FontWeight.Normal,
                                    color = if (isSubSelected) subColor else SumiSecondary
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 4. 辅助工具栏：选择记账日期 & 展开备注
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // 日期选择胶囊
                val dateStr = DateTimeUtils.formatFriendlyDate(selectedTimestamp)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White)
                        .border(0.8.dp, WashiBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            val curDate = DateTimeUtils.toLocalDate(selectedTimestamp)
                            DatePickerDialog(
                                context,
                                { _, year, month, dayOfMonth ->
                                    val newDate = LocalDate.of(year, month + 1, dayOfMonth)
                                    val nowTime = LocalDateTime.now().toLocalTime()
                                    val newLdt = newDate.atTime(nowTime)
                                    selectedTimestamp = DateTimeUtils.toMillis(newLdt)
                                },
                                curDate.year,
                                curDate.monthValue - 1,
                                curDate.dayOfMonth
                            ).show()
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.CalendarToday,
                            contentDescription = "日期",
                            tint = MatchaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = dateStr,
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium,
                            color = SumiInk
                        )
                    }
                }

                // 备注开关按钮
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(if (isNoteInputVisible || note.isNotEmpty()) SakuraSoft else Color.White)
                        .border(0.8.dp, if (isNoteInputVisible || note.isNotEmpty()) SakuraAccent else WashiBorder, RoundedCornerShape(8.dp))
                        .clickable {
                            isNoteInputVisible = !isNoteInputVisible
                        }
                        .padding(horizontal = 10.dp, vertical = 6.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.EditNote,
                            contentDescription = "备注",
                            tint = if (isNoteInputVisible || note.isNotEmpty()) SakuraAccent else SumiSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = if (note.isNotEmpty()) note else "备注",
                            style = MaterialTheme.typography.bodySmall,
                            color = if (isNoteInputVisible || note.isNotEmpty()) SakuraAccent else SumiSecondary
                        )
                    }
                }
            }

            // 备注输入框展开
            AnimatedVisibility(
                visible = isNoteInputVisible,
                enter = expandVertically() + fadeIn(),
                exit = shrinkVertically() + fadeOut()
            ) {
                Column(modifier = Modifier.padding(top = 8.dp)) {
                    BasicTextField(
                        value = note,
                        onValueChange = { note = it },
                        textStyle = MaterialTheme.typography.bodyMedium.copy(color = SumiInk),
                        cursorBrush = SolidColor(MatchaPrimary),
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(10.dp))
                            .background(Color.White)
                            .border(0.8.dp, WashiBorder, RoundedCornerShape(10.dp))
                            .padding(12.dp),
                        decorationBox = { innerTextField ->
                            if (note.isEmpty()) {
                                Text(
                                    text = "输入记账备注 (如：午餐黄焖鸡、咖啡、日用)",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = SumiTertiary
                                )
                            }
                            innerTextField()
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 5. 和风算盘质感 4x4 计算器键盘
            WashiCalculatorKeypad(
                expression = expression,
                onExpressionChange = { expression = it },
                onSave = {
                    val mainCat = selectedMainCategory
                    if (mainCat == null) {
                        Toast.makeText(context, "请先选择消费分类", Toast.LENGTH_SHORT).show()
                        return@WashiCalculatorKeypad
                    }

                    val finalAmount = evaluateExpression(expression)
                    if (finalAmount == null || finalAmount <= 0.0) {
                        Toast.makeText(context, "请输入有效的记账金额", Toast.LENGTH_SHORT).show()
                        return@WashiCalculatorKeypad
                    }

                    onSaveRecord(
                        finalAmount,
                        mainCat.id,
                        selectedSubCategory?.id,
                        note,
                        selectedTimestamp,
                        null
                    )
                    onDismiss()
                },
                haptic = haptic
            )
        }
    }
}

@Composable
private fun WashiCalculatorKeypad(
    expression: String,
    onExpressionChange: (String) -> Unit,
    onSave: () -> Unit,
    haptic: HapticFeedback
) {
    val keys = listOf(
        listOf("7", "8", "9", "+"),
        listOf("4", "5", "6", "-"),
        listOf("1", "2", "3", "del"),
        listOf("C", "0", ".", "ok")
    )

    Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        keys.forEach { rowKeys ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                rowKeys.forEach { key ->
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .height(52.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(
                                when (key) {
                                    "ok" -> MatchaPrimary
                                    "+", "-" -> SakuraSoft
                                    "del", "C" -> WashiPaperSubtle
                                    else -> Color.White
                                }
                            )
                            .border(
                                width = 0.8.dp,
                                color = when (key) {
                                    "ok" -> MatchaPrimary
                                    "+", "-" -> SakuraAccent.copy(alpha = 0.4f)
                                    else -> WashiBorder
                                },
                                shape = RoundedCornerShape(12.dp)
                            )
                            .clickable {
                                haptic.performHapticFeedback(HapticFeedbackType.TextHandleMove)
                                handleKeyClick(key, expression, onExpressionChange, onSave)
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        when (key) {
                            "del" -> Icon(
                                imageVector = Icons.AutoMirrored.Filled.Backspace,
                                contentDescription = "退格",
                                tint = SumiSecondary,
                                modifier = Modifier.size(20.dp)
                            )
                            "ok" -> Text(
                                text = "完成",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White
                            )
                            "+", "-" -> Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.Bold,
                                color = SakuraAccent
                            )
                            "C" -> Text(
                                text = "清空",
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.SemiBold,
                                color = SumiSecondary
                            )
                            else -> Text(
                                text = key,
                                style = MaterialTheme.typography.titleLarge,
                                fontWeight = FontWeight.SemiBold,
                                color = SumiInk
                            )
                        }
                    }
                }
            }
        }
    }
}

private fun handleKeyClick(
    key: String,
    current: String,
    onUpdate: (String) -> Unit,
    onSave: () -> Unit
) {
    when (key) {
        "C" -> onUpdate("0")
        "del" -> {
            if (current.length <= 1) {
                onUpdate("0")
            } else {
                onUpdate(current.dropLast(1))
            }
        }
        "ok" -> onSave()
        "+", "-" -> {
            val lastChar = current.lastOrNull()
            if (lastChar == '+' || lastChar == '-') {
                onUpdate(current.dropLast(1) + key)
            } else {
                onUpdate(current + key)
            }
        }
        "." -> {
            val parts = current.split('+', '-')
            val currentSegment = parts.lastOrNull() ?: ""
            if (!currentSegment.contains('.')) {
                onUpdate(current + ".")
            }
        }
        else -> {
            if (current == "0") {
                onUpdate(key)
            } else {
                // 限制小数位数最多2位
                val parts = current.split('+', '-')
                val lastPart = parts.lastOrNull() ?: ""
                if (lastPart.contains('.')) {
                    val decimals = lastPart.substringAfter('.')
                    if (decimals.length < 2) {
                        onUpdate(current + key)
                    }
                } else {
                    onUpdate(current + key)
                }
            }
        }
    }
}

fun isExpression(expr: String): Boolean {
    return expr.contains('+') || (expr.contains('-') && !expr.startsWith("-") || expr.indexOf('-', 1) > 0)
}

fun evaluateExpression(expr: String): Double? {
    if (expr.isBlank()) return null
    return try {
        var clean = expr.trim()
        if (clean.endsWith("+") || clean.endsWith("-")) {
            clean = clean.dropLast(1)
        }
        if (clean.isEmpty()) return null

        val tokens = mutableListOf<String>()
        var currentNumber = StringBuilder()

        for (i in clean.indices) {
            val c = clean[i]
            if (c == '+' || (c == '-' && i > 0 && clean[i - 1] != '+' && clean[i - 1] != '-')) {
                if (currentNumber.isNotEmpty()) {
                    tokens.add(currentNumber.toString())
                    currentNumber = StringBuilder()
                }
                tokens.add(c.toString())
            } else {
                currentNumber.append(c)
            }
        }
        if (currentNumber.isNotEmpty()) {
            tokens.add(currentNumber.toString())
        }

        if (tokens.isEmpty()) return null

        var result = tokens[0].toDoubleOrNull() ?: return null
        var i = 1
        while (i < tokens.size) {
            val op = tokens[i]
            val nextVal = tokens.getOrNull(i + 1)?.toDoubleOrNull() ?: break
            if (op == "+") result += nextVal
            if (op == "-") result -= nextVal
            i += 2
        }

        (Math.round(result * 100.0) / 100.0).coerceAtLeast(0.0)
    } catch (e: Exception) {
        null
    }
}
