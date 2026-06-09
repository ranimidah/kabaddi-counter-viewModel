package com.example.kabaddikounter.service

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.kabaddikounter.MainActivity
import com.example.kabaddikounter.R

class LiveScoreForegroundService : Service() {

    companion object {
        var isRunning: Boolean = false
            private set
        private const val NOTIF_ID = 2001
        private const val CHANNEL_ID = "foreground_live_score"

        fun start(context: Context, teamA: String, teamB: String, scoreA: Int, scoreB: Int) {
            val intent = Intent(context, LiveScoreForegroundService::class.java).apply {
                putExtra("teamA", teamA); putExtra("teamB", teamB)
                putExtra("scoreA", scoreA); putExtra("scoreB", scoreB)
            }
            context.startForegroundService(intent)
        }

        fun stop(context: Context) {
            context.stopService(Intent(context, LiveScoreForegroundService::class.java))
        }

        fun updateScore(context: Context, teamA: String, teamB: String, scoreA: Int, scoreB: Int) {
            start(context, teamA, teamB, scoreA, scoreB)  // re-start dengan data baru
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val teamA = intent?.getStringExtra("teamA") ?: "Tim A"
        val teamB = intent?.getStringExtra("teamB") ?: "Tim B"
        val scoreA = intent?.getIntExtra("scoreA", 0) ?: 0
        val scoreB = intent?.getIntExtra("scoreB", 0) ?: 0

        createChannel()
        startForeground(NOTIF_ID, buildNotification(teamA, teamB, scoreA, scoreB))
        return START_STICKY
    }

    private fun buildNotification(teamA: String, teamB: String, scoreA: Int, scoreB: Int): Notification {
        val intent = PendingIntent.getActivity(
            this, 0, Intent(this, MainActivity::class.java),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_save)
            .setContentTitle("🔴 LIVE: $teamA vs $teamB")
            .setContentText("$scoreA — $scoreB")
            .setOngoing(true)
            .setSilent(true)
            .setContentIntent(intent)
            .build()
    }

    private fun createChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID, "Live Score Foreground",
                NotificationManager.IMPORTANCE_LOW
            )
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onBind(intent: Intent?) = null
}