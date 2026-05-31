package com.example.kabaddikounter.service

import android.content.Intent
import com.example.kabaddikounter.helper.LiveScoreNotificationHelper
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class FirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(message: RemoteMessage) {
        val data = message.data
        val matchId = data["matchId"] ?: return
        val teamA = data["teamA"] ?: ""
        val teamB = data["teamB"] ?: ""
        val scoreA = data["scoreA"]?.toIntOrNull() ?: 0
        val scoreB = data["scoreB"]?.toIntOrNull() ?: 0
        val status = data["status"] ?: "LIVE"

        // Tampilkan notifikasi skor berubah
        LiveScoreNotificationHelper.showLiveScore(
            context = this,
            scoreA = scoreA,
            scoreB = scoreB,
            teamA = teamA,
            teamB = teamB
        )

        if (status == "END") {
            LiveScoreForegroundService.stop(this)       // pertandingan selesai → matikan
        } else {
            LiveScoreForegroundService.updateScore(     // masih live → update skor
                this, teamA, teamB, scoreA, scoreB
            )
        }

        // Broadcast ke Activity agar ViewModel update
        sendBroadcast(Intent("SCORE_UPDATE").apply {
            putExtra("matchId", matchId)
            putExtra("scoreA", scoreA)
            putExtra("scoreB", scoreB)
            putExtra("status", status)
        })
    }
}