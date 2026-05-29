package com.example.kabaddikounter

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

object LiveScoreNotificationHelper {

    const val CHANNEL_ID = "live_score_channel"
    private const val CHANNEL_NAME = "Live Score"
    const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_LOW // LOW agar tidak bunyi tiap update
            ).apply {
                description = "Menampilkan skor pertandingan secara live"
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager.createNotificationChannel(channel)
        }
    }

    fun buildNotification(
        context: Context,
        teamA: String,
        teamB: String,
        scoreA: Int,
        scoreB: Int
    ): Notification {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val teamALabel = teamA.ifBlank { "Tim A" }
        val teamBLabel = teamB.ifBlank { "Tim B" }
        val statusText = when {
            scoreA > scoreB -> "$teamALabel sedang unggul!"
            scoreB > scoreA -> "$teamBLabel sedang unggul!"
            else -> "Skor seri!"
        }

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_save)
            .setContentTitle("⚡ Live Score Kabaddi")
            .setContentText("$teamALabel $scoreA — $scoreB $teamBLabel")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("$teamALabel vs $teamBLabel\nSkor: $scoreA  —  $scoreB\n$statusText")
            )
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()
    }

    fun showLiveScore(
        context: Context,
        teamA: String,
        teamB: String,
        scoreA: Int,
        scoreB: Int
    ) {
        NotificationManagerCompat.from(context)
            .notify(NOTIFICATION_ID, buildNotification(context, teamA, teamB, scoreA, scoreB))
    }

    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}