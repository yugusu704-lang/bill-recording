package com.localbill.recording.data.model

import com.localbill.recording.data.entity.CategoryEntity

enum class PeriodType(val label: String) {
    DAY("日"),
    WEEK("周"),
    MONTH("月")
}

data class SubCategoryAggregation(
    val category: CategoryEntity,
    val amount: Double,
    val count: Int,
    val percentage: Float // 0.0f - 100.0f
)

data class CategoryAggregation(
    val mainCategory: CategoryEntity,
    val totalAmount: Double,
    val count: Int,
    val percentage: Float, // 0.0f - 100.0f
    val subCategoryBreakdowns: List<SubCategoryAggregation> = emptyList()
)

data class TrendPoint(
    val label: String,        // 如 "周一", "8/23"
    val subLabel: String = "", // 如 "08-23"
    val amount: Double,
    val timestamp: Long,
    val isCurrent: Boolean = false
)

data class KakeiboPillarStat(
    val pillar: KakeiboPillar,
    val amount: Double,
    val count: Int,
    val percentage: Float
)

data class PeriodSummary(
    val totalAmount: Double = 0.0,
    val dailyAverage: Double = 0.0,
    val recordCount: Int = 0,
    val topCategoryName: String? = null,
    val topCategoryAmount: Double = 0.0,
    val highestSingleExpense: Double = 0.0,
    val kakeiboPillars: List<KakeiboPillarStat> = emptyList(),
    val reflectionQuote: String = "花钱如泼水，省钱如针挑。理智审视每一笔心动支出。"
)
