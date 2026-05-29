package com.example.kabaddikounter.data

import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Match(
    val id: String,
    val teamA: String,
    val teamB: String,
    val scoreA: Int,
    val scoreB: Int,
    val status: String
) : Parcelable

data class SubscribeRequest(val fcmToken: String)
