package com.localbill.recording

import com.localbill.recording.util.DateTimeUtils
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.YearMonth

class DateTimeUtilsTest {

    @Test
    fun testDayRangeCalculation() {
        val date = LocalDate.of(2026, 8, 23)
        val (start, end) = DateTimeUtils.getDayRange(date)

        val startLdt = DateTimeUtils.toLocalDateTime(start)
        val endLdt = DateTimeUtils.toLocalDateTime(end)

        assertEquals(2026, startLdt.year)
        assertEquals(8, startLdt.monthValue)
        assertEquals(23, startLdt.dayOfMonth)
        assertEquals(0, startLdt.hour)
        assertEquals(0, startLdt.minute)
        assertEquals(0, startLdt.second)

        assertEquals(2026, endLdt.year)
        assertEquals(8, endLdt.monthValue)
        assertEquals(23, endLdt.dayOfMonth)
        assertEquals(23, endLdt.hour)
        assertEquals(59, endLdt.minute)

        assertTrue(end > start)
    }

    @Test
    fun testWeekRangeCalculation() {
        // 2026-08-23 is Sunday
        val sunday = LocalDate.of(2026, 8, 23)
        val (start, end) = DateTimeUtils.getWeekRange(sunday)

        val startLdt = DateTimeUtils.toLocalDateTime(start)
        val endLdt = DateTimeUtils.toLocalDateTime(end)

        // Monday should be 2026-08-17
        assertEquals(17, startLdt.dayOfMonth)
        assertEquals(8, startLdt.monthValue)

        // Sunday should be 2026-08-23
        assertEquals(23, endLdt.dayOfMonth)
        assertEquals(8, endLdt.monthValue)
    }

    @Test
    fun testMonthRangeCalculation() {
        val ym = YearMonth.of(2026, 8)
        val (start, end) = DateTimeUtils.getMonthRange(ym)

        val startLdt = DateTimeUtils.toLocalDateTime(start)
        val endLdt = DateTimeUtils.toLocalDateTime(end)

        assertEquals(1, startLdt.dayOfMonth)
        assertEquals(31, endLdt.dayOfMonth)
    }

    @Test
    fun testWeekdayFormatting() {
        val date = LocalDate.of(2026, 8, 23) // Sunday
        val weekday = DateTimeUtils.formatWeekday(date)
        assertEquals("周日", weekday)

        val monday = LocalDate.of(2026, 8, 17)
        assertEquals("周一", DateTimeUtils.formatWeekday(monday))
    }
}
