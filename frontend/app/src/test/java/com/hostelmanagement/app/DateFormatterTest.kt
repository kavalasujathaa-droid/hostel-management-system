package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.DateFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DateFormatterTest {

    @Test
    fun formatIsoDate_nullOrBlank_returnsDash() {
        assertEquals("—", DateFormatter.formatIsoDate(null))
        assertEquals("—", DateFormatter.formatIsoDate(""))
        assertEquals("—", DateFormatter.formatIsoDate("   "))
    }

    @Test
    fun formatIsoDate_validDateString_returnsFormattedDate() {
        val result = DateFormatter.formatIsoDate("2026-11-10")
        assertNotNull(result)
        assertTrue(result.contains("2026") || result.contains("Nov"))
    }

    @Test
    fun formatIsoDateTime_validDateTimeString_returnsFormattedDateTime() {
        val result = DateFormatter.formatIsoDateTime("2026-11-10T14:30:00")
        assertNotNull(result)
        assertTrue(result.contains("2026") || result.contains("Nov"))
    }

    @Test
    fun getMonthName_validMonths_returnsCorrectName() {
        assertEquals("January", DateFormatter.getMonthName(1))
        assertEquals("July", DateFormatter.getMonthName(7))
        assertEquals("December", DateFormatter.getMonthName(12))
    }

    @Test
    fun getMonthName_invalidMonth_returnsFallback() {
        assertEquals("Month 0", DateFormatter.getMonthName(0))
        assertEquals("Month 13", DateFormatter.getMonthName(13))
    }
}
