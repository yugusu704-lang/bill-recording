package com.localbill.recording.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 分类实体（支持两级分类结构）
 * parentId == null 为主分类，非 null 为子分类
 */
@Entity(
    tableName = "categories",
    indices = [Index("parentId"), Index("name")]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val iconName: String,
    val colorHex: Long, // ARGB 颜色数值，如 0xFF22C55E
    val parentId: Long? = null,
    val isBuiltIn: Boolean = false,
    val sortOrder: Int = 0
)
