package com.hostelmanagement.app.core.common

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object DateFormatter {
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val friendlyDateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
    private val isoDateTimeFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss", Locale.getDefault())
    private val friendlyDateTimeFormat = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale.getDefault())

    fun formatIsoDate(isoDate: String?): String {
        if (isoDate.isNullOrBlank()) return "—"
        return try {
            val cleanDate = if (isoDate.length >= 10) isoDate.substring(0, 10) else isoDate
            val date: Date = isoDateFormat.parse(cleanDate) ?: return isoDate
            friendlyDateFormat.format(date)
        } catch (e: Exception) {
            isoDate
        }
    }

    fun formatDate(isoDate: String?): String = formatIsoDate(isoDate)

    fun formatIsoDateTime(isoDateTime: String?): String {
        if (isoDateTime.isNullOrBlank()) return "—"
        return try {
            val cleanDate = if (isoDateTime.length >= 19) isoDateTime.substring(0, 19) else isoDateTime
            val date: Date = isoDateTimeFormat.parse(cleanDate) ?: return isoDateTime
            friendlyDateTimeFormat.format(date)
        } catch (e: Exception) {
            isoDateTime
        }
    }

    fun formatDateTime(isoDateTime: String?): String = formatIsoDateTime(isoDateTime)

    fun getMonthName(monthNumber: Int): String {
        val months = arrayOf(
            "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"
        )
        return if (monthNumber in 1..12) months[monthNumber - 1] else "Month $monthNumber"
    }
}
