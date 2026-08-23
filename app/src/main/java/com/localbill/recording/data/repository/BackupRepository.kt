package com.localbill.recording.data.repository

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.localbill.recording.data.dao.CategoryDao
import com.localbill.recording.data.dao.RecordDao
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordEntity
import com.localbill.recording.data.model.BackupPayload
import com.localbill.recording.data.model.CategoryBackupDto
import com.localbill.recording.data.model.RecordBackupDto
import com.localbill.recording.util.CsvExporter
import java.io.File

class BackupRepository(
    private val categoryDao: CategoryDao,
    private val recordDao: RecordDao
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * 生成全量 JSON 备份数据字符串
     */
    suspend fun createBackupJson(): String {
        val categories = categoryDao.getAllCategories().map {
            CategoryBackupDto(
                id = it.id,
                name = it.name,
                iconName = it.iconName,
                colorHex = it.colorHex,
                parentId = it.parentId,
                isBuiltIn = it.isBuiltIn,
                sortOrder = it.sortOrder
            )
        }

        val records = recordDao.getAllRecords().map {
            RecordBackupDto(
                id = it.id,
                amount = it.amount,
                categoryId = it.categoryId,
                subCategoryId = it.subCategoryId,
                note = it.note,
                timestamp = it.timestamp,
                imagePath = it.imagePath,
                createdAt = it.createdAt
            )
        }

        val payload = BackupPayload(
            categories = categories,
            records = records
        )
        return gson.toJson(payload)
    }

    /**
     * 从 JSON 字符串恢复数据库
     */
    suspend fun restoreFromJson(jsonString: String): Result<Int> {
        return try {
            val payload = gson.fromJson(jsonString, BackupPayload::class.java)
                ?: return Result.failure(IllegalArgumentException("备份文件内容无效"))

            if (payload.categories.isEmpty()) {
                return Result.failure(IllegalArgumentException("备份数据中无有效分类信息"))
            }

            // 清空现有数据
            recordDao.clearAll()
            categoryDao.clearAll()

            // 恢复分类（先恢复主分类，再恢复子分类以满足外键依赖）
            val categoryEntities = payload.categories.map {
                CategoryEntity(
                    id = it.id,
                    name = it.name,
                    iconName = it.iconName,
                    colorHex = it.colorHex,
                    parentId = it.parentId,
                    isBuiltIn = it.isBuiltIn,
                    sortOrder = it.sortOrder
                )
            }
            categoryDao.insertCategories(categoryEntities)

            // 恢复记账记录
            val recordEntities = payload.records.map {
                RecordEntity(
                    id = it.id,
                    amount = it.amount,
                    categoryId = it.categoryId,
                    subCategoryId = it.subCategoryId,
                    note = it.note,
                    timestamp = it.timestamp,
                    imagePath = it.imagePath,
                    createdAt = it.createdAt
                )
            }
            recordDao.insertRecords(recordEntities)

            Result.success(recordEntities.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 导出为 CSV 文件
     */
    suspend fun exportCsv(targetFile: File): Boolean {
        val records = recordDao.getAllRecordsWithCategory()
        return CsvExporter.exportRecordsToCsv(records, targetFile)
    }
}
