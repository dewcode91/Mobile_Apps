package com.bubu.cycle.ui.screens

import android.Manifest
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.bubu.cycle.data.SettingsRepository
import com.bubu.cycle.data.ReminderSettings
import com.bubu.cycle.notifications.ReminderScheduler

@Composable
fun SettingsScreen() {
    val context = LocalContext.current
    val repo = remember { SettingsRepository(context) }

    var enabled by remember { mutableStateOf(true) }
    var hour by remember { mutableStateOf("9") }
    var minute by remember { mutableStateOf("00") }
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
                repo.setReminderSettings(ReminderSettings(enabled = enabled, hour = h, minute = m))
                ReminderScheduler.ensureDailyReminder(context)
                message = "Settings saved"
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save settings")
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
    }
}
