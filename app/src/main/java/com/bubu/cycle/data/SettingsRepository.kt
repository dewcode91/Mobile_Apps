package com.bubu.cycle.data

import android.content.Context

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int,
    val trackedSymptoms: Set<String>
)

class SettingsRepository(context: Context) {
    private val prefs = SecurePrefs.get(context)

    fun getReminderSettings(): ReminderSettings {
        val enabled = prefs.getBoolean("reminder_enabled", true)
        val hour = prefs.getInt("reminder_hour", 9)
        val minute = prefs.getInt("reminder_minute", 0)
        val trackedSymptoms = prefs.getStringSet("tracked_symptoms", emptySet()).orEmpty()
        return ReminderSettings(enabled, hour, minute, trackedSymptoms)
    }

    fun setReminderSettings(settings: ReminderSettings) {
        prefs.edit()
            .putBoolean("reminder_enabled", settings.enabled)
            .putInt("reminder_hour", settings.hour)
            .putInt("reminder_minute", settings.minute)
            .putStringSet("tracked_symptoms", settings.trackedSymptoms)
            .apply()
    }
}
