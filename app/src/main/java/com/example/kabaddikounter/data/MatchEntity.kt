package com.example.kabaddikounter.data;

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "matches")
data class MatchEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val title: String,
    val teamAName: String,
    val teamBName: String,
    val scoreA: Int,
    val scoreB: Int,
    val timestamp: Long = System.currentTimeMillis()
){
    val winner: String
        get() = when {
            scoreA > scoreB -> teamAName
            scoreB > scoreA -> teamBName
            else -> "Seri"
        }
}