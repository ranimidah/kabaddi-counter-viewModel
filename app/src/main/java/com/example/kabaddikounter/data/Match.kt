package com.example.kabaddikounter.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Match (
    val id: Int = 1,
    val team_a: String = "",
    val team_b: String = "",
    val score_a: Int = 0,
    val score_b: Int = 0,
    val match_time: String = "",
    val status: String = "LIVE"  // "LIVE" atau "END"
) : Parcelable


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

data class SubscribeRequest(val fcmToken: String)
