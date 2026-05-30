package com.example.kabaddikounter.data

data class Match (
    val id: String = "",
    val teamA: String = "",
    val teamB: String = "",
    val scoreA: Int = 0,
    val scoreB: Int = 0,
    val status: String = "LIVE"  // "LIVE" atau "END"
)

data class SubscribeRequest(val fcmToken: String)