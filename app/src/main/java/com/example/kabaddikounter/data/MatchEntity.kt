package com.example.kabaddikounter.data;

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val team_a: String,
    val team_b: String,
    val score_a: Int,
    val score_b: Int,
    val status: String = "LIVE",
    val timestamp: Long = System.currentTimeMillis()
){
    val winner: String
        get() = when {
            score_a > score_b -> team_a
            score_b > score_a -> team_b
            else -> "Seri"
        }
}