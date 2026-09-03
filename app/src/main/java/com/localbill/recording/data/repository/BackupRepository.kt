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
import com.localbill.recording.widget.BillWidgetProvider
import java.io.File

class BackupRepository(
    private val categoryDao: CategoryDao,
    private val recordDao: RecordDao,
    private val context: android.content.Context? = null
) {
    private val gson: Gson = GsonBuilder().setPrettyPrinting().create()

    /**
     * 鐢熸垚鍏ㄩ噺 JSON 澶囦唤鏁版嵁瀛楃涓?     */
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
     * 浠?JSON 瀛楃涓叉仮澶嶆暟鎹簱
     */
    suspend fun restoreFromJson(jsonString: String): Result<Int> {
        return try {
            val payload = gson.fromJson(jsonString, BackupPayload::class.java)
                ?: return Result.failure(IllegalArgumentException("澶囦唤鏂囦欢鍐呭鏃犳晥"))

            if (payload.categories.isEmpty()) {
                return Result.failure(IllegalArgumentException("澶囦唤鏁版嵁涓棤鏈夋晥鍒嗙被淇℃伅"))
            }

            // 娓呯┖鐜版湁鏁版嵁
            recordDao.clearAll()
            categoryDao.clearAll()

            // 鎭㈠鍒嗙被锛堝厛鎭㈠涓诲垎绫伙紝鍐嶆仮澶嶅瓙鍒嗙被浠ユ弧瓒冲閿緷璧栵級
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

            // 鎭㈠璁拌处璁板綍
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
            context?.let { BillWidgetProvider.refreshAllWidgets(it) }

            Result.success(recordEntities.size)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    /**
     * 瀵煎嚭涓?CSV 鏂囦欢
     */
    suspend fun exportCsv(targetFile: File): Boolean {
        val records = recordDao.getAllRecordsWithCategory()
        return CsvExporter.exportRecordsToCsv(records, targetFile)
    }
}

