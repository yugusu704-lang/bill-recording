package com.localbill.recording

import android.app.Application
import com.localbill.recording.data.AppDatabase
import com.localbill.recording.data.repository.BackupRepository
import com.localbill.recording.data.repository.CategoryRepository
import com.localbill.recording.data.repository.RecordRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob

class BillApplication : Application() {

    val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    val database by lazy { AppDatabase.getDatabase(this, applicationScope) }

    val categoryRepository by lazy {
        CategoryRepository(database.categoryDao())
    }

    val recordRepository by lazy {
        RecordRepository(database.recordDao(), database.categoryDao())
    }

    val backupRepository by lazy {
        BackupRepository(database.categoryDao(), database.recordDao())
    }
}
