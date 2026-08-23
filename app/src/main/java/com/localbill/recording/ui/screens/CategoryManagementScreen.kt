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
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.ui.components.CategoryIconBadge
import com.localbill.recording.ui.components.CategoryIcons
import com.localbill.recording.ui.theme.MacaronColorList
import com.localbill.recording.ui.theme.PrimaryGreen
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
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    editingCategory = null
                    parentCategoryForNewSub = null
                    isEditDialogVisible = true
                },
                containerColor = PrimaryGreen,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.padding(bottom = 16.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(imageVector = Icons.Default.Add, contentDescription = "新增大类")
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "添加主分类", fontWeight = FontWeight.Bold)
                }
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 12.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            item {
                Text(
                    text = "分类与层级管理",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = "支持自定义主分类及任意下属子分类，主分类自动聚合子类总额",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            items(
                items = uiState.categoryNodes,
                key = { it.mainCategory.id }
            ) { node ->
                CategoryNodeCard(
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
        AddEditCategoryDialog(
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
private fun CategoryNodeCard(
    node: CategoryNode,
    onAddSubCategory: () -> Unit,
    onEditCategory: (CategoryEntity) -> Unit,
    onDeleteCategory: (CategoryEntity) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.5.dp)
    ) {
        Column(modifier = Modifier.padding(14.dp)) {
            // 主分类头部
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    CategoryIconBadge(
                        iconName = node.mainCategory.iconName,
                        colorHex = node.mainCategory.colorHex,
                        size = 40.dp,
                        iconSize = 22.dp,
                        cornerRadius = 12.dp
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = node.mainCategory.name,
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.onSurface
                            )
                            if (node.mainCategory.isBuiltIn) {
                                Spacer(modifier = Modifier.width(6.dp))
                                Box(
                                    modifier = Modifier
                                        .clip(RoundedCornerShape(4.dp))
                                        .background(PrimaryGreen.copy(alpha = 0.12f))
                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                ) {
                                    Text(
                                        text = "内置",
                                        style = MaterialTheme.typography.labelSmall.copy(fontSize = 10.sp),
                                        color = PrimaryGreen,
                                        fontWeight = FontWeight.Medium
                                    )
                                }
                            }
                        }
                        Text(
                            text = "下辖 ${node.subCategories.size} 个子分类",
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = { onEditCategory(node.mainCategory) },
                        modifier = Modifier.size(32.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "编辑",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    if (!node.mainCategory.isBuiltIn) {
                        IconButton(
                            onClick = { onDeleteCategory(node.mainCategory) },
                            modifier = Modifier.size(32.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.DeleteOutline,
                                contentDescription = "删除",
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // 子分类列表与添加按钮
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(12.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                node.subCategories.forEach { sub ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(8.dp))
                            .background(MaterialTheme.colorScheme.surface)
                            .padding(horizontal = 10.dp, vertical = 6.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            CategoryIconBadge(
                                iconName = sub.iconName,
                                colorHex = sub.colorHex,
                                size = 26.dp,
                                iconSize = 14.dp,
                                cornerRadius = 6.dp
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = sub.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Medium
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = { onEditCategory(sub) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Edit,
                                    contentDescription = "编辑",
                                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                            IconButton(
                                onClick = { onDeleteCategory(sub) },
                                modifier = Modifier.size(28.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.DeleteOutline,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error,
                                    modifier = Modifier.size(15.dp)
                                )
                            }
                        }
                    }
                }

                // 添加子分类胶囊按钮
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .clickable(onClick = onAddSubCategory)
                        .padding(vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = null,
                            tint = PrimaryGreen,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "添加「${node.mainCategory.name}」的子分类",
                            style = MaterialTheme.typography.bodySmall,
                            color = PrimaryGreen,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalLayoutApi::class)
@Composable
private fun AddEditCategoryDialog(
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
        mutableLongStateOf(category?.colorHex ?: parentCategory?.colorHex ?: MacaronColorList.first())
    }

    val isEditing = category != null
    val isSubCategory = category?.parentId != null || parentCategory != null
    val dialogTitle = when {
        isEditing -> "修改分类「${category?.name}」"
        parentCategory != null -> "为「${parentCategory.name}」添加子分类"
        else -> "新建主分类"
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(text = dialogTitle, fontWeight = FontWeight.Bold) },
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

                // 预览效果
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = "预览效果：", style = MaterialTheme.typography.bodySmall)
                    Spacer(modifier = Modifier.width(8.dp))
                    CategoryIconBadge(
                        iconName = selectedIconName,
                        colorHex = selectedColorHex,
                        size = 36.dp,
                        iconSize = 20.dp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (name.isNotBlank()) name else "分类预览",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color(selectedColorHex)
                    )
                }

                // 图标选择器
                Text(text = "选择图标", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    CategoryIcons.AVAILABLE_ICONS.forEach { (iconKey, vector) ->
                        val isSelected = selectedIconName == iconKey
                        Box(
                            modifier = Modifier
                                .size(36.dp)
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (isSelected) PrimaryGreen.copy(alpha = 0.2f)
                                    else MaterialTheme.colorScheme.surfaceVariant
                                )
                                .border(
                                    width = if (isSelected) 1.5.dp else 0.dp,
                                    color = if (isSelected) PrimaryGreen else Color.Transparent,
                                    shape = RoundedCornerShape(8.dp)
                                )
                                .clickable { selectedIconName = iconKey },
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = vector,
                                contentDescription = iconKey,
                                tint = if (isSelected) PrimaryGreen else MaterialTheme.colorScheme.onSurface,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }

                // 色彩选择器
                Text(text = "选择主题色", style = MaterialTheme.typography.bodySmall, fontWeight = FontWeight.SemiBold)
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    MacaronColorList.forEach { colorVal ->
                        val isSelected = selectedColorHex == colorVal
                        Box(
                            modifier = Modifier
                                .size(32.dp)
                                .clip(CircleShape)
                                .background(Color(colorVal))
                                .border(
                                    width = if (isSelected) 2.5.dp else 0.dp,
                                    color = if (isSelected) Color.Black else Color.Transparent,
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
                Text(text = "保存", fontWeight = FontWeight.Bold)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(text = "取消")
            }
        }
    )
}
