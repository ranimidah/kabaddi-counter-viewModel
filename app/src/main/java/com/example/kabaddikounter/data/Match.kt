package com.example.kabaddikounter.data

data class Match (
    val id: Int = 1,
    val team_a: String = "",
    val team_b: String = "",
    val score_a: Int = 0,
    val score_b: Int = 0,
    val match_time: String = "",
    val status: String = "LIVE"  // "LIVE" atau "END"
)
data class MatchUpdate (
    val id: Int = 1,
    val team : String = "",
    val points: Int = 0,
    val score_a: Int = 0,
    val score_b: Int = 0,
    val time: String = "",
){
    val description: String
        get() = "$team +$points poin — Skor: $score_a : $score_b ($time)"
}
