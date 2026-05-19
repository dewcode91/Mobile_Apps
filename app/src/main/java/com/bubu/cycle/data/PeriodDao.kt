package com.bubu.cycle.data

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query

@Dao
interface PeriodDao {
    @Query("SELECT * FROM period_logs ORDER BY start_date DESC")
    suspend fun getAll(): List<PeriodLog>

    @Insert
    suspend fun insert(log: PeriodLog)

    @Delete
    suspend fun delete(log: PeriodLog)
}
