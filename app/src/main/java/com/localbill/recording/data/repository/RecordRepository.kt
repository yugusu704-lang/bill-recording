package com.localbill.recording.data.repository

import com.localbill.recording.data.dao.CategoryDao
import com.localbill.recording.data.dao.RecordDao
import com.localbill.recording.data.entity.CategoryEntity
import com.localbill.recording.data.entity.RecordEntity
import com.localbill.recording.data.entity.RecordWithCategory
import com.localbill.recording.data.model.CategoryAggregation
import com.localbill.recording.data.model.KakeiboPillar
import com.localbill.recording.data.model.KakeiboPillarStat
import com.localbill.recording.data.model.PeriodSummary
import com.localbill.recording.data.model.PeriodType
import com.localbill.recording.data.model.SubCategoryAggregation
import com.localbill.recording.data.model.TrendPoint
import com.localbill.recording.util.DateTimeUtils
import kotlinx.coroutines.flow.Flow
import java.time.LocalDate
import java.time.YearMonth

class RecordRepository(
    private val recordDao: RecordDao,
    private val categoryDao: CategoryDao
) {
    val allRecordsFlow: Flow<List<RecordWithCategory>> = recordDao.getAllRecordsWithCategoryFlow()

    fun getRecordsInRangeFlow(startTime: Long, endTime: Long): Flow<List<RecordWithCategory>> {
        return recordDao.getRecordsInRangeFlow(startTime, endTime)
    }

    suspend fun getRecordsInRange(startTime: Long, endTime: Long): List<RecordWithCategory> {
        return recordDao.getRecordsInRange(startTime, endTime)
    }

    suspend fun getRecordWithCategoryById(id: Long): RecordWithCategory? {
        return recordDao.getRecordWithCategoryById(id)
    }

    suspend fun saveRecord(
        id: Long = 0,
        amount: Double,
        categoryId: Long,
        subCategoryId: Long?,
        note: String,
        timestamp: Long,
        imagePath: String?
    ): Long {
        val record = RecordEntity(
            id = id,
            amount = amount,
            categoryId = categoryId,
            subCategoryId = subCategoryId,
            note = note.trim(),
            timestamp = timestamp,
            imagePath = imagePath
        )
        return recordDao.insertRecord(record)
    }

    suspend fun deleteRecordById(id: Long) {
        recordDao.deleteRecordById(id)
    }

    /**
     * 智能推断或解析记账的 Kakeibo 四支柱属性
     */
    fun resolveKakeiboPillar(item: RecordWithCategory): KakeiboPillar {
        val note = item.record.note
        // 若备注包含 [WANTS], [CULTURE], [UNEXPECTED] 等标记直接解析
        if (note.contains("心动") || note.contains("浪费") || note.contains("想要")) return KakeiboPillar.WANTS
        if (note.contains("文化") || note.contains("学习") || note.contains("投资") || note.contains("提升")) return KakeiboPillar.CULTURE
        if (note.contains("突发") || note.contains("意外") || note.contains("维修") || note.contains("医疗")) return KakeiboPillar.UNEXPECTED

        // 根据分类名称启发式归纳
        val catName = item.displayCategoryName
        return when {
            catName.contains("学习") || catName.contains("书籍") || catName.contains("运动") -> KakeiboPillar.CULTURE
            catName.contains("娱乐") || catName.contains("数码") || catName.contains("服饰") || catName.contains("外卖") || catName.contains("零食") -> KakeiboPillar.WANTS
            catName.contains("医疗") || catName.contains("维修") || catName.contains("突发") -> KakeiboPillar.UNEXPECTED
            else -> KakeiboPillar.NEEDS
        }
    }

    /**
     * 计算日本家计簿 (Kakeibo) 四大消费支柱统计
     */
    fun calculateKakeiboPillars(records: List<RecordWithCategory>): List<KakeiboPillarStat> {
        val total = records.sumOf { it.record.amount }
        val pillarMap = mutableMapOf<KakeiboPillar, MutableList<RecordWithCategory>>()

        records.forEach { r ->
            val p = resolveKakeiboPillar(r)
            pillarMap.getOrPut(p) { mutableListOf() }.add(r)
        }

        return KakeiboPillar.values().map { pillar ->
            val list = pillarMap[pillar] ?: emptyList()
            val amount = list.sumOf { it.record.amount }
            val percentage = if (total > 0) ((amount / total) * 100).toFloat() else 0f
            KakeiboPillarStat(
                pillar = pillar,
                amount = amount,
                count = list.size,
                percentage = percentage
            )
        }
    }

    /**
     * 计算周期统计概览 (纯函数)
     */
    fun calculatePeriodSummary(records: List<RecordWithCategory>, daysCount: Int): PeriodSummary {
        if (records.isEmpty()) {
            return PeriodSummary()
        }

        val totalAmount = records.sumOf { it.record.amount }
        val dailyAverage = if (daysCount > 0) totalAmount / daysCount else totalAmount
        val recordCount = records.size
        val highest = records.maxOfOrNull { it.record.amount } ?: 0.0

        // 统计最高支出分类
        val aggregations = calculateCategoryAggregations(records)
        val topCategory = aggregations.firstOrNull()

        // 统计 Kakeibo 四支柱
        val pillars = calculateKakeiboPillars(records)

        val wantsPercentage = pillars.find { it.pillar == KakeiboPillar.WANTS }?.percentage ?: 0f
        val quote = when {
            wantsPercentage > 40f -> "本月「心动·浪費」支出偏高，适度克制冲动，把温暖留给更持久的热爱。"
            wantsPercentage < 15f -> "理智而克制的一段生活，「必要」井井有条，手账里的每一笔都在守护未来。"
            else -> "生活张弛有度，心动与必需平衡恰好，愿每一份开销都换来内心的丰盛。"
        }

        return PeriodSummary(
            totalAmount = totalAmount,
            dailyAverage = dailyAverage,
            recordCount = recordCount,
            topCategoryName = topCategory?.mainCategory?.name,
            topCategoryAmount = topCategory?.totalAmount ?: 0.0,
            highestSingleExpense = highest,
            kakeiboPillars = pillars,
            reflectionQuote = quote
        )
    }

    /**
     * 核心分类聚合算法 (纯函数)
     */
    fun calculateCategoryAggregations(records: List<RecordWithCategory>): List<CategoryAggregation> {
        if (records.isEmpty()) return emptyList()

        val totalExpense = records.sumOf { it.record.amount }
        if (totalExpense <= 0.0) return emptyList()

        // 1. 按主分类分组
        val groupedByMain = records.groupBy { it.category }
        val resultList = mutableListOf<CategoryAggregation>()

        for ((mainCategory, mainRecords) in groupedByMain) {
            val mainCategoryTotal = mainRecords.sumOf { it.record.amount }
            val mainPercentage = ((mainCategoryTotal / totalExpense) * 100).toFloat()

            // 2. 分析该主分类下的子分类明细
            val subCategoryMap = mutableMapOf<CategoryEntity, MutableList<RecordWithCategory>>()
            var directWithoutSubTotal = 0.0
            var directWithoutSubCount = 0

            for (r in mainRecords) {
                val sub = r.subCategory
                if (sub != null) {
                    subCategoryMap.getOrPut(sub) { mutableListOf() }.add(r)
                } else {
                    directWithoutSubTotal += r.record.amount
                    directWithoutSubCount++
                }
            }

            val subBreakdowns = mutableListOf<SubCategoryAggregation>()

            for ((subCat, subRecords) in subCategoryMap) {
                val subTotal = subRecords.sumOf { it.record.amount }
                val subPercentage = if (mainCategoryTotal > 0) {
                    ((subTotal / mainCategoryTotal) * 100).toFloat()
                } else 0f

                subBreakdowns.add(
                    SubCategoryAggregation(
                        category = subCat,
                        amount = subTotal,
                        count = subRecords.size,
                        percentage = subPercentage
                    )
                )
            }

            if (directWithoutSubCount > 0 && subBreakdowns.isNotEmpty()) {
                val directPercentage = ((directWithoutSubTotal / mainCategoryTotal) * 100).toFloat()
                val directPseudoCategory = CategoryEntity(
                    id = -mainCategory.id,
                    name = "直接支出",
                    iconName = mainCategory.iconName,
                    colorHex = mainCategory.colorHex,
                    parentId = mainCategory.id
                )
                subBreakdowns.add(
                    SubCategoryAggregation(
                        category = directPseudoCategory,
                        amount = directWithoutSubTotal,
                        count = directWithoutSubCount,
                        percentage = directPercentage
                    )
                )
            }

            subBreakdowns.sortByDescending { it.amount }

            resultList.add(
                CategoryAggregation(
                    mainCategory = mainCategory,
                    totalAmount = mainCategoryTotal,
                    count = mainRecords.size,
                    percentage = mainPercentage,
                    subCategoryBreakdowns = subBreakdowns
                )
            )
        }

        resultList.sortByDescending { it.totalAmount }
        return resultList
    }

    /**
     * 生成趋势走势图数据点 (纯函数)
     */
    fun calculateTrendPoints(
        records: List<RecordWithCategory>,
        periodType: PeriodType,
        selectedDate: LocalDate,
        startTime: Long
    ): List<TrendPoint> {
        return when (periodType) {
            PeriodType.DAY -> {
                val timeBuckets = listOf("0-4时", "4-8时", "8-12时", "12-16时", "16-20时", "20-24时")
                val bucketAmounts = DoubleArray(6) { 0.0 }

                for (r in records) {
                    val ldt = DateTimeUtils.toLocalDateTime(r.record.timestamp)
                    val hour = ldt.hour
                    val bucketIndex = (hour / 4).coerceIn(0, 5)
                    bucketAmounts[bucketIndex] += r.record.amount
                }

                timeBuckets.mapIndexed { index, label ->
                    TrendPoint(
                        label = label,
                        amount = bucketAmounts[index],
                        timestamp = startTime + index * 4 * 3600 * 1000L
                    )
                }
            }

            PeriodType.WEEK -> {
                val (weekStart, _) = DateTimeUtils.getWeekRange(selectedDate)
                val mondayDate = DateTimeUtils.toLocalDate(weekStart)

                val points = mutableListOf<TrendPoint>()
                val today = LocalDate.now()

                for (i in 0 until 7) {
                    val dayDate = mondayDate.plusDays(i.toLong())
                    val (dayStart, dayEnd) = DateTimeUtils.getDayRange(dayDate)

                    val dayAmount = records
                        .filter { it.record.timestamp in dayStart..dayEnd }
                        .sumOf { it.record.amount }

                    val weekdayLabel = DateTimeUtils.formatWeekday(dayDate)
                    val shortDateLabel = "${dayDate.monthValue}/${dayDate.dayOfMonth}"

                    points.add(
                        TrendPoint(
                            label = weekdayLabel,
                            subLabel = shortDateLabel,
                            amount = dayAmount,
                            timestamp = dayStart,
                            isCurrent = dayDate == today
                        )
                    )
                }
                points
            }

            PeriodType.MONTH -> {
                val yearMonth = YearMonth.from(selectedDate)
                val lengthOfMonth = yearMonth.lengthOfMonth()
                val today = LocalDate.now()

                val points = mutableListOf<TrendPoint>()
                for (day in 1..lengthOfMonth) {
                    val currentDayDate = yearMonth.atDay(day)
                    val (dayStart, dayEnd) = DateTimeUtils.getDayRange(currentDayDate)

                    val dayAmount = records
                        .filter { it.record.timestamp in dayStart..dayEnd }
                        .sumOf { it.record.amount }

                    points.add(
                        TrendPoint(
                            label = "${day}日",
                            subLabel = "${currentDayDate.monthValue}/${day}",
                            amount = dayAmount,
                            timestamp = dayStart,
                            isCurrent = currentDayDate == today
                        )
                    )
                }
                points
            }
        }
    }
}
