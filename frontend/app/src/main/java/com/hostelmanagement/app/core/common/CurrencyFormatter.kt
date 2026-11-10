package com.hostelmanagement.app.core.common

import java.math.BigDecimal
import java.text.NumberFormat
import java.util.Locale

object CurrencyFormatter {
    private val indianLocale = Locale("en", "IN")

    fun format(amount: BigDecimal?): String {
        if (amount == null) return "₹0.00"
        return try {
            val formatter = NumberFormat.getCurrencyInstance(indianLocale)
            formatter.maximumFractionDigits = 2
            formatter.minimumFractionDigits = 2
            formatter.format(amount)
        } catch (e: Exception) {
            "₹${amount.setScale(2)}"
        }
    }

    fun format(amountStr: String?): String {
        if (amountStr.isNullOrBlank()) return "₹0.00"
        return try {
            val bd = BigDecimal(amountStr)
            format(bd)
        } catch (e: Exception) {
            "₹$amountStr"
        }
    }

    fun format(amountDouble: Double?): String {
        if (amountDouble == null) return "₹0.00"
        return format(BigDecimal.valueOf(amountDouble))
    }
}
