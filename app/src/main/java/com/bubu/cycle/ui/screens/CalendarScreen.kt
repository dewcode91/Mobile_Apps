package com.bubu.cycle.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
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
private const val STATUS_DAY_ALPHA = 0.24f
private const val DEFAULT_DAY_ALPHA = 0.18f
private val MARKER_SIZE = 5.dp

private data class CalendarAnalytics(
    val loggedDays: Int = 0,
    val predictedDays: Int = 0,
    val ovulationDays: Int = 0,
    val periodEntries: Int = 0
)

@Composable
fun CalendarScreen() {
    val context = LocalContext.current
    val repo = remember { CycleRepository(AppDatabase.get(context).periodDao()) }
    var month by remember { mutableStateOf(YearMonth.now()) }
    var logs by remember { mutableStateOf<List<PeriodLog>>(emptyList()) }
    var predictedPeriod by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    var ovulationWindow by remember { mutableStateOf<Pair<LocalDate, LocalDate>?>(null) }
    var analytics by remember { mutableStateOf(CalendarAnalytics()) }

    LaunchedEffect(month) {
        val loadedLogs = repo.getAllLogs()
        val loadedPredictedPeriod = repo.predictNextPeriodRange()
        val loadedOvulationWindow = repo.predictOvulationWindow()

        logs = loadedLogs
        predictedPeriod = loadedPredictedPeriod
        ovulationWindow = loadedOvulationWindow
        analytics = buildMonthAnalytics(month, loadedLogs, loadedPredictedPeriod, loadedOvulationWindow)
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
        AnalyticsSummary(analytics = analytics)
        CalendarGrid(
            month = month,
            logs = logs,
            predictedPeriod = predictedPeriod,
            ovulationWindow = ovulationWindow
        )
    }
}

@Composable
private fun AnalyticsSummary(analytics: CalendarAnalytics) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp), modifier = Modifier.fillMaxWidth()) {
        AnalyticsPill("Logged", analytics.loggedDays, Modifier.weight(1f))
        AnalyticsPill("Predicted", analytics.predictedDays, Modifier.weight(1f))
        AnalyticsPill("Fertile", analytics.ovulationDays, Modifier.weight(1f))
    }
    Text(
        text = "Entries this month: ${analytics.periodEntries}",
        style = MaterialTheme.typography.bodySmall
    )
}

@Composable
private fun AnalyticsPill(label: String, value: Int, modifier: Modifier = Modifier) {
    Surface(
        modifier = modifier,
        color = MaterialTheme.colorScheme.surfaceVariant,
        shape = RoundedCornerShape(12.dp)
    ) {
        Column(modifier = Modifier.padding(10.dp), verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(text = label, style = MaterialTheme.typography.labelSmall)
            Text(text = value.toString(), style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        }
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
    val today = date == LocalDate.now()

    val background = when {
        logged -> MaterialTheme.colorScheme.primary.copy(alpha = STATUS_DAY_ALPHA)
        predicted -> MaterialTheme.colorScheme.secondary.copy(alpha = STATUS_DAY_ALPHA)
        ovulation -> MaterialTheme.colorScheme.tertiary.copy(alpha = STATUS_DAY_ALPHA)
        else -> MaterialTheme.colorScheme.surfaceVariant.copy(alpha = DEFAULT_DAY_ALPHA)
    }
    val foreground = MaterialTheme.colorScheme.onSurface
    val markers = listOf(
        logged to MaterialTheme.colorScheme.primary,
        predicted to MaterialTheme.colorScheme.secondary,
        ovulation to MaterialTheme.colorScheme.tertiary
    ).filter { it.first }.map { it.second }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .background(background, shape = RoundedCornerShape(8.dp))
            .then(
                if (today) {
                    Modifier.border(1.dp, MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(vertical = 4.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = date?.dayOfMonth?.toString() ?: "",
                style = MaterialTheme.typography.bodySmall,
                color = foreground,
                textAlign = TextAlign.Center
            )
            Row(horizontalArrangement = Arrangement.spacedBy(3.dp)) {
                markers.forEach { color ->
                    Box(
                        modifier = Modifier
                            .size(MARKER_SIZE)
                            .background(color, CircleShape)
                    )
                }
            }
        }
    }
}

private fun buildMonthAnalytics(
    month: YearMonth,
    logs: List<PeriodLog>,
    predictedPeriod: Pair<LocalDate, LocalDate>?,
    ovulationWindow: Pair<LocalDate, LocalDate>?
): CalendarAnalytics {
    val monthStart = month.atDay(1)
    val monthEnd = month.atEndOfMonth()
    val loggedDays = logs.sumOf { overlapDays(it.start(), it.end(), monthStart, monthEnd) }
    val predictedDays = predictedPeriod?.let { overlapDays(it.first, it.second, monthStart, monthEnd) } ?: 0
    val ovulationDays = ovulationWindow?.let { overlapDays(it.first, it.second, monthStart, monthEnd) } ?: 0
    val periodEntries = logs.count { it.start() <= monthEnd && it.end() >= monthStart }

    return CalendarAnalytics(
        loggedDays = loggedDays,
        predictedDays = predictedDays,
        ovulationDays = ovulationDays,
        periodEntries = periodEntries
    )
}

private fun overlapDays(
    start: LocalDate,
    end: LocalDate,
    monthStart: LocalDate,
    monthEnd: LocalDate
): Int {
    if (end < monthStart || start > monthEnd) return 0
    val effectiveStart = if (start < monthStart) monthStart else start
    val effectiveEnd = if (end > monthEnd) monthEnd else end
    return java.time.temporal.ChronoUnit.DAYS.between(effectiveStart, effectiveEnd).toInt() + 1
}
