package com.example.kabaddikounter.service

import android.content.Intent
import com.example.kabaddikounter.helper.LiveScoreNotificationHelper
import com.example.kabaddikounter.repository.MatchRepository
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class FirebaseMessagingService : FirebaseMessagingService() {

    private val repository by lazy {
        val api = RetrofitClient.apiService
        MatchRepository(this)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val data = message.data
        val matchId = data["matchId"] ?: return
        val teamA = data["team_a"] ?: ""
        val teamB = data["team_b"] ?: ""
        val scoreA = data["score_a"]?.toIntOrNull() ?: 0
        val scoreB = data["score_b"]?.toIntOrNull() ?: 0
        val status = data["status"] ?: "LIVE"

        // Tampilkan notifikasi perubahan skor
        LiveScoreNotificationHelper.showLiveScore(
            context = this,
            scoreA = scoreA,
            scoreB = scoreB,
            teamA = teamA,
            teamB = teamB,
            matchId = matchId.toIntOrNull() ?: 0
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

    override fun onNewToken(token: String) {
        CoroutineScope(Dispatchers.IO).launch {
            val prefs = getSharedPreferences("fcm", MODE_PRIVATE)
            val oldToken = prefs.getString("token", null)

            if (!oldToken.isNullOrEmpty()) {
                // Ada token lama → kemungkinan sudah subscribe sesuatu → update di server
                repository.updateFcmToken(oldToken, token)
            }

            // Simpan token baru
            prefs.edit().putString("token", token).apply()
        }
    }
}