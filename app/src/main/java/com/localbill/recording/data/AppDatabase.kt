package com.localbill.recording.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase
import com.localbill.recording.data.dao.CategoryDao
import com.localbill.recording.data.dao.RecordDao
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [CategoryEntity::class, RecordEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun categoryDao(): CategoryDao
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        private val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                // 表结构未变化，仅保留所有旧数据并清洗旧版 Kakeibo 备注标记
                db.execSQL(
                    """
                    UPDATE records
                    SET note = TRIM(
                        REPLACE(
                            REPLACE(
                                REPLACE(
                                    REPLACE(TRIM(note), '[NEEDS]', ''),
                                    '[WANTS]', ''
                                ),
                                '[CULTURE]', ''
                            ),
                            '[UNEXPECTED]', ''
                        )
                    )
                    """.trimIndent()
                )
            }
        }

        fun getDatabase(context: Context, scope: CoroutineScope = CoroutineScope(Dispatchers.IO)): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "bill_recording.db"
                )
                    .addMigrations(MIGRATION_1_2)
                    .addCallback(DatabaseCallback(scope))
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateDefaultCategories(database.categoryDao())
                    }
                }
            }
        }

        suspend fun populateDefaultCategories(categoryDao: CategoryDao) {
            if (categoryDao.getCategoryCount() > 0) return

            // 1. 学习 (Study)
            val studyId = categoryDao.insertCategory(
                CategoryEntity(
                    name = "学习",
                    iconName = "school",
                    colorHex = 0xFF3B82F6, // 亮蓝
                    parentId = null,
                    isBuiltIn = true,
                    sortOrder = 1
                )
            )

            // 2. 饮食 (Dining)
            val foodId = categoryDao.insertCategory(
                CategoryEntity(
                    name = "饮食",
                    iconName = "restaurant",
                    colorHex = 0xFFF59E0B, // 暖橙
                    parentId = null,
                    isBuiltIn = true,
                    sortOrder = 2
                )
            )

            // 饮食子分类：食堂、外卖、外出、水果
            val foodSubCategories = listOf(
                CategoryEntity(
                    name = "食堂",
                    iconName = "soup_kitchen",
                    colorHex = 0xFFF97316, // 橙色
                    parentId = foodId,
                    isBuiltIn = true,
                    sortOrder = 1
                ),
                CategoryEntity(
                    name = "外卖",
                    iconName = "delivery_dining",
                    colorHex = 0xFFEF4444, // 珊瑚红
                    parentId = foodId,
                    isBuiltIn = true,
                    sortOrder = 2
                ),
                CategoryEntity(
                    name = "外出",
                    iconName = "flatware",
                    colorHex = 0xFFEC4899, // 玫瑰粉
                    parentId = foodId,
                    isBuiltIn = true,
                    sortOrder = 3
                ),
                CategoryEntity(
                    name = "水果",
                    iconName = "eco",
                    colorHex = 0xFF10B981, // 翡翠绿
                    parentId = foodId,
                    isBuiltIn = true,
                    sortOrder = 4
                )
            )
            categoryDao.insertCategories(foodSubCategories)

            // 3. 交通 (Transport)
            categoryDao.insertCategory(
                CategoryEntity(
                    name = "交通",
                    iconName = "directions_car",
                    colorHex = 0xFF06B6D4, // 青蓝
                    parentId = null,
                    isBuiltIn = true,
                    sortOrder = 3
                )
            )

            // 4. 衣物 (Clothing)
            categoryDao.insertCategory(
                CategoryEntity(
                    name = "衣物",
                    iconName = "checkroom",
                    colorHex = 0xFF8B5CF6, // 优雅紫
                    parentId = null,
                    isBuiltIn = true,
                    sortOrder = 4
                )
            )
        }
    }
}
