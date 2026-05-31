package com.example.kabaddikounter.service

data class ScoreLogRequest(
    val match_id: Int? = null,
    val team: String,       // "team_a" atau "team_b"
    val points: Int,
    val score_a: Int,
    val score_b: Int
)

data class ScoreLogResponse(
    val success: Boolean
)