package com.localbill.recording.util

import java.time.DayOfWeek
import java.time.Instant
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.temporal.TemporalAdjusters
import java.util.Locale

object DateTimeUtils {

    private val zoneId: ZoneId = ZoneId.systemDefault()

    val DATE_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd", Locale.getDefault())
    val DATE_DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("M月d日 EEEE", Locale.CHINESE)
    val MONTH_DISPLAY_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy年M月", Locale.CHINESE)
    val TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("HH:mm", Locale.getDefault())
    val DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm", Locale.getDefault())
    val CSV_DATE_TIME_FORMATTER: DateTimeFormatter = DateTimeFormatter.ofPattern("yyyy/MM/dd HH:mm:ss", Locale.getDefault())

    fun toMillis(localDateTime: LocalDateTime): Long {
        return localDateTime.atZone(zoneId).toInstant().toEpochMilli()
    }

    fun toLocalDateTime(millis: Long): LocalDateTime {
        return LocalDateTime.ofInstant(Instant.ofEpochMilli(millis), zoneId)
    }

    fun toLocalDate(millis: Long): LocalDate {
        return toLocalDateTime(millis).toLocalDate()
    }

    /**
     * 获取指定日期的起止时间戳 (00:00:00.000 - 23:59:59.999)
     */
    fun getDayRange(date: LocalDate): Pair<Long, Long> {
        val start = date.atStartOfDay()
        val end = date.atTime(LocalTime.MAX)
        return Pair(toMillis(start), toMillis(end))
    }

    /**
     * 获取指定日期所在周的起止时间戳（周一至周日）
     */
    fun getWeekRange(date: LocalDate): Pair<Long, Long> {
        val monday = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
        val sunday = date.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY))
        val start = monday.atStartOfDay()
        val end = sunday.atTime(LocalTime.MAX)
        return Pair(toMillis(start), toMillis(end))
    }

    /**
     * 获取指定年月的起止时间戳（1号至最后一天）
     */
    fun getMonthRange(yearMonth: YearMonth): Pair<Long, Long> {
        val start = yearMonth.atDay(1).atStartOfDay()
        val end = yearMonth.atEndOfMonth().atTime(LocalTime.MAX)
        return Pair(toMillis(start), toMillis(end))
    }

    /**
     * 格式化友好日期标签
     */
    fun formatFriendlyDate(timestamp: Long): String {
        val date = toLocalDate(timestamp)
        val today = LocalDate.now()
        val yesterday = today.minusDays(1)

        return when (date) {
            today -> "今天 " + toLocalDateTime(timestamp).format(TIME_FORMATTER)
            yesterday -> "昨天 " + toLocalDateTime(timestamp).format(TIME_FORMATTER)
            else -> toLocalDateTime(timestamp).format(DATE_TIME_FORMATTER)
        }
    }

    /**
     * 格式化简写月份日
     */
    fun formatShortDate(timestamp: Long): String {
        val date = toLocalDate(timestamp)
        return "${date.monthValue}/${date.dayOfMonth}"
    }

    /**
     * 格式化星期几简称
     */
    fun formatWeekday(date: LocalDate): String {
        return when (date.dayOfWeek) {
            DayOfWeek.MONDAY -> "周一"
            DayOfWeek.TUESDAY -> "周二"
            DayOfWeek.WEDNESDAY -> "周三"
            DayOfWeek.THURSDAY -> "周四"
            DayOfWeek.FRIDAY -> "周五"
            DayOfWeek.SATURDAY -> "周六"
            DayOfWeek.SUNDAY -> "周日"
            else -> ""
        }
    }
}
