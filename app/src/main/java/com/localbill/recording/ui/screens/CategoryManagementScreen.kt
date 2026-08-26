package com.localbill.recording.ui.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.components.CategoryIcons
import com.localbill.recording.ui.theme.JapaneseCategoryColors
import com.localbill.recording.ui.theme.MatchaPrimary
import com.localbill.recording.ui.theme.SakuraAccent
import com.localbill.recording.ui.theme.SumiInk
import com.localbill.recording.ui.theme.SumiSecondary
import com.localbill.recording.ui.theme.SumiTertiary
import com.localbill.recording.ui.theme.WashiBorder
import com.localbill.recording.ui.theme.WashiCardBg
import com.localbill.recording.ui.theme.WashiPaperBg
import com.localbill.recording.ui.theme.WashiPaperSubtle
import com.localbill.recording.ui.viewmodel.CategoryEvent
import com.localbill.recording.ui.viewmodel.CategoryNode
import com.localbill.recording.ui.viewmodel.CategoryViewModel
import kotlinx.coroutines.flow.collectLatest

@Composable
fun CategoryManagementScreen(
    viewModel: CategoryViewModel,
    modifier: Modifier = Modifier
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    var isEditDialogVisible by remember { mutableStateOf(false) }
    var editingCategory by remember { mutableStateOf<CategoryEntity?>(null) }
    var parentCategoryForNewSub by remember { mutableStateOf<CategoryEntity?>(null) }

    LaunchedEffect(viewModel.eventFlow) {
        viewModel.eventFlow.collectLatest { event ->
            when (event) {
                is CategoryEvent.ShowToast -> {
                    Toast.makeText(context, event.message, Toast.LENGTH_SHORT).show()
                }
                CategoryEvent.CategorySaved -> {
                    isEditDialogVisible = false
                    editingCategory = null
                    parentCategoryForNewSub = null
                }
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        containerColor = WashiPaperBg,
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    parentCategoryForNewSub = null
                    isEditDialogVisible = true
                },
                containerColor = MatchaPrimary,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier
                    .padding(bottom = 16.dp)
                    .shadow(8.dp, CircleShape, spotColor = MatchaPrimary.copy(alpha = 0.35f))
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "新增主分类", modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(text = "添加主分类", fontWeight = FontWeight.Bold, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(horizontal = 18.dp, vertical = 14.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Column {
                    Text(
                        text = "分类系统与层级",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold,
                        color = SumiInk
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = "支持主分类与子分类自由拓展，自动汇总下辖流水",
                        style = MaterialTheme.typography.bodySmall,
                        color = SumiSecondary
                    )
                }
            }

            items(
                items = uiState.categoryNodes,
                key = { it.mainCategory.id }
            ) { node ->
                WashiCategoryCard(
                    node = node,
                    onAddSubCategory = {
                        parentCategoryForNewSub = node.mainCategory
                        editingCategory = null
                        isEditDialogVisible = true
                    },
                    onEditCategory = { cat ->
                        editingCategory = cat
                        parentCategoryForNewSub = null
                        isEditDialogVisible = true
                    },
                    onDeleteCategory = { cat ->
                        viewModel.deleteCategory(cat)
                    }
                )
            }

            item {
                Spacer(modifier = Modifier.height(72.dp))
            }
        }
    }

    if (isEditDialogVisible) {
        WashiAddEditCategoryDialog(
            category = editingCategory,
            parentCategory = parentCategoryForNewSub,
            onDismiss = {
                isEditDialogVisible = false
                editingCategory = null
                parentCategoryForNewSub = null
            },
            onSave = { name, iconName, colorHex ->
                viewModel.addOrUpdateCategory(
                    id = editingCategory?.id ?: 0L,
                    name = name,
                    iconName = iconName,
                    colorHex = colorHex,
                    parentId = editingCategory?.parentId ?: parentCategoryForNewSub?.id,
                    isBuiltIn = editingCategory?.isBuiltIn ?: false,
                    sortOrder = editingCategory?.sortOrder ?: 0
                )
            }
        )
    }
}

@Composable
private fun WashiCategoryCard(
    node: CategoryNode,
    onAddSubCategory: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(WashiCardBg)
            .border(0.8.dp, WashiBorder, RoundedCornerShape(16.dp))
            .padding(14.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = node.mainCategory.iconName,
                        colorHex = node.mainCategory.colorHex,
                        size = 38.dp,
                        iconSize = 18.dp,
                        cornerRadius = 10.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = node.mainCategory.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SumiInk
                            )
                            if (node.mainCategory.isBuiltIn) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "内置",
                                    style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                    color = SumiTertiary
                                )
                            }
                        }
                        Text(
                            text = "${node.subCategories.size} 个子分类",
                            style = MaterialTheme.typography.bodySmall,
                            color = SumiSecondary
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onEditCategory(node.mainCategory) },
                        modifier = Modifier.size(28.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = SumiSecondary,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                    if (!node.mainCategory.isBuiltIn) {
                        IconButton(
                            onClick = { onDeleteCategory(node.mainCategory) },
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

            Spacer(modifier = Modifier.height(10.dp))
            HorizontalDivider(color = WashiBorder.copy(alpha = 0.5f), thickness = 0.8.dp)
            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                node.subCategories.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp, horizontal = 2.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIconBadge(
                                iconName = sub.iconName,
                                colorHex = sub.colorHex,
                                size = 28.dp,
                                iconSize = 14.dp,
                                cornerRadius = 6.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.bodyMedium,
                                color = SumiInk
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEditCategory(sub) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "编辑",
                                    tint = SumiSecondary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteCategory(sub) },
                                modifier = Modifier.size(24.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "删除",
                                    tint = SumiTertiary,
                                    modifier = Modifier.size(14.dp)
                                )
                            }
                        }
                    }
                }

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddSubCategory)
                        .padding(vertical = 6.dp),
                    contentAlignment = Alignment.CenterStart
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(start = 2.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = MatchaPrimary,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "添加子分类...",
                            style = MaterialTheme.typography.bodySmall,
                            color = MatchaPrimary,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun WashiAddEditCategoryDialog(
    category: CategoryEntity?,
    parentCategory: CategoryEntity?,
    onDismiss: () -> Unit,
    onSave: (name: String, iconName: String, colorHex: Long) -> Unit
) {
    var name by remember { mutableStateOf(category?.name ?: "") }
    var selectedIconName by remember {
        mutableStateOf(category?.iconName ?: parentCategory?.iconName ?: "shopping_cart")
    }
    var selectedColorHex by remember {
        mutableLongStateOf(category?.colorHex ?: parentCategory?.colorHex ?: JapaneseCategoryColors.first())
    }

    val dialogTitle = when {
        category != null -> "编辑分类「${category.name}」"
        parentCategory != null -> "为「${parentCategory.name}」添加子分类"
        else -> "新建主分类"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color.White,
        title = { Text(text = dialogTitle, fontWeight = FontWeight.Bold, color = SumiInk) },
        text = {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("分类名称") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )

                // 预览
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "预览：", style = MaterialTheme.typography.bodySmall, color = SumiSecondary)
                    Spacer(modifier = Modifier.width(6.dp))
                    CategoryIconBadge(
                        iconName = selectedIconName,
                        colorHex = selectedColorHex,
                        size = 32.dp,
                        iconSize = 16.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (name.isNotBlank()) name else "分类名称",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(selectedColorHex)
                    )
                }

                // 图标选择
                Text(text = "选择图标", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SumiInk)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIcons.AVAILABLE_ICONS.forEach { (iconKey, vector) ->
                        val isSelected = selectedIconName == iconKey
                        Box(
                            modifier = Modifier
                                .size(34.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(if (isSelected) MatchaPrimary else WashiPaperSubtle)
                                .border(0.8.dp, if (isSelected) MatchaPrimary else WashiBorder, RoundedCornerShape(8.dp))
                            .clickable { selectedIconName = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconKey,
                                tint = if (isSelected) Color.White else SumiInk,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }

                // 色彩选择
                Text(text = "选择和风色调", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold, color = SumiInk)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    JapaneseCategoryColors.forEach { colorVal ->
                        val isSelected = selectedColorHex == colorVal
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) SumiInk else Color.Transparent,
                                    shape = CircleShape
                                )
                                .clickable { selectedColorHex = colorVal }
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onSave(name, selectedIconName, selectedColorHex) }
            ) {
                Text(text = "保存", fontWeight = FontWeight.Bold, color = MatchaPrimary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消", color = SumiSecondary)
            }
        }
    )
}
