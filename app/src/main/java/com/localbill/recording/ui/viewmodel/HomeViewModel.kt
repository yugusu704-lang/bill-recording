package com.localbill.recording.ui.viewmodel

import androidx.compose.runtime.Immutable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.data.repository.CategoryRepository
import com.localbill.recording.data.repository.RecordRepository
import com.localbill.recording.util.DateTimeUtils
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.YearMonth

@Immutable
data class DayGroupItem(
    val date: LocalDate,
    val totalAmount: Double,
    val records: List<RecordWithCategory>
)

@Immutable
data class HomeUiState(
    val todayExpense: Double = 0.0,
    val weekExpense: Double = 0.0,
    val monthExpense: Double = 0.0,
    val groupedDays: List<DayGroupItem> = emptyList(),
    val totalRecordCount: Int = 0,
    val mainCategories: List<CategoryEntity> = emptyList(),
    val subCategoriesMap: Map<Long, List<CategoryEntity>> = emptyMap(),
    val isLoading: Boolean = false
)

class HomeViewModel(
    private val recordRepository: RecordRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    val uiState: StateFlow<HomeUiState> = combine(
        recordRepository.allRecordsFlow,
        categoryRepository.mainCategoriesFlow,
        categoryRepository.allCategoriesFlow
    ) { records, mainCategories, allCategories ->
        val today = LocalDate.now()
        val (todayStart, todayEnd) = DateTimeUtils.getDayRange(today)
        val (weekStart, weekEnd) = DateTimeUtils.getWeekRange(today)
        val (monthStart, monthEnd) = DateTimeUtils.getMonthRange(YearMonth.from(today))

        var todayTotal = 0.0
        var weekTotal = 0.0
        var monthTotal = 0.0

        for (r in records) {
            val ts = r.record.timestamp
            val amt = r.record.amount
            if (ts in todayStart..todayEnd) todayTotal += amt
            if (ts in weekStart..weekEnd) weekTotal += amt
            if (ts in monthStart..monthEnd) monthTotal += amt
        }

        val subMap = mutableMapOf<Long, List<CategoryEntity>>()
        mainCategories.forEach { mainCat ->
            subMap[mainCat.id] = allCategories.filter { it.parentId == mainCat.id }
        }

        // 后台高性能预先分组
        val groupedMap = LinkedHashMap<LocalDate, MutableList<RecordWithCategory>>()
        for (r in records) {
            val date = DateTimeUtils.toLocalDate(r.record.timestamp)
            groupedMap.getOrPut(date) { mutableListOf() }.add(r)
        }

        val groupedDays = groupedMap.map { (date, recs) ->
            DayGroupItem(
                date = date,
                totalAmount = recs.sumOf { it.record.amount },
                records = recs
            )
        }

        HomeUiState(
            todayExpense = todayTotal,
            weekExpense = weekTotal,
            monthExpense = monthTotal,
            groupedDays = groupedDays,
            totalRecordCount = records.size,
            mainCategories = mainCategories,
            subCategoriesMap = subMap,
            isLoading = false
        )
    }.flowOn(Dispatchers.Default).stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = HomeUiState()
    )

    fun saveRecord(
        amount: Double,
        categoryId: Long,
        subCategoryId: Long?,
        note: String,
        timestamp: Long,
        imagePath: String?
    ) {
        viewModelScope.launch(Dispatchers.IO) {
            recordRepository.saveRecord(
                amount = amount,
                categoryId = categoryId,
                subCategoryId = subCategoryId,
                note = note,
                timestamp = timestamp,
                imagePath = imagePath
            )
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch(Dispatchers.IO) {
            recordRepository.deleteRecordById(recordId)
        }
    }

    class Factory(
        private val recordRepository: RecordRepository,
        private val categoryRepository: CategoryRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(recordRepository, categoryRepository) as T
        }
    }
}
