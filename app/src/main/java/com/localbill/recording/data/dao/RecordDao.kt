package com.localbill.recording.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.localbill.recording.data.entity.RecordEntity
import com.localbill.recording.data.entity.RecordWithCategory
import kotlinx.coroutines.flow.Flow

data class CategoryExpenseSum(
    val categoryId: Long,
    val totalAmount: Double,
    val count: Int
)

data class SubCategoryExpenseSum(
    val subCategoryId: Long,
    val totalAmount: Double,
    val count: Int
)

@Dao
interface RecordDao {

    @Transaction
    @Query("SELECT * FROM records ORDER BY timestamp DESC, id DESC")
    fun getAllRecordsWithCategoryFlow(): Flow<List<RecordWithCategory>>

    @Transaction
    @Query("SELECT * FROM records ORDER BY timestamp DESC, id DESC")
    suspend fun getAllRecordsWithCategory(): List<RecordWithCategory>

    @Query("SELECT * FROM records ORDER BY timestamp DESC, id DESC")
    suspend fun getAllRecords(): List<RecordEntity>

    @Transaction
    @Query("SELECT * FROM records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC, id DESC")
    fun getRecordsInRangeFlow(startTime: Long, endTime: Long): Flow<List<RecordWithCategory>>

    @Transaction
    @Query("SELECT * FROM records WHERE timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC, id DESC")
    suspend fun getRecordsInRange(startTime: Long, endTime: Long): List<RecordWithCategory>

    @Transaction
    @Query("SELECT * FROM records WHERE (categoryId = :categoryId OR subCategoryId = :categoryId) AND timestamp >= :startTime AND timestamp <= :endTime ORDER BY timestamp DESC")
    suspend fun getRecordsBySpecificCategoryInRange(categoryId: Long, startTime: Long, endTime: Long): List<RecordWithCategory>

    @Transaction
    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun getRecordWithCategoryById(id: Long): RecordWithCategory?

    @Query("SELECT * FROM records WHERE id = :id LIMIT 1")
    suspend fun getRecordById(id: Long): RecordEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecord(record: RecordEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertRecords(records: List<RecordEntity>): List<Long>

    @Update
    suspend fun updateRecord(record: RecordEntity)

    @Delete
    suspend fun deleteRecord(record: RecordEntity)

    @Query("DELETE FROM records WHERE id = :id")
    suspend fun deleteRecordById(id: Long)

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM records WHERE timestamp >= :startTime AND timestamp <= :endTime")
    fun getTotalExpenseInRangeFlow(startTime: Long, endTime: Long): Flow<Double>

    @Query("SELECT COALESCE(SUM(amount), 0.0) FROM records WHERE timestamp >= :startTime AND timestamp <= :endTime")
    suspend fun getTotalExpenseInRange(startTime: Long, endTime: Long): Double

    @Query("""
        SELECT categoryId, SUM(amount) as totalAmount, COUNT(id) as count
        FROM records
        WHERE timestamp >= :startTime AND timestamp <= :endTime
        GROUP BY categoryId
        ORDER BY totalAmount DESC
    """)
    suspend fun getCategoryExpensesInRange(startTime: Long, endTime: Long): List<CategoryExpenseSum>

    @Query("""
        SELECT subCategoryId, SUM(amount) as totalAmount, COUNT(id) as count
        FROM records
        WHERE categoryId = :mainCategoryId AND subCategoryId IS NOT NULL 
          AND timestamp >= :startTime AND timestamp <= :endTime
        GROUP BY subCategoryId
        ORDER BY totalAmount DESC
    """)
    suspend fun getSubCategoryExpensesInRange(mainCategoryId: Long, startTime: Long, endTime: Long): List<SubCategoryExpenseSum>

    @Query("SELECT COUNT(*) FROM records")
    suspend fun getRecordCount(): Int

    @Query("DELETE FROM records")
    suspend fun clearAll()
}
