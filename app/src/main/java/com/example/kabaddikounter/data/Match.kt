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