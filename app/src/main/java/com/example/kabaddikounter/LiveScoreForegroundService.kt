package com.example.kabaddikounter

import android.app.Service
import android.content.Intent
import android.os.IBinder
import com.example.kabaddikounter.viewModels.LiveScoreViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class LiveScoreForegroundService : Service() {

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        isRunning = true

        LiveScoreNotificationHelper.createChannel(this)

        val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
        val teamA = prefs.getString(KEY_TEAM_A, "Tim A") ?: "Tim A"
        val teamB = prefs.getString(KEY_TEAM_B, "Tim B") ?: "Tim B"

        startForeground(
            LiveScoreNotificationHelper.NOTIFICATION_ID,
            LiveScoreNotificationHelper.buildNotification(this, teamA, teamB, 0, 0)
        )

        collectScoreUpdates()
        broadcastUiUpdate()
    }

    private fun collectScoreUpdates() {
        serviceScope.launch {
            LiveScoreViewModel.scoreUpdateFlow.collect { update ->
                val prefs = getSharedPreferences(PREFS_NAME, MODE_PRIVATE)
                val teamA = prefs.getString(KEY_TEAM_A, "Tim A") ?: "Tim A"
                val teamB = prefs.getString(KEY_TEAM_B, "Tim B") ?: "Tim B"

                LiveScoreNotificationHelper.showLiveScore(
                    this@LiveScoreForegroundService,
                    teamA, teamB, update.scoreA, update.scoreB
                )

                if (update.status == "FINISHED") stopSelf()
            }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int = START_STICKY

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        super.onDestroy()
        isRunning = false
        serviceScope.cancel()
        LiveScoreNotificationHelper.cancelNotification(this)
        broadcastUiUpdate()
    }

    private fun broadcastUiUpdate() {
        sendBroadcast(
            Intent(this, ForegroundServiceWidget::class.java).apply {
                action = ForegroundServiceWidget.ACTION_UPDATE_UI
            }
        )
    }

    companion object {
        var isRunning: Boolean = false
            private set

        const val PREFS_NAME = "kabaddi_live_score_prefs"
        const val KEY_TEAM_A = "team_a_name"
        const val KEY_TEAM_B = "team_b_name"
    }
}
