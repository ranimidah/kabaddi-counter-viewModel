package com.example.kabaddikounter.data

data class Match (
    val id: String = "",
    val team_a: String = "",
    val team_b: String = "",
    val score_a: Int = 0,
    val score_b: Int = 0,
    val match_time: String = "",
    val status: String = "LIVE"  // "LIVE" atau "END"
)

data class SubscribeRequest(val fcmToken: String)