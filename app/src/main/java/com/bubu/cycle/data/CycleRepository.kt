package com.bubu.cycle.data

import java.time.LocalDate
import java.time.temporal.ChronoUnit

class CycleRepository(private val dao: PeriodDao) {
    suspend fun getAllLogs(): List<PeriodLog> = dao.getAll()

    suspend fun deleteLog(log: PeriodLog) {
        dao.delete(log)
    }

    suspend fun addLog(startDate: String, endDate: String) {
        dao.insert(PeriodLog(startDate = startDate, endDate = endDate))
    }

    suspend fun latestLog(): PeriodLog? {
        return dao.getAll().maxByOrNull { it.startDate }
    }

    suspend fun averageCycleLength(): Long? {
        val logs = dao.getAll().sortedBy { it.startDate }
        if (logs.size < 2) return null
        val diffs = logs.zipWithNext { a, b -> ChronoUnit.DAYS.between(a.start(), b.start()) }
        return diffs.average().toLong()
    }

    suspend fun averagePeriodLength(): Long? {
        val logs = dao.getAll()
        if (logs.isEmpty()) return null
        val durations = logs.map { ChronoUnit.DAYS.between(it.start(), it.end()) + 1 }
        return durations.average().toLong()
    }

    suspend fun predictNextPeriodStart(): LocalDate? {
        val avg = averageCycleLength() ?: return null
        val lastStart = latestLog()?.start() ?: return null
        return lastStart.plusDays(avg)
    }

    suspend fun predictNextPeriodRange(): Pair<LocalDate, LocalDate>? {
        val avgCycle = averageCycleLength() ?: return null
        val avgPeriod = averagePeriodLength() ?: 5L
        val lastStart = latestLog()?.start() ?: return null
        val nextStart = lastStart.plusDays(avgCycle)
        val nextEnd = nextStart.plusDays(avgPeriod - 1)
        return nextStart to nextEnd
    }

    suspend fun predictOvulationWindow(): Pair<LocalDate, LocalDate>? {
        val nextPeriod = predictNextPeriodStart() ?: return null
        val ovulation = nextPeriod.minusDays(14)
        return ovulation.minusDays(2) to ovulation.plusDays(2)
    }
}
