package com.example.kabaddikounter.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "score_logs")
data class ScoreLogEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val matchId: Int? = null,       // null jika belum disimpan sebagai match
    val team: String,               // "team_a" atau "team_b"
    val points: Int,                // 1 atau 2
    val scoreA: Int,                // skor A setelah perubahan
    val scoreB: Int,                // skor B setelah perubahan
    val timestamp: Long = System.currentTimeMillis()
)