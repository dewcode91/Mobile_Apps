package com.bubu.cycle.data

import org.json.JSONArray
import org.json.JSONObject
import java.time.Instant

class BackupRepository(
    private val cycleRepository: CycleRepository,
    private val settingsRepository: SettingsRepository
) {
    suspend fun exportBackupJson(): String {
        val logs = cycleRepository.getAllLogs().sortedByDescending { it.startDate }
        val settings = settingsRepository.getReminderSettings()

        val logsJson = JSONArray()
        logs.forEach { log ->
            logsJson.put(
                JSONObject()
                    .put("startDate", log.startDate)
                    .put("endDate", log.endDate)
            )
        }

        return JSONObject()
            .put("app", "Cycle Tracker")
            .put("version", 1)
            .put("exportedAtUtc", Instant.now().toString())
            .put(
                "reminderSettings",
                JSONObject()
                    .put("enabled", settings.enabled)
                    .put("hour", settings.hour)
                    .put("minute", settings.minute)
                    .put("trackedSymptoms", JSONArray(settings.trackedSymptoms.toList().sorted()))
            )
            .put("periodLogs", logsJson)
            .toString(2)
    }
}
