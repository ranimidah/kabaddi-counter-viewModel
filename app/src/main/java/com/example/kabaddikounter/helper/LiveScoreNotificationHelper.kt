package com.example.kabaddikounter.helper

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import com.example.kabaddikounter.MainActivity
import com.example.kabaddikounter.R

object LiveScoreNotificationHelper {

    private const val CHANNEL_ID = "live_score_channel"
    private const val CHANNEL_NAME = "Live Score"
    private const val NOTIFICATION_ID = 1001

    fun createChannel(context: Context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                CHANNEL_NAME,
                NotificationManager.IMPORTANCE_DEFAULT // LOW agar tidak bunyi tiap update
            ).apply {
                description = "Menampilkan skor pertandingan secara live"
                enableVibration(true)
            }
            val manager = context.getSystemService(NotificationManager::class.java)
            manager?.createNotificationChannel(channel)
        }
    }

    fun showLiveScore(
        context: Context,
        teamA: String,
        teamB: String,
        scoreA: Int,
        scoreB: Int,
        matchId: Int = 0
    ) {
        // PendingIntent → buka MainActivity saat notifikasi diklik
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("matchId", matchId)
            putExtra("navigateTo", "detailMatchFragment")
        }
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val teamALabel = teamA.ifBlank { "Tim A" }
        val teamBLabel = teamB.ifBlank { "Tim B" }

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_save) // ganti dengan ikon scorecard jika ada
            .setContentTitle("⚡ Live Score Kabaddi")
            .setContentText("$teamALabel $scoreA — $scoreB $teamBLabel")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText(
                        "$teamALabel vs $teamBLabel\n" +
                                "Skor: $scoreA  —  $scoreB\n" +
                                if (scoreA > scoreB) "$teamALabel sedang unggul!"
                                else if (scoreB > scoreA) "$teamBLabel sedang unggul!"
                                else "Skor seri!"
                    )
            )
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setOngoing(true)          // tidak bisa di-swipe hilang
            .setOnlyAlertOnce(false)    // tidak bunyi/getar di setiap update
            .setSilent(false)
            .setContentIntent(pendingIntent)
            .setAutoCancel(false)
            .build()

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(
                    context,
                    Manifest.permission.POST_NOTIFICATIONS
                ) != PackageManager.PERMISSION_GRANTED
            ) return // ← tidak ada permission, batalkan
        }

        NotificationManagerCompat.from(context).notify(NOTIFICATION_ID, notification)
    }

    fun cancelNotification(context: Context) {
        NotificationManagerCompat.from(context).cancel(NOTIFICATION_ID)
    }
}