package com.localbill.recording.widget

import android.content.Context
import com.localbill.recording.data.AppDatabase
import com.localbill.recording.data.repository.RecordRepository
import com.localbill.recording.util.DateTimeUtils
import com.localbill.recording.util.formatAmount
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import java.time.LocalDate
import java.time.YearMonth
import java.util.Locale

data class BillWidgetRecentItem(
    val id: Long,
    val timeText: String,
    val categoryText: String,
    val amountText: String
)

data class BillWidgetSnapshot(
    val monthExpenseText: String = "¥0.00",
    val todayExpenseText: String = "¥0.00",
    val todayCountText: String = "0 笔",
    val engelText: String = "--",
    val hasTodayData: Boolean = false,
    val recentItems: List<BillWidgetRecentItem> = emptyList()
)

object BillWidgetDataLoader {

    fun load(context: Context): BillWidgetSnapshot {
        val database = AppDatabase.getDatabase(context)
        val repository = RecordRepository(database.recordDao(), database.categoryDao())
        return runBlocking(Dispatchers.IO) {
            val today = LocalDate.now()
            val (todayStart, todayEnd) = DateTimeUtils.getDayRange(today)
            val (monthStart, monthEnd) = DateTimeUtils.getMonthRange(YearMonth.from(today))

            val todayRecords = repository.getRecordsInRange(todayStart, todayEnd)
            val monthRecords = repository.getRecordsInRange(monthStart, monthEnd)
            val recentRecords = database.recordDao().getRecentRecordsWithCategory(3)
            val engel = repository.calculateEngelCoefficient(monthRecords)

            val recentItems = recentRecords.map { r ->
                val dateTime = DateTimeUtils.toLocalDateTime(r.record.timestamp)
                BillWidgetRecentItem(
                    id = r.record.id,
                    timeText = dateTime.format(DateTimeUtils.TIME_FORMATTER),
                    categoryText = if (r.displaySubCategoryName != null) "${r.displayCategoryName} · ${r.displaySubCategoryName}" else r.displayCategoryName,
                    amountText = "¥" + formatAmount(r.record.amount)
                )
            }

            BillWidgetSnapshot(
                monthExpenseText = "¥" + formatAmount(monthRecords.sumOf { it.record.amount }),
                todayExpenseText = "¥" + formatAmount(todayRecords.sumOf { it.record.amount }),
                todayCountText = "${todayRecords.size} 笔",
                engelText = if (engel.totalAmount > 0.0) String.format(Locale.US, "%.0f%%", engel.percentage) else "--",
                hasTodayData = todayRecords.isNotEmpty(),
                recentItems = recentItems
            )
        }
    }
}
