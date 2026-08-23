package com.localbill.recording.ui.viewmodel

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.localbill.recording.data.repository.BackupRepository
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharedFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.io.File

data class BackupUiState(
    val isExporting: Boolean = false,
    val isRestoring: Boolean = false,
    val lastBackupJson: String? = null
)

sealed class BackupEvent {
    data class ShowToast(val message: String) : BackupEvent()
    data class ExportJsonSuccess(val jsonContent: String) : BackupEvent()
    data class ExportCsvSuccess(val filePath: String) : BackupEvent()
}

class BackupViewModel(
    private val backupRepository: BackupRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow(BackupUiState())
    val uiState: StateFlow<BackupUiState> = _uiState.asStateFlow()

    private val _eventFlow = MutableSharedFlow<BackupEvent>()
    val eventFlow: SharedFlow<BackupEvent> = _eventFlow.asSharedFlow()

    fun exportBackupJson() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val json = backupRepository.createBackupJson()
                _uiState.value = _uiState.value.copy(isExporting = false, lastBackupJson = json)
                _eventFlow.emit(BackupEvent.ExportJsonSuccess(json))
                _eventFlow.emit(BackupEvent.ShowToast("备份数据生成成功"))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false)
                _eventFlow.emit(BackupEvent.ShowToast("导出备份失败: ${e.message}"))
            }
        }
    }

    fun restoreFromJson(jsonString: String) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRestoring = true)
            val result = backupRepository.restoreFromJson(jsonString)
            _uiState.value = _uiState.value.copy(isRestoring = false)

            if (result.isSuccess) {
                val count = result.getOrDefault(0)
                _eventFlow.emit(BackupEvent.ShowToast("恢复成功！共导入 ${count} 条流水账单"))
            } else {
                _eventFlow.emit(BackupEvent.ShowToast("恢复失败: ${result.exceptionOrNull()?.message}"))
            }
        }
    }

    fun exportToCsv(context: Context) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isExporting = true)
            try {
                val exportDir = File(context.getExternalFilesDir(null), "exports")
                if (!exportDir.exists()) exportDir.mkdirs()
                val targetFile = File(exportDir, "bill_records_${System.currentTimeMillis()}.csv")

                val success = backupRepository.exportCsv(targetFile)
                _uiState.value = _uiState.value.copy(isExporting = false)

                if (success) {
                    _eventFlow.emit(BackupEvent.ExportCsvSuccess(targetFile.absolutePath))
                    _eventFlow.emit(BackupEvent.ShowToast("CSV 已保存至: ${targetFile.name}"))
                } else {
                    _eventFlow.emit(BackupEvent.ShowToast("导出 CSV 失败"))
                }
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(isExporting = false)
                _eventFlow.emit(BackupEvent.ShowToast("导出失败: ${e.message}"))
            }
        }
    }

    class Factory(
        private val backupRepository: BackupRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return BackupViewModel(backupRepository) as T
        }
    }
}
