package com.example.kabaddikounter.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface MatchDao {
    @Insert
    suspend fun insertMatch(match: MatchEntity)

    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    fun getAllMatches(): Flow<List<MatchEntity>>

    @Query("SELECT * FROM matches ORDER BY timestamp DESC")
    suspend fun getAllMatchesOnce(): List<MatchEntity>

    @Query("SELECT * FROM matches ORDER BY timestamp DESC LIMIT 1")
    suspend fun getLastMatch(): MatchEntity?

    @Query("UPDATE matches SET score_a = :scoreA, score_b = :scoreB WHERE id = :id")
    suspend fun updateScore(id: Int, scoreA: Int, scoreB: Int)

    @Query("UPDATE matches SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Int, status: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertOrReplace(match: MatchEntity)
}