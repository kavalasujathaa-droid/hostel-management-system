package com.hostelmanagement.app.core.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.hostelmanagement.app.MainActivity
import com.hostelmanagement.app.R

object SmartTrackNotificationHelper {

    const val CHANNEL_PAYMENTS = "smarttrack_payments"
    const val CHANNEL_REMINDERS = "smarttrack_reminders"
    const val CHANNEL_COMPLAINTS = "smarttrack_complaints"
    const val CHANNEL_ALERTS = "smarttrack_alerts"

    const val EXTRA_DESTINATION = "extra_destination"
    const val EXTRA_TARGET_ID = "extra_target_id"

    fun createNotificationChannels(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as? NotificationManager
                ?: return

            val channels = listOf(
                NotificationChannel(
                    CHANNEL_PAYMENTS,
                    "Payments",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Notifications for payment submissions, approvals, and rejections"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_REMINDERS,
                    "Rent Reminders",
                    NotificationManager.IMPORTANCE_HIGH
                ).apply {
                    description = "Timely reminders for upcoming rent payments and overdue notices"
                    enableVibration(true)
                },
                NotificationChannel(
                    CHANNEL_COMPLAINTS,
                    "Complaints",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Status updates and activity on tenant complaints"
                },
                NotificationChannel(
                    CHANNEL_ALERTS,
                    "General Alerts",
                    NotificationManager.IMPORTANCE_DEFAULT
                ).apply {
                    description = "Hostel management announcements and general system alerts"
                }
            )

            channels.forEach { channel ->
                notificationManager.createNotificationChannel(channel)
            }
        }
    }

    fun showNotification(
        context: Context,
        channelId: String,
        notificationId: Int,
        title: String,
        message: String,
        destination: String? = null,
        targetId: Long? = null
    ) {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            destination?.let { putExtra(EXTRA_DESTINATION, it) }
            targetId?.let { putExtra(EXTRA_TARGET_ID, it) }
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            notificationId,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val builder = NotificationCompat.Builder(context, channelId)
            .setSmallIcon(R.drawable.ic_smarttrack_logo)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFF149EA5.toInt()) // BrandTeal color
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)

        try {
            val manager = NotificationManagerCompat.from(context)
            manager.notify(notificationId, builder.build())
        } catch (_: SecurityException) {
            // Permission not granted on Android 13+; safely handled
        }
    }
}
