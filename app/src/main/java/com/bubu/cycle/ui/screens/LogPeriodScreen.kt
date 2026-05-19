package com.bubu.cycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.bubu.cycle.data.AppDatabase
import com.bubu.cycle.data.CycleRepository
import com.bubu.cycle.notifications.ReminderScheduler
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.format.DateTimeFormatter

private val InputFormatter = DateTimeFormatter.ofPattern("dd-MM-yyyy")

@Composable
fun LogPeriodScreen() {
    val context = LocalContext.current
    val repo = remember { CycleRepository(AppDatabase.get(context).periodDao()) }
    val scope = rememberCoroutineScope()

    var startDate by remember { mutableStateOf("") }
    var endDate by remember { mutableStateOf("") }
    var message by remember { mutableStateOf("") }
    var refreshKey by remember { mutableStateOf(0) }
    var logs by remember { mutableStateOf(emptyList<com.bubu.cycle.data.PeriodLog>()) }

    LaunchedEffect(refreshKey) {
        logs = repo.getAllLogs()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Log Period", style = MaterialTheme.typography.headlineSmall)
        OutlinedTextField(
            value = startDate,
            onValueChange = { startDate = it },
            label = { Text("Start date (DD-MM-YYYY)") },
            modifier = Modifier.fillMaxWidth()
        )
        OutlinedTextField(
            value = endDate,
            onValueChange = { endDate = it },
            label = { Text("End date (DD-MM-YYYY)") },
            modifier = Modifier.fillMaxWidth()
        )
        Button(
            onClick = {
                scope.launch {
                    try {
                        val start = LocalDate.parse(startDate.trim(), InputFormatter)
                        val end = LocalDate.parse(endDate.trim(), InputFormatter)
                        require(!end.isBefore(start))
                        repo.addLog(start.toString(), end.toString())
                        startDate = ""
                        endDate = ""
                        message = "Saved"
                        refreshKey++
                        ReminderScheduler.ensureDailyReminder(context)
                    } catch (e: Exception) {
                        message = "Please enter valid dates (DD-MM-YYYY)"
                    }
                }
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Save")
        }
        if (message.isNotBlank()) {
            Text(text = message, style = MaterialTheme.typography.bodyMedium)
        }

        if (logs.isNotEmpty()) {
            Text(text = "Recent logs", style = MaterialTheme.typography.titleMedium)
            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                logs.take(5).forEach { log ->
                    val start = log.start().format(InputFormatter)
                    val end = log.end().format(InputFormatter)
                    Text("$start → $end")
                }
            }
        }
    }
}