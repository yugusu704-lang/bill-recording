package com.localbill.recording.data.repository

import com.localbill.recording.data.dao.CategoryDao
import com.localbill.recording.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

class CategoryRepository(
    private val categoryDao: CategoryDao
) {
    val allCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getAllCategoriesFlow()
    val mainCategoriesFlow: Flow<List<CategoryEntity>> = categoryDao.getMainCategoriesFlow()

    fun getSubCategoriesFlow(parentId: Long): Flow<List<CategoryEntity>> {
        return categoryDao.getSubCategoriesFlow(parentId)
    }

    suspend fun getAllCategories(): List<CategoryEntity> {
        return categoryDao.getAllCategories()
    }

    suspend fun getMainCategories(): List<CategoryEntity> {
        return categoryDao.getMainCategories()
    }

    suspend fun getSubCategories(parentId: Long): List<CategoryEntity> {
        return categoryDao.getSubCategories(parentId)
    }

    suspend fun getCategoryById(id: Long): CategoryEntity? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun addCategory(
        name: String,
        iconName: String,
        colorHex: Long,
        parentId: Long? = null,
        sortOrder: Int = 0
    ): Long {
        val category = CategoryEntity(
            name = name.trim(),
            iconName = iconName,
            colorHex = colorHex,
            parentId = parentId,
            isBuiltIn = false,
            sortOrder = sortOrder
        )
        return categoryDao.insertCategory(category)
    }

    suspend fun updateCategory(category: CategoryEntity) {
        categoryDao.updateCategory(category)
    }

    /**
     * 删除分类：若为主分类，连同其所有子分类一并删除（前提是无关联账单）
     */
    suspend fun deleteCategory(category: CategoryEntity): Boolean {
        val recordCount = categoryDao.countRecordsUsingCategory(category.id)
        if (recordCount > 0) {
            return false // 存在关联账单，禁止直接删除
        }

        // 如果是主分类，检查其子分类是否有关联账单
        if (category.parentId == null) {
            val subCategories = categoryDao.getSubCategories(category.id)
            for (sub in subCategories) {
                if (categoryDao.countRecordsUsingCategory(sub.id) > 0) {
                    return false
                }
            }
            // 删除所有子分类
            categoryDao.deleteSubCategories(category.id)
        }

        categoryDao.deleteCategory(category)
        return true
    }

    suspend fun countRecordsUsingCategory(categoryId: Long): Int {
        return categoryDao.countRecordsUsingCategory(categoryId)
    }
}
