package com.bubu.cycle.notifications

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.bubu.cycle.data.AppDatabase
import com.bubu.cycle.data.CycleRepository
import com.bubu.cycle.data.DEFAULT_SYMPTOM_PROMPT
import com.bubu.cycle.data.SettingsRepository
import java.time.format.DateTimeFormatter

private const val CHANNEL_ID = "cycle_reminders"
private val DateFormatter = DateTimeFormatter.ofPattern("MMM d")

class ReminderWorker(
    appContext: Context,
    params: WorkerParameters
) : CoroutineWorker(appContext, params) {
    override suspend fun doWork(): Result {
        if (Build.VERSION.SDK_INT >= 33) {
            val permissionGranted = ContextCompat.checkSelfPermission(
                applicationContext,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED

            if (!permissionGranted) return Result.success()
        }

        val repo = CycleRepository(AppDatabase.get(applicationContext).periodDao())
        val settings = SettingsRepository(applicationContext).getReminderSettings()
        val nextPeriod = repo.predictNextPeriodStart()?.format(DateFormatter) ?: "Not enough data"
        val ovulation = repo.predictOvulationWindow()?.let {
            "${it.first.format(DateFormatter)} - ${it.second.format(DateFormatter)}"
        } ?: "Not enough data"
        val symptomsText = settings.trackedSymptoms
            .toList()
            .sorted()
            .joinToString(separator = ", ")
            .ifBlank { DEFAULT_SYMPTOM_PROMPT }

        val message = buildString {
            appendLine("Next period: $nextPeriod")
            appendLine("Ovulation: $ovulation")
            append("Track today: $symptomsText")
        }
        showNotification("Cycle reminder", message)
        return Result.success()
    }

    private fun showNotification(title: String, message: String) {
        val notificationManager = NotificationManagerCompat.from(applicationContext)
        createChannelIfNeeded(notificationManager)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentTitle(title)
            .setContentText(message)
            .setStyle(NotificationCompat.BigTextStyle().bigText(message))
            .setAutoCancel(true)
            .build()

        notificationManager.notify(1001, notification)
    }

    private fun createChannelIfNeeded(notificationManager: NotificationManagerCompat) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Cycle reminders",
                NotificationManager.IMPORTANCE_DEFAULT
            )
            notificationManager.createNotificationChannel(channel)
        }
    }
}
