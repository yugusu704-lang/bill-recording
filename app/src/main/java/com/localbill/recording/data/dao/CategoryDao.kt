package com.localbill.recording.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.localbill.recording.data.entity.CategoryEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface CategoryDao {

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY sortOrder ASC, id ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder ASC, id ASC")
    fun getMainCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId IS NULL ORDER BY sortOrder ASC, id ASC")
    suspend fun getMainCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder ASC, id ASC")
    fun getSubCategoriesFlow(parentId: Long): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories WHERE parentId = :parentId ORDER BY sortOrder ASC, id ASC")
    suspend fun getSubCategories(parentId: Long): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE id = :id LIMIT 1")
    suspend fun getCategoryById(id: Long): CategoryEntity?

    @Query("SELECT * FROM categories WHERE name = :name AND parentId IS :parentId LIMIT 1")
    suspend fun getCategoryByNameAndParent(name: String, parentId: Long?): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertCategories(categories: List<CategoryEntity>): List<Long>

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE parentId = :parentId")
    suspend fun deleteSubCategories(parentId: Long)

    @Query("SELECT COUNT(*) FROM records WHERE categoryId = :categoryId OR subCategoryId = :categoryId")
    suspend fun countRecordsUsingCategory(categoryId: Long): Int

    @Query("SELECT COUNT(*) FROM categories")
    suspend fun getCategoryCount(): Int

    @Query("DELETE FROM categories")
    suspend fun clearAll()
}
