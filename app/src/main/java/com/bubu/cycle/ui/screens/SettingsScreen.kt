package com.bubu.cycle.ui.screens

import android.Manifest
import android.content.Intent
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.width
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.bubu.cycle.data.AppDatabase
import com.bubu.cycle.data.BackupRepository
import com.bubu.cycle.data.CycleRepository
import com.bubu.cycle.data.SettingsRepository
import com.bubu.cycle.data.ReminderSettings
import com.bubu.cycle.notifications.ReminderScheduler
import kotlinx.coroutines.launch

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val clipboardManager = LocalClipboardManager.current
    val repo = remember { SettingsRepository(context) }
    val cycleRepo = remember { CycleRepository(AppDatabase.get(context).periodDao()) }
    val backupRepo = remember { BackupRepository(cycleRepo, repo) }
    val scope = rememberCoroutineScope()
    val symptomOptions = remember { listOf("Cramps", "Bloating", "Headache", "Mood swings", "Low energy") }

    var enabled by remember { mutableStateOf(true) }
    var hour by remember { mutableStateOf("9") }
    var minute by remember { mutableStateOf("00") }
    var trackedSymptoms by remember { mutableStateOf(setOf<String>()) }
    var message by remember { mutableStateOf("") }

    val permissionLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { granted ->
        message = if (granted) "Notifications enabled" else "Notifications permission denied"
    }

    LaunchedEffect(Unit) {
        val settings = repo.getReminderSettings()
        enabled = settings.enabled
        hour = settings.hour.toString()
        minute = settings.minute.toString().padStart(2, '0')
        trackedSymptoms = settings.trackedSymptoms
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Settings", style = MaterialTheme.typography.headlineSmall)
        Row(verticalAlignment = androidx.compose.ui.Alignment.CenterVertically) {
            Text("Daily reminder")
            Spacer(modifier = Modifier.width(12.dp))
            Switch(checked = enabled, onCheckedChange = { enabled = it })
        }
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            OutlinedTextField(
                value = hour,
                onValueChange = { hour = it.filter { ch -> ch.isDigit() }.take(2) },
                label = { Text("Hour (0-23)") },
                modifier = Modifier.weight(1f)
            )
            OutlinedTextField(
                value = minute,
                onValueChange = { minute = it.filter { ch -> ch.isDigit() }.take(2) },
                label = { Text("Minute (0-59)") },
                modifier = Modifier.weight(1f)
            )
        }
        Button(
            onClick = {
                val h = hour.toIntOrNull() ?: -1
                val m = minute.toIntOrNull() ?: -1
                if (h !in 0..23 || m !in 0..59) {
                    message = "Enter a valid time"
                    return@Button
                }
                repo.setReminderSettings(
                    ReminderSettings(
                        enabled = enabled,
                        hour = h,
                        minute = m,
                        trackedSymptoms = trackedSymptoms
                    )
                )
                ReminderScheduler.ensureDailyReminder(context)
                message = "Settings saved"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save settings")
        }

        Text(text = "Symptom tracking", style = MaterialTheme.typography.titleMedium)
        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            symptomOptions.forEach { symptom ->
                val selected = symptom in trackedSymptoms
                FilterChip(
                    selected = selected,
                    onClick = {
                        trackedSymptoms = if (selected) {
                            trackedSymptoms - symptom
                        } else {
                            trackedSymptoms + symptom
                        }
                    },
                    label = { Text(symptom) }
                )
            }
        }

        Button(
            onClick = {
                scope.launch {
                    val exportJson = backupRepo.exportBackupJson()
                    clipboardManager.setText(AnnotatedString(exportJson))
                    val shareIntent = Intent(Intent.ACTION_SEND)
                        .setType("application/json")
                        .putExtra(Intent.EXTRA_SUBJECT, "Cycle Tracker Offline Backup")
                        .putExtra(Intent.EXTRA_TEXT, exportJson)
                    context.startActivity(Intent.createChooser(shareIntent, "Export backup"))
                    message = "Backup copied to clipboard and ready to share offline"
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Export offline backup (JSON)")
        }

        if (Build.VERSION.SDK_INT >= 33) {
            val granted = ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == android.content.pm.PackageManager.PERMISSION_GRANTED

            if (!granted) {
                Button(onClick = {
                    permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }) {
                    Text("Enable notifications")
                }
            }
        }

        if (message.isNotBlank()) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }

        Text(
            text = "Reminders run offline and use local predictions only.",
            style = MaterialTheme.typography.bodySmall
        )
        Text(
            text = "Backups stay local unless you manually share the exported JSON.",
            style = MaterialTheme.typography.bodySmall
        )
    }
}
