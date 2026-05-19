package com.bubu.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.platform.LocalContext
import com.bubu.cycle.data.AppDatabase
import com.bubu.cycle.data.CycleRepository
import com.bubu.cycle.data.PeriodLog
import java.time.DayOfWeek
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.DateTimeFormatter

private val MonthFormatter = DateTimeFormatter.ofPattern("MMMM yyyy")

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val repo = remember { CycleRepository(AppDatabase.get(context).periodDao()) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var logs by remember { mutableStateOf<List<PeriodLog>>(emptyList()) }
    var predictedPeriod by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    var ovulationWindow by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }

    LaunchedEffect(month) {
        logs = repo.getAllLogs()
        predictedPeriod = repo.predictNextPeriodRange()
        ovulationWindow = repo.predictOvulationWindow()
    }

    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            TextButton(onClick = { month = month.minusMonths(1) }) {
                Text("Prev")
            }
            Text(text = month.format(MonthFormatter), style = MaterialTheme.typography.headlineSmall)
            TextButton(onClick = { month = month.plusMonths(1) }) {
                Text("Next")
            }
        }

        CalendarLegend()
        CalendarGrid(
            month = month,
            logs = logs,
            predictedPeriod = predictedPeriod,
            ovulationWindow = ovulationWindow
        )
    }
}

@Composable
private fun CalendarLegend() {
    Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
        LegendDot(color = MaterialTheme.colorScheme.primary.copy(alpha = 0.35f), label = "Logged period")
        LegendDot(color = MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f), label = "Predicted period")
        LegendDot(color = MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f), label = "Ovulation")
    }
}

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(6.dp)) {
        Box(
            modifier = Modifier
                .size(12.dp)
                .background(color, shape = MaterialTheme.shapes.small)
        )
        Text(text = label, style = MaterialTheme.typography.bodySmall)
    }
}

@Composable
private fun CalendarGrid(
    month: YearMonth,
    logs: List<PeriodLog>,
    predictedPeriod: Pair<LocalDate, LocalDate>?,
    ovulationWindow: Pair<LocalDate, LocalDate>?
) {
    val firstDay = month.atDay(1)
    val leadingBlanks = firstDay.dayOfWeek.value - DayOfWeek.MONDAY.value
    val daysInMonth = month.lengthOfMonth()
    val totalCells = leadingBlanks + daysInMonth
    val rows = (totalCells + 6) / 7

    val cells = buildList<LocalDate?> {
        repeat(leadingBlanks) { add(null) }
        (1..daysInMonth).forEach { add(month.atDay(it)) }
        repeat(rows * 7 - totalCells) { add(null) }
    }

    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        DayOfWeek.entries.forEach { day ->
            Text(
                text = day.name.take(3),
                modifier = Modifier.weight(1f),
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.SemiBold
            )
        }
    }

    cells.chunked(7).forEach { week ->
        Row(modifier = Modifier.fillMaxWidth()) {
            week.forEach { date ->
                DayCell(
                    date = date,
                    logs = logs,
                    predictedPeriod = predictedPeriod,
                    ovulationWindow = ovulationWindow,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun DayCell(
    date: LocalDate?,
    logs: List<PeriodLog>,
    predictedPeriod: Pair<LocalDate, LocalDate>?,
    ovulationWindow: Pair<LocalDate, LocalDate>?,
    modifier: Modifier = Modifier
) {
    val logged = date != null && logs.any { date >= it.start() && date <= it.end() }
    val predicted = date != null && predictedPeriod?.let { date >= it.first && date <= it.second } == true
    val ovulation = date != null && ovulationWindow?.let { date >= it.first && date <= it.second } == true

    val background = when {
        logged -> MaterialTheme.colorScheme.primary.copy(alpha = 0.35f)
        predicted -> MaterialTheme.colorScheme.secondary.copy(alpha = 0.35f)
        ovulation -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
        else -> Color.Transparent
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(background, shape = MaterialTheme.shapes.small),
        contentAlignment = Alignment.Center
    ) {
        Text(text = date?.dayOfMonth?.toString() ?: "", style = MaterialTheme.typography.bodySmall)
    }
}
