package com.localbill.recording.ui.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.data.model.CategoryAggregation
import com.localbill.recording.data.model.PeriodSummary
import com.localbill.recording.data.model.PeriodType
import com.localbill.recording.data.model.TrendPoint
import com.localbill.recording.data.repository.RecordRepository
import com.localbill.recording.util.DateTimeUtils
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import java.time.LocalDate
import java.time.YearMonth

data class StatisticsUiState(
    val periodType: PeriodType = PeriodType.MONTH,
    val selectedDate: LocalDate = LocalDate.now(),
    val periodTitle: String = "",
    val summary: PeriodSummary = PeriodSummary(),
    val trendPoints: List<TrendPoint> = emptyList(),
    val categoryAggregations: List<CategoryAggregation> = emptyList(),
    val periodRecords: List<RecordWithCategory> = emptyList(),
    val selectedCategoryAggregation: CategoryAggregation? = null,
    val isLoading: Boolean = false
)

class StatisticsViewModel(
    private val recordRepository: RecordRepository
) : ViewModel() {

    private val _periodType = MutableStateFlow(PeriodType.MONTH)
    private val _selectedDate = MutableStateFlow(LocalDate.now())
    private val _selectedCategory = MutableStateFlow<CategoryAggregation?>(null)

    @OptIn(ExperimentalCoroutinesApi::class)
    val uiState: StateFlow<StatisticsUiState> = combine(
        _periodType,
        _selectedDate,
        _selectedCategory
    ) { periodType, selectedDate, selectedCategory ->
        Triple(periodType, selectedDate, selectedCategory)
    }.flatMapLatest { (periodType, selectedDate, selectedCategory) ->
        val (startTime, endTime) = when (periodType) {
            PeriodType.DAY -> DateTimeUtils.getDayRange(selectedDate)
            PeriodType.WEEK -> DateTimeUtils.getWeekRange(selectedDate)
            PeriodType.MONTH -> DateTimeUtils.getMonthRange(YearMonth.from(selectedDate))
        }

        val daysCount = when (periodType) {
            PeriodType.DAY -> 1
            PeriodType.WEEK -> 7
            PeriodType.MONTH -> YearMonth.from(selectedDate).lengthOfMonth()
        }

        val title = when (periodType) {
            PeriodType.DAY -> selectedDate.format(DateTimeUtils.DATE_DISPLAY_FORMATTER)
            PeriodType.WEEK -> {
                val (wStart, wEnd) = DateTimeUtils.getWeekRange(selectedDate)
                val sDate = DateTimeUtils.toLocalDate(wStart)
                val eDate = DateTimeUtils.toLocalDate(wEnd)
                "${sDate.monthValue}/${sDate.dayOfMonth} - ${eDate.monthValue}/${eDate.dayOfMonth}"
            }
            PeriodType.MONTH -> YearMonth.from(selectedDate).format(DateTimeUtils.MONTH_DISPLAY_FORMATTER)
        }

        recordRepository.getRecordsInRangeFlow(startTime, endTime).map { records ->
            val summary = recordRepository.calculatePeriodSummary(records, daysCount)
            val trendPoints = recordRepository.calculateTrendPoints(records, periodType, selectedDate, startTime)
            val aggregations = recordRepository.calculateCategoryAggregations(records)

            // 如果之前选中的下钻分类在新数据中仍然存在，保留其最新聚合数据
            val updatedSelectedCategory = selectedCategory?.let { oldCat ->
                aggregations.find { it.mainCategory.id == oldCat.mainCategory.id }
            }

            StatisticsUiState(
                periodType = periodType,
                selectedDate = selectedDate,
                periodTitle = title,
                summary = summary,
                trendPoints = trendPoints,
                categoryAggregations = aggregations,
                periodRecords = records,
                selectedCategoryAggregation = updatedSelectedCategory,
                isLoading = false
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.Eagerly,
        initialValue = StatisticsUiState()
    )

    fun setPeriodType(periodType: PeriodType) {
        _periodType.value = periodType
        _selectedCategory.value = null
    }

    fun navigatePeriod(step: Int) {
        val current = _selectedDate.value
        val newDate = when (_periodType.value) {
            PeriodType.DAY -> current.plusDays(step.toLong())
            PeriodType.WEEK -> current.plusWeeks(step.toLong())
            PeriodType.MONTH -> current.plusMonths(step.toLong())
        }
        _selectedDate.value = newDate
        _selectedCategory.value = null
    }

    fun setSelectedCategory(aggregation: CategoryAggregation?) {
        _selectedCategory.value = aggregation
    }

    class Factory(
        private val recordRepository: RecordRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return StatisticsViewModel(recordRepository) as T
        }
    }
}
