package com.localbill.recording.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * 记账明细实体
 */
@Entity(
    tableName = "records",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index("categoryId"),
        Index("subCategoryId"),
        Index("timestamp")
    ]
)
data class RecordEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val amount: Double,
    val categoryId: Long,
    val subCategoryId: Long? = null,
    val note: String = "",
    val timestamp: Long,
    val imagePath: String? = null,
    val createdAt: Long = System.currentTimeMillis()
)
