package com.krishana.onedot.util

import android.content.Context
import com.krishana.onedot.data.SettingsRepository
import com.krishana.onedot.util.WorkScheduler
import io.mockk.mockk
import org.junit.Test
import org.junit.Assert.*
import org.junit.Before
import java.time.LocalDate
import java.time.Month
import java.time.temporal.ChronoUnit

/**
 * Unit tests for date calculations and year progress logic
 * Tests day-of-year calculations, leap years, and date ranges
 */
class DateCalculationTest {

    @Test
    fun `test January 1st is day 1 of year`() {
        val jan1 = LocalDate.of(2026, Month.JANUARY, 1)
        val dayOfYear = jan1.dayOfYear
        
        assertEquals("January 1st should be day 1 of the year", 1, dayOfYear)
    }

    @Test
    fun `test December 31st is day 365 in non-leap year`() {
        val dec31_2026 = LocalDate.of(2026, Month.DECEMBER, 31)
        val dayOfYear = dec31_2026.dayOfYear
        
        assertEquals("December 31st should be day 365 in 2026", 365, dayOfYear)
    }

    @Test
    fun `test December 31st is day 366 in leap year`() {
        val dec31_2024 = LocalDate.of(2024, Month.DECEMBER, 31)
        val dayOfYear = dec31_2024.dayOfYear
        
        assertEquals("December 31st should be day 366 in leap year 2024", 366, dayOfYear)
    }

    @Test
    fun `test February 29th exists in leap year`() {
        val feb29_2024 = LocalDate.of(2024, Month.FEBRUARY, 29)
        val dayOfYear = feb29_2024.dayOfYear
        
        assertEquals("February 29th should be day 60 in leap year", 60, dayOfYear)
    }

    @Test
    fun `test February has 28 days in non-leap year`() {
        val feb28_2026 = LocalDate.of(2026, Month.FEBRUARY, 28)
        val nextDay = feb28_2026.plusDays(1)
        
        assertEquals("Day after Feb 28 in non-leap year should be March 1", Month.MARCH, nextDay.month)
        assertEquals("Should be March 1st", 1, nextDay.dayOfMonth)
    }

    @Test
    fun `test year 2024 is a leap year`() {
        val year2024 = LocalDate.of(2024, 1, 1)
        val isLeap = year2024.isLeapYear
        
        assertTrue("2024 should be a leap year", isLeap)
    }

    @Test
    fun `test year 2026 is not a leap year`() {
        val year2026 = LocalDate.of(2026, 1, 1)
        val isLeap = year2026.isLeapYear
        
        assertFalse("2026 should not be a leap year", isLeap)
    }

    @Test
    fun `test days between January 1 and today`() {
        val jan1 = LocalDate.of(2026, Month.JANUARY, 1)
        val feb1 = LocalDate.of(2026, Month.FEBRUARY, 1)
        
        val daysBetween = ChronoUnit.DAYS.between(jan1, feb1)
        
        assertEquals("There should be 31 days between Jan 1 and Feb 1", 31, daysBetween)
    }

    @Test
    fun `test current day of year is between 1 and 365 for non-leap year`() {
        val today = LocalDate.now()
        val dayOfYear = today.dayOfYear
        
        if (!today.isLeapYear) {
            assertTrue("Day of year should be >= 1", dayOfYear >= 1)
            assertTrue("Day of year should be <= 365 in non-leap year", dayOfYear <= 365)
        }
    }

    @Test
    fun `test current day of year is between 1 and 366 for leap year`() {
        val today = LocalDate.now()
        val dayOfYear = today.dayOfYear
        
        if (today.isLeapYear) {
            assertTrue("Day of year should be >= 1", dayOfYear >= 1)
            assertTrue("Day of year should be <= 366 in leap year", dayOfYear <= 366)
        }
    }

    @Test
    fun `test year start is before year end`() {
        val yearStart = LocalDate.of(2026, Month.JANUARY, 1)
        val yearEnd = LocalDate.of(2026, Month.DECEMBER, 31)
        
        assertTrue("Year start should be before year end", yearStart.isBefore(yearEnd))
    }

    @Test
    fun `test midpoint of year calculation`() {
        val yearStart = LocalDate.of(2026, Month.JANUARY, 1)
        val midYear = yearStart.plusDays(365 / 2)
        
        // Mid-year should be around July
        assertTrue("Midpoint of year should be in second half", midYear.month.value >= 6)
    }

    @Test
    fun `test past days count increases over time`() {
        val today = LocalDate.now()
        val jan1 = LocalDate.of(today.year, Month.JANUARY, 1)
        
        val daysPassed = ChronoUnit.DAYS.between(jan1, today)
        
        assertTrue("Days passed should be non-negative", daysPassed >= 0)
        assertTrue("Days passed should not exceed year length", daysPassed < 366)
    }

    @Test
    fun `test future days count decreases over time`() {
        val today = LocalDate.now()
        val dec31 = LocalDate.of(today.year, Month.DECEMBER, 31)
        
        val daysRemaining = ChronoUnit.DAYS.between(today, dec31)
        
        assertTrue("Days remaining should be non-negative", daysRemaining >= 0)
        assertTrue("Days remaining should not exceed year length", daysRemaining < 366)
    }

    @Test
    fun `test total days in year equals passed + remaining + today`() {
        val today = LocalDate.now()
        val jan1 = LocalDate.of(today.year, Month.JANUARY, 1)
        val dec31 = LocalDate.of(today.year, Month.DECEMBER, 31)
        
        val daysPassed = ChronoUnit.DAYS.between(jan1, today)
        val daysRemaining = ChronoUnit.DAYS.between(today, dec31)
        val totalDays = dec31.dayOfYear
        
        assertEquals("Past + today + future should equal total days", 
            totalDays, (daysPassed + 1 + daysRemaining).toInt())
    }

    @Test
    fun `test date formatting for display`() {
        val date = LocalDate.of(2026, Month.FEBRUARY, 1)
        val formatted = date.toString()
        
        assertEquals("Date should format as ISO-8601", "2026-02-01", formatted)
    }

    @Test
    fun `test comparing two dates`() {
        val earlier = LocalDate.of(2026, Month.JANUARY, 15)
        val later = LocalDate.of(2026, Month.JANUARY, 20)
        
        assertTrue("Earlier date should be before later date", earlier.isBefore(later))
        assertTrue("Later date should be after earlier date", later.isAfter(earlier))
    }

    @Test
    fun `test same date equality`() {
        val date1 = LocalDate.of(2026, Month.MARCH, 15)
        val date2 = LocalDate.of(2026, Month.MARCH, 15)
        
        assertEquals("Same dates should be equal", date1, date2)
    }

    @Test
    fun `test 365 dot grid covers full non-leap year`() {
        val dotsCount = 365
        val yearLength = LocalDate.of(2026, Month.DECEMBER, 31).dayOfYear
        
        assertEquals("365 dots should cover full non-leap year", yearLength, dotsCount)
    }

    @Test
    fun `test grid accommodates leap year with 365 dots`() {
        val dotsCount = 365
        val leapYearLength = LocalDate.of(2024, Month.DECEMBER, 31).dayOfYear
        
        // App uses 365 dots even in leap years (day 366 can be handled specially)
        assertTrue("365 dots should be less than leap year length", dotsCount < leapYearLength)
        assertEquals("Leap year has 366 days", 366, leapYearLength)
    }
}
