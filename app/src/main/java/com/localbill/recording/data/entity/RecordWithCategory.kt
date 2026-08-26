package com.localbill.recording.data.entity

import androidx.room.Embedded
import androidx.room.Relation
import com.localbill.recording.data.model.KakeiboPillar

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

    /**
     * 高性能预解析四支柱（避免 LazyColumn 每次重组与滑动时重复 split 字符串）
     */
    val resolvedPillar: KakeiboPillar by lazy {
        val note = record.note
        when {
            note.contains("[WANTS]") -> KakeiboPillar.WANTS
            note.contains("[CULTURE]") -> KakeiboPillar.CULTURE
            note.contains("[UNEXPECTED]") -> KakeiboPillar.UNEXPECTED
            note.contains("[NEEDS]") -> KakeiboPillar.NEEDS
            displayCategoryName.contains("学习") || displayCategoryName.contains("书籍") -> KakeiboPillar.CULTURE
            displayCategoryName.contains("娱乐") || displayCategoryName.contains("外卖") || displayCategoryName.contains("服饰") -> KakeiboPillar.WANTS
            displayCategoryName.contains("医疗") || displayCategoryName.contains("维修") -> KakeiboPillar.UNEXPECTED
            else -> KakeiboPillar.NEEDS
        }
    }

    val cleanNote: String by lazy {
        record.note
            .replace("[NEEDS]", "")
            .replace("[WANTS]", "")
            .replace("[CULTURE]", "")
            .replace("[UNEXPECTED]", "")
            .trim()
    }
}
