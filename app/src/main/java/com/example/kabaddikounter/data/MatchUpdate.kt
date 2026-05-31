package com.example.kabaddikounter.data

data class MatchUpdate(
    val id: String = "",
    val description: String = "",
    val timestamp: Long = System.currentTimeMillis()
)