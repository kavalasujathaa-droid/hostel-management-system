package com.hostelmanagement.app

import com.hostelmanagement.app.core.common.CurrencyFormatter
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import java.math.BigDecimal

class CurrencyFormatterTest {

    @Test
    fun format_nullBigDecimal_returnsZeroRupees() {
        val result = CurrencyFormatter.format(null as BigDecimal?)
        assertTrue(result.contains("0.00"))
    }

    @Test
    fun format_nullOrBlankString_returnsZeroRupees() {
        assertEquals("₹0.00", CurrencyFormatter.format(null as String?))
        assertEquals("₹0.00", CurrencyFormatter.format(""))
        assertEquals("₹0.00", CurrencyFormatter.format("   "))
    }

    @Test
    fun format_exactDecimalString_formatsCorrectly() {
        val result = CurrencyFormatter.format("5000.00")
        assertTrue(result.contains("5,000") || result.contains("5000"))
        assertTrue(result.contains("₹") || result.contains("Rs") || result.contains("INR"))
    }

    @Test
    fun format_largeAmountDecimal_formatsWithPrecision() {
        val bd = BigDecimal("1250000.50")
        val result = CurrencyFormatter.format(bd)
        assertTrue(result.contains(".50"))
    }

    @Test
    fun format_doubleValue_formatsCorrectly() {
        val result = CurrencyFormatter.format(7500.0)
        assertTrue(result.contains("7,500") || result.contains("7500"))
    }
}
