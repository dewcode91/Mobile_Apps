package com.bubu.cycle.data

import android.content.Context

data class ReminderSettings(
    val enabled: Boolean,
    val hour: Int,
    val minute: Int
)

class SettingsRepository(context: Context) {
    private val prefs = SecurePrefs.get(context)

    fun getReminderSettings(): ReminderSettings {
        val enabled = prefs.getBoolean("reminder_enabled", true)
        val hour = prefs.getInt("reminder_hour", 9)
        val minute = prefs.getInt("reminder_minute", 0)
        return ReminderSettings(enabled, hour, minute)
    }

    fun setReminderSettings(settings: ReminderSettings) {
        prefs.edit()
            .putBoolean("reminder_enabled", settings.enabled)
            .putInt("reminder_hour", settings.hour)
            .putInt("reminder_minute", settings.minute)
            .apply()
    }
}
