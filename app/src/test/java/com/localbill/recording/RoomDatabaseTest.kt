package com.localbill.recording

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.localbill.recording.data.AppDatabase
import com.localbill.recording.data.dao.CategoryDao
import com.localbill.recording.data.dao.RecordDao
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordEntity
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RoomDatabaseTest {

    private lateinit var db: AppDatabase
    private lateinit var categoryDao: CategoryDao
    private lateinit var recordDao: RecordDao

    @Before
    fun createDb() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        categoryDao = db.categoryDao()
        recordDao = db.recordDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        db.close()
    }

    @Test
    fun testPrepopulateBuiltInCategories() = runBlocking {
        AppDatabase.populateDefaultCategories(categoryDao)

        val mainCategories = categoryDao.getMainCategories()
        // 验证主分类数量：学习、饮食、交通、衣物 4个
        assertEquals(4, mainCategories.size)
        val names = mainCategories.map { it.name }
        assertTrue(names.contains("学习"))
        assertTrue(names.contains("饮食"))
        assertTrue(names.contains("交通"))
        assertTrue(names.contains("衣物"))

        // 验证饮食下的4个子分类：食堂、外卖、外出、水果
        val foodCategory = mainCategories.first { it.name == "饮食" }
        val subCategories = categoryDao.getSubCategories(foodCategory.id)
        assertEquals(4, subCategories.size)
        val subNames = subCategories.map { it.name }
        assertTrue(subNames.contains("食堂"))
        assertTrue(subNames.contains("外卖"))
        assertTrue(subNames.contains("外出"))
        assertTrue(subNames.contains("水果"))
    }

    @Test
    fun testRecordInsertionAndRelationshipQuery() = runBlocking {
        AppDatabase.populateDefaultCategories(categoryDao)
        val foodCat = categoryDao.getMainCategories().first { it.name == "饮食" }
        val takeoutCat = categoryDao.getSubCategories(foodCat.id).first { it.name == "外卖" }

        val timestamp = 1700000000000L
        val recordId = recordDao.insertRecord(
            RecordEntity(
                amount = 45.8,
                categoryId = foodCat.id,
                subCategoryId = takeoutCat.id,
                note = "黄焖鸡米饭",
                timestamp = timestamp
            )
        )

        assertTrue(recordId > 0)

        // 测试关联查询
        val recordsWithCategory = recordDao.getAllRecordsWithCategory()
        assertEquals(1, recordsWithCategory.size)
        val item = recordsWithCategory[0]
        assertEquals(45.8, item.record.amount, 0.001)
        assertEquals("饮食", item.displayCategoryName)
        assertEquals("外卖", item.displaySubCategoryName)
        assertEquals("外卖", item.subCategory?.name)

        // 测试区间总额汇总
        val total = recordDao.getTotalExpenseInRange(timestamp - 1000, timestamp + 1000)
        assertEquals(45.8, total, 0.001)
    }

    @Test
    fun testCountRecordsUsingCategory() = runBlocking {
        AppDatabase.populateDefaultCategories(categoryDao)
        val foodCat = categoryDao.getMainCategories().first { it.name == "饮食" }
        val canteenCat = categoryDao.getSubCategories(foodCat.id).first { it.name == "食堂" }

        assertEquals(0, categoryDao.countRecordsUsingCategory(foodCat.id))

        recordDao.insertRecord(
            RecordEntity(
                amount = 15.0,
                categoryId = foodCat.id,
                subCategoryId = canteenCat.id,
                note = "食堂早餐",
                timestamp = System.currentTimeMillis()
            )
        )

        // 主分类与子分类均被统计到
        assertEquals(1, categoryDao.countRecordsUsingCategory(foodCat.id))
        assertEquals(1, categoryDao.countRecordsUsingCategory(canteenCat.id))
    }
}
