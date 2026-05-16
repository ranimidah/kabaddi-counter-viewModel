package com.example.kabaddikounter.data

data class Match (
    val id: String,
    val teamA: String,
    val teamB: String,
    val scoreA: Int,
    val scoreB: Int,
    val status: String
)

data class SubscribeRequest(val fcmToken: String)