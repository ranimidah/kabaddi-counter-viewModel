package com.example.kabaddikounter

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.kabaddikounter.viewModels.LiveScoreViewModel
import com.example.kabaddikounter.viewModels.ScoreUpdate
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage

class KabaddiFirebaseMessagingService : FirebaseMessagingService() {

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val data = remoteMessage.data
        val scoreA = data["scoreA"]?.toIntOrNull() ?: 0
        val scoreB = data["scoreB"]?.toIntOrNull() ?: 0
        val scoringTeam = data["scoringTeam"] ?: ""
        val status = data["status"] ?: "LIVE"

        // Push update into LiveScoreViewModel via companion object flow
        LiveScoreViewModel.emitScoreUpdate(ScoreUpdate(scoreA, scoreB, scoringTeam, status))

        // Show Android notification
        showNotification(scoringTeam, scoreA, scoreB, status)
    }

    private fun showNotification(scoringTeam: String, scoreA: Int, scoreB: Int, status: String) {
        createChannel()

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val (title, message) = if (status == "FINISHED") {
            "Pertandingan Berakhir" to "Skor akhir: $scoreA - $scoreB"
        } else {
            "⚡ Skor Diperbarui!" to "$scoringTeam mencetak poin! Skor: $scoreA - $scoreB"
        }

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_save)
            .setContentTitle(title)
            .setContentText(message)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        NotificationManagerCompat.from(this).notify(NOTIFICATION_ID, notification)
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Score Updates",
                NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "Notifikasi perubahan skor pertandingan" }
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    companion object {
        private const val CHANNEL_ID = "fcm_score_updates"
        private const val NOTIFICATION_ID = 1002
    }
}
