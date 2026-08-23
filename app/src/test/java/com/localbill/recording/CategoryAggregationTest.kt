package com.localbill.recording

import com.localbill.recording.data.dao.CategoryDao
import com.localbill.recording.data.dao.RecordDao
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordEntity
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.data.repository.RecordRepository
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import org.mockito.Mockito.mock

class CategoryAggregationTest {

    private val recordDao = mock(RecordDao::class.java)
    private val categoryDao = mock(CategoryDao::class.java)
    private val repository = RecordRepository(recordDao, categoryDao)

    @Test
    fun testAutomaticParentCategoryAggregation() {
        // 主分类：饮食
        val foodCat = CategoryEntity(id = 1, name = "饮食", iconName = "restaurant", colorHex = 0xFFF59E0B, parentId = null)
        // 4个子分类：食堂、外卖、外出、水果
        val canteenCat = CategoryEntity(id = 11, name = "食堂", iconName = "soup_kitchen", colorHex = 0xFFF97316, parentId = 1)
        val takeoutCat = CategoryEntity(id = 12, name = "外卖", iconName = "delivery_dining", colorHex = 0xFFEF4444, parentId = 1)
        val diningOutCat = CategoryEntity(id = 13, name = "外出", iconName = "flatware", colorHex = 0xFFEC4899, parentId = 1)
        val fruitCat = CategoryEntity(id = 14, name = "水果", iconName = "eco", colorHex = 0xFF10B981, parentId = 1)

        // 另一个主分类：学习
        val studyCat = CategoryEntity(id = 2, name = "学习", iconName = "school", colorHex = 0xFF3B82F6, parentId = null)

        val records = listOf(
            // 食堂 25元
            RecordWithCategory(
                record = RecordEntity(id = 1, amount = 25.0, categoryId = 1, subCategoryId = 11, note = "午餐", timestamp = 1000L),
                category = foodCat,
                subCategory = canteenCat
            ),
            // 外卖 35.5元
            RecordWithCategory(
                record = RecordEntity(id = 2, amount = 35.5, categoryId = 1, subCategoryId = 12, note = "晚餐", timestamp = 2000L),
                category = foodCat,
                subCategory = takeoutCat
            ),
            // 水果 15.0元
            RecordWithCategory(
                record = RecordEntity(id = 3, amount = 15.0, categoryId = 1, subCategoryId = 14, note = "苹果", timestamp = 3000L),
                category = foodCat,
                subCategory = fruitCat
            ),
            // 饮食直接支出（未选子分类）10.0元
            RecordWithCategory(
                record = RecordEntity(id = 4, amount = 10.0, categoryId = 1, subCategoryId = null, note = "零食", timestamp = 4000L),
                category = foodCat,
                subCategory = null
            ),
            // 学习 50.0元
            RecordWithCategory(
                record = RecordEntity(id = 5, amount = 50.0, categoryId = 2, subCategoryId = null, note = "购买教材", timestamp = 5000L),
                category = studyCat,
                subCategory = null
            )
        )

        // 总支出 = 25 + 35.5 + 15 + 10 + 50 = 135.5
        val aggregations = repository.calculateCategoryAggregations(records)

        assertEquals(2, aggregations.size)

        // 第一名为饮食: 25 + 35.5 + 15 + 10 = 85.5
        val foodAggregation = aggregations.find { it.mainCategory.id == 1L }
        assertNotNull(foodAggregation)
        assertEquals(85.5, foodAggregation!!.totalAmount, 0.001)
        assertEquals(4, foodAggregation.count)

        // 验证占比: 85.5 / 135.5 ≈ 63.1%
        assertEquals((85.5 / 135.5 * 100).toFloat(), foodAggregation.percentage, 0.01f)

        // 验证饮食下的子分类自动聚合
        assertEquals(4, foodAggregation.subCategoryBreakdowns.size)
        // 子分类最高为外卖 35.5
        assertEquals("外卖", foodAggregation.subCategoryBreakdowns[0].category.name)
        assertEquals(35.5, foodAggregation.subCategoryBreakdowns[0].amount, 0.001)

        // 第二名为学习 50.0
        val studyAggregation = aggregations.find { it.mainCategory.id == 2L }
        assertNotNull(studyAggregation)
        assertEquals(50.0, studyAggregation!!.totalAmount, 0.001)
        assertEquals(1, studyAggregation.count)
    }

    @Test
    fun testEmptyRecordAggregation() {
        val aggregations = repository.calculateCategoryAggregations(emptyList())
        assertTrue(aggregations.isEmpty())
    }
}
