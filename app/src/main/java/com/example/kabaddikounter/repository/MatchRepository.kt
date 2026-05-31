package com.example.kabaddikounter.repository

import android.content.Context
import com.example.kabaddikounter.data.Match
import com.example.kabaddikounter.data.SubscribeRequest
import com.example.kabaddikounter.service.LiveScoreForegroundService
import com.example.kabaddikounter.service.RetrofitClient

class MatchRepository(private val context: Context) {
    private val api = RetrofitClient.apiService

    suspend fun getMatches(): Result<List<Match>> = try {
        val res = api.getMatches()
        if (res.isSuccessful) Result.success(res.body() ?: emptyList())
        else Result.failure(Exception("Gagal memuat: ${res.code()}"))
    } catch (e: Exception) {
        Result.failure(e)
    }
    suspend fun subscribeToMatch(matchId: String, fcmToken: String): Result<Unit> = runCatching {
        api.subscribeToMatch(matchId, SubscribeRequest(fcmToken))
    }

    fun updateScore(match: Match) {
        LiveScoreForegroundService.start(context, match.team_a, match.team_b, match.score_a, match.score_b)
    }

    fun subscribe(matchId: String) {
        // TODO: panggil ApiService POST /subscribe
    }

    fun stop() {
        LiveScoreForegroundService.stop(context)
    }
}