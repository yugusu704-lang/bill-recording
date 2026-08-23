package com.localbill.recording.data.entity

import androidx.room.Embedded
import androidx.room.Relation

/**
 * 关联查询：记录关联主分类与可选的子分类
 */
data class RecordWithCategory(
    @Embedded
    val record: RecordEntity,

    @Relation(
        parentColumn = "categoryId",
        entityColumn = "id"
    )
    val category: CategoryEntity,

    @Relation(
        parentColumn = "subCategoryId",
        entityColumn = "id"
    )
    val subCategory: CategoryEntity? = null
) {
    /**
     * 获取显示用的主分类名称
     */
    val displayCategoryName: String
        get() = category.name

    /**
     * 获取显示用的子分类名称（若有）
     */
    val displaySubCategoryName: String?
        get() = subCategory?.name

    /**
     * 获取分类图标标识
     */
    val displayIconName: String
        get() = subCategory?.iconName ?: category.iconName

    /**
     * 获取分类色值
     */
    val displayColorHex: Long
        get() = subCategory?.colorHex ?: category.colorHex
}
