package com.example.kabaddikounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface ScoreLogDao {
    @Insert
    suspend fun insertLog(log: ScoreLogEntity)

    @Query("SELECT * FROM score_logs ORDER BY timestamp DESC")
    fun getAllLogs(): Flow<List<ScoreLogEntity>>

    @Query("SELECT * FROM score_logs WHERE matchId = :matchId ORDER BY timestamp ASC")
    suspend fun getLogsByMatch(matchId: Int): List<ScoreLogEntity>
}