package com.example.smartexpense.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.smartexpense.R
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NotificationHelper @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    companion object {
        const val BUDGET_CHANNEL_ID = "budget_alerts"
        const val REPORT_CHANNEL_ID = "spending_reports"
    }

    init {
        createNotificationChannels()
    }

    private fun createNotificationChannels() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val budgetChannel = NotificationChannel(
                BUDGET_CHANNEL_ID,
                "Budget Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Alerts when you approach or exceed your budget"
            }

            val reportChannel = NotificationChannel(
                REPORT_CHANNEL_ID,
                "Spending Reports",
                NotificationManager.IMPORTANCE_DEFAULT
            ).apply {
                description = "Periodic spending insights"
            }

            notificationManager.createNotificationChannel(budgetChannel)
            notificationManager.createNotificationChannel(reportChannel)
        }
    }

    fun showBudgetAlert(category: String, percentage: Int, isCritical: Boolean) {
        val title = if (isCritical) "Critical Budget Alert! 🚨" else "Budget Warning ⚠️"
        val message = if (isCritical) 
            "You have exceeded your budget for $category ($percentage%)"
            else "You have used $percentage% of your $category budget."

        val builder = NotificationCompat.Builder(context, BUDGET_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(if (isCritical) NotificationCompat.PRIORITY_HIGH else NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(category.hashCode(), builder.build())
    }

    fun showReportNotification(title: String, message: String) {
        val builder = NotificationCompat.Builder(context, REPORT_CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)

        notificationManager.notify(title.hashCode(), builder.build())
    }
}
