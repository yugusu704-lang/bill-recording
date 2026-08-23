package com.localbill.recording

import com.localbill.recording.data.dao.CategoryDao
import com.localbill.recording.data.dao.RecordDao
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordEntity
import com.localbill.recording.data.repository.BackupRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.`when`
import org.mockito.Mockito.mock
import org.mockito.Mockito.verify

class BackupRestoreTest {

    private val categoryDao = mock(CategoryDao::class.java)
    private val recordDao = mock(RecordDao::class.java)
    private val backupRepository = BackupRepository(categoryDao, recordDao)

    @Test
    fun testJsonBackupGenerationAndRestore() = runBlocking {
        val categories = listOf(
            CategoryEntity(id = 1, name = "饮食", iconName = "restaurant", colorHex = 0xFFF59E0B, parentId = null, isBuiltIn = true, sortOrder = 1),
            CategoryEntity(id = 2, name = "食堂", iconName = "soup_kitchen", colorHex = 0xFFF97316, parentId = 1, isBuiltIn = true, sortOrder = 1)
        )
        val records = listOf(
            RecordEntity(id = 1, amount = 18.5, categoryId = 1, subCategoryId = 2, note = "午饭", timestamp = 1700000000000L, createdAt = 1700000000000L)
        )

        `when`(categoryDao.getAllCategories()).thenReturn(categories)
        `when`(recordDao.getAllRecords()).thenReturn(records)

        val json = backupRepository.createBackupJson()
        assertTrue(json.contains("饮食"))
        assertTrue(json.contains("食堂"))
        assertTrue(json.contains("18.5"))

        // 测试从该 JSON 恢复
        val result = backupRepository.restoreFromJson(json)
        assertTrue(result.isSuccess)
        assertEquals(1, result.getOrNull())

        verify(recordDao).clearAll()
        verify(categoryDao).clearAll()
    }
}
