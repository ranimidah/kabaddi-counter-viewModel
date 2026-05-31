package com.example.kabaddikounter.repository

import android.content.Context
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.service.RetrofitClient
import com.example.kabaddikounter.data.SubscribeRequest
import com.example.kabaddikounter.service.LiveScoreForegroundService

class MatchRepository(private val context: Context) {
    private val api = RetrofitClient.instance

    suspend fun getMatches(): Result<List<Match>> = runCatching {
        api.getMatches()
    }
    suspend fun subscribeToMatch(matchId: String, fcmToken: String): Result<Unit> = runCatching {
        api.subscribeToMatch(matchId, SubscribeRequest(fcmToken))
    }

    fun updateScore(match: Match) {
        LiveScoreForegroundService.start(context, match.teamA, match.teamB, match.scoreA, match.scoreB)
    }

    fun subscribe(matchId: String) {
        // TODO: panggil ApiService POST /subscribe
    }

    fun stop() {
        LiveScoreForegroundService.stop(context)
    }
}