package com.bubu.cycle.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.bubu.cycle.data.AppDatabase
import com.bubu.cycle.data.CycleRepository
import com.bubu.cycle.ui.components.StatsCard
import java.time.format.DateTimeFormatter

private val DateFormatter = DateTimeFormatter.ofPattern("MMM d")

data class DashboardState(
    val nextPeriod: String = "—",
    val ovulation: String = "—",
    val avgCycle: String = "—",
    val lastPeriod: String = "—"
)

@Composable
fun DashboardScreen() {
    val context = LocalContext.current
    val repo = remember { CycleRepository(AppDatabase.get(context).periodDao()) }
    var state by remember { mutableStateOf(DashboardState()) }

    LaunchedEffect(Unit) {
        val avgCycle = repo.averageCycleLength()?.let { "$it days" } ?: "—"
        val nextPeriod = repo.predictNextPeriodStart()?.format(DateFormatter) ?: "Add 2+ logs"
        val ovulation = repo.predictOvulationWindow()?.let {
            "${it.first.format(DateFormatter)} - ${it.second.format(DateFormatter)}"
        } ?: "—"
        val last = repo.latestLog()?.let {
            "${it.start().format(DateFormatter)} - ${it.end().format(DateFormatter)}"
        } ?: "—"
        state = DashboardState(nextPeriod = nextPeriod, ovulation = ovulation, avgCycle = avgCycle, lastPeriod = last)
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(text = "Overview", style = MaterialTheme.typography.headlineSmall)
        StatsCard(title = "Next period", value = state.nextPeriod)
        StatsCard(title = "Ovulation window", value = state.ovulation)
        StatsCard(title = "Cycle length avg", value = state.avgCycle)
        StatsCard(title = "Last period", value = state.lastPeriod)
        Text(
            text = "Log at least two cycles to enable more accurate predictions.",
            style = MaterialTheme.typography.bodyMedium
        )
    }
}
